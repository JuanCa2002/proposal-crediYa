package co.com.pragma.r2dbc.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.r2dbc.entity.ProposalEntity;
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

import java.math.BigInteger;
import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProposalReactiveRepositoryAdapterTest {

    @InjectMocks
    ProposalReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    ProposalReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator txOperator;

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

    private final ProposalEntity entity = ProposalEntity.builder()
            .id(BigInteger.ONE)
            .amount(3000.0)
            .proposalLimit(4)
            .limitDate(LocalDate.of(2005, 5, 24))
            .email("juan@email.com")
            .stateId(1)
            .proposalTypeId(1L)
            .build();

    @Test
    void shouldSaveProposalType() {
        when(mapper.map(domain, ProposalEntity.class))
                .thenReturn(entity);

        when(mapper.map(entity, Proposal.class))
                .thenReturn(domain);

        when(repository.save(Mockito.any(ProposalEntity.class)))
                .thenReturn(Mono.just(entity));

        when(txOperator.transactional(ArgumentMatchers.<Mono<Proposal>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<Proposal> result = repositoryAdapter.save(domain);

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();
    }
}
