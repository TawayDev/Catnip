package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.data.music.MusicQueueEntry;
import dev.taway.catnip.util.CacheDataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicQueueCacheService {
    private static final String CACHE_PATH = "/cache/music-queue.json";
    private final CacheDataHandler<MusicQueueEntry> cacheHandler;
    private final MusicQueueManager queueManager;

    @Autowired
    public MusicQueueCacheService(MusicQueueManager queueManager) {
        this.queueManager = queueManager;
        this.cacheHandler = new CacheDataHandler<>(MusicQueueEntry.class);
    }

    /**
     * Loads queue from disk
     */
    public void loadCache() {
//        TODO: index "/cache/music/backup-playlist/" on init
        List<MusicQueueEntry> cached = cacheHandler.load(CACHE_PATH);
        queueManager.getQueueEntries().clear();
        queueManager.getQueueEntries().addAll(cached);
    }

    /**
     * Saves queue to disk
     */
    public void saveCache() {
        cacheHandler.save(CACHE_PATH, new ArrayList<>(queueManager.getQueueEntries()));
    }
}
