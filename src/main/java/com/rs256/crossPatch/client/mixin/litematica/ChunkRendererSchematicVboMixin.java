package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.ChunkRendererSchematicVbo")
public class ChunkRendererSchematicVboMixin {
    @Redirect(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/util/LayerRange;getClampedRenderBoundingBox(Lfi/dy/masa/malilib/util/IntBoundingBox;)Lfi/dy/masa/malilib/util/IntBoundingBox;",
                    remap = false
            ),
            remap = false
    )
    private IntBoundingBox crosspatch$clipBoxLayerBeforeRenderLayerClamp(
            LayerRange range,
            IntBoundingBox box
    ) {
        IntBoundingBox clippedBox = BoxLayerController.clipBox(box);
        return clippedBox != null ? range.getClampedRenderBoundingBox(clippedBox) : null;
    }
}
