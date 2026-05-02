CREATE TABLE incidents (
                           id BIGSERIAL PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT,
                           status VARCHAR(50) NOT NULL,
                           priority VARCHAR(50) NOT NULL,
                           category VARCHAR(50),
                           assigned_to VARCHAR(255),
                           reported_by VARCHAR(255) NOT NULL,
                           sla_deadline TIMESTAMP,
                           resolved_at TIMESTAMP,
                           sla_breached BOOLEAN DEFAULT FALSE,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP
);
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            incident_id BIGINT NOT NULL,
                            action VARCHAR(255) NOT NULL,
                            old_value TEXT,
                            new_value TEXT,
                            performed_by VARCHAR(255) NOT NULL,
                            timestamp TIMESTAMP NOT NULL,
                            FOREIGN KEY (incident_id) REFERENCES incidents(id)
);