package co.com.pragma.r2dbc.proposal;

import co.com.pragma.r2dbc.entity.ProposalEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.math.BigInteger;

// TODO: This file is just an example, you should delete or modify it
public interface ProposalReactiveRepository extends ReactiveCrudRepository<ProposalEntity, BigInteger>, ReactiveQueryByExampleExecutor<ProposalEntity> {

}
