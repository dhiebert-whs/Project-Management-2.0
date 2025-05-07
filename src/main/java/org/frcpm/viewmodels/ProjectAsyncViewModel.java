// src/main/java/org/frcpm/viewmodels/ProjectAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ProjectService;
import org.frcpm.services.TaskService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for Project management with asynchronous operations.
 */
public class ProjectAsyncViewModel extends ProjectViewModel {

    private static final Logger LOGGER = Logger.getLogger(ProjectAsyncViewModel.class.getName());
    
    private final ProjectServiceAsyncImpl projectServiceAsync;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncSaveCommand;
    private Command asyncLoadProjectsCommand;
    private Command asyncLoadTasksCommand;
    private Command asyncDeleteCommand;
    
    /**
     * Creates a new ProjectAsyncViewModel with default services.
     */
    public ProjectAsyncViewModel(
            ProjectService projectService,
            MilestoneService milestoneService,
            TaskService taskService,
            MeetingService meetingService,
            SubsystemService subsystemService) {
        super(projectService, milestoneService, taskService, meetingService, subsystemService);
        
        // Get the async service implementations
        this.projectServiceAsync = AsyncServiceFactory.getProjectService();
        this.taskServiceAsync = AsyncServiceFactory.getTaskService();
        
        // Initialize async commands
        asyncSaveCommand = new Command(this::saveAsync, this::isValid);
        asyncLoadProjectsCommand = new Command(this::loadProjectsAsync);
        asyncLoadTasksCommand = new Command(this::loadTasksAsync, () -> getSelectedProject() != null);
        asyncDeleteCommand = new Command(this::deleteAsync, () -> getSelectedProject() != null);
    }
    
    /**
     * Loads projects asynchronously.
     */
    public void loadProjectsAsync() {
        loading.set(true);
        
        projectServiceAsync.findAllAsync(
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    ObservableList<Project> projects = getProjects();
                    projects.clear();
                    projects.addAll(result);
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " projects asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading projects asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to load projects: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Loads tasks for the selected project asynchronously.
     */
    public void loadTasksAsync() {
        Project project = getSelectedProject();
        if (project == null) {
            return;
        }
        
        loading.set(true);
        
        taskServiceAsync.findByProjectAsync(
            project,
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    ObservableList<Task> tasks = getTasks();
                    tasks.clear();
                    tasks.addAll(result);
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " tasks asynchronously for project: " + project.getName());
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Loads project summary asynchronously.
     */
    public void loadProjectSummaryAsync(Project project) {
        if (project == null || project.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        projectServiceAsync.getProjectSummaryAsync(
            project.getId(),
            // Success handler
            summary -> {
                Platform.runLater(() -> {
                    // Update summary properties
                    totalTasksProperty().set((Integer) summary.get("totalTasks"));
                    completedTasksProperty().set((Integer) summary.get("completedTasks"));
                    completionPercentageProperty().set((Double) summary.get("completionPercentage"));
                    daysUntilGoalProperty().set((Long) summary.get("daysUntilGoal"));
                    daysUntilDeadlineProperty().set((Long) summary.get("daysUntilDeadline"));
                    
                    loading.set(false);
                    LOGGER.info("Loaded project summary asynchronously for project: " + project.getName());
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading project summary asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to load project summary: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Saves the project asynchronously.
     */
    public void saveAsync() {
        if (!isValid()) {
            return;
        }
        
        loading.set(true);
        
        try {
            if (isNewProject()) {
                // Create new project
                projectServiceAsync.createProjectAsync(
                    getProjectName(),
                    getStartDate(),
                    getGoalEndDate(),
                    getHardDeadline(),
                    // Success handler
                    createdProject -> {
                        Platform.runLater(() -> {
                            // Update description in a separate step
                            projectServiceAsync.updateProjectAsync(
                                createdProject.getId(),
                                createdProject.getName(),
                                createdProject.getStartDate(),
                                createdProject.getGoalEndDate(),
                                createdProject.getHardDeadline(),
                                getProjectDescription(),
                                // Success handler for update
                                updatedProject -> {
                                    Platform.runLater(() -> {
                                        // Add to projects list
                                        getProjects().add(updatedProject);
                                        
                                        // Update selected project
                                        setSelectedProject(updatedProject);
                                        
                                        // Clear dirty flag
                                        setDirty(false);
                                        loading.set(false);
                                        
                                        LOGGER.info("Created project asynchronously: " + updatedProject.getName());
                                    });
                                },
                                // Error handler for update
                                error -> {
                                    Platform.runLater(() -> {
                                        LOGGER.log(Level.SEVERE, "Error updating project description asynchronously", error);
                                        loading.set(false);
                                        setErrorMessage("Failed to update project description: " + error.getMessage());
                                    });
                                }
                            );
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating project asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create project: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing project
                Project selectedProject = getSelectedProject();
                
                projectServiceAsync.updateProjectAsync(
                    selectedProject.getId(),
                    getProjectName(),
                    getStartDate(),
                    getGoalEndDate(),
                    getHardDeadline(),
                    getProjectDescription(),
                    // Success handler
                    updatedProject -> {
                        Platform.runLater(() -> {
                            // Update in projects list
                            int index = getProjects().indexOf(selectedProject);
                            if (index >= 0) {
                                getProjects().set(index, updatedProject);
                            }
                            
                            // Update selected project
                            setSelectedProject(updatedProject);
                            
                            // Clear dirty flag
                            setDirty(false);
                            loading.set(false);
                            
                            LOGGER.info("Updated project asynchronously: " + updatedProject.getName());
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating project asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to update project: " + error.getMessage());
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in saveAsync method", e);
            setErrorMessage("Failed to save project: " + e.getMessage());
        }
    }
    
    /**
     * Deletes the selected project asynchronously.
     */
    public void deleteAsync() {
        Project project = getSelectedProject();
        if (project == null || project.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        projectServiceAsync.deleteByIdAsync(
            project.getId(),
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        // Remove from projects list
                        getProjects().remove(project);
                        
                        // Clear selection
                        setSelectedProject(null);
                        
                        LOGGER.info("Deleted project asynchronously: " + project.getName());
                    } else {
                        setErrorMessage("Failed to delete project: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting project asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to delete project: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the view model is currently loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the async save command.
     * 
     * @return the async save command
     */
    public Command getAsyncSaveCommand() {
        return asyncSaveCommand;
    }
    
    /**
     * Gets the async load projects command.
     * 
     * @return the async load projects command
     */
    public Command getAsyncLoadProjectsCommand() {
        return asyncLoadProjectsCommand;
    }
    
    /**
     * Gets the async load tasks command.
     * 
     * @return the async load tasks command
     */
    public Command getAsyncLoadTasksCommand() {
        return asyncLoadTasksCommand;
    }
    
    /**
     * Gets the async delete command.
     * 
     * @return the async delete command
     */
    public Command getAsyncDeleteCommand() {
        return asyncDeleteCommand;
    }
}