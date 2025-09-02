package co.com.pragma.r2dbc.proposal;

import co.com.pragma.model.proposal.Proposal;
import co.com.pragma.model.proposal.gateways.ProposalRepository;
import co.com.pragma.r2dbc.entity.ProposalEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.LocalDate;

@Repository
public class ProposalReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Proposal,
        ProposalEntity,
        BigInteger,
        ProposalReactiveRepository
> implements ProposalRepository {

    private final TransactionalOperator txOperator;
    public ProposalReactiveRepositoryAdapter(ProposalReactiveRepository repository,
                                             ObjectMapper mapper, TransactionalOperator txOperator) {
        super(repository, mapper, d -> mapper.map(d, Proposal.class));
        this.txOperator = txOperator;
    }

    @Override
    public Mono<Proposal> save(Proposal proposal) {
        return super.save(proposal)
                .as(txOperator::transactional);
    }

    @Override
    public Flux<Proposal> findByCriteria(Long proposalTypeId, Integer stateId, String email,
             LocalDate initialDate, LocalDate endDate, Integer proposalLimit, int limit, int offset) {
        return repository.findByCriteria(proposalTypeId, stateId, email,
                        initialDate, endDate, proposalLimit,limit, offset)
                .map(entry -> mapper.map(entry, Proposal.class))
                .as(txOperator::transactional);
    }

}
