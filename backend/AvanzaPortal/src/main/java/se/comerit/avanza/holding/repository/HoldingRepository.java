package se.comerit.avanza.holding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.instrument.model.InstrumentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Integer> {

    List<Holding> findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(Integer userId);

    Optional<Holding> findByIdAndAccountUserId(Integer holdingId, Integer userId);

    Page<Holding> findByAccountUserId(Integer userId, Pageable pageable);

    @Query("""
            SELECT h
            FROM Holding h
            WHERE h.account.userId = :userId
              AND (:accountId IS NULL OR h.accountId = :accountId)
              AND (:instrumentType IS NULL OR h.instrument.instrumentType = :instrumentType)
            """)
    Page<Holding> findFilteredByUserId(
            @Param("userId") Integer userId,
            @Param("accountId") Integer accountId,
            @Param("instrumentType") InstrumentType instrumentType,
            Pageable pageable
    );

    @Query("""
            SELECT h
            FROM Holding h
            WHERE h.account.userId = :userId
              AND (:accountId IS NULL OR h.accountId = :accountId)
              AND (:instrumentType IS NULL OR h.instrument.instrumentType = :instrumentType)
            ORDER BY h.account.accountType ASC, h.instrument.ticker ASC
            """)
    List<Holding> findAllFilteredByUserId(
            @Param("userId") Integer userId,
            @Param("accountId") Integer accountId,
            @Param("instrumentType") InstrumentType instrumentType
    );
}
