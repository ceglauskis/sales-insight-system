CREATE TABLE insights (
                          id           UUID        NOT NULL DEFAULT gen_random_uuid(),
                          meeting_id   UUID        NOT NULL,
                          summary      TEXT        NOT NULL,
                          sentiment    VARCHAR(50) NOT NULL,
                          generated_at TIMESTAMP   NOT NULL,

                          CONSTRAINT pk_insights PRIMARY KEY (id),
                          CONSTRAINT fk_insights_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id)
);

CREATE TABLE insight_action_points (
                                       insight_id   UUID            NOT NULL,
                                       action_point VARCHAR(1000)   NOT NULL,

                                       CONSTRAINT fk_action_points_insight FOREIGN KEY (insight_id) REFERENCES insights(id)
);

CREATE TABLE insight_next_steps (
                                    insight_id UUID            NOT NULL,
                                    next_step  VARCHAR(1000)   NOT NULL,

                                    CONSTRAINT fk_next_steps_insight FOREIGN KEY (insight_id) REFERENCES insights(id)
);