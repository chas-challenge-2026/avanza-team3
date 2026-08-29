package se.comerit.avanza.targetallocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.targetallocation.model.TargetAllocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetAllocationRepository extends JpaRepository<TargetAllocation, Integer> {

    List<TargetAllocation> findByUserIdOrderByAccountTypeAsc(Integer userId);

    Optional<TargetAllocation> findByIdAndUserId(Integer id, Integer userId);

}
