// src/main/java/org/frcpm/async/DatabaseTask.java
package org.frcpm.async;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A task for database operations.
 * Provides progress tracking, cancellation support, and error handling.
 * 
 * @param <T> the result type
 */
public class DatabaseTask<T> {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseTask.class.getName());
    
    private final Callable<T> task;
    private final ExecutorService executor;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
    private final DoubleProperty progressProperty = new SimpleDoubleProperty(0);
    private final BooleanProperty successProperty = new SimpleBooleanProperty(false);
    private Future<?> future;
    private Consumer<T> onSuccess;
    private Consumer<Throwable> onError;
    private Runnable onFinally;
    
    /**
     * Creates a new database task.
     * 
     * @param task the task to execute
     * @param executor the executor to use
     */
    public DatabaseTask(Callable<T> task, ExecutorService executor) {
        this.task = task;
        this.executor = executor;
    }
    
    /**
     * Sets the success callback.
     * This will be called on the JavaFX application thread.
     * 
     * @param onSuccess the success callback
     * @return this task for method chaining
     */
    public DatabaseTask<T> onSuccess(Consumer<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }
    
    /**
     * Sets the error callback.
     * This will be called on the JavaFX application thread.
     * 
     * @param onError the error callback
     * @return this task for method chaining
     */
    public DatabaseTask<T> onError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }
    
    /**
     * Sets a callback to be executed after the task completes, regardless of success or failure.
     * This will be called on the JavaFX application thread.
     * 
     * @param action the callback
     * @return this task for method chaining
     */
    public DatabaseTask<T> onFinally(Runnable action) {
        this.onFinally = action;
        return this;
    }
    
    /**
     * Updates the progress of the task.
     * 
     * @param progress the progress value between 0.0 and 1.0
     */
    public void updateProgress(double progress) {
        if (progress < 0.0) progress = 0.0;
        if (progress > 1.0) progress = 1.0;
        
        final double finalProgress = progress;
        Platform.runLater(() -> progressProperty.set(finalProgress));
    }
    
    /**
     * Gets the progress property.
     * 
     * @return the progress property
     */
    public DoubleProperty progressProperty() {
        return progressProperty;
    }
    
    /**
     * Gets the running property.
     * 
     * @return the running property
     */
    public BooleanProperty runningProperty() {
        return runningProperty;
    }
    
    /**
     * Gets the success property.
     * 
     * @return the success property
     */
    public BooleanProperty successProperty() {
        return successProperty;
    }
    
    /**
     * Checks if the task is running.
     * 
     * @return true if the task is running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Checks if the task was successful.
     * 
     * @return true if the task was successful, false otherwise
     */
    public boolean isSuccess() {
        return successProperty.get();
    }
    
    /**
     * Executes the task and returns immediately.
     */
    public void execute() {
        if (running.getAndSet(true)) {
            // Already running
            return;
        }
        
        Platform.runLater(() -> runningProperty.set(true));
        
        future = executor.submit(() -> {
            try {
                // Reset progress
                updateProgress(0);
                
                // Check if cancelled before starting
                if (cancelled.get()) {
                    completeTask(false);
                    return;
                }
                
                // Execute the task
                T result = task.call();
                
                // Check if cancelled after execution
                if (cancelled.get()) {
                    completeTask(false);
                    return;
                }
                
                // Update progress to indicate completion
                updateProgress(1.0);
                
                // Call success callback on JavaFX thread
                if (onSuccess != null) {
                    Platform.runLater(() -> {
                        try {
                            onSuccess.accept(result);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error in success callback", e);
                        }
                    });
                }
                
                completeTask(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error executing database task", e);
                
                // Call error callback on JavaFX thread
                if (onError != null) {
                    Platform.runLater(() -> {
                        try {
                            onError.accept(e);
                        } catch (Exception callbackError) {
                            LOGGER.log(Level.SEVERE, "Error in error callback", callbackError);
                        }
                    });
                }
                
                completeTask(false);
            }
        });
    }
    
    /**
     * Executes the task and waits for completion.
     * 
     * @return true if the task completed successfully, false otherwise
     */
    public boolean executeAndWait() {
        final boolean[] success = new boolean[1];
        final boolean[] done = new boolean[1];
        
        // Set up callbacks
        onSuccess(result -> {
            success[0] = true;
            done[0] = true;
        });
        
        onError(error -> {
            success[0] = false;
            done[0] = true;
        });
        
        // Execute the task
        execute();
        
        // Wait for completion
        while (!done[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return success[0];
    }
    
    /**
     * Cancels the task if it is running.
     */
    public void cancel() {
        cancelled.set(true);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        
        completeTask(false);
    }
    
    /**
     * Completes the task, updating status and calling the finally callback.
     * 
     * @param success whether the task completed successfully
     */
    private void completeTask(boolean success) {
        // Update state
        running.set(false);
        
        // Update properties on JavaFX thread
        Platform.runLater(() -> {
            runningProperty.set(false);
            successProperty.set(success);
            
            // Call finally callback
            if (onFinally != null) {
                try {
                    onFinally.run();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in finally callback", e);
                }
            }
        });
    }
}