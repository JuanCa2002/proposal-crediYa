package co.com.pragma.r2dbc.proposal;

import co.com.pragma.r2dbc.entity.ProposalEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.math.BigInteger;
import java.time.LocalDate;

public interface ProposalReactiveRepository extends ReactiveCrudRepository<ProposalEntity, BigInteger>, ReactiveQueryByExampleExecutor<ProposalEntity> {

    @Query("SELECT * FROM proposals " +
            "WHERE (:proposalTypeId IS NULL OR proposal_type_id = :proposalTypeId) " +
            "AND (:stateId IS NULL OR state_id = :stateId) " +
            "AND (:email IS NULL OR email = :email) " +
            "AND (:proposalLimit IS NULL OR proposal_limit >= :proposalLimit) " +
            "AND ( " +
            "      (:initialDate IS NOT NULL AND :endDate IS NOT NULL AND creation_date BETWEEN :initialDate AND :endDate) " +
            "   OR (:initialDate IS NOT NULL AND :endDate IS NULL AND creation_date >= :initialDate) " +
            "   OR (:initialDate IS NULL AND :endDate IS NOT NULL AND creation_date <= :endDate) " +
            "   OR (:initialDate IS NULL AND :endDate IS NULL) " +
            ") " +
            "LIMIT :limit OFFSET :offset")
    Flux<ProposalEntity> findByCriteria(@Param("proposalTypeId") Long proposalTypeId,
                                        @Param("stateId") Integer stateId,
                                        @Param("email") String email,
                                        @Param("initialDate") LocalDate initialDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("proposalLimit") Integer proposalLimit,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    Flux<ProposalEntity> findByEmail(String email);

}
