package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.inventory.AnvilMenu;
//? if <=1.21.11 {
/*import net.minecraft.world.inventory.ClickType;
*///?} else {
import net.minecraft.world.inventory.ContainerInput;
//?}
import net.minecraft.world.item.ItemStack;

/**
 * Mass crafting for the anvil, mirroring {@link StonecutterMassCraftHandler}.
 *
 * <p>While the ItemScroller mass-craft hotkey is held over an open anvil, this
 * keeps taking the finished result and refilling both input slots with the next
 * matching items from the player inventory, so the same anvil operation (rename,
 * repair, or enchant combine like {@code diamond_sword + enchanted_book}) is
 * applied to a whole inventory of items.</p>
 *
 * <p>The anvil rename text field normally grabs the keyboard focus, which would
 * swallow the hotkeys; that is handled separately by the AnvilScreen mixin which
 * stops the rename box from auto-focusing while this feature is enabled.</p>
 */
public class AnvilMassCraftHandler implements IClientTickHandler {
    private static final AnvilMassCraftHandler INSTANCE = new AnvilMassCraftHandler();
    private static final int INPUT_SLOT = 0;       // AnvilMenu.INPUT_SLOT
    private static final int ADDITIONAL_SLOT = 1;  // AnvilMenu.ADDITIONAL_SLOT
    private static final int RESULT_SLOT = 2;      // AnvilMenu.RESULT_SLOT
    private static final int PLAYER_INVENTORY_START = 3;

    private int ticker;

    /**
     * The two input items currently being processed, captured from the input
     * slots before a result is taken, so the slots can be refilled with more of
     * the same items once the server has cleared them. {@code additionalTemplate}
     * is empty for single-item operations (rename / repair without a material).
     */
    private ItemStack inputTemplate = ItemStack.EMPTY;
    private ItemStack additionalTemplate = ItemStack.EMPTY;

    private AnvilMassCraftHandler() {
    }

    public static AnvilMassCraftHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onClientTick(Minecraft mc) {
        if (!this.canMassCraft(mc)) {
            this.ticker = 0;
            this.inputTemplate = ItemStack.EMPTY;
            this.additionalTemplate = ItemStack.EMPTY;
            return;
        }

        int interval = StonecutterInputUtils.getMassCraftInterval();

        if (++this.ticker < interval) {
            return;
        }

        this.ticker = 0;

        if (mc.screen instanceof AnvilScreen screen) {
            this.tryMassCraft(mc, screen);
        }
    }

    private boolean canMassCraft(Minecraft mc) {
        return Configs.ItemScroller.MASS_CRAFT_ANVIL.getBooleanValue() &&
                StonecutterInputUtils.craftingFeaturesEnabled() &&
                StonecutterInputUtils.isMassCraftHotkeyActive() &&
                mc.player != null &&
                mc.level != null &&
                mc.gameMode != null &&
                mc.screen instanceof AnvilScreen;
    }

    private void tryMassCraft(Minecraft mc, AnvilScreen screen) {
        AnvilMenu menu = screen.getMenu();

        AnvilRecipeStorage storage = AnvilRecipeStorage.getInstance();
        AnvilRecipePattern recipe = storage.getRecipe(storage.getSelection());

        ItemStack template0;
        ItemStack template1;

        if (recipe.isValid()) {
            // Use the selected stored recipe. Don't disturb the slots if the player
            // has put unrelated items there.
            if (!slotMatchesOrEmpty(menu, INPUT_SLOT, recipe.getInput()) ||
                    !slotMatchesOrEmpty(menu, ADDITIONAL_SLOT, recipe.getAdditional())) {
                return;
            }

            template0 = recipe.getInput();
            template1 = recipe.getAdditional();
        } else {
            // No stored recipe selected: fall back to whatever the player has put in
            // the slots, remembering it so the slots can be refilled after a take.
            ItemStack input = menu.getSlot(INPUT_SLOT).getItem();
            ItemStack additional = menu.getSlot(ADDITIONAL_SLOT).getItem();

            if (!input.isEmpty()) {
                this.inputTemplate = input.copy();
            }

            if (!additional.isEmpty()) {
                this.additionalTemplate = additional.copy();
            }

            template0 = this.inputTemplate;
            template1 = this.additionalTemplate;
        }

        // Take the finished result first: this consumes the current inputs (and
        // pays the XP cost), then top the slots back up (slot 0 first, so the
        // shift-click order puts each item back into its own slot) so the server
        // computes a new result for the next tick.
        this.takeResult(mc, menu);
        this.refillSlot(mc, menu, INPUT_SLOT, template0);
        this.refillSlot(mc, menu, ADDITIONAL_SLOT, template1);
    }

    private static boolean slotMatchesOrEmpty(AnvilMenu menu, int slot, ItemStack template) {
        ItemStack inSlot = menu.getSlot(slot).getItem();

        if (inSlot.isEmpty()) {
            return true;
        }

        return !template.isEmpty() && sameItemAndComponents(inSlot, template);
    }

    private void takeResult(Minecraft mc, AnvilMenu menu) {
        if (menu.getSlot(RESULT_SLOT).getItem().isEmpty() || !this.canAfford(mc, menu)) {
            return;
        }

        this.quickMoveSlot(mc, menu, RESULT_SLOT);
    }

    /**
     * Survival players can only take the result when they can pay the level
     * cost; the server rejects it otherwise, so skip the click to avoid spamming.
     */
    private boolean canAfford(Minecraft mc, AnvilMenu menu) {
        if (mc.player.getAbilities().instabuild) {
            return true;
        }

        return mc.player.experienceLevel >= menu.getCost();
    }

    /**
     * Refills the given input slot with matching items from the player inventory.
     * The anvil's shift-click always fills the first empty input slot first, so
     * refilling slot 0 before slot 1 puts each item back into its own slot even
     * when the two differ (e.g. the sword into slot 0 and the book into slot 1).
     */
    private void refillSlot(Minecraft mc, AnvilMenu menu, int targetSlot, ItemStack template) {
        if (template.isEmpty()) {
            return;
        }

        for (int i = PLAYER_INVENTORY_START; i < menu.slots.size(); i++) {
            // The local view may not have cleared the slot yet after taking the
            // result; in that case leave it and retry on a later tick.
            if (!menu.getSlot(targetSlot).getItem().isEmpty()) {
                break;
            }

            if (sameItemAndComponents(menu.getSlot(i).getItem(), template)) {
                this.quickMoveSlot(mc, menu, i);
            }
        }
    }

    private void quickMoveSlot(Minecraft mc, AnvilMenu menu, int slot) {
        //? if <=1.21.11 {
        /*mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
         *///?} else {
        mc.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE, mc.player);
        //?}
    }

    private static boolean sameItemAndComponents(ItemStack actual, ItemStack expected) {
        return !actual.isEmpty() && !expected.isEmpty() && ItemStack.isSameItemSameComponents(actual, expected);
    }
}
