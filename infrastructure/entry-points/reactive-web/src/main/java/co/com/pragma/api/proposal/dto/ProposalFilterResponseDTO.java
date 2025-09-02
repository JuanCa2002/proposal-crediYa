package co.com.pragma.api.proposal.dto;

import lombok.*;

@Getter
@Setter
public class ProposalFilterResponseDTO extends ProposalResponseDTO{

    private String proposalType;
    private String state;
    private Double interestRate;

}
