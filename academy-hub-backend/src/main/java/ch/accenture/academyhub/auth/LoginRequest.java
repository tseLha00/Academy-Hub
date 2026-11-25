package ch.accenture.academyhub.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(description = "Username of a locally provisioned account", example = "learner.one")
        @NotBlank @Size(max = 80) String username,
        @Schema(description = "The account password", format = "password")
        @NotBlank @Size(max = 72) String password
) {
}
