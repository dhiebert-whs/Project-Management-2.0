// src/main/java/org/frcpm/mvvm/views/DashboardMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.DashboardMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;
import org.frcpm.mvvm.views.TaskDetailMvvmView;

/**
 * View for the Dashboard using MVVMFx.
 */
public class DashboardMvvmView implements FxmlView<DashboardMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardMvvmView.class.getName());
    
    // FXML UI components - Charts
    @FXML private PieChart taskStatusChart;
    @FXML private LineChart<Number, Number> progressChart;
    
    // FXML UI components - Project info
    @FXML private Label projectNameLabel;
    @FXML private Label startDateLabel;
    @FXML private Label goalDateLabel;
    @FXML private Label deadlineLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressPercentLabel;
    @FXML private Label daysRemainingLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    // FXML UI components - Tables
    @FXML private TableView<Task> upcomingTasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, LocalDate> taskDueDateColumn;
    @FXML private TableColumn<Task, Integer> taskProgressColumn;
    
    @FXML private TableView<Milestone> upcomingMilestonesTable;
    @FXML private TableColumn<Milestone, String> milestoneNameColumn;
    @FXML private TableColumn<Milestone, LocalDate> milestoneDateColumn;
    
    @FXML private TableView<Meeting> upcomingMeetingsTable;
    @FXML private TableColumn<Meeting, LocalDate> meetingDateColumn;
    @FXML private TableColumn<Meeting, String> meetingTimeColumn;
    @FXML private TableColumn<Meeting, String> meetingTitleColumn;
    
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private DashboardMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing DashboardMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTasksTable();
        setupMilestonesTable();
        setupMeetingsTable();
        
        // Set up bindings
        setupBindings();
        
        // Set up error handling
        setupErrorHandling();
    }
    
    /**
     * Sets up the tasks table columns.
     */
    private void setupTasksTable() {
        if (upcomingTasksTable == null || taskTitleColumn == null || 
            taskDueDateColumn == null || taskProgressColumn == null) {
            LOGGER.warning("Tasks table components not initialized - likely in test environment");
            return;
        }

        // Setup task title column
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Setup due date column with formatter
        taskDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        taskDueDateColumn.setCellFactory(column -> new TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        // Setup progress column with progress bar
        taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        taskProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
            private final ProgressBar progressBar = new ProgressBar();
            
            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress / 100.0);
                    progressBar.setPrefWidth(60);
                    setText(progress + "%");
                    setGraphic(progressBar);
                }
            }
        });
        
        // Setup row double-click handler for editing
        upcomingTasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditTask(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the milestones table columns.
     */
    private void setupMilestonesTable() {
        if (upcomingMilestonesTable == null || milestoneNameColumn == null || milestoneDateColumn == null) {
            LOGGER.warning("Milestones table components not initialized - likely in test environment");
            return;
        }

        // Setup milestone name column
        milestoneNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Setup milestone date column with formatter
        milestoneDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        milestoneDateColumn.setCellFactory(column -> new TableCell<Milestone, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
    }
    
// Continuing src/main/java/org/frcpm/mvvm/views/DashboardMvvmView.java
    /**
     * Sets up the meetings table columns.
     */
    private void setupMeetingsTable() {
        if (upcomingMeetingsTable == null || meetingDateColumn == null || 
            meetingTimeColumn == null || meetingTitleColumn == null) {
            LOGGER.warning("Meetings table components not initialized - likely in test environment");
            return;
        }

        // Setup meeting title column
        meetingTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Setup meeting date column with formatter
        meetingDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        meetingDateColumn.setCellFactory(column -> new TableCell<Meeting, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        // Setup meeting time column
        meetingTimeColumn.setCellValueFactory(cellData -> {
            Meeting meeting = cellData.getValue();
            String startTime = meeting.getStartTime() != null ? meeting.getStartTime().toString() : "";
            String endTime = meeting.getEndTime() != null ? meeting.getEndTime().toString() : "";
            return new javafx.beans.property.SimpleStringProperty(startTime + " - " + endTime);
        });
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        try {
            // Check for null UI components for testability
            if (projectNameLabel == null || startDateLabel == null || goalDateLabel == null ||
                deadlineLabel == null || overallProgressBar == null || progressPercentLabel == null ||
                daysRemainingLabel == null || upcomingTasksTable == null || 
                upcomingMilestonesTable == null || upcomingMeetingsTable == null || refreshButton == null ||
                loadingIndicator == null) {
                LOGGER.warning("UI components not initialized - likely in test environment");
                return;
            }

            // Bind project info labels
            projectNameLabel.textProperty().bind(viewModel.projectNameProperty());
            
            // Format dates for the labels
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            startDateLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getStartDate() != null ? 
                          viewModel.getStartDate().format(dateFormatter) : "",
                    viewModel.startDateProperty()
                )
            );
            
            goalDateLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getGoalEndDate() != null ? 
                          viewModel.getGoalEndDate().format(dateFormatter) : "",
                    viewModel.goalEndDateProperty()
                )
            );
            
            deadlineLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getHardDeadline() != null ? 
                          viewModel.getHardDeadline().format(dateFormatter) : "",
                    viewModel.hardDeadlineProperty()
                )
            );
            
            // Bind progress indicators
            overallProgressBar.progressProperty().bind(viewModel.progressPercentageProperty().divide(100.0));
            progressPercentLabel.textProperty().bind(
                javafx.beans.binding.Bindings.format("%.1f%%", viewModel.progressPercentageProperty())
            );
            
            daysRemainingLabel.textProperty().bind(
                javafx.beans.binding.Bindings.format("%d days remaining", viewModel.daysRemainingProperty())
            );
            
            // Bind tables to ViewModel lists
            upcomingTasksTable.setItems(viewModel.getUpcomingTasks());
            upcomingMilestonesTable.setItems(viewModel.getUpcomingMilestones());
            upcomingMeetingsTable.setItems(viewModel.getUpcomingMeetings());
            
            // Bind selection changes
            upcomingTasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
                    
            upcomingMilestonesTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMilestone(newVal));
                    
            upcomingMeetingsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMeeting(newVal));
            
            // Bind refresh button to refresh command
            CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Bind loading indicator
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            
            // Setup charts
            setupCharts();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert(resources.getString("error.title"), 
                          resources.getString("setup.bindings.failed") + " " + e.getMessage());
        }
    }
    
    /**
     * Sets up the charts.
     */
    private void setupCharts() {
        if (taskStatusChart == null || progressChart == null) {
            LOGGER.warning("Chart components not initialized - likely in test environment");
            return;
        }

        // Bind task status chart data
        taskStatusChart.setData(viewModel.getTaskStatusChartData());
        taskStatusChart.setTitle(resources.getString("chart.task.status"));
        taskStatusChart.setLabelsVisible(true);
        taskStatusChart.setAnimated(false);
        
        // Bind progress chart data
        progressChart.setTitle(resources.getString("chart.progress.time"));
        progressChart.setAnimated(false);
        progressChart.getData().clear();
        progressChart.setData(viewModel.getProgressChartData());
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (errorLabel == null) {
            LOGGER.warning("Error label not initialized - likely in test environment");
            return;
        }
        
        // Bind error label to view model error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
    }
    
    /**
     * Sets the current project.
     * This method would be called from the main controller.
     * 
     * @param project the project to set
     */
    public void setProject(Project project) {
        viewModel.setProject(project);
    }
    
    /**
     * Handles editing a task.
     * 
     * @param task the task to edit
     */
    private void handleEditTask(Task task) {
        if (task == null) {
            return;
        }
        
        try {
            // Use MVVMFx ViewLoader to load the task detail view
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the view controller
            TaskDetailMvvmView taskDetailView = viewTuple.getCodeBehind();
            
            // Initialize the view with the existing task
            taskDetailView.initExistingTask(task);
            
            // Create a dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(upcomingTasksTable.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh data after dialog closes
            viewModel.getRefreshCommand().execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            showErrorAlert(resources.getString("error.title"), 
                          resources.getString("error.task.dialog.failed") + e.getMessage());
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Gets the view model.
     * This method is useful for testing.
     * 
     * @return the view model
     */
    public DashboardMvvmViewModel getViewModel() {
        return viewModel;
    }
}