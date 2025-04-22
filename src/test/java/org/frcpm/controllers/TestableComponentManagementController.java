// src/test/java/org/frcpm/controllers/TestableComponentManagementController.java
package org.frcpm.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.frcpm.models.Component;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Specialized version of ComponentManagementController for testing purposes.
 * Overrides methods that interact with JavaFX components to enable testing
 * without requiring the JavaFX toolkit.
 */
public class TestableComponentManagementController extends ComponentManagementController {
    
    private static final Logger LOGGER = Logger.getLogger(TestableComponentManagementController.class.getName());
    
    // Tracking variables for tests
    private boolean componentDialogShown = false;
    private String lastDialogTitle = null;
    private Component lastEditedComponent = null;
    private boolean addComponentCalled = false;
    
    /**
     * Overrides the method to track calls without actually showing a dialog.
     */
    @Override
    protected void showComponentDialog(String title, Component component) throws IOException {
        // Track the call instead of showing an actual dialog
        componentDialogShown = true;
        lastDialogTitle = title;
        lastEditedComponent = component;
        
        // Simulate successful dialog completion
        getViewModel().loadComponents();
    }
    
    /**
     * Overrides to avoid creating actual FXML loader.
     */
    @Override
    protected FXMLLoader createFXMLLoader(String fxmlFile) {
        // Return a mock loader that won't actually load FXML
        return new FXMLLoader();
    }
    
    /**
     * Overrides to avoid creating an actual Stage.
     */
    @Override
    protected Stage createDialogStage(String title, Parent content) {
        // Return a mock stage that won't actually show
        return new Stage();
    }
    
    /**
     * Overrides the add component method to track calls.
     */
    @Override
    public void handleAddComponent() {
        addComponentCalled = true;
        try {
            // Call our overridden method instead of the original
            showComponentDialog("New Component", null);
        } catch (Exception e) {
            // Log and ignore in test environment
            LOGGER.info("Test exception: " + e.getMessage());
        }
    }
    
    /**
     * Overrides the edit component method to track calls.
     */
    @Override
    protected void handleEditComponent(Component component) {
        lastEditedComponent = component;
        try {
            // Call our overridden method instead of the original
            showComponentDialog("Edit Component", component);
        } catch (Exception e) {
            // Log and ignore in test environment
            LOGGER.info("Test exception: " + e.getMessage());
        }
    }
    
    // Accessors for test verification
    
    public boolean wasComponentDialogShown() {
        return componentDialogShown;
    }
    
    public String getLastDialogTitle() {
        return lastDialogTitle;
    }
    
    public Component getLastEditedComponent() {
        return lastEditedComponent;
    }
    
    public boolean wasAddComponentCalled() {
        return addComponentCalled;
    }
    
    public void resetTracking() {
        componentDialogShown = false;
        lastDialogTitle = null;
        lastEditedComponent = null;
        addComponentCalled = false;
    }
}