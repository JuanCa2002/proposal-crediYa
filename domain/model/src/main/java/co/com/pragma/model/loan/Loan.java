package co.com.pragma.model.loan;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Loan {

    private Integer quoteNumber;
    private Double totalQuote;
    private Double interests;
    private Double capital;
    private Double remainingBalance;
}
