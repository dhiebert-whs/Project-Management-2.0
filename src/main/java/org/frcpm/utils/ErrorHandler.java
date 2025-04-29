// src/main/java/org/frcpm/utils/ErrorHandler.java

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
 * Error handling utilities for JavaFX applications.
 */
public class ErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Shows an error alert with details.
     * 
     * @param title the title of the alert
     * @param message the error message
     * @param exception the exception (optional, can be null)
     */
    public static void showError(String title, String message, Throwable exception) {
        if (Platform.isFxApplicationThread()) {
            showErrorInternal(title, message, exception);
        } else {
            Platform.runLater(() -> showErrorInternal(title, message, exception));
        }
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
     * Internal method to show an error alert.
     * Must be called on the JavaFX application thread.
     * 
     * @param title the title of the alert
     * @param message the error message
     * @param exception the exception (optional, can be null)
     */
    private static void showErrorInternal(String title, String message, Throwable exception) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            
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
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);
                
                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(textArea, 0, 0);
                
                alert.getDialogPane().setExpandableContent(expContent);
            }
            
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing error alert", e);
            LOGGER.log(Level.INFO, "Original error was: " + message);
        }
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title the title of the dialog
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            return showConfirmationInternal(title, message);
        } else {
            // Use a wrapper to get the result from another thread
            final boolean[] result = new boolean[1];
            
            try {
                // Use CountDownLatch to wait for the result
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                
                Platform.runLater(() -> {
                    result[0] = showConfirmationInternal(title, message);
                    latch.countDown();
                });
                
                // Wait for the dialog to complete
                latch.await();
                
                return result[0];
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Thread interrupted while showing confirmation dialog", e);
                return false;
            }
        }
    }
    
    /**
     * Internal method to show a confirmation dialog.
     * Must be called on the JavaFX application thread.
     * 
     * @param title the title of the dialog
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private static boolean showConfirmationInternal(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(title);
            alert.setContentText(message);
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing confirmation dialog", e);
            return false;
        }
    }
    
    /**
     * Shows an information dialog.
     * 
     * @param title the title of the dialog
     * @param message the message
     */
    public static void showInfo(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            showInfoInternal(title, message);
        } else {
            Platform.runLater(() -> showInfoInternal(title, message));
        }
    }
    
    /**
     * Internal method to show an information dialog.
     * Must be called on the JavaFX application thread.
     * 
     * @param title the title of the dialog
     * @param message the message
     */
    private static void showInfoInternal(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing info dialog", e);
        }
    }
}