
package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import ch.accenture.academyhub.service.TrainingCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingCommentService commentService;

    private static final String COMMENTS_URL =
            "/api/training-assignments/2/comments";

    @Test
    void learnerCanReadOwnAssignmentComments() throws Exception {
        AcademyUserPrincipal learner = new AcademyUserPrincipal(
                4L, "learner", "hash", "Test", "Learner", Role.LEARNER
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                learner, null, learner.getAuthorities()
        );

        mockMvc.perform(get(COMMENTS_URL)
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        verify(commentService).getComments(2L, 4L, Role.LEARNER);
    }

    @Test
    void adminCanReadAssignmentComments() throws Exception {
        AcademyUserPrincipal admin = new AcademyUserPrincipal(
                6L, "admin", "hash", "Test", "Admin", Role.ADMIN
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities()
        );

        mockMvc.perform(get(COMMENTS_URL)
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        verify(commentService).getComments(2L, 6L, Role.ADMIN);
    }

    @Test
    void learnerCanAddComment() throws Exception {
        AcademyUserPrincipal learner = new AcademyUserPrincipal(
                4L, "learner", "hash", "Test", "Learner", Role.LEARNER
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                learner, null, learner.getAuthorities()
        );

        mockMvc.perform(post(COMMENTS_URL)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have completed this section."
                                }
                                """))
                .andExpect(status().isCreated());

        verify(commentService).addComment(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(4L),
                org.mockito.ArgumentMatchers.eq(Role.LEARNER),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void adminCanAddComment() throws Exception {
        AcademyUserPrincipal admin = new AcademyUserPrincipal(
                6L, "admin", "hash", "Test", "Admin", Role.ADMIN
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities()
        );

        mockMvc.perform(post(COMMENTS_URL)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Please review this assignment."
                                }
                                """))
                .andExpect(status().isCreated());

        verify(commentService).addComment(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(6L),
                org.mockito.ArgumentMatchers.eq(Role.ADMIN),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void unauthenticatedUserCannotReadComments() throws Exception {
        mockMvc.perform(get(COMMENTS_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(commentService);
    }

    @Test
    void unauthenticatedUserCannotAddComment() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Hello"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(commentService);
    }

    @Test
    void emptyCommentIsRejected() throws Exception {
        AcademyUserPrincipal learner = new AcademyUserPrincipal(
                4L, "learner", "hash", "Test", "Learner", Role.LEARNER
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                learner, null, learner.getAuthorities()
        );

        mockMvc.perform(post(COMMENTS_URL)
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }
}
