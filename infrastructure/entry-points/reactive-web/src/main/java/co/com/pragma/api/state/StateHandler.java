package co.com.pragma.api.state;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.exception.FieldValidationException;
import co.com.pragma.api.state.dto.CreateStateDTO;
import co.com.pragma.api.state.dto.StateResponseDTO;
import co.com.pragma.api.state.mapper.StateMapper;
import co.com.pragma.usecase.state.StateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "States", description = "State management APIs")
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "Validation data error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        )
})
@Slf4j
@Component
@RequiredArgsConstructor
public class StateHandler {

    private final StateUseCase stateUseCase;
    private final StateMapper mapper;
    private final Validator validator;

    @Operation(
            operationId = "saveState",
            summary = "Save a state",
            description = "Creates a new state",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateStateDTO.class)
                    )
            ),responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "State created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StateResponseDTO.class)
                    )
            )
    }
    )
    public Mono<ServerResponse> listenSaveState(ServerRequest serverRequest) {
        log.info("[StateHandler] Received request to save a new state");
        return serverRequest.bodyToMono(CreateStateDTO.class)
                .doOnNext(dto -> log.debug("[StateHandler] Payload received: {}", dto))
                .flatMap(dto -> {
                    Errors errors = new BeanPropertyBindingResult(dto, CreateStateDTO.class.getName());
                    validator.validate(dto, errors);

                    if (errors.hasErrors()) {
                        List<String> messageErrors = errors.getFieldErrors().stream()
                                .map(fieldError ->  fieldError.getField() + " " + fieldError.getDefaultMessage())
                                .toList();
                        log.warn("[StateHandler] Validation failed: {}", messageErrors);
                        return Mono.error(new FieldValidationException(messageErrors));
                    }
                    log.info("[StateHandler] Validation successful for state with name={}", dto.getName());
                    return Mono.just(dto);
                })
                .map(mapper::toDomain)
                .doOnNext(domain -> log.debug("[StateHandler] Mapped DTO to domain model: {}", domain))
                .flatMap(stateUseCase::saveState)
                .doOnSuccess(saved -> log.info("[StateHandler] State saved successfully with id={}", saved.getId()))
                .map(mapper::toResponse)
                .doOnNext(response -> log.debug("[StateHandler] Mapped domain model to response DTO: {}", response))
                .flatMap(savedStateResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedStateResponse))
                .doOnError(error -> log.error("[StateHandler] Error while saving state", error));
    }


}
