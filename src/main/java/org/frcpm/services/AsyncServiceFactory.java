// src/main/java/org/frcpm/services/AsyncServiceFactory.java
package org.frcpm.services;

import org.frcpm.async.TaskExecutor;
import org.frcpm.services.impl.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating and managing async service instances.
 * Provides singleton instances of async services.
 * Updated to integrate with the task-based threading model and MVVMFx compatibility.
 */
public class AsyncServiceFactory {

    private static final Logger LOGGER = Logger.getLogger(AsyncServiceFactory.class.getName());
    private static final ConcurrentHashMap<Class<?>, Object> SERVICE_INSTANCES = new ConcurrentHashMap<>();

    // Singleton instances of async services
    private static ProjectServiceAsyncImpl projectService;
    private static TaskServiceAsyncImpl taskService;
    private static MilestoneServiceAsyncImpl milestoneService;
    private static TeamMemberServiceAsyncImpl teamMemberService;
    private static SubsystemServiceAsyncImpl subsystemService;
    private static ComponentServiceAsyncImpl componentService;
    private static MeetingServiceAsyncImpl meetingService;
    private static AttendanceServiceAsyncImpl attendanceService;
    private static SubteamServiceAsyncImpl subteamService;

    // Private constructor to prevent instantiation
    private AsyncServiceFactory() {
    }

    /**
     * Initializes async service instances if needed.
     * Ensures each service is properly set up with the task-based threading model.
     */
    private static void initializeIfNeeded() {
        LOGGER.info("Initializing async services");
        
        // Initialize services on first use
        if (projectService == null) {
            projectService = new ProjectServiceAsyncImpl();
            configureAsyncService(projectService);
        }
        
        if (taskService == null) {
            taskService = new TaskServiceAsyncImpl();
            configureAsyncService(taskService);
        }
        
        if (milestoneService == null) {
            milestoneService = new MilestoneServiceAsyncImpl();
            configureAsyncService(milestoneService);
        }
        
        if (teamMemberService == null) {
            teamMemberService = new TeamMemberServiceAsyncImpl();
            configureAsyncService(teamMemberService);
        }
        
        if (subsystemService == null) {
            subsystemService = new SubsystemServiceAsyncImpl();
            configureAsyncService(subsystemService);
        }
        
        if (componentService == null) {
            componentService = new ComponentServiceAsyncImpl();
            configureAsyncService(componentService);
        }
        
        if (meetingService == null) {
            meetingService = new MeetingServiceAsyncImpl();
            configureAsyncService(meetingService);
        }
        
        if (attendanceService == null) {
            attendanceService = new AttendanceServiceAsyncImpl();
            configureAsyncService(attendanceService);
        }
        
        if (subteamService == null) {
            subteamService = new SubteamServiceAsyncImpl();
            configureAsyncService(subteamService);
        }

        LOGGER.info("Async services initialization complete");
    }
    
