package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TaskService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing subsystems in the FRC Project Management System.
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

    private final SubsystemService subsystemService = ServiceFactory.getSubsystemService();
    private final TaskService taskService = ServiceFactory.getTaskService();
    private final ObservableList<Subsystem> subsystemList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing SubsystemManagementController");

        // Set up the table columns
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
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            return new SimpleObjectProperty<>(tasks.size());
        });
        
        completionColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            if (tasks.isEmpty()) {
                return new SimpleObjectProperty<>(0.0);
            }
            long completed = tasks.stream().filter(Task::isCompleted).count();
            double percentage = (double) completed / tasks.size() * 100.0;
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

        // Set up button handlers
        addSubsystemButton.setOnAction(event -> handleAddSubsystem());
        editSubsystemButton.setOnAction(event -> handleEditSubsystem());
        deleteSubsystemButton.setOnAction(event -> handleDeleteSubsystem());
        closeButton.setOnAction(event -> closeDialog());

        // Disable edit and delete buttons when no subsystem is selected
        editSubsystemButton.disableProperty().bind(
                subsystemsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteSubsystemButton.disableProperty().bind(
                subsystemsTable.getSelectionModel().selectedItemProperty().isNull());

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

        // Load subsystems
        loadSubsystems();
    }

    /**
     * Loads subsystems from the database.
     */
    private void loadSubsystems() {
        try {
            List<Subsystem> subsystems = subsystemService.findAll();
            subsystemList.setAll(subsystems);
            subsystemsTable.setItems(subsystemList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystems", e);
            showErrorAlert("Error", "Failed to load subsystems: " + e.getMessage());
        }
    }

    /**
     * Handles adding a new subsystem.
     */
    private void handleAddSubsystem() {
        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.showSubsystemDialog(null);
            loadSubsystems(); // Refresh the list
        } else {
            openSubsystemDialog(null);
        }
    }

    /**
     * Handles editing the selected subsystem.
     */
    private void handleEditSubsystem() {
        Subsystem selectedSubsystem = subsystemsTable.getSelectionModel().getSelectedItem();
        if (selectedSubsystem != null) {
            MainController mainController = MainController.getInstance();
            if (mainController != null) {
                mainController.showSubsystemDialog(selectedSubsystem);
                loadSubsystems(); // Refresh the list
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
            loadSubsystems();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }

    /**
     * Handles deleting the selected subsystem.
     */
    private void handleDeleteSubsystem() {
        Subsystem selectedSubsystem = subsystemsTable.getSelectionModel().getSelectedItem();
        if (selectedSubsystem == null) {
            return;
        }

        // Check if the subsystem has tasks
        List<Task> tasks = taskService.findBySubsystem(selectedSubsystem);
        if (!tasks.isEmpty()) {
            showErrorAlert("Cannot Delete", 
                "Cannot delete a subsystem that has tasks. Reassign or delete the tasks first.");
            return;
        }

        // Ask for confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete " + selectedSubsystem.getName());
        confirmDialog.setContentText("Are you sure you want to delete this subsystem?");
        
        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                subsystemService.deleteById(selectedSubsystem.getId());
                subsystemList.remove(selectedSubsystem);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting subsystem", e);
                showErrorAlert("Error", "Failed to delete subsystem: " + e.getMessage());
            }
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
}