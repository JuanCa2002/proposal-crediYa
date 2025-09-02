package co.com.pragma.api.proposal;

import co.com.pragma.api.external.authentication.auth.ExternalAuthHandler;
import co.com.pragma.api.external.authentication.auth.dto.ValidateResponseDTO;
import co.com.pragma.api.proposal.dto.CreateProposalDTO;
import co.com.pragma.api.proposal.dto.ProposalFilterResponseDTO;
import co.com.pragma.api.proposal.dto.ProposalPaginatedResponseDTO;
import co.com.pragma.api.proposal.dto.ProposalResponseDTO;
import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.api.exception.FieldValidationException;
import co.com.pragma.api.proposal.dto.filter.ProposalFilterDTO;
import co.com.pragma.api.proposal.mapper.ProposalMapper;
import co.com.pragma.usecase.proposal.ProposalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final ExternalAuthHandler authHandler;
    private final ProposalMapper mapper;
    private final Validator validator;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
                .flatMap(domain ->
                        searchTokenInformation(serverRequest)
                                .doOnNext(validateResponse -> log.debug("[ProposalHandler] Token validated: {}", validateResponse))
                                .flatMap(validateResponse -> proposalUseCase.saveProposal(domain, validateResponse.getUserName()))
                )
                .doOnSuccess(saved -> log.info("[ProposalHandler] Proposal saved successfully with id={}", saved.getId()))
                .map(mapper::toResponse)
                .doOnNext(response -> log.debug("[ProposalHandler] Mapped domain model to response DTO: {}", response))
                .flatMap(savedProposalResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedProposalResponse))
                .doOnError(error -> log.error("[ProposalHandler] Error while saving proposal", error));
    }

    @Operation(
            operationId = "filterByCriteria",
            summary = "Filters proposals by criteria",
            description = "Filter proposals by criteria and returns a paginated list",
            parameters = {
                    @Parameter(name = "proposalTypeId", description = "ID of proposal type", in = ParameterIn.QUERY),
                    @Parameter(name = "stateId", description = "State ID", in = ParameterIn.QUERY),
                    @Parameter(name = "email", description = "Email to filter", in = ParameterIn.QUERY),
                    @Parameter(name = "initialDate", description = "Initial Date references to creation date of the proposal", in = ParameterIn.QUERY),
                    @Parameter(name = "endDate", description = "Final Date references to creation date of the proposal", in = ParameterIn.QUERY),
                    @Parameter(name = "proposalLimit", description = "Proposal months limit time", in = ParameterIn.QUERY),
                    @Parameter(name = "limit", description = "Page size", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "offset", description = "Page offset", in = ParameterIn.QUERY, required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of proposals by criteria",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProposalPaginatedResponseDTO.class)
                            )
                    )
            }
    )
    public Mono<ServerResponse> listenFilterByCriteria(ServerRequest serverRequest) {
        log.info("[ProposalHandler] Getting query params");
        String initialDate = serverRequest
                .queryParam("initialDate")
                .orElse(null);

        String endDate = serverRequest
                .queryParam("endDate")
                .orElse(null);

        Long proposalTypeId = serverRequest
                .queryParam("proposalTypeId")
                .map(Long::valueOf)
                .orElse(null);

        Integer stateId = serverRequest
                .queryParam("stateId")
                .map(Integer::valueOf)
                .orElse(null);

        Integer proposalLimit = serverRequest
                .queryParam("proposalLimit")
                .map(Integer::valueOf)
                .orElse(null);

        String email = serverRequest
                .queryParam("email")
                .orElse(null);

        Integer limit = Integer.parseInt(serverRequest.queryParam("limit").orElse("10"));
        Integer offset = Integer.parseInt(serverRequest.queryParam("offset").orElse("0"));

        ProposalFilterDTO filter = new ProposalFilterDTO();
        filter.setProposalTypeId(proposalTypeId);
        filter.setProposalLimit(proposalLimit);
        filter.setEndDate(endDate);
        filter.setInitialDate(initialDate);
        filter.setStateId(stateId);
        filter.setEmail(email);
        filter.setLimit(limit);
        filter.setOffset(offset);
        log.info("[ProposalHandler] Starting search with the following filter {}", filter);

        int page = (offset / limit) + 1;
        log.info("[ProposalHandler] Validating DTO errors");
        Errors errors = new BeanPropertyBindingResult(filter, ProposalFilterDTO.class.getName());
        validator.validate(filter, errors);
        if (errors.hasErrors()) {
            List<String> messageErrors = errors.getFieldErrors().stream()
                    .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                    .toList();
            log.warn("[ProposalHandler] Validation failed for entered filter with errors: {}", messageErrors);
            return Mono.error(new FieldValidationException(messageErrors));
        }
        log.info("[ProposalHandler] Finding records that match with the entered filter");
        return proposalUseCase.findByCriteria(
                        filter.getProposalTypeId(),
                        filter.getStateId(),
                        filter.getEmail(),
                        filter.getInitialDate()!= null ? LocalDate.parse(filter.getInitialDate(), FORMATTER) : null,
                        filter.getEndDate()!= null ? LocalDate.parse(filter.getEndDate(), FORMATTER): null,
                        filter.getProposalLimit(),
                        filter.getLimit(),
                        filter.getOffset()
                )
                .doOnNext(domain -> log.info("[ProposalHandler] Mapping domain to filter response"))
                .map(mapper::toResponseFilter)
                .collectList()
                .map(list -> {
                    log.info("[ProposalHandler] Collecting information and sorting it into the response model");
                    ProposalPaginatedResponseDTO response = new ProposalPaginatedResponseDTO();
                    response.setData(list);
                    response.setTotalElements(BigInteger.valueOf(list.size()));
                    response.setApprovedOnes(list.stream()
                            .filter(filterResponse -> filterResponse.getState().contains("APROBADO"))
                            .count());
                    response.setSumRequestApprovedAmount(list.stream()
                            .filter(filterResponse -> filterResponse.getState().contains("APROBADO"))
                            .mapToDouble(ProposalFilterResponseDTO::getAmount)
                            .sum());
                    response.setPage(page);
                    return response;
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnError(error -> log.error("[ProposalHandler] Error while searching records with the entered filter", error));
    }

    @Operation(
            operationId = "updateProposalState",
            summary = "Updates Proposal State",
            description = "Updates Proposal State of a previous created proposal",
            parameters = {
                    @Parameter(name = "id", description = "ID of proposal", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "stateId", description = "State ID", in = ParameterIn.QUERY, required = true),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "State updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProposalResponseDTO.class)
                            )
                    )
            }
    )
    public Mono<ServerResponse> listenUpdateStateProposal(ServerRequest serverRequest) {
        log.info("[ProposalHandler] Getting query and path params");
        BigInteger id = new BigInteger(serverRequest.pathVariable("id"));
        Integer stateId = serverRequest
                .queryParam("stateId")
                .map(Integer::valueOf)
                .orElse(null);
        log.info("[ProposalHandler] Updating state of the proposal with the id {} with the state id {}", id, stateId);
        return proposalUseCase.updateState(id, stateId)
                .doOnNext(proposal -> log.info("[ProposalHandler] Mapping domain to response"))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnError(error -> log.error("[ProposalHandler] Error while searching records with the entered filter", error));
    }

    private Mono<ValidateResponseDTO> searchTokenInformation(ServerRequest serverRequest) {
        String token = null;
        String authHeader = serverRequest.headers().firstHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        return authHandler.validateToken(token);
    }


}
