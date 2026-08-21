package com.rs256.crossPatch.client.mixin.malilib;

import com.rs256.crossPatch.client.config.ConfigQuery;
import com.rs256.crossPatch.client.config.ConfigTag;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Paints the option name red in the config list for configs tagged
 * {@link ConfigTag#EXPERIMENTAL}, pairing with the red warning that
 * {@code GuiConfigs} appends to the bottom of the hover text.
 *
 * <p>Scoped to the label built in {@code addConfigOption}: the same display name
 * is also used for the search filter and for the label column width, and those
 * must keep seeing the undecorated string. (The width would in fact still match
 * - the font renderer ignores {@code §} codes when measuring - but the search
 * filter lowercases and matches the raw text, so it must not.)
 *
 * <p>{@link ConfigQuery#hasTag} returns false for configs CrossPatch does not
 * own, so every other mod's config list renders unchanged.
 */
@Mixin(value = fi.dy.masa.malilib.gui.widgets.WidgetConfigOption.class, remap = false)
public class WidgetConfigOptionMixin {
    @Redirect(
            method = "addConfigOption",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/config/IConfigBase;getConfigGuiDisplayName()Ljava/lang/String;"
            ),
            remap = false
    )
    private String crosspatch$colorExperimentalName(IConfigBase config) {
        String name = config.getConfigGuiDisplayName();

        if (!ConfigQuery.hasTag(config, ConfigTag.EXPERIMENTAL)) {
            return name;
        }

        return GuiBase.TXT_RED + name + GuiBase.TXT_RST;
    }
}
