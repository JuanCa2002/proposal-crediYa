package co.com.pragma.api.config;


import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @Bean
    public WebFilter tokenWebFilter() {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null) {
                return chain.filter(exchange)
                        .contextWrite(ctx -> ctx.put("Authorization", token));
            } else {
                return chain.filter(exchange);
            }
        };
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        List<String> excludedPaths = List.of("/autenticacion");

        return builder
                .filter((request, next) -> {
                    boolean skipAuth = excludedPaths.stream()
                            .anyMatch(path -> request.url().getPath().startsWith(path));

                    if (skipAuth) return next.exchange(request);

                    return Mono.deferContextual(ctx -> {
                        String token = ctx.get("Authorization");
                        ClientRequest newRequest = ClientRequest.from(request)
                                .headers(headers -> {
                                    headers.set("Authorization", token);
                                })
                                .build();
                        return next.exchange(newRequest);
                    });
                })
                .build();
    }
}