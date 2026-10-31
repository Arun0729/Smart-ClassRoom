package com.smartclassroom.service;

import com.smartclassroom.dao.UserDAO;
import com.smartclassroom.model.User;
import com.smartclassroom.util.PasswordUtil;
import com.smartclassroom.util.SessionManager;

/**
 * Service class responsible for authentication operations.
 * Handles user login/logout, credential verification, and session management.
 *
 * <p>Follows the service-layer pattern: validates inputs, enforces business rules,
 * and delegates to {@link UserDAO} for persistence.</p>
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates a user by username (or email) and plain-text password.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Validate that neither {@code username} nor {@code password} is blank.</li>
     *   <li>Look up the user by username; if not found, try email.</li>
     *   <li>If no user is found, throw {@link RuntimeException} with a generic message.</li>
     *   <li>Reject inactive accounts.</li>
     *   <li>Verify the password against the stored BCrypt hash.</li>
     *   <li>Store the authenticated user in {@link SessionManager}.</li>
     * </ol>
     * </p>
     *
     * @param username the username (or email) supplied by the user
     * @param password the plain-text password to verify
     * @return the authenticated {@link User} object
     * @throws IllegalArgumentException if {@code username} or {@code password} is blank
     * @throws RuntimeException         if credentials are invalid or the account is inactive
     */
    public User login(String username, String password) {
        // 1. Validate inputs
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // 2. Find user by username; fall back to email lookup
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            user = userDAO.findByEmail(username.trim());
        }

        // 3. User not found
        if (user == null) {
            throw new RuntimeException("Invalid username or password.");
        }

        // 4. Check account is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive. Please contact admin.");
        }

        // 5. Verify password against stored hash
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password.");
        }

        // 6. Set session
        SessionManager.getInstance().setCurrentUser(user);

        // 7. Return authenticated user
        return user;
    }

    /**
     * Logs out the currently authenticated user by clearing the session.
     */
    public void logout() {
        SessionManager.getInstance().clearSession();
    }
}
