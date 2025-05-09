// src/main/java/org/frcpm/di/DialogFactory.java

package org.frcpm.di;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.airhacks.afterburner.views.FXMLView;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating dialogs using AfterburnerFX.
 * This standardizes dialog creation across the application.
 */
public class DialogFactory {
    
    private static final Logger LOGGER = Logger.getLogger(DialogFactory.class.getName());
    
    /**
     * Creates and shows a modal dialog.
     * 
     * @param <T> the type of the presenter
     * @param viewClass the view class to load
     * @param title the title for the dialog
     * @param owner the owner window
     * @param presenterInitializer a consumer to initialize the presenter
     * @return the presenter instance
     */
    public static <T> T showDialog(Class<? extends FXMLView> viewClass, String title, 
            Window owner, Consumer<T> presenterInitializer) {
        
        LOGGER.fine("Creating dialog: " + viewClass.getSimpleName());
        
        try {
            // Create the view
            FXMLView view = viewClass.getDeclaredConstructor().newInstance();
            Parent root = view.getView();
            T presenter = (T) view.getPresenter();
            
            // Initialize the presenter if needed
            if (presenterInitializer != null) {
                presenterInitializer.accept(presenter);
            }
            
            // Create scene and stage
            Scene scene = new Scene(root);
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
            return presenter;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating dialog", e);
            throw new RuntimeException("Failed to create dialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates and shows a modal dialog with resource bundle.
     * 
     * @param <T> the type of the presenter
     * @param viewClass the view class to load
     * @param title the title for the dialog
     * @param owner the owner window
     * @param resources the resource bundle
     * @param presenterInitializer a consumer to initialize the presenter
     * @return the presenter instance
     */
    public static <T> T showDialog(Class<? extends FXMLView> viewClass, String title, 
            Window owner, ResourceBundle resources, Consumer<T> presenterInitializer) {
        
        LOGGER.fine("Creating dialog with resources: " + viewClass.getSimpleName());
        
        try {
            // Create the view with resources
            FXMLView view = viewClass.getDeclaredConstructor(ResourceBundle.class)
                .newInstance(resources);
            Parent root = view.getView();
            T presenter = (T) view.getPresenter();
            
            // Initialize the presenter if needed
            if (presenterInitializer != null) {
                presenterInitializer.accept(presenter);
            }
            
            // Create scene and stage
            Scene scene = new Scene(root);
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
            return presenter;
            
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
    public static String debugViewResolution(Class<? extends FXMLView> viewClass) {
        StringBuilder debug = new StringBuilder();
        debug.append("Debugging view resolution for ").append(viewClass.getName()).append("\n");
        
        // Get class name and derive expected FXML name according to AfterburnerFX convention
        String simpleName = viewClass.getSimpleName();
        // Remove "View" suffix if present
        String baseName = simpleName.endsWith("View") ? 
                simpleName.substring(0, simpleName.length() - 4).toLowerCase() : 
                simpleName.toLowerCase();
        
        debug.append("Base name for FXML lookup: ").append(baseName).append("\n");
        
        // Check for presence of files
        String packagePath = viewClass.getPackage().getName().replace('.', '/');
        String resourceName = "/" + packagePath + "/" + baseName + ".fxml";
        debug.append("Looking for resource: ").append(resourceName).append("\n");
        
        boolean resourceExists = viewClass.getResource(resourceName) != null;
        debug.append("Resource exists: ").append(resourceExists).append("\n");
        
        // Check for variants
        String variantWithView = "/" + packagePath + "/" + baseName + "view.fxml";
        boolean variantExists = viewClass.getResource(variantWithView) != null;
        debug.append("Variant with 'view' suffix exists: ").append(variantExists).append(" (").append(variantWithView).append(")\n");
        
        // Check for properties file
        String propertiesName = "/" + packagePath + "/" + baseName + ".properties";
        boolean propertiesExist = viewClass.getResource(propertiesName) != null;
        debug.append("Properties file exists: ").append(propertiesExist).append(" (").append(propertiesName).append(")\n");
        
        return debug.toString();
    }
}