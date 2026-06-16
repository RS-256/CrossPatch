package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.client.config.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Extends the reach of Litematica's schematic-world pick block so the pickBlockPro
 * {@code reachOverride} applies to schematic picks too.
 *
 * <p>{@code WorldUtils.doSchematicWorldPickBlock} ray-traces the schematic world only as far
 * as {@code getValidBlockRange} (the player's normal block interaction range). When
 * {@link Configs.PickBlock#PICK_BLOCK_PRO} is enabled we widen the range passed to those
 * ray-traces to {@link Configs.PickBlock#PICK_BLOCK_PRO_REACH_OVERRIDE}, letting distant
 * schematic blocks be picked.</p>
 */
@Mixin(targets = "fi.dy.masa.litematica.util.WorldUtils")
public class WorldUtilsPickBlockMixin {
    @ModifyArg(
            method = "doSchematicWorldPickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getFurthestSchematicWorldBlockBeforeVanilla"
            ),
            index = 2,
            remap = false
    )
    private static double crosspatch$extendFurthestSchematicRange(double range) {
        return crosspatch$extendedRange(range);
    }

    @ModifyArg(
            method = "doSchematicWorldPickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getSchematicWorldTraceIfClosestNoFluids"
            ),
            index = 2,
            remap = false
    )
    private static double crosspatch$extendClosestSchematicRange(double range) {
        return crosspatch$extendedRange(range);
    }

    @Unique
    private static double crosspatch$extendedRange(double range) {
        if (!Configs.PickBlock.PICK_BLOCK_PRO.getBooleanValue()) {
            return range;
        }
        return Math.max(range, Configs.PickBlock.PICK_BLOCK_PRO_REACH_OVERRIDE.getDoubleValue());
    }
}
