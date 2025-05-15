// src/main/java/org/frcpm/MvvmMainApp.java
package org.frcpm;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// Remove FrcpmModule and use ServiceLocator instead
import org.frcpm.di.ServiceLocator;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.mvvm.viewmodels.MainMvvmViewModel;
import org.frcpm.mvvm.views.MainMvvmView;
import org.frcpm.services.ProjectService;
import org.frcpm.utils.ErrorHandler;

import java.lang.reflect.Field;
import java.net.URL;
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
            
            // Log diagnostic information
            URL directFxmlUrl = getClass().getClassLoader().getResource("org/frcpm/mvvm/views/MainMvvmView.fxml");
            LOGGER.info("Direct FXML URL from resources: " + directFxmlUrl);
            
            // Create a copy of FXML file in Java package if needed (helper method at bottom of class)
            try {
                createFxmlCopyInJavaPackage(MainMvvmView.class);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not create FXML copy in Java package", e);
            }
            
            // Initialize MVVMFx configuration with ServiceLocator
            MvvmConfig.initialize();
            
            // First try MVVMFx loading directly
            try {
                LOGGER.info("Attempting to load view with MVVMFx");
                ViewTuple<MainMvvmView, MainMvvmViewModel> viewTuple = 
                    FluentViewLoader.fxmlView(MainMvvmView.class)
                        .resourceBundle(ResourceBundle.getBundle("org.frcpm.mvvm.views.MainMvvmView"))
                        .load();
                
                LOGGER.info("MVVMFx loading successful");
                
                // Set up the scene
                Scene scene = new Scene(viewTuple.getView());
                
                // Add CSS stylesheets if needed
                URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                
                primaryStage.setTitle("FRC Project Management System (MVVMFx)");
                primaryStage.setScene(scene);
                primaryStage.show();
                
                LOGGER.info("Application started successfully with MVVMFx");
                return;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "MVVMFx loading failed, falling back to direct loading", e);
            }
            
            // Fall back to direct JavaFX loading if MVVMFx fails
            try {
                if (directFxmlUrl != null) {
                    // Create the loader
                    FXMLLoader loader = new FXMLLoader(directFxmlUrl);
                    
// Load resource bundle
                    ResourceBundle resources = ResourceBundle.getBundle("org.frcpm.mvvm.views.MainMvvmView");
                    loader.setResources(resources);
                    
                    // Load the view
                    Parent root = loader.load();
                    
                    // Get the controller
                    MainMvvmView controller = loader.getController();
                    
                    // Create view model directly using ServiceLocator instead of MVVMFx or FrcpmModule
                    ProjectService projectService = ServiceLocator.getProjectService();
                    MainMvvmViewModel viewModel = new MainMvvmViewModel(projectService);
                    
                    // Use reflection to inject the view model
                    Field viewModelField = MainMvvmView.class.getDeclaredField("viewModel");
                    viewModelField.setAccessible(true);
                    viewModelField.set(controller, viewModel);
                    
                    // Set up the scene
                    Scene scene = new Scene(root);
                    
                    // Add CSS stylesheets if needed
                    URL cssUrl = getClass().getResource("/css/styles.css");
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }
                    
                    primaryStage.setTitle("FRC Project Management System (Direct Loading)");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    
                    LOGGER.info("Application started successfully with direct FXML loading");
                    return;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error with direct loading", e);
            }
            
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
            
            // Shutdown other resources if needed
            try {
                org.frcpm.config.DatabaseConfig.shutdown();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error shutting down database", e);
            }
            
            // Shutdown task executor
            try {
                org.frcpm.async.TaskExecutor.shutdown();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error shutting down task executor", e);
            }
            
            LOGGER.info("Application stopped");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error stopping application", e);
        }
    }

    /**
     * Creates a copy of FXML file in the Java package for MVVMFx compatibility.
     * MVVMFx expects FXML files to be in the same package as the view classes.
     * 
     * @param viewClass the view class
     * @return true if the copy was created successfully, false otherwise
     */
    private boolean createFxmlCopyInJavaPackage(Class<?> viewClass) {
        try {
            String packageName = viewClass.getPackage().getName();
            String simpleName = viewClass.getSimpleName();
            
            // Get the original FXML path
            String resourcePath = "/" + packageName.replace('.', '/') + "/" + simpleName + ".fxml";
            java.net.URL resourceUrl = viewClass.getResource(resourcePath);
            
            if (resourceUrl == null) {
                LOGGER.warning("Original FXML file not found at: " + resourcePath);
                return false;
            }
            
            // Load the original content
            java.io.InputStream inputStream = resourceUrl.openStream();
            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            
            // Create output directory and file
            String outputDir = "src/main/java/" + packageName.replace('.', '/');
            java.io.File dir = new java.io.File(outputDir);
            dir.mkdirs();
            
            java.io.File outputFile = new java.io.File(dir, simpleName + ".fxml");
            
            // Skip if file already exists
            if (outputFile.exists()) {
                LOGGER.info("FXML copy in Java package already exists at: " + outputFile.getAbsolutePath());
                return true;
            }
            
            // Write content to file
            try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
                writer.write(content);
            }
            
            LOGGER.info("Created FXML copy in Java package: " + outputFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating FXML copy", e);
            return false;
        }
    }

    /**
     * Diagnoses MVVMFx issues by checking paths and resources.
     * 
     * @return diagnostic information
     */
    private String diagnoseIssues() {
        StringBuilder diagnosis = new StringBuilder();
        diagnosis.append("MVVMFx Diagnostics:\n");
        
        // Check if MVVMFx is initialized
        diagnosis.append("MVVMFx Config Initialized: ").append(MvvmConfig.isInitialized()).append("\n");
        
        // Check class paths
        diagnosis.append("MainMvvmView class: ").append(MainMvvmView.class.getName()).append("\n");
        
        // Check resource bundle paths
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("org.frcpm.mvvm.views.MainMvvmView");
            diagnosis.append("Resource bundle found: yes\n");
        } catch (Exception e) {
            diagnosis.append("Resource bundle found: no (").append(e.getMessage()).append(")\n");
        }
        
        // Check FXML paths
        String fxmlPath = "/" + MainMvvmView.class.getName().replace('.', '/') + ".fxml";
        URL fxmlUrl = getClass().getResource(fxmlPath);
        diagnosis.append("FXML at ").append(fxmlPath).append(": ").append(fxmlUrl != null ? "found" : "not found").append("\n");
        
        // We cannot directly check the dependency injector
        diagnosis.append("MVVMFx custom dependency injector: configured\n");
        
        return diagnosis.toString();
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