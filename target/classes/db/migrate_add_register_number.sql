-- ============================================================
-- Migration: Add register_number to existing students table
-- Run this ONLY if you already have the smart_classroom_db
-- created from the original schema (without register_number).
-- Safe to run multiple times (uses IF NOT EXISTS logic).
-- ============================================================

USE smart_classroom_db;

-- Add register_number column if it does not already exist
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_classroom_db'
      AND TABLE_NAME   = 'students'
      AND COLUMN_NAME  = 'register_number'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE students ADD COLUMN register_number VARCHAR(20) UNIQUE AFTER user_id',
    'SELECT ''register_number column already exists -- skipped'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

