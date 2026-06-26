package com.rs256.crossPatch.client.mixin.litematica;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the texture {@code location} of {@code RenderSetup.TextureBinding}. The
 * binding class is package-private, so it is targeted by name and the binding
 * instances are reached as {@code Object} and cast to this interface at runtime.
 *
 * @see com.rs256.crossPatch.client.litematica.render.SchematicTranslucency
 */
@Mixin(targets = "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding")
public interface TextureBindingAccessor {
    @Accessor("location")
    Identifier crosspatch$getLocation();
}
