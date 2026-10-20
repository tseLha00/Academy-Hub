package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.CreateTrainingRequest;
import ch.accenture.academyhub.dto.TrainingDetailsResponse;
import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.dto.UpdateTrainingAssignmentsRequest;
import ch.accenture.academyhub.dto.UpdateTrainingRequest;
import ch.accenture.academyhub.service.TrainingManagementService;
import ch.accenture.academyhub.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev")
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

    @GetMapping("/learners/{learnerId}/trainings")
    public List<TrainingResponse> getTrainings(
            @PathVariable("learnerId") Long learnerId
    ) {
        return trainingService.getTrainingsForLearner(learnerId);
    }

    @PostMapping("/trainings")
    public ResponseEntity<TrainingDetailsResponse> createTraining(
            @RequestHeader("X-Dev-Actor-Id") Long actorId,
            @Valid @RequestBody CreateTrainingRequest request
    ) {
        TrainingDetailsResponse response =
                managementService.createTraining(actorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/trainings/{trainingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTraining(
            @RequestHeader("X-Dev-Actor-Id") Long actorId,
            @PathVariable("trainingId") Long trainingId,
            @Valid @RequestBody UpdateTrainingRequest request
    ) {
        managementService.updateTraining(actorId, trainingId, request);
    }

    @PutMapping("/trainings/{trainingId}/assignments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAssignments(
            @RequestHeader("X-Dev-Actor-Id") Long actorId,
            @PathVariable("trainingId") Long trainingId,
            @Valid @RequestBody UpdateTrainingAssignmentsRequest request
    ) {
        managementService.updateAssignments(actorId, trainingId, request);
    }

    @DeleteMapping("/trainings/{trainingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraining(
            @RequestHeader("X-Dev-Actor-Id") Long actorId,
            @PathVariable("trainingId") Long trainingId
    ) {
        managementService.deleteTraining(actorId, trainingId);
    }
}