// src/test/java/org/frcpm/mvvm/viewmodels/MilestoneListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.mvvm.viewmodels.MilestoneListMvvmViewModel.MilestoneFilter;
import org.frcpm.services.MilestoneService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the MilestoneListMvvmViewModel class.
 */
public class MilestoneListMvvmViewModelTest {
    
    private MilestoneService milestoneService;
    
    private Project testProject;
    private List<Milestone> testMilestones;
    private Milestone pastMilestone;
    private Milestone upcomingMilestone;
    private Milestone todayMilestone;
    private MilestoneListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data first
        setupTestData();
        
        // Get service reference from TestModule (uses TestableMilestoneServiceImpl)
        milestoneService = TestModule.getService(MilestoneService.class);
        
        // Configure the service for our test data
        configureMilestoneService();
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new MilestoneListMvvmViewModel(milestoneService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusMonths(2));
        testProject.setGoalEndDate(LocalDate.now().plusMonths(2));
        testProject.setHardDeadline(LocalDate.now().plusMonths(3));
        
        // Create test milestones
        pastMilestone = new Milestone();
        pastMilestone.setId(1L);
        pastMilestone.setName("Past Milestone");
        pastMilestone.setDescription("A milestone that has passed");
        pastMilestone.setDate(LocalDate.now().minusDays(10));
        pastMilestone.setProject(testProject);
        
        todayMilestone = new Milestone();
        todayMilestone.setId(2L);
        todayMilestone.setName("Today Milestone");
        todayMilestone.setDescription("A milestone due today");
        todayMilestone.setDate(LocalDate.now());
        todayMilestone.setProject(testProject);
        
        upcomingMilestone = new Milestone();
        upcomingMilestone.setId(3L);
        upcomingMilestone.setName("Upcoming Milestone");
        upcomingMilestone.setDescription("A future milestone");
        upcomingMilestone.setDate(LocalDate.now().plusDays(15));
        upcomingMilestone.setProject(testProject);
        
