// src/main/java/org/frcpm/di/ServiceLocator.java
package org.frcpm.di;

import org.frcpm.services.*;
import org.frcpm.services.impl.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.repositories.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service locator to replace AfterburnerFX dependency injection.
 * This centralizes service instantiation and access for the MVVMFx architecture.
 */
public class ServiceLocator {
    
    private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class.getName());
    private static final Map<Class<?>, Object> SERVICES = new HashMap<>();
    private static boolean initialized = false;
    
    // Service instances
    private static ProjectService projectService;
    private static TaskService taskService;
    private static TeamMemberService teamMemberService;
    private static SubteamService subteamService;
    private static SubsystemService subsystemService;
    private static ComponentService componentService;
    private static MeetingService meetingService;
    private static AttendanceService attendanceService;
    private static MilestoneService milestoneService;
    private static DialogService dialogService;
    private static GanttDataService ganttDataService;
    private static WebViewBridgeService webViewBridgeService;
    private static GanttChartTransformationService transformationService;
    
    /**
     * Initializes the service locator by registering all services.
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.info("ServiceLocator already initialized");
            return;
        }
        
        LOGGER.info("Initializing ServiceLocator");
        
        // Register repositories
        registerRepositories();
        
        // Register services
        registerServices();
        
        initialized = true;
        LOGGER.info("ServiceLocator initialization complete");
    }
    
    /**
     * Registers all repositories.
     */
    private static void registerRepositories() {
        ProjectRepository projectRepo = new ProjectRepositoryImpl();
        TaskRepository taskRepo = new TaskRepositoryImpl();
        TeamMemberRepository teamMemberRepo = new TeamMemberRepositoryImpl();
        SubteamRepository subteamRepo = new SubteamRepositoryImpl();
        SubsystemRepository subsystemRepo = new SubsystemRepositoryImpl();
        ComponentRepository componentRepo = new ComponentRepositoryImpl();
        MeetingRepository meetingRepo = new MeetingRepositoryImpl();
        AttendanceRepository attendanceRepo = new AttendanceRepositoryImpl();
        MilestoneRepository milestoneRepo = new MilestoneRepositoryImpl();
        
        register(ProjectRepository.class, projectRepo);
        register(TaskRepository.class, taskRepo);
        register(TeamMemberRepository.class, teamMemberRepo);
        register(SubteamRepository.class, subteamRepo);
        register(SubsystemRepository.class, subsystemRepo);
        register(ComponentRepository.class, componentRepo);
        register(MeetingRepository.class, meetingRepo);
        register(AttendanceRepository.class, attendanceRepo);
        register(MilestoneRepository.class, milestoneRepo);
    }
    
    /**
     * Registers all services.
     */
    private static void registerServices() {
        // Create service instances
        projectService = new ProjectServiceImpl(getProjectRepository());
        taskService = new TaskServiceImpl(getTaskRepository());
        teamMemberService = new TeamMemberServiceImpl(getTeamMemberRepository());
        subteamService = new SubteamServiceImpl(getSubteamRepository());
        subsystemService = new SubsystemServiceImpl(getSubsystemRepository());
        componentService = new ComponentServiceImpl(getComponentRepository());
        meetingService = new MeetingServiceImpl(getMeetingRepository());
        attendanceService = new AttendanceServiceImpl(getAttendanceRepository());
        milestoneService = new MilestoneServiceImpl(getMilestoneRepository());
        dialogService = new JavaFXDialogService();
        ganttDataService = new GanttDataServiceImpl();
        webViewBridgeService = new WebViewBridgeServiceImpl();
        
        // Get transformation service from Gantt data service
        if (ganttDataService != null) {
            transformationService = ganttDataService.getTransformationService();
        }
        
        // Register services
        register(ProjectService.class, projectService);
        register(TaskService.class, taskService);
        register(TeamMemberService.class, teamMemberService);
        register(SubteamService.class, subteamService);
        register(SubsystemService.class, subsystemService);
        register(ComponentService.class, componentService);
        register(MeetingService.class, meetingService);
        register(AttendanceService.class, attendanceService);
        register(MilestoneService.class, milestoneService);
        register(DialogService.class, dialogService);
        register(GanttDataService.class, ganttDataService);
        register(WebViewBridgeService.class, webViewBridgeService);
        register(GanttChartTransformationService.class, transformationService);
    }
    
    /**
     * Registers a class with a specific implementation.
     * 
     * @param <T> the type of the class
     * @param clazz the class to register
     * @param implementation the implementation to use
     */
    public static <T> void register(Class<T> clazz, T implementation) {
        SERVICES.put(clazz, implementation);
        LOGGER.fine("Registered: " + clazz.getSimpleName());
    }
    
    /**
     * Gets a service instance by its interface class.
     * 
     * @param <T> the type of service
     * @param serviceClass the interface class of the service
     * @return the service instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        return (T) SERVICES.get(serviceClass);
    }
    
    // Convenience methods for common services
    
    public static ProjectService getProjectService() {
        if (!initialized) initialize();
        return projectService;
    }
    
    public static TaskService getTaskService() {
        if (!initialized) initialize();
        return taskService;
    }
    
    public static TeamMemberService getTeamMemberService() {
        if (!initialized) initialize();
        return teamMemberService;
    }
    
    public static SubteamService getSubteamService() {
        if (!initialized) initialize();
        return subteamService;
    }
    
    public static SubsystemService getSubsystemService() {
        if (!initialized) initialize();
        return subsystemService;
    }
    
    public static ComponentService getComponentService() {
        if (!initialized) initialize();
        return componentService;
    }
    
    public static MeetingService getMeetingService() {
        if (!initialized) initialize();
        return meetingService;
    }
    
    public static AttendanceService getAttendanceService() {
        if (!initialized) initialize();
        return attendanceService;
    }
    
    public static MilestoneService getMilestoneService() {
        if (!initialized) initialize();
        return milestoneService;
    }
    
    public static DialogService getDialogService() {
        if (!initialized) initialize();
        return dialogService;
    }
    
    public static GanttDataService getGanttDataService() {
        if (!initialized) initialize();
        return ganttDataService;
    }
    
    public static WebViewBridgeService getWebViewBridgeService() {
        if (!initialized) initialize();
        return webViewBridgeService;
    }
    
    public static GanttChartTransformationService getTransformationService() {
        if (!initialized) initialize();
        return transformationService;
    }
    
    // Repository getters
    
    public static ProjectRepository getProjectRepository() {
        if (!initialized) initialize();
        return getService(ProjectRepository.class);
    }
    
    public static TaskRepository getTaskRepository() {
        if (!initialized) initialize();
        return getService(TaskRepository.class);
    }
    
    public static TeamMemberRepository getTeamMemberRepository() {
        if (!initialized) initialize();
        return getService(TeamMemberRepository.class);
    }
    
    public static SubteamRepository getSubteamRepository() {
        if (!initialized) initialize();
        return getService(SubteamRepository.class);
    }
    
    public static SubsystemRepository getSubsystemRepository() {
        if (!initialized) initialize();
        return getService(SubsystemRepository.class);
    }
    
    public static ComponentRepository getComponentRepository() {
        if (!initialized) initialize();
        return getService(ComponentRepository.class);
    }
    
    public static MeetingRepository getMeetingRepository() {
        if (!initialized) initialize();
        return getService(MeetingRepository.class);
    }
    
    public static AttendanceRepository getAttendanceRepository() {
        if (!initialized) initialize();
        return getService(AttendanceRepository.class);
    }
    
    public static MilestoneRepository getMilestoneRepository() {
        if (!initialized) initialize();
        return getService(MilestoneRepository.class);
    }
    
    /**
     * Clears all registered services and repositories.
     */
    public static void clear() {
        SERVICES.clear();
        
        // Clear service instances
        projectService = null;
        taskService = null;
        teamMemberService = null;
        subteamService = null;
        subsystemService = null;
        componentService = null;
        meetingService = null;
        attendanceService = null;
        milestoneService = null;
        dialogService = null;
        ganttDataService = null;
        webViewBridgeService = null;
        transformationService = null;
        
        initialized = false;
        LOGGER.info("ServiceLocator cleared");
    }
    
    /**
     * Shut down the service locator and release resources.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down ServiceLocator");
        clear();
    }
}