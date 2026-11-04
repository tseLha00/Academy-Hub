package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingAssignmentRepository assignmentRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void completesTrainingAssignedToLearner() {
        TrainingAssignment assignment = mock(TrainingAssignment.class);
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.of(assignment));

        trainingService.completeTraining(4L, 2L);

        verify(assignment).markCompleted();
        verify(assignmentRepository).save(assignment);
    }

    @Test
    void doesNotCompleteTrainingNotAssignedToLearner() {
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.completeTraining(4L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void updatesProgressForAssignedLearner() {
        TrainingAssignment assignment = mock(TrainingAssignment.class);
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.of(assignment));

        trainingService.updateTrainingProgress(4L, 2L, 65);

        verify(assignment).updateProgressPercentage(65);
        verify(assignmentRepository).save(assignment);
    }

    @Test
    void doesNotUpdateProgressWhenTrainingIsNotAssignedToLearner() {
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.updateTrainingProgress(4L, 2L, 65)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void rejectsReducingProgressAfterCompletion() {
        TrainingAssignment assignment = mock(TrainingAssignment.class);
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.of(assignment));
        doThrow(new IllegalStateException("Completed training progress cannot be reduced"))
                .when(assignment).updateProgressPercentage(65);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.updateTrainingProgress(4L, 2L, 65)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

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
        when(assignment.getProgressPercentage()).thenReturn(0);

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
                        null,
                        0
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
        when(assignment.getProgressPercentage()).thenReturn(100);

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
                        completedAt,
                        100
                )),
                result
        );
    }
}