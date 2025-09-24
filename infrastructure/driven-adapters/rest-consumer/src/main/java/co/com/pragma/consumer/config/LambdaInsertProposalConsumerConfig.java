package co.com.pragma.consumer.config;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class LambdaInsertProposalConsumerConfig {

    private final String url;
    private final int timeout;
    private final String accessKey;
    private final String secretKey;
    private final String region;

    public LambdaInsertProposalConsumerConfig(@Value("${adapter.restconsumer.insertproposal.url}") String url,
                                        @Value("${adapter.restconsumer.timeout}") int timeout,
                                        @Value("${aws.accessKeyId}") String accessKey,
                                        @Value("${aws.secretAccessKey}") String secretKey,
                                        @Value("${aws.region}") String region) {
        this.url = url;
        this.timeout = timeout;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    @Bean(name = "lambdaInsertProposalWebClient")
    public WebClient lambdaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(getClientHttpConnector())
                .filter(awsSignerFilter())
                .build();
    }

    private ExchangeFilterFunction awsSignerFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            try {
                URI uri = clientRequest.url();

                AWS4Signer signer = new AWS4Signer();
                signer.setServiceName("execute-api");
                signer.setRegionName(region);

                AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

                String bodyContent = clientRequest.attribute("signedBody")
                        .map(Object::toString)
                        .orElse("");

                Request<?> awsRequest = new DefaultRequest<>("execute-api");
                awsRequest.setHttpMethod(
                        com.amazonaws.http.HttpMethodName.valueOf(clientRequest.method().name())
                );
                awsRequest.setEndpoint(new URI(uri.getScheme() + "://" + uri.getHost()));
                awsRequest.setResourcePath(uri.getRawPath());

                if (!bodyContent.isEmpty()) {
                    awsRequest.setContent(new ByteArrayInputStream(bodyContent.getBytes(StandardCharsets.UTF_8)));
                }

                clientRequest.headers().forEach((key, values) ->
                        awsRequest.addHeader(key, String.join(",", values))
                );

                signer.sign(awsRequest, credentials);

                ClientRequest filteredRequest = ClientRequest.from(clientRequest)
                        .headers(h -> {
                            h.clear();
                            awsRequest.getHeaders().forEach(h::add);
                        })
                        .body(BodyInserters.fromValue(bodyContent))
                        .build();

                return Mono.just(filteredRequest);

            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    private ClientHttpConnector getClientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }
}
