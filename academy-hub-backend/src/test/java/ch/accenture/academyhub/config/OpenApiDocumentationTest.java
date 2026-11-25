package ch.accenture.academyhub.config;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true"
})
@AutoConfigureMockMvc
@Transactional
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void documentationDescribesTheCurrentApiWithoutRequiringLogin() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Academy Hub API"))
                .andExpect(jsonPath("$.paths['/api/auth/csrf'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/csrf'].get.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security[0].csrfToken").isArray())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security[0].sessionCookie")
                        .doesNotExist())
                .andExpect(jsonPath("$.paths['/api/trainings'].get.security[0].sessionCookie").isArray())
                .andExpect(jsonPath("$.paths['/api/admin/trainings'].post.security[0].sessionCookie")
                        .isArray())
                .andExpect(jsonPath("$.paths['/api/admin/trainings'].post.security[0].csrfToken")
                        .isArray())
                .andExpect(jsonPath("$.components.securitySchemes.csrfToken.name")
                        .value("X-XSRF-TOKEN"));
    }

    @Test
    void swaggerResourcesAreAvailableWithoutLogin() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
    }

    @Test
    void addingSwaggerDoesNotMakeProtectedApisPublic() throws Exception {
        mockMvc.perform(get("/api/trainings")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentedCsrfAndSessionFlowAuthenticatesAnAccount() throws Exception {
        String username = "swagger-" + UUID.randomUUID();
        String password = "local-test-password";
        userRepository.saveAndFlush(new User(
                "Swagger", "Learner", Role.LEARNER,
                username, passwordEncoder.encode(password)
        ));

        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        String body = csrfResult.getResponse().getContentAsString();
        String headerName = JsonPath.read(body, "$.headerName");
        String token = JsonPath.read(body, "$.token");
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrfCookie);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header(headerName, token)
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("LEARNER"));
    }
}
