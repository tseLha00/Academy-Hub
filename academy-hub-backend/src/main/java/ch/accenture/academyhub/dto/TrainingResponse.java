package ch.accenture.academyhub.dto;

import java.time.Instant;

public record TrainingResponse(
        Long assignmentId,
        Long trainingId,
        String name,
        String description,
        boolean completed,
        Instant completedAt,
        int progressPercentage
) {
}
