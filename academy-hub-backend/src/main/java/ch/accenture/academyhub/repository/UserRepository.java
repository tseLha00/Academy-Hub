package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
