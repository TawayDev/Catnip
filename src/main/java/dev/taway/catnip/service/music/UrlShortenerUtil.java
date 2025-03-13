package dev.taway.catnip.service.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlShortenerUtil {
    public static String shortenURL(String url) {
        String regex = "(?:youtu\\.be/|youtube\\.com/(?:.*v=|embed/|v/|shorts/|live/))([a-zA-Z0-9_-]{11})";
        Matcher matcher = Pattern.compile(regex).matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
}