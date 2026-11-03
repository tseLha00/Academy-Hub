# TASK-10 — Learner training completion

## Endpoint

`POST /api/trainings/{trainingId}/complete`

The endpoint requires an authenticated user with the `LEARNER` role. It takes the training ID from the URL and obtains the learner ID from the authenticated principal; clients must not submit a learner ID.

## Behaviour

- The backend looks up the assignment by both training ID and authenticated learner ID.
- If the learner is not assigned to that training, the endpoint returns `404 Not Found` and does not modify another learner's assignment.
- If an assignment exists, its `completed_at` timestamp is set and persisted.
- Repeating the request is safe: an existing completion timestamp is preserved.
- Success returns `204 No Content`.
- A subsequent `GET /api/trainings` response includes `completed: true` and the saved `completedAt` timestamp.

## Verification

Automated tests cover authenticated identity delegation, assignment scoping, missing assignments, and persistence of completion status after reloading the assignment.
