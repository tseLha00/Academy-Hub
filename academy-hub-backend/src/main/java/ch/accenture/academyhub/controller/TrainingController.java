package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.TrainingResponse;
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
    @GetMapping("/trainings")
    @PreAuthorize("hasRole('LEARNER')")
    public List<TrainingResponse> getMyTrainings(
            @AuthenticationPrincipal AcademyUserPrincipal principal
    ) {
        return trainingService.getTrainingsForLearner(principal.getId());
    }

    /** Admin-only learner lookup, used by administrative workflows. */
    @GetMapping("/admin/learners/{learnerId}/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TrainingResponse> getLearnerTrainings(
            @PathVariable Long learnerId
    ) {
        return trainingService.getTrainingsForLearner(learnerId);
    }

    @PostMapping("/admin/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingDetailsResponse> createTraining(
            @AuthenticationPrincipal AcademyUserPrincipal principal,
            @Valid @RequestBody CreateTrainingRequest request
    ) {
        TrainingDetailsResponse response =
                managementService.createTraining(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/trainings/{trainingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTraining(
            @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId,
            @Valid @RequestBody UpdateTrainingRequest request
    ) {
        managementService.updateTraining(principal.getId(), trainingId, request);
    }

    @PutMapping("/admin/trainings/{trainingId}/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAssignments(
            @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId,
            @Valid @RequestBody UpdateTrainingAssignmentsRequest request
    ) {
        managementService.updateAssignments(principal.getId(), trainingId, request);
    }

    @DeleteMapping("/admin/trainings/{trainingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraining(
            @AuthenticationPrincipal AcademyUserPrincipal principal,
            @PathVariable Long trainingId
    ) {
        managementService.deleteTraining(principal.getId(), trainingId);
    }
}
