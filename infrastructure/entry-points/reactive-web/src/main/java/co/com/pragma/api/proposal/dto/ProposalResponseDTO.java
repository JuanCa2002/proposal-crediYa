package co.com.pragma.api.proposal.dto;

import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDTO {

    private BigInteger id;
    private Double amount;
    private Double baseSalary;
    private LocalDate limitDate;
    private LocalDate creationDate;
    private String email;
}
