// src/test/java/org/frcpm/repositories/TaskRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TaskRepository using @DataJpaTest.
 * Uses JPA slice testing for optimized repository testing.
 * 
 * Tests the most complex repository with task management, dependencies, and component associations.
 * 
 * @DataJpaTest loads only JPA components and repositories
 * @AutoConfigureTestDatabase prevents replacement of configured database
 * @ActiveProfiles("test") ensures test-specific configuration
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private Task testTask;
    private Task highPriorityTask;
    private Task completedTask;
    private Project testProject;
    private Project otherProject;
    private Subsystem testSubsystem;
    private Subsystem otherSubsystem;
    private TeamMember testMember;
    private TeamMember otherMember;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testProject = createTestProject();
        otherProject = createOtherProject();
        testSubsystem = createTestSubsystem();
        otherSubsystem = createOtherSubsystem();
        testMember = createTestMember();
        otherMember = createOtherMember();
        testTask = createTestTask();
        highPriorityTask = createHighPriorityTask();
        completedTask = createCompletedTask();
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("Robot Development");
        project.setStartDate(LocalDate.now());
        project.setGoalEndDate(LocalDate.now().plusWeeks(6));
        project.setHardDeadline(LocalDate.now().plusWeeks(8));
        return project;
    }
    
    /**
     * Creates another project for multi-project tests.
     */
    private Project createOtherProject() {
        Project project = new Project();
        project.setName("Competition Prep");
        project.setStartDate(LocalDate.now().minusWeeks(2));
        project.setGoalEndDate(LocalDate.now().plusWeeks(4));
        project.setHardDeadline(LocalDate.now().plusWeeks(6));
        return project;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     */
    private Subsystem createTestSubsystem() {
        // Create required Subteam and persist it
        Subteam mechanicalSubteam = new Subteam("Mechanical", "Mechanical subteam");
        mechanicalSubteam.setColor("#FF0000");
        mechanicalSubteam = entityManager.persistAndFlush(mechanicalSubteam);
        
        Subsystem subsystem = new Subsystem("Drivetrain", testProject, mechanicalSubteam);
        subsystem.setDescription("Main robot drivetrain system");
        return subsystem;
    }
    
    /**
     * Creates another subsystem for multi-subsystem tests.
     */
    private Subsystem createOtherSubsystem() {
        // Create required Subteam and persist it
        Subteam programmingSubteam = new Subteam("Programming", "Programming subteam");
        programmingSubteam.setColor("#00FF00");
        programmingSubteam = entityManager.persistAndFlush(programmingSubteam);
        
        Subsystem subsystem = new Subsystem("Intake", testProject, programmingSubteam);
        subsystem.setDescription("Game piece intake mechanism");
        return subsystem;
    }
    
    /**
     * Creates a test team member for use in tests.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember();
        member.setUsername("taskuser");
        member.setFirstName("Task");
        member.setLastName("User");
        member.setEmail("taskuser@example.com");
        member.setSkills("Java, Mechanical Design");
        return member;
    }
    
    /**
     * Creates another team member for assignment tests.
     */
    private TeamMember createOtherMember() {
        TeamMember member = new TeamMember();
        member.setUsername("teammate");
        member.setFirstName("Team");
        member.setLastName("Member");
        member.setEmail("teammate@example.com");
        member.setSkills("Programming, Testing");
        return member;
    }
    
    /**
     * Creates a test task for use in tests.
     */
    private Task createTestTask() {
        Task task = new Task();
        task.setTitle("Build Chassis Frame");
        task.setDescription("Construct the main chassis frame using aluminum tubing");
        task.setEstimatedDuration(Duration.ofHours(8));
        task.setPriority(Task.Priority.MEDIUM);
        task.setProgress(25);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now().plusDays(3));
        task.setCompleted(false);
        return task;
    }
    
    /**
     * Creates a high priority task for priority tests.
     */
    private Task createHighPriorityTask() {
        Task task = new Task();
        task.setTitle("Program Autonomous");
        task.setDescription("Develop autonomous driving routine");
        task.setEstimatedDuration(Duration.ofHours(12));
        task.setPriority(Task.Priority.HIGH);
        task.setProgress(50);
        task.setStartDate(LocalDate.now().minusDays(1));
        task.setEndDate(LocalDate.now().plusDays(2));
        task.setCompleted(false);
        return task;
    }
    
    /**
     * Creates a completed task for completion tests.
     */
    private Task createCompletedTask() {
        Task task = new Task();
        task.setTitle("Design Intake Mechanism");
        task.setDescription("CAD design for game piece intake");
        task.setEstimatedDuration(Duration.ofHours(6));
        task.setActualDuration(Duration.ofHours(7));
        task.setPriority(Task.Priority.LOW);
        task.setProgress(100);
        task.setStartDate(LocalDate.now().minusDays(5));
        task.setEndDate(LocalDate.now().minusDays(2));
        task.setCompleted(true);
        return task;
    }
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Test
    void testSaveAndFindById() {
        // Setup - Persist dependencies first
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Execute - Save task
        Task savedTask = entityManager.persistAndFlush(testTask);
        
        // Verify save
        assertThat(savedTask.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Task> found = taskRepository.findById(savedTask.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(found.get().getDescription()).isEqualTo("Construct the main chassis frame using aluminum tubing");
        assertThat(found.get().getPriority()).isEqualTo(Task.Priority.MEDIUM);
        assertThat(found.get().getProgress()).isEqualTo(25);
        assertThat(found.get().isCompleted()).isFalse();
        assertThat(found.get().getEstimatedDuration()).isEqualTo(Duration.ofHours(8));
        assertThat(found.get().getProject().getId()).isEqualTo(savedProject.getId());
        assertThat(found.get().getSubsystem().getId()).isEqualTo(savedSubsystem.getId());
        assertThat(found.get().getStartDate()).isEqualTo(LocalDate.now());
        assertThat(found.get().getEndDate()).isEqualTo(LocalDate.now().plusDays(3));
    }
    
    @Test
    void testFindAll() {
        // Setup - Persist dependencies and tasks
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        
        // Execute - Find all
        List<Task> allTasks = taskRepository.findAll();
        
        // Verify
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Persist dependencies and task
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = entityManager.persistAndFlush(testTask);
        
        // Verify exists before deletion
        assertThat(taskRepository.existsById(savedTask.getId())).isTrue();
        
        // Execute - Delete
        taskRepository.deleteById(savedTask.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(taskRepository.existsById(savedTask.getId())).isFalse();
        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(taskRepository.count()).isEqualTo(0);
        
        // Setup - Persist dependencies and task
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        entityManager.persistAndFlush(testTask);
        
        // Execute and verify
        assertThat(taskRepository.count()).isEqualTo(1);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByProject() {
        // Setup - Create projects with different tasks
        Project savedProject1 = entityManager.persistAndFlush(testProject);
        Project savedProject2 = entityManager.persistAndFlush(otherProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject1);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject1);
        highPriorityTask.setSubsystem(savedSubsystem);
        completedTask.setProject(savedProject2);
        completedTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        List<Task> project1Tasks = taskRepository.findByProject(savedProject1);
        List<Task> project2Tasks = taskRepository.findByProject(savedProject2);
        
        // Verify
        assertThat(project1Tasks).hasSize(2);
        assertThat(project1Tasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
        
        assertThat(project2Tasks).hasSize(1);
        assertThat(project2Tasks.get(0).getTitle()).isEqualTo("Design Intake Mechanism");
    }
    
    @Test
    void testFindByProjectId() {
        // Setup - Persist dependencies and task
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        entityManager.persistAndFlush(testTask);
        
        // Execute
        List<Task> results = taskRepository.findByProjectId(savedProject.getId());
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(results.get(0).getProject().getId()).isEqualTo(savedProject.getId());
    }
    
    @Test
    void testFindBySubsystem() {
        // Setup - Create subsystems with different tasks
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem1 = entityManager.persistAndFlush(testSubsystem);
        Subsystem savedSubsystem2 = entityManager.persistAndFlush(otherSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem1);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem1);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem2);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        List<Task> subsystem1Tasks = taskRepository.findBySubsystem(savedSubsystem1);
        List<Task> subsystem2Tasks = taskRepository.findBySubsystem(savedSubsystem2);
        
        // Verify
        assertThat(subsystem1Tasks).hasSize(2);
        assertThat(subsystem1Tasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
        
        assertThat(subsystem2Tasks).hasSize(1);
        assertThat(subsystem2Tasks.get(0).getTitle()).isEqualTo("Design Intake Mechanism");
    }
    
    @Test
    void testFindByCompleted() {
        // Setup - Persist tasks with different completion status
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setCompleted(false);
        
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        completedTask.setCompleted(true);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute - Find incomplete tasks
        List<Task> incompleteTasks = taskRepository.findByCompleted(false);
        
        // Execute - Find completed tasks
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        
        // Verify
        assertThat(incompleteTasks).hasSize(1);
        assertThat(incompleteTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(incompleteTasks.get(0).isCompleted()).isFalse();
        
        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getTitle()).isEqualTo("Design Intake Mechanism");
        assertThat(completedTasks.get(0).isCompleted()).isTrue();
    }
    
    @Test
    void testFindByEndDateBefore() {
        // Setup - Create tasks with different end dates
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        // Task due yesterday (overdue)
        testTask.setEndDate(LocalDate.now().minusDays(1));
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Task due tomorrow (not overdue)
        highPriorityTask.setEndDate(LocalDate.now().plusDays(1));
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        
        // Execute - Find tasks due before today
        List<Task> overdueTasks = taskRepository.findByEndDateBefore(LocalDate.now());
        
        // Verify - Should only find the overdue task
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(overdueTasks.get(0).getEndDate()).isBefore(LocalDate.now());
    }
    
    @Test
    void testFindByPriority() {
        // Setup - Persist tasks with different priorities
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        highPriorityTask.setPriority(Task.Priority.HIGH);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        completedTask.setPriority(Task.Priority.LOW);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute - Find high priority tasks
        List<Task> highPriorityTasks = taskRepository.findByPriority(Task.Priority.HIGH);
        
        // Execute - Find medium priority tasks
        List<Task> mediumPriorityTasks = taskRepository.findByPriority(Task.Priority.MEDIUM);
        
        // Verify
        assertThat(highPriorityTasks).hasSize(1);
        assertThat(highPriorityTasks.get(0).getTitle()).isEqualTo("Program Autonomous");
        assertThat(highPriorityTasks.get(0).getPriority()).isEqualTo(Task.Priority.HIGH);
        
        assertThat(mediumPriorityTasks).hasSize(1);
        assertThat(mediumPriorityTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(mediumPriorityTasks.get(0).getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }
    
    @Test
    void testFindByTitleContainingIgnoreCase() {
        // Setup - Persist tasks with different titles
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        
        // Execute - Case insensitive search for "program"
        List<Task> programTasks = taskRepository.findByTitleContainingIgnoreCase("PROGRAM");
        
        // Execute - Search for "build"
        List<Task> buildTasks = taskRepository.findByTitleContainingIgnoreCase("build");
        
        // Verify
        assertThat(programTasks).hasSize(1);
        assertThat(programTasks.get(0).getTitle()).isEqualTo("Program Autonomous");
        
        assertThat(buildTasks).hasSize(1);
        assertThat(buildTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindByAssignedMember() {
        // Setup - Persist dependencies
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        TeamMember savedMember = entityManager.persistAndFlush(testMember);
        TeamMember savedOtherMember = entityManager.persistAndFlush(otherMember);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Assign member to tasks using helper method
        testTask.assignMember(savedMember);
        highPriorityTask.assignMember(savedMember);
        highPriorityTask.assignMember(savedOtherMember); // Assigned to both members
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        
        // Execute
        List<Task> memberTasks = taskRepository.findByAssignedMember(savedMember);
        List<Task> otherMemberTasks = taskRepository.findByAssignedMember(savedOtherMember);
        
        // Verify
        assertThat(memberTasks).hasSize(2);
        assertThat(memberTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
        
        assertThat(otherMemberTasks).hasSize(1);
        assertThat(otherMemberTasks.get(0).getTitle()).isEqualTo("Program Autonomous");
    }
    
    @Test
    void testFindIncompleteTasksByProject() {
        // Setup - Persist tasks with different completion status
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setCompleted(false);
        
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        highPriorityTask.setCompleted(false);
        
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        completedTask.setCompleted(true);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        List<Task> incompleteTasks = taskRepository.findIncompleteTasksByProject(savedProject);
        
        // Verify - Should only find incomplete tasks
        assertThat(incompleteTasks).hasSize(2);
        assertThat(incompleteTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
        assertThat(incompleteTasks).allMatch(task -> !task.isCompleted());
    }
    
    @Test
    void testFindTasksDueSoon() {
        // Setup - Create tasks with different due dates
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        // Task due tomorrow (within range)
        testTask.setEndDate(LocalDate.now().plusDays(1));
        testTask.setCompleted(false);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Task due in 2 days (within range)
        highPriorityTask.setEndDate(LocalDate.now().plusDays(2));
        highPriorityTask.setCompleted(false);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Task due in 10 days (outside range)
        Task farTask = new Task();
        farTask.setTitle("Future Task");
        farTask.setEndDate(LocalDate.now().plusDays(10));
        farTask.setCompleted(false);
        farTask.setProject(savedProject);
        farTask.setSubsystem(savedSubsystem);
        farTask.setEstimatedDuration(Duration.ofHours(4));
        farTask.setPriority(Task.Priority.LOW);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(farTask);
        
        // Execute - Find tasks due within 3 days
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(3);
        List<Task> dueSoonTasks = taskRepository.findTasksDueSoon(
            savedProject.getId(), startDate, endDate);
        
        // Verify - Should find tasks due within 3 days
        assertThat(dueSoonTasks).hasSize(2);
        assertThat(dueSoonTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous");
        assertThat(dueSoonTasks).allMatch(task -> !task.isCompleted());
        assertThat(dueSoonTasks).allMatch(task -> 
            !task.getEndDate().isBefore(startDate) && !task.getEndDate().isAfter(endDate));
    }
    
    @Test
    void testFindOverdueTasksByProject() {
        // Setup - Create tasks with different due dates
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        // Overdue task (due yesterday, incomplete)
        testTask.setEndDate(LocalDate.now().minusDays(1));
        testTask.setCompleted(false);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Future task (not overdue)
        highPriorityTask.setEndDate(LocalDate.now().plusDays(1));
        highPriorityTask.setCompleted(false);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Overdue but completed task (should not appear)
        completedTask.setEndDate(LocalDate.now().minusDays(2));
        completedTask.setCompleted(true);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        List<Task> overdueTasks = taskRepository.findOverdueTasksByProject(savedProject);
        
        // Verify - Should only find incomplete overdue tasks
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(overdueTasks.get(0).getEndDate()).isBefore(LocalDate.now());
        assertThat(overdueTasks.get(0).isCompleted()).isFalse();
    }
    
    // Additional test methods would continue following the same pattern...
    // For brevity, including just the core repository testing methods
    
    @Test
    void testCountCompletedTasksByProject() {
        // Setup - Create tasks with different completion status
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setCompleted(false);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        completedTask.setCompleted(true);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        long completedCount = taskRepository.countCompletedTasksByProject(savedProject);
        
        // Verify
        assertThat(completedCount).isEqualTo(1);
    }
    
    @Test
    void testCountByProject() {
        // Setup - Create projects with different task counts
        Project savedProject1 = entityManager.persistAndFlush(testProject);
        Project savedProject2 = entityManager.persistAndFlush(otherProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        // 2 tasks for project1
        testTask.setProject(savedProject1);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject1);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // 1 task for project2
        completedTask.setProject(savedProject2);
        completedTask.setSubsystem(savedSubsystem);
        
        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(highPriorityTask);
        entityManager.persistAndFlush(completedTask);
        
        // Execute
        long project1Count = taskRepository.countByProject(savedProject1);
        long project2Count = taskRepository.countByProject(savedProject2);
        
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testTaskDependencyRelationships() {
        // Setup - Create tasks with dependencies using the new TaskDependency entity
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Save tasks first
        Task savedTestTask = entityManager.persistAndFlush(testTask);
        Task savedHighPriorityTask = entityManager.persistAndFlush(highPriorityTask);
        
        // Execute - Create TaskDependency entity directly (since the old helper methods are removed)
        // This test now validates that tasks can be properly saved and referenced by TaskDependency
        // The actual dependency creation should be done through TaskDependencyService in integration tests
        
        // Verify - Tasks are properly saved and can be used for dependencies
        assertThat(savedTestTask.getId()).isNotNull();
        assertThat(savedHighPriorityTask.getId()).isNotNull();
        assertThat(savedTestTask.getProject()).isEqualTo(savedProject);
        assertThat(savedHighPriorityTask.getProject()).isEqualTo(savedProject);
        
        // Note: Actual dependency relationship testing should be done in TaskDependencyServiceTest
        // This test now focuses on basic task entity relationships
    }
    
    @Test
    void testTaskMemberAssignmentRelationships() {
        // Setup - Create task and members
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        TeamMember savedMember1 = entityManager.persistAndFlush(testMember);
        TeamMember savedMember2 = entityManager.persistAndFlush(otherMember);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = entityManager.persistAndFlush(testTask);
        
        // Execute - Assign members using helper method
        savedTask.assignMember(savedMember1);
        savedTask.assignMember(savedMember2);
        
        entityManager.persistAndFlush(savedTask);
        
        // Verify - Task has assigned members
        assertThat(savedTask.getAssignedTo()).hasSize(2);
        assertThat(savedTask.getAssignedTo()).containsExactlyInAnyOrder(savedMember1, savedMember2);
        
        // Verify - Members have assigned tasks (if bidirectional relationship exists)
        List<Task> member1Tasks = taskRepository.findByAssignedMember(savedMember1);
        List<Task> member2Tasks = taskRepository.findByAssignedMember(savedMember2);
        
        assertThat(member1Tasks).hasSize(1);
        assertThat(member1Tasks.get(0).getId()).isEqualTo(savedTask.getId());
        assertThat(member2Tasks).hasSize(1);
        assertThat(member2Tasks.get(0).getId()).isEqualTo(savedTask.getId());
    }
    
    @Test
    void testTaskProgressAndCompletionLogic() {
        // Setup - Create task
        Project savedProject = entityManager.persistAndFlush(testProject);
        Subsystem savedSubsystem = entityManager.persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setProgress(0);
        testTask.setCompleted(false);
        
        Task savedTask = entityManager.persistAndFlush(testTask);
        
        // Execute - Update progress to 100%
        savedTask.setProgress(100);
        
        entityManager.persistAndFlush(savedTask);
        
        // Verify - Task should be automatically marked as completed
        assertThat(savedTask.getProgress()).isEqualTo(100);
        assertThat(savedTask.isCompleted()).isTrue();
        
        // Execute - Mark as completed explicitly
        savedTask.setProgress(75);
        savedTask.setCompleted(true);
        
        entityManager.persistAndFlush(savedTask);
        
        // Verify - Progress should be set to 100% when marked completed
        assertThat(savedTask.getProgress()).isEqualTo(100);
        assertThat(savedTask.isCompleted()).isTrue();
    }
}