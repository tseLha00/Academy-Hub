package ch.accenture.academyhub.dto;

import java.util.List;

public record TrainingDetailsResponse(
        Long trainingId,
        String name,
        Long createdById,
        List<Long> learnerIds
) {
}