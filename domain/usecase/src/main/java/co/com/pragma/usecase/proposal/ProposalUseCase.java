package co.com.pragma.usecase.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.exception.InitialStateNotFound;
import co.com.pragma.usecase.proposal.exception.ProposalTypeByIdNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProposalUseCase {

    private final ProposalRepository proposalRepository;
    private final StateRepository stateRepository;
    private final ProposalTypeRepository proposalTypeRepository;
    private final UserRepository userRepository;

    private static final String INITIAL_STATE_NAME = "PENDIENTE_REVISION";

    public Mono<Proposal> saveProposal(Proposal proposal) {
        return stateRepository.findByName(INITIAL_STATE_NAME)
                .switchIfEmpty(Mono.error(new InitialStateNotFound(INITIAL_STATE_NAME)))
                .flatMap(state -> {
                    proposal.setStateId(state.getId());
                    return proposalTypeRepository.findById(proposal.getProposalTypeId())
                            .switchIfEmpty(Mono.error(new ProposalTypeByIdNotFoundException(proposal.getProposalTypeId())));
                })
                .flatMap(proposalType ->
                        userRepository.findByIdentificationNumber(proposal.getUserIdentificationNumber())
                )
                .flatMap(user -> {
                    proposal.setEmail(user.getEmail());
                    return proposalRepository.save(proposal);
                });
    }
}
