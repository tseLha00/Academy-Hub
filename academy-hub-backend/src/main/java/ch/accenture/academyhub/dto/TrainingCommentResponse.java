package ch.accenture.academyhub.dto;

import java.time.Instant;

public record TrainingCommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorName,
        String authorRole,
        Instant createdAt
) {}