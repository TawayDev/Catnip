package dev.taway.catnip.service.music.cache;

import java.io.File;
import java.text.Normalizer;

public class FileUtil {

    /**
     * Normalizes a file name by removing special characters, replacing spaces with a single space,
     * trimming leading/trailing whitespace, and converting it to lowercase.
     * This ensures the file name is safe for use in file systems.
     *
     * @param fileName The original file name to be normalized.
     * @return A normalized version of the file name.
     */
    public static String normalizeFileName(String fileName) {
        return Normalizer.normalize(fileName, Normalizer.Form.NFKC)
                .replaceAll("[^\\w\\s.-]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    /**
     * Checks if a file exists at the specified path.
     *
     * @param path The file path to check.
     * @return {@code true} if the file exists, {@code false} otherwise.
     * Returns {@code false} if the path is null.
     */
    public static boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }
}
