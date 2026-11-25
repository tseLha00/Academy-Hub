package ch.accenture.academyhub.dto;

import java.util.List;

public record TrainingDetailsResponse(
        Long trainingId,
        String name,
        String description,
        Long createdById,
        List<Long> learnerIds
) {
}