package se.comerit.avanza.alert.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.alert.model.Alert;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {

    Page<Alert> findByUserIdAndDismissedOrderByCreatedAtDesc(Integer userId, boolean dismissed, Pageable pageable);

    Optional<Alert> findByIdAndUserId(
            Integer id,
            Integer userId
    );
}
