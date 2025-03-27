// src/main/java/org/frcpm/viewmodels/BaseViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Base class for all ViewModels.
 * Provides common functionality shared by all ViewModels.
 */
public abstract class BaseViewModel {
    
    // Common properties
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty();
    
    /**
     * Gets the property indicating if the ViewModel has unsaved changes.
     * 
     * @return the dirty property
     */
    public BooleanProperty dirtyProperty() {
        return dirty;
    }
    
    /**
     * Gets whether the ViewModel has unsaved changes.
     * 
     * @return true if there are unsaved changes, false otherwise
     */
    public boolean isDirty() {
        return dirty.get();
    }
    
    /**
     * Sets the dirty state of the ViewModel.
     * 
     * @param isDirty whether the ViewModel has unsaved changes
     */
    protected void setDirty(boolean isDirty) {
        dirty.set(isDirty);
    }
    
    /**
     * Gets the error message property.
     * 
     * @return the error message property
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
    
    /**
     * Gets the current error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage.get();
    }
    
    /**
     * Sets the error message.
     * 
     * @param message the error message
     */
    protected void setErrorMessage(String message) {
        errorMessage.set(message);
    }
    
    /**
     * Clears the error message.
     */
    protected void clearErrorMessage() {
        errorMessage.set(null);
    }
}