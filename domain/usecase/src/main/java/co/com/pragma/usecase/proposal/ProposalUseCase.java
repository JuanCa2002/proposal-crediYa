package co.com.pragma.usecase.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.proposal.exception.ProposalTypeByIdNotFoundException;
import co.com.pragma.usecase.proposal.exception.StateByIdNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProposalUseCase {

    private final ProposalRepository proposalRepository;
    private final StateRepository stateRepository;
    private final ProposalTypeRepository proposalTypeRepository;
    private final UserRepository userRepository;

    public Mono<Proposal> saveProposal(Proposal proposal) {
        return stateRepository.findById(proposal.getStateId())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new StateByIdNotFoundException(proposal.getStateId()))))
                .flatMap(state ->
                        proposalTypeRepository.findById(proposal.getProposalTypeId())
                                .switchIfEmpty(Mono.defer(() -> Mono.error(new ProposalTypeByIdNotFoundException(proposal.getProposalTypeId()))))
                                .flatMap(proposalType ->
                                        userRepository.findByIdentificationNumber(proposal.getUserIdentificationNumber())
                                                .flatMap(user -> {
                                                    proposal.setEmail(user.getEmail());
                                                    return proposalRepository.save(proposal);
                                                })
                                )
                );
    }
}
