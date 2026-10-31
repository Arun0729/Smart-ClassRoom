package com.smartclassroom.dao;

import com.smartclassroom.model.Submission;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code submissions} table.
 * Supports student submission lifecycle: create/resubmit, retrieval by various
 * filters, and faculty evaluation (marks + feedback).
 */
public class SubmissionDAO {

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a {@link Submission}.
     * Reads {@code student_name} and {@code assignment_title} display columns
     * when available (may be {@code null} in some query variants).
     */
    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission submission = new Submission();
        submission.setSubmissionId(rs.getInt("submission_id"));
        submission.setAssignmentId(rs.getInt("assignment_id"));
        submission.setStudentId(rs.getInt("student_id"));
        submission.setFilePath(rs.getString("file_path"));

        Timestamp submittedAt = rs.getTimestamp("submitted_at");
        if (submittedAt != null) {
            submission.setSubmittedAt(submittedAt.toLocalDateTime());
        }

        // marks is nullable – use getObject to detect SQL NULL
        double marksVal = rs.getDouble("marks");
        if (!rs.wasNull()) {
            submission.setMarks(marksVal);
        }

        submission.setFeedback(rs.getString("feedback"));

        String evalStatus = rs.getString("evaluation_status");
        if (evalStatus != null) {
            submission.setEvaluationStatus(Submission.EvaluationStatus.valueOf(evalStatus));
        }

        // Display fields – safe individual tries to remain compatible with all queries
        try { submission.setStudentName(rs.getString("student_name")); } catch (SQLException ignored) {}
        try { submission.setAssignmentTitle(rs.getString("assignment_title")); } catch (SQLException ignored) {}

        return submission;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Saves a new submission or updates an existing one (resubmission) using an
     * {@code INSERT … ON DUPLICATE KEY UPDATE} upsert. The unique constraint must
     * be defined on {@code (assignment_id, student_id)} in the DB schema.
     *
     * @param submission the Submission to persist
     * @return {@code true} if the operation affected at least one row
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean save(Submission submission) {
        final String sql =
                "INSERT INTO submissions (assignment_id, student_id, file_path, submitted_at, evaluation_status) "
              + "VALUES (?, ?, ?, ?, 'SUBMITTED') "
              + "ON DUPLICATE KEY UPDATE "
              + "  file_path = VALUES(file_path), "
              + "  submitted_at = VALUES(submitted_at)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, submission.getAssignmentId());
            pst.setInt(2, submission.getStudentId());
            pst.setString(3, submission.getFilePath());
            pst.setTimestamp(4, submission.getSubmittedAt() != null
                    ? Timestamp.valueOf(submission.getSubmittedAt()) : new Timestamp(System.currentTimeMillis()));

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "SubmissionDAO.save failed for assignmentId=" + submission.getAssignmentId()
                  + ", studentId=" + submission.getStudentId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all submissions for a given assignment, joined with student and
     * assignment title information for display in faculty evaluation screens.
     *
     * @param assignmentId the assignment whose submissions are retrieved
     * @return list of submissions ordered by student full name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Submission> findByAssignmentId(int assignmentId) {
        final String sql =
                "SELECT sub.submission_id, sub.assignment_id, sub.student_id, sub.file_path, "
              + "       sub.submitted_at, sub.marks, sub.feedback, sub.evaluation_status, "
              + "       st.full_name AS student_name, a.title AS assignment_title "
              + "FROM submissions sub "
              + "JOIN students st ON sub.student_id = st.student_id "
              + "JOIN assignments a ON sub.assignment_id = a.assignment_id "
              + "WHERE sub.assignment_id = ? "
              + "ORDER BY st.full_name";

        List<Submission> submissions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, assignmentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubmissionDAO.findByAssignmentId failed for assignmentId=" + assignmentId + ": " + e.getMessage(), e);
        }
        return submissions;
    }

