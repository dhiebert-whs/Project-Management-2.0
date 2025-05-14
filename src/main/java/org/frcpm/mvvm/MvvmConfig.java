// src/main/java/org/frcpm/mvvm/MvvmConfig.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;

import org.frcpm.repositories.specific.*;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;
import org.frcpm.di.ServiceProvider;
import org.frcpm.mvvm.viewmodels.AttendanceMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ComponentDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ComponentListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.DailyMvvmViewModel;
import org.frcpm.mvvm.viewmodels.DashboardMvvmViewModel;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MainMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MeetingDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MeetingListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MetricsMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MilestoneDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MilestoneListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubsystemDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubsystemListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubteamDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubteamListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskSelectionMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TeamMemberDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TeamMemberListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TeamMemberSelectionMvvmViewModel;

import java.util.ResourceBundle;
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
        
        // Configure resource bundle path for MVVMFx framework
        try {
            // Set up a global resource bundle with common properties
            ResourceBundle commonBundle = ResourceBundle.getBundle("org.frcpm.mvvm.views.common");
            MvvmFX.setGlobalResourceBundle(commonBundle);
            
            // Configure the default path for resource bundles
            System.setProperty("mvvmfx.resource.bundle.path", "org.frcpm.mvvm.views");
        } catch (Exception e) {
            LOGGER.warning("Could not set up resource bundles: " + e.getMessage());
        }
        
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
                else if (type == AttendanceMvvmViewModel.class) {
                    return new AttendanceMvvmViewModel(
                        ServiceProvider.getAttendanceService(),
                        ServiceProvider.getTeamMemberService(),
                        ServiceProvider.getMeetingService());
                } else if (type == ComponentDetailMvvmViewModel.class) {
                    return new ComponentDetailMvvmViewModel(
                        ServiceProvider.getComponentService(),
                        ServiceProvider.getTaskService());
                } else if (type == ComponentListMvvmViewModel.class) {
                    return new ComponentListMvvmViewModel(
                        ServiceProvider.getComponentService());
                } else if (type == DailyMvvmViewModel.class) {
                    return new DailyMvvmViewModel(
                        ServiceProvider.getTaskService(),
                        ServiceProvider.getMeetingService());
                } else if (type == DashboardMvvmViewModel.class) {
                    return new DashboardMvvmViewModel(
                        ServiceProvider.getTaskService(),
                        ServiceProvider.getMilestoneService(),
                        ServiceProvider.getMeetingService());
                } else if (type == GanttChartMvvmViewModel.class) {
                    return new GanttChartMvvmViewModel(
                        ServiceProvider.getGanttDataService());
                } else if (type == MainMvvmViewModel.class) {
                    return new MainMvvmViewModel(
                        ServiceProvider.getProjectService());
                } else if (type == MeetingDetailMvvmViewModel.class) {
                    return new MeetingDetailMvvmViewModel(
                        ServiceProvider.getMeetingService());
                } else if (type == MeetingListMvvmViewModel.class) {
                    return new MeetingListMvvmViewModel(
                        ServiceProvider.getMeetingService());
                } else if (type == MetricsMvvmViewModel.class) {
                    return new MetricsMvvmViewModel(
                        ServiceProvider.getProjectService(),
                        ServiceProvider.getSubsystemService(),
                        ServiceProvider.getTeamMemberService());
                } else if (type == MilestoneDetailMvvmViewModel.class) {
                    return new MilestoneDetailMvvmViewModel(
                        ServiceProvider.getMilestoneService());
                } else if (type == MilestoneListMvvmViewModel.class) {
                    return new MilestoneListMvvmViewModel(
                        ServiceProvider.getMilestoneService());
                } else if (type == ProjectListMvvmViewModel.class) {
                    return new ProjectListMvvmViewModel(
                        ServiceProvider.getProjectService());
                } else if (type == SubsystemDetailMvvmViewModel.class) {
                    return new SubsystemDetailMvvmViewModel(
                        ServiceProvider.getSubsystemService(),
                        ServiceProvider.getSubteamService(),
                        ServiceProvider.getTaskService());
                } else if (type == SubsystemListMvvmViewModel.class) {
                    return new SubsystemListMvvmViewModel(
                        ServiceProvider.getSubsystemService());
                } else if (type == SubteamDetailMvvmViewModel.class) {
                    return new SubteamDetailMvvmViewModel(
                        ServiceProvider.getSubteamService(),
                        ServiceProvider.getTeamMemberService());
                } else if (type == SubteamListMvvmViewModel.class) {
                    return new SubteamListMvvmViewModel(
                        ServiceProvider.getSubteamService());
                } else if (type == TaskDetailMvvmViewModel.class) {
                    return new TaskDetailMvvmViewModel(
                        ServiceProvider.getTaskService(),
                        ServiceProvider.getTeamMemberService(),
                        ServiceProvider.getComponentService());
                } else if (type == TaskListMvvmViewModel.class) {
                    return new TaskListMvvmViewModel(
                        ServiceProvider.getTaskService());
                } else if (type == TaskSelectionMvvmViewModel.class) {
                    return new TaskSelectionMvvmViewModel(
                        ServiceProvider.getTaskService());
                } else if (type == TeamMemberDetailMvvmViewModel.class) {
                    return new TeamMemberDetailMvvmViewModel(
                        ServiceProvider.getTeamMemberService(),
                        ServiceProvider.getSubteamService());
                } else if (type == TeamMemberListMvvmViewModel.class) {
                    return new TeamMemberListMvvmViewModel(
                        ServiceProvider.getTeamMemberService());
                } else if (type == TeamMemberSelectionMvvmViewModel.class) {
                    return new TeamMemberSelectionMvvmViewModel(
                        ServiceProvider.getTeamMemberService());
                }
                
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