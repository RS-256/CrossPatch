package com.rs256.crossPatch.client.litematicdownloader;

import com.rs256.crossPatch.CrossPatch;
import com.rs256.crossPatch.Reference;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextInput;
import fi.dy.masa.malilib.interfaces.IStringConsumer;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CrossPatch's own litematic downloader feature.
 *
 * <p>Accepts a direct link to a {@code .litematic} file, downloads it, and saves
 * it under {@code <.minecraft>/schematics/download/}. Only direct file links are
 * allowed (see {@link DirectLinkValidator}); the directory is created on demand.
 */
public final class LitematicDownloader {
    /** Reasonable upper bound for a pasted download URL. */
    private static final int MAX_LINK_LENGTH = 512;

    /** Hard cap on the downloaded file size, to avoid filling the disk. */
    private static final long MAX_DOWNLOAD_BYTES = 64L * 1024 * 1024;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private static final String MESSAGE_PREFIX = Reference.MOD_ID + ".message.litematic_downloader.";

    /** Single background worker so downloads never block the render thread. */
    private static final ExecutorService DOWNLOAD_EXECUTOR =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "CrossPatch-LitematicDownloader");
                thread.setDaemon(true);
                return thread;
            });

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    private LitematicDownloader() {
    }

    /**
     * Opens the modal that prompts the user for a litematic download link.
     */
    public static void openModal(@Nullable Screen parent) {
        GuiBase.openGui(new GuiTextInput(
                MAX_LINK_LENGTH,
                Reference.MOD_ID + ".gui.title.litematic_downloader",
                "",
                parent,
                new LinkConsumer()
        ));
    }

    /**
     * Validates the link and, if it is a direct litematic file link, kicks off the
     * download on a background thread. Runs on the client thread.
     */
    private static void submit(String link) {
        DirectLinkValidator.Result result = DirectLinkValidator.validate(link);

        if (!result.valid()) {
            InfoUtils.printActionbarMessage(result.errorKey());
            return;
        }

        InfoUtils.printActionbarMessage(MESSAGE_PREFIX + "downloading", result.fileName());
        DOWNLOAD_EXECUTOR.submit(() -> download(result.uri(), result.fileName()));
    }

    private static void download(URI uri, String fileName) {
        Path downloadDir = getDownloadDirectory();
        Path tmp = downloadDir.resolve(fileName + ".tmp");

        try {
            if (!FileUtils.createDirectoriesIfMissing(downloadDir)) {
                messageOnClientThread(MESSAGE_PREFIX + "directory_failed", downloadDir.toString());
                return;
            }

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", "CrossPatch/" + CrossPatch.VERSION)
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() / 100 != 2) {
                messageOnClientThread(MESSAGE_PREFIX + "http_error", String.valueOf(response.statusCode()));
                return;
            }

            copyToFile(response.body(), tmp);

            // Never overwrite: pick a free "name (n).ext" if the file already exists.
            Path target = resolveUniqueTarget(downloadDir, fileName);
            Files.move(tmp, target);

            messageOnClientThread(MESSAGE_PREFIX + "download_success", target.getFileName().toString());
        } catch (DownloadTooLargeException e) {
            deleteQuietly(tmp);
            messageOnClientThread(MESSAGE_PREFIX + "too_large", fileName);
        } catch (Exception e) {
            CrossPatch.LOGGER.warn("Failed to download litematic from {}", uri, e);
            deleteQuietly(tmp);
            messageOnClientThread(MESSAGE_PREFIX + "download_failed", fileName);
        }
    }

    private static void copyToFile(InputStream in, Path tmp) throws Exception {
        try (InputStream source = in;
             OutputStream out = Files.newOutputStream(tmp)) {
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;

            while ((read = source.read(buffer)) != -1) {
                total += read;

                if (total > MAX_DOWNLOAD_BYTES) {
                    throw new DownloadTooLargeException();
                }

                out.write(buffer, 0, read);
            }
        }
    }

    /**
     * Returns a path in {@code dir} that does not yet exist: {@code fileName} if it
     * is free, otherwise {@code name (1).ext}, {@code name (2).ext}, and so on -
     * mirroring how browsers / Windows de-duplicate downloads.
     */
    private static Path resolveUniqueTarget(Path dir, String fileName) {
        Path candidate = dir.resolve(fileName);

        if (!Files.exists(candidate)) {
            return candidate;
        }

        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot) : "";

        for (int i = 1; ; i++) {
            Path next = dir.resolve(base + " (" + i + ")" + extension);

            if (!Files.exists(next)) {
                return next;
            }
        }
    }

    private static Path getDownloadDirectory() {
        //? if <=1.21.11 {
        /*return FileUtils.getMinecraftDirectoryAsPath().resolve("schematics").resolve("download");
         *///?} else {
        return FileUtils.getMinecraftDirectory().resolve("schematics").resolve("download");
        //?}
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Best effort cleanup of the temp file.
        }
    }

    /** Posts an action bar message from any thread, hopping to the client thread. */
    private static void messageOnClientThread(String translationKey, Object... args) {
        Minecraft.getInstance().execute(() -> InfoUtils.printActionbarMessage(translationKey, args));
    }

    private record LinkConsumer() implements IStringConsumer {
        @Override
        public void setString(String link) {
            submit(link);
        }
    }

    private static final class DownloadTooLargeException extends RuntimeException {
    }
}
