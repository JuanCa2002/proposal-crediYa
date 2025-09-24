package co.com.pragma.consumer;


import co.com.pragma.consumer.lambdaloanplan.LambdaLoanPlanConsumer;
import co.com.pragma.consumer.lambdaloanplan.dto.LambdaLoanPlanRequest;
import co.com.pragma.consumer.lambdaloanplan.dto.LambdaLoanPlanResponse;
import co.com.pragma.consumer.lambdaloanplan.mapper.LambdaMapper;
import co.com.pragma.model.proposal.Proposal;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;


@ExtendWith(MockitoExtension.class)
class LambdaLoanPlanConsumerTest {

    private static LambdaLoanPlanConsumer lambdaLoanPlanConsumer;

    private static MockWebServer mockBackEnd;

    @Mock
    private LambdaMapper mapper;

    @BeforeAll
    static void setUpAll() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void setUp() {
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        lambdaLoanPlanConsumer = new LambdaLoanPlanConsumer(mapper, webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Validate the function testPost.")
    void validateTestPost() {

        final Proposal proposal = Proposal.builder()
                        .amount(19000.0)
                        .email("juan@email.com")
                        .baseSalary(10000.0)
                        .currentMonthlyDebt(100.0)
                        .interestRate(0.02)
                        .proposalLimit(2)
                        .build();

        final LambdaLoanPlanRequest request = LambdaLoanPlanRequest.builder()
                .amount(19000.0)
                .email("juan@email.com")
                .baseSalary(10000.0)
                .currentMonthlyDebt(100.0)
                .interestRate(0.02)
                .proposalLimit(2)
                .build();

        Mockito
                .when(mapper.toRequest(Mockito.any(Proposal.class)))
                        .thenReturn(request);

        Mockito
                .when(mapper.toDomain(Mockito.any(LambdaLoanPlanResponse.class)))
                        .thenReturn(proposal);

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"amount\" : 19000.0}"));
        var response = lambdaLoanPlanConsumer.postLambdaLoanPlan(proposal);

        StepVerifier.create(response)
                .expectNextMatches(objectResponse -> objectResponse.getAmount().equals(19000.0))
                .verifyComplete();
    }
}