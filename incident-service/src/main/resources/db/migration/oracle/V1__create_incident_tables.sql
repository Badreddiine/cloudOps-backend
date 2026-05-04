-- V1__init.sql

CREATE TABLE incidents (
                           id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           title VARCHAR2(255) NOT NULL,
                           description CLOB NOT NULL,
                           status VARCHAR2(50) NOT NULL,
                           priority VARCHAR2(50) NOT NULL,
                           category VARCHAR2(50),
                           assigned_to VARCHAR2(255),
                           reported_by VARCHAR2(255),
                           sla_deadline TIMESTAMP,
                           resolved_at TIMESTAMP,
                           sla_breached NUMBER(1) DEFAULT 0 NOT NULL,
                           created_at TIMESTAMP,
                           updated_at TIMESTAMP,
                           last_modified_by_user_id VARCHAR2(255)
);

CREATE TABLE audit_logs (
                            id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            incident_id NUMBER(19) NOT NULL,
                            action VARCHAR2(255) NOT NULL,
                            old_value CLOB,
                            new_value CLOB,
                            performed_by VARCHAR2(255),
                            timestamp TIMESTAMP,
                            CONSTRAINT fk_audit_incident FOREIGN KEY (incident_id) REFERENCES incidents(id)
);