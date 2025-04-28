package org.frcpm;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.di.FrcpmModule;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
import org.frcpm.services.SubteamService;
import org.frcpm.utils.DatabaseInitializer;
import org.frcpm.utils.DatabaseTestUtil;
import org.frcpm.views.MainView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the FRC Project Management System.
 * This is the entry point for the JavaFX application.
 */
public class MainApp extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    
    // Flag to indicate development mode (can be set via JVM args)
    private static boolean developmentMode = Boolean.getBoolean("app.db.dev");
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize AfterburnerFX dependency injection
            FrcpmModule.initialize();
            
            // Load the main view using AfterburnerFX
            Parent root = ViewLoader.loadView(MainView.class);
            
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("FRC Project Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            LOGGER.info("FRC Project Management System started");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading main view", e);
            showErrorAndExit("Error loading main view: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application stops
        LOGGER.info("FRC Project Management System stopping...");
        
        // Shutdown AfterburnerFX
        FrcpmModule.shutdown();
        
        // Shutdown database
        DatabaseConfig.shutdown();
    }
    
    @Override
    public void init() throws Exception {
        // Initialize resources before application starts
        LOGGER.info("Initializing FRC Project Management System...");
        try {
            // Set development mode for database (create-drop vs update)
            DatabaseConfig.setDevelopmentMode(developmentMode);
            
            // Initialize database configuration with appropriate schema mode
            DatabaseConfig.initialize();
            LOGGER.info("Database initialized in " + 
                       (developmentMode ? "DEVELOPMENT" : "PRODUCTION") + " mode");
            
            // Test database connection and schema
            boolean dbTestSuccess = DatabaseTestUtil.testDatabase();
            if (!dbTestSuccess) {
                LOGGER.severe("Database test failed - application may not function correctly");
            }
            
            // Check if this is the first run (no projects exist)
            boolean firstRun = ServiceProvider.getProjectService().findAll().isEmpty();
            if (firstRun) {
                LOGGER.info("First run detected - creating initial data");
                DatabaseInitializer.initialize(true); // Create sample data
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
            SubteamService subteamService = ServiceProvider.getSubteamService();
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
        // Check for development mode flag in arguments
        for (String arg : args) {
            if (arg.equals("--dev")) {
                developmentMode = true;
                break;
            }
        }
        
        // Launch the JavaFX application
        launch(args);
    }
}