package co.com.pragma.consumer.lambdainsertproposal;

import co.com.pragma.consumer.lambdainsertproposal.dto.LambdaInsertProposalRequestDTO;
import co.com.pragma.consumer.lambdainsertproposal.mapper.LambdaInsertProposalMapper;
import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.restconsumer.gateways.LambdaInsertProposal;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LambdaInsertProposalConsumer implements LambdaInsertProposal {

    private final LambdaInsertProposalMapper mapper;
    private final WebClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LambdaInsertProposalConsumer(LambdaInsertProposalMapper mapper,
                                        @Qualifier("lambdaInsertProposalWebClient") WebClient client) {
        this.mapper = mapper;
        this.client = client;
    }


    @CircuitBreaker(name = "insertProposal")
    public Mono<Void> insertProposal(Proposal proposal) {
        log.info("[LambdaInsertProposalConsumer] preparing proposal to insert...");
        LambdaInsertProposalRequestDTO request = mapper.toRequest(proposal);
        log.info("[LambdaInsertProposalConsumer] request ready to insert");
        try {
            String bodyAsJson = objectMapper.writeValueAsString(request);

            return client.post()
                    .attribute("signedBody", bodyAsJson)
                    .bodyValue(bodyAsJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("[LambdaInsertProposalConsumer] proposal inserted successfully {}", response))
                    .doOnError(error -> log.info("[LambdaInsertProposalConsumer] error while inserting proposal ", error))
                    .then(Mono.empty());

        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error serializing request to JSON", e));
        }
    }
}
