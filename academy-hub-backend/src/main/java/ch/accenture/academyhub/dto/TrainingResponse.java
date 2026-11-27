package ch.accenture.academyhub.dto;

import ch.accenture.academyhub.entity.TrainingStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TrainingResponse(
        Long assignmentId,
        Long trainingId,
        String name,
        boolean completed,
        Instant completedAt,
        TrainingStatus status,
        LocalDate startDate,
        LocalDate estimatedCompletionDate
) {
}
