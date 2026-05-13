package com.rs256.crossPatch.client.gui.widget;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.Configs;
import com.rs256.crossPatch.client.litematica.layer.BoxLayerController;
import fi.dy.masa.litematica.gui.Icons;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public final class Buttons {
    private Buttons() {
    }

    public static ButtonGeneric setHereButton(
            GuiBase gui,
            int x,
            int y,
            AxisType axis,
            ConfigInteger value,
            Runnable afterChange
    ) {
        return setHereButton(
                gui,
                x,
                y,
                Reference.MOD_ID + ".gui.button.set_here",
                axis,
                value,
                afterChange
        );
    }

    public static ButtonGeneric setHereButton(
            GuiBase gui,
            int x,
            int y,
            String labelKey,
            AxisType axis,
            ConfigInteger value,
            Runnable afterChange
    ) {
        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                -1,
                20,
                StringUtils.translate(labelKey)
        );

        gui.addButton(
                button,
                new SetHereListener(axis, value, afterChange)
        );

        return button;
    }

    public static ButtonGeneric valueAdjustButton(
            GuiBase gui,
            int x,
            int y,
            ConfigInteger value,
            Runnable afterChange
    ) {
        ButtonGeneric button = new ButtonGeneric(
                x,
                y + 2,
                Icons.BUTTON_PLUS_MINUS_16
        );

        gui.addButton(
                button,
                new ValueAdjustListener(value, afterChange)
        );

        return button;
    }

    public static ButtonGeneric actionButton(
            GuiBase gui,
            int x,
            int y,
            String labelKey,
            IButtonActionListener listener
    ) {
        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                -1,
                20,
                StringUtils.translate(labelKey)
        );

        gui.addButton(button, listener);

        return button;
    }

    private record SetHereListener(
            AxisType axis,
            ConfigInteger value,
            Runnable afterChange
    ) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            Entity entity = EntityUtils.getCameraEntity();

            if (entity == null) {
                return;
            }

            BlockPos pos = entity.blockPosition();

            this.value.setIntegerValue(this.axis.get(pos));

            if (this.afterChange != null) {
                this.afterChange.run();
            }
        }
    }

    private record ValueAdjustListener(
            ConfigInteger value,
            Runnable afterChange
    ) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            int change = mouseButton == 1 ? -1 : 1;

            if (GuiBase.isShiftDown()) {
                change *= 16;
            }

            if (GuiBase.isCtrlDown()) {
                change *= 64;
            }

            this.value.setIntegerValue(this.value.getIntegerValue() + change);

            if (this.afterChange != null) {
                this.afterChange.run();
            }
        }
    }
}