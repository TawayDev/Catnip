package dev.taway.catnip.service.music;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.MusicCacheEntry;
import dev.taway.catnip.service.file.FileWatchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MusicCacheService {
    private static final Logger log = LogManager.getLogger(MusicCacheService.class);
    private CatnipConfig config;
    private FileWatchService fileWatchService;

    private static final String MUSIC_CACHE_JSON_PATH = "/cache/music-cache.json";
    private final String FILE_CACHE_LOCATION = System.getProperty("user.dir") + "/cache/music/download/";
    private ArrayList<MusicCacheEntry> cacheEntries;

    @Autowired
    public MusicCacheService(CatnipConfig config, FileWatchService fileWatchService) {
        this.config = config;
        this.fileWatchService = fileWatchService;
    }

    @PostConstruct
    public void init() {
        File cacheFile = new File(System.getProperty("user.dir") + MUSIC_CACHE_JSON_PATH);
        if (!cacheFile.exists()) {
            log.warn("Music cache json was not loaded as it does not exist!");
            cacheEntries = new ArrayList<>();
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MusicCacheEntry[] entries = objectMapper.readValue(new File(System.getProperty("user.dir") + MUSIC_CACHE_JSON_PATH), MusicCacheEntry[].class);

            cacheEntries = new ArrayList<>(Arrays.asList(entries));

            log.info("Successfully loaded {} music cache entries", cacheEntries.size());
        } catch (IOException e) {
            log.error("Error while reading music cache JSON! {}", e.getMessage());
            cacheEntries = new ArrayList<>();
        }
//        TODO: Remove songs that have not been played for a long time
//        Cleanup entries after loading
        cleanupCacheEntries();
    }

    /***
     * Returns entry from cache if it has been cached already. Otherwise, null.
     * @param url_shortened Full YouTube URL of song
     * @return Returns optional
     */
    public Optional<MusicCacheEntry> getMusicCacheEntry(String url_shortened) {
        Optional<MusicCacheEntry> entry = existsInCache(url_shortened);

        if (entry.isPresent()) {
            log.debug("Found cache entry for \"{}\"", url_shortened);
        } else {
            log.debug("No cache entry for \"{}\"", url_shortened);
        }
        return entry;
    }

    /**
     * @param url_shortened Shortened URL (Not including the YouTube.com link part)
     * @return Returns cache entry or null depending on if it was found or not.
     */
    private Optional<MusicCacheEntry> existsInCache(String url_shortened) {
        return cacheEntries.stream().filter(
                entry1 -> entry1.getUrlShortened().equals(url_shortened)
        ).findFirst();
    }

    public Optional<MusicCacheEntry> cacheSong(String url) {
        String url_shortened = shortenURL(url);
//        Get music entry or null if it does not exist
        MusicCacheEntry entry = existsInCache(url_shortened).orElse(null);

        if (entry == null) {
//            Entry was not found. We need to download it
//            Handle meta:
            entry = getMeta(url);

            entry.setUrl(url);
            entry.setUrlShortened(url_shortened);

//            Check if song does not exceed allowed duration
            if (entry.getDuration() > config.getCache().getMaximumSongDurationSeconds()) {
                log.warn("[{}] Requested song exceeded allowed playtime  {}/{} seconds and will not be added to queue!",
                        entry.getUrlShortened(),
                        entry.getDuration(),
                        config.getCache().getMaximumSongDurationSeconds()
                );

                entry.setBlocked(true);

                log.debug("[{}] Cached song search result!", entry.getUrlShortened());
                cacheEntries.add(entry);
            } else {
//                Song does not exceed allowed duration so download it
                entry = downloadSong(entry);
                cacheEntries.add(entry);
            }
        }

        return Optional.of(entry);
    }

    private MusicCacheEntry downloadSong(MusicCacheEntry entry) {
        String url_shortened = shortenURL(entry.getUrl());
        log.debug("Downloading song \"{}\"", entry.getUrl());

//        Download process:
        try {
            Process process = createDownloadProcess(entry.getUrl()).start();
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> output = handleProcessOutput(process);

            process.waitFor();

//            Parse output:
            AbstractMap.SimpleEntry<MusicCacheEntry.LocalData, Boolean> parsedOutput = downloadProcessOutputToObject(output);
            MusicCacheEntry.LocalData localData = parsedOutput.getKey();

            if (localData != null) {
                if (parsedOutput.getValue()) {
                    log.info("[{}] Waiting for container correcting to finish!", url_shortened);
                    fileWatchService.waitForFile(localData.getFullPath()).get();
//                    here it continues after the file is found
                    log.info("[{}] Container correcting finished!", url_shortened);
                } else {
                    log.debug("[{}] Did not wait for container correcting as it was not happening!", url_shortened);
                }
                entry.setLocalData(localData);
            } else {
                log.error("[{}] downloadProcessOutputToObject returned no local data information!", url_shortened);
            }
        } catch (Exception e) {
            log.error("[{}] Unexpected error occurred while downloading song! ", url_shortened, e);
        }

        return entry;
    }

    /**
     * @param processOutput STD and STDERR of the yt-dlp process
     * @return First is entry with added local data. Second is boolean if container is correcting.
     */
    private AbstractMap.SimpleEntry<MusicCacheEntry.LocalData, Boolean> downloadProcessOutputToObject(
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> processOutput
    ) {
        AbstractMap.SimpleEntry<MusicCacheEntry.LocalData, Boolean> result = new AbstractMap.SimpleEntry<>(null, false);

        String destinationPath = null;
        boolean correctingContainer = false;

        for (String str : processOutput.getKey()) {
            String strLower = str.toLowerCase();
//            get if container is being corrected or not
            if (strLower.contains("correcting container")) {
                correctingContainer = true;
            }
//            get file destination path string from STD
            if (strLower.contains("destination:")) {
                int destinationIndex = strLower.indexOf("destination:");
                destinationPath = str.substring(destinationIndex + "destination:".length()).trim();
            }
        }

        if (destinationPath != null) {
            File file = new File(destinationPath);
            String path = file.getParent();
            String filenameWithExt = file.getName();
            int lastDotIndex = filenameWithExt.lastIndexOf('.');
            String filename = filenameWithExt.substring(0, lastDotIndex);
            String extension = filenameWithExt.substring(lastDotIndex + 1);

            MusicCacheEntry.LocalData localData = new MusicCacheEntry.LocalData(
                    destinationPath,
                    filename,
                    extension,
                    path,
                    System.currentTimeMillis(),
                    0L
            );

            log.trace("[downloadProcessOutputToObject] RETURNING RESULT: Container correcting: {}, {}", correctingContainer, localData.toString());

            result = new AbstractMap.SimpleEntry<>(localData, correctingContainer);
        } else {
            log.error("Destination path could not be extracted from yt-dlp output!");
        }

        return result;
    }

    private MusicCacheEntry getMeta(String url) {
        try {
            Process process = createMetadataCheckProcess(url).start();
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> output = handleProcessOutput(process);

            process.waitFor();

            return metaProcessOutputToObject(output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve metadata for URL: " + url, e);
        }
    }

    /**
     * Parses process output for getting video metadata
     *
     * @param processOutput STD and STDERR of the yt-dlp process
     * @return MusicCacheEntry with
     */
    private MusicCacheEntry metaProcessOutputToObject(
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> processOutput
    ) {

        MusicCacheEntry entry = new MusicCacheEntry();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = null;
        boolean found = false;

//        Checking stdout
        for (String str : processOutput.getKey()) {
            try {
                data = mapper.readValue(str, Map.class);
                if (data.containsKey("title")) {
                    found = true;
                    break;
                }
            } catch (JsonProcessingException e) {
                log.debug("Failed to parse JSON from stdout: {}", str);
            }
        }

//        Checking stderr
        if (!found) {
            for (String str : processOutput.getValue()) {
                try {
                    data = mapper.readValue(str, Map.class);
                    if (data.containsKey("title")) {
                        found = true;
                        break;
                    }
                } catch (JsonProcessingException e) {
                    log.debug("Failed to parse JSON from stderr: {}", str);
                }
            }
        }

        if (!found) {
            log.error("Metadata extraction failed: Valid JSON not found in stdout or stderr!");
            return null;
        }

//        JSON format: {"title": "Example title", "channel": "Example channel", "duration": 69}
        entry.setTitle(data.getOrDefault("title", "N/A").toString());
        entry.setArtist(data.getOrDefault("channel", "N/A").toString());

        String durationStr = data.getOrDefault("duration", "0").toString();
        entry.setDuration(Double.parseDouble(durationStr));

        return entry;
    }

    /**
     * @param process Process which will have its output read.
     * @return Pair of lines and error lines.
     * @throws Exception When either of buffered readers cannot be opened.
     */
    private AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> handleProcessOutput(Process process) throws Exception {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> errLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[yt-dlp - STD] {}", line);
                lines.add(line);
            }

            while ((line = readerError.readLine()) != null) {
                log.debug("[yt-dlp - STDERR] {}", line);
                errLines.add(line);
            }
        }

        process.waitFor();
        return new AbstractMap.SimpleEntry<>(lines, errLines);
    }

    private ProcessBuilder createDownloadProcess(String url) {
        return new ProcessBuilder(
                "yt-dlp",
                "-f", "bestaudio[ext=m4a]/bestaudio[ext=mp3]",
                "--audio-format", "mp3",
                "-P", FILE_CACHE_LOCATION,
                "-o", "%(title)s.%(ext)s",
                url
        );
    }

    private ProcessBuilder createMetadataCheckProcess(String url) {
        return new ProcessBuilder(
                "yt-dlp",
                "--print", "{\\\"title\\\": %(title)j, \\\"channel\\\": %(channel)j, \\\"duration\\\": %(duration)j}",
                url
        );
    }


    private String normalizeFileName(String fileName) {
        return Normalizer.normalize(fileName, Normalizer.Form.NFKC)
//                Remove non-alphanumeric characters except '.', '-' and spaces
                .replaceAll("[^\\w\\s.-]", "")
//                Replace multiple spaces with a single space
                .replaceAll("\\s+", " ")
                .trim()
//                Convert to lowercase for case-insensitive matching
                .toLowerCase();
    }

    /**
     * "Shortens" YouTube URL by removing the "https://www.youtube.com/" or "https://www.youtu.be/"
     *
     * @param url Long URL that will be shortened
     * @return Returns shortened URL or null if it could not be shortened.
     */
    public String shortenURL(String url) {
        String regex = "(?:youtu\\.be/|youtube\\.com/(?:.*v=|embed/|v/|shorts/|live/))([a-zA-Z0-9_-]{11})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Removes invalid music cache entries.
     */
    public void cleanupCacheEntries() {
        if (cacheEntries.isEmpty()) {
            log.debug("Music cache is empty. No cleanup will be performed.");
            return;
        }

        int cleanupCount = 0;
        ArrayList<MusicCacheEntry> validEntries = new ArrayList<>();

        for (MusicCacheEntry entry : cacheEntries) {
            boolean isInvalid = false;
//            Entries that are not blocked must contain local data
            if (!entry.isBlocked() && entry.getLocalData() == null) {
                isInvalid = true;
                log.trace("Music cache entry \"{}\" is not blocked but does not contain any local data information and will be removed.", entry.getUrlShortened());
            }
//            Can't be a duplicate
            if (validEntries.contains(entry)) {
                isInvalid = true;
                log.trace("Music cache entry \"{}\" is a duplicate and will be removed.", entry.getUrlShortened());
            }

//            Local data must be valid
            if (entry.getLocalData() != null) {
//                FIXME: this is dumb. implement removal of unplayed songs but keep links cached.
                if (entry.getLocalData().getFullPath() != null) {
//                    No file found? Delete.
                    File file = new File(entry.getLocalData().getFullPath());
                    if (!file.exists()) {
                        isInvalid = true;
                        log.trace("Music cache entry \"{}\" does not have a valid file associated with it and will be removed.", entry.getUrlShortened());
                    }
                } else {
//                    No file link? Delete.
                    isInvalid = true;
                    log.trace("Music cache entry \"{}\" does not have a file associated with it and will be removed.", entry.getUrlShortened());
                }
            }

            if (!isInvalid) {
                validEntries.add(entry);
            } else {
                cleanupCount++;
            }
        }

        if (cleanupCount > 0) {
            cacheEntries.clear();
            cacheEntries.addAll(validEntries);

            log.debug("Removed {} invalid entries from music cache!", cleanupCount);
        } else {
            log.debug("No cache entries were removed from music cache during cleanup!");
        }
    }

    /**
     * Removes a specific music cache entry
     *
     * @param entry
     */
    public void removeCacheEntry(MusicCacheEntry entry) {
        log.debug("Removed cache entry \"{}\"!", entry.getUrlShortened());
        cacheEntries.remove(entry);
    }

    /**
     * Finds and removes a specific music cache entry based on link
     *
     * @param url_shortened
     */
    public void removeCacheEntry(String url_shortened) {
        MusicCacheEntry entry = getMeta(url_shortened);
        if (entry != null) {
            removeCacheEntry(entry);
        } else {
            log.debug("Unable to remove cache entry \"{}\" as it does not exist!", url_shortened);
        }
    }

    @PreDestroy
    public void destroy() {
//        Cleanup entries before saving
        cleanupCacheEntries();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(System.getProperty("user.dir") + MUSIC_CACHE_JSON_PATH), cacheEntries);
            log.info("Music cache saved successfully!");
        } catch (IOException e) {
            log.error("An error occurred while trying to save music cache to file! {}", e.getMessage());
        }
    }
}
