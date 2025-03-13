package dev.taway.catnip.service.music;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.data.MusicCacheEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

@Service
public class MetadataService {

    private static final Logger log = LogManager.getLogger(MetadataService.class);

    public MusicCacheEntry fetchMetadata(String url) {
        try {
            Process process = createMetadataProcess(url).start();
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> output = handleProcessOutput(process);
            process.waitFor();
            return parseMetadata(output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve metadata for URL: " + url, e);
        }
    }

    private ProcessBuilder createMetadataProcess(String url) {
        return new ProcessBuilder(
                "yt-dlp",
                "--print", "{\\\"title\\\": %(title)j, \\\"channel\\\": %(channel)j, \\\"duration\\\": %(duration)j}",
                url
        );
    }

    private MusicCacheEntry parseMetadata(AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> processOutput) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = null;

        for (String str : processOutput.getKey()) {
            try {
                data = mapper.readValue(str, Map.class);
                if (data.containsKey("title")) break;
            } catch (JsonProcessingException ignored) {
            }
        }

        if (data == null || !data.containsKey("title")) {
            for (String str : processOutput.getValue()) {
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

        MusicCacheEntry entry = new MusicCacheEntry();
        entry.setTitle(data.get("title").toString());
        entry.setArtist(data.get("channel").toString());
        entry.setDuration(Double.parseDouble(data.get("duration").toString()));
        return entry;
    }

    private AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> handleProcessOutput(Process process)
            throws Exception {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> errLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
            while ((line = errReader.readLine()) != null) errLines.add(line);
        }

        return new AbstractMap.SimpleEntry<>(lines, errLines);
    }
}
