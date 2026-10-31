package com.smartclassroom;

import javafx.application.Application;
import javafx.stage.Stage;
import com.smartclassroom.util.NavigationUtil;

/**
 * Entry point for the Smart Classroom Management System.
 * Loads the login screen and starts the JavaFX application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        NavigationUtil.loadScene("login.fxml", primaryStage, "Smart Classroom Management System");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

