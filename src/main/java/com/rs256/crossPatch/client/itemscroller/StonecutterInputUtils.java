package com.rs256.crossPatch.client.itemscroller;

import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.config.Hotkeys;
import fi.dy.masa.malilib.util.GuiUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;

public final class StonecutterInputUtils {
    private StonecutterInputUtils() {
    }

    public static boolean isRecipeViewOpen() {
        return Configs.ItemScroller.MASS_CRAFT_STONECUTTER.getBooleanValue() &&
                craftingFeaturesEnabled() &&
                GuiUtils.getCurrentScreen() instanceof StonecutterScreen &&
                isRecipeViewKeybindHeld();
    }

    /**
     * Mirrors ItemScroller's "enableCraftingFeatures" toggle: when ItemScroller
     * is present and the toggle is off, all the crafting features stay disabled.
     */
    public static boolean craftingFeaturesEnabled() {
        if (FabricLoader.getInstance().isModLoaded("itemscroller")) {
            return fi.dy.masa.itemscroller.config.Configs.Toggles.CRAFTING_FEATURES.getBooleanValue();
        }

        return true;
    }

    public static boolean isStoreRecipeHotkeyActive(int keyCode) {
        if (useItemScrollerHotkeys()) {
            return fi.dy.masa.itemscroller.config.Hotkeys.STORE_RECIPE.getKeybind().matches(keyCode) ||
                    fi.dy.masa.itemscroller.config.Hotkeys.STORE_RECIPE.getKeybind().isPressed();
        }

        return Hotkeys.STORE_RECIPE.getKeybind().matches(keyCode) ||
                Hotkeys.STORE_RECIPE.getKeybind().isPressed();
    }

    /**
     * Checks only the ItemScroller keybind: the CrossPatch fallback hotkey is
     * dispatched through its keybind callback instead, so checking it here as
     * well would trigger the crafting twice for one key press.
     */
    public static boolean isCraftEverythingHotkeyActive(int keyCode) {
        if (useItemScrollerHotkeys()) {
            return fi.dy.masa.itemscroller.config.Configs.Generic.MOD_MAIN_TOGGLE.getBooleanValue() &&
                    (fi.dy.masa.itemscroller.config.Hotkeys.CRAFT_EVERYTHING.getKeybind().matches(keyCode) ||
                            fi.dy.masa.itemscroller.config.Hotkeys.CRAFT_EVERYTHING.getKeybind().isPressed());
        }

        return false;
    }

    public static boolean isMassCraftHotkeyActive() {
        return useItemScrollerHotkeys() &&
                fi.dy.masa.itemscroller.config.Configs.Generic.MOD_MAIN_TOGGLE.getBooleanValue() &&
                (fi.dy.masa.itemscroller.config.Hotkeys.MASS_CRAFT.getKeybind().isKeybindHeld() ||
                        fi.dy.masa.itemscroller.config.Configs.Generic.MASS_CRAFT_HOLD.getBooleanValue());
    }

    public static int getMassCraftInterval() {
        if (FabricLoader.getInstance().isModLoaded("itemscroller")) {
            return fi.dy.masa.itemscroller.config.Configs.Generic.MASS_CRAFT_INTERVAL.getIntegerValue();
        }

        return 2;
    }

    public static boolean isRecipeViewKeybindHeld() {
        if (useItemScrollerHotkeys()) {
            return fi.dy.masa.itemscroller.config.Hotkeys.RECIPE_VIEW.getKeybind().isKeybindHeld();
        }

        return Hotkeys.RECIPE_VIEW.getKeybind().isKeybindHeld();
    }

    private static boolean useItemScrollerHotkeys() {
        return Configs.ItemScroller.USE_ITEMSCROLLER_HOTKEYS.getBooleanValue() &&
                FabricLoader.getInstance().isModLoaded("itemscroller");
    }
}