    /**
     * Finds a specific submission by student and assignment combination.
     * Useful for checking whether a student has already submitted an assignment.
     *
     * @param studentId    the student ID
     * @param assignmentId the assignment ID
     * @return the matching {@link Submission}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Submission findByStudentAndAssignment(int studentId, int assignmentId) {
        final String sql =
                "SELECT sub.submission_id, sub.assignment_id, sub.student_id, sub.file_path, "
              + "       sub.submitted_at, sub.marks, sub.feedback, sub.evaluation_status, "
              + "       st.full_name AS student_name, a.title AS assignment_title "
              + "FROM submissions sub "
              + "JOIN students st ON sub.student_id = st.student_id "
              + "JOIN assignments a ON sub.assignment_id = a.assignment_id "
              + "WHERE sub.student_id = ? AND sub.assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            pst.setInt(2, assignmentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "SubmissionDAO.findByStudentAndAssignment failed for studentId=" + studentId
                  + ", assignmentId=" + assignmentId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all submissions made by a specific student, joined with assignment
     * title and deadline information for display in the student dashboard.
     * Ordered by assignment deadline descending.
     *
     * @param studentId the student whose submissions are retrieved
     * @return list of submissions with assignment context
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Submission> findByStudentId(int studentId) {
        final String sql =
                "SELECT sub.submission_id, sub.assignment_id, sub.student_id, sub.file_path, "
              + "       sub.submitted_at, sub.marks, sub.feedback, sub.evaluation_status, "
              + "       a.title AS assignment_title, a.deadline, a.max_marks "
              + "FROM submissions sub "
              + "JOIN assignments a ON sub.assignment_id = a.assignment_id "
              + "WHERE sub.student_id = ? "
              + "ORDER BY a.deadline DESC";

        List<Submission> submissions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Submission s = new Submission();
                    s.setSubmissionId(rs.getInt("submission_id"));
                    s.setAssignmentId(rs.getInt("assignment_id"));
                    s.setStudentId(rs.getInt("student_id"));
                    s.setFilePath(rs.getString("file_path"));

                    Timestamp submittedAt = rs.getTimestamp("submitted_at");
                    if (submittedAt != null) {
                        s.setSubmittedAt(submittedAt.toLocalDateTime());
                    }

                    double marksVal = rs.getDouble("marks");
                    if (!rs.wasNull()) {
                        s.setMarks(marksVal);
                    }

                    s.setFeedback(rs.getString("feedback"));

                    String evalStatus = rs.getString("evaluation_status");
                    if (evalStatus != null) {
                        s.setEvaluationStatus(Submission.EvaluationStatus.valueOf(evalStatus));
                    }

                    s.setAssignmentTitle(rs.getString("assignment_title"));
                    submissions.add(s);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubmissionDAO.findByStudentId failed for studentId=" + studentId + ": " + e.getMessage(), e);
        }
        return submissions;
    }

    /**
     * Records evaluation results (marks and feedback) for a submission, updating
     * its status to {@code EVALUATED}.
     *
     * @param submissionId the ID of the submission to evaluate
     * @param marks        the marks awarded (must be {@code <= maxMarks})
     * @param feedback     optional textual feedback from the faculty
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean updateEvaluation(int submissionId, double marks, String feedback) {
        final String sql = "UPDATE submissions SET marks = ?, feedback = ?, "
                         + "evaluation_status = 'EVALUATED' WHERE submission_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDouble(1, marks);
            pst.setString(2, feedback);
            pst.setInt(3, submissionId);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SubmissionDAO.updateEvaluation failed for submissionId=" + submissionId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether a student has already submitted a particular assignment.
     *
     * @param studentId    the student ID
     * @param assignmentId the assignment ID
     * @return {@code true} if a submission record exists
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean existsByStudentAndAssignment(int studentId, int assignmentId) {
        final String sql = "SELECT COUNT(*) FROM submissions WHERE student_id = ? AND assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, studentId);
            pst.setInt(2, assignmentId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "SubmissionDAO.existsByStudentAndAssignment failed for studentId=" + studentId
                  + ", assignmentId=" + assignmentId + ": " + e.getMessage(), e);
        }
        return false;
    }
}
