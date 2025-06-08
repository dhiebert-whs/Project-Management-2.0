// src/test/java/org/frcpm/services/impl/TaskServiceTest.java

package org.frcpm.services.impl;

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
import static org.mockito.Mockito.*;

/**
 * Test class for TaskService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ComponentRepository componentRepository;
    
    private TaskServiceImpl taskService;
    
    private Task testTask;
    private Project testProject;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // Initialize dates and objects first to avoid NullPointerException
        now = LocalDate.now();
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testMember = createTestMember();
        testTask = createTestTask();
        
        // Configure mock repository responses
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findAll()).thenReturn(List.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure project repository
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // Create service with injected mocks
        taskService = new TaskServiceImpl(
            taskRepository,
            projectRepository,
            componentRepository
        );
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
        Task task = new Task("UNIQUE_TEST_TASK_NAME", testProject, testSubsystem);
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
        // Reset mocks to ensure clean test state
        reset(taskRepository);
        
        // Setup - create a special task for this test to isolate the issue
        Task uniqueTask = new Task("UNIQUE_TEST_TASK_NAME", testProject, testSubsystem);
        uniqueTask.setId(1L);
        
        // Configure fresh mock behavior for this test
        when(taskRepository.findById(1L)).thenReturn(Optional.of(uniqueTask));
        
        // Execute
        Task result = taskService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Task ID should match");
        assertEquals("UNIQUE_TEST_TASK_NAME", result.getTitle(), "Task title should match exactly");
        
        // Verify repository was called exactly once with the correct ID
        verify(taskRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Task> results = taskService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(taskRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Task newTask = new Task("New Task", testProject, testSubsystem);
        
        // Execute
        Task result = taskService.save(newTask);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        
        // Verify repository was called
        verify(taskRepository).save(newTask);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(taskRepository).delete(any(Task.class));
        
        // Execute
        taskService.delete(testTask);
        
        // Verify repository was called
        verify(taskRepository).delete(testTask);
    }
    
    @Test
    void testDeleteById() {
        // Setup - Use doNothing() for void methods instead of when().thenReturn()
        doNothing().when(taskRepository).deleteById(anyLong());
        
        // Execute - deleteById returns void, so don't capture return value
        taskService.deleteById(1L);
        
        // Verify repository was called
        verify(taskRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(taskRepository.count()).thenReturn(5L);
        
        // Execute
        long result = taskService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
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
        
        // Verify repository was called
        verify(taskRepository).findByProject(testProject);
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
        
        // Verify repository was called
        verify(taskRepository).findBySubsystem(testSubsystem);
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
        
        // Verify repository was called
        verify(taskRepository).findByAssignedMember(testMember);
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
        
        // Verify repository was called
        verify(taskRepository).findByCompleted(false);
    }
    
    @Test
    void testCreateTask() {
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
        
        // Verify repository was called
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testUpdateTaskProgress() {
        // Execute
        Task result = taskService.updateTaskProgress(1L, 50, false);
        
        // Verify
        assertNotNull(result);
        assertEquals(50, result.getProgress());
        assertFalse(result.isCompleted());
        
        // Verify repository was called
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testUpdateTaskProgress_Completed() {
        // Execute - set completed to true
        Task result = taskService.updateTaskProgress(1L, 50, true);
        
        // Verify - progress should be 100% when completed is true
        assertNotNull(result);
        assertEquals(100, result.getProgress());
        assertTrue(result.isCompleted());
        
        // Verify repository was called
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testUpdateTaskProgress_InvalidTaskId() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = taskService.updateTaskProgress(999L, 50, false);
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository was called
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testGetTasksDueSoon() {
        // Setup
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> result = taskService.getTasksDueSoon(1L, 10);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
        
        // Verify repository calls
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
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testAssignMembers() {
        // Setup
        Set<TeamMember> members = Set.of(testMember);
        
        // Execute
        Task result = taskService.assignMembers(1L, members);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testAssignMembers_InvalidTaskId() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        Set<TeamMember> members = Set.of(testMember);
        
        // Execute
        Task result = taskService.assignMembers(999L, members);
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testAddDependency() {
        // Setup
        Task dependencyTask = new Task("Dependency Task", testProject, testSubsystem);
        dependencyTask.setId(2L);
        
        when(taskRepository.findById(2L)).thenReturn(Optional.of(dependencyTask));
        
        // Execute
        boolean result = taskService.addDependency(1L, 2L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(taskRepository).findById(2L);
        verify(taskRepository, times(2)).save(any(Task.class));
    }
    
    @Test
    void testAddDependency_SameTask() {
        // Execute - try to add task as dependency to itself
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addDependency(1L, 1L);
        });
        
        // Verify exception message
        assertEquals("A task cannot depend on itself", exception.getMessage());
        
        // Verify no repository calls
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testUpdateRequiredComponents() {
        // Setup
        org.frcpm.models.Component component = new org.frcpm.models.Component();
        component.setId(1L);
        component.setName("Test Component");
        
        when(componentRepository.findById(1L)).thenReturn(Optional.of(component));
        
        // Execute
        Task result = taskService.updateRequiredComponents(1L, Set.of(1L));
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(componentRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void testUpdateRequiredComponents_InvalidTaskId() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = taskService.updateRequiredComponents(999L, Set.of(1L));
        
        // Verify - should return null if task not found
        assertNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(999L);
        verify(componentRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }
}