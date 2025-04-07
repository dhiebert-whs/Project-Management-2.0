// src/main/java/org/frcpm/services/DialogService.java
package org.frcpm.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

/**
 * Interface for dialog services in the application.
 * This service abstracts dialog creation and display to improve testability
 * and maintain separation of concerns in the MVVM pattern.
 */
public interface DialogService {
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    void showErrorAlert(String title, String message);
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    void showInfoAlert(String title, String message);
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if the user confirmed, false otherwise
     */
    boolean showConfirmationAlert(String title, String message);
    
    /**
     * Shows a file chooser dialog for opening files.
     * 
     * @param title the title of the dialog
     * @param owner the owner window
     * @param extensionFilters the extension filters to use
     * @return the selected file, or null if no file was selected
     */
    File showOpenFileDialog(String title, Window owner, FileChooser.ExtensionFilter... extensionFilters);
    
    /**
     * Shows a file chooser dialog for saving files.
     * 
     * @param title the title of the dialog
     * @param owner the owner window
     * @param extensionFilters the extension filters to use
     * @return the selected file, or null if no file was selected
     */
    File showSaveFileDialog(String title, Window owner, FileChooser.ExtensionFilter... extensionFilters);
    
    /**
     * Creates and shows a custom alert dialog.
     * 
     * @param alertType the type of alert
     * @param title the title
     * @param headerText the header text
     * @param contentText the content text
     * @return the optional result from the dialog
     */
    Optional<ButtonType> showCustomAlert(Alert.AlertType alertType, String title, String headerText, String contentText);
}