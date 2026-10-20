package ch.accenture.academyhub.dto;

import java.time.Instant;

public record TrainingResponse(
        Long assignmentId,
        Long trainingId,
        String name,
        boolean completed,
        Instant completedAt
) {
}
