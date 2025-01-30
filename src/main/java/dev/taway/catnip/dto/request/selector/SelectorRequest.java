package dev.taway.catnip.dto.request.selector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.taway.catnip.dto.request.BasicRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectorRequest extends BasicRequest {
    private String[] options;

    @JsonCreator
    public SelectorRequest(
            @JsonProperty("options") String[] options
    ) {
        this.options = options;
    }
}
