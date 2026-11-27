package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/** The dates are set by learners only; admins assign without dates. */
public record CreateTrainingRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        @NotNull
        Set<@NotNull @Positive Long> learnerIds,

        LocalDate startDate,

        LocalDate estimatedCompletionDate

) {
    public CreateTrainingRequest(String name, Set<Long> learnerIds) {
        this(name, learnerIds, null, null);
    }
}
