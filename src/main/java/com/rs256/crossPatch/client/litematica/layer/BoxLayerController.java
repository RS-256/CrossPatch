package com.rs256.crossPatch.client.litematica.layer;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.litematica.util.SchematicWorldRefresher;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerMode;
import fi.dy.masa.malilib.util.LayerRange;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

public final class BoxLayerController {
    private BoxLayerController() {
    }

    public static boolean isEnabled() {
        return Configs.Generic.BOX_LAYER_ENABLED.getBooleanValue();
    }

    public static boolean shouldUseLitematicaLayerHotkeys() {
        return isEnabled()
                && Configs.Generic.USE_LITEMATICA_LAYER_HOTKEYS.getBooleanValue();
    }

    public static boolean shouldRender(BlockPos pos) {
        if (!isEnabled()) {
            return true;
        }

        return isWithinAxis(
                pos.getX(),
                Configs.Generic.BOX_LAYER_X_MIN_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_X_MIN_VALUE.getIntegerValue(),
                Configs.Generic.BOX_LAYER_X_MAX_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_X_MAX_VALUE.getIntegerValue()
        ) && isWithinAxis(
                pos.getY(),
                Configs.Generic.BOX_LAYER_Y_MIN_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_Y_MIN_VALUE.getIntegerValue(),
                Configs.Generic.BOX_LAYER_Y_MAX_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_Y_MAX_VALUE.getIntegerValue()
        ) && isWithinAxis(
                pos.getZ(),
                Configs.Generic.BOX_LAYER_Z_MIN_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_Z_MIN_VALUE.getIntegerValue(),
                Configs.Generic.BOX_LAYER_Z_MAX_ENABLED.getBooleanValue(),
                Configs.Generic.BOX_LAYER_Z_MAX_VALUE.getIntegerValue()
        );
    }

    private static boolean isWithinAxis(
            int coordinate,
            boolean minEnabled,
            int min,
            boolean maxEnabled,
            int max
    ) {
        return (!minEnabled || coordinate >= min)
                && (!maxEnabled || coordinate <= max);
    }

    public static IntBoundingBox clipBox(IntBoundingBox box) {
        if (!isEnabled()) {
            return box;
        }

        int minX = clipMin(box.minX(), Configs.Generic.BOX_LAYER_X_MIN_ENABLED, Configs.Generic.BOX_LAYER_X_MIN_VALUE);
        int minY = clipMin(box.minY(), Configs.Generic.BOX_LAYER_Y_MIN_ENABLED, Configs.Generic.BOX_LAYER_Y_MIN_VALUE);
        int minZ = clipMin(box.minZ(), Configs.Generic.BOX_LAYER_Z_MIN_ENABLED, Configs.Generic.BOX_LAYER_Z_MIN_VALUE);
        int maxX = clipMax(box.maxX(), Configs.Generic.BOX_LAYER_X_MAX_ENABLED, Configs.Generic.BOX_LAYER_X_MAX_VALUE);
        int maxY = clipMax(box.maxY(), Configs.Generic.BOX_LAYER_Y_MAX_ENABLED, Configs.Generic.BOX_LAYER_Y_MAX_VALUE);
        int maxZ = clipMax(box.maxZ(), Configs.Generic.BOX_LAYER_Z_MAX_ENABLED, Configs.Generic.BOX_LAYER_Z_MAX_VALUE);

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return null;
        }

        return new IntBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static int clipMin(
            int current,
            ConfigBoolean enabled,
            ConfigInteger value
    ) {
        return enabled.getBooleanValue() ? Math.max(current, value.getIntegerValue()) : current;
    }

    private static int clipMax(
            int current,
            ConfigBoolean enabled,
            ConfigInteger value
    ) {
        return enabled.getBooleanValue() ? Math.min(current, value.getIntegerValue()) : current;
    }

    public static void next() {
        offsetSelectedBounds(1);
    }

    public static void previous() {
        offsetSelectedBounds(-1);
    }

    public static void setHere(Minecraft mc) {
        Entity entity = mc.getCameraEntity();

        if (entity == null) {
            return;
        }

        setSelectedBoundsToPosition(entity.blockPosition());
    }

    public static void cycleAxis() {
        LayerRange layerRange = DataManager.getRenderLayerRange();
        layerRange.setAxis(nextAxis(layerRange.getAxis()));

        if (isEnabled()) {
            cycleBoxLayerEnabledStates();
            Configs.saveToFile();
        }

        refreshSchematic();
    }

    public static void cycleHotkeyAxis() {
        cycleBoxLayerHotkeyStates();
        Configs.saveToFile();
        printHotkeySelectionMessage();
        refreshSchematic();
    }

