package ch.accenture.academyhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.dto.UpdateTrainingProgressRequest;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import ch.accenture.academyhub.service.TrainingManagementService;
import ch.accenture.academyhub.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trainings", description = "Learner assignments and admin training management.")
@RestController
@RequestMapping("/api")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingManagementService managementService;

    public TrainingController(
            TrainingService trainingService,
            TrainingManagementService managementService
    ) {
        this.trainingService = trainingService;
        this.managementService = managementService;
    }

    /** A learner can only retrieve their own assigned trainings. */
    @Operation(summary = "Get my training assignments",
            description = "LEARNER only. Returns the signed-in learner's assignments, including assignment IDs for comment requests. An empty list is valid.")
    @GetMapping("/trainings")
    @PreAuthorize("hasRole('LEARNER')")
    public List<TrainingResponse> getMyTrainings(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal
    ) {
        return trainingService.getTrainingsForLearner(principal.getId());
    }

    /** A learner can complete only a training assigned to their own account. */
    @Operation(summary = "Complete my assigned training",
            description = "LEARNER only. Uses a training ID, not an assignment ID. Returns 404 if no matching assignment exists. Repeating completion preserves its timestamp.")
    @PostMapping("/trainings/{trainingId}/complete")
    @PreAuthorize("hasRole('LEARNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeTraining(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId
    ) {
        trainingService.completeTraining(principal.getId(), trainingId);
    }

    /** A learner can update progress only for their own assigned training. */
    @Operation(summary = "Update my training percentage",
            description = "LEARNER only. Accepts 0-100; reaching 100 completes the assignment. Reducing a completed assignment returns 409. This is the current API; status/date replacement is a separate ticket.")
    @PutMapping("/trainings/{trainingId}/progress")
    @PreAuthorize("hasRole('LEARNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTrainingProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId,
            @Valid @RequestBody UpdateTrainingProgressRequest request
    ) {
        trainingService.updateTrainingProgress(
                principal.getId(), trainingId, request.progressPercentage()
        );
    }

    /** Admin-only learner lookup, used by administrative workflows. */
    @Operation(summary = "Get a learner's assignments",
            description = "ADMIN only. Returns the selected learner's assignments. An empty response does not distinguish a missing learner from a learner without assignments.")
    @GetMapping("/admin/learners/{learnerId}/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TrainingResponse> getLearnerTrainings(
            @PathVariable Long learnerId
    ) {
        return trainingService.getTrainingsForLearner(learnerId);
    }

    @Operation(summary = "Create an admin training",
            description = "ADMIN only in the current HTTP API. learnerIds is required; use [] for an unassigned training. Selected IDs must belong to existing learners.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingDetailsResponse> createTraining(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @Valid @RequestBody CreateTrainingRequest request
    ) {
        TrainingDetailsResponse response =
                managementService.createTraining(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Rename a training",
            description = "ADMIN only in the current HTTP API. Changes the shared training name for all assignments.")
    @PutMapping("/admin/trainings/{trainingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTraining(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId,
            @Valid @RequestBody UpdateTrainingRequest request
    ) {
        managementService.updateTraining(principal.getId(), trainingId, request);
    }

    @Operation(summary = "Replace training assignments",
            description = "ADMIN only. learnerIds is the complete desired set, not an incremental addition. Retained assignments keep progress; removed assignments and their comments are deleted.")
    @PutMapping("/admin/trainings/{trainingId}/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAssignments(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId,
            @Valid @RequestBody UpdateTrainingAssignmentsRequest request
    ) {
        managementService.updateAssignments(principal.getId(), trainingId, request);
    }

    @Operation(summary = "Delete a training",
            description = "ADMIN only. Removes the training and its assignments; assignment comments are deleted through the database cascade.")
    @DeleteMapping("/admin/trainings/{trainingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraining(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId
    ) {
        managementService.deleteTraining(principal.getId(), trainingId);
    }
}
