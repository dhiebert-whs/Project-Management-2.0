package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.MeetingService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the dashboard view in the FRC Project Management System.
 * Shows overview of project progress, upcoming milestones, recent activity, etc.
 * Follows the MVVM pattern to separate business logic from UI.
 */
public class DashboardViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardViewModel.class.getName());
    
    // Maximum number of items to show in lists
    private static final int MAX_UPCOMING_TASKS = 5;
    private static final int MAX_UPCOMING_MILESTONES = 5;
    private static final int MAX_UPCOMING_MEETINGS = 5;
    
    // Services
    private final TaskService taskService;
    private final MilestoneService milestoneService;
    private final MeetingService meetingService;
    
    // Observable properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final StringProperty projectName = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> goalEndDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> hardDeadline = new SimpleObjectProperty<>();
    private final DoubleProperty progressPercentage = new SimpleDoubleProperty(0);
    private final IntegerProperty daysRemaining = new SimpleIntegerProperty(0);
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    
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
    private final Command refreshCommand;
    
    /**
     * Creates a new DashboardViewModel with the specified services.
     * 
     * @param taskService the task service
     * @param milestoneService the milestone service
     * @param meetingService the meeting service
     */
    public DashboardViewModel(TaskService taskService, MilestoneService milestoneService, MeetingService meetingService) {
        if (taskService == null) {
            throw new IllegalArgumentException("Task service cannot be null");
        }
        if (milestoneService == null) {
            throw new IllegalArgumentException("Milestone service cannot be null");
        }
        if (meetingService == null) {
            throw new IllegalArgumentException("Meeting service cannot be null");
        }
        
        this.taskService = taskService;
        this.milestoneService = milestoneService;
        this.meetingService = meetingService;
        
        // Initialize commands using BaseViewModel helper methods
        refreshCommand = createValidOnlyCommand(this::refreshDashboard, () -> project.get() != null);
        
        // Set up property listeners
        setupPropertyListeners();
        
        // Initialize chart data
        initializeChartData();
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
        valid.set(isValid);
        
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
        // Using competition deadline as the hard deadline
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
     * Refreshes all dashboard data.
     */
    public void refreshDashboard() {
        Project currentProject = project.get();
        if (currentProject == null) {
            LOGGER.warning("Cannot refresh dashboard for null project");
            return;
        }
        
        try {
            // Update project info (in case it changed)
            currentProject = refreshProject(currentProject);
            updateProjectInfo(currentProject);
            
            // Load upcoming tasks
            loadUpcomingTasks(currentProject);
            
            // Load upcoming milestones
            loadUpcomingMilestones(currentProject);
            
            // Load upcoming meetings
            loadUpcomingMeetings(currentProject);
            
            // Update charts
            updateCharts(currentProject);
            
            // Calculate overall progress
            calculateOverallProgress(currentProject);
            
            // Clear error message on successful refresh
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing dashboard", e);
            setErrorMessage("Failed to refresh dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Refreshes the project from the database.
     * 
     * @param project the project to refresh
     * @return the refreshed project
     */
    private Project refreshProject(Project project) {
        try {
            // This would use a project service to refresh the project
            // For now, we'll just return the project as is
            return project;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error refreshing project", e);
            return project;
        }
    }
    
    /**
     * Loads upcoming tasks for the project.
     * 
     * @param project the project
     */
    private void loadUpcomingTasks(Project project) {
        try {
            LocalDate today = LocalDate.now();
            List<Task> allTasks = taskService.findByProject(project);
            
            // Filter to incomplete tasks that are due after today
            List<Task> upcomingTasksList = allTasks.stream()
                .filter(task -> !task.isCompleted() && task.getEndDate() != null && 
                       !task.getEndDate().isBefore(today))
                .sorted((t1, t2) -> t1.getEndDate().compareTo(t2.getEndDate()))
                .limit(MAX_UPCOMING_TASKS)
                .toList();
                
            upcomingTasks.setAll(upcomingTasksList);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading upcoming tasks", e);
            upcomingTasks.clear();
        }
    }
    
    /**
     * Loads upcoming milestones for the project.
     * 
     * @param project the project
     */
    private void loadUpcomingMilestones(Project project) {
        try {
            // Since we don't have a dedicated method, we're getting all and filtering
            List<Milestone> allMilestones = milestoneService.findByProject(project);
            LocalDate today = LocalDate.now();
            
            List<Milestone> upcomingMilestonesList = allMilestones.stream()
                .filter(m -> m.getDate() != null && !m.getDate().isBefore(today))
                .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                .limit(MAX_UPCOMING_MILESTONES)
                .toList();
                
            upcomingMilestones.setAll(upcomingMilestonesList);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading upcoming milestones", e);
            upcomingMilestones.clear();
        }
    }
    
    /**
     * Loads upcoming meetings for the project.
     * 
     * @param project the project
     */
    private void loadUpcomingMeetings(Project project) {
        try {
            // Using available methods to get upcoming meetings
            if (project.getId() != null) {
                List<Meeting> meetings = meetingService.getUpcomingMeetings(project.getId(), 30); // 30 days ahead
                
                // Sort by date and limit
                List<Meeting> upcomingMeetingsList = meetings.stream()
                    .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                    .limit(MAX_UPCOMING_MEETINGS)
                    .toList();
                    
                upcomingMeetings.setAll(upcomingMeetingsList);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading upcoming meetings", e);
            upcomingMeetings.clear();
        }
    }
    
    /**
     * Updates chart data.
     * 
     * @param project the project
     */
    private void updateCharts(Project project) {
        try {
            // Update task status chart
            updateTaskStatusChart(project);
            
            // Update progress chart
            updateProgressChart(project);
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
     * Updates the task status pie chart.
     * 
     * @param project the project
     */
    private void updateTaskStatusChart(Project project) {
        try {
            // Get counts of tasks by status
            Map<TaskStatusCategory, Integer> statusCounts = getTaskStatusCounts(project);
            
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
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating task status chart", e);
        }
    }
    
    /**
     * Gets counts of tasks by status.
     * 
     * @param project the project
     * @return map of task status to count
     */
    private Map<TaskStatusCategory, Integer> getTaskStatusCounts(Project project) {
        Map<TaskStatusCategory, Integer> counts = new HashMap<>();
        counts.put(TaskStatusCategory.NOT_STARTED, 0);
        counts.put(TaskStatusCategory.IN_PROGRESS, 0);
        counts.put(TaskStatusCategory.COMPLETED, 0);
        
        try {
            // Get task counts by status from service
            List<Task> tasks = taskService.findByProject(project);
            
            for (Task task : tasks) {
                TaskStatusCategory status;
                
                if (task.isCompleted()) {
                    status = TaskStatusCategory.COMPLETED;
                } else if (task.getProgress() > 0) {
                    status = TaskStatusCategory.IN_PROGRESS;
                } else {
                    status = TaskStatusCategory.NOT_STARTED;
                }
                
                counts.put(status, counts.getOrDefault(status, 0) + 1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting task status counts", e);
        }
        
        return counts;
    }
    
    /**
     * Updates the progress chart.
     * 
     * @param project the project
     */
    private void updateProgressChart(Project project) {
        try {
            // Get progress data from service
            // For now, we'll just add some sample data
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
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating progress chart", e);
        }
    }
    
    /**
     * Calculates the overall progress percentage.
     * 
     * @param project the project
     */
    private void calculateOverallProgress(Project project) {
        try {
            // Calculate progress manually based on task progress
            List<Task> tasks = taskService.findByProject(project);
            
            if (tasks.isEmpty()) {
                progressPercentage.set(0);
                return;
            }
            
            // Sum up progress of all tasks
            double totalProgress = 0;
            for (Task task : tasks) {
                totalProgress += task.getProgress();
            }
            
            // Calculate average progress
            double averageProgress = totalProgress / tasks.size();
            progressPercentage.set(averageProgress);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating overall progress", e);
            progressPercentage.set(0);
        }
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
    public void cleanupResources() {
        super.cleanupResources();
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
    
    public BooleanProperty validProperty() {
        return valid;
    }
    
    public boolean isValid() {
        return valid.get();
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