    /**
     * Configures an async service with task executor and any other settings.
     * 
     * @param service the service to configure
     */
    private static void configureAsyncService(AbstractAsyncService<?, ?, ?> service) {
        try {
            // Use reflection to set task executor or any configuration needed
            java.lang.reflect.Method configureMethod = AbstractAsyncService.class.getDeclaredMethod("configureExecutor");
            if (configureMethod != null) {
                configureMethod.setAccessible(true);
                configureMethod.invoke(service);
            }
        } catch (Exception e) {
            // If method doesn't exist or other error, just log it - old services may not have this method
            LOGGER.log(Level.WARNING, "Could not configure service with TaskExecutor: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the project service with async capabilities.
     * 
     * @return the project service with async methods
     */
    public static ProjectServiceAsyncImpl getProjectService() {
        initializeIfNeeded();
        return projectService;
    }

    /**
     * Gets the task service with async capabilities.
     * 
     * @return the task service with async methods
     */
    public static TaskServiceAsyncImpl getTaskService() {
        initializeIfNeeded();
        return taskService;
    }

    /**
     * Gets the milestone service with async capabilities.
     * 
     * @return the milestone service with async methods
     */
    public static MilestoneServiceAsyncImpl getMilestoneService() {
        initializeIfNeeded();
        return milestoneService;
    }

    /**
     * Gets the team member service with async capabilities.
     * 
     * @return the team member service with async methods
     */
    public static TeamMemberServiceAsyncImpl getTeamMemberService() {
        initializeIfNeeded();
        return teamMemberService;
    }

    /**
     * Gets the subsystem service with async capabilities.
     * 
     * @return the subsystem service with async methods
     */
    public static SubsystemServiceAsyncImpl getSubsystemService() {
        initializeIfNeeded();
        return subsystemService;
    }

    /**
     * Gets the component service with async capabilities.
     * 
     * @return the component service with async methods
     */
    public static ComponentServiceAsyncImpl getComponentService() {
        initializeIfNeeded();
        return componentService;
    }

    /**
     * Gets the meeting service with async capabilities.
     * 
     * @return the meeting service with async methods
     */
    public static MeetingServiceAsyncImpl getMeetingService() {
        initializeIfNeeded();
        return meetingService;
    }

    /**
     * Gets the attendance service with async capabilities.
     * 
     * @return the attendance service with async methods
     */
    public static AttendanceServiceAsyncImpl getAttendanceService() {
        initializeIfNeeded();
        return attendanceService;
    }

    /**
     * Gets the subteam service with async capabilities.
     * 
     * @return the subteam service with async methods
     */
    public static SubteamServiceAsyncImpl getSubteamService() {
        initializeIfNeeded();
        return subteamService;
    }

    /**
     * Generic method to get async service by type.
     * 
     * @param <T>         the service type
     * @param serviceType the service class
     * @return the async service implementation
     */
    @SuppressWarnings("unchecked")
    public static <T> T getServiceAsync(Class<T> serviceType) {
        try {
            // First, check if we have the service in our cache
            T cachedService = (T) SERVICE_INSTANCES.get(serviceType);
            if (cachedService != null) {
                return cachedService;
            }
            
            // Otherwise, get the appropriate service
            T service = null;
            
            if (serviceType == ProjectService.class) {
                service = (T) getProjectService();
            } else if (serviceType == TaskService.class) {
                service = (T) getTaskService();
            } else if (serviceType == MilestoneService.class) {
                service = (T) getMilestoneService();
            } else if (serviceType == SubsystemService.class) {
                service = (T) getSubsystemService();
            } else if (serviceType == TeamMemberService.class) {
                service = (T) getTeamMemberService();
            } else if (serviceType == ComponentService.class) {
                service = (T) getComponentService();
            } else if (serviceType == MeetingService.class) {
                service = (T) getMeetingService();
            } else if (serviceType == AttendanceService.class) {
                service = (T) getAttendanceService();
            } else if (serviceType == SubteamService.class) {
                service = (T) getSubteamService();
            } else {
                LOGGER.warning("Unknown service type: " + serviceType);
                return null;
            }
            
            // Store in cache for future use
            if (service != null) {
                SERVICE_INSTANCES.put(serviceType, service);
            }
            
            return service;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating async service for type: " + serviceType, e);
            return null;
        }
    }

    /**
     * Initializes all async services.
     * This method should be called during application startup.
     */
    public static void initialize() {
        LOGGER.info("Initializing all async services");
        
        // Ensure all services are initialized
        initializeIfNeeded();
        
        // Initialize TaskExecutor if needed
        TaskExecutor.executeAsync("AsyncServiceFactory-Init", 
            () -> "Initialization completed", 
            result -> LOGGER.info("TaskExecutor successfully used: " + result),
            error -> LOGGER.log(Level.SEVERE, "Error initializing TaskExecutor", error)
        );
    }

    /**
     * Shuts down all async services.
     * This method should be called during application shutdown.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down async services");
        
        // Ensure TaskExecutor is properly shut down to release thread resources
        TaskExecutor.shutdown();
        
        // Clear service instances
        clearAll();
        
        LOGGER.info("Async services shutdown complete");
    }

    /**
     * Clears all service instances.
     * Primarily used for testing.
     */
    public static void clearAll() {
        SERVICE_INSTANCES.clear();
        
        // Reset service instances
        projectService = null;
        taskService = null;
        milestoneService = null;
        teamMemberService = null;
        subsystemService = null;
        componentService = null;
        meetingService = null;
        attendanceService = null;
        subteamService = null;
        
        LOGGER.info("Async service instances cleared");
    }
}