// src/main/java/org/frcpm/di/FrcpmModule.java

package org.frcpm.di;

import com.airhacks.afterburner.injection.Injector;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;
import org.frcpm.repositories.*;
import org.frcpm.repositories.impl.*;
import org.frcpm.repositories.specific.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Main module for AfterburnerFX dependency injection configuration.
 * Centralizes all service and repository registrations.
 */
public class FrcpmModule {
    
    private static final Logger LOGGER = Logger.getLogger(FrcpmModule.class.getName());
    private static final Map<Class<?>, Object> CUSTOM_SERVICES = new HashMap<>();
    
    /**
     * Configures and initializes the AfterburnerFX dependency injection system.
     */
    public static void initialize() {
        LOGGER.info("Initializing FrcpmModule for dependency injection");
        
        // Register all services and repositories
        registerDependencies();
        
        // Register custom producers
        Map<String, Object> customConfig = new HashMap<>();
        customConfig.put("application.name", "FRC Project Management System");
        customConfig.put("application.version", "2.0");
        
        // Configure afterburner injection system
        Injector.setConfigurationSource(customConfig::get);
        
        // Register custom producers for service resolution
        Injector.setInstanceSupplier(FrcpmModule::getCustomImplementation);
        
        LOGGER.info("FrcpmModule initialization complete");
    }
    
    /**
     * Registers all dependencies with the DI system.
     */
    private static void registerDependencies() {
        // Register repositories first
        registerSpecificRepositories();
        
        // Then register services that use repositories
        registerServices();
        
        LOGGER.info("Registered all dependencies");
    }
    
    /**
     * Registers all specific repositories.
     */
    private static void registerSpecificRepositories() {
        // Register repositories with interfaces
        register(ProjectRepository.class, RepositoryFactory.getProjectRepository());
        register(TaskRepository.class, RepositoryFactory.getTaskRepository());
        register(TeamMemberRepository.class, RepositoryFactory.getTeamMemberRepository());
        register(SubteamRepository.class, RepositoryFactory.getSubteamRepository());
        register(SubsystemRepository.class, RepositoryFactory.getSubsystemRepository());
        register(ComponentRepository.class, RepositoryFactory.getComponentRepository());
        register(MeetingRepository.class, RepositoryFactory.getMeetingRepository());
        register(AttendanceRepository.class, RepositoryFactory.getAttendanceRepository());
        register(MilestoneRepository.class, RepositoryFactory.getMilestoneRepository());
    }
    
    /**
     * Registers all services.
     */
    private static void registerServices() {
        // Register services with interfaces
        register(ProjectService.class, ServiceFactory.getProjectService());
        register(TaskService.class, ServiceFactory.getTaskService());
        register(TeamMemberService.class, ServiceFactory.getTeamMemberService());
        register(SubteamService.class, ServiceFactory.getSubteamService());
        register(SubsystemService.class, ServiceFactory.getSubsystemService());
        register(ComponentService.class, ServiceFactory.getComponentService());
        register(MeetingService.class, ServiceFactory.getMeetingService());
        register(AttendanceService.class, ServiceFactory.getAttendanceService());
        register(MilestoneService.class, ServiceFactory.getMilestoneService());
        register(DialogService.class, ServiceFactory.getDialogService());
        register(GanttDataService.class, ServiceFactory.getGanttDataService());
        register(WebViewBridgeService.class, ServiceFactory.getWebViewBridgeService());
        register(GanttChartTransformationService.class, ServiceFactory.getGanttDataService().getTransformationService());
    }
    
    /**
     * Registers a class with a specific implementation.
     * 
     * @param <T> the type of the class
     * @param clazz the class to register
     * @param implementation the implementation to use
     */
    public static <T> void register(Class<T> clazz, T implementation) {
        CUSTOM_SERVICES.put(clazz, implementation);
        LOGGER.fine("Registered: " + clazz.getSimpleName());
    }
    
    /**
     * Custom implementation provider for AfterburnerFX.
     * This method is called by the AfterburnerFX injector to resolve dependencies.
     * 
     * @param clazz the class to get an implementation for
     * @return the implementation or null if not found
     */
    private static Object getCustomImplementation(Class<?> clazz) {
        return CUSTOM_SERVICES.get(clazz);
    }
    
    /**
     * Shuts down the module and cleans up resources.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down FrcpmModule");
        CUSTOM_SERVICES.clear();
    }
}