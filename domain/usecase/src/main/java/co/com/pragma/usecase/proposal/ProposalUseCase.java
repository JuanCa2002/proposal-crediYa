package co.com.pragma.usecase.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.exception.InitialStateNotFound;
import co.com.pragma.usecase.proposal.exception.ProposalAmountDoesNotMatchTypeBusinessException;
import co.com.pragma.usecase.proposal.exception.ProposalTypeByIdNotFoundException;
import co.com.pragma.usecase.proposal.exception.UserLoginNotMatchUserRequestUnauthorizedException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
public class ProposalUseCase {

    private final ProposalRepository proposalRepository;
    private final StateRepository stateRepository;
    private final ProposalTypeRepository proposalTypeRepository;
    private final UserRepository userRepository;

    private static final String INITIAL_STATE_NAME = "PENDIENTE_REVISION";

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

    private Mono<Void> validateProposalTypeRange(ProposalType proposalType, Proposal proposal) {
        boolean isValid = proposal.getAmount() >= proposalType.getMinimumAmount()
                && proposal.getAmount() <= proposalType.getMaximumAmount();

        return isValid
                ? Mono.empty()
                : Mono.error(new ProposalAmountDoesNotMatchTypeBusinessException(proposalType.getName(),
                proposalType.getMinimumAmount(), proposalType.getMaximumAmount()));
    }
}
