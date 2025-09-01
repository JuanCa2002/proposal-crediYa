package co.com.pragma.api.helper;

import co.com.pragma.api.external.authentication.auth.ExternalAuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final ExternalAuthHandler authHandler;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        return authHandler.validateToken(token)
                .map(validateResponse -> {
                    List<GrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority(validateResponse.getRole()));

                    return new UsernamePasswordAuthenticationToken(
                            validateResponse.getUserName(), token, authorities
                    );
                });
    }

    public ServerAuthenticationConverter authenticationConverter() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
            }
            return Mono.empty();
        };
    }
}
