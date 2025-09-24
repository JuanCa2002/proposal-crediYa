package co.com.pragma.model.restconsumer.gateways;

import co.com.pragma.model.proposal.Proposal;
import reactor.core.publisher.Mono;

public interface LambdaInsertProposal {

    Mono<Void> insertProposal(Proposal proposal);
}
