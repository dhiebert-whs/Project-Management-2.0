package org.frcpm.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.frcpm.utils.DatabaseMigrationUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the database migration dialog.
 */
public class DatabaseMigrationController {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationController.class.getName());
    
    @FXML
    private TextField sourceDbPathField;
    
    @FXML
    private Button browseButton;
    
    @FXML
    private Button migrateButton;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label progressLabel;
    
    @FXML
    private TextArea logTextArea;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private VBox errorBox;
    
    @FXML
    private ListView<String> errorListView;
    
    private Stage dialogStage;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Hide error box initially
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        
        // Initialize progress
        progressBar.setProgress(0);
        progressLabel.setText("Ready");
        
        // Set up event handlers
        browseButton.setOnAction(event -> handleBrowseSourceDb());
        migrateButton.setOnAction(event -> handleMigrate());
        closeButton.setOnAction(event -> dialogStage.close());
    }
    
    /**
     * Sets the dialog stage.
     * 
     * @param dialogStage the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        // Disable close button initially
        closeButton.setDisable(true);
    }
    
    /**
     * Handles browsing for the source database file.
     */
    private void handleBrowseSourceDb() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select SQLite Database");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQLite Database", "*.db", "*.sqlite", "*.sqlite3")
        );
        
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            sourceDbPathField.setText(file.getAbsolutePath());
            migrateButton.setDisable(false);
        }
    }
    
    /**
     * Handles the migration process.
     */
    private void handleMigrate() {
        String sourceDbPath = sourceDbPathField.getText();
        if (sourceDbPath == null || sourceDbPath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Source database path cannot be empty");
            return;
        }
        
        // Disable UI controls during migration
        sourceDbPathField.setDisable(true);
        browseButton.setDisable(true);
        migrateButton.setDisable(true);
        
        // Clear log and error list
        logTextArea.clear();
        errorListView.getItems().clear();
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        
        // Create and start migration task
        MigrationTask migrationTask = new MigrationTask(sourceDbPath);
        
        // Set up progress tracking
        progressBar.progressProperty().bind(migrationTask.progressProperty());
        
        // Set up task completion handling
        migrationTask.setOnSucceeded(event -> {
            Boolean success = migrationTask.getValue();
            if (success != null && success) {
                progressLabel.setText("Migration completed successfully");
                logMessage("Migration completed successfully");
            } else {
                progressLabel.setText("Migration completed with errors");
                logMessage("Migration completed with errors");
                
                // Display errors
                List<String> errors = migrationTask.getMigrationErrors();
                if (errors != null && !errors.isEmpty()) {
                    errorListView.getItems().addAll(errors);
                    errorBox.setVisible(true);
                    errorBox.setManaged(true);
                }
            }
            
            // Enable close button
            closeButton.setDisable(false);
        });
        
        migrationTask.setOnFailed(event -> {
            progressLabel.setText("Migration failed");
            logMessage("Migration failed: " + migrationTask.getException().getMessage());
            
            // Enable close button
            closeButton.setDisable(false);
        });
        
        // Start the task
        Thread thread = new Thread(migrationTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Logs a message to the log text area.
     * 
     * @param message the message to log
     */
    private void logMessage(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
            logTextArea.setScrollTop(Double.MAX_VALUE); // Scroll to bottom
        });
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param alertType the alert type
     * @param title the title
     * @param message the message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Task for performing the database migration in a background thread.
     */
    private class MigrationTask extends Task<Boolean> {
        
        private final String sourceDbPath;
        private final DatabaseMigrationUtil migrationUtil;
        private List<String> migrationErrors;
        
        public MigrationTask(String sourceDbPath) {
            this.sourceDbPath = sourceDbPath;
            this.migrationUtil = new DatabaseMigrationUtil();
        }
        
        @Override
        protected Boolean call() throws Exception {
            try {
                logMessage("Starting migration from: " + sourceDbPath);
                
                // Start migration
                boolean success = migrationUtil.migrateFromSqlite(sourceDbPath);
                
                // Get errors
                migrationErrors = migrationUtil.getMigrationErrors();
                
                return success;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during migration", e);
                logMessage("Error during migration: " + e.getMessage());
                throw e;
            }
        }
        
        /**
         * Gets the migration errors.
         * 
         * @return the migration errors
         */
        public List<String> getMigrationErrors() {
            return migrationErrors;
        }
        
        @Override
        protected void updateProgress(double workDone, double max) {
            super.updateProgress(workDone, max);
            
            // Update progress label
            Platform.runLater(() -> {
                double percentage = workDone / max * 100;
                progressLabel.setText(String.format("%.1f%%", percentage));
                
                // Log progress updates at 10% intervals
                if (Math.floor(percentage) % 10 == 0) {
                    logMessage(String.format("Migration progress: %.1f%%", percentage));
                }
            });
        }
    }
}