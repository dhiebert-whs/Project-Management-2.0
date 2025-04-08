package org.frcpm.viewmodels;

import org.frcpm.binding.Command;
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
    
    @Mock
    private Project mockProject;
    
    @Mock
    private Subsystem mockSubsystem;
    
    @Mock
    private TeamMember mockTeamMember;
    
    @Mock
    private Component mockComponent;
    
    @Mock
    private Task mockTask;
    
    @Mock
    private Task mockDependencyTask;
    
    private TaskViewModel viewModel;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize view model with mock services
        viewModel = new TaskViewModel(taskService, teamMemberService, componentService);
        
        // Set up mock task
        when(mockTask.getTitle()).thenReturn("Test Task");
        when(mockTask.getDescription()).thenReturn("Test Description");
        when(mockTask.getProject()).thenReturn(mockProject);
        when(mockTask.getSubsystem()).thenReturn(mockSubsystem);
        when(mockTask.getEstimatedDuration()).thenReturn(Duration.ofHours(2));
        when(mockTask.getActualDuration()).thenReturn(Duration.ofHours(1));
        when(mockTask.getPriority()).thenReturn(Task.Priority.HIGH);
        when(mockTask.getProgress()).thenReturn(50);
        when(mockTask.getStartDate()).thenReturn(LocalDate.now());
        when(mockTask.getEndDate()).thenReturn(LocalDate.now().plusDays(3));
        when(mockTask.isCompleted()).thenReturn(false);
        when(mockTask.getAssignedTo()).thenReturn(new HashSet<>());
        when(mockTask.getPreDependencies()).thenReturn(new HashSet<>());
        when(mockTask.getRequiredComponents()).thenReturn(new HashSet<>());
        when(mockTask.getId()).thenReturn(1L);
        
        // Set up mocks for service methods
        when(taskService.createTask(anyString(), any(Project.class), any(Subsystem.class), 
                anyDouble(), any(Task.Priority.class), any(LocalDate.class), any()))
                .thenReturn(mockTask);
        when(taskService.save(any(Task.class))).thenReturn(mockTask);
        when(taskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean()))
                .thenReturn(mockTask);
    }
    
    @Test
    public void testInitNewTask() {
        // Act
        viewModel.initNewTask(mockProject, mockSubsystem);
        
        // Assert
        assertTrue(viewModel.isNewTask());
        assertEquals(mockProject, viewModel.getProject());
        assertEquals(mockSubsystem, viewModel.getSubsystem());
        assertEquals("", viewModel.getTitle());
        assertEquals("", viewModel.getDescription());
        assertEquals(1.0, viewModel.getEstimatedHours());
        assertEquals(0.0, viewModel.getActualHours());
        assertEquals(Task.Priority.MEDIUM, viewModel.getPriority());
        assertEquals(0, viewModel.getProgress());
        assertEquals(LocalDate.now(), viewModel.getStartDate());
        assertNull(viewModel.getEndDate());
        assertFalse(viewModel.isCompleted());
        assertTrue(viewModel.getAssignedMembers().isEmpty());
        assertTrue(viewModel.getPreDependencies().isEmpty());
        assertTrue(viewModel.getRequiredComponents().isEmpty());
        assertFalse(viewModel.isDirty());
        
        // Verify validation state - task title is empty, so should be invalid
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("title"));
    }
    
    @Test
    public void testInitExistingTask() {
        // Set up mock task with assigned team member
        Set<TeamMember> assignedMembers = new HashSet<>();
        assignedMembers.add(mockTeamMember);
        when(mockTask.getAssignedTo()).thenReturn(assignedMembers);
        
        // Act
        viewModel.initExistingTask(mockTask);
        
        // Assert
        assertFalse(viewModel.isNewTask());
        assertEquals(mockTask, viewModel.getTask());
        assertEquals(mockProject, viewModel.getProject());
        assertEquals(mockSubsystem, viewModel.getSubsystem());
        assertEquals("Test Task", viewModel.getTitle());
        assertEquals("Test Description", viewModel.getDescription());
        assertEquals(2.0, viewModel.getEstimatedHours());
        assertEquals(1.0, viewModel.getActualHours());
        assertEquals(Task.Priority.HIGH, viewModel.getPriority());
        assertEquals(50, viewModel.getProgress());
        assertEquals(LocalDate.now(), viewModel.getStartDate());
        assertEquals(LocalDate.now().plusDays(3), viewModel.getEndDate());
        assertFalse(viewModel.isCompleted());
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(mockTeamMember));
        assertFalse(viewModel.isDirty());
        
        // Verify validation state
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_ValidTask() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("Valid Title");
        
        // Assert
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_EmptyTitle() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("");
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("title"));
    }
    
    @Test
    public void testValidation_NullProject() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setProject(null);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Project"));
    }
    
    @Test
    public void testValidation_NegativeEstimatedHours() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setEstimatedHours(-1.0);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("hours"));
    }
    
    @Test
    public void testValidation_EndDateBeforeStartDate() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setEndDate(LocalDate.now().minusDays(1));
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("date"));
    }
    
    @Test
    public void testSaveCommand_NewTask() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("New Task");
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(taskService).createTask(
                eq("New Task"),
                eq(mockProject),
                eq(mockSubsystem),
                eq(1.0),
                eq(Task.Priority.MEDIUM),
                any(LocalDate.class),
                isNull());
        
        assertEquals(mockTask, viewModel.getTask());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testSaveCommand_ExistingTask() {
        // Arrange
        viewModel.initExistingTask(mockTask);
        viewModel.setProgress(75);
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(taskService).updateTaskProgress(eq(1L), eq(75), eq(false));
        assertEquals(mockTask, viewModel.getTask());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testSaveCommand_ErrorHandling() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.setTitle("New Task");
        
        // Set up to throw exception
        when(taskService.createTask(anyString(), any(), any(), anyDouble(), any(), any(), any()))
                .thenThrow(new RuntimeException("Test error"));
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to save task"));
        assertTrue(viewModel.getErrorMessage().contains("Test error"));
    }
    
    @Test
    public void testAddMember() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        assertFalse(viewModel.isDirty());
        
        // Act
        viewModel.addMember(mockTeamMember);
        
        // Assert
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(mockTeamMember));
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testRemoveMember() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.addMember(mockTeamMember);
        viewModel.setDirty(false);
        
        // Act
        viewModel.setSelectedMember(mockTeamMember);
        viewModel.getRemoveMemberCommand().execute();
        
        // Assert
        assertTrue(viewModel.getAssignedMembers().isEmpty());
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testAddComponent() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        assertFalse(viewModel.isDirty());
        
        // Act
        viewModel.addComponent(mockComponent);
        
        // Assert
        assertEquals(1, viewModel.getRequiredComponents().size());
        assertTrue(viewModel.getRequiredComponents().contains(mockComponent));
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testRemoveComponent() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.addComponent(mockComponent);
        viewModel.setDirty(false);
        
        // Act
        viewModel.setSelectedComponent(mockComponent);
        viewModel.getRemoveComponentCommand().execute();
        
        // Assert
        assertTrue(viewModel.getRequiredComponents().isEmpty());
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testAddDependency() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        assertFalse(viewModel.isDirty());
        
        // Act
        viewModel.addDependency(mockDependencyTask);
        
        // Assert
        assertEquals(1, viewModel.getPreDependencies().size());
        assertTrue(viewModel.getPreDependencies().contains(mockDependencyTask));
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testRemoveDependency() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        viewModel.addDependency(mockDependencyTask);
        viewModel.setDirty(false);
        
        // Act
        viewModel.setSelectedDependency(mockDependencyTask);
        viewModel.getRemoveDependencyCommand().execute();
        
        // Assert
        assertTrue(viewModel.getPreDependencies().isEmpty());
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testCircularDependencyDetection() {
        // Arrange
        viewModel.initExistingTask(mockTask);
        
        // Create circular dependency situation
        Set<Task> postDependencies = new HashSet<>();
        postDependencies.add(mockTask);
        when(mockDependencyTask.getPostDependencies()).thenReturn(postDependencies);
        
        // Act
        viewModel.addDependency(mockDependencyTask);
        
        // Assert
        assertTrue(viewModel.getPreDependencies().isEmpty());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("circular dependency"));
    }
    
    @Test
    public void testProgressCompletionSync() {
        // Arrange
        viewModel.initNewTask(mockProject, mockSubsystem);
        
        // Act - Set progress to 100%
        viewModel.setProgress(100);
        
        // Assert - Completed should be automatically set to true
        assertTrue(viewModel.isCompleted());
        
        // Act - Set progress to 50% and completed to false
        viewModel.setProgress(50);
        viewModel.setCompleted(false);
        
        // Assert
        assertEquals(50, viewModel.getProgress());
        assertFalse(viewModel.isCompleted());
        
        // Act - Set completed to true
        viewModel.setCompleted(true);
        
        // Assert - Progress should be automatically set to 100%
        assertEquals(100, viewModel.getProgress());
        assertTrue(viewModel.isCompleted());
    }
}