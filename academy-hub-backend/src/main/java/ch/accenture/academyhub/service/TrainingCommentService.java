package ch.accenture.academyhub.service;

import ch.accenture.academyhub.dto.CreateTrainingCommentRequest;
import ch.accenture.academyhub.dto.TrainingCommentResponse;
import ch.accenture.academyhub.entity.Role;
import ch.accenture.academyhub.entity.TrainingComment;
import ch.accenture.academyhub.repository.TrainingAssignmentRepository;
import ch.accenture.academyhub.repository.TrainingCommentRepository;
import ch.accenture.academyhub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrainingCommentService {

    private final TrainingCommentRepository commentRepository;
    private final TrainingAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public TrainingCommentService(
            TrainingCommentRepository commentRepository,
            TrainingAssignmentRepository assignmentRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingCommentResponse> getComments(
            Long assignmentId,
            Long userId,
            Role userRole
    ) {
        var assignment = getAuthorizedAssignment(assignmentId, userId, userRole);

        return commentRepository
                .findAllByAssignmentIdOrderByCreatedAtAscIdAsc(assignment.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TrainingCommentResponse addComment(
            Long assignmentId,
            Long userId,
            Role userRole,
            CreateTrainingCommentRequest request
    ) {
        var assignment = getAuthorizedAssignment(assignmentId, userId, userRole);

        var author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        TrainingComment comment = new TrainingComment(
                assignment,
                author,
                request.content().strip()
        );

        return toResponse(commentRepository.save(comment));
    }

    private ch.accenture.academyhub.entity.TrainingAssignment getAuthorizedAssignment(
            Long assignmentId,
            Long userId,
            Role userRole
    ) {
        var assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Training assignment not found"
                ));

        if (userRole == Role.LEARNER
                && !assignment.getLearner().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Training assignment not found"
            );
        }

        if (userRole != Role.ADMIN && userRole != Role.LEARNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access comments"
            );
        }

        return assignment;
    }

    private TrainingCommentResponse toResponse(TrainingComment comment) {
        return new TrainingCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getAuthor().getRole().name(),
                comment.getCreatedAt()
        );
    }
}