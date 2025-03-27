// Path: src/test/java/org/frcpm/viewmodels/TaskViewModelTest.java

package org.frcpm.viewmodels;

import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskViewModelTest {

    @Mock
    private TaskService taskService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private ComponentService componentService;
    
    private TaskViewModel viewModel;
    private Project testProject;
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    private Task testTask;
    private TeamMember testMember;
    private Component testComponent;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create test data
        testProject = new Project(
            "Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);
        
        testSubsystem = new Subsystem("Test Subsystem", testProject, testSubteam);
        testSubsystem.setId(1L);
        
        testTask = new Task("Test Task", testProject, testSubsystem);
        testTask.setId(1L);
        testTask.setDescription("Test task description");
        testTask.setEstimatedDuration(Duration.ofHours(2));
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(3));
        
        testMember = new TeamMember("testuser", "Test", "User", "test@example.com");
        testMember.setId(1L);
        testMember.setSubteam(testSubteam);
        
        testComponent = new Component("Test Component", "TC-001");
        testComponent.setId(1L);
        
        // Set up mock service
        when(taskService.createTask(anyString(), any(), any(), anyDouble(), any(), any(), any()))
            .thenReturn(testTask);
        when(taskService.save(any(Task.class))).thenReturn(testTask);
        when(taskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean()))
            .thenReturn(testTask);
        when(taskService.assignMembers(anyLong(), anySet()))
            .thenReturn(testTask);
        when(taskService.addDependency(anyLong(), anyLong()))
            .thenReturn(true);
        
        // Create ViewModel with mocked services
        viewModel = new TaskViewModel(taskService, teamMemberService, componentService);
    }
    
    @Test
    public void testInitNewTask() {
        // Init for new task
        viewModel.initNewTask(testProject, testSubsystem);
        
        // Verify state
        assertEquals(testProject, viewModel.getProject());
        assertEquals(testSubsystem, viewModel.getSubsystem());
        assertNull(viewModel.getTask());
        assertTrue(viewModel.isNewTask());
        
        // Verify default values
        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.descriptionProperty().get());
        assertEquals(1.0, viewModel.estimatedHoursProperty().get());
        assertEquals(0.0, viewModel.actualHoursProperty().get());
        assertEquals(Task.Priority.MEDIUM, viewModel.priorityProperty().get());
        assertEquals(0, viewModel.progressProperty().get());
        assertEquals(LocalDate.now(), viewModel.startDateProperty().get());
        assertNull(viewModel.endDateProperty().get());
        assertFalse(viewModel.completedProperty().get());
        
        // Verify collections are empty
        assertTrue(viewModel.getAssignedMembers().isEmpty());
        assertTrue(viewModel.getPreDependencies().isEmpty());
        assertTrue(viewModel.getRequiredComponents().isEmpty());
        
        // Verify validation state - should be invalid due to empty title
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testInitExistingTask() {
        // Add test data to task
        testTask.assignMember(testMember);
        testTask.addRequiredComponent(testComponent);
        
        // Init for existing task
        viewModel.initExistingTask(testTask);
        
        // Verify state
        assertEquals(testTask, viewModel.getTask());
        assertEquals(testProject, viewModel.getProject());
        assertEquals(testSubsystem, viewModel.getSubsystem());
        assertFalse(viewModel.isNewTask());
        
        // Verify field values
        assertEquals(testTask.getTitle(), viewModel.titleProperty().get());
        assertEquals(testTask.getDescription(), viewModel.descriptionProperty().get());
        assertEquals(2.0, viewModel.estimatedHoursProperty().get());
        assertEquals(Task.Priority.HIGH, viewModel.priorityProperty().get());
        assertEquals(testTask.getProgress(), viewModel.progressProperty().get());
        assertEquals(testTask.getStartDate(), viewModel.startDateProperty().get());
        assertEquals(testTask.getEndDate(), viewModel.endDateProperty().get());
        assertEquals(testTask.isCompleted(), viewModel.completedProperty().get());
        
        // Verify collections
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(testMember));
        assertEquals(1, viewModel.getRequiredComponents().size());
        assertTrue(viewModel.getRequiredComponents().contains(testComponent));
        
        // Verify validation state
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_EmptyTitle() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        
        // Title is already empty by default, so just verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("Task title cannot be empty", viewModel.getErrorMessage());
        
        // Set a valid title and verify it becomes valid
        viewModel.titleProperty().set("Test Task");
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_NegativeEstimatedHours() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.titleProperty().set("Test Task");
        
        // Set invalid value
        viewModel.estimatedHoursProperty().set(-1.0);
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("Estimated hours must be positive", viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_EndDateBeforeStartDate() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.titleProperty().set("Test Task");
        
        // Set invalid dates
        viewModel.startDateProperty().set(LocalDate.now());
        viewModel.endDateProperty().set(LocalDate.now().minusDays(1));
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("End date cannot be before start date", viewModel.getErrorMessage());
    }
    
    @Test
    public void testSave_NewTask() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        
        // Set values
        viewModel.titleProperty().set("New Task");
        viewModel.descriptionProperty().set("New task description");
        viewModel.estimatedHoursProperty().set(3.0);
        viewModel.priorityProperty().set(Task.Priority.HIGH);
        viewModel.startDateProperty().set(LocalDate.now());
        viewModel.endDateProperty().set(LocalDate.now().plusDays(5));
        
        // Add a member and component
        viewModel.addMember(testMember);
        viewModel.addComponent(testComponent);
        
        // Execute save command
        viewModel.getSaveCommand().execute();
        
