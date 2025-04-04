package org.frcpm.controllers;

import javafx.beans.binding.Bindings;
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
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.viewmodels.SubsystemManagementViewModel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing subsystems in the FRC Project Management System.
 * Follows MVVM pattern by delegating business logic to the ViewModel.
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

    // ViewModel
    private final SubsystemManagementViewModel viewModel = new SubsystemManagementViewModel();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing SubsystemManagementController");

        // Set up the table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up event handlers
        setupEventHandlers();
    }
    
    /**
     * Sets up the table columns with cell factories and value factories.
     */
    private void setupTableColumns() {
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
     */
    private void setupBindings() {
        // Bind the table to the ViewModel's subsystems list
        subsystemsTable.setItems(viewModel.getSubsystems());
        
        // Bind the selected subsystem
        subsystemsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> viewModel.setSelectedSubsystem(newValue));
        
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
     */
    private void setupEventHandlers() {
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
     */
    private void handleAddSubsystem() {
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
     */
    private void handleEditSubsystem() {
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
     * 
     * @param subsystem the subsystem to edit, or null to create a new one
     */
    private void openSubsystemDialog(Subsystem subsystem) {
        try {
            // Load the subsystem dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubsystemView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(subsystem == null ? "New Subsystem" : "Edit Subsystem");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(subsystemsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            SubsystemController controller = loader.getController();
            
            // Initialize controller based on whether we're creating or editing
            if (subsystem != null) {
                controller.initExistingSubsystem(subsystem);
            } else {
                controller.initNewSubsystem();
            }

            // Show the dialog
            dialogStage.showAndWait();
            
            // Refresh the subsystems list
            viewModel.getLoadSubsystemsCommand().execute();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Gets the ViewModel for this controller.
     * Primarily used for testing.
     * 
     * @return the ViewModel
     */
    public SubsystemManagementViewModel getViewModel() {
        return viewModel;
    }
}