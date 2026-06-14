package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "fi.dy.masa.litematica.world.WorldSchematic")
public abstract class WorldSchematicMixin {
    @Shadow
    public abstract boolean isOutsideBuildHeight(BlockPos pos);

    @Shadow
    public abstract LevelChunk getChunkAt(BlockPos pos);

    @Unique
    public BlockState getBlockState(BlockPos pos) {
        if (!BoxLayerController.shouldRender(pos) || this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return this.getChunkAt(pos).getBlockState(pos);
    }
}
