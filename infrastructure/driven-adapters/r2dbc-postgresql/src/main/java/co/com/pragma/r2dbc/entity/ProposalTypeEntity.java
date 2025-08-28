package co.com.pragma.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("proposal_types")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProposalTypeEntity {

    @Id
    private Long id;
    private String name;
    private Double minimumAmount;
    private Double maximumAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}
