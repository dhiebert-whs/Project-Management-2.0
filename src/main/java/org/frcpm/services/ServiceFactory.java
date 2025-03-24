package org.frcpm.services;

import org.frcpm.services.impl.*;

/**
 * Factory class for creating service instances.
 * This follows the factory pattern to centralize service creation.
 */
public class ServiceFactory {
    
    private static final ProjectService projectService = new ProjectServiceImpl();
    private static final TaskService taskService = new TaskServiceImpl();
    private static final TeamMemberService teamMemberService = new TeamMemberServiceImpl();
    // Add other services as we implement them
    
    /**
     * Gets the project service instance.
     * 
     * @return the project service
     */
    public static ProjectService getProjectService() {
        return projectService;
    }
    
    /**
     * Gets the task service instance.
     * 
     * @return the task service
     */
    public static TaskService getTaskService() {
        return taskService;
    }
    
    /**
     * Gets the team member service instance.
     * 
     * @return the team member service
     */
    public static TeamMemberService getTeamMemberService() {
        return teamMemberService;
    }
    
    // Add getters for other services as we implement them
}