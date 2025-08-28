package co.com.pragma.model.proposaltype.gateways;

import co.com.pragma.model.proposaltype.ProposalType;
import reactor.core.publisher.Mono;

public interface ProposalTypeRepository {

    Mono<ProposalType> save(ProposalType proposalType);
    Mono<ProposalType> findById(Long id);
}
