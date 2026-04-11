CREATE TABLE user_blocks
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    blocked_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT user_blocks_no_self CHECK (blocker_id <> blocked_id),
    CONSTRAINT user_blocks_unique UNIQUE (blocker_id, blocked_id)
);

CREATE INDEX idx_user_blocks_blocker_id
    ON user_blocks (blocker_id);

CREATE INDEX idx_user_blocks_blocked_id
    ON user_blocks (blocked_id);

CREATE TABLE user_reports
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reported_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reason      VARCHAR(32) NOT NULL,
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT user_reports_no_self CHECK (reporter_id <> reported_id)
);

CREATE INDEX idx_user_reports_reported_created_at
    ON user_reports (reported_id, created_at DESC);
