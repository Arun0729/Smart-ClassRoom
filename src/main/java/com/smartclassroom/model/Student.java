package com.smartclassroom.model;

/**
 * Represents a student user in the Smart Classroom system.
 * Students can view attendance, submit assignments, and track their academic progress.
 */
public class Student extends User {

    private int studentId;
    private String registerNumber;  // Unique register number (e.g. "REG001")
    private String fullName;
    private String phone;
    private int classId;
    private String enrollmentDate;  // Stored as "YYYY-MM-DD" string
    private String className;       // Transient display field; may be null

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor. Sets role to "STUDENT".
     */
    public Student() {
        super();
        this.role = "STUDENT";
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
    public Student(int userId, String username, String email,
                   String passwordHash, boolean active) {
        super(userId, username, email, passwordHash, "STUDENT", active);
    }

    /**
     * Full constructor including student-specific profile fields.
     *
     * @param userId         the database primary key (users table)
     * @param username       the login username
     * @param email          the email address
     * @param passwordHash   the BCrypt-hashed password
     * @param active         whether the account is active
     * @param studentId      the primary key from the students table
     * @param registerNumber the unique register number
     * @param fullName       the student's full name
     * @param phone          the contact phone number
     * @param classId        the foreign key referencing the class
     * @param enrollmentDate the date of enrollment in "YYYY-MM-DD" format
     */
    public Student(int userId, String username, String email,
                   String passwordHash, boolean active,
                   int studentId, String registerNumber, String fullName,
                   String phone, int classId, String enrollmentDate) {
        super(userId, username, email, passwordHash, "STUDENT", active);
        this.studentId = studentId;
        this.registerNumber = registerNumber;
        this.fullName = fullName;
        this.phone = phone;
        this.classId = classId;
        this.enrollmentDate = enrollmentDate;
    }

    /**
     * Full constructor including the transient className display field.
     *
     * @param userId         the database primary key (users table)
     * @param username       the login username
     * @param email          the email address
     * @param passwordHash   the BCrypt-hashed password
     * @param active         whether the account is active
     * @param studentId      the primary key from the students table
     * @param registerNumber the unique register number
     * @param fullName       the student's full name
     * @param phone          the contact phone number
     * @param classId        the foreign key referencing the class
     * @param enrollmentDate the date of enrollment in "YYYY-MM-DD" format
     * @param className      the display name of the class (transient, may be null)
     */
    public Student(int userId, String username, String email,
                   String passwordHash, boolean active,
                   int studentId, String registerNumber, String fullName,
                   String phone, int classId, String enrollmentDate, String className) {
        this(userId, username, email, passwordHash, active,
                studentId, registerNumber, fullName, phone, classId, enrollmentDate);
        this.className = className;
    }

    // -------------------------------------------------------------------------
    // Abstract method implementation
    // -------------------------------------------------------------------------

    /**
     * Returns the dashboard type identifier for student users.
     *
     * @return "STUDENT"
     */
    @Override
    public String getDashboardType() {
        return "STUDENT";
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the primary key from the students profile table
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * @param studentId the student profile primary key to set
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * @return the unique register number
     */
    public String getRegisterNumber() {
        return registerNumber;
    }

    /**
     * @param registerNumber the unique register number to set
     */
    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    /**
     * @return the student's full name
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
     * @return the class ID this student belongs to
     */
    public int getClassId() {
        return classId;
    }

    /**
     * @param classId the class ID to set
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * @return the enrollment date as "YYYY-MM-DD" string
     */
    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    /**
     * @param enrollmentDate the enrollment date in "YYYY-MM-DD" format
     */
    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    /**
     * Returns the transient display name of the class. May be null if not joined.
     *
     * @return the class name or null
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the transient display class name to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", registerNumber='" + registerNumber + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                ", enrollmentDate='" + enrollmentDate + '\'' +
                ", active=" + active +
                '}';
    }
}
