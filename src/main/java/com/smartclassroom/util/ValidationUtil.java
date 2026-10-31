package com.smartclassroom.util;

import java.util.regex.Pattern;

/**
 * Utility class providing static validation methods used across the application.
 * All methods are stateless; this class must not be instantiated.
 */
public final class ValidationUtil {

    /** Regex pattern for validating email addresses. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    /** Regex pattern for validating 10-digit phone numbers. */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\d{10}");

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtil() {
    }

    /**
     * Checks that every supplied field is non-null and non-blank.
     *
     * @param fields one or more String values to check
     * @return {@code true} if ALL fields are non-null and contain at least one
     *         non-whitespace character; {@code false} if any field is null or blank
     */
    public static boolean isNotEmpty(String... fields) {
        if (fields == null) {
            return false;
        }
        for (String field : fields) {
            if (field == null || field.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates an email address against a standard RFC-style pattern.
     *
     * @param email the email address to validate
     * @return {@code true} if the email matches the pattern; {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates that a phone number consists of exactly 10 numeric digits.
     *
     * @param phone the phone number string to validate
     * @return {@code true} if the phone is exactly 10 digits; {@code false} otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates that a filename has one of the specified allowed extensions.
     * The comparison is case-insensitive.
     *
     * @param filename          the filename to check (e.g., "report.pdf")
     * @param allowedExtensions an array of allowed extensions including the dot
     *                          (e.g., {@code {".pdf", ".docx", ".zip"}})
     * @return {@code true} if the filename ends with one of the allowed extensions
     *         (case-insensitive); {@code false} otherwise or if filename is null/empty
     */
    public static boolean isValidFileExtension(String filename, String[] allowedExtensions) {
        if (filename == null || filename.isBlank() || allowedExtensions == null) {
            return false;
        }
        String lowerFilename = filename.toLowerCase();
        for (String ext : allowedExtensions) {
            if (ext != null && lowerFilename.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that a marks value is within the valid range [0, maxMarks].
     *
     * @param marks    the marks awarded (must be &gt;= 0)
     * @param maxMarks the maximum possible marks (must be &gt; 0)
     * @return {@code true} if {@code 0 <= marks <= maxMarks}; {@code false} otherwise
     */
    public static boolean isValidMarks(double marks, double maxMarks) {
        return marks >= 0 && marks <= maxMarks;
    }
}
