ALTER TABLE training_assignments
    ADD COLUMN progress_percentage INTEGER NOT NULL DEFAULT 0;

-- Existing completed assignments must remain consistent with 100% progress.
UPDATE training_assignments
SET progress_percentage = 100
WHERE completed_at IS NOT NULL;

ALTER TABLE training_assignments
    ADD CONSTRAINT chk_training_assignments_progress_percentage
        CHECK (progress_percentage BETWEEN 0 AND 100);
