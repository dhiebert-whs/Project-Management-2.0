// src/main/java/org/frcpm/mvvm/viewmodels/DailyMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the Daily view using MVVMFx.
 * Displays tasks and meetings for a specific date.
 */
public class DailyMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(DailyMvvmViewModel.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    
    // Service dependencies
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final MeetingService meetingService;
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    
    // Observable properties
    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty formattedDate = new SimpleStringProperty("");
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Meeting> meetings = FXCollections.observableArrayList();
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final ObjectProperty<Meeting> selectedMeeting = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command addTaskCommand;
    private Command editTaskCommand;
    private Command addMeetingCommand;
    private Command editMeetingCommand;
    private Command takeAttendanceCommand;
    private Command refreshCommand;
    
    /**
     * Creates a new DailyMvvmViewModel.
     * 
     * @param taskService the task service
     * @param meetingService the meeting service
     */
    
    public DailyMvvmViewModel(TaskService taskService, MeetingService meetingService) {
        this.taskService = taskService;
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        this.meetingService = meetingService;
        this.meetingServiceAsync = (MeetingServiceAsyncImpl) meetingService;
        
        initializeCommands();
        setupPropertyListeners();
        updateFormattedDate();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Add task command
        addTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Add task command executed");
            },
            this::canAddTask
        );
        
        // Edit task command
        editTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit task command executed for: " + 
                    (selectedTask.get() != null ? selectedTask.get().getTitle() : "null"));
            },
            this::canEditTask
        );
        
        // Add meeting command
        addMeetingCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Add meeting command executed");
            },
            this::canAddMeeting
        );
        
        // Edit meeting command
        editMeetingCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit meeting command executed for: " + 
                    (selectedMeeting.get() != null ? selectedMeeting.get().getDate() : "null"));
            },
            this::canEditMeeting
        );
        
        // Take attendance command
        takeAttendanceCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Take attendance command executed for: " + 
                    (selectedMeeting.get() != null ? selectedMeeting.get().getDate() : "null"));
            },
            this::canTakeAttendance
        );
        
        // Refresh command
        refreshCommand = createValidOnlyCommand(
            this::refreshData,
            () -> !loading.get()
        );
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Listen for date changes to load data
        selectedDate.addListener((obs, oldDate, newDate) -> {
            updateFormattedDate();
            if (newDate != null && currentProject.get() != null) {
                loadDataForDate(newDate);
            }
        });
        
        // Listen for project changes to load data
        currentProject.addListener((obs, oldProject, newProject) -> {
            if (newProject != null && selectedDate.get() != null) {
                loadDataForDate(selectedDate.get());
            }
        });
        
        // Listen for task selection changes
        selectedTask.addListener((obs, oldTask, newTask) -> {
            LOGGER.fine("Selected task changed: " + 
                (newTask != null ? newTask.getTitle() : "null"));
        });
        
        // Listen for meeting selection changes
        selectedMeeting.addListener((obs, oldMeeting, newMeeting) -> {
            LOGGER.fine("Selected meeting changed: " + 
                (newMeeting != null ? newMeeting.getDate() : "null"));
        });
    }
    
    /**
     * Updates the formatted date string.
     */
    private void updateFormattedDate() {
        LocalDate date = selectedDate.get();
        if (date != null) {
            formattedDate.set(date.format(DATE_FORMATTER));
        } else {
            formattedDate.set("");
        }
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadDataForDate(selectedDate.get());
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
        
        Project project = currentProject.get();
        if (project == null) {
            LOGGER.warning("Cannot load data for null project");
            return;
        }
        
        loading.set(true);
        
        // Clear selection
        selectedTask.set(null);
        selectedMeeting.set(null);
        
        // Load tasks for the date asynchronously
        loadTasksForDateAsync(date, project);
        
        // Load meetings for the date asynchronously
        loadMeetingsForDateAsync(date, project);
    }
    
    /**
     * Loads tasks for the specified date asynchronously.
     * 
     * @param date the date to load tasks for
     * @param project the project to load tasks for
     */
    private void loadTasksForDateAsync(LocalDate date, Project project) {
        taskServiceAsync.findByProjectAsync(
            project,
            // Success callback
            projectTasks -> {
                Platform.runLater(() -> {
                    // Filter tasks by date
                    List<Task> tasksForDate = projectTasks.stream()
                        .filter(task -> isTaskOnDate(task, date))
                        .toList();
                    
                    tasks.clear();
                    tasks.addAll(tasksForDate);
                    
                    LOGGER.info("Loaded " + tasksForDate.size() + " tasks for date: " + date);
                    finishLoading();
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks for date: " + date, error);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                    finishLoading();
                });
            }
        );
    }
    
    /**
     * Loads meetings for the specified date asynchronously.
     * 
     * @param date the date to load meetings for
     * @param project the project to load meetings for
     */
    private void loadMeetingsForDateAsync(LocalDate date, Project project) {
        meetingServiceAsync.findByDateAsync(
            date,
            // Success callback
            allMeetings -> {
                Platform.runLater(() -> {
                    // Filter to only include meetings for this project
                    List<Meeting> meetingsForProject = allMeetings.stream()
                        .filter(m -> m.getProject() != null && 
                                m.getProject().getId().equals(project.getId()))
                        .toList();
                    
                    meetings.clear();
                    meetings.addAll(meetingsForProject);
                    
                    LOGGER.info("Loaded " + meetingsForProject.size() + " meetings for date: " + date);
                    finishLoading();
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading meetings for date: " + date, error);
                    setErrorMessage("Failed to load meetings: " + error.getMessage());
                    finishLoading();
                });
            }
        );
    }
    
    /**
     * Finishes loading by clearing the loading flag if all operations are complete.
     */
    private void finishLoading() {
        // This simple implementation just clears the loading flag.
        // In a more complex implementation, it might check if all operations are complete.
        loading.set(false);
        clearErrorMessage();
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
    
    // Command condition methods
    
    /**
     * Checks if a task can be added.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canAddTask() {
        return currentProject.get() != null && !loading.get();
    }
    
    /**
     * Checks if a task can be edited.
     * 
     * @return true if a task is selected, false otherwise
     */
    private boolean canEditTask() {
        return selectedTask.get() != null && !loading.get();
    }
    
    /**
     * Checks if a meeting can be added.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canAddMeeting() {
        return currentProject.get() != null && !loading.get();
    }
    
    /**
     * Checks if a meeting can be edited.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canEditMeeting() {
        return selectedMeeting.get() != null && !loading.get();
    }
    
    /**
     * Checks if attendance can be taken for a meeting.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canTakeAttendance() {
        return selectedMeeting.get() != null && !loading.get();
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
    
    public StringProperty formattedDateProperty() {
        return formattedDate;
    }
    
    public String getFormattedDate() {
        return formattedDate.get();
    }
    
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    public void setCurrentProject(Project project) {
        currentProject.set(project);
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
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public boolean isLoading() {
        return loading.get();
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
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        tasks.clear();
        meetings.clear();
        selectedTask.set(null);
        selectedMeeting.set(null);
    }
}