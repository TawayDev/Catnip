package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.data.music.MusicQueueEntry;
import dev.taway.catnip.service.music.util.UrlUtil;
import dev.taway.catnip.websocket.PlaybackStatusHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade service providing unified access to music queue operations. Delegates to specialized
 * services for playback control, duration calculations, persistence, and queue management.
 * Maintains queue state through cache persistence and provides backward compatibility
 * with the original MusicQueueService interface.
 */
@Service
public class MusicQueueService {
    private final MusicQueueManager queueManager;
    private final PlaybackControlService playbackControl;
    private final QueueDurationService durationService;
    private final MusicQueueCacheService cacheService;
    private final PlaybackStatusHandler playbackStatusHandler;

    /**
     * Constructs the facade with required dependencies.
     *
     * @param queueManager    Core queue management service
     * @param playbackControl Playback control service
     * @param durationService Queue duration calculation service
     * @param cacheService    Cache persistence service
     */
    @Autowired
    public MusicQueueService(MusicQueueManager queueManager,
                             PlaybackControlService playbackControl,
                             QueueDurationService durationService,
                             MusicQueueCacheService cacheService, PlaybackStatusHandler playbackStatusHandler) {
        this.queueManager = queueManager;
        this.playbackControl = playbackControl;
        this.durationService = durationService;
        this.cacheService = cacheService;
        this.playbackStatusHandler = playbackStatusHandler;
    }

    /**
     * Adds track to queue with full configuration.
     *
     * @param entry      Music metadata entry
     * @param playTime   Already played time in seconds
     * @param paused     Initial paused state
     * @param fromBackup Backup playlist origin flag
     */
    public void addToQueue(MusicCacheEntry entry, double playTime, boolean paused, boolean fromBackup) {
        queueManager.addToQueue(entry, playTime, paused, fromBackup);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Adds track to queue with default parameters (0 play time, not paused).
     *
     * @param entry Music metadata entry
     */
    public void addToQueue(MusicCacheEntry entry) {
        queueManager.addToQueue(entry);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Adds existing queue entry to the queue.
     *
     * @param entry Pre-configured queue entry
     */
    public void addToQueue(MusicQueueEntry entry) {
        queueManager.addToQueue(entry);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Removes entry by URL (full or shortened).
     *
     * @param url Track URL to remove
     */
    public void removeFromQueue(String url) {
        queueManager.removeFromQueue(url);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Removes entry by position index (zero-based).
     *
     * @param position Queue index to remove
     */
    public void removeFromQueue(int position) {
        queueManager.removeFromQueue(position);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Removes specific entry from the queue.
     *
     * @param entry Queue entry to remove
     */
    public void removeFromQueue(MusicQueueEntry entry) {
        queueManager.getQueueEntries().remove(entry);
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Searches for entry by URL.
     *
     * @param url Full or shortened URL
     * @return Optional containing matching entry
     * @throws IllegalArgumentException if URL is null/empty
     */
    public Optional<MusicQueueEntry> findInQueue(String url) {
        return queueManager.getQueueEntries().stream()
                .filter(e -> e.getUrlShortened().equals(UrlUtil.shortenURL(url)))
                .findFirst();
    }

    /**
     * Pauses current track playback.
     */
    public void pause() {
        playbackControl.pause();
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Resumes current track playback.
     */
    public void play() {
        playbackControl.play();
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * Skips current track and advances to next.
     */
    public void skip() {
        playbackControl.skip();
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }

    /**
     * @return Total duration of non-backup tracks in seconds
     */
    public double totalDuration() {
        return durationService.totalDuration();
    }

    /**
     * @return Remaining queue time excluding already played portion
     */
    public double queueEmptyIn() {
        return durationService.queueEmptyIn();
    }

    /**
     * @return Human-readable remaining queue time
     */
    public String queueEmptyInAsString() {
        return durationService.queueEmptyInAsString();
    }

    /**
     * Loads queue state from persistent storage.
     */
    public void loadCache() {
        cacheService.loadCache();
    }

    /**
     * Saves queue state to persistent storage.
     */
    public void saveCache() {
        cacheService.saveCache();
    }

    /**
     * @return Currently playing track or empty if queue is empty
     */
    public Optional<MusicQueueEntry> getCurrentlyPlaying() {
        return queueManager.getCurrentlyPlaying();
    }

    /**
     * Replaces currently playing entry while preserving playback state.
     * WARNING: Should only be used for metadata updates, not track replacement.
     *
     * @param entry New entry to set as current
     */
    public void replaceCurrentlyPlaying(MusicQueueEntry entry) {
        queueManager.getCurrentlyPlaying().ifPresent(current -> {
            queueManager.removeFromQueue(0);
            queueManager.addToQueue(entry, 0, current.isPaused(), current.isFromBackupPlaylist());
        });
        getCurrentlyPlaying().ifPresent(playbackStatusHandler::broadcastStatus);
    }
}