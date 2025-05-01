package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.DashboardViewModel;
import org.frcpm.views.TaskView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the dashboard view using AfterburnerFX pattern.
 * Shows overview of project progress, upcoming milestones, recent activity, etc.
 */
public class DashboardPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardPresenter.class.getName());

    // FXML UI components - Charts
    @FXML
    private PieChart taskStatusChart;

    @FXML
    private LineChart<Number, Number> progressChart;

    @FXML
    private VBox chartsContainer;

    // FXML UI components - Project info
    @FXML
    private Label projectNameLabel;

    @FXML
    private Label startDateLabel;

    @FXML
    private Label goalDateLabel;

    @FXML
    private Label deadlineLabel;

    @FXML
    private ProgressBar overallProgressBar;

    @FXML
    private Label progressPercentLabel;

    @FXML
    private Label daysRemainingLabel;

    // FXML UI components - Tables
    @FXML
    private TableView<Task> upcomingTasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableView<Milestone> upcomingMilestonesTable;

    @FXML
    private TableColumn<Milestone, String> milestoneNameColumn;

    @FXML
    private TableColumn<Milestone, LocalDate> milestoneDateColumn;

    @FXML
    private TableView<Meeting> upcomingMeetingsTable;

    @FXML
    private TableColumn<Meeting, LocalDate> meetingDateColumn;

    @FXML
    private TableColumn<Meeting, String> meetingTimeColumn;
    
    @FXML
    private TableColumn<Meeting, String> meetingTitleColumn;

    @FXML
    private Button refreshButton;

    // Injected services
    @Inject
    private TaskService taskService;
    
    @Inject
    private MilestoneService milestoneService;
    
    @Inject
    private MeetingService meetingService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel - now injected
    @Inject
    private DashboardViewModel viewModel;
    
    private ResourceBundle resources;
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing DashboardPresenter with resource bundle");
        
        this.resources = resources;
        
        // Check if we're in a testing environment and create fallbacks
        if (viewModel == null) {
            LOGGER.severe("ViewModel not injected - creating manually as fallback");
            viewModel = new DashboardViewModel(taskService, milestoneService, meetingService);
        }

        // Setup table columns
        setupTasksTable();
        setupMilestonesTable();
        setupMeetingsTable();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
    }

    /**
     * Sets up the tasks table columns.
     */
    private void setupTasksTable() {
        // Check for null UI components for testability
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
        // Check for null UI components for testability
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
    
    /**
     * Sets up the meetings table columns.
     */
    private void setupMeetingsTable() {
        // Check for null UI components for testability
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
        // Check for null UI components for testability
        if (projectNameLabel == null || startDateLabel == null || goalDateLabel == null ||
            deadlineLabel == null || overallProgressBar == null || progressPercentLabel == null ||
            daysRemainingLabel == null || upcomingTasksTable == null || 
            upcomingMilestonesTable == null || upcomingMeetingsTable == null || refreshButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
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
            
            // Bind refresh button
            ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Bind selection changes
            upcomingTasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
                    
            upcomingMilestonesTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMilestone(newVal));
                    
            upcomingMeetingsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMeeting(newVal));
            
            // Setup charts
            setupCharts();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
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
        taskStatusChart.setTitle("Task Status");
        taskStatusChart.setLabelsVisible(true);
        taskStatusChart.setAnimated(false);
        
        // Bind progress chart data
        progressChart.setTitle("Progress Over Time");
        progressChart.setAnimated(false);
        progressChart.getData().clear();
        progressChart.setData(viewModel.getProgressChartData());
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        // Show an alert when error message changes
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Sets the current project and loads dashboard data.
     * 
     * @param project the current project
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        this.currentProject = project;
        viewModel.setProject(project);
        viewModel.refreshDashboard();
    }
    
    /**
     * Refreshes dashboard data.
     */
    @FXML
    private void refreshData() {
        viewModel.refreshDashboard();
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
            // Use ViewLoader to show dialog
            TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                    resources.getString("task.edit.title"), 
                    upcomingTasksTable.getScene().getWindow());
            
            if (presenter != null) {
                presenter.initExistingTask(task);
                
                // Refresh data after dialog closes
                viewModel.refreshDashboard();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
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
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public DashboardViewModel getViewModel() {
        return viewModel;
    }
}