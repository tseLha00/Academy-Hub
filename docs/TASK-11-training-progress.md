# TASK-11 — Per-training progress

## Endpoint

`PUT /api/trainings/{trainingId}/progress`

Requires an authenticated user with the `LEARNER` role. The learner identity is taken from the authenticated principal; no learner ID is accepted from the request.

Request body:

```json
{
  "progressPercentage": 65
}
```

The value must be an integer from 0 to 100. The endpoint returns `204 No Content` on success and `404 Not Found` when the training is not assigned to the authenticated learner. A completed assignment cannot be moved backwards; attempting to reduce it returns `409 Conflict`.

## Data model

`training_assignments.progress_percentage` stores progress for each learner/training assignment. New assignments start at 0%. Existing completed assignments are migrated to 100%.

Setting progress to 100% also records `completed_at`. The existing completion endpoint likewise sets progress to 100%. Completion is terminal in this MVP: a completed assignment cannot be changed to a lower percentage.

The learner training response includes `progressPercentage`.
