CREATE TABLE users
(
    id      UUID PRIMARY KEY    DEFAULT uuid_generate_v4(),
    username    VARCHAR(50) NOT NULL,
    email       VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR NOT NULL,
    enabled     BOOLEAN NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT into users (username, email, password, enabled) values (trim('slavko'), trim('user@gmail.com'),'password',true);

CREATE TABLE authorities
(
    user_id     UUID REFERENCES users (id) NOT NULL,
    username    VARCHAR(50) NOT NULL,
    authority   VARCHAR(50) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT into authorities (user_id,username,authority) values ((select id from users where username = 'slavko'),trim('slavko'), trim('USER'));