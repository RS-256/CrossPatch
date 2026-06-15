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
//? if <=1.21.11 {
/*import net.minecraft.world.inventory.ClickType;
*///?} else {
import net.minecraft.world.inventory.ContainerInput;
//?}

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
    private static final int HOTBAR_SIZE = Inventory.getSelectionSize();

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
     * Brings the item at an existing inventory slot to hand: re-select it if it is already on
     * the hotbar, otherwise swap it onto an optimal hotbar slot.
     */
    private static boolean placeFromInventorySlot(Minecraft mc, Inventory inventory, int slot) {
        if (Inventory.isHotbarSlot(slot)) {
            inventory.setSelectedSlot(slot);
            return true;
        }
        int hotbarSlot = selectOptimalHotbarSlot(inventory);
        swapToHotbar(mc, hotbarSlot, slot);
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
     * Selects an empty hotbar slot (starting from the current one), falling back to the
     * currently selected slot when the whole hotbar is occupied.
     *
     * @return the selected hotbar slot index
     */
    private static int selectOptimalHotbarSlot(Inventory inventory) {
        int slot = findEmptyHotbarSlot(inventory);
        if (slot == -1) {
            slot = inventory.getSelectedSlot();
        }
        inventory.setSelectedSlot(slot);
        return slot;
    }

    /**
     * @return an empty hotbar slot index, scanning from the selected slot, or {@code -1} if the
     * hotbar is full.
     */
    private static int findEmptyHotbarSlot(Inventory inventory) {
        int slot = inventory.getSelectedSlot();
        int tries = 0;
        while (!inventory.getItem(slot).isEmpty() && tries < HOTBAR_SIZE) {
            tries++;
            slot++;
            if (slot >= HOTBAR_SIZE) {
                slot = 0;
            }
        }
        return tries >= HOTBAR_SIZE ? -1 : slot;
    }

    /**
     * Swaps a main-inventory item onto the given hotbar slot via a container click on the
     * player's own inventory menu (no reach check).
     *
     * @param hotbarSlot    destination hotbar slot (0-8)
     * @param inventorySlot source main-inventory slot index (9-35); these map 1:1 onto
     *                      {@code InventoryMenu} slot indices
     */
    private static void swapToHotbar(Minecraft mc, int hotbarSlot, int inventorySlot) {
        int containerId = mc.player.inventoryMenu.containerId;
        //? if <=1.21.11 {
        /*mc.gameMode.handleInventoryMouseClick(containerId, inventorySlot, hotbarSlot, ClickType.SWAP, mc.player);
        *///?} else {
        mc.gameMode.handleContainerInput(containerId, inventorySlot, hotbarSlot, ContainerInput.SWAP, mc.player);
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
