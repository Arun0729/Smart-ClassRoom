package com.smartclassroom.controller;

import com.smartclassroom.dao.ClassDAO;
import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.dao.SubjectDAO;
import com.smartclassroom.model.ClassRoom;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.Subject;
import com.smartclassroom.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Class and Subject Management screen.
 * Manages two independent panels: one for ClassRoom CRUD and one for Subject CRUD.
 */
public class ClassSubjectController implements Initializable {

    // ── Class panel ────────────────────────────────────────────────────────────
    @FXML private TableView<ClassRoom> classTable;
    @FXML private TableColumn<ClassRoom, Integer> colClassId;
    @FXML private TableColumn<ClassRoom, String> colClassName;
    @FXML private TableColumn<ClassRoom, String> colDept;
    @FXML private TableColumn<ClassRoom, String> colYear;

    @FXML private TextField classNameField;
    @FXML private TextField departmentField;
    @FXML private TextField academicYearField;

    @FXML private Button btnAddClass;
    @FXML private Button btnUpdateClass;
    @FXML private Button btnDeleteClass;
    @FXML private Button btnClearClass;

    @FXML private Label classStatusLabel;

    // ── Subject panel ──────────────────────────────────────────────────────────
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, Integer> colSubjectId;
    @FXML private TableColumn<Subject, String> colSubjectName;
    @FXML private TableColumn<Subject, String> colSubjectCode;
    @FXML private TableColumn<Subject, String> colSubClass;
    @FXML private TableColumn<Subject, String> colSubFaculty;

    @FXML private TextField subjectNameField;
    @FXML private TextField subjectCodeField;
    @FXML private ComboBox<ClassRoom> subjectClassCombo;
    @FXML private ComboBox<Faculty> subjectFacultyCombo;

    @FXML private Button btnAddSubject;
    @FXML private Button btnUpdateSubject;
    @FXML private Button btnDeleteSubject;
    @FXML private Button btnClearSubject;

    @FXML private Label subjectStatusLabel;

    // ── DAOs ───────────────────────────────────────────────────────────────────
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();

    private ClassRoom selectedClass = null;
    private Subject selectedSubject = null;

