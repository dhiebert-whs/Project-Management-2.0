// src/main/java/org/frcpm/viewmodels/ProjectListAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.ProjectService;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for the project list view with asynchronous operations.
 */
public class ProjectListAsyncViewModel extends ProjectListViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListAsyncViewModel.class.getName());
    
    private final ProjectServiceAsyncImpl projectServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncLoadProjectsCommand;
    private Command asyncDeleteProjectCommand;
    
    /**
     * Creates a new project list async view model.
     * 
     * @param projectService the project service
     */
    public ProjectListAsyncViewModel(ProjectService projectService) {
        super(projectService);
        
        // Get the async service implementation
        this.projectServiceAsync = AsyncServiceFactory.getProjectService();
        
        // Initialize async commands
        asyncLoadProjectsCommand = new Command(this::loadProjectsAsync);
        
        asyncDeleteProjectCommand = new Command(() -> {
            Project selectedProject = getSelectedProject();
            if (selectedProject != null) {
                deleteProjectAsync(selectedProject);
            }
        }, () -> getSelectedProject() != null);
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
                    // Show error notification
                    setErrorMessage("Failed to load projects: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Deletes a project asynchronously.
     * 
     * @param project the project to delete
     */
    public void deleteProjectAsync(Project project) {
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
                        ObservableList<Project> projects = getProjects();
                        projects.remove(project);
                        if (getSelectedProject() == project) {
                            setSelectedProject(null);
                        }
                        LOGGER.info("Deleted project: " + project.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete project: " + project.getName() + " asynchronously");
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
                    // Show error notification
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
     * Gets the async load projects command.
     * 
     * @return the async load projects command
     */
    public Command getAsyncLoadProjectsCommand() {
        return asyncLoadProjectsCommand;
    }
    
    /**
     * Gets the async delete project command.
     * 
     * @return the async delete project command
     */
    public Command getAsyncDeleteProjectCommand() {
        return asyncDeleteProjectCommand;
    }
    
    /**
         * Sets the error message.
         * 
         * @param message the error message
         */
        public void setErrorMessage(String message) {
            Platform.runLater(() -> {
                super.setErrorMessage(message);
            });
        }
}