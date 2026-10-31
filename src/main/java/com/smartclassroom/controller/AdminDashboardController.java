package com.smartclassroom.controller;

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
 * Controller for the Admin Dashboard.
 * Displays system-wide statistics and provides navigation to management sub-screens.
 */
public class AdminDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane contentArea;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalFacultyLabel;
    @FXML private Label totalClassesLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private VBox dashboardHome;
    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnFaculty;
    @FXML private Button btnClasses;
    @FXML private Button btnLogout;

    private final ReportService reportService = new ReportService();
    private Button activeNavBtn;

    /**
     * Initializes the dashboard. Loads the logged-in user's info,
     * populates statistics, and shows the home view.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getUsername());
        userRoleLabel.setText("Administrator");
        loadStats();
        setActiveNav(btnDashboard);
        showHome();
    }

    /**
     * Loads system-wide counts from the ReportService and displays them on stat cards.
     */
    private void loadStats() {
        try {
            totalStudentsLabel.setText(String.valueOf(reportService.getTotalStudents()));
            totalFacultyLabel.setText(String.valueOf(reportService.getTotalFaculty()));
            totalClassesLabel.setText(String.valueOf(reportService.getTotalClasses()));
            totalSubjectsLabel.setText(String.valueOf(reportService.getTotalSubjects()));
        } catch (Exception e) {
            NavigationUtil.showError("Stats Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    /** Shows the default home view with stat cards. */
    @FXML
    private void showHome() {
        setActiveNav(btnDashboard);
        loadStats();
        contentArea.getChildren().setAll(dashboardHome);
    }

    /** Loads and displays the Student Management sub-screen. */
    @FXML
    private void showStudents() {
        setActiveNav(btnStudents);
        try {
            Parent view = NavigationUtil.loadPane("student-management.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Student Management: " + e.getMessage());
        }
    }

    /** Loads and displays the Faculty Management sub-screen. */
    @FXML
    private void showFaculty() {
        setActiveNav(btnFaculty);
        try {
            Parent view = NavigationUtil.loadPane("faculty-management.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Faculty Management: " + e.getMessage());
        }
    }

    /** Loads and displays the Class & Subject Management sub-screen. */
    @FXML
    private void showClasses() {
        setActiveNav(btnClasses);
        try {
            Parent view = NavigationUtil.loadPane("class-subject-management.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Class & Subject Management: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user and returns to the Login screen.
     * Shows a confirmation dialog before proceeding.
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
     * Updates the active navigation button styling.
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
