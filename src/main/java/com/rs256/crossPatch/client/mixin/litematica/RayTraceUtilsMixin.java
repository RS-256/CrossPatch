package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceCalcsData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "fi.dy.masa.litematica.util.RayTraceUtils")
public class RayTraceUtilsMixin {
    @Inject(
            method = "traceFirstStep",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void crosspatch$filterBoxLayerFirstStep(
            RayTraceCalcsData data,
            Level world,
            BlockState blockState,
            FluidState fluidState,
            boolean ignoreBlockWithoutBoundingBox,
            boolean returnLastUncollidableBlock,
            boolean respectLayerRange,
            CallbackInfoReturnable<BlockHitResult> cir
    ) {
        if (respectLayerRange && !BoxLayerController.shouldRender(data.blockPos)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
            method = "traceLoopSteps",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void crosspatch$filterBoxLayerLoopStep(
            RayTraceCalcsData data,
            Level world,
            BlockState blockState,
            FluidState fluidState,
            boolean ignoreBlockWithoutBoundingBox,
            boolean returnLastUncollidableBlock,
            boolean respectLayerRange,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (respectLayerRange && !BoxLayerController.shouldRender(data.blockPos)) {
            cir.setReturnValue(false);
        }
    }
}
