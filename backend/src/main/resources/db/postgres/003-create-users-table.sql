--liquibase formatted sql
--changeset mkrasikoff:003-create-users-table

CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),
    provider    VARCHAR(50)  NOT NULL DEFAULT 'local',
    provider_id VARCHAR(255),
    created_at  BIGINT
    );
