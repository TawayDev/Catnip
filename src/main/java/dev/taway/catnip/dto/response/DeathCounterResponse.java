package dev.taway.catnip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeathCounterResponse extends BasicResponse {
    private int deaths;
    
    public DeathCounterResponse(boolean error, String message, int deaths) {
        super(error, message);
        this.deaths = deaths;
    }
}
