-- ============================================================
-- Smart Classroom Management System – Database Schema
-- Database: smart_classroom_db
-- Run this file FIRST before seed.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_classroom_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_classroom_db;

-- ============================================================
-- 1. USERS  (authentication + role)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','FACULTY','STUDENT') NOT NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_username (username),
    UNIQUE KEY uq_email    (email)
) ENGINE=InnoDB;

-- ============================================================
-- 2. CLASSES  (academic groups / classrooms)
-- ============================================================
CREATE TABLE IF NOT EXISTS classes (
    class_id      INT         NOT NULL AUTO_INCREMENT,
    class_name    VARCHAR(100) NOT NULL,
    department    VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20)  NOT NULL,

    PRIMARY KEY (class_id)
) ENGINE=InnoDB;

-- ============================================================
-- 3. FACULTY  (faculty profile linked to users)
-- ============================================================
CREATE TABLE IF NOT EXISTS faculty (
    faculty_id  INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    employee_id VARCHAR(20)  UNIQUE,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    department  VARCHAR(100),

    PRIMARY KEY (faculty_id),
    UNIQUE KEY uq_faculty_user (user_id),
    CONSTRAINT fk_faculty_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 4. STUDENTS  (student profile linked to users + classes)
-- ============================================================
CREATE TABLE IF NOT EXISTS students (
    student_id      INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    register_number VARCHAR(20)  UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    class_id        INT,
    enrollment_date DATE,

    PRIMARY KEY (student_id),
    UNIQUE KEY uq_student_user (user_id),
    CONSTRAINT fk_student_user  FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id)
        REFERENCES classes (class_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- 5. SUBJECTS  (linked to a class, taught by a faculty member)
-- ============================================================
CREATE TABLE IF NOT EXISTS subjects (
    subject_id   INT         NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20)  NOT NULL,
    class_id     INT          NOT NULL,
    faculty_id   INT,

    PRIMARY KEY (subject_id),
    UNIQUE KEY uq_subject_code (subject_code),
    CONSTRAINT fk_subject_class   FOREIGN KEY (class_id)
        REFERENCES classes  (class_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_subject_faculty FOREIGN KEY (faculty_id)
        REFERENCES faculty  (faculty_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- 6. ATTENDANCE
--    Unique constraint prevents duplicate records per
--    (student, subject, date).
-- ============================================================
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id   INT  NOT NULL AUTO_INCREMENT,
    student_id      INT  NOT NULL,
    subject_id      INT  NOT NULL,
    attendance_date DATE NOT NULL,
    status          ENUM('PRESENT','ABSENT') NOT NULL,

    PRIMARY KEY (attendance_id),
    UNIQUE KEY uq_attendance (student_id, subject_id, attendance_date),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id)
        REFERENCES students (student_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_att_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (subject_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 7. ASSIGNMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id INT           NOT NULL AUTO_INCREMENT,
    title         VARCHAR(200)  NOT NULL,
    description   TEXT,
    subject_id    INT           NOT NULL,
    faculty_id    INT           NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deadline      DATETIME      NOT NULL,
    max_marks     DECIMAL(6,2)  NOT NULL DEFAULT 100.00,

    PRIMARY KEY (assignment_id),
    CONSTRAINT fk_asgn_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (subject_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_asgn_faculty FOREIGN KEY (faculty_id)
        REFERENCES faculty  (faculty_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 8. SUBMISSIONS
--    Unique constraint supports one active submission per
--    (student, assignment). evaluation_status tracks grading.
-- ============================================================
CREATE TABLE IF NOT EXISTS submissions (
    submission_id     INT           NOT NULL AUTO_INCREMENT,
    assignment_id     INT           NOT NULL,
    student_id        INT           NOT NULL,
    file_path         VARCHAR(500),
    submitted_at      DATETIME,
    marks             DECIMAL(6,2),
    feedback          TEXT,
    evaluation_status ENUM('SUBMITTED','EVALUATED') DEFAULT 'SUBMITTED',

    PRIMARY KEY (submission_id),
    UNIQUE KEY uq_submission (assignment_id, student_id),
    CONSTRAINT fk_sub_assignment FOREIGN KEY (assignment_id)
        REFERENCES assignments (assignment_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_sub_student FOREIGN KEY (student_id)
        REFERENCES students (student_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- END OF SCHEMA
-- ============================================================

