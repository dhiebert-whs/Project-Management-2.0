// src/main/java/org/frcpm/MvvmMainApp.java

package org.frcpm;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.di.FrcpmModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;
import org.frcpm.mvvm.views.ProjectListMvvmView;
import org.frcpm.utils.ErrorHandler;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class using MVVMFx framework.
 * This is an alternative to MainApp.java that uses MVVMFx instead of AfterburnerFX.
 * Designed to work alongside the existing implementation during migration.
 */
public class MvvmMainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MvvmMainApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.info("Starting FRC Project Management System with MVVMFx");
            
            // Initialize the AfterburnerFX module first (needed during migration phase)
            FrcpmModule.initialize();
            
            // Initialize MVVMFx
            MvvmConfig.initialize();
            
            // Set up error handler
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                LOGGER.log(Level.SEVERE, "Uncaught exception", throwable);
                ErrorHandler.showError("Application Error", 
                    "An unexpected error occurred: " + throwable.getMessage(), throwable);
            });
            
            // Set the application title
            primaryStage.setTitle("FRC Project Management System (MVVMFx)");
            
            // Load resource bundle
            ResourceBundle resources = ResourceBundle.getBundle("org.frcpm.mvvm.views.projectlistmvvm", Locale.getDefault());
            
            // Load the view using MVVMFx
            ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(ProjectListMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Set up the scene
            Scene scene = new Scene(viewTuple.getView());
            
            // Add CSS stylesheets if needed
            try {
                String css = MvvmMainApp.class.getResource("/styles/application.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load CSS. Will use default styling.", e);
            }
            
            primaryStage.setScene(scene);
            
            // Show the stage
            primaryStage.show();
            
            LOGGER.info("Application started successfully with MVVMFx");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            ErrorHandler.showError("Startup Error", 
                "Failed to start the application: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            // Shut down MVVMFx
            MvvmConfig.shutdown();
            
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