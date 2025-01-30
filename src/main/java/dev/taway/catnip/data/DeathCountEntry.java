package dev.taway.catnip.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeathCountEntry {
    private String gameName;
    private int deaths;

    @JsonCreator
    public DeathCountEntry(
            @JsonProperty("gameName")String gameName,
            @JsonProperty("deaths")int deaths
    ) {
        this.gameName = gameName;
        this.deaths = deaths;
    }
}
