package com.smartclassroom.service;

import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.dao.UserDAO;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Subject;
import com.smartclassroom.util.PasswordUtil;
import com.smartclassroom.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service class encapsulating all business logic related to {@link Faculty} entities.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate faculty data before persistence.</li>
 *   <li>Coordinate user-account and faculty-profile creation via DAOs.</li>
 *   <li>Provide queries for faculty listing, searching, and subject assignments.</li>
 * </ul>
 * </p>
 *
 * <p>All database access is delegated to {@link FacultyDAO}, {@link UserDAO},
 * and {@link SubjectDAO}. No SQL appears in this class.</p>
 */
public class FacultyService {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    // -----------------------------------------------------------------------
    // Create / Update / Deactivate
    // -----------------------------------------------------------------------

    /**
     * Registers a new faculty member by creating a user account and faculty profile.
     *
     * <p>Validations performed:
     * <ul>
     *   <li>Employee ID, full name, username, email, and password must not be blank.</li>
     *   <li>Email must match a standard format.</li>
     *   <li>Phone, if provided, must be a valid phone number.</li>
     *   <li>Username must not already exist in the system.</li>
     *   <li>Email must not already exist in the system.</li>
     *   <li>Employee ID must not already exist in the system.</li>
     * </ul>
     * </p>
     *
     * @param faculty       the {@link Faculty} object populated with profile data
     * @param plainPassword the plain-text password to hash and store
     * @throws IllegalArgumentException if any required field is missing or invalid
     * @throws RuntimeException         if the username, email, or employee ID is already taken
     */
    public void addFaculty(Faculty faculty, String plainPassword) {
        // Validate required fields
        if (faculty.getEmployeeId() == null || faculty.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        if (faculty.getFullName() == null || faculty.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (faculty.getUsername() == null || faculty.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (faculty.getEmail() == null || faculty.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(faculty.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + faculty.getEmail());
        }

        // Validate phone if provided
        if (faculty.getPhone() != null && !faculty.getPhone().isBlank()) {
            if (!ValidationUtil.isValidPhone(faculty.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number: " + faculty.getPhone());
            }
        }

        // Check username uniqueness
        if (userDAO.findByUsername(faculty.getUsername()) != null) {
            throw new RuntimeException("Username '" + faculty.getUsername() + "' is already taken.");
        }

        // Check email uniqueness
        if (userDAO.findByEmail(faculty.getEmail()) != null) {
            throw new RuntimeException("Email '" + faculty.getEmail() + "' is already registered.");
        }

        // Check employee ID uniqueness
        if (facultyDAO.findByEmployeeId(faculty.getEmployeeId().trim()) != null) {
            throw new RuntimeException("Employee ID '" + faculty.getEmployeeId() + "' is already in use.");
        }

        // Hash password and set account defaults
        faculty.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        faculty.setRole("FACULTY");
        faculty.setActive(true);

        // Persist: user row first, then faculty profile
        int userId = facultyDAO.saveUser(faculty);
        facultyDAO.saveProfile(faculty, userId);
    }

    /**
     * Updates an existing faculty member's profile information.
     *
     * <p>Does <em>not</em> change the faculty member's password. Validates that
     * the full name and employee ID are present and the email is well-formed.</p>
     *
     * @param faculty the {@link Faculty} with updated values (must have valid facultyId)
     * @throws IllegalArgumentException if full name is blank or email format is invalid
     */
    public void updateFaculty(Faculty faculty) {
        if (faculty.getEmployeeId() == null || faculty.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        if (faculty.getFullName() == null || faculty.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (faculty.getEmail() == null || faculty.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(faculty.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + faculty.getEmail());
        }
        if (faculty.getPhone() != null && !faculty.getPhone().isBlank()) {
            if (!ValidationUtil.isValidPhone(faculty.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number: " + faculty.getPhone());
            }
        }
        facultyDAO.update(faculty);
    }

    /**
     * Soft-deletes (deactivates) a faculty account by setting {@code is_active = false}
     * in the {@code users} table.
     *
     * @param userId the {@code user_id} of the faculty's user account
     */
    public void deactivateFaculty(int userId) {
        facultyDAO.deactivate(userId);
    }

    /**
     * Finds a faculty member by their unique employee ID.
     *
     * @param employeeId the employee ID
     * @return the matching {@link Faculty}, or {@code null} if not found
     */
    public Faculty findByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        return facultyDAO.findByEmployeeId(employeeId.trim());
    }

    /**
     * Finds a faculty member by Employee ID, numeric Faculty ID, or Username.
     *
     * @param identifier the search identifier
     * @return the matching {@link Faculty}, or {@code null} if not found
     */
    public Faculty findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Search identifier cannot be empty.");
        }
        return facultyDAO.findByIdentifier(identifier.trim());
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    /**
     * Returns all faculty members in the system, sorted alphabetically by full name
     * (case-insensitive).
     *
     * @return a mutable {@link List} of {@link Faculty} objects sorted by name
     */
    public List<Faculty> getAllFaculty() {
        List<Faculty> facultyList = facultyDAO.findAll();
        facultyList.sort(Comparator.comparing(Faculty::getFullName, String.CASE_INSENSITIVE_ORDER));
        return facultyList;
    }

    /**
     * Searches faculty members by name, username, department, or employee ID.
     *
     * @param query the search query string
     * @return a list of matching {@link Faculty} objects
     * @throws IllegalArgumentException if the query is null or blank
     */
    public List<Faculty> searchFaculty(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }
        return facultyDAO.search(query.trim());
    }

    /**
     * Returns all subjects assigned to a specific faculty member.
     *
     * @param facultyId the ID of the faculty member
     * @return a list of {@link Subject} objects assigned to the faculty
     */
    public List<Subject> getAssignedSubjects(int facultyId) {
        return subjectDAO.findByFacultyId(facultyId);
    }

    /**
     * Returns the total count of faculty records in the system.
     *
     * @return total number of faculty members
     */
    public int getTotalCount() {
        return facultyDAO.count();
    }
}
