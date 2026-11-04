package ch.accenture.academyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTrainingCommentRequest(

        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
        String content

) {}