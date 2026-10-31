package com.smartclassroom.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Static utility class for JavaFX scene/pane navigation and common alert dialogs.
 *
 * <p>All FXML files are expected to reside in the {@code /fxml/} directory on the
 * classpath (e.g. {@code src/main/resources/fxml/Login.fxml}).
 *
 * <p>This class must not be instantiated; all methods are static.
 */
public final class NavigationUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private NavigationUtil() {
    }

    /**
     * Loads an FXML file and replaces the scene on the given {@link Stage}.
     * Use this method when navigating between top-level screens.
     *
     * @param fxmlPath the FXML filename (without path prefix), e.g. {@code "Login.fxml"}
     * @param stage    the {@link Stage} whose scene will be replaced
     * @param title    the window title to set on the stage
     * @throws IOException if the FXML file cannot be found or parsed
     */
    public static void loadScene(String fxmlPath, Stage stage, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                NavigationUtil.class.getResource("/fxml/" + fxmlPath));
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: /fxml/" + fxmlPath);
        }
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Loads an FXML file and returns its root {@link Parent} node.
     * Use this method when embedding a pane inside an existing layout (e.g., a
     * content area within a dashboard).
     *
     * @param fxmlPath the FXML filename (without path prefix), e.g. {@code "AttendanceView.fxml"}
     * @return the root {@link Parent} node of the loaded FXML
     * @throws IOException if the FXML file cannot be found or parsed
     */
    public static Parent loadPane(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                NavigationUtil.class.getResource("/fxml/" + fxmlPath));
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: /fxml/" + fxmlPath);
        }
        return loader.load();
    }

    /**
     * Displays a modal error alert dialog.
     *
     * @param title   the dialog window title
     * @param message the error message body to display
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a modal informational alert dialog.
     *
     * @param title   the dialog window title
     * @param message the informational message body to display
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a modal confirmation dialog with OK and Cancel buttons.
     *
     * @param title   the dialog window title
     * @param message the confirmation question to display
     * @return {@code true} if the user clicked OK; {@code false} if they cancelled or closed
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
