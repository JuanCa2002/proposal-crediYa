package co.com.pragma.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProposalOpenApi {

    @Value("${routes.base-path}")
    private String basePath;

    @Bean
    public OpenAPI ProposalOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("Proposal management API of CrediYa")
                        .description("API that provides services for the management, administration and control of Proposals of CrediYa")
                        .version("v0.0.1"))
                .servers(List.of(new Server().url(basePath).description("Base path for all endpoints")));
    }
}
