// src/main/java/org/frcpm/utils/ErrorHandler.java

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
 * Utility class for handling errors in a consistent manner across the application.
 */
public class ErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    
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
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called from JavaFX Application Thread");
        }
        
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