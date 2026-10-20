package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTrainingRequest(
        @NotBlank
        @Size(max = 200)
        String name
) {
}