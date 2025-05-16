// src/main/java/org/frcpm/mvvm/viewmodels/DashboardMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.async.TaskExecutor;
import org.frcpm.models.*;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.MeetingService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;
import org.frcpm.services.impl.MilestoneServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the Dashboard view using MVVMFx.
 * Shows overview of project progress, upcoming milestones, recent activity, etc.
 */
public class DashboardMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardMvvmViewModel.class.getName());
    
    // Maximum number of items to show in lists
    private static final int MAX_UPCOMING_TASKS = 5;
    private static final int MAX_UPCOMING_MILESTONES = 5;
    private static final int MAX_UPCOMING_MEETINGS = 5;
    
    // Services
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final MilestoneService milestoneService;
    private final MilestoneServiceAsyncImpl milestoneServiceAsync;
    private final MeetingService meetingService;
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    
    // Observable properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final StringProperty projectName = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> goalEndDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> hardDeadline = new SimpleObjectProperty<>();
    private final DoubleProperty progressPercentage = new SimpleDoubleProperty(0);
    private final IntegerProperty daysRemaining = new SimpleIntegerProperty(0);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Observable lists
    private final ObservableList<Task> upcomingTasks = FXCollections.observableArrayList();
    private final ObservableList<Milestone> upcomingMilestones = FXCollections.observableArrayList();
    private final ObservableList<Meeting> upcomingMeetings = FXCollections.observableArrayList();
    
    // Selected items
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final ObjectProperty<Milestone> selectedMilestone = new SimpleObjectProperty<>();
    private final ObjectProperty<Meeting> selectedMeeting = new SimpleObjectProperty<>();
    
    // Chart data
    private final ObservableList<PieChart.Data> taskStatusChartData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Series<Number, Number>> progressChartData = FXCollections.observableArrayList();
    
    // Commands
    private Command refreshCommand;
    
    /**
     * Creates a new DashboardMvvmViewModel with the specified services.
     * 
     * @param taskService the task service
     * @param milestoneService the milestone service
     * @param meetingService the meeting service
     */
   
    public DashboardMvvmViewModel(TaskService taskService, MilestoneService milestoneService, MeetingService meetingService) {
        this.taskService = taskService;
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        this.milestoneService = milestoneService;
        this.milestoneServiceAsync = (MilestoneServiceAsyncImpl) milestoneService;
        this.meetingService = meetingService;
        this.meetingServiceAsync = (MeetingServiceAsyncImpl) meetingService;
        
        // Initialize commands
        initializeCommands();
        
        // Set up property listeners
        setupPropertyListeners();
        
        // Initialize chart data
        initializeChartData();
    }
    
    /**
     * Initializes commands.
     */
    private void initializeCommands() {
        // Refresh command - using MvvmAsyncHelper to create an async command
        refreshCommand = MvvmAsyncHelper.createAsyncCommand(
                // Return a CompletableFuture
                () -> TaskExecutor.executeAsync(
                    "RefreshDashboard",
                    () -> {
                        refreshDashboardAsync();
                        return null;
                    },
                    null,
                    null
                ),
                // Success handler
                result -> {
                    // No additional handling needed, everything is done in refreshDashboardAsync
                },
                // Error handler
                error -> {
                    LOGGER.log(Level.SEVERE, "Error refreshing dashboard", error);
                    setErrorMessage("Failed to refresh dashboard: " + error.getMessage());
                },
                // Loading property
                loading
            );
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Create validation handler
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        
        // Listen for project changes to refresh dashboard
        project.addListener((obs, oldProject, newProject) -> {
            if (newProject != null) {
                updateProjectInfo(newProject);
                refreshCommand.execute();
                validationHandler.run();
            } else {
                clearData();
            }
        });
        
        // Listen for task selection changes
        selectedTask.addListener((obs, oldTask, newTask) -> {
            validationHandler.run();
        });
        
        // Listen for milestone selection changes
        selectedMilestone.addListener((obs, oldMilestone, newMilestone) -> {
            validationHandler.run();
        });
        
        // Listen for meeting selection changes
        selectedMeeting.addListener((obs, oldMeeting, newMeeting) -> {
            validationHandler.run();
        });
        
        // Track the listener
        trackPropertyListener(validationHandler);
    }
    
    /**
     * Validates the current state.
     */
    private void validate() {
        boolean isValid = project.get() != null;
        
        if (!isValid) {
            setErrorMessage("No project selected");
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Updates project information from the project model.
     * 
     * @param project the project to update from
     */
    private void updateProjectInfo(Project project) {
        if (project == null) {
            return;
        }
        
        projectName.set(project.getName());
        startDate.set(project.getStartDate());
        goalEndDate.set(project.getGoalEndDate());
        hardDeadline.set(project.getHardDeadline());
        
        // Calculate days remaining
        LocalDate today = LocalDate.now();
        LocalDate endDate = project.getGoalEndDate();
        if (endDate != null && !endDate.isBefore(today)) {
            daysRemaining.set((int) ChronoUnit.DAYS.between(today, endDate));
        } else {
            daysRemaining.set(0);
        }
    }
    
    /**
     * Initializes chart data structures.
     */
    private void initializeChartData() {
        // Initialize task status chart data
        taskStatusChartData.clear();
        taskStatusChartData.addAll(
            new PieChart.Data("Not Started", 0),
            new PieChart.Data("In Progress", 0),
            new PieChart.Data("Completed", 0)
        );
        
        // Initialize progress chart data
        progressChartData.clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Progress");
        progressChartData.add(series);
    }
    
    /**
     * Refreshes all dashboard data asynchronously.
     */
    private void refreshDashboardAsync() {
        Project currentProject = project.get();
        if (currentProject == null) {
            LOGGER.warning("Cannot refresh dashboard for null project");
            return;
        }
        
        //loading.set(true);
        
        try {
            // Load upcoming tasks asynchronously
            loadUpcomingTasksAsync(currentProject);
            
            // Load upcoming milestones asynchronously
            loadUpcomingMilestonesAsync(currentProject);
            
            // Load upcoming meetings asynchronously
            loadUpcomingMeetingsAsync(currentProject);
            
            // Update charts asynchronously
            updateChartsAsync(currentProject);
            
            // Calculate overall progress asynchronously
            calculateOverallProgressAsync(currentProject);
            
            // Clear error message on successful refresh
            clearErrorMessage();
        } catch (Exception e) {
            Platform.runLater(() -> {
                LOGGER.log(Level.SEVERE, "Error refreshing dashboard", e);
                setErrorMessage("Failed to refresh dashboard: " + e.getMessage());
                loading.set(false);
            });
        }
    }
    
    /**
     * Loads upcoming tasks for the project asynchronously.
     * 
     * @param project the project
     */
    private void loadUpcomingTasksAsync(Project project) {
        LocalDate today = LocalDate.now();
        
        taskServiceAsync.findByProjectAsync(
            project,
            // Success handler
            tasks -> {
                Platform.runLater(() -> {
                    // Filter to incomplete tasks that are due after today
                    List<Task> upcomingTasksList = tasks.stream()
                        .filter(task -> !task.isCompleted() && task.getEndDate() != null && 
                               !task.getEndDate().isBefore(today))
                        .sorted((t1, t2) -> t1.getEndDate().compareTo(t2.getEndDate()))
                        .limit(MAX_UPCOMING_TASKS)
                        .toList();
                        
                    upcomingTasks.setAll(upcomingTasksList);
                    LOGGER.info("Loaded " + upcomingTasksList.size() + " upcoming tasks");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error loading upcoming tasks", error);
                    setErrorMessage("Failed to load upcoming tasks: " + error.getMessage());
                    upcomingTasks.clear();
                });
            }
        );
    }
    
    /**
     * Loads upcoming milestones for the project asynchronously.
     * 
     * @param project the project
     */
    private void loadUpcomingMilestonesAsync(Project project) {
        LocalDate today = LocalDate.now();
        
        milestoneServiceAsync.findByProjectAsync(
            project,
            // Success handler
            milestones -> {
                Platform.runLater(() -> {
                    List<Milestone> upcomingMilestonesList = milestones.stream()
                        .filter(m -> m.getDate() != null && !m.getDate().isBefore(today))
                        .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                        .limit(MAX_UPCOMING_MILESTONES)
                        .toList();
                        
                    upcomingMilestones.setAll(upcomingMilestonesList);
                    LOGGER.info("Loaded " + upcomingMilestonesList.size() + " upcoming milestones");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error loading upcoming milestones", error);
                    setErrorMessage("Failed to load upcoming milestones: " + error.getMessage());
                    upcomingMilestones.clear();
                });
            }
        );
    }
    
    /**
     * Loads upcoming meetings for the project asynchronously.
     * 
     * @param project the project
     */
    private void loadUpcomingMeetingsAsync(Project project) {
        if (project.getId() == null) {
            return;
        }
        
        meetingServiceAsync.getUpcomingMeetingsAsync(
            project.getId(), 
            30, // 30 days ahead
            // Success handler
            meetings -> {
                Platform.runLater(() -> {
                    // Sort by date and limit
                    List<Meeting> upcomingMeetingsList = meetings.stream()
                        .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                        .limit(MAX_UPCOMING_MEETINGS)
                        .toList();
                        
                    upcomingMeetings.setAll(upcomingMeetingsList);
                    LOGGER.info("Loaded " + upcomingMeetingsList.size() + " upcoming meetings");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error loading upcoming meetings", error);
                    setErrorMessage("Failed to load upcoming meetings: " + error.getMessage());
                    upcomingMeetings.clear();
                });
            }
        );
    }
    
    /**
     * Updates chart data asynchronously.
     * 
     * @param project the project
     */
    private void updateChartsAsync(Project project) {
        try {
            // Update task status chart
            updateTaskStatusChartAsync(project);
            
            // Update progress chart
            updateProgressChartAsync(project);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating charts", e);
        }
    }
    
    /**
     * Enum to represent task status categories
     */
    private enum TaskStatusCategory {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
    
    /**
     * Updates the task status pie chart asynchronously.
     * 
     * @param project the project
     */
    private void updateTaskStatusChartAsync(Project project) {
        taskServiceAsync.findByProjectAsync(
            project,
            // Success handler
            tasks -> {
                Platform.runLater(() -> {
                    Map<TaskStatusCategory, Integer> statusCounts = new HashMap<>();
                    statusCounts.put(TaskStatusCategory.NOT_STARTED, 0);
                    statusCounts.put(TaskStatusCategory.IN_PROGRESS, 0);
                    statusCounts.put(TaskStatusCategory.COMPLETED, 0);
                    
                    for (Task task : tasks) {
                        TaskStatusCategory status;
                        
                        if (task.isCompleted()) {
                            status = TaskStatusCategory.COMPLETED;
                        } else if (task.getProgress() > 0) {
                            status = TaskStatusCategory.IN_PROGRESS;
                        } else {
                            status = TaskStatusCategory.NOT_STARTED;
                        }
                        
                        statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
                    }
                    
                    // Update chart data
                    for (PieChart.Data data : taskStatusChartData) {
                        String status = data.getName();
                        int count = 0;
                        
                        if (status.equals("Not Started")) {
                            count = statusCounts.getOrDefault(TaskStatusCategory.NOT_STARTED, 0);
                        } else if (status.equals("In Progress")) {
                            count = statusCounts.getOrDefault(TaskStatusCategory.IN_PROGRESS, 0);
                        } else if (status.equals("Completed")) {
                            count = statusCounts.getOrDefault(TaskStatusCategory.COMPLETED, 0);
                        }
                        
                        data.setPieValue(count);
                    }
                    
                    LOGGER.info("Updated task status chart");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error updating task status chart", error);
                    setErrorMessage("Failed to update task status chart: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Updates the progress chart asynchronously.
     * 
     * @param project the project
     */
    private void updateProgressChartAsync(Project project) {
        // For now, we'll just add some sample data asynchronously
        // In a real implementation, this would fetch actual historical data
        Platform.runLater(() -> {
            XYChart.Series<Number, Number> series = progressChartData.get(0);
            series.getData().clear();
            
            // Sample data - this would be replaced with actual data from the service
            series.getData().add(new XYChart.Data<>(0, 0));
            series.getData().add(new XYChart.Data<>(7, 10));
            series.getData().add(new XYChart.Data<>(14, 25));
            series.getData().add(new XYChart.Data<>(21, 40));
            series.getData().add(new XYChart.Data<>(28, 60));
            series.getData().add(new XYChart.Data<>(35, 75));
            series.getData().add(new XYChart.Data<>(42, 80));
            
            LOGGER.info("Updated progress chart");
        });
    }
    
    /**
     * Calculates the overall progress percentage asynchronously.
     * 
     * @param project the project
     */
    private void calculateOverallProgressAsync(Project project) {
        taskServiceAsync.findByProjectAsync(
            project,
            // Success handler
            tasks -> {
                Platform.runLater(() -> {
                    if (tasks.isEmpty()) {
                        progressPercentage.set(0);
                    } else {
                        // Sum up progress of all tasks
                        double totalProgress = 0;
                        for (Task task : tasks) {
                            totalProgress += task.getProgress();
                        }
                        
                        // Calculate average progress
                        double averageProgress = totalProgress / tasks.size();
                        progressPercentage.set(averageProgress);
                    }
                    
                    LOGGER.info("Calculated overall progress: " + progressPercentage.get() + "%");
                    
                    // Now that everything is loaded, set loading to false
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error calculating overall progress", error);
                    setErrorMessage("Failed to calculate overall progress: " + error.getMessage());
                    progressPercentage.set(0);
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Clears all dashboard data.
     */
    private void clearData() {
        projectName.set("");
        startDate.set(null);
        goalEndDate.set(null);
        hardDeadline.set(null);
        progressPercentage.set(0);
        daysRemaining.set(0);
        
        upcomingTasks.clear();
        upcomingMilestones.clear();
        upcomingMeetings.clear();
        
        selectedTask.set(null);
        selectedMilestone.set(null);
        selectedMeeting.set(null);
        
        // Reset chart data
        for (PieChart.Data data : taskStatusChartData) {
            data.setPieValue(0);
        }
        
        XYChart.Series<Number, Number> series = progressChartData.get(0);
        series.getData().clear();
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        upcomingTasks.clear();
        upcomingMilestones.clear();
        upcomingMeetings.clear();
        
        selectedTask.set(null);
        selectedMilestone.set(null);
        selectedMeeting.set(null);
        
        taskStatusChartData.clear();
        progressChartData.clear();
    }
    
    // Getters and setters
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    public StringProperty projectNameProperty() {
        return projectName;
    }
    
    public String getProjectName() {
        return projectName.get();
    }
    
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    public ObjectProperty<LocalDate> goalEndDateProperty() {
        return goalEndDate;
    }
    
    public LocalDate getGoalEndDate() {
        return goalEndDate.get();
    }
    
    public ObjectProperty<LocalDate> hardDeadlineProperty() {
        return hardDeadline;
    }
    
    public LocalDate getHardDeadline() {
        return hardDeadline.get();
    }
    
    public DoubleProperty progressPercentageProperty() {
        return progressPercentage;
    }
    
    public double getProgressPercentage() {
        return progressPercentage.get();
    }
    
    public IntegerProperty daysRemainingProperty() {
        return daysRemaining;
    }
    
    public int getDaysRemaining() {
        return daysRemaining.get();
    }
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public boolean isLoading() {
        return loading.get();
    }
    
    public ObservableList<Task> getUpcomingTasks() {
        return upcomingTasks;
    }
    
    public ObservableList<Milestone> getUpcomingMilestones() {
        return upcomingMilestones;
    }
    
    public ObservableList<Meeting> getUpcomingMeetings() {
        return upcomingMeetings;
    }
    
    public ObjectProperty<Task> selectedTaskProperty() {
        return selectedTask;
    }
    
    public Task getSelectedTask() {
        return selectedTask.get();
    }
    
    public void setSelectedTask(Task task) {
        selectedTask.set(task);
    }
    
    public ObjectProperty<Milestone> selectedMilestoneProperty() {
        return selectedMilestone;
    }
    
    public Milestone getSelectedMilestone() {
        return selectedMilestone.get();
    }
    
    public void setSelectedMilestone(Milestone milestone) {
        selectedMilestone.set(milestone);
    }
    
    public ObjectProperty<Meeting> selectedMeetingProperty() {
        return selectedMeeting;
    }
    
    public Meeting getSelectedMeeting() {
        return selectedMeeting.get();
    }
    
    public void setSelectedMeeting(Meeting meeting) {
        selectedMeeting.set(meeting);
    }
    
    public ObservableList<PieChart.Data> getTaskStatusChartData() {
        return taskStatusChartData;
    }
    
    public ObservableList<XYChart.Series<Number, Number>> getProgressChartData() {
        return progressChartData;
    }
    
    public Command getRefreshCommand() {
        return refreshCommand;
    }
}