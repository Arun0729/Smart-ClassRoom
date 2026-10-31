package com.smartclassroom.service;

import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.dao.UserDAO;
import com.smartclassroom.model.Student;
import com.smartclassroom.util.PasswordUtil;
import com.smartclassroom.util.ValidationUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service class encapsulating all business logic related to {@link Student} entities.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate student data before persistence.</li>
 *   <li>Coordinate user-account creation and student-profile creation via DAOs.</li>
 *   <li>Provide sorting (by name/ID) and binary-search DSA demonstrations.</li>
 * </ul>
 * </p>
 *
 * <p>All database access is performed through {@link StudentDAO} and {@link UserDAO}.
 * No SQL appears in this class.</p>
 */
public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    // -----------------------------------------------------------------------
    // Create / Update / Deactivate
    // -----------------------------------------------------------------------

    /**
     * Registers a new student by creating a user account and a student profile
     * in a single logical operation.
     *
     * <p>Validations performed:
     * <ul>
     *   <li>Register number, full name, username, email, and password must not be blank.</li>
     *   <li>Email must match a standard format.</li>
     *   <li>Phone, if provided, must be a valid phone number.</li>
     *   <li>Username must not already exist in the system.</li>
     *   <li>Email must not already exist in the system.</li>
     * </ul>
     * </p>
     *
     * @param student       the {@link Student} object populated with profile data
     * @param plainPassword the plain-text password to hash and store
     * @throws IllegalArgumentException if any required field is missing or invalid
     * @throws RuntimeException         if the username or email is already taken
     */
    public void addStudent(Student student, String plainPassword) {
        // Validate required fields
        if (student.getRegisterNumber() == null || student.getRegisterNumber().isBlank()) {
            throw new IllegalArgumentException("Register number cannot be empty.");
        }
        if (student.getFullName() == null || student.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (student.getUsername() == null || student.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (student.getEmail() == null || student.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + student.getEmail());
        }

        // Validate phone if provided
        if (student.getPhone() != null && !student.getPhone().isBlank()) {
            if (!ValidationUtil.isValidPhone(student.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number: " + student.getPhone());
            }
        }

        // Check username uniqueness
        if (userDAO.findByUsername(student.getUsername()) != null) {
            throw new RuntimeException("Username '" + student.getUsername() + "' is already taken.");
        }

        // Check email uniqueness
        if (userDAO.findByEmail(student.getEmail()) != null) {
            throw new RuntimeException("Email '" + student.getEmail() + "' is already registered.");
        }

        // Check register number uniqueness
        if (studentDAO.findByRegisterNumber(student.getRegisterNumber()) != null) {
            throw new RuntimeException("Register number '" + student.getRegisterNumber() + "' is already in use.");
        }

        // Hash password and set account defaults
        student.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        student.setRole("STUDENT");
        student.setActive(true);

        // Default enrollment date to today if not set
        if (student.getEnrollmentDate() == null || student.getEnrollmentDate().isBlank()) {
            student.setEnrollmentDate(LocalDate.now().toString());
        }

        // Persist: user row first, then student profile
        int userId = studentDAO.saveUser(student);
        studentDAO.saveProfile(student, userId);
    }

    /**
     * Updates an existing student's profile information.
     *
     * <p>Does <em>not</em> change the student's password. Validates that the full
     * name is present and the email is well-formed.</p>
     *
     * @param student the {@link Student} with updated values (must have valid studentId)
     * @throws IllegalArgumentException if full name is blank or email format is invalid
     */
    public void updateStudent(Student student) {
        if (student.getRegisterNumber() == null || student.getRegisterNumber().isBlank()) {
            throw new IllegalArgumentException("Register number cannot be empty.");
        }
        if (student.getFullName() == null || student.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (student.getEmail() == null || student.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + student.getEmail());
        }
        if (student.getPhone() != null && !student.getPhone().isBlank()) {
            if (!ValidationUtil.isValidPhone(student.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number: " + student.getPhone());
            }
        }
        studentDAO.update(student);
    }

    /**
     * Soft-deletes (deactivates) a student account by setting {@code is_active = false}
     * in the {@code users} table.
     *
     * @param userId the {@code user_id} of the student's user account
     */
    public void deactivateStudent(int userId) {
        studentDAO.deactivate(userId);
    }

    /**
     * Finds a student by their unique register number.
     *
     * @param registerNumber the register number to look up
     * @return the matching {@link Student}, or {@code null} if not found
     * @throws IllegalArgumentException if registerNumber is null or blank
     */
    public Student findByRegisterNumber(String registerNumber) {
        if (registerNumber == null || registerNumber.isBlank()) {
            throw new IllegalArgumentException("Register number cannot be empty.");
        }
        return studentDAO.findByRegisterNumber(registerNumber.trim());
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    /**
     * Returns all students in the system, sorted alphabetically by full name
     * (case-insensitive).
     *
     * @return a mutable {@link List} of {@link Student} objects sorted by name
     */
    public List<Student> getAllStudents() {
        List<Student> students = studentDAO.findAll();
        students.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        return students;
    }

    /**
     * Returns all students belonging to a specific class, sorted by full name.
     *
     * @param classId the ID of the class to filter by
     * @return sorted list of {@link Student} objects in the class
     */
    public List<Student> getStudentsByClass(int classId) {
        List<Student> students = studentDAO.findByClassId(classId);
        students.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        return students;
    }

    /**
     * Searches students by name, username, register number, or numeric student ID.
     *
     * @param query the search query string
     * @return a list of matching {@link Student} objects
     * @throws IllegalArgumentException if the query is null or blank
     */
    public List<Student> searchStudents(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }
        return studentDAO.search(query.trim());
    }

    /**
     * Returns the total count of student records in the system.
     *
     * @return total number of students
     */
    public int getTotalCount() {
        return studentDAO.count();
    }

    // -----------------------------------------------------------------------
    // DSA Demonstrations: Sorting & Binary Search
    // -----------------------------------------------------------------------

    /**
     * Returns a new list of students sorted alphabetically by full name (A → Z).
     *
     * <p><strong>DSA:</strong> Uses {@link Comparator} with {@link String#CASE_INSENSITIVE_ORDER}
     * applied via {@link List#sort(Comparator)}.</p>
     *
     * @param students the input list (not modified)
     * @return a new sorted {@link ArrayList}
     */
    public List<Student> sortByName(List<Student> students) {
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    /**
     * Returns a new list of students sorted by their {@code studentId} in ascending order.
     *
     * <p><strong>DSA:</strong> Uses {@link Comparator#comparingInt} with
     * {@link List#sort(Comparator)} — prerequisite for binary search.</p>
     *
     * @param students the input list (not modified)
     * @return a new sorted {@link ArrayList}
     */
    public List<Student> sortById(List<Student> students) {
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparingInt(Student::getStudentId));
        return sorted;
    }

    /**
     * Performs a binary search on a list that is already sorted by {@code studentId}
     * in ascending order.
     *
     * <p><strong>DSA:</strong> Classic iterative binary search — O(log n) time complexity
     * versus O(n) linear scan. Requires the list to be pre-sorted by ID (e.g., via
     * {@link #sortById(List)}).</p>
     *
     * @param sortedById the list sorted ascending by {@code studentId}
     * @param targetId   the {@code studentId} to find
     * @return the matching {@link Student}, or {@code null} if not found
     */
    public Student binarySearchById(List<Student> sortedById, int targetId) {
        int lo = 0;
        int hi = sortedById.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int midId = sortedById.get(mid).getStudentId();
            if (midId == targetId) {
                return sortedById.get(mid);
            } else if (midId < targetId) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return null;
    }
}
