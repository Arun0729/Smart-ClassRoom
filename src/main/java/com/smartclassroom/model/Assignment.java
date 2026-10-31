package com.smartclassroom.model;

import java.time.LocalDateTime;

/**
 * Represents an assignment created by a faculty member for a subject.
 * The {@code subjectName} field is a transient display value populated via JOIN queries.
 * The {@code derivedStatus} is a transient computed field based on submission state and deadline.
 */
public class Assignment {

    /**
     * Enum representing the derived status of an assignment from a student's perspective.
     * This is a transient field and is not stored in the database.
     */
    public enum AssignmentStatus {
        PENDING,
        SUBMITTED,
        OVERDUE,
        LATE_SUBMITTED,
        EVALUATED
    }

    private int assignmentId;
    private String title;
    private String description;
    private int subjectId;
    private int facultyId;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private double maxMarks;
    private String subjectName;              // Transient display field from JOIN; may be null
    private AssignmentStatus derivedStatus;  // Transient computed status; not persisted

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public Assignment() {
    }

    /**
     * Core constructor for DB-persisted fields only.
     *
     * @param assignmentId the database primary key
     * @param title        the assignment title
     * @param description  the assignment description/instructions
     * @param subjectId    the subject this assignment belongs to
     * @param facultyId    the faculty member who created this assignment
     * @param createdAt    the timestamp when the assignment was created
     * @param deadline     the submission deadline timestamp
     * @param maxMarks     the maximum marks achievable
     */
    public Assignment(int assignmentId, String title, String description,
                      int subjectId, int facultyId,
                      LocalDateTime createdAt, LocalDateTime deadline,
                      double maxMarks) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.subjectId = subjectId;
        this.facultyId = facultyId;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.maxMarks = maxMarks;
    }

    /**
     * Full constructor including the transient display and computed fields.
     *
     * @param assignmentId  the database primary key
     * @param title         the assignment title
     * @param description   the assignment description/instructions
     * @param subjectId     the subject this assignment belongs to
     * @param facultyId     the faculty member who created this assignment
     * @param createdAt     the timestamp when the assignment was created
     * @param deadline      the submission deadline timestamp
     * @param maxMarks      the maximum marks achievable
     * @param subjectName   the display name of the subject (from JOIN; may be null)
     * @param derivedStatus the computed status (transient; may be null)
     */
    public Assignment(int assignmentId, String title, String description,
                      int subjectId, int facultyId,
                      LocalDateTime createdAt, LocalDateTime deadline,
                      double maxMarks, String subjectName,
                      AssignmentStatus derivedStatus) {
        this(assignmentId, title, description, subjectId, facultyId,
                createdAt, deadline, maxMarks);
        this.subjectName = subjectName;
        this.derivedStatus = derivedStatus;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key
     */
    public int getAssignmentId() {
        return assignmentId;
    }

    /**
     * @param assignmentId the primary key to set
     */
    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * @return the assignment title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the assignment description/instructions
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the subject ID this assignment belongs to
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId the subject ID to set
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * @return the faculty ID who created this assignment
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
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the submission deadline timestamp
     */
    public LocalDateTime getDeadline() {
        return deadline;
    }

    /**
     * @param deadline the deadline timestamp to set
     */
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    /**
     * @return the maximum marks achievable for this assignment
     */
    public double getMaxMarks() {
        return maxMarks;
    }

    /**
     * @param maxMarks the maximum marks to set
     */
    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    /**
     * Returns the transient subject display name. May be null if not joined.
     *
     * @return subject name or null
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName the transient subject display name to set
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * Returns the transient derived status. May be null if not computed yet.
     *
     * @return the derived assignment status or null
     */
    public AssignmentStatus getDerivedStatus() {
        return derivedStatus;
    }

    /**
     * @param derivedStatus the transient derived status to set
     */
    public void setDerivedStatus(AssignmentStatus derivedStatus) {
        this.derivedStatus = derivedStatus;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignmentId +
                ", title='" + title + '\'' +
                ", subjectId=" + subjectId +
                ", facultyId=" + facultyId +
                ", deadline=" + deadline +
                ", maxMarks=" + maxMarks +
                ", derivedStatus=" + derivedStatus +
                '}';
    }
}
