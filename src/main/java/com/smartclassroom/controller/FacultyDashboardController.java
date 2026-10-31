package com.smartclassroom.controller;

import com.smartclassroom.dao.FacultyDAO;
import com.smartclassroom.model.Faculty;
import com.smartclassroom.model.User;
import com.smartclassroom.service.AuthService;
import com.smartclassroom.service.ReportService;
import com.smartclassroom.util.NavigationUtil;
import com.smartclassroom.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Faculty Dashboard.
 * Displays subject count statistics and provides navigation to attendance,
 * assignment, submission, and report sub-screens.
 */
public class FacultyDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label deptLabel;
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardHome;
    @FXML private Button btnDashboard;
    @FXML private Button btnAttendance;
    @FXML private Button btnAssignments;
    @FXML private Button btnSubmissions;
    @FXML private Button btnReports;
    @FXML private Button btnLogout;
    @FXML private Label subjectCountLabel;

    private final ReportService reportService = new ReportService();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private Faculty currentFaculty;
    private Button activeNavBtn;

    /**
     * Initializes the faculty dashboard. Loads the logged-in faculty profile,
     * displays their name/department, shows subject count, and shows the home view.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentFaculty = facultyDAO.findByUserId(user.getUserId());

            if (currentFaculty != null) {
                userNameLabel.setText(currentFaculty.getFullName());
                deptLabel.setText(currentFaculty.getDepartment() != null ? currentFaculty.getDepartment() : "");
                int subjectCount = reportService.getFacultySubjects(currentFaculty.getFacultyId()).size();
                subjectCountLabel.setText(String.valueOf(subjectCount));
            } else {
                userNameLabel.setText(user.getUsername());
                deptLabel.setText("");
                subjectCountLabel.setText("0");
            }
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load faculty profile: " + e.getMessage());
        }

        setActiveNav(btnDashboard);
        showHome();
    }

    /** Shows the default home/statistics view. */
    @FXML
    private void showHome() {
        setActiveNav(btnDashboard);
        contentArea.getChildren().setAll(dashboardHome);
    }

    /** Loads and displays the Attendance Marking sub-screen. */
    @FXML
    private void showAttendance() {
        setActiveNav(btnAttendance);
        try {
            Parent view = NavigationUtil.loadPane("attendance.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Attendance: " + e.getMessage());
        }
    }

    /** Loads and displays the Assignment Management sub-screen. */
    @FXML
    private void showAssignments() {
        setActiveNav(btnAssignments);
        try {
            Parent view = NavigationUtil.loadPane("assignment-management.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Assignments: " + e.getMessage());
        }
    }

    /** Loads and displays the Submissions Evaluation sub-screen. */
    @FXML
    private void showSubmissions() {
        setActiveNav(btnSubmissions);
        try {
            Parent view = NavigationUtil.loadPane("submissions.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Submissions: " + e.getMessage());
        }
    }

    /** Loads and displays the Attendance Report sub-screen. */
    @FXML
    private void showReports() {
        setActiveNav(btnReports);
        try {
            Parent view = NavigationUtil.loadPane("attendance-report.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Reports: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user and navigates back to the Login screen
     * after showing a confirmation dialog.
     */
    @FXML
    private void handleLogout() {
        if (NavigationUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            new AuthService().logout();
            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                NavigationUtil.loadScene("login.fxml", stage, "Login");
            } catch (Exception e) {
                NavigationUtil.showError("Error", "Failed to navigate to login: " + e.getMessage());
            }
        }
    }

    /**
     * Sets the given button as the active navigation button,
     * updating CSS styling accordingly.
     *
     * @param btn the button to mark as active
     */
    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-btn-active");
        }
        btn.getStyleClass().add("nav-btn-active");
        activeNavBtn = btn;
    }
}
