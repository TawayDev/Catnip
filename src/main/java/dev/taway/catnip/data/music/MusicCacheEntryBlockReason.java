package dev.taway.catnip.data.music;

import lombok.Getter;

@Getter
public enum MusicCacheEntryBlockReason {
    TOO_LONG("Exceeded allowed play time."),
    BLACKLISTED("Blacklisted."),;

    final String message;

    MusicCacheEntryBlockReason(String message) {
        this.message = message;
    }
}
