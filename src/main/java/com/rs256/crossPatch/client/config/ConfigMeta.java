package com.rs256.crossPatch.client.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Non-translatable metadata attached to a config: relationships to other mods or
 * configs that are evaluated at runtime (e.g. "requires Litematica") and surfaced
 * in the hover text. Unlike the name/hover strings this never goes through the
 * translation system, so the mod ids it carries are shown verbatim.
 *
 * <p>Immutable; build instances with the {@code with*} helpers off {@link #EMPTY}.
 * {@link #requiredMods()} are hard dependencies; {@link #suggestedMods()} are
 * optional companions; {@link #seeAlso()} are references to related configs (their
 * display names come from the lang files, the relationship itself lives here).
 * Further relationships (incompatible / conflicted) can be added as additional
 * lists alongside these.
 */
public record ConfigMeta(
        List<String> requiredMods,
        List<String> suggestedMods,
        List<String> seeAlso
) {
    public static final ConfigMeta EMPTY = new ConfigMeta(List.of(), List.of(), List.of());

    public ConfigMeta {
        requiredMods = List.copyOf(requiredMods);
        suggestedMods = List.copyOf(suggestedMods);
        seeAlso = List.copyOf(seeAlso);
    }

    /** Returns a copy with the given mod ids appended to {@link #requiredMods()}. */
    public ConfigMeta withRequired(String... mods) {
        return new ConfigMeta(append(this.requiredMods, mods), this.suggestedMods, this.seeAlso);
    }

    /** Returns a copy with the given mod ids appended to {@link #suggestedMods()}. */
    public ConfigMeta withSuggested(String... mods) {
        return new ConfigMeta(this.requiredMods, append(this.suggestedMods, mods), this.seeAlso);
    }

    /** Returns a copy with the given config references appended to {@link #seeAlso()}. */
    public ConfigMeta withSeeAlso(String... refs) {
        return new ConfigMeta(this.requiredMods, this.suggestedMods, append(this.seeAlso, refs));
    }

    public boolean isEmpty() {
        return this.requiredMods.isEmpty() && this.suggestedMods.isEmpty() && this.seeAlso.isEmpty();
    }

    private static List<String> append(List<String> base, String... extra) {
        List<String> merged = new ArrayList<>(base);
        Collections.addAll(merged, extra);
        return merged;
    }
}
