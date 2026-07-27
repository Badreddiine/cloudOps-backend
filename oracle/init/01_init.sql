-- ================================================================
-- 01_init.sql — Init PostgreSQL
-- ================================================================

-- ── User lecture seule pour Reporting Service (Sprint 3) ─────────
CREATE USER incidents_ro WITH PASSWORD 'ROIncidents2025';

GRANT CONNECT ON DATABASE cloudops TO incidents_ro;
GRANT USAGE ON SCHEMA public TO incidents_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO incidents_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO incidents_ro;
