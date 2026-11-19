ALTER TABLE users
    ADD COLUMN email VARCHAR(254),
    ADD COLUMN location VARCHAR(100),
    ADD COLUMN profession VARCHAR(150),
    ADD COLUMN apprenticeship_year INTEGER;

ALTER TABLE users
    ADD CONSTRAINT chk_users_email_not_blank
        CHECK (email IS NULL OR LENGTH(TRIM(email)) > 0),
    ADD CONSTRAINT chk_users_location_not_blank
        CHECK (location IS NULL OR LENGTH(TRIM(location)) > 0),
    ADD CONSTRAINT chk_users_profession_not_blank
        CHECK (profession IS NULL OR LENGTH(TRIM(profession)) > 0),
    ADD CONSTRAINT chk_users_apprenticeship_year_positive
        CHECK (apprenticeship_year IS NULL OR apprenticeship_year > 0);