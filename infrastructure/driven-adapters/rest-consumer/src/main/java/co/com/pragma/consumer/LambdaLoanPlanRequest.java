package co.com.pragma.consumer;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LambdaLoanPlanRequest {

    private BigInteger id;
    private String email;
    private Double baseSalary;
    private Double currentMonthlyDebt;
    private Double amount;
    private Double interestRate;
    private Integer proposalLimit;


}
