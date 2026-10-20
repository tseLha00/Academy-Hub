package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UpdateTrainingAssignmentsRequest(
        @NotNull
        Set<@NotNull @Positive Long> learnerIds
) {
}