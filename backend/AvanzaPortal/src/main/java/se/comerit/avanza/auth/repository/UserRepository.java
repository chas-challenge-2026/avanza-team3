package se.comerit.avanza.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.auth.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
