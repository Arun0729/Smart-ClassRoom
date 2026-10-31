package com.smartclassroom.controller;

import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.dao.SubmissionDAO;
import com.smartclassroom.model.Assignment;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.Subject;
import com.smartclassroom.model.Submission;
import com.smartclassroom.model.User;
import com.smartclassroom.service.AssignmentService;
import com.smartclassroom.service.ReportService;
import com.smartclassroom.util.NavigationUtil;
import com.smartclassroom.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Student Assignments screen.
 * Allows students to view assignments for their enrolled subjects,
 * check status and grades/feedback, and submit assignment files.
 */
public class StudentAssignmentsController implements Initializable {

    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private TableView<Assignment> assignmentTable;
    @FXML private TableColumn<Assignment, String> colTitle;
    @FXML private TableColumn<Assignment, String> colDeadline;
    @FXML private TableColumn<Assignment, String> colMaxMarks;
    @FXML private TableColumn<Assignment, String> colStatus;

    @FXML private Label statusLabel;
    @FXML private Label selectedTitleLabel;
    @FXML private Label selectedDescLabel;
    @FXML private Label selectedDeadlineLabel;
    @FXML private Label selectedMaxMarksLabel;
    @FXML private Label selectedStatusLabel;
    @FXML private Label marksLabel;
    @FXML private Label feedbackLabel;

    @FXML private Button btnSelectFile;
    @FXML private Button btnSubmit;
    @FXML private Label selectedFileLabel;

