// src/main/java/org/frcpm/mvvm/async/MvvmAsyncHelper.java

package org.frcpm.mvvm.async;

import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import org.frcpm.async.TaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for async operations with MVVMFx.
 * Provides utilities for creating commands that execute asynchronous operations.
 */
public class MvvmAsyncHelper {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmAsyncHelper.class.getName());
    
    /**
     * Creates a command that executes an async operation.
     * 
     * @param <T> the result type
     * @param asyncOperation the async operation supplier
     * @param onSuccess the success callback
     * @param onFailure the failure callback
     * @param loadingProperty the loading property to update
     * @return a command that executes the async operation
     */
    public static <T> Command createAsyncCommand(
            Supplier<CompletableFuture<T>> asyncOperation,
            Consumer<T> onSuccess,
            Consumer<Throwable> onFailure,
            BooleanProperty loadingProperty) {
        
        return new AsyncCommand<>(asyncOperation, onSuccess, onFailure, loadingProperty);
    }
    
    /**
     * Creates a command that executes a simple async operation.
     * 
     * @param asyncOperation the async operation to execute
     * @return a command that executes the async operation
     */
    public static Command createSimpleAsyncCommand(Runnable asyncOperation) {
        return new SimpleAsyncCommand(asyncOperation);
    }
    
    /**
     * Executes an operation on the JavaFX application thread and returns a CompletableFuture.
     * 
     * @param <T> the result type
     * @param operation the operation to execute
     * @return a CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> runOnFxThread(Supplier<T> operation) {
        return TaskExecutor.executeOnFxThread(operation::get);
    }
    
    /**
     * Implementation of a command that executes an async operation.
     */
    private static class AsyncCommand<T> implements Command {
        
        private final Supplier<CompletableFuture<T>> asyncOperation;
        private final Consumer<T> onSuccess;
        private final Consumer<Throwable> onFailure;
        private final BooleanProperty loadingProperty;
        private final BooleanProperty executableProperty = new SimpleBooleanProperty(true);
        private final ReadOnlyBooleanWrapper notExecutableProperty = new ReadOnlyBooleanWrapper(false);
        private final ReadOnlyBooleanWrapper notRunningProperty = new ReadOnlyBooleanWrapper(true);
        private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        
        public AsyncCommand(
                Supplier<CompletableFuture<T>> asyncOperation,
                Consumer<T> onSuccess,
                Consumer<Throwable> onFailure,
                BooleanProperty loadingProperty) {
            
            this.asyncOperation = asyncOperation;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
            this.loadingProperty = loadingProperty;
            
            // When loading changes, update executable property
            if (loadingProperty != null) {
                loadingProperty.addListener((obs, oldVal, newVal) -> {
                    updateExecutableState();
                    notRunningProperty.set(!newVal);
                });
            }
            
            // Set up binding for notExecutableProperty
            executableProperty.addListener((obs, oldVal, newVal) -> {
                notExecutableProperty.set(!newVal);
            });
        }
        
        @Override
        public void execute() {
            if (!isExecutable()) {
                return;
            }
            
            if (loadingProperty != null) {
                loadingProperty.set(true);
                notRunningProperty.set(false);
            }
            executableProperty.set(false);
            notExecutableProperty.set(true);
            
            try {
                CompletableFuture<T> future = asyncOperation.get();
                future.thenAccept(result -> {
                    Platform.runLater(() -> {
                        try {
                            if (onSuccess != null) {
                                onSuccess.accept(result);
                            }
                        } finally {
                            if (loadingProperty != null) {
                                loadingProperty.set(false);
                                notRunningProperty.set(true);
                            }
                            updateExecutableState();
                        }
                    });
                }).exceptionally(error -> {
                    Platform.runLater(() -> {
                        try {
                            if (onFailure != null) {
                                onFailure.accept(error);
                            }
                        } finally {
                            if (loadingProperty != null) {
                                loadingProperty.set(false);
                                notRunningProperty.set(true);
                            }
                            updateExecutableState();
                        }
                    });
                    return null;
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error starting async operation", e);
                Platform.runLater(() -> {
                    try {
                        if (onFailure != null) {
                            onFailure.accept(e);
                        }
                    } finally {
                        if (loadingProperty != null) {
                            loadingProperty.set(false);
                            notRunningProperty.set(true);
                        }
                        updateExecutableState();
                    }
                });
            }
        }
        
        private void updateExecutableState() {
            boolean loading = loadingProperty != null && loadingProperty.get();
            executableProperty.set(!loading);
            notExecutableProperty.set(loading);
        }
        
        @Override
        public BooleanProperty executableProperty() {
            return executableProperty;
        }
        
        @Override
        public boolean isExecutable() {
            return executableProperty.get();
        }
        
        @Override
        public BooleanProperty runningProperty() {
            return loadingProperty != null ? loadingProperty : new SimpleBooleanProperty(false);
        }
        
        @Override
        public boolean isRunning() {
            return loadingProperty != null && loadingProperty.get();
        }

        @Override
        public boolean isNotExecutable() {
            return !isExecutable();
        }

        @Override
        public ReadOnlyBooleanProperty notExecutableProperty() {
            return notExecutableProperty.getReadOnlyProperty();
        }

        @Override
        public boolean isNotRunning() {
            return !isRunning();
        }

        @Override
        public ReadOnlyBooleanProperty notRunningProperty() {
            return notRunningProperty.getReadOnlyProperty();
        }

        @Override
        public double getProgress() {
            return progressProperty.get();
        }

        @Override
        public ReadOnlyDoubleProperty progressProperty() {
            return progressProperty;
        }
    }
    
    /**
     * Simple implementation of an async command.
     */
    private static class SimpleAsyncCommand implements Command {
        
        private final Runnable asyncOperation;
        private final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
        private final BooleanProperty executableProperty = new SimpleBooleanProperty(true);
        private final ReadOnlyBooleanWrapper notRunningProperty = new ReadOnlyBooleanWrapper(true);
        private final ReadOnlyBooleanWrapper notExecutableProperty = new ReadOnlyBooleanWrapper(false);
        private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        
        public SimpleAsyncCommand(Runnable asyncOperation) {
            this.asyncOperation = asyncOperation;
            
            // When running changes, update executable property and notRunningProperty
            runningProperty.addListener((obs, oldVal, newVal) -> {
                executableProperty.set(!newVal);
                notExecutableProperty.set(newVal);
                notRunningProperty.set(!newVal);
            });
        }
        
        @Override
        public void execute() {
            if (!isExecutable()) {
                return;
            }
            
            runningProperty.set(true);
            notRunningProperty.set(false);
            
            TaskExecutor.executeAsync(
                "SimpleAsyncCommand",
                () -> {
                    asyncOperation.run();
                    return null;
                },
                result -> {
                    runningProperty.set(false);
                    notRunningProperty.set(true);
                },
                error -> {
                    LOGGER.log(Level.SEVERE, "Error in async operation", error);
                    runningProperty.set(false);
                    notRunningProperty.set(true);
                }
            );
        }
        
        @Override
        public BooleanProperty executableProperty() {
            return executableProperty;
        }
        
        @Override
        public boolean isExecutable() {
            return executableProperty.get();
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
            return !isExecutable();
        }

        @Override
        public ReadOnlyBooleanProperty notExecutableProperty() {
            return notExecutableProperty.getReadOnlyProperty();
        }

        @Override
        public boolean isNotRunning() {
            return !isRunning();
        }

        @Override
        public ReadOnlyBooleanProperty notRunningProperty() {
            return notRunningProperty.getReadOnlyProperty();
        }

        @Override
        public double getProgress() {
            return progressProperty.get();
        }

        @Override
        public ReadOnlyDoubleProperty progressProperty() {
            return progressProperty;
        }
    }
}