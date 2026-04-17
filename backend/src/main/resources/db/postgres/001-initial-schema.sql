--liquibase formatted sql
--changeset mkrasikoff:001-initial-schema
CREATE TABLE IF NOT EXISTS quotes (
                                      id      BIGSERIAL PRIMARY KEY,
                                      added   BIGINT,
                                      author  VARCHAR(255),
    fav     BOOLEAN NOT NULL DEFAULT FALSE,
    source  VARCHAR(255),
    tags    VARCHAR(255),
    text    VARCHAR(3000) NOT NULL
    );