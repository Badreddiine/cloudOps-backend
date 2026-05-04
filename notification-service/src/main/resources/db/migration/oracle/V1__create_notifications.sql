-- V1__create_notifications_table.sql

CREATE TABLE notifications (
                               id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY NOT NULL,
                               user_id     VARCHAR2(255)                              NOT NULL,
                               type        VARCHAR2(100)                              NOT NULL,
                               message     CLOB                                       NOT NULL,
                               incident_id NUMBER(19),
                               is_read     NUMBER(1)     DEFAULT 0                    NOT NULL,
                               created_at  TIMESTAMP                                  NOT NULL,
                               CONSTRAINT pk_notifications PRIMARY KEY (id),
                               CONSTRAINT chk_notif_read   CHECK (is_read IN (0, 1))
);

CREATE INDEX idx_notif_user_id ON notifications (user_id);
CREATE INDEX idx_notif_is_read ON notifications (is_read);