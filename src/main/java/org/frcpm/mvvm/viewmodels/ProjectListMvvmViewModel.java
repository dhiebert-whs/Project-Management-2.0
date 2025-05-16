// src/main/java/org/frcpm/mvvm/viewmodels/ProjectListMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ProjectService;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;

/**
 * ViewModel for the ProjectList view using MVVMFx.
 * This is the first implementation using the MVVMFx framework.
 */
public class ProjectListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListMvvmViewModel.class.getName());
    
    // Service dependencies
    private final ProjectService projectService;
    private final ProjectServiceAsyncImpl projectServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command loadProjectsCommand;
    private Command newProjectCommand;
    private Command openProjectCommand;
    private Command importProjectCommand;
    private Command deleteProjectCommand;
    
    /**
     * Creates a new ProjectListMvvmViewModel.
     * 
     * @param projectService the project service
     */
   
    public ProjectListMvvmViewModel(ProjectService projectService) {
        this.projectService = projectService;
        
        // Get the async service implementation
        this.projectServiceAsync = AsyncServiceFactory.getProjectService();
        
        initializeCommands();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load projects command - create with simple async command since we'll handle futures ourselves
        loadProjectsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadProjectsAsync);
        
        // New project command
        newProjectCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            // This will be handled by the view/controller
            LOGGER.info("New project command executed");
        });
        
        // Open project command
        openProjectCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Open project command executed for: " + 
                    (selectedProject.get() != null ? selectedProject.get().getName() : "null"));
            },
            () -> selectedProject.get() != null
        );
        
        // Import project command
        importProjectCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            // This will be handled by the view/controller
            LOGGER.info("Import project command executed");
        });
        
        // Delete project command - create with simple async command since we'll handle futures ourselves
        deleteProjectCommand = createValidOnlyCommand(
            this::deleteProjectAsync,
            () -> selectedProject.get() != null
        );
    }
    
    /**
     * Gets the projects list.
     * 
     * @return the projects list
     */
    public ObservableList<Project> getProjects() {
        return projects;
    }
    
    /**
     * Gets the selected project property.
     * 
     * @return the selected project property
     */
    public ObjectProperty<Project> selectedProjectProperty() {
        return selectedProject;
    }
    
    /**
     * Gets the selected project.
     * 
     * @return the selected project
     */
    public Project getSelectedProject() {
        return selectedProject.get();
    }
    
    /**
     * Sets the selected project.
     * 
     * @param project the selected project
     */
    public void setSelectedProject(Project project) {
        selectedProject.set(project);
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
     * Checks if the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the load projects command.
     * 
     * @return the load projects command
     */
    public Command getLoadProjectsCommand() {
        return loadProjectsCommand;
    }
    
    /**
     * Gets the new project command.
     * 
     * @return the new project command
     */
    public Command getNewProjectCommand() {
        return newProjectCommand;
    }
    
    /**
     * Gets the open project command.
     * 
     * @return the open project command
     */
    public Command getOpenProjectCommand() {
        return openProjectCommand;
    }
    
    /**
     * Gets the import project command.
     * 
     * @return the import project command
     */
    public Command getImportProjectCommand() {
        return importProjectCommand;
    }
    
    /**
     * Gets the delete project command.
     * 
     * @return the delete project command
     */
    public Command getDeleteProjectCommand() {
        return deleteProjectCommand;
    }
    
    /**
     * Loads projects asynchronously.
     */
    private void loadProjectsAsync() {
        loading.set(true);
        
        // Use the callback-based API of your AbstractAsyncService
        projectServiceAsync.findAllAsync(
            // Success callback
            projectList -> {
                Platform.runLater(() -> {
                    projects.clear();
                    projects.addAll(projectList);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + projectList.size() + " projects asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading projects asynchronously", error);
                    setErrorMessage("Failed to load projects: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a project asynchronously.
     */
    private void deleteProjectAsync() {
        Project project = selectedProject.get();
        if (project == null || project.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        // Use the callback-based API of your AbstractAsyncService
        projectServiceAsync.deleteByIdAsync(
            project.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        projects.remove(project);
                        selectedProject.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted project: " + project.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete project: " + project.getName() + " asynchronously");
                        setErrorMessage("Failed to delete project: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting project asynchronously", error);
                    setErrorMessage("Failed to delete project: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
}