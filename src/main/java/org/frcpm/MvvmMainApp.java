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
import org.frcpm.di.FrcpmModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.mvvm.viewmodels.MainMvvmViewModel;
import org.frcpm.mvvm.views.MainMvvmView;
import org.frcpm.repositories.impl.ProjectRepositoryImpl;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.ProjectService;
import org.frcpm.services.impl.ProjectServiceImpl;
import org.frcpm.utils.ErrorHandler;

import java.lang.reflect.Field;
import java.net.URL;
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
            
            // Log diagnostic information
            URL directFxmlUrl = getClass().getClassLoader().getResource("org/frcpm/mvvm/views/MainMvvmView.fxml");
            LOGGER.info("Direct FXML URL from resources: " + directFxmlUrl);
            
            // Initialize MVVMFx configuration only
            MvvmConfig.initialize();
            
            // Use direct JavaFX loading for now
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
                    
                    // Create view model directly using MVVMFx's dependency injection
                    // Do not use FrcpmModule (AfterburnerFX)
                    MainMvvmViewModel viewModel = MvvmFX.getCustomDependencyInjector()
                        .call(MainMvvmViewModel.class);
                    
                    if (viewModel == null) {
                        LOGGER.warning("ViewModel was not created by MVVMFx dependency injector. Creating manually.");
                        // Fallback to manual creation with ProjectService
                        ProjectService projectService = MvvmFX.getCustomDependencyInjector()
                            .call(ProjectService.class);
                        
                        if (projectService == null) {
                            LOGGER.warning("ProjectService not available from MVVMFx injector. Creating directly.");
                            // Create services manually as a last resort
                            ProjectRepository projectRepository = new ProjectRepositoryImpl();
                            projectService = new ProjectServiceImpl(projectRepository);
                        }
                        
                        viewModel = new MainMvvmViewModel(projectService);
                    }
                    
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
                LOGGER.log(Level.SEVERE, "Error with direct loading, trying MVVMFx", e);
            }
            
            // If direct loading fails, try MVVMFx as a backup
            ViewTuple<MainMvvmView, MainMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MainMvvmView.class)
                    .resourceBundle(ResourceBundle.getBundle("org.frcpm.mvvm.views.MainMvvmView"))
                    .load();
            
            // Set up the scene
            Scene scene = new Scene(viewTuple.getView());
            primaryStage.setTitle("FRC Project Management System (MVVMFx)");
            primaryStage.setScene(scene);
            primaryStage.show();
            
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
            
            // No more AfterburnerFX shutdown needed
            
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