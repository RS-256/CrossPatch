package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.ChunkRendererSchematicVbo")
public class ChunkRendererSchematicVboMixin {
    @Shadow(remap = false)
    protected WorldSchematic world;

    @Final
    @Shadow(remap = false)
    protected BlockPos.MutableBlockPos position;

    @Shadow(remap = false)
    protected void clear() {
    }

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

    @Inject(
            method = "setNeedsUpdate",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void crosspatch$skipBoxLayerExcludedChunkUpdate(
            boolean immediate,
            CallbackInfo ci
    ) {
        if (!this.crosspatch$intersectsBoxLayer()) {
            this.clear();
            ci.cancel();
        }
    }

    @Unique
    private boolean crosspatch$intersectsBoxLayer() {
        if (!BoxLayerController.isEnabled()) {
            return true;
        }

        int minX = this.position.getX();
        int minY = this.world.getMinY();
        int minZ = this.position.getZ();
        int maxX = minX + 15;
        int maxY = this.world.getMaxY() - 1;
        int maxZ = minZ + 15;

        return BoxLayerController.clipBox(new IntBoundingBox(minX, minY, minZ, maxX, maxY, maxZ)) != null;
    }
}
