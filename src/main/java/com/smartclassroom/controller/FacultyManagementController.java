package com.smartclassroom.controller;

import com.smartclassroom.model.Faculty;
import com.smartclassroom.service.FacultyService;
import com.smartclassroom.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
 * Controller for the Faculty Management screen.
 *
 * <p>Provides four distinct operational modes selectable via the top action buttons:
 * <ol>
 *   <li><b>Add Faculty</b> – full form to create a new faculty member with an Employee ID.</li>
 *   <li><b>Edit Faculty</b> – looks up by Employee/Faculty ID, then edits details.</li>
 *   <li><b>Deactivate Faculty</b> – looks up by Employee/Faculty ID, then soft-deletes.</li>
 *   <li><b>View Faculty</b> – shows all faculty members in a searchable table.</li>
 * </ol>
 */
public class FacultyManagementController implements Initializable {

    // ── Top-level panels (one shown at a time) ─────────────────────────────
    @FXML private VBox addPanel;
    @FXML private VBox editPanel;
    @FXML private VBox deletePanel;
    @FXML private VBox viewPanel;

    // ── Status label ───────────────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── ADD PANEL fields ───────────────────────────────────────────────────
    @FXML private TextField     addEmployeeIdField;
    @FXML private TextField     addFullNameField;
    @FXML private TextField     addUsernameField;
    @FXML private TextField     addEmailField;
    @FXML private TextField     addPhoneField;
    @FXML private TextField     addDepartmentField;
    @FXML private PasswordField addPasswordField;

    // ── EDIT PANEL fields ──────────────────────────────────────────────────
    @FXML private TextField editLookupField;
    @FXML private VBox      editFormSection;
    @FXML private HBox      editCancelRow;
    @FXML private TextField editEmployeeIdField;
    @FXML private TextField editFullNameField;
    @FXML private TextField editUsernameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editDepartmentField;

    // ── DELETE (DEACTIVATE) PANEL fields ───────────────────────────────────
    @FXML private TextField deleteLookupField;
    @FXML private VBox      deleteConfirmSection;
    @FXML private HBox      deleteCancelRow;
    @FXML private Label     deleteFacultyInfoLabel;

    // ── VIEW PANEL fields ──────────────────────────────────────────────────
    @FXML private TextField              searchField;
    @FXML private TableView<Faculty>     facultyTable;
    @FXML private TableColumn<Faculty, String>  colEmployeeId;
    @FXML private TableColumn<Faculty, Integer> colFacultyId;
    @FXML private TableColumn<Faculty, String>  colName;
    @FXML private TableColumn<Faculty, String>  colUsername;
    @FXML private TableColumn<Faculty, String>  colEmail;
    @FXML private TableColumn<Faculty, String>  colPhone;
    @FXML private TableColumn<Faculty, String>  colDept;
    @FXML private TableColumn<Faculty, String>  colStatus;

    // ── Services ───────────────────────────────────────────────────────────
    private final FacultyService facultyService = new FacultyService();

    // currently found faculty (used by edit / deactivate flows)
    private Faculty foundFaculty = null;

    // ── Initialization ─────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusLabel.setVisible(false);

        // Configure View table columns
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFacultyId.setSortable(false);
        colFacultyId.setCellValueFactory(cellData -> {
            int idx = facultyTable.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(idx >= 0 ? idx + 1 : 0).asObject();
        });
        colFacultyId.setCellFactory(col -> new TableCell<>() {
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
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));

