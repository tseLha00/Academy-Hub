package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainingProgressRequest(
        @NotNull
        @Min(0)
        @Max(100)
        Integer progressPercentage
) {
}
