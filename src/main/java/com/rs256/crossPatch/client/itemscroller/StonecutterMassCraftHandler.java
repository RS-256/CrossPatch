package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.StonecutterMenu;
//? if <=1.21.11 {
/*import net.minecraft.world.inventory.ClickType;
*///?} else {
import net.minecraft.world.inventory.ContainerInput;
//?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;

public class StonecutterMassCraftHandler implements IClientTickHandler {
    private static final StonecutterMassCraftHandler INSTANCE = new StonecutterMassCraftHandler();
    private static final int INPUT_SLOT = 0;
    private static final int RESULT_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = 2;

    private int ticker;

    private StonecutterMassCraftHandler() {
    }

    public static StonecutterMassCraftHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onClientTick(Minecraft mc) {
        if (!this.canMassCraft(mc)) {
            this.ticker = 0;
            return;
        }

        int interval = StonecutterInputUtils.getMassCraftInterval();

        if (++this.ticker < interval) {
            return;
        }

        this.ticker = 0;

        if (mc.screen instanceof StonecutterScreen screen) {
            this.tryMassCraft(mc, screen);
        }
    }

    private boolean canMassCraft(Minecraft mc) {
        return Configs.ItemScroller.MASS_CRAFT_STONECUTTER.getBooleanValue() &&
                StonecutterInputUtils.craftingFeaturesEnabled() &&
                StonecutterInputUtils.isMassCraftHotkeyActive() &&
                mc.player != null &&
                mc.level != null &&
                mc.gameMode != null &&
                mc.screen instanceof StonecutterScreen;
    }

    private void tryMassCraft(Minecraft mc, StonecutterScreen screen) {
        StonecutterMenu menu = screen.getMenu();

        if (this.prepareCraft(mc, menu) <= 0) {
            return;
        }

        // A single whole-stack THROW click is enough: the server keeps taking
        // and dropping the result while the slot refills with the same item,
        // so this crafts and drops everything from the current input stack.
        this.throwResult(mc, menu);
    }

    /**
     * Crafts everything possible with the stored recipe into the player
     * inventory, like ItemScroller's craftEverything hotkey.
     */
    public boolean craftEverything(Minecraft mc) {
        if (!Configs.ItemScroller.MASS_CRAFT_STONECUTTER.getBooleanValue() ||
                !StonecutterInputUtils.craftingFeaturesEnabled() ||
                mc.player == null ||
                mc.level == null ||
                mc.gameMode == null ||
                !(mc.screen instanceof StonecutterScreen screen)) {
            return false;
        }

        StonecutterMenu menu = screen.getMenu();
        int crafts = this.prepareCraft(mc, menu);

        // The result slot contents are only known by the server, so the local
        // copy can stay empty until the next sync; cap the crafts by the input
        // amount instead of checking the result slot between the clicks.
        for (int i = 0; i < crafts; i++) {
            this.quickMoveSlot(mc, menu, RESULT_SLOT);
        }

        return crafts > 0;
    }

    /**
     * Refills the input slot and selects the stored recipe.
     * @return the number of the available input items, or 0 if crafting can't proceed
     */
    private int prepareCraft(Minecraft mc, StonecutterMenu menu) {
        StonecutterRecipePattern recipe = StonecutterRecipeStorage.getInstance()
                .getRecipe(StonecutterRecipeStorage.getInstance().getSelection());

        if (recipe.isEmpty()) {
            return 0;
        }

        ItemStack expectedInput = recipe.getIngredient();
        ItemStack inputStack = menu.getSlot(INPUT_SLOT).getItem();

        // Don't touch an unrelated item the player put into the input slot
        if (!inputStack.isEmpty() && !sameItemAndComponents(inputStack, expectedInput)) {
            return 0;
        }

        this.refillInputSlot(mc, menu, expectedInput);

        inputStack = menu.getSlot(INPUT_SLOT).getItem();

        if (!sameItemAndComponents(inputStack, expectedInput)) {
            return 0;
        }

        int recipeIndex = findRecipeIndex(mc, menu, recipe.getResult());

        if (recipeIndex < 0) {
            return 0;
        }

        if (menu.clickMenuButton(mc.player, recipeIndex)) {
            mc.gameMode.handleInventoryButtonClick(menu.containerId, recipeIndex);
        }

        return inputStack.getCount();
    }

    /**
     * Tops up the input slot by quick moving matching stacks from the player
     * inventory. The local click simulation merges the stacks into the input
     * slot immediately, so the callers can re-read the slot afterwards.
     */
    private void refillInputSlot(Minecraft mc, StonecutterMenu menu, ItemStack expectedInput) {
        for (int i = PLAYER_INVENTORY_START; i < menu.slots.size(); i++) {
            if (menu.getSlot(INPUT_SLOT).getItem().getCount() >= expectedInput.getMaxStackSize()) {
                break;
            }

            if (sameItemAndComponents(menu.getSlot(i).getItem(), expectedInput)) {
                this.quickMoveSlot(mc, menu, i);
            }
        }
    }

    private static int findRecipeIndex(Minecraft mc, StonecutterMenu menu, ItemStack expectedResult) {
        List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> entries = menu.getVisibleRecipes().entries();
        ContextMap context = SlotDisplayContext.fromLevel(mc.level);

        for (int i = 0; i < entries.size(); i++) {
            ItemStack optionStack = entries.get(i).recipe().optionDisplay().resolveForFirstStack(context);

            if (sameResult(optionStack, expectedResult)) {
                return i;
            }
        }

        return -1;
    }

    private void quickMoveSlot(Minecraft mc, StonecutterMenu menu, int slot) {
        //? if <=1.21.11 {
        /*mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
         *///?} else {
        mc.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE, mc.player);
        //?}
    }

    private void throwResult(Minecraft mc, StonecutterMenu menu) {
        //? if <=1.21.11 {
        /*mc.gameMode.handleInventoryMouseClick(menu.containerId, RESULT_SLOT, 1, ClickType.THROW, mc.player);
         *///?} else {
        mc.gameMode.handleContainerInput(menu.containerId, RESULT_SLOT, 1, ContainerInput.THROW, mc.player);
        //?}
    }

    private static boolean sameItemAndComponents(ItemStack actual, ItemStack expected) {
        return !actual.isEmpty() && !expected.isEmpty() && ItemStack.isSameItemSameComponents(actual, expected);
    }

    private static boolean sameResult(ItemStack actual, ItemStack expected) {
        return sameItemAndComponents(actual, expected) && actual.getCount() == expected.getCount();
    }
}
