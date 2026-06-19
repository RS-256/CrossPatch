package com.rs256.crossPatch.client.mixin.tweakermore;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.util.data.ItemType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
 *     <li>{@link #crosspatch$collectShulkersForEntry} pulls whole shulker boxes whose
 *     contents match a missing material out of the container and folds the collected
 *     amount into TweakerMore's own per-entry tally, so the action-bar / summary output
 *     reports loose items and shulker contents together as one result.</li>
 * </ul>
 *
 * <p>When a box holds several needed materials (only possible while
 * {@link Configs.TweakerMore#AUTO_COLLECT_WITH_SHULKER_SINGLE_ITEM_ONLY} is off), the
 * box is grabbed once during the first matching entry; the amounts for the box's other
 * materials are stashed in {@link #crosspatch$shulkerCredits} so each of those materials
 * is still counted on its own result line when its entry is processed.</p>
 */
@Mixin(
        targets = "me.fallenbreath.tweakermore.impl.features.autoContainerProcess.processors.ContainerMaterialListItemCollector",
        remap = false
)
public class ContainerMaterialListItemCollectorMixin {
    /**
     * Shulker contents collected for materials other than the one currently being
     * processed, carried across the per-entry loop so they are reported on their own
     * result line. Reset at the start of every {@code process} call.
     */
    @Unique
    private Object2IntOpenHashMap<ItemType> crosspatch$shulkerCredits;

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
     * Reset the per-call credit bookkeeping before TweakerMore starts collecting.
     */
    @Inject(method = "process", at = @At("HEAD"), remap = false)
    private void crosspatch$resetShulkerCredits(
            LocalPlayer player,
            AbstractContainerScreen<?> containerScreen,
            List<Slot> allSlots,
            List<Slot> playerInvSlots,
            List<Slot> containerInvSlots,
            CallbackInfoReturnable<?> cir
    ) {
        if (this.crosspatch$shulkerCredits == null) {
            this.crosspatch$shulkerCredits = new Object2IntOpenHashMap<>();
        } else {
            this.crosspatch$shulkerCredits.clear();
        }
    }

    /**
     * Runs for every still-missing entry, right where TweakerMore initialises its
     * per-entry {@code totalTaken} counter (before the loose-item loop). When enabled,
     * we first claim any amount of this material that an earlier entry already pulled in
     * via a shared box, then move whole shulker boxes that still contain this material
     * into the player inventory. The remaining {@code missing} is decremented so the
     * loose-item loop does not double-collect, and {@code totalTaken} is seeded with the
     * box amount so TweakerMore's existing message reports loose + shulker as one result.
     */
    @ModifyVariable(
            method = "process",
            at = @At(value = "STORE", ordinal = 0),
            name = "totalTaken",
            remap = false
    )
    private int crosspatch$collectShulkersForEntry(
            int totalTaken,
            @Local MaterialListEntry entry,
            @Local(name = "missing") LocalIntRef missingRef,
            @Local(argsOnly = true) AbstractContainerScreen<?> containerScreen,
            @Local(argsOnly = true, ordinal = 2) List<Slot> containerInvSlots
    ) {
        if (!Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER.getBooleanValue()) {
            return totalTaken;
        }

        Object2IntOpenHashMap<ItemType> credits = this.crosspatch$shulkerCredits;
        ItemStack needed = entry.getStack();
        int missing = missingRef.get();
        int taken = 0;

        // 1) Claim this material's share of boxes that earlier entries already pulled in.
        if (credits != null) {
            int pending = credits.removeInt(new ItemType(needed, true, false));
            if (pending > 0) {
                taken += pending;
                missing -= pending;
            }
        }

        // 2) Pull whole boxes that still contain this material out of the container.
        if (missing > 0) {
            boolean singleItemOnly = Configs.TweakerMore.AUTO_COLLECT_WITH_SHULKER_SINGLE_ITEM_ONLY.getBooleanValue();
            boolean roundUp = Configs.TweakerMore.AUTO_COLLECT_STACK_ROUND_UP.getBooleanValue();

            for (Slot slot : containerInvSlots) {
                ItemStack slotStack = slot.getItem();
                if (slotStack.isEmpty() || !crosspatch$isShulkerBox(slotStack)) {
                    continue;
                }

                ItemStack boxCopy = slotStack.copy();
                if (!fi.dy.masa.litematica.util.InventoryUtils.doesShulkerBoxContainItem(boxCopy, needed)) {
                    continue;
                }
                if (singleItemOnly && !crosspatch$isFilledWithSingleItem(boxCopy)) {
                    continue;
                }

                int inBox = crosspatch$countMatchingItems(boxCopy, needed);

                if (!roundUp && inBox > missing) {
                    continue;
                }

                // Shift-click the whole shulker box into the player inventory.
                fi.dy.masa.itemscroller.util.InventoryUtils.shiftClickSlot(containerScreen, slot.index);

                // The box (max stack size 1) leaves the slot only if the move succeeded.
                if (!slot.getItem().isEmpty()) {
                    // Could not move it (e.g. player inventory is full) - stop for this entry.
                    break;
                }

                taken += inBox;
                missing -= inBox;

                // Stash the box's other materials so their own entries still report them.
                if (credits != null) {
                    crosspatch$recordOtherMaterials(credits, boxCopy, needed);
                }

                if (missing <= 0) {
                    break;
                }
            }
        }

        // Hand the remaining count back so TweakerMore's loose-item loop and its logged
        // "still missing" value stay correct (never report a negative remainder).
        missingRef.set(Math.max(0, missing));

        return totalTaken + taken;
    }

    @Unique
    private static boolean crosspatch$isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * @return true when the box holds a single item type and nothing else.
     */
    @Unique
    private static boolean crosspatch$isFilledWithSingleItem(ItemStack shulkerBox) {
        NonNullList<ItemStack> stored = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(shulkerBox);
        ItemStack first = null;

        for (ItemStack boxStack : stored) {
            if (boxStack.isEmpty()) {
                continue;
            }
            if (first == null) {
                first = boxStack;
            } else if (!fi.dy.masa.itemscroller.util.InventoryUtils.areStacksEqual(first, boxStack)) {
                return false;
            }
        }

        return first != null;
    }

    @Unique
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

    /**
     * Records the amounts of every stored item that does not match {@code current} so
     * the matching material entries can claim them later in the loop.
     */
    @Unique
    private static void crosspatch$recordOtherMaterials(
            Object2IntOpenHashMap<ItemType> credits,
            ItemStack shulkerBox,
            ItemStack current
    ) {
        NonNullList<ItemStack> stored = fi.dy.masa.malilib.util.InventoryUtils.getStoredItems(shulkerBox);

        for (ItemStack boxStack : stored) {
            if (boxStack.isEmpty()
                    || fi.dy.masa.itemscroller.util.InventoryUtils.areStacksEqual(current, boxStack)) {
                continue;
            }
            credits.addTo(new ItemType(boxStack, true, false), boxStack.getCount());
        }
    }
}
