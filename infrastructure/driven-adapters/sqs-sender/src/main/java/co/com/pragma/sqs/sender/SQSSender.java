package co.com.pragma.sqs.sender;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Locale;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements SQSProposalNotification {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    public Mono<String> send(Proposal payload) {
        return Mono.fromCallable(() -> buildRequest(payload))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("[SQSSender] Message sent {}", response.messageId()))
                .doOnError(error -> log.error("[SQSSender] Error while sending email with payload {} and with error {}",payload, error))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(Proposal proposal) {
        String messageBody = String.format(
                Locale.US,
                "{\"email\":\"%s\",\"state\":\"%s\",\"id\":%d,\"amount\":%.2f}",
                proposal.getEmail(),
                proposal.getState().getName(),
                proposal.getId(),
                proposal.getAmount()
        );

        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(messageBody)
                .build();
    }
}
