package co.com.pragma.model.proposal.gateways;

import co.com.pragma.model.proposal.Proposal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProposalRepository {

    Mono<Proposal> save(Proposal proposal);
    Flux<Proposal> findByCriteria(Long proposalTypeId,
                                  Integer stateId,
                                  String email,
                                  int limit,
                                  int offset);
}
