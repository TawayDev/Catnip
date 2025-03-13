package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.config.CatnipConfig;
import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.data.music.MusicQueueEntry;
import dev.taway.catnip.util.CacheDataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MusicQueueService {
    private static final Logger log = LogManager.getLogger(MusicQueueService.class);
    private static final String PATH = "/cache/music-queue.json";
    private final CatnipConfig config;
    private final CacheDataHandler<MusicQueueEntry> cacheDataHandler;

    @Getter
    private ArrayList<MusicQueueEntry> queueEntries = new ArrayList<>();

    @Autowired
    public MusicQueueService(CatnipConfig config) {
        this.config = config;
        this.cacheDataHandler = new CacheDataHandler<>(MusicQueueEntry.class);
    }

    @PostConstruct
    public void init() {
//        TODO: index "/cache/music/backup-playlist/" on init
        loadCache();
    }

    /**
     * Pauses the currently playing music entry in the queue.
     * If the queue is not empty, the first entry in the queue is marked as paused.
     */
    public void pause() {
        if (!queueEntries.isEmpty()) {
            MusicQueueEntry entry = queueEntries.getFirst();
            entry.setPaused(true);
            queueEntries.set(0, entry);
        }
    }

    /**
     * Resumes playback of the currently paused music entry in the queue.
     * If the queue is not empty, the first entry in the queue is marked as unpaused.
     */
    public void play() {
        if (!queueEntries.isEmpty()) {
            MusicQueueEntry entry = queueEntries.getFirst();
            entry.setPaused(false);
            queueEntries.set(0, entry);
        }
    }

    /**
     * Calculates the total duration of all music entries in the queue,
     * excluding entries from the backup playlist.
     *
     * @return the total duration (in seconds) of all non-backup playlist entries.
     */
    public double totalDuration() {
        double totalDuration = 0;
        for (MusicQueueEntry entry : queueEntries) {
            if (!entry.isFromBackupPlaylist()) totalDuration += entry.getDuration();
        }
        return totalDuration;
    }

    /**
     * Calculates the remaining time until the queue is empty,
     * excluding the already played portion of the currently playing entry.
     *
     * @return the remaining time (in seconds) until the queue is empty.
     */
    public double queueEmptyIn() {
        double totalDuration = totalDuration();

        if (!queueEntries.isEmpty()) {
            totalDuration -= queueEntries.getFirst().getPlayTime();
        }

        return totalDuration;
    }

    /**
     * Calculates the remaining time until the queue is empty and returns it as a human-readable string.
     * The time is expressed in minutes, or "now" if the queue is already empty.
     *
     * @return a string representation of the remaining time in the format "<minutes> minutes",
     * or "now" if the queue is empty or the remaining time is zero.
     */
    public String queueEmptyInAsString() {
        double time = queueEmptyIn();
        double minutes = time / 60;
        return time == 0 ? "now" : String.format("%.1f minutes", minutes);
    }

    /**
     * Adds a new music entry to the queue with detailed configuration.
     *
     * @param entry              the music cache entry to add to the queue.
     * @param playTime           the amount of time (in seconds) that has already been played.
     * @param paused             whether the entry should be paused when added to the queue.
     * @param fromBackupPlaylist whether the entry is from the backup playlist.
     */
    public void addToQueue(MusicCacheEntry entry, double playTime, boolean paused, boolean fromBackupPlaylist) {
        queueEntries.add(new MusicQueueEntry(entry, playTime, paused, fromBackupPlaylist));
    }

    /**
     * Adds a new music entry to the queue with default settings.
     * The entry will have zero play time, will not be paused, and will not be marked as from the backup playlist.
     *
     * @param entry the music cache entry to add to the queue.
     */
    public void addToQueue(MusicCacheEntry entry) {
        queueEntries.add(new MusicQueueEntry(entry, 0));
    }

    /**
     * Adds an existing music queue entry to the queue.
     *
     * @param entry the music queue entry to add to the queue.
     */
    public void addToQueue(MusicQueueEntry entry) {
        queueEntries.add(entry);
    }

    /**
     * Loads the music queue entries from the specified file on disk into memory.
     * If the file does not exist or is empty, the cache will remain empty.
     */
    public void loadCache() {
        queueEntries = cacheDataHandler.load(PATH);
    }

    /**
     * Saves the current in-memory music queue entries to the specified file on disk.
     * This ensures persistence of the cache data across application restarts.
     */
    public void saveCache() {
        cacheDataHandler.save(PATH, queueEntries);
    }

    @PreDestroy
    public void destroy() {
//        Pause before saving so the currently playing song will not be played directly at startup.
        pause();
        saveCache();
    }
}
