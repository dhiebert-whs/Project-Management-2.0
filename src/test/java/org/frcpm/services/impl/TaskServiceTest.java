// src/test/java/org/frcpm/services/impl/TaskServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.ComponentRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.frcpm.events.WebSocketEventPublisher;

/**
 * Test class for TaskService implementation using Spring Boot testing patterns.
 * ✅ PROVEN PATTERN APPLIED: Following AttendanceServiceTest template for 100% success rate.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private WebSocketEventPublisher webSocketEventPublisher;
    
    private TaskServiceImpl taskService; // ✅ Use implementation class, not interface
    
    private Task testTask;
    private Project testProject;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - NO mock stubbing here
        now = LocalDate.now();
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testMember = createTestMember();
        testTask = createTestTask();
        
        // ✅ FIXED: Create service with WebSocketEventPublisher mock
        taskService = new TaskServiceImpl(taskRepository, projectRepository, componentRepository, webSocketEventPublisher);
        
        // NO mock stubbing in setUp() - move to individual test methods
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now, now.plusMonths(1), now.plusMonths(2));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        return subsystem;
    }
    
    /**
     * Creates a test member for use in tests.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember("user", "Test", "User", "test@example.com");
        member.setId(1L);
        return member;
    }
    
    /**
     * Creates a test task for use in tests.
     */
    private Task createTestTask() {
        Task task = new Task("Test Task", testProject, testSubsystem);
        task.setId(1L);
        task.setDescription("Test Description");
        task.setPriority(Task.Priority.MEDIUM);
        task.setEstimatedDuration(Duration.ofHours(2));
        task.setStartDate(now);
        task.setEndDate(now.plusDays(7));
        task.setProgress(0);
        task.setCompleted(false);
        return task;
    }
    
    @Test
    void testFindById() {
        // Setup - Only stub what THIS test needs
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        
        // Execute
        Task result = taskService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Task ID should match");
        assertEquals("Test Task", result.getTitle(), "Task title should match");
        
        // Verify repository interaction
        verify(taskRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = taskService.findById(999L);
        
        // Verify
        assertNull(result, "Result should be null for non-existent ID");
        
        // Verify repository interaction
        verify(taskRepository).findById(999L);
    }
    
    @Test
    void testFindById_NullParameter() {
        // Execute
        Task result = taskService.findById(null);
        
        // Verify
        assertNull(result, "Result should be null for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(taskRepository, never()).findById(any());
    }
    
    @Test
    void testFindAll() {
        // Setup
        when(taskRepository.findAll()).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask, results.get(0));
        
        // Verify repository interaction
        verify(taskRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Task newTask = new Task("New Task", testProject, testSubsystem);
        when(taskRepository.save(newTask)).thenReturn(newTask);
        
        // Execute
        Task result = taskService.save(newTask);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        
        // Verify repository interaction
        verify(taskRepository).save(newTask);
    }
    
    @Test
    void testSave_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.save(null);
        });
        
        // Verify exception message
        assertEquals("Task cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(taskRepository).delete(testTask);
        
        // Execute
        taskService.delete(testTask);
        
        // Verify repository interaction
        verify(taskRepository).delete(testTask);
    }
    
    @Test
    void testDelete_NullParameter() {
        // Execute (should not throw exception)
        taskService.delete(null);
        
        // Verify repository was NOT called for null parameter
        verify(taskRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteById() {
        // Setup - Configure existsById and deleteById behavior
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);
        
        // Execute
        boolean result = taskService.deleteById(1L);
        
        // Verify
        assertTrue(result, "Delete should return true for existing entity");
        
        // Verify repository interactions in correct order
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotFound() {
        // Setup
        when(taskRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = taskService.deleteById(999L);
        
        // Verify
        assertFalse(result, "Delete should return false for non-existent entity");
        
        // Verify repository interactions
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteById_NullParameter() {
        // Execute
        boolean result = taskService.deleteById(null);
        
        // Verify
        assertFalse(result, "Delete should return false for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(taskRepository, never()).existsById(any());
        verify(taskRepository, never()).deleteById(any());
    }
    
    @Test
    void testCount() {
        // Setup
        when(taskRepository.count()).thenReturn(5L);
        
        // Execute
        long result = taskService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository interaction
        verify(taskRepository).count();
    }
    
    @Test
    void testFindByProject() {
        // Setup
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findByProject(testProject);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask, results.get(0));
        
        // Verify repository interaction
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testFindByProject_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findByProject(null);
        });
        
        // Verify exception message
        assertEquals("Project cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testFindBySubsystem() {
        // Setup
        when(taskRepository.findBySubsystem(testSubsystem)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findBySubsystem(testSubsystem);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask, results.get(0));
        
        // Verify repository interaction
        verify(taskRepository).findBySubsystem(testSubsystem);
    }
    
    @Test
    void testFindBySubsystem_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findBySubsystem(null);
        });
        
        // Verify exception message
        assertEquals("Subsystem cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findBySubsystem(any());
    }
    
    @Test
    void testFindByAssignedMember() {
        // Setup
        when(taskRepository.findByAssignedMember(testMember)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findByAssignedMember(testMember);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask, results.get(0));
        
        // Verify repository interaction
        verify(taskRepository).findByAssignedMember(testMember);
    }
    
    @Test
    void testFindByAssignedMember_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findByAssignedMember(null);
        });
        
        // Verify exception message
        assertEquals("Member cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findByAssignedMember(any());
    }
    
    @Test
    void testFindByCompleted() {
        // Setup
        when(taskRepository.findByCompleted(false)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findByCompleted(false);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask, results.get(0));
        
        // Verify repository interaction
        verify(taskRepository).findByCompleted(false);
    }
    
    @Test
    void testCreateTask() {
        // Setup
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Task result = taskService.createTask(
            "New Task",
            testProject,
            testSubsystem,
            2.5,
            Task.Priority.HIGH,
            now,
            now.plusDays(5)
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals(testProject, result.getProject());
        assertEquals(testSubsystem, result.getSubsystem());
        assertEquals(Task.Priority.HIGH, result.getPriority());
        assertEquals(now, result.getStartDate());
        assertEquals(now.plusDays(5), result.getEndDate());
        
        // Verify repository interaction
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testCreateTask_NullTitle() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null, testProject, testSubsystem, 2.5, Task.Priority.HIGH, now, now.plusDays(5));
        });
        
        // Verify exception message
        assertEquals("Task title cannot be empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testCreateTask_EmptyTitle() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("  ", testProject, testSubsystem, 2.5, Task.Priority.HIGH, now, now.plusDays(5));
        });
        
        // Verify exception message
        assertEquals("Task title cannot be empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testCreateTask_NullProject() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("New Task", null, testSubsystem, 2.5, Task.Priority.HIGH, now, now.plusDays(5));
        });
        
        // Verify exception message
        assertEquals("Project cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testCreateTask_NullSubsystem() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("New Task", testProject, null, 2.5, Task.Priority.HIGH, now, now.plusDays(5));
        });
        
        // Verify exception message
        assertEquals("Subsystem cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testCreateTask_InvalidEstimatedHours() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("New Task", testProject, testSubsystem, -1.0, Task.Priority.HIGH, now, now.plusDays(5));
        });
        
        // Verify exception message
        assertEquals("Estimated hours must be positive", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testCreateTask_EndDateBeforeStartDate() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("New Task", testProject, testSubsystem, 2.5, Task.Priority.HIGH, now.plusDays(5), now);
        });
        
        // Verify exception message
        assertEquals("End date cannot be before start date", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testUpdateTaskProgress() {
        // Setup
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Task result = taskService.updateTaskProgress(1L, 50, false);
        
        // Verify
        assertNotNull(result);
        assertEquals(50, result.getProgress());
        assertFalse(result.isCompleted());
        
        // Verify repository interactions
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }
    
    @Test
    void testUpdateTaskProgress_Completed() {
        // Setup
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute - set completed to true
        Task result = taskService.updateTaskProgress(1L, 50, true);
        
        // Verify - progress should be 100% when completed is true
        assertNotNull(result);
        assertEquals(100, result.getProgress());
        assertTrue(result.isCompleted());
        
        // Verify repository interactions
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }
    
    @Test
    void testUpdateTaskProgress_TaskNotFound() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = taskService.updateTaskProgress(999L, 50, false);
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository interactions
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testUpdateTaskProgress_NullTaskId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTaskProgress(null, 50, false);
        });
        
        // Verify exception message
        assertEquals("Task ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testGetTasksDueSoon() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> result = taskService.getTasksDueSoon(1L, 10);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
        
        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetTasksDueSoon_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Task> result = taskService.getTasksDueSoon(999L, 10);
        
        // Verify - should return empty list if project not found
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetTasksDueSoon_NullProjectId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksDueSoon(null, 10);
        });
        
        // Verify exception message
        assertEquals("Project ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetTasksDueSoon_InvalidDays() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksDueSoon(1L, -1);
        });
        
        // Verify exception message
        assertEquals("Days must be positive", exception.getMessage());
        
        // Verify repository was NOT called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testAssignMembers() {
        // Setup
        Set<TeamMember> members = Set.of(testMember);
        
        // ✅ FIXED: Mock for multiple findById calls
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Task result = taskService.assignMembers(1L, members);
        
        // Verify
        assertNotNull(result);
        
        // ✅ FIXED: Update expected call counts
        verify(taskRepository, times(2)).findById(1L); // Called in assignMembers and in save()
        verify(taskRepository).save(testTask);
    }
    
    @Test
    void testAssignMembers_TaskNotFound() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        Set<TeamMember> members = Set.of(testMember);
        
        // Execute
        Task result = taskService.assignMembers(999L, members);
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository interactions
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testAssignMembers_NullTaskId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.assignMembers(null, Set.of(testMember));
        });
        
        // Verify exception message
        assertEquals("Task ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testAddDependency() {
        // Setup
        Task dependencyTask = new Task("Dependency Task", testProject, testSubsystem);
        dependencyTask.setId(2L);
        
        // ✅ FIXED: Mock for multiple findById calls, removed unnecessary save() stub
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(dependencyTask));
        
        // Execute
        boolean result = taskService.addDependency(1L, 2L);
        
        // Verify
        assertTrue(result);
        
        // Updated for current implementation - dependencies now managed through TaskDependencyService
        verify(taskRepository, times(1)).findById(1L); // Called once in addDependency
        verify(taskRepository, times(1)).findById(2L); // Called once in addDependency
        verify(taskRepository, never()).save(any(Task.class)); // No saves in current implementation
    }
    
    @Test
    void testAddDependency_SameTask() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addDependency(1L, 1L);
        });
        
        // Verify exception message
        assertEquals("A task cannot depend on itself", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testAddDependency_NullParameters() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addDependency(null, 2L);
        });
        
        // Verify exception message
        assertEquals("Task IDs cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testRemoveDependency() {
        // Setup
        Task dependencyTask = new Task("Dependency Task", testProject, testSubsystem);
        dependencyTask.setId(2L);
        
        // ✅ FIXED: Mock for multiple findById calls, removed unnecessary save() stub
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(dependencyTask));
        
        // Execute
        boolean result = taskService.removeDependency(1L, 2L);
        
        // Verify
        assertTrue(result);
        
        // Updated for current implementation - dependencies now managed through TaskDependencyService
        verify(taskRepository, times(1)).findById(1L); // Called once in removeDependency
        verify(taskRepository, times(1)).findById(2L); // Called once in removeDependency
        verify(taskRepository, never()).save(any(Task.class)); // No saves in current implementation
    }
    
    @Test
    void testUpdateRequiredComponents() {
        // Setup
        org.frcpm.models.Component component = new org.frcpm.models.Component();
        component.setId(1L);
        component.setName("Test Component");
        
        // ✅ FIXED: Mock findById to be called multiple times (once in updateRequiredComponents, once in save via WebSocket)
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Task result = taskService.updateRequiredComponents(1L, Set.of(1L));
        
        // Verify
        assertNotNull(result);
        
        // ✅ FIXED: Verify repository interactions with correct expected call counts
        verify(taskRepository, times(2)).findById(1L); // Called twice: once in method, once in save()
        verify(componentRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }
    
    @Test
    void testUpdateRequiredComponents_TaskNotFound() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = taskService.updateRequiredComponents(999L, Set.of(1L));
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository interactions
        verify(taskRepository).findById(999L);
        verify(componentRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testUpdateRequiredComponents_NullTaskId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateRequiredComponents(null, Set.of(1L));
        });
        
        // Verify exception message
        assertEquals("Task ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(taskRepository, never()).findById(any());
        verify(componentRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testSave_NewTask_PublishesCreationEvent() {
        // Setup
        Task newTask = new Task("New Task", testProject, testSubsystem);
        when(taskRepository.save(newTask)).thenReturn(newTask);
        
        // Execute
        Task result = taskService.save(newTask);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        
        // Verify repository interaction
        verify(taskRepository).save(newTask);
        
        // Verify WebSocket event was published (new task)
        verify(webSocketEventPublisher).publishTaskCreation(eq(newTask), any());
    }

    @Test
    void testUpdateTaskProgress_PublishesProgressEvent() {
        // Setup
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Task result = taskService.updateTaskProgress(1L, 75, false);
        
        // Verify
        assertNotNull(result);
        assertEquals(75, result.getProgress());
        
        // Verify repository interactions
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(testTask);
        
        // Verify WebSocket progress event was published
        verify(webSocketEventPublisher).publishTaskProgressUpdate(eq(testTask), eq(0), any());
    }

    @Test
    void testUpdateTaskProgress_Completion_PublishesCompletionEvent() {
        // Setup - Reset the test task to ensure clean state
        testTask.setCompleted(false);  // CRITICAL: Must start as incomplete
        testTask.setProgress(25);      // Set to a specific starting progress
        
        // Mock repository to return our test task
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Verify initial state
        assertFalse(testTask.isCompleted(), "Task should start incomplete");
        assertEquals(25, testTask.getProgress(), "Task should start at 25% progress");
        
        // Execute - complete the task (this should trigger completion event)
        Task result = taskService.updateTaskProgress(1L, 100, true);
        
        // Verify final state
        assertNotNull(result);
        assertEquals(100, result.getProgress(), "Task progress should be 100%");
        assertTrue(result.isCompleted(), "Task should be completed");
        
        // Verify repository interactions
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(testTask);
        
        // Verify WebSocket events - BOTH progress update AND completion should be called
        verify(webSocketEventPublisher).publishTaskProgressUpdate(eq(testTask), eq(25), any());
        verify(webSocketEventPublisher).publishTaskCompletion(eq(testTask), any());
    }
}