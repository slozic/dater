ALTER TABLE dates
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE date_attendees
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ON_WAITLIST';

UPDATE date_attendees
SET status = CASE
    WHEN soft_deleted = TRUE THEN 'REJECTED'
    WHEN accepted = TRUE THEN 'ACCEPTED'
    ELSE 'ON_WAITLIST'
END;

ALTER TABLE date_attendees
    DROP COLUMN accepted,
    DROP COLUMN soft_deleted;
