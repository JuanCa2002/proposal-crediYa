package co.com.pragma.api.state.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStateDTO {

    @NotNull
    @NotBlank
    @Length(min = 1, max = 100)
    @Pattern(regexp = "^[A-Z]+(_[A-Z]+)*$", message = "debe estar en mayúsculas y separado por guiones bajos")
    @Schema(description = "Name of the proposal state", example = "PENDIENTE_REVISION")
    private String name;

    @NotNull
    @NotBlank
    @Length(min = 1, max = 255)
    @Schema(description = "Description of the proposal state", example = "Pendiente de revisión")
    private String description;

}
