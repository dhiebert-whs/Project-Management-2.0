package org.frcpm.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.frcpm.binding.Command;
import org.frcpm.utils.DatabaseMigrationUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for Database Migration operations.
 * Follows the MVVM pattern to separate business logic from UI.
 */
public class DatabaseMigrationViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationViewModel.class.getName());
    
    // Properties
    private final StringProperty sourceDbPath = new SimpleStringProperty("");
    private final StringProperty progressText = new SimpleStringProperty("Ready");
    private final StringProperty logText = new SimpleStringProperty("");
    private final BooleanProperty migrateButtonDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty closeButtonDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty migrationInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty errorBoxVisible = new SimpleBooleanProperty(false);
    private final ObservableList<String> errorList = FXCollections.observableArrayList();
    private final ObjectProperty<Double> progressValue = new SimpleObjectProperty<>(0.0);
    
    // Migration utility
    private final DatabaseMigrationUtil migrationUtil;
    
    // Commands
    private final Command browseSourceDbCommand;
    private final Command migrateCommand;
    private final Command closeCommand;
    
    /**
     * Creates a new DatabaseMigrationViewModel.
     */
    public DatabaseMigrationViewModel() {
        this(new DatabaseMigrationUtil());
    }
    
    /**
     * Creates a new DatabaseMigrationViewModel with the specified migration utility.
     * This constructor is mainly used for testing.
     * 
     * @param migrationUtil the database migration utility
     */
    public DatabaseMigrationViewModel(DatabaseMigrationUtil migrationUtil) {
        this.migrationUtil = migrationUtil;
        
        // Initialize commands
        browseSourceDbCommand = new Command(this::browseSourceDb);
        migrateCommand = new Command(this::migrate, this::canMigrate);
        closeCommand = new Command(this::close);
        
        // Set up property listeners
        sourceDbPath.addListener((observable, oldValue, newValue) -> {
            migrateButtonDisabled.set(newValue == null || newValue.trim().isEmpty());
        });
    }
    
    /**
     * Handles browsing for a source database file.
     * Note: This will be delegated to the controller to handle the file dialog.
     */
    public void browseSourceDb() {
        // This is a placeholder because file dialogs will be handled by the controller
        // The controller will call setSourceDbPath with the selected file path
        LOGGER.info("Browse source database action triggered");
    }
    
    /**
     * Sets the source database path.
     * 
     * @param path the source database path
     */
    public void setSourceDbPath(String path) {
        sourceDbPath.set(path);
    }
    
    /**
     * Handles the migration process.
     */
    public void migrate() {
        String path = sourceDbPath.get();
        if (path == null || path.trim().isEmpty()) {
            setErrorMessage("Source database path cannot be empty");
            return;
        }
        
        // Update UI state
        migrationInProgress.set(true);
        errorBoxVisible.set(false);
        errorList.clear();
        progressValue.set(0.0);
        progressText.set("0.0%");
        logText.set("");
        clearErrorMessage();
        
        // Create migration task
        MigrationTask migrationTask = new MigrationTask(path);
        
        // Set up task completion handling
        migrationTask.setOnSucceeded(event -> {
            Boolean success = migrationTask.getValue();
            if (success != null && success) {
                progressText.set("Migration completed successfully");
                logMessage("Migration completed successfully");
            } else {
                progressText.set("Migration completed with errors");
                logMessage("Migration completed with errors");
                
                // Display errors
                List<String> errors = migrationTask.getMigrationErrors();
                if (errors != null && !errors.isEmpty()) {
                    errorList.addAll(errors);
                    errorBoxVisible.set(true);
                }
            }
            
            // Update UI state
            migrationInProgress.set(false);
            closeButtonDisabled.set(false);
        });
        
        migrationTask.setOnFailed(event -> {
            progressText.set("Migration failed");
            logMessage("Migration failed: " + migrationTask.getException().getMessage());
            
            // Update UI state
            migrationInProgress.set(false);
            closeButtonDisabled.set(false);
        });
        
        // Start the task
        Thread thread = new Thread(migrationTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Checks if the migrate command can be executed.
     * 
     * @return true if the command can be executed, false otherwise
     */
    public boolean canMigrate() {
        return !migrationInProgress.get() && !sourceDbPath.get().trim().isEmpty();
    }
    
    /**
     * Handles closing the dialog.
     * Note: This will be delegated to the controller to handle the dialog.
     */
    public void close() {
        // This is a placeholder because dialog closing will be handled by the controller
        LOGGER.info("Close dialog action triggered");
    }
    
    /**
     * Logs a message to the log text.
     * 
     * @param message the message to log
     */
    public void logMessage(String message) {
        String currentLog = logText.get();
        logText.set(currentLog + message + "\n");
    }
    
    /**
     * Task for performing the database migration in a background thread.
     */
    private class MigrationTask extends Task<Boolean> {
        
        private final String sourceDbPath;
        private List<String> migrationErrors;
        
        public MigrationTask(String sourceDbPath) {
            this.sourceDbPath = sourceDbPath;
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
            
            // Update progress properties
            double percentage = workDone / max * 100;
            progressValue.set(workDone / max);
            progressText.set(String.format("%.1f%%", percentage));
            
            // Log progress updates at 10% intervals
            if (Math.floor(percentage) % 10 == 0) {
                logMessage(String.format("Migration progress: %.1f%%", percentage));
            }
        }
    }
    
    // Property accessors
    
    public StringProperty sourceDbPathProperty() {
        return sourceDbPath;
    }
    
    public StringProperty progressTextProperty() {
        return progressText;
    }
    
    public StringProperty logTextProperty() {
        return logText;
    }
    
    public BooleanProperty migrateButtonDisabledProperty() {
        return migrateButtonDisabled;
    }
    
    public BooleanProperty closeButtonDisabledProperty() {
        return closeButtonDisabled;
    }
    
    public BooleanProperty migrationInProgressProperty() {
        return migrationInProgress;
    }
    
    public BooleanProperty errorBoxVisibleProperty() {
        return errorBoxVisible;
    }
    
    public ObservableList<String> getErrorList() {
        return errorList;
    }
    
    public ObjectProperty<Double> progressValueProperty() {
        return progressValue;
    }
    
    // Command accessors
    
    public Command getBrowseSourceDbCommand() {
        return browseSourceDbCommand;
    }
    
    public Command getMigrateCommand() {
        return migrateCommand;
    }
    
    public Command getCloseCommand() {
        return closeCommand;
    }
    
    // Getters and setters
    
    public String getSourceDbPath() {
        return sourceDbPath.get();
    }
    
    public String getProgressText() {
        return progressText.get();
    }
    
    public void setProgressText(String text) {
        progressText.set(text);
    }
    
    public String getLogText() {
        return logText.get();
    }
    
    public void setLogText(String text) {
        logText.set(text);
    }
    
    public boolean isMigrateButtonDisabled() {
        return migrateButtonDisabled.get();
    }
    
    public void setMigrateButtonDisabled(boolean disabled) {
        migrateButtonDisabled.set(disabled);
    }
    
    public boolean isCloseButtonDisabled() {
        return closeButtonDisabled.get();
    }
    
    public void setCloseButtonDisabled(boolean disabled) {
        closeButtonDisabled.set(disabled);
    }
    
    public boolean isMigrationInProgress() {
        return migrationInProgress.get();
    }
    
    public void setMigrationInProgress(boolean inProgress) {
        migrationInProgress.set(inProgress);
    }
    
    public boolean isErrorBoxVisible() {
        return errorBoxVisible.get();
    }
    
    public void setErrorBoxVisible(boolean visible) {
        errorBoxVisible.set(visible);
    }
    
    public Double getProgressValue() {
        return progressValue.get();
    }
    
    public void setProgressValue(Double value) {
        progressValue.set(value);
    }
}
