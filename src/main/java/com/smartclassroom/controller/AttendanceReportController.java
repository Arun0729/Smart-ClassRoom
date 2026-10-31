package com.smartclassroom.controller;

import com.smartclassroom.dao.ClassDAO;
import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.model.ClassRoom;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Subject;
import com.smartclassroom.model.User;
import com.smartclassroom.service.AttendanceService;
import com.smartclassroom.util.NavigationUtil;
import com.smartclassroom.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Attendance Report screen.
 * Supports two modes: per-subject attendance summary and low-attendance alert.
 */
public class AttendanceReportController implements Initializable {

    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private ComboBox<ClassRoom> classComboBox;
    @FXML private Spinner<Double> thresholdSpinner;
    @FXML private TableView<Object> reportTable;
    @FXML private TableColumn<Object, String> colRepStudentName;
    @FXML private TableColumn<Object, String> colRepPresent;
    @FXML private TableColumn<Object, String> colRepTotal;
    @FXML private TableColumn<Object, String> colRepPercent;
    @FXML private Button btnGenerateReport;
    @FXML private Label statusLabel;
    @FXML private Label reportTypeLabel;
    @FXML private RadioButton rbSubjectReport;
    @FXML private RadioButton rbLowAttendance;
    @FXML private ToggleGroup reportTypeGroup;

    private final AttendanceService attendanceService = new AttendanceService();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private Faculty currentFaculty;

    /**
     * Initializes the report screen. Loads faculty subjects and classes,
     * configures the spinner, and sets up toggle visibility.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);

        // Load current faculty
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentFaculty = facultyDAO.findByUserId(user.getUserId());
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load faculty: " + e.getMessage());
            return;
        }

        // Load subjects
        try {
            List<Subject> subjects = subjectDAO.findByFacultyId(currentFaculty.getFacultyId());
            subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        } catch (Exception e) {
            showStatus("Failed to load subjects: " + e.getMessage(), true);
        }

        // Load classes
        try {
            List<ClassRoom> classes = classDAO.findAll();
            classComboBox.setItems(FXCollections.observableArrayList(classes));
        } catch (Exception e) {
            showStatus("Failed to load classes: " + e.getMessage(), true);
        }

        // Threshold spinner (0..100, default 75, step 5)
        thresholdSpinner.setValueFactory(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 75, 5));
        thresholdSpinner.setEditable(true);

        // Toggle visibility of controls based on report type
        reportTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean isSubjectReport = newToggle == rbSubjectReport;
            subjectComboBox.setVisible(isSubjectReport);
            classComboBox.setVisible(!isSubjectReport);
            thresholdSpinner.setVisible(!isSubjectReport);
            reportTypeLabel.setText(isSubjectReport
                ? "Shows attendance counts per student for the selected subject."
                : "Lists students whose attendance % falls below the threshold.");
        });

        // Set initial state
        subjectComboBox.setVisible(true);
        classComboBox.setVisible(false);
        thresholdSpinner.setVisible(false);
        reportTypeLabel.setText("Shows attendance counts per student for the selected subject.");

        // Set up table columns with generic property extraction
        setupColumns();
    }

    /**
     * Configures the report table columns.
     * Columns are populated dynamically in handleGenerateReport().
     */
    private void setupColumns() {
        colRepStudentName.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof AttendanceService.StudentAttendanceSummary s) {
                return new SimpleStringProperty(
                    s.getStudent() != null ? s.getStudent().getFullName() : "");
            }
            return new SimpleStringProperty("");
        });
        colRepPresent.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof AttendanceService.StudentAttendanceSummary s) {
                return new SimpleStringProperty(String.valueOf(s.getPresentCount()));
            }
            return new SimpleStringProperty("");
        });
        colRepTotal.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof AttendanceService.StudentAttendanceSummary s) {
                return new SimpleStringProperty(String.valueOf(s.getTotalCount()));
            }
            return new SimpleStringProperty("");
        });
        colRepPercent.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof AttendanceService.StudentAttendanceSummary s) {
                return new SimpleStringProperty(String.format("%.1f%%", s.getPercentage()));
            }
            return new SimpleStringProperty("");
        });
    }

    /**
     * Generates the report based on the selected radio button.
     * Either a per-subject attendance summary or a low-attendance alert list.
     */
    @FXML
    private void handleGenerateReport() {
        try {
            if (rbSubjectReport.isSelected()) {
                Subject selectedSubject = subjectComboBox.getValue();
                if (selectedSubject == null) {
                    showStatus("Please select a subject.", true);
                    return;
                }
                List<AttendanceService.StudentAttendanceSummary> report =
                    attendanceService.getSubjectAttendanceSummaryList(selectedSubject.getSubjectId());
                reportTable.setItems(FXCollections.observableArrayList(report));
                showStatus("Report generated: " + report.size() + " student(s).", false);

            } else {
                // Low attendance
                ClassRoom selectedClass = classComboBox.getValue();
                if (selectedClass == null) {
                    showStatus("Please select a class.", true);
                    return;
                }
                double threshold = thresholdSpinner.getValue();
                List<AttendanceService.StudentAttendanceSummary> report =
                    attendanceService.getLowAttendanceReport(
                        selectedClass.getClassId(),
                        currentFaculty.getFacultyId(),
                        threshold);
                reportTable.setItems(FXCollections.observableArrayList(report));
                showStatus("Low attendance report: " + report.size() + " student(s) below " + threshold + "%.", false);
            }
        } catch (Exception e) {
            showStatus("Failed to generate report: " + e.getMessage(), true);
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error", "status-success", "status-info");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        statusLabel.setVisible(true);
    }
}
