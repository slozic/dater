CREATE TABLE dates
(
    id      UUID PRIMARY KEY    DEFAULT uuid_generate_v4(),
    description TEXT NOT NULL,
    location    VARCHAR(250) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT false,
    scheduled_time  TIMESTAMPTZ NOT NULL,
    created_by  UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE date_attendees
(
    attendee_id  UUID REFERENCES users (id) NOT NULL,
    date_id  UUID REFERENCES dates (id) ON DELETE CASCADE NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    constraint date_attendee_unique unique (attendee_id, date_id)
);
