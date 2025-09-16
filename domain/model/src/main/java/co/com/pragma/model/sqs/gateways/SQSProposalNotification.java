package co.com.pragma.model.sqs.gateways;

import co.com.pragma.model.proposal.Proposal;
import reactor.core.publisher.Mono;

public interface SQSProposalNotification {
    Mono<String> sendNotification(Proposal payload);
    Mono<String> sendRequestAutomaticRevision(Proposal proposal);
    Mono<String> sendMetricsToReport(Double extraApprovedAmount);
}
