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

import java.util.ArrayList;
import java.util.List;

public final class BoxLayerController {
    private static final BoxAxis X_AXIS = new BoxAxis(
            Configs.Litematica.BOX_LAYER_X_MIN_ENABLED,
            Configs.Litematica.BOX_LAYER_X_MIN_SELECTED,
            Configs.Litematica.BOX_LAYER_X_MIN_VALUE,
            Configs.Litematica.BOX_LAYER_X_MAX_ENABLED,
            Configs.Litematica.BOX_LAYER_X_MAX_SELECTED,
            Configs.Litematica.BOX_LAYER_X_MAX_VALUE
    );
    private static final BoxAxis Y_AXIS = new BoxAxis(
            Configs.Litematica.BOX_LAYER_Y_MIN_ENABLED,
            Configs.Litematica.BOX_LAYER_Y_MIN_SELECTED,
            Configs.Litematica.BOX_LAYER_Y_MIN_VALUE,
            Configs.Litematica.BOX_LAYER_Y_MAX_ENABLED,
            Configs.Litematica.BOX_LAYER_Y_MAX_SELECTED,
            Configs.Litematica.BOX_LAYER_Y_MAX_VALUE
    );
    private static final BoxAxis Z_AXIS = new BoxAxis(
            Configs.Litematica.BOX_LAYER_Z_MIN_ENABLED,
            Configs.Litematica.BOX_LAYER_Z_MIN_SELECTED,
            Configs.Litematica.BOX_LAYER_Z_MIN_VALUE,
            Configs.Litematica.BOX_LAYER_Z_MAX_ENABLED,
            Configs.Litematica.BOX_LAYER_Z_MAX_SELECTED,
            Configs.Litematica.BOX_LAYER_Z_MAX_VALUE
    );

    private BoxLayerController() {
    }

    public static boolean isEnabled() {
        return Configs.Litematica.BOX_LAYER_ENABLED.getBooleanValue();
    }

    public static boolean shouldUseLitematicaLayerHotkeys() {
        return isEnabled()
                && Configs.Litematica.USE_LITEMATICA_LAYER_HOTKEYS.getBooleanValue();
    }

    public static boolean shouldRender(BlockPos pos) {
        if (!isEnabled()) {
            return true;
        }

        return X_AXIS.contains(pos.getX())
                && Y_AXIS.contains(pos.getY())
                && Z_AXIS.contains(pos.getZ());
    }

    public static IntBoundingBox clipBox(IntBoundingBox box) {
        if (!isEnabled()) {
            return box;
        }

        int minX = X_AXIS.clipMin(box.minX());
        int minY = Y_AXIS.clipMin(box.minY());
        int minZ = Z_AXIS.clipMin(box.minZ());
        int maxX = X_AXIS.clipMax(box.maxX());
        int maxY = Y_AXIS.clipMax(box.maxY());
        int maxZ = Z_AXIS.clipMax(box.maxZ());

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return null;
        }

        return new IntBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static int layerChangeAmount() {
        return Math.max(1, Configs.Litematica.LAYER_CHANGE_AMOUNT.getIntegerValue());
    }

    public static void next() {
        offsetSelectedBounds(layerChangeAmount());
    }

    public static void previous() {
        offsetSelectedBounds(-layerChangeAmount());
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
        cycleAxisStates(BoxAxis::minEnabled);
        cycleAxisStates(BoxAxis::maxEnabled);
    }

    private static void cycleBoxLayerHotkeyStates() {
        cycleAxisStates(BoxAxis::minSelected);
        cycleAxisStates(BoxAxis::maxSelected);
    }

    private static void cycleAxisStates(AxisStateSelector selector) {
        ConfigBoolean x = selector.select(X_AXIS);
        ConfigBoolean y = selector.select(Y_AXIS);
        ConfigBoolean z = selector.select(Z_AXIS);
        boolean previousX = x.getBooleanValue();

        x.setBooleanValue(z.getBooleanValue());
        z.setBooleanValue(y.getBooleanValue());
        y.setBooleanValue(previousX);
    }

