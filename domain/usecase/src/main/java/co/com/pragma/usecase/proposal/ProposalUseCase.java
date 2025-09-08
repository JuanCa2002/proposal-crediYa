package co.com.pragma.usecase.proposal;

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
                    return proposalRepository.save(proposal);
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

    public Mono<Proposal> updateState(BigInteger id, Integer stateId, String role) {
        return proposalRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProposalByIdNotFoundException(id)))
                .flatMap(proposal ->
                        stateRepository.findById(stateId)
                                .switchIfEmpty(Mono.error(new StateByIdNotFoundException(stateId)))
                                .flatMap(state ->
                                        proposalTypeRepository.findById(proposal.getProposalTypeId())
                                                .flatMap(proposalType ->
                                                        stateRepository.findById(proposal.getStateId())
                                                                .flatMap(currentState -> {
                                                                    if((currentState.getName().contains(APPROVED_STATE_NAME) ||
                                                                    currentState.getName().contains(REJECTED_STATE_NAME)) && !role.equals(ROLE_ADMIN_NAME)){
                                                                        return Mono.error(new ProposalStateCanNotBeChangeBusinessException());
                                                                    }

                                                                    if(currentState.getId().equals(state.getId())) {
                                                                        return Mono.error(new ProposalStateAlreadyTheOneBusinessException(state.getName()));
                                                                    }

                                                                    if(state.getName().contains(APPROVED_STATE_NAME)) {
                                                                        proposal.setMonthlyFee(calculateMonthlyFee(proposal.getAmount(),
                                                                                proposalType.getInterestRate(), proposal.getProposalLimit()));
                                                                    }

                                                                    if(state.getName().contains(REJECTED_STATE_NAME) || state.getName().contains(INITIAL_STATE_NAME)){
                                                                        proposal.setMonthlyFee(null);
                                                                    }
                                                                    proposal.setStateId(stateId);
                                                                    return proposalRepository.save(proposal)
                                                                            .flatMap(updatedProposal -> {
                                                                                updatedProposal.setState(state);
                                                                                if(state.getName().contains(APPROVED_STATE_NAME) || state.getName().contains(REJECTED_STATE_NAME)){
                                                                                    return sqsProposalNotification.send(updatedProposal)
                                                                                            .thenReturn(updatedProposal);
                                                                                }
                                                                                return Mono.just(updatedProposal);
                                                                            });
                                                                })

                                                        )

                                )
                );
    }

    private Mono<Void> validateProposalTypeRange(ProposalType proposalType, Proposal proposal) {
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
}
