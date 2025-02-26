package dev.taway.catnip.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBlacklistEntry {
    private String username;
    private String bannedBy;
    private long banTimestamp;
    private long banExpiryTimestamp;

    @JsonCreator
    public UserBlacklistEntry(
            @JsonProperty("username") String username,
            @JsonProperty("bannedBy") String bannedBy,
            @JsonProperty("banTimestamp") long banTimestamp
    ) {
        this.username = username;
        this.bannedBy = bannedBy;
        this.banTimestamp = banTimestamp;
        this.banExpiryTimestamp = 0;
    }

    @JsonSetter(value = "banExpiryTimestamp", nulls = com.fasterxml.jackson.annotation.Nulls.AS_EMPTY)
    public void setBanExpiryTimestamp(Long banExpiryTimestamp) {
        this.banExpiryTimestamp = (banExpiryTimestamp != null) ? banExpiryTimestamp : 0L;
    }
}
