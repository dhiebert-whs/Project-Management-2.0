// src/main/java/org/frcpm/viewmodels/ProjectViewModel.java

package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for Project management in the FRC Project Management System.
 * Standardized to follow the MVVM pattern.
 */
public class ProjectViewModel extends BaseViewModel {

    private static final Logger LOGGER = Logger.getLogger(ProjectViewModel.class.getName());

    // Services
    private final ProjectService projectService;
    private final MilestoneService milestoneService;
    private final TaskService taskService;
    private final MeetingService meetingService;
    private final SubsystemService subsystemService;

    // Observable properties
    private final StringProperty projectName = new SimpleStringProperty("");
    private final StringProperty projectDescription = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> goalEndDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> hardDeadline = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Milestone> milestones = FXCollections.observableArrayList();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Meeting> meetings = FXCollections.observableArrayList();
    private final BooleanProperty isNewProject = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    // Project summary properties
    private final IntegerProperty totalTasks = new SimpleIntegerProperty(0);
    private final IntegerProperty completedTasks = new SimpleIntegerProperty(0);
    private final DoubleProperty completionPercentage = new SimpleDoubleProperty(0);
    private final LongProperty daysUntilGoal = new SimpleLongProperty(0);
    private final LongProperty daysUntilDeadline = new SimpleLongProperty(0);

    // Commands
    private final Command saveCommand;
    private final Command createNewCommand;
    private final Command deleteCommand;
    private final Command loadProjectsCommand;
    private final Command loadMilestonesCommand;
    private final Command loadTasksCommand;
    private final Command loadMeetingsCommand;
    private final Command addTaskCommand;
    private final Command addMilestoneCommand;
    private final Command scheduleMeetingCommand;

    /**
     * Creates a new ProjectViewModel with default services.
     */
    public ProjectViewModel() {
        this(
                ServiceFactory.getProjectService(),
                ServiceFactory.getMilestoneService(),
                ServiceFactory.getTaskService(),
                ServiceFactory.getMeetingService(),
                ServiceFactory.getSubsystemService());
    }

    /**
     * Creates a new ProjectViewModel with just project and milestone services.
     * Provided for backward compatibility with existing tests.
     * 
     * @param projectService   the project service
     * @param milestoneService the milestone service
     */
    public ProjectViewModel(ProjectService projectService, MilestoneService milestoneService) {
        this(
                projectService,
                milestoneService,
                ServiceFactory.getTaskService(),
                ServiceFactory.getMeetingService(),
                ServiceFactory.getSubsystemService());
    }

