package com.rs256.crossPatch.client.gui;

import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.ConfigQuery;
import com.rs256.crossPatch.client.config.ConfigTag;
import com.rs256.crossPatch.client.config.TaggedConfig;
import com.rs256.crossPatch.client.config.lang.CrossPatchI18n;
import com.rs256.crossPatch.client.gui.litematica.GuiLitematicaBoxLayer;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigs extends GuiConfigsBase {
    private static ConfigGuiTab currentTab = ConfigGuiTab.ALL;

    public GuiConfigs(@Nullable Screen parent) {
        super(
                10,
                50,
                Reference.MOD_ID,
                parent,
                Reference.MOD_ID + ".gui.title.configs",
                CrossPatch.VERSION
        );

        // Wrap the config hover text so long comments fold instead of running
        // off-screen. The wrap width tracks the current screen size because the
        // config list (and thus the hover widgets) is rebuilt on every resize.
        this.setHoverInfoProvider(new WrappingHoverInfoProvider());
    }

    @Override
    public void initGui() {
        super.initGui();

        int x = 10;
        int y = 26;

        x += this.createTabButton(x, y, ConfigGuiTab.ALL) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.CROSSPATCH) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.GENERIC) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.LITEMATICA) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.ITEMSCROLLER) + 2;
        x += this.createTabButton(x, y, ConfigGuiTab.TWEAKERMORE) + 8;

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

    /**
     * Builds each config's hover text: the (white) comment body folded to roughly
     * half the current screen width, followed by grey metadata sections — a
     * "see also" list of related configs and a "required" mod checklist whose
     * boxes are ticked from the live mod-presence state. Lines are joined with
     * {@code \n}, which malilib's hover widget renders as separate lines, and the
     * {@code §} colour codes are honoured by the font renderer.
     */
    private final class WrappingHoverInfoProvider implements IConfigInfoProvider {
        @Override
        public String getHoverInfo(IConfigBase config) {
            List<String> lines = new ArrayList<>();
            int maxLineWidth = Math.max(1, GuiConfigs.this.width / 2);

            String comment = config.getComment();
            if (comment != null && !comment.isEmpty()) {
                StringUtils.splitTextToLines(lines, comment, maxLineWidth);
            }

            TaggedConfig entry = ConfigQuery.entryOf(config);
            if (entry != null) {
                appendSeeAlso(lines, entry, maxLineWidth);
                appendRequired(lines, entry, maxLineWidth);
                appendSuggested(lines, entry, maxLineWidth);
            }

            return String.join("\n", lines);
        }

        private void appendSeeAlso(List<String> lines, TaggedConfig entry, int maxLineWidth) {
            List<String> refs = entry.meta().seeAlso();

            if (refs.isEmpty()) {
                return;
            }

            addColoredWrapped(lines, StringUtils.translate("crosspatch.gui.hover.see_also"),
                    GuiBase.TXT_AQUA, maxLineWidth);

            String defaultGroup = ConfigQuery.groupOf(entry);
            for (String ref : refs) {
                String name = StringUtils.translate(CrossPatchI18n.nameKey(ref, defaultGroup));
                addColoredWrapped(lines, "- " + name, GuiBase.TXT_GRAY, maxLineWidth);
            }
        }

        private void appendRequired(List<String> lines, TaggedConfig entry, int maxLineWidth) {
            List<String> required = entry.meta().requiredMods();

            if (required.isEmpty()) {
                return;
            }

            addColoredWrapped(lines, StringUtils.translate("crosspatch.gui.hover.required"),
                    GuiBase.TXT_GOLD, maxLineWidth);

            for (String mod : required) {
                boolean present = FabricLoader.getInstance().isModLoaded(mod);
                String box = present ? "[x] " : "[ ] ";
                addColoredWrapped(lines, box + mod, GuiBase.TXT_GRAY, maxLineWidth);
            }
        }

        private void appendSuggested(List<String> lines, TaggedConfig entry, int maxLineWidth) {
            List<String> suggested = entry.meta().suggestedMods();

            if (suggested.isEmpty()) {
                return;
            }

            addColoredWrapped(lines, StringUtils.translate("crosspatch.gui.hover.suggested"),
                    GuiBase.TXT_YELLOW, maxLineWidth);

            for (String mod : suggested) {
                boolean present = FabricLoader.getInstance().isModLoaded(mod);
                String box = present ? "[x] " : "[ ] ";
                addColoredWrapped(lines, box + mod, GuiBase.TXT_GRAY, maxLineWidth);
            }
        }

        /**
         * Wraps {@code text} to {@code maxLineWidth} like the comment body, then adds
         * each wrapped line in {@code color}. The colour is re-applied per line because
         * the font renderer resets formatting at every {@code \n}.
         */
        private void addColoredWrapped(List<String> lines, String text, String color, int maxLineWidth) {
            List<String> wrapped = new ArrayList<>();
            StringUtils.splitTextToLines(wrapped, text, maxLineWidth);

            for (String line : wrapped) {
                lines.add(color + line + GuiBase.TXT_RST);
            }
        }
    }

    private enum ConfigGuiTab {
        ALL(null, "crosspatch.gui.button.tab.all"),
        CROSSPATCH(ConfigTag.CROSSPATCH, "crosspatch.gui.button.tab.crosspatch"),
        GENERIC(ConfigTag.GENERIC, "crosspatch.gui.button.tab.generic"),
        LITEMATICA(ConfigTag.LITEMATICA, "crosspatch.gui.button.tab.litematica"),
        ITEMSCROLLER(ConfigTag.ITEMSCROLLER, "crosspatch.gui.button.tab.itemscroller"),
        TWEAKERMORE(ConfigTag.TWEAKERMORE, "crosspatch.gui.button.tab.tweakermore");

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
