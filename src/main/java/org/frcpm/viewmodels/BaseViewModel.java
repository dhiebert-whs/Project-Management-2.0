package org.frcpm.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;

/**
 * Base class for all ViewModels in the MVVM architecture.
 * Provides common functionality for error handling and dirty state tracking.
 */
public abstract class BaseViewModel {

    // Common properties for all ViewModels
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    // List to store property listeners for cleanup
    private final ObservableList<Runnable> propertyListenersList = FXCollections.observableArrayList();

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
     * @return the error message or null if no error message is set
     */
    public String getErrorMessage() {
        String message = errorMessage.get();
        return message == null ? "" : message;
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

    /**
     * New feature: Adds a property listener to be tracked for cleanup.
     * 
     * @param listener the listener to add
     */
    protected void trackPropertyListener(Runnable listener) {
        propertyListenersList.add(listener);
    }

    /**
     * New feature: Creates a property change handler that sets dirty flag.
     * 
     * @param runAfter optional action to run after setting dirty flag
     * @return a runnable that can be used as a change listener
     */
    protected Runnable createDirtyFlagHandler(Runnable runAfter) {
        return () -> {
            setDirty(true);
            if (runAfter != null) {
                runAfter.run();
            }
        };
    }

    /**
     * New feature: Helper to create a valid-only command.
     * 
     * @param action     the action to execute
     * @param validCheck the validity check supplier
     * @return a command that checks validity before execution
     */
    protected Command createValidOnlyCommand(Runnable action, java.util.function.Supplier<Boolean> validCheck) {
        return new Command(action, validCheck);
    }

    /**
     * New feature: Helper to create a save command (valid + dirty).
     * 
     * @param action     the action to execute
     * @param validCheck the validity check supplier
     * @return a command that checks validity and dirty state before execution
     */
    protected Command createValidAndDirtyCommand(Runnable action, java.util.function.Supplier<Boolean> validCheck) {
        return new Command(action, () -> validCheck.get() && isDirty());
    }

    /**
     * New feature: Cleanup method to be called when the ViewModel is no longer
     * needed.
     */
    public void cleanupResources() {
        // Base implementation just clears tracked listeners
        propertyListenersList.clear();
    }

}