package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingAssignmentRepository assignmentRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void returnsEmptyListWhenLearnerHasNoAssignments() {
        when(assignmentRepository.findAllWithTrainingByLearnerId(4L))
                .thenReturn(List.of());

        List<TrainingResponse> result =
                trainingService.getTrainingsForLearner(4L);

        assertTrue(result.isEmpty());
        verify(assignmentRepository).findAllWithTrainingByLearnerId(4L);
    }

    @Test
    void returnsIncompleteTrainingDetails() {
        Training training = mock(Training.class);
        TrainingAssignment assignment = mock(TrainingAssignment.class);

        when(training.getId()).thenReturn(2L);
        when(training.getName()).thenReturn("Java Fundamentals");

        when(assignment.getId()).thenReturn(3L);
        when(assignment.getTraining()).thenReturn(training);
        when(assignment.isCompleted()).thenReturn(false);
        when(assignment.getCompletedAt()).thenReturn(null);

        when(assignmentRepository.findAllWithTrainingByLearnerId(4L))
                .thenReturn(List.of(assignment));

        List<TrainingResponse> result =
                trainingService.getTrainingsForLearner(4L);

        assertEquals(
                List.of(new TrainingResponse(
                        3L,
                        2L,
                        "Java Fundamentals",
                        false,
                        null
                )),
                result
        );
    }

    @Test
    void returnsCompletedTrainingWithTimestamp() {
        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");

        Training training = mock(Training.class);
        TrainingAssignment assignment = mock(TrainingAssignment.class);

        when(training.getId()).thenReturn(2L);
        when(training.getName()).thenReturn("Java Fundamentals");

        when(assignment.getId()).thenReturn(3L);
        when(assignment.getTraining()).thenReturn(training);
        when(assignment.isCompleted()).thenReturn(true);
        when(assignment.getCompletedAt()).thenReturn(completedAt);

        when(assignmentRepository.findAllWithTrainingByLearnerId(4L))
                .thenReturn(List.of(assignment));

        List<TrainingResponse> result =
                trainingService.getTrainingsForLearner(4L);

        assertEquals(
                List.of(new TrainingResponse(
                        3L,
                        2L,
                        "Java Fundamentals",
                        true,
                        completedAt
                )),
                result
        );
    }
}