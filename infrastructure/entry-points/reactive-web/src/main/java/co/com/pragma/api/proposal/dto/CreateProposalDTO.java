package co.com.pragma.api.proposal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProposalDTO {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "debe ser mayor o igual a 0")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "no puede superar 15.000.000")
    @Digits(integer = 8, fraction = 2, message = "debe tener hasta 8 dígitos enteros y 2 decimales")
    @Schema(description = "Amount of the proposal")
    private Double amount;

    @NotNull
    @Min(value = 1)
    @Schema(description = "Limit in months of the proposal")
    private Integer proposalLimit;

    @NotNull
    @NotBlank
    @Length(max = 50, min = 1)
    @Schema(description = "User identification number")
    private String userIdentificationNumber;

    @NotNull
    @Min(value = 1)
    @Schema(description = "Unique identifier of the proposal type")
    private Long proposalTypeId;
}
