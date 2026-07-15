package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.util.ReplaceBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "fi.dy.masa.litematica.util.WorldPlacingUtils")
public class WorldPlacingUtilsMixin {
    /**
     * {@code placeBlocksToProtoChunk} only builds the schematic preview world
     * (the actual paste-to-world path lives in {@code SchematicPlacingUtils}),
     * so forcing {@code WITH_NON_AIR} here changes rendering only: air from a
     * later-placed placement no longer erases the blocks an earlier placement
     * already put into the chunk, which is what normally hides all but one of
     * the overlapping schematics.
     */
    @ModifyVariable(
            method = "placeBlocksToProtoChunk",
            at = @At("STORE"),
            ordinal = 0,
            remap = false
    )
    private static ReplaceBehavior crosspatch$renderOverlappingSchematics(ReplaceBehavior replace) {
        if (Configs.Litematica.RENDER_OVERLAPPING_SCHEMATICS.getBooleanValue()) {
            return ReplaceBehavior.WITH_NON_AIR;
        }

        return replace;
    }
}
