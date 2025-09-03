package co.com.pragma.api.config;

import co.com.pragma.api.SecurityTestConfig;
import co.com.pragma.api.state.StateHandler;
import co.com.pragma.api.state.StateRouterRest;
import co.com.pragma.api.state.config.StatePath;
import co.com.pragma.api.state.dto.CreateStateDTO;
import co.com.pragma.api.state.dto.StateResponseDTO;
import co.com.pragma.api.state.mapper.StateMapper;
import co.com.pragma.model.state.State;
import co.com.pragma.usecase.state.StateUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StateRouterRest.class, StateHandler.class, StatePath.class, BasePath.class})
@TestPropertySource(properties = "routes.base-path=/api/v1")
@TestPropertySource(properties = "routes.paths.states=/estado")
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, SecurityTestConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private StateUseCase stateUseCase;

    @MockitoBean
    private StateMapper stateMapper;

    @MockitoBean
    private Validator validator;

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

    @BeforeEach
    void setUp() {
        when(validator.supports(CreateStateDTO.class)).thenReturn(true);
        Mockito.doAnswer(invocation -> null)
                .when(validator).validate(Mockito.any(), Mockito.any());

        when(stateMapper.toDomain(Mockito.any(CreateStateDTO.class))).thenReturn(domain);
        when(stateUseCase.saveState(Mockito.any(State.class))).thenReturn(Mono.just(domain));
        when(stateMapper.toResponse(Mockito.any(State.class))).thenReturn(response);
    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri("/api/v1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectBody(StateResponseDTO.class);
    }

}