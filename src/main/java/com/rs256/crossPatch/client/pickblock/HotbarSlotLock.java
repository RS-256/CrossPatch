package com.rs256.crossPatch.client.pickblock;

import com.rs256.crossPatch.client.config.Configs;
import net.minecraft.world.entity.player.Inventory;

import java.util.Arrays;

/**
 * Parses {@link Configs.PickBlock#PICK_BLOCK_PRO_PICK_SLOT_ENABLED} and decides which hotbar slot
 * a picked item should land in.
 *
 * <p>The config value lists the allowed slot numbers 1-9 (any non-digit separators are ignored,
 * so {@code "135"}, {@code "1,3,5"} and {@code "1 3 5"} are equivalent). An empty value, or one
 * with no valid digit, means no restriction.</p>
 */
final class HotbarSlotLock {
    private static final int HOTBAR_SIZE = Inventory.getSelectionSize();

    private HotbarSlotLock() {
    }

    /**
     * @return {@code true} if the config restricts pick to a subset of hotbar slots.
     */
    static boolean isRestricted() {
        String value = Configs.PickBlock.PICK_BLOCK_PRO_PICK_SLOT_ENABLED.getStringValue();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '1' && c <= '9' && (c - '1') < HOTBAR_SIZE) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a per-hotbar-slot mask of the slots pick may use; all-{@code true} when unrestricted.
     */
    private static boolean[] allowedMask() {
        String value = Configs.PickBlock.PICK_BLOCK_PRO_PICK_SLOT_ENABLED.getStringValue();
        boolean[] allowed = new boolean[HOTBAR_SIZE];
        boolean any = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '1' && c <= '9') {
                int slot = c - '1';
                if (slot < HOTBAR_SIZE) {
                    allowed[slot] = true;
                    any = true;
                }
            }
        }

        if (!any) {
            Arrays.fill(allowed, true);
        }
        return allowed;
    }

    /**
     * Chooses the hotbar slot the picked item should end up in, honouring the allowed-slot mask.
     *
     * @param sourceSlot the inventory slot the item currently occupies, or
     *                   {@link Inventory#NOT_FOUND_INDEX} if it is not in the inventory
     * @return an allowed hotbar slot index (0-8)
     */
    static int chooseTargetSlot(Inventory inventory, int sourceSlot) {
        boolean[] allowed = allowedMask();

        // Keep an allowed hotbar slot the item already sits in (no move needed).
        if (Inventory.isHotbarSlot(sourceSlot) && allowed[sourceSlot]) {
            return sourceSlot;
        }

        // Prefer an empty allowed slot, scanning from the currently selected one.
        int selected = inventory.getSelectedSlot();
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            int slot = (selected + i) % HOTBAR_SIZE;
            if (allowed[slot] && inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }

        // Fall back to the selected slot if allowed, otherwise the first allowed slot.
        if (allowed[selected]) {
            return selected;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (allowed[slot]) {
                return slot;
            }
        }
        return selected;
    }
}
