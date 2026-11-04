package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrainingService {

    private final TrainingAssignmentRepository assignmentRepository;

    public TrainingService(
            TrainingAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
    }

    /** Completes only the assignment belonging to the authenticated learner. */
    @Transactional
    public void completeTraining(Long learnerId, Long trainingId) {
        var assignment = assignmentRepository
                .findByTrainingIdAndLearnerId(trainingId, learnerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Training assignment not found"
                ));

        assignment.markCompleted();
        assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainingsForLearner(Long learnerId) {
        return assignmentRepository
                .findAllWithTrainingByLearnerId(learnerId)
                .stream()
                .map(assignment -> new TrainingResponse(
                        assignment.getId(),
                        assignment.getTraining().getId(),
                        assignment.getTraining().getName(),
                        assignment.isCompleted(),
                        assignment.getCompletedAt()
                ))
                .toList();
    }
}