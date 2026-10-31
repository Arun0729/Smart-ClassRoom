package com.smartclassroom.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * This class must not be instantiated; all methods are static.
 */
public final class PasswordUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password using BCrypt with a work factor of 10.
     *
     * @param plainPassword the plain-text password to hash (must not be null or empty)
     * @return a BCrypt-hashed password string
     * @throws IllegalArgumentException if the plainPassword is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plain-text password against a BCrypt-hashed password.
     *
     * @param plainPassword  the plain-text password to verify (must not be null)
     * @param hashedPassword the BCrypt hash to verify against (must not be null)
     * @return {@code true} if the plain-text password matches the hash; {@code false} otherwise
     * @throws IllegalArgumentException if either argument is null or empty
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Plain password must not be null or empty.");
        }
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            throw new IllegalArgumentException("Hashed password must not be null or empty.");
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
