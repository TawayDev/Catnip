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

    public Optional<MusicCacheEntry> getMusicCacheEntry(String urlShortened) {
        return cacheManager.getEntry(urlShortened);
    }

    public Optional<MusicCacheEntry> cacheSong(String url) {
        String shortenedUrl = UrlShortenerUtil.shortenURL(url);
        Optional<MusicCacheEntry> existing = cacheManager.getEntry(shortenedUrl);

        if (existing.isPresent()) {
            return existing;
        }

        MusicCacheEntry newEntry = downloadService.downloadSong(url);
        cacheManager.addEntry(newEntry);
        return Optional.of(newEntry);
    }

    public void cleanupCache() {
        cacheManager.cleanupCache();
    }
}
