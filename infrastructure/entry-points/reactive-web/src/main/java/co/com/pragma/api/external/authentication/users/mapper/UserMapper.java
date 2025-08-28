package co.com.pragma.api.external.authentication.users.mapper;

import co.com.pragma.api.external.authentication.users.dto.UserResponseDTO;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toDomain(UserResponseDTO response);
}
