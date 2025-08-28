package co.com.pragma.usecase.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;
import co.com.pragma.usecase.proposal.exception.InitialStateNotFound;
import co.com.pragma.usecase.proposal.exception.ProposalTypeByIdNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProposalUseCaseTest {

    @InjectMocks
    ProposalUseCase proposalUseCase;

    @Mock
    ProposalRepository proposalRepository;

    @Mock
    StateRepository stateRepository;

    @Mock
    ProposalTypeRepository proposalTypeRepository;

    @Mock
    UserRepository userRepository;

    private final Proposal proposal = Proposal.builder()
                .id(BigInteger.ONE)
                .amount(3000.0)
                .userIdentificationNumber("123")
                .proposalLimit(4)
                .limitDate(LocalDate.of(2005, 5, 24))
                .email("juan@email.com")
                .stateId(1)
                .proposalTypeId(1L)
                .build();

    private final State state = State.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    private final ProposalType proposalType = ProposalType.builder()
            .id(1L)
            .name("Tipo de solicitud")
            .interestRate(0.19)
            .maximumAmount(20000.0)
            .minimumAmount(1000.0)
            .automaticValidation(false)
            .build();

    private final User user = User.builder()
            .id("1")
            .identificationNumber("123")
            .firstName("Juan")
            .secondName("Camilo")
            .firstLastName("Torres")
            .secondLastName("Beltrán")
            .email("juan@email.com")
            .baseSalary(1444.00)
            .build();


    @Test
    void shouldSaveProposal() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        when(proposalRepository.save(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal);

        StepVerifier.create(result)
                .expectNextMatches(proposal -> proposal.getId().equals(BigInteger.ONE))
                .verifyComplete();
    }

    @Test
    void shouldSaveProposal_InitialStateNotFoundException() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.empty());

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InitialStateNotFound &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.INITIAL_STATE_NOT_FOUND, "PENDIENTE_REVISION")
                                )
                )
                .verify();
    }

    @Test
    void shouldSaveProposal_ProposalTypeByIdNotFoundException() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.empty());

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalTypeByIdNotFoundException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.PROPOSAL_TYPE_NOT_FOUND, proposal.getProposalTypeId())
                                )
                )
                .verify();
    }

}