        // Status column with coloured text
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Faculty f = getTableRow().getItem();
                    if (f.isActive()) {
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

    /** Shows the Add Faculty form panel. */
    @FXML
    private void handleShowAdd() {
        hideAllPanels();
        clearAddForm();
        showPanel(addPanel);
        statusLabel.setVisible(false);
    }

    /** Shows the Edit Faculty lookup panel. */
    @FXML
    private void handleShowEdit() {
        hideAllPanels();
        editLookupField.clear();
        setVisible(editFormSection, false);
        setVisible(editCancelRow, true);
        foundFaculty = null;
        showPanel(editPanel);
        statusLabel.setVisible(false);
    }

    /** Shows the Deactivate Faculty lookup panel. */
    @FXML
    private void handleShowDelete() {
        hideAllPanels();
        deleteLookupField.clear();
        setVisible(deleteConfirmSection, false);
        setVisible(deleteCancelRow, true);
        foundFaculty = null;
        showPanel(deletePanel);
        statusLabel.setVisible(false);
    }

    /** Shows the View Faculty table panel and loads all faculty. */
    @FXML
    private void handleShowView() {
        hideAllPanels();
        showPanel(viewPanel);
        loadAllFaculty();
        statusLabel.setVisible(false);
    }

    /** Hides all sub-panels (returns to menu-only state). */
    @FXML
    private void handleCancel() {
        hideAllPanels();
        statusLabel.setVisible(false);
        foundFaculty = null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADD FACULTY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Saves a new faculty member from the Add form.
     */
    @FXML
    private void handleAdd() {
        try {
            Faculty faculty = new Faculty();
            faculty.setEmployeeId(addEmployeeIdField.getText().trim());
            faculty.setFullName(addFullNameField.getText().trim());
            faculty.setUsername(addUsernameField.getText().trim());
            faculty.setEmail(addEmailField.getText().trim());
            faculty.setPhone(addPhoneField.getText().trim());
            faculty.setDepartment(addDepartmentField.getText().trim());

            facultyService.addFaculty(faculty, addPasswordField.getText());
            showStatus("✔ Faculty '" + faculty.getFullName() + "' added successfully (ID: "
                    + faculty.getEmployeeId() + ").", false);
            clearAddForm();
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EDIT FACULTY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Looks up a faculty member by Employee ID, numeric ID, or username.
     */
    @FXML
    private void handleFindForEdit() {
        String identifier = editLookupField.getText().trim();
        if (identifier.isEmpty()) {
            showStatus("Please enter an Employee ID, Faculty ID, or username.", true);
            return;
        }
        try {
            foundFaculty = facultyService.findByIdentifier(identifier);
            if (foundFaculty == null) {
                showStatus("No faculty found matching: " + identifier, true);
                setVisible(editFormSection, false);
                return;
            }
            // Populate edit form
            editEmployeeIdField.setText(nvl(foundFaculty.getEmployeeId()));
            editFullNameField.setText(nvl(foundFaculty.getFullName()));
            editUsernameField.setText(nvl(foundFaculty.getUsername()));
            editEmailField.setText(nvl(foundFaculty.getEmail()));
            editPhoneField.setText(nvl(foundFaculty.getPhone()));
            editDepartmentField.setText(nvl(foundFaculty.getDepartment()));

            setVisible(editFormSection, true);
            setVisible(editCancelRow, false);
            showStatus("Faculty found: " + foundFaculty.getFullName() + " — edit the fields below.", false);
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Updates the found faculty member with the values entered in the edit form.
     */
    @FXML
    private void handleUpdate() {
        if (foundFaculty == null) {
            showStatus("No faculty selected for update.", true);
            return;
        }
        try {
            foundFaculty.setEmployeeId(editEmployeeIdField.getText().trim());
            foundFaculty.setFullName(editFullNameField.getText().trim());
            foundFaculty.setUsername(editUsernameField.getText().trim());
            foundFaculty.setEmail(editEmailField.getText().trim());
            foundFaculty.setPhone(editPhoneField.getText().trim());
            foundFaculty.setDepartment(editDepartmentField.getText().trim());

            facultyService.updateFaculty(foundFaculty);
            showStatus("✔ Faculty updated successfully.", false);
            setVisible(editFormSection, false);
            setVisible(editCancelRow, true);
            editLookupField.clear();
            foundFaculty = null;
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEACTIVATE FACULTY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Looks up a faculty member and displays deactivation confirmation details.
     */
    @FXML
    private void handleFindForDelete() {
        String identifier = deleteLookupField.getText().trim();
        if (identifier.isEmpty()) {
            showStatus("Please enter an Employee ID, Faculty ID, or username.", true);
            return;
        }
        try {
            foundFaculty = facultyService.findByIdentifier(identifier);
            if (foundFaculty == null) {
                showStatus("No faculty found matching: " + identifier, true);
                setVisible(deleteConfirmSection, false);
                return;
            }
            String status = foundFaculty.isActive() ? "Active" : "Already Inactive";
            deleteFacultyInfoLabel.setText(
                "Found: " + foundFaculty.getFullName()
                + "  |  Emp ID: " + nvl(foundFaculty.getEmployeeId())
                + "  |  Username: " + foundFaculty.getUsername()
                + "  |  Dept: " + nvl(foundFaculty.getDepartment())
                + "  |  Status: " + status
            );
            setVisible(deleteConfirmSection, true);
            setVisible(deleteCancelRow, false);
            showStatus("Review faculty info below, then confirm deactivation.", false);
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Soft-deletes (deactivates) the found faculty member after confirmation.
     */
    @FXML
    private void handleConfirmDeactivate() {
        if (foundFaculty == null) {
            showStatus("No faculty selected.", true);
            return;
        }
        if (!foundFaculty.isActive()) {
            showStatus("Faculty member '" + foundFaculty.getFullName() + "' is already inactive.", true);
            return;
        }
        boolean confirmed = NavigationUtil.showConfirmation(
                "Deactivate Faculty",
                "Deactivate \"" + foundFaculty.getFullName() + "\" (Emp ID: "
                        + nvl(foundFaculty.getEmployeeId()) + ")?\n\n"
                        + "The faculty member will no longer be able to log in."
        );
        if (confirmed) {
            try {
                facultyService.deactivateFaculty(foundFaculty.getUserId());
                showStatus("✔ Faculty '" + foundFaculty.getFullName() + "' has been deactivated.", false);
                setVisible(deleteConfirmSection, false);
                setVisible(deleteCancelRow, true);
                deleteLookupField.clear();
                foundFaculty = null;
            } catch (Exception e) {
                showStatus("Error: " + e.getMessage(), true);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VIEW FACULTY
    // ═══════════════════════════════════════════════════════════════════════

    /** Searches faculty in the View panel. */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAllFaculty();
            return;
        }
        try {
            List<Faculty> results = facultyService.searchFaculty(query);
            facultyTable.setItems(FXCollections.observableArrayList(results));
            showStatus(results.size() + " result(s) found.", false);
        } catch (Exception e) {
            showStatus("Search error: " + e.getMessage(), true);
        }
    }

    /** Refreshes the faculty table in the View panel. */
    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadAllFaculty();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════════════════

    /** Loads all faculty into the View table. */
    private void loadAllFaculty() {
        try {
            List<Faculty> list = facultyService.getAllFaculty();
            facultyTable.setItems(FXCollections.observableArrayList(list));
            showStatus(list.size() + " faculty member(s) loaded.", false);
        } catch (Exception e) {
            showStatus("Failed to load faculty: " + e.getMessage(), true);
        }
    }

    /** Hides all four panels (managed = false prevents taking layout space). */
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

    /** Toggles visibility + managed state together so layout is clean. */
    private void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /** Clears all fields in the Add Faculty form. */
    private void clearAddForm() {
        addEmployeeIdField.clear();
        addFullNameField.clear();
        addUsernameField.clear();
        addEmailField.clear();
        addPhoneField.clear();
        addDepartmentField.clear();
        addPasswordField.clear();
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
