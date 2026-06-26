package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.render.SchematicTranslucency;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Rewrites schematic geometry submissions to be translucent while Litematica is
 * submitting its schematic entities or specially-rendered block entities
 * ({@link SchematicTranslucency#isActive()}).
 *
 * <p>{@code submitModel} / {@code submitModelPart} bucket each submission by the
 * {@code RenderType} argument and carry an ARGB tint {@code color}. Swapping the
 * render type to a blending variant and lowering the tint alpha turns the schematic
 * geometry translucent without affecting the real world, which is submitted earlier
 * in the frame with the flag disarmed. The argument indices match the concrete
 * {@code SubmitNodeCollection} methods (verified against the bytecode):
 * {@code submitModel} render type = 4, color = 7; {@code submitModelPart} render
 * type = 3, color = 9.</p>
 *
 * @see SchematicTranslucency
 */
@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {
    @ModifyVariable(method = "submitModel", at = @At("HEAD"), index = 4, argsOnly = true)
    private RenderType crosspatch$translucentModelType(RenderType renderType) {
        return SchematicTranslucency.isActive() ? SchematicTranslucency.translucent(renderType) : renderType;
    }

    @ModifyVariable(method = "submitModel", at = @At("HEAD"), index = 7, argsOnly = true)
    private int crosspatch$translucentModelColor(int color) {
        return SchematicTranslucency.isActive() ? SchematicTranslucency.modulateColor(color) : color;
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"), index = 3, argsOnly = true)
    private RenderType crosspatch$translucentModelPartType(RenderType renderType) {
        return SchematicTranslucency.isActive() ? SchematicTranslucency.translucent(renderType) : renderType;
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"), index = 9, argsOnly = true)
    private int crosspatch$translucentModelPartColor(int color) {
        return SchematicTranslucency.isActive() ? SchematicTranslucency.modulateColor(color) : color;
    }
}
