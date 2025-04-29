package org.frcpm.viewmodels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;

import java.util.logging.Logger;

/**
 * ViewModel for the project list view.
 */
public class ProjectListViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListViewModel.class.getName());
    
    private final ProjectService projectService;
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private Project selectedProject;

    private Command loadProjectsCommand;
    private Command newProjectCommand;
    private Command openProjectCommand;
    private Command importProjectCommand;
    private Command deleteProjectCommand;
    
    /**
     * Creates a new project list view model.
     * 
     * @param projectService the project service
     */
    public ProjectListViewModel(ProjectService projectService) {
        this.projectService = projectService;

           
        // Initialize commands
        loadProjectsCommand = new Command(this::loadProjects);
        
        newProjectCommand = new Command(() -> {
            // This will be handled by the controller/presenter
            LOGGER.info("New project command executed");
        });
        
        openProjectCommand = new Command(() -> {
            // This will be handled by the controller/presenter
            LOGGER.info("Open project command executed");
        }, () -> selectedProject != null);
        
        importProjectCommand = new Command(() -> {
            // This will be handled by the controller/presenter
            LOGGER.info("Import project command executed");
        });
        
        deleteProjectCommand = new Command(() -> {
            // This will be handled by the controller/presenter
            if (selectedProject != null) {
                deleteProject(selectedProject);
            }
        }, () -> selectedProject != null);
    }
    
    /**
     * Loads projects.
     */
    public void loadProjects() {
        projects.clear();
        projects.addAll(projectService.findAll());
        LOGGER.info("Loaded " + projects.size() + " projects");
    }
    
    /**
     * Deletes a project.
     * 
     * @param project the project to delete
     * @return true if the project was deleted, false otherwise
     */
    public boolean deleteProject(Project project) {
        if (project == null) {
            return false;
        }
        
        boolean success = projectService.deleteById(project.getId());
        if (success) {
            projects.remove(project);
            if (selectedProject == project) {
                selectedProject = null;
            }
        }
        
        return success;
    }
    
    /**
     * Gets the projects.
     * 
     * @return the projects
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
     * @param selectedProject the project to select
     */
    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
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