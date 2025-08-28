package co.com.pragma.r2dbc.state;

import co.com.pragma.model.state.State;
import co.com.pragma.model.state.gateways.StateRepository;
import co.com.pragma.r2dbc.entity.StateEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class StateReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        State,
        StateEntity,
        Integer,
        StateReactiveRepository
> implements StateRepository {

    private final TransactionalOperator txOperator;
    public StateReactiveRepositoryAdapter(StateReactiveRepository repository,
                                          ObjectMapper mapper, TransactionalOperator txOperator) {
        super(repository, mapper, d -> mapper.map(d, State.class));
        this.txOperator = txOperator;
    }

    @Override
    public Mono<State> save(State state) {
        return super.save(state)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<State> findByName(String name) {
        return repository.findByName(name)
                .map(entity -> mapper.map(entity, State.class))
                .as(txOperator::transactional);
    }

    @Override
    public Mono<State> findById(Integer id) {
        return repository.findById(id)
                .map(entity -> mapper.map(entity, State.class))
                .as(txOperator::transactional);
    }

}
