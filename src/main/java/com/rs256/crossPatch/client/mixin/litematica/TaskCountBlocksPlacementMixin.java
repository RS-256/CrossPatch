package com.rs256.crossPatch.client.mixin.litematica;

import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fi.dy.masa.litematica.scheduler.tasks.TaskCountBlocksPlacement")
public class TaskCountBlocksPlacementMixin {
    @Redirect(
            method = "countAtPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/world/WorldSchematic;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    remap = false
            ),
            remap = false
    )
    private BlockState crosspatch$getUnfilteredSchematicStateForMaterialList(
            WorldSchematic world,
            BlockPos pos
    ) {
        return world.getChunkAt(pos).getBlockState(pos);
    }
}
