package com.rs256.crossPatch.client.litematica.cache;

import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.client.config.Configs;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keeps parsed {@link LitematicaSchematic}s alive across a dimension change,
 * for the {@code optimizedLitematicLoading} option.
 *
 * <p>Litematica drops its whole {@code SchematicHolder} on every world load
 * ({@code SchematicPlacementManager#clear} → {@code clearLoadedSchematics}),
 * and then {@code SchematicPlacement#fromJson} re-reads every placed .litematic
 * from disk through {@code SchematicHolder#getOrLoad}, before it even looks at
 * whether the placement is enabled or rendered. Hopping to the Nether and back
 * therefore re-parses, data-fixes and re-allocates every placed schematic twice,
 * which is what makes large hidden placements so expensive.
 *
 * <p>This cache sits <em>behind</em> {@code SchematicHolder}, not in place of
 * it: {@code SchematicHolderMixin} only diverts the {@code createFromFile} call
 * that {@code getOrLoad} makes on a miss. Litematica's own list is still built
 * and cleared exactly as before, so the "Loaded Schematics" GUI, the unload
 * button and everything else keep their current behaviour - the file just is
 * not read a second time.
 *
 * <p>Entries are dropped when:
 * <ul>
 *   <li>the connection's registries change (a different server, or a re-login),
 *       since block states were resolved against them - checked on every access
 *       so it does not depend on world-load listener ordering;</li>
 *   <li>the file's size or modification time changed, so re-saving a schematic
 *       is still picked up;</li>
 *   <li>Litematica unloads the schematic ({@code removeSchematic});</li>
 *   <li>the world is left, or the option is switched off.</li>
 * </ul>
 */
public final class LitematicCache implements IWorldLoadListener {
    private static final LitematicCache INSTANCE = new LitematicCache();

    private final Map<Path, Entry> entries = new HashMap<>();

    /**
     * Identity of the registries the cached schematics were parsed against. Held
     * as a bare reference and compared by identity only, never dereferenced: the
     * client's {@code ClientPacketListener} hands the same frozen instance to
     * every {@code ClientLevel} it creates, so this changes on a re-login but not
     * on a dimension change - exactly the distinction the cache needs.
     */
    @Nullable
    private RegistryAccess registries;

    private LitematicCache() {
    }

    public static LitematicCache getInstance() {
        return INSTANCE;
    }

    /**
     * Only call when Litematica is loaded — this touches Litematica classes.
     */
    public static void init() {
        Configs.Litematica.OPTIMIZED_LITEMATIC_LOADING.setValueChangeCallback(config -> {
            if (!config.getBooleanValue()) {
                INSTANCE.clear();
            }
        });
    }

    /**
     * The cached schematic for {@code file}, or {@code null} when it has to be
     * read from disk. A stale entry (file changed on disk, or parsed against
     * different registries) is dropped here rather than returned.
     */
    @Nullable
    public synchronized LitematicaSchematic get(Path file) {
        if (!isEnabled()) {
            return null;
        }

        this.dropIfRegistriesChanged();

        Entry entry = this.entries.get(file);

        if (entry == null) {
            return null;
        }

        Entry current = Entry.of(entry.schematic(), file);

        if (current == null || !current.matchesFileState(entry)) {
            CrossPatch.LOGGER.debug("LitematicCache: '{}' changed on disk, re-reading it.", file);
            this.entries.remove(file);

            return null;
        }

        return entry.schematic();
    }

    /** Remembers a freshly read schematic under the path it was read from. */
    public synchronized void put(Path file, LitematicaSchematic schematic) {
        if (!isEnabled()) {
            return;
        }

        this.dropIfRegistriesChanged();

        Entry entry = Entry.of(schematic, file);

        if (entry != null) {
            this.entries.put(file, entry);
        }
    }

    /**
     * Forgets a schematic Litematica is unloading, so that "unload schematic"
     * does not silently come back from the cache on the next placement load.
     */
    public synchronized void remove(@Nullable LitematicaSchematic schematic) {
        if (schematic == null || this.entries.isEmpty()) {
            return;
        }

        Iterator<Entry> it = this.entries.values().iterator();

        while (it.hasNext()) {
            if (it.next().schematic() == schematic) {
                it.remove();
            }
        }
    }

    public synchronized void clear() {
        this.entries.clear();
        this.registries = null;
    }

    @Override
    public void onWorldLoadPost(@Nullable ClientLevel worldBefore, @Nullable ClientLevel worldAfter, Minecraft mc) {
        // Dimension changes are handled by the registry check on access; this is
        // only here to let go of the parsed schematics when leaving the world.
        if (worldAfter == null) {
            this.clear();
        }
    }

    private static boolean isEnabled() {
        return Configs.Litematica.OPTIMIZED_LITEMATIC_LOADING.getBooleanValue();
    }

    private void dropIfRegistriesChanged() {
        ClientLevel level = Minecraft.getInstance().level;
        RegistryAccess current = level != null ? level.registryAccess() : null;

        if (current != this.registries) {
            if (!this.entries.isEmpty()) {
                CrossPatch.LOGGER.debug(
                        "LitematicCache: registries changed, dropping {} cached schematic(s).",
                        this.entries.size());
            }

            this.entries.clear();
            this.registries = current;
        }
    }

    /**
     * A cached schematic together with the state of the file it was read from.
     * Size and modification time are cheap to re-stat and catch the realistic
     * ways a .litematic changes under us (re-saved from Litematica itself, or
     * replaced from outside the game).
     */
    private record Entry(LitematicaSchematic schematic, long lastModified, long size) {
        @Nullable
        static Entry of(LitematicaSchematic schematic, Path file) {
            try {
                if (!Files.isReadable(file)) {
                    return null;
                }

                return new Entry(schematic, Files.getLastModifiedTime(file).toMillis(), Files.size(file));
            } catch (IOException e) {
                return null;
            }
        }

        boolean matchesFileState(Entry other) {
            return this.lastModified == other.lastModified && this.size == other.size;
        }
    }
}
