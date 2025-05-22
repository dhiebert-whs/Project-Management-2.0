// src/test/java/org/frcpm/di/TestModule.java (FIXED)

package org.frcpm.di;

import de.saxsys.mvvmfx.MvvmFX;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.*;
import org.frcpm.services.impl.*;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Test module for dependency injection in tests.
 * Provides mock implementations of services and repositories for testing.
 * Enhanced to work with MVVMFx dependency injection.
 */
public class TestModule {
    
    private static final Logger LOGGER = Logger.getLogger(TestModule.class.getName());
    
    // Maps to store mock instances by type
    private static final Map<Class<?>, Object> MOCK_SERVICES = new HashMap<>();
    private static final Map<Class<?>, Object> MOCK_REPOSITORIES = new HashMap<>();
    
    private static boolean initialized = false;
    
    /**
     * Initializes the test module by clearing the ServiceLocator and
     * configuring MVVMFx with mock services and repositories.
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.info("TestModule already initialized");
            return;
        }
        
        LOGGER.info("Initializing TestModule");
        
        // Clear any existing ServiceLocator state
        ServiceLocator.clear();
        
        // Create mock repositories
        createMockRepositories();
        
        // Create mock services
        createMockServices();
        
        // Register mock services and repositories with ServiceLocator
        registerWithServiceLocator();
        
        // Configure MVVMFx dependency injection
        configureMvvmFxDependencyInjection();
        
        initialized = true;
        LOGGER.info("TestModule initialization complete");
    }
    
    /**
     * Creates mock repositories for testing.
     */
    private static void createMockRepositories() {
        LOGGER.info("Creating mock repositories");
        
        // Create mock repositories
        ProjectRepository projectRepo = Mockito.mock(ProjectRepository.class);
        TaskRepository taskRepo = Mockito.mock(TaskRepository.class);
        TeamMemberRepository teamMemberRepo = Mockito.mock(TeamMemberRepository.class);
        SubteamRepository subteamRepo = Mockito.mock(SubteamRepository.class);
        SubsystemRepository subsystemRepo = Mockito.mock(SubsystemRepository.class);
        ComponentRepository componentRepo = Mockito.mock(ComponentRepository.class);
        MeetingRepository meetingRepo = Mockito.mock(MeetingRepository.class);
        AttendanceRepository attendanceRepo = Mockito.mock(AttendanceRepository.class);
        MilestoneRepository milestoneRepo = Mockito.mock(MilestoneRepository.class);
        
        // Store mocks in repository map
        MOCK_REPOSITORIES.put(ProjectRepository.class, projectRepo);
        MOCK_REPOSITORIES.put(TaskRepository.class, taskRepo);
        MOCK_REPOSITORIES.put(TeamMemberRepository.class, teamMemberRepo);
        MOCK_REPOSITORIES.put(SubteamRepository.class, subteamRepo);
        MOCK_REPOSITORIES.put(SubsystemRepository.class, subsystemRepo);
        MOCK_REPOSITORIES.put(ComponentRepository.class, componentRepo);
        MOCK_REPOSITORIES.put(MeetingRepository.class, meetingRepo);
        MOCK_REPOSITORIES.put(AttendanceRepository.class, attendanceRepo);
        MOCK_REPOSITORIES.put(MilestoneRepository.class, milestoneRepo);
    }
    
