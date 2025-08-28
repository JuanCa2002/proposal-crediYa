package co.com.pragma.r2dbc.state;

import co.com.pragma.model.state.State;
import co.com.pragma.r2dbc.entity.StateEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateReactiveRepositoryAdapterTest {

    @InjectMocks
    StateReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    StateReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator txOperator;

    private final State domain = State.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    private final StateEntity entity = StateEntity.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    @Test
    void shouldSaveState() {
        when(mapper.map(domain, StateEntity.class))
                .thenReturn(entity);

        when(mapper.map(entity, State.class))
                .thenReturn(domain);

        when(repository.save(Mockito.any(StateEntity.class)))
                .thenReturn(Mono.just(entity));

        when(txOperator.transactional(ArgumentMatchers.<Mono<State>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<State> result = repositoryAdapter.save(domain);

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void shouldFindByName() {

        when(repository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(entity));

        when(mapper.map(Mockito.any(StateEntity.class), Mockito.eq(State.class)))
                .thenReturn(domain);

        when(txOperator.transactional(ArgumentMatchers.<Mono<State>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<State> result = repositoryAdapter.findByName("PENDIENTE_REVISION");

        StepVerifier.create(result)
                .expectNextMatches(state -> state.getId().equals(1))
                .verifyComplete();
    }

    @Test
    void shouldFindById() {

        when(repository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(entity));

        when(mapper.map(Mockito.any(StateEntity.class), Mockito.eq(State.class)))
                .thenReturn(domain);

        when(txOperator.transactional(ArgumentMatchers.<Mono<State>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<State> result = repositoryAdapter.findById(1);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(1))
                .verifyComplete();
    }


}
