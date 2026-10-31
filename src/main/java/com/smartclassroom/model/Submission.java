package com.smartclassroom.model;

import java.time.LocalDateTime;

/**
 * Represents a student's submission for an assignment.
 * The {@code studentName} and {@code assignmentTitle} fields are transient display values
 * populated via JOIN queries.
 * The {@code marks} and {@code feedback} fields are nullable until the submission is evaluated.
 */
public class Submission {

    /**
     * Enum representing the evaluation status of a submission.
     */
    public enum EvaluationStatus {
        SUBMITTED,
        EVALUATED
    }

    private int submissionId;
    private int assignmentId;
    private int studentId;
    private String filePath;
    private LocalDateTime submittedAt;
    private Double marks;            // Nullable until evaluated
    private String feedback;         // Nullable until evaluated
    private EvaluationStatus evaluationStatus;
    private String studentName;      // Transient display field from JOIN; may be null
    private String assignmentTitle;  // Transient display field from JOIN; may be null

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public Submission() {
    }

    /**
     * Core constructor for DB-persisted fields only (before evaluation).
     *
     * @param submissionId     the database primary key
     * @param assignmentId     the assignment this submission is for
     * @param studentId        the student who submitted
     * @param filePath         the path to the submitted file
     * @param submittedAt      the timestamp of submission
     * @param evaluationStatus SUBMITTED or EVALUATED
     */
    public Submission(int submissionId, int assignmentId, int studentId,
                      String filePath, LocalDateTime submittedAt,
                      EvaluationStatus evaluationStatus) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.filePath = filePath;
        this.submittedAt = submittedAt;
        this.evaluationStatus = evaluationStatus;
    }

    /**
     * Full constructor including nullable evaluation fields.
     *
     * @param submissionId     the database primary key
     * @param assignmentId     the assignment this submission is for
     * @param studentId        the student who submitted
     * @param filePath         the path to the submitted file
     * @param submittedAt      the timestamp of submission
     * @param marks            the marks awarded (null if not yet evaluated)
     * @param feedback         the feedback text (null if not yet evaluated)
     * @param evaluationStatus SUBMITTED or EVALUATED
     */
    public Submission(int submissionId, int assignmentId, int studentId,
                      String filePath, LocalDateTime submittedAt,
                      Double marks, String feedback,
                      EvaluationStatus evaluationStatus) {
        this(submissionId, assignmentId, studentId, filePath, submittedAt, evaluationStatus);
        this.marks = marks;
        this.feedback = feedback;
    }

    /**
     * Full constructor including nullable evaluation fields and transient display fields.
     *
     * @param submissionId     the database primary key
     * @param assignmentId     the assignment this submission is for
     * @param studentId        the student who submitted
     * @param filePath         the path to the submitted file
     * @param submittedAt      the timestamp of submission
     * @param marks            the marks awarded (null if not yet evaluated)
     * @param feedback         the feedback text (null if not yet evaluated)
     * @param evaluationStatus SUBMITTED or EVALUATED
     * @param studentName      the display name of the student (from JOIN; may be null)
     * @param assignmentTitle  the display title of the assignment (from JOIN; may be null)
     */
    public Submission(int submissionId, int assignmentId, int studentId,
                      String filePath, LocalDateTime submittedAt,
                      Double marks, String feedback,
                      EvaluationStatus evaluationStatus,
                      String studentName, String assignmentTitle) {
        this(submissionId, assignmentId, studentId, filePath, submittedAt,
                marks, feedback, evaluationStatus);
        this.studentName = studentName;
        this.assignmentTitle = assignmentTitle;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key
     */
    public int getSubmissionId() {
        return submissionId;
    }

    /**
     * @param submissionId the primary key to set
     */
    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    /**
     * @return the assignment ID this submission is for
     */
    public int getAssignmentId() {
        return assignmentId;
    }

    /**
     * @param assignmentId the assignment ID to set
     */
    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * @return the student ID who submitted
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * @param studentId the student ID to set
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * @return the file path of the submitted file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the file path to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the timestamp when the assignment was submitted
     */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    /**
     * @param submittedAt the submission timestamp to set
     */
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    /**
     * Returns the marks awarded. Null if the submission has not been evaluated yet.
     *
     * @return marks or null
     */
    public Double getMarks() {
        return marks;
    }

    /**
     * @param marks the marks to set (may be null)
     */
    public void setMarks(Double marks) {
        this.marks = marks;
    }

    /**
     * Returns the feedback text. Null if the submission has not been evaluated yet.
     *
     * @return feedback or null
     */
    public String getFeedback() {
        return feedback;
    }

    /**
     * @param feedback the feedback text to set (may be null)
     */
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    /**
     * @return the evaluation status (SUBMITTED or EVALUATED)
     */
    public EvaluationStatus getEvaluationStatus() {
        return evaluationStatus;
    }

    /**
     * @param evaluationStatus the evaluation status to set
     */
    public void setEvaluationStatus(EvaluationStatus evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }

    /**
     * Returns the transient student display name. May be null if not joined.
     *
     * @return student name or null
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * @param studentName the transient student display name to set
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * Returns the transient assignment title display value. May be null if not joined.
     *
     * @return assignment title or null
     */
    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    /**
     * @param assignmentTitle the transient assignment title to set
     */
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Submission{" +
                "submissionId=" + submissionId +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", submittedAt=" + submittedAt +
                ", marks=" + marks +
                ", evaluationStatus=" + evaluationStatus +
                ", studentName='" + studentName + '\'' +
                ", assignmentTitle='" + assignmentTitle + '\'' +
                '}';
    }
}
