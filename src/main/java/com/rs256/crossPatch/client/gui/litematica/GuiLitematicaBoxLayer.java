package com.rs256.crossPatch.client.gui.litematica;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.gui.widget.AxisType;
import com.rs256.crossPatch.client.gui.widget.Buttons;
import com.rs256.crossPatch.client.gui.widget.CheckBoxes;
import com.rs256.crossPatch.client.gui.widget.TextFields;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiLitematicaBoxLayer extends GuiBase {
    private final List<GuiTextFieldGeneric> textFields = new ArrayList<>();
    private static final int ROW_HEIGHT = 21;
    private static final int AXIS_GAP = 8;
    private static final int AXIS_LABEL_GAP = 14;

    public GuiLitematicaBoxLayer(@Nullable Screen parent) {
        this.setParent(parent);
        this.title = Reference.MOD_ID + ".gui.title.litematica_box_layer";
    }

    @Override
    public void initGui() {
        super.initGui();

        this.textFields.clear();

        int sectionX = 10;
        int sectionY = 32;

        sectionY = this.createAxisSection(
                sectionX,
                sectionY,
                AxisType.X,
                "X",
                Configs.Generic.BOX_LAYER_X_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_X_MAX_SELECTED,
                Configs.Generic.BOX_LAYER_X_MAX_VALUE,
                Configs.Generic.BOX_LAYER_X_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_X_MIN_SELECTED,
                Configs.Generic.BOX_LAYER_X_MIN_VALUE
        );

        sectionY += AXIS_LABEL_GAP;

        sectionY = this.createAxisSection(
                sectionX,
                sectionY,
                AxisType.Y,
                "Y",
                Configs.Generic.BOX_LAYER_Y_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MAX_SELECTED,
                Configs.Generic.BOX_LAYER_Y_MAX_VALUE,
                Configs.Generic.BOX_LAYER_Y_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MIN_SELECTED,
                Configs.Generic.BOX_LAYER_Y_MIN_VALUE
        );

        sectionY += AXIS_GAP;

        this.createAxisSection(
                sectionX,
                sectionY,
                AxisType.Z,
                "Z",
                Configs.Generic.BOX_LAYER_Z_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MAX_SELECTED,
                Configs.Generic.BOX_LAYER_Z_MAX_VALUE,
                Configs.Generic.BOX_LAYER_Z_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MIN_SELECTED,
                Configs.Generic.BOX_LAYER_Z_MIN_VALUE
        );

        this.updateTextFieldValues();
    }

    private int createAxisSection(int x, int y, AxisType axis, String axisName, ConfigBoolean maxEnabled, ConfigBoolean maxHotkey, ConfigInteger maxValue, ConfigBoolean minEnabled, ConfigBoolean minHotkey, ConfigInteger minValue) {
        this.addLabel(x, y, 100, 12, 0xFFFFFFFF, StringUtils.translate(Reference.MOD_ID + ".gui.label.axis", axisName));

        y += AXIS_LABEL_GAP;

        this.createLayerRow(x, y, axis, Reference.MOD_ID + ".gui.label.max_layer", maxEnabled, maxHotkey, maxValue);

        y += ROW_HEIGHT;

        this.createLayerRow(x, y, axis, Reference.MOD_ID + ".gui.label.min_layer", minEnabled, minHotkey, minValue);

        return y + ROW_HEIGHT;
    }

    private void createLayerRow(int x, int y, AxisType axis, String labelKey, ConfigBoolean enabled, ConfigBoolean hotkey, ConfigInteger value) {
        String label = StringUtils.translate(labelKey) + ":";

        int labelWidth = 60;

        this.addLabel(x, y + 5, labelWidth, 12, 0xFFFFFFFF, label);

        x += labelWidth + 10;
        GuiTextFieldGeneric textField = TextFields.integerField(this, this.font, x, y, 90, value, this::refreshGui);

        x += textField.getWidth() + 3;
        ButtonGeneric adjustButton = Buttons.valueAdjustButton(this, x, y, value, this::refreshGui);

        x += adjustButton.getWidth() + 4;
        ButtonGeneric setHereButton = Buttons.setHereButton(this, x, y, axis, value, this::refreshGui);

        x += setHereButton.getWidth() + 8;
        CheckBoxes.enableCheckBox(this, x, y + 4, enabled, this::refreshGui);
        CheckBoxes.hotkeyCheckBox(this, x + 60, y + 3, hotkey, this::refreshGui);
    }

    private void updateTextFieldValues() {
        int index = 0;

        this.setTextFieldValue(index++, Configs.Generic.BOX_LAYER_X_MAX_VALUE);
        this.setTextFieldValue(index++, Configs.Generic.BOX_LAYER_X_MIN_VALUE);

        this.setTextFieldValue(index++, Configs.Generic.BOX_LAYER_Y_MAX_VALUE);
        this.setTextFieldValue(index++, Configs.Generic.BOX_LAYER_Y_MIN_VALUE);

        this.setTextFieldValue(index++, Configs.Generic.BOX_LAYER_Z_MAX_VALUE);
        this.setTextFieldValue(index, Configs.Generic.BOX_LAYER_Z_MIN_VALUE);
    }

    private void setTextFieldValue(int index, ConfigInteger value) {
        if (index >= 0 && index < this.textFields.size()) {
            this.textFields.get(index).setValue(String.valueOf(value.getIntegerValue()));
        }
    }

    private void refreshGui() {
        Configs.saveToFile();
        this.initGui();
    }

    @Override
    protected void drawContents(GuiContext ctx, int mouseX, int mouseY, float partialTicks) {
        super.drawContents(ctx, mouseX, mouseY, partialTicks);

        List<String> warnings = this.getWarnings();

        if (!warnings.isEmpty()) {
            int x = 10;
            int y = this.height - 48;

            this.drawString(
                    ctx,
                    GuiBase.TXT_RED + StringUtils.translate(Reference.MOD_ID + ".gui.warning.invalid_range") + GuiBase.TXT_RST,
                    x,
                    y,
                    0xFFFFFFFF
            );

            y += 12;

            for (String warning : warnings) {
                this.drawString(ctx, GuiBase.TXT_RED + warning + GuiBase.TXT_RST, x, y, 0xFFFFFFFF);
                y += 12;
            }
        }
    }

    private List<String> getWarnings() {
        List<String> warnings = new ArrayList<>();

        this.addInvalidAxisWarning(
                warnings,
                "X",
                Configs.Generic.BOX_LAYER_X_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_X_MIN_VALUE,
                Configs.Generic.BOX_LAYER_X_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_X_MAX_VALUE
        );

        this.addInvalidAxisWarning(
                warnings,
                "Y",
                Configs.Generic.BOX_LAYER_Y_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MIN_VALUE,
                Configs.Generic.BOX_LAYER_Y_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Y_MAX_VALUE
        );

        this.addInvalidAxisWarning(
                warnings,
                "Z",
                Configs.Generic.BOX_LAYER_Z_MIN_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MIN_VALUE,
                Configs.Generic.BOX_LAYER_Z_MAX_ENABLED,
                Configs.Generic.BOX_LAYER_Z_MAX_VALUE
        );

        return warnings;
    }

    private void addInvalidAxisWarning(
            List<String> warnings,
            String axis,
            ConfigBoolean minEnabled,
            ConfigInteger minValue,
            ConfigBoolean maxEnabled,
            ConfigInteger maxValue
    ) {
        if (minEnabled.getBooleanValue()
                && maxEnabled.getBooleanValue()
                && minValue.getIntegerValue() > maxValue.getIntegerValue()) {
            warnings.add(axis + " min > " + axis + " max");
        }
    }
}