    /**
     * Creates mock services for testing.
     */
    private static void createMockServices() {
        LOGGER.info("Creating mock services");
        
        // Create services with injected repository mocks
        
        // Task service with injected mocks
        TaskService taskService = new TestableTaskServiceImpl(
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class),
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class),
            (ComponentRepository) MOCK_REPOSITORIES.get(ComponentRepository.class)
        );
        
        // Team member service with async support - CREATE A SPY INSTEAD OF REAL IMPLEMENTATION
        TestableTeamMemberServiceAsyncImpl teamMemberServiceImpl = new TestableTeamMemberServiceAsyncImpl(
            (TeamMemberRepository) MOCK_REPOSITORIES.get(TeamMemberRepository.class),
            (SubteamRepository) MOCK_REPOSITORIES.get(SubteamRepository.class)
        );
        // Create a spy so we can stub async methods
        TeamMemberService teamMemberService = Mockito.spy(teamMemberServiceImpl);
        
        // Subsystem service with injected mocks
        SubsystemService subsystemService = new TestableSubsystemServiceImpl(
            (SubsystemRepository) MOCK_REPOSITORIES.get(SubsystemRepository.class),
            (SubteamRepository) MOCK_REPOSITORIES.get(SubteamRepository.class)
        );
        
        // Subteam service with injected mocks
        SubteamService subteamService = new TestableSubteamServiceImpl(
            (SubteamRepository) MOCK_REPOSITORIES.get(SubteamRepository.class)
        );
        
        // Milestone service with injected mocks
        MilestoneService milestoneService = new TestableMilestoneServiceImpl(
            (MilestoneRepository) MOCK_REPOSITORIES.get(MilestoneRepository.class),
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class)
        );
        
        // Attendance service with injected mocks
        AttendanceService attendanceService = new TestableAttendanceServiceImpl(
            (AttendanceRepository) MOCK_REPOSITORIES.get(AttendanceRepository.class),
            (MeetingRepository) MOCK_REPOSITORIES.get(MeetingRepository.class),
            (TeamMemberRepository) MOCK_REPOSITORIES.get(TeamMemberRepository.class)
        );
        
        // Component service with injected mocks
        ComponentService componentService = new TestableComponentServiceImpl(
            (ComponentRepository) MOCK_REPOSITORIES.get(ComponentRepository.class),
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class)
        );
        
        // Meeting service with injected mocks
        MeetingService meetingService = new TestableMeetingServiceImpl(
            (MeetingRepository) MOCK_REPOSITORIES.get(MeetingRepository.class),
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class)
        );

        // Gantt chart service with injected mocks - use the existing testable implementation
        GanttDataService ganttDataService = new TestableGanttDataServiceImpl(
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class),
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class),
            (MilestoneRepository) MOCK_REPOSITORIES.get(MilestoneRepository.class),
            Mockito.mock(GanttChartTransformationService.class)
        );

        // Visualization service with injected mocks
        VisualizationService visualizationService = new TestableVisualizationServiceImpl(
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class),
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class),
            (MilestoneRepository) MOCK_REPOSITORIES.get(MilestoneRepository.class),
            (SubsystemRepository) MOCK_REPOSITORIES.get(SubsystemRepository.class),
            ganttDataService
        );
        
        // Project service (using mock)
        ProjectService projectService = Mockito.mock(ProjectService.class);
        
        // Dialog service (using mock)
        DialogService dialogService = Mockito.mock(DialogService.class);
        
        // Gantt services (using mocks)
        GanttChartTransformationService transformationService = Mockito.mock(GanttChartTransformationService.class);
        
        // Add MetricsCalculationService (using testable implementation)
        MetricsCalculationService metricsService = new TestableMetricsCalculationServiceImpl(
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class),
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class),
            (TeamMemberRepository) MOCK_REPOSITORIES.get(TeamMemberRepository.class),
            (MilestoneRepository) MOCK_REPOSITORIES.get(MilestoneRepository.class),
            (AttendanceRepository) MOCK_REPOSITORIES.get(AttendanceRepository.class),
            (MeetingRepository) MOCK_REPOSITORIES.get(MeetingRepository.class),
            (SubsystemRepository) MOCK_REPOSITORIES.get(SubsystemRepository.class)
        );
        
        // Add ReportGenerationService (using testable implementation)
        ReportGenerationService reportService = new TestableReportGenerationServiceImpl(
            (ProjectRepository) MOCK_REPOSITORIES.get(ProjectRepository.class),
            (TaskRepository) MOCK_REPOSITORIES.get(TaskRepository.class),
            (TeamMemberRepository) MOCK_REPOSITORIES.get(TeamMemberRepository.class),
            (MilestoneRepository) MOCK_REPOSITORIES.get(MilestoneRepository.class),
            (AttendanceRepository) MOCK_REPOSITORIES.get(AttendanceRepository.class),
            (MeetingRepository) MOCK_REPOSITORIES.get(MeetingRepository.class),
            (SubsystemRepository) MOCK_REPOSITORIES.get(SubsystemRepository.class),
            metricsService,
            ganttDataService,
            visualizationService
        );
        
        // Store services in service map
        MOCK_SERVICES.put(TaskService.class, taskService);
        MOCK_SERVICES.put(TeamMemberService.class, teamMemberService);
        MOCK_SERVICES.put(SubsystemService.class, subsystemService);
        MOCK_SERVICES.put(SubteamService.class, subteamService);
        MOCK_SERVICES.put(MilestoneService.class, milestoneService);
        MOCK_SERVICES.put(AttendanceService.class, attendanceService);
        MOCK_SERVICES.put(ComponentService.class, componentService);
        MOCK_SERVICES.put(MeetingService.class, meetingService);
        MOCK_SERVICES.put(ProjectService.class, projectService);
        MOCK_SERVICES.put(DialogService.class, dialogService);
        MOCK_SERVICES.put(GanttDataService.class, ganttDataService);
        MOCK_SERVICES.put(GanttChartTransformationService.class, transformationService);
        MOCK_SERVICES.put(VisualizationService.class, visualizationService);
        MOCK_SERVICES.put(MetricsCalculationService.class, metricsService);
        MOCK_SERVICES.put(ReportGenerationService.class, reportService);
    }
    
    /**
     * Registers mock services and repositories with the ServiceLocator.
     */
    private static void registerWithServiceLocator() {
        LOGGER.info("Registering mocks with ServiceLocator");
        
        // Register repositories
        for (Map.Entry<Class<?>, Object> entry : MOCK_REPOSITORIES.entrySet()) {
            ServiceLocator.register((Class) entry.getKey(), entry.getValue());
        }
        
        // Register services
        for (Map.Entry<Class<?>, Object> entry : MOCK_SERVICES.entrySet()) {
            ServiceLocator.register((Class) entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Configures MVVMFx dependency injection to use mock services.
     */
    private static void configureMvvmFxDependencyInjection() {
        LOGGER.info("Configuring MVVMFx dependency injection");
        
        // Set up MVVMFx custom dependency injector to use our mocks
        MvvmFX.setCustomDependencyInjector(type -> {
            // First check services
            if (MOCK_SERVICES.containsKey(type)) {
                return MOCK_SERVICES.get(type);
            }
            
            // Then check repositories
            if (MOCK_REPOSITORIES.containsKey(type)) {
                return MOCK_REPOSITORIES.get(type);
            }
            
            // Return null for unknown types
            return null;
        });
    }
    
    /**
     * Gets a mock service by its interface class.
     * 
     * @param <T> the service type
     * @param serviceClass the service interface class
     * @return the mock service
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        if (!initialized) initialize();
        return (T) MOCK_SERVICES.get(serviceClass);
    }
    
    /**
     * Gets a mock repository by its interface class.
     * 
     * @param <T> the repository type
     * @param repositoryClass the repository interface class
     * @return the mock repository
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRepository(Class<T> repositoryClass) {
        if (!initialized) initialize();
        return (T) MOCK_REPOSITORIES.get(repositoryClass);
    }
    
    /**
     * Replaces a mock service with a custom implementation.
     * 
     * @param <T> the service type
     * @param serviceClass the service interface class
     * @param service the service implementation
     */
    public static <T> void setService(Class<T> serviceClass, T service) {
        if (!initialized) initialize();
        MOCK_SERVICES.put(serviceClass, service);
        ServiceLocator.register(serviceClass, service);
        
        // Update MVVMFx dependency injector
        configureMvvmFxDependencyInjection();
    }
    
    /**
     * Replaces a mock repository with a custom implementation.
     * 
     * @param <T> the repository type
     * @param repositoryClass the repository interface class
     * @param repository the repository implementation
     */
    public static <T> void setRepository(Class<T> repositoryClass, T repository) {
        if (!initialized) initialize();
        MOCK_REPOSITORIES.put(repositoryClass, repository);
        ServiceLocator.register(repositoryClass, repository);
        
        // Update MVVMFx dependency injector to pick up the change
        configureMvvmFxDependencyInjection();
    }
    
    /**
     * Resets all mock services and repositories.
     */
    public static void resetMocks() {
        if (!initialized) return;
        
        LOGGER.info("Resetting all mocks");
        
        // Reset all mocks
        for (Object mock : MOCK_SERVICES.values()) {
            if (mock instanceof TestableTaskServiceImpl || 
                mock instanceof TestableSubsystemServiceImpl ||
                mock instanceof TestableSubteamServiceImpl ||
                mock instanceof TestableMilestoneServiceImpl ||
                mock instanceof TestableAttendanceServiceImpl ||
                mock instanceof TestableComponentServiceImpl ||
                mock instanceof TestableMeetingServiceImpl ||
                mock instanceof TestableGanttDataServiceImpl ||
                mock instanceof TestableVisualizationServiceImpl ||
                mock instanceof TestableMetricsCalculationServiceImpl ||
                mock instanceof TestableReportGenerationServiceImpl) {
                // Skip resetting the testable service implementations because they're not pure mocks
                continue;
            }
            
            try {
                Mockito.reset(mock);
            } catch (Exception e) {
                LOGGER.warning("Failed to reset mock: " + mock.getClass().getName() + " - " + e.getMessage());
            }
        }
        
        for (Object mock : MOCK_REPOSITORIES.values()) {
            try {
                Mockito.reset(mock);
            } catch (Exception e) {
                LOGGER.warning("Failed to reset mock: " + mock.getClass().getName() + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Shuts down the test module and clears all mocks.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down TestModule");
        
        // Clean up MVVMFx
        MvvmFX.setCustomDependencyInjector(null);
        
        // Clear all mocks
        MOCK_SERVICES.clear();
        MOCK_REPOSITORIES.clear();
        
        // Clear ServiceLocator
        ServiceLocator.clear();
        
        initialized = false;
    }
    
    /**
     * Checks if the TestModule has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
}