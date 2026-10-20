package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainingControllerTest {

    private TrainingService trainingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        trainingService = mock(TrainingService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrainingController(trainingService))
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
}