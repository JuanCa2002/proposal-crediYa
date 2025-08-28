package co.com.pragma.r2dbc.proposaltype;

import co.com.pragma.r2dbc.entity.ProposalTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProposalTypeReactiveRepository extends ReactiveCrudRepository<ProposalTypeEntity, Long>, ReactiveQueryByExampleExecutor<ProposalTypeEntity> {

}
