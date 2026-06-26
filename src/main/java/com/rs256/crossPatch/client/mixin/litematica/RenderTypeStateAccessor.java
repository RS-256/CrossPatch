package com.rs256.crossPatch.client.mixin.litematica;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link RenderType}'s private {@code state} so the schematic entity
 * translucency feature can read the source render type's texture binding and build
 * a matching translucent variant.
 *
 * @see com.rs256.crossPatch.client.litematica.render.SchematicTranslucency
 */
@Mixin(RenderType.class)
public interface RenderTypeStateAccessor {
    @Accessor("state")
    RenderSetup crosspatch$getState();
}
