package ch.accenture.academyhub.repository;

import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.TrainingStatus;
import ch.accenture.academyhub.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
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
        assertEquals(TrainingStatus.COMPLETED, reloaded.getStatus());

        TrainingAssignment otherLearnerAssignment = assignmentRepository
                .findByTrainingIdAndLearnerId(training.getId(), learnerB.getId())
                .orElseThrow();
        assertFalse(otherLearnerAssignment.isCompleted());
        assertEquals(TrainingStatus.TODO, otherLearnerAssignment.getStatus());
    }

    @Test
    void persistsStatusAndDatesForLearnerAssignment() {
        TrainingAssignment assignment = persistAssignment("Status Test");
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate estimatedCompletionDate = LocalDate.of(2025, 4, 30);

        assignment.updateDates(startDate, estimatedCompletionDate);
        assignment.updateStatus(TrainingStatus.BLOCKED);
        entityManager.flush();
        entityManager.clear();

        TrainingAssignment reloaded = assignmentRepository.findById(assignment.getId())
                .orElseThrow();
        assertEquals(TrainingStatus.BLOCKED, reloaded.getStatus());
        assertEquals(startDate, reloaded.getStartDate());
        assertEquals(estimatedCompletionDate, reloaded.getEstimatedCompletionDate());
        assertNull(reloaded.getCompletedAt());
    }

    @Test
    void rejectsEstimatedCompletionDateBeforeStartDate() {
        TrainingAssignment assignment = persistAssignment("Reversed Dates");
        LocalDate startDate = LocalDate.of(2025, 3, 1);

        assertThrows(IllegalArgumentException.class,
                () -> assignment.updateDates(startDate, startDate.minusDays(1)));
        assertThrows(IllegalArgumentException.class,
                () -> assignment.updateDates(startDate, null));
        assertNull(assignment.getEstimatedCompletionDate());
    }

    @Test
    void reopeningClearsCompletionAndCompletingAgainRecordsNewTimestamp() {
        TrainingAssignment assignment = persistAssignment("Reopen Test");

        assignment.updateStatus(TrainingStatus.COMPLETED);
        Instant firstCompletion = assignment.getCompletedAt();
        assertNotNull(firstCompletion);

        assignment.updateStatus(TrainingStatus.IN_PROGRESS);
        assertNull(assignment.getCompletedAt());
        assertFalse(assignment.isCompleted());

        assignment.updateStatus(TrainingStatus.COMPLETED);
        assertNotNull(assignment.getCompletedAt());
        assertFalse(assignment.getCompletedAt().isBefore(firstCompletion));
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

    private TrainingAssignment persistAssignment(String name) {
        User learner = new User(name, "Learner", Role.LEARNER);
        entityManager.persist(learner);
        Training training = new Training(name + " Training", learner);
        entityManager.persist(training);
        TrainingAssignment assignment = new TrainingAssignment(training, learner);
        entityManager.persist(assignment);
        return assignment;
    }
}
