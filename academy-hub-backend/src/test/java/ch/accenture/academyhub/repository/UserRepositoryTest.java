package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndRetrievesLearnerProfile() {
        User learner = new User("Test", "Learner", Role.LEARNER);
        learner.updateProfile(
                "  learner@example.com  ",
                "  Zurich  ",
                "  Application Development  ",
                3
        );

        User saved = userRepository.saveAndFlush(learner);
        Long id = saved.getId();
        entityManager.clear();

        User retrieved = userRepository.findById(id).orElseThrow();

        assertEquals("learner@example.com", retrieved.getEmail());
        assertEquals("Zurich", retrieved.getLocation());
        assertEquals("Application Development", retrieved.getProfession());
        assertEquals(Integer.valueOf(3), retrieved.getApprenticeshipYear());
    }

    @Test
    void savesAdminWithoutLearnerSpecificInformation() {
        User admin = new User("Test", "Admin", Role.ADMIN);
        admin.updateProfile("admin@example.com", "Zurich", null, null);

        User saved = userRepository.saveAndFlush(admin);
        Long id = saved.getId();
        entityManager.clear();

        User retrieved = userRepository.findById(id).orElseThrow();

        assertEquals("admin@example.com", retrieved.getEmail());
        assertEquals("Zurich", retrieved.getLocation());
        assertNull(retrieved.getProfession());
        assertNull(retrieved.getApprenticeshipYear());
    }

    @Test
    void savesLearnerWithoutOptionalProfileInformation() {
        User learner = new User("Existing", "Learner", Role.LEARNER);

        User saved = userRepository.saveAndFlush(learner);
        Long id = saved.getId();
        entityManager.clear();

        User retrieved = userRepository.findById(id).orElseThrow();

        assertNull(retrieved.getEmail());
        assertNull(retrieved.getLocation());
        assertNull(retrieved.getProfession());
        assertNull(retrieved.getApprenticeshipYear());
    }
}