package co.com.pragma.usecase.state;

import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.usecase.state.exception.StateByNameAlreadyExistsBusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StateUseCase {

    private final StateRepository stateRepository;

    public Mono<State> saveState(State state) {
        return stateRepository.findByName(state.getName())
                .flatMap(existing -> Mono.<State>error(new StateByNameAlreadyExistsBusinessException(state.getName())))
                .switchIfEmpty(stateRepository.save(state));
    }
}
