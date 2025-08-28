package co.com.pragma.api.state.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateResponseDTO {
    @Schema(description = "Unique identifier number of the proposal state")
    private Integer id;

    @Schema(description = "Name of the proposal state")
    private String name;

    @Schema(description = "Description of the proposal state")
    private String description;
}
