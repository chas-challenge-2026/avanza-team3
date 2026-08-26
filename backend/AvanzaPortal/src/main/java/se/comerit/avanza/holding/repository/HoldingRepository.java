package se.comerit.avanza.holding.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.holding.model.Holding;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Integer> {

    List<Holding> findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(Integer userId);
}
