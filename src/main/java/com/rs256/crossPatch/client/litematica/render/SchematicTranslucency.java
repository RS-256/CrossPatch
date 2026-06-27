package com.rs256.crossPatch.client.litematica.render;

import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.mixin.litematica.RenderSetupTexturesAccessor;
import com.rs256.crossPatch.client.mixin.litematica.RenderTypeStateAccessor;
import com.rs256.crossPatch.client.mixin.litematica.TextureBindingAccessor;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared translucency rewrite for Litematica's schematic preview, used by two
 * independent options:
 * <ul>
 *   <li>{@code renderEntitiesAsTranslucent} — the schematic's entities
 *       (armor stands, item frames, mobs…), armed by {@link #beginEntities()}.</li>
 *   <li>{@code renderAnimativeBlockAsTranslucent} — block entities rendered by a
 *       special renderer (chests, shulker boxes, ender chests, the bell's golden
 *       body…), armed by {@link #beginBlockEntities()}.</li>
 * </ul>
 *
 * <p>Both submit their geometry into the same vanilla {@code SubmitNodeCollector},
 * through {@code SubmitNodeCollection.submitModel} / {@code submitModelPart}, which
 * carry the model's {@code RenderType} (deciding whether blending happens) and an
 * ARGB tint {@code color}. While {@link #isActive()} is set (only during the
 * matching Litematica render method), {@code SubmitNodeCollectionMixin} swaps the
 * render type to a translucent-blending equivalent ({@link #translucent(RenderType)})
 * and multiplies the tint alpha by the armed opacity ({@link #modulateColor(int)}).
 * The real world is submitted earlier in the frame with the flag disarmed, so it is
 * never touched.</p>
 *
 * @see com.rs256.crossPatch.client.mixin.litematica.SubmitNodeCollectionMixin
 * @see com.rs256.crossPatch.client.mixin.litematica.WorldRendererSchematicMixin
 */
public final class SchematicTranslucency {
    /**
     * Source render type -> translucent variant. Render types are effectively
     * interned per texture, so identity keys stay small and stable. Shared by both
     * options since the rewrite is identical.
     */
    private static final Map<RenderType, RenderType> TRANSLUCENT_CACHE = new ConcurrentHashMap<>();

    /** True only while a schematic render pass with the feature on is submitting. */
    private static boolean active;

    /** Opacity applied while {@link #active}; 1.0 opaque, 0.0 invisible. */
    private static double alpha = 1.0;

    private SchematicTranslucency() {
    }

    /** Arms the rewrite for the schematic's entities, if that option is on. */
    public static void beginEntities() {
        begin(
                Configs.Litematica.RENDER_ENTITIES_AS_TRANSLUCENT.getBooleanValue(),
                Configs.Litematica.RENDER_ENTITIES_TRANSLUCENT_ALPHA.getDoubleValue()
        );
    }

    /** Arms the rewrite for the schematic's specially-rendered block entities, if that option is on. */
    public static void beginBlockEntities() {
        begin(
                Configs.Litematica.RENDER_ANIMATIVE_BLOCK_AS_TRANSLUCENT.getBooleanValue(),
                Configs.Litematica.RENDER_ANIMATIVE_BLOCK_TRANSLUCENT_ALPHA.getDoubleValue()
        );
    }

    private static void begin(boolean enabled, double opacity) {
        active = enabled;
        alpha = opacity;
    }

    /** Disarms the rewrite once the schematic render pass is done. */
    public static void end() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    /**
     * Returns a translucent-blending render type that draws the same texture as
     * {@code source}. Falls back to {@code source} unchanged when the texture cannot
     * be determined (the geometry then simply stays opaque rather than failing).
     */
    public static RenderType translucent(RenderType source) {
        if (source == null) {
            return null;
        }

        return TRANSLUCENT_CACHE.computeIfAbsent(source, SchematicTranslucency::buildTranslucent);
    }

    private static RenderType buildTranslucent(RenderType source) {
        try {
            Identifier texture = extractTexture(source);

            if (texture != null) {
                return RenderTypes.entityTranslucent(texture);
            }
        } catch (Exception ex) {
            CrossPatch.LOGGER.warn(
                    "SchematicTranslucency: could not derive a translucent render type for '{}'; leaving it opaque.",
                    source, ex
            );
        }

        return source;
    }

    private static Identifier extractTexture(RenderType source) {
        RenderSetup setup = ((RenderTypeStateAccessor) (Object) source).crosspatch$getState();

        if (setup == null) {
            return null;
        }

        Map<String, ?> bindings = ((RenderSetupTexturesAccessor) (Object) setup).crosspatch$getTextures();

        if (bindings == null || bindings.isEmpty()) {
            return null;
        }

        // Entity / block-entity render types expose their texture under "Sampler0";
        // fall back to the first binding for anything that names it differently.
        Object binding = bindings.get("Sampler0");

        if (binding == null) {
            binding = bindings.values().iterator().next();
        }

        return ((TextureBindingAccessor) binding).crosspatch$getLocation();
    }

    /**
     * Multiplies the alpha byte of an ARGB tint by the armed opacity, leaving the RGB
     * channels untouched.
     */
    public static int modulateColor(int color) {
        int sourceAlpha = (color >>> 24) & 0xFF;
        int scaledAlpha = (int) Math.round(sourceAlpha * alpha);
        scaledAlpha = Math.max(0, Math.min(255, scaledAlpha));

        return (color & 0x00FFFFFF) | (scaledAlpha << 24);
    }
}
