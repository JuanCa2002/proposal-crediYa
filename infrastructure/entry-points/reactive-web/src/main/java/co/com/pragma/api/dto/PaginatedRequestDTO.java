package co.com.pragma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class PaginatedRequestDTO {

    @NotNull
    @Min(value = 1)
    @Schema(description = "Limit quantity of records")
    private Integer limit;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Offset quantity to skip")
    private Integer offset;


}
