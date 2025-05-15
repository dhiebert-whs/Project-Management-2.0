package org.frcpm.di;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating dialogs using MVVMFx.
 * This standardizes dialog creation across the application.
 */
public class DialogFactory {
    
    private static final Logger LOGGER = Logger.getLogger(DialogFactory.class.getName());
    
    /**
     * Shows a modal dialog using MVVMFx.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class to load
     * @param title the title for the dialog
     * @param owner the owner window
     * @param viewModelInitializer a consumer to initialize the view model
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner, Consumer<VM> viewModelInitializer) {
        
        LOGGER.fine("Creating dialog: " + viewClass.getSimpleName());
        
        try {
            // Load the view using MVVMFx
            ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass).load();
            
            // Get the view model
            VM viewModel = viewTuple.getViewModel();
            
            // Initialize the view model if needed
            if (viewModelInitializer != null) {
                viewModelInitializer.accept(viewModel);
            }
            
            // Create scene and stage
            Scene scene = new Scene(viewTuple.getView());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            // Configure modality
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
            
            // Show the dialog and wait
            stage.showAndWait();
            return viewTuple;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating dialog", e);
            throw new RuntimeException("Failed to create dialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows a modal dialog using MVVMFx with resource bundle.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class to load
     * @param title the title for the dialog
     * @param owner the owner window
     * @param resources the resource bundle
     * @param viewModelInitializer a consumer to initialize the view model
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner, ResourceBundle resources, 
            Consumer<VM> viewModelInitializer) {
        
        LOGGER.fine("Creating dialog with resources: " + viewClass.getSimpleName());
        
        try {
            // Load the view using MVVMFx with resources
            ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass)
                .resourceBundle(resources)
                .load();
            
            // Get the view model
            VM viewModel = viewTuple.getViewModel();
            
            // Initialize the view model if needed
            if (viewModelInitializer != null) {
                viewModelInitializer.accept(viewModel);
            }
            
            // Create scene and stage
            Scene scene = new Scene(viewTuple.getView());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            // Configure modality
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
            
            // Show the dialog and wait
            stage.showAndWait();
            return viewTuple;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating dialog with resources", e);
            throw new RuntimeException("Failed to create dialog: " + e.getMessage(), e);
        }
    }

    /**
     * Debug method to check FXML file existence and path resolution.
     * 
     * @param viewClass the view class to check
     * @return information about file resolution
     */
    public static <V extends FxmlView<?>> String debugViewResolution(Class<V> viewClass) {
        StringBuilder debug = new StringBuilder();
        debug.append("Debugging view resolution for ").append(viewClass.getName()).append("\n");
        
        // Get class name and derive expected FXML name according to MVVMFx convention
        String simpleName = viewClass.getSimpleName();
        
        debug.append("Class simple name: ").append(simpleName).append("\n");
        
        // Check for presence of files
        String packagePath = viewClass.getPackage().getName().replace('.', '/');
        String resourceName = "/" + packagePath + "/" + simpleName + ".fxml";
        debug.append("Looking for resource: ").append(resourceName).append("\n");
        
        boolean resourceExists = viewClass.getResource(resourceName) != null;
        debug.append("Resource exists: ").append(resourceExists).append("\n");
        
        // Also check in Java package structure
        String javaPackagePath = packagePath + "/" + simpleName + ".fxml";
        debug.append("Looking for resource in Java package: ").append(javaPackagePath).append("\n");
        
        boolean javaResourceExists = viewClass.getClassLoader().getResource(javaPackagePath) != null;
        debug.append("Resource exists in Java package: ").append(javaResourceExists).append("\n");
        
        // Check for properties file
        String propertiesName = "/" + packagePath + "/" + simpleName + ".properties";
        boolean propertiesExist = viewClass.getResource(propertiesName) != null;
        debug.append("Properties file exists: ").append(propertiesExist).append(" (").append(propertiesName).append(")\n");
        
        return debug.toString();
    }
}