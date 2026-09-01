package se.comerit.avanza.holding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.holding.model.Holding;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Integer> {

    List<Holding> findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(Integer userId);

    Optional<Holding> findByIdAndAccountUserId(Integer holdingId, Integer userId);

    Page<Holding> findByAccountUserId(Integer userId, Pageable pageable);
}
