// src/test/java/org/frcpm/mvvm/viewmodels/SubsystemDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SubsystemDetailMvvmViewModel class.
 */
public class SubsystemDetailMvvmViewModelTest {
    
    private SubsystemService subsystemService;
    private SubteamService subteamService;
    private TaskService taskService;
    
    private Subsystem testSubsystem;
    private List<Subteam> testSubteams;
    private List<Task> testTasks;
    private SubsystemDetailMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mocks first
        SubsystemService mockSubsystemService = mock(SubsystemService.class);
        SubteamService mockSubteamService = mock(SubteamService.class);
        TaskService mockTaskService = mock(TaskService.class);
        
        // Configure mock behavior for sync methods
        when(mockSubsystemService.findById(anyLong())).thenReturn(testSubsystem);
        when(mockSubsystemService.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockSubteamService.findAll()).thenReturn(testSubteams); // Use synchronous method
        when(mockTaskService.findById(anyLong())).thenReturn(testTasks.get(0));
        
        // Configure mock behavior for async methods that are known to exist
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Consumer<Subsystem> callback = invocation.getArgument(1);
            callback.accept(testSubsystem);
            return null;
        }).when(mockSubsystemService).findByIdAsync(anyLong(), any(), any());
        
        doAnswer(invocation -> {
            Subsystem subsystem = invocation.getArgument(0);
            Consumer<Subsystem> callback = invocation.getArgument(1);
            callback.accept(subsystem);
            return null;
        }).when(mockSubsystemService).saveAsync(any(), any(), any());
        
        // Register mocks with TestModule
        TestModule.setService(SubsystemService.class, mockSubsystemService);
        TestModule.setService(SubteamService.class, mockSubteamService);
        TestModule.setService(TaskService.class, mockTaskService);
        
        // Get services from TestModule (now returns our mocks)
        subsystemService = TestModule.getService(SubsystemService.class);
        subteamService = TestModule.getService(SubteamService.class);
        taskService = TestModule.getService(TaskService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new SubsystemDetailMvvmViewModel(subsystemService, subteamService, taskService);
        });
    }
    
    private void setupTestData() {
        // Create test subteams
        testSubteams = new ArrayList<>();
        
        Subteam mechSubteam = new Subteam();
        mechSubteam.setId(1L);
        mechSubteam.setName("Mechanical");
        testSubteams.add(mechSubteam);
        
        Subteam elecSubteam = new Subteam();
        elecSubteam.setId(2L);
        elecSubteam.setName("Electrical");
        testSubteams.add(elecSubteam);
        
        Subteam progSubteam = new Subteam();
        progSubteam.setId(3L);
        progSubteam.setName("Programming");
        testSubteams.add(progSubteam);
        
        // Create test subsystem
        testSubsystem = new Subsystem();
        testSubsystem.setId(1L);
        testSubsystem.setName("Drive Train");
        testSubsystem.setDescription("Drivetrain subsystem for robot");
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem.setResponsibleSubteam(mechSubteam);
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Install motors");
        task1.setDescription("Install and secure drive motors");
        task1.setProgress(50);
        task1.setCompleted(false);
        task1.setSubsystem(testSubsystem);
        testTasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Connect motor controllers");
        task2.setDescription("Wire motor controllers to power and CAN bus");
        task2.setProgress(25);
        task2.setCompleted(false);
        task2.setSubsystem(testSubsystem);
        testTasks.add(task2);
        
        // Add tasks to subsystem
        testSubsystem.getTasks().addAll(testTasks);
    }
    
    @Test
    public void testInitialStateForNewSubsystem() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Verify initial state
            assertTrue(viewModel.isNewSubsystem());
            assertEquals("", viewModel.getSubsystemName());
            assertEquals("", viewModel.getSubsystemDescription());
            assertEquals(Subsystem.Status.NOT_STARTED, viewModel.getStatus());
            assertNull(viewModel.getResponsibleSubteam());
            assertFalse(viewModel.isDirty());
            assertFalse(viewModel.isValid()); // Should be invalid due to empty name
            
            // Verify tasks list is empty
            assertTrue(viewModel.getTasks().isEmpty());
            
            // Verify commands
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            assertNotNull(viewModel.getAddTaskCommand());
            assertNotNull(viewModel.getViewTaskCommand());
            
            // Save command should not be executable (not valid)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Add task command should not be executable (new subsystem)
            assertTrue(viewModel.getAddTaskCommand().isNotExecutable());
            
            // View task command should not be executable (no selection)
            assertTrue(viewModel.getViewTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testInitExistingSubsystem() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Verify state
            assertFalse(viewModel.isNewSubsystem());
            assertEquals("Drive Train", viewModel.getSubsystemName());
            assertEquals("Drivetrain subsystem for robot", viewModel.getSubsystemDescription());
            assertEquals(Subsystem.Status.IN_PROGRESS, viewModel.getStatus());
            assertEquals(testSubteams.get(0), viewModel.getResponsibleSubteam());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid()); // Should be valid with existing data
            
            // Verify subsystem reference
            assertEquals(testSubsystem, viewModel.getSelectedSubsystem());
            
            // Add task command should be executable for existing subsystem
            assertTrue(viewModel.getAddTaskCommand().isExecutable());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(2, viewModel.getTasks().size());
        });
    }
    
    @Test
    public void testValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Initially not valid (name is empty)
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("name"));
            
            // Set name only
            viewModel.setSubsystemName("Test Subsystem");
            
            // Should now be valid
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set null status (which shouldn't happen in UI but test anyway)
            viewModel.setStatus(null);
            
            // Should be invalid due to missing status
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Status"));
            
            // Set status back
            viewModel.setStatus(Subsystem.Status.NOT_STARTED);
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testSaveNewSubsystem() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Set required properties
            viewModel.setSubsystemName("New Subsystem");
            viewModel.setSubsystemDescription("Description for new subsystem");
            viewModel.setStatus(Subsystem.Status.NOT_STARTED);
            viewModel.setResponsibleSubteam(testSubteams.get(0));
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be a new subsystem
            assertFalse(viewModel.isNewSubsystem());
            
            // Should no longer be dirty
            assertFalse(viewModel.isDirty());
            
            // Verify save was called
            verify(subsystemService).saveAsync(any(Subsystem.class), any(), any());
        });
    }
    
       

    @Test
    public void testTaskRelatedFunctionality() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks list
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(2, viewModel.getTasks().size());
            
            // Test task selection
            assertNull(viewModel.getSelectedTask());
            viewModel.setSelectedTask(testTasks.get(0));
            assertEquals(testTasks.get(0), viewModel.getSelectedTask());
            
            // Test Add Task command existence (not testing execution)
            assertNotNull(viewModel.getAddTaskCommand());
        });
    }
    @Test
    public void testTaskStatistics() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify task statistics
            assertEquals(2, viewModel.totalTasksProperty().get());
            assertEquals(0, viewModel.completedTasksProperty().get());
            
            // Average progress is (50 + 25) / 2 = 37.5
            assertEquals(37.5, viewModel.completionPercentageProperty().get(), 0.01);
            
            // Complete one task
            testTasks.get(0).setCompleted(true);
            testTasks.get(0).setProgress(100);
            
            // Force update statistics
            try {
                TestUtils.invokePrivateMethod(viewModel, "updateTaskStatistics", new Class<?>[]{}, new Object[]{});
            } catch (Exception e) {
                fail("Failed to invoke updateTaskStatistics: " + e.getMessage());
            }
            
            // Verify updated statistics
            assertEquals(2, viewModel.totalTasksProperty().get());
            assertEquals(1, viewModel.completedTasksProperty().get());
            
            // Average progress is (100 + 25) / 2 = 62.5
            assertEquals(62.5, viewModel.completionPercentageProperty().get(), 0.01);
        });
    }
    
    @Test
    public void testErrorHandlingDuringSave() {
        // Create new mocks for error scenario
        SubsystemService errorSubsystemService = mock(SubsystemService.class);
        
        // Configure mock service to return an error
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Save error"));
            return null;
        }).when(errorSubsystemService).saveAsync(any(), any(), any());
        
        // Register error mock
        TestModule.setService(SubsystemService.class, errorSubsystemService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with error service
            SubsystemDetailMvvmViewModel errorViewModel = new SubsystemDetailMvvmViewModel(
                TestModule.getService(SubsystemService.class), 
                TestModule.getService(SubteamService.class),
                TestModule.getService(TaskService.class)
            );
            
            // Initialize for a new subsystem
            errorViewModel.initNewSubsystem();
            errorViewModel.setSubsystemName("Test Subsystem");
            
            // Execute save command
            errorViewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to save"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
        });
    }
    
    @Test
    public void testErrorHandlingDuringSubteamLoad() {
        // Create new mocks for error scenario
        SubteamService errorSubteamService = mock(SubteamService.class);
        
        // Configure mock service to throw an exception for synchronous findAll
        when(errorSubteamService.findAll()).thenThrow(new RuntimeException("Load error"));
        
        // Register error mock
        TestModule.setService(SubteamService.class, errorSubteamService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with error service
            SubsystemDetailMvvmViewModel errorViewModel = new SubsystemDetailMvvmViewModel(
                TestModule.getService(SubsystemService.class), 
                TestModule.getService(SubteamService.class),
                TestModule.getService(TaskService.class)
            );
            
            // Initialize for a new subsystem - this would trigger subteam loading
            errorViewModel.initNewSubsystem();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
            
            // Available subteams should be empty
            assertTrue(errorViewModel.getAvailableSubteams().isEmpty());
        });
    }
    
    @Test
    public void testStatusOptions() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify status options
            assertEquals(5, viewModel.getStatusOptions().size());
            assertTrue(viewModel.getStatusOptions().contains(Subsystem.Status.NOT_STARTED));
            assertTrue(viewModel.getStatusOptions().contains(Subsystem.Status.IN_PROGRESS));
            assertTrue(viewModel.getStatusOptions().contains(Subsystem.Status.COMPLETED));
            assertTrue(viewModel.getStatusOptions().contains(Subsystem.Status.TESTING));
            assertTrue(viewModel.getStatusOptions().contains(Subsystem.Status.ISSUES));
        });
    }
    
    @Test
    public void testPropertyBindings() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Test name property
            viewModel.setSubsystemName("Test Name");
            assertEquals("Test Name", viewModel.getSubsystemName());
            assertEquals("Test Name", viewModel.subsystemNameProperty().get());
            
            // Test description property
            viewModel.setSubsystemDescription("Test Description");
            assertEquals("Test Description", viewModel.getSubsystemDescription());
            assertEquals("Test Description", viewModel.subsystemDescriptionProperty().get());
            
            // Test status property
            viewModel.setStatus(Subsystem.Status.TESTING);
            assertEquals(Subsystem.Status.TESTING, viewModel.getStatus());
            assertEquals(Subsystem.Status.TESTING, viewModel.statusProperty().get());
            
            // Test responsible subteam property
            viewModel.setResponsibleSubteam(testSubteams.get(1));
            assertEquals(testSubteams.get(1), viewModel.getResponsibleSubteam());
            assertEquals(testSubteams.get(1), viewModel.responsibleSubteamProperty().get());
        });
    }
    
    @Test
    public void testTaskSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially no task selected
            assertNull(viewModel.getSelectedTask());
            
            // View task command should not be executable (no selection)
            assertTrue(viewModel.getViewTaskCommand().isNotExecutable());
            
            // Select a task
            Task selectedTask = testTasks.get(0);
            viewModel.setSelectedTask(selectedTask);
            
            // Verify selection
            assertEquals(selectedTask, viewModel.getSelectedTask());
            
            // View task command should now be executable
            assertTrue(viewModel.getViewTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testCanAddTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // New subsystem (not saved yet)
            viewModel.initNewSubsystem();
            
            // Add task command should not be executable
            assertTrue(viewModel.getAddTaskCommand().isNotExecutable());
            
            // Now test with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Add task command should be executable
            assertTrue(viewModel.getAddTaskCommand().isExecutable());
            
            // Test during loading state
            try {
                TestUtils.setPrivateField(viewModel, "loading", new javafx.beans.property.SimpleBooleanProperty(true));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            // Add task command should not be executable while loading
            assertTrue(viewModel.getAddTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testAsyncServiceMocking() {
        // Verify that the services are mocks and async methods work
        assertNotNull(subsystemService);
        assertNotNull(subteamService);
        assertNotNull(taskService);
        
        // Verify we can stub the async methods without NotAMockException
        doAnswer(invocation -> {
            Consumer<Subsystem> callback = invocation.getArgument(1);
            callback.accept(testSubsystem);
            return null;
        }).when(subsystemService).findByIdAsync(anyLong(), any(), any());
        
        // Test that the stubbing worked
        subsystemService.findByIdAsync(1L, 
            result -> assertEquals(testSubsystem, result),
            error -> fail("Should not have error"));
    }
    
    @Test
    public void testSubteamLoading() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify subteams were loaded
            assertFalse(viewModel.getAvailableSubteams().isEmpty());
            assertEquals(3, viewModel.getAvailableSubteams().size());
            
            // Verify specific subteams
            assertTrue(viewModel.getAvailableSubteams().contains(testSubteams.get(0)));
            assertTrue(viewModel.getAvailableSubteams().contains(testSubteams.get(1)));
            assertTrue(viewModel.getAvailableSubteams().contains(testSubteams.get(2)));
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify data exists
            assertFalse(viewModel.getTasks().isEmpty());
            assertFalse(viewModel.getAvailableSubteams().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getTasks().isEmpty());
            assertTrue(viewModel.getAvailableSubteams().isEmpty());
        });
    }
    
    @Test
    public void testDirtyFlag() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subsystem
            viewModel.initNewSubsystem();
            
            // Initially not dirty
            assertFalse(viewModel.isDirty());
            
            // Change name
            viewModel.setSubsystemName("Test Name");
            
            // Should be dirty now
            assertTrue(viewModel.isDirty());
            
            // Change other properties
            viewModel.setSubsystemDescription("Test Description");
            viewModel.setStatus(Subsystem.Status.TESTING);
            viewModel.setResponsibleSubteam(testSubteams.get(0));
            
            // Should still be dirty
            assertTrue(viewModel.isDirty());
            
            // Configure mock for saving
            doAnswer(invocation -> {
                Subsystem subsystem = invocation.getArgument(0);
                Consumer<Subsystem> callback = invocation.getArgument(1);
                subsystem.setId(1L); // Give it an ID
                callback.accept(subsystem);
                return null;
            }).when(subsystemService).saveAsync(any(), any(), any());
            
            // Save the subsystem
            viewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be dirty after saving
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testLoadTasks() {
        // Configure mock subsystem service
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Consumer<Subsystem> callback = invocation.getArgument(1);
            callback.accept(testSubsystem);
            return null;
        }).when(subsystemService).findByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subsystem
            viewModel.initExistingSubsystem(testSubsystem);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Clear tasks and verify
            viewModel.getTasks().clear();
            assertTrue(viewModel.getTasks().isEmpty());
            
            // Call loadTasks through reflection
            try {
                TestUtils.invokePrivateMethod(viewModel, "loadTasks", new Class<?>[]{}, new Object[]{});
            } catch (Exception e) {
                fail("Failed to invoke loadTasks: " + e.getMessage());
            }
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(2, viewModel.getTasks().size());
        });
    }
}