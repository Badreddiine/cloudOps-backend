-- V1__init.sql

CREATE TABLE incidents (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT NOT NULL,
                           status VARCHAR(50) NOT NULL,
                           priority VARCHAR(50) NOT NULL,
                           category VARCHAR(50),
                           assigned_to VARCHAR(255),
                           reported_by VARCHAR(255),
                           sla_deadline TIMESTAMP,
                           resolved_at TIMESTAMP,
                           sla_breached BOOLEAN DEFAULT FALSE NOT NULL,
                           created_at TIMESTAMP,
                           updated_at TIMESTAMP,
                           last_modified_by_user_id VARCHAR(255)
);

CREATE TABLE audit_logs (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            incident_id BIGINT NOT NULL,
                            action VARCHAR(255) NOT NULL,
                            old_value TEXT,
                            new_value TEXT,
                            performed_by VARCHAR(255),
                            timestamp TIMESTAMP,
                            CONSTRAINT fk_audit_incident FOREIGN KEY (incident_id) REFERENCES incidents(id)
);
