// src/test/java/org/frcpm/mvvm/viewmodels/TaskDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.frcpm.di.TestModule;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.Task.Priority;
import org.frcpm.models.TeamMember;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TestableTaskServiceImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for the TaskDetailMvvmViewModel class.
 */
public class TaskDetailMvvmViewModelTest {
    
    private TaskService taskService;
    private TeamMemberService teamMemberService;
    private ComponentService componentService;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    private TeamMember testTeamMember;
    private Component testComponent;
    private TaskDetailMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data first
        setupTestData();
        
        // Replace TaskService with our mock
        TaskService mockTaskService = mock(TaskService.class);
        
        // Configure the mock service
        when(mockTaskService.createTask(
                anyString(), any(Project.class), any(Subsystem.class), 
                anyDouble(), any(Priority.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testTask);
                
        when(mockTaskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean()))
                .thenReturn(testTask);
        
        // Register the mock with TestModule
        TestModule.setService(TaskService.class, mockTaskService);
        
        // Get service references from TestModule
        taskService = TestModule.getService(TaskService.class);
        teamMemberService = TestModule.getService(TeamMemberService.class);
        componentService = TestModule.getService(ComponentService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new TaskDetailMvvmViewModel(taskService, teamMemberService, componentService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test subsystem
        testSubsystem = new Subsystem();
        testSubsystem.setId(1L);
        testSubsystem.setName("Test Subsystem");
        
        // Create test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setProject(testProject);
        testTask.setSubsystem(testSubsystem);
        testTask.setPriority(Priority.MEDIUM);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(7));
        testTask.setEstimatedDuration(Duration.ofHours(10));
        testTask.setProgress(0);
        testTask.setCompleted(false);
        
        // Create test team member
        testTeamMember = new TeamMember();
        testTeamMember.setId(1L);
        testTeamMember.setFirstName("John");
        testTeamMember.setLastName("Doe");
        testTeamMember.setUsername("jdoe");
        
        // Create test component
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Test Component");
    }
    
    @Test
    public void testInitialStateForNewTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Verify initial state
            assertTrue(viewModel.isNewTask());
            assertEquals(testProject, viewModel.getProject());
            assertEquals(testSubsystem, viewModel.getSubsystem());
            assertEquals("", viewModel.getTitle());
            assertEquals("", viewModel.getDescription());
            assertEquals(1.0, viewModel.getEstimatedHours());
            assertEquals(0.0, viewModel.getActualHours());
            assertEquals(Priority.MEDIUM, viewModel.getPriority());
            assertEquals(0, viewModel.getProgress());
            assertEquals(LocalDate.now(), viewModel.getStartDate());
            assertNull(viewModel.getEndDate());
            assertFalse(viewModel.isCompleted());
            assertTrue(viewModel.getAssignedMembers().isEmpty());
            assertTrue(viewModel.getPreDependencies().isEmpty());
            assertTrue(viewModel.getRequiredComponents().isEmpty());
            
            // Verify commands
            assertTrue(viewModel.getSaveCommand().isNotExecutable()); // Not dirty and not valid
        });
    }
    
    @Test
    public void testInitExistingTask() {
        // Create a set of assigned members
        Set<TeamMember> assignedMembers = new HashSet<>();
        assignedMembers.add(testTeamMember);
        testTask.setAssignedTo(assignedMembers);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing task
            viewModel.initExistingTask(testTask);
            
            // Verify state
            assertFalse(viewModel.isNewTask());
            assertEquals(testProject, viewModel.getProject());
            assertEquals(testSubsystem, viewModel.getSubsystem());
            assertEquals("Test Task", viewModel.getTitle());
            assertEquals("Test Description", viewModel.getDescription());
            assertEquals(10.0, viewModel.getEstimatedHours());
            assertEquals(0.0, viewModel.getActualHours());
            assertEquals(Priority.MEDIUM, viewModel.getPriority());
            assertEquals(0, viewModel.getProgress());
            assertEquals(LocalDate.now(), viewModel.getStartDate());
            assertEquals(LocalDate.now().plusDays(7), viewModel.getEndDate());
            assertFalse(viewModel.isCompleted());
            
            // Verify collections
            assertEquals(1, viewModel.getAssignedMembers().size());
            assertEquals(testTeamMember, viewModel.getAssignedMembers().get(0));
        });
    }
    
    @Test
    public void testAddRemoveMember() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Initially no members
            assertTrue(viewModel.getAssignedMembers().isEmpty());
            
            // Add a member
            viewModel.addMember(testTeamMember);
            
            // Verify member was added
            assertEquals(1, viewModel.getAssignedMembers().size());
            assertEquals(testTeamMember, viewModel.getAssignedMembers().get(0));
            
            // Verify dirty flag set
            assertTrue(viewModel.isDirty());
            
            // Set selected member
            viewModel.setSelectedMember(testTeamMember);
            
            // Verify remove command is executable
            assertTrue(viewModel.getRemoveMemberCommand().isExecutable());
            
            // Execute remove command
            viewModel.getRemoveMemberCommand().execute();
            
            // Verify member was removed
            assertTrue(viewModel.getAssignedMembers().isEmpty());
        });
    }
    
    @Test
    public void testAddRemoveComponent() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Initially no components
            assertTrue(viewModel.getRequiredComponents().isEmpty());
            
            // Add a component
            viewModel.addComponent(testComponent);
            
            // Verify component was added
            assertEquals(1, viewModel.getRequiredComponents().size());
            assertEquals(testComponent, viewModel.getRequiredComponents().get(0));
            
            // Verify dirty flag set
            assertTrue(viewModel.isDirty());
            
            // Set selected component
            viewModel.setSelectedComponent(testComponent);
            
            // Verify remove command is executable
            assertTrue(viewModel.getRemoveComponentCommand().isExecutable());
            
            // Execute remove command
            viewModel.getRemoveComponentCommand().execute();
            
            // Verify component was removed
            assertTrue(viewModel.getRequiredComponents().isEmpty());
        });
    }
    
    @Test
    public void testSaveNewTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Set required properties
            viewModel.setTitle("New Task");
            viewModel.setDescription("New Description");
            viewModel.setEstimatedHours(5.0);
            viewModel.setStartDate(LocalDate.now());
            viewModel.setEndDate(LocalDate.now().plusDays(14));
            
            // Add a team member
            viewModel.addMember(testTeamMember);
            
            // Validate the task
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Verify task service was called
            verify(taskService).createTask(
                    eq("New Task"),
                    eq(testProject),
                    eq(testSubsystem),
                    eq(5.0),
                    eq(Priority.MEDIUM),
                    eq(LocalDate.now()),
                    eq(LocalDate.now().plusDays(14))
            );
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Test
    public void testUpdateExistingTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing task
            viewModel.initExistingTask(testTask);
            
            // Update some properties
            viewModel.setTitle("Updated Task");
            viewModel.setProgress(50);
            
            // Should be dirty but valid
            assertTrue(viewModel.isDirty());
            assertTrue(viewModel.isValid());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Verify update progress was called
            verify(taskService).updateTaskProgress(
                    eq(1L),
                    eq(50),
                    eq(false)
            );
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Test
    public void testCompletionAndProgressSync() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Set completion to true
            viewModel.setCompleted(true);
            
            // Progress should be set to 100%
            assertEquals(100, viewModel.getProgress());
            
            // Reset progress
            viewModel.setProgress(0);
            
            // Setting progress to 100% should set completed
            viewModel.setProgress(100);
            assertTrue(viewModel.isCompleted());
        });
    }
    
    @Test
    public void testValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new task
            viewModel.initNewTask(testProject, testSubsystem);
            
            // Initially not valid (title is empty)
            assertFalse(viewModel.isValid());
            
            // Set title only
            viewModel.setTitle("Test Task");
            
            // Should still be valid - project and subsystem required but already set in initNewTask
            assertTrue(viewModel.isValid());
            
            // Set start date after end date - should be invalid
            viewModel.setStartDate(LocalDate.now().plusDays(10));
            viewModel.setEndDate(LocalDate.now());
            
            // Should be invalid due to dates
            assertFalse(viewModel.isValid());
            
            // Fix dates
            viewModel.setStartDate(LocalDate.now());
            viewModel.setEndDate(LocalDate.now().plusDays(10));
            
            // Should be valid now
            assertTrue(viewModel.isValid());
            
            // Set invalid hours
            viewModel.setEstimatedHours(-1.0);
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            
            // Fix hours
            viewModel.setEstimatedHours(10.0);
            
            // Should be valid again
            assertTrue(viewModel.isValid());
        });
    }
}