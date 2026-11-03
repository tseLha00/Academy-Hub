# TASK-08 – Authentication and role-based access

## Implementation

- Username/password authentication is handled by Spring Security.
- Passwords are stored as BCrypt hashes (work factor 12), never as plain text.
- Successful login creates a server-side HTTP session.
- The authenticated principal contains the database user ID, username, display name and role.
- `GET /api/auth/csrf` returns a CSRF token and sets the `XSRF-TOKEN` cookie.
- `POST /api/auth/login` authenticates credentials and returns the authenticated user's basic profile.
- `GET /api/auth/me` returns the current authenticated user's profile.
- `POST /api/auth/logout` invalidates the session.
- All endpoints other than the CSRF and login endpoints require authentication.

## Existing user records

The migration adds nullable `username` and `password_hash` columns so existing user rows remain valid. A user without both values cannot log in until credentials are provisioned. No default credentials are created.

To provision an account, generate a BCrypt hash with Spring Security's `BCryptPasswordEncoder` (strength 12). From the backend directory, build the dependency classpath and open JShell:

```bash
./mvnw -q dependency:build-classpath -Dmdep.outputFile=/tmp/academy-hub-classpath.txt
jshell --class-path "$(cat /tmp/academy-hub-classpath.txt)"
```

At the JShell prompt, generate the hash (enter the password locally; do not commit it):

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
new BCryptPasswordEncoder(12).encode("your-password");
```

Then update the intended user row with the resulting hash:

```sql
UPDATE users
SET username = 'learner.one',
    password_hash = '<BCrypt hash generated for the chosen password>'
WHERE id = 1;
```

Usernames are normalized to lowercase by the application. The database enforces uniqueness for non-null usernames.

## Request flow

1. `GET /api/auth/csrf`.
2. Send the returned token in the returned header name, and retain the `XSRF-TOKEN` cookie.
3. `POST /api/auth/login` with JSON containing `username` and `password`, plus the CSRF header and cookie.
4. Retain the `JSESSIONID` cookie for subsequent requests.
5. `GET /api/auth/me` to inspect the authenticated identity.
6. `POST /api/auth/logout` with the CSRF header and cookie to end the session.

## Scope boundary

This task establishes authentication and the authenticated principal. Replacing the development actor header in the training endpoints and applying endpoint-specific Learner/Admin permissions is TASK-09.
