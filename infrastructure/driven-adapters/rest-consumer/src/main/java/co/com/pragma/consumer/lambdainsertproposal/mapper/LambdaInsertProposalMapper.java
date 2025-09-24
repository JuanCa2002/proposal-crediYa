package co.com.pragma.consumer.lambdainsertproposal.mapper;

import co.com.pragma.consumer.lambdainsertproposal.dto.LambdaInsertProposalRequestDTO;
import co.com.pragma.model.proposal.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {DateMapper.class})
public interface LambdaInsertProposalMapper {

    @Mapping(source = "limitDate", target = "limitDate", qualifiedByName = "localDateToString")
    @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "localDateToString")
    @Mapping(source = "proposalType.name", target = "proposalType")
    @Mapping(source = "state.name", target = "state")
    @Mapping(source = "proposalType.interestRate", target = "interestRate")
    LambdaInsertProposalRequestDTO toRequest(Proposal domain);
}