    private static Direction.Axis nextAxis(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.Axis.Y;
            case Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.X;
        };
    }

    private static void cycleBoxLayerEnabledStates() {
        cycleEnabledStates(
                Configs.Generic.BOX_LAYER_X_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MIN_ENABLED
        );
        cycleEnabledStates(
                Configs.Generic.BOX_LAYER_X_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MAX_ENABLED
        );
    }

    private static void cycleBoxLayerHotkeyStates() {
        cycleEnabledStates(
                Configs.Generic.BOX_LAYER_X_MIN_SELECTED,
                Configs.Generic.BOX_LAYER_Y_MIN_SELECTED,
                Configs.Generic.BOX_LAYER_Z_MIN_SELECTED
        );
        cycleEnabledStates(
                Configs.Generic.BOX_LAYER_X_MAX_SELECTED,
                Configs.Generic.BOX_LAYER_Y_MAX_SELECTED,
                Configs.Generic.BOX_LAYER_Z_MAX_SELECTED
        );
    }

    private static void cycleEnabledStates(ConfigBoolean x, ConfigBoolean y, ConfigBoolean z) {
        boolean previousX = x.getBooleanValue();
        x.setBooleanValue(z.getBooleanValue());
        z.setBooleanValue(y.getBooleanValue());
        y.setBooleanValue(previousX);
    }

    private static void printHotkeySelectionMessage() {
        InfoUtils.printActionbarMessage(Reference.MOD_ID + ".message.box_layer_hotkey_selection_cycled");
    }

    public static void offsetSelectedBounds(int amount) {
        offsetIfSelected(Configs.Generic.BOX_LAYER_X_MIN_SELECTED, Configs.Generic.BOX_LAYER_X_MIN_VALUE, amount);
        offsetIfSelected(Configs.Generic.BOX_LAYER_X_MAX_SELECTED, Configs.Generic.BOX_LAYER_X_MAX_VALUE, amount);

        offsetIfSelected(Configs.Generic.BOX_LAYER_Y_MIN_SELECTED, Configs.Generic.BOX_LAYER_Y_MIN_VALUE, amount);
        offsetIfSelected(Configs.Generic.BOX_LAYER_Y_MAX_SELECTED, Configs.Generic.BOX_LAYER_Y_MAX_VALUE, amount);

        offsetIfSelected(Configs.Generic.BOX_LAYER_Z_MIN_SELECTED, Configs.Generic.BOX_LAYER_Z_MIN_VALUE, amount);
        offsetIfSelected(Configs.Generic.BOX_LAYER_Z_MAX_SELECTED, Configs.Generic.BOX_LAYER_Z_MAX_VALUE, amount);

        Configs.saveToFile();
        refreshSchematic();
    }

    private static void offsetIfSelected(
            fi.dy.masa.malilib.config.options.ConfigBoolean selected,
            fi.dy.masa.malilib.config.options.ConfigInteger value,
            int amount
    ) {
        if (selected.getBooleanValue()) {
            value.setIntegerValue(value.getIntegerValue() + amount);
        }
    }

    public static void setSelectedBoundsToPosition(BlockPos pos) {
        setIfSelected(Configs.Generic.BOX_LAYER_X_MIN_SELECTED, Configs.Generic.BOX_LAYER_X_MIN_VALUE, pos.getX());
        setIfSelected(Configs.Generic.BOX_LAYER_X_MAX_SELECTED, Configs.Generic.BOX_LAYER_X_MAX_VALUE, pos.getX());

        setIfSelected(Configs.Generic.BOX_LAYER_Y_MIN_SELECTED, Configs.Generic.BOX_LAYER_Y_MIN_VALUE, pos.getY());
        setIfSelected(Configs.Generic.BOX_LAYER_Y_MAX_SELECTED, Configs.Generic.BOX_LAYER_Y_MAX_VALUE, pos.getY());

        setIfSelected(Configs.Generic.BOX_LAYER_Z_MIN_SELECTED, Configs.Generic.BOX_LAYER_Z_MIN_VALUE, pos.getZ());
        setIfSelected(Configs.Generic.BOX_LAYER_Z_MAX_SELECTED, Configs.Generic.BOX_LAYER_Z_MAX_VALUE, pos.getZ());

        Configs.saveToFile();
        refreshSchematic();
    }

    private static void setIfSelected(
            fi.dy.masa.malilib.config.options.ConfigBoolean selected,
            fi.dy.masa.malilib.config.options.ConfigInteger value,
            int coordinate
    ) {
        if (selected.getBooleanValue()) {
            value.setIntegerValue(coordinate);
        }
    }

    public static void refreshSchematic() {
        if (shouldForceLitematicaLayerAll()) {
            forceLitematicaLayerAll();
        }

        SchematicWorldRefresher.INSTANCE.updateAll();
    }

    public static boolean shouldForceLitematicaLayerAll() {
        return Configs.Generic.BOX_LAYER_ENABLED.getBooleanValue()
                && Configs.Generic.FORCE_LITEMATICA_LAYER_ALL.getBooleanValue();
    }

    public static void forceLitematicaLayerAll(){
        LayerRange layerRange = DataManager.getRenderLayerRange();

        if (layerRange.getLayerMode() != LayerMode.ALL) {
            layerRange.setLayerMode(LayerMode.ALL);
        }
    }
}
