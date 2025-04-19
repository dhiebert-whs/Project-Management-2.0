package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.*;
import org.frcpm.utils.ShortcutManager;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the main application in the FRC Project Management System.
 * Follows the MVVM pattern to separate business logic from UI.
 * Updated to follow consistent ViewModel patterns and best practices.
 */
public class MainViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MainViewModel.class.getName());
    
    // Services
    private final ProjectService projectService;
    private final MeetingService meetingService;
    private final TaskService taskService;
    private final SubsystemService subsystemService;
    
    // Utilities
    private final ShortcutManager shortcutManager;
    
    // Observable properties
    private final ObservableList<Project> projectList = FXCollections.observableArrayList();
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();
    private final BooleanProperty projectTabDisabled = new SimpleBooleanProperty(true);
    private final StringProperty projectTabTitle = new SimpleStringProperty("Project Details");
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    
    // Date formatter for displaying dates
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    // Commands for UI actions
    private final Command loadProjectsCommand;
    private final Command createNewProjectCommand;
    private final Command openProjectCommand;
    private final Command closeProjectCommand;
    private final Command saveProjectCommand;
    private final Command saveProjectAsCommand;
    private final Command importProjectCommand;
    private final Command exportProjectCommand;
    private final Command exitCommand;
    
    private final Command undoCommand;
    private final Command redoCommand;
    private final Command cutCommand;
    private final Command copyCommand;
    private final Command pasteCommand;
    private final Command deleteCommand;
    private final Command selectAllCommand;
    private final Command findCommand;
    
    private final Command viewDashboardCommand;
    private final Command viewGanttCommand;
    private final Command viewCalendarCommand;
    private final Command viewDailyCommand;
    private final Command refreshCommand;
    
    private final Command projectPropertiesCommand;
    private final Command addMilestoneCommand;
    private final Command scheduleMeetingCommand;
    private final Command addTaskCommand;
    private final Command projectStatisticsCommand;
    
    private final Command subteamsCommand;
    private final Command membersCommand;
    private final Command takeAttendanceCommand;
    private final Command attendanceHistoryCommand;
    private final Command subsystemsCommand;
    
    private final Command settingsCommand;
    private final Command databaseManagementCommand;
    private final Command userGuideCommand;
    private final Command aboutCommand;
    
    /**
     * Creates a new MainViewModel with default services.
     */
    public MainViewModel() {
        this(
            ServiceFactory.getProjectService(),
            ServiceFactory.getMeetingService(),
            ServiceFactory.getTaskService(),
            ServiceFactory.getSubsystemService(),
            new ShortcutManager()
        );
    }
    
    /**
     * Creates a new MainViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param projectService the project service
     * @param meetingService the meeting service
     * @param taskService the task service
     * @param subsystemService the subsystem service
     * @param shortcutManager the shortcut manager
     */
    public MainViewModel(
            ProjectService projectService,
            MeetingService meetingService,
            TaskService taskService,
            SubsystemService subsystemService,
            ShortcutManager shortcutManager) {
        
        if (projectService == null) {
            throw new IllegalArgumentException("Project service cannot be null");
        }
        if (meetingService == null) {
            throw new IllegalArgumentException("Meeting service cannot be null");
        }
        if (taskService == null) {
            throw new IllegalArgumentException("Task service cannot be null");
        }
        if (subsystemService == null) {
            throw new IllegalArgumentException("Subsystem service cannot be null");
        }
        if (shortcutManager == null) {
            throw new IllegalArgumentException("Shortcut manager cannot be null");
        }
        
        this.projectService = projectService;
        this.meetingService = meetingService;
        this.taskService = taskService;
        this.subsystemService = subsystemService;
        this.shortcutManager = shortcutManager;
        
        // Initialize commands using BaseViewModel helper methods
        loadProjectsCommand = createValidOnlyCommand(this::loadProjects, () -> true);
        createNewProjectCommand = createValidOnlyCommand(this::handleNewProject, () -> true);
        openProjectCommand = createValidOnlyCommand(this::handleOpenSelectedProject, this::canOpenSelectedProject);
        closeProjectCommand = createValidOnlyCommand(this::handleCloseProject, this::canCloseProject);
        saveProjectCommand = createValidOnlyCommand(this::handleSave, this::canSave);
        saveProjectAsCommand = createValidOnlyCommand(this::handleSaveAs, this::canSaveAs);
        importProjectCommand = createValidOnlyCommand(this::handleImportProject, () -> true);
        exportProjectCommand = createValidOnlyCommand(this::handleExportProject, this::canExportProject);
        exitCommand = createValidOnlyCommand(this::handleExit, () -> true);
        
        // Edit menu commands
        undoCommand = createValidOnlyCommand(this::handleUndo, () -> false); // Not implemented yet
        redoCommand = createValidOnlyCommand(this::handleRedo, () -> false); // Not implemented yet
        cutCommand = createValidOnlyCommand(this::handleCut, () -> false); // Not implemented yet
        copyCommand = createValidOnlyCommand(this::handleCopy, () -> false); // Not implemented yet
        pasteCommand = createValidOnlyCommand(this::handlePaste, () -> false); // Not implemented yet
        deleteCommand = createValidOnlyCommand(this::handleDelete, () -> false); // Not implemented yet
        selectAllCommand = createValidOnlyCommand(this::handleSelectAll, () -> false); // Not implemented yet
        findCommand = createValidOnlyCommand(this::handleFind, () -> false); // Not implemented yet
        
        // View menu commands
        viewDashboardCommand = createValidOnlyCommand(this::handleViewDashboard, this::canUseProjectCommands);
        viewGanttCommand = createValidOnlyCommand(this::handleViewGantt, this::canUseProjectCommands);
        viewCalendarCommand = createValidOnlyCommand(this::handleViewCalendar, this::canUseProjectCommands);
        viewDailyCommand = createValidOnlyCommand(this::handleViewDaily, this::canUseProjectCommands);
        refreshCommand = createValidOnlyCommand(this::handleRefresh, () -> true);
        
        // Project menu commands
        projectPropertiesCommand = createValidOnlyCommand(this::handleProjectProperties, this::canUseProjectCommands);
        addMilestoneCommand = createValidOnlyCommand(this::handleAddMilestone, this::canUseProjectCommands);
        scheduleMeetingCommand = createValidOnlyCommand(this::handleScheduleMeeting, this::canUseProjectCommands);
        addTaskCommand = createValidOnlyCommand(this::handleAddTask, this::canUseProjectCommands);
        projectStatisticsCommand = createValidOnlyCommand(this::handleProjectStatistics, this::canUseProjectCommands);
        
        // Team menu commands
        subteamsCommand = createValidOnlyCommand(this::handleSubteams, this::canUseProjectCommands);
        membersCommand = createValidOnlyCommand(this::handleMembers, this::canUseProjectCommands);
        takeAttendanceCommand = createValidOnlyCommand(this::handleTakeAttendance, this::canUseProjectCommands);
        attendanceHistoryCommand = createValidOnlyCommand(this::handleAttendanceHistory, this::canUseProjectCommands);
        subsystemsCommand = createValidOnlyCommand(this::handleSubsystems, this::canUseProjectCommands);
        
        // Tools menu commands
        settingsCommand = createValidOnlyCommand(this::handleSettings, () -> true);
        databaseManagementCommand = createValidOnlyCommand(this::handleDatabaseManagement, () -> true);
        userGuideCommand = createValidOnlyCommand(this::handleUserGuide, () -> true);
        aboutCommand = createValidOnlyCommand(this::handleAbout, () -> true);
        
        // Set up property listeners
        setupPropertyListeners();
        
        // Initial data load
        loadProjects();
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Create validation handler
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        
        // Set up project selection listener
        selectedProject.addListener((obs, oldValue, newValue) -> {
            boolean hasProject = (newValue != null);
            projectTabDisabled.set(!hasProject);
            
            if (hasProject) {
                projectTabTitle.set(newValue.getName());
            } else {
                projectTabTitle.set("Project Details");
            }
            
            validationHandler.run();
        });
        
        // Track the listener
        trackPropertyListener(validationHandler);
    }
    
    /**
     * Validates the current state.
     * Currently very simple since most validation is done in child ViewModels.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();
        
        // Currently no specific validation rules for MainViewModel
        // This is mostly a container for project operations
        
        // Update valid state and error message
        valid.set(errors.isEmpty());
        
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Loads projects from the database.
     */
    public void loadProjects() {
        try {
            List<Project> projects = projectService.findAll();
            projectList.setAll(projects);
            clearErrorMessage(); // Clear any previous error message on successful load
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            setErrorMessage("Failed to load projects from the database.");
        }
    }
    
    /**
     * Opens the selected project.
     */
    public void handleOpenSelectedProject() {
        Project project = selectedProject.get();
        if (project != null) {
            openProject(project);
        }
    }
    
    /**
     * Opens a project.
     * 
     * @param project the project to open
     */
    public void openProject(Project project) {
        if (project == null) {
            LOGGER.warning("Attempted to open null project");
            return;
        }
        
        // Set selected project
        selectedProject.set(project);
        clearErrorMessage();
    }
    
    /**
     * Creates a new project.
     */
    public void handleNewProject() {
        // This will be handled by the controller which will show a dialog
        // The dialog will use ProjectService to create the project
        // After project creation, we'll refresh the project list
        LOGGER.info("New Project action triggered");
    }
    
    /**
     * Opens a project by file.
     */
    public void handleOpenProject() {
        // This will be handled by the controller which will show a file dialog
        LOGGER.info("Open Project action triggered");
    }
    
    /**
     * Closes the currently open project.
     */
    public void handleCloseProject() {
        selectedProject.set(null);
        LOGGER.info("Close Project action triggered");
    }
    
    /**
     * Saves the current project.
     */
    public void handleSave() {
        Project project = selectedProject.get();
        if (project == null) {
            LOGGER.warning("No project selected to save");
            return;
        }
        
        try {
            projectService.save(project);
            setDirty(false);
            clearErrorMessage();
            LOGGER.info("Project saved successfully: " + project.getName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving project", e);
            setErrorMessage("Failed to save project: " + e.getMessage());
        }
    }
    
    /**
     * Saves the current project with a new name.
     */
    public void handleSaveAs() {
        // This will be handled by the controller which will show a dialog
        LOGGER.info("Save Project As action triggered");
    }
    
    /**
     * Imports a project from a file.
     */
    public void handleImportProject() {
        // This will be handled by the controller which will show a file dialog
        LOGGER.info("Import Project action triggered");
    }
    
    /**
     * Imports a project from a specified file.
     * 
     * @param file the file to import from
     */
    public void importProject(File file) {
        if (file == null) {
            LOGGER.warning("Null file provided for import");
            return;
        }
        
        try {
            LOGGER.info("Importing project from " + file.getName());
            // Implementation would go here
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error importing project", e);
            setErrorMessage("Failed to import project: " + e.getMessage());
        }
    }
    
    /**
     * Exports the current project to a file.
     */
    public void handleExportProject() {
        // This will be handled by the controller which will show a file dialog
        LOGGER.info("Export Project action triggered");
    }
    
    /**
     * Exits the application.
     */
    public void handleExit() {
        System.exit(0);
    }
    
    // Edit menu handlers
    
    /**
     * Performs an undo operation.
     */
    public void handleUndo() {
        LOGGER.info("Undo action triggered");
    }
    
    /**
     * Performs a redo operation.
     */
    public void handleRedo() {
        LOGGER.info("Redo action triggered");
    }
    
    /**
     * Performs a cut operation.
     */
    public void handleCut() {
        LOGGER.info("Cut action triggered");
    }
    
    /**
     * Performs a copy operation.
     */
    public void handleCopy() {
        LOGGER.info("Copy action triggered");
    }
    
    /**
     * Performs a paste operation.
     */
    public void handlePaste() {
        LOGGER.info("Paste action triggered");
    }
    
    /**
     * Performs a delete operation.
     */
    public void handleDelete() {
        LOGGER.info("Delete action triggered");
    }
    
    /**
     * Performs a select all operation.
     */
    public void handleSelectAll() {
        LOGGER.info("Select All action triggered");
    }
    
    /**
     * Performs a find operation.
     */
    public void handleFind() {
        LOGGER.info("Find action triggered");
    }
    
    // View menu handlers
    
    /**
     * Shows the dashboard view.
     */
    public void handleViewDashboard() {
        LOGGER.info("Switching to Dashboard view");
    }
    
    /**
     * Shows the Gantt chart view.
     */
    public void handleViewGantt() {
        LOGGER.info("Gantt Chart View action triggered");
    }
    
    /**
     * Shows the calendar view.
     */
    public void handleViewCalendar() {
        LOGGER.info("Calendar View action triggered");
    }
    
    /**
     * Shows the daily view.
     */
    public void handleViewDaily() {
        LOGGER.info("Daily View action triggered");
    }
    
    /**
     * Refreshes the current view.
     */
    public void handleRefresh() {
        LOGGER.info("Refresh View action triggered");
        loadProjects();
    }
    
    // Project menu handlers
    
    /**
     * Shows the project properties dialog.
     */
    public void handleProjectProperties() {
        LOGGER.info("Project Properties action triggered");
    }
    
    /**
     * Shows the add milestone dialog.
     */
    public void handleAddMilestone() {
        LOGGER.info("Add Milestone action triggered");
    }
    
    /**
     * Shows the schedule meeting dialog.
     */
    public void handleScheduleMeeting() {
        LOGGER.info("Schedule Meeting action triggered");
    }
    
    /**
     * Shows the add task dialog.
     */
    public void handleAddTask() {
        LOGGER.info("Add Task action triggered");
    }
    
    /**
     * Shows the project statistics dialog.
     */
    public void handleProjectStatistics() {
        LOGGER.info("Project Statistics action triggered");
    }
    
    // Team menu handlers
    
    /**
     * Shows the subteams dialog.
     */
    public void handleSubteams() {
        LOGGER.info("Subteams action triggered");
    }
    
    /**
     * Shows the team members dialog.
     */
    public void handleMembers() {
        LOGGER.info("Team Members action triggered");
        // Delegates to subteams dialog with members tab selected
        handleSubteams();
    }
    
    /**
     * Shows the take attendance dialog.
     */
    public void handleTakeAttendance() {
        // This will be handled by the controller which will show a meeting selection dialog
        LOGGER.info("Take Attendance action triggered");
    }
    
    /**
     * Shows the attendance history dialog.
     */
    public void handleAttendanceHistory() {
        LOGGER.info("Attendance History action triggered");
    }
    
    /**
     * Shows the subsystems dialog.
     */
    public void handleSubsystems() {
        LOGGER.info("Subsystems action triggered");
    }
    
    // Tools menu handlers
    
    /**
     * Shows the settings dialog.
     */
    public void handleSettings() {
        LOGGER.info("Settings action triggered");
    }
    
    /**
     * Shows the database management dialog.
     */
    public void handleDatabaseManagement() {
        LOGGER.info("Database Management action triggered");
    }
    
    // Help menu handlers
    
    /**
     * Shows the user guide.
     */
    public void handleUserGuide() {
        LOGGER.info("User Guide action triggered");
    }
    
    /**
     * Shows the about dialog.
     */
    public void handleAbout() {
        LOGGER.info("About action triggered");
    }
    
    /**
     * Shows a task dialog.
     * 
     * @param task the task to edit, or null to create a new task
     * @param subsystem the subsystem for a new task
     */
    public void showTaskDialog(Task task, Subsystem subsystem) {
        if (task == null && subsystem == null) {
            LOGGER.warning("Both task and subsystem are null in showTaskDialog");
        }
        LOGGER.info("Show Task Dialog action triggered");
    }
    
    /**
     * Shows a subsystem dialog.
     * 
     * @param subsystem the subsystem to edit, or null to create a new subsystem
     */
    public void showSubsystemDialog(Subsystem subsystem) {
        LOGGER.info("Show Subsystem Dialog action triggered");
    }
    
    // Command condition methods
    
    /**
     * Checks if a project is selected and can be opened.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canOpenSelectedProject() {
        return selectedProject.get() != null;
    }
    
    /**
     * Checks if a project is currently open and can be closed.
     * 
     * @return true if a project is open, false otherwise
     */
    private boolean canCloseProject() {
        return selectedProject.get() != null;
    }
    
    /**
     * Checks if the current project can be saved.
     * 
     * @return true if a project is open, false otherwise
     */
    private boolean canSave() {
        return selectedProject.get() != null;
    }
    
    /**
     * Checks if the current project can be saved as.
     * 
     * @return true if a project is open, false otherwise
     */
    private boolean canSaveAs() {
        return selectedProject.get() != null;
    }
    
    /**
     * Checks if the current project can be exported.
     * 
     * @return true if a project is open, false otherwise
     */
    private boolean canExportProject() {
        return selectedProject.get() != null;
    }
    
    /**
     * Checks if project-specific commands can be used.
     * 
     * @return true if a project is open, false otherwise
     */
    private boolean canUseProjectCommands() {
        return selectedProject.get() != null;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Any additional cleanup specific to this ViewModel
    }
    
    // Getters for commands
    
    public Command getLoadProjectsCommand() {
        return loadProjectsCommand;
    }
    
    public Command getCreateNewProjectCommand() {
        return createNewProjectCommand;
    }
    
    public Command getOpenProjectCommand() {
        return openProjectCommand;
    }
    
    public Command getCloseProjectCommand() {
        return closeProjectCommand;
    }
    
    public Command getSaveProjectCommand() {
        return saveProjectCommand;
    }
    
    public Command getSaveProjectAsCommand() {
        return saveProjectAsCommand;
    }
    
    public Command getImportProjectCommand() {
        return importProjectCommand;
    }
    
    public Command getExportProjectCommand() {
        return exportProjectCommand;
    }
    
    public Command getExitCommand() {
        return exitCommand;
    }
    
    public Command getUndoCommand() {
        return undoCommand;
    }
    
    public Command getRedoCommand() {
        return redoCommand;
    }
    
    public Command getCutCommand() {
        return cutCommand;
    }
    
    public Command getCopyCommand() {
        return copyCommand;
    }
    
    public Command getPasteCommand() {
        return pasteCommand;
    }
    
    public Command getDeleteCommand() {
        return deleteCommand;
    }
    
    public Command getSelectAllCommand() {
        return selectAllCommand;
    }
    
    public Command getFindCommand() {
        return findCommand;
    }
    
    public Command getViewDashboardCommand() {
        return viewDashboardCommand;
    }
    
    public Command getViewGanttCommand() {
        return viewGanttCommand;
    }
    
    public Command getViewCalendarCommand() {
        return viewCalendarCommand;
    }
    
    public Command getViewDailyCommand() {
        return viewDailyCommand;
    }
    
    public Command getRefreshCommand() {
        return refreshCommand;
    }
    
    public Command getProjectPropertiesCommand() {
        return projectPropertiesCommand;
    }
    
    public Command getAddMilestoneCommand() {
        return addMilestoneCommand;
    }
    
    public Command getScheduleMeetingCommand() {
        return scheduleMeetingCommand;
    }
    
    public Command getAddTaskCommand() {
        return addTaskCommand;
    }
    
    public Command getProjectStatisticsCommand() {
        return projectStatisticsCommand;
    }
    
    public Command getSubteamsCommand() {
        return subteamsCommand;
    }
    
    public Command getMembersCommand() {
        return membersCommand;
    }
    
    public Command getTakeAttendanceCommand() {
        return takeAttendanceCommand;
    }
    
    public Command getAttendanceHistoryCommand() {
        return attendanceHistoryCommand;
    }
    
    public Command getSubsystemsCommand() {
        return subsystemsCommand;
    }
    
    public Command getSettingsCommand() {
        return settingsCommand;
    }
    
    public Command getDatabaseManagementCommand() {
        return databaseManagementCommand;
    }
    
    public Command getUserGuideCommand() {
        return userGuideCommand;
    }
    
    public Command getAboutCommand() {
        return aboutCommand;
    }
    
    // Getters and setters for properties
    
    public ObservableList<Project> getProjectList() {
        return projectList;
    }
    
    public ObjectProperty<Project> selectedProjectProperty() {
        return selectedProject;
    }
    
    public Project getSelectedProject() {
        return selectedProject.get();
    }
    
    public void setSelectedProject(Project project) {
        selectedProject.set(project);
    }
    
    public BooleanProperty projectTabDisabledProperty() {
        return projectTabDisabled;
    }
    
    public boolean isProjectTabDisabled() {
        return projectTabDisabled.get();
    }
    
    public StringProperty projectTabTitleProperty() {
        return projectTabTitle;
    }
    
    public String getProjectTabTitle() {
        return projectTabTitle.get();
    }
    
    public BooleanProperty validProperty() {
        return valid;
    }
    
    public boolean isValid() {
        return valid.get();
    }
    
    public ShortcutManager getShortcutManager() {
        return shortcutManager;
    }
    
    /**
     * Formats a date for display using the configured formatter.
     * 
     * @param date the date to format
     * @return the formatted date string, or an empty string if the date is null
     */
    public String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(dateFormatter);
    }
    
    /**
     * Handles the user pressing a key combination.
     * 
     * @param combination the key combination
     */
    public void handleKeyShortcut(String combination) {
        if (combination == null || combination.isEmpty()) {
            LOGGER.warning("Null or empty key combination received");
            return;
        }
        
        LOGGER.info("Key shortcut: " + combination);
        // This will be expanded later to handle keyboard shortcuts
    }
}