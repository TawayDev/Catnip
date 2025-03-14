package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.data.music.MusicQueueEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlaybackControlService {
    private static final Logger log = LogManager.getLogger(PlaybackControlService.class);
    private final MusicQueueManager queueManager;

    @Autowired
    public PlaybackControlService(MusicQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Pauses current track
     */
    public void pause() {
        queueManager.getCurrentlyPlaying().ifPresent(entry -> {
            entry.setPaused(true);
            log.info("♫ Paused: {} - {}", entry.getArtist(), entry.getTitle());
        });
    }

    /**
     * Resumes playback of current track
     */
    public void play() {
        queueManager.getCurrentlyPlaying().ifPresent(entry -> {
            entry.setPaused(false);
            log.info("♫ Resumed: {} - {}", entry.getArtist(), entry.getTitle());
        });
    }

    /**
     * Skips current track and starts next
     */
    public void skip() {
        if (queueManager.getQueueEntries().isEmpty()) {
            log.info("♫ Nothing to skip. Queue is empty.");
            return;
        }

        MusicQueueEntry skipped = queueManager.getQueueEntries().removeFirst();
        log.info("♫ Skipped: {} - {}", skipped.getArtist(), skipped.getTitle());

        queueManager.getCurrentlyPlaying().ifPresent(next -> next.setPaused(false));
    }
}
