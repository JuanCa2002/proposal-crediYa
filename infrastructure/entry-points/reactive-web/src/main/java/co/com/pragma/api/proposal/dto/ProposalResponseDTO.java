package co.com.pragma.api.proposal.dto;

import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalResponseDTO {

    private BigInteger id;
    private Double amount;
    private LocalDate limitDate;
    private String email;
}
