// src/test/java/org/frcpm/repositories/specific/TaskRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.ProjectRepositoryImpl;
import org.frcpm.repositories.impl.SubsystemRepositoryImpl;
import org.frcpm.repositories.impl.TaskRepositoryImpl;
import org.frcpm.repositories.impl.TeamMemberRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TaskRepository implementation.
 */
public class TaskRepositoryTest extends BaseRepositoryTest {
    
    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private SubsystemRepository subsystemRepository;
    private TeamMemberRepository teamMemberRepository;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        taskRepository = new TaskRepositoryImpl();
        projectRepository = new ProjectRepositoryImpl();
        subsystemRepository = new SubsystemRepositoryImpl();
        teamMemberRepository = new TeamMemberRepositoryImpl();
        
        // Create test data
        createTestEntities();
    }
    
    @Override
    protected void setupTestData() {
        // No data setup by default - tests will create their own data
    }
    
    private void createTestEntities() {
        // Create and save a test project
        testProject = new Project("Test Project", 
                                LocalDate.now(), 
                                LocalDate.now().plusMonths(1), 
                                LocalDate.now().plusMonths(2));
        testProject = projectRepository.save(testProject);
        
        // Create and save a test subsystem
        testSubsystem = new Subsystem("Test Subsystem");
        beginTransaction();
        em.persist(testSubsystem);
        commitTransaction();
        
        // Create and save a test team member
        testMember = new TeamMember("testuser", "Test", "User", "test@example.com");
        beginTransaction();
        em.persist(testMember);
        commitTransaction();
    }
    
    @Test
    public void testSaveAndFindById() {
        // Create a test task
        Task task = new Task("Test Task", testProject, testSubsystem);
        task.setDescription("Test Description");
        task.setPriority(Task.Priority.HIGH);
        task.setEstimatedDuration(Duration.ofHours(2));
        
        // Save the task
        Task savedTask = taskRepository.save(task);
        
        // Verify that ID was generated
        assertNotNull(savedTask.getId());
        
        // Find the task by ID
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());
        
        // Verify that the task was found
        assertTrue(foundTask.isPresent());
        assertEquals(savedTask.getId(), foundTask.get().getId());
        assertEquals("Test Task", foundTask.get().getTitle());
        assertEquals("Test Description", foundTask.get().getDescription());
        assertEquals(Task.Priority.HIGH, foundTask.get().getPriority());
        assertEquals(Duration.ofHours(2), foundTask.get().getEstimatedDuration());
    }
    
    @Test
    public void testFindAll() {
        // Create test tasks
        Task task1 = new Task("Task 1", testProject, testSubsystem);
        Task task2 = new Task("Task 2", testProject, testSubsystem);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Find all tasks
        List<Task> tasks = taskRepository.findAll();
        
        // Verify all tasks were found
        assertTrue(tasks.size() >= 2);
    }
    
    @Test
    public void testUpdate() {
        // Create a test task
        Task task = new Task("Original Title", testProject, testSubsystem);
        task.setDescription("Original Description");
        
        // Save the task
        Task savedTask = taskRepository.save(task);
        
        // Update the task
        savedTask.setTitle("Updated Title");
        savedTask.setDescription("Updated Description");
        savedTask.setPriority(Task.Priority.CRITICAL);
        
        // Save the updated task
        Task updatedTask = taskRepository.save(savedTask);
        
        // Find the task by ID
        Optional<Task> foundTask = taskRepository.findById(updatedTask.getId());
        
        // Verify that the task was updated
        assertTrue(foundTask.isPresent());
        assertEquals("Updated Title", foundTask.get().getTitle());
        assertEquals("Updated Description", foundTask.get().getDescription());
        assertEquals(Task.Priority.CRITICAL, foundTask.get().getPriority());
    }
    
    @Test
    public void testDelete() {
        // Create a test task
        Task task = new Task("Task to Delete", testProject, testSubsystem);
        
        // Save the task
        Task savedTask = taskRepository.save(task);
        
        // Verify that the task exists
        Optional<Task> foundBeforeDelete = taskRepository.findById(savedTask.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the task
        taskRepository.delete(savedTask);
        
        // Verify that the task was deleted
        Optional<Task> foundAfterDelete = taskRepository.findById(savedTask.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // Create a test task
        Task task = new Task("Task to Delete by ID", testProject, testSubsystem);
        
        // Save the task
        Task savedTask = taskRepository.save(task);
        
        // Verify that the task exists
        Optional<Task> foundBeforeDelete = taskRepository.findById(savedTask.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the task by ID
        boolean deleted = taskRepository.deleteById(savedTask.getId());
        
        // Verify that deletion was successful
        assertTrue(deleted);
        
        // Verify that the task was deleted
        Optional<Task> foundAfterDelete = taskRepository.findById(savedTask.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testCount() {
        // Get initial count
        long initialCount = taskRepository.count();
        
        // Create test tasks
        Task task1 = new Task("Task 1", testProject, testSubsystem);
        Task task2 = new Task("Task 2", testProject, testSubsystem);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Verify updated count
        assertEquals(initialCount + 2, taskRepository.count());
        
        // Delete a task
        taskRepository.delete(task1);
        
        // Verify updated count after deletion
        assertEquals(initialCount + 1, taskRepository.count());
    }
    
    @Test
    public void testFindByProject() {
        // Create a second project
        Project anotherProject = new Project("Another Project", 
                                           LocalDate.now(), 
                                           LocalDate.now().plusMonths(1), 
                                           LocalDate.now().plusMonths(2));
        anotherProject = projectRepository.save(anotherProject);
        
        // Create tasks for different projects
        Task task1 = new Task("Task for Project 1", testProject, testSubsystem);
        Task task2 = new Task("Another Task for Project 1", testProject, testSubsystem);
        Task task3 = new Task("Task for Project 2", anotherProject, testSubsystem);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        // Find tasks by project
        List<Task> projectTasks = taskRepository.findByProject(testProject);
        
        // Verify that only tasks for the specified project were found
        assertTrue(projectTasks.size() >= 2);
        assertTrue(projectTasks.stream().allMatch(t -> t.getProject().getId().equals(testProject.getId())));
    }
    
    @Test
    public void testFindBySubsystem() {
        // Create a second subsystem
        Subsystem anotherSubsystem = new Subsystem("Another Subsystem");
        beginTransaction();
        em.persist(anotherSubsystem);
        commitTransaction();
        
        // Create tasks for different subsystems
        Task task1 = new Task("Task for Subsystem 1", testProject, testSubsystem);
        Task task2 = new Task("Another Task for Subsystem 1", testProject, testSubsystem);
        Task task3 = new Task("Task for Subsystem 2", testProject, anotherSubsystem);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        // Find tasks by subsystem
        List<Task> subsystemTasks = taskRepository.findBySubsystem(testSubsystem);
        
        // Verify that only tasks for the specified subsystem were found
        assertTrue(subsystemTasks.size() >= 2);
        assertTrue(subsystemTasks.stream().allMatch(t -> t.getSubsystem().getId().equals(testSubsystem.getId())));
    }
    
    @Test
    public void testFindByAssignedMember() {
        // Create a second team member
        TeamMember anotherMember = new TeamMember("anotheruser", "Another", "User", "another@example.com");
        beginTransaction();
        em.persist(anotherMember);
        commitTransaction();
        
        // Create tasks
        Task task1 = new Task("Task 1", testProject, testSubsystem);
        Task task2 = new Task("Task 2", testProject, testSubsystem);
        
        // Save tasks
        task1 = taskRepository.save(task1);
        task2 = taskRepository.save(task2);
        
        // Assign tasks to members
        task1.assignMember(testMember);
        task2.assignMember(anotherMember);
        
        // Update tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Find tasks by assigned member
        List<Task> memberTasks = taskRepository.findByAssignedMember(testMember);
        
        // Verify that only tasks assigned to the specified member were found
        assertEquals(1, memberTasks.size());
        assertEquals(task1.getId(), memberTasks.get(0).getId());
    }
    
    @Test
    public void testFindByCompleted() {
        // Create tasks with different completion status
        Task task1 = new Task("Completed Task", testProject, testSubsystem);
        task1.setCompleted(true);
        
        Task task2 = new Task("Incomplete Task", testProject, testSubsystem);
        task2.setCompleted(false);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Find completed tasks
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        
        // Verify that only completed tasks were found
        assertTrue(completedTasks.size() >= 1);
        assertTrue(completedTasks.stream().allMatch(Task::isCompleted));
        
        // Find incomplete tasks
        List<Task> incompleteTasks = taskRepository.findByCompleted(false);
        
        // Verify that only incomplete tasks were found
        assertTrue(incompleteTasks.size() >= 1);
        assertTrue(incompleteTasks.stream().noneMatch(Task::isCompleted));
    }
    
    @Test
    public void testFindByEndDateBefore() {
        LocalDate today = LocalDate.now();
        
        // Create tasks with different end dates
        Task task1 = new Task("Early Deadline Task", testProject, testSubsystem);
        task1.setEndDate(today.plusDays(5));
        
        Task task2 = new Task("Late Deadline Task", testProject, testSubsystem);
        task2.setEndDate(today.plusDays(15));
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Find tasks with end date before a specific date
        LocalDate deadline = today.plusDays(10);
        List<Task> earlyTasks = taskRepository.findByEndDateBefore(deadline);
        
        // Verify that only tasks with end date before the specified date were found
        assertTrue(earlyTasks.size() >= 1);
        assertTrue(earlyTasks.stream().allMatch(t -> t.getEndDate().isBefore(deadline)));
    }
    
    @Test
    public void testFindByPriority() {
        // Create tasks with different priorities
        Task task1 = new Task("High Priority Task", testProject, testSubsystem);
        task1.setPriority(Task.Priority.HIGH);
        
        Task task2 = new Task("Medium Priority Task", testProject, testSubsystem);
        task2.setPriority(Task.Priority.MEDIUM);
        
        Task task3 = new Task("Another High Priority Task", testProject, testSubsystem);
        task3.setPriority(Task.Priority.HIGH);
        
        // Save tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        // Find tasks by priority
        List<Task> highPriorityTasks = taskRepository.findByPriority(Task.Priority.HIGH);
        
        // Verify that only tasks with the specified priority were found
        assertTrue(highPriorityTasks.size() >= 2);
        assertTrue(highPriorityTasks.stream().allMatch(t -> t.getPriority() == Task.Priority.HIGH));
    }
}