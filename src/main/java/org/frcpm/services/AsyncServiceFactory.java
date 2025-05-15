// src/main/java/org/frcpm/services/AsyncServiceFactory.java

package org.frcpm.services;

import org.frcpm.services.impl.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating and managing async service instances.
 * Provides singleton instances of async services.
 * Updated for MVVMFx compatibility.
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
     */
    private static void initializeIfNeeded() {
        // Initialize services on first use
        if (projectService == null) {
            projectService = new ProjectServiceAsyncImpl();
        }
        
        if (taskService == null) {
            taskService = new TaskServiceAsyncImpl();
        }
        
        if (milestoneService == null) {
            milestoneService = new MilestoneServiceAsyncImpl();
        }
        
        if (teamMemberService == null) {
            teamMemberService = new TeamMemberServiceAsyncImpl();
        }
        
        if (subsystemService == null) {
            subsystemService = new SubsystemServiceAsyncImpl();
        }
        
        if (componentService == null) {
            componentService = new ComponentServiceAsyncImpl();
        }
        
        if (meetingService == null) {
            meetingService = new MeetingServiceAsyncImpl();
        }
        
        if (attendanceService == null) {
            attendanceService = new AttendanceServiceAsyncImpl();
        }
        
        if (subteamService == null) {
            subteamService = new SubteamServiceAsyncImpl();
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
            if (serviceType == ProjectService.class) {
                return (T) getProjectService();
            } else if (serviceType == TaskService.class) {
                return (T) getTaskService();
            } else if (serviceType == MilestoneService.class) {
                return (T) getMilestoneService();
            } else if (serviceType == SubsystemService.class) {
                return (T) getSubsystemService();
            } else if (serviceType == TeamMemberService.class) {
                return (T) getTeamMemberService();
            } else if (serviceType == ComponentService.class) {
                return (T) getComponentService();
            } else if (serviceType == MeetingService.class) {
                return (T) getMeetingService();
            } else if (serviceType == AttendanceService.class) {
                return (T) getAttendanceService();
            } else if (serviceType == SubteamService.class) {
                return (T) getSubteamService();
            } else {
                LOGGER.warning("Unknown service type: " + serviceType);
                return null;
            }
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
        // Ensure all services are initialized
        initializeIfNeeded();
    }

    /**
     * Shuts down all async services.
     * This method should be called during application shutdown.
     */
    public static void shutdown() {
        // Ensure TaskExecutor is properly shut down to release thread resources
        org.frcpm.async.TaskExecutor.shutdown();
        
        // Clear service instances
        clearAll();
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
    }
}