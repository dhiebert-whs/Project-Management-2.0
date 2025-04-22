package org.frcpm.controllers;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Specialized version of NewProjectController for testing.
 * Overrides UI-dependent methods to enable testing without JavaFX toolkit.
 */
public class TestableNewProjectController extends NewProjectController {
    
    private boolean errorMessageHandled = false;
    private String lastErrorTitle;
    private String lastErrorMessage;
    private boolean dialogStageClosed = false;
    
    // For tracking whether the error listener was triggered
    private boolean errorListenerTriggered = false;
    
    @Override
    protected Alert createErrorAlert() {
        // Don't create an actual Alert in tests
        return null;
    }
    
    @Override
    public void setDialogStage(Stage dialogStage) {
        // Store the dialog stage but don't interact with UI components
        super.setDialogStage(dialogStage);
        
        // Check if project was created and mark as closed if so
        if (getViewModel().getCreatedProject() != null) {
            dialogStageClosed = true;
        }
    }
    
    @Override
    public void testShowErrorAlert(String title, String message) {
        // Track the call instead of showing an actual alert
        lastErrorTitle = title;
        lastErrorMessage = message;
        errorMessageHandled = true;
    }
    
    /**
     * Instead of overriding the private showErrorAlert method,
     * we'll provide our own implementation with the same functionality
     */
    private void showErrorAlert(String title, String message) {
        lastErrorTitle = title;
        lastErrorMessage = message;
        errorMessageHandled = true;
    }
    
    /**
     * Override setupErrorHandling to manually handle the listener
     */
    @Override
    public void testSetupErrorHandling() {
        // Don't call super to avoid NullPointerException with JavaFX components
        
        // Instead, manually set up a listener on the ViewModel's error property
        getViewModel().errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                errorListenerTriggered = true;
                showErrorAlert("Error Creating Project", newValue);
                getViewModel().errorMessageProperty().set("");
            }
        });
    }
    
    /**
     * Override initialize to avoid UI component access
     */
    @Override
    public void testInitialize() {
        // Don't call super to avoid NullPointerException with JavaFX components
        
        // Just set up error handling for testing
        testSetupErrorHandling();
    }
    
    // Tracking getters
    public boolean wasErrorMessageHandled() {
        return errorMessageHandled;
    }
    
    public String getLastErrorTitle() {
        return lastErrorTitle;
    }
    
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public boolean wasDialogStageClosed() {
        return dialogStageClosed;
    }
    
    public boolean wasErrorListenerTriggered() {
        return errorListenerTriggered;
    }
    
    // Reset tracking for test isolation
    public void resetTracking() {
        errorMessageHandled = false;
        lastErrorTitle = null;
        lastErrorMessage = null;
        dialogStageClosed = false;
        errorListenerTriggered = false;
    }
}