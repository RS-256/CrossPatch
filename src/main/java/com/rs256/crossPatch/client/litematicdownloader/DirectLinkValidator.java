package com.rs256.crossPatch.client.litematicdownloader;

import com.rs256.crossPatch.Reference;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

/**
 * Validates that a user supplied string is a direct link to a downloadable
 * litematic file (as opposed to a web page, a directory listing, an indirect
 * share link, etc.).
 *
 * <p>The check is purely syntactic - it never touches the network. A link is
 * accepted only when it is an {@code http(s)} URL whose path ends in a concrete
 * file name with an {@link #ALLOWED_EXTENSIONS allowed} extension.
 */
public final class DirectLinkValidator {
    /** File extensions accepted as a direct litematic download. */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("litematic", "schematic", "schema", "schem", "nbt", "dat");

    private static final String MESSAGE_PREFIX = Reference.MOD_ID + ".message.litematic_downloader.";

    private DirectLinkValidator() {
    }

    /**
     * Outcome of validating a link.
     *
     * <p>When {@link #valid()} is {@code true}, {@link #uri()} and
     * {@link #fileName()} are non-null. Otherwise {@link #errorKey()} holds the
     * translation key describing why the link was rejected.
     */
    public record Result(boolean valid, URI uri, String fileName, String errorKey) {
        private static Result invalid(String errorKey) {
            return new Result(false, null, null, errorKey);
        }

        private static Result valid(URI uri, String fileName) {
            return new Result(true, uri, fileName, null);
        }
    }

    public static Result validate(String rawLink) {
        if (rawLink == null || rawLink.isBlank()) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_empty");
        }

        URI uri;

        try {
            uri = new URI(rawLink.trim());
        } catch (URISyntaxException e) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_url");
        }

        String scheme = uri.getScheme();

        if (scheme == null
                || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_scheme");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_url");
        }

        String path = uri.getPath();

        if (path == null || path.isBlank() || path.endsWith("/")) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_not_a_file");
        }

        String fileName = sanitizeFileName(path);

        if (fileName == null) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_not_a_file");
        }

        int dot = fileName.lastIndexOf('.');

        if (dot <= 0 || dot == fileName.length() - 1) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_not_a_file");
        }

        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.invalid(MESSAGE_PREFIX + "invalid_extension");
        }

        return Result.valid(uri, fileName);
    }

    /**
     * Extracts the bare file name from a URL path, guarding against path
     * traversal. Returns {@code null} when no usable file name remains.
     */
    private static String sanitizeFileName(String path) {
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        String decoded = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8);

        // getFileName() strips any separators a decoded segment might smuggle in.
        Path namePath = Paths.get(decoded).getFileName();

        if (namePath == null) {
            return null;
        }

        String name = namePath.toString();

        if (name.isBlank() || name.equals("..")) {
            return null;
        }

        return name;
    }
}
