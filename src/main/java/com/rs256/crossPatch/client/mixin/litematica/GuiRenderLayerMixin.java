package com.rs256.crossPatch.client.mixin.litematica;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.gui.litematica.GuiLitematicaBoxLayer;
import com.rs256.crossPatch.client.gui.widget.AxisType;
import com.rs256.crossPatch.client.gui.widget.Buttons;
import com.rs256.crossPatch.client.gui.widget.CheckBoxes;
import com.rs256.crossPatch.client.gui.widget.TextFields;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.rs256.crossPatch.client.config.Configs.Litematica.BOX_LAYER_ENABLED;

@Mixin(targets = "fi.dy.masa.litematica.gui.GuiRenderLayer")
public abstract class GuiRenderLayerMixin {
    @Unique
    private final List<GuiTextFieldGeneric> crosspatch$textFields = new ArrayList<>();

    @ModifyConstant(
            method = "initGui",
            constant = @Constant(intValue = 60),
            remap = false
    )
    private int crosspatch$moveLayerControlsDown(int original) {
        return BOX_LAYER_ENABLED.getBooleanValue() ? original + 26 : original;
    }

    @Inject(method = "initGui", at = @At("RETURN"), remap = false)
    private void crosspatch$addBoxLayerControls(CallbackInfo ci) {
        this.crosspatch$textFields.clear();

        if (BOX_LAYER_ENABLED.getBooleanValue()) {

            GuiBase gui = (GuiBase) (Object) this;
            Screen screen = (Screen) (Object) this;

            if (screen.width < 540) {
                this.crosspatch$addOpenButton(gui, screen);
            } else {
                this.crosspatch$addInlineEditor(gui, screen);
            }
        }
    }

    @Unique
    private void crosspatch$addOpenButton(GuiBase gui, Screen screen) {
        int x = 10;
        int y = 60;
        int buttonWidth = 120;

        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                buttonWidth,
                20,
                StringUtils.translate(Reference.MOD_ID + ".gui.button.open_litematica_box_layer")
        );

