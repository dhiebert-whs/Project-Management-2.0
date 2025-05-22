// src/test/java/org/frcpm/mvvm/viewmodels/GanttChartMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.frcpm.di.TestModule;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel.FilterOption;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel.ViewMode;
import org.frcpm.services.GanttDataService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.layout.Pane;

/**
 * Tests for the GanttChartMvvmViewModel class.
 */
public class GanttChartMvvmViewModelTest {
    
    private GanttDataService ganttDataService;
    
    private Project testProject;
    private Map<String, Object> testGanttData;
    private List<Long> testCriticalPath;
    private GanttChartMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create clean mock for GanttDataService
        GanttDataService mockGanttDataService = mock(GanttDataService.class);
        
        // Configure mock responses
        when(mockGanttDataService.formatTasksForGantt(anyLong(), any(), any()))
                .thenReturn(testGanttData);
        when(mockGanttDataService.calculateCriticalPath(anyLong()))
                .thenReturn(testCriticalPath);
        when(mockGanttDataService.applyFiltersToGanttData(any(), any()))
                .thenReturn(testGanttData);
        
        // Register the mock with TestModule
        TestModule.setService(GanttDataService.class, mockGanttDataService);
        
        // Get service reference
        ganttDataService = TestModule.getService(GanttDataService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new GanttChartMvvmViewModel(ganttDataService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusMonths(1));
        testProject.setHardDeadline(LocalDate.now().plusMonths(2));
        
        // Create test Gantt data
        testGanttData = new HashMap<>();
        testGanttData.put("tasks", new ArrayList<>());
        testGanttData.put("milestones", new ArrayList<>());
        testGanttData.put("dependencies", new ArrayList<>());
        testGanttData.put("startDate", testProject.getStartDate().toString());
        testGanttData.put("endDate", testProject.getHardDeadline().toString());
        testGanttData.put("projectName", testProject.getName());
        
        // Create test critical path
        testCriticalPath = List.of(1L, 2L, 3L);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertNull(viewModel.getProject());
            assertEquals(ViewMode.WEEK, viewModel.getViewMode());
            assertEquals(FilterOption.ALL_TASKS, viewModel.getFilterOption());
            assertEquals("", viewModel.getStatusMessage());
            assertNull(viewModel.getChartData());
            assertFalse(viewModel.isDataLoaded());
            assertNull(viewModel.getSelectedTask());
            assertNull(viewModel.getSelectedMilestone());
            assertNotNull(viewModel.getStartDate());
            assertNotNull(viewModel.getEndDate());
            assertTrue(viewModel.getCriticalPathTasks().isEmpty());
            assertTrue(viewModel.isShowMilestones());
            assertTrue(viewModel.isShowDependencies());
            assertNull(viewModel.getSelectedSubsystem());
            assertNull(viewModel.getSelectedSubteam());
            assertTrue(viewModel.isShowCompletedTasks());
            assertFalse(viewModel.isLoading());
            assertNull(viewModel.getChartPane());
        });
    }
    
    @Test
    public void testProjectSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project
            viewModel.setProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getProject());
            
            // Verify date range was updated from project
            assertEquals(testProject.getStartDate(), viewModel.getStartDate());
            assertEquals(testProject.getHardDeadline(), viewModel.getEndDate());
            
            // Let any async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify service was called to load data
            verify(ganttDataService).formatTasksForGantt(
                eq(testProject.getId()),
                eq(testProject.getStartDate()),
                eq(testProject.getHardDeadline())
            );
        });
    }
    
    @Test
    public void testRefreshCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setProject(testProject);
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Execute refresh command
            viewModel.getRefreshCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify service was called
            verify(ganttDataService).formatTasksForGantt(
                eq(testProject.getId()),
                any(LocalDate.class),
                any(LocalDate.class)
            );
            
            // Verify data was loaded
            assertTrue(viewModel.isDataLoaded());
            assertNotNull(viewModel.getChartData());
        });
    }
    
    @Test
    public void testViewModeChange() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Change view mode
            viewModel.setViewMode(ViewMode.MONTH);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify view mode was changed
            assertEquals(ViewMode.MONTH, viewModel.getViewMode());
            
            // Verify refresh was triggered
            verify(ganttDataService).formatTasksForGantt(anyLong(), any(), any());
        });
    }
    
    @Test
    public void testFilterOptionChange() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Change filter option
            viewModel.setFilterOption(FilterOption.CRITICAL_PATH);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify filter was changed
            assertEquals(FilterOption.CRITICAL_PATH, viewModel.getFilterOption());
            
            // Verify filter was applied
            verify(ganttDataService).applyFiltersToGanttData(any(), any());
        });
    }
    
    @Test
    public void testZoomInCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Store original date range
            LocalDate originalStart = viewModel.getStartDate();
            LocalDate originalEnd = viewModel.getEndDate();
            long originalRange = originalStart.until(originalEnd).getDays();
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Execute zoom in command
            assertTrue(viewModel.getZoomInCommand().isExecutable());
            viewModel.getZoomInCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify date range was reduced
            long newRange = viewModel.getStartDate().until(viewModel.getEndDate()).getDays();
            assertTrue(newRange < originalRange, "Date range should be reduced after zoom in");
            
            // Verify data was reloaded
            verify(ganttDataService).formatTasksForGantt(anyLong(), any(), any());
        });
    }
    
    @Test
    public void testZoomOutCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Store original date range
            LocalDate originalStart = viewModel.getStartDate();
            LocalDate originalEnd = viewModel.getEndDate();
            long originalRange = originalStart.until(originalEnd).getDays();
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Execute zoom out command
            assertTrue(viewModel.getZoomOutCommand().isExecutable());
            viewModel.getZoomOutCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify date range was increased
            long newRange = viewModel.getStartDate().until(viewModel.getEndDate()).getDays();
            assertTrue(newRange > originalRange, "Date range should be increased after zoom out");
            
            // Verify data was reloaded
            verify(ganttDataService).formatTasksForGantt(anyLong(), any(), any());
        });
    }
    
    @Test
    public void testTodayCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Set a date range that doesn't include today
            LocalDate pastDate = LocalDate.now().minusMonths(2);
            viewModel.setStartDate(pastDate);
            viewModel.setEndDate(pastDate.plusDays(14));
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Execute today command
            assertTrue(viewModel.getTodayCommand().isExecutable());
            viewModel.getTodayCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify today is now within the date range
            LocalDate today = LocalDate.now();
            assertTrue(!viewModel.getStartDate().isAfter(today) && !viewModel.getEndDate().isBefore(today),
                "Today should be within the new date range");
            
            // Verify data was reloaded
            verify(ganttDataService).formatTasksForGantt(anyLong(), any(), any());
        });
    }
    
    @Test
    public void testToggleMilestones() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially should show milestones
            assertTrue(viewModel.isShowMilestones());
            
            // Toggle milestones
            viewModel.toggleMilestones();
            
            // Should now be hidden
            assertFalse(viewModel.isShowMilestones());
            assertTrue(viewModel.getStatusMessage().contains("Hiding milestones"));
            
            // Toggle again
            viewModel.toggleMilestones();
            
            // Should now be shown
            assertTrue(viewModel.isShowMilestones());
            assertTrue(viewModel.getStatusMessage().contains("Showing milestones"));
        });
    }
    
    @Test
    public void testToggleDependencies() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially should show dependencies
            assertTrue(viewModel.isShowDependencies());
            
            // Toggle dependencies
            viewModel.toggleDependencies();
            
            // Should now be hidden
            assertFalse(viewModel.isShowDependencies());
            assertTrue(viewModel.getStatusMessage().contains("Hiding dependencies"));
            
            // Toggle again
            viewModel.toggleDependencies();
            
            // Should now be shown
            assertTrue(viewModel.isShowDependencies());
            assertTrue(viewModel.getStatusMessage().contains("Showing dependencies"));
        });
    }
    
    @Test
    public void testToggleCompletedTasks() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially should show completed tasks
            assertTrue(viewModel.isShowCompletedTasks());
            
            // Clear previous interactions
            clearInvocations(ganttDataService);
            
            // Toggle completed tasks
            viewModel.toggleCompletedTasks();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should now be hidden
            assertFalse(viewModel.isShowCompletedTasks());
            assertTrue(viewModel.getStatusMessage().contains("Hiding completed tasks"));
            
            // Verify data was reloaded
            verify(ganttDataService).formatTasksForGantt(anyLong(), any(), any());
        });
    }
    
    @Test
    public void testSubsystemAndSubteamFilters() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create test subsystem and subteam
            Subsystem testSubsystem = new Subsystem();
            testSubsystem.setId(1L);
            testSubsystem.setName("Test Subsystem");
            
            Subteam testSubteam = new Subteam();
            testSubteam.setId(1L);
            testSubteam.setName("Test Subteam");
            
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Set subsystem filter
            viewModel.setSelectedSubsystem(testSubsystem);
            assertEquals(testSubsystem, viewModel.getSelectedSubsystem());
            
            // Set subteam filter
            viewModel.setSelectedSubteam(testSubteam);
            assertEquals(testSubteam, viewModel.getSelectedSubteam());
        });
    }
    
    @Test
    public void testDateRangeChanges() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Change date range
            LocalDate newStartDate = LocalDate.now().minusMonths(3);
            LocalDate newEndDate = LocalDate.now().plusMonths(3);
            
            viewModel.setStartDate(newStartDate);
            viewModel.setEndDate(newEndDate);
            
            // Verify dates were set
            assertEquals(newStartDate, viewModel.getStartDate());
            assertEquals(newEndDate, viewModel.getEndDate());
            
            // Verify dirty flag was set
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testSelectedTaskAndMilestone() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create test objects
            Task testTask = new Task();
            testTask.setId(1L);
            testTask.setTitle("Test Task");
            
            Milestone testMilestone = new Milestone();
            testMilestone.setId(1L);
            testMilestone.setName("Test Milestone");
            
            // Set selected objects
            viewModel.setSelectedTask(testTask);
            viewModel.setSelectedMilestone(testMilestone);
            
            // Verify selection
            assertEquals(testTask, viewModel.getSelectedTask());
            assertEquals(testMilestone, viewModel.getSelectedMilestone());
        });
    }
    
    @Test
    public void testExportCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Execute export command
            assertTrue(viewModel.getExportCommand().isExecutable());
            viewModel.getExportCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify status message indicates export
            assertTrue(viewModel.getStatusMessage().contains("Export"));
        });
    }
    
    @Test
    public void testErrorHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Configure service to throw an exception
            when(ganttDataService.formatTasksForGantt(anyLong(), any(), any()))
                    .thenThrow(new RuntimeException("Test error"));
            
            // Set project to trigger loading
            viewModel.setProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error was handled
            assertFalse(viewModel.isDataLoaded());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("error") || 
                      viewModel.getErrorMessage().contains("Error"));
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.setProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify critical path tasks were loaded
            assertFalse(viewModel.getCriticalPathTasks().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared (base dispose should be called)
            // Note: Specific collection clearing depends on implementation
        });
    }
}