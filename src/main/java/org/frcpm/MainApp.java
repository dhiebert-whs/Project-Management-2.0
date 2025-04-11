package org.frcpm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubteamService;
import org.frcpm.utils.DatabaseInitializer;
import org.frcpm.utils.DatabaseTestUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the FRC Project Management System.
 * This is the entry point for the JavaFX application.
 */
public class MainApp extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseInitializer.initialize(true); // Pass true to create sample data



            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("FRC Project Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            LOGGER.info("FRC Project Management System started");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading main view", e);
            showErrorAndExit("Error loading main view: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application stops
        LOGGER.info("FRC Project Management System stopping...");
        DatabaseConfig.shutdown();
    }
    
    @Override
    public void init() throws Exception {
        // Initialize resources before application starts
        LOGGER.info("Initializing FRC Project Management System...");
        try {
            DatabaseConfig.initialize();
            LOGGER.info("Database initialized successfully");
            
            // Test database connection and schema
            boolean dbTestSuccess = DatabaseTestUtil.testDatabase();
            if (!dbTestSuccess) {
                LOGGER.severe("Database test failed - application may not function correctly");
            }
            
            // Check if this is the first run (no projects exist)
            boolean firstRun = ServiceFactory.getProjectService().findAll().isEmpty();
            if (firstRun) {
                LOGGER.info("First run detected - creating default data");
                createDefaultData();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            showErrorAndExit("Error initializing database: " + e.getMessage());
        }
    }


    /**
     * Creates default data for first-time run.
     */
    private void createDefaultData() {
        try {
            // Create default subteams
            SubteamService subteamService = ServiceFactory.getSubteamService();
            subteamService.createSubteam("Programming", "#3366CC", "Java, Vision, Controls");
            subteamService.createSubteam("Mechanical", "#CC3333", "CAD, Fabrication, Assembly");
            subteamService.createSubteam("Electrical", "#FFCC00", "Wiring, Electronics, Control Systems");
            
            LOGGER.info("Default data created successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating default data", e);
        }
    }

    private void showErrorAndExit(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Fatal Error");
            alert.setContentText(message);
            alert.showAndWait();
            System.exit(1);
        });
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