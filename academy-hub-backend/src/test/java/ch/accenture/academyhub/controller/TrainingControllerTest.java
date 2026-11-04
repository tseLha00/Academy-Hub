package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.dto.UpdateTrainingProgressRequest;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import ch.accenture.academyhub.service.TrainingManagementService;
import ch.accenture.academyhub.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingControllerTest {

    private TrainingService trainingService;
    private TrainingManagementService managementService;
    private TrainingController controller;

    @BeforeEach
    void setUp() {
        trainingService = mock(TrainingService.class);
        managementService = mock(TrainingManagementService.class);
        controller = new TrainingController(trainingService, managementService);
    }

    private AcademyUserPrincipal learner() {
        return new AcademyUserPrincipal(
                4L, "learner", "hash", "Test", "Learner", Role.LEARNER
        );
    }

    private AcademyUserPrincipal admin() {
        return new AcademyUserPrincipal(
                6L, "admin", "hash", "Test", "Admin", Role.ADMIN
        );
    }

    @Test
    void learnerRetrievesTrainingsUsingAuthenticatedIdentity() {
        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");
        List<TrainingResponse> expected = List.of(new TrainingResponse(
                3L, 2L, "Java Fundamentals", true, completedAt, 100
        ));
        when(trainingService.getTrainingsForLearner(4L)).thenReturn(expected);

        assertEquals(expected, controller.getMyTrainings(learner()));
        verify(trainingService).getTrainingsForLearner(4L);
    }

    @Test
    void learnerCanCompleteTrainingUsingAuthenticatedIdentity() {
        controller.completeTraining(learner(), 2L);
        verify(trainingService).completeTraining(4L, 2L);
    }

    @Test
    void learnerUpdatesProgressUsingAuthenticatedIdentity() {
        controller.updateTrainingProgress(
                learner(), 2L, new UpdateTrainingProgressRequest(65)
        );
        verify(trainingService).updateTrainingProgress(4L, 2L, 65);
    }

    @Test
    void adminCanRetrieveTrainingForSelectedLearner() {
        when(trainingService.getTrainingsForLearner(4L)).thenReturn(List.of());

        assertTrue(controller.getLearnerTrainings(4L).isEmpty());
        verify(trainingService).getTrainingsForLearner(4L);
    }

    @Test
    void adminCreatesTrainingUsingAuthenticatedIdentity() {
        CreateTrainingRequest request = new CreateTrainingRequest("Java Practice", Set.of(4L));
        TrainingDetailsResponse expected = new TrainingDetailsResponse(
                40L, "Java Practice", 6L, List.of(4L)
        );
        when(managementService.createTraining(6L, request)).thenReturn(expected);

        var response = controller.createTraining(admin(), request);
        assertEquals(expected, response.getBody());
        assertEquals(201, response.getStatusCode().value());
        verify(managementService).createTraining(6L, request);
    }

    @Test
    void adminCanUpdateTraining() {
        controller.updateTraining(admin(), 40L, new UpdateTrainingRequest("Updated Name"));
        verify(managementService).updateTraining(6L, 40L, new UpdateTrainingRequest("Updated Name"));
    }

    @Test
    void adminCanUpdateAssignments() {
        UpdateTrainingAssignmentsRequest request = new UpdateTrainingAssignmentsRequest(Set.of(4L, 5L));
        controller.updateAssignments(admin(), 40L, request);
        verify(managementService).updateAssignments(6L, 40L, request);
    }

    @Test
    void adminCanDeleteTraining() {
        controller.deleteTraining(admin(), 40L);
        verify(managementService).deleteTraining(6L, 40L);
    }
}
