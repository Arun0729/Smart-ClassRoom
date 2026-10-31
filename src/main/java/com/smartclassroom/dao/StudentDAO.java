package com.smartclassroom.dao;

import com.smartclassroom.model.Student;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@code students} and their associated {@code users} rows.
 * All persistence operations use {@link DBConnection} and {@link PreparedStatement}.
 */
public class StudentDAO {

    // -------------------------------------------------------------------------
    // SQL fragments shared by multiple query methods
    // -------------------------------------------------------------------------

    private static final String SELECT_JOIN =
            "SELECT s.student_id, s.user_id, s.register_number, s.full_name, s.phone, s.class_id, s.enrollment_date, "
          + "       u.username, u.email, u.password_hash, u.is_active, "
          + "       c.class_name "
          + "FROM students s "
          + "JOIN users u ON s.user_id = u.user_id "
          + "LEFT JOIN classes c ON s.class_id = c.class_id ";

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a fully populated {@link Student}.
     */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();

        // User fields
        student.setUserId(rs.getInt("user_id"));
        student.setUsername(rs.getString("username"));
        student.setEmail(rs.getString("email"));
        student.setPasswordHash(rs.getString("password_hash"));
        student.setRole("STUDENT");
        student.setActive(rs.getBoolean("is_active"));

        // Student-specific fields
        student.setStudentId(rs.getInt("student_id"));
        student.setRegisterNumber(rs.getString("register_number"));
        student.setFullName(rs.getString("full_name"));
        student.setPhone(rs.getString("phone"));
        student.setClassId(rs.getInt("class_id"));
        student.setEnrollmentDate(rs.getString("enrollment_date"));

        // Display field (may be null for students without a class)
        student.setClassName(rs.getString("class_name"));

        return student;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new row into the {@code users} table for the given student,
     * returning the auto-generated {@code user_id}.
     *
     * @param student the Student whose user credentials will be saved
     * @return generated {@code user_id}, or {@code -1} on failure
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int saveUser(Student student) {
        final String sql = "INSERT INTO users (username, email, password_hash, role, is_active) "
                         + "VALUES (?, ?, ?, 'STUDENT', 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, student.getUsername());
            pst.setString(2, student.getEmail());
            pst.setString(3, student.getPasswordHash());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.saveUser failed for username='" + student.getUsername() + "': " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Inserts a new row into the {@code students} table linking the profile
     * data to the given {@code userId}.
     *
     * @param student the student profile to persist
     * @param userId  the {@code user_id} returned from {@link #saveUser(Student)}
     * @return {@code true} if the insert succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean saveProfile(Student student, int userId) {
        final String sql = "INSERT INTO students (user_id, register_number, full_name, phone, class_id, enrollment_date) "
                         + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setString(2, student.getRegisterNumber());
            pst.setString(3, student.getFullName());
            pst.setString(4, student.getPhone());
            pst.setInt(5, student.getClassId());
            pst.setString(6, student.getEnrollmentDate());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.saveProfile failed for userId=" + userId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all students joined with their user and class information.
     *
     * @return list of all {@link Student} objects, ordered by full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Student> findAll() {
        final String sql = SELECT_JOIN + "ORDER BY s.full_name";

        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.findAll failed: " + e.getMessage(), e);
        }
        return students;
    }

    /**
     * Finds a single student by their {@code student_id}.
     *
     * @param studentId the primary key in the {@code students} table
     * @return the matching {@link Student}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Student findById(int studentId) {
        final String sql = SELECT_JOIN + "WHERE s.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.findById failed for studentId=" + studentId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a single student by their {@code register_number}.
     *
     * @param registerNumber the unique register number
     * @return the matching {@link Student}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Student findByRegisterNumber(String registerNumber) {
        final String sql = SELECT_JOIN + "WHERE s.register_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, registerNumber);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.findByRegisterNumber failed for registerNumber='" + registerNumber + "': " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a student by their {@code user_id} (FK into the {@code users} table).
     *
     * @param userId the user_id of the student
     * @return the matching {@link Student}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Student findByUserId(int userId) {
        final String sql = SELECT_JOIN + "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.findByUserId failed for userId=" + userId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all students enrolled in the specified class.
     *
     * @param classId the target {@code class_id}
     * @return list of matching students, ordered by full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Student> findByClassId(int classId) {
        final String sql = SELECT_JOIN + "WHERE s.class_id = ? ORDER BY s.full_name";

        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, classId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.findByClassId failed for classId=" + classId + ": " + e.getMessage(), e);
        }
        return students;
    }

    /**
     * Updates a student's profile and associated user credentials within a
     * single transaction. Rolls back both statements if either fails.
     *
     * @param student the student with updated values; must have valid
     *                {@code studentId} and {@code userId}
     * @return {@code true} if both updates succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error or rollback failure
     */
    public boolean update(Student student) {
        final String updateStudent = "UPDATE students SET register_number = ?, full_name = ?, phone = ?, class_id = ? "
                                   + "WHERE student_id = ?";
        final String updateUser    = "UPDATE users SET username = ?, email = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            boolean studentUpdated;
            try (PreparedStatement pst = conn.prepareStatement(updateStudent)) {
                pst.setString(1, student.getRegisterNumber());
                pst.setString(2, student.getFullName());
                pst.setString(3, student.getPhone());
                pst.setInt(4, student.getClassId());
                pst.setInt(5, student.getStudentId());
                studentUpdated = pst.executeUpdate() > 0;
            }

            boolean userUpdated;
            try (PreparedStatement pst = conn.prepareStatement(updateUser)) {
                pst.setString(1, student.getUsername());
                pst.setString(2, student.getEmail());
                pst.setInt(3, student.getUserId());
                userUpdated = pst.executeUpdate() > 0;
            }

            conn.commit();
            return studentUpdated && userUpdated;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("StudentDAO.update rollback failed: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw new RuntimeException("StudentDAO.update failed for studentId=" + student.getStudentId() + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Log and swallow – data was already committed or rolled back
                }
            }
        }
    }

    /**
     * Soft-deletes a student by setting {@code is_active = 0} in the users table.
     *
     * @param userId the user_id of the student to deactivate
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean deactivate(int userId) {
        final String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.deactivate failed for userId=" + userId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Full-text search across student name, username, register number, and student ID.
     *
     * @param query the search term (will be wrapped with {@code %…%} wildcards)
     * @return list of matching students
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Student> search(String query) {
        final String sql = SELECT_JOIN
                + "WHERE s.full_name LIKE ? OR u.username LIKE ? OR CAST(s.student_id AS CHAR) LIKE ? "
                + "OR s.register_number LIKE ? "
                + "ORDER BY s.full_name";

        String param = "%" + query + "%";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, param);
            pst.setString(2, param);
            pst.setString(3, param);
            pst.setString(4, param);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.search failed for query='" + query + "': " + e.getMessage(), e);
        }
        return students;
    }

    /**
     * Returns the total number of active student records in the database.
     * Deactivated students (is_active = 0) are excluded from the count.
     *
     * @return total active student count
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int count() {
        final String sql = "SELECT COUNT(*) FROM students s "
                         + "JOIN users u ON s.user_id = u.user_id "
                         + "WHERE u.is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("StudentDAO.count failed: " + e.getMessage(), e);
        }
        return 0;
    }
}
