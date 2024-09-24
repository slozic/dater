CREATE TABLE user_images
(
    id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    image_path    VARCHAR(500) NOT NULL,
    image_size    INTEGER NOT NULL,
    user_id  UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
