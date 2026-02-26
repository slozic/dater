ALTER TABLE users
    ADD COLUMN date_list_gender_filter VARCHAR(16) NOT NULL DEFAULT 'ALL';

UPDATE users
SET date_list_gender_filter = 'ALL'
WHERE date_list_gender_filter IS NULL;
