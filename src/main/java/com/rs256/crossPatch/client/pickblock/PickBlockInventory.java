package com.rs256.crossPatch.client.pickblock;

import com.rs256.crossPatch.client.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.stream.Stream;

/**
 * Client-side inventory placement for {@code pickBlockPro}.
 *
 * <p>Modelled on the standalone <a href="https://github.com/RS-256/pick-block-pro">
 * pick-block-pro</a> mod. The whole pick is resolved on the client and synced only through
 * packets that carry no server-side reach check, so it works regardless of the server:</p>
 * <ul>
 *     <li><b>Already on the hotbar</b> &rarr; just re-select the slot (the change is synced by
 *     vanilla's {@code MultiPlayerGameMode#ensureHasSentCarriedItem} each tick).</li>
 *     <li><b>In the main inventory</b> (survival or creative) &rarr; a {@code SWAP} click on the
 *     player's own inventory menu moves it onto the hotbar. Operating on your own inventory is
 *     not reach-checked.</li>
 *     <li><b>Inside a shulker box</b> &rarr; when
 *     {@link Configs.PickBlock#PICK_BLOCK_PRO_PICK_SHULKER_WITH_ITEM} is on and the item isn't
 *     loose, the shulker box holding it is brought to hand (contents untouched).</li>
 *     <li><b>Not in the inventory</b> (creative only) &rarr; create it with
 *     {@link Inventory#addAndPickItem} and sync via the creative set-slot packet.</li>
 * </ul>
 */
public final class PickBlockInventory {
    /** First hotbar slot index inside the player {@code InventoryMenu}. */
    private static final int INVENTORY_MENU_HOTBAR_START = 36;

    private PickBlockInventory() {
    }

    /**
     * Picks or places {@code item} into the player's inventory entirely client-side.
     *
     * @return {@code true} if the pick was handled (vanilla pick block should be cancelled);
     * {@code false} when nothing could be done (e.g. survival without the item).
     */
    public static boolean pickOrPlace(Minecraft mc, ItemStack item) {
        if (mc.player == null || mc.gameMode == null || item.isEmpty()) {
            return false;
        }

        Inventory inventory = mc.player.getInventory();
        boolean creative = mc.player.hasInfiniteMaterials();

        // Loose copy already somewhere in the inventory.
        int stackSlot = inventory.findSlotMatchingItem(item);
        if (stackSlot != Inventory.NOT_FOUND_INDEX) {
            return placeFromInventorySlot(mc, inventory, stackSlot);
        }

        // Not loose: in survival, optionally bring a shulker box that contains it. In creative
        // the item is conjured directly below instead, so the shulker is left alone.
        if (!creative && Configs.PickBlock.PICK_BLOCK_PRO_PICK_SHULKER_WITH_ITEM.getBooleanValue()) {
            int shulkerSlot = findShulkerContainingItem(inventory, item.getItem());
            if (shulkerSlot != Inventory.NOT_FOUND_INDEX) {
                return placeFromInventorySlot(mc, inventory, shulkerSlot);
            }
        }

        // Creative can conjure the item itself.
        if (creative) {
            // With a hotbar lock active, place the item directly into an allowed slot rather than
            // letting addAndPickItem choose its own slot.
            if (HotbarSlotLock.isRestricted()) {
                int target = HotbarSlotLock.chooseTargetSlot(inventory, Inventory.NOT_FOUND_INDEX);
                inventory.setSelectedSlot(target);
                inventory.setSelectedItem(item);
                updateCreativeSlot(mc, target);
                return true;
            }

            int freeSlotBefore = inventory.getFreeSlot();
            inventory.addAndPickItem(item);

            int selectedSlot = inventory.getSelectedSlot();
            updateCreativeSlot(mc, selectedSlot);

            // addAndPickItem may have displaced the previously held item into a free slot.
            if (freeSlotBefore != -1 && freeSlotBefore != selectedSlot) {
                updateCreativeSlot(mc, freeSlotBefore);
            }
            return true;
        }

        return false;
    }

