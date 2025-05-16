// src/main/java/org/frcpm/mvvm/viewmodels/MainMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ProjectService;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;
import org.frcpm.utils.ShortcutManager;

/**
 * ViewModel for the Main application view using MVVMFx.
 */
public class MainMvvmViewModel extends BaseMvvmViewModel {

    private static final Logger LOGGER = Logger.getLogger(MainMvvmViewModel.class.getName());

    // Service dependencies
    private final ProjectService projectService;
    private final ProjectServiceAsyncImpl projectServiceAsync;

    // Utilities
    private final ShortcutManager shortcutManager;
    private final NotificationCenter notificationCenter;

    // Observable collections and properties
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    // UI State properties
    private final BooleanProperty projectTabDisabled = new SimpleBooleanProperty(true);
    private final StringProperty projectTabTitle = new SimpleStringProperty("Project Details");
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");
    private final StringProperty versionInfo = new SimpleStringProperty("FRC Project Management System - v0.1.0");

    // Selected tab index
    private final ObjectProperty<Integer> selectedTabIndex = new SimpleObjectProperty<>(0);

    // Commands
    private Command loadProjectsCommand;
    private Command newProjectCommand;
    private Command openProjectCommand;
    private Command closeProjectCommand;
    private Command saveProjectCommand;
    private Command saveProjectAsCommand;
    private Command importProjectCommand;
    private Command exportProjectCommand;
    private Command exitCommand;

    private Command viewDashboardCommand;
    private Command viewGanttCommand;
    private Command viewCalendarCommand;
    private Command viewDailyCommand;
    private Command refreshCommand;

    private Command projectPropertiesCommand;
    private Command addMilestoneCommand;
    private Command scheduleMeetingCommand;
    private Command addTaskCommand;
    private Command projectStatisticsCommand;

    private Command subteamsCommand;
    private Command membersCommand;
    private Command takeAttendanceCommand;
    private Command attendanceHistoryCommand;
    private Command subsystemsCommand;

    private Command settingsCommand;
    private Command databaseManagementCommand;
    private Command userGuideCommand;
    private Command aboutCommand;

    private Command viewMetricsCommand;

    /**
     * Creates a new MainMvvmViewModel.
     * 
     * @param projectService the project service
     */

    public MainMvvmViewModel(ProjectService projectService) {
        this.projectService = projectService;

        // Get the async service implementation
        this.projectServiceAsync = AsyncServiceFactory.getProjectService();

        // Initialize shortcut manager
        this.shortcutManager = new ShortcutManager();

        // Get notification center
        this.notificationCenter = MvvmFX.getNotificationCenter();

        // Initialize commands
        initializeCommands();

        // Set up property listeners
        setupPropertyListeners();

        // Register for notifications
        registerNotifications();
    }

    /**
     * Sets up property listeners.
     */
    private void setupPropertyListeners() {
        // Listen for project selection changes
        selectedProject.addListener((obs, oldProject, newProject) -> {
            boolean hasProject = (newProject != null);
            projectTabDisabled.set(!hasProject);

            if (hasProject) {
                projectTabTitle.set(newProject.getName());
            } else {
                projectTabTitle.set("Project Details");
            }

            // Update status message
            if (hasProject) {
                statusMessage.set("Project: " + newProject.getName());
            } else {
                statusMessage.set("Ready");
            }
        });
    }

