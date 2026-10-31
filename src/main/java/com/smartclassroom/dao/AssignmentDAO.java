package com.smartclassroom.dao;

import com.smartclassroom.model.Assignment;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code assignments} table.
 * Handles creation, retrieval, updating, and deletion of {@link Assignment} entities.
 * All datetime values are stored as MySQL {@code DATETIME} and mapped via
 * {@link Timestamp#toLocalDateTime()}.
 */
public class AssignmentDAO {

    // -------------------------------------------------------------------------
    // SQL fragments shared by multiple query methods
    // -------------------------------------------------------------------------

    private static final String SELECT_JOIN =
            "SELECT a.assignment_id, a.title, a.description, a.subject_id, a.faculty_id, "
          + "       a.created_at, a.deadline, a.max_marks, "
          + "       sub.subject_name "
          + "FROM assignments a "
          + "JOIN subjects sub ON a.subject_id = sub.subject_id ";

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to an {@link Assignment}.
     */
    private Assignment mapRow(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(rs.getInt("assignment_id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));
        assignment.setSubjectId(rs.getInt("subject_id"));
        assignment.setFacultyId(rs.getInt("faculty_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            assignment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp deadline = rs.getTimestamp("deadline");
        if (deadline != null) {
            assignment.setDeadline(deadline.toLocalDateTime());
        }

        assignment.setMaxMarks(rs.getDouble("max_marks"));
        assignment.setSubjectName(rs.getString("subject_name"));
        return assignment;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new assignment into the {@code assignments} table and returns
     * the auto-generated {@code assignment_id}.
     * <p>Note: {@code created_at} is omitted from the INSERT and will default
     * to {@code CURRENT_TIMESTAMP} at the DB level.</p>
     *
     * @param assignment the Assignment to persist
     * @return the generated {@code assignment_id}, or {@code -1} on failure
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int save(Assignment assignment) {
        final String sql = "INSERT INTO assignments "
                         + "(title, description, subject_id, faculty_id, deadline, max_marks) "
                         + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, assignment.getTitle());
            pst.setString(2, assignment.getDescription());
            pst.setInt(3, assignment.getSubjectId());
            pst.setInt(4, assignment.getFacultyId());
            pst.setTimestamp(5, assignment.getDeadline() != null
                    ? Timestamp.valueOf(assignment.getDeadline()) : null);
            pst.setDouble(6, assignment.getMaxMarks());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.save failed for title='" + assignment.getTitle() + "': " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Finds a single assignment by its primary key, joined with subject info.
     *
     * @param id the {@code assignment_id} to look up
     * @return the matching {@link Assignment}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Assignment findById(int id) {
        final String sql = SELECT_JOIN + "WHERE a.assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.findById failed for id=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all assignments for a given subject, ordered by deadline.
     *
     * @param subjectId the target {@code subject_id}
     * @return list of matching assignments
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Assignment> findBySubjectId(int subjectId) {
        final String sql = SELECT_JOIN + "WHERE a.subject_id = ? ORDER BY a.deadline";

        List<Assignment> assignments = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, subjectId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.findBySubjectId failed for subjectId=" + subjectId + ": " + e.getMessage(), e);
        }
        return assignments;
    }

    /**
     * Returns all assignments created by a given faculty member, ordered by deadline.
     *
     * @param facultyId the target {@code faculty_id}
     * @return list of matching assignments
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Assignment> findByFacultyId(int facultyId) {
        final String sql = SELECT_JOIN + "WHERE a.faculty_id = ? ORDER BY a.deadline";

        List<Assignment> assignments = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.findByFacultyId failed for facultyId=" + facultyId + ": " + e.getMessage(), e);
        }
        return assignments;
    }

    /**
     * Updates the editable fields (title, description, deadline, maxMarks)
     * of an existing assignment.
     *
     * @param assignment the assignment with updated values; must have valid {@code assignmentId}
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean update(Assignment assignment) {
        final String sql = "UPDATE assignments SET title = ?, description = ?, "
                         + "deadline = ?, max_marks = ? WHERE assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, assignment.getTitle());
            pst.setString(2, assignment.getDescription());
            pst.setTimestamp(3, assignment.getDeadline() != null
                    ? Timestamp.valueOf(assignment.getDeadline()) : null);
            pst.setDouble(4, assignment.getMaxMarks());
            pst.setInt(5, assignment.getAssignmentId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.update failed for assignmentId=" + assignment.getAssignmentId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Permanently deletes an assignment record. Associated submissions should
     * be handled via FK cascade or removed prior to calling this method.
     *
     * @param assignmentId the ID of the assignment to delete
     * @return {@code true} if at least one row was deleted
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean delete(int assignmentId) {
        final String sql = "DELETE FROM assignments WHERE assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, assignmentId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("AssignmentDAO.delete failed for assignmentId=" + assignmentId + ": " + e.getMessage(), e);
        }
    }
}
