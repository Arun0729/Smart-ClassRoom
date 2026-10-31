package com.smartclassroom.controller;

import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.model.Assignment;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Subject;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Assignment Management screen (Faculty view).
 * Allows faculty to create, update, and delete assignments for their subjects.
 */
public class AssignmentManagementController implements Initializable {

    @FXML private TableView<Assignment> assignmentTable;
    @FXML private TableColumn<Assignment, String> colTitle;
    @FXML private TableColumn<Assignment, String> colSubject;
    @FXML private TableColumn<Assignment, String> colDeadline;
    @FXML private TableColumn<Assignment, String> colMaxMarks;

    @FXML private TextField titleField;
    @FXML private TextField maxMarksField;
    @FXML private TextField deadlineTimeField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private DatePicker deadlineDatePicker;

    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    @FXML private Label statusLabel;

    private final AssignmentService assignmentService = new AssignmentService();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private Assignment selectedAssignment = null;
    private Faculty currentFaculty;

    /**
     * Initializes the assignment management screen.
     * Loads the faculty's subjects, configures table columns,
     * and sets a default time in the deadline time field.
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

        // Load subjects for this faculty
        try {
            List<Subject> subjects = subjectDAO.findByFacultyId(currentFaculty.getFacultyId());
            subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        } catch (Exception e) {
            showStatus("Failed to load subjects: " + e.getMessage(), true);
        }

        // Set up table columns
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));

        colDeadline.setCellValueFactory(cellData -> {
            Assignment a = cellData.getValue();
            if (a.getDeadline() != null) {
                return new SimpleStringProperty(
                    a.getDeadline().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            return new SimpleStringProperty("N/A");
        });

        colMaxMarks.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.valueOf(cellData.getValue().getMaxMarks())));

        // Selection listener populates form
        assignmentTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedAssignment = newVal;
                    populateForm(newVal);
                }
            }
        );

        // Default deadline time
        deadlineTimeField.setText("23:59");

        loadAssignments();
    }

    /**
     * Creates a new assignment using the values entered in the form.
     */
    @FXML
    private void handleCreate() {
        try {
            Assignment assignment = buildAssignmentFromForm();
            assignmentService.createAssignment(assignment, currentFaculty.getFacultyId());
            showStatus("Assignment created successfully.", false);
            handleClear();
            loadAssignments();
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Updates the currently selected assignment.
     */
    @FXML
    private void handleUpdate() {
        if (selectedAssignment == null) {
            showStatus("Please select an assignment to update.", true);
            return;
        }
        try {
            selectedAssignment.setTitle(titleField.getText().trim());
            selectedAssignment.setDescription(descriptionArea.getText().trim());
            if (subjectComboBox.getValue() != null) {
                selectedAssignment.setSubjectId(subjectComboBox.getValue().getSubjectId());
            }
            if (deadlineDatePicker.getValue() != null) {
                LocalTime time = LocalTime.parse(deadlineTimeField.getText().trim());
                selectedAssignment.setDeadline(deadlineDatePicker.getValue().atTime(time));
            }
            selectedAssignment.setMaxMarks(Double.parseDouble(maxMarksField.getText().trim()));
            assignmentService.updateAssignment(selectedAssignment, currentFaculty.getFacultyId());
            showStatus("Assignment updated successfully.", false);
            loadAssignments();
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Deletes the currently selected assignment after confirmation.
     */
    @FXML
    private void handleDelete() {
        if (selectedAssignment == null) {
            showStatus("Please select an assignment to delete.", true);
            return;
        }
        if (NavigationUtil.showConfirmation("Delete Assignment",
                "Delete assignment \"" + selectedAssignment.getTitle() + "\"? This cannot be undone.")) {
            try {
                assignmentService.deleteAssignment(selectedAssignment.getAssignmentId(), currentFaculty.getFacultyId());
                showStatus("Assignment deleted.", false);
                handleClear();
                loadAssignments();
            } catch (Exception e) {
                showStatus("Error: " + e.getMessage(), true);
            }
        }
    }

    /**
     * Clears the form and deselects the current assignment.
     */
    @FXML
    private void handleClear() {
        selectedAssignment = null;
        titleField.clear();
        descriptionArea.clear();
        maxMarksField.clear();
        deadlineTimeField.setText("23:59");
        deadlineDatePicker.setValue(null);
        subjectComboBox.getSelectionModel().clearSelection();
        assignmentTable.getSelectionModel().clearSelection();
        statusLabel.setVisible(false);
    }

    /**
     * Loads all assignments for the current faculty into the table.
     */
    private void loadAssignments() {
        try {
            List<Assignment> assignments =
                assignmentService.getAssignmentsByFaculty(currentFaculty.getFacultyId());
            assignmentTable.setItems(FXCollections.observableArrayList(assignments));
        } catch (Exception e) {
            showStatus("Failed to load assignments: " + e.getMessage(), true);
        }
    }

    /**
     * Builds an Assignment object from the current form field values.
     *
     * @return a populated Assignment instance
     * @throws Exception if date/time or marks parsing fails
     */
    private Assignment buildAssignmentFromForm() throws Exception {
        Assignment a = new Assignment();
        a.setTitle(titleField.getText().trim());
        a.setDescription(descriptionArea.getText().trim());
        if (subjectComboBox.getValue() != null) {
            a.setSubjectId(subjectComboBox.getValue().getSubjectId());
        }
        if (deadlineDatePicker.getValue() != null) {
            LocalTime time = LocalTime.parse(deadlineTimeField.getText().trim());
            a.setDeadline(deadlineDatePicker.getValue().atTime(time));
        }
        a.setMaxMarks(Double.parseDouble(maxMarksField.getText().trim()));
        return a;
    }

    /**
     * Populates the form fields with data from the given assignment.
     *
     * @param a the assignment to display in the form
     */
    private void populateForm(Assignment a) {
        titleField.setText(a.getTitle() != null ? a.getTitle() : "");
        descriptionArea.setText(a.getDescription() != null ? a.getDescription() : "");
        maxMarksField.setText(String.valueOf(a.getMaxMarks()));
        if (a.getDeadline() != null) {
            deadlineDatePicker.setValue(a.getDeadline().toLocalDate());
            deadlineTimeField.setText(a.getDeadline().toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        // Pre-select matching subject
        if (subjectComboBox.getItems() != null) {
            subjectComboBox.getItems().stream()
                .filter(s -> s.getSubjectId() == a.getSubjectId())
                .findFirst()
                .ifPresent(subjectComboBox::setValue);
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        statusLabel.setVisible(true);
    }
}
