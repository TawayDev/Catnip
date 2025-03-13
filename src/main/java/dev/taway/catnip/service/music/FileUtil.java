package dev.taway.catnip.service.music;

import java.io.File;
import java.text.Normalizer;

public class FileUtil {
    public static String normalizeFileName(String fileName) {
        return Normalizer.normalize(fileName, Normalizer.Form.NFKC)
                .replaceAll("[^\\w\\s.-]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    public static boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }
}
