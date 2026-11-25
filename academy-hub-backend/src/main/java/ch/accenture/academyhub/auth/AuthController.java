package ch.accenture.academyhub.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ch.accenture.academyhub.security.AcademyUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Tag(name = "Authentication", description = "Session login, current account, CSRF tokens, and logout.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Operation(summary = "Get a CSRF token",
            description = "Public. Execute this first, keep browser cookies, and paste response token into Authorize > csrfToken.")
    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @Operation(summary = "Log in",
            description = "Requires the csrfToken header and matching CSRF cookie. A successful login creates a browser-managed session.")
    @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content)
    @PostMapping("/login")
    public AuthenticatedUserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().strip().toLowerCase(Locale.ROOT),
                            request.password()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        httpRequest.getSession(true);
        httpRequest.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return toResponse((AcademyUserPrincipal) authentication.getPrincipal());
    }

    @Operation(summary = "Get the signed-in account",
            description = "Requires a session. Returns account identity and role; does not expose password hashes.")
    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal AcademyUserPrincipal principal
    ) {
        return toResponse(principal);
    }

    @Operation(summary = "Log out",
            description = "Requires a session and csrfToken. Invalidates the session. Retrieve a fresh token before logging in again.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUserResponse toResponse(AcademyUserPrincipal principal) {
        return new AuthenticatedUserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getFirstName(),
                principal.getLastName(),
                principal.getFullName(),
                principal.getRole()
        );
    }
}
