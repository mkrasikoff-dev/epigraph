--liquibase formatted sql
--changeset mkrasikoff:007-add-cascade-delete-user

-- Re-add FK with CASCADE so deleting a user removes all their quotes and push subscriptions
ALTER TABLE quotes
DROP CONSTRAINT IF EXISTS quotes_user_id_fkey,
    ADD CONSTRAINT quotes_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE push_subscriptions
DROP CONSTRAINT IF EXISTS push_subscriptions_user_id_fkey,
    ADD CONSTRAINT push_subscriptions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
