package dev.taway.catnip.data.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MusicQueueEntry extends MusicCacheEntry {
    private double playTime = 0;
    private boolean paused = false;
    private boolean fromBackupPlaylist = false;

    public MusicQueueEntry(
            MusicCacheEntry entry,
            double playTime
    ) {
        this(
                entry.getUrl(),
                entry.getUrlShortened(),
                entry.getTitle(),
                entry.getArtist(),
                entry.getDuration(),
                entry.isBlocked(),
                entry.getBlockReason(),
                entry.getLocalData(),
                playTime,
                false,
                false
        );
    }

    public MusicQueueEntry(
            MusicCacheEntry entry,
            double playTime,
            boolean paused,
            boolean fromBackupPlaylist
    ) {
        this(
                entry.getUrl(),
                entry.getUrlShortened(),
                entry.getTitle(),
                entry.getArtist(),
                entry.getDuration(),
                entry.isBlocked(),
                entry.getBlockReason(),
                entry.getLocalData(),
                playTime,
                paused,
                fromBackupPlaylist
        );
    }

    @JsonCreator
    public MusicQueueEntry(
            @JsonProperty("url") String url,
            @JsonProperty("urlShortened") String urlShortened,
            @JsonProperty("title") String title,
            @JsonProperty("artist") String artist,
            @JsonProperty("duration") double duration,
            @JsonProperty("blocked") boolean blocked,
            @JsonProperty("blockReason") MusicCacheEntryBlockReason blockReason,
            @JsonProperty("localData") LocalData localData,
            @JsonProperty("playTime") double playTime,
            @JsonProperty("paused") boolean paused,
            @JsonProperty("fromBackupPlaylist") boolean fromBackupPlaylist
    ) {
        super(url, urlShortened, title, artist, duration, blocked, blockReason, localData);
        this.playTime = playTime;
    }

    @Override
    public String toString() {
        return "MusicQueueEntry{" +
                "playTime=" + playTime +
                '}';
    }
}
