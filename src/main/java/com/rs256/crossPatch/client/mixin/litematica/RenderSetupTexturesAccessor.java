package com.rs256.crossPatch.client.mixin.litematica;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes {@link RenderSetup}'s package-private {@code textures} map. The map values
 * are {@code RenderSetup.TextureBinding} records (not nameable here, hence the
 * wildcard); their texture {@code location()} is read through
 * {@link TextureBindingAccessor}.
 *
 * @see com.rs256.crossPatch.client.litematica.render.SchematicTranslucency
 */
@Mixin(RenderSetup.class)
public interface RenderSetupTexturesAccessor {
    @Accessor("textures")
    Map<String, ?> crosspatch$getTextures();
}
