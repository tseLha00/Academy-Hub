package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.dto.UpdateTrainingProgressRequest;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.TrainingStatus;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
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

    private static final LocalDate START_DATE = LocalDate.of(2025, 3, 1);
    private static final LocalDate ESTIMATED_COMPLETION = LocalDate.of(2025, 4, 30);
    private static final UpdateTrainingProgressRequest REQUEST = new UpdateTrainingProgressRequest(
            TrainingStatus.IN_PROGRESS, START_DATE, ESTIMATED_COMPLETION
    );

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
    void updatesStatusAndDatesForAssignedLearner() {
        TrainingAssignment assignment = mock(TrainingAssignment.class);
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.of(assignment));

        trainingService.updateTrainingProgress(4L, 2L, REQUEST);

        verify(assignment).updateDates(START_DATE, ESTIMATED_COMPLETION);
        verify(assignment).updateStatus(TrainingStatus.IN_PROGRESS);
        verify(assignmentRepository).save(assignment);
    }

    @Test
    void doesNotUpdateProgressWhenTrainingIsNotAssignedToLearner() {
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.updateTrainingProgress(4L, 2L, REQUEST)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidDatesWithoutChangingStatus() {
        TrainingAssignment assignment = mock(TrainingAssignment.class);
        when(assignmentRepository.findByTrainingIdAndLearnerId(2L, 4L))
                .thenReturn(Optional.of(assignment));
        doThrow(new IllegalArgumentException("Estimated completion date cannot be before the start date"))
                .when(assignment).updateDates(START_DATE, ESTIMATED_COMPLETION);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.updateTrainingProgress(4L, 2L, REQUEST)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(assignment, never()).updateStatus(any());
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
        when(assignment.getStatus()).thenReturn(TrainingStatus.TODO);

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
                        TrainingStatus.TODO,
                        null,
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
        when(assignment.getStatus()).thenReturn(TrainingStatus.COMPLETED);

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
                        TrainingStatus.COMPLETED,
                        null,
                        null
                )),
                result
        );
    }
}