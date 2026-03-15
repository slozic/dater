ALTER TABLE users
    ADD COLUMN IF NOT EXISTS attendee_accepted_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS date_request_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS chat_message_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
