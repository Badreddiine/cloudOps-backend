-- V1__create_notifications_table.sql

CREATE TABLE notifications (
                               id          BIGINT       GENERATED ALWAYS AS IDENTITY NOT NULL,
                               user_id     VARCHAR(255)                              NOT NULL,
                               type        VARCHAR(100)                              NOT NULL,
                               message     TEXT                                      NOT NULL,
                               incident_id BIGINT,
                               is_read     BOOLEAN      DEFAULT FALSE                NOT NULL,
                               created_at  TIMESTAMP                                 NOT NULL,
                               CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE INDEX idx_notif_user_id ON notifications (user_id);
CREATE INDEX idx_notif_is_read ON notifications (is_read);
