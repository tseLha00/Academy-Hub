package ch.accenture.academyhub.auth;

public record CsrfTokenResponse(String headerName, String token) {
}
