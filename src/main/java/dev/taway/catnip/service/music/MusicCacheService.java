package dev.taway.catnip.service.music;

import dev.taway.catnip.data.MusicCacheEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MusicCacheService {
    private final MusicCacheManagerService cacheManager;
    private final DownloadService downloadService;

    @Autowired
    public MusicCacheService(MusicCacheManagerService cacheManager, DownloadService downloadService) {
        this.cacheManager = cacheManager;
        this.downloadService = downloadService;
    }

    /**
     * Retrieves a MusicCacheEntry from the cache based on the shortened URL.
     *
     * @param urlShortened The shortened URL used as a key to look up the cache entry.
     * @return An Optional containing the MusicCacheEntry if found, or an empty Optional if not found.
     */
    public Optional<MusicCacheEntry> getMusicCacheEntry(String urlShortened) {
        return cacheManager.getEntry(urlShortened);
    }

    /**
     * Caches a song by downloading it (if not already cached) and adds its metadata to the cache.
     * If the song is already cached, the existing entry is returned without re-downloading.
     *
     * @param url The URL of the song to be cached.
     * @return An Optional containing the MusicCacheEntry for the cached song.
     */
    public Optional<MusicCacheEntry> cacheSong(String url) {
        String shortenedUrl = UrlUtil.shortenURL(url);
        Optional<MusicCacheEntry> existing = cacheManager.getEntry(shortenedUrl);

        if (existing.isPresent()) {
            return existing;
        }

        MusicCacheEntry newEntry = downloadService.downloadSong(url);
        cacheManager.addEntry(newEntry);
        return Optional.of(newEntry);
    }

    /**
     * Cleans up the music cache by removing invalid entries.
     * This method delegates the cleanup operation to the MusicCacheManagerService.
     */
    public void cleanupCache() {
        cacheManager.cleanupCache();
    }
}
