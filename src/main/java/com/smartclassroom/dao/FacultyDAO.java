package com.smartclassroom.dao;

import com.smartclassroom.model.Faculty;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@code faculty} and their associated {@code users} rows.
 * All persistence operations use {@link DBConnection} and {@link PreparedStatement}.
 */
public class FacultyDAO {

    // -------------------------------------------------------------------------
    // SQL fragments shared by multiple query methods
    // -------------------------------------------------------------------------

    private static final String SELECT_JOIN =
            "SELECT f.faculty_id, f.user_id, f.employee_id, f.full_name, f.phone, f.department, "
          + "       u.username, u.email, u.password_hash, u.is_active "
          + "FROM faculty f "
          + "JOIN users u ON f.user_id = u.user_id ";

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a fully populated {@link Faculty}.
     */
    private Faculty mapRow(ResultSet rs) throws SQLException {
        Faculty faculty = new Faculty();

        // User fields
        faculty.setUserId(rs.getInt("user_id"));
        faculty.setUsername(rs.getString("username"));
        faculty.setEmail(rs.getString("email"));
        faculty.setPasswordHash(rs.getString("password_hash"));
        faculty.setRole("FACULTY");
        faculty.setActive(rs.getBoolean("is_active"));

        // Faculty-specific fields
        faculty.setFacultyId(rs.getInt("faculty_id"));
        faculty.setEmployeeId(rs.getString("employee_id"));
        faculty.setFullName(rs.getString("full_name"));
        faculty.setPhone(rs.getString("phone"));
        faculty.setDepartment(rs.getString("department"));

        return faculty;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new row into the {@code users} table for the given faculty member,
     * returning the auto-generated {@code user_id}.
     *
     * @param faculty the Faculty whose user credentials will be saved
     * @return generated {@code user_id}, or {@code -1} on failure
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int saveUser(Faculty faculty) {
        final String sql = "INSERT INTO users (username, email, password_hash, role, is_active) "
                         + "VALUES (?, ?, ?, 'FACULTY', 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, faculty.getUsername());
            pst.setString(2, faculty.getEmail());
            pst.setString(3, faculty.getPasswordHash());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.saveUser failed for username='" + faculty.getUsername() + "': " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Inserts a new row into the {@code faculty} table linking the profile
     * data to the given {@code userId}.
     *
     * @param faculty the faculty profile to persist
     * @param userId  the {@code user_id} returned from {@link #saveUser(Faculty)}
     * @return {@code true} if the insert succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean saveProfile(Faculty faculty, int userId) {
        final String sql = "INSERT INTO faculty (user_id, employee_id, full_name, phone, department) "
                         + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setString(2, faculty.getEmployeeId());
            pst.setString(3, faculty.getFullName());
            pst.setString(4, faculty.getPhone());
            pst.setString(5, faculty.getDepartment());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.saveProfile failed for userId=" + userId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all faculty members joined with their user information.
     *
     * @return list of all {@link Faculty} objects, ordered by full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Faculty> findAll() {
        final String sql = SELECT_JOIN + "ORDER BY f.full_name";

        List<Faculty> facultyList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                facultyList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.findAll failed: " + e.getMessage(), e);
        }
        return facultyList;
    }

    /**
     * Finds a single faculty member by their {@code faculty_id}.
     *
     * @param facultyId the primary key in the {@code faculty} table
     * @return the matching {@link Faculty}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Faculty findById(int facultyId) {
        final String sql = SELECT_JOIN + "WHERE f.faculty_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.findById failed for facultyId=" + facultyId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a single faculty member by their unique {@code employee_id}.
     *
     * @param employeeId the unique employee/faculty ID
     * @return the matching {@link Faculty}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Faculty findByEmployeeId(String employeeId) {
        final String sql = SELECT_JOIN + "WHERE f.employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, employeeId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.findByEmployeeId failed for employeeId='" + employeeId + "': " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a faculty member by Employee ID, numeric Faculty ID, or Username.
     *
     * @param identifier search string
     * @return matching Faculty or null
     */
    public Faculty findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        String id = identifier.trim();

        // 1. Try by Employee ID
        Faculty f = findByEmployeeId(id);
        if (f != null) return f;

        // 2. Try by numeric faculty_id
        try {
            int numId = Integer.parseInt(id);
            f = findById(numId);
            if (f != null) return f;
        } catch (NumberFormatException ignored) {}

        // 3. Try by username
        final String sql = SELECT_JOIN + "WHERE u.username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.findByIdentifier failed: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a faculty member by their {@code user_id} (FK into the {@code users} table).
     *
     * @param userId the user_id of the faculty member
     * @return the matching {@link Faculty}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Faculty findByUserId(int userId) {
        final String sql = SELECT_JOIN + "WHERE f.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.findByUserId failed for userId=" + userId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Updates a faculty member's profile and associated user credentials within
     * a single transaction. Rolls back both statements if either fails.
     *
     * @param faculty the faculty member with updated values; must have valid
     *                {@code facultyId} and {@code userId}
     * @return {@code true} if both updates succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error or rollback failure
     */
    public boolean update(Faculty faculty) {
        final String updateFaculty = "UPDATE faculty SET employee_id = ?, full_name = ?, phone = ?, department = ? "
                                   + "WHERE faculty_id = ?";
        final String updateUser    = "UPDATE users SET username = ?, email = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            boolean facultyUpdated;
            try (PreparedStatement pst = conn.prepareStatement(updateFaculty)) {
                pst.setString(1, faculty.getEmployeeId());
                pst.setString(2, faculty.getFullName());
                pst.setString(3, faculty.getPhone());
                pst.setString(4, faculty.getDepartment());
                pst.setInt(5, faculty.getFacultyId());
                facultyUpdated = pst.executeUpdate() > 0;
            }

            boolean userUpdated;
            try (PreparedStatement pst = conn.prepareStatement(updateUser)) {
                pst.setString(1, faculty.getUsername());
                pst.setString(2, faculty.getEmail());
                pst.setInt(3, faculty.getUserId());
                userUpdated = pst.executeUpdate() > 0;
            }

            conn.commit();
            return facultyUpdated && userUpdated;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("FacultyDAO.update rollback failed: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw new RuntimeException("FacultyDAO.update failed for facultyId=" + faculty.getFacultyId() + ": " + e.getMessage(), e);
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
     * Soft-deletes a faculty member by setting {@code is_active = 0} in the users table.
     *
     * @param userId the user_id of the faculty member to deactivate
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
            throw new RuntimeException("FacultyDAO.deactivate failed for userId=" + userId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Full-text search across faculty name, username, and employee ID.
     *
     * @param query the search term (will be wrapped with {@code %…%} wildcards)
     * @return list of matching faculty members, ordered by full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Faculty> search(String query) {
        final String sql = SELECT_JOIN
                + "WHERE f.full_name LIKE ? OR u.username LIKE ? OR f.department LIKE ? "
                + "OR f.employee_id LIKE ? OR CAST(f.faculty_id AS CHAR) LIKE ? "
                + "ORDER BY f.full_name";

        String param = "%" + query + "%";
        List<Faculty> facultyList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, param);
            pst.setString(2, param);
            pst.setString(3, param);
            pst.setString(4, param);
            pst.setString(5, param);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    facultyList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.search failed for query='" + query + "': " + e.getMessage(), e);
        }
        return facultyList;
    }

    /**
     * Returns the total number of active faculty records in the database.
     * Deactivated faculty (is_active = 0) are excluded from the count.
     *
     * @return total active faculty count
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int count() {
        final String sql = "SELECT COUNT(*) FROM faculty f "
                         + "JOIN users u ON f.user_id = u.user_id "
                         + "WHERE u.is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("FacultyDAO.count failed: " + e.getMessage(), e);
        }
        return 0;
    }
}
