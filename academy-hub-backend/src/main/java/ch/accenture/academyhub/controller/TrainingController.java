package ch.accenture.academyhub.controller;

import ch.accenture.academyhub.dto.TrainingResponse;
import ch.accenture.academyhub.service.TrainingService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dev/learners")
public class TrainingController {
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/{learnerId}/trainings")
    public List<TrainingResponse> getTrainings(
            @PathVariable("learnerId") Long learnerId
    ) {
        return trainingService.getTrainingsForLearner(learnerId);
    }
}
