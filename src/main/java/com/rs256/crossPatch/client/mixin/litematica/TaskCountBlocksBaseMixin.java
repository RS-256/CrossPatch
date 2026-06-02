package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.materials.IMaterialList;
import fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksBase;
import fi.dy.masa.litematica.util.BlockInfoListType;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksBase")
public abstract class TaskCountBlocksBaseMixin {
    @Final
    @Shadow(remap = false)
    protected IMaterialList materialList;

    @Shadow(remap = false)
    protected abstract void countAtPosition(BlockPos pos);

    @Redirect(
            method = "countBlocksInChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskCountBlocksBase;countAtPosition(Lnet/minecraft/core/BlockPos;)V",
                    remap = false
            ),
            remap = false
    )
    private void crosspatch$countOnlyRenderedBoxLayerBlocks(
            TaskCountBlocksBase instance,
            BlockPos pos
    ) {
        if (this.materialList.getMaterialListType() != BlockInfoListType.RENDER_LAYERS
                || BoxLayerController.shouldRender(pos)) {
            this.countAtPosition(pos);
        }
    }
}
