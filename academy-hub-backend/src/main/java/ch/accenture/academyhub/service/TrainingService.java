package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingService {

    private final TrainingAssignmentRepository assignmentRepository;

    public TrainingService(
            TrainingAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
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