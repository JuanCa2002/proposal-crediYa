package co.com.pragma.consumer.lambdaloanplan;

import co.com.pragma.consumer.lambdaloanplan.dto.LambdaLoanPlanRequest;
import co.com.pragma.consumer.lambdaloanplan.dto.LambdaLoanPlanResponse;
import co.com.pragma.consumer.lambdaloanplan.mapper.LambdaMapper;
import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.restconsumer.gateways.LambdaLoanPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class LambdaLoanPlanConsumer implements LambdaLoanPlan {

    private final LambdaMapper mapper;
    private final WebClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LambdaLoanPlanConsumer(LambdaMapper mapper,
                                  @Qualifier("lambdaWebClient") WebClient client) {
        this.mapper = mapper;
        this.client = client;
    }


    @CircuitBreaker(name = "postLambdaLoanPlan")
    public Mono<Proposal> postLambdaLoanPlan(Proposal proposal) {
        LambdaLoanPlanRequest request = mapper.toRequest(proposal);
        try {
            String bodyAsJson = objectMapper.writeValueAsString(request);

            return client.post()
                    .attribute("signedBody", bodyAsJson)
                    .bodyValue(bodyAsJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(json -> {
                        try {
                            LambdaLoanPlanResponse response =
                                    objectMapper.readValue(json, LambdaLoanPlanResponse.class);
                            return mapper.toDomain(response);
                        } catch (Exception e) {
                            throw new RuntimeException("Error parsing Lambda response", e);
                        }
                    });

        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error serializing request to JSON", e));
        }
    }
}
