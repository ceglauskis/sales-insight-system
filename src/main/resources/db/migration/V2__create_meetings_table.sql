CREATE TABLE meetings (
                          id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                          title           VARCHAR(255)    NOT NULL,
                          video_url       VARCHAR(500)    NOT NULL,
                          transcription   TEXT,
                          status          VARCHAR(50)     NOT NULL,
                          owner_id        UUID            NOT NULL,
                          created_at      TIMESTAMP       NOT NULL,
                          updated_at      TIMESTAMP       NOT NULL,

                          CONSTRAINT pk_meetings PRIMARY KEY (id),
                          CONSTRAINT fk_meetings_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);