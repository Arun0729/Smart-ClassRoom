package com.smartclassroom.model;

/**
 * Represents an administrator user in the Smart Classroom system.
 * Admins can manage users, classes, and subjects.
 */
public class Admin extends User {

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor. Sets role to "ADMIN".
     */
    public Admin() {
        super();
        this.role = "ADMIN";
    }

    /**
     * Full constructor for Admin.
     *
     * @param userId       the database primary key
     * @param username     the login username
     * @param email        the email address
     * @param passwordHash the BCrypt-hashed password
     * @param active       whether the account is active
     */
    public Admin(int userId, String username, String email,
                 String passwordHash, boolean active) {
        super(userId, username, email, passwordHash, "ADMIN", active);
    }

    // -------------------------------------------------------------------------
    // Abstract method implementation
    // -------------------------------------------------------------------------

    /**
     * Returns the dashboard type identifier for administrators.
     *
     * @return "ADMIN"
     */
    @Override
    public String getDashboardType() {
        return "ADMIN";
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
