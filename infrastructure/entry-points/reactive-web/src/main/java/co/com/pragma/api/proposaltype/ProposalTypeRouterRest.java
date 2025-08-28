package co.com.pragma.api.proposaltype;

import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.proposaltype.config.ProposalTypePath;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class ProposalTypeRouterRest {

    private final BasePath basePath;
    private final ProposalTypePath proposalTypePath;
    private final ProposalTypeHandler proposalTypeHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/tipo-prestamo",
                    method = {RequestMethod.POST},
                    beanClass = ProposalTypeHandler.class,
                    beanMethod = "listenSaveProposalType"
            )
    })
    public RouterFunction<ServerResponse> proposalTypeRoutes(ProposalTypeHandler proposalTypeHandler) {
        return RouterFunctions
                .route()
                .path(basePath.getBasePath(), builder -> builder
                        .POST(proposalTypePath.getProposalTypes(), this.proposalTypeHandler::listenSaveProposalType)
                )
                .build();
    }
}
