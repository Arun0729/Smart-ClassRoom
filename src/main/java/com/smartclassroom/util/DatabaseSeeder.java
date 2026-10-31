package com.smartclassroom.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Standalone utility class for seeding the database with initial test data.
 *
 * <p>Run this class once after creating the schema to populate:
 * <ul>
 *   <li>1 admin account</li>
 *   <li>2 faculty accounts with profiles</li>
 *   <li>3 student accounts with profiles</li>
 *   <li>3 subjects across two classes</li>
 * </ul>
 *
 * <p>All INSERT statements use {@code INSERT IGNORE} so re-running is safe.
 * Passwords are BCrypt-hashed via {@link PasswordUtil}.
 */
public class DatabaseSeeder {

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Main method – runs the full seeding sequence.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  Smart Classroom Management System Seeder");
        System.out.println("============================================");

        // Test connectivity before proceeding
        if (!DBConnection.testConnection()) {
            System.err.println("[FATAL] Cannot connect to the database. Check db.properties.");
            return;
        }
        System.out.println("[OK] Database connection verified.");
        System.out.println();

        // Seed each user type
        seedAdmin();
        seedFaculty1();
        seedFaculty2();
        seedStudent1();
        seedStudent2();
        seedStudent3();
        seedSubjects();

        System.out.println();
        System.out.println("============================================");
        System.out.println("  Seeding complete.");
        System.out.println("============================================");
    }

    // -------------------------------------------------------------------------
    // Admin
    // -------------------------------------------------------------------------

    /** Seeds the admin user account. */
    private static void seedAdmin() {
        System.out.println("[Seeding] Admin user...");
        String hashedPassword = PasswordUtil.hashPassword("Admin@123");

        String userSql = "INSERT IGNORE INTO users (username, email, password_hash, role, is_active) " +
                         "VALUES (?, ?, ?, 'ADMIN', 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(userSql)) {

            ps.setString(1, "admin");
            ps.setString(2, "admin@smartclassroom.com");
            ps.setString(3, hashedPassword);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("  [OK] Admin user created: username=admin, password=Admin@123");
            } else {
                System.out.println("  [SKIP] Admin user already exists.");
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] Failed to seed admin: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Faculty 1
    // -------------------------------------------------------------------------

    /** Seeds Faculty 1 (Dr. Priya Sharma) in users and faculty tables. */
    private static void seedFaculty1() {
        System.out.println("[Seeding] Faculty 1 (Dr. Priya Sharma)...");
        String hashedPassword = PasswordUtil.hashPassword("Faculty@123");

        String userSql = "INSERT IGNORE INTO users (username, email, password_hash, role, is_active) " +
                         "VALUES (?, ?, ?, 'FACULTY', 1)";
        String profileSql = "INSERT IGNORE INTO faculty (user_id, employee_id, full_name, phone, department) " +
                            "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Insert into users
            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "faculty1");
                ps.setString(2, "priya.sharma@smartclassroom.com");
                ps.setString(3, hashedPassword);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                    System.out.println("  [OK] Faculty1 user created (user_id=" + userId + ").");
                } else {
                    System.out.println("  [SKIP] Faculty1 user already exists.");
                    userId = getUserIdByUsername(conn, "faculty1");
                }
            }

            // Insert into faculty profile
            if (userId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, "FAC001");
                    ps.setString(3, "Dr. Priya Sharma");
                    ps.setString(4, "9876543210");
                    ps.setString(5, "Computer Science");
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("  [OK] Faculty1 profile created.");
                    } else {
                        System.out.println("  [SKIP] Faculty1 profile already exists.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] Failed to seed Faculty1: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Faculty 2
    // -------------------------------------------------------------------------

    /** Seeds Faculty 2 (Prof. Ravi Kumar) in users and faculty tables. */
    private static void seedFaculty2() {
        System.out.println("[Seeding] Faculty 2 (Prof. Ravi Kumar)...");
        String hashedPassword = PasswordUtil.hashPassword("Faculty@123");

        String userSql = "INSERT IGNORE INTO users (username, email, password_hash, role, is_active) " +
                         "VALUES (?, ?, ?, 'FACULTY', 1)";
        String profileSql = "INSERT IGNORE INTO faculty (user_id, employee_id, full_name, phone, department) " +
                            "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "faculty2");
                ps.setString(2, "ravi.kumar@smartclassroom.com");
                ps.setString(3, hashedPassword);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                    System.out.println("  [OK] Faculty2 user created (user_id=" + userId + ").");
                } else {
                    System.out.println("  [SKIP] Faculty2 user already exists.");
                    userId = getUserIdByUsername(conn, "faculty2");
                }
            }

            if (userId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, "FAC002");
                    ps.setString(3, "Prof. Ravi Kumar");
                    ps.setString(4, "9876543211");
                    ps.setString(5, "Information Technology");
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("  [OK] Faculty2 profile created.");
                    } else {
                        System.out.println("  [SKIP] Faculty2 profile already exists.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] Failed to seed Faculty2: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Students
    // -------------------------------------------------------------------------

    /** Seeds Student 1 (Arjun Singh) in users and students tables (class_id=1). */
    private static void seedStudent1() {
        System.out.println("[Seeding] Student 1 (Arjun Singh)...");
        seedStudent("student1", "arjun.singh@smartclassroom.com", "Student@123",
                "REG001", "Arjun Singh", "9123456780", 1, "2024-07-15");
    }

    /** Seeds Student 2 (Priya Patel) in users and students tables (class_id=1). */
    private static void seedStudent2() {
        System.out.println("[Seeding] Student 2 (Priya Patel)...");
        seedStudent("student2", "priya.patel@smartclassroom.com", "Student@123",
                "REG002", "Priya Patel", "9123456781", 1, "2024-07-15");
    }

    /** Seeds Student 3 (Rahul Gupta) in users and students tables (class_id=2). */
    private static void seedStudent3() {
        System.out.println("[Seeding] Student 3 (Rahul Gupta)...");
        seedStudent("student3", "rahul.gupta@smartclassroom.com", "Student@123",
                "REG003", "Rahul Gupta", "9123456782", 2, "2024-07-15");
    }

    /**
     * Generic helper that creates a user row and a student profile row.
     *
     * @param username       login username
     * @param email          email address
     * @param plainPassword  plain-text password (will be hashed)
     * @param registerNumber unique register number (e.g. "REG001")
     * @param fullName       student's full name
     * @param phone          10-digit phone number
     * @param classId        the class the student belongs to
     * @param enrollmentDate enrollment date in "YYYY-MM-DD" format
     */
    private static void seedStudent(String username, String email, String plainPassword,
                                    String registerNumber, String fullName, String phone,
                                    int classId, String enrollmentDate) {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        String userSql = "INSERT IGNORE INTO users (username, email, password_hash, role, is_active) " +
                         "VALUES (?, ?, ?, 'STUDENT', 1)";
        String profileSql = "INSERT IGNORE INTO students (user_id, register_number, full_name, phone, class_id, enrollment_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hashedPassword);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                    System.out.println("  [OK] User created: username=" + username +
                            " (user_id=" + userId + ").");
                } else {
                    System.out.println("  [SKIP] User already exists: " + username);
                    userId = getUserIdByUsername(conn, username);
                }
            }

            if (userId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, registerNumber);
                    ps.setString(3, fullName);
                    ps.setString(4, phone);
                    ps.setInt(5, classId);
                    ps.setString(6, enrollmentDate);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("  [OK] Student profile created: " + fullName + " (" + registerNumber + ")");
                    } else {
                        System.out.println("  [SKIP] Student profile already exists: " + fullName);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] Failed to seed student '" + username + "': " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Subjects
    // -------------------------------------------------------------------------

    /** Seeds the initial set of subjects. */
    private static void seedSubjects() {
        System.out.println("[Seeding] Subjects...");

        // Retrieve the faculty_id values based on user_id lookup
        int facultyId1 = getFacultyId("faculty1");
        int facultyId2 = getFacultyId("faculty2");

        if (facultyId1 < 0) {
            System.err.println("  [ERROR] Could not find faculty_id for faculty1. " +
                               "Ensure faculty1 was seeded first.");
        }
        if (facultyId2 < 0) {
            System.err.println("  [ERROR] Could not find faculty_id for faculty2. " +
                               "Ensure faculty2 was seeded first.");
        }

        String subjectSql = "INSERT IGNORE INTO subjects (subject_name, subject_code, class_id, faculty_id) " +
                            "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(subjectSql)) {

            // Subjects for class_id=1, faculty_id=1 (facultyId1)
            if (facultyId1 > 0) {
                // Data Structures
                ps.setString(1, "Data Structures");
                ps.setString(2, "CS101");
                ps.setInt(3, 1);
                ps.setInt(4, facultyId1);
                int r1 = ps.executeUpdate();
                System.out.println(r1 > 0
                        ? "  [OK] Subject created: Data Structures (CS101)"
                        : "  [SKIP] Subject already exists: Data Structures");

                // Database Management
                ps.setString(1, "Database Management");
                ps.setString(2, "CS102");
                ps.setInt(3, 1);
                ps.setInt(4, facultyId1);
                int r2 = ps.executeUpdate();
                System.out.println(r2 > 0
                        ? "  [OK] Subject created: Database Management (CS102)"
                        : "  [SKIP] Subject already exists: Database Management");
            }

            // Subject for class_id=2, faculty_id=2 (facultyId2)
            if (facultyId2 > 0) {
                // Web Technologies
                ps.setString(1, "Web Technologies");
                ps.setString(2, "IT101");
                ps.setInt(3, 2);
                ps.setInt(4, facultyId2);
                int r3 = ps.executeUpdate();
                System.out.println(r3 > 0
                        ? "  [OK] Subject created: Web Technologies (IT101)"
                        : "  [SKIP] Subject already exists: Web Technologies");
            }

        } catch (SQLException e) {
            System.err.println("  [ERROR] Failed to seed subjects: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Retrieves the {@code user_id} for a given username using an existing connection.
     *
     * @param conn     an open {@link Connection}
     * @param username the username to look up
     * @return the user_id, or -1 if not found
     */
    private static int getUserIdByUsername(Connection conn, String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] getUserIdByUsername failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Looks up the {@code faculty_id} from the faculty profile table using
     * the faculty's username (via a JOIN with users).
     *
     * @param username the faculty username to look up
     * @return the faculty_id, or -1 if not found
     */
    private static int getFacultyId(String username) {
        String sql = "SELECT f.faculty_id FROM faculty f " +
                     "JOIN users u ON f.user_id = u.user_id " +
                     "WHERE u.username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("faculty_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] getFacultyId failed for '" + username + "': " + e.getMessage());
        }
        return -1;
    }
}
