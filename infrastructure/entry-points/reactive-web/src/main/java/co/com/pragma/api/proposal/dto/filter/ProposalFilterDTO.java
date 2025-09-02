package co.com.pragma.api.proposal.dto.filter;

import co.com.pragma.api.dto.PaginatedRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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

    @Length(min = 1, max = 10)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "debe estar en yyyy-MM-dd formato")
    @Schema(description = "Initial Date to search a proposal based on its creation date")
    private String initialDate;

    @Length(min = 1, max = 10)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "debe estar en yyyy-MM-dd formato")
    @Schema(description = "End Date to search a proposal based on its creation date")
    private String endDate;

    @Min(value = 1)
    @Schema(description = "Proposal months limit time")
    private Integer proposalLimit;
}
