package dev.taway.catnip.service.music.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.config.CookiesFromBrowser;
import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.data.music.MusicCacheEntryBlockReason;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

@Service
public class MetadataService {

    private static final Logger log = LogManager.getLogger(MetadataService.class);
    private CatnipConfig config;

    @Autowired
    public MetadataService(CatnipConfig config) {
        this.config = config;
    }

    /**
     * Fetches metadata for a given URL using an external process (yt-dlp) and returns a MusicCacheEntry object.
     * The metadata includes details such as title, artist/channel, and duration.
     *
     * @param url The URL of the media for which metadata is to be fetched.
     * @return A MusicCacheEntry object containing the extracted metadata.
     * @throws RuntimeException If the metadata retrieval or parsing fails.
     */
    public MusicCacheEntry fetchMetadata(String url) {
        try {
            Process process = createMetadataProcess(url).start();
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> output = ProcessOutputUtil.handleProcessOutput(process);
            process.waitFor();
            return parseMetadata(output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve metadata for URL: " + url, e);
        }
    }

    private ProcessBuilder createMetadataProcess(String url) {
        if (config.getCookiesFromBrowser().equals(CookiesFromBrowser.NONE)) {
            return new ProcessBuilder(
                    "yt-dlp",
                    "--print", "{\\\"title\\\": %(title)j, \\\"channel\\\": %(channel)j, \\\"duration\\\": %(duration)j}",
                    url
            );
        } else {
            return new ProcessBuilder(
                    "yt-dlp",
                    "--print", "{\\\"title\\\": %(title)j, \\\"channel\\\": %(channel)j, \\\"duration\\\": %(duration)j}",
                    "--cookies-from-browser",
                    config.getCookiesFromBrowser().getName(),
                    url
            );
        }
    }

    private MusicCacheEntry parseMetadata(AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> processOutput) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = null;

        MusicCacheEntry entry = new MusicCacheEntry();

        for (String str : processOutput.getKey()) {
            if (str.contains("Sign in to confirm your age")) {
                return getAgeRestrictedEntry();
            }

            try {
                data = mapper.readValue(str, Map.class);
                if (data.containsKey("title")) break;
            } catch (JsonProcessingException ignored) {
            }
        }

        if (data == null || !data.containsKey("title")) {
            for (String str : processOutput.getValue()) {
                if (str.contains("Sign in to confirm your age")) {
                    return getAgeRestrictedEntry();
                }
                try {
                    data = mapper.readValue(str, Map.class);
                    if (data.containsKey("title")) break;
                } catch (JsonProcessingException ignored) {
                }
            }
        }

        if (data == null || !data.containsKey("title")) {
            throw new RuntimeException("Metadata extraction failed");
        }

        entry.setTitle(data.get("title").toString());
        entry.setArtist(data.get("channel").toString());
        entry.setDuration(Double.parseDouble(data.get("duration").toString()));
        return entry;
    }

    private MusicCacheEntry getAgeRestrictedEntry() {
        MusicCacheEntry entry = new MusicCacheEntry();
        entry.setTitle("N/A");
        entry.setArtist("N/A");
        entry.setDuration(0);
        entry.setBlocked(true);
        entry.setBlockReason(MusicCacheEntryBlockReason.AGE_RESTRICTED);
        return entry;
    }
}
