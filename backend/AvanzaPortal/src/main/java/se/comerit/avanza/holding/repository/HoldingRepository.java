package se.comerit.avanza.holding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.instrument.model.InstrumentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Integer> {

    boolean existsByAccountId(Integer accountId);

    List<Holding> findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(Integer userId);

    @Query("""
            SELECT a.id AS accountId,
                   a.accountType AS accountType,
                   i.ticker AS ticker,
                   i.currency AS currency,
                   i.instrumentType AS instrumentType,
                   h.quantity AS quantity,
                   COUNT(h.id) AS holdingCount
            FROM Holding h
            JOIN h.account a
            LEFT JOIN h.instrument i
            WHERE a.userId = :userId
            GROUP BY a.id, a.accountType, i.id, i.ticker, i.currency,
                     i.instrumentType, h.quantity
            """)
    List<HoldingValuationGroup> findValuationGroupsByUserId(
            @Param("userId") Integer userId);

    interface HoldingValuationGroup {
        Integer getAccountId();
        String getAccountType();
        String getTicker();
        String getCurrency();
        InstrumentType getInstrumentType();
        java.math.BigDecimal getQuantity();
        Long getHoldingCount();
    }

    Optional<Holding> findByIdAndAccountUserId(Integer holdingId, Integer userId);

    @Query("""
            SELECT h FROM Holding h
            JOIN FETCH h.instrument
            JOIN FETCH h.account
            WHERE h.accountId = :accountId AND h.account.userId = :userId
            ORDER BY h.instrument.ticker ASC
            """)
    List<Holding> findAllByOwnedAccount(
            @Param("accountId") Integer accountId,
            @Param("userId") Integer userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT h FROM Holding h
            JOIN FETCH h.instrument
            JOIN FETCH h.account
            WHERE h.accountId = :accountId AND h.account.userId = :userId
            ORDER BY h.instrument.ticker ASC
            """)

    List<Holding> findAllByOwnedAccountForUpdate(
            @Param("accountId") Integer accountId,
            @Param("userId") Integer userId
    );

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
