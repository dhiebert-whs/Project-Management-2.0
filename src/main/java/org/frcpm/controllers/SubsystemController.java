// src/main/java/org/frcpm/controllers/SubsystemController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.SubsystemViewModel;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for subsystem management using MVVM pattern.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * SubsystemViewModel.
 */
public class SubsystemController {

    private static final Logger LOGGER = Logger.getLogger(SubsystemController.class.getName());

    // Main form fields
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Subsystem.Status> statusComboBox;

    @FXML
    private ComboBox<Subteam> responsibleSubteamComboBox;

    // Task table
    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;

    // Summary fields
    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label completedTasksLabel;

    @FXML
    private Label completionPercentageLabel;

    @FXML
    private ProgressBar completionProgressBar;

    // Error display
    @FXML
    private Label errorLabel;

    // Buttons
    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button viewTaskButton;

    // ViewModel and services
    private SubsystemViewModel viewModel = new SubsystemViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing SubsystemController");
        
        if (nameField == null || descriptionArea == null || statusComboBox == null || 
            responsibleSubteamComboBox == null || tasksTable == null || taskTitleColumn == null || 
            taskProgressColumn == null || taskDueDateColumn == null || totalTasksLabel == null || 
            completedTasksLabel == null || completionPercentageLabel == null || 
            completionProgressBar == null || saveButton == null || cancelButton == null || 
            addTaskButton == null || viewTaskButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Set up tasks table
            setupTasksTable();

            // Set up bindings
            setupBindings();
            
            // Set up error message listener
            setupErrorListener();
            
            // Initialize status combo box if not already populated
            if (statusComboBox.getItems().isEmpty()) {
                statusComboBox.getItems().addAll(Subsystem.Status.values());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing controller", e);
            showErrorAlert("Initialization Error", "Failed to initialize controller: " + e.getMessage());
        }
    }

