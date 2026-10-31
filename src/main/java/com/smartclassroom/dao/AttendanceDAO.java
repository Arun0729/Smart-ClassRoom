package com.smartclassroom.dao;

import com.smartclassroom.model.Attendance;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code attendance} table.
 * Supports batch upserts, filtered queries, and attendance summary aggregation.
 */
public class AttendanceDAO {

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to an {@link Attendance} object.
     * Reads {@code student_name} only when the JOIN with {@code students} is
     * part of the query (may be {@code null} otherwise).
     */
    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSubjectId(rs.getInt("subject_id"));

        Date date = rs.getDate("attendance_date");
        attendance.setAttendanceDate(date != null ? date.toLocalDate() : null);

        String statusStr = rs.getString("status");
        attendance.setStatus(Attendance.Status.valueOf(statusStr));

        // Only available when the students JOIN is present
        try {
            attendance.setStudentName(rs.getString("full_name"));
        } catch (SQLException ignored) {
            // Column not present in this query variant – leave null
        }

        return attendance;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Saves a batch of attendance records in a single transaction using an
     * {@code INSERT … ON DUPLICATE KEY UPDATE} upsert pattern.
     * This allows re-recording attendance for the same student/subject/date
     * without a unique-key violation.
     *
     * @param records the list of {@link Attendance} objects to persist
     * @return {@code true} if all records were processed successfully
     * @throws RuntimeException wrapping {@link SQLException} on DB error or rollback failure
     */
    public boolean saveBatch(List<Attendance> records) {
        if (records == null || records.isEmpty()) {
            return true;
        }

        final String sql = "INSERT INTO attendance (student_id, subject_id, attendance_date, status) "
                         + "VALUES (?, ?, ?, ?) "
                         + "ON DUPLICATE KEY UPDATE status = VALUES(status)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                for (Attendance record : records) {
                    pst.setInt(1, record.getStudentId());
                    pst.setInt(2, record.getSubjectId());
                    pst.setDate(3, Date.valueOf(record.getAttendanceDate()));
                    pst.setString(4, record.getStatus().name());
                    pst.addBatch();
                }
                pst.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("AttendanceDAO.saveBatch rollback failed: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw new RuntimeException("AttendanceDAO.saveBatch failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Swallow – data committed or rolled back already
                }
            }
        }
    }

    /**
     * Returns all attendance records for a given subject on a specific date,
     * joined with student names for display.
     *
     * @param subjectId the subject to filter by
     * @param date      the date to filter by
     * @return list of attendance records ordered by student full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Attendance> findBySubjectAndDate(int subjectId, LocalDate date) {
        final String sql =
                "SELECT a.attendance_id, a.student_id, a.subject_id, a.attendance_date, a.status, "
              + "       s.full_name "
              + "FROM attendance a "
              + "JOIN students s ON a.student_id = s.student_id "
              + "WHERE a.subject_id = ? AND a.attendance_date = ? "
              + "ORDER BY s.full_name";

        List<Attendance> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, subjectId);
            pst.setDate(2, Date.valueOf(date));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.findBySubjectAndDate failed for subjectId=" + subjectId + ", date=" + date + ": " + e.getMessage(), e);
        }
        return records;
    }

    /**
     * Returns all attendance records for a specific student in a specific subject,
     * ordered by date descending (most recent first).
     *
     * @param studentId the student to filter by
     * @param subjectId the subject to filter by
     * @return list of matching attendance records
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Attendance> findByStudentAndSubject(int studentId, int subjectId) {
        final String sql =
                "SELECT attendance_id, student_id, subject_id, attendance_date, status "
              + "FROM attendance "
              + "WHERE student_id = ? AND subject_id = ? "
              + "ORDER BY attendance_date DESC";

        List<Attendance> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            pst.setInt(2, subjectId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance();
                    a.setAttendanceId(rs.getInt("attendance_id"));
                    a.setStudentId(rs.getInt("student_id"));
                    a.setSubjectId(rs.getInt("subject_id"));
                    Date d = rs.getDate("attendance_date");
                    a.setAttendanceDate(d != null ? d.toLocalDate() : null);
                    a.setStatus(Attendance.Status.valueOf(rs.getString("status")));
                    records.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.findByStudentAndSubject failed for studentId=" + studentId + ", subjectId=" + subjectId + ": " + e.getMessage(), e);
        }
        return records;
    }

    /**
     * Returns all attendance records for a given student across all subjects,
     * joined with subject name for display. Ordered by date descending.
     *
     * @param studentId the student whose attendance is retrieved
     * @return list of attendance records with subject name populated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Attendance> findByStudentId(int studentId) {
        final String sql =
                "SELECT a.attendance_id, a.student_id, a.subject_id, a.attendance_date, a.status, "
              + "       sub.subject_name "
              + "FROM attendance a "
              + "JOIN subjects sub ON a.subject_id = sub.subject_id "
              + "WHERE a.student_id = ? "
              + "ORDER BY a.attendance_date DESC";

        List<Attendance> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance();
                    a.setAttendanceId(rs.getInt("attendance_id"));
                    a.setStudentId(rs.getInt("student_id"));
                    a.setSubjectId(rs.getInt("subject_id"));
                    Date d = rs.getDate("attendance_date");
                    a.setAttendanceDate(d != null ? d.toLocalDate() : null);
                    a.setStatus(Attendance.Status.valueOf(rs.getString("status")));
                    // Reuse studentName field to carry subject_name for display
                    a.setStudentName(rs.getString("subject_name"));
                    records.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.findByStudentId failed for studentId=" + studentId + ": " + e.getMessage(), e);
        }
        return records;
    }

    /**
     * Checks whether an attendance record already exists for the given
     * student, subject, and date combination.
     *
     * @param studentId the student ID
     * @param subjectId the subject ID
     * @param date      the attendance date
     * @return {@code true} if at least one matching record exists
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean hasRecord(int studentId, int subjectId, LocalDate date) {
        final String sql = "SELECT COUNT(*) FROM attendance "
                         + "WHERE student_id = ? AND subject_id = ? AND attendance_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            pst.setInt(2, subjectId);
            pst.setDate(3, Date.valueOf(date));

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.hasRecord failed for studentId=" + studentId + ", subjectId=" + subjectId + ", date=" + date + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Updates the status of a specific attendance record.
     *
     * @param attendanceId the record to update
     * @param status       the new {@link Attendance.Status}
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean updateStatus(int attendanceId, Attendance.Status status) {
        final String sql = "UPDATE attendance SET status = ? WHERE attendance_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, status.name());
            pst.setInt(2, attendanceId);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.updateStatus failed for attendanceId=" + attendanceId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Computes the attendance summary (present count and total count) for a
     * specific student in a specific subject.
     *
     * @param studentId the student ID
     * @param subjectId the subject ID
     * @return {@code int[]{presentCount, totalCount}}; both values are 0 if no records exist
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int[] getAttendanceSummary(int studentId, int subjectId) {
        final String sql =
                "SELECT "
              + "  SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, "
              + "  COUNT(*) AS total_count "
              + "FROM attendance "
              + "WHERE student_id = ? AND subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            pst.setInt(2, subjectId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int presentCount = rs.getInt("present_count");
                    int totalCount   = rs.getInt("total_count");
                    return new int[]{presentCount, totalCount};
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "AttendanceDAO.getAttendanceSummary failed for studentId=" + studentId + ", subjectId=" + subjectId + ": " + e.getMessage(), e);
        }
        return new int[]{0, 0};
    }
}
