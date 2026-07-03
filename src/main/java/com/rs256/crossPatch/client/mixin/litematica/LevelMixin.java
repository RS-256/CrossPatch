package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Single choke point for hiding box-layer blocks from every <em>world-level</em> read of the
 * schematic.
 *
 * <p>{@code WorldSchematic} does not declare {@code getBlockState}; it inherits {@code Level}'s.
 * Because Mixin injectors can only target methods that actually exist in the target class'
 * bytecode, we cannot {@code @Inject}/{@code @Overwrite} {@code WorldSchematic.getBlockState}
 * directly. A plain (non-annotated) override <em>compiles</em> against the named runtime, but the
 * remapper does not rename it, so in the remapped (intermediary) build it fails to override the
 * inherited {@code Level.getBlockState} and the choke point silently disappears — the cause of the
 * "placement restriction / pick block penetrate box-layer-hidden blocks on servers" bug.</p>
 *
 * <p>Instead we inject into {@code Level.getBlockState} — a real, declared, remappable method — and
 * gate the filter to {@code WorldSchematic} instances. This keeps the interception rooted at the
 * schematic world (only {@code WorldSchematic} reads are affected) while surviving remapping in
 * every build. Every world-facade consumer — Litematica's pick-block ray traces, the schematic
 * overlay renderer, easy place, schematic editing, TweakerMore's auto-pick and placement
 * restriction — is filtered at once.</p>
 *
 * <p>The cheap {@link BoxLayerController#isEnabled()} check runs first so this adds no more than a
 * single boolean read to the (very hot) vanilla {@code getBlockState} path when the box layer is
 * off. {@code isOutsideBuildHeight} handling is left to the vanilla method body, which already
 * returns air for out-of-range positions.</p>
 *
 * <p>Reads that must see the <em>full</em> schematic regardless of the box layer deliberately bypass
 * this facade by going straight to the chunk ({@code getChunkAt(pos).getBlockState(pos)}):
 * {@code TaskCountBlocksPlacementMixin} (material list / block counting) and
 * {@code SchematicVerifierMixin} (verifier). Rendering is unaffected here: it reads the raw chunk
 * through {@code ChunkCacheSchematic}, which has its own box-layer filter
 * ({@code ChunkCacheSchematicMixin}). Do NOT filter {@code ChunkSchematic.getBlockState} directly —
 * it would also filter those bypasses and break {@code ALL}-mode counting/verifying.</p>
 */
@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void crosspatch$filterBoxLayer(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (BoxLayerController.isEnabled() && (Object) this instanceof WorldSchematic && !BoxLayerController.shouldRender(pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
