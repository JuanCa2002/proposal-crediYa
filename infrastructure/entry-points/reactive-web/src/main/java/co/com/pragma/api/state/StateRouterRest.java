package co.com.pragma.api.state;

import co.com.pragma.api.config.BasePath;
import co.com.pragma.api.state.config.StatePath;
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
public class StateRouterRest {

    private final BasePath basePath;
    private final StatePath statePath;
    private final StateHandler stateHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/estado",
                    method = {RequestMethod.POST},
                    beanClass = StateHandler.class,
                    beanMethod = "listenSaveState"
            )
    })
    public RouterFunction<ServerResponse> statesRoutes(StateHandler proposalHandler) {
        return RouterFunctions
                .route()
                .path(basePath.getBasePath(), builder -> builder
                        .POST(statePath.getStates(), this.stateHandler::listenSaveState)
                )
                .build();
    }
}
