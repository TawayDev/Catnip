package dev.taway.catnip.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BasicRequest {
    private String username;
    private boolean isSubscriber;
    private boolean isVIP;
    private boolean isMod;
    private boolean isStreamer;

    @JsonCreator
    public BasicRequest(
            @JsonProperty("username") String username,
            @JsonProperty("isSubscriber") boolean isSubscriber,
            @JsonProperty("isVIP") boolean isVIP,
            @JsonProperty("isMod") boolean isMod,
            @JsonProperty("isStreamer") boolean isStreamer
    ) {
        this.username = username;
        this.isSubscriber = isSubscriber;
        this.isVIP = isVIP;
        this.isMod = isMod;
        this.isStreamer = isStreamer;
    }

    @Override
    public String toString() {
        return "BasicRequest{" +
                "username='" + username + '\'' +
                ", isSubscriber=" + isSubscriber +
                ", isVIP=" + isVIP +
                ", isMod=" + isMod +
                ", isStreamer=" + isStreamer +
                '}';
    }
}
