CREATE TABLE users (
                       id          UUID            NOT NULL DEFAULT gen_random_uuid(),
                       name        VARCHAR(255)    NOT NULL,
                       email       VARCHAR(255)    NOT NULL,
                       password_hash VARCHAR(255)  NOT NULL,
                       role        VARCHAR(50)     NOT NULL,
                       created_at  TIMESTAMP       NOT NULL,

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email)
);