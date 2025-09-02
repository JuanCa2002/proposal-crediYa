package co.com.pragma.api.proposal.dto.filter;

import co.com.pragma.api.dto.PaginatedRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalFilterDTO extends PaginatedRequestDTO {

    @Min(value = 1)
    @Schema(description = "Unique identifier of the proposal type")
    private Long proposalTypeId;

    @Min(value = 1)
    @Schema(description = "Unique identifier of the state")
    private Integer stateId;

    @Length(min = 1, max = 255)
    @Email
    @Schema(description = "Email related with the proposal")
    private String email;
}
