package co.com.pragma.api.proposaltype.mapper;

import co.com.pragma.api.proposaltype.dto.CreateProposalTypeDTO;
import co.com.pragma.api.proposaltype.dto.ProposalTypeResponseDTO;
import co.com.pragma.model.proposaltype.ProposalType;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProposalTypeMapper {

    @Mapping(source = "automaticValidation", target = "automaticValidation", qualifiedByName = "numberValidationToBoolean")
    ProposalType toDomain(CreateProposalTypeDTO request);
    ProposalTypeResponseDTO toResponse(ProposalType domain);

    @Named("numberValidationToBoolean")
    default Boolean numberValidationToBoolean(Integer automaticValidation){
        return automaticValidation.equals(1);
    }
}
