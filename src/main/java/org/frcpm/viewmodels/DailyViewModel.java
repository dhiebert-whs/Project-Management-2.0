package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the daily view in the FRC Project Management System.
 * Shows tasks and meetings scheduled for a selected date.
 * Follows the MVVM pattern to separate business logic from UI.
 */
public class DailyViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(DailyViewModel.class.getName());
    
    // Services
    private final TaskService taskService;
    private final MeetingService meetingService;
    
    // Observable properties
    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Meeting> meetings = FXCollections.observableArrayList();
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final ObjectProperty<Meeting> selectedMeeting = new SimpleObjectProperty<>();
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    
    // Commands for UI actions
    private final Command addTaskCommand;
    private final Command editTaskCommand;
    private final Command addMeetingCommand;
    private final Command editMeetingCommand;
    private final Command takeAttendanceCommand;
    private final Command refreshCommand;
    
    /**
     * Creates a new DailyViewModel with the specified services.
     * 
     * @param taskService the task service
     * @param meetingService the meeting service
     */
    public DailyViewModel(TaskService taskService, MeetingService meetingService) {
        if (taskService == null) {
            throw new IllegalArgumentException("Task service cannot be null");
        }
        if (meetingService == null) {
            throw new IllegalArgumentException("Meeting service cannot be null");
        }
        
        this.taskService = taskService;
        this.meetingService = meetingService;
        
        // Initialize commands using BaseViewModel helper methods
        addTaskCommand = createValidOnlyCommand(this::handleAddTask, this::canAddTask);
        editTaskCommand = createValidOnlyCommand(this::handleEditTask, this::canEditTask);
        addMeetingCommand = createValidOnlyCommand(this::handleAddMeeting, this::canAddMeeting);
        editMeetingCommand = createValidOnlyCommand(this::handleEditMeeting, this::canEditMeeting);
        takeAttendanceCommand = createValidOnlyCommand(this::handleTakeAttendance, this::canTakeAttendance);
        refreshCommand = createValidOnlyCommand(this::refreshData, () -> true);
        
        // Set up property listeners
        setupPropertyListeners();
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Create validation handler
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        
        // Listen for date changes to load data
        selectedDate.addListener((obs, oldDate, newDate) -> {
            if (newDate != null && project.get() != null) {
                loadDataForDate(newDate);
                validationHandler.run();
            }
        });
        
        // Listen for project changes to load data
        project.addListener((obs, oldProject, newProject) -> {
            if (newProject != null && selectedDate.get() != null) {
                loadDataForDate(selectedDate.get());
                validationHandler.run();
            }
        });
        
        // Listen for task selection changes
        selectedTask.addListener((obs, oldTask, newTask) -> {
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
     * Loads data for the specified date.
     * 
     * @param date the date to load data for
     */
    public void loadDataForDate(LocalDate date) {
        if (date == null) {
            LOGGER.warning("Cannot load data for null date");
            return;
        }
        
        Project currentProject = project.get();
        if (currentProject == null) {
            LOGGER.warning("Cannot load data for null project");
            return;
        }
        
        try {
            // Load tasks for the date
            List<Task> allProjectTasks = taskService.findByProject(currentProject);
            List<Task> tasksForDate = filterTasksByDate(allProjectTasks, date);
            tasks.setAll(tasksForDate);
            
            // Load meetings for the date
            List<Meeting> meetingsForDate = meetingService.findByDate(date);
            // Filter to only include meetings for this project
            meetingsForDate = meetingsForDate.stream()
                .filter(m -> m.getProject() != null && 
                        m.getProject().getId().equals(currentProject.getId()))
                .toList();
            meetings.setAll(meetingsForDate);
            
            // Clear selection
            selectedTask.set(null);
            selectedMeeting.set(null);
            
            // Clear error message on successful load
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading data for date: " + date, e);
            setErrorMessage("Failed to load data for the selected date: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to filter tasks by date.
     * 
     * @param tasks the list of tasks to filter
     * @param date the date to filter by
     * @return a list of tasks that fall on the specified date
     */
    private List<Task> filterTasksByDate(List<Task> tasks, LocalDate date) {
        return tasks.stream()
            .filter(task -> isTaskOnDate(task, date))
            .toList();
    }
    
    /**
     * Checks if a task falls on a specific date.
     * 
     * @param task the task to check
     * @param date the date to check against
     * @return true if the task falls on the date, false otherwise
     */
    private boolean isTaskOnDate(Task task, LocalDate date) {
        LocalDate startDate = task.getStartDate();
        LocalDate endDate = task.getEndDate();
        
        // If either date is null, can't determine
        if (startDate == null || endDate == null) {
            return false;
        }
        
        // Check if the date is within the task's date range (inclusive)
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Refreshes the data for the current date.
     */
    public void refreshData() {
        loadDataForDate(selectedDate.get());
    }
    
    // Command handlers
    
    /**
     * Handles adding a new task.
     * This will be delegated to the presenter.
     */
    private void handleAddTask() {
        LOGGER.info("Add Task action triggered");
        // No implementation needed - will be handled by presenter
    }
    
    /**
     * Handles editing a task.
     * This will be delegated to the presenter.
     */
    private void handleEditTask() {
        if (selectedTask.get() == null) {
            LOGGER.warning("No task selected to edit");
            return;
        }
        
        LOGGER.info("Edit Task action triggered for: " + selectedTask.get().getTitle());
        // No implementation needed - will be handled by presenter
    }
    
    /**
     * Handles adding a new meeting.
     * This will be delegated to the presenter.
     */
    private void handleAddMeeting() {
        LOGGER.info("Add Meeting action triggered");
        // No implementation needed - will be handled by presenter
    }
    
    /**
     * Handles editing a meeting.
     * This will be delegated to the presenter.
     */
    private void handleEditMeeting() {
        if (selectedMeeting.get() == null) {
            LOGGER.warning("No meeting selected to edit");
            return;
        }
        
        Meeting meeting = selectedMeeting.get();
        String meetingInfo = meeting.getDate() + " (" + meeting.getStartTime() + " - " + meeting.getEndTime() + ")";
        LOGGER.info("Edit Meeting action triggered for: " + meetingInfo);
        // No implementation needed - will be handled by presenter
    }
    
    /**
     * Handles taking attendance for a meeting.
     * This will be delegated to the presenter.
     */
    private void handleTakeAttendance() {
        if (selectedMeeting.get() == null) {
            LOGGER.warning("No meeting selected for attendance");
            return;
        }
        
        Meeting meeting = selectedMeeting.get();
        String meetingInfo = meeting.getDate() + " (" + meeting.getStartTime() + " - " + meeting.getEndTime() + ")";
        LOGGER.info("Take Attendance action triggered for: " + meetingInfo);
        // No implementation needed - will be handled by presenter
    }
    
    // Command condition methods
    
    /**
     * Checks if a task can be added.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canAddTask() {
        return project.get() != null;
    }
    
    /**
     * Checks if a task can be edited.
     * 
     * @return true if a task is selected, false otherwise
     */
    private boolean canEditTask() {
        return selectedTask.get() != null;
    }
    
    /**
     * Checks if a meeting can be added.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canAddMeeting() {
        return project.get() != null;
    }
    
    /**
     * Checks if a meeting can be edited.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canEditMeeting() {
        return selectedMeeting.get() != null;
    }
    
    /**
     * Checks if attendance can be taken for a meeting.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canTakeAttendance() {
        return selectedMeeting.get() != null;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        tasks.clear();
        meetings.clear();
        selectedTask.set(null);
        selectedMeeting.set(null);
    }
    
    // Getters and setters for properties
    
    public ObjectProperty<LocalDate> selectedDateProperty() {
        return selectedDate;
    }
    
    public LocalDate getSelectedDate() {
        return selectedDate.get();
    }
    
    public void setSelectedDate(LocalDate date) {
        selectedDate.set(date);
    }
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    public ObservableList<Task> getTasks() {
        return tasks;
    }
    
    public ObservableList<Meeting> getMeetings() {
        return meetings;
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
    
    public ObjectProperty<Meeting> selectedMeetingProperty() {
        return selectedMeeting;
    }
    
    public Meeting getSelectedMeeting() {
        return selectedMeeting.get();
    }
    
    public void setSelectedMeeting(Meeting meeting) {
        selectedMeeting.set(meeting);
    }
    
    public BooleanProperty validProperty() {
        return valid;
    }
    
    public boolean isValid() {
        return valid.get();
    }
    
    // Command getters
    
    public Command getAddTaskCommand() {
        return addTaskCommand;
    }
    
    public Command getEditTaskCommand() {
        return editTaskCommand;
    }
    
    public Command getAddMeetingCommand() {
        return addMeetingCommand;
    }
    
    public Command getEditMeetingCommand() {
        return editMeetingCommand;
    }
    
    public Command getTakeAttendanceCommand() {
        return takeAttendanceCommand;
    }
    
    public Command getRefreshCommand() {
        return refreshCommand;
    }
}