    private final AssignmentService assignmentService = new AssignmentService();
    private final ReportService reportService = new ReportService();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    private Student currentStudent;
    private Assignment selectedAssignment = null;
    private String selectedFilePath = null;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);
        clearDetails();

        // Load student
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentStudent = studentDAO.findByUserId(user.getUserId());
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load student: " + e.getMessage());
            return;
        }

        // Configure subject combobox
        subjectComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Subject s) {
                if (s == null) return "";
                String code = s.getSubjectCode() != null ? " (" + s.getSubjectCode() + ")" : "";
                return s.getSubjectName() + code;
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });

        // Load subjects
        loadSubjects();

        // Configure table columns
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        colDeadline.setCellValueFactory(cellData -> {
            Assignment a = cellData.getValue();
            if (a.getDeadline() != null) {
                return new SimpleStringProperty(a.getDeadline().format(dtf));
            }
            return new SimpleStringProperty("N/A");
        });

        colMaxMarks.setCellValueFactory(cellData -> {
            Assignment a = cellData.getValue();
            return new SimpleStringProperty(String.format("%.1f", a.getMaxMarks()));
        });

        colStatus.setCellValueFactory(cellData -> {
            Assignment a = cellData.getValue();
            return new SimpleStringProperty(a.getDerivedStatus() != null ? a.getDerivedStatus().name() : "PENDING");
        });

        // Status badge styling
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "EVALUATED" -> setStyle("-fx-text-fill: #0D47A1; -fx-font-weight: bold;");
                        case "SUBMITTED" -> setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
                        case "LATE_SUBMITTED" -> setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                        case "OVERDUE" -> setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Subject combo listener
        subjectComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAssignments();
            } else {
                assignmentTable.getItems().clear();
                clearDetails();
            }
        });

        // Table selection listener
        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedAssignment = newVal;
            if (newVal != null) {
                populateDetails(newVal);
            } else {
                clearDetails();
            }
        });
    }

    /**
     * Loads the enrolled subjects for this student.
     */
    private void loadSubjects() {
        if (currentStudent == null) return;
        try {
            List<Subject> subjects = reportService.getStudentSubjects(currentStudent.getStudentId());
            subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
            if (!subjects.isEmpty()) {
                subjectComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showStatus("Failed to load subjects: " + e.getMessage(), true);
        }
    }

    /**
     * Loads assignments for the selected subject.
     */
    @FXML
    private void loadAssignments() {
        Subject selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null || currentStudent == null) return;

        try {
            List<Assignment> assignments = assignmentService.getAssignmentsForStudent(
                    currentStudent.getStudentId(),
                    selectedSubject.getSubjectId()
            );
            assignmentTable.setItems(FXCollections.observableArrayList(assignments));
            if (!assignments.isEmpty()) {
                assignmentTable.getSelectionModel().selectFirst();
            } else {
                clearDetails();
            }
        } catch (Exception e) {
            showStatus("Error loading assignments: " + e.getMessage(), true);
        }
    }

    /**
     * Populates the assignment detail view and shows marks/feedback if available.
     */
    private void populateDetails(Assignment a) {
        selectedTitleLabel.setText(a.getTitle());
        selectedDescLabel.setText(a.getDescription() != null && !a.getDescription().isBlank()
                ? a.getDescription()
                : "No description provided.");

        selectedDeadlineLabel.setText("Deadline: " + (a.getDeadline() != null ? a.getDeadline().format(dtf) : "N/A"));
        selectedMaxMarksLabel.setText(String.format("Max Marks: %.1f", a.getMaxMarks()));

        String status = a.getDerivedStatus() != null ? a.getDerivedStatus().name() : "PENDING";
        selectedStatusLabel.setText("Status: " + status);

        // Load submission evaluation if exists
        try {
            Submission sub = submissionDAO.findByStudentAndAssignment(currentStudent.getStudentId(), a.getAssignmentId());
            if (sub != null) {
                if (sub.getMarks() != null) {
                    marksLabel.setText(String.format("Marks: %.1f / %.1f", sub.getMarks(), a.getMaxMarks()));
                    marksLabel.setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
                } else {
                    marksLabel.setText("Marks: Submitted (Pending Evaluation)");
                    marksLabel.setStyle("-fx-text-fill: #E65100;");
                }
                feedbackLabel.setText("Feedback: " + (sub.getFeedback() != null && !sub.getFeedback().isBlank()
                        ? sub.getFeedback()
                        : "No feedback yet."));
            } else {
                marksLabel.setText("Marks: Not Submitted");
                marksLabel.setStyle("-fx-text-fill: #757575;");
                feedbackLabel.setText("Feedback: -");
            }
        } catch (Exception e) {
            marksLabel.setText("Marks: Error loading");
            feedbackLabel.setText("Feedback: Error");
        }
    }

    /**
     * Clears all detail labels.
     */
    private void clearDetails() {
        selectedAssignment = null;
        selectedTitleLabel.setText("Select an assignment to view details");
        selectedDescLabel.setText("");
        selectedDeadlineLabel.setText("");
        selectedMaxMarksLabel.setText("");
        selectedStatusLabel.setText("");
        marksLabel.setText("Marks: -");
        feedbackLabel.setText("Feedback: -");
        selectedFilePath = null;
        selectedFileLabel.setText("No file selected");
    }

    /**
     * Opens a file chooser for selecting assignment submission file.
     */
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Assignment File to Submit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Allowed Documents (*.pdf, *.doc, *.docx, *.zip, *.txt, *.java)",
                        "*.pdf", "*.doc", "*.docx", "*.zip", "*.txt", "*.java"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) btnSelectFile.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            selectedFileLabel.setText(file.getName());
        }
    }

    /**
     * Submits the chosen file for the selected assignment.
     */
    @FXML
    private void handleSubmit() {
        if (selectedAssignment == null) {
            showStatus("Please select an assignment to submit.", true);
            return;
        }
        if (selectedFilePath == null || selectedFilePath.isBlank()) {
            showStatus("Please select a file to submit first.", true);
            return;
        }

        try {
            assignmentService.submitAssignment(
                    selectedAssignment.getAssignmentId(),
                    currentStudent.getStudentId(),
                    selectedFilePath
            );
            showStatus("Assignment submitted successfully!", false);
            selectedFilePath = null;
            selectedFileLabel.setText("No file selected");

            // Refresh table & details
            int selectedIdx = assignmentTable.getSelectionModel().getSelectedIndex();
            loadAssignments();
            if (selectedIdx >= 0 && selectedIdx < assignmentTable.getItems().size()) {
                assignmentTable.getSelectionModel().select(selectedIdx);
            }
        } catch (Exception e) {
            showStatus("Submission failed: " + e.getMessage(), true);
        }
    }

    /**
     * Displays a status message.
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        statusLabel.setVisible(true);
    }
}

