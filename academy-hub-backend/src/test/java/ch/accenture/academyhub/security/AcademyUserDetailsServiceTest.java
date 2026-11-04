package ch.accenture.academyhub.security;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcademyUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AcademyUserDetailsService userDetailsService =
            new AcademyUserDetailsService(userRepository);

    @Test
    void loadsUserAndMapsRoleToAuthority() {
        User user = new User(
                "Test", "Learner", Role.LEARNER, "learner.one", "$2a$12$hashed-password"
        );
        when(userRepository.findByUsername("learner.one"))
                .thenReturn(Optional.of(user));

        AcademyUserPrincipal principal = (AcademyUserPrincipal)
                userDetailsService.loadUserByUsername("  LEARNER.ONE  ");

        assertEquals("learner.one", principal.getUsername());
        assertEquals("Test", principal.getFirstName());
        assertEquals("Learner", principal.getLastName());
        assertEquals("Test Learner", principal.getFullName());
        assertEquals(Role.LEARNER, principal.getRole());
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LEARNER")));
    }

    @Test
    void rejectsUserWithoutProvisionedCredentials() {
        User user = new User("Legacy", "Learner", Role.LEARNER);
        when(userRepository.findByUsername("legacy.learner"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("legacy.learner")
        );
    }

    @Test
    void rejectsUnknownUsername() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown")
        );
    }
}
