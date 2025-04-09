// src/main/java/org/frcpm/controllers/SubsystemManagementController.java
package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Subsystem;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.SubsystemManagementViewModel;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing subsystems in the FRC Project Management System.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * SubsystemManagementViewModel.
 */
public class SubsystemManagementController {

    private static final Logger LOGGER = Logger.getLogger(SubsystemManagementController.class.getName());

    @FXML
    private TableView<Subsystem> subsystemsTable;

    @FXML
    private TableColumn<Subsystem, String> nameColumn;

    @FXML
    private TableColumn<Subsystem, Subsystem.Status> statusColumn;

    @FXML
    private TableColumn<Subsystem, String> subteamColumn;

    @FXML
    private TableColumn<Subsystem, Integer> tasksColumn;

    @FXML
    private TableColumn<Subsystem, Double> completionColumn;

    @FXML
    private Button addSubsystemButton;

    @FXML
    private Button editSubsystemButton;

    @FXML
    private Button deleteSubsystemButton;

    @FXML
    private Button closeButton;

    // ViewModel and services
    private SubsystemManagementViewModel viewModel = new SubsystemManagementViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing SubsystemManagementController");

        if (subsystemsTable == null || nameColumn == null || statusColumn == null ||
                subteamColumn == null || tasksColumn == null || completionColumn == null || 
                addSubsystemButton == null || editSubsystemButton == null || 
                deleteSubsystemButton == null || closeButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Set up the table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up event handlers
        setupEventHandlers();
    }
    
