package com.smartclassroom.dao;

import com.smartclassroom.model.Subject;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code subjects} table.
 * Handles CRUD operations for {@link Subject} entities, including JOIN queries
 * to populate the {@code facultyName} and {@code className} display fields.
 */
public class SubjectDAO {

    // -------------------------------------------------------------------------
    // SQL fragments shared by multiple query methods
    // -------------------------------------------------------------------------

    private static final String SELECT_JOIN =
            "SELECT sub.subject_id, sub.subject_name, sub.subject_code, sub.class_id, sub.faculty_id, "
          + "       c.class_name, f.full_name AS faculty_name "
          + "FROM subjects sub "
          + "JOIN classes c ON sub.class_id = c.class_id "
          + "LEFT JOIN faculty f ON sub.faculty_id = f.faculty_id ";

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a {@link Subject}.
     */
    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setClassId(rs.getInt("class_id"));
        subject.setFacultyId(rs.getInt("faculty_id"));
        subject.setClassName(rs.getString("class_name"));
        subject.setFacultyName(rs.getString("faculty_name"));
        return subject;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new subject record into the {@code subjects} table.
     *
     * @param subject the Subject to persist (subject_id is auto-generated)
     * @return {@code true} if the insert succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean save(Subject subject) {
        final String sql = "INSERT INTO subjects (subject_name, subject_code, class_id, faculty_id) "
                         + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, subject.getSubjectName());
            pst.setString(2, subject.getSubjectCode());
            pst.setInt(3, subject.getClassId());
            pst.setInt(4, subject.getFacultyId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.save failed for subjectName='" + subject.getSubjectName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns all subjects joined with class and faculty display information.
     *
     * @return list of all {@link Subject} objects, ordered by subject name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Subject> findAll() {
        final String sql = SELECT_JOIN + "ORDER BY sub.subject_name";

        List<Subject> subjects = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                subjects.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.findAll failed: " + e.getMessage(), e);
        }
        return subjects;
    }

    /**
     * Finds a single subject by its primary key.
     *
     * @param subjectId the {@code subject_id} to look up
     * @return the matching {@link Subject} with display fields populated, or {@code null}
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public Subject findById(int subjectId) {
        final String sql = SELECT_JOIN + "WHERE sub.subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, subjectId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.findById failed for subjectId=" + subjectId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all subjects associated with the specified class.
     *
     * @param classId the target {@code class_id}
     * @return list of matching subjects, ordered by subject name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Subject> findByClassId(int classId) {
        final String sql = SELECT_JOIN + "WHERE sub.class_id = ? ORDER BY sub.subject_name";

        List<Subject> subjects = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, classId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.findByClassId failed for classId=" + classId + ": " + e.getMessage(), e);
        }
        return subjects;
    }

    /**
     * Returns all subjects taught by the specified faculty member.
     *
     * @param facultyId the target {@code faculty_id}
     * @return list of matching subjects, ordered by subject name
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<Subject> findByFacultyId(int facultyId) {
        final String sql = SELECT_JOIN + "WHERE sub.faculty_id = ? ORDER BY sub.subject_name";

        List<Subject> subjects = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.findByFacultyId failed for facultyId=" + facultyId + ": " + e.getMessage(), e);
        }
        return subjects;
    }

    /**
     * Updates an existing subject record. The {@code subjectId} must be set.
     *
     * @param subject the Subject with updated values
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean update(Subject subject) {
        final String sql = "UPDATE subjects SET subject_name = ?, subject_code = ?, "
                         + "class_id = ?, faculty_id = ? WHERE subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, subject.getSubjectName());
            pst.setString(2, subject.getSubjectCode());
            pst.setInt(3, subject.getClassId());
            pst.setInt(4, subject.getFacultyId());
            pst.setInt(5, subject.getSubjectId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.update failed for subjectId=" + subject.getSubjectId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a subject record by its primary key.
     *
     * @param subjectId the {@code subject_id} to delete
     * @return {@code true} if at least one row was deleted
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean delete(int subjectId) {
        final String sql = "DELETE FROM subjects WHERE subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, subjectId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.delete failed for subjectId=" + subjectId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns the total number of subject records in the database.
     *
     * @return total subject count
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int count() {
        final String sql = "SELECT COUNT(*) FROM subjects";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SubjectDAO.count failed: " + e.getMessage(), e);
        }
        return 0;
    }
}
