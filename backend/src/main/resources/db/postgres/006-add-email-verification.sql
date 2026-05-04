--liquibase formatted sql
--changeset mkrasikoff:006-add-email-verification

-- Add email verification flag to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE;

-- Mark all existing users as already verified (they registered before this feature)
UPDATE users SET email_verified = TRUE;

-- New local registrations start as unverified
-- (set to FALSE in application logic, not here)

-- Table to store pending verification codes
CREATE TABLE IF NOT EXISTS email_verifications
(
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    code       VARCHAR(6)   NOT NULL,
    expires_at BIGINT       NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at BIGINT       NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_email_verifications_email ON email_verifications(email);
