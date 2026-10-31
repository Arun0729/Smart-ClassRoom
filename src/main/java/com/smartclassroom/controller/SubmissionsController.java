package com.smartclassroom.controller;

import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.model.Assignment;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Submission;
import com.smartclassroom.model.User;
import com.smartclassroom.service.AssignmentService;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Submission Evaluation screen (Faculty view).
 * Allows faculty to view student submissions for their assignments
 * and assign marks and feedback.
 */
public class SubmissionsController implements Initializable {

    @FXML private ComboBox<Assignment> assignmentComboBox;
    @FXML private TableView<Submission> submissionsTable;
    @FXML private TableColumn<Submission, String> colStudent;
    @FXML private TableColumn<Submission, String> colSubmittedAt;
    @FXML private TableColumn<Submission, String> colEvalStatus;
    @FXML private TableColumn<Submission, String> colMarks;

    @FXML private TextField marksField;
    @FXML private TextArea feedbackArea;
    @FXML private Button btnEvaluate;
    @FXML private Button btnLoadSubmissions;

    @FXML private Label statusLabel;
    @FXML private Label assignmentInfoLabel;
    @FXML private Label submissionCountLabel;
    @FXML private Label selectedStudentLabel;
    @FXML private Label filePathLabel;

    private final AssignmentService assignmentService = new AssignmentService();
    private final FacultyDAO facultyDAO = new FacultyDAO();

    private Faculty currentFaculty;
    private Assignment selectedAssignment = null;
    private Submission selectedSubmission = null;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);
        selectedStudentLabel.setText("None selected");
        filePathLabel.setText("N/A");
        assignmentInfoLabel.setText("");
        submissionCountLabel.setText("");

        // Load current faculty
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentFaculty = facultyDAO.findByUserId(user.getUserId());
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load faculty: " + e.getMessage());
            return;
        }

        // Configure assignment combo converter
        assignmentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Assignment a) {
                if (a == null) return "";
                String subj = a.getSubjectName() != null ? " [" + a.getSubjectName() + "]" : "";
                return a.getTitle() + subj;
            }

            @Override
            public Assignment fromString(String string) {
                return null;
            }
        });

        // Load faculty assignments
        loadAssignments();

        // Listen for assignment selection changes
        assignmentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedAssignment = newVal;
            if (newVal != null) {
                String deadlineStr = newVal.getDeadline() != null ? newVal.getDeadline().format(dtf) : "N/A";
                assignmentInfoLabel.setText(String.format("Max Marks: %.1f | Deadline: %s",
                        newVal.getMaxMarks(), deadlineStr));
                handleLoadSubmissions();
            } else {
                assignmentInfoLabel.setText("");
                submissionsTable.getItems().clear();
                submissionCountLabel.setText("");
            }
            clearEvaluationForm();
        });

        // Set up table columns
        colStudent.setCellValueFactory(cellData -> {
            Submission s = cellData.getValue();
            String name = s.getStudentName() != null ? s.getStudentName() : "Student #" + s.getStudentId();
            return new SimpleStringProperty(name);
        });

        colSubmittedAt.setCellValueFactory(cellData -> {
            Submission s = cellData.getValue();
            if (s.getSubmittedAt() != null) {
                return new SimpleStringProperty(s.getSubmittedAt().format(dtf));
            }
            return new SimpleStringProperty("N/A");
        });

        colEvalStatus.setCellValueFactory(cellData -> {
            Submission s = cellData.getValue();
            return new SimpleStringProperty(s.getEvaluationStatus() != null ? s.getEvaluationStatus().name() : "N/A");
        });

        // Style status badge cell
        colEvalStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("EVALUATED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colMarks.setCellValueFactory(cellData -> {
            Submission s = cellData.getValue();
            if (s.getMarks() != null) {
                return new SimpleStringProperty(String.format("%.1f", s.getMarks()));
            }
            return new SimpleStringProperty("Pending");
        });

        // Table selection listener
        submissionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSubmission = newVal;
            if (newVal != null) {
                populateEvaluationForm(newVal);
            } else {
                clearEvaluationForm();
            }
        });
    }

    /**
     * Loads all assignments created by the current faculty member.
     */
    private void loadAssignments() {
        if (currentFaculty == null) return;
        try {
            List<Assignment> list = assignmentService.getAssignmentsByFaculty(currentFaculty.getFacultyId());
            assignmentComboBox.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                assignmentComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showStatus("Failed to load assignments: " + e.getMessage(), true);
        }
    }

    /**
     * Loads submissions for the selected assignment.
     */
    @FXML
    private void handleLoadSubmissions() {
        if (selectedAssignment == null) {
            showStatus("Please select an assignment first.", true);
            return;
        }

        try {
            List<Submission> submissions = assignmentService.getSubmissions(selectedAssignment.getAssignmentId());
            submissionsTable.setItems(FXCollections.observableArrayList(submissions));
            submissionCountLabel.setText("Total Submissions: " + submissions.size());
            showStatus("Loaded " + submissions.size() + " submissions.", false);
        } catch (Exception e) {
            showStatus("Error loading submissions: " + e.getMessage(), true);
        }
    }

    /**
     * Populates the evaluation form with the selected submission's data.
     */
    private void populateEvaluationForm(Submission s) {
        String studentDisplay = s.getStudentName() != null ? s.getStudentName() : "Student ID: " + s.getStudentId();
        selectedStudentLabel.setText(studentDisplay);
        filePathLabel.setText(s.getFilePath() != null ? s.getFilePath() : "No file attached");

        if (s.getMarks() != null) {
            marksField.setText(String.valueOf(s.getMarks()));
        } else {
            marksField.clear();
        }

        if (s.getFeedback() != null) {
            feedbackArea.setText(s.getFeedback());
        } else {
            feedbackArea.clear();
        }
    }

    /**
     * Clears the evaluation form.
     */
    private void clearEvaluationForm() {
        selectedSubmission = null;
        selectedStudentLabel.setText("None selected");
        filePathLabel.setText("N/A");
        marksField.clear();
        feedbackArea.clear();
    }

    /**
     * Evaluates the selected submission and saves marks and feedback.
     */
    @FXML
    private void handleEvaluate() {
        if (selectedAssignment == null) {
            showStatus("Please select an assignment.", true);
            return;
        }
        if (selectedSubmission == null) {
            showStatus("Please select a student submission to evaluate.", true);
            return;
        }

        String marksText = marksField.getText().trim();
        if (marksText.isEmpty()) {
            showStatus("Please enter marks.", true);
            return;
        }

        double marks;
        try {
            marks = Double.parseDouble(marksText);
        } catch (NumberFormatException e) {
            showStatus("Marks must be a valid numeric value.", true);
            return;
        }

        if (marks < 0 || marks > selectedAssignment.getMaxMarks()) {
            showStatus("Marks must be between 0 and " + selectedAssignment.getMaxMarks() + ".", true);
            return;
        }

        String feedback = feedbackArea.getText().trim();

        try {
            assignmentService.evaluateSubmission(
                    selectedSubmission.getSubmissionId(),
                    selectedAssignment.getAssignmentId(),
                    marks,
                    feedback
            );
            showStatus("Submission evaluated successfully!", false);

            // Refresh the table
            int selectedIndex = submissionsTable.getSelectionModel().getSelectedIndex();
            handleLoadSubmissions();
            if (selectedIndex >= 0 && selectedIndex < submissionsTable.getItems().size()) {
                submissionsTable.getSelectionModel().select(selectedIndex);
            }
        } catch (Exception e) {
            showStatus("Evaluation failed: " + e.getMessage(), true);
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

