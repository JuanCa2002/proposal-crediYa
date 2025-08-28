package co.com.pragma.usecase.proposaltype;

import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.usecase.proposaltype.exception.MaximumHasToBeGreaterThanMinimumBusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProposalTypeUseCase {

    private final ProposalTypeRepository proposalTypeRepository;

    public Mono<ProposalType> saveProposalType(ProposalType proposalType) {
        return ValidMaximumGreaterThanMinimum(proposalType)
                .flatMap(isValid -> {
                    if(isValid){
                        return proposalTypeRepository.save(proposalType);
                    }
                    return Mono.error(new MaximumHasToBeGreaterThanMinimumBusinessException());
                });
    }

    private Mono<Boolean> ValidMaximumGreaterThanMinimum(ProposalType proposalType) {
        return Mono.just(proposalType.getMaximumAmount() >= proposalType.getMinimumAmount());
    }
}
