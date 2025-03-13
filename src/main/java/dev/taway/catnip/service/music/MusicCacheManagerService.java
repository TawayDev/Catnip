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

    /**
     * Loads the music cache entries from the specified file on disk into memory.
     * If the file does not exist or is empty, the cache will remain empty.
     */
    public void loadCache() {
        cacheEntries = cacheDataHandler.load(PATH);
    }

    /**
     * Saves the current in-memory music cache entries to the specified file on disk.
     * This ensures persistence of the cache data across application restarts.
     */
    public void saveCache() {
        cacheDataHandler.save(PATH, cacheEntries);
    }

    /**
     * Retrieves a MusicCacheEntry from the cache based on the shortened URL.
     *
     * @param urlShortened The shortened URL used as a key to look up the cache entry.
     * @return An Optional containing the MusicCacheEntry if found, or an empty Optional if not found.
     */
    public Optional<MusicCacheEntry> getEntry(String urlShortened) {
        return cacheEntries.stream()
                .filter(entry -> entry.getUrlShortened().equals(urlShortened))
                .findFirst();
    }

    /**
     * Adds a new MusicCacheEntry to the cache.
     *
     * @param entry The MusicCacheEntry to be added to the cache.
     */
    public void addEntry(MusicCacheEntry entry) {
        cacheEntries.add(entry);
    }

    /**
     * Removes a MusicCacheEntry from the cache.
     *
     * @param entry The MusicCacheEntry to be removed from the cache.
     */
    public void removeEntry(MusicCacheEntry entry) {
        cacheEntries.remove(entry);
    }

    /**
     * Cleans up the music cache by removing invalid or expired entries.
     * Invalid entries include:
     * - Blocked entries without local data.
     * - Entries where the local file no longer exists on disk.
     * Logs the number of entries removed during the cleanup process.
     */
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
