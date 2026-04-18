--liquibase formatted sql
--changeset mkrasikoff:004-add-user-id-to-quotes

ALTER TABLE quotes
    ADD COLUMN user_id BIGINT REFERENCES users(id);
