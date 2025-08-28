package co.com.pragma.model.proposal.gateways;

import co.com.pragma.model.proposal.Proposal;
import reactor.core.publisher.Mono;

public interface ProposalRepository {

    Mono<Proposal> save(Proposal proposal);
}