    /**
     * Brings the shulker box that contains a matching item to hand, without touching its
     * contents. Used for picks within normal reach (where vanilla would otherwise handle the
     * pick) so the caller is expected to have already checked the item isn't loose.
     *
     * @return {@code true} if a containing shulker was found and brought to hand.
     */
    public static boolean bringContainingShulker(Minecraft mc, ItemStack item) {
        if (mc.player == null || mc.gameMode == null || item.isEmpty()) {
            return false;
        }

        Inventory inventory = mc.player.getInventory();
        int shulkerSlot = findShulkerContainingItem(inventory, item.getItem());
        if (shulkerSlot == Inventory.NOT_FOUND_INDEX) {
            return false;
        }
        return placeFromInventorySlot(mc, inventory, shulkerSlot);
    }

    /**
     * Brings the item at an existing inventory slot to hand, honouring the hotbar lock: it ends up
     * in an allowed hotbar slot (re-selected in place when already there, otherwise swapped in).
     */
    private static boolean placeFromInventorySlot(Minecraft mc, Inventory inventory, int sourceSlot) {
        int targetSlot = HotbarSlotLock.chooseTargetSlot(inventory, sourceSlot);
        inventory.setSelectedSlot(targetSlot);

        if (targetSlot != sourceSlot) {
            swapSlots(mc, targetSlot, sourceSlot);
        }
        return true;
    }

    /**
     * Scans the player's storage for the shulker box holding the most of {@code item} and
     * returns its slot, or {@link Inventory#NOT_FOUND_INDEX} if none carry it.
     */
    private static int findShulkerContainingItem(Inventory inventory, Item item) {
        int bestSlot = Inventory.NOT_FOUND_INDEX;
        int bestAmount = 0;

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack slotStack = inventory.getItem(slot);
            if (slotStack.isEmpty() || !isShulkerBox(slotStack)) {
                continue;
            }

            int amount = countItemInShulker(slotStack, item);
            if (amount > bestAmount) {
                bestAmount = amount;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * @return how many of {@code item} are stored inside {@code shulker}.
     */
    private static int countItemInShulker(ItemStack shulker, Item item) {
        ItemContainerContents contents = shulker.get(DataComponents.CONTAINER);
        if (contents == null) {
            return 0;
        }

        //? if <=1.21.11 {
        /*Stream<ItemStack> stored = contents.nonEmptyStream();
        *///?} else {
        Stream<ItemStack> stored = contents.nonEmptyItemCopyStream();
        //?}
        return stored.filter(stack -> stack.getItem() == item).mapToInt(ItemStack::getCount).sum();
    }

    /**
     * Swaps the item at {@code sourceSlot} onto {@code hotbarSlot} via a {@code SWAP} click on the
     * player's own inventory menu (no reach check). Handles both main-inventory sources (9-35, which
     * map 1:1 onto menu slots) and hotbar sources (0-8, which map to menu slots 36-44).
     *
     * @param hotbarSlot destination hotbar slot (0-8)
     * @param sourceSlot source inventory slot index (0-35)
     */
    private static void swapSlots(Minecraft mc, int hotbarSlot, int sourceSlot) {
        int menuSlot = Inventory.isHotbarSlot(sourceSlot)
                ? INVENTORY_MENU_HOTBAR_START + sourceSlot
                : sourceSlot;
        int containerId = mc.player.inventoryMenu.containerId;
        //? if <=1.21.11 {
        /*mc.gameMode.handleInventoryMouseClick(containerId, menuSlot, hotbarSlot, net.minecraft.world.inventory.ClickType.SWAP, mc.player);
        *///?} else {
        mc.gameMode.handleContainerInput(containerId, menuSlot, hotbarSlot, net.minecraft.world.inventory.ContainerInput.SWAP, mc.player);
        //?}
    }

    /**
     * Syncs a single inventory slot to the (integrated or remote) server through the creative
     * set-slot packet, which has no reach check.
     */
    private static void updateCreativeSlot(Minecraft mc, int slot) {
        if (slot < 0) {
            return;
        }
        ItemStack item = mc.player.getInventory().getItem(slot);
        int menuSlot = Inventory.isHotbarSlot(slot) ? INVENTORY_MENU_HOTBAR_START + slot : slot;
        mc.gameMode.handleCreativeModeItemAdd(item, menuSlot);
    }
}
