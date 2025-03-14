package dev.taway.catnip.service.music.queue;

import dev.taway.catnip.data.music.MusicQueueEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueueDurationService {
    private final MusicQueueManager queueManager;

    @Autowired
    public QueueDurationService(MusicQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * @return Total duration of non-backup tracks in seconds
     */
    public double totalDuration() {
        double totalDuration = 0;
        for (MusicQueueEntry entry : queueManager.getQueueEntries()) {
            if (!entry.isFromBackupPlaylist()) totalDuration += entry.getDuration();
        }
        return totalDuration;
    }

    /**
     * @return Remaining queue time excluding already played portion
     */
    public double queueEmptyIn() {
        double total = totalDuration();
        return queueManager.getCurrentlyPlaying()
                .map(current -> total - current.getPlayTime())
                .orElse(0.0);
    }

    /**
     * @return Human-readable remaining time
     */
    public String queueEmptyInAsString() {
        double time = queueEmptyIn();
        return time == 0 ? "now" : String.format("%.1f minutes", time / 60);
    }
}