    /**
     * Initializes both panels: loads data, sets up columns, and registers selection listeners.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        classStatusLabel.setVisible(false);
        subjectStatusLabel.setVisible(false);

        setupClassTable();
        setupSubjectTable();
        refreshCombos();
        loadClasses();
        loadSubjects();
    }

    // ──────────────────────────────── CLASS PANEL ─────────────────────────────

    /** Configures the ClassRoom table columns and selection listener. */
    private void setupClassTable() {
        colClassId.setSortable(false);
        colClassId.setCellValueFactory(cellData -> {
            int idx = classTable.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(idx >= 0 ? idx + 1 : 0).asObject();
        });
        colClassId.setCellFactory(col -> new TableCell<>() {
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
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));

        classTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedClass = newVal;
                    classNameField.setText(newVal.getClassName());
                    departmentField.setText(newVal.getDepartment());
                    academicYearField.setText(newVal.getAcademicYear());
                }
            }
        );
    }

    /** Adds a new ClassRoom from the class form fields. */
    @FXML
    private void handleAddClass() {
        try {
            ClassRoom cr = buildClassFromForm();
            classDAO.save(cr);
            showClassStatus("Class added successfully.", false);
            handleClearClass();
            loadClasses();
            refreshCombos();
        } catch (Exception e) {
            showClassStatus("Error: " + e.getMessage(), true);
        }
    }

    /** Updates the selected ClassRoom. */
    @FXML
    private void handleUpdateClass() {
        if (selectedClass == null) {
            showClassStatus("Please select a class to update.", true);
            return;
        }
        try {
            selectedClass.setClassName(classNameField.getText().trim());
            selectedClass.setDepartment(departmentField.getText().trim());
            selectedClass.setAcademicYear(academicYearField.getText().trim());
            classDAO.update(selectedClass);
            showClassStatus("Class updated successfully.", false);
            loadClasses();
            refreshCombos();
        } catch (Exception e) {
            showClassStatus("Error: " + e.getMessage(), true);
        }
    }

    /** Deletes the selected ClassRoom after user confirmation. */
    @FXML
    private void handleDeleteClass() {
        if (selectedClass == null) {
            showClassStatus("Please select a class to delete.", true);
            return;
        }
        boolean confirmed = NavigationUtil.showConfirmation(
                "Delete Class",
                "Are you sure you want to delete class \"" + selectedClass.getClassName() + "\"?\n\n"
                + "Note: Any subjects associated with this class will also be removed."
        );
        if (confirmed) {
            try {
                classDAO.delete(selectedClass.getClassId());
                showClassStatus("Class deleted successfully.", false);
                handleClearClass();
                loadClasses();
                loadSubjects();
                refreshCombos();
            } catch (Exception e) {
                showClassStatus("Error deleting class: " + e.getMessage(), true);
            }
        }
    }

    /** Clears the class form fields and deselects the current row. */
    @FXML
    private void handleClearClass() {
        selectedClass = null;
        classNameField.clear();
        departmentField.clear();
        academicYearField.clear();
        classTable.getSelectionModel().clearSelection();
        classStatusLabel.setVisible(false);
    }

    /** Reloads all classes into the class table. */
    private void loadClasses() {
        try {
            List<ClassRoom> list = classDAO.findAll();
            classTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showClassStatus("Failed to load classes: " + e.getMessage(), true);
        }
    }

    /** Builds a ClassRoom from the current form field values. */
    private ClassRoom buildClassFromForm() {
        ClassRoom cr = new ClassRoom();
        cr.setClassName(classNameField.getText().trim());
        cr.setDepartment(departmentField.getText().trim());
        cr.setAcademicYear(academicYearField.getText().trim());
        return cr;
    }

    // ──────────────────────────────── SUBJECT PANEL ───────────────────────────

    /** Configures the Subject table columns and selection listener. */
    private void setupSubjectTable() {
        colSubjectId.setSortable(false);
        colSubjectId.setCellValueFactory(cellData -> {
            int idx = subjectTable.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(idx >= 0 ? idx + 1 : 0).asObject();
        });
        colSubjectId.setCellFactory(col -> new TableCell<>() {
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
        colSubjectName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colSubjectCode.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        colSubClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colSubFaculty.setCellValueFactory(new PropertyValueFactory<>("facultyName"));

        subjectTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedSubject = newVal;
                    subjectNameField.setText(newVal.getSubjectName());
                    subjectCodeField.setText(newVal.getSubjectCode());

                    // Pre-select matching class
                    subjectClassCombo.getItems().stream()
                        .filter(c -> c.getClassId() == newVal.getClassId())
                        .findFirst()
                        .ifPresent(subjectClassCombo::setValue);

                    // Pre-select matching faculty
                    subjectFacultyCombo.getItems().stream()
                        .filter(f -> f.getFacultyId() == newVal.getFacultyId())
                        .findFirst()
                        .ifPresent(subjectFacultyCombo::setValue);
                }
            }
        );
    }

    /** Adds a new Subject from the subject form fields. */
    @FXML
    private void handleAddSubject() {
        try {
            Subject subject = buildSubjectFromForm();
            subjectDAO.save(subject);
            showSubjectStatus("Subject added successfully.", false);
            handleClearSubject();
            loadSubjects();
        } catch (Exception e) {
            showSubjectStatus("Error: " + e.getMessage(), true);
        }
    }

    /** Updates the selected Subject. */
    @FXML
    private void handleUpdateSubject() {
        if (selectedSubject == null) {
            showSubjectStatus("Please select a subject to update.", true);
            return;
        }
        try {
            selectedSubject.setSubjectName(subjectNameField.getText().trim());
            selectedSubject.setSubjectCode(subjectCodeField.getText().trim());
            if (subjectClassCombo.getValue() != null) {
                selectedSubject.setClassId(subjectClassCombo.getValue().getClassId());
            }
            if (subjectFacultyCombo.getValue() != null) {
                selectedSubject.setFacultyId(subjectFacultyCombo.getValue().getFacultyId());
            }
            subjectDAO.update(selectedSubject);
            showSubjectStatus("Subject updated successfully.", false);
            loadSubjects();
        } catch (Exception e) {
            showSubjectStatus("Error: " + e.getMessage(), true);
        }
    }

    /** Deletes the selected Subject after user confirmation. */
    @FXML
    private void handleDeleteSubject() {
        if (selectedSubject == null) {
            showSubjectStatus("Please select a subject to delete.", true);
            return;
        }
        boolean confirmed = NavigationUtil.showConfirmation(
                "Delete Subject",
                "Are you sure you want to delete subject \"" + selectedSubject.getSubjectName() + "\"?"
        );
        if (confirmed) {
            try {
                subjectDAO.delete(selectedSubject.getSubjectId());
                showSubjectStatus("Subject deleted successfully.", false);
                handleClearSubject();
                loadSubjects();
            } catch (Exception e) {
                showSubjectStatus("Error deleting subject: " + e.getMessage(), true);
            }
        }
    }

    /** Clears the subject form fields and deselects the current row. */
    @FXML
    private void handleClearSubject() {
        selectedSubject = null;
        subjectNameField.clear();
        subjectCodeField.clear();
        subjectClassCombo.getSelectionModel().clearSelection();
        subjectFacultyCombo.getSelectionModel().clearSelection();
        subjectTable.getSelectionModel().clearSelection();
        subjectStatusLabel.setVisible(false);
    }

    /** Reloads all subjects into the subject table. */
    private void loadSubjects() {
        try {
            List<Subject> list = subjectDAO.findAll();
            subjectTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showSubjectStatus("Failed to load subjects: " + e.getMessage(), true);
        }
    }

    /** Reloads class and faculty combo boxes. */
    private void refreshCombos() {
        try {
            List<ClassRoom> classes = classDAO.findAll();
            subjectClassCombo.setItems(FXCollections.observableArrayList(classes));
        } catch (Exception e) {
            showSubjectStatus("Failed to load classes for combo: " + e.getMessage(), true);
        }
        try {
            List<Faculty> faculties = facultyDAO.findAll();
            subjectFacultyCombo.setItems(FXCollections.observableArrayList(faculties));
        } catch (Exception e) {
            showSubjectStatus("Failed to load faculty for combo: " + e.getMessage(), true);
        }
    }

    /** Builds a Subject from the current form field values. */
    private Subject buildSubjectFromForm() {
        Subject s = new Subject();
        s.setSubjectName(subjectNameField.getText().trim());
        s.setSubjectCode(subjectCodeField.getText().trim());
        if (subjectClassCombo.getValue() != null) {
            s.setClassId(subjectClassCombo.getValue().getClassId());
        }
        if (subjectFacultyCombo.getValue() != null) {
            s.setFacultyId(subjectFacultyCombo.getValue().getFacultyId());
        }
        return s;
    }

    // ──────────────────────────────── HELPERS ─────────────────────────────────

    private void showClassStatus(String msg, boolean isError) {
        classStatusLabel.setText(msg);
        classStatusLabel.getStyleClass().removeAll("status-error", "status-success");
        classStatusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        classStatusLabel.setVisible(true);
    }

    private void showSubjectStatus(String msg, boolean isError) {
        subjectStatusLabel.setText(msg);
        subjectStatusLabel.getStyleClass().removeAll("status-error", "status-success");
        subjectStatusLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        subjectStatusLabel.setVisible(true);
    }
}
