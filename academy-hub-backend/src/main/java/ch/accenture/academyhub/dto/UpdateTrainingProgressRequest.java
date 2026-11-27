package ch.accenture.academyhub.dto;

import ch.accenture.academyhub.entity.TrainingStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTrainingProgressRequest(
        @NotNull
        TrainingStatus status,

        LocalDate startDate,

        @NotNull
        LocalDate estimatedCompletionDate
) {
}
