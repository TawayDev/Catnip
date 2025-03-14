package dev.taway.catnip.data.music;

import lombok.Getter;

@Getter
public enum MusicCacheEntryBlockReason {
    TOO_LONG("Exceeded allowed play time."),
    BLACKLISTED("Blacklisted."),
    AGE_RESTRICTED("Age Restricted.");

    final String message;

    MusicCacheEntryBlockReason(String message) {
        this.message = message;
    }
}