// Continuation of src/test/java/org/frcpm/viewmodels/TaskViewModelTest.java

        // Verify service call
        verify(taskService).createTask(
            eq("New Task"),
            eq(testProject),
            eq(testSubsystem),
            eq(3.0),
            eq(Task.Priority.HIGH),
            eq(LocalDate.now()),
            eq(LocalDate.now().plusDays(5))
        );
        
        // Verify task is updated
        assertEquals(testTask, viewModel.getTask());
        
        // Verify collections were handled
        // In a real implementation, we would verify the service calls for
        // adding members, dependencies, and components
    }
    
    @Test
    public void testSave_ExistingTask() {
        // Set up
        testTask.assignMember(testMember);
        viewModel.initExistingTask(testTask);
        
        // Set updated values
        viewModel.titleProperty().set("Updated Task");
        viewModel.descriptionProperty().set("Updated description");
        viewModel.estimatedHoursProperty().set(4.0);
        viewModel.priorityProperty().set(Task.Priority.CRITICAL);
        viewModel.progressProperty().set(50);
        viewModel.startDateProperty().set(LocalDate.now().plusDays(1));
        viewModel.endDateProperty().set(LocalDate.now().plusDays(7));
        
        // Execute save command
        viewModel.getSaveCommand().execute();
        
        // Verify service calls
        verify(taskService).updateTaskProgress(
            eq(testTask.getId()),
            eq(50),
            eq(false)
        );
        
        // We would verify other service calls in a real test
        
        // Verify task is updated
        assertEquals(testTask, viewModel.getTask());
    }
    
    @Test
    public void testProgress_CompletionSync() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.titleProperty().set("Test Task");
        
        // Set progress to 100%
        viewModel.setProgress(100);
        
        // Verify completion is also set to true
        assertTrue(viewModel.isCompleted());
        
        // Reset progress and completion
        viewModel.setProgress(50);
        viewModel.setCompleted(false);
        assertEquals(50, viewModel.getProgress());
        assertFalse(viewModel.isCompleted());
        
        // Set completion to true
        viewModel.setCompleted(true);
        
        // Verify progress is set to 100%
        assertEquals(100, viewModel.getProgress());
    }
    
    @Test
    public void testAddRemoveMembers() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        
        // Add a member
        viewModel.addMember(testMember);
        
        // Verify member was added
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(testMember));
        
        // Remove the member
        viewModel.removeMember(testMember);
        
        // Verify member was removed
        assertTrue(viewModel.getAssignedMembers().isEmpty());
    }
    
    @Test
    public void testAddRemoveComponents() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        
        // Add a component
        viewModel.addComponent(testComponent);
        
        // Verify component was added
        assertEquals(1, viewModel.getRequiredComponents().size());
        assertTrue(viewModel.getRequiredComponents().contains(testComponent));
        
        // Remove the component
        viewModel.removeComponent(testComponent);
        
        // Verify component was removed
        assertTrue(viewModel.getRequiredComponents().isEmpty());
    }
    
    @Test
    public void testAddRemoveDependencies() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        
        Task dependency = new Task("Dependency", testProject, testSubsystem);
        dependency.setId(2L);
        
        // Add a dependency
        viewModel.addDependency(dependency);
        
        // Verify dependency was added
        assertEquals(1, viewModel.getPreDependencies().size());
        assertTrue(viewModel.getPreDependencies().contains(dependency));
        
        // Remove the dependency
        viewModel.removeDependency(dependency);
        
        // Verify dependency was removed
        assertTrue(viewModel.getPreDependencies().isEmpty());
    }
}