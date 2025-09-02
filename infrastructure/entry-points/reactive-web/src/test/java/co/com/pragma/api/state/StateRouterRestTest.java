package co.com.pragma.api.state;

import co.com.pragma.api.SecurityTestConfig;
import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.state.config.StatePath;
import co.com.pragma.api.state.dto.CreateStateDTO;
import co.com.pragma.api.state.dto.StateResponseDTO;
import co.com.pragma.api.state.mapper.StateMapper;
import co.com.pragma.model.state.State;
import co.com.pragma.usecase.state.StateUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StateRouterRest.class, StateHandler.class})
@EnableConfigurationProperties({StatePath.class, BasePath.class})
@TestPropertySource(properties = "routes.base-path=/api/v1")
@TestPropertySource(properties = "routes.paths.states=/estado")
@Import(SecurityTestConfig.class)
@WebFluxTest
public class StateRouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private StateUseCase stateUseCase;

    @MockitoBean
    private StateMapper stateMapper;

    @MockitoBean
    private Validator validator;

    private final String states = "/estado";
    private final String mainPath = "/api/v1";

    private final CreateStateDTO request = CreateStateDTO.builder()
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    private final State domain = State.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    private final StateResponseDTO response = StateResponseDTO.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    @Autowired
    private StatePath statePath;

    @Autowired
    private BasePath basePath;

    @Test
    void shouldLoadUserPathProperties() {
        assertEquals("/estado", statePath.getStates());
        assertEquals("/api/v1", basePath.getBasePath());
    }

    @Test
    void shouldPostSaveProposal() {
        when(validator.supports(CreateStateDTO.class)).thenReturn(true);
        Mockito.doAnswer(invocation -> null)
                .when(validator).validate(Mockito.any(), Mockito.any());

        when(stateMapper.toDomain(Mockito.any(CreateStateDTO.class))).thenReturn(domain);
        when(stateUseCase.saveState(Mockito.any(State.class))).thenReturn(Mono.just(domain));
        when(stateMapper.toResponse(Mockito.any(State.class))).thenReturn(response);

        webTestClient.post()
                .uri(mainPath + states)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StateResponseDTO.class)
                .value(saved -> Assertions.assertThat(saved.getId()).isEqualTo(response.getId()));
    }
}
