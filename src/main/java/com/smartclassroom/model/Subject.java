package com.smartclassroom.model;

/**
 * Represents a subject (course) taught in a specific classroom by a faculty member.
 * The {@code facultyName} and {@code className} fields are transient display values
 * populated via JOIN queries and are not persisted independently.
 */
public class Subject {

    private int subjectId;
    private String subjectName;
    private String subjectCode;
    private int classId;
    private int facultyId;
    private String facultyName;  // Display field from JOIN; may be null
    private String className;    // Display field from JOIN; may be null

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public Subject() {
    }

    /**
     * Core constructor without display fields.
     *
     * @param subjectId   the database primary key
     * @param subjectName the full name of the subject
     * @param subjectCode the short code (e.g., "CS101")
     * @param classId     the class this subject belongs to
     * @param facultyId   the faculty member teaching this subject
     */
    public Subject(int subjectId, String subjectName, String subjectCode,
                   int classId, int facultyId) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.classId = classId;
        this.facultyId = facultyId;
    }

    /**
     * Full constructor including display fields.
     *
     * @param subjectId   the database primary key
     * @param subjectName the full name of the subject
     * @param subjectCode the short code (e.g., "CS101")
     * @param classId     the class this subject belongs to
     * @param facultyId   the faculty member teaching this subject
     * @param facultyName the display name of the faculty (from JOIN; may be null)
     * @param className   the display name of the class (from JOIN; may be null)
     */
    public Subject(int subjectId, String subjectName, String subjectCode,
                   int classId, int facultyId,
                   String facultyName, String className) {
        this(subjectId, subjectName, subjectCode, classId, facultyId);
        this.facultyName = facultyName;
        this.className = className;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId the primary key to set
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * @return the full name of the subject
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName the subject name to set
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * @return the subject code (e.g., "CS101")
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * @param subjectCode the subject code to set
     */
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    /**
     * @return the class ID this subject is assigned to
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
     * @return the faculty ID of the teacher for this subject
     */
    public int getFacultyId() {
        return facultyId;
    }

    /**
     * @param facultyId the faculty ID to set
     */
    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    /**
     * Returns the transient faculty display name. May be null if not joined.
     *
     * @return faculty name or null
     */
    public String getFacultyName() {
        return facultyName;
    }

    /**
     * @param facultyName the transient faculty display name to set
     */
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    /**
     * Returns the transient class display name. May be null if not joined.
     *
     * @return class name or null
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the transient class display name to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Returns the subject name so that Subject objects display correctly
     * in JavaFX ComboBox and ListView components.
     *
     * @return the subjectName
     */
    @Override
    public String toString() {
        return subjectName;
    }
}
