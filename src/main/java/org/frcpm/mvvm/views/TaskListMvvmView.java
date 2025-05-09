// src/main/java/org/frcpm/mvvm/views/TaskListMvvmView.java (updated)

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.MvvmDialogHelper;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskListMvvmViewModel;

/**
 * View for the task list using MVVMFx.
 */
public class TaskListMvvmView implements FxmlView<TaskListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TaskListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectNameLabel;
    
    @FXML
    private TableView<Task> taskTableView;
    
    @FXML
    private TableColumn<Task, String> titleColumn;
    
    @FXML
    private TableColumn<Task, Task.Priority> priorityColumn;
    
    @FXML
    private TableColumn<Task, Integer> progressColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> startDateColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> endDateColumn;
    
    @FXML
    private Button newTaskButton;
    
    @FXML
    private Button editTaskButton;
    
    @FXML
    private Button deleteTaskButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private TaskListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TaskListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // Set up task table view
        taskTableView.setItems(viewModel.getTasks());
        
        // Bind selected task
        taskTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedTask(newVal);
        });
        
        // Bind project name label
        viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                projectNameLabel.setText(newVal.getName());
            } else {
                projectNameLabel.setText("");
            }
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(newTaskButton, viewModel.getNewTaskCommand());
        CommandAdapter.bindCommandButton(editTaskButton, viewModel.getEditTaskCommand());
        CommandAdapter.bindCommandButton(deleteTaskButton, viewModel.getDeleteTaskCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshTasksCommand());
        
        // Override button actions to handle dialogs
        newTaskButton.setOnAction(e -> handleNewTask());
        editTaskButton.setOnAction(e -> handleEditTask());
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
    }
    
    /**
     * Sets the project for the task list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadTasksCommand().execute();
    }
    
    /**
     * Handle new task button click.
     */
    private void handleNewTask() {
        Project project = viewModel.getCurrentProject();
        if (project == null) {
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.project.required"));
            return;
        }
        
        try {
            // We need to get a subsystem from elsewhere since Project doesn't have getSubsystems()
            // For now, we'll assume we need to look up subsystems separately
            // In a real implementation, you would get the subsystems for the project from a service
            Subsystem subsystem = null;
            
            // This would be replaced with an actual service call
            // Example: List<Subsystem> subsystems = subsystemService.findByProject(project);
            // if (!subsystems.isEmpty()) subsystem = subsystems.get(0);
            
            // For demo purposes, let's create a stub subsystem or show a dialog to select one
            // Here we'll just show an error if we don't have a subsystem
            if (subsystem == null) {
                showErrorAlert(resources.getString("error.title"), 
                            resources.getString("error.subsystem.required"));
                return;
            }
            
            // Create a new temporary task for initialization
            Task newTask = new Task("New Task", project, subsystem);
            
            // Show task dialog
            openTaskDetailDialog(newTask, true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new task", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.task.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit task button click.
     */
    private void handleEditTask() {
        Task selectedTask = viewModel.getSelectedTask();
        if (selectedTask == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.task.select"));
            return;
        }
        
        try {
            // Show task dialog
            openTaskDetailDialog(selectedTask, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.task.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a task detail dialog.
     * 
     * @param task the task to edit or create
     * @param isNew true if creating a new task, false if editing
     */
    private void openTaskDetailDialog(Task task, boolean isNew) {
        try {
            // Load the task detail view
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller (code behind) and view model
            TaskDetailMvvmView viewController = viewTuple.getCodeBehind();
            TaskDetailMvvmViewModel taskViewModel = viewTuple.getViewModel();
            
            // Initialize the controller with the task
            if (isNew) {
                viewController.initNewTask(task);
            } else {
                viewController.initExistingTask(task);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("task.new.title") : 
                                        resources.getString("task.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            
            // Use viewTuple.getView() which returns Parent for the scene
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh tasks after dialog closes
            viewModel.getRefreshTasksCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening task dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.task.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete task button click.
     */
    @FXML
    private void onDeleteTaskAction() {
        if (viewModel.getSelectedTask() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.task"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("task.delete.confirm") + 
            " '" + viewModel.getSelectedTask().getTitle() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteTaskCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshTasksCommand().execute();
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
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
}