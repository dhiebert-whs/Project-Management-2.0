package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TaskRepositoryTest {
    
    private TaskRepository repository;
    private ProjectRepository projectRepository;
    private SubsystemRepository subsystemRepository;
    private TeamMemberRepository teamMemberRepository;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getTaskRepository();
        projectRepository = RepositoryFactory.getProjectRepository();
        subsystemRepository = RepositoryFactory.getSubsystemRepository();
        teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
        
        // Create required test entities
        testProject = new Project(
            "Test Task Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        testProject = projectRepository.save(testProject);
        
        testSubsystem = new Subsystem("Test Task Subsystem");
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        testSubsystem = subsystemRepository.save(testSubsystem);
        
        // Generate unique username for each test run
        String uniqueUsername = "tasktestuser_" + System.currentTimeMillis();
        testMember = new TeamMember(uniqueUsername, "Task", "Test", "tasktest@example.com");
        testMember = teamMemberRepository.save(testMember);
        
        // Add test data
        createTestTasks();
    }
    
    @AfterEach
    public void tearDown() {
        try {
            // Clean up test data
            cleanupTestTasks();
            
            // More robust error handling and sequence of cleanup
            if (testMember != null && testMember.getId() != null) {
                // First, remove member from any tasks
                List<Task> memberTasks = repository.findByAssignedMember(testMember);
                for (Task task : memberTasks) {
                    task.unassignMember(testMember);
                    repository.save(task);
                }
                // Then delete the member
                teamMemberRepository.deleteById(testMember.getId());
            }
            
            if (testSubsystem != null && testSubsystem.getId() != null) {
                subsystemRepository.deleteById(testSubsystem.getId());
            }
            
            if (testProject != null && testProject.getId() != null) {
                projectRepository.deleteById(testProject.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConfig.shutdown();
        }
    }
    
    private void createTestTasks() {
        Task task1 = new Task("Test Task 1", testProject, testSubsystem);
        task1.setEstimatedDuration(Duration.ofHours(5));
        task1.setPriority(Task.Priority.HIGH);
        task1.setStartDate(LocalDate.now());
        task1.setEndDate(LocalDate.now().plusWeeks(1));
        task1.setProgress(25);
        
        Task task2 = new Task("Test Task 2", testProject, testSubsystem);
        task2.setEstimatedDuration(Duration.ofHours(10));
        task2.setPriority(Task.Priority.MEDIUM);
        task2.setStartDate(LocalDate.now().plusDays(3));
        task2.setEndDate(LocalDate.now().plusWeeks(2));
        task2.setProgress(0);
        
        Task task3 = new Task("Test Task 3", testProject, testSubsystem);
        task3.setEstimatedDuration(Duration.ofHours(2));
        task3.setPriority(Task.Priority.LOW);
        task3.setStartDate(LocalDate.now().minusDays(3));
        task3.setEndDate(LocalDate.now().plusDays(4));
        task3.setProgress(100);
        task3.setCompleted(true);
        
        repository.save(task1);
        repository.save(task2);
        repository.save(task3);
        
        // Assign team member to task1
        task1.assignMember(testMember);
        repository.save(task1);
        
        // Set task dependencies
        task2.addPreDependency(task1);
        repository.save(task2);
    }
    
    private void cleanupTestTasks() {
        try {
            List<Task> tasks = repository.findByProject(testProject);
            for (Task task : tasks) {
                // Clear relationships first to avoid constraint violations
                task.getAssignedTo().clear();
                task.getPreDependencies().clear();
                task.getPostDependencies().clear();
                task.getRequiredComponents().clear();
                repository.save(task);
                
                // Now delete the task
                repository.deleteById(task.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testFindAll() {
        List<Task> tasks = repository.findAll();
        assertNotNull(tasks);
        assertTrue(tasks.size() >= 3);
    }
    
    @Test
    public void testFindById() {
        // First, get a task ID from the DB
        List<Task> tasks = repository.findByProject(testProject);
        Task firstTask = tasks.stream()
            .filter(t -> t.getTitle().equals("Test Task 1"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Task> found = repository.findById(firstTask.getId());
        assertTrue(found.isPresent());
        assertEquals(firstTask.getTitle(), found.get().getTitle());
    }
    
    @Test
    public void testFindByProject() {
        List<Task> tasks = repository.findByProject(testProject);
        assertFalse(tasks.isEmpty());
        assertTrue(tasks.stream().allMatch(t -> t.getProject().getId().equals(testProject.getId())));
        assertEquals(3, tasks.size());
    }
    
    @Test
    public void testFindBySubsystem() {
        List<Task> tasks = repository.findBySubsystem(testSubsystem);
        assertFalse(tasks.isEmpty());
        assertTrue(tasks.stream().allMatch(t -> t.getSubsystem().getId().equals(testSubsystem.getId())));
        assertEquals(3, tasks.size());
    }
    
    @Test
    public void testFindByAssignedMember() {
        List<Task> tasks = repository.findByAssignedMember(testMember);
        assertFalse(tasks.isEmpty());
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 1")));
    }
    
    @Test
    public void testFindByCompleted() {
        List<Task> completedTasks = repository.findByCompleted(true);
        assertFalse(completedTasks.isEmpty());
        assertTrue(completedTasks.stream().allMatch(Task::isCompleted));
        assertTrue(completedTasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 3")));
        
        List<Task> incompleteTasks = repository.findByCompleted(false);
        assertFalse(incompleteTasks.isEmpty());
        assertTrue(incompleteTasks.stream().noneMatch(Task::isCompleted));
        assertTrue(incompleteTasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 1")));
    }
    
    @Test
    public void testFindByEndDateBefore() {
        LocalDate cutoffDate = LocalDate.now().plusWeeks(1).plusDays(1);
        List<Task> tasks = repository.findByEndDateBefore(cutoffDate);
        assertFalse(tasks.isEmpty());
        
        for (Task task : tasks) {
            assertTrue(task.getEndDate().isBefore(cutoffDate));
        }
        
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 3")));
    }
    
    @Test
    public void testFindByPriority() {
        List<Task> highPriorityTasks = repository.findByPriority(Task.Priority.HIGH);
        assertFalse(highPriorityTasks.isEmpty());
        assertTrue(highPriorityTasks.stream().allMatch(t -> t.getPriority() == Task.Priority.HIGH));
        assertTrue(highPriorityTasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 1")));
        
        List<Task> mediumPriorityTasks = repository.findByPriority(Task.Priority.MEDIUM);
        assertFalse(mediumPriorityTasks.isEmpty());
        assertTrue(mediumPriorityTasks.stream().allMatch(t -> t.getPriority() == Task.Priority.MEDIUM));
        assertTrue(mediumPriorityTasks.stream().anyMatch(t -> t.getTitle().equals("Test Task 2")));
    }
    
    @Test
    public void testSave() {
        Task newTask = new Task("Test Save Task", testProject, testSubsystem);
        newTask.setEstimatedDuration(Duration.ofHours(3));
        newTask.setPriority(Task.Priority.MEDIUM);
        newTask.setStartDate(LocalDate.now());
        newTask.setEndDate(LocalDate.now().plusWeeks(1));
        
        Task saved = repository.save(newTask);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Task> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Task", found.get().getTitle());
    }
    
    @Test
    public void testUpdate() {
        // First, create a task
        Task task = new Task("Test Update Task", testProject, testSubsystem);
        task.setEstimatedDuration(Duration.ofHours(3));
        Task saved = repository.save(task);
        
        // Now update it
        saved.setTitle("Updated Task Title");
        saved.setProgress(50);
        Task updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Task Title", updated.getTitle());
        assertEquals(50, updated.getProgress());
        
        // Check in DB
        Optional<Task> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Task Title", found.get().getTitle());
        assertEquals(50, found.get().getProgress());
    }
    
    @Test
    public void testDelete() {
        // First, create a task
        Task task = new Task("Test Delete Task", testProject, testSubsystem);
        Task saved = repository.save(task);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Task> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a task
        Task task = new Task("Test DeleteById Task", testProject, testSubsystem);
        Task saved = repository.save(task);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Task> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new task
        Task task = new Task("Test Count Task", testProject, testSubsystem);
        repository.save(task);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}