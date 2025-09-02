package co.com.pragma.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;
import java.time.LocalDate;

@Table("proposals")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProposalEntity {

    @Id
    private BigInteger id;
    private Double amount;
    private Integer proposalLimit;
    private LocalDate limitDate;
    private String email;
    private Double baseSalary;
    private Integer stateId;
    private Long proposalTypeId;
}
