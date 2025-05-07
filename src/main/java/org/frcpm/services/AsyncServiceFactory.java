// Updated section of src/main/java/org/frcpm/services/AsyncServiceFactory.java

package org.frcpm.services;

import org.frcpm.services.impl.MilestoneServiceAsyncImpl;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * Factory class for creating service instances with async capabilities.
 * This complements the existing ServiceFactory by providing access to async-capable implementations.
 */
public class AsyncServiceFactory {
    
    private static final ProjectServiceAsyncImpl projectService = new ProjectServiceAsyncImpl();
    private static final TaskServiceAsyncImpl taskService = new TaskServiceAsyncImpl();
    private static final MilestoneServiceAsyncImpl milestoneService = new MilestoneServiceAsyncImpl();
    private static final TeamMemberServiceAsyncImpl teamMemberService = new TeamMemberServiceAsyncImpl();
    private static final SubsystemServiceAsyncImpl subsystemService = new SubsystemServiceAsyncImpl();
    
    // More async service implementations will be added here as they are created
    
    /**
     * Gets the project service with async capabilities.
     * 
     * @return the project service with async methods
     */
    public static ProjectServiceAsyncImpl getProjectService() {
        return projectService;
    }
    
    /**
     * Gets the task service with async capabilities.
     * 
     * @return the task service with async methods
     */
    public static TaskServiceAsyncImpl getTaskService() {
        return taskService;
    }
    
/**
     * Gets the milestone service with async capabilities.
     * 
     * @return the milestone service with async methods
     */
    public static MilestoneServiceAsyncImpl getMilestoneService() {
        return milestoneService;
    }
    
    /**
     * Gets the team member service with async capabilities.
     * 
     * @return the team member service with async methods
     */
    public static TeamMemberServiceAsyncImpl getTeamMemberService() {
        return teamMemberService;
    }
    
    /**
     * Gets the subsystem service with async capabilities.
     * 
     * @return the subsystem service with async methods
     */
    public static SubsystemServiceAsyncImpl getSubsystemService() {
        return subsystemService;
    }
    
    /**
     * Initializes all async services.
     * This method should be called during application startup.
     */
    public static void initialize() {
        // Initialization logic for async services can be added here if needed
    }
    
    /**
     * Shuts down all async services.
     * This method should be called during application shutdown.
     */
    public static void shutdown() {
        // Ensure TaskExecutor is properly shut down to release thread resources
        org.frcpm.async.TaskExecutor.shutdown();
    }
}