package se.comerit.avanza.targetallocation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.targetallocation.model.TargetAllocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetAllocationRepository extends JpaRepository<TargetAllocation, Integer> {

    List<TargetAllocation> findByUserIdOrderByAccountTypeAsc(Integer userId);

    Page<TargetAllocation> findByUserIdOrderByAccountTypeAsc(Integer userId, Pageable pageable);

    Optional<TargetAllocation> findByIdAndUserId(Integer id, Integer userId);

}
