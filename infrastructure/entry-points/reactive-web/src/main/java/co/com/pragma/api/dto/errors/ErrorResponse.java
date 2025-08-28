package co.com.pragma.api.dto.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private String mainMessage;
    private int code;
    private List<String> messages;

    @JsonCreator
    public ErrorResponse(
            @JsonProperty("mainMessage") String mainMessage,
            @JsonProperty("code") int code,
            @JsonProperty("messages") List<String> messages) {
        this.mainMessage = mainMessage;
        this.code = code;
        this.messages = messages;
    }
}