    /**
     * Sets up the tasks table columns and behaviors.
     * Protected for testability.
     */
    protected void setupTasksTable() {
        if (tasksTable == null || taskTitleColumn == null || 
            taskProgressColumn == null || taskDueDateColumn == null) {
            
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        try {
            taskTitleColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));

            taskProgressColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("progress"));
            taskProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
                @Override
                protected void updateItem(Integer progress, boolean empty) {
                    super.updateItem(progress, empty);
                    if (empty || progress == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Create a progress bar for the cell
                        ProgressBar pb = new ProgressBar(progress / 100.0);
                        pb.setPrefWidth(80);
                        setGraphic(pb);
                        setText(progress + "%");
                    }
                }
            });

            taskDueDateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endDate"));
            taskDueDateColumn.setCellFactory(column -> new TableCell<Task, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.toString());
                    }
                }
            });

            // Set up double-click handler
            tasksTable.setRowFactory(tv -> {
                TableRow<Task> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        viewModel.setSelectedTask(row.getItem());
                        handleViewTask();
                    }
                });
                return row;
            });
            
            // Set selection handling
            tasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up tasks table", e);
            showErrorAlert("Setup Error", "Failed to set up tasks table: " + e.getMessage());
        }
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     * Protected for testability.
     */
    protected void setupBindings() {
        if (nameField == null || descriptionArea == null || statusComboBox == null || 
            responsibleSubteamComboBox == null || tasksTable == null || totalTasksLabel == null || 
            completedTasksLabel == null || completionPercentageLabel == null || 
            completionProgressBar == null || saveButton == null || cancelButton == null || 
            addTaskButton == null || viewTaskButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        try {
            // Bind text fields
            ViewModelBinding.bindTextField(nameField, viewModel.subsystemNameProperty());
            ViewModelBinding.bindTextArea(descriptionArea, viewModel.subsystemDescriptionProperty());

            // Bind combo boxes
            ViewModelBinding.bindComboBox(statusComboBox, viewModel.statusProperty());
            
            // Set up the subteam combo box
            responsibleSubteamComboBox.setItems(viewModel.getAvailableSubteams());
            ViewModelBinding.bindComboBox(responsibleSubteamComboBox, viewModel.responsibleSubteamProperty());

            // Bind table items
            tasksTable.setItems(viewModel.getTasks());

            // Bind summary fields
            totalTasksLabel.textProperty().bind(viewModel.totalTasksProperty().asString());
            completedTasksLabel.textProperty().bind(viewModel.completedTasksProperty().asString());
            completionPercentageLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.format("%.1f%%", viewModel.completionPercentageProperty()));
            completionProgressBar.progressProperty().bind(
                    viewModel.completionPercentageProperty().divide(100.0));

            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
            ViewModelBinding.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            ViewModelBinding.bindCommandButton(viewTaskButton, viewModel.getViewTaskCommand());

            // Handle cancel button
            cancelButton.setOnAction(event -> closeDialog());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to set up bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the error message listener.
     * Protected for testability.
     */
    protected void setupErrorListener() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        try {
            // Set up validation error listener
            viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    if (errorLabel != null) {
                        // Display error in the UI label
                        errorLabel.setText(newValue);
                        errorLabel.setVisible(true);
                    } else {
                        // Fallback to dialog if label is not available
                        showErrorAlert("Error", newValue);
                    }
                } else if (errorLabel != null) {
                    // Clear error message
                    errorLabel.setText("");
                    errorLabel.setVisible(false);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up error listener", e);
        }
    }

    /**
     * Sets up the controller for creating a new subsystem.
     */
    public void initNewSubsystem() {
        try {
            viewModel.initNewSubsystem();
            
            // Clear error message
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new subsystem", e);
            showErrorAlert("Initialization Error", "Failed to initialize new subsystem: " + e.getMessage());
        }
    }

    /**
     * Sets up the controller for editing an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            LOGGER.warning("Cannot initialize with null subsystem");
            return;
        }
        
        try {
            viewModel.initExistingSubsystem(subsystem);
            
            // Clear error message
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing subsystem", e);
            showErrorAlert("Initialization Error", "Failed to initialize subsystem: " + e.getMessage());
        }
    }

    /**
     * Handles the add task action.
     * This method is bound to the Add Task button.
     */
    @FXML
    public void handleAddTask() {
        try {
            Subsystem subsystem = viewModel.getSelectedSubsystem();
            if (subsystem == null) {
                showErrorAlert("Error", "Please save the subsystem before adding tasks");
                return;
            }
            
            // Create a new task
            Task newTask = new Task();
            newTask.setSubsystem(subsystem);
            
            // Show task dialog
            TaskController controller = showTaskDialog(newTask);
            if (controller != null) {
                // Refresh tasks after dialog closes
                viewModel.loadTasks();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            showErrorAlert("Error", "Failed to add task: " + e.getMessage());
        }
    }
    
    /**
     * Handles the view task action.
     * This method is bound to the View Task button.
     */
    @FXML
    public void handleViewTask() {
        Task selectedTask = viewModel.getSelectedTask();
        if (selectedTask == null) {
            selectedTask = getSelectedTask();
            if (selectedTask == null) {
                showInfoAlert("No Selection", "Please select a task to view");
                return;
            }
            viewModel.setSelectedTask(selectedTask);
        }
        
        // Show task dialog
        TaskController controller = showTaskDialog(selectedTask);
        if (controller != null) {
            // Refresh tasks after dialog closes
            viewModel.loadTasks();
        }
    }
    
    /**
     * Shows the task dialog.
     * 
     * @param task the task to edit
     * @return the task controller
     */
    protected TaskController showTaskDialog(Task task) {
        try {
            // Show task in main controller if available
            MainController mainController = MainController.getInstance();
            if (mainController != null) {
                mainController.showTaskDialog(task, null);
                return null; // Can't return controller in this case
            } else {
                // Open dialog directly
                return openTaskDialogDirectly(task);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task dialog", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Opens the task dialog directly when MainController is not available.
     * Protected for testability.
     * 
     * @param task the task to edit
     * @return the task controller
     */
    protected TaskController openTaskDialogDirectly(Task task) {
        try {
            if (saveButton == null || saveButton.getScene() == null || 
                saveButton.getScene().getWindow() == null) {
                
                LOGGER.warning("Cannot open task dialog - UI components not initialized");
                return null;
            }
            
            // Load the task dialog
            FXMLLoader loader = createFXMLLoader("/fxml/TaskView.fxml");
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = createDialogStage("Edit Task", saveButton.getScene().getWindow(), dialogView);

            // Get the controller
            TaskController controller = loader.getController();
            if (task.getId() == null) {
                controller.initNewTask(task);
            } else {
                controller.initExistingTask(task);
            }

            // Show the dialog
            showAndWaitDialog(dialogStage);
            
            return controller;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading task dialog directly", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (cancelButton != null && cancelButton.getScene() != null && 
                cancelButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) cancelButton.getScene().getWindow();
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
     * @param title   the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            if (dialogService != null) {
                dialogService.showErrorAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            if (dialogService != null) {
                dialogService.showInfoAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Info alert would show: {0} - {1}", new Object[] { title, message });
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Info alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows a confirmation alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    protected boolean showConfirmationAlert(String title, String message) {
        try {
            if (dialogService != null) {
                return dialogService.showConfirmationAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Confirmation alert would show: {0} - {1}", new Object[] { title, message });
                return false;
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation alert would show: {0} - {1}", new Object[] { title, message });
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
     * @return the created Stage
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
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
    
    /**
     * Sets the ViewModel for this controller.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(SubsystemViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Gets the subsystem from the ViewModel.
     * 
     * @return the subsystem
     */
    public Subsystem getSubsystem() {
        return viewModel.getSelectedSubsystem();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public SubsystemViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the selected task from the table.
     * Protected for testability.
     * 
     * @return the selected task
     */
    protected Task getSelectedTask() {
        if (tasksTable != null && tasksTable.getSelectionModel() != null) {
            return tasksTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Cleanup resources when the controller is no longer needed.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
    }
    
    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}