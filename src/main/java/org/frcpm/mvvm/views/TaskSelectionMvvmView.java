// src/main/java/org/frcpm/mvvm/views/TaskSelectionMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.TaskSelectionMvvmViewModel;

/**
 * View for task selection dialog using MVVMFx.
 */
public class TaskSelectionMvvmView implements FxmlView<TaskSelectionMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TaskSelectionMvvmView.class.getName());
    
    @FXML
    private TableView<Task> tasksTable;
    
    @FXML
    private TableColumn<Task, String> titleColumn;
    
    @FXML
    private TableColumn<Task, String> subsystemColumn;
    
    @FXML
    private TableColumn<Task, Integer> progressColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> startDateColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> endDateColumn;
    
    @FXML
    private Button selectButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private TaskSelectionMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    private Task selectedTask;
    private boolean taskWasSelected = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TaskSelectionMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up tasks table
        tasksTable.setItems(viewModel.getTasks());
        
        // Bind selected task
        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedTask(newVal);
            this.selectedTask = newVal;
        });
        
        // Set up row double-click handler
        tasksTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Task> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleSelectTask();
                }
            });
            return row;
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(selectButton, viewModel.getSelectTaskCommand());
        CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
        
        // Override button actions
        selectButton.setOnAction(e -> handleSelectTask());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        subsystemColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    task.getSubsystem() != null ? task.getSubsystem().getName() : "");
        });
        
        // Set up the progress column with progress bars
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progressColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Task, Integer>() {
            private final javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();

            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress / 100.0);
                    progressBar.setPrefWidth(80);
                    setText(progress + "%");
                    setGraphic(progressBar);
                }
            }
        });
        
        // Set up date formatters
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
    }
    
    /**
     * Initializes the view with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadTasksCommand().execute();
    }
    
    /**
     * Handles the select task button click.
     */
    private void handleSelectTask() {
        if (selectedTask == null) {
            return;
        }
        
        taskWasSelected = true;
        closeDialog();
    }
    
    /**
     * Handles the cancel button click.
     */
    private void handleCancel() {
        taskWasSelected = false;
        closeDialog();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (selectButton != null && selectButton.getScene() != null && 
                selectButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
                Stage stage = (Stage) selectButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Gets whether a task was selected.
     * 
     * @return true if a task was selected, false if canceled
     */
    public boolean wasTaskSelected() {
        return taskWasSelected;
    }
    
    /**
     * Gets the selected task.
     * 
     * @return the selected task
     */
    public Task getSelectedTask() {
        return selectedTask;
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public TaskSelectionMvvmViewModel getViewModel() {
        return viewModel;
    }
}