// src/test/java/org/frcpm/mvvm/viewmodels/MilestoneDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.frcpm.di.TestModule;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the MilestoneDetailMvvmViewModel class.
 */
public class MilestoneDetailMvvmViewModelTest {
    
    private MilestoneService milestoneService;
    
    private Project testProject;
    private Milestone testMilestone;
    private MilestoneDetailMvvmViewModel viewModel;
    
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
            viewModel = new MilestoneDetailMvvmViewModel(milestoneService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusMonths(1));
        testProject.setGoalEndDate(LocalDate.now().plusMonths(2));
        testProject.setHardDeadline(LocalDate.now().plusMonths(3));
        
        // Create test milestone
        testMilestone = new Milestone();
        testMilestone.setId(1L);
        testMilestone.setName("Test Milestone");
        testMilestone.setDescription("Test Description");
        testMilestone.setDate(LocalDate.now().plusDays(30));
        testMilestone.setProject(testProject);
    }
    
    private void configureMilestoneService() {
        // Since we're using TestableMilestoneServiceImpl, we need to work with the actual service
        // Configure the repositories and additional mocking if needed
        try {
            // Get the project repository from TestModule
            var projectRepo = TestModule.getRepository(org.frcpm.repositories.specific.ProjectRepository.class);
            when(projectRepo.findById(1L)).thenReturn(java.util.Optional.of(testProject));
            
            // Get the milestone repository from TestModule
            var milestoneRepo = TestModule.getRepository(org.frcpm.repositories.specific.MilestoneRepository.class);
            when(milestoneRepo.findById(1L)).thenReturn(java.util.Optional.of(testMilestone));
            when(milestoneRepo.save(any(Milestone.class))).thenAnswer(invocation -> {
                Milestone milestone = invocation.getArgument(0);
                if (milestone.getId() == null) {
                    milestone.setId(2L); // Simulate new ID assignment
                }
                return milestone;
            });
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
            assertEquals("", viewModel.getName());
            assertEquals("", viewModel.getDescription());
            assertEquals(LocalDate.now(), viewModel.getDate());
            assertNull(viewModel.getProject());
            assertNull(viewModel.getMilestone());
            assertTrue(viewModel.isNewMilestone());
            assertFalse(viewModel.isValid());
            assertFalse(viewModel.isLoading());
            assertFalse(viewModel.isDirty());
            
            // Check that commands exist
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            
            // Save command should not be executable (not valid and not dirty)
            assertFalse(viewModel.getSaveCommand().isExecutable());
            
            // Cancel command should always be executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitNewMilestone() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Verify state
            assertTrue(viewModel.isNewMilestone());
            assertEquals(testProject, viewModel.getProject());
            assertNull(viewModel.getMilestone());
            assertEquals("", viewModel.getName());
            assertEquals("", viewModel.getDescription());
            assertEquals(LocalDate.now(), viewModel.getDate());
            assertFalse(viewModel.isDirty());
            assertFalse(viewModel.isValid()); // Name is empty
            
            // Save command should not be executable (not valid)
            assertFalse(viewModel.getSaveCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitExistingMilestone() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing milestone
            viewModel.initExistingMilestone(testMilestone);
            
            // Verify state
            assertFalse(viewModel.isNewMilestone());
            assertEquals(testProject, viewModel.getProject());
            assertEquals(testMilestone, viewModel.getMilestone());
            assertEquals("Test Milestone", viewModel.getName());
            assertEquals("Test Description", viewModel.getDescription());
            assertEquals(LocalDate.now().plusDays(30), viewModel.getDate());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid());
            
            // Save command should not be executable (not dirty)
            assertFalse(viewModel.getSaveCommand().isExecutable());
        });
    }
    
    @Test
    public void testValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Initially not valid (name is empty)
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Milestone name cannot be empty"));
            
            // Set name only
            viewModel.setName("Test Milestone");
            
            // Should be valid now (project and date are set from init)
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set date before project start date
            viewModel.setDate(testProject.getStartDate().minusDays(1));
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Milestone date cannot be before project start date"));
            
            // Set date after project deadline
            viewModel.setDate(testProject.getHardDeadline().plusDays(1));
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Milestone date cannot be after project hard deadline"));
            
            // Set valid date
            viewModel.setDate(LocalDate.now().plusDays(30));
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDirtyFlag() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Initially not dirty
            assertFalse(viewModel.isDirty());
            
            // Change name
            viewModel.setName("New Milestone");
            
            // Should be dirty
            assertTrue(viewModel.isDirty());
            
            // Change date
            viewModel.setDate(LocalDate.now().plusDays(15));
            
            // Should still be dirty
            assertTrue(viewModel.isDirty());
            
            // Change description (doesn't affect validation but marks dirty)
            viewModel.setDescription("New Description");
            
            // Should still be dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testSaveNewMilestone() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Set required properties
            viewModel.setName("New Milestone");
            viewModel.setDescription("New Description");
            viewModel.setDate(LocalDate.now().plusDays(45));
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
            
            // Should not be dirty after successful save
            assertFalse(viewModel.isDirty());
            
            // Should have a milestone set (created)
            assertNotNull(viewModel.getMilestone());
        });
    }
    
    @Test
    public void testSaveExistingMilestone() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing milestone
            viewModel.initExistingMilestone(testMilestone);
            
            // Update some properties
            viewModel.setName("Updated Milestone");
            viewModel.setDate(LocalDate.now().plusDays(60));
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
            
            // Should not be dirty after successful save
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testSaveExistingMilestoneDescriptionOnly() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing milestone
            viewModel.initExistingMilestone(testMilestone);
            
            // Update only description (no date change)
            viewModel.setDescription("Updated Description");
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
            
            // Should not be dirty after successful save
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testCancelCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Make some changes
            viewModel.setName("Test Milestone");
            viewModel.setDescription("Test Description");
            
            // Should be dirty
            assertTrue(viewModel.isDirty());
            
            // Cancel command should always be executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
            
            // Execute cancel command
            viewModel.getCancelCommand().execute();
            
            // Command execution is logged - in real application would close dialog
            // For test purposes, we just verify the command executed without error
            assertTrue(viewModel.getCancelCommand().isExecutable());
        });
    }
    
    @Test
    public void testPropertyBindings() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test that properties are properly bound
            assertNotNull(viewModel.nameProperty());
            assertNotNull(viewModel.descriptionProperty());
            assertNotNull(viewModel.dateProperty());
            assertNotNull(viewModel.projectProperty());
            assertNotNull(viewModel.milestoneProperty());
            assertNotNull(viewModel.isNewMilestoneProperty());
            assertNotNull(viewModel.validProperty());
            assertNotNull(viewModel.loadingProperty());
            
            // Test property changes
            viewModel.setName("Test Name");
            assertEquals("Test Name", viewModel.nameProperty().get());
            
            viewModel.setDescription("Test Description");
            assertEquals("Test Description", viewModel.descriptionProperty().get());
            
            LocalDate testDate = LocalDate.now().plusDays(10);
            viewModel.setDate(testDate);
            assertEquals(testDate, viewModel.dateProperty().get());
            
            viewModel.setProject(testProject);
            assertEquals(testProject, viewModel.projectProperty().get());
        });
    }
    
    @Test
    public void testInvalidArgumentHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test null milestone in initExistingMilestone
            assertThrows(IllegalArgumentException.class, () -> {
                viewModel.initExistingMilestone(null);
            });
        });
    }
    
    @Test
    public void testErrorHandling() {
        // Configure repository to throw exception on save
        var milestoneRepo = TestModule.getRepository(org.frcpm.repositories.specific.MilestoneRepository.class);
        when(milestoneRepo.save(any(Milestone.class))).thenThrow(new RuntimeException("Database error"));
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Set required properties
            viewModel.setName("New Milestone");
            viewModel.setDate(LocalDate.now().plusDays(30));
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to create milestone"));
            
            // Should not be loading
            assertFalse(viewModel.isLoading());
            
            // Should still be dirty (save failed)
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new milestone
            viewModel.initNewMilestone(testProject);
            
            // Set some properties
            viewModel.setName("Test Milestone");
            viewModel.setDescription("Test Description");
            
            // Call dispose
            viewModel.dispose();
            
            // ViewModel should still be functional after dispose
            // (dispose mainly cleans up listeners)
            assertEquals("Test Milestone", viewModel.getName());
            assertEquals("Test Description", viewModel.getDescription());
        });
    }
}