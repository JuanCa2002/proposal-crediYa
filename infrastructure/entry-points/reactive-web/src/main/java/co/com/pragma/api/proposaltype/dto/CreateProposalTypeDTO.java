package co.com.pragma.api.proposaltype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProposalTypeDTO {

    @NotNull
    @NotBlank
    @Length(min = 1, max = 100)
    @Schema(description = "Name of the proposal type")
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "debe ser mayor o igual a 0")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "no puede superar 15.000.000")
    @Digits(integer = 8, fraction = 2, message = "debe tener hasta 8 dígitos enteros y 2 decimales")
    @Schema(description = "Minimum Amount of the proposal type")
    private Double minimumAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "debe ser mayor o igual a 0")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "no puede superar 15.000.000")
    @Digits(integer = 8, fraction = 2, message = "debe tener hasta 8 dígitos enteros y 2 decimales")
    @Schema(description = "Maximum Amount of the proposal type")
    private Double maximumAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "debe ser mayor o igual a 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "no puede superar 1")
    @Digits(integer = 1, fraction = 2, message = "debe tener hasta 1 dígito enteros y 2 decimales")
    @Schema(description = "Interest Rate of the proposal type", example = "0.19")
    private Double interestRate;

    @NotNull
    @Min(value = 0)
    @Max(value = 1)
    @Schema(description = "Automatic Validation of the system about the proposal")
    private Integer automaticValidation;

}
