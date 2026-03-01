CREATE TABLE chat_messages
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date_id      UUID REFERENCES dates (id) ON DELETE CASCADE NOT NULL,
    sender_id    UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    recipient_id UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    message      TEXT                                        NOT NULL,
    created_at   TIMESTAMPTZ                                 NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_messages_date_created_at
    ON chat_messages (date_id, created_at DESC);
