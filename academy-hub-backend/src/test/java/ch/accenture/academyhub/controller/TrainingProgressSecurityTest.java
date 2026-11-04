
package ch.accenture.academyhub.controller;

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

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingProgressSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    private static final String PROGRESS_URL =
            "/api/trainings/2/progress";

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
}
