package co.com.pragma.r2dbc.proposaltype;

import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.r2dbc.entity.ProposalTypeEntity;
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
public class ProposalTypeReactiveRepositoryAdapterTest {

    @InjectMocks
    ProposalTypeReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    ProposalTypeReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator txOperator;

    private final ProposalType domain = ProposalType.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    private final ProposalTypeEntity entity = ProposalTypeEntity.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    @Test
    void shouldSaveProposalType() {
        when(mapper.map(domain, ProposalTypeEntity.class))
                .thenReturn(entity);

        when(mapper.map(entity, ProposalType.class))
                .thenReturn(domain);

        when(repository.save(Mockito.any(ProposalTypeEntity.class)))
                .thenReturn(Mono.just(entity));

        when(txOperator.transactional(ArgumentMatchers.<Mono<ProposalType>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ProposalType> result = repositoryAdapter.save(domain);

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void shouldFindById() {

        when(repository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(entity));

        when(mapper.map(Mockito.any(ProposalTypeEntity.class), Mockito.eq(ProposalType.class)))
                .thenReturn(domain);

        when(txOperator.transactional(ArgumentMatchers.<Mono<ProposalType>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ProposalType> result = repositoryAdapter.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(proposalType ->
                        proposalType.getId().equals(1L))
                .verifyComplete();
    }
}
