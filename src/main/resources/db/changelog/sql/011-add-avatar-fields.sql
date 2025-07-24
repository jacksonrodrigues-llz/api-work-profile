-- liquibase formatted sql

-- changeset system:add-avatar-fields-to-users
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;