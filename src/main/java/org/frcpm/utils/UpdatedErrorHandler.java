// src/main/java/org/frcpm/utils/UpdatedErrorHandler.java

package org.frcpm.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
 * Enhanced utility class for handling errors in a consistent manner across the application.
 * This version works better with AfterburnerFX and JavaFX threading model.
 */
public class UpdatedErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(UpdatedErrorHandler.class.getName());
    
    /**
     * Shows an error alert with the specified title and message.
     * 
     * @param title the alert title
     * @param message the alert message
     */
    public static void showError(String title, String message) {
        LOGGER.log(Level.SEVERE, "Error: {0} - {1}", new Object[]{title, message});
        
        if (Platform.isFxApplicationThread()) {
            showErrorAlert(title, message, null);
        } else {
            Platform.runLater(() -> showErrorAlert(title, message, null));
        }
    }
    
    /**
     * Shows an error alert with the specified title, message, and exception details.
     * 
     * @param title the alert title
     * @param message the alert message
     * @param exception the exception that caused the error
     */
    public static void showError(String title, String message, Throwable exception) {
        LOGGER.log(Level.SEVERE, "Error: " + message, exception);
        
        if (Platform.isFxApplicationThread()) {
            showErrorAlert(title, message, exception);
        } else {
            Platform.runLater(() -> showErrorAlert(title, message, exception));
        }
    }
    
    /**
     * Shows a confirmation dialog with the specified title and message.
     * 
     * @param title the dialog title
     * @param message the dialog message
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        // For thread safety, we need to handle non-FX thread calls
        if (!Platform.isFxApplicationThread()) {
            // Create a wrapper to hold the result
            final boolean[] result = new boolean[1];
            
            // Run on FX thread and wait
            Platform.runLater(() -> {
                result[0] = showConfirmationDialogOnFxThread(title, message);
            });
            
            // Wait for FX thread to complete (this is a simplified approach)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return result[0];
        }
        
        return showConfirmationDialogOnFxThread(title, message);
    }
    
    /**
     * Helper method to show a confirmation dialog on the JavaFX thread.
     * 
     * @param title the dialog title
     * @param message the dialog message
     * @return true if the user clicked OK, false otherwise
     */
    private static boolean showConfirmationDialogOnFxThread(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows an information alert with the specified title and message.
     * 
     * @param title the alert title
     * @param message the alert message
     */
    public static void showInfo(String title, String message) {
        LOGGER.log(Level.INFO, "Info: {0} - {1}", new Object[]{title, message});
        
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }
    
    /**
     * Shows a warning alert with the specified title and message.
     * 
     * @param title the alert title
     * @param message the alert message
     */
    public static void showWarning(String title, String message) {
        LOGGER.log(Level.WARNING, "Warning: {0} - {1}", new Object[]{title, message});
        
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }
    
    /**
     * Helper method to show an error alert with optional exception details.
     * 
     * @param title the alert title
     * @param message the alert message
     * @param exception the exception that caused the error, or null if none
     */
    private static void showErrorAlert(String title, String message, Throwable exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (exception != null) {
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
    }
}