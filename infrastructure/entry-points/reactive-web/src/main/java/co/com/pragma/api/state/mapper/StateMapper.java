package co.com.pragma.api.state.mapper;

import co.com.pragma.api.state.dto.CreateStateDTO;
import co.com.pragma.api.state.dto.StateResponseDTO;
import co.com.pragma.model.state.State;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StateMapper {

    State toDomain(CreateStateDTO request);

    StateResponseDTO toResponse(State domain);
}
