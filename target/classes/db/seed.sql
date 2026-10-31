-- ============================================================
-- Smart Classroom Management System – Seed Data
-- Run AFTER schema.sql.
-- NOTE: User accounts (admin, faculty, students) are created
--       by running DatabaseSeeder.java (see README.md).
--       This file only seeds academic structure data.
-- ============================================================

USE smart_classroom_db;

-- ============================================================
-- Sample Classes
-- ============================================================
INSERT INTO classes (class_name, department, academic_year) VALUES
    ('CS-A 2024',  'Computer Science', '2024-2025'),
    ('CS-B 2024',  'Computer Science', '2024-2025'),
    ('IT-A 2024',  'Information Technology', '2024-2025'),
    ('EC-A 2024',  'Electronics',       '2024-2025');

-- ============================================================
-- Sample Subjects  (faculty_id filled after DatabaseSeeder runs)
-- These are inserted by DatabaseSeeder along with faculty.
-- This file intentionally leaves subjects empty to avoid
-- FK conflicts when faculty rows do not exist yet.
-- ============================================================

-- ============================================================
-- END OF SEED DATA
-- ============================================================

