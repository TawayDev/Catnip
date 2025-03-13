package dev.taway.catnip.service.music;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.MusicCacheEntry;
import dev.taway.catnip.util.CacheDataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class MusicCacheManagerService {
    private static final String PATH = "/cache/music-cache.json";
    private static final Logger log = LogManager.getLogger(MusicCacheManagerService.class);
    private final CatnipConfig config;
    private final CacheDataHandler<MusicCacheEntry> cacheDataHandler;
    private ArrayList<MusicCacheEntry> cacheEntries = new ArrayList<>();

    @Autowired
    public MusicCacheManagerService(CatnipConfig config) {
        this.config = config;
        this.cacheDataHandler = new CacheDataHandler<>(MusicCacheEntry.class);
    }

    @PostConstruct
    public void init() {
        loadCache();
        cleanupCache();
    }

    public void loadCache() {
        cacheEntries = cacheDataHandler.load(PATH);
    }

    public void saveCache() {
        cacheDataHandler.save(PATH, cacheEntries);
    }

    public Optional<MusicCacheEntry> getEntry(String urlShortened) {
        return cacheEntries.stream()
                .filter(entry -> entry.getUrlShortened().equals(urlShortened))
                .findFirst();
    }

    public void addEntry(MusicCacheEntry entry) {
        cacheEntries.add(entry);
    }

    public void removeEntry(MusicCacheEntry entry) {
        cacheEntries.remove(entry);
    }

    public void cleanupCache() {
        int cleanupCount = 0;
        ArrayList<MusicCacheEntry> validEntries = new ArrayList<>();

        for (MusicCacheEntry entry : cacheEntries) {
            boolean isValid = true;

            if (!entry.isBlocked() && entry.getLocalData() == null) {
                isValid = false;
            }

            if (entry.getLocalData() != null && !new File(entry.getLocalData().getFullPath()).exists()) {
                isValid = false;
            }

            if (isValid) {
                validEntries.add(entry);
            } else {
                cleanupCount++;
            }
        }

        if (cleanupCount > 0) {
            cacheEntries = validEntries;
            log.debug("Removed {} invalid entries from music cache!", cleanupCount);
        }
    }

    @PreDestroy
    public void destroy() {
        cleanupCache();
        saveCache();
    }
}
