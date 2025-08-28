package co.com.pragma.api.proposaltype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalTypeResponseDTO {
    @Schema(description = "Unique identifier of the proposal type")
    private Long id;

    @Schema(description = "Name of the proposal type")
    private String name;

    @Schema(description = "Minimum amount of the proposal type")
    private Double minimumAmount;

    @Schema(description = "Maximum amount of the proposal type")
    private Double maximumAmount;

    @Schema(description = "Interest Rate of the proposal type")
    private Double interestRate;

    @Schema(description = "Automatic Validation of the system about the proposal")
    private Boolean automaticValidation;
}
