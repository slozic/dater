CREATE TABLE notifications
(
    id           UUID PRIMARY KEY                  DEFAULT uuid_generate_v4(),
    user_id      UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type         VARCHAR(64)              NOT NULL,
    title        VARCHAR(255)             NOT NULL,
    body         TEXT                     NOT NULL,
    related_date_id UUID                           REFERENCES dates (id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ              NOT NULL DEFAULT now(),
    read_at      TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_created_at
    ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON notifications (user_id, read_at);
