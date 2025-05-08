// src/main/java/org/frcpm/MainApp.java

package org.frcpm;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.di.FrcpmModule;
import org.frcpm.di.ViewLoader;
import org.frcpm.utils.ErrorHandler;
import org.frcpm.views.ProjectListView;
import org.frcpm.views.SimpleProjectListView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the FRC Project Management System.
 * Initializes the AfterburnerFX framework and loads the main view.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.info("Starting FRC Project Management System");
            
            // Initialize the AfterburnerFX module
            FrcpmModule.initialize();
            
            // Set up error handler
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                LOGGER.log(Level.SEVERE, "Uncaught exception", throwable);
                ErrorHandler.showError("Application Error", 
                    "An unexpected error occurred: " + throwable.getMessage(), throwable);
            });
            
            // Set the application title
            primaryStage.setTitle("FRC Project Management System");
            
            // Load the view using our simple loader instead of AfterburnerFX
            try {
                SimpleProjectListView simpleView = new SimpleProjectListView();
                Parent root = simpleView.getView();
                
                if (root != null) {
                    primaryStage.setScene(new Scene(root));
                } else {
                    throw new RuntimeException("Failed to load the view");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading view directly", e);
                throw e;
            }
            
            // Show the stage
            primaryStage.show();
            
            LOGGER.info("Application started successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            ErrorHandler.showError("Startup Error", 
                "Failed to start the application: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            // Shut down the module and clean up resources
            FrcpmModule.shutdown();
            LOGGER.info("Application stopped");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error stopping application", e);
        }
    }

    /**
     * Main method to launch the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}