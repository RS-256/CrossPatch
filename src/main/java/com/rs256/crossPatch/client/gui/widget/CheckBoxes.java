package com.rs256.crossPatch.client.gui.widget;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.gui.Icons;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetCheckBox;
import fi.dy.masa.malilib.util.StringUtils;

public final class CheckBoxes {
    private CheckBoxes() {
    }

    public static WidgetCheckBox enableCheckBox(
            GuiBase gui,
            int x,
            int y,
            ConfigBoolean config,
            Runnable afterChange
    ) {
        return checkBox(
                gui,
                x,
                y,
                config,
                Reference.MOD_ID + ".gui.label.enable",
                Reference.MOD_ID + ".gui.hover.enable",
                afterChange
        );
    }

    public static WidgetCheckBox hotkeyCheckBox(
            GuiBase gui,
            int x,
            int y,
            ConfigBoolean config,
            Runnable afterChange
    ) {
        return checkBox(
                gui,
                x,
                y,
                config,
                Reference.MOD_ID + ".gui.label.hotkey",
                Reference.MOD_ID + ".gui.hover.hotkey",
                afterChange
        );
    }

    public static WidgetCheckBox checkBox(
            GuiBase gui,
            int x,
            int y,
            ConfigBoolean config,
            String labelKey,
            String hoverKey,
            Runnable afterChange
    ) {
        WidgetCheckBox checkBox = new WidgetCheckBox(
                x,
                y,
                Icons.CHECKBOX_UNSELECTED,
                Icons.CHECKBOX_SELECTED,
                StringUtils.translate(labelKey),
                StringUtils.translate(hoverKey)
        );

        checkBox.setChecked(config.getBooleanValue(), false);
        checkBox.setListener(new BooleanConfigCheckBoxListener(config, afterChange));

        gui.addWidget(checkBox);

        return checkBox;
    }

    private record BooleanConfigCheckBoxListener(
            ConfigBoolean config,
            Runnable afterChange
    ) implements ISelectionListener {
        @Override
        public void onSelectionChange(Object entry) {
            if (entry instanceof WidgetCheckBox checkBox) {
                this.config.setBooleanValue(checkBox.isChecked());

                Configs.saveToFile();
                BoxLayerController.refreshSchematic();

                if (this.afterChange != null) {
                    this.afterChange.run();
                }
            }
        }
    }
}