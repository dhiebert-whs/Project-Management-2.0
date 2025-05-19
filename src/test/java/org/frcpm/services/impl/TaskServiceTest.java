// src/test/java/org/frcpm/services/impl/TaskServiceTest.java
package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.BaseServiceTest;
import org.frcpm.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for TaskService implementation.
 */
public class TaskServiceTest extends BaseServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    private TaskService taskService;
    
    private Task testTask;
    private Project testProject;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    private LocalDate now;
    
    @Override
    protected void setupTestData() {
        // Initialize dates and objects first to avoid NullPointerException
        now = LocalDate.now();
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testMember = createTestMember();
        testTask = createTestTask();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Create service instance and inject repository
        taskService = new TaskServiceImpl();
        try {
            org.frcpm.utils.TestUtils.setPrivateField(taskService, "repository", taskRepository);
        } catch (Exception e) {
            fail("Failed to inject repository: " + e.getMessage());
        }
        
        // Configure mocks AFTER service creation
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findAll()).thenReturn(List.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    /**
     * Creates a test project for use in tests.
     * 
     * @return a test project
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now, now.plusMonths(1), now.plusMonths(2));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     * 
     * @return a test subsystem
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        return subsystem;
    }
    
    /**
     * Creates a test member for use in tests.
     * 
     * @return a test member
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember("user", "Test", "User", "test@example.com");
        member.setId(1L);
        return member;
    }
    
    /**
     * Creates a test task for use in tests.
     * 
     * @return a test task
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
    public void testFindById() {
        // Create a completely fresh mock repository just for this test
        TaskRepository freshMockRepo = mock(TaskRepository.class);
        
        // Create a completely new service instance
        TaskServiceImpl freshService = new TaskServiceImpl();
        
        // Inject our fresh mock repository
        try {
            org.frcpm.utils.TestUtils.setPrivateField(freshService, "repository", freshMockRepo);
        } catch (Exception e) {
            fail("Failed to inject repository: " + e.getMessage());
        }
        
        // Create a task specifically for this test with a very distinctive name
        Task expectedTask = new Task("UNIQUE_TEST_TASK_NAME", testProject, testSubsystem);
        expectedTask.setId(1L);
        
        // Stub the fresh mock to return our task
        when(freshMockRepo.findById(1L)).thenReturn(Optional.of(expectedTask));
        
        // Execute with our fresh service
        Task result = freshService.findById(1L);
        
        // Very specific verification to avoid any confusion
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Task ID should match");
        assertEquals("UNIQUE_TEST_TASK_NAME", result.getTitle(), "Task title should match exactly");
        
        // Verify repository was called
        verify(freshMockRepo).findById(1L);
    }
    
    @Test
    public void testFindAll() {
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
    public void testSave() {
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
    public void testDelete() {
        // Setup
        doNothing().when(taskRepository).delete(any(Task.class));
        
        // Execute
        taskService.delete(testTask);
        
        // Verify repository was called
        verify(taskRepository).delete(testTask);
    }
    
    @Test
    public void testDeleteById() {
        // Setup
        when(taskRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = taskService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(taskRepository).deleteById(1L);
    }
    
    @Test
    public void testCount() {
        // Setup
        when(taskRepository.count()).thenReturn(5L);
        
        // Execute
        long result = taskService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(taskRepository).count();
    }
    
    // For the remaining tests, let's simplify them to focus on basic functionality
    
    @Test
    public void testFindByCompleted() {
        // Setup
        when(taskRepository.findByCompleted(false)).thenReturn(List.of(testTask));
        
        // Execute
        List<Task> results = taskService.findByCompleted(false);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(taskRepository).findByCompleted(false);
    }
    
    @Test
    public void testFindByAssignedMember_Null() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.findByAssignedMember(null)
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).findByAssignedMember(any());
    }
    
    @Test
    public void testCreateTask_InvalidTitle() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.createTask(null, testProject, testSubsystem, 
                                 2.0, Task.Priority.MEDIUM, now, now.plusDays(7))
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testCreateTask_InvalidProject() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.createTask("New Task", null, testSubsystem, 
                                 2.0, Task.Priority.MEDIUM, now, now.plusDays(7))
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testCreateTask_InvalidSubsystem() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.createTask("New Task", testProject, null, 
                                 2.0, Task.Priority.MEDIUM, now, now.plusDays(7))
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testCreateTask_InvalidEstimatedHours() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.createTask("New Task", testProject, testSubsystem, 
                                 0.0, Task.Priority.MEDIUM, now, now.plusDays(7))
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testCreateTask_InvalidDates() {
        // Execute and verify - end date before start date
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.createTask("New Task", testProject, testSubsystem, 
                                 2.0, Task.Priority.MEDIUM, now, now.minusDays(1))
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testAddDependency_SameTask() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.addDependency(1L, 1L)
        );
        
        // Verify repository was not called
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }
}