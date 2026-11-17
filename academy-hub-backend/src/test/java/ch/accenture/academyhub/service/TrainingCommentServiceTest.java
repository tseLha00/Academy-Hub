
package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.CreateTrainingCommentRequest;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.Training;
import ch.accenture.academyhub.entity.TrainingAssignment;
import ch.accenture.academyhub.entity.User;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import ch.accenture.academyhub.repository.TrainingCommentRepository;
import ch.accenture.academyhub.repository.TrainingRepository;
import ch.accenture.academyhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TrainingCommentServiceTest {

    @Autowired
    private TrainingCommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingAssignmentRepository assignmentRepository;

    @Autowired
    private TrainingCommentRepository commentRepository;

    private User admin;
    private User learner;
    private User otherLearner;
    private TrainingAssignment learnerAssignment;
    private TrainingAssignment otherLearnerAssignment;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(
                new User("Comment", "Admin", Role.ADMIN)
        );

        learner = userRepository.save(
                new User("Comment", "Learner", Role.LEARNER)
        );

        otherLearner = userRepository.save(
                new User("Other", "Learner", Role.LEARNER)
        );

        Training training = trainingRepository.save(
                new Training("Comment Test Training", admin)
        );

        learnerAssignment = assignmentRepository.save(
                new TrainingAssignment(training, learner)
        );

        otherLearnerAssignment = assignmentRepository.save(
                new TrainingAssignment(training, otherLearner)
        );
    }

    @Test
    void adminCanAddCommentToAnyAssignment() {
        var response = commentService.addComment(
                otherLearnerAssignment.getId(),
                admin.getId(),
                Role.ADMIN,
                new CreateTrainingCommentRequest("Admin comment")
        );

        assertEquals("Admin comment", response.content());
        assertEquals(admin.getId(), response.authorId());
        assertEquals("Comment Admin", response.authorName());
        assertEquals("ADMIN", response.authorRole());
    }

    @Test
    void learnerCanAddCommentToOwnAssignment() {
        var response = commentService.addComment(
                learnerAssignment.getId(),
                learner.getId(),
                Role.LEARNER,
                new CreateTrainingCommentRequest("Learner comment")
        );

        assertEquals("Learner comment", response.content());
        assertEquals(learner.getId(), response.authorId());
        assertEquals("Comment Learner", response.authorName());
        assertEquals("LEARNER", response.authorRole());
    }

    @Test
    void learnerCannotAddCommentToAnotherLearnersAssignment() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.addComment(
                        otherLearnerAssignment.getId(),
                        learner.getId(),
                        Role.LEARNER,
                        new CreateTrainingCommentRequest("Unauthorized comment")
                )
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals(0, commentRepository.count());
    }

    @Test
    void learnerCannotReadAnotherLearnersComments() {
        commentService.addComment(
                otherLearnerAssignment.getId(),
                otherLearner.getId(),
                Role.LEARNER,
                new CreateTrainingCommentRequest("Private comment")
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.getComments(
                        otherLearnerAssignment.getId(),
                        learner.getId(),
                        Role.LEARNER
                )
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void adminCanReadCommentsFromAnyAssignment() {
        commentService.addComment(
                otherLearnerAssignment.getId(),
                otherLearner.getId(),
                Role.LEARNER,
                new CreateTrainingCommentRequest("Learner comment")
        );

        var comments = commentService.getComments(
                otherLearnerAssignment.getId(),
                admin.getId(),
                Role.ADMIN
        );

        assertEquals(1, comments.size());
        assertEquals("Learner comment", comments.get(0).content());
    }

    @Test
    void commentsAreReturnedInChronologicalOrder() {
        commentService.addComment(
                learnerAssignment.getId(),
                learner.getId(),
                Role.LEARNER,
                new CreateTrainingCommentRequest("First comment")
        );

        commentService.addComment(
                learnerAssignment.getId(),
                admin.getId(),
                Role.ADMIN,
                new CreateTrainingCommentRequest("Second comment")
        );

        var comments = commentService.getComments(
                learnerAssignment.getId(),
                learner.getId(),
                Role.LEARNER
        );

        assertEquals(2, comments.size());
        assertEquals("First comment", comments.get(0).content());
        assertEquals("Second comment", comments.get(1).content());
    }

    @Test
    void missingAssignmentReturnsNotFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> commentService.getComments(
                        999999L,
                        admin.getId(),
                        Role.ADMIN
                )
        );

        assertEquals(404, exception.getStatusCode().value());
    }
}
