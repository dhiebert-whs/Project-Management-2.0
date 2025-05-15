// src/main/java/org/frcpm/di/ViewLoader.java
package org.frcpm.di;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for loading views and controllers using MVVMFx.
 * Provides consistent view loading throughout the application.
 */
public class ViewLoader {
    
    private static final Logger LOGGER = Logger.getLogger(ViewLoader.class.getName());
    
    /**
     * Loads a view based on the view class.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to load
     * @return the loaded view as a Parent
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> Parent loadView(Class<V> viewClass) {
        LOGGER.fine("Loading view: " + viewClass.getSimpleName());
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass).load();
        return viewTuple.getView();
    }
    
    /**
     * Loads a view and gets its view model.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to load
     * @return the view model associated with the view
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> VM loadViewModel(Class<V> viewClass) {
        LOGGER.fine("Loading view model for: " + viewClass.getSimpleName());
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass).load();
        return viewTuple.getViewModel();
    }
    
    /**
     * Loads a view tuple containing both view and view model.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to load
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> loadViewTuple(Class<V> viewClass) {
        LOGGER.fine("Loading view tuple for: " + viewClass.getSimpleName());
        return FluentViewLoader.fxmlView(viewClass).load();
    }
    
    /**
     * Loads a view tuple with a resource bundle.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to load
     * @param resources the resource bundle
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> loadViewTuple(
            Class<V> viewClass, ResourceBundle resources) {
        LOGGER.fine("Loading view tuple with resources for: " + viewClass.getSimpleName());
        return FluentViewLoader.fxmlView(viewClass).resourceBundle(resources).load();
    }
    
    /**
     * Shows a view in a new window.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @return the stage containing the view
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> Stage showView(
            Class<V> viewClass, String title) {
        return showView(viewClass, title, null, null);
    }
    
    /**
     * Shows a view in a new modal window with the specified parent window.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @param owner the owner window
     * @return the stage containing the view
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> Stage showView(
            Class<V> viewClass, String title, Window owner) {
        return showView(viewClass, title, owner, null);
    }
    
    /**
     * Shows a view in a new window with a resource bundle.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to show
     * @param title the title for the window
     * @param owner the owner window (can be null)
     * @param resources the resource bundle (can be null)
     * @return the stage containing the view
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> Stage showView(
            Class<V> viewClass, String title, Window owner, ResourceBundle resources) {
        LOGGER.fine("Showing view: " + viewClass.getSimpleName());
        
        try {
            // Load the view
            ViewTuple<V, VM> viewTuple;
            if (resources != null) {
                viewTuple = FluentViewLoader.fxmlView(viewClass).resourceBundle(resources).load();
            } else {
                viewTuple = FluentViewLoader.fxmlView(viewClass).load();
            }
            
            // Create scene and stage
            Scene scene = new Scene(viewTuple.getView());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }
            
            stage.show();
            return stage;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing view", e);
            throw new RuntimeException("Failed to show view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows a view in a new modal dialog and waits for it to close.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to show
     * @param title the title for the dialog
     * @param owner the owner window
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner) {
        return showDialog(viewClass, title, owner, null);
    }
    
/**
     * Shows a view in a new modal dialog with resources and waits for it to close.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the class of the view to show
     * @param title the title for the dialog
     * @param owner the owner window
     * @param resources the resource bundle (can be null)
     * @return the view tuple containing both view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner, ResourceBundle resources) {
        LOGGER.fine("Showing dialog: " + viewClass.getSimpleName());
        
        try {
            // Load the view
            ViewTuple<V, VM> viewTuple;
            if (resources != null) {
                viewTuple = FluentViewLoader.fxmlView(viewClass).resourceBundle(resources).load();
            } else {
                viewTuple = FluentViewLoader.fxmlView(viewClass).load();
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
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
            throw new RuntimeException("Failed to show dialog: " + e.getMessage(), e);
        }
    }
    

    /**
     * Utility method to diagnose FXML loading issues.
     * Helps identify path issues and missing resources.
     * 
     * @param <V> the view type
     * @param viewClass the class of the view to check
     * @return diagnostic information as a string
     */
    public static <V extends ViewTuple> String diagnoseViewLoading(Class<V> viewClass) {
        StringBuilder diagnosis = new StringBuilder();
        diagnosis.append("View Loading Diagnosis for: ").append(viewClass.getName()).append("\n\n");
        
        // Check class location
        diagnosis.append("Class location: ").append(viewClass.getProtectionDomain().getCodeSource().getLocation()).append("\n");
        
        // Get expected FXML path
        String packageName = viewClass.getPackage().getName();
        String simpleName = viewClass.getSimpleName();
        
        // Standard location (resources directory)
        String resourcePath = "/" + packageName.replace('.', '/') + "/" + simpleName + ".fxml";
        diagnosis.append("Expected FXML resource path: ").append(resourcePath).append("\n");
        diagnosis.append("Resource exists: ").append(viewClass.getResource(resourcePath) != null).append("\n");
        
        // Java package location (MVVMFx also looks here)
        String javaPath = packageName.replace('.', '/') + "/" + simpleName + ".fxml";
        diagnosis.append("Expected FXML Java package path: ").append(javaPath).append("\n");
        diagnosis.append("Resource exists in Java package: ").append(
            viewClass.getClassLoader().getResource(javaPath) != null).append("\n");
        
        // Check properties files
        String resourcePropertiesPath = "/" + packageName.replace('.', '/') + "/" + simpleName + ".properties";
        diagnosis.append("Expected properties path: ").append(resourcePropertiesPath).append("\n");
        diagnosis.append("Properties exist: ").append(viewClass.getResource(resourcePropertiesPath) != null).append("\n");
        
        // Check common path issues
        String alternativePath = "/" + packageName.replace('.', '/') + "/" + 
            simpleName.replace("MvvmView", "").toLowerCase() + ".fxml";
        diagnosis.append("Alternative path to check: ").append(alternativePath).append("\n");
        diagnosis.append("Alternative exists: ").append(viewClass.getResource(alternativePath) != null).append("\n");
        
        try {
            // Try loading directly with FluentViewLoader - but modify to avoid type issues
            diagnosis.append("\nAttempting to load view with FluentViewLoader: ");
            if (viewClass.isAssignableFrom(de.saxsys.mvvmfx.FxmlView.class)) {
                // Only try loading if it's really an FxmlView
                FluentViewLoader.fxmlView((Class<? extends de.saxsys.mvvmfx.FxmlView<?>>) viewClass).load();
                diagnosis.append("SUCCESS\n");
            } else {
                diagnosis.append("SKIPPED - class is not an FxmlView\n");
            }
        } catch (Exception e) {
            diagnosis.append("FAILED\n");
            diagnosis.append("Error: ").append(e.getMessage()).append("\n");
            
            // Get more details about the exception
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
                diagnosis.append("Caused by: ").append(cause.getClass().getName())
                    .append(": ").append(cause.getMessage()).append("\n");
            }
        }
        
