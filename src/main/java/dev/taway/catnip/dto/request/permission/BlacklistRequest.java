package dev.taway.catnip.dto.request.permission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.taway.catnip.dto.request.BasicRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlacklistRequest extends BasicRequest {
    private String blacklistUsername;
    private int expiresInDays;

    @JsonCreator
    public BlacklistRequest(
            @JsonProperty("blacklistUsername") String blacklistUsername
    ) {
        this.blacklistUsername = blacklistUsername;
    }

    @JsonSetter(value = "expiresInDays", nulls = com.fasterxml.jackson.annotation.Nulls.AS_EMPTY)
    public void setExpiresInDays(int expiresInDays) {
        this.expiresInDays = expiresInDays;
    }
}
