package org.frcpm.controllers;

import javafx.scene.control.Alert;
import org.frcpm.models.Task;

/**
 * A specialized subclass of TaskController for testing purposes.
 * Overrides methods that access UI components to prevent NullPointerExceptions in tests.
 */
public class TestableTaskController extends TaskController {
    
    private boolean errorMessageHandled = false;
    private String lastErrorTitle;
    private String lastErrorMessage;
    
    /**
     * Override showErrorAlert to capture error messages for verification in tests
     */
    @Override
    protected void showErrorAlert(String title, String message) {
        lastErrorTitle = title;
        lastErrorMessage = message;
        errorMessageHandled = true;
        
        // Don't call super as it might interact with UI or use mocks
    }
    
    /**
     * Override showInfoAlert to avoid UI interactions in tests
     */
    @Override
    protected void showInfoAlert(String title, String message) {
        // Don't call super as it might interact with UI
    }
    
    /**
     * Check if an error message was handled
     * @return true if an error message was handled, false otherwise
     */
    public boolean wasErrorMessageHandled() {
        return errorMessageHandled;
    }
    
    /**
     * Get the last error title
     * @return the last error title
     */
    public String getLastErrorTitle() {
        return lastErrorTitle;
    }
    
    /**
     * Get the last error message
     * @return the last error message
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    /**
     * Reset the error message tracking
     */
    public void resetErrorMessageTracking() {
        errorMessageHandled = false;
        lastErrorTitle = null;
        lastErrorMessage = null;
    }
    
    /**
     * Override closeDialog to prevent UI access in tests
     */
    @Override
    protected void closeDialog() {
        // Do nothing in tests to avoid accessing scene/window
    }
    
    /**
     * Manually trigger the error message handling logic to test it
     */
    public void triggerErrorMessageHandling(String errorMessage) {
        showErrorAlert("Validation Error", errorMessage);
    }
}