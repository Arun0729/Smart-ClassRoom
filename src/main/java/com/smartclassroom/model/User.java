package com.smartclassroom.model;

/**
 * Abstract base class representing a system user.
 * All user types (Admin, Faculty, Student) extend this class.
 */
public abstract class User {

    protected int userId;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String role; // "ADMIN", "FACULTY", "STUDENT"
    protected boolean active;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public User() {
    }

    /**
     * Full constructor.
     *
     * @param userId       the unique user ID (from DB auto-increment)
     * @param username     the login username
     * @param email        the user's email address
     * @param passwordHash the BCrypt-hashed password
     * @param role         the role string: "ADMIN", "FACULTY", or "STUDENT"
     * @param active       whether the account is currently active
     */
    public User(int userId, String username, String email,
                String passwordHash, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    /**
     * Returns a string identifying which dashboard type this user should see.
     *
     * @return dashboard type identifier (e.g. "ADMIN", "FACULTY", "STUDENT")
     */
    public abstract String getDashboardType();

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key for this user
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the database primary key to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the login username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the login username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the BCrypt-hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordHash the BCrypt-hashed password to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return the role string ("ADMIN", "FACULTY", "STUDENT")
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role string to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return true if the account is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active set to true to activate the account, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
