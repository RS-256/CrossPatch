package com.rs256.crossPatch.client.gui.widget;

import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.GuiTextFieldInteger;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

public final class TextFields {
    private TextFields() {
    }

    public static GuiTextFieldGeneric integerField(
            GuiBase gui,
            Font font,
            int x,
            int y,
            int width,
            ConfigInteger config,
            Runnable afterChange
    ) {
        GuiTextFieldGeneric textField = new GuiTextFieldInteger(
                x,
                y,
                width,
                20,
                font
        );

        gui.addTextField(
                textField,
                new IntegerFieldListener(config, afterChange),
                TextFieldType.INTEGER
        );

        textField.setValue(String.valueOf(config.getIntegerValue()));

        return textField;
    }

    private record IntegerFieldListener(
            ConfigInteger config,
            Runnable afterChange
    ) implements ITextFieldListener {
        @Override
        public boolean onTextChange(EditBox textField) {
            int parsed;

            try {
                parsed = Integer.parseInt(textField.getValue());
            } catch (NumberFormatException e) {
                return false;
            }

            this.config.setIntegerValue(parsed);

            if (this.afterChange != null) {
                this.afterChange.run();
            }

            return true;
        }
    }
}