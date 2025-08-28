package co.com.pragma.api.proposaltype;

import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.proposaltype.config.ProposalTypePath;
import co.com.pragma.api.proposaltype.dto.CreateProposalTypeDTO;
import co.com.pragma.api.proposaltype.dto.ProposalTypeResponseDTO;
import co.com.pragma.api.proposaltype.mapper.ProposalTypeMapper;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.usecase.proposaltype.ProposalTypeUseCase;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ProposalTypeRouterRest.class, ProposalTypeHandler.class})
@EnableConfigurationProperties({ProposalTypePath.class, BasePath.class})
@TestPropertySource(properties = "routes.base-path=/api/v1")
@TestPropertySource(properties = "routes.paths.proposalTypes=/tipo-prestamo")
@WebFluxTest
public class ProposalTypeRouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProposalTypeUseCase proposalTypeUseCase;

    @MockitoBean
    private ProposalTypeMapper proposalTypeMapper;

    @MockitoBean
    private Validator validator;

    private final String proposalTypes = "/tipo-prestamo";
    private final String mainPath = "/api/v1";

    private final CreateProposalTypeDTO request = CreateProposalTypeDTO.builder()
            .name("Tipo de solicitud")
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .interestRate(0.19)
            .build();

    private final ProposalType domain = ProposalType.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    private final ProposalTypeResponseDTO response = ProposalTypeResponseDTO.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    @Autowired
    private ProposalTypePath proposalTypePath;

    @Autowired
    private BasePath basePath;

    @Test
    void shouldLoadUserPathProperties() {
        assertEquals("/tipo-prestamo", proposalTypePath.getProposalTypes());
        assertEquals("/api/v1", basePath.getBasePath());
    }

    @Test
    void shouldPostSaveProposal() {
        when(validator.supports(CreateProposalTypeDTO.class)).thenReturn(true);
        Mockito.doAnswer(invocation -> null)
                .when(validator).validate(Mockito.any(), Mockito.any());

        when(proposalTypeMapper.toDomain(Mockito.any(CreateProposalTypeDTO.class))).thenReturn(domain);
        when(proposalTypeUseCase.saveProposalType(Mockito.any(ProposalType.class))).thenReturn(Mono.just(domain));
        when(proposalTypeMapper.toResponse(Mockito.any(ProposalType.class))).thenReturn(response);

        webTestClient.post()
                .uri(mainPath + proposalTypes)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProposalTypeResponseDTO.class)
                .value(saved -> Assertions.assertThat(saved.getId()).isEqualTo(response.getId()));
    }
}