    /**
     * Creates a new ProjectViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param projectService   the project service
     * @param milestoneService the milestone service
     * @param taskService      the task service
     * @param meetingService   the meeting service
     * @param subsystemService the subsystem service
     */
    public ProjectViewModel(
            ProjectService projectService,
            MilestoneService milestoneService,
            TaskService taskService,
            MeetingService meetingService,
            SubsystemService subsystemService) {
        this.projectService = projectService;
        this.milestoneService = milestoneService;
        this.taskService = taskService;
        this.meetingService = meetingService;
        this.subsystemService = subsystemService;

        // Create commands
        saveCommand = new Command(this::save, this::isValid);
        createNewCommand = new Command(this::createNew);
        deleteCommand = new Command(this::delete, this::canDelete);
        loadProjectsCommand = new Command(this::loadProjects);
        loadMilestonesCommand = new Command(this::loadMilestones, this::canLoadMilestones);
        loadTasksCommand = new Command(this::loadTasks, this::canLoadData);
        loadMeetingsCommand = new Command(this::loadMeetings, this::canLoadData);
        addTaskCommand = new Command(this::addTask, this::canLoadData);
        addMilestoneCommand = new Command(this::addMilestone, this::canLoadData);
        scheduleMeetingCommand = new Command(this::scheduleMeeting, this::canLoadData);

        // Set up validation listeners
        projectName.addListener((observable, oldValue, newValue) -> validate());
        startDate.addListener((observable, oldValue, newValue) -> validate());
        goalEndDate.addListener((observable, oldValue, newValue) -> validate());
        hardDeadline.addListener((observable, oldValue, newValue) -> validate());

        // Set up dirty flag listeners
        projectDescription.addListener((observable, oldValue, newValue) -> setDirty(true));

        // Set up selection listener
        selectedProject.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateFormFromProject(newValue);
                loadProjectSummary(newValue);
                loadMilestones();
                loadTasks();
                loadMeetings();
            } else {
                clearForm();
            }
        });

        // Initial validation
        validate();

        // Load projects
        loadProjects();
    }

    /**
     * Validates the form data.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (projectName.get() == null || projectName.get().trim().isEmpty()) {
            errors.add("Project name is required");
        }

        if (startDate.get() == null) {
            errors.add("Start date is required");
        }

        if (hardDeadline.get() == null) {
            errors.add("Competition date is required");
        }

        // Check date relationships
        if (startDate.get() != null && goalEndDate.get() != null &&
                goalEndDate.get().isBefore(startDate.get())) {
            errors.add("Goal end date cannot be before start date");
        }

        if (startDate.get() != null && hardDeadline.get() != null &&
                hardDeadline.get().isBefore(startDate.get())) {
            errors.add("Competition date cannot be before start date");
        }

        if (goalEndDate.get() != null && hardDeadline.get() != null &&
                hardDeadline.get().isBefore(goalEndDate.get())) {
            errors.add("Competition date cannot be before goal end date");
        }

        // Update valid state and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }

    /**
     * Loads the list of projects.
     */
    private void loadProjects() {
        try {
            List<Project> projectList = projectService.findAll();
            projects.clear();
            projects.addAll(projectList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            setErrorMessage("Failed to load projects: " + e.getMessage());
        }
    }

    /**
     * Loads milestones for the selected project.
     */
    private void loadMilestones() {
        try {
            Project project = selectedProject.get();
            if (project != null) {
                List<Milestone> milestoneList = milestoneService.findByProject(project);
                milestones.clear();
                milestones.addAll(milestoneList);
            } else {
                milestones.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading milestones", e);
            setErrorMessage("Failed to load milestones: " + e.getMessage());
        }
    }

    /**
     * Loads tasks for the selected project.
     */
    private void loadTasks() {
        try {
            Project project = selectedProject.get();
            if (project != null) {
                List<Task> taskList = taskService.findByProject(project);
                tasks.clear();
                tasks.addAll(taskList);
            } else {
                tasks.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            setErrorMessage("Failed to load tasks: " + e.getMessage());
        }
    }

    /**
     * Loads meetings for the selected project.
     */
    private void loadMeetings() {
        try {
            Project project = selectedProject.get();
            if (project != null) {
                List<Meeting> meetingList = meetingService.findByProject(project);
                meetings.clear();
                meetings.addAll(meetingList);
            } else {
                meetings.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meetings", e);
            setErrorMessage("Failed to load meetings: " + e.getMessage());
        }
    }

    // src/main/java/org/frcpm/viewmodels/ProjectViewModel.java (continued)

    /**
     * Loads project summary data.
     * 
     * @param project the project to load summary for
     */
    private void loadProjectSummary(Project project) {
        try {
            Map<String, Object> summary = projectService.getProjectSummary(project.getId());

            // Update summary properties
            totalTasks.set((Integer) summary.get("totalTasks"));
            completedTasks.set((Integer) summary.get("completedTasks"));
            completionPercentage.set((Double) summary.get("completionPercentage"));
            daysUntilGoal.set((Long) summary.get("daysUntilGoal"));
            daysUntilDeadline.set((Long) summary.get("daysUntilDeadline"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading project summary", e);
            setErrorMessage("Failed to load project summary: " + e.getMessage());
        }
    }

    /**
     * Prepares to add a new task.
     * This does not actually create the task; the controller will handle showing
     * the dialog.
     */
    private void addTask() {
        Project project = selectedProject.get();
        if (project == null) {
            setErrorMessage("No project is selected.");
            return;
        }

        try {
            // Load subsystems for selection
            List<Subsystem> subsystems = subsystemService.findAll();

            if (subsystems.isEmpty()) {
                setErrorMessage("Please create at least one subsystem before adding tasks.");
                return;
            }

            // The actual selection and task creation will be handled by the controller
            // which will show the appropriate dialogs

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error preparing to add task", e);
            setErrorMessage("Failed to prepare for adding task: " + e.getMessage());
        }
    }

    /**
     * Prepares to add a new milestone.
     * This does not actually create the milestone; the controller will handle
     * showing the dialog.
     */
    private void addMilestone() {
        Project project = selectedProject.get();
        if (project == null) {
            setErrorMessage("No project is selected.");
        }
        // The actual milestone creation will be handled by the controller
        // which will show the appropriate dialog
    }

    /**
     * Prepares to schedule a new meeting.
     * This does not actually create the meeting; the controller will handle showing
     * the dialog.
     */
    private void scheduleMeeting() {
        Project project = selectedProject.get();
        if (project == null) {
            setErrorMessage("No project is selected.");
        }
        // The actual meeting creation will be handled by the controller
        // which will show the appropriate dialog
    }

    /**
     * Sets up the form for creating a new project.
     */
    public void initNewProject() {
        selectedProject.set(null);
        isNewProject.set(true);

        // Set default values
        projectName.set("");
        projectDescription.set("");
        startDate.set(LocalDate.now());
        goalEndDate.set(LocalDate.now().plusWeeks(5));
        hardDeadline.set(LocalDate.now().plusWeeks(6));

        // Clear data collections
        tasks.clear();
        milestones.clear();
        meetings.clear();

        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }

    /**
     * Sets up the form for editing an existing project.
     * 
     * @param project the project to edit
     */
    public void initExistingProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        selectedProject.set(project);
        isNewProject.set(false);

        // Update form from project
        updateFormFromProject(project);

        // Clear dirty flag
        setDirty(false);
    }

    /**
     * Updates the form fields from a project.
     * 
     * @param project the project to get values from
     */
    private void updateFormFromProject(Project project) {
        projectName.set(project.getName());
        projectDescription.set(project.getDescription());
        startDate.set(project.getStartDate());
        goalEndDate.set(project.getGoalEndDate());
        hardDeadline.set(project.getHardDeadline());
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        projectName.set("");
        projectDescription.set("");
        startDate.set(LocalDate.now());
        goalEndDate.set(null);
        hardDeadline.set(null);

        // Clear data collections
        tasks.clear();
        milestones.clear();
        meetings.clear();

        // Reset summary properties
        totalTasks.set(0);
        completedTasks.set(0);
        completionPercentage.set(0);
        daysUntilGoal.set(0);
        daysUntilDeadline.set(0);

        // Clear error message
        clearErrorMessage();
    }

    /**
     * Saves the project.
     * Called when the save command is executed.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }

        try {
            Project project;

            if (isNewProject.get()) {
                // Create new project
                project = projectService.createProject(
                        projectName.get(),
                        startDate.get(),
                        goalEndDate.get(),
                        hardDeadline.get());

                // Update description in a separate step
                project = projectService.updateProject(
                        project.getId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getGoalEndDate(),
                        project.getHardDeadline(),
                        projectDescription.get());

                // Add to projects list
                projects.add(project);
            } else {
                // Update existing project
                project = projectService.updateProject(
                        selectedProject.get().getId(),
                        projectName.get(),
                        startDate.get(),
                        goalEndDate.get(),
                        hardDeadline.get(),
                        projectDescription.get());

                // Update in projects list
                int index = projects.indexOf(selectedProject.get());
                if (index >= 0) {
                    projects.set(index, project);
                }
            }

            // Update selected project
            selectedProject.set(project);

            // Clear dirty flag
            setDirty(false);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving project", e);
            setErrorMessage("Failed to save project: " + e.getMessage());
        }
    }

    /**
     * Creates a new project.
     * Called when the create new command is executed.
     */
    private void createNew() {
        initNewProject();
    }

    /**
     * Deletes the selected project.
     * Called when the delete command is executed.
     */
    private void delete() {
        try {
            Project project = selectedProject.get();
            if (project != null) {
                projectService.deleteById(project.getId());

                // Remove from projects list
                projects.remove(project);

                // Clear selection
                selectedProject.set(null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting project", e);
            setErrorMessage("Failed to delete project: " + e.getMessage());
        }
    }

    /**
     * Checks if the delete command can be executed.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canDelete() {
        return selectedProject.get() != null;
    }

    /**
     * Checks if the load milestones command can be executed.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canLoadMilestones() {
        return selectedProject.get() != null;
    }

    /**
     * Checks if data loading commands can be executed.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canLoadData() {
        return selectedProject.get() != null;
    }

    /**
     * Gets the list of subsystems.
     * Used by the controller for subsystem selection.
     * 
     * @return the list of subsystems
     */
    public List<Subsystem> getSubsystems() {
        try {
            return subsystemService.findAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystems", e);
            setErrorMessage("Failed to load subsystems: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Property accessors

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public StringProperty projectDescriptionProperty() {
        return projectDescription;
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public ObjectProperty<LocalDate> goalEndDateProperty() {
        return goalEndDate;
    }

    public ObjectProperty<LocalDate> hardDeadlineProperty() {
        return hardDeadline;
    }

    public ObjectProperty<Project> selectedProjectProperty() {
        return selectedProject;
    }

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public ObservableList<Milestone> getMilestones() {
        return milestones;
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public ObservableList<Meeting> getMeetings() {
        return meetings;
    }

    public BooleanProperty isNewProjectProperty() {
        return isNewProject;
    }

    public BooleanProperty validProperty() {
        return valid;
    }

    public IntegerProperty totalTasksProperty() {
        return totalTasks;
    }

    public IntegerProperty completedTasksProperty() {
        return completedTasks;
    }

    public DoubleProperty completionPercentageProperty() {
        return completionPercentage;
    }

    public LongProperty daysUntilGoalProperty() {
        return daysUntilGoal;
    }

    public LongProperty daysUntilDeadlineProperty() {
        return daysUntilDeadline;
    }

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCreateNewCommand() {
        return createNewCommand;
    }

    public Command getDeleteCommand() {
        return deleteCommand;
    }

    public Command getLoadProjectsCommand() {
        return loadProjectsCommand;
    }

    public Command getLoadMilestonesCommand() {
        return loadMilestonesCommand;
    }

    public Command getLoadTasksCommand() {
        return loadTasksCommand;
    }

    public Command getLoadMeetingsCommand() {
        return loadMeetingsCommand;
    }

    public Command getAddTaskCommand() {
        return addTaskCommand;
    }

    public Command getAddMilestoneCommand() {
        return addMilestoneCommand;
    }

    public Command getScheduleMeetingCommand() {
        return scheduleMeetingCommand;
    }

    // Getters and setters

    public String getProjectName() {
        return projectName.get();
    }

    public void setProjectName(String name) {
        projectName.set(name);
    }

    public String getProjectDescription() {
        return projectDescription.get();
    }

    public void setProjectDescription(String description) {
        projectDescription.set(description);
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate date) {
        startDate.set(date);
    }

    public LocalDate getGoalEndDate() {
        return goalEndDate.get();
    }

    public void setGoalEndDate(LocalDate date) {
        goalEndDate.set(date);
    }

    public LocalDate getHardDeadline() {
        return hardDeadline.get();
    }

    public void setHardDeadline(LocalDate date) {
        hardDeadline.set(date);
    }

    public Project getSelectedProject() {
        return selectedProject.get();
    }

    public void setSelectedProject(Project project) {
        selectedProject.set(project);
    }

    public boolean isNewProject() {
        return isNewProject.get();
    }

    public void setIsNewProject(boolean isNew) {
        isNewProject.set(isNew);
    }

    public boolean isValid() {
        return valid.get();
    }

    public int getTotalTasks() {
        return totalTasks.get();
    }

    public int getCompletedTasks() {
        return completedTasks.get();
    }

    public double getCompletionPercentage() {
        return completionPercentage.get();
    }

    public long getDaysUntilGoal() {
        return daysUntilGoal.get();
    }

    public long getDaysUntilDeadline() {
        return daysUntilDeadline.get();
    }

    /**
     * Clears the error message.
     * Public wrapper for BaseViewModel's protected clearErrorMessage method.
     */
    public void clearErrorMessage() {
        errorMessageProperty().set("");
    }
}