// src/main/java/org/frcpm/presenters/SubsystemPresenter.java
package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.SubsystemViewModel;
import org.frcpm.views.TaskView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for subsystem management using AfterburnerFX pattern.
 */
public class SubsystemPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(SubsystemPresenter.class.getName());

    // FXML UI components
    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<Subsystem.Status> statusComboBox;

    @FXML
    private ComboBox<Subteam> responsibleSubteamComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;

    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label completedTasksLabel;

    @FXML
    private Label completionPercentageLabel;

    @FXML
    private ProgressBar completionProgressBar;

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button viewTaskButton;

    // Injected services
    @Inject
    private SubsystemService subsystemService;
    
    @Inject
    private SubteamService subteamService;
    
    @Inject
    private TaskService taskService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private SubsystemViewModel viewModel;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubsystemPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected services
        viewModel = new SubsystemViewModel(subsystemService, subteamService, taskService);

        // Set up status combo box
        setupStatusComboBox();
        
        // Set up tasks table
        setupTasksTable();
        
        // Set up bindings
        setupBindings();
        
        // Set up error message handling
        setupErrorHandling();
        
        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
    
    /**
     * Sets up the status combo box.
     */
    private void setupStatusComboBox() {
        if (statusComboBox == null) {
            LOGGER.warning("Status combo box not initialized - likely in test environment");
            return;
        }
        
        statusComboBox.getItems().setAll(Subsystem.Status.values());
    }

    /**
     * Sets up the tasks table columns and behaviors.
     */
    private void setupTasksTable() {
        if (tasksTable == null || taskTitleColumn == null || 
            taskProgressColumn == null || taskDueDateColumn == null) {
            
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Set up the progress column
        taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
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

        taskDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
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
        
        // Add task selection listener
        tasksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        if (nameField == null || statusComboBox == null || responsibleSubteamComboBox == null || 
            descriptionArea == null || tasksTable == null || totalTasksLabel == null || 
            completedTasksLabel == null || completionPercentageLabel == null || 
            completionProgressBar == null || saveButton == null || cancelButton == null || 
            addTaskButton == null || viewTaskButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        try {
            // Bind text field and text area
            ViewModelBinding.bindTextField(nameField, viewModel.subsystemNameProperty());
            ViewModelBinding.bindTextArea(descriptionArea, viewModel.subsystemDescriptionProperty());
            
            // Bind combo boxes
            ViewModelBinding.bindComboBox(statusComboBox, viewModel.statusProperty());
            
            // Set up the subteam combo box
            responsibleSubteamComboBox.setItems(viewModel.getAvailableSubteams());
            ViewModelBinding.bindComboBox(responsibleSubteamComboBox, viewModel.responsibleSubteamProperty());
            
            // Bind tasks table
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
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the error message listener.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        try {
            // Set up validation error listener
            viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    // If we have an error label, use it
                    if (errorLabel != null) {
                        errorLabel.setText(newValue);
                        errorLabel.setVisible(true);
                    } else {
                        // Fall back to dialog
                        showErrorAlert("Validation Error", newValue);
                    }
                } else if (errorLabel != null) {
                    // Hide error label when no error
                    errorLabel.setVisible(false);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up error listener", e);
            showErrorAlert("Setup Error", "Failed to initialize error handling: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the presenter for creating a new subsystem.
     */
    public void initNewSubsystem() {
        viewModel.initNewSubsystem();
    }

    /**
     * Sets up the presenter for editing an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            LOGGER.warning("Cannot initialize with null subsystem");
            return;
        }
        
        try {
            // Initialize the view model with the existing subsystem
            viewModel.initExistingSubsystem(subsystem);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing subsystem", e);
            showErrorAlert("Initialization Error", "Failed to initialize subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Handles the add task action.
     * This method is called by the AddTaskCommand.
     */
    @FXML
    private void handleAddTask() {
        Subsystem subsystem = viewModel.getSelectedSubsystem();
        if (subsystem == null) {
            showErrorAlert("Error", "Please save the subsystem before adding tasks");
            return;
        }
        
        // Use ViewLoader for AfterburnerFX integration
        TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                resources.getString("task.new.title"), 
                getWindow());
        
        if (presenter != null) {
            // Create a new task associated with just the subsystem
            // The project association will be handled by the task presenter
            Task newTask = new Task();
            newTask.setSubsystem(subsystem);
            presenter.initNewTask(newTask);
            
            // Refresh the tasks
            viewModel.loadTasks();
        }
    }
    
    /**
     * Handles the view task action.
     * This method is called by the ViewTaskCommand.
     */
    @FXML
    private void handleViewTask() {
        Task selectedTask = viewModel.getSelectedTask();
        if (selectedTask == null) {
            showInfoAlert("No Selection", "Please select a task to view");
            return;
        }
        
        // Use ViewLoader for AfterburnerFX integration
        TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                resources.getString("task.edit.title"), 
                getWindow());
        
        if (presenter != null) {
            presenter.initExistingTask(selectedTask);
            
            // Refresh the tasks
            viewModel.loadTasks();
        }
    }
    
    /**
     * Gets the window for this presenter.
     * 
     * @return the window, or null if not available
     */
    private Window getWindow() {
        if (saveButton != null && saveButton.getScene() != null) {
            return saveButton.getScene().getWindow();
        }
        return null;
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            // Clean up resources
            cleanup();
            
            // Close the window
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Cleans up resources.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Gets the subsystem.
     * 
     * @return the subsystem from the view model
     */
    public Subsystem getSubsystem() {
        return viewModel.getSelectedSubsystem();
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public SubsystemViewModel getViewModel() {
        return viewModel;
    }
}