    /**
     * Registers for notifications from other ViewModels.
     */
    private void registerNotifications() {
        // Project saved notification
        notificationCenter.subscribe(MvvmConfig.PROJECT_SAVED, (key, payload) -> {
            if (payload != null && payload.length > 0 && payload[0] instanceof Project) {
                Platform.runLater(() -> {
                    Project savedProject = (Project) payload[0];
                    updateProject(savedProject);
                    statusMessage.set("Project saved: " + savedProject.getName());
                });
            }
        });

        // Project deleted notification
        notificationCenter.subscribe(MvvmConfig.PROJECT_DELETED, (key, payload) -> {
            if (payload != null && payload.length > 0 && payload[0] instanceof Long) {
                Platform.runLater(() -> {
                    Long deletedId = (Long) payload[0];
                    removeProjectById(deletedId);
                    statusMessage.set("Project deleted");
                });
            }
        });

        // Project selected notification
        notificationCenter.subscribe(MvvmConfig.PROJECT_SELECTED, (key, payload) -> {
            if (payload != null && payload.length > 0 && payload[0] instanceof Project) {
                Platform.runLater(() -> {
                    Project project = (Project) payload[0];
                    selectProject(project);
                });
            }
        });

        // Project opened notification
        notificationCenter.subscribe(MvvmConfig.PROJECT_OPENED, (key, payload) -> {
            if (payload != null && payload.length > 0 && payload[0] instanceof Project) {
                Platform.runLater(() -> {
                    Project project = (Project) payload[0];
                    openProject(project);
                    // Switch to project tab
                    selectedTabIndex.set(1);
                });
            }
        });
    }

    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // File menu commands
        loadProjectsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadProjectsAsync);

        newProjectCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("New project command executed");
                },
                () -> true);

        openProjectCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Open project command executed for: " +
                            (selectedProject.get() != null ? selectedProject.get().getName() : "null"));
                },
                () -> selectedProject.get() != null && !loading.get());

        closeProjectCommand = createValidOnlyCommand(
                this::closeProject,
                () -> selectedProject.get() != null && !loading.get());

        saveProjectCommand = createValidOnlyCommand(
                this::saveProject,
                () -> selectedProject.get() != null && !loading.get());

        saveProjectAsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Save project as command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        importProjectCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Import project command executed");
                },
                () -> !loading.get());

        exportProjectCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Export project command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        exitCommand = createValidOnlyCommand(
                () -> {
                    LOGGER.info("Exit command executed");
                    Platform.exit();
                },
                () -> true);

        // View menu commands
        viewDashboardCommand = createValidOnlyCommand(
                () -> {
                    // Switch to dashboard tab
                    selectedTabIndex.set(0);
                    LOGGER.info("View dashboard command executed");
                },
                () -> !loading.get());

        viewGanttCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("View Gantt chart command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        viewCalendarCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("View calendar command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        viewDailyCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("View daily command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        refreshCommand = MvvmAsyncHelper.createSimpleAsyncCommand(
                () -> {
                    LOGGER.info("Refresh command executed");
                    loadProjectsAsync();
                });

        // Project menu commands
        projectPropertiesCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Project properties command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        addMilestoneCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Add milestone command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        scheduleMeetingCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Schedule meeting command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        addTaskCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Add task command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        projectStatisticsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Project statistics command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        // Team menu commands
        subteamsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Subteams command executed");
                },
                () -> !loading.get());

        membersCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Members command executed");
                },
                () -> !loading.get());

        takeAttendanceCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Take attendance command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        attendanceHistoryCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Attendance history command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        subsystemsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Subsystems command executed");
                },
                () -> selectedProject.get() != null && !loading.get());

        // Tools menu commands
        settingsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Settings command executed");
                },
                () -> !loading.get());

        databaseManagementCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("Database management command executed");
                },
                () -> !loading.get());

        // Help menu commands
        userGuideCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("User guide command executed");
                },
                () -> true);

        aboutCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("About command executed");
                },
                () -> true);

        viewMetricsCommand = createValidOnlyCommand(
                () -> {
                    // This will be handled by the view/controller
                    LOGGER.info("View metrics command executed");
                },
                () -> selectedProject.get() != null && !loading.get());
    }

    /**
     * Loads projects asynchronously.
     */
    private void loadProjectsAsync() {
        loading.set(true);

        projectServiceAsync.findAllAsync(
                // Success callback
                result -> {
                    Platform.runLater(() -> {
                        projects.clear();
                        projects.addAll(result);
                        clearErrorMessage();
                        loading.set(false);
                        statusMessage.set("Projects loaded successfully");
                        LOGGER.info("Loaded " + result.size() + " projects asynchronously");
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading projects asynchronously", error);
                        setErrorMessage("Failed to load projects: " + error.getMessage());
                        statusMessage.set("Error loading projects");
                        loading.set(false);
                    });
                });
    }

    /**
     * Updates a project in the list or adds it if it doesn't exist.
     * 
     * @param project the project to update
     */
    private void updateProject(Project project) {
        if (project == null) {
            return;
        }

        // Find and update the project in the list
        boolean found = false;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId().equals(project.getId())) {
                projects.set(i, project);
                found = true;
                break;
            }
        }

        // If not found, add it
        if (!found) {
            projects.add(project);
        }

        // Update selected project if it's the same one
        if (selectedProject.get() != null &&
                selectedProject.get().getId().equals(project.getId())) {
            selectedProject.set(project);
        }
    }

    /**
     * Removes a project by ID from the list.
     * 
     * @param projectId the project ID to remove
     */
    private void removeProjectById(Long projectId) {
        if (projectId == null) {
            return;
        }

        // Find and remove the project
        projects.removeIf(p -> p.getId().equals(projectId));

        // Clear selected project if it was removed
        if (selectedProject.get() != null &&
                selectedProject.get().getId().equals(projectId)) {
            selectedProject.set(null);
        }
    }

    /**
     * Selects a project.
     * 
     * @param project the project to select
     */
    public void selectProject(Project project) {
        selectedProject.set(project);
    }

    /**
     * Opens a project.
     * 
     * @param project the project to open
     */
    public void openProject(Project project) {
        selectedProject.set(project);

        // Send notification about project opened
        notificationCenter.publish(MvvmConfig.PROJECT_OPENED, project);
    }

    /**
     * Closes the current project.
     */
    public void closeProject() {
        selectedProject.set(null);
        selectedTabIndex.set(0); // Return to dashboard
    }

    /**
     * Saves the current project.
     */
    public void saveProject() {
        Project project = selectedProject.get();
        if (project == null) {
            return;
        }

        loading.set(true);

        projectServiceAsync.saveAsync(
                project,
                // Success callback
                savedProject -> {
                    Platform.runLater(() -> {
                        updateProject(savedProject);
                        clearErrorMessage();
                        statusMessage.set("Project saved: " + savedProject.getName());

                        // Send notification about project saved
                        notificationCenter.publish(MvvmConfig.PROJECT_SAVED, savedProject);

                        loading.set(false);
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving project", error);
                        setErrorMessage("Failed to save project: " + error.getMessage());
                        statusMessage.set("Error saving project");
                        loading.set(false);
                    });
                });
    }

    // Property getters and setters

    public ObservableList<Project> getProjects() {
        return projects;
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

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }

    public StringProperty versionInfoProperty() {
        return versionInfo;
    }

    public String getVersionInfo() {
        return versionInfo.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public ObjectProperty<Integer> selectedTabIndexProperty() {
        return selectedTabIndex;
    }

    public Integer getSelectedTabIndex() {
        return selectedTabIndex.get();
    }

    public void setSelectedTabIndex(Integer index) {
        selectedTabIndex.set(index);
    }

    public ShortcutManager getShortcutManager() {
        return shortcutManager;
    }

    // Command getters

    public Command getLoadProjectsCommand() {
        return loadProjectsCommand;
    }

    public Command getNewProjectCommand() {
        return newProjectCommand;
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

    public Command getViewMetricsCommand() {
        return viewMetricsCommand;
    }

    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        projects.clear();
        selectedProject.set(null);
    }
}