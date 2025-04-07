// src/main/java/org/frcpm/services/impl/JavaFXDialogService.java
package org.frcpm.services.impl;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.frcpm.services.DialogService;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX implementation of the DialogService interface.
 */
public class JavaFXDialogService implements DialogService {
    
    private static final Logger LOGGER = Logger.getLogger(JavaFXDialogService.class.getName());
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    @Override
    public void showErrorAlert(String title, String message) {
        try {
            Alert alert = createAlert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Error - {0} - {1}", new Object[]{title, message});
        }
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    @Override
    public void showInfoAlert(String title, String message) {
        try {
            Alert alert = createAlert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Info - {0} - {1}", new Object[]{title, message});
        }
    }
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if the user confirmed, false otherwise
     */
    @Override
    public boolean showConfirmationAlert(String title, String message) {
        try {
            Alert alert = createAlert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(title);
            alert.setContentText(message);
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Confirmation - {0} - {1}", new Object[]{title, message});
            return false;
        }
    }
    
    /**
     * Shows a file chooser dialog for opening files.
     * 
     * @param title the title of the dialog
     * @param owner the owner window
     * @param extensionFilters the extension filters to use
     * @return the selected file, or null if no file was selected
     */
    @Override
    public File showOpenFileDialog(String title, Window owner, FileChooser.ExtensionFilter... extensionFilters) {
        try {
            FileChooser fileChooser = createFileChooser();
            fileChooser.setTitle(title);
            for (FileChooser.ExtensionFilter filter : extensionFilters) {
                fileChooser.getExtensionFilters().add(filter);
            }
            return fileChooser.showOpenDialog(owner);
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "FileChooser would show: Open - {0}", title);
            return null;
        }
    }
    
    /**
     * Shows a file chooser dialog for saving files.
     * 
     * @param title the title of the dialog
     * @param owner the owner window
     * @param extensionFilters the extension filters to use
     * @return the selected file, or null if no file was selected
     */
    @Override
    public File showSaveFileDialog(String title, Window owner, FileChooser.ExtensionFilter... extensionFilters) {
        try {
            FileChooser fileChooser = createFileChooser();
            fileChooser.setTitle(title);
            for (FileChooser.ExtensionFilter filter : extensionFilters) {
                fileChooser.getExtensionFilters().add(filter);
            }
            return fileChooser.showSaveDialog(owner);
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "FileChooser would show: Save - {0}", title);
            return null;
        }
    }
    
    /**
     * Creates and shows a custom alert dialog.
     * 
     * @param alertType the type of alert
     * @param title the title
     * @param headerText the header text
     * @param contentText the content text
     * @return the optional result from the dialog
     */
    @Override
    public Optional<ButtonType> showCustomAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        try {
            Alert alert = createAlert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            return alert.showAndWait();
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Custom - {0} - {1} - {2}", new Object[]{title, headerText, contentText});
            return Optional.empty();
        }
    }
    
    /**
     * Creates a new Alert dialog.
     * Extracted for testability.
     * 
     * @param alertType the alert type
     * @return the created alert
     */
    protected Alert createAlert(Alert.AlertType alertType) {
        return new Alert(alertType);
    }
    
    /**
     * Creates a new FileChooser.
     * Extracted for testability.
     * 
     * @return the created file chooser
     */
    protected FileChooser createFileChooser() {
        return new FileChooser();
    }
}