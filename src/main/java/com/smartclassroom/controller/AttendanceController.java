package com.smartclassroom.controller;

import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.model.Attendance;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.Subject;
import com.smartclassroom.model.User;
import com.smartclassroom.service.AttendanceService;
import com.smartclassroom.util.NavigationUtil;
import com.smartclassroom.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for the Attendance Marking screen.
 * Loads students for a chosen subject and date, allows per-student status toggle,
 * and saves the batch attendance record to the database.
 */
public class AttendanceController implements Initializable {

    // ── Inner helper class for table rows ─────────────────────────────────────

    /**
     * Helper JavaFX model class representing a single row in the attendance table.
     * Uses JavaFX properties so the TableView can observe changes.
     */
    public static class AttendanceRow {
        private final SimpleIntegerProperty studentId;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty status;
        private final int originalAttendanceId;

        /**
         * Constructs an AttendanceRow.
         *
         * @param studentId         the student's ID
         * @param studentName       the student's full name
         * @param status            "PRESENT" or "ABSENT"
         * @param originalAttendanceId the existing attendance record ID (0 if new)
         */
        public AttendanceRow(int studentId, String studentName, String status, int originalAttendanceId) {
            this.studentId = new SimpleIntegerProperty(studentId);
            this.studentName = new SimpleStringProperty(studentName);
            this.status = new SimpleStringProperty(status);
            this.originalAttendanceId = originalAttendanceId;
        }

        public int getStudentId() { return studentId.get(); }
        public String getStudentName() { return studentName.get(); }
        public String getStatus() { return status.get(); }
        public void setStatus(String s) { status.set(s); }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public SimpleIntegerProperty studentIdProperty() { return studentId; }
        public int getOriginalAttendanceId() { return originalAttendanceId; }
    }

    // ── FXML fields ────────────────────────────────────────────────────────────
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TableView<AttendanceRow> attendanceTable;
    @FXML private TableColumn<AttendanceRow, String> colStudentName;
    @FXML private TableColumn<AttendanceRow, String> colStatus;
    @FXML private Button btnLoadStudents;
    @FXML private Button btnSaveAttendance;
    @FXML private Button btnMarkAllPresent;
    @FXML private Label statusLabel;
    @FXML private Label recordCountLabel;

    // ── Services / DAOs ────────────────────────────────────────────────────────
    private final AttendanceService attendanceService = new AttendanceService();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private Faculty currentFaculty;

    /**
     * Initializes the attendance screen. Loads the faculty's subjects into the combo box,
     * sets today's date, and configures the table columns.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);
        recordCountLabel.setText("");

        // Load current faculty
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentFaculty = facultyDAO.findByUserId(user.getUserId());
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load faculty: " + e.getMessage());
            return;
        }

        // Populate subjects for this faculty
        try {
            List<Subject> subjects = subjectDAO.findByFacultyId(currentFaculty.getFacultyId());
            subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        } catch (Exception e) {
            showStatus("Failed to load subjects: " + e.getMessage(), true);
        }

        // Default date to today
        datePicker.setValue(LocalDate.now());

        // Configure student name column
        colStudentName.setCellValueFactory(row -> row.getValue().studentNameProperty());

        // Configure status column with ComboBox cell
        colStatus.setCellValueFactory(row -> row.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo =
                new ComboBox<>(FXCollections.observableArrayList("PRESENT", "ABSENT"));

            {
                combo.setOnAction(e -> {
                    AttendanceRow rowItem = getTableRow() != null ? getTableRow().getItem() : null;
                    if (rowItem != null) {
                        rowItem.setStatus(combo.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });
    }

    /**
     * Loads the student list for the selected subject and date.
     * Pre-fills statuses if attendance records already exist for that date.
     */
    @FXML
    private void handleLoadStudents() {
        Subject selectedSubject = subjectComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedSubject == null) {
            showStatus("Please select a subject.", true);
            return;
        }
        if (selectedDate == null) {
            showStatus("Please select a date.", true);
            return;
        }

        try {
            // Load students for this subject's class
            List<Student> students = studentDAO.findByClassId(selectedSubject.getClassId());

            // Check existing attendance for this date
            List<Attendance> existing = attendanceService.getAttendanceForDate(
                selectedSubject.getSubjectId(), selectedDate);

            // Build a map from studentId → existing record for quick lookup
            Map<Integer, Attendance> existingMap = existing.stream()
                .collect(Collectors.toMap(Attendance::getStudentId, Function.identity()));

            // Build rows
            List<AttendanceRow> rows = new ArrayList<>();
            for (Student s : students) {
                Attendance att = existingMap.get(s.getStudentId());
                String status = (att != null) ? att.getStatus().name() : "PRESENT";
                int attId = (att != null) ? att.getAttendanceId() : 0;
                rows.add(new AttendanceRow(s.getStudentId(), s.getFullName(), status, attId));
            }

            attendanceTable.setItems(FXCollections.observableArrayList(rows));
            recordCountLabel.setText(students.size() + " students loaded");
            showStatus("Students loaded successfully.", false);

        } catch (Exception e) {
            showStatus("Failed to load students: " + e.getMessage(), true);
        }
    }

    /**
     * Saves the current attendance entries for all rows in the table.
     */
    @FXML
    private void handleSaveAttendance() {
        Subject selectedSubject = subjectComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedSubject == null || selectedDate == null) {
            showStatus("Please load students first.", true);
            return;
        }
        if (attendanceTable.getItems().isEmpty()) {
            showStatus("No students to save attendance for.", true);
            return;
        }

        try {
            List<Attendance> records = new ArrayList<>();
            for (AttendanceRow row : attendanceTable.getItems()) {
                Attendance a = new Attendance();
                a.setAttendanceId(row.getOriginalAttendanceId());
                a.setStudentId(row.getStudentId());
                a.setSubjectId(selectedSubject.getSubjectId());
                a.setAttendanceDate(selectedDate);
                a.setStatus(Attendance.Status.valueOf(row.getStatus()));
                records.add(a);
            }
            attendanceService.markAttendance(records, currentFaculty.getFacultyId());
            showStatus("Attendance saved successfully for " + records.size() + " students.", false);

        } catch (Exception e) {
            showStatus("Failed to save attendance: " + e.getMessage(), true);
        }
    }

    /**
     * Sets all rows in the attendance table to "PRESENT" status.
     */
    @FXML
    private void handleMarkAllPresent() {
        for (AttendanceRow row : attendanceTable.getItems()) {
            row.setStatus("PRESENT");
        }
        // Refresh the table to show updated values
        attendanceTable.refresh();
        showStatus("All students marked as Present.", false);
    }

    /**
     * Shows a status message with appropriate styling.
     *
     * @param msg     the message to display
     * @param isError true for error, false for success/info
     */
    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error", "status-success", "status-info");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        statusLabel.setVisible(true);
    }
}
