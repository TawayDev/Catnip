package dev.taway.catnip.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MusicCacheEntry {
    private String url;
    //    Will be sorted by url shortened for faster look-up
    private String urlShortened;

    private String title;
    private String artist;

    private double duration;

//    If blocked do NOT re-download.
//    Valid reasons for blocking: Duration is over the allowed limit.
    private boolean blocked;
    private MusicCacheEntryBlockReason blockReason;

    private LocalData localData;

    @JsonCreator
    public MusicCacheEntry(
            @JsonProperty("url") String url,
            @JsonProperty("urlShortened") String urlShortened,
            @JsonProperty("title") String title,
            @JsonProperty("artist") String artist,
            @JsonProperty("duration") double duration,
            @JsonProperty("blocked") boolean blocked,
            @JsonProperty("blockReason") MusicCacheEntryBlockReason blockReason,
            @JsonProperty("localData") LocalData localData
    ) {
        this.url = url;
        this.urlShortened = urlShortened;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.blocked = blocked;
        this.blockReason = blockReason;
        this.localData = localData;
    }

    @Data
    @NoArgsConstructor
    public static class LocalData {
        private String fullPath;
        private String filename;
        private String extension;
        private String path;

        private long downloadedTimestamp;
        //        For deleting un-played songs.
        private long lastPlayedTimestamp;

        @JsonCreator
        public LocalData(
                @JsonProperty("fullPath") String fullPath,
                @JsonProperty("filename") String filename,
                @JsonProperty("extension") String extension,
                @JsonProperty("path") String path,
                @JsonProperty("downloadedTimestamp") long downloadedTimestamp,
                @JsonProperty("lastPlayedTimestamp") long lastPlayedTimestamp
        ) {
            this.fullPath = fullPath;
            this.filename = filename;
            this.extension = extension;
            this.path = path;
            this.downloadedTimestamp = downloadedTimestamp;
            this.lastPlayedTimestamp = lastPlayedTimestamp;
        }
    }
}
