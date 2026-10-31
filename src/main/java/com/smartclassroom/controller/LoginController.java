package com.smartclassroom.controller;

import com.smartclassroom.model.User;
import com.smartclassroom.service.AuthService;
import com.smartclassroom.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigates to the appropriate dashboard
 * based on the logged-in user's role (ADMIN, FACULTY, or STUDENT).
 */
public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label appTitleLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    /**
     * Initializes the login screen. Hides error and loading indicator,
     * and binds Enter key in passwordField to trigger login.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        passwordField.setOnAction(e -> handleLogin());
    }

    /**
     * Handles the Login button click. Validates inputs, calls AuthService,
     * and redirects to the role-appropriate dashboard.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);

        try {
            User user = authService.login(username, password);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            switch (user.getRole()) {
                case "ADMIN"   -> NavigationUtil.loadScene("admin-dashboard.fxml",   stage, "Admin Dashboard");
                case "FACULTY" -> NavigationUtil.loadScene("faculty-dashboard.fxml", stage, "Faculty Dashboard");
                case "STUDENT" -> NavigationUtil.loadScene("student-dashboard.fxml", stage, "Student Dashboard");
                default        -> showError("Unknown role: " + user.getRole());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        }
    }

    /**
     * Displays an error message in the errorLabel.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
