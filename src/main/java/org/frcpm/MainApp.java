// src/main/java/org/frcpm/MainApp.java

package org.frcpm;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.di.FrcpmModule;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
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

            // Configure default uncaught exception handler for JavaFX thread
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                LOGGER.log(Level.SEVERE, "Uncaught exception in JavaFX thread", throwable);
                showErrorAndContinue("Application Error",
                        "An unexpected error occurred. Please try again or restart the application.",
                        throwable.getMessage());
            });

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

        try {
            // Shutdown AfterburnerFX
            FrcpmModule.shutdown();

            // Properly forget all dependencies
            Injector.forgetAll();

            // Shutdown database with appropriate closure
            DatabaseConfig.shutdown();

            LOGGER.info("Application shutdown complete");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during application shutdown", e);
        }
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
     * Shows a non-fatal error dialog but allows the application to continue.
     * 
     * @param title   the dialog title
     * @param header  the header message
     * @param message the detailed message
     */
    private void showErrorAndContinue(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a fatal error dialog and exits the application.
     * 
     * @param message the error message
     */
    private void showErrorAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
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