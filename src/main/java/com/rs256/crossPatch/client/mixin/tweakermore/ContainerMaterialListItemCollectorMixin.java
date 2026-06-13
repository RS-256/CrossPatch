package com.rs256.crossPatch.client.mixin.tweakermore;

import com.llamalad7.mixinextras.sugar.Local;
import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Adds a "round up to full stacks" behaviour to TweakerMore's
 * "auto collect material list item" feature.
 *
 * <p>TweakerMore computes the amount to collect for each entry as:
 * <pre>int missing = entry.getCountMissing() * multiplier - entry.getCountAvailable();</pre>
 * We intercept the first store of {@code missing} and, when enabled, round it up
 * to the next multiple of the item's max stack size so materials are pulled in
 * whole stacks (e.g. 14 missing with a stack size of 64 becomes 64).</p>
 */
@Mixin(
        targets = "me.fallenbreath.tweakermore.impl.features.autoContainerProcess.processors.ContainerMaterialListItemCollector",
        remap = false
)
public class ContainerMaterialListItemCollectorMixin {
    @ModifyVariable(
            method = "process",
            at = @At(value = "STORE", ordinal = 0),
            name = "missing",
            remap = false
    )
    private int crosspatch$roundUpMissingToStack(int missing, @Local MaterialListEntry entry) {
        if (!Configs.TweakerMore.AUTO_COLLECT_STACK_ROUND_UP.getBooleanValue()) {
            return missing;
        }
        if (missing <= 0) {
            return missing;
        }

        int stackSize = entry.getStack().getMaxStackSize();
        if (stackSize <= 1) {
            return missing;
        }

        int stacks = (missing + stackSize - 1) / stackSize;
        return stacks * stackSize;
    }
}
