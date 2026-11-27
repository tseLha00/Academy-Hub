ALTER TABLE training_assignments
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    ADD COLUMN start_date DATE,
    ADD COLUMN estimated_completion_date DATE;

-- Derive the status of existing assignments from their previous progress.
-- Existing assignments keep empty dates; the assigned learner provides them afterward.
UPDATE training_assignments
SET status = CASE
                 WHEN completed_at IS NOT NULL THEN 'COMPLETED'
                 WHEN progress_percentage > 0 THEN 'IN_PROGRESS'
                 ELSE 'TODO'
    END;

ALTER TABLE training_assignments
    DROP COLUMN progress_percentage;

ALTER TABLE training_assignments
    ADD CONSTRAINT chk_training_assignments_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'BLOCKED', 'COMPLETED')),
    ADD CONSTRAINT chk_training_assignments_completed_at
        CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL)),
    ADD CONSTRAINT chk_training_assignments_dates
        CHECK (start_date IS NULL
            OR estimated_completion_date IS NULL
            OR estimated_completion_date >= start_date);
