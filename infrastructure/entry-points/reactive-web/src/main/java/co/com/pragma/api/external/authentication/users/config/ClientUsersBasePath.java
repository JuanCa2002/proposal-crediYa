package co.com.pragma.api.external.authentication.users.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "clients")
public class ClientUsersBasePath {
    private String users;
}
