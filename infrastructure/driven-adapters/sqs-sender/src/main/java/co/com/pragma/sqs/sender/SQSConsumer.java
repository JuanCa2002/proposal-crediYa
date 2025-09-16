package co.com.pragma.sqs.sender;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.model.sqs.gateways.SQSProposalNotification;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@EnableScheduling
@Log4j2
@RequiredArgsConstructor
public class SQSConsumer {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ProposalRepository proposalRepository;
    private final StateRepository stateRepository;
    private final SQSProposalNotification sqsProposalNotification;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000)
    public void pollMessages() throws ExecutionException, InterruptedException {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(properties.queueAutomaticEvaluationResponsesUrl())
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        List<Message> messages = client.receiveMessage(request).get().messages();

        for (Message message : messages) {
            log.info("Receive message {}", message.body());

            try {
                Proposal proposal = objectMapper.readValue(message.body(), Proposal.class);
                log.info("Starting updating state for proposal {}", proposal.getId());

                updateState(proposal)
                        .flatMap(p ->
                                sqsProposalNotification.sendNotification(proposal)
                                        .thenReturn(p)
                        )
                        .doOnSuccess(p -> {
                            log.info("Proposal with id {}, updated with state {}", p.getId(), proposal.getFinalDecision());
                            client.deleteMessage(DeleteMessageRequest.builder()
                                    .queueUrl(properties.queueAutomaticEvaluationResponsesUrl())
                                    .receiptHandle(message.receiptHandle())
                                    .build());
                        })
                        .doOnError(e -> log.error("Error updating proposal {}: {}", proposal.getId(), e.getMessage()))
                        .subscribe();
            } catch (Exception e) {
                log.error("Error while processing message {}: {}", message.body(), e.getMessage());
            }
        }
    }

    private Mono<Proposal> updateState(Proposal proposal) {
        return validateIfProposalExists(proposal.getId())
                .flatMap(currentProposal -> validateAndAddStateIfExists(proposal.getFinalDecision(), currentProposal, proposal.getNewMonthlyFee()))
                .flatMap(proposalRepository::save);
    }

    private Mono<Proposal> validateIfProposalExists(BigInteger id){
        return proposalRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException()));
    }

    private Mono<Proposal> validateAndAddStateIfExists(String name, Proposal proposal, Double newMonthlyFee){
        return stateRepository.findByName(name)
                .switchIfEmpty(Mono.error(new RuntimeException()))
                .flatMap(state -> {
                    proposal.setStateId(state.getId());
                    proposal.setState(state);
                    if(state.getName().equals("APROBADO")){
                        proposal.setMonthlyFee(newMonthlyFee);
                    }
                    return  state.getName().equals("APROBADO") ?
                            sqsProposalNotification.sendMetricsToReport(proposal.getAmount())
                                            .thenReturn(proposal):
                            Mono.just(proposal);
                });
    }
}
