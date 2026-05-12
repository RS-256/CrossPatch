package com.rs256.crossPatch.client.mixin;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.ChunkCacheSchematic")
public class ChunkCacheSchematicMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void crosspatch$filterBoxLayer(
            BlockPos pos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!BoxLayerController.shouldRender(pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}