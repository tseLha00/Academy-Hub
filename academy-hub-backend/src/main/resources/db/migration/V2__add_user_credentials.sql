ALTER TABLE users
    ADD COLUMN username VARCHAR(80),
    ADD COLUMN password_hash VARCHAR(255);

CREATE UNIQUE INDEX uq_users_username
    ON users (username)
    WHERE username IS NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT chk_users_credentials_paired
        CHECK (
            (username IS NULL AND password_hash IS NULL)
            OR
            (username IS NOT NULL AND length(trim(username)) > 0
                AND password_hash IS NOT NULL AND length(trim(password_hash)) > 0)
        );
