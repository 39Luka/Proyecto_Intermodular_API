-- Migration: Add image column to products table
-- Description: Stores product images as BLOB (LONGBLOB for MySQL to support large images)

ALTER TABLE products ADD COLUMN image LONGBLOB;
