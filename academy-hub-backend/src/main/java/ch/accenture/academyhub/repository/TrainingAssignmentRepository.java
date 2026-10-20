package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.TrainingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingAssignmentRepository
        extends JpaRepository<TrainingAssignment, Long> {

    @Query("""
            SELECT assignment
            FROM TrainingAssignment assignment
            JOIN FETCH assignment.training training
            WHERE assignment.learner.id = :learnerId
            ORDER BY training.name ASC, assignment.id ASC
            """)
    List<TrainingAssignment> findAllWithTrainingByLearnerId(
            @Param("learnerId") Long learnerId
    );

    List<TrainingAssignment> findAllByTrainingId(Long trainingId);
}