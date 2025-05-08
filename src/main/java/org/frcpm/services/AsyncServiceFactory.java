// src/main/java/org/frcpm/services/AsyncServiceFactory.java

package org.frcpm.services;

import org.frcpm.services.impl.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating and managing async service instances.
 * Provides singleton instances of async services.
 */
public class AsyncServiceFactory {

    private static final Logger LOGGER = Logger.getLogger(AsyncServiceFactory.class.getName());
    private static final ConcurrentHashMap<Class<?>, Object> SERVICE_INSTANCES = new ConcurrentHashMap<>();

    // Singleton instances of async services
    private static final ProjectServiceAsyncImpl projectService = new ProjectServiceAsyncImpl();
    private static final TaskServiceAsyncImpl taskService = new TaskServiceAsyncImpl();
    private static final MilestoneServiceAsyncImpl milestoneService = new MilestoneServiceAsyncImpl();
    private static final TeamMemberServiceAsyncImpl teamMemberService = new TeamMemberServiceAsyncImpl();
    private static final SubsystemServiceAsyncImpl subsystemService = new SubsystemServiceAsyncImpl();
    private static final ComponentServiceAsyncImpl componentService = new ComponentServiceAsyncImpl();
    private static final MeetingServiceAsyncImpl meetingService = new MeetingServiceAsyncImpl();
    private static final AttendanceServiceAsyncImpl attendanceService = new AttendanceServiceAsyncImpl();

    // Private constructor to prevent instantiation
    private AsyncServiceFactory() {
    }

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
     * Gets the component service with async capabilities.
     * 
     * @return the component service with async methods
     */
    public static ComponentServiceAsyncImpl getComponentService() {
        return componentService;
    }

    /**
     * Gets the meeting service with async capabilities.
     * 
     * @return the meeting service with async methods
     */
    public static MeetingServiceAsyncImpl getMeetingService() {
        return meetingService;
    }

    /**
     * Gets the attendance service with async capabilities.
     * 
     * @return the attendance service with async methods
     */
    public static AttendanceServiceAsyncImpl getAttendanceService() {
        return attendanceService;
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

    /**
     * Clears all service instances.
     * Primarily used for testing.
     */
    public static void clearAll() {
        SERVICE_INSTANCES.clear();
    }
}