package com.rs256.crossPatch.client.config;

import fi.dy.masa.malilib.config.IConfigBase;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class ConfigQuery {
    private ConfigQuery() {
    }

    /**
     * The registry entry backing a given config option, or {@code null} when the
     * config is not registered. Identity comparison: configs are singletons.
     */
    @Nullable
    public static TaggedConfig entryOf(IConfigBase config) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.config() == config)
                .findFirst()
                .orElse(null);
    }

    /**
     * The translation-key group an entry's keys live under, e.g. {@code litematica}
     * or {@code hotkeys}. Hotkeys share a single {@code hotkeys} group regardless of
     * which mod they patch, matching how the i18n keys are laid out.
     */
    public static String groupOf(TaggedConfig entry) {
        if (entry.has(ConfigTag.HOTKEY)) {
            return "hotkeys";
        }
        if (entry.has(ConfigTag.CROSSPATCH)) {
            return "crosspatch";
        }
        if (entry.has(ConfigTag.LITEMATICA)) {
            return "litematica";
        }
        if (entry.has(ConfigTag.ITEMSCROLLER)) {
            return "itemscroller";
        }
        if (entry.has(ConfigTag.TWEAKERMORE)) {
            return "tweakermore";
        }

        return "";
    }

    /**
     * All options belonging to a single mod group, saved under that group's
     * category (named after its config class). Includes internal options that
     * are not shown in the normal config GUI.
     */
    public static List<IConfigBase> optionsFor(ConfigTag modTag) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(ConfigTag.OPTION))
                .filter(entry -> entry.has(modTag))
                .map(TaggedConfig::config)
                .toList();
    }

    /**
     * All hotkeys to be saved under "Hotkeys".
     */
    public static List<IConfigBase> allHotkeys() {
        return collectByTag(ConfigTag.HOTKEY);
    }

    /**
     * All entries visible in the normal config GUI.
     */
    public static List<IConfigBase> viewableAll() {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(ConfigTag.VIEWABLE))
                .sorted(ConfigQuery::compareOptionThenHotkey)
                .map(TaggedConfig::config)
                .toList();
    }

    /**
     * Visible entries for a specific mod tab.
     * Example: LITEMATICA tab shows VIEWABLE + LITEMATICA entries.
     */
    public static List<IConfigBase> viewableFor(ConfigTag modTag) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(ConfigTag.VIEWABLE))
                .filter(entry -> entry.has(modTag))
                .sorted(ConfigQuery::compareOptionThenHotkey)
                .map(TaggedConfig::config)
                .toList();
    }

    private static List<IConfigBase> collectByTag(ConfigTag tag) {
        return ConfigRegistry.ENTRIES.stream()
                .filter(entry -> entry.has(tag))
                .map(TaggedConfig::config)
                .toList();
    }

    private static int compareOptionThenHotkey(TaggedConfig a, TaggedConfig b) {
        int typeCompare = Integer.compare(typeOrder(a), typeOrder(b));

        if (typeCompare != 0) {
            return typeCompare;
        }

        return a.config().getName().compareToIgnoreCase(b.config().getName());
    }

    private static int typeOrder(TaggedConfig entry) {
        if (entry.has(ConfigTag.OPTION)) {
            return 0;
        }

        if (entry.has(ConfigTag.HOTKEY)) {
            return 1;
        }

        return 2;
    }
}
