package org.frcpm.viewmodels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    
    /**
     * Creates a new project list view model.
     * 
     * @param projectService the project service
     */
    public ProjectListViewModel(ProjectService projectService) {
        this.projectService = projectService;
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
}