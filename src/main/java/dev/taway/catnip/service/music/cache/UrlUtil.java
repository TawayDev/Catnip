package dev.taway.catnip.service.music.cache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {
    /**
     * Shortens a YouTube URL. Example: <br>
     * Input: {@code "https://youtu.be/lgzCxqQUU5g?si=OGS77kTm_KTKp8I0"} <br>
     * Output: {@code "lgzCxqQUU5g"}
     *
     * @param url YouTube URL to be shortened.
     * @return Shortened URL
     */
    public static String shortenURL(String url) {
//        11 is not a magic number. YouTube has 11 char long video IDs
        String regex = "(?:youtu\\.be/|youtube\\.com/(?:.*v=|embed/|v/|shorts/|live/))([a-zA-Z0-9_-]{11})";
        Matcher matcher = Pattern.compile(regex).matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Sanitizes a YouTube URL by removing extra parameters (e.g., playlists, tracking tokens)
     * and returning the standard watch URL format. <br>
     * Example: <br>
     * Input: {@code "https://www.youtube.com/watch?v=cvaIgq5j2Q8&list=PL0wqt_um4x0bsdViTJBmnl6KGMoSqxfZy"} <br>
     * Output: {@code "https://www.youtube.com/watch?v=cvaIgq5j2Q8"} <br>
     * Invalid input example: <br>
     * Input: {@code "invalid_url"} <br>
     * Output: {@code "https://www.youtube.com/watch?v=null"}
     *
     * @param url YouTube URL to sanitize
     * @return Sanitized YouTube watch URL, or URL with {@code "null"} if input is invalid
     */
    public static String sanitizeURL(String url) {
        String urlShortened = shortenURL(url);
        return String.format("https://www.youtube.com/watch?v=%s", urlShortened);
    }
}