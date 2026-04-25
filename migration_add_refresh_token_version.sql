-- Migration: Add refresh token version to users table
-- Description: Supports refresh token rotation by invalidating previously used refresh tokens

ALTER TABLE users ADD COLUMN refresh_token_version BIGINT NOT NULL DEFAULT 0;
