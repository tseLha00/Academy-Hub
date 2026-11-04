package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import ch.accenture.academyhub.repository.TrainingRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TrainingManagementServiceTest {

    @Autowired
    private TrainingManagementService managementService;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingAssignmentRepository assignmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void learnerCreatesTrainingAssignedToThemselves() {
        User learner = persistUser("Creation Learner", Role.LEARNER);

        TrainingDetailsResponse response = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("  Java Practice  ", Set.of())
        );

        entityManager.flush();
        entityManager.clear();

        assertEquals("Java Practice", response.name());
        assertEquals(learner.getId(), response.createdById());
        assertEquals(List.of(learner.getId()), response.learnerIds());

        List<TrainingAssignment> assignments = assignmentRepository
                .findAllWithTrainingByLearnerId(learner.getId());

        assertEquals(1, assignments.size());
        assertEquals(
                response.trainingId(),
                assignments.get(0).getTraining().getId()
        );
        assertFalse(assignments.get(0).isCompleted());
    }

    @Test
    void adminCreatesTrainingAssignedToSelectedLearners() {
        User admin = persistUser("Creation Admin", Role.ADMIN);
        User learnerA = persistUser("Selected A", Role.LEARNER);
        User learnerB = persistUser("Selected B", Role.LEARNER);

        TrainingDetailsResponse response = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Shared Training",
                        Set.of(learnerA.getId(), learnerB.getId())
                )
        );

        entityManager.flush();
        entityManager.clear();

        assertEquals(admin.getId(), response.createdById());
        assertEquals(
                Set.of(learnerA.getId(), learnerB.getId()),
                Set.copyOf(response.learnerIds())
        );

        for (Long learnerId : response.learnerIds()) {
            List<TrainingAssignment> assignments = assignmentRepository
                    .findAllWithTrainingByLearnerId(learnerId);

            assertEquals(1, assignments.size());
            assertEquals(
                    response.trainingId(),
                    assignments.get(0).getTraining().getId()
            );
            assertFalse(assignments.get(0).isCompleted());
        }
    }

    @Test
    void adminCanCreateUnassignedTraining() {
        User admin = persistUser("Unassigned Admin", Role.ADMIN);
        long assignmentsBefore = assignmentRepository.count();

        TrainingDetailsResponse response = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest("Unassigned Training", Set.of())
        );

        entityManager.flush();
        entityManager.clear();

        assertTrue(trainingRepository.existsById(response.trainingId()));
        assertTrue(response.learnerIds().isEmpty());
        assertEquals(assignmentsBefore, assignmentRepository.count());
    }

    @Test
    void learnerCannotSelectOtherLearners() {
        User learner = persistUser("Creator", Role.LEARNER);
        User other = persistUser("Other Learner", Role.LEARNER);
        long trainingsBefore = trainingRepository.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.createTraining(
                        learner.getId(),
                        new CreateTrainingRequest(
                                "Forbidden Assignment",
                                Set.of(other.getId())
                        )
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(trainingsBefore, trainingRepository.count());
    }

    @Test
    void adminCannotAssignTrainingToAnotherAdmin() {
        User admin = persistUser("Creator Admin", Role.ADMIN);
        User otherAdmin = persistUser("Other Admin", Role.ADMIN);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.createTraining(
                        admin.getId(),
                        new CreateTrainingRequest(
                                "Invalid Assignment",
                                Set.of(otherAdmin.getId())
                        )
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsMissingSelectedLearner() {
        User admin = persistUser("Missing Learner Admin", Role.ADMIN);

        // Persist then remove a user to obtain an ID that does not exist.
        User removedLearner = persistUser("Removed Learner", Role.LEARNER);
        Long missingId = removedLearner.getId();
        entityManager.remove(removedLearner);
        entityManager.flush();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.createTraining(
                        admin.getId(),
                        new CreateTrainingRequest(
                                "Missing Learner",
                                Set.of(missingId)
                        )
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsBlankTrainingName() {
        User learner = persistUser("Validation Learner", Role.LEARNER);

        assertThrows(
                ConstraintViolationException.class,
                () -> managementService.createTraining(
                        learner.getId(),
                        new CreateTrainingRequest("   ", Set.of())
                )
        );
    }

    @Test
    void rejectsTrainingNameOver200Characters() {
        User learner = persistUser("Length Learner", Role.LEARNER);

        assertThrows(
                ConstraintViolationException.class,
                () -> managementService.createTraining(
                        learner.getId(),
                        new CreateTrainingRequest("a".repeat(201), Set.of())
                )
        );
    }

    @Test
    void learnerCanRenameTheirOwnTraining() {
        User learner = persistUser("Training Owner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("Original Name", Set.of())
        );

        managementService.updateTraining(
                learner.getId(),
                created.trainingId(),
                new UpdateTrainingRequest("  Updated Name  ")
        );

        entityManager.flush();
        entityManager.clear();

        Training updated = trainingRepository
                .findById(created.trainingId())
                .orElseThrow();

        assertEquals("Updated Name", updated.getName());
        assertEquals(learner.getId(), updated.getCreatedBy().getId());
    }

    @Test
    void adminRenameAppliesToAllAssignmentsAndPreservesCompletion() {
        User admin = persistUser("Editing Admin", Role.ADMIN);
        User learnerA = persistUser("Editing Learner A", Role.LEARNER);
        User learnerB = persistUser("Editing Learner B", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Original Shared Name",
                        Set.of(learnerA.getId(), learnerB.getId())
                )
        );

        TrainingAssignment assignmentA = assignmentRepository
                .findAllWithTrainingByLearnerId(learnerA.getId())
                .get(0);

        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");

        entityManager.flush();
        entityManager.createNativeQuery("""
            UPDATE training_assignments
            SET completed_at = :completedAt
            WHERE id = :assignmentId
            """)
                .setParameter("completedAt", completedAt)
                .setParameter("assignmentId", assignmentA.getId())
                .executeUpdate();
        entityManager.clear();

        managementService.updateTraining(
                admin.getId(),
                created.trainingId(),
                new UpdateTrainingRequest("Updated Shared Name")
        );

        entityManager.flush();
        entityManager.clear();

        TrainingAssignment updatedA = assignmentRepository
                .findAllWithTrainingByLearnerId(learnerA.getId())
                .get(0);

        TrainingAssignment updatedB = assignmentRepository
                .findAllWithTrainingByLearnerId(learnerB.getId())
                .get(0);

        assertEquals("Updated Shared Name", updatedA.getTraining().getName());
        assertEquals("Updated Shared Name", updatedB.getTraining().getName());
        assertEquals(completedAt, updatedA.getCompletedAt());
        assertFalse(updatedB.isCompleted());
    }

    @Test
    void learnerCannotRenameAnotherUsersTraining() {
        User owner = persistUser("Original Creator", Role.LEARNER);
        User other = persistUser("Other Editor", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                owner.getId(),
                new CreateTrainingRequest("Original Name", Set.of())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.updateTraining(
                        other.getId(),
                        created.trainingId(),
                        new UpdateTrainingRequest("Unauthorized Name")
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(
                "Original Name",
                trainingRepository.findById(created.trainingId())
                        .orElseThrow()
                        .getName()
        );
    }

    @Test
    void rejectsBlankNameWhenEditing() {
        User learner = persistUser("Editing Validation", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("Original Name", Set.of())
        );

        assertThrows(
                ConstraintViolationException.class,
                () -> managementService.updateTraining(
                        learner.getId(),
                        created.trainingId(),
                        new UpdateTrainingRequest("   ")
                )
        );
    }

    @Test
    void assignmentChangesPreserveRetainedCompletionAndAddNewLearners() {
        User admin = persistUser("Assignment Admin", Role.ADMIN);
        User retained = persistUser("Retained Learner", Role.LEARNER);
        User removed = persistUser("Removed Learner", Role.LEARNER);
        User added = persistUser("Added Learner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Assignment Changes",
                        Set.of(retained.getId(), removed.getId())
                )
        );

        TrainingAssignment retainedAssignment = assignmentRepository
                .findAllWithTrainingByLearnerId(retained.getId()).get(0);
        Long retainedAssignmentId = retainedAssignment.getId();

        Instant completedAt = Instant.parse("2026-10-20T10:00:00Z");

        entityManager.flush();
        entityManager.createNativeQuery("""
            UPDATE training_assignments
            SET completed_at = :completedAt
            WHERE training_id = :trainingId
            """)
                .setParameter("completedAt", completedAt)
                .setParameter("trainingId", created.trainingId())
                .executeUpdate();
        entityManager.clear();

        managementService.updateAssignments(
                admin.getId(),
                created.trainingId(),
                new UpdateTrainingAssignmentsRequest(
                        Set.of(retained.getId(), added.getId())
                )
        );

        entityManager.flush();
        entityManager.clear();

        List<TrainingAssignment> assignments = assignmentRepository
                .findAllByTrainingId(created.trainingId());

        assertEquals(2, assignments.size());

        TrainingAssignment retainedResult = assignments.stream()
                .filter(a -> a.getLearner().getId().equals(retained.getId()))
                .findFirst()
                .orElseThrow();

        TrainingAssignment addedResult = assignments.stream()
                .filter(a -> a.getLearner().getId().equals(added.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(retainedAssignmentId, retainedResult.getId());
        assertEquals(completedAt, retainedResult.getCompletedAt());
        assertFalse(addedResult.isCompleted());

        assertTrue(assignmentRepository
                .findAllWithTrainingByLearnerId(removed.getId())
                .isEmpty());
    }

    @Test
    void reassigningRemovedLearnerStartsIncomplete() {
        User admin = persistUser("Reassignment Admin", Role.ADMIN);
        User learner = persistUser("Reassigned Learner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Reassignment",
                        Set.of(learner.getId())
                )
        );

        entityManager.flush();
        entityManager.createNativeQuery("""
            UPDATE training_assignments
            SET completed_at = CURRENT_TIMESTAMP
            WHERE training_id = :trainingId
            """)
                .setParameter("trainingId", created.trainingId())
                .executeUpdate();
        entityManager.clear();

        managementService.updateAssignments(
                admin.getId(),
                created.trainingId(),
                new UpdateTrainingAssignmentsRequest(Set.of())
        );

        entityManager.flush();
        entityManager.clear();

        assertTrue(assignmentRepository
                .findAllByTrainingId(created.trainingId()).isEmpty());

        managementService.updateAssignments(
                admin.getId(),
                created.trainingId(),
                new UpdateTrainingAssignmentsRequest(Set.of(learner.getId()))
        );

        entityManager.flush();
        entityManager.clear();

        List<TrainingAssignment> assignments = assignmentRepository
                .findAllByTrainingId(created.trainingId());

        assertEquals(1, assignments.size());
        assertEquals(learner.getId(), assignments.get(0).getLearner().getId());
        assertNull(assignments.get(0).getCompletedAt());
    }

    @Test
    void learnerCannotChangeAssignmentsEvenForOwnTraining() {
        User learner = persistUser("Assignment Owner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("Personal Training", Set.of())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.updateAssignments(
                        learner.getId(),
                        created.trainingId(),
                        new UpdateTrainingAssignmentsRequest(Set.of())
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void rejectsAdminAssigneeBeforeChangingExistingAssignments() {
        User admin = persistUser("Validation Admin", Role.ADMIN);
        User learner = persistUser("Existing Assignee", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Preserved Assignments",
                        Set.of(learner.getId())
                )
        );

        Long originalAssignmentId = assignmentRepository
                .findAllByTrainingId(created.trainingId()).get(0).getId();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.updateAssignments(
                        admin.getId(),
                        created.trainingId(),
                        new UpdateTrainingAssignmentsRequest(
                                Set.of(admin.getId())
                        )
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        List<TrainingAssignment> assignments = assignmentRepository
                .findAllByTrainingId(created.trainingId());

        assertEquals(1, assignments.size());
        assertEquals(originalAssignmentId, assignments.get(0).getId());
    }

    @Test
    void adminCanDeleteAssignedTrainingAndItsCompletionRecords() {
        User admin = persistUser("Deletion Admin", Role.ADMIN);
        User learner = persistUser("Deletion Learner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest(
                        "Training To Delete",
                        Set.of(learner.getId())
                )
        );

        Long assignmentId = assignmentRepository
                .findAllByTrainingId(created.trainingId())
                .get(0)
                .getId();

        entityManager.flush();
        entityManager.createNativeQuery("""
            UPDATE training_assignments
            SET completed_at = CURRENT_TIMESTAMP
            WHERE id = :assignmentId
            """)
                .setParameter("assignmentId", assignmentId)
                .executeUpdate();
        entityManager.clear();

        managementService.deleteTraining(admin.getId(), created.trainingId());

        entityManager.flush();
        entityManager.clear();

        assertFalse(trainingRepository.existsById(created.trainingId()));
        assertFalse(assignmentRepository.existsById(assignmentId));
        assertNotNull(entityManager.find(User.class, learner.getId()));
    }

    @Test
    void learnerCannotDeleteOwnAssignedTraining() {
        User learner = persistUser("Assigned Owner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("Assigned Personal Training", Set.of())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.deleteTraining(
                        learner.getId(),
                        created.trainingId()
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(trainingRepository.existsById(created.trainingId()));
        assertEquals(
                1,
                assignmentRepository
                        .findAllByTrainingId(created.trainingId())
                        .size()
        );
    }

    @Test
    void learnerCanDeleteOwnTrainingAfterAdminRemovesAssignments() {
        User admin = persistUser("Unassigning Admin", Role.ADMIN);
        User learner = persistUser("Unassigned Owner", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                learner.getId(),
                new CreateTrainingRequest("Unassigned Personal Training", Set.of())
        );

        managementService.updateAssignments(
                admin.getId(),
                created.trainingId(),
                new UpdateTrainingAssignmentsRequest(Set.of())
        );

        entityManager.flush();
        entityManager.clear();

        managementService.deleteTraining(
                learner.getId(),
                created.trainingId()
        );

        entityManager.flush();
        entityManager.clear();

        assertFalse(trainingRepository.existsById(created.trainingId()));
    }

    @Test
    void learnerCannotDeleteAnotherUsersTraining() {
        User admin = persistUser("Training Creator Admin", Role.ADMIN);
        User learner = persistUser("Unauthorized Deleter", Role.LEARNER);

        TrainingDetailsResponse created = managementService.createTraining(
                admin.getId(),
                new CreateTrainingRequest("Protected Training", Set.of())
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> managementService.deleteTraining(
                        learner.getId(),
                        created.trainingId()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(trainingRepository.existsById(created.trainingId()));
    }

    private User persistUser(String name, Role role) {
        String[] nameParts = name.strip().split("\\s+", 2);

        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "User";

        User user = new User(firstName, lastName, role);
        entityManager.persist(user);
        return user;
    }
}