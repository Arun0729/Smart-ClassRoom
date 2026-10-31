package com.smartclassroom.controller;

import com.smartclassroom.dao.ClassDAO;
import com.smartclassroom.model.ClassRoom;
import com.smartclassroom.model.Student;
import com.smartclassroom.service.StudentService;
import com.smartclassroom.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Student Management screen.
 *
 * <p>Provides four distinct operational modes selectable via the top action buttons:
 * <ol>
 *   <li><b>Add Student</b> – full form to create a new student with a register number.</li>
 *   <li><b>Edit Student</b> – looks up by register number, then edits details.</li>
 *   <li><b>Deactivate Student</b> – looks up by register number, then soft-deletes.</li>
 *   <li><b>View Students</b> – shows all students in a searchable table.</li>
 * </ol>
 */
public class StudentManagementController implements Initializable {

    // ── Top-level panels (one shown at a time) ─────────────────────────────
    @FXML private VBox addPanel;
    @FXML private VBox editPanel;
    @FXML private VBox deletePanel;
    @FXML private VBox viewPanel;

    // ── Status label ───────────────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── ADD PANEL fields ───────────────────────────────────────────────────
    @FXML private TextField    addRegNoField;
    @FXML private TextField    addFullNameField;
    @FXML private TextField    addUsernameField;
    @FXML private TextField    addEmailField;
    @FXML private TextField    addPhoneField;
    @FXML private PasswordField addPasswordField;
    @FXML private ComboBox<ClassRoom> addClassComboBox;
    @FXML private TextField    addEnrollDateField;

    // ── EDIT PANEL fields ──────────────────────────────────────────────────
    @FXML private TextField    editRegNoLookupField;
    @FXML private VBox         editFormSection;
    @FXML private HBox         editCancelRow;
    @FXML private TextField    editRegNoField;
    @FXML private TextField    editFullNameField;
    @FXML private TextField    editUsernameField;
    @FXML private TextField    editEmailField;
    @FXML private TextField    editPhoneField;
    @FXML private ComboBox<ClassRoom> editClassComboBox;
    @FXML private TextField    editEnrollDateField;

    // ── DELETE (DEACTIVATE) PANEL fields ───────────────────────────────────
    @FXML private TextField deleteRegNoField;
    @FXML private VBox      deleteConfirmSection;
    @FXML private HBox      deleteCancelRow;
    @FXML private Label     deleteStudentInfoLabel;

    // ── VIEW PANEL fields ──────────────────────────────────────────────────
    @FXML private TextField             searchField;
    @FXML private TableView<Student>    studentTable;
    @FXML private TableColumn<Student, String>  colRegNo;
    @FXML private TableColumn<Student, Integer> colStudentId;
    @FXML private TableColumn<Student, String>  colName;
    @FXML private TableColumn<Student, String>  colUsername;
    @FXML private TableColumn<Student, String>  colEmail;
    @FXML private TableColumn<Student, String>  colPhone;
    @FXML private TableColumn<Student, String>  colClass;
    @FXML private TableColumn<Student, String>  colStatus;

    // ── Services ───────────────────────────────────────────────────────────
    private final StudentService studentService = new StudentService();
    private final ClassDAO classDAO = new ClassDAO();

    // currently found student (used by edit / deactivate flows)
    private Student foundStudent = null;

    // ── Initialization ─────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);

        // Load classes for both combo boxes
        try {
            List<ClassRoom> classes = classDAO.findAll();
            addClassComboBox.setItems(FXCollections.observableArrayList(classes));
            editClassComboBox.setItems(FXCollections.observableArrayList(classes));
        } catch (Exception e) {
            showStatus("Failed to load classes: " + e.getMessage(), true);
        }