        testMilestones = new ArrayList<>();
        testMilestones.add(pastMilestone);
        testMilestones.add(todayMilestone);
        testMilestones.add(upcomingMilestone);
    }
    
    private void configureMilestoneService() {
        // Since we're using TestableMilestoneServiceImpl, we need to work with the actual service
        // We'll use the fact that the service has injected mock repositories
        // Configure the milestone repository mock to return our test data
        try {
            // Get the milestone repository from TestModule
            var milestoneRepo = TestModule.getRepository(org.frcpm.repositories.specific.MilestoneRepository.class);
            when(milestoneRepo.findByProject(testProject)).thenReturn(testMilestones);
            when(milestoneRepo.findById(1L)).thenReturn(java.util.Optional.of(pastMilestone));
            when(milestoneRepo.findById(2L)).thenReturn(java.util.Optional.of(todayMilestone));
            when(milestoneRepo.findById(3L)).thenReturn(java.util.Optional.of(upcomingMilestone));
            
            // Configure delete operations
            when(milestoneRepo.deleteById(anyLong())).thenReturn(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getAllMilestones().isEmpty());
            assertTrue(viewModel.getFilteredMilestones().isEmpty());
            assertNull(viewModel.getSelectedMilestone());
            assertNull(viewModel.getCurrentProject());
            assertEquals(MilestoneFilter.ALL, viewModel.getCurrentFilter());
            assertFalse(viewModel.isLoading());
            
            // Check that commands exist
            assertNotNull(viewModel.getLoadMilestonesCommand());
            assertNotNull(viewModel.getNewMilestoneCommand());
            assertNotNull(viewModel.getEditMilestoneCommand());
            assertNotNull(viewModel.getDeleteMilestoneCommand());
            assertNotNull(viewModel.getRefreshMilestonesCommand());
            
            // Check command states
            assertTrue(viewModel.getLoadMilestonesCommand().isExecutable());
            assertFalse(viewModel.getNewMilestoneCommand().isExecutable()); // No project
            assertFalse(viewModel.getEditMilestoneCommand().isExecutable()); // No selection
            assertFalse(viewModel.getDeleteMilestoneCommand().isExecutable()); // No selection
            assertTrue(viewModel.getRefreshMilestonesCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitWithProject() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Verify milestones were loaded
            assertEquals(3, viewModel.getAllMilestones().size());
            assertEquals(3, viewModel.getFilteredMilestones().size());
            
            // New milestone command should now be executable
            assertTrue(viewModel.getNewMilestoneCommand().isExecutable());
        });
    }
    
    @Test
    public void testMilestoneFiltering() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Test ALL filter (default)
            assertEquals(MilestoneFilter.ALL, viewModel.getCurrentFilter());
            assertEquals(3, viewModel.getFilteredMilestones().size());
            
            // Test UPCOMING filter
            viewModel.setCurrentFilter(MilestoneFilter.UPCOMING);
            assertEquals(2, viewModel.getFilteredMilestones().size()); // today + future
            assertTrue(viewModel.getFilteredMilestones().contains(todayMilestone));
            assertTrue(viewModel.getFilteredMilestones().contains(upcomingMilestone));
            assertFalse(viewModel.getFilteredMilestones().contains(pastMilestone));
            
            // Test PASSED filter
            viewModel.setCurrentFilter(MilestoneFilter.PASSED);
            assertEquals(1, viewModel.getFilteredMilestones().size());
            assertTrue(viewModel.getFilteredMilestones().contains(pastMilestone));
            assertFalse(viewModel.getFilteredMilestones().contains(todayMilestone));
            assertFalse(viewModel.getFilteredMilestones().contains(upcomingMilestone));
            
            // Test back to ALL filter
            viewModel.setCurrentFilter(MilestoneFilter.ALL);
            assertEquals(3, viewModel.getFilteredMilestones().size());
        });
    }
    
    @Test
    public void testMilestoneSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially no selection
            assertNull(viewModel.getSelectedMilestone());
            assertFalse(viewModel.getEditMilestoneCommand().isExecutable());
            assertFalse(viewModel.getDeleteMilestoneCommand().isExecutable());
            
            // Select a milestone
            viewModel.setSelectedMilestone(upcomingMilestone);
            
            // Verify selection
            assertEquals(upcomingMilestone, viewModel.getSelectedMilestone());
            
            // Commands should now be executable
            assertTrue(viewModel.getEditMilestoneCommand().isExecutable());
            assertTrue(viewModel.getDeleteMilestoneCommand().isExecutable());
        });
    }
    
    @Test
    public void testNewMilestoneCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no project)
            assertFalse(viewModel.getNewMilestoneCommand().isExecutable());
            
            // Set project
            viewModel.setCurrentProject(testProject);
            
            // Now should be executable
            assertTrue(viewModel.getNewMilestoneCommand().isExecutable());
            
            // Execute new milestone command
            viewModel.getNewMilestoneCommand().execute();
            
            // Command execution is logged - in real application would open new milestone dialog
            // For test purposes, we just verify the command executed without error
            assertTrue(viewModel.getNewMilestoneCommand().isExecutable());
        });
    }
    
    @Test
    public void testEditMilestoneCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project and select milestone
            viewModel.initWithProject(testProject);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedMilestone(upcomingMilestone);
            
            // Verify command is executable
            assertTrue(viewModel.getEditMilestoneCommand().isExecutable());
            
            // Execute edit milestone command
            viewModel.getEditMilestoneCommand().execute();
            
            // Command execution is logged - in real application would open edit milestone dialog
            // For test purposes, we just verify the command executed without error
            assertNotNull(viewModel.getSelectedMilestone());
        });
    }
    
    @Test
    public void testDeleteMilestoneCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project and select milestone
            viewModel.initWithProject(testProject);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedMilestone(upcomingMilestone);
            
            // Verify initial state
            assertEquals(3, viewModel.getAllMilestones().size());
            assertTrue(viewModel.getDeleteMilestoneCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteMilestoneCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify milestone was removed from list
            assertEquals(2, viewModel.getAllMilestones().size());
            assertFalse(viewModel.getAllMilestones().contains(upcomingMilestone));
            
            // Verify selection was cleared
            assertNull(viewModel.getSelectedMilestone());
        });
    }
    
    @Test
    public void testRefreshMilestonesCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let initial load complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify milestones loaded
            assertEquals(3, viewModel.getAllMilestones().size());
            
            // Execute refresh command
            viewModel.getRefreshMilestonesCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify milestones are still loaded (refresh worked)
            assertEquals(3, viewModel.getAllMilestones().size());
        });
    }
    
    @Test
    public void testLoadingState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Set project to trigger loading
            viewModel.setCurrentProject(testProject);
            
            // Should be loading immediately after setting project
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testFilterEnumValues() {
        // Test the MilestoneFilter enum
        assertEquals("All Milestones", MilestoneFilter.ALL.toString());
        assertEquals("Upcoming", MilestoneFilter.UPCOMING.toString());
        assertEquals("Passed", MilestoneFilter.PASSED.toString());
    }
    
    @Test
    public void testPropertyBindings() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test that properties are properly bound
            assertNotNull(viewModel.selectedMilestoneProperty());
            assertNotNull(viewModel.currentProjectProperty());
            assertNotNull(viewModel.currentFilterProperty());
            assertNotNull(viewModel.loadingProperty());
            assertNotNull(viewModel.getAllMilestones());
            assertNotNull(viewModel.getFilteredMilestones());
            
            // Test property changes
            viewModel.setSelectedMilestone(upcomingMilestone);
            assertEquals(upcomingMilestone, viewModel.selectedMilestoneProperty().get());
            
            viewModel.setCurrentProject(testProject);
            assertEquals(testProject, viewModel.currentProjectProperty().get());
            
            viewModel.setCurrentFilter(MilestoneFilter.UPCOMING);
            assertEquals(MilestoneFilter.UPCOMING, viewModel.currentFilterProperty().get());
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify milestones were loaded
            assertFalse(viewModel.getAllMilestones().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getAllMilestones().isEmpty());
        });
    }
    
    @Test
    public void testErrorHandling() {
        // Configure repository to throw exception
        var milestoneRepo = TestModule.getRepository(org.frcpm.repositories.specific.MilestoneRepository.class);
        when(milestoneRepo.findByProject(any())).thenThrow(new RuntimeException("Database error"));
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project (this will trigger the error)
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load milestones"));
            
            // Should not be loading
            assertFalse(viewModel.isLoading());
            
            // Milestones list should be empty
            assertTrue(viewModel.getAllMilestones().isEmpty());
        });
    }
    
    @Test
    public void testDeleteMilestoneFailure() {
        // Configure repository to return false for delete
        var milestoneRepo = TestModule.getRepository(org.frcpm.repositories.specific.MilestoneRepository.class);
        when(milestoneRepo.deleteById(anyLong())).thenReturn(false);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project and select milestone
            viewModel.initWithProject(testProject);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedMilestone(upcomingMilestone);
            
            // Execute delete command
            viewModel.getDeleteMilestoneCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Milestone should still be in list (delete failed)
            assertEquals(3, viewModel.getAllMilestones().size());
            assertTrue(viewModel.getAllMilestones().contains(upcomingMilestone));
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete milestone"));
        });
    }
    
    @Test
    public void testApplyFilterMethod() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Test direct applyFilter method call
            viewModel.setCurrentFilter(MilestoneFilter.UPCOMING);
            viewModel.applyFilter();
            
            // Verify filter was applied
            assertEquals(2, viewModel.getFilteredMilestones().size());
            assertTrue(viewModel.getFilteredMilestones().contains(todayMilestone));
            assertTrue(viewModel.getFilteredMilestones().contains(upcomingMilestone));
            assertFalse(viewModel.getFilteredMilestones().contains(pastMilestone));
        });
    }
    
    @Test
    public void testMilestoneHelperMethods() {
        // Test the helper methods in the Milestone model
        // Past milestone
        assertTrue(pastMilestone.isPassed());
        assertTrue(pastMilestone.getDaysUntil() < 0);
        
        // Today milestone
        assertFalse(todayMilestone.isPassed());
        assertEquals(0, todayMilestone.getDaysUntil());
        
        // Upcoming milestone
        assertFalse(upcomingMilestone.isPassed());
        assertTrue(upcomingMilestone.getDaysUntil() > 0);
        assertEquals(15, upcomingMilestone.getDaysUntil());
    }
    
    @Test
    public void testFilteredListUpdatesOnDataChange() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Set filter to show only upcoming milestones
            viewModel.setCurrentFilter(MilestoneFilter.UPCOMING);
            assertEquals(2, viewModel.getFilteredMilestones().size());
            
            // Remove an upcoming milestone from the all milestones list
            viewModel.getAllMilestones().remove(upcomingMilestone);
            
            // Filtered list should automatically update
            assertEquals(1, viewModel.getFilteredMilestones().size());
            assertTrue(viewModel.getFilteredMilestones().contains(todayMilestone));
            assertFalse(viewModel.getFilteredMilestones().contains(upcomingMilestone));
        });
    }
}