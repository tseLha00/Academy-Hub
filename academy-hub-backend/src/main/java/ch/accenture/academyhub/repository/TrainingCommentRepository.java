package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.TrainingComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingCommentRepository
        extends JpaRepository<TrainingComment, Long> {

    List<TrainingComment> findAllByAssignmentIdOrderByCreatedAtAscIdAsc(
            Long assignmentId
    );
}