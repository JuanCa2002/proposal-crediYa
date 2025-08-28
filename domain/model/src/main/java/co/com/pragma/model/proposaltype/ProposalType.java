package co.com.pragma.model.proposaltype;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProposalType {

    private Long id;
    private String name;
    private Double minimumAmount;
    private Double maximumAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}
