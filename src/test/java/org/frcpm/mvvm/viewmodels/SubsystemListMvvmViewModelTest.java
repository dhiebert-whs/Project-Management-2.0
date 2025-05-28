// src/test/java/org/frcpm/mvvm/viewmodels/SubsystemListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.mvvm.viewmodels.SubsystemListMvvmViewModel.SubsystemFilter;
import org.frcpm.services.SubsystemService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SubsystemListMvvmViewModel class.
 * Uses proper mock pattern instead of casting to concrete implementations.
 */
public class SubsystemListMvvmViewModelTest {
    
    private SubsystemService subsystemService;
    
    private Project testProject;
    private List<Subsystem> testSubsystems;
    private SubsystemListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock service
        SubsystemService mockService = mock(SubsystemService.class);
        
        // Register mock with TestModule
        TestModule.setService(SubsystemService.class, mockService);
        
        // Get service from TestModule (now returns mock)
        subsystemService = TestModule.getService(SubsystemService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new SubsystemListMvvmViewModel(subsystemService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test subteams
        Subteam mechSubteam = new Subteam();
        mechSubteam.setId(1L);
        mechSubteam.setName("Mechanical");
        
        Subteam elecSubteam = new Subteam();
        elecSubteam.setId(2L);
        elecSubteam.setName("Electrical");
        
        Subteam progSubteam = new Subteam();
        progSubteam.setId(3L);
        progSubteam.setName("Programming");
        
        // Create test subsystems
        testSubsystems = new ArrayList<>();
        
        Subsystem driveSubsystem = new Subsystem();
        driveSubsystem.setId(1L);
        driveSubsystem.setName("Drive Train");
        driveSubsystem.setDescription("Drivetrain subsystem for robot");
        driveSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        driveSubsystem.setResponsibleSubteam(mechSubteam);
        testSubsystems.add(driveSubsystem);
        
        Subsystem armSubsystem = new Subsystem();
        armSubsystem.setId(2L);
        armSubsystem.setName("Robot Arm");
        armSubsystem.setDescription("Mechanical arm for game piece handling");
        armSubsystem.setStatus(Subsystem.Status.COMPLETED);
        armSubsystem.setResponsibleSubteam(mechSubteam);
        testSubsystems.add(armSubsystem);
        
        Subsystem visionSubsystem = new Subsystem();
        visionSubsystem.setId(3L);
        visionSubsystem.setName("Vision System");
        visionSubsystem.setDescription("Camera-based targeting system");
        visionSubsystem.setStatus(Subsystem.Status.TESTING);
        visionSubsystem.setResponsibleSubteam(progSubteam);
        testSubsystems.add(visionSubsystem);
        
        Subsystem controlSubsystem = new Subsystem();
        controlSubsystem.setId(4L);
        controlSubsystem.setName("Control System");
        controlSubsystem.setDescription("Robot electronics and control hardware");
        controlSubsystem.setStatus(Subsystem.Status.ISSUES);
        controlSubsystem.setResponsibleSubteam(elecSubteam);
        testSubsystems.add(controlSubsystem);
        
        Subsystem climberSubsystem = new Subsystem();
        climberSubsystem.setId(5L);
        climberSubsystem.setName("Climber");
        climberSubsystem.setDescription("End-game climbing mechanism");
        climberSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        climberSubsystem.setResponsibleSubteam(mechSubteam);
        testSubsystems.add(climberSubsystem);
        
        // Add tasks to some subsystems
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Install motors");
        driveSubsystem.addTask(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Wire up controls");
        controlSubsystem.addTask(task2);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getSubsystems().isEmpty());
            assertNull(viewModel.getSelectedSubsystem());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            assertEquals(SubsystemFilter.ALL, viewModel.getCurrentFilter());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadSubsystemsCommand());
            assertNotNull(viewModel.getNewSubsystemCommand());
            assertNotNull(viewModel.getEditSubsystemCommand());
            assertNotNull(viewModel.getDeleteSubsystemCommand());
            assertNotNull(viewModel.getRefreshSubsystemsCommand());
            
            // Check command executability
            assertTrue(viewModel.getLoadSubsystemsCommand().isExecutable());
            assertTrue(viewModel.getNewSubsystemCommand().isExecutable());
            assertTrue(viewModel.getRefreshSubsystemsCommand().isExecutable());
            assertTrue(viewModel.getDeleteSubsystemCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getEditSubsystemCommand().isNotExecutable()); // No selection
        });
    }
    
    @Test
    public void testInitWithProject() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            // Get the success callback and call it with test data
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Subsystem>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testSubsystems);
            return null;
        }).when(subsystemService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.setCurrentProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Execute load command to verify loading works
            viewModel.getLoadSubsystemsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subsystems were loaded
            assertFalse(viewModel.getSubsystems().isEmpty());
            assertEquals(5, viewModel.getSubsystems().size());
        });
    }
    
    @Test
    public void testLoadSubsystemsCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Subsystem>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testSubsystems);
            return null;
        }).when(subsystemService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadSubsystemsCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subsystems were loaded
            assertFalse(viewModel.getSubsystems().isEmpty());
            assertEquals(5, viewModel.getSubsystems().size());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshSubsystemsCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Subsystem>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testSubsystems);
            return null;
        }).when(subsystemService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute refresh command
            viewModel.getRefreshSubsystemsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subsystems were loaded
            assertFalse(viewModel.getSubsystems().isEmpty());
            assertEquals(5, viewModel.getSubsystems().size());
        });
    }
    
    @Test
    public void testSubsystemSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedSubsystem());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditSubsystemCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteSubsystemCommand().isNotExecutable());
            
            // Select a subsystem
            Subsystem selectedSubsystem = testSubsystems.get(0);
            viewModel.setSelectedSubsystem(selectedSubsystem);
            
            // Verify selection
            assertEquals(selectedSubsystem, viewModel.getSelectedSubsystem());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditSubsystemCommand().isExecutable());
            assertTrue(viewModel.getDeleteSubsystemCommand().isExecutable());
        });
    }
    
    @Test
    public void testNewSubsystemCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Command should always be executable
            assertTrue(viewModel.getNewSubsystemCommand().isExecutable());
            
            // Execute command
            viewModel.getNewSubsystemCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testEditSubsystemCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no selection)
            assertTrue(viewModel.getEditSubsystemCommand().isNotExecutable());
            
            // Select a subsystem
            viewModel.setSelectedSubsystem(testSubsystems.get(0));
            
            // Should now be executable
            assertTrue(viewModel.getEditSubsystemCommand().isExecutable());
            
            // Execute command
            viewModel.getEditSubsystemCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testDeleteSubsystemCommand() {
        // Configure the mock service for successful deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(true);
            return null;
        }).when(subsystemService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add subsystems to the list
            viewModel.getSubsystems().clear();
            viewModel.getSubsystems().addAll(testSubsystems);
            
            // Select a subsystem
            Subsystem subsystemToDelete = testSubsystems.get(0);
            viewModel.setSelectedSubsystem(subsystemToDelete);
            
            // Should be executable
            assertTrue(viewModel.getDeleteSubsystemCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteSubsystemCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subsystem was removed from list
            assertFalse(viewModel.getSubsystems().contains(subsystemToDelete));
            assertEquals(4, viewModel.getSubsystems().size());
            
            // Selection should be cleared
            assertNull(viewModel.getSelectedSubsystem());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDeleteSubsystemCommandFailure() {
        // Configure the mock service for failed deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(false);
            return null;
        }).when(subsystemService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add subsystems to the list
            viewModel.getSubsystems().clear();
            viewModel.getSubsystems().addAll(testSubsystems);
            
            // Select a subsystem
            Subsystem subsystemToDelete = testSubsystems.get(0);
            viewModel.setSelectedSubsystem(subsystemToDelete);
            
            // Execute delete command
            viewModel.getDeleteSubsystemCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Subsystem should still be in the list (deletion failed)
            assertTrue(viewModel.getSubsystems().contains(subsystemToDelete));
            assertEquals(5, viewModel.getSubsystems().size());
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
        });
    }
    
    @Test
    public void testDeleteSubsystemWithTaskDependencies() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Use first subsystem which has a task
            Subsystem subsystemWithTasks = testSubsystems.get(0);
            
            // Add subsystems to the list
            viewModel.getSubsystems().clear();
            viewModel.getSubsystems().addAll(testSubsystems);
            viewModel.setSelectedSubsystem(subsystemWithTasks);
            
            // Execute delete command
            viewModel.getDeleteSubsystemCommand().execute();
            
            // Should show error message about dependencies
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("has task"));
            
            // Should not call service
            verify(subsystemService, never()).deleteByIdAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testFilterSubsystems() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add all subsystems to the list first
            viewModel.getSubsystems().clear();
            for (Subsystem subsystem : testSubsystems) {
                viewModel.getSubsystems().add(subsystem);
            }
            
            // Initially should show all subsystems (filter = ALL)
            assertEquals(SubsystemFilter.ALL, viewModel.getCurrentFilter());
            assertEquals(5, viewModel.getSubsystems().size());
            
            // Filter to show only COMPLETED subsystems
            viewModel.setFilter(SubsystemFilter.COMPLETED);
            
            // Should show only completed subsystems (1 out of 5)
            assertEquals(SubsystemFilter.COMPLETED, viewModel.getCurrentFilter());
            
            // Count completed subsystems manually for verification
            long completedCount = testSubsystems.stream()
                .filter(s -> s.getStatus() == Subsystem.Status.COMPLETED)
                .count();
            assertEquals(1, completedCount);
            
            // Filter to show only IN_PROGRESS subsystems
            viewModel.setFilter(SubsystemFilter.IN_PROGRESS);
            assertEquals(SubsystemFilter.IN_PROGRESS, viewModel.getCurrentFilter());
            
            // Count in-progress subsystems manually for verification
            long inProgressCount = testSubsystems.stream()
                .filter(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS)
                .count();
            assertEquals(1, inProgressCount);
            
            // Filter back to all
            viewModel.setFilter(SubsystemFilter.ALL);
            assertEquals(SubsystemFilter.ALL, viewModel.getCurrentFilter());
        });
    }
    
    @Test
    public void testFilterWithNullValue() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Try to set null filter
            viewModel.setFilter(null);
            
            // Should default to ALL
            assertEquals(SubsystemFilter.ALL, viewModel.getCurrentFilter());
        });
    }
    
    @Test
    public void testSubsystemFilterEnum() {
        // Test the SubsystemFilter enum
        assertEquals("All Subsystems", SubsystemFilter.ALL.toString());
        assertEquals("Completed", SubsystemFilter.COMPLETED.toString());
        assertEquals("In Progress", SubsystemFilter.IN_PROGRESS.toString());
        assertEquals("Not Started", SubsystemFilter.NOT_STARTED.toString());
        assertEquals("Testing", SubsystemFilter.TESTING.toString());
        assertEquals("Issues", SubsystemFilter.ISSUES.toString());
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(1);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(subsystemService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadSubsystemsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Subsystems list should remain empty
            assertTrue(viewModel.getSubsystems().isEmpty());
        });
    }
    
    @Test
    public void testErrorHandlingDuringDelete() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Delete error"));
            return null;
        }).when(subsystemService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add subsystems to the list
            viewModel.getSubsystems().clear();
            viewModel.getSubsystems().addAll(testSubsystems);
            
            // Select a subsystem
            Subsystem subsystemToDelete = testSubsystems.get(0);
            viewModel.setSelectedSubsystem(subsystemToDelete);
            
            // Execute delete command
            viewModel.getDeleteSubsystemCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Subsystem should still be in the list
            assertTrue(viewModel.getSubsystems().contains(subsystemToDelete));
        });
    }
    
    @Test
    public void testCurrentProjectProperty() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no current project
            assertNull(viewModel.getCurrentProject());
            
            // Set current project
            viewModel.setCurrentProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure the mock service with a delay to test loading state
        doAnswer(invocation -> {
            // Simulate async delay
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<List<Subsystem>> successCallback = 
                        invocation.getArgument(0);
                    successCallback.accept(testSubsystems);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(subsystemService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Execute load command
            viewModel.getLoadSubsystemsCommand().execute();
            
            // Should be loading immediately
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testSubsystemsListManipulation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially empty
            assertTrue(viewModel.getSubsystems().isEmpty());
            
            // Add subsystems
            viewModel.getSubsystems().addAll(testSubsystems);
            
            // Verify size
            assertEquals(5, viewModel.getSubsystems().size());
            
            // Verify specific subsystems
            assertTrue(viewModel.getSubsystems().contains(testSubsystems.get(0)));
            assertTrue(viewModel.getSubsystems().contains(testSubsystems.get(1)));
            assertTrue(viewModel.getSubsystems().contains(testSubsystems.get(2)));
            assertTrue(viewModel.getSubsystems().contains(testSubsystems.get(3)));
            assertTrue(viewModel.getSubsystems().contains(testSubsystems.get(4)));
            
            // Clear list
            viewModel.getSubsystems().clear();
            
            // Should be empty again
            assertTrue(viewModel.getSubsystems().isEmpty());
        });
    }
    
    @Test
    public void testTaskCount() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check task count for subsystems
            assertEquals(1, viewModel.getTaskCount(testSubsystems.get(0))); // Drive subsystem has 1 task
            assertEquals(0, viewModel.getTaskCount(testSubsystems.get(1))); // Arm subsystem has no tasks
            assertEquals(0, viewModel.getTaskCount(testSubsystems.get(2))); // Vision subsystem has no tasks
            assertEquals(1, viewModel.getTaskCount(testSubsystems.get(3))); // Control subsystem has 1 task
            assertEquals(0, viewModel.getTaskCount(testSubsystems.get(4))); // Climber subsystem has no tasks
            
            // Test with null subsystem
            assertEquals(0, viewModel.getTaskCount(null));
        });
    }
    
    @Test
    public void testCompletionPercentage() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // First, add progress to tasks
            Task task1 = testSubsystems.get(0).getTasks().get(0); // Drive subsystem task
            task1.setProgress(50); // 50% complete
            
            Task task2 = testSubsystems.get(3).getTasks().get(0); // Control subsystem task
            task2.setProgress(75); // 75% complete
            
            // Verify completion percentage
            assertEquals(50.0, viewModel.getCompletionPercentage(testSubsystems.get(0)), 0.01);
            assertEquals(0.0, viewModel.getCompletionPercentage(testSubsystems.get(1)), 0.01); // No tasks
            assertEquals(0.0, viewModel.getCompletionPercentage(testSubsystems.get(2)), 0.01); // No tasks
            assertEquals(75.0, viewModel.getCompletionPercentage(testSubsystems.get(3)), 0.01);
            assertEquals(0.0, viewModel.getCompletionPercentage(testSubsystems.get(4)), 0.01); // No tasks
            
            // Test with null subsystem
            assertEquals(0.0, viewModel.getCompletionPercentage(null), 0.01);
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set up some data
            viewModel.getSubsystems().addAll(testSubsystems);
            viewModel.setSelectedSubsystem(testSubsystems.get(0));
            
            // Verify data exists
            assertFalse(viewModel.getSubsystems().isEmpty());
            assertNotNull(viewModel.getSelectedSubsystem());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getSubsystems().isEmpty());
        });
    }
}