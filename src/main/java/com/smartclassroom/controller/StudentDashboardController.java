package com.smartclassroom.controller;

import com.smartclassroom.dao.StudentDAO;
import com.smartclassroom.model.Student;
import com.smartclassroom.model.Subject;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Student Dashboard screen.
 * Displays overall student profile, subject-wise attendance percentage cards,
 * and allows navigation to assignment viewing and submission.
 */
public class StudentDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label classLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label overallAttendanceLabel;

    @FXML private StackPane contentArea;
    @FXML private VBox dashboardHome;
    @FXML private FlowPane attendanceSummaryBox;

    @FXML private Button btnDashboard;
    @FXML private Button btnViewAttendance;
    @FXML private Button btnAssignments;
    @FXML private Button btnLogout;

    private final ReportService reportService = new ReportService();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AuthService authService = new AuthService();

    private Student currentStudent;
    private Button activeNavBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            currentStudent = studentDAO.findByUserId(user.getUserId());

            if (currentStudent != null) {
                userNameLabel.setText(currentStudent.getFullName());
                classLabel.setText(currentStudent.getClassName() != null ? currentStudent.getClassName() : "Class N/A");
                userRoleLabel.setText("Student");
            } else {
                userNameLabel.setText(user.getUsername());
                classLabel.setText("N/A");
                userRoleLabel.setText("Student");
            }
        } catch (Exception e) {
            NavigationUtil.showError("Init Error", "Failed to load student profile: " + e.getMessage());
        }

        setActiveNav(btnDashboard);
        loadAttendanceSummary();
        showHome();
    }

    /**
     * Loads subject-wise attendance data and dynamically builds UI cards.
     */
    private void loadAttendanceSummary() {
        if (currentStudent == null) return;

        attendanceSummaryBox.getChildren().clear();

        try {
            Map<Subject, Double> attendanceMap = reportService.getStudentAttendanceSummary(currentStudent.getStudentId());
            totalSubjectsLabel.setText(String.valueOf(attendanceMap.size()));

            if (attendanceMap.isEmpty()) {
                overallAttendanceLabel.setText("N/A");
                Label emptyLabel = new Label("No subjects or attendance records found.");
                emptyLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
                attendanceSummaryBox.getChildren().add(emptyLabel);
                return;
            }

            double totalPct = 0.0;
            for (Map.Entry<Subject, Double> entry : attendanceMap.entrySet()) {
                Subject subject = entry.getKey();
                double pct = entry.getValue();
                totalPct += pct;

                VBox card = new VBox(8);
                card.getStyleClass().add("attendance-card");
                card.setPrefWidth(220);

                Label subjLabel = new Label(subject.getSubjectName());
                subjLabel.getStyleClass().add("attendance-card-subject");
                subjLabel.setWrapText(true);

                Label codeLabel = new Label(subject.getSubjectCode() != null ? subject.getSubjectCode() : "");
                codeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

                Label pctLabel = new Label(String.format("%.1f%%", pct));
                if (pct >= 75.0) {
                    pctLabel.getStyleClass().addAll("attendance-card-pct", "pct-good");
                } else if (pct >= 60.0) {
                    pctLabel.getStyleClass().addAll("attendance-card-pct", "pct-warning");
                } else {
                    pctLabel.getStyleClass().addAll("attendance-card-pct", "pct-danger");
                }

                Label noteLabel = new Label(pct >= 75.0 ? "✓ Above 75% threshold" : "⚠️ Attendance Warning (<75%)");
                noteLabel.setStyle(pct >= 75.0
                        ? "-fx-text-fill: #2E7D32; -fx-font-size: 11px;"
                        : "-fx-text-fill: #C62828; -fx-font-size: 11px; -fx-font-weight: bold;");

                card.getChildren().addAll(subjLabel, codeLabel, pctLabel, noteLabel);
                attendanceSummaryBox.getChildren().add(card);
            }

            double avgPct = totalPct / attendanceMap.size();
            overallAttendanceLabel.setText(String.format("%.1f%%", avgPct));

        } catch (Exception e) {
            NavigationUtil.showError("Data Error", "Could not load attendance summary: " + e.getMessage());
        }
    }

    /**
     * Shows the default dashboard home.
     */
    @FXML
    private void showHome() {
        setActiveNav(btnDashboard);
        contentArea.getChildren().setAll(dashboardHome);
        loadAttendanceSummary();
    }

    /**
     * Shows attendance cards view.
     */
    @FXML
    private void showAttendance() {
        setActiveNav(btnViewAttendance);
        contentArea.getChildren().setAll(dashboardHome);
        loadAttendanceSummary();
    }

    /**
     * Loads the student assignments screen into the content area.
     */
    @FXML
    private void showAssignments() {
        setActiveNav(btnAssignments);
        try {
            Parent view = NavigationUtil.loadPane("student-assignments.fxml");
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            NavigationUtil.showError("Error", "Could not load Assignments: " + e.getMessage());
        }
    }

    /**
     * Confirms and executes logout.
     */
    @FXML
    private void handleLogout() {
        if (NavigationUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            authService.logout();
            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                NavigationUtil.loadScene("login.fxml", stage, "Smart Classroom Management System");
            } catch (Exception e) {
                NavigationUtil.showError("Navigation Error", e.getMessage());
            }
        }
    }

    /**
     * Updates navigation button active styles.
     */
    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-btn-active");
        }
        if (btn != null) {
            btn.getStyleClass().add("nav-btn-active");
            activeNavBtn = btn;
        }
    }
}