        return diagnosis.toString();
    }
    
    /**
     * Creates a copy of FXML file in the Java package for MVVMFx compatibility.
     * This is a helper for MVVMFx migration as it expects FXML files in the Java package.
     * 
     * @param <V> the view type
     * @param viewClass the view class
     * @return true if the copy was created successfully, false otherwise
     */
    public static <V extends FxmlView<?>> boolean createFxmlCopyInJavaPackage(Class<V> viewClass) {
        try {
            String packageName = viewClass.getPackage().getName();
            String simpleName = viewClass.getSimpleName();
            
            // Get the original FXML path
            String resourcePath = "/" + packageName.replace('.', '/') + "/" + simpleName + ".fxml";
            java.net.URL resourceUrl = viewClass.getResource(resourcePath);
            
            if (resourceUrl == null) {
                LOGGER.severe("Original FXML file not found at: " + resourcePath);
                return false;
            }
            
            // Load the original content
            java.io.InputStream inputStream = resourceUrl.openStream();
            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            
            // Create output directory and file
            String outputDir = "src/main/java/" + packageName.replace('.', '/');
            java.io.File dir = new java.io.File(outputDir);
            dir.mkdirs();
            
            java.io.File outputFile = new java.io.File(dir, simpleName + ".fxml");
            
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
}