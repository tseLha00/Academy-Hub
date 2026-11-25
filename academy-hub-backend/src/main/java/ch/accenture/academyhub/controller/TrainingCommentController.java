package ch.accenture.academyhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ch.accenture.academyhub.dto.CreateTrainingCommentRequest;
import ch.accenture.academyhub.dto.TrainingCommentResponse;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import ch.accenture.academyhub.service.TrainingCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Training comments", description = "Comments belonging to one learner's assignment.")
@RestController
@RequestMapping("/api/training-assignments/{assignmentId}/comments")
public class TrainingCommentController {

    private final TrainingCommentService commentService;

    public TrainingCommentController(TrainingCommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Get assignment comments",
            description = "The assigned learner or an admin can read comments. Results are ordered by creation time and ID. A learner accessing another learner's assignment receives 404.")
    @GetMapping
    public List<TrainingCommentResponse> getComments(
            @PathVariable Long assignmentId,
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal
    ) {
        return commentService.getComments(
                assignmentId,
                principal.getId(),
                principal.getRole()
        );
    }

    @Operation(summary = "Add an assignment comment",
            description = "The assigned learner or an admin can add a comment. Uses assignmentId, not trainingId. Blank or oversized content is rejected.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingCommentResponse addComment(
            @PathVariable Long assignmentId,
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @Valid @RequestBody CreateTrainingCommentRequest request
    ) {
        return commentService.addComment(
                assignmentId,
                principal.getId(),
                principal.getRole(),
                request
        );
    }
}