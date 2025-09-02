package co.com.pragma.api.proposal.dto;

import co.com.pragma.api.dto.PaginatedResponseDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProposalPaginatedResponseDTO extends PaginatedResponseDTO<ProposalFilterResponseDTO> {

    private Long approvedOnes;
    private Double sumRequestApprovedAmount;
}
