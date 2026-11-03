package ch.accenture.academyhub.auth;

import ch.accenture.academyhub.entity.Role;

public record AuthenticatedUserResponse(
        Long id,
        String username,
        String displayName,
        Role role
) {
}
