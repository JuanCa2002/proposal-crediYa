package co.com.pragma.consumer.lambdainsertproposal.dto;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LambdaInsertProposalRequestDTO {

    private BigInteger id;
    private Double amount;
    private String limitDate;
    private String creationDate;
    private Double baseSalary;
    private Double monthlyFee;
    private String email;
    private String proposalType;
    private String state;
    private Double interestRate;
}
