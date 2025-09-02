package co.com.pragma.api.proposal.dto;

import co.com.pragma.api.dto.PaginatedResponseDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProposalPaginatedResponseDTO extends PaginatedResponseDTO<ProposalFilterResponseDTO> {
}
