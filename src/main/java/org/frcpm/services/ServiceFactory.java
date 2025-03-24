package org.frcpm.services;

import org.frcpm.services.impl.*;

/**
 * Factory class for creating service instances.
 * This follows the factory pattern to centralize service creation.
 */
public class ServiceFactory {
    
    private static final ProjectService projectService = new ProjectServiceImpl();
    // Add other services as we implement them
    
    /**
     * Gets the project service instance.
     * 
     * @return the project service
     */
    public static ProjectService getProjectService() {
        return projectService;
    }
    
    // Add getters for other services as we implement them
}