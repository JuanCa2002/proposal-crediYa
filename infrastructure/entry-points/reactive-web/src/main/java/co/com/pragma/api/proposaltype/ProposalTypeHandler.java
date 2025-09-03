package co.com.pragma.api.proposaltype;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.exception.FieldValidationException;
import co.com.pragma.api.proposaltype.dto.CreateProposalTypeDTO;
import co.com.pragma.api.proposaltype.dto.ProposalTypeResponseDTO;
import co.com.pragma.api.proposaltype.mapper.ProposalTypeMapper;
import co.com.pragma.usecase.proposaltype.ProposalTypeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Proposal Types", description = "Proposal Types management APIs")
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
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
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
public class ProposalTypeHandler {

    private final ProposalTypeUseCase proposalTypeUseCase;
    private final ProposalTypeMapper mapper;
    private final Validator validator;

    @Operation(
            operationId = "saveProposalType",
            summary = "Save a proposal type",
            description = "Creates a new proposal type",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateProposalTypeDTO.class)
                    )
            ),responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Proposal Type created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProposalTypeResponseDTO.class)
                    )
            )
    }
    )
    public Mono<ServerResponse> listenSaveProposalType(ServerRequest serverRequest) {
        log.info("[ProposalTypeHandler] Received request to save a new proposal type");
        return serverRequest.bodyToMono(CreateProposalTypeDTO.class)
                .doOnNext(dto -> log.debug("[ProposalTypeHandler] Payload received: {}", dto))
                .flatMap(dto -> {
                    Errors errors = new BeanPropertyBindingResult(dto, CreateProposalTypeDTO.class.getName());
                    validator.validate(dto, errors);

                    if (errors.hasErrors()) {
                        List<String> messageErrors = errors.getFieldErrors().stream()
                                .map(fieldError ->  fieldError.getField() + " " + fieldError.getDefaultMessage())
                                .toList();
                        log.warn("[ProposalTypeHandler] Validation failed: {}", messageErrors);
                        return Mono.error(new FieldValidationException(messageErrors));
                    }
                    log.info("[ProposalTypeHandler] Validation successful for proposal type with name={}", dto.getName());
                    return Mono.just(dto);
                })
                .map(mapper::toDomain)
                .doOnNext(domain -> log.debug("[ProposalTypeHandler] Mapped DTO to domain model: {}", domain))
                .flatMap(proposalTypeUseCase::saveProposalType)
                .doOnSuccess(saved -> log.info("[ProposalTypeHandler] Proposal type saved successfully with id={}", saved.getId()))
                .map(mapper::toResponse)
                .doOnNext(response -> log.debug("[ProposalTypeHandler] Mapped domain model to response DTO: {}", response))
                .flatMap(savedProposalTypeResponse -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedProposalTypeResponse))
                .doOnError(error -> log.error("[ProposalTypeHandler] Error while saving proposal type", error));
    }


}
