package ch.accenture.academyhub.security;

import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AcademyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (user.getPasswordHash() == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        return new AcademyUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
