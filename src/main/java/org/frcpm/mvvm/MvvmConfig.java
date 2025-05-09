// src/main/java/org/frcpm/mvvm/MvvmConfig.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;

import org.frcpm.repositories.specific.*;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;
import org.frcpm.di.ServiceProvider;

import java.util.logging.Logger;

/**
 * Configuration class for MVVMFx framework integration.
 * Centralizes dependency injection and notification setup.
 */
public class MvvmConfig {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmConfig.class.getName());
    
    // Global notification message types (use strings instead of NotificationType class)
    public static final String PROJECT_SAVED = "PROJECT_SAVED";
    public static final String PROJECT_DELETED = "PROJECT_DELETED";
    public static final String PROJECT_SELECTED = "PROJECT_SELECTED";
    public static final String PROJECT_CREATED = "PROJECT_CREATED";
    public static final String PROJECT_OPENED = "PROJECT_OPENED";
    public static final String TASK_CREATED = "TASK_CREATED";
    public static final String TASK_UPDATED = "TASK_UPDATED";
    public static final String TASK_DELETED = "TASK_DELETED";
    
    /**
     * Initializes the MVVMFx framework with required services and factories.
     */
    public static void initialize() {
        LOGGER.info("Initializing MVVMFx framework");
        
        // Configure dependency injection
        registerServices();
        setupNotifications();
        
        LOGGER.info("MVVMFx framework initialized successfully");
    }
    
    /**
     * Registers all services with the MVVMFx dependency injection system.
     */
    private static void registerServices() {
        // Register service implementations with MVVMFx's dependency injector
        MvvmFX.setCustomDependencyInjector(type -> {
            try {
                // Services
                if (type == ProjectService.class) {
                    return ServiceProvider.getProjectService();
                } else if (type == TaskService.class) {
                    return ServiceProvider.getTaskService();
                } else if (type == TeamMemberService.class) {
                    return ServiceProvider.getTeamMemberService();
                } else if (type == SubteamService.class) {
                    return ServiceProvider.getSubteamService();
                } else if (type == SubsystemService.class) {
                    return ServiceProvider.getSubsystemService();
                } else if (type == ComponentService.class) {
                    return ServiceProvider.getComponentService();
                } else if (type == MeetingService.class) {
                    return ServiceProvider.getMeetingService();
                } else if (type == AttendanceService.class) {
                    return ServiceProvider.getAttendanceService();
                } else if (type == MilestoneService.class) {
                    return ServiceProvider.getMilestoneService();
                } else if (type == DialogService.class) {
                    return ServiceProvider.getDialogService();
                } else if (type == GanttDataService.class) {
                    return ServiceProvider.getGanttDataService();
                } else if (type == GanttChartTransformationService.class) {
                    GanttDataService ganttDataService = ServiceProvider.getGanttDataService();
                    if (ganttDataService != null) {
                        return ganttDataService.getTransformationService();
                    }
                } else if (type == WebViewBridgeService.class) {
                    return ServiceProvider.getWebViewBridgeService();
                }
                
                // Repositories
                else if (type == ProjectRepository.class) {
                    return ServiceProvider.getProjectRepository();
                } else if (type == TaskRepository.class) {
                    return ServiceProvider.getTaskRepository();
                } else if (type == TeamMemberRepository.class) {
                    return ServiceProvider.getTeamMemberRepository();
                } else if (type == SubteamRepository.class) {
                    return ServiceProvider.getSubteamRepository();
                } else if (type == SubsystemRepository.class) {
                    return ServiceProvider.getSubsystemRepository();
                } else if (type == ComponentRepository.class) {
                    return ServiceProvider.getComponentRepository();
                } else if (type == MeetingRepository.class) {
                    return ServiceProvider.getMeetingRepository();
                } else if (type == AttendanceRepository.class) {
                    return ServiceProvider.getAttendanceRepository();
                } else if (type == MilestoneRepository.class) {
                    return ServiceProvider.getMilestoneRepository();
                }
                
                // ViewModels
                // This will be expanded as more ViewModels are created
                
                // Return null for unknown types
                return null;
            } catch (Exception e) {
                LOGGER.severe("Error resolving dependency for type: " + type.getName() + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Sets up the global notification system.
     */
    private static void setupNotifications() {
        // Get the notification center
        NotificationCenter notificationCenter = MvvmFX.getNotificationCenter();
        
        // Log notification setup
        LOGGER.info("Setting up global notification system");
        
        // Define notification observers
        NotificationObserver projectSavedObserver = (key, payload) -> {
            LOGGER.fine("Notification received: PROJECT_SAVED");
        };
        
        NotificationObserver projectDeletedObserver = (key, payload) -> {
            LOGGER.fine("Notification received: PROJECT_DELETED");
        };
        
        NotificationObserver projectSelectedObserver = (key, payload) -> {
            LOGGER.fine("Notification received: PROJECT_SELECTED");
        };
        
        // Register global observers for debugging
        notificationCenter.subscribe(PROJECT_SAVED, projectSavedObserver);
        notificationCenter.subscribe(PROJECT_DELETED, projectDeletedObserver);
        notificationCenter.subscribe(PROJECT_SELECTED, projectSelectedObserver);
    }
    
    /**
     * Creates a new MVVMFx application instance with the correct configuration.
     * This will be used for gradual migration to MVVMFx.
     */
    public static void configureMvvmFxApplication() {
        LOGGER.info("Configuring MVVMFx application entry point");
        
        // Additional configuration for the application
        // This would be used by a new MvvmFxMainApp class
    }
    
    /**
     * Shuts down the MVVMFx framework.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down MVVMFx framework");
        
        // Unsubscribe from notifications if needed
        NotificationCenter notificationCenter = MvvmFX.getNotificationCenter();
        
        // Perform any needed cleanup
        LOGGER.info("MVVMFx framework shut down successfully");
    }
}