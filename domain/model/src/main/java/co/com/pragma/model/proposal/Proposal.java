package co.com.pragma.model.proposal;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.state.State;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Proposal {

    private BigInteger id;
    private Double amount;
    private String userIdentificationNumber;
    private Integer proposalLimit;
    private LocalDate limitDate;
    private Double baseSalary;
    private String email;
    private Integer stateId;
    private Long proposalTypeId;
    private ProposalType proposalType;
    private State state;
}
