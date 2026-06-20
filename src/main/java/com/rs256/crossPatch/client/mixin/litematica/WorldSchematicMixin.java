package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Single choke point for hiding box-layer blocks from every <em>world-level</em> read of the
 * schematic.
 *
 * <p>{@code WorldSchematic} does not declare {@code getBlockState}; it inherits {@code Level}'s,
 * which routes through {@code getChunkAt(pos).getBlockState(pos)} (i.e. the raw
 * {@code ChunkSchematic}). By overriding {@code getBlockState} here we make every consumer that
 * reads through the schematic world facade — Litematica's pick-block ray traces, the schematic
 * overlay renderer, easy place, schematic editing, TweakerMore's auto-pick, etc. — see box-layer
 * hidden positions as air. This is what fixes pick block tracing through (and picking) hidden
 * schematic blocks.</p>
 *
 * <p>Reads that must see the <em>full</em> schematic regardless of the box layer deliberately bypass
 * this facade by going straight to the chunk ({@code getChunkAt(pos).getBlockState(pos)}):
 * {@code TaskCountBlocksPlacementMixin} (material list / block counting) and
 * {@code SchematicVerifierMixin} (verifier). That keeps the {@code ALL} list/verifier mode whole
 * while the {@code RENDER_LAYERS} mode stays scoped to the visible region via their own position
 * filters. Rendering is unaffected here: it reads the raw chunk through {@code ChunkCacheSchematic},
 * which has its own box-layer filter ({@code ChunkCacheSchematicMixin}).</p>
 *
 * <p>Note: this is a plain (non-{@code @Unique}) override so it actually replaces the inherited
 * method. The previous {@code @Unique} version never overrode anything and was dead code, which is
 * why the choke point did not exist.</p>
 */
@Mixin(targets = "fi.dy.masa.litematica.world.WorldSchematic")
public abstract class WorldSchematicMixin {
    @Shadow
    public abstract boolean isOutsideBuildHeight(BlockPos pos);

    @Shadow
    public abstract LevelChunk getChunkAt(BlockPos pos);

    public BlockState getBlockState(BlockPos pos) {
        if (!BoxLayerController.shouldRender(pos) || this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return this.getChunkAt(pos).getBlockState(pos);
    }
}
