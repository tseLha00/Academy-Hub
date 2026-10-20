package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.TrainingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingAssignmentRepository
        extends JpaRepository<TrainingAssignment, Long> {
}