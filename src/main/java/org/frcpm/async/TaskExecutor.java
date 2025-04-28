package org.frcpm.async;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for executing tasks asynchronously.
 * This provides a task-based threading model for improved UI responsiveness.
 */
public class TaskExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(TaskExecutor.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    
    /**
     * Executes a task asynchronously.
     * 
     * @param <T> the result type
     * @param taskName the name of the task (for logging)
     * @param work the work to execute
     * @param onSuccess the callback to run on success (on JavaFX thread)
     * @param onFailure the callback to run on failure (on JavaFX thread)
     * @return a CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> executeAsync(String taskName, Callable<T> work, 
                                                    Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        LOGGER.fine("Executing task: " + taskName);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
            
            @Override
            protected void succeeded() {
                T result = getValue();
                
                Platform.runLater(() -> {
                    try {
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                        
                        future.complete(result);
                        LOGGER.fine("Task completed successfully: " + taskName);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error in task success handler: " + taskName, e);
                        future.completeExceptionally(e);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                
                Platform.runLater(() -> {
                    try {
                        if (onFailure != null) {
                            onFailure.accept(exception);
                        }
                        
                        future.completeExceptionally(exception);
                        LOGGER.log(Level.WARNING, "Task failed: " + taskName, exception);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error in task failure handler: " + taskName, e);
                        future.completeExceptionally(e);
                    }
                });
            }
        };
        
        EXECUTOR.submit(task);
        
        return future;
    }
    
    /**
     * Executes a task asynchronously with progress reporting.
     * 
     * @param <T> the result type
     * @param progressTask the task to execute
     * @param onSuccess the callback to run on success (on JavaFX thread)
     * @param onFailure the callback to run on failure (on JavaFX thread)
     * @return a CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> executeAsync(Task<T> progressTask, 
                                                    Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        LOGGER.fine("Executing task with progress reporting");
        
        CompletableFuture<T> future = new CompletableFuture<>();
        
        progressTask.setOnSucceeded(event -> {
            T result = progressTask.getValue();
            
            Platform.runLater(() -> {
                try {
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                    
                    future.complete(result);
                    LOGGER.fine("Task completed successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in task success handler", e);
                    future.completeExceptionally(e);
                }
            });
        });
        
        progressTask.setOnFailed(event -> {
            Throwable exception = progressTask.getException();
            
            Platform.runLater(() -> {
                try {
                    if (onFailure != null) {
                        onFailure.accept(exception);
                    }
                    
                    future.completeExceptionally(exception);
                    LOGGER.log(Level.WARNING, "Task failed", exception);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in task failure handler", e);
                    future.completeExceptionally(e);
                }
            });
        });
        
        EXECUTOR.submit(progressTask);
        
        return future;
    }
    
    /**
     * Executes a task on the JavaFX application thread.
     * 
     * @param <T> the result type
     * @param work the work to execute
     * @return a CompletableFuture that will be completed with the result
     */
    public static <T> CompletableFuture<T> executeOnFxThread(Callable<T> work) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            try {
                T result = work.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Shuts down the executor service.
     * This should be called when the application is shutting down.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down TaskExecutor");
        EXECUTOR.shutdown();
    }
}