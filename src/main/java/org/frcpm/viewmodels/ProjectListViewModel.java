// src/main/java/org/frcpm/viewmodels/ProjectListViewModel.java
package org.frcpm.viewmodels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ServiceFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.viewmodels.BaseViewModel;

/**
 * ViewModel for the project list view.
 * Follows MVVM pattern by providing observable properties and commands.
 */
public class ProjectListViewModel extends BaseViewModel {

    private static final Logger LOGGER = Logger.getLogger(ProjectListViewModel.class.getName());
    
    // Services
    private final ProjectService projectService;
    
    // Observable collections
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    
    // Selected project
    private Project selectedProject;
    
    // Commands
    private final Command loadProjectsCommand;
    private final Command newProjectCommand;
    private final Command openProjectCommand;
    private final Command importProjectCommand;
    private final Command deleteProjectCommand;
    
    /**
     * Creates a new ProjectListViewModel with default services.
     */
    public ProjectListViewModel() {
        this(ServiceFactory.getProjectService());
    }
    
    /**
     * Creates a new ProjectListViewModel with specified service.
     * This constructor is mainly used for testing.
     * 
     * @param projectService the project service
     */
    public ProjectListViewModel(ProjectService projectService) {
        this.projectService = projectService;
        
        // Initialize commands
        loadProjectsCommand = new Command(this::loadProjects);
        newProjectCommand = new Command(this::createNewProject);
        openProjectCommand = new Command(this::openSelectedProject, this::canOpenProject);
        importProjectCommand = new Command(this::importProject);
        deleteProjectCommand = new Command(this::deleteSelectedProject, this::canDeleteProject);
        
        // Load projects initially
        loadProjects();
    }
    
    /**
     * Loads all projects.
     */
    private void loadProjects() {
        try {
            // Clear existing projects
            projects.clear();
            
            // Load all projects from service
            projects.addAll(projectService.findAll());
            
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            setErrorMessage("Failed to load projects: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new project (stub implementation, handled by separate dialog).
     */
    private void createNewProject() {
        // This is just a stub - actual implementation is in NewProjectController
        LOGGER.info("Create new project action triggered");
    }
    
    /**
     * Opens the selected project (stub implementation, handled by controller).
     */
    private void openSelectedProject() {
        // This is just a stub - actual implementation is in ProjectListController
        LOGGER.info("Open project action triggered for project: " + 
            (selectedProject != null ? selectedProject.getName() : "null"));
    }
    
    /**
     * Checks if a project can be opened.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canOpenProject() {
        return selectedProject != null;
    }
    
    /**
     * Imports a project (stub implementation, to be implemented later).
     */
    private void importProject() {
        // To be implemented
        LOGGER.info("Import project action triggered");
        setErrorMessage("Project import is not yet implemented");
    }
    
    /**
     * Deletes the selected project.
     */
    private void deleteSelectedProject() {
        if (selectedProject == null) {
            setErrorMessage("No project selected");
            return;
        }
        
        try {
            boolean success = projectService.deleteById(selectedProject.getId());
            
            if (success) {
                // Remove from list
                projects.remove(selectedProject);
                selectedProject = null;
                clearErrorMessage();
            } else {
                setErrorMessage("Failed to delete project");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting project", e);
            setErrorMessage("Error deleting project: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a project can be deleted.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canDeleteProject() {
        return selectedProject != null;
    }
    
    /**
     * Gets the projects list.
     * 
     * @return the observable list of projects
     */
    public ObservableList<Project> getProjects() {
        return projects;
    }
    
    /**
     * Gets the selected project.
     * 
     * @return the selected project
     */
    public Project getSelectedProject() {
        return selectedProject;
    }
    
    /**
     * Sets the selected project.
     * 
     * @param project the project to select
     */
    public void setSelectedProject(Project project) {
        this.selectedProject = project;
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
}