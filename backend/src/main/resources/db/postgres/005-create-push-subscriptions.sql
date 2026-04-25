--liquibase formatted sql
--changeset mkrasikoff:005-create-push-subscriptions.sql

CREATE TABLE push_subscriptions
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    endpoint       TEXT         NOT NULL UNIQUE,
    p256dh         VARCHAR(255) NOT NULL,
    auth           VARCHAR(100) NOT NULL,
    interval_hours INT          NOT NULL DEFAULT 24,
    last_sent      BIGINT       NOT NULL DEFAULT 0,
    subscribed_at  BIGINT       NOT NULL
);

CREATE INDEX idx_push_subs_user ON push_subscriptions (user_id);
