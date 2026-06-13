package com.rs256.crossPatch.client.mixin.tweakermore;

import com.llamalad7.mixinextras.sugar.Local;
import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Patches for TweakerMore's "auto collect material list item" feature.
 *
 * <ul>
 *     <li>{@link #crosspatch$roundUpMissingToStack} rounds the per-entry collect
 *     amount up to whole stacks.</li>
 *     <li>{@link #crosspatch$collectMatchingShulkerBoxes} pulls whole shulker boxes
 *     whose contents match a missing material out of the container before the normal
 *     loose-item collection runs.</li>
 * </ul>
 */
@Mixin(
        targets = "me.fallenbreath.tweakermore.impl.features.autoContainerProcess.processors.ContainerMaterialListItemCollector",
        remap = false
)
public class ContainerMaterialListItemCollectorMixin {
    /**
     * TweakerMore computes the amount to collect for each entry as:
     * <pre>int missing = entry.getCountMissing() * multiplier - entry.getCountAvailable();</pre>
     * We intercept the first store of {@code missing} and, when enabled, round it up
     * to the next multiple of the item's max stack size so materials are pulled in
     * whole stacks (e.g. 14 missing with a stack size of 64 becomes 64).
     */
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

    /**
     * Before TweakerMore's normal collection runs, move whole shulker boxes whose
     * contents match a still-missing material into the player inventory. Because this
     * happens before TweakerMore refreshes the available counts (and Litematica counts
     * the contents of shulker boxes carried in the player inventory), the collected
     * boxes are automatically credited and TweakerMore will not grab duplicate loose
     * items for the satisfied amount.
     */
    @Inject(method = "process", at = @At("HEAD"), remap = false)
    private void crosspatch$collectMatchingShulkerBoxes(
            LocalPlayer player,
            AbstractContainerScreen<?> containerScreen,
            List<Slot> allSlots,
            List<Slot> playerInvSlots,
            List<Slot> containerInvSlots,
            CallbackInfoReturnable<?> cir
    ) {
        if (!Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER.getBooleanValue()) {
            return;
        }

        MaterialListBase materialList = DataManager.getMaterialList();
        if (materialList == null) {
            return;
        }

        boolean singleItemOnly = Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER_SINGLE_ITEM_ONLY.getBooleanValue();

        // Refresh first so the missing amounts reflect the current player inventory.
        MaterialListUtils.updateAvailableCounts(materialList.getMaterialsAll(), player);
        List<MaterialListEntry> missingOnly = materialList.getMaterialsMissingOnly(true);
        int multiplier = materialList.getMultiplier();

        for (MaterialListEntry entry : missingOnly) {
            int missing = entry.getCountMissing() * multiplier - entry.getCountAvailable();
            if (missing <= 0) {
                continue;
            }

            ItemStack needed = entry.getStack();

            for (Slot slot : containerInvSlots) {
                ItemStack slotStack = slot.getItem();
                if (slotStack.isEmpty() || !crosspatch$isShulkerBox(slotStack)) {
                    continue;
                }
                if (!fi.dy.masa.litematica.util.InventoryUtils.doesShulkerBoxContainItem(slotStack, needed)) {
                    continue;
                }
                if (singleItemOnly && !crosspatch$isFilledWithSingleItem(slotStack, needed)) {
                    continue;
                }

                int inBox = crosspatch$countMatchingItems(slotStack, needed);

                // Shift-click the whole shulker box into the player inventory.
                fi.dy.masa.itemscroller.util.InventoryUtils.shiftClickSlot(containerScreen, slot.index);

                // The box (max stack size 1) leaves the slot only if the move succeeded.
                if (!slot.getItem().isEmpty()) {
                    // Could not move it (e.g. player inventory is full) - stop for this entry.
                    break;
                }

                missing -= inBox;
                if (missing <= 0) {
                    break;
                }
            }
        }
    }

    private static boolean crosspatch$isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * @return true when every non-empty stack stored in the box matches the needed item.
     */
    private static boolean crosspatch$isFilledWithSingleItem(ItemStack shulkerBox, ItemStack needed) {
        NonNullList<ItemStack> stored = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(shulkerBox);
        boolean hasAny = false;

        for (ItemStack boxStack : stored) {
            if (boxStack.isEmpty()) {
                continue;
            }
            if (!fi.dy.masa.itemscroller.util.InventoryUtils.areStacksEqual(needed, boxStack)) {
                return false;
            }
            hasAny = true;
        }

        return hasAny;
    }

    private static int crosspatch$countMatchingItems(ItemStack shulkerBox, ItemStack needed) {
        NonNullList<ItemStack> stored = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(shulkerBox);
        int count = 0;

        for (ItemStack boxStack : stored) {
            if (!boxStack.isEmpty()
                    && fi.dy.masa.itemscroller.util.InventoryUtils.areStacksEqual(needed, boxStack)) {
                count += boxStack.getCount();
            }
        }

        return count;
    }
}
