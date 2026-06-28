package com.rs256.crossPatch.client.config.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.Reference;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads CrossPatch translations from a <em>nested</em> JSON source bundled at
 * {@code assets/crosspatch/i18n/<lang>.json}, instead of relying on Minecraft's
 * flat {@code lang/*.json} loader (which cannot parse nested objects).
 *
 * <p>At load time the nested tree is flattened into the long dotted keys that
 * malilib and the rest of CrossPatch already expect, e.g.
 * {@code crosspatch.config.litematica.comment.boxLayerEnabled}. The authoring
 * format groups everything about one config together:
 *
 * <pre>{@code
 * "crosspatch": {
 *   "config": {
 *     "litematica": {
 *       "boxLayerEnabled": {
 *         "name":  "boxLayerEnabled",
 *         "hover": "Enable CrossPatch Box Layer ..."
 *       }
 *     }
 *   },
 *   "gui": { "title": { "configs": "CrossPatch v%s" } }
 * }
 * }</pre>
 *
 * <p>Resolution order (enforced by the client-language mixin via {@link #find}):
 * vanilla I18n first (so a resource pack shipping a real {@code lang/} file can
 * still override us), then this nested data, then the key itself as a last resort.
 */
public final class CrossPatchI18n {
    private static final String I18N_PATH = "/assets/" + Reference.MOD_ID + "/i18n/";
    private static final String FALLBACK_LANG = "en_us";

    /** {@code config.<mod>} path segment under which grouped config entries live. */
    private static final String CONFIG_NODE = "config";
    private static final String NAME_FIELD = "name";
    private static final String HOVER_FIELD = "hover";

    /** Flattened {@code key -> text} table (en_us overlaid by the active language). */
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    /** Language the {@link #TRANSLATIONS} table was last built for, or {@code null}. */
    @Nullable
    private static String loadedLanguage;

    private CrossPatchI18n() {
    }

    /**
     * (Re)load the nested translation files for the current game language, with
     * en_us as the fallback layer. Safe to call repeatedly; it is a no-op when
     * the language has not changed since the last load.
     */
    public static synchronized void reloadIfNeeded() {
        String current = currentLanguageCode();

        if (current.equals(loadedLanguage)) {
            return;
        }

        TRANSLATIONS.clear();

        // en_us first so the active language only needs to override what differs.
        loadInto(FALLBACK_LANG);

        if (!FALLBACK_LANG.equals(current)) {
            loadInto(current);
        }

        loadedLanguage = current;

        CrossPatch.LOGGER.info(
                "CrossPatchI18n: loaded {} translations for language '{}'.",
                TRANSLATIONS.size(), current);
    }

    /**
     * Raw template lookup (no {@code %s} formatting); {@code null} when the key is
     * unknown to CrossPatch. Used by the client-language mixin to fill in keys that
     * vanilla (and any resource pack) does not provide, so the resolution order is
     * vanilla/resource-pack first, then this nested data, then the raw key.
     */
    @Nullable
    public static String find(String key) {
        reloadIfNeeded();
        return TRANSLATIONS.get(key);
    }

    /**
     * Builds the display-name translation key for a config reference, as used by the
     * "see also" hover section. A bare reference like {@code "useLitematicaLayerHotkeys"}
     * is resolved within {@code defaultGroup}; a qualified {@code "litematica.boxLayerEnabled"}
     * names its own group. Returns e.g. {@code crosspatch.config.litematica.name.boxLayerEnabled}.
     */
    public static String nameKey(String ref, String defaultGroup) {
        String group;
        String cfg;

        int dot = ref.indexOf('.');
        if (dot >= 0) {
            group = ref.substring(0, dot);
            cfg = ref.substring(dot + 1);
        } else {
            group = defaultGroup;
            cfg = ref;
        }

        return Reference.MOD_ID + "." + CONFIG_NODE + "." + group + ".name." + cfg;
    }

    private static void loadInto(String langCode) {
        JsonObject root = read(langCode);

        if (root == null) {
            return;
        }

        // The file already nests everything under a top-level "crosspatch" key, so
        // top-level entries are used verbatim (no extra prefix) to avoid doubling it.
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            handle(entry.getKey(), entry.getValue());
        }
    }

    @Nullable
    private static JsonObject read(String langCode) {
        String resource = I18N_PATH + langCode + ".json";

        try (InputStream in = CrossPatchI18n.class.getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }

            JsonElement element = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));

            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception e) {
            CrossPatch.LOGGER.error("CrossPatchI18n: failed to read '{}'.", resource, e);
            return null;
        }
    }

    /**
     * Flatten one node at the absolute dotted path {@code key} into
     * {@link #TRANSLATIONS}. Objects that sit exactly at
     * {@code crosspatch.config.<mod>.<cfg>} are treated as grouped config entries
     * and expanded into malilib's {@code .name.}/{@code .comment.} key shape;
     * other objects recurse, and primitives are stored as the leaf value.
     */
    private static void handle(String key, JsonElement value) {
        if (isConfigEntryPath(key) && value.isJsonObject()) {
            expandConfigEntry(key, value.getAsJsonObject());
        } else if (value.isJsonObject()) {
            for (Map.Entry<String, JsonElement> child : value.getAsJsonObject().entrySet()) {
                handle(key + "." + child.getKey(), child.getValue());
            }
        } else if (value.isJsonPrimitive()) {
            TRANSLATIONS.put(key, value.getAsString());
        }
    }

    /** True for keys shaped exactly {@code crosspatch.config.<mod>.<cfg>}. */
    private static boolean isConfigEntryPath(String key) {
        String configPrefix = Reference.MOD_ID + "." + CONFIG_NODE + ".";

        if (!key.startsWith(configPrefix)) {
            return false;
        }

        // crosspatch . config . <mod> . <cfg>  -> 4 segments past nothing
        return key.split("\\.").length == 4;
    }

    /**
     * Expand a grouped config entry at {@code crosspatch.config.<mod>.<cfg>} into
     * the flat keys malilib resolves: {@code ...config.<mod>.name.<cfg>} and
     * {@code ...config.<mod>.comment.<cfg>}.
     */
    private static void expandConfigEntry(String entryKey, JsonObject entry) {
        String[] parts = entryKey.split("\\.");
        String mod = parts[2];
        String cfg = parts[3];
        String groupPrefix = Reference.MOD_ID + "." + CONFIG_NODE + "." + mod;

        JsonElement name = entry.get(NAME_FIELD);
        if (name != null && name.isJsonPrimitive()) {
            TRANSLATIONS.put(groupPrefix + ".name." + cfg, name.getAsString());
        }

        JsonElement hover = entry.get(HOVER_FIELD);
        if (hover != null && hover.isJsonPrimitive()) {
            TRANSLATIONS.put(groupPrefix + ".comment." + cfg, hover.getAsString());
        }
    }

    private static String currentLanguageCode() {
        Minecraft mc = Minecraft.getInstance();

        if (mc != null && mc.options != null && mc.options.languageCode != null) {
            return mc.options.languageCode;
        }

        return FALLBACK_LANG;
    }
}
