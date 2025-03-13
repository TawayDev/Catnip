package dev.taway.catnip.service.music.cache;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.data.music.MusicCacheEntryBlockReason;
import dev.taway.catnip.service.file.FileWatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DownloadService {

    private static final String FILE_CACHE_LOCATION = System.getProperty("user.dir") + "/cache/music/download/";
    private static final Logger log = LogManager.getLogger(DownloadService.class);
    private final FileWatchService fileWatchService;
    private final MetadataService metadataService;
    private final CatnipConfig config;

    @Autowired
    public DownloadService(FileWatchService fileWatchService, MetadataService metadataService, CatnipConfig config) {
        this.fileWatchService = fileWatchService;
        this.metadataService = metadataService;
        this.config = config;
    }

    /**
     * Downloads a song from the provided URL and returns a MusicCacheEntry object containing metadata and download details.
     * If the song's duration exceeds the maximum allowed duration specified in the configuration, the entry will be marked as blocked.
     *
     * @param url The URL of the song to be downloaded.
     * @return A MusicCacheEntry object containing metadata, download status, and local file information.
     */
    public MusicCacheEntry downloadSong(String url) {
        String urlShortened = UrlUtil.shortenURL(url);
        MusicCacheEntry entry = metadataService.fetchMetadata(url);
        entry.setUrl(url);
        entry.setUrlShortened(urlShortened);

        if (entry.getDuration() > config.getCache().getMaximumSongDurationSeconds()) {
            entry.setBlocked(true);
            entry.setBlockReason(MusicCacheEntryBlockReason.TOO_LONG);
            return entry;
        }

        return performDownload(entry);
    }

    private MusicCacheEntry performDownload(MusicCacheEntry entry) {
        try {
            Process process = createDownloadProcess(entry.getUrl()).start();
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> output = ProcessOutputUtil.handleProcessOutput(process);
            process.waitFor();

            var downloadResult = parseDownloadOutput(output);
            entry.setLocalData(downloadResult.getKey());

            if (downloadResult.getValue()) {
                log.info("[{}] Waiting for container correcting to finish!", entry.getUrlShortened());
                fileWatchService.waitForFile(downloadResult.getKey().getFullPath()).get();
            }

        } catch (Exception e) {
            log.error("[{}] Download failed", entry.getUrlShortened(), e);
        }

        return entry;
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

    private AbstractMap.SimpleEntry<MusicCacheEntry.LocalData, Boolean> parseDownloadOutput(
            AbstractMap.SimpleEntry<ArrayList<String>, ArrayList<String>> processOutput
    ) {
        String destinationPath = null;
        boolean correctingContainer = false;

        for (List<String> lines : Arrays.asList(processOutput.getKey(), processOutput.getValue())) {
            for (String str : lines) {
                String strLower = str.toLowerCase();
                if (strLower.contains("correcting container")) {
                    correctingContainer = true;
                }
                if (strLower.contains("destination:")) {
                    int destinationIndex = strLower.indexOf("destination:");
                    String pathCandidate = str.substring(destinationIndex + "destination:".length()).trim();
                    if (!pathCandidate.endsWith(File.separator)) {
                        destinationPath = pathCandidate;
                    }
                }
            }
        }

        if (destinationPath == null) {
            throw new RuntimeException("Failed to parse download output");
        }

        File file = new File(destinationPath);
        MusicCacheEntry.LocalData localData = new MusicCacheEntry.LocalData(
                file.getAbsolutePath(),
                file.getName().replaceFirst("[.][^.]+$", ""),
                getFileExtension(file),
                file.getParent(),
                System.currentTimeMillis(),
                0L
        );

        return new AbstractMap.SimpleEntry<>(localData, correctingContainer);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf + 1);
    }
}
