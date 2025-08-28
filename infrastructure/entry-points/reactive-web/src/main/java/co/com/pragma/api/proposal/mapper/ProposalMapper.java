package co.com.pragma.api.proposal.mapper;

import co.com.pragma.api.mapper.DateMapper;
import co.com.pragma.api.proposal.dto.CreateProposalDTO;
import co.com.pragma.api.proposal.dto.ProposalResponseDTO;
import co.com.pragma.model.proposal.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {DateMapper.class})
public interface ProposalMapper {

    @Mapping(source = "proposalLimit", target = "limitDate", qualifiedByName = "sumDaysToLimitDay")
    Proposal toDomain(CreateProposalDTO request);

    @Mapping(source = "limitDate", target = "limitDate", qualifiedByName = "localDateToString")
    ProposalResponseDTO toResponse(Proposal domain);
}
