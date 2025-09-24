package co.com.pragma.consumer.lambdaloanplan.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanResponse {
    private Integer quoteNumber;
    private Double totalQuote;
    private Double interests;
    private Double capital;
    private Double remainingBalance;
}
