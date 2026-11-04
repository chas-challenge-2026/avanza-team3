package se.comerit.avanza.accountallocation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;

public interface AccountAllocationTargetRepository
        extends JpaRepository<AccountAllocationTarget, Integer> {
    List<AccountAllocationTarget> findByAccountIdOrderByAssetCategoryAsc(Integer accountId);
}
