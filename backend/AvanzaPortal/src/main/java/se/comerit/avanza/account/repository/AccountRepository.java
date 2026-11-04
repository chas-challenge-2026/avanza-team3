package se.comerit.avanza.account.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.account.model.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    List<Account> findByUserIdOrderByAccountTypeAscAccountNameAsc(Integer userId);

    Page<Account> findByUserIdOrderByAccountTypeAscAccountNameAsc(Integer userId, Pageable pageable);

    Optional<Account> findByIdAndUserId(Integer id, Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.userId = :userId")
    Optional<Account> findOwnedAccountForUpdate(@Param("id") Integer id, @Param("userId") Integer userId);
}
