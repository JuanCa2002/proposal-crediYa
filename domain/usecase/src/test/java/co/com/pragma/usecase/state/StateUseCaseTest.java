package co.com.pragma.usecase.state;

import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.usecase.state.constants.StateMessageConstants;
import co.com.pragma.usecase.state.exception.StateByNameAlreadyExistsBusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.MessageFormat;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StateUseCaseTest {

    @InjectMocks
    StateUseCase stateUseCase;

    @Mock
    StateRepository stateRepository;

    private final State state = State.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    @Test
    void shouldSaveState() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.empty());

        when(stateRepository.save(Mockito.any(State.class)))
                .thenReturn(Mono.just(state));

        Mono<State> result = stateUseCase.saveState(state);

        StepVerifier.create(result)
                .expectNextMatches(state ->
                        state.getId().equals(1) &&
                        state.getName().equals(this.state.getName()))
                .verifyComplete();
    }

    @Test
    void shouldSaveState_StateByNameAlreadyExistsBusinessException() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(stateRepository.save(Mockito.any(State.class)))
                .thenReturn(Mono.just(state));

        Mono<State> result = stateUseCase.saveState(state);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof StateByNameAlreadyExistsBusinessException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(StateMessageConstants.STATE_BY_NAME_ALREADY_EXISTS, state.getName())
                                )
                )
                .verify();
    }
}
