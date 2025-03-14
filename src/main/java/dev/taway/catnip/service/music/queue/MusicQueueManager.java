package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.data.music.MusicCacheEntry;
import dev.taway.catnip.data.music.MusicQueueEntry;
import dev.taway.catnip.service.music.util.UrlUtil;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Service
public class MusicQueueManager {
    private final List<MusicQueueEntry> queueEntries = new ArrayList<>();

    /**
     * Adds a track to the queue with detailed configuration
     * @param entry Music cache entry to add
     * @param playTime Time already played in seconds
     * @param paused Initial paused state
     * @param fromBackupPlaylist Backup playlist origin flag
     */
    public void addToQueue(MusicCacheEntry entry, double playTime, boolean paused, boolean fromBackupPlaylist) {
        queueEntries.add(new MusicQueueEntry(entry, playTime, paused, fromBackupPlaylist));
    }

    /**
     * Adds a track to the queue with default parameters
     * @param entry Music cache entry to add
     */
    public void addToQueue(MusicCacheEntry entry) {
        addToQueue(entry, 0, false, false);
    }

    /**
     * Removes entry by URL (full or shortened)
     * @param url Track URL to remove
     */
    public void removeFromQueue(String url) {
        String shortened = UrlUtil.shortenURL(url);
        queueEntries.removeIf(e -> e.getUrlShortened().equals(shortened));
    }

    /**
     * Removes entry by position
     * @param position Zero-based index
     */
    public void removeFromQueue(int position) {
        if (position >= 0 && position < queueEntries.size()) {
            queueEntries.remove(position);
        }
    }

    /**
     * @return Currently playing track or empty if queue is empty
     */
    public Optional<MusicQueueEntry> getCurrentlyPlaying() {
        return queueEntries.isEmpty() ? Optional.empty() : Optional.of(queueEntries.getFirst());
    }
}