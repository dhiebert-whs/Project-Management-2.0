// src/test/java/org/frcpm/mvvm/viewmodels/MetricsMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subsystem.Status;
import org.frcpm.mvvm.viewmodels.MetricsMvvmViewModel.MetricType;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javafx.scene.chart.PieChart;

/**
 * Tests for the MetricsMvvmViewModel class.
 */
public class MetricsMvvmViewModelTest {
    
    private ProjectService projectService;
    private SubsystemService subsystemService;
    private TeamMemberService teamMemberService;
    private MetricsCalculationService metricsService;
    
    private Project testProject;
    private List<Subsystem> testSubsystems;
    private MetricsMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Get service mocks from TestModule
        projectService = TestModule.getService(ProjectService.class);
        subsystemService = TestModule.getService(SubsystemService.class);
        teamMemberService = TestModule.getService(TeamMemberService.class);
        metricsService = TestModule.getService(MetricsCalculationService.class);
        
        // Create test data
        setupTestData();
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new MetricsMvvmViewModel(projectService, subsystemService, teamMemberService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusMonths(1));
        testProject.setGoalEndDate(LocalDate.now().plusMonths(1));
        
        // Create test subsystems
        testSubsystems = new ArrayList<>();
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setId(1L);
        subsystem1.setName("Subsystem 1");
        subsystem1.setStatus(Status.IN_PROGRESS);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setId(2L);
        subsystem2.setName("Subsystem 2");
        subsystem2.setStatus(Status.COMPLETED);
        
        testSubsystems.add(subsystem1);
        testSubsystems.add(subsystem2);
        
        // Configure mock responses
        when(subsystemService.findAll()).thenReturn(testSubsystems);
        
        // Mock project summary response for metrics
        Map<String, Object> projectSummary = new HashMap<>();
        projectSummary.put("completedTasks", 5);
        projectSummary.put("totalTasks", 10);
        when(projectService.getProjectSummary(anyLong())).thenReturn(projectSummary);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Now check initial values
            assertEquals("", viewModel.getProjectName());
            assertEquals(MetricType.TASK_COMPLETION, viewModel.getSelectedMetricType());
            assertNotNull(viewModel.getStartDate());
            assertNotNull(viewModel.getEndDate());
            assertFalse(viewModel.hasData());
            assertFalse(viewModel.isLoading());
            assertTrue(viewModel.getTaskDistributionData().isEmpty());
        });
    }
    
    @Test
    public void testProjectSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and verify project name is updated
            viewModel.setProject(testProject);
            
            // Verify project name was updated
            assertEquals("Test Project", viewModel.getProjectName());
            
            // Verify subsystems were loaded
            verify(subsystemService).findAll();
        });
    }
    
    @Test
    public void testLoadTaskCompletionData() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and metric type
            viewModel.setProject(testProject);
            viewModel.setSelectedMetricType(MetricType.TASK_COMPLETION);
            
            // Execute refresh command
            viewModel.getRefreshCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify project summary was retrieved
            verify(projectService).getProjectSummary(testProject.getId());
            
            // Check that data was loaded
            assertTrue(viewModel.hasData());
            
            // Task distribution should have been populated
            assertFalse(viewModel.getTaskDistributionData().isEmpty());
        });
    }
    
    @Test
    public void testLoadSubsystemProgressData() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and metric type
            viewModel.setProject(testProject);
            viewModel.setSelectedMetricType(MetricType.SUBSYSTEM_PROGRESS);
            
            // Execute refresh command
            viewModel.getRefreshCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subsystems were loaded
            verify(subsystemService).findAll();
            
            // Check that data was loaded
            assertTrue(viewModel.hasData());
            
            // Subsystem data should have been populated
            assertFalse(viewModel.getSubsystemProgressData().getData().isEmpty());
        });
    }
    
    @Test
    public void testDateRangeChanges() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project
            viewModel.setProject(testProject);
            
            // Initial state should be clean
            assertFalse(viewModel.isDirty());
            
            // Change date range
            LocalDate newStartDate = LocalDate.now().minusMonths(2);
            LocalDate newEndDate = LocalDate.now().plusMonths(2);
            
            viewModel.setStartDate(newStartDate);
            viewModel.setEndDate(newEndDate);
            
            // Check that dirty flag was set
            assertTrue(viewModel.isDirty());
            
            // Verify the date properties were updated
            assertEquals(newStartDate, viewModel.getStartDate());
            assertEquals(newEndDate, viewModel.getEndDate());
        });
    }
    
    @Test
    public void testExportDataCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            viewModel.getRefreshCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Check that export command is executable
            assertTrue(viewModel.getExportDataCommand().isExecutable());
            
            // Execute export command
            viewModel.getExportDataCommand().execute();
            
            // Verify loading state during export
            assertTrue(viewModel.isLoading());
            
            // Let async export complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after export completes
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testGenerateReportCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            viewModel.getRefreshCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Check that report command is executable
            assertTrue(viewModel.getGenerateReportCommand().isExecutable());
            
            // Execute report command
            viewModel.getGenerateReportCommand().execute();
            
            // Verify loading state during report generation
            assertTrue(viewModel.isLoading());
            
            // Let async operation complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after report generation completes
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            viewModel.getRefreshCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify collections were populated
            assertFalse(viewModel.getTaskDistributionData().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getTaskDistributionData().isEmpty());
            assertTrue(viewModel.getAvailableSubsystems().isEmpty());
        });
    }
}