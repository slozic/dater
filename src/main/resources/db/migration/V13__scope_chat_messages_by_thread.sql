ALTER TABLE chat_messages
    ADD COLUMN participant_user_id UUID;

UPDATE chat_messages cm
SET participant_user_id = CASE
                              WHEN cm.sender_id = d.created_by THEN cm.recipient_id
                              ELSE cm.sender_id
    END
FROM dates d
WHERE cm.date_id = d.id
  AND cm.participant_user_id IS NULL;

ALTER TABLE chat_messages
    ALTER COLUMN participant_user_id SET NOT NULL;

ALTER TABLE chat_messages
    ADD CONSTRAINT fk_chat_messages_participant_user
        FOREIGN KEY (participant_user_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_chat_messages_date_thread_created_at
    ON chat_messages (date_id, participant_user_id, created_at DESC);