    private static void printHotkeySelectionMessage() {
        InfoUtils.printActionbarMessage(
                Reference.MOD_ID + ".message.box_layer_hotkey_selection",
                getHotkeySelectionLabel()
        );
    }

    private static String getHotkeySelectionLabel() {
        List<String> selected = new ArrayList<>();

        addSelectedLabel(selected, "X min", X_AXIS.minSelected());
        addSelectedLabel(selected, "X max", X_AXIS.maxSelected());
        addSelectedLabel(selected, "Y min", Y_AXIS.minSelected());
        addSelectedLabel(selected, "Y max", Y_AXIS.maxSelected());
        addSelectedLabel(selected, "Z min", Z_AXIS.minSelected());
        addSelectedLabel(selected, "Z max", Z_AXIS.maxSelected());

        return selected.isEmpty() ? "none" : String.join(", ", selected);
    }

    private static void addSelectedLabel(List<String> selected, String label, ConfigBoolean config) {
        if (config.getBooleanValue()) {
            selected.add(label);
        }
    }

    public static void offsetSelectedBounds(int amount) {
        X_AXIS.offsetSelected(amount);
        Y_AXIS.offsetSelected(amount);
        Z_AXIS.offsetSelected(amount);

        Configs.saveToFile();
        refreshSchematic();
    }

    public static void setSelectedBoundsToPosition(BlockPos pos) {
        X_AXIS.setSelected(pos.getX());
        Y_AXIS.setSelected(pos.getY());
        Z_AXIS.setSelected(pos.getZ());

        Configs.saveToFile();
        refreshSchematic();
    }

    public static void refreshSchematic() {
        if (shouldForceLitematicaLayerAll()) {
            forceLitematicaLayerAll();
        }

        SchematicWorldRefresher.INSTANCE.updateAll();
    }

    public static boolean shouldForceLitematicaLayerAll() {
        return Configs.Litematica.BOX_LAYER_ENABLED.getBooleanValue()
                && Configs.Litematica.FORCE_LITEMATICA_LAYER_ALL.getBooleanValue();
    }

    public static void forceLitematicaLayerAll(){
        LayerRange layerRange = DataManager.getRenderLayerRange();

        if (layerRange.getLayerMode() != LayerMode.ALL) {
            layerRange.setLayerMode(LayerMode.ALL, false);
        }
    }

    private interface AxisStateSelector {
        ConfigBoolean select(BoxAxis axis);
    }

    private record BoxAxis(
            ConfigBoolean minEnabled,
            ConfigBoolean minSelected,
            ConfigInteger minValue,
            ConfigBoolean maxEnabled,
            ConfigBoolean maxSelected,
            ConfigInteger maxValue
    ) {
        boolean contains(int coordinate) {
            return (!this.minEnabled.getBooleanValue() || coordinate >= this.minValue.getIntegerValue())
                    && (!this.maxEnabled.getBooleanValue() || coordinate <= this.maxValue.getIntegerValue());
        }

        int clipMin(int current) {
            return this.minEnabled.getBooleanValue() ? Math.max(current, this.minValue.getIntegerValue()) : current;
        }

        int clipMax(int current) {
            return this.maxEnabled.getBooleanValue() ? Math.min(current, this.maxValue.getIntegerValue()) : current;
        }

        void offsetSelected(int amount) {
            this.offsetIfSelected(this.minSelected, this.minValue, amount);
            this.offsetIfSelected(this.maxSelected, this.maxValue, amount);
        }

        void setSelected(int coordinate) {
            this.setIfSelected(this.minSelected, this.minValue, coordinate);
            this.setIfSelected(this.maxSelected, this.maxValue, coordinate);
        }

        private void offsetIfSelected(ConfigBoolean selected, ConfigInteger value, int amount) {
            if (selected.getBooleanValue()) {
                value.setIntegerValue(value.getIntegerValue() + amount);
            }
        }

        private void setIfSelected(ConfigBoolean selected, ConfigInteger value, int coordinate) {
            if (selected.getBooleanValue()) {
                value.setIntegerValue(coordinate);
            }
        }
    }
}
