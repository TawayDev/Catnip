package dev.taway.catnip.dto.request.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.taway.catnip.dto.request.BasicRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MusicQueueRequest extends BasicRequest {
    private String URL;

    @JsonCreator
    public MusicQueueRequest(
            @JsonProperty("URL") String URL
    ) {
        this.URL = URL;
    }
}
