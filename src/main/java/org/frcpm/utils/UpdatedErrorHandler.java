// src/main/java/org/frcpm/utils/UpdatedErrorHandler.java

package org.frcpm.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Improved error handling utilities for JavaFX applications.
 * Thread-safe implementation for AfterburnerFX integration.
 */
public class UpdatedErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(UpdatedErrorHandler.class.getName());
    
    /**
     * Shows an error alert with details.
     * 
     * @param title the title of the alert
     * @param message the error message
     * @param exception the exception (optional, can be null)
     */
    public static void showError(String title, String message, Throwable exception) {
        runInFxThread(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(title);
                alert.setContentText(message);
                
                // If there's an exception, add exception details
                if (exception != null) {
                    LOGGER.log(Level.SEVERE, message, exception);
                    
                    // Create expandable Exception section
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    exception.printStackTrace(pw);
                    String exceptionText = sw.toString();
                    
                    TextArea textArea = new TextArea(exceptionText);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    
                    textArea.setMaxWidth(Double.MAX_VALUE);
                    // src/main/java/org/frcpm/utils/UpdatedErrorHandler.java (continued)

                    GridPane expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);
                    expContent.add(textArea, 0, 0);
                    GridPane.setVgrow(textArea, Priority.ALWAYS);
                    GridPane.setHgrow(textArea, Priority.ALWAYS);
                    
                    alert.getDialogPane().setExpandableContent(expContent);
                }
                
                alert.showAndWait();
            } catch (Exception e) {
                // This can happen if trying to show an alert when FX thread is not available
                LOGGER.log(Level.SEVERE, "Error showing error alert: " + e.getMessage(), e);
                LOGGER.log(Level.INFO, "Original error was: " + message);
            }
        });
    }
    
    /**
     * Shows an error alert without exception details.
     * 
     * @param title the title of the alert
     * @param message the error message
     */
    public static void showError(String title, String message) {
        showError(title, message, null);
    }
    
    /**
     * Shows an information alert.
     * 
     * @param title the title of the alert
     * @param message the message
     */
    public static void showInfo(String title, String message) {
        runInFxThread(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(title);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing info alert", e);
            }
        });
    }
    
    /**
     * Shows a warning alert.
     * 
     * @param title the title of the alert
     * @param message the message
     */
    public static void showWarning(String title, String message) {
        runInFxThread(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(title);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing warning alert", e);
            }
        });
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title the title of the dialog
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        final boolean[] result = new boolean[1];
        
        runInFxThreadAndWait(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(title);
                alert.setContentText(message);
                
                Optional<ButtonType> response = alert.showAndWait();
                result[0] = response.isPresent() && response.get() == ButtonType.OK;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing confirmation dialog", e);
                result[0] = false;
            }
        });
        
        return result[0];
    }
    
    /**
     * Runs a task on the JavaFX application thread.
     * 
     * @param runnable the task to run
     */
    private static void runInFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
    
    /**
     * Runs a task on the JavaFX application thread and waits for it to complete.
     * 
     * @param runnable the task to run
     */
    private static void runInFxThreadAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            // Use a CountDownLatch to wait for the task to complete
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Thread interrupted while waiting for JavaFX thread", e);
            }
        }
    }
}