package co.com.pragma.usecase.proposaltype;

import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.usecase.proposaltype.constants.ProposalTypeMessageConstants;
import co.com.pragma.usecase.proposaltype.exception.MaximumHasToBeGreaterThanMinimumBusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProposalTypeUseCaseTest {

    @InjectMocks
    ProposalTypeUseCase proposalTypeUseCase;

    @Mock
    ProposalTypeRepository repository;

    private final ProposalType proposalType = ProposalType.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    @Test
    void shouldSaveProposalType() {
        when(repository.save(Mockito.any(ProposalType.class)))
                .thenReturn(Mono.just(proposalType));

        Mono<ProposalType> result = proposalTypeUseCase.saveProposalType(proposalType);

        StepVerifier.create(result)
                .expectNextMatches(proposalType ->
                        proposalType.getId().equals(1L) &&
                        proposalType.getName().equals(this.proposalType.getName()))
                .verifyComplete();
    }

    @Test
    void shouldSaveProposalType_MaximumHasToBeGreaterThanMinimumBusinessException() {
        proposalType.setMaximumAmount(0.0);

        Mono<ProposalType> result = proposalTypeUseCase.saveProposalType(proposalType);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof MaximumHasToBeGreaterThanMinimumBusinessException &&
                                throwable.getMessage().equals(
                                        ProposalTypeMessageConstants.MAXIMUM_AMOUNT_INVALID
                                )
                )
                .verify();
    }

}
