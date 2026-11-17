package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TrainingAssignmentRepositoryTest {

    @Autowired
    private TrainingAssignmentRepository assignmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void returnsOnlyRequestedLearnersAssignmentsSortedByTrainingName() {
        User learnerA = new User("Repository", "Test A", Role.LEARNER);
        User learnerB = new User("Repository", "Test B", Role.LEARNER);
        entityManager.persist(learnerA);
        entityManager.persist(learnerB);

        Training java = new Training("Java Fundamentals", learnerA);
        Training angular = new Training("Angular Fundamentals", learnerA);
        Training other = new Training("Other Learner Training", learnerB);
        entityManager.persist(java);
        entityManager.persist(angular);
        entityManager.persist(other);

        entityManager.persist(new TrainingAssignment(java, learnerA));
        entityManager.persist(new TrainingAssignment(angular, learnerA));
        entityManager.persist(new TrainingAssignment(other, learnerB));

        entityManager.flush();
        entityManager.clear();

        List<TrainingAssignment> result = assignmentRepository
                .findAllWithTrainingByLearnerId(learnerA.getId());

        assertEquals(
                List.of("Angular Fundamentals", "Java Fundamentals"),
                result.stream()
                        .map(assignment -> assignment.getTraining().getName())
                        .toList()
        );

        assertTrue(result.stream().allMatch(assignment ->
                assignment.getLearner().getId().equals(learnerA.getId())
        ));
    }

    @Test
    void findsAssignmentOnlyForMatchingTrainingAndLearnerAndPersistsCompletion() {
        User learnerA = new User("Completion Test", "A", Role.LEARNER);
        User learnerB = new User("Completion Test", "B", Role.LEARNER);
        entityManager.persist(learnerA);
        entityManager.persist(learnerB);

        Training training = new Training("Completion Test Training", learnerA);
        entityManager.persist(training);
        TrainingAssignment assignmentA = new TrainingAssignment(training, learnerA);
        TrainingAssignment assignmentB = new TrainingAssignment(training, learnerB);
        entityManager.persist(assignmentA);
        entityManager.persist(assignmentB);
        entityManager.flush();

        assertTrue(assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learnerA.getId())
                .isPresent());
        assertTrue(assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), 999999L)
                .isEmpty());

        TrainingAssignment learnerAssignment = assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learnerA.getId())
                .orElseThrow();
        learnerAssignment.markCompleted();
        var firstCompletionTimestamp = learnerAssignment.getCompletedAt();
        learnerAssignment.markCompleted();
        assertEquals(firstCompletionTimestamp, learnerAssignment.getCompletedAt());
        entityManager.flush();
        entityManager.clear();

        TrainingAssignment reloaded = assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learnerA.getId())
                .orElseThrow();
        assertTrue(reloaded.isCompleted());
        assertNotNull(reloaded.getCompletedAt());
        assertEquals(100, reloaded.getProgressPercentage());

        TrainingAssignment otherLearnerAssignment = assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learnerB.getId())
                .orElseThrow();
        assertFalse(otherLearnerAssignment.isCompleted());
        assertEquals(0, otherLearnerAssignment.getProgressPercentage());
    }

    @Test
    void persistsPartialProgressForLearnerAssignment() {
        User learner = new User("Progress Test", "Learner", Role.LEARNER);
        entityManager.persist(learner);
        Training training = new Training("Progress Test Training", learner);
        entityManager.persist(training);
        TrainingAssignment assignment = new TrainingAssignment(training, learner);
        entityManager.persist(assignment);

        assignment.updateProgressPercentage(45);
        entityManager.flush();
        entityManager.clear();

        TrainingAssignment reloaded = assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learner.getId())
                .orElseThrow();
        assertEquals(45, reloaded.getProgressPercentage());
        assertFalse(reloaded.isCompleted());
        assertNull(reloaded.getCompletedAt());
    }

    @Test
    void rejectsProgressOutsideZeroToOneHundred() {
        User learner = new User("Invalid Progress", "Learner", Role.LEARNER);
        entityManager.persist(learner);
        Training training = new Training("Invalid Progress Training", learner);
        entityManager.persist(training);
        TrainingAssignment assignment = new TrainingAssignment(training, learner);
        assertThrows(IllegalArgumentException.class,
                () -> assignment.updateProgressPercentage(101));
        assertThrows(IllegalArgumentException.class,
                () -> assignment.updateProgressPercentage(-1));
    }

    @Test
    void settingProgressToOneHundredMarksAssignmentCompleted() {
        User learner = new User("Complete Progress", "Learner", Role.LEARNER);
        entityManager.persist(learner);
        Training training = new Training("Complete Progress Training", learner);
        entityManager.persist(training);
        TrainingAssignment assignment = new TrainingAssignment(training, learner);

        assignment.updateProgressPercentage(100);

        assertEquals(100, assignment.getProgressPercentage());
        assertTrue(assignment.isCompleted());
        assertNotNull(assignment.getCompletedAt());
    }

    @Test
    void completedAssignmentCannotBeMovedBackwards() {
        User learner = new User("Backward Progress", "Learner", Role.LEARNER);
        entityManager.persist(learner);
        Training training = new Training("Backward Progress Training", learner);
        entityManager.persist(training);
        TrainingAssignment assignment = new TrainingAssignment(training, learner);
        assignment.markCompleted();

        assertThrows(IllegalStateException.class,
                () -> assignment.updateProgressPercentage(80));
        assertEquals(100, assignment.getProgressPercentage());
        assertTrue(assignment.isCompleted());
    }

    @Test
    void returnsEmptyListForLearnerWithoutAssignments() {
        User learner = new User("Repository Test", "Empty", Role.LEARNER);
        entityManager.persist(learner);
        entityManager.flush();
        entityManager.clear();

        List<TrainingAssignment> result = assignmentRepository
                .findAllWithTrainingByLearnerId(learner.getId());

        assertTrue(result.isEmpty());
    }
}