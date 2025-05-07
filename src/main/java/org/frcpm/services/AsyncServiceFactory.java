// src/main/java/org/frcpm/services/AsyncServiceFactory.java (updated)

package org.frcpm.services;

import org.frcpm.services.impl.ProjectServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * Factory class for creating service instances with async capabilities.
 * This complements the existing ServiceFactory by providing access to async-capable implementations.
 */
public class AsyncServiceFactory {
    
    private static final ProjectServiceAsyncImpl projectService = new ProjectServiceAsyncImpl();
    private static final TaskServiceAsyncImpl taskService = new TaskServiceAsyncImpl();
    
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