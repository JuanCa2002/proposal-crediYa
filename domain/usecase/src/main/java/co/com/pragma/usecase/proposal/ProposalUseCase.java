package co.com.pragma.usecase.proposal;

import co.com.pragma.model.loan.Loan;
import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.exception.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ProposalUseCase {

    private final ProposalRepository proposalRepository;
    private final StateRepository stateRepository;
    private final ProposalTypeRepository proposalTypeRepository;
    private final UserRepository userRepository;
    private final SQSProposalNotification sqsProposalNotification;

    private static final String APPROVED_STATE_NAME = "APROBADO";
    private static final String REJECTED_STATE_NAME = "RECHAZADO";
    private static final String INITIAL_STATE_NAME = "PENDIENTE_REVISION";
    private static final String ROLE_ADMIN_NAME = "ADMINISTRADOR";

    public Mono<Proposal> saveProposal(Proposal proposal, String userName) {
        proposal.setCreationDate(LocalDate.now());
        return stateRepository.findByName(INITIAL_STATE_NAME)
                .switchIfEmpty(Mono.error(new InitialStateNotFound(INITIAL_STATE_NAME)))
                .flatMap(state -> {
                    proposal.setStateId(state.getId());
                    return proposalTypeRepository.findById(proposal.getProposalTypeId())
                            .switchIfEmpty(Mono.error(new ProposalTypeByIdNotFoundException(proposal.getProposalTypeId())));
                })
                .flatMap(proposalType -> validateProposalTypeRange(proposalType, proposal))
                .then(userRepository.findByIdentificationNumber(proposal.getUserIdentificationNumber()))
                .flatMap(user -> {
                    if(!user.getUserName().equals(userName)){
                        return Mono.error(new UserLoginNotMatchUserRequestUnauthorizedException());
                    }
                    proposal.setEmail(user.getEmail());
                    proposal.setBaseSalary(user.getBaseSalary());
                    return proposalRepository.save(proposal)
                                    .flatMap(savedProposal -> {
                                        proposal.setId(savedProposal.getId());
                                        return validateSendAutomaticValidation(proposal);
                                    });

                });
    }

    private Mono<Proposal> validateSendAutomaticValidation(Proposal proposal){
        if(proposal.getProposalType().getAutomaticValidation()){
            return calculateCurrentDebt(proposal)
                    .flatMap(sqsProposalNotification::sendRequestAutomaticRevision)
                    .thenReturn(proposal);
        }
        return Mono.just(proposal);
    }

    private Mono<Proposal> calculateCurrentDebt(Proposal proposal) {
        return proposalRepository.findByEmail(proposal.getEmail())
                .flatMap(currentProposal ->
                        stateRepository.findById(currentProposal.getStateId())
                                .map(state -> {
                                    currentProposal.setState(state);
                                    return currentProposal;
                                })
                )
                .filter(currentProposal ->
                        currentProposal.getState() != null &&
                                APPROVED_STATE_NAME.equals(currentProposal.getState().getName())
                )
                .map(Proposal::getMonthlyFee)
                .reduce(0.0, Double::sum)
                .flatMap(total -> {
                    proposal.setCurrentMonthlyDebt(total);
                    return Mono.just(proposal);
                });
    }

    public Flux<Proposal> findByCriteria(Long proposalTypeId, Integer stateId, String email,
                                         LocalDate initialDate, LocalDate endDate, Integer proposalLimit,int limit, int offset) {
        return proposalRepository.findByCriteria(proposalTypeId, stateId, email,
                        initialDate, endDate, proposalLimit, limit, offset)
                .flatMap(proposal ->
                        Mono.zip(
                                proposalTypeRepository.findById(proposal.getProposalTypeId()),
                                stateRepository.findById(proposal.getStateId())
                        ).map(tuple -> {
                            ProposalType type = tuple.getT1();
                            State state = tuple.getT2();
                            proposal.setState(state);
                            proposal.setProposalType(type);
                            return proposal;
                        })
                );
    }

    public Mono<Proposal> updateState(BigInteger proposalId, Integer newStateId, String role) {
        return validateIfProposalExists(proposalId)
                .flatMap(proposal -> findStateById(newStateId)
                        .flatMap(newState -> validateStateChange(proposal, newState, role))
                        .flatMap(newState -> applyStateChanges(proposal, newState))
                );
    }


    private Mono<State> findStateById(Integer stateId) {
        return stateRepository.findById(stateId)
                .switchIfEmpty(Mono.error(new StateByIdNotFoundException(stateId)));
    }

    private Mono<State> validateStateChange(Proposal proposal, State newState, String role) {
        return stateRepository.findById(proposal.getStateId())
                .flatMap(currentState -> {
                    if ((APPROVED_STATE_NAME.equals(currentState.getName()) ||
                            REJECTED_STATE_NAME.equals(currentState.getName())) &&
                            !ROLE_ADMIN_NAME.equals(role)) {
                        return Mono.error(new ProposalStateCanNotBeChangeBusinessException());
                    }

                    if (currentState.getId().equals(newState.getId())) {
                        return Mono.error(new ProposalStateAlreadyTheOneBusinessException(newState.getName()));
                    }

                    return Mono.just(newState);
                });
    }

    private Mono<Proposal> applyStateChanges(Proposal proposal, State newState) {
        return proposalTypeRepository.findById(proposal.getProposalTypeId())
                .flatMap(proposalType -> {
                    proposal.setState(newState);
                    proposal.setStateId(newState.getId());
                    proposal.setInterestRate(proposalType.getInterestRate());
                    proposal.setFinalDecision(newState.getName());
                    return calculateCurrentDebt(proposal)
                            .flatMap(this::calculateMaxCapacity)
                            .flatMap(this::calculateAllowCapacity)
                            .flatMap(this::calculateNewQuote)
                            .flatMap(this::calculateLoanPlan)
                            .flatMap(proposalRepository::save)
                            .flatMap(savedProposal -> sendNotificationIfRequired(proposal, newState));
                });
    }

    private Mono<Proposal> sendNotificationIfRequired(Proposal proposal, State state) {
        if (APPROVED_STATE_NAME.equals(state.getName()) || REJECTED_STATE_NAME.equals(state.getName())) {
            return sqsProposalNotification.sendNotification(proposal)
                    .thenReturn(proposal);
        }
        return Mono.just(proposal);
    }

    private Mono<Proposal> validateIfProposalExists(BigInteger id){
        return proposalRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProposalByIdNotFoundException(id)));
    }

    private Mono<Void> validateProposalTypeRange(ProposalType proposalType, Proposal proposal) {
        proposal.setProposalType(proposalType);
        proposal.setInterestRate(proposalType.getInterestRate());
        boolean isValid = proposal.getAmount() >= proposalType.getMinimumAmount()
                && proposal.getAmount() <= proposalType.getMaximumAmount();

        return isValid
                ? Mono.empty()
                : Mono.error(new ProposalAmountDoesNotMatchTypeBusinessException(proposalType.getName(),
                proposalType.getMinimumAmount(), proposalType.getMaximumAmount()));
    }

    private double calculateMonthlyFee(double amount, double yearlyRate, int months) {
        double i = yearlyRate / 12.0;
        return (amount * i) / (1 - Math.pow(1 + i, -months));
    }

    private Mono<Proposal> calculateMaxCapacity(Proposal proposal){
        proposal.setMaximumCapacity(proposal.getBaseSalary() * 0.35);
        return Mono.just(proposal);
    }

    private Mono<Proposal> calculateAllowCapacity(Proposal proposal){
        proposal.setAllowCapacity(proposal.getMaximumCapacity() - proposal.getCurrentMonthlyDebt());
        return Mono.just(proposal);
    }

    private Mono<Proposal> calculateNewQuote(Proposal proposal) {
        double i = proposal.getInterestRate();
        int n = proposal.getProposalLimit();
        proposal.setMonthlyFee(proposal.getAmount() * i * Math.pow(1 + i, n) / (Math.pow(1 + i, n) - 1));
        proposal.setNewMonthlyFee(proposal.getMonthlyFee());
        if (REJECTED_STATE_NAME.equals(proposal.getState().getName()) ||
                INITIAL_STATE_NAME.equals(proposal.getState().getName())) {
            proposal.setMonthlyFee(null);
        }
        return Mono.just(proposal);
    }

    private Mono<Proposal> calculateLoanPlan(Proposal proposal) {
        if(proposal.getFinalDecision().equals(APPROVED_STATE_NAME)){
            double balance = proposal.getAmount();
            double monthlyFee = proposal.getNewMonthlyFee();
            double interestRate = proposal.getInterestRate();
            int months = proposal.getProposalLimit();

            List<Loan> plan = new ArrayList<>();

            for (int month = 1; month <= months; month++) {
                double interestPayment = balance * interestRate;
                double capitalPayment = monthlyFee - interestPayment;
                balance -= capitalPayment;

                Loan loan = new Loan();
                loan.setQuoteNumber(month);
                loan.setTotalQuote(round(monthlyFee));
                loan.setInterests(round(interestPayment));
                loan.setCapital(round(capitalPayment));
                loan.setRemainingBalance(round(Math.max(balance, 0)));

                plan.add(loan);
            }

            proposal.setLoanPlan(plan);
        }
        return Mono.just(proposal);
    }

    private double round(double value) {
        double scale = Math.pow(10, 2);
        return Math.round(value * scale) / scale;
    }
}
