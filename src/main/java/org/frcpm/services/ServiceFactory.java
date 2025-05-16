// src/main/java/org/frcpm/services/ServiceFactory.java
package org.frcpm.services;

import org.frcpm.di.ServiceLocator;
import org.frcpm.services.impl.*;

/**
 * Factory class for creating service instances.
 * This follows the factory pattern to centralize service creation.
 * Updated to use ServiceLocator for MVVMFx compatibility.
 */
public class ServiceFactory {

    /**
     * Initializes all services.
     * This method should be called during application startup.
     */
    public static void initialize() {
        // Initialize ServiceLocator
        ServiceLocator.initialize();
    }
    
    /**
     * Gets the project service instance.
     * 
     * @return the project service
     */
    public static ProjectService getProjectService() {
        return ServiceLocator.getProjectService();
    }
    
    /**
     * Gets the task service instance.
     * 
     * @return the task service
     */
    public static TaskService getTaskService() {
        return ServiceLocator.getTaskService();
    }
    
    /**
     * Gets the team member service instance.
     * 
     * @return the team member service
     */
    public static TeamMemberService getTeamMemberService() {
        return ServiceLocator.getTeamMemberService();
    }
    
    /**
     * Gets the subteam service instance.
     * 
     * @return the subteam service
     */
    public static SubteamService getSubteamService() {
        return ServiceLocator.getSubteamService();
    }
    
    /**
     * Gets the subsystem service instance.
     * 
     * @return the subsystem service
     */
    public static SubsystemService getSubsystemService() {
        return ServiceLocator.getSubsystemService();
    }
    
    /**
     * Gets the component service instance.
     * 
     * @return the component service
     */
    public static ComponentService getComponentService() {
        return ServiceLocator.getComponentService();
    }
    
    /**
     * Gets the meeting service instance.
     * 
     * @return the meeting service
     */
    public static MeetingService getMeetingService() {
        return ServiceLocator.getMeetingService();
    }
    
    /**
     * Gets the attendance service instance.
     * 
     * @return the attendance service
     */
    public static AttendanceService getAttendanceService() {
        return ServiceLocator.getAttendanceService();
    }
    
    /**
     * Gets the milestone service instance.
     * 
     * @return the milestone service
     */
    public static MilestoneService getMilestoneService() {
        return ServiceLocator.getMilestoneService();
    }

    /**
     * Gets the dialog service.
     * 
     * @return the dialog service
     */
    public static DialogService getDialogService() {
        return ServiceLocator.getDialogService();
    }

    /**
     * Gets the Gantt data service instance.
     * 
     * @return the Gantt data service
     */
    public static GanttDataService getGanttDataService() {
        return ServiceLocator.getGanttDataService();
    }
    
    /**
     * Gets the transformation service instance.
     * 
     * @return the transformation service
     */
    public static GanttChartTransformationService getTransformationService() {
        return ServiceLocator.getTransformationService();
    }
    
    /**
     * Gets a service by its interface type.
     * 
     * @param <T> the service type
     * @param serviceType the service interface class
     * @return the service instance
     */
    public static <T> T getService(Class<T> serviceType) {
        return ServiceLocator.getService(serviceType);
    }
    
    /**
     * Shuts down all services.
     * This method should be called during application shutdown.
     */
    public static void shutdown() {
        ServiceLocator.shutdown();
    }
}