-- Migration: Add profile image column to users table
-- Description: Stores user profile images as BLOB (LONGBLOB for MySQL to support large images)

ALTER TABLE users ADD COLUMN profile_image LONGBLOB;
