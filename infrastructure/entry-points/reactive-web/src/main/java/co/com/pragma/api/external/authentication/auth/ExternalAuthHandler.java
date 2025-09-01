package co.com.pragma.api.external.authentication.auth;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.external.authentication.auth.config.ClientAuthBasePath;
import co.com.pragma.api.external.authentication.auth.dto.ValidateResponseDTO;
import co.com.pragma.api.external.authentication.auth.exception.InvalidTokenUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalAuthHandler implements AuthRepository{

    private final ClientAuthBasePath authBasePath;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<ValidateResponseDTO> validateToken(String token) {
        log.info("[ExternalAuthHandler] Starting request to external Auth service to validate token: {}", token);

        WebClient client = webClientBuilder
                .baseUrl(authBasePath.getAuth())
                .build();

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/validate")
                        .queryParam("token", token)
                        .build())
                .bodyValue(Collections.singletonMap("token", token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.warn("[ExternalAuthHandler] 4xx error occurred while validating token: {}", token);
                    return response.bodyToMono(ErrorResponse.class)
                            .flatMap(error -> {
                                log.error("[ExternalAuthHandler] Invalid token: {}", error.getMessages());
                                return Mono.error(new InvalidTokenUnauthorizedException(error.getMessages().get(0)));
                            });
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("[ExternalUserHandler] 5xx error occurred while validating token: {}", token);
                    return response.bodyToMono(ErrorResponse.class)
                            .flatMap(error -> {
                                log.error("[ExternalUserHandler] Server error: {}", error.getMessages());
                                return Mono.error(new RuntimeException(error.getMessages().get(0)));
                            });
                })
                .bodyToMono(ValidateResponseDTO.class)
                .doOnSuccess(response -> log.info("[ExternalUserHandler] validating token, with response {}", response))
                .doOnError(e -> log.error("[ExternalUserHandler] error while validating token, with error", e));

    }
}
