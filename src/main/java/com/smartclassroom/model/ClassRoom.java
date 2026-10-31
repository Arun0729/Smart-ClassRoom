package com.smartclassroom.model;

/**
 * Represents an academic classroom/batch in the Smart Classroom system.
 * A ClassRoom groups students together and is associated with subjects.
 */
public class ClassRoom {

    private int classId;
    private String className;
    private String department;
    private String academicYear;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public ClassRoom() {
    }

    /**
     * Full constructor.
     *
     * @param classId      the database primary key
     * @param className    the display name of the class (e.g., "CS-A 2024")
     * @param department   the department offering this class (e.g., "Computer Science")
     * @param academicYear the academic year (e.g., "2024-2025")
     */
    public ClassRoom(int classId, String className, String department, String academicYear) {
        this.classId = classId;
        this.className = className;
        this.department = department;
        this.academicYear = academicYear;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key for this class
     */
    public int getClassId() {
        return classId;
    }

    /**
     * @param classId the primary key to set
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * @return the display name of the class
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the class display name to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the department offering this class
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * @return the academic year string (e.g., "2024-2025")
     */
    public String getAcademicYear() {
        return academicYear;
    }

    /**
     * @param academicYear the academic year to set
     */
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Returns the class name so that ClassRoom objects display correctly
     * in JavaFX ComboBox and ListView components.
     *
     * @return the className
     */
    @Override
    public String toString() {
        return className;
    }
}
