ALTER TABLE users
    ADD COLUMN first_name VARCHAR(75),
    ADD COLUMN last_name VARCHAR(75);

-- Use the first word as the first name and the remaining words as the last name.
UPDATE users
SET
    first_name = split_part(trim(display_name), ' ', 1),
    last_name = NULLIF(
            trim(substring(trim(display_name) FROM position(' ' IN trim(display_name)) + 1)),
            ''
                );

-- Make both columns mandatory after migrating existing users.
ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE users
DROP COLUMN display_name;