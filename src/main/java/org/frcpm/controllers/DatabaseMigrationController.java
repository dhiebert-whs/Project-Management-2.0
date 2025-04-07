package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.viewmodels.DatabaseMigrationViewModel;

import java.io.File;
import java.util.logging.Logger;

/**
 * Controller for the database migration dialog.
 * Follows the MVVM pattern by delegating business logic to the DatabaseMigrationViewModel.
 */
public class DatabaseMigrationController {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationController.class.getName());
    
    // View components
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
    
    // Dialog stage
    private Stage dialogStage;
    
    // ViewModel
    private final DatabaseMigrationViewModel viewModel = new DatabaseMigrationViewModel();
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing DatabaseMigrationController with MVVM pattern");
        
        // Set up bindings between view and view model
        setupBindings();
        
        // Set up event handlers
        browseButton.setOnAction(event -> handleBrowseSourceDb());
    }
    
    /**
     * Sets up the bindings between the view and the view model.
     */
    private void setupBindings() {
        // Bind text fields to view model properties
        ViewModelBinding.bindTextField(sourceDbPathField, viewModel.sourceDbPathProperty());
        
        // Bind progress indicators
        progressBar.progressProperty().bind(viewModel.progressValueProperty());
        progressLabel.textProperty().bind(viewModel.progressTextProperty());
        
        // Bind log text area
        logTextArea.textProperty().bind(viewModel.logTextProperty());
        
        // Bind error box
        errorBox.visibleProperty().bind(viewModel.errorBoxVisibleProperty());
        errorBox.managedProperty().bind(viewModel.errorBoxVisibleProperty());
        errorListView.setItems(viewModel.getErrorList());
        
        // Bind button states
        migrateButton.disableProperty().bind(viewModel.migrateButtonDisabledProperty());
        closeButton.disableProperty().bind(viewModel.closeButtonDisabledProperty());
        
        // Bind command buttons
        ViewModelBinding.bindCommandButton(migrateButton, viewModel.getMigrateCommand());
        
        // Set up close button action (handled by controller)
        closeButton.setOnAction(event -> dialogStage.close());
        
        // Bind error message property
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", newValue);
                viewModel.errorMessageProperty().set("");
            }
        });
    }
    
    /**
     * Sets the dialog stage.
     * 
     * @param dialogStage the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Handles browsing for the source database file.
     */
    private void handleBrowseSourceDb() {
        File file = showFileChooserDialog(dialogStage);
        if (file != null) {
            viewModel.setSourceDbPath(file.getAbsolutePath());
        }
    }
    
    /**
     * Shows a file chooser dialog.
     * This method is protected to allow overriding in tests.
     * 
     * @param ownerStage the owner stage
     * @return the selected file, or null if cancelled
     */
    protected File showFileChooserDialog(Stage ownerStage) {
        FileChooser fileChooser = createFileChooser();
        return fileChooser.showOpenDialog(ownerStage);
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param alertType the alert type
     * @param title the title
     * @param message the message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = createAlert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Creates a FileChooser for selecting database files.
     * This method is protected for testing purposes.
     * 
     * @return a configured FileChooser
     */
    protected FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select SQLite Database");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQLite Database", "*.db", "*.sqlite", "*.sqlite3")
        );
        return fileChooser;
    }
    
    /**
     * Creates an Alert dialog.
     * This method is protected for testing purposes.
     * 
     * @param alertType the type of alert to create
     * @return the created alert
     */
    protected Alert createAlert(Alert.AlertType alertType) {
        return new Alert(alertType);
    }
    
    // Getters for testing
    
    /**
     * Gets the source database path field.
     * 
     * @return the source database path field
     */
    public TextField getSourceDbPathField() {
        return sourceDbPathField;
    }
    
    /**
     * Gets the browse button.
     * 
     * @return the browse button
     */
    public Button getBrowseButton() {
        return browseButton;
    }
    
    /**
     * Gets the migrate button.
     * 
     * @return the migrate button
     */
    public Button getMigrateButton() {
        return migrateButton;
    }
    
    /**
     * Gets the progress bar.
     * 
     * @return the progress bar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }
    
    /**
     * Gets the progress label.
     * 
     * @return the progress label
     */
    public Label getProgressLabel() {
        return progressLabel;
    }
    
    /**
     * Gets the log text area.
     * 
     * @return the log text area
     */
    public TextArea getLogTextArea() {
        return logTextArea;
    }
    
    /**
     * Gets the close button.
     * 
     * @return the close button
     */
    public Button getCloseButton() {
        return closeButton;
    }
    
    /**
     * Gets the error box.
     * 
     * @return the error box
     */
    public VBox getErrorBox() {
        return errorBox;
    }
    
    /**
     * Gets the error list view.
     * 
     * @return the error list view
     */
    public ListView<String> getErrorListView() {
        return errorListView;
    }
    
    /**
     * Gets the dialog stage.
     * 
     * @return the dialog stage
     */
    public Stage getDialogStage() {
        return dialogStage;
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public DatabaseMigrationViewModel getViewModel() {
        return viewModel;
    }
    
    // Public methods for testing
    
    /**
     * Public method to access handleBrowseSourceDb for testing.
     */
    public void testHandleBrowseSourceDb() {
        handleBrowseSourceDb();
    }
    
    /**
     * Public method to access showAlert for testing.
     * 
     * @param alertType the alert type
     * @param title the title
     * @param message the message
     */
    public void testShowAlert(Alert.AlertType alertType, String title, String message) {
        showAlert(alertType, title, message);
    }
}