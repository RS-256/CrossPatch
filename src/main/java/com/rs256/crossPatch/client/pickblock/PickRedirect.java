package com.rs256.crossPatch.client.pickblock;

import com.rs256.crossPatch.client.config.Configs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Parses {@link Configs.PickBlock#PICK_BLOCK_PRO_PICK_REDIRECT} and maps picked blocks to a
 * replacement item.
 *
 * <p>Each config entry is {@code "<sourceBlockId> -> <targetItemId>"}; the separator may be an
 * arrow ({@code ->}), an equals sign, or whitespace. The source is resolved against the block
 * registry, the target against the item registry. Unparseable or unknown ids are skipped, so a
 * bad entry never breaks the rest of the list.</p>
 */
public final class PickRedirect {
    private PickRedirect() {
    }

    /**
     * @return {@code true} if any redirect entries are configured.
     */
    public static boolean isEnabled() {
        return !Configs.PickBlock.PICK_BLOCK_PRO_PICK_REDIRECT.getStrings().isEmpty();
    }

    /**
     * Looks up the redirect target for {@code source}.
     *
     * @return the item to give instead of picking {@code source}, or {@code null} if {@code source}
     * has no (valid) redirect entry.
     */
    public static Item lookup(Block source) {
        List<String> entries = Configs.PickBlock.PICK_BLOCK_PRO_PICK_REDIRECT.getStrings();

        for (String entry : entries) {
            String[] pair = splitEntry(entry);
            if (pair == null) {
                continue;
            }

            Identifier sourceId = Identifier.tryParse(pair[0]);
            if (sourceId == null || BuiltInRegistries.BLOCK.getOptional(sourceId).orElse(null) != source) {
                continue;
            }

            Identifier targetId = Identifier.tryParse(pair[1]);
            if (targetId == null) {
                continue;
            }

            Item target = BuiltInRegistries.ITEM.getOptional(targetId).orElse(null);
            if (target != null) {
                return target;
            }
        }

        return null;
    }

    /**
     * Splits an entry into {@code [sourceId, targetId]}, treating {@code ->}, {@code =} and
     * whitespace as separators. Returns {@code null} when fewer than two tokens are present.
     */
    private static String[] splitEntry(String entry) {
        String normalised = entry.replace("->", " ").replace("=", " ").trim();
        if (normalised.isEmpty()) {
            return null;
        }

        String[] tokens = normalised.split("\\s+");
        if (tokens.length < 2) {
            return null;
        }
        return new String[]{tokens[0], tokens[1]};
    }
}
