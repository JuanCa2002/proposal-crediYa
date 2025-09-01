package co.com.pragma.api.proposal;

import co.com.pragma.api.proposal.dto.CreateProposalDTO;
import co.com.pragma.api.proposal.dto.ProposalResponseDTO;
import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.exception.FieldValidationException;
import co.com.pragma.api.proposal.mapper.ProposalMapper;
import co.com.pragma.usecase.proposal.ProposalUseCase;
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
@Tag(name = "Proposals", description = "Proposal management APIs")
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
public class ProposalHandler {

    private final ProposalUseCase proposalUseCase;
    private final ProposalMapper mapper;
    private final Validator validator;

    @Operation(
            operationId = "saveProposal",
            summary = "Save a proposal",
            description = "Creates a new proposal",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateProposalDTO.class)
                    )
            ),responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proposal created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProposalResponseDTO.class)
                    )
            )
    }
    )
    public Mono<ServerResponse> listenSaveProposal(ServerRequest serverRequest) {
        log.info("[ProposalHandler] Received request to save a new proposal");
        return serverRequest.bodyToMono(CreateProposalDTO.class)
                .doOnNext(dto -> log.debug("Payload received: {}", dto))
                .flatMap(dto -> {
                    Errors errors = new BeanPropertyBindingResult(dto, CreateProposalDTO.class.getName());
                    validator.validate(dto, errors);

                    if (errors.hasErrors()) {
                        List<String> messageErrors = errors.getFieldErrors().stream()
                                .map(fieldError ->  fieldError.getField() + " " + fieldError.getDefaultMessage())
                                .toList();
                        log.warn("[ProposalHandler] Validation failed for proposal: {}", messageErrors);
                        return Mono.error(new FieldValidationException(messageErrors));
                    }
                    log.info("[ProposalHandler] Validation successful for this proposal");
                    return Mono.just(dto);
                })
                .map(mapper::toDomain)
                .doOnNext(domain -> log.debug("[ProposalHandler] Mapped DTO to domain model: {}", domain))
                .flatMap(proposalUseCase::saveProposal)
                .doOnSuccess(saved -> log.info("[ProposalHandler] Proposal saved successfully with id={}", saved.getId()))
                .map(mapper::toResponse)
                .doOnNext(response -> log.debug("[ProposalHandler] Mapped domain model to response DTO: {}", response))
                .flatMap(savedProposalResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedProposalResponse))
                .doOnError(error -> log.error("[ProposalHandler] Error while saving proposal", error));
    }


}