        gui.addButton(
                button,
                (buttonBase, mouseButton) -> GuiBase.openGui(new GuiLitematicaBoxLayer(screen))
        );
    }

    @Unique
    private void crosspatch$addInlineEditor(GuiBase gui, Screen screen) {
        int x = 120;
        int y = 60;

        gui.addLabel(
                x,
                y,
                200,
                12,
                0xFFFFFFFF,
                StringUtils.translate(Reference.MOD_ID + ".gui.title.inline_box_layer")
        );

        y += 18;

        y = this.crosspatch$createAxisSection(
                gui,
                x,
                y,
                AxisType.X,
                "X",
                Configs.Litematica.BOX_LAYER_X_MAX_ENABLED,
                Configs.Litematica.BOX_LAYER_X_MAX_SELECTED,
                Configs.Litematica.BOX_LAYER_X_MAX_VALUE,
                Configs.Litematica.BOX_LAYER_X_MIN_ENABLED,
                Configs.Litematica.BOX_LAYER_X_MIN_SELECTED,
                Configs.Litematica.BOX_LAYER_X_MIN_VALUE
        );

        y += 6;

        y = this.crosspatch$createAxisSection(
                gui,
                x,
                y,
                AxisType.Y,
                "Y",
                Configs.Litematica.BOX_LAYER_Y_MAX_ENABLED,
                Configs.Litematica.BOX_LAYER_Y_MAX_SELECTED,
                Configs.Litematica.BOX_LAYER_Y_MAX_VALUE,
                Configs.Litematica.BOX_LAYER_Y_MIN_ENABLED,
                Configs.Litematica.BOX_LAYER_Y_MIN_SELECTED,
                Configs.Litematica.BOX_LAYER_Y_MIN_VALUE
        );

        y += 6;

        this.crosspatch$createAxisSection(
                gui,
                x,
                y,
                AxisType.Z,
                "Z",
                Configs.Litematica.BOX_LAYER_Z_MAX_ENABLED,
                Configs.Litematica.BOX_LAYER_Z_MAX_SELECTED,
                Configs.Litematica.BOX_LAYER_Z_MAX_VALUE,
                Configs.Litematica.BOX_LAYER_Z_MIN_ENABLED,
                Configs.Litematica.BOX_LAYER_Z_MIN_SELECTED,
                Configs.Litematica.BOX_LAYER_Z_MIN_VALUE
        );

        this.crosspatch$updateTextFieldValues();
    }

    @Unique
    private int crosspatch$createAxisSection(
            GuiBase gui,
            int x,
            int y,
            AxisType axis,
            String axisName,
            ConfigBoolean maxEnabled,
            ConfigBoolean maxHotkey,
            ConfigInteger maxValue,
            ConfigBoolean minEnabled,
            ConfigBoolean minHotkey,
            ConfigInteger minValue
    ) {
        gui.addLabel(
                x,
                y,
                100,
                12,
                0xFFFFFFFF,
                StringUtils.translate(Reference.MOD_ID + ".gui.label.axis", axisName)
        );

        y += 13;

        this.crosspatch$createRow(
                gui,
                x,
                y,
                axis,
                Reference.MOD_ID + ".gui.label.max_layer",
                maxEnabled,
                maxHotkey,
                maxValue
        );

        y += 21;

        this.crosspatch$createRow(
                gui,
                x,
                y,
                axis,
                Reference.MOD_ID + ".gui.label.min_layer",
                minEnabled,
                minHotkey,
                minValue
        );

        return y + 21;
    }

    @Unique
    private void crosspatch$createRow(
            GuiBase gui,
            int x,
            int y,
            AxisType axis,
            String labelKey,
            ConfigBoolean enabled,
            ConfigBoolean hotkey,
            ConfigInteger value
    ) {
        String label = StringUtils.translate(labelKey) + ":";

        int labelWidth = 70;
        int textFieldWidth = 64;

        gui.addLabel(x, y + 5, labelWidth, 12, 0xFFFFFFFF, label);

        x += labelWidth + 8;

        GuiTextFieldGeneric textField = TextFields.integerField(
                gui,
                Minecraft.getInstance().font,
                x,
                y,
                textFieldWidth,
                value,
                this::crosspatch$onConfigChanged
        );

        this.crosspatch$textFields.add(textField);

        x += textFieldWidth + 3;

        ButtonGeneric adjustButton = Buttons.valueAdjustButton(
                gui,
                x,
                y,
                value,
                this::crosspatch$onConfigChangedAndRefresh
        );

        x += adjustButton.getWidth() + 4;

        ButtonGeneric setHereButton = Buttons.setHereButton(
                gui,
                x,
                y,
                axis,
                value,
                this::crosspatch$onConfigChangedAndRefresh
        );

        x += setHereButton.getWidth() + 8;

        CheckBoxes.enableCheckBox(
                gui,
                x,
                y + 4,
                enabled,
                this::crosspatch$onConfigChangedAndRefresh
        );

        CheckBoxes.hotkeyCheckBox(
                gui,
                x + 60,
                y + 4,
                hotkey,
                this::crosspatch$onConfigChangedAndRefresh
        );
    }

    @Unique
    private void crosspatch$onConfigChanged() {
        Configs.saveToFile();
        BoxLayerController.refreshSchematic();
    }

    @Unique
    private void crosspatch$onConfigChangedAndRefresh() {
        this.crosspatch$onConfigChanged();
        ((GuiBase) (Object) this).initGui();
    }

    @Unique
    private void crosspatch$updateTextFieldValues() {
        int index = 0;

        this.crosspatch$setTextFieldValue(index++, Configs.Litematica.BOX_LAYER_X_MAX_VALUE);
        this.crosspatch$setTextFieldValue(index++, Configs.Litematica.BOX_LAYER_X_MIN_VALUE);

        this.crosspatch$setTextFieldValue(index++, Configs.Litematica.BOX_LAYER_Y_MAX_VALUE);
        this.crosspatch$setTextFieldValue(index++, Configs.Litematica.BOX_LAYER_Y_MIN_VALUE);

        this.crosspatch$setTextFieldValue(index++, Configs.Litematica.BOX_LAYER_Z_MAX_VALUE);
        this.crosspatch$setTextFieldValue(index, Configs.Litematica.BOX_LAYER_Z_MIN_VALUE);
    }

    @Unique
    private void crosspatch$setTextFieldValue(int index, ConfigInteger value) {
        if (index >= 0 && index < this.crosspatch$textFields.size()) {
            this.crosspatch$textFields.get(index).setValue(String.valueOf(value.getIntegerValue()));
        }
    }
}