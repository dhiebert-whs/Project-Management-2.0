package org.frcpm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the FRC Project Management System.
 * This is the entry point for the JavaFX application.
 */
public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("FRC Project Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Log application start
            System.out.println("FRC Project Management System started");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading main view: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application stops
        System.out.println("FRC Project Management System stopping...");
        // TODO: Close database connections and other resources
    }
    
    @Override
    public void init() throws Exception {
        // Initialize resources before application starts
        System.out.println("Initializing FRC Project Management System...");
        // TODO: Initialize database connection and other resources
    }

    /**
     * Main method that launches the JavaFX application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}