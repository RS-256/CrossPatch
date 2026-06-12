package com.rs256.crossPatch.client.event;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Hotkeys;
import com.rs256.crossPatch.client.itemscroller.StonecutterInputUtils;
import com.rs256.crossPatch.client.itemscroller.StonecutterMassCraftHandler;
import com.rs256.crossPatch.client.itemscroller.StonecutterRecipeStorage;
import com.rs256.crossPatch.client.itemscroller.StonecutterRenderEventHandler;
import com.rs256.crossPatch.client.mixin.screen.AbstractContainerScreenAccessor;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import fi.dy.masa.malilib.util.KeyCodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {
    private static final InputHandler INSTANCE = new InputHandler();

    private InputHandler() {
    }

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey hotkey : Hotkeys.HOTKEY_LIST) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(
                Reference.MOD_NAME,
                Reference.MOD_ID + ".hotkeys.category.box_layer",
                Hotkeys.HOTKEY_LIST
        );
    }

    @Override
    public boolean onKeyInput(KeyEvent input, boolean eventKeyState) {
        if (eventKeyState && StonecutterInputUtils.isCraftEverythingHotkeyActive(input.key()) &&
                StonecutterMassCraftHandler.getInstance().craftEverything(Minecraft.getInstance())) {
            return true;
        }

        if (eventKeyState && StonecutterInputUtils.isRecipeViewOpen() &&
                StonecutterInputUtils.isStoreRecipeHotkeyActive(input.key())) {
            return storeStonecutterRecipeUnderMouse(Minecraft.getInstance());
        }

        if (StonecutterInputUtils.isRecipeViewOpen() && eventKeyState) {
            int index = -1;
            StonecutterRecipeStorage recipes = StonecutterRecipeStorage.getInstance();
            int oldIndex = recipes.getSelection();
            int recipesPerPage = recipes.getRecipeCountPerPage();
            int recipeIndexChange = (input.hasShiftDown() || GuiBase.isShiftDown()) ? recipesPerPage : recipesPerPage / 2;

            if (input.key() >= KeyCodes.KEY_1 && input.key() <= KeyCodes.KEY_9) {
                index = Mth.clamp(input.key() - GLFW.GLFW_KEY_1, 0, 8);
            } else if (input.key() == KeyCodes.KEY_UP && oldIndex > 0) {
                index = oldIndex - 1;
            } else if (input.key() == KeyCodes.KEY_DOWN && oldIndex < recipes.getTotalRecipeCount() - 1) {
                index = oldIndex + 1;
            } else if (input.key() == KeyCodes.KEY_LEFT && oldIndex >= recipeIndexChange) {
                index = oldIndex - recipeIndexChange;
            } else if (input.key() == KeyCodes.KEY_RIGHT && oldIndex < recipes.getTotalRecipeCount() - recipeIndexChange) {
                index = oldIndex + recipeIndexChange;
            }

            if (index >= 0) {
                recipes.changeSelectedRecipe(index);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (StonecutterInputUtils.isRecipeViewOpen()) {
            StonecutterRecipeStorage.getInstance().scrollSelection(amount < 0);
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent click, boolean eventButtonState) {
        if (!eventButtonState) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();

        if (StonecutterInputUtils.isCraftEverythingHotkeyActive(click.input() - 100) &&
                StonecutterMassCraftHandler.getInstance().craftEverything(mc)) {
            return true;
        }

        if (!StonecutterInputUtils.isRecipeViewOpen()) {
            return false;
        }

        if (StonecutterInputUtils.isStoreRecipeHotkeyActive(click.input() - 100)) {
            return storeStonecutterRecipeUnderMouse(mc);
        }

        if (mc.screen instanceof StonecutterScreen gui) {
            StonecutterRecipeStorage recipes = StonecutterRecipeStorage.getInstance();
            int mouseX = fi.dy.masa.malilib.util.InputUtils.getMouseX();
            int mouseY = fi.dy.masa.malilib.util.InputUtils.getMouseY();
            int hoveredRecipeId = StonecutterRenderEventHandler.instance().getHoveredRecipeId(mouseX, mouseY, recipes, gui);

            if (hoveredRecipeId >= 0) {
                recipes.changeSelectedRecipe(hoveredRecipeId);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
    }

    public static boolean storeStonecutterRecipeUnderMouse(Minecraft mc) {
        if (mc.screen instanceof StonecutterScreen screen) {
            Slot slot = ((AbstractContainerScreenAccessor) screen).crosspatch$getHoveredSlot();

            if (slot != null && slot.index == 1) {
                StonecutterRecipeStorage.getInstance().storeRecipeToCurrentSelection(screen, slot, true, mc);
                return true;
            }
        }

        return false;
    }
}
