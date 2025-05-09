// src/main/java/org/frcpm/mvvm/MvvmViewAdapter.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter class to ease transition from AfterburnerFX to MVVMFx.
 * Provides compatibility layer for view loading and initialization.
 */
public abstract class MvvmViewAdapter<VM extends ViewModel> {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmViewAdapter.class.getName());
    
    private Parent root;
    private VM viewModel;
    private Object controller;
    
    /**
     * Creates a new MVVMFx view adapter.
     */
    public MvvmViewAdapter() {
        loadFxml();
    }
    
    /**
     * Creates the view model for this view.
     * Must be implemented by subclasses.
     * 
     * @return the view model
     */
    protected abstract VM createViewModel();
    
    /**
     * Loads the FXML file for this view.
     */
    private void loadFxml() {
        try {
            String fxmlPath = getFxmlPath();
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                LOGGER.severe("Could not find FXML file: " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            
            // Create the controller and set it
            try {
                // Determine controller class from FXML naming convention
                String controllerClassName = getControllerClassName();
                Class<?> controllerClass = Class.forName(controllerClassName);
                controller = controllerClass.getDeclaredConstructor().newInstance();
                loader.setController(controller);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "Could not find controller class, will use controller specified in FXML", e);
            }
            
            // Load the FXML file
            root = loader.load();
            
            // If controller wasn't set earlier, get it from the loader
            if (controller == null) {
                controller = loader.getController();
            }
            
            // Create view model
            viewModel = createViewModel();
            
            // Inject view model into controller if it has @InjectViewModel annotation
            if (controller != null && viewModel != null) {
                injectViewModel(controller, viewModel);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading FXML", e);
        }
    }
    
    /**
     * Gets the path to the FXML file for this view.
     * 
     * @return the FXML file path
     */
    protected String getFxmlPath() {
        // Determine FXML path from class name
        String className = getClass().getSimpleName();
        String packageName = getClass().getPackage().getName();
        String packagePath = packageName.replace('.', '/');
        
        // Remove "View" suffix and convert to lowercase for FXML name
        String fxmlName;
        if (className.endsWith("View")) {
            fxmlName = className.substring(0, className.length() - 4).toLowerCase();
        } else {
            fxmlName = className.toLowerCase();
        }
        
        // Check for different naming variants
        String[] variants = {
            "/" + packagePath + "/" + fxmlName + ".fxml",
            "/" + packagePath + "/" + fxmlName + "view.fxml",
            "/" + packagePath + "/views/" + fxmlName + ".fxml",
            "/" + packagePath + "/views/" + fxmlName + "view.fxml"
        };
        
        for (String variant : variants) {
            if (getClass().getResource(variant) != null) {
                return variant;
            }
        }
        
        // Default to standard path
        return "/" + packagePath + "/" + fxmlName + ".fxml";
    }
    
    /**
     * Gets the class name of the controller for this view.
     * 
     * @return the controller class name
     */
    protected String getControllerClassName() {
        String className = getClass().getSimpleName();
        String packageName = getClass().getPackage().getName();
        
        // Convert ViewName to ControllerName or PresenterName
        String controllerName;
        if (className.endsWith("View")) {
            // Try presenter first
            String presenterName = packageName.replace(".views", ".presenters") + "." + 
                                   className.substring(0, className.length() - 4) + "Presenter";
            try {
                Class.forName(presenterName);
                return presenterName;
            } catch (ClassNotFoundException e) {
                // Try controller
                controllerName = packageName.replace(".views", ".controllers") + "." + 
                                className.substring(0, className.length() - 4) + "Controller";
            }
        } else {
            // If not ending with View, just append Controller
            controllerName = packageName + "." + className + "Controller";
        }
        
        return controllerName;
    }
    
    /**
     * Injects the view model into a controller using reflection to support the @InjectViewModel annotation.
     * 
     * @param controller the controller
     * @param viewModel the view model
     */
    private void injectViewModel(Object controller, VM viewModel) {
        Class<?> controllerClass = controller.getClass();
        for (Field field : controllerClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectViewModel.class)) {
                field.setAccessible(true);
                try {
                    field.set(controller, viewModel);
                } catch (IllegalAccessException e) {
                    LOGGER.log(Level.SEVERE, "Error injecting view model", e);
                }
            }
        }
    }
    
    /**
     * Gets the root node of this view.
     * 
     * @return the root node
     */
    public Parent getView() {
        return root;
    }
    
    /**
     * Gets the view model for this view.
     * 
     * @return the view model
     */
    public VM getViewModel() {
        return viewModel;
    }
    
    /**
     * Gets the controller for this view.
     * 
     * @return the controller
     */
    public Object getController() {
        return controller;
    }
    
    /**
     * Loads a view with MVVMFx.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @return the view tuple containing the view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> load(Class<V> viewClass) {
        return FluentViewLoader.fxmlView(viewClass).load();
    }
    
    /**
     * Loads a view with MVVMFx and a resource bundle.
     * 
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @param resources the resource bundle
     * @return the view tuple containing the view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> load(
            Class<V> viewClass, ResourceBundle resources) {
        return FluentViewLoader.fxmlView(viewClass).resourceBundle(resources).load();
    }
}