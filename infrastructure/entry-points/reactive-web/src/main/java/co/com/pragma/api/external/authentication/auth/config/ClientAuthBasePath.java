package co.com.pragma.api.external.authentication.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "clients")
public class ClientAuthBasePath {
    private String auth;
}
