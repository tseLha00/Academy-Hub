package ch.accenture.academyhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateTrainingRequest(

        @Schema(example = "Java Fundamentals")
        @NotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "Existing learner IDs; an empty array creates an unassigned training", example = "[]")
        @NotNull
        Set<@NotNull @Positive Long> learnerIds

) {
}