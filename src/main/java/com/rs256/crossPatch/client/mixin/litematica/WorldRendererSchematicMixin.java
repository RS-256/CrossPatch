package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import com.rs256.crossPatch.client.litematica.render.SchematicTranslucency;
import fi.dy.masa.malilib.util.LayerRange;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.WorldRendererSchematic")
public class WorldRendererSchematicMixin {
    /**
     * Arm the translucency rewrite only while the schematic's own entities / block
     * entities are being submitted. {@code renderEntities} and
     * {@code renderBlockEntities} submit their geometry synchronously into the vanilla
     * collector, so the flag is set for exactly those submissions and cleared at every
     * return (including the early empty-list return). The two passes never overlap, so
     * each can carry its own opacity.
     *
     * @see SchematicTranslucency
     */
    @Inject(method = "renderEntities", at = @At("HEAD"), remap = false)
    private void crosspatch$beginEntityTranslucent(CallbackInfo ci) {
        SchematicTranslucency.beginEntities();
    }

    @Inject(method = "renderEntities", at = @At("RETURN"), remap = false)
    private void crosspatch$endEntityTranslucent(CallbackInfo ci) {
        SchematicTranslucency.end();
    }

    @Redirect(
            method = "prepareEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/util/LayerRange;isPositionWithinRange(III)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean crosspatch$filterEntitiesByBoxLayer(
            LayerRange layerRange,
            int x,
            int y,
            int z
    ) {
        return layerRange.isPositionWithinRange(x, y, z)
                && BoxLayerController.shouldRender(new BlockPos(x, y, z));
    }
}
