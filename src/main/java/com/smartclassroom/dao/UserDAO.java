package com.smartclassroom.dao;

import com.smartclassroom.model.Admin;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.User;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for the {@code users} table.
 * Handles authentication lookups and user-level persistence.
 * All SQL is executed via {@link PreparedStatement} using {@link DBConnection}.
 */
public class UserDAO {

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link ResultSet} row from the {@code users} table to the
     * appropriate {@link User} subclass based on the {@code role} column.
     *
     * @param rs an open ResultSet positioned on the desired row
     * @return a fully populated User subclass instance
     * @throws SQLException if any column cannot be read
     */
    private User mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");

        User user;
        switch (role) {
            case "ADMIN"   -> user = new Admin();
            case "FACULTY" -> user = new Faculty();
            case "STUDENT" -> user = new Student();
            default        -> throw new RuntimeException("Unknown role in users table: " + role);
        }

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(role);
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Looks up a user by their username.
     *
     * @param username the username to search for
     * @return the matching {@link User} subclass, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public User findByUsername(String username) {
        final String sql = "SELECT user_id, username, email, password_hash, role, is_active "
                         + "FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByUsername failed for username='" + username + "': " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Looks up a user by their email address.
     *
     * @param email the email address to search for
     * @return the matching {@link User} subclass, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public User findByEmail(String email) {
        final String sql = "SELECT user_id, username, email, password_hash, role, is_active "
                         + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.findByEmail failed for email='" + email + "': " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Inserts a new user record into the {@code users} table.
     * Uses {@link Statement#RETURN_GENERATED_KEYS} to retrieve the auto-generated
     * {@code user_id}.
     *
     * @param user the User to persist; {@code role} and {@code passwordHash} must be set
     * @return the generated {@code user_id}, or {@code -1} on failure
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int save(User user) {
        final String sql = "INSERT INTO users (username, email, password_hash, role, is_active) "
                         + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, user.getUsername());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPasswordHash());
            pst.setString(4, user.getRole());
            pst.setBoolean(5, user.isActive());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.save failed for username='" + user.getUsername() + "': " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Updates the {@code is_active} flag for the given user.
     *
     * @param userId the target user's ID
     * @param active {@code true} to activate, {@code false} to deactivate
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean updateActiveStatus(int userId, boolean active) {
        final String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setBoolean(1, active);
            pst.setInt(2, userId);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDAO.updateActiveStatus failed for userId=" + userId + ": " + e.getMessage(), e);
        }
    }
}
