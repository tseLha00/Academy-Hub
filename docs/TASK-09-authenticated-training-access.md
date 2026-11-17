# TASK-09 — Authenticated training access and role permissions

## What changed

- Removed the `/api/dev` training API and the `X-Dev-Actor-Id` identity header.
- The authenticated `AcademyUserPrincipal` is now the source of the current user's ID and role.
- Learners retrieve only their own assignments through `GET /api/trainings`.
- Admin-only operations are grouped under `/api/admin` and protected with method-level `@PreAuthorize("hasRole('ADMIN')")` checks.
- Admins can retrieve a learner's assigned trainings through `GET /api/admin/learners/{learnerId}/trainings`.
- Training create, edit, assignment changes, and deletion are no longer exposed to learners through the HTTP API.

## Endpoints

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/trainings` | LEARNER — current user's trainings only |
| GET | `/api/admin/learners/{learnerId}/trainings` | ADMIN |
| POST | `/api/admin/trainings` | ADMIN |
| PUT | `/api/admin/trainings/{trainingId}` | ADMIN |
| PUT | `/api/admin/trainings/{trainingId}/assignments` | ADMIN |
| DELETE | `/api/admin/trainings/{trainingId}` | ADMIN |

The caller's identity is taken from the server-side session. Supplying a different user ID in a request header is no longer supported.

## CSRF and session usage

For browser clients, first request `GET /api/auth/csrf`, then include the returned CSRF cookie/token on state-changing requests. Authenticate with `POST /api/auth/login`; the session cookie is then sent with subsequent requests. `POST /api/auth/logout` ends the session.

## Verification

Run the full backend test suite:

```bash
./mvnw test
```

Role annotations are enforced by Spring method security, enabled in `SecurityConfig`. The service-layer tests continue to cover the underlying training and assignment rules; controller tests cover use of the authenticated principal rather than a caller-supplied actor ID.
