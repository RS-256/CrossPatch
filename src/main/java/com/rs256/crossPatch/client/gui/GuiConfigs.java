package com.rs256.crossPatch.client.gui;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.ConfigQuery;
import com.rs256.crossPatch.client.config.ConfigTag;
import com.rs256.crossPatch.client.gui.litematica.GuiLitematicaBoxLayer;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GuiConfigs extends GuiConfigsBase {
    private static ConfigGuiTab currentTab = ConfigGuiTab.ALL;

    public GuiConfigs(@Nullable Screen parent) {
        super(
                10,
                50,
                Reference.MOD_ID,
                parent,
                Reference.MOD_ID + ".gui.title.configs"
        );
    }

    @Override
    public void initGui() {
        super.initGui();

        int x = 10;
        int y = 26;

        x += this.createTabButton(x, y, ConfigGuiTab.ALL) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.LITEMATICA) + 8;

        if (currentTab == ConfigGuiTab.LITEMATICA) {
            this.createOpenLayerGuiButton(x, y);
        }
    }

    private int createTabButton(int x, int y, ConfigGuiTab tab) {
        String label = tab == currentTab
                ? GuiBase.TXT_GREEN + tab.getDisplayName() + GuiBase.TXT_RST
                : tab.getDisplayName();

        ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, label);

        this.addButton(button, new TabButtonListener(tab, this));

        return button.getWidth();
    }

    private void createOpenLayerGuiButton(int x, int y) {
        ButtonGeneric button = new ButtonGeneric(
                x,
                y,
                -1,
                20,
                StringUtils.translate(Reference.MOD_ID + ".gui.button.open_litematica_box_layer")
        );

        button.setEnabled(FabricLoader.getInstance().isModLoaded("litematica"));

        this.addButton(button, new OpenLayerGuiButtonListener(this));
    }

    @Override
    protected int getConfigWidth() {
        return 220;
    }

    @Override
    protected boolean useKeybindSearch() {
        return true;
    }

    @Override
    public List getConfigs() {
        return ConfigOptionWrapper.createFor(currentTab.getConfigs());
    }

    private enum ConfigGuiTab {
        ALL(null, "crosspatch.gui.button.tab.all"),
        LITEMATICA(ConfigTag.LITEMATICA, "crosspatch.gui.button.tab.litematica");

        private final ConfigTag modTag;
        private final String translationKey;

        ConfigGuiTab(ConfigTag modTag, String translationKey) {
            this.modTag = modTag;
            this.translationKey = translationKey;
        }

        public List<IConfigBase> getConfigs() {
            if (this.modTag == null) {
                return ConfigQuery.viewableAll();
            }

            return ConfigQuery.viewableFor(this.modTag);
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }
    }

    private record TabButtonListener(
            ConfigGuiTab tab,
            GuiConfigs gui
    ) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            currentTab = this.tab;
            this.gui.initGui();
        }
    }

    private record OpenLayerGuiButtonListener(
            GuiConfigs gui
    ) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (FabricLoader.getInstance().isModLoaded("litematica")) {
                GuiBase.openGui(new GuiLitematicaBoxLayer(this.gui));
            }
        }
    }
}