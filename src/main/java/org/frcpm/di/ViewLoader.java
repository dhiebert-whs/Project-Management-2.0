// src/main/java/org/frcpm/di/ViewLoader.java

package org.frcpm.di;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Utility class for loading views and controllers using AfterburnerFX.
 * Provides consistent view loading throughout the application.
 */
public class ViewLoader {
    
    private static final Logger LOGGER = Logger.getLogger(ViewLoader.class.getName());
    
    /**
     * Loads a view based on the view class.
     * 
     * @param viewClass the class of the view to load
     * @return the loaded view as a Parent
     */
    public static Parent loadView(Class<? extends FXMLView> viewClass) {
        LOGGER.fine("Loading view: " + viewClass.getSimpleName());
        FXMLView view = instantiateView(viewClass);
        return view.getView();
    }
    
    /**
     * Loads a view and gets its controller.
     * 
     * @param <T> the type of the controller
     * @param viewClass the class of the view to load
     * @return the controller associated with the view
     */
    public static <T> T loadController(Class<? extends FXMLView> viewClass) {
        LOGGER.fine("Loading controller for: " + viewClass.getSimpleName());
        FXMLView view = instantiateView(viewClass);
        return (T) view.getPresenter(); // Add explicit cast to T
    }
    
    /**
     * Shows a view in a new window.
     * 
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @return the stage containing the view
     */
    public static Stage showView(Class<? extends FXMLView> viewClass, String title) {
        return showView(viewClass, title, null, null);
    }
    
    /**
     * Shows a view in a new modal window with the specified parent window.
     * 
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @param owner the owner window
     * @return the stage containing the view
     */
    public static Stage showView(Class<? extends FXMLView> viewClass, String title, Window owner) {
        return showView(viewClass, title, owner, null);
    }
    
    /**
     * Shows a view in a new window with a resource bundle.
     * 
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @param owner the owner window (can be null)
     * @param resources the resource bundle (can be null)
     * @return the stage containing the view
     */
    public static Stage showView(Class<? extends FXMLView> viewClass, String title, Window owner, ResourceBundle resources) {
        LOGGER.fine("Showing view: " + viewClass.getSimpleName());
        FXMLView view;
        
        if (resources != null) {
            view = instantiateView(viewClass, resources);
        } else {
            view = instantiateView(viewClass);
        }
        
        Parent root = view.getView();
        Scene scene = new Scene(root);
        
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        }
        
        stage.show();
        return stage;
    }
    
    /**
     * Shows a view in a new modal dialog and waits for it to close.
     * 
     * @param <T> the type of the controller
     * @param viewClass the class of the view to show
     * @param title the title for the dialog
     * @param owner the owner window
     * @return the controller associated with the view
     */
    public static <T> T showDialog(Class<? extends FXMLView> viewClass, String title, Window owner) {
        LOGGER.fine("Showing dialog: " + viewClass.getSimpleName());
        FXMLView view = instantiateView(viewClass);
        Parent root = view.getView();
        T controller = (T) view.getPresenter();
        
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        
        stage.showAndWait();
        return controller;
    }
    
    /**
     * Creates an instance of a view class.
     * 
     * @param viewClass the class of the view to instantiate
     * @return the instantiated view
     */
    private static FXMLView instantiateView(Class<? extends FXMLView> viewClass) {
        try {
            return viewClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.severe("Error instantiating view: " + viewClass.getSimpleName());
            throw new RuntimeException("Failed to instantiate view: " + viewClass.getSimpleName(), e);
        }
    }
    
    /**
     * Creates an instance of a view class with a resource bundle.
     * 
     * @param viewClass the class of the view to instantiate
     * @param resources the resource bundle
     * @return the instantiated view
     */
    private static FXMLView instantiateView(Class<? extends FXMLView> viewClass, ResourceBundle resources) {
        try {
            return viewClass.getDeclaredConstructor(ResourceBundle.class).newInstance(resources);
        } catch (Exception e) {
            LOGGER.severe("Error instantiating view with resources: " + viewClass.getSimpleName());
            throw new RuntimeException("Failed to instantiate view: " + viewClass.getSimpleName(), e);
        }
    }
}