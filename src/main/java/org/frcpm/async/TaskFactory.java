// src/main/java/org/frcpm/async/TaskFactory.java
package org.frcpm.async;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating task objects for asynchronous operations.
 * Provides convenience methods for common task types.
 */
public class TaskFactory {
    
    private static final Logger LOGGER = Logger.getLogger(TaskFactory.class.getName());
    
    /**
     * Creates a database task for executing a database operation.
     * 
     * @param <T> the result type
     * @param task the task to execute
     * @return the database task
     */
    public static <T> DatabaseTask<T> createDatabaseTask(Callable<T> task) {
        return TaskExecutor.createDatabaseTask(task);
    }
    
    /**
     * Creates a task for executing a database operation with callbacks.
     * 
     * @param <T> the result type
     * @param task the task to execute
     * @param onSuccess the success callback
     * @param onError the error callback
     * @return the database task
     */
    public static <T> DatabaseTask<T> createDatabaseTask(
            Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        return createDatabaseTask(task)
                .onSuccess(onSuccess)
                .onError(onError);
    }
    
    /**
     * Creates a JavaFX task for executing a background operation.
     * 
     * @param <T> the result type
     * @param task the task to execute
     * @return the JavaFX task
     */
    public static <T> Task<T> createJavaFxTask(Callable<T> task) {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };
    }
    
    /**
     * Creates a JavaFX service for executing a repeatable background operation.
     * 
     * @param <T> the result type
     * @param taskFactory the factory for creating tasks
     * @return the JavaFX service
     */
    public static <T> Service<T> createJavaFxService(Supplier<Task<T>> taskFactory) {
        return new Service<T>() {
            @Override
            protected Task<T> createTask() {
                return taskFactory.get();
            }
        };
    }
    
    /**
     * Creates a database report generation task.
     * 
     * @param <T> the report type
     * @param reportGenerator the report generator
     * @param onComplete the completion callback
     * @param onError the error callback
     */
    public static <T> void createReportTask(
            Supplier<T> reportGenerator, Consumer<T> onComplete, Consumer<Throwable> onError) {
        
        // Create a database task for generating the report
        DatabaseTask<T> task = createDatabaseTask(() -> reportGenerator.get());
        
        // Set up callbacks
        task.onSuccess(report -> {
            Platform.runLater(() -> {
                try {
                    onComplete.accept(report);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in report completion callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in report error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a task that loads data from a database.
     * 
     * @param <T> the data type
     * @param dataLoader the data loader
     * @param onSuccess the success callback
     * @param onError the error callback
     */
    public static <T> void createDataLoadTask(
            Callable<T> dataLoader, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        
        // Create a database task for loading the data
        DatabaseTask<T> task = createDatabaseTask(dataLoader);
        
        // Set up callbacks
        task.onSuccess(data -> {
            Platform.runLater(() -> {
                try {
                    onSuccess.accept(data);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in data load success callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in data load error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a task that saves data to a database.
     * 
     * @param <T> the data type
     * @param dataSaver the data saver
     * @param onSuccess the success callback
     * @param onError the error callback
     */
    public static <T> void createDataSaveTask(
            Callable<T> dataSaver, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        
        // Create a database task for saving the data
        DatabaseTask<T> task = createDatabaseTask(dataSaver);
        
        // Set up callbacks
        task.onSuccess(result -> {
            Platform.runLater(() -> {
                try {
                    onSuccess.accept(result);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in data save success callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in data save error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a task that imports data from a file.
     * 
     * @param <T> the data type
     * @param fileImporter the file importer
     * @param onSuccess the success callback
     * @param onError the error callback
     */
    public static <T> void createFileImportTask(
            Callable<T> fileImporter, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        
        // Create a database task for importing the file
        DatabaseTask<T> task = createDatabaseTask(fileImporter);
        
        // Set up callbacks
        task.onSuccess(data -> {
            Platform.runLater(() -> {
                try {
                    onSuccess.accept(data);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in file import success callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in file import error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a task that exports data to a file.
     * 
     * @param <T> the result type
     * @param fileExporter the file exporter
     * @param onSuccess the success callback
     * @param onError the error callback
     */
    public static <T> void createFileExportTask(
            Callable<T> fileExporter, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        
        // Create a database task for exporting the file
        DatabaseTask<T> task = createDatabaseTask(fileExporter);
        
        // Set up callbacks
        task.onSuccess(result -> {
            Platform.runLater(() -> {
                try {
                    onSuccess.accept(result);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in file export success callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in file export error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a task for batch processing.
     * 
     * @param <T> the result type
     * @param batchProcessor the batch processor
     * @param progressCallback the progress callback
     * @param onSuccess the success callback
     * @param onError the error callback
     */
    public static <T> void createBatchProcessingTask(
            Callable<T> batchProcessor, 
            Consumer<Double> progressCallback,
            Consumer<T> onSuccess, 
            Consumer<Throwable> onError) {
        
        // Create a database task for batch processing
        DatabaseTask<T> task = createDatabaseTask(batchProcessor);
        
        // Set up progress callback
        if (progressCallback != null) {
            task.progressProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> progressCallback.accept(newVal.doubleValue()));
            });
        }
        
        // Set up callbacks
        task.onSuccess(result -> {
            Platform.runLater(() -> {
                try {
                    onSuccess.accept(result);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in batch processing success callback", e);
                }
            });
        });
        
        task.onError(error -> {
            Platform.runLater(() -> {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in batch processing error callback", e);
                }
            });
        });
        
        // Execute the task
        task.execute();
    }
    
    /**
     * Creates a periodic background task.
     * 
     * @param <T> the result type
     * @param task the task to execute
     * @param initialDelay the initial delay in milliseconds
     * @param period the period in milliseconds
     * @param onResult the result callback
     * @param onError the error callback
     * @return the scheduled future for the task
     */
    public static <T> java.util.concurrent.ScheduledFuture<?> createPeriodicTask(
            Callable<T> task,
            long initialDelay,
            long period,
            Consumer<T> onResult,
            Consumer<Throwable> onError) {
        
        return TaskExecutor.scheduleRepeatingTask(() -> {
            try {
                T result = task.call();
                if (onResult != null) {
                    Platform.runLater(() -> onResult.accept(result));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in periodic task", e);
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                }
            }
        }, initialDelay, period, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}