package co.com.pragma.model.sqs.gateways;

import co.com.pragma.model.proposal.Proposal;
import reactor.core.publisher.Mono;

public interface SQSProposalNotification {
    Mono<String> send(Proposal payload);
}
