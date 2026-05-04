-- ================================================================
-- 01_init.sql — Init Oracle XE corrigé
-- Fix ORA-00990 : CREATE TABLE/SEQUENCE/INDEX inclus dans RESOURCE
-- ================================================================

ALTER SESSION SET CONTAINER = XEPDB1;

-- ── User Keycloak ────────────────────────────────────────────────
CREATE USER kc_app IDENTIFIED BY "AppKc2025"
  DEFAULT TABLESPACE USERS
  QUOTA 200M ON USERS;

GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO kc_app;

-- ── Droits DDL pour incidents_app ────────────────────────────────
-- RESOURCE inclut déjà : CREATE TABLE, CREATE SEQUENCE, CREATE INDEX
-- CREATE VIEW, CREATE PROCEDURE, CREATE TRIGGER
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO incidents_app;

-- ── User lecture seule pour Reporting Service (Sprint 3) ─────────
CREATE USER incidents_ro IDENTIFIED BY "ROIncidents2025"
  DEFAULT TABLESPACE USERS
  QUOTA 0 ON USERS;

GRANT CREATE SESSION TO incidents_ro;

COMMIT;