// src/test/java/org/frcpm/viewmodels/TaskViewModelTest.java

package org.frcpm.viewmodels;

import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskViewModelTest {

    @Mock
    private TaskService taskService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private ComponentService componentService;
    
    @Mock
    private Project project;
    
    @Mock
    private Subsystem subsystem;
    
    @Mock
    private TeamMember teamMember;
    
    @Mock
    private Component component;
    
    private TaskViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new TaskViewModel(taskService, teamMemberService, componentService);
        
        // We'll set up mock IDs only in tests that need them
        // This avoids the "unnecessary stubbing" error
    }
    
    @Test
    void testInitNewTask() {
        // Act
        viewModel.initNewTask(project, subsystem);
        
        // Assert
        assertTrue(viewModel.isNewTask());
        assertEquals(project, viewModel.getProject());
        assertEquals(subsystem, viewModel.getSubsystem());
        assertEquals("", viewModel.getTitle());
        assertEquals("", viewModel.getDescription());
        assertEquals(1.0, viewModel.getEstimatedHours());
        assertEquals(0.0, viewModel.getActualHours());
        assertEquals(Task.Priority.MEDIUM, viewModel.getPriority());
        assertEquals(0, viewModel.getProgress());
        assertEquals(LocalDate.now(), viewModel.getStartDate());
        assertNull(viewModel.getEndDate());
        assertFalse(viewModel.isCompleted());
        assertEquals(0, viewModel.getAssignedMembers().size());
        assertEquals(0, viewModel.getPreDependencies().size());
        assertEquals(0, viewModel.getRequiredComponents().size());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    void testInitExistingTask() {
        // Arrange
        Task task = new Task("Test Task", project, subsystem);
        task.setId(1L);
        task.setDescription("Test description");
        task.setEstimatedDuration(Duration.ofHours(2));
        task.setActualDuration(Duration.ofHours(1));
        task.setPriority(Task.Priority.HIGH);
        task.setProgress(50);
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        task.setStartDate(startDate);
        task.setEndDate(endDate);
        task.setCompleted(false);
        
        Set<TeamMember> members = new HashSet<>();
        members.add(teamMember);
        task.setAssignedTo(members);
        
        // Act
        viewModel.initExistingTask(task);
        
        // Assert
        assertFalse(viewModel.isNewTask());
        assertEquals(task, viewModel.getTask());
        assertEquals("Test Task", viewModel.getTitle());
        assertEquals("Test description", viewModel.getDescription());
        assertEquals(2.0, viewModel.getEstimatedHours());
        assertEquals(1.0, viewModel.getActualHours());
        assertEquals(Task.Priority.HIGH, viewModel.getPriority());
        assertEquals(50, viewModel.getProgress());
        assertEquals(startDate, viewModel.getStartDate());
        assertEquals(endDate, viewModel.getEndDate());
        assertFalse(viewModel.isCompleted());
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    void testValidation_ValidTask() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setEstimatedHours(2.0);
        viewModel.setStartDate(LocalDate.now());
        
        // Assert
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    void testValidation_EmptyTitle() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("");
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("title"));
    }
    
    @Test
    void testValidation_NullProject() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setProject(null);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Project"));
    }
    
    @Test
    void testValidation_NullSubsystem() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setSubsystem(null);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Subsystem"));
    }
    
    @Test
    void testValidation_ZeroEstimatedHours() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setEstimatedHours(0);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("hours"));
    }
    
    @Test
    void testValidation_NullStartDate() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setStartDate(null);
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("date"));
    }
    
    @Test
    void testValidation_InvalidDates() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("Valid Title");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setEndDate(LocalDate.now().minusDays(1)); // End date before start date
        
        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("date"));
    }
    
    @Test
    void testSaveCommand_NewTask() {
        // Arrange
        when(project.getId()).thenReturn(1L); // Only stub where needed
        when(subsystem.getId()).thenReturn(1L); // Only stub where needed
        
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("New Task");
        viewModel.setDescription("Description");
        viewModel.setEstimatedHours(2.0);
        viewModel.setStartDate(LocalDate.now());
        
        Task savedTask = new Task("New Task", project, subsystem);
        savedTask.setId(1L);
        
        when(taskService.createTask(
            eq("New Task"), 
            eq(project), 
            eq(subsystem), 
            eq(2.0), 
            eq(Task.Priority.MEDIUM),
            any(LocalDate.class), 
            isNull())
        ).thenReturn(savedTask);
        
        when(taskService.save(any(Task.class))).thenReturn(savedTask);
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(taskService).createTask(
            eq("New Task"), 
            eq(project), 
            eq(subsystem), 
            eq(2.0), 
            eq(Task.Priority.MEDIUM),
            any(LocalDate.class), 
            isNull()
        );
        
        assertEquals(savedTask, viewModel.getTask());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    void testSaveCommand_ExistingTask() {
        // Arrange
        Task existingTask = new Task("Existing Task", project, subsystem);
        existingTask.setId(1L);
        
        viewModel.initExistingTask(existingTask);
        viewModel.setTitle("Updated Task");
        viewModel.setProgress(75);
        
        Task updatedTask = new Task("Updated Task", project, subsystem);
        updatedTask.setId(1L);
        updatedTask.setProgress(75);
        
        when(taskService.updateTaskProgress(eq(1L), eq(75), eq(false))).thenReturn(updatedTask);
        when(taskService.save(any(Task.class))).thenReturn(updatedTask);
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(taskService).updateTaskProgress(eq(1L), eq(75), eq(false));
        
        assertEquals(updatedTask, viewModel.getTask());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    void testAddMember() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        
        // Act
        viewModel.addMember(teamMember);
        
        // Assert
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(teamMember));
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    void testRemoveMember() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        viewModel.addMember(teamMember);
        viewModel.setDirty(false);
        
        // Act
        viewModel.removeMember(teamMember);
        
        // Assert
        assertEquals(0, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    void testAddDependency() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        Task dependency = new Task("Dependency", project, subsystem);
        dependency.setId(2L);
        
        // Act
        viewModel.addDependency(dependency);
        
        // Assert
        assertEquals(1, viewModel.getPreDependencies().size());
        assertTrue(viewModel.getPreDependencies().contains(dependency));
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    void testAddDependency_CircularDependency() {
        // Arrange
        Task existingTask = new Task("Existing Task", project, subsystem);
        existingTask.setId(1L);
        
        Task dependency = new Task("Dependency", project, subsystem);
        dependency.setId(2L);
        
        // Set up circular dependency
        Set<Task> postDependencies = new HashSet<>();
        postDependencies.add(existingTask);
        dependency.setPostDependencies(postDependencies);
        
        viewModel.initExistingTask(existingTask);
        
        // Act
        viewModel.addDependency(dependency);
        
        // Assert
        assertEquals(0, viewModel.getPreDependencies().size());
        assertTrue(viewModel.getErrorMessage().contains("circular dependency"));
    }
    
    @Test
    void testProgressAndCompletedSync() {
        // Arrange
        viewModel.initNewTask(project, subsystem);
        
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
    
    @Test
    void testExceptionHandlingInSave() {
        // Arrange
        when(project.getId()).thenReturn(1L); // Only stub where needed
        when(subsystem.getId()).thenReturn(1L); // Only stub where needed
        
        viewModel.initNewTask(project, subsystem);
        viewModel.setTitle("New Task");
        
        // Set up to throw exception on save
        when(taskService.createTask(
            any(), any(), any(), anyDouble(), any(), any(), any())
        ).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to save task"));
        assertTrue(viewModel.getErrorMessage().contains("Test exception"));
        assertFalse(viewModel.isValid());
    }
}