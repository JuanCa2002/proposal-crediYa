package co.com.pragma.consumer.mapper;

import co.com.pragma.consumer.LambdaLoanPlanRequest;
import co.com.pragma.consumer.LambdaLoanPlanResponse;
import co.com.pragma.consumer.LoanResponse;
import co.com.pragma.model.loan.Loan;
import co.com.pragma.model.proposal.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LambdaMapper {

    LambdaLoanPlanRequest toRequest(Proposal proposal);

    Proposal toDomain(LambdaLoanPlanResponse response);

    Loan toDomainLoan(LoanResponse response);

    List<Loan> toDomainsLoan(List<LoanResponse> responses);
}
