package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.service.TrainingService;
import ch.accenture.academyhub.service.TrainingManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

class TrainingControllerTest {

    private TrainingService trainingService;
    private TrainingManagementService managementService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        trainingService = mock(TrainingService.class);

        managementService = mock(TrainingManagementService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new TrainingController(trainingService, managementService)
                )
                .build();
    }

    @Test
    void returnsAssignedTrainingsAsJson() throws Exception {
        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");

        when(trainingService.getTrainingsForLearner(4L))
                .thenReturn(List.of(new TrainingResponse(
                        3L,
                        2L,
                        "Java Fundamentals",
                        true,
                        completedAt
                )));

        mockMvc.perform(get("/api/dev/learners/4/trainings"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignmentId").value(3))
                .andExpect(jsonPath("$[0].trainingId").value(2))
                .andExpect(jsonPath("$[0].name")
                        .value("Java Fundamentals"))
                .andExpect(jsonPath("$[0].completed").value(true))
                .andExpect(jsonPath("$[0].completedAt")
                        .value("2026-10-20T10:00:00Z"));

        verify(trainingService).getTrainingsForLearner(4L);
    }

    @Test
    void returnsEmptyJsonArrayWhenNoAssignmentsExist() throws Exception {
        when(trainingService.getTrainingsForLearner(5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dev/learners/5/trainings"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(trainingService).getTrainingsForLearner(5L);
    }

    @Test
    void rejectsNonNumericLearnerId() throws Exception {
        mockMvc.perform(get("/api/dev/learners/abc/trainings"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    void createsTrainingAndReturnsCreatedResponse() throws Exception {
        CreateTrainingRequest request =
                new CreateTrainingRequest("Java Practice", Set.of());

        when(managementService.createTraining(4L, request))
                .thenReturn(new TrainingDetailsResponse(
                        40L, "Java Practice", 4L, List.of(4L)
                ));

        mockMvc.perform(post("/api/dev/trainings")
                        .header("X-Dev-Actor-Id", "4")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Java Practice",
                                  "learnerIds": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainingId").value(40))
                .andExpect(jsonPath("$.name").value("Java Practice"))
                .andExpect(jsonPath("$.createdById").value(4))
                .andExpect(jsonPath("$.learnerIds[0]").value(4));

        verify(managementService).createTraining(4L, request);
    }

    @Test
    void rejectsBlankNameBeforeCallingManagementService() throws Exception {
        mockMvc.perform(post("/api/dev/trainings")
                        .header("X-Dev-Actor-Id", "4")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "   ",
                                  "learnerIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(managementService);
    }

    @Test
    void requiresDevelopmentActorHeader() throws Exception {
        mockMvc.perform(post("/api/dev/trainings")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Java Practice",
                                  "learnerIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(managementService);
    }

    @Test
    void returnsForbiddenWhenServiceRejectsCreation() throws Exception {
        CreateTrainingRequest request =
                new CreateTrainingRequest("Forbidden", Set.of(5L));

        when(managementService.createTraining(4L, request))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Learners cannot select training assignees"
                ));

        mockMvc.perform(post("/api/dev/trainings")
                        .header("X-Dev-Actor-Id", "4")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Forbidden",
                                  "learnerIds": [5]
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(managementService).createTraining(4L, request);
    }

    @Test
    void updatesTrainingAndReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/dev/trainings/40")
                        .header("X-Dev-Actor-Id", "4")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Updated Name"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(managementService).updateTraining(
                4L, 40L, new UpdateTrainingRequest("Updated Name")
        );
    }

    @Test
    void updatesAssignmentsAndReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/dev/trainings/40/assignments")
                        .header("X-Dev-Actor-Id", "6")
                        .contentType("application/json")
                        .content("""
                                {
                                  "learnerIds": [4, 5]
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(managementService).updateAssignments(
                6L,
                40L,
                new UpdateTrainingAssignmentsRequest(Set.of(4L, 5L))
        );
    }

    @Test
    void deletesTrainingAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/dev/trainings/40")
                        .header("X-Dev-Actor-Id", "6"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(managementService).deleteTraining(6L, 40L);
    }
}