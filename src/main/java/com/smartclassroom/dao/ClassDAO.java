package com.smartclassroom.dao;

import com.smartclassroom.model.ClassRoom;
import com.smartclassroom.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code classes} table.
 * Handles CRUD operations for {@link ClassRoom} entities.
 */
public class ClassDAO {

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a {@link ClassRoom}.
     */
    private ClassRoom mapRow(ResultSet rs) throws SQLException {
        ClassRoom classRoom = new ClassRoom();
        classRoom.setClassId(rs.getInt("class_id"));
        classRoom.setClassName(rs.getString("class_name"));
        classRoom.setDepartment(rs.getString("department"));
        classRoom.setAcademicYear(rs.getString("academic_year"));
        return classRoom;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new class record into the {@code classes} table.
     *
     * @param classRoom the ClassRoom to persist (class_id is auto-generated)
     * @return {@code true} if the insert succeeded
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean save(ClassRoom classRoom) {
        final String sql = "INSERT INTO classes (class_name, department, academic_year) "
                         + "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, classRoom.getClassName());
            pst.setString(2, classRoom.getDepartment());
            pst.setString(3, classRoom.getAcademicYear());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.save failed for className='" + classRoom.getClassName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns all class records ordered alphabetically by class name.
     *
     * @return list of all {@link ClassRoom} objects
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public List<ClassRoom> findAll() {
        final String sql = "SELECT class_id, class_name, department, academic_year "
                         + "FROM classes ORDER BY class_name";

        List<ClassRoom> classes = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                classes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.findAll failed: " + e.getMessage(), e);
        }
        return classes;
    }

    /**
     * Finds a single class by its primary key.
     *
     * @param classId the {@code class_id} to look up
     * @return the matching {@link ClassRoom}, or {@code null} if not found
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public ClassRoom findById(int classId) {
        final String sql = "SELECT class_id, class_name, department, academic_year "
                         + "FROM classes WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, classId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.findById failed for classId=" + classId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Updates an existing class record. The {@code class_id} field must be set.
     *
     * @param classRoom the ClassRoom with updated values
     * @return {@code true} if at least one row was updated
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean update(ClassRoom classRoom) {
        final String sql = "UPDATE classes SET class_name = ?, department = ?, academic_year = ? "
                         + "WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, classRoom.getClassName());
            pst.setString(2, classRoom.getDepartment());
            pst.setString(3, classRoom.getAcademicYear());
            pst.setInt(4, classRoom.getClassId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.update failed for classId=" + classRoom.getClassId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a class record by its primary key.
     *
     * @param classId the {@code class_id} to delete
     * @return {@code true} if at least one row was deleted
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public boolean delete(int classId) {
        final String sql = "DELETE FROM classes WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, classId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.delete failed for classId=" + classId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns the total number of class records in the database.
     *
     * @return total class count
     * @throws RuntimeException wrapping {@link SQLException} on DB error
     */
    public int count() {
        final String sql = "SELECT COUNT(*) FROM classes";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("ClassDAO.count failed: " + e.getMessage(), e);
        }
        return 0;
    }
}
