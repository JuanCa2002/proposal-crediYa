package co.com.pragma.r2dbc.proposaltype;

import co.com.pragma.model.proposaltype.ProposalType;
import co.com.pragma.model.proposaltype.gateways.ProposalTypeRepository;
import co.com.pragma.r2dbc.entity.ProposalTypeEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class ProposalTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        ProposalType,
        ProposalTypeEntity,
        Long,
        ProposalTypeReactiveRepository
> implements ProposalTypeRepository {

    private final TransactionalOperator txOperator;
    public ProposalTypeReactiveRepositoryAdapter(ProposalTypeReactiveRepository repository,
                                                 ObjectMapper mapper, TransactionalOperator txOperator) {
        super(repository, mapper, d -> mapper.map(d, ProposalType.class));
        this.txOperator = txOperator;
    }

    @Override
    public Mono<ProposalType> save(ProposalType proposalType) {
        return super.save(proposalType)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<ProposalType> findById(Long id) {
        return repository.findById(id)
                .map(entity -> mapper.map(entity, ProposalType.class))
                .as(txOperator::transactional);
    }

}
