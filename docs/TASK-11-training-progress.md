# TASK-11 — Per-training progress (statuses and dates)

Progress is tracked with a status and learner-selected dates. This replaces the earlier percentage (issue #52).

## Endpoint

`PUT /api/trainings/{trainingId}/progress`

Requires an authenticated user with the `LEARNER` role. The learner identity is taken from the authenticated principal; no learner ID is accepted from the request.

Request body:

```json
{
  "status": "IN_PROGRESS",
  "startDate": "2026-11-01",
  "estimatedCompletionDate": "2026-12-31"
}
```

- `status` is required: `TODO`, `IN_PROGRESS`, `BLOCKED` or `COMPLETED`.
- `startDate` is optional and independent of the training's creation date.
- `estimatedCompletionDate` is required and cannot be before a supplied `startDate`.

Responses:

- `204 No Content` on success.
- `400 Bad Request` when a required value is missing or the dates are invalid.
- `404 Not Found` when the training is not assigned to the authenticated learner, so a learner cannot update another learner's assignment.

## Completion timestamp

`completed_at` always matches the status:

- Changing to `COMPLETED` records the completion timestamp.
- Repeating `COMPLETED` preserves the original timestamp.
- Changing to any other status reopens the assignment and clears `completed_at`.
- Completing again afterwards records a new timestamp.

`POST /api/trainings/{trainingId}/complete` still works and sets the status to `COMPLETED`.

## Creating trainings

- A learner creating a training must provide `estimatedCompletionDate` (and optionally `startDate`). The dates are stored on the learner's assignment.
- Admins create and assign trainings without dates; the assigned learner provides them afterward. Sending dates as an admin returns `400 Bad Request`.

The learner rule is enforced in `TrainingManagementService`; there is no learner endpoint for creating trainings yet (`POST /api/admin/trainings` is admin-only).

## Data model

Migration `V6__replace_progress_percentage_with_status_and_dates.sql` changes `training_assignments`:

| Column | Type | Notes |
|---|---|---|
| `status` | `VARCHAR(20)`, not null | Default `TODO` |
| `start_date` | `DATE`, nullable | Chosen by the learner |
| `estimated_completion_date` | `DATE`, nullable | Chosen by the learner; empty until the learner provides it |

The `progress_percentage` column is removed.

Existing assignments are migrated as follows:

| Previous data | New status |
|---|---|
| `completed_at` set | `COMPLETED` (timestamp kept) |
| progress above 0% | `IN_PROGRESS` |
| otherwise | `TODO` |

Existing assignments keep empty dates.

Database constraints:

- `status` must be one of the four values.
- `completed_at` is set exactly when the status is `COMPLETED`.
- `estimated_completion_date` cannot be before `start_date`.

## Responses

`GET /api/trainings` (learner) and `GET /api/admin/learners/{learnerId}/trainings` (admin) return for each training:

```json
{
  "assignmentId": 3,
  "trainingId": 2,
  "name": "Java Fundamentals",
  "completed": false,
  "completedAt": null,
  "status": "IN_PROGRESS",
  "startDate": "2026-11-01",
  "estimatedCompletionDate": "2026-12-31"
}
```

`progressPercentage` is no longer returned.