        // Configure View table columns
        colRegNo.setCellValueFactory(new PropertyValueFactory<>("registerNumber"));
        colStudentId.setSortable(false);
        colStudentId.setCellValueFactory(cellData -> {
            int idx = studentTable.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(idx >= 0 ? idx + 1 : 0).asObject();
        });
        colStudentId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));

        // Status column with coloured text
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Student s = getTableRow().getItem();
                    if (s.isActive()) {
                        setText("Active");
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setText("Inactive");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // All panels hidden on load – user picks an action
        hideAllPanels();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TOP-LEVEL BUTTON HANDLERS – show the corresponding panel
    // ═══════════════════════════════════════════════════════════════════════

    /** Shows the Add Student form panel. */
    @FXML
    private void handleShowAdd() {
        hideAllPanels();
        clearAddForm();
        showPanel(addPanel);
        statusLabel.setVisible(false);
    }

    /** Shows the Edit Student lookup panel. */
    @FXML
    private void handleShowEdit() {
        hideAllPanels();
        editRegNoLookupField.clear();
        setVisible(editFormSection, false);
        setVisible(editCancelRow, true);
        foundStudent = null;
        showPanel(editPanel);
        statusLabel.setVisible(false);
    }

    /** Shows the Deactivate Student lookup panel. */
    @FXML
    private void handleShowDelete() {
        hideAllPanels();
        deleteRegNoField.clear();
        setVisible(deleteConfirmSection, false);
        setVisible(deleteCancelRow, true);
        foundStudent = null;
        showPanel(deletePanel);
        statusLabel.setVisible(false);
    }

    /** Shows the View Students table panel and loads all students. */
    @FXML
    private void handleShowView() {
        hideAllPanels();
        showPanel(viewPanel);
        loadAllStudents();
        statusLabel.setVisible(false);
    }

    /** Hides all sub-panels (returns to menu-only state). */
    @FXML
    private void handleCancel() {
        hideAllPanels();
        statusLabel.setVisible(false);
        foundStudent = null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADD STUDENT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Saves a new student from the Add form.
     */
    @FXML
    private void handleAdd() {
        try {
            Student student = new Student();
            student.setRegisterNumber(addRegNoField.getText().trim());
            student.setFullName(addFullNameField.getText().trim());
            student.setUsername(addUsernameField.getText().trim());
            student.setEmail(addEmailField.getText().trim());
            student.setPhone(addPhoneField.getText().trim());
            student.setEnrollmentDate(addEnrollDateField.getText().trim());
            if (addClassComboBox.getValue() != null) {
                student.setClassId(addClassComboBox.getValue().getClassId());
            }

            studentService.addStudent(student, addPasswordField.getText());
            showStatus("✔ Student '" + student.getFullName() + "' added successfully (Reg: "
                    + student.getRegisterNumber() + ").", false);
            clearAddForm();
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EDIT STUDENT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Looks up a student by register number and populates the edit form.
     */
    @FXML
    private void handleFindForEdit() {
        String regNo = editRegNoLookupField.getText().trim();
        if (regNo.isEmpty()) {
            showStatus("Please enter a register number.", true);
            return;
        }
        try {
            foundStudent = studentService.findByRegisterNumber(regNo);
            if (foundStudent == null) {
                showStatus("No student found with register number: " + regNo, true);
                setVisible(editFormSection, false);
                return;
            }
            // Populate edit form
            editRegNoField.setText(nvl(foundStudent.getRegisterNumber()));
            editFullNameField.setText(nvl(foundStudent.getFullName()));
            editUsernameField.setText(nvl(foundStudent.getUsername()));
            editEmailField.setText(nvl(foundStudent.getEmail()));
            editPhoneField.setText(nvl(foundStudent.getPhone()));
            editEnrollDateField.setText(nvl(foundStudent.getEnrollmentDate()));
            editClassComboBox.getItems().stream()
                    .filter(c -> c.getClassId() == foundStudent.getClassId())
                    .findFirst()
                    .ifPresent(editClassComboBox::setValue);

            setVisible(editFormSection, true);
            setVisible(editCancelRow, false);
            showStatus("Student found: " + foundStudent.getFullName() + " — edit the fields below.", false);
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Updates the found student with the values entered in the edit form.
     */
    @FXML
    private void handleUpdate() {
        if (foundStudent == null) {
            showStatus("No student selected for update.", true);
            return;
        }
        try {
            foundStudent.setRegisterNumber(editRegNoField.getText().trim());
            foundStudent.setFullName(editFullNameField.getText().trim());
            foundStudent.setUsername(editUsernameField.getText().trim());
            foundStudent.setEmail(editEmailField.getText().trim());
            foundStudent.setPhone(editPhoneField.getText().trim());
            foundStudent.setEnrollmentDate(editEnrollDateField.getText().trim());
            if (editClassComboBox.getValue() != null) {
                foundStudent.setClassId(editClassComboBox.getValue().getClassId());
            }
            studentService.updateStudent(foundStudent);
            showStatus("✔ Student updated successfully.", false);
            setVisible(editFormSection, false);
            setVisible(editCancelRow, true);
            editRegNoLookupField.clear();
            foundStudent = null;
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEACTIVATE STUDENT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Looks up a student by register number and shows the deactivation confirmation.
     */
    @FXML
    private void handleFindForDelete() {
        String regNo = deleteRegNoField.getText().trim();
        if (regNo.isEmpty()) {
            showStatus("Please enter a register number.", true);
            return;
        }
        try {
            foundStudent = studentService.findByRegisterNumber(regNo);
            if (foundStudent == null) {
                showStatus("No student found with register number: " + regNo, true);
                setVisible(deleteConfirmSection, false);
                return;
            }
            String status = foundStudent.isActive() ? "Active" : "Already Inactive";
            deleteStudentInfoLabel.setText(
                "Found: " + foundStudent.getFullName()
                + "  |  Reg: " + foundStudent.getRegisterNumber()
                + "  |  Username: " + foundStudent.getUsername()
                + "  |  Status: " + status
            );
            setVisible(deleteConfirmSection, true);
            setVisible(deleteCancelRow, false);
            showStatus("Review student info below, then confirm deactivation.", false);
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Soft-deletes (deactivates) the found student after confirmation.
     */
    @FXML
    private void handleConfirmDeactivate() {
        if (foundStudent == null) {
            showStatus("No student selected.", true);
            return;
        }
        if (!foundStudent.isActive()) {
            showStatus("Student '" + foundStudent.getFullName() + "' is already inactive.", true);
            return;
        }
        boolean confirmed = NavigationUtil.showConfirmation(
                "Deactivate Student",
                "Deactivate \"" + foundStudent.getFullName() + "\" (Reg: "
                        + foundStudent.getRegisterNumber() + ")?\n\n"
                        + "The student will no longer be able to log in."
        );
        if (confirmed) {
            try {
                studentService.deactivateStudent(foundStudent.getUserId());
                showStatus("✔ Student '" + foundStudent.getFullName() + "' has been deactivated.", false);
                setVisible(deleteConfirmSection, false);
                setVisible(deleteCancelRow, true);
                deleteRegNoField.clear();
                foundStudent = null;
            } catch (Exception e) {
                showStatus("Error: " + e.getMessage(), true);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VIEW STUDENTS
    // ═══════════════════════════════════════════════════════════════════════

    /** Searches students in the View panel. */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAllStudents();
            return;
        }
        try {
            List<Student> results = studentService.searchStudents(query);
            studentTable.setItems(FXCollections.observableArrayList(results));
            showStatus(results.size() + " result(s) found.", false);
        } catch (Exception e) {
            showStatus("Search error: " + e.getMessage(), true);
        }
    }

    /** Refreshes the student table in the View panel. */
    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadAllStudents();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════════════════

    /** Loads all students into the View table. */
    private void loadAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentTable.setItems(FXCollections.observableArrayList(students));
            showStatus(students.size() + " student(s) loaded.", false);
        } catch (Exception e) {
            showStatus("Failed to load students: " + e.getMessage(), true);
        }
    }

    /** Hides all four panels (managed = false prevents them from taking layout space). */
    private void hideAllPanels() {
        setVisible(addPanel, false);
        setVisible(editPanel, false);
        setVisible(deletePanel, false);
        setVisible(viewPanel, false);
    }

    /** Shows the given panel. */
    private void showPanel(VBox panel) {
        panel.setVisible(true);
        panel.setManaged(true);
    }

    /** Toggles visibility + managed state together so layout is not affected. */
    private void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /** Clears all fields in the Add Student form. */
    private void clearAddForm() {
        addRegNoField.clear();
        addFullNameField.clear();
        addUsernameField.clear();
        addEmailField.clear();
        addPhoneField.clear();
        addPasswordField.clear();
        addEnrollDateField.clear();
        addClassComboBox.getSelectionModel().clearSelection();
    }

    /** Returns the string if non-null, otherwise empty string. */
    private String nvl(String s) {
        return s != null ? s : "";
    }

    /** Displays a status message. */
    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        statusLabel.setVisible(true);
    }
}
