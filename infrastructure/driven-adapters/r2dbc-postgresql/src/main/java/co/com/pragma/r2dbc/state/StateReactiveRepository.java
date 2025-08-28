package co.com.pragma.r2dbc.state;

import co.com.pragma.r2dbc.entity.StateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


// TODO: This file is just an example, you should delete or modify it
public interface StateReactiveRepository extends ReactiveCrudRepository<StateEntity, Integer>, ReactiveQueryByExampleExecutor<StateEntity> {

    Mono<StateEntity> findByName(String name);
}
