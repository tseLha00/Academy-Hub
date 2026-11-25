# Explore the API with Swagger UI

Swagger UI generates interactive documentation from the backend controllers and DTOs. Each teammate runs it locally; a shared Postman workspace is not required.

## Addresses

| Resource | Local address |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml |

This documents the current HTTP API. It does not implement the pending learner-submission, approval, description, or status/date tickets.

## Before trying requests

Start PostgreSQL and the backend using the [setup guide](../README.md#local-setup). Provision a local account using the [authentication guide](TASK-08-authentication.md#existing-user-records); there are no default credentials. Use the same hostname throughout, rather than mixing `localhost` with `127.0.0.1`.

Documentation can be read without login. Business endpoints still require the existing session and permissions. State-changing requests, including login, still require CSRF protection.

## Sign in through Swagger UI

1. Expand **Authentication > Get a CSRF token**.
2. Click **Try it out**, then **Execute**.
3. Copy `token` from the JSON response. The browser retains the matching CSRF cookie.
4. Click **Authorize**, paste the token into **csrfToken**, click its **Authorize** button, then **Close**.
5. Leave **sessionCookie** empty. Browsers manage `JSESSIONID` after login; typing a cookie into this dialog is not the login mechanism.
6. Expand **Log in**, click **Try it out**, and submit your local username and password.
7. Execute **Get the signed-in account** (`GET /api/auth/me`) to check the returned identity and role.

Authorise the response-body token, not the raw `XSRF-TOKEN` cookie. The existing Spring Security configuration returns a masked token. Automatic raw-cookie CSRF injection is therefore disabled in Swagger UI; the header security scheme sends the response token without changing the application's CSRF configuration.

## Training and comment requests

- As a learner, call `GET /api/trainings`. An empty array is valid if there are no assignments.
- Use `trainingId` for completion and percentage-progress requests.
- Use `assignmentId` for comments. Learner assignment responses contain both IDs.
- Admin management routes require an admin account. Use `"learnerIds": []` to create an unassigned training.
- Assignment updates supply the complete desired learner set. Removing a learner deletes their assignment and its comments. Retained assignments keep their progress.
- Individual percentages are the current API; the planned status/date replacement is a separate ticket.

Requests can change records in the configured database. Use your local development database.

## Switch accounts or recover from a CSRF error

Execute `POST /api/auth/logout` with your currently authorised CSRF token. Retrieve and authorise a fresh token before logging in as another account. Logout does not necessarily clear the value displayed in the Authorize dialog.

For a state-changing request returning `403`, obtain a fresh token and also check the account's role and ownership permissions. A CSRF token does not grant access permissions.

## Maintain the documentation

- Controllers describe paths, summaries, roles, and ownership behaviour.
- DTOs define fields; validation annotations provide constraints.
- `OpenApiConfig` describes API information and session/CSRF requirements.
- Internal principal and CSRF framework arguments are hidden from request inputs.
- Update annotations and run the documentation tests when changing endpoints.

Documentation describes security; it does not enforce it.

## Disable API documentation

Set `API_DOCS_ENABLED=false` in the process environment or root `.env`, then restart the backend. This disables both Swagger UI and generated specifications. Documentation is enabled by default for local setup.

## Validation

From `academy-hub-backend/`, with PostgreSQL running:

```bash
./mvnw test -Dtest=OpenApiDocumentationTest
./mvnw test
```

On Windows PowerShell use `.\mvnw.cmd` instead of `./mvnw`.

The tests check documentation access, operation security descriptions, protection of business endpoints, and the response-body-CSRF-token/login/session flow using a temporary account.

## References

- [Springdoc setup](https://springdoc.org/getting-started.html)
- [Spring Boot compatibility](https://springdoc.org/faq.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
