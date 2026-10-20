package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
