package co.com.pragma.api.proposal;

import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.proposal.config.ProposalPath;
import co.com.pragma.api.proposal.dto.CreateProposalDTO;
import co.com.pragma.api.proposal.dto.ProposalResponseDTO;
import co.com.pragma.api.proposal.mapper.ProposalMapper;
import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.usecase.proposal.ProposalUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ProposalRouterRest.class, ProposalHandler.class})
@EnableConfigurationProperties({ProposalPath.class, BasePath.class})
@TestPropertySource(properties = "routes.base-path=/api/v1")
@TestPropertySource(properties = "routes.paths.proposals=/solicitud")
@WebFluxTest
class ProposalRouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProposalUseCase proposalUseCase;

    @MockitoBean
    private ProposalMapper proposalMapper;

    @MockitoBean
    private Validator validator;

    private final String proposals = "/solicitud";
    private final String mainPath = "/api/v1";

    private final CreateProposalDTO request = CreateProposalDTO.builder()
            .amount(3000.0)
            .userIdentificationNumber("123")
            .proposalLimit(4)
            .proposalTypeId(1L)
            .build();

    private final Proposal domain = Proposal.builder()
            .id(BigInteger.ONE)
            .amount(3000.0)
            .userIdentificationNumber("123")
            .proposalLimit(4)
            .limitDate(LocalDate.of(2005, 5, 24))
            .email("juan@email.com")
            .stateId(1)
            .proposalTypeId(1L)
            .build();

    private final ProposalResponseDTO response = ProposalResponseDTO.builder()
            .id(BigInteger.ONE)
            .amount(3000.0)
            .limitDate(LocalDate.of(2028, 8, 20))
            .email("juan@email.com")
            .build();

    @Autowired
    private ProposalPath proposalPath;

    @Autowired
    private BasePath basePath;

    @Test
    void shouldLoadUserPathProperties() {
        assertEquals("/solicitud", proposalPath.getProposals());
        assertEquals("/api/v1", basePath.getBasePath());
    }

    @Test
    void shouldPostSaveProposal() {
        when(validator.supports(CreateProposalDTO.class)).thenReturn(true);
        Mockito.doAnswer(invocation -> null)
                .when(validator).validate(Mockito.any(), Mockito.any());

        when(proposalMapper.toDomain(Mockito.any(CreateProposalDTO.class))).thenReturn(domain);
        when(proposalUseCase.saveProposal(Mockito.any(Proposal.class))).thenReturn(Mono.just(domain));
        when(proposalMapper.toResponse(Mockito.any(Proposal.class))).thenReturn(response);

        webTestClient.post()
                .uri(mainPath + proposals)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProposalResponseDTO.class)
                .value(saved -> Assertions.assertThat(saved.getEmail()).isEqualTo(response.getEmail()));
    }


}
