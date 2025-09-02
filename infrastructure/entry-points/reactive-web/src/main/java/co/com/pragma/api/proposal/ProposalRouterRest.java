package co.com.pragma.api.proposal;

import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.proposal.config.ProposalPath;
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
public class ProposalRouterRest {

    private final BasePath basePath;
    private final ProposalPath proposalPath;
    private final ProposalHandler proposalHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/solicitud",
                    method = {RequestMethod.POST},
                    beanClass = ProposalHandler.class,
                    beanMethod = "listenSaveProposal"
            ),
            @RouterOperation(
                    path = "/solicitud",
                    method = {RequestMethod.GET},
                    beanClass = ProposalHandler.class,
                    beanMethod = "listenFilterByCriteria"
            ),
            @RouterOperation(
                    path = "/solicitud/{id}",
                    method = {RequestMethod.PATCH},
                    beanClass = ProposalHandler.class,
                    beanMethod = "listenUpdateStateProposal"
            )
    })
    public RouterFunction<ServerResponse> proposalRoutes(ProposalHandler proposalHandler) {
        return RouterFunctions
                .route()
                .path(basePath.getBasePath(), builder -> builder
                        .POST(proposalPath.getProposals(), this.proposalHandler::listenSaveProposal)
                        .GET(proposalPath.getProposals(), this.proposalHandler::listenFilterByCriteria)
                        .PATCH(proposalPath.getProposals()+"/{id}", this.proposalHandler::listenUpdateStateProposal)
                )
                .build();
    }
}
