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
}