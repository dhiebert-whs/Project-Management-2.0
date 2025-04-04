package org.frcpm.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Base class for all ViewModels in the MVVM architecture.
 * Provides common functionality for error handling and dirty state tracking.
 */
public abstract class BaseViewModel {
    
    // Common properties for all ViewModels
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    
    /**
     * Gets the error message property.
     * 
     * @return the error message property
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
    
    /**
     * Gets the error message.
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
        errorMessage.set("");
    }
    
    /**
     * Gets the dirty property.
     * Dirty indicates that the ViewModel has unsaved changes.
     * 
     * @return the dirty property
     */
    public BooleanProperty dirtyProperty() {
        return dirty;
    }
    
    /**
     * Checks if the ViewModel has unsaved changes.
     * 
     * @return true if the ViewModel has unsaved changes, false otherwise
     */
    public boolean isDirty() {
        return dirty.get();
    }
    
    /**
     * Sets the dirty flag.
     * 
     * @param value the new dirty value
     */
    protected void setDirty(boolean value) {
        dirty.set(value);
    }
}