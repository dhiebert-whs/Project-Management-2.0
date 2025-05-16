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
 * Enhanced version with proper repository sharing and lifecycle management.
 */
public class ServiceLocator {
    
    private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class.getName());
    private static final Map<Class<?>, Object> SERVICES = new HashMap<>();
    private static final Map<Class<?>, Object> REPOSITORIES = new HashMap<>();
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
        
        // Register repositories first
        registerRepositories();
        
        // Register services
        registerServices();
        
        // Initialize and inject repositories into services
        injectRepositories();
        
        initialized = true;
        LOGGER.info("ServiceLocator initialization complete");
    }
    
    /**
     * Registers all repositories.
     */
    private static void registerRepositories() {
        LOGGER.info("Registering repositories...");
        
        // Create repository instances
        ProjectRepository projectRepo = new ProjectRepositoryImpl();
        TaskRepository taskRepo = new TaskRepositoryImpl();
        TeamMemberRepository teamMemberRepo = new TeamMemberRepositoryImpl();
        SubteamRepository subteamRepo = new SubteamRepositoryImpl();
        SubsystemRepository subsystemRepo = new SubsystemRepositoryImpl();
        ComponentRepository componentRepo = new ComponentRepositoryImpl();
        MeetingRepository meetingRepo = new MeetingRepositoryImpl();
        AttendanceRepository attendanceRepo = new AttendanceRepositoryImpl();
        MilestoneRepository milestoneRepo = new MilestoneRepositoryImpl();
        
        // Register repositories in both maps for flexibility
        register(ProjectRepository.class, projectRepo);
        register(TaskRepository.class, taskRepo);
        register(TeamMemberRepository.class, teamMemberRepo);
        register(SubteamRepository.class, subteamRepo);
        register(SubsystemRepository.class, subsystemRepo);
        register(ComponentRepository.class, componentRepo);
        register(MeetingRepository.class, meetingRepo);
        register(AttendanceRepository.class, attendanceRepo);
        register(MilestoneRepository.class, milestoneRepo);
        
        // Also store in repository map
        REPOSITORIES.put(ProjectRepository.class, projectRepo);
        REPOSITORIES.put(TaskRepository.class, taskRepo);
        REPOSITORIES.put(TeamMemberRepository.class, teamMemberRepo);
        REPOSITORIES.put(SubteamRepository.class, subteamRepo);
        REPOSITORIES.put(SubsystemRepository.class, subsystemRepo);
        REPOSITORIES.put(ComponentRepository.class, componentRepo);
        REPOSITORIES.put(MeetingRepository.class, meetingRepo);
        REPOSITORIES.put(AttendanceRepository.class, attendanceRepo);
        REPOSITORIES.put(MilestoneRepository.class, milestoneRepo);
    }
    
    /**
     * Registers all services.
     */
    private static void registerServices() {
        LOGGER.info("Registering services...");
        
        // Create service instances
        projectService = new ProjectServiceImpl();
        taskService = new TaskServiceImpl();
        teamMemberService = new TeamMemberServiceImpl();
        subteamService = new SubteamServiceImpl();
        subsystemService = new SubsystemServiceImpl();
        componentService = new ComponentServiceImpl();
        meetingService = new MeetingServiceImpl();
        attendanceService = new AttendanceServiceImpl();
        milestoneService = new MilestoneServiceImpl();
        dialogService = new JavaFXDialogService();
        ganttDataService = new GanttDataServiceImpl();
    
        // Get transformation service from Gantt data service
        if (ganttDataService != null && ganttDataService instanceof GanttDataServiceImpl) {
            transformationService = ((GanttDataServiceImpl) ganttDataService).getTransformationService();
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
        register(GanttChartTransformationService.class, transformationService);
    }
    
    /**
     * Injects repositories into services using reflection.
     * This ensures services share the same repository instances.
     */
    private static void injectRepositories() {
        LOGGER.info("Injecting repositories into services...");
        
        try {
            // Inject repositories into each service that needs them
            // ProjectService
            injectRepository(projectService, "projectRepository", ProjectRepository.class);
            
            // TaskService
            injectRepository(taskService, "taskRepository", TaskRepository.class);
            injectRepository(taskService, "projectRepository", ProjectRepository.class);
            
            // TeamMemberService
            injectRepository(teamMemberService, "teamMemberRepository", TeamMemberRepository.class);
            injectRepository(teamMemberService, "subteamRepository", SubteamRepository.class);
            
            // SubteamService
            injectRepository(subteamService, "subteamRepository", SubteamRepository.class);
            
            // SubsystemService
            injectRepository(subsystemService, "subsystemRepository", SubsystemRepository.class);
            
            // ComponentService
            injectRepository(componentService, "componentRepository", ComponentRepository.class);
            injectRepository(componentService, "taskRepository", TaskRepository.class);
            
            // MeetingService
            injectRepository(meetingService, "meetingRepository", MeetingRepository.class);
            injectRepository(meetingService, "projectRepository", ProjectRepository.class);
            
            // AttendanceService
            injectRepository(attendanceService, "attendanceRepository", AttendanceRepository.class);
            injectRepository(attendanceService, "meetingRepository", MeetingRepository.class);
            injectRepository(attendanceService, "teamMemberRepository", TeamMemberRepository.class);
            
            // MilestoneService
            injectRepository(milestoneService, "milestoneRepository", MilestoneRepository.class);
            injectRepository(milestoneService, "projectRepository", ProjectRepository.class);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error injecting repositories into services", e);
            throw new RuntimeException("Failed to inject repositories into services", e);
        }
    }
    
    /**
     * Helper method to inject a repository into a service using reflection.
     * 
     * @param service the service to inject into
     * @param fieldName the name of the field to inject
     * @param repositoryClass the class of the repository to inject
     */
    private static void injectRepository(Object service, String fieldName, Class<?> repositoryClass) {
        try {
            if (service == null) {
                LOGGER.warning("Service is null, cannot inject repository " + repositoryClass.getSimpleName());
                return;
            }
            
            // Get the repository to inject
            Object repository = REPOSITORIES.get(repositoryClass);
            if (repository == null) {
                LOGGER.warning("Repository not found: " + repositoryClass.getSimpleName());
                return;
            }
            
            // Use reflection to inject the repository
            java.lang.reflect.Field field = findField(service.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(service, repository);
                LOGGER.fine("Injected " + repositoryClass.getSimpleName() + " into " + 
                          service.getClass().getSimpleName() + "." + fieldName);
            } else {
                LOGGER.warning("Field not found: " + fieldName + " in " + service.getClass().getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error injecting repository " + repositoryClass.getSimpleName() + 
                     " into " + service.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Helper method to find a field in a class or its superclasses.
     * 
     * @param clazz the class to search
     * @param fieldName the name of the field to find
     * @return the field, or null if not found
     */
    private static java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Try superclass if exists
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, fieldName);
            }
            return null;
        }
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
        if (!initialized) initialize();
        return (T) SERVICES.get(serviceClass);
    }
    
    /**
     * Gets a repository instance by its interface class.
     * 
     * @param <T> the type of repository
     * @param repositoryClass the interface class of the repository
     * @return the repository instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRepository(Class<T> repositoryClass) {
        if (!initialized) initialize();
        return (T) REPOSITORIES.get(repositoryClass);
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
    
    public static GanttChartTransformationService getTransformationService() {
        if (!initialized) initialize();
        return transformationService;
    }
    
    // Repository getters
    
    public static ProjectRepository getProjectRepository() {
        if (!initialized) initialize();
        return getRepository(ProjectRepository.class);
    }
    
    public static TaskRepository getTaskRepository() {
        if (!initialized) initialize();
        return getRepository(TaskRepository.class);
    }
    
    public static TeamMemberRepository getTeamMemberRepository() {
        if (!initialized) initialize();
        return getRepository(TeamMemberRepository.class);
    }
    
    public static SubteamRepository getSubteamRepository() {
        if (!initialized) initialize();
        return getRepository(SubteamRepository.class);
    }
    
    public static SubsystemRepository getSubsystemRepository() {
        if (!initialized) initialize();
        return getRepository(SubsystemRepository.class);
    }
    
    public static ComponentRepository getComponentRepository() {
        if (!initialized) initialize();
        return getRepository(ComponentRepository.class);
    }
    
    public static MeetingRepository getMeetingRepository() {
        if (!initialized) initialize();
        return getRepository(MeetingRepository.class);
    }
    
    public static AttendanceRepository getAttendanceRepository() {
        if (!initialized) initialize();
        return getRepository(AttendanceRepository.class);
    }
    
    public static MilestoneRepository getMilestoneRepository() {
        if (!initialized) initialize();
        return getRepository(MilestoneRepository.class);
    }
    
    /**
     * Clears all registered services and repositories.
     * Primarily used for testing.
     */
    public static void clear() {
        SERVICES.clear();
        REPOSITORIES.clear();
        
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
        transformationService = null;
        
        initialized = false;
        LOGGER.info("ServiceLocator cleared");
    }
    
    /**
     * Returns whether the ServiceLocator has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Shut down the service locator and release resources.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down ServiceLocator");
        clear();
    }
}