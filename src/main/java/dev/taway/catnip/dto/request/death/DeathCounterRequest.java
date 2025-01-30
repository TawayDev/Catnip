package dev.taway.catnip.dto.request.death;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.taway.catnip.dto.request.BasicRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeathCounterRequest extends BasicRequest {
    private String gameName;

    @JsonCreator
    public DeathCounterRequest(
            String username,
            boolean isSubscriber,
            boolean isVIP,
            boolean isMod,
            boolean isStreamer,
            @JsonProperty("gameName") String gameName
    ) {
        super(username, isSubscriber, isVIP, isMod, isStreamer);
        this.gameName = gameName;
    }

    @Override
    public String toString() {
        return "DeathCounterRequest{" +
                "gameName='" + gameName + '\'' +
                "super=" + super.toString() +
                '}';
    }
}
