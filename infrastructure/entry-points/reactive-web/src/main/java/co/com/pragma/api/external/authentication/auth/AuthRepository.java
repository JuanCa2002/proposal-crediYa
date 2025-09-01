package co.com.pragma.api.external.authentication.auth;

import co.com.pragma.api.external.authentication.auth.dto.ValidateResponseDTO;
import reactor.core.publisher.Mono;

public interface AuthRepository {
    Mono<ValidateResponseDTO> validateToken(String token);
}
