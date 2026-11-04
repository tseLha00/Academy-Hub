package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.CreateTrainingCommentRequest;
import ch.accenture.academyhub.dto.TrainingCommentResponse;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import ch.accenture.academyhub.service.TrainingCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-assignments/{assignmentId}/comments")
public class TrainingCommentController {

    private final TrainingCommentService commentService;

    public TrainingCommentController(TrainingCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<TrainingCommentResponse> getComments(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal AcademyUserPrincipal principal
    ) {
        return commentService.getComments(
                assignmentId,
                principal.getId(),
                principal.getRole()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingCommentResponse addComment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal AcademyUserPrincipal principal,
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