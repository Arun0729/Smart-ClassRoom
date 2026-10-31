package com.smartclassroom.model;

import java.time.LocalDate;

/**
 * Represents a single attendance record for a student in a subject on a given date.
 * The {@code studentName} field is a transient display value populated via JOIN queries.
 */
public class Attendance {

    /**
     * Enum representing the attendance status for a record.
     */
    public enum Status {
        PRESENT,
        ABSENT
    }

    private int attendanceId;
    private int studentId;
    private int subjectId;
    private LocalDate attendanceDate;
    private Status status;
    private String studentName;  // Transient display field from JOIN; may be null

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default (no-arg) constructor.
     */
    public Attendance() {
    }

    /**
     * Core constructor without the display field.
     *
     * @param attendanceId   the database primary key
     * @param studentId      the student this record belongs to
     * @param subjectId      the subject this record is for
     * @param attendanceDate the date of attendance
     * @param status         PRESENT or ABSENT
     */
    public Attendance(int attendanceId, int studentId, int subjectId,
                      LocalDate attendanceDate, Status status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    /**
     * Full constructor including the transient studentName display field.
     *
     * @param attendanceId   the database primary key
     * @param studentId      the student this record belongs to
     * @param subjectId      the subject this record is for
     * @param attendanceDate the date of attendance
     * @param status         PRESENT or ABSENT
     * @param studentName    the display name of the student (from JOIN; may be null)
     */
    public Attendance(int attendanceId, int studentId, int subjectId,
                      LocalDate attendanceDate, Status status, String studentName) {
        this(attendanceId, studentId, subjectId, attendanceDate, status);
        this.studentName = studentName;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /**
     * @return the database primary key
     */
    public int getAttendanceId() {
        return attendanceId;
    }

    /**
     * @param attendanceId the primary key to set
     */
    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    /**
     * @return the student ID for this attendance record
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
     * @return the subject ID for this attendance record
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
     * @return the date of this attendance record
     */
    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    /**
     * @param attendanceDate the attendance date to set
     */
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    /**
     * @return the attendance status (PRESENT or ABSENT)
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
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
     * @param studentName the transient display name to set
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", studentId=" + studentId +
                ", subjectId=" + subjectId +
                ", attendanceDate=" + attendanceDate +
                ", status=" + status +
                ", studentName='" + studentName + '\'' +
                '}';
    }
}
