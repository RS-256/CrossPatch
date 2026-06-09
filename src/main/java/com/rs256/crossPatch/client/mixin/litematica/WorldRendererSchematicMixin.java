package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.util.LayerRange;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.WorldRendererSchematic")
public class WorldRendererSchematicMixin {
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
