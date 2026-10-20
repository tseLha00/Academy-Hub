package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateTrainingRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        @NotNull
        Set<@NotNull @Positive Long> learnerIds

) {
}