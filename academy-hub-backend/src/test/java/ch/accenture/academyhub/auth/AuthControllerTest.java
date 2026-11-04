package ch.accenture.academyhub.auth;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import org.springframework.mock.web.MockHttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private SecurityContextRepository securityContextRepository;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        securityContextRepository = mock(SecurityContextRepository.class);
        controller = new AuthController(authenticationManager, securityContextRepository);
    }

    @Test
    void loginAuthenticatesAndCreatesSession() {
        AcademyUserPrincipal principal = new AcademyUserPrincipal(
                7L, "admin.one", "encoded-password", "Admin", "One", Role.ADMIN
        );
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()
        );
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticatedUserResponse result = controller.login(
                new LoginRequest("ADMIN.ONE", "correct-password"), request, response
        );

        assertEquals(7L, result.id());
        assertEquals("admin.one", result.username());
        assertEquals(Role.ADMIN, result.role());
        assertNotNull(request.getSession(false));
        verify(securityContextRepository).saveContext(
                any(SecurityContext.class), eq(request), eq(response)
        );
    }

    @Test
    void loginReturnsGenericUnauthorizedErrorForInvalidCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(
                        new LoginRequest("admin.one", "wrong-password"),
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse()
                )
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Invalid username or password", exception.getReason());
        verifyNoInteractions(securityContextRepository);
    }

    @Test
    void logoutInvalidatesExistingSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        controller.logout(request);

        assertTrue(session.isInvalid());
    }
}
