// src/main/java/org/frcpm/mvvm/views/SubsystemDetailMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.SubsystemDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;
import org.frcpm.mvvm.views.TaskDetailMvvmView;

/**
 * View for the subsystem detail using MVVMFx.
 */
public class SubsystemDetailMvvmView implements FxmlView<SubsystemDetailMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemDetailMvvmView.class.getName());
    
    @FXML private TextField nameTextField;
    @FXML private ComboBox<Subsystem.Status> statusComboBox;
    @FXML private ComboBox<Subteam> responsibleSubteamComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, Integer> taskProgressColumn;
    @FXML private TableColumn<Task, LocalDate> taskDueDateColumn;
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label completionPercentageLabel;
    @FXML private ProgressBar completionProgressBar;
    @FXML private Button addTaskButton;
    @FXML private Button viewTaskButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private SubsystemDetailMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubsystemDetailMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTableColumns();
        
        // Set up combo boxes
        setupComboBoxes();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        }
        
        // Set up error label
        if (errorLabel != null) {
            errorLabel.textProperty().bind(viewModel.errorMessageProperty());
            errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        }
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        if (tasksTable == null || taskTitleColumn == null || 
            taskProgressColumn == null || taskDueDateColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        try {
            taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    
            // Set up the progress column
            taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            taskProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
                private final ProgressBar progressBar = new ProgressBar();
                
                {
                    progressBar.setPrefWidth(80);
                }
                
                @Override
                protected void updateItem(Integer progress, boolean empty) {
                    super.updateItem(progress, empty);
                    if (empty || progress == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        progressBar.setProgress(progress / 100.0);
                        setText(progress + "%");
                        setGraphic(progressBar);
                    }
                }
            });
    
            // Set up the due date column
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
    }
    
    /**
     * Sets up the combo boxes.
     */
    private void setupComboBoxes() {
        if (statusComboBox == null || responsibleSubteamComboBox == null) {
            LOGGER.warning("Combo boxes not initialized - likely in test environment");
            return;
        }
        
        try {
            // Set up status combo box
            statusComboBox.setItems(viewModel.getStatusOptions());
            
            // Set up subteam combo box
            responsibleSubteamComboBox.setItems(viewModel.getAvailableSubteams());
            
            // Set up cell factories for subteam combo box
            responsibleSubteamComboBox.setCellFactory(column -> new ListCell<Subteam>() {
                @Override
                protected void updateItem(Subteam item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("None");
                    } else {
                        setText(item.getName());
                    }
                }
            });
            
            responsibleSubteamComboBox.setButtonCell(new ListCell<Subteam>() {
                @Override
                protected void updateItem(Subteam item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("None");
                    } else {
                        setText(item.getName());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up combo boxes", e);
            throw new RuntimeException("Failed to set up combo boxes", e);
        }
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (nameTextField == null || statusComboBox == null || 
            responsibleSubteamComboBox == null || descriptionTextArea == null || 
            tasksTable == null || totalTasksLabel == null || 
            completedTasksLabel == null || completionPercentageLabel == null || 
            completionProgressBar == null || saveButton == null || 
            cancelButton == null || addTaskButton == null || viewTaskButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text fields
            nameTextField.textProperty().bindBidirectional(viewModel.subsystemNameProperty());
            descriptionTextArea.textProperty().bindBidirectional(viewModel.subsystemDescriptionProperty());
            
            // Bind combo boxes
            statusComboBox.valueProperty().bindBidirectional(viewModel.statusProperty());
            responsibleSubteamComboBox.valueProperty().bindBidirectional(viewModel.responsibleSubteamProperty());
            
            // Bind table and statistics
            tasksTable.setItems(viewModel.getTasks());
            totalTasksLabel.textProperty().bind(viewModel.totalTasksProperty().asString());
            completedTasksLabel.textProperty().bind(viewModel.completedTasksProperty().asString());
            completionPercentageLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.format("%.1f%%", viewModel.completionPercentageProperty()));
            completionProgressBar.progressProperty().bind(
                    viewModel.completionPercentageProperty().divide(100.0));
            
            // Setup selection changes
            tasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            CommandAdapter.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            CommandAdapter.bindCommandButton(viewTaskButton, viewModel.getViewTaskCommand());
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
            
            // Set up task buttons
            addTaskButton.setOnAction(event -> handleAddTask());
            viewTaskButton.setOnAction(event -> handleViewTask());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Initializes the view with a new subsystem.
     */
    public void initNewSubsystem() {
        try {
            viewModel.initNewSubsystem();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new subsystem", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize new subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the view with an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        try {
            viewModel.initExistingSubsystem(subsystem);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing subsystem", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a task to the subsystem.
     */
    private void handleAddTask() {
        Subsystem subsystem = viewModel.getSelectedSubsystem();
        if (subsystem == null || subsystem.getId() == null) {
            showErrorAlert(resources.getString("error.title"), 
                          "Please save the subsystem before adding tasks");
            return;
        }
        
        try {
            // Load the task detail view (note: this is a placeholder, we'll need TaskDetailMvvmView)
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TaskDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the new task
            Task newTask = new Task();
            newTask.setSubsystem(subsystem);
            viewController.initNewTask(newTask);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.new.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(saveButton.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh the tasks
            viewModel.initExistingSubsystem(subsystem);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                         resources.getString("error.task.dialog.failed") + ": " + e.getMessage());
        }
    }
    /**
     * Handles viewing/editing an existing task.
     */
    private void handleViewTask() {
        Task selectedTask = viewModel.getSelectedTask();
        if (selectedTask == null) {
            showErrorAlert(resources.getString("error.title"), 
                         "Please select a task to view");
            return;
        }
        
        try {
            // Load the task detail view (note: this is a placeholder, we'll need TaskDetailMvvmView)
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TaskDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the existing task
            viewController.initExistingTask(selectedTask);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(saveButton.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh the tasks
            viewModel.initExistingSubsystem(viewModel.getSelectedSubsystem());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                         resources.getString("error.task.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public SubsystemDetailMvvmViewModel getViewModel() {
        return viewModel;
    }
}