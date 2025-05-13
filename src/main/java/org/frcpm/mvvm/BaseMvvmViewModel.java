// src/main/java/org/frcpm/mvvm/BaseMvvmViewModel.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.function.Supplier;

/**
 * Base ViewModel class for the MVVMFx framework.
 * Provides common functionality for all ViewModels in the application.
 */
public abstract class BaseMvvmViewModel implements ViewModel {
    
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
        // Return null if the message is empty, to standardize behavior across all ViewModels
        return (message == null || message.isEmpty()) ? null : message;
    }
    
    /**
     * Sets the error message.
     * 
     * @param message the error message
     */
    public void setErrorMessage(String message) {
        errorMessage.set(message);
    }
    
    /**
     * Clears the error message.
     */
    public void clearErrorMessage() {
        // Set to empty string internally, but getErrorMessage() will return null
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
    public void setDirty(boolean value) {
        dirty.set(value);
    }
    
    /**
     * Adds a property listener to be tracked for cleanup.
     * 
     * @param listener the listener to add
     */
    protected void trackPropertyListener(Runnable listener) {
        propertyListenersList.add(listener);
    }
    
    /**
     * Creates a property change handler that sets dirty flag.
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
     * Helper to create a valid-only command.
     * 
     * @param action the action to execute
     * @param validCheck the validity check supplier
     * @return a command that checks validity before execution
     */
    protected Command createValidOnlyCommand(Runnable action, Supplier<Boolean> validCheck) {
        return new CommandWrapper(action, validCheck);
    }
    
    /**
     * Helper to create a save command (valid + dirty).
     * 
     * @param action the action to execute
     * @param validCheck the validity check supplier
     * @return a command that checks validity and dirty state before execution
     */
    protected Command createValidAndDirtyCommand(Runnable action, Supplier<Boolean> validCheck) {
        return new CommandWrapper(action, () -> validCheck.get() && isDirty());
    }
    
    /**
     * Simple wrapper for MVVMFx Command that integrates with our existing command pattern.
     */
    private static class CommandWrapper implements Command {
        private final Runnable action;
        private final Supplier<Boolean> executable;
        private final BooleanProperty executableProperty = new SimpleBooleanProperty(true);
        private final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
        
        public CommandWrapper(Runnable action, Supplier<Boolean> executable) {
            this.action = action;
            this.executable = executable;
        }
        
        @Override
        public void execute() {
            if (isExecutable()) {
                runningProperty.set(true);
                try {
                    action.run();
                } finally {
                    runningProperty.set(false);
                }
            }
        }
        
        @Override
        public BooleanProperty executableProperty() {
            // Update the property value before returning
            executableProperty.set(isExecutable());
            return executableProperty;
        }
        
        @Override
        public boolean isExecutable() {
            return !runningProperty.get() && executable.get();
        }
        
        @Override
        public BooleanProperty runningProperty() {
            return runningProperty;
        }
        
        @Override
        public boolean isRunning() {
            return runningProperty.get();
        }

        @Override
        public boolean isNotExecutable() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isNotExecutable'");
        }

        @Override
        public ReadOnlyBooleanProperty notExecutableProperty() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'notExecutableProperty'");
        }

        @Override
        public boolean isNotRunning() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isNotRunning'");
        }

        @Override
        public ReadOnlyBooleanProperty notRunningProperty() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'notRunningProperty'");
        }

        @Override
        public double getProgress() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getProgress'");
        }

        @Override
        public ReadOnlyDoubleProperty progressProperty() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'progressProperty'");
        }
    }
    
    /**
     * Cleanup method to be called when the ViewModel is no longer needed.
     * This is called by the MVVMFx framework when the View is unloaded.
     */
    public void dispose() {
        // Clear all tracked property listeners
        propertyListenersList.clear();
    }
}