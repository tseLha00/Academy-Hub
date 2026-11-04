
package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.security.AcademyUserPrincipal;

import ch.accenture.academyhub.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.Instant;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingProgressSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    private static final String PROGRESS_URL =
            "/api/trainings/2/progress";

    private static final String ADMIN_LEARNER_TRAININGS_URL =
            "/api/admin/learners/4/trainings";

    @Test
    void learnerCanUpdateTrainingProgress() throws Exception {
        AcademyUserPrincipal learner = new AcademyUserPrincipal(
                4L, "learner", "hash", "Test", "Learner", Role.LEARNER
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                learner, null, learner.getAuthorities()
        );

        mockMvc.perform(put(PROGRESS_URL)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"progressPercentage\":65}"))
                .andExpect(status().isNoContent());

        verify(trainingService).updateTrainingProgress(4L, 2L, 65);
    }

    @Test
    void adminCannotUpdateTrainingProgress() throws Exception {
        AcademyUserPrincipal admin = new AcademyUserPrincipal(
                6L, "admin", "hash", "Test", "Admin", Role.ADMIN
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities()
        );

        mockMvc.perform(put(PROGRESS_URL)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"progressPercentage\":65}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotUpdateTrainingProgress() throws Exception {
        mockMvc.perform(put(PROGRESS_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"progressPercentage\":65}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanRetrieveLearnerTrainingProgress() throws Exception {
        AcademyUserPrincipal admin = new AcademyUserPrincipal(
                6L, "admin", "hash", "Test Admin", Role.ADMIN
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities()
        );

        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");

        when(trainingService.getTrainingsForLearner(4L)).thenReturn(List.of(
                new TrainingResponse(
                        3L,
                        2L,
                        "Java Fundamentals",
                        false,
                        null,
                        65
                ),
                new TrainingResponse(
                        5L,
                        4L,
                        "Spring Boot",
                        true,
                        completedAt,
                        100
                )
        ));

        mockMvc.perform(get(ADMIN_LEARNER_TRAININGS_URL)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java Fundamentals"))
                .andExpect(jsonPath("$[0].progressPercentage").value(65))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"))
                .andExpect(jsonPath("$[1].progressPercentage").value(100))
                .andExpect(jsonPath("$[1].completed").value(true));

        verify(trainingService).getTrainingsForLearner(4L);
    }

    @Test
    void learnerCannotRetrieveAnotherLearnersTrainingProgress() throws Exception {
        AcademyUserPrincipal learner = new AcademyUserPrincipal(
                4L, "learner", "hash", "Test Learner", Role.LEARNER
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                learner, null, learner.getAuthorities()
        );

        mockMvc.perform(get(ADMIN_LEARNER_TRAININGS_URL)
                        .with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotRetrieveLearnerTrainingProgress() throws Exception {
        mockMvc.perform(get(ADMIN_LEARNER_TRAININGS_URL))
                .andExpect(status().isUnauthorized());
    }
}
