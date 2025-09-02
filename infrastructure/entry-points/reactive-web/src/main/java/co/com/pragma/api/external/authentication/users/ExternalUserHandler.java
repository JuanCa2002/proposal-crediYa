package co.com.pragma.api.external.authentication.users;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.exception.FieldValidationException;
import co.com.pragma.api.external.authentication.users.config.ClientUsersBasePath;
import co.com.pragma.api.external.authentication.users.dto.UserResponseDTO;
import co.com.pragma.api.external.authentication.users.exception.UserByIdentificationNumberNotFoundException;
import co.com.pragma.api.external.authentication.users.mapper.UserMapper;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalUserHandler implements UserRepository {

    private final ClientUsersBasePath usersBaseUrl;
    private final WebClient client;
    private final UserMapper mapper;

    public Mono<User> findByIdentificationNumber(String identificationNumber) {
        log.info("[ExternalUserHandler] Starting request to external Users service for identification number: {}", identificationNumber);

        return client.get()
                .uri(usersBaseUrl.getUsers() +"/{identificationNumber}", identificationNumber)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.warn("[ExternalUserHandler] 4xx error occurred while fetching user with identification number: {}", identificationNumber);
                    return response.bodyToMono(ErrorResponse.class)
                            .flatMap(error -> {
                                log.error("[ExternalUserHandler] User not found: {}", error.getMessages());
                                return Mono.error(new UserByIdentificationNumberNotFoundException(error.getMessages().get(0)));
                            });
                })
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.warn("[ExternalUserHandler] Validation error occurred while fetching user with identification number: {}", identificationNumber);
                    return response.bodyToMono(ErrorResponse.class)
                            .flatMap(error -> {
                                log.error("[ExternalUserHandler] Validation errors: {}", error.getMessages());
                                return Mono.error(new FieldValidationException(error.getMessages()));
                            });
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("[ExternalUserHandler] 5xx error occurred while fetching user with identification number: {}", identificationNumber);
                    return response.bodyToMono(ErrorResponse.class)
                            .flatMap(error -> {
                                log.error("[ExternalUserHandler] Server error: {}", error.getMessages());
                                return Mono.error(new RuntimeException(error.getMessages().get(0)));
                            });
                })
                .bodyToMono(UserResponseDTO.class)
                .doOnNext(userResponse -> log.info("[ExternalUserHandler] Successfully retrieved user from external service: {}", userResponse))
                .map(mapper::toDomain)
                .doOnSuccess(user -> log.info("[ExternalUserHandler] Mapped UserResponseDTO to domain User successfully: {}", user))
                .doOnError(error -> log.error("[ExternalUserHandler] An error occurred while retrieving user: {}", error.getMessage(), error));
    }


}
