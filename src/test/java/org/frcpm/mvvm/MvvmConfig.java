// src/main/java/org/frcpm/mvvm/MvvmConfig.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;

import java.util.logging.Logger;

/**
 * Configuration class for MVVMFx framework integration.
 * Centralizes dependency injection and notification setup.
 */
public class MvvmConfig {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmConfig.class.getName());
    
    /**
     * Initializes the MVVMFx framework with required services and factories.
     */
    public static void initialize() {
        LOGGER.info("Initializing MVVMFx framework");
        
        // Configure dependency injection
        registerServices();
        registerFactories();
        setupNotifications();
        
        LOGGER.info("MVVMFx framework initialized successfully");
    }
    
    /**
     * Registers all services with the MVVMFx dependency injection system.
     */
    private static void registerServices() {
        // Register service implementations with MVVMFx's dependency injector
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == ProjectService.class) {
                return ServiceFactory.getProjectService();
            } else if (type == TaskService.class) {
                return ServiceFactory.getTaskService();
            } else if (type == TeamMemberService.class) {
                return ServiceFactory.getTeamMemberService();
            } else if (type == SubteamService.class) {
                return ServiceFactory.getSubteamService();
            } else if (type == SubsystemService.class) {
                return ServiceFactory.getSubsystemService();
            } else if (type == ComponentService.class) {
                return ServiceFactory.getComponentService();
            } else if (type == MeetingService.class) {
                return ServiceFactory.getMeetingService();
            } else if (type == AttendanceService.class) {
                return ServiceFactory.getAttendanceService();
            } else if (type == MilestoneService.class) {
                return ServiceFactory.getMilestoneService();
            } else if (type == DialogService.class) {
                return ServiceFactory.getDialogService();
            } else if (type == GanttDataService.class) {
                return ServiceFactory.getGanttDataService();
            }
            
            // Return null for unknown types
            return null;
        });
    }
    
    /**
     * Registers factory classes with the MVVMFx system.
     */
    private static void registerFactories() {
        // Register additional factories or singleton objects if needed
    }
    
    /**
     * Sets up the global notification system.
     */
    private static void setupNotifications() {
        // Get the notification center
        NotificationCenter notificationCenter = MvvmFX.getNotificationCenter();
        
        // Define global notification types
        // Example: PROJECT_SAVED, PROJECT_DELETED, etc.
    }
    
    /**
     * Shuts down the MVVMFx framework.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down MVVMFx framework");
        // Perform any necessary cleanup
    }
}