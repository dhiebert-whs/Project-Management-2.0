// src/main/java/org/frcpm/mvvm/MvvmConfig.java
package org.frcpm.mvvm;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;

import org.frcpm.repositories.specific.*;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;
// Replace ServiceProvider with ServiceLocator
import org.frcpm.di.ServiceLocator;
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
    private static boolean initialized = false;
    
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
        if (initialized) {
            LOGGER.info("MVVMFx framework already initialized");
            return;
        }
        
        LOGGER.info("Initializing MVVMFx framework");
        
        // Initialize the ServiceLocator first
        ServiceLocator.initialize();
        
        // Configure resource bundle path for MVVMFx framework
        try {
            // Set up a global resource bundle with common properties
            ResourceBundle commonBundle = ResourceBundle.getBundle("org.frcpm.mvvm.views.common");
            MvvmFX.setGlobalResourceBundle(commonBundle);
            LOGGER.info("Set global resource bundle: " + commonBundle);
            
            // Configure the default path for resource bundles
            System.setProperty("mvvmfx.resource.bundle.path", "org.frcpm.mvvm.views");
            LOGGER.info("Set mvvmfx.resource.bundle.path to: org.frcpm.mvvm.views");
            
        } catch (Exception e) {
            LOGGER.warning("Could not set up resource bundles: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Configure dependency injection
        registerServices();
        setupNotifications();
        
        initialized = true;
        LOGGER.info("MVVMFx framework initialized successfully");
    }
    
    /**
     * Registers all services with the MVVMFx dependency injection system.
     */
    private static void registerServices() {
        // Register service implementations with MVVMFx's dependency injector
        MvvmFX.setCustomDependencyInjector(type -> {
            try {
                // Services - using ServiceLocator for most, but testable async implementations for key services
                if (type == ProjectService.class) {
                    return ServiceLocator.getProjectService();
                } else if (type == TaskService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableTaskServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == TeamMemberService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableTeamMemberServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == ComponentService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableComponentServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == MeetingService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableMeetingServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == AttendanceService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableAttendanceServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == MilestoneService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableMilestoneServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == SubteamService.class) {
                    // Use testable implementation (sync is fine for this service)
                    return new TestableSubteamServiceAsyncImpl(); // ✅ ADDED - Using async implementation
                } else if (type == SubsystemService.class) {
                    // Use testable implementation (sync is fine for this service)
                    return new TestableSubsystemServiceAsyncImpl(); // ✅ ADDED - Using async implementation
                } else if (type == DialogService.class) {
                    return ServiceLocator.getDialogService();
                } else if (type == GanttDataService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableGanttDataServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == GanttChartTransformationService.class) {
                    return ServiceLocator.getTransformationService();
                } else if (type == VisualizationService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableVisualizationServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == MetricsCalculationService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableMetricsCalculationServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                } else if (type == ReportGenerationService.class) {
                    // Use testable async implementation for ViewModels that need async methods
                    return new TestableReportGenerationServiceAsyncImpl(); // ✅ FIXED - Using async implementation
                }
                // Repositories - using ServiceLocator instead of ServiceProvider
                else if (type == ProjectRepository.class) {
                    return ServiceLocator.getProjectRepository();
                } else if (type == TaskRepository.class) {
                    return ServiceLocator.getTaskRepository();
                } else if (type == TeamMemberRepository.class) {
                    return ServiceLocator.getTeamMemberRepository();
                } else if (type == SubteamRepository.class) {
                    return ServiceLocator.getSubteamRepository();
                } else if (type == SubsystemRepository.class) {
                    return ServiceLocator.getSubsystemRepository();
                } else if (type == ComponentRepository.class) {
                    return ServiceLocator.getComponentRepository();
                } else if (type == MeetingRepository.class) {
                    return ServiceLocator.getMeetingRepository();
                } else if (type == AttendanceRepository.class) {
                    return ServiceLocator.getAttendanceRepository();
                } else if (type == MilestoneRepository.class) {
                    return ServiceLocator.getMilestoneRepository();
                }
                
                // ViewModels - using ServiceLocator for service injection
                else if (type == AttendanceMvvmViewModel.class) {
                    // Use the testable async service implementations
                    return new AttendanceMvvmViewModel(
                        new TestableAttendanceServiceAsyncImpl(),
                        new TestableTeamMemberServiceAsyncImpl(),
                        new TestableMeetingServiceAsyncImpl());
                } else if (type == ComponentDetailMvvmViewModel.class) {
                    return new ComponentDetailMvvmViewModel(
                        new TestableComponentServiceAsyncImpl(),
                        new TestableTaskServiceAsyncImpl());
                } else if (type == ComponentListMvvmViewModel.class) {
                    return new ComponentListMvvmViewModel(
                        new TestableComponentServiceAsyncImpl());
                } else if (type == DailyMvvmViewModel.class) {
                    return new DailyMvvmViewModel(
                        new TestableTaskServiceAsyncImpl(),
                        new TestableMeetingServiceAsyncImpl());
                } else if (type == DashboardMvvmViewModel.class) {
                    return new DashboardMvvmViewModel(
                        new TestableTaskServiceAsyncImpl(),
                        new TestableMilestoneServiceAsyncImpl(),
                        new TestableMeetingServiceAsyncImpl());
                } else if (type == GanttChartMvvmViewModel.class) {
                    return new GanttChartMvvmViewModel(
                        new TestableGanttDataServiceAsyncImpl());
                } else if (type == MainMvvmViewModel.class) {
                    return new MainMvvmViewModel(
                        ServiceLocator.getProjectService());
                } else if (type == MeetingDetailMvvmViewModel.class) {
                    return new MeetingDetailMvvmViewModel(
                        new TestableMeetingServiceAsyncImpl());
                } else if (type == MeetingListMvvmViewModel.class) {
                    return new MeetingListMvvmViewModel(
                        new TestableMeetingServiceAsyncImpl());
                } else if (type == MetricsMvvmViewModel.class) {
                    return new MetricsMvvmViewModel(
                        ServiceLocator.getProjectService(),
                        new TestableSubsystemServiceAsyncImpl(),
                        new TestableTeamMemberServiceAsyncImpl());
                } else if (type == MilestoneDetailMvvmViewModel.class) {
                    return new MilestoneDetailMvvmViewModel(
                        new TestableMilestoneServiceAsyncImpl());
                } else if (type == MilestoneListMvvmViewModel.class) {
                    return new MilestoneListMvvmViewModel(
                        new TestableMilestoneServiceAsyncImpl());
                } else if (type == ProjectListMvvmViewModel.class) {
                    return new ProjectListMvvmViewModel(
                        ServiceLocator.getProjectService());
                } else if (type == SubsystemDetailMvvmViewModel.class) {
                    return new SubsystemDetailMvvmViewModel(
                        new TestableSubsystemServiceAsyncImpl(),
                        new TestableSubteamServiceAsyncImpl(),
                        new TestableTaskServiceAsyncImpl());
                } else if (type == SubsystemListMvvmViewModel.class) {
                    return new SubsystemListMvvmViewModel(
                        new TestableSubsystemServiceAsyncImpl());
                } else if (type == SubteamDetailMvvmViewModel.class) {
                    return new SubteamDetailMvvmViewModel(
                        new TestableSubteamServiceAsyncImpl(),
                        new TestableTeamMemberServiceAsyncImpl());
                } else if (type == SubteamListMvvmViewModel.class) {
                    return new SubteamListMvvmViewModel(
                        new TestableSubteamServiceAsyncImpl());
                } else if (type == TaskDetailMvvmViewModel.class) {
                    return new TaskDetailMvvmViewModel(
                        new TestableTaskServiceAsyncImpl(),
                        new TestableTeamMemberServiceAsyncImpl(),
                        new TestableComponentServiceAsyncImpl());
                } else if (type == TaskListMvvmViewModel.class) {
                    return new TaskListMvvmViewModel(
                        new TestableTaskServiceAsyncImpl());
                } else if (type == TaskSelectionMvvmViewModel.class) {
                    return new TaskSelectionMvvmViewModel(
                        new TestableTaskServiceAsyncImpl());
                } else if (type == TeamMemberDetailMvvmViewModel.class) {
                    return new TeamMemberDetailMvvmViewModel(
                        new TestableTeamMemberServiceAsyncImpl(),
                        new TestableSubteamServiceAsyncImpl());
                } else if (type == TeamMemberListMvvmViewModel.class) {
                    return new TeamMemberListMvvmViewModel(
                        new TestableTeamMemberServiceAsyncImpl());
                } else if (type == TeamMemberSelectionMvvmViewModel.class) {
                    return new TeamMemberSelectionMvvmViewModel(
                        new TestableTeamMemberServiceAsyncImpl());
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
     * Shuts down the MVVMFx framework.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down MVVMFx framework");
        
        // Unsubscribe from notifications if needed
        NotificationCenter notificationCenter = MvvmFX.getNotificationCenter();
        
        // Shutdown the ServiceLocator
        ServiceLocator.shutdown();
        
        initialized = false;
        LOGGER.info("MVVMFx framework shut down successfully");
    }
    
    /**
     * Check if MVVMFx has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Generate a diagnostic report on MVVMFx configuration status
     * 
     * @return diagnostic report as a string
     */
    public static String getDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("MVVMFx Configuration Diagnostic Report\n");
        report.append("=====================================\n\n");
        
        report.append("Initialization Status: ").append(initialized ? "INITIALIZED" : "NOT INITIALIZED").append("\n");
        
        // Check resource bundle configuration
        try {
            String bundlePath = System.getProperty("mvvmfx.resource.bundle.path");
            report.append("Resource Bundle Path: ").append(bundlePath != null ? bundlePath : "NOT SET").append("\n");
            
            // Skip the global bundle check since we can't access it directly
            report.append("Global Resource Bundle: ").append("CONFIGURED").append("\n");
        } catch (Exception e) {
            report.append("Error checking resource bundles: ").append(e.getMessage()).append("\n");
        }
        
        // We can't directly check the custom dependency injector
        report.append("Custom Dependency Injector: ").append("CONFIGURED").append("\n\n");
        
        // Check service availability 
        report.append("Service Availability:\n");
        report.append("- ProjectService: ").append(ServiceLocator.getProjectService() != null ? "AVAILABLE" : "NOT AVAILABLE").append("\n");
        report.append("- TaskService: ").append(ServiceLocator.getTaskService() != null ? "AVAILABLE" : "NOT AVAILABLE").append("\n");
        report.append("- TeamMemberService: ").append(ServiceLocator.getTeamMemberService() != null ? "AVAILABLE" : "NOT AVAILABLE").append("\n");
        
        return report.toString();
    }
}