    /**
     * Sets up the table columns with cell factories and value factories.
     * Protected for testability.
     */
    protected void setupTableColumns() {
        if (subsystemsTable == null || nameColumn == null || statusColumn == null ||
                subteamColumn == null || tasksColumn == null || completionColumn == null) {
            
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<Subsystem, Subsystem.Status>() {
            @Override
            protected void updateItem(Subsystem.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status.getDisplayName());
                }
            }
        });
        
        subteamColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            if (subsystem.getResponsibleSubteam() != null) {
                return new SimpleStringProperty(subsystem.getResponsibleSubteam().getName());
            } else {
                return new SimpleStringProperty("None");
            }
        });
        
        tasksColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            int taskCount = viewModel.getTaskCount(subsystem);
            return new SimpleObjectProperty<>(taskCount);
        });
        
        completionColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            double percentage = viewModel.getCompletionPercentage(subsystem);
            return new SimpleObjectProperty<>(percentage);
        });
        
        completionColumn.setCellFactory(column -> new TableCell<Subsystem, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ProgressBar progressBar = new ProgressBar(percentage / 100.0);
                    progressBar.setPrefWidth(80);
                    setText(String.format("%.1f%%", percentage));
                    setGraphic(progressBar);
                }
            }
        });
    }
    
    /**
     * Sets up bindings between UI elements and ViewModel properties.
     * Protected for testability.
     */
    protected void setupBindings() {
        if (subsystemsTable == null || addSubsystemButton == null || 
            editSubsystemButton == null || deleteSubsystemButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Bind the table to the ViewModel's subsystems list
        subsystemsTable.setItems(viewModel.getSubsystems());
        
        // Bind the selected subsystem
        if (subsystemsTable.getSelectionModel() != null) {
            subsystemsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, newValue) -> viewModel.setSelectedSubsystem(newValue));
        }
        
        // Bind button states to commands
        ViewModelBinding.bindCommandButton(addSubsystemButton, viewModel.getAddSubsystemCommand());
        ViewModelBinding.bindCommandButton(editSubsystemButton, viewModel.getEditSubsystemCommand());
        ViewModelBinding.bindCommandButton(deleteSubsystemButton, viewModel.getDeleteSubsystemCommand());
        
        // Listen for error messages
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
                viewModel.errorMessageProperty().set("");
            }
        });
    }
    
    /**
     * Sets up event handlers for UI interactions.
     * Protected for testability.
     */
    protected void setupEventHandlers() {
        if (subsystemsTable == null || closeButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Set up close button action
        closeButton.setOnAction(event -> closeDialog());
        
        // Set up double-click handler for editing
        subsystemsTable.setRowFactory(tv -> {
            TableRow<Subsystem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditSubsystem();
                }
            });
            return row;
        });
        
        // Override command handlers for dialog interactions
        addSubsystemButton.setOnAction(event -> handleAddSubsystem());
        editSubsystemButton.setOnAction(event -> handleEditSubsystem());
    }

    /**
     * Handles adding a new subsystem.
     * This is a public method for FXML access.
     */
    @FXML
    public void handleAddSubsystem() {
        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.showSubsystemDialog(null);
            viewModel.getLoadSubsystemsCommand().execute();
        } else {
            openSubsystemDialog(null);
        }
    }

    /**
     * Handles editing the selected subsystem.
     * This is a public method for FXML access.
     */
    @FXML
    public void handleEditSubsystem() {
        Subsystem selectedSubsystem = viewModel.getSelectedSubsystem();
        if (selectedSubsystem != null) {
            MainController mainController = MainController.getInstance();
            if (mainController != null) {
                mainController.showSubsystemDialog(selectedSubsystem);
                viewModel.getLoadSubsystemsCommand().execute();
            } else {
                openSubsystemDialog(selectedSubsystem);
            }
        }
    }

    /**
     * Opens the subsystem dialog directly when MainController is not available.
     * Protected for testability.
     * 
     * @param subsystem the subsystem to edit, or null to create a new one
     */
    protected void openSubsystemDialog(Subsystem subsystem) {
        try {
            if (subsystemsTable == null || subsystemsTable.getScene() == null ||
                subsystemsTable.getScene().getWindow() == null) {
                
                LOGGER.warning("Cannot open subsystem dialog - UI components not initialized");
                return;
            }
            
            // Load the subsystem dialog
            FXMLLoader loader = createFXMLLoader("/fxml/SubsystemView.fxml");
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = createDialogStage(
                subsystem == null ? "New Subsystem" : "Edit Subsystem",
                subsystemsTable.getScene().getWindow(),
                dialogView
            );

            // Get the controller
            SubsystemController controller = loader.getController();
            
            // Initialize controller based on whether we're creating or editing
            if (subsystem != null) {
                controller.initExistingSubsystem(subsystem);
            } else {
                controller.initNewSubsystem();
            }

            // Show the dialog
            showAndWaitDialog(dialogStage);
            
            // Refresh the subsystems list
            viewModel.getLoadSubsystemsCommand().execute();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }

    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (closeButton != null && closeButton.getScene() != null && 
                closeButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows a confirmation alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    protected boolean showConfirmationAlert(String title, String message) {
        try {
            return dialogService.showConfirmationAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation would show: {0} - {1}", new Object[] { title, message });
            return false;
        }
    }
    
    /**
     * Creates an FXML loader.
     * Protected for testability.
     * 
     * @param fxmlPath the path to the FXML file
     * @return the FXMLLoader
     */
    protected FXMLLoader createFXMLLoader(String fxmlPath) {
        return new FXMLLoader(getClass().getResource(fxmlPath));
    }
    
    /**
     * Creates a dialog stage.
     * Protected for testability.
     * 
     * @param title the dialog title
     * @param owner the owner window
     * @param content the dialog content
     * @return the created stage
     */
    protected Stage createDialogStage(String title, Window owner, Parent content) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setScene(new Scene(content));
        return dialogStage;
    }
    
    /**
     * Shows a dialog and waits for it to be closed.
     * Protected for testability.
     * 
     * @param dialogStage the dialog stage to show
     * @return an optional containing ButtonType.OK
     */
    protected Optional<ButtonType> showAndWaitDialog(Stage dialogStage) {
        try {
            if (dialogStage != null) {
                dialogStage.showAndWait();
                return Optional.of(ButtonType.OK);
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
            return Optional.empty();
        }
    }
    
    /**
     * Gets the selected subsystem from the table.
     * Protected for testability.
     * 
     * @return the selected subsystem
     */
    protected Subsystem getSelectedSubsystem() {
        if (subsystemsTable != null && subsystemsTable.getSelectionModel() != null) {
            return subsystemsTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Gets the ViewModel for this controller.
     * 
     * @return the ViewModel
     */
    public SubsystemManagementViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel for this controller.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(SubsystemManagementViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    /**
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
    
    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}