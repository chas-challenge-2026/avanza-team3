package se.comerit.avanza.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.avanza.account.model.Account;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    List<Account> findByUserIdOrderByAccountTypeAscAccountNameAsc(Integer userId);

}
