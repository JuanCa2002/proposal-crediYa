package co.com.pragma.usecase.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.restconsumer.gateways.LambdaLoanPlan;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;
import co.com.pragma.usecase.proposal.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
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

    @Mock
    SQSProposalNotification sqsProposalNotification;

    @Mock
    LambdaLoanPlan lambdaLoanPlan;

    private final Proposal proposal = Proposal.builder()
                .id(BigInteger.ONE)
                .amount(3000.0)
                .userIdentificationNumber("123")
                .newMonthlyFee(2000.0)
                .currentMonthlyDebt(40000.0)
                .monthlyFee(222.0)
                .allowCapacity(30000.0)
                .baseSalary(300000.0)
                .maximumCapacity(70000.0)
                .finalDecision("APROBADO")
                .interestRate(0.02)
                .proposalLimit(4)
                .limitDate(LocalDate.of(2005, 5, 24))
                .email("juan@email.com")
                .stateId(1)
                .proposalTypeId(1L)
                .build();

    private final Proposal proposalTwo = Proposal.builder()
            .id(BigInteger.TWO)
            .amount(60000.0)
            .userIdentificationNumber("456")
            .proposalLimit(4)
            .limitDate(LocalDate.of(2003, 5, 24))
            .email("goku@email.com")
            .stateId(1)
            .proposalTypeId(1L)
            .build();

    private final State state = State.builder()
            .id(1)
            .name("PENDIENTE_REVISION")
            .description("Pendiente de revisión")
            .build();

    private final State currentState = State.builder()
            .id(1)
            .name("APROBADO")
            .description("Aprobado")
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
            .userName("juan")
            .baseSalary(1440.0)
            .identificationNumber("123")
            .firstName("Juan")
            .secondName("Camilo")
            .firstLastName("Torres")
            .secondLastName("Beltrán")
            .email("juan@email.com")
            .baseSalary(1444.00)
            .build();

    @Test
    void shouldUpdateProposalState() {
        proposal.setStateId(2);
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        state.setId(1);
        currentState.setId(2);
        currentState.setName("PENDIENTE_REVISION");
        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(proposalRepository.save(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(proposalRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Flux.just(proposal));

        when(lambdaLoanPlan.postLambdaLoanPlan(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ASESOR");

        StepVerifier.create(result)
                .expectNextMatches(proposal ->
                        proposal.getId().equals(BigInteger.ONE) &&
                                proposal.getStateId().equals(state.getId())
                )
                .verifyComplete();
    }

    @Test
    void shouldUpdateProposalState_whenStateIsApproved() {
        proposal.setStateId(2);
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        state.setId(1);
        currentState.setId(2);
        state.setName("APROBADO");
        currentState.setName("PENDIENTE_REVISION");
        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(proposalRepository.save(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(proposalRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Flux.just(proposal));

        when(lambdaLoanPlan.postLambdaLoanPlan(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(sqsProposalNotification.sendNotification(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just("good"));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ASESOR");

        StepVerifier.create(result)
                .expectNextMatches(proposal ->
                        proposal.getId().equals(BigInteger.ONE) &&
                                proposal.getStateId().equals(state.getId())  &&
                                proposal.getMonthlyFee() != null
                )
                .verifyComplete();
    }

    @Test
    void shouldUpdateProposalState_WhenRoleIsAdmin() {
        proposal.setStateId(2);
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        state.setId(1);
        currentState.setId(2);
        state.setName("RECHAZADO");
        currentState.setName("APROBADO");
        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(proposalRepository.save(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(proposalRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Flux.just(proposal));

        when(lambdaLoanPlan.postLambdaLoanPlan(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(sqsProposalNotification.sendNotification(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just("good"));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ADMINISTRADOR");

        StepVerifier.create(result)
                .expectNextMatches(proposal ->
                        proposal.getId().equals(BigInteger.ONE) &&
                                proposal.getStateId().equals(state.getId()) &&
                                proposal.getMonthlyFee() == null
                )
                .verifyComplete();
    }

    @Test
    void shouldUpdateProposalState_ProposalCanNotChangeManuallyBusinessException() {
        proposal.setStateId(2);
        proposal.setFinalDecision("PENDIENTE_REVISION");
        proposalType.setAutomaticValidation(true);
        proposal.setProposalType(proposalType);
        proposal.setProposalTypeId(proposalType.getId());
        state.setId(1);
        state.setName("PENDIENTE_REVISION");
        currentState.setId(2);
        currentState.setName("PENDIENTE_REVISION");

        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ADMINISTRADOR");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalCanNotChangeManuallyBusinessException &&
                                throwable.getMessage().equals(
                                        ProposalMessageConstants.PROPOSAL_CAN_CHANGE_STATE_MANUALLY
                                )
                )
                .verify();
    }

    @Test
    void shouldUpdateProposalState_ProposalStateCanNotBeChangeBusinessException() {
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        state.setName("APROBADO");
        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ASESOR");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalStateCanNotBeChangeBusinessException &&
                                throwable.getMessage().equals(
                                        ProposalMessageConstants.PROPOSAL_STATE_CAN_NOT_BE_CHANGE
                                )
                )
                .verify();
    }

    @Test
    void shouldUpdateProposalState_ProposalStateAlreadyTheOneBusinessException() {
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        state.setName("APROBADO");
        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state))
                .thenReturn(Mono.just(currentState));

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ADMINISTRADOR");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalStateAlreadyTheOneBusinessException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.PROPOSAL_STATE_ALREADY_IS_THE_SELECTED_ONE, state.getName())
                                )
                )
                .verify();
    }

    @Test
    void shouldUpdateProposalState_ProposalByIdNotFoundException() {
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.empty());

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ASESOR");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalByIdNotFoundException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.PROPOSAL_BY_ID_NOT_FOUND_EXCEPTION, proposal.getId())
                                )
                )
                .verify();
    }

    @Test
    void shouldUpdateProposalState_StateByIdNotFoundException() {
        when(proposalRepository.findById(Mockito.any(BigInteger.class)))
                .thenReturn(Mono.just(proposal));

        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.empty());

        Mono<Proposal> result = proposalUseCase.updateState(proposal.getId(), state.getId(), "ASESOR");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof StateByIdNotFoundException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.STATE_NOT_FOUND, state.getId())
                                )
                )
                .verify();
    }

    @Test
    void findByCriteria() {
        when(proposalRepository.findByCriteria(Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Flux.just(proposal, proposalTwo));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state));

        Flux<Proposal> result = proposalUseCase.findByCriteria(proposalType.getId(),
                state.getId(), proposal.getEmail(), LocalDate.of(2000, 8, 8), LocalDate.of(2080, 8, 8), 1, 10, 0);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.getId().equals(BigInteger.ONE))
                .expectNextMatches(p -> p.getId().equals(BigInteger.TWO))
                .verifyComplete();
    }

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

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, user.getUserName());

        StepVerifier.create(result)
                .expectNextMatches(proposal -> proposal.getId().equals(BigInteger.ONE))
                .verifyComplete();
    }

    @Test
    void shouldSaveProposal_WhenIsAutomaticValidation() {
        proposalType.setAutomaticValidation(true);
        proposal.setProposalType(proposalType);
        proposal.setState(state);
        proposal.setStateId(state.getId());
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(stateRepository.findById(Mockito.anyInt()))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        when(proposalRepository.save(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just(proposal));

        when(proposalRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Flux.just(proposal));

        when(sqsProposalNotification.sendRequestAutomaticRevision(Mockito.any(Proposal.class)))
                .thenReturn(Mono.just("ok"));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, user.getUserName());

        StepVerifier.create(result)
                .expectNextMatches(proposal -> proposal.getId().equals(BigInteger.ONE))
                .verifyComplete();
    }

    @Test
    void shouldSaveProposal_InitialStateNotFoundException() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.empty());

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, user.getUserName());

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

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, user.getUserName());

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalTypeByIdNotFoundException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.PROPOSAL_TYPE_NOT_FOUND, proposal.getProposalTypeId())
                                )
                )
                .verify();
    }

    @Test
    void shouldSaveProposal_ProposalAmountDoesNotMatchTypeBusinessException() {
        proposal.setAmount(80000.0);
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, user.getUserName());

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProposalAmountDoesNotMatchTypeBusinessException &&
                                throwable.getMessage().equals(
                                        MessageFormat.format(ProposalMessageConstants.PROPOSAL_TYPE_NOT_MATCH, proposalType.getName()
                                                , proposalType.getMinimumAmount(), proposalType.getMaximumAmount())
                                )
                )
                .verify();
    }

    @Test
    void shouldSaveProposal_UserLoginNotMatchUserRequestUnauthorizedException() {
        when(stateRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(state));

        when(proposalTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(proposalType));

        when(userRepository.findByIdentificationNumber(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        Mono<Proposal> result = proposalUseCase.saveProposal(proposal, "error");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserLoginNotMatchUserRequestUnauthorizedException &&
                                throwable.getMessage().equals(
                                        ProposalMessageConstants.USER_NOT_MATCH_LOGIN_USER
                                )
                )
                .verify();
    }

}
