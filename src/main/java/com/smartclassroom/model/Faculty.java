package com.smartclassroom.model;

/**
 * Represents a faculty (teacher) user in the Smart Classroom system.
 * Faculty can manage subjects, assignments, attendance, and evaluate submissions.
 */
public class Faculty extends User {

    private int facultyId;
    private String employeeId;     // Unique employee/faculty ID (e.g. "FAC001")
    private String fullName;
    private String phone;
    private String department;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor. Sets role to "FACULTY".
     */
    public Faculty() {
        super();
        this.role = "FACULTY";
    }

    /**
     * Constructor with User-level fields only.
     *
     * @param userId       the database primary key
     * @param username     the login username
     * @param email        the email address
     * @param passwordHash the BCrypt-hashed password
     * @param active       whether the account is active
     */
    public Faculty(int userId, String username, String email,
                   String passwordHash, boolean active) {
        super(userId, username, email, passwordHash, "FACULTY", active);
    }

    /**
     * Full constructor including faculty-specific profile fields (without employeeId).
     *
     * @param userId       the database primary key (users table)
     * @param username     the login username
     * @param email        the email address
     * @param passwordHash the BCrypt-hashed password
     * @param active       whether the account is active
     * @param facultyId    the primary key from the faculty table
     * @param fullName     the faculty member's full name
     * @param phone        the contact phone number
     * @param department   the academic department (e.g., "Computer Science")
     */
    public Faculty(int userId, String username, String email,
                   String passwordHash, boolean active,
                   int facultyId, String fullName, String phone, String department) {
        super(userId, username, email, passwordHash, "FACULTY", active);
        this.facultyId = facultyId;
        this.fullName = fullName;
        this.phone = phone;
        this.department = department;
    }

    /**
     * Full constructor including employeeId.
     *
     * @param userId       the database primary key (users table)
     * @param username     the login username
     * @param email        the email address
     * @param passwordHash the BCrypt-hashed password
     * @param active       whether the account is active
     * @param facultyId    the primary key from the faculty table
     * @param employeeId   the unique employee/faculty ID
     * @param fullName     the faculty member's full name
     * @param phone        the contact phone number
     * @param department   the academic department (e.g., "Computer Science")
     */
    public Faculty(int userId, String username, String email,
                   String passwordHash, boolean active,
                   int facultyId, String employeeId, String fullName, String phone, String department) {
        super(userId, username, email, passwordHash, "FACULTY", active);
        this.facultyId = facultyId;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phone = phone;
        this.department = department;
    }

    // -------------------------------------------------------------------------
    // Abstract method implementation
    // -------------------------------------------------------------------------

    /**
     * Returns the dashboard type identifier for faculty users.
     *
     * @return "FACULTY"
     */
    @Override
    public String getDashboardType() {
        return "FACULTY";
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the primary key from the faculty profile table
     */
    public int getFacultyId() {
        return facultyId;
    }

    /**
     * @param facultyId the faculty profile primary key to set
     */
    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    /**
     * @return the unique employee/faculty ID
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * @param employeeId the unique employee ID to set
     */
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * @return the faculty member's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the contact phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the academic department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the academic department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Faculty{" +
                "facultyId=" + facultyId +
                ", employeeId='" + employeeId + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                ", active=" + active +
                '}';
    }
}
