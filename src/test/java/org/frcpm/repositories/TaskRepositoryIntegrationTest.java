// src/test/java/org/frcpm/repositories/TaskRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TaskRepository using Spring Boot @SpringBootTest.
 * Uses full Spring context instead of @DataJpaTest to avoid context loading issues.
 * 
 * Tests the most complex repository with task management, dependencies, and component associations.
 * 
 * @SpringBootTest loads the complete application context
 * @Transactional ensures each test runs in a transaction that's rolled back
 * @AutoConfigureMockMvc configures MockMvc (though not used in repository tests)
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskRepositoryIntegrationTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private EntityManager entityManager;
    
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
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Drivetrain");
        subsystem.setDescription("Main robot drivetrain system");
        subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        return subsystem;
    }
    
    /**
     * Creates another subsystem for multi-subsystem tests.
     */
    private Subsystem createOtherSubsystem() {
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Intake");
        subsystem.setDescription("Game piece intake mechanism");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
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
    
    /**
     * Helper method to persist and flush an entity.
     * Replaces TestEntityManager's persistAndFlush functionality.
     */
    private <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Test
    void testSaveAndFindById() {
        // Setup - Persist dependencies first
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Execute - Save task
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        taskRepository.save(testTask);
        entityManager.flush();
        
        // Execute and verify
        assertThat(taskRepository.count()).isEqualTo(1);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByProject() {
        // Setup - Create projects with different tasks
        Project savedProject1 = persistAndFlush(testProject);
        Project savedProject2 = persistAndFlush(otherProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject1);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject1);
        highPriorityTask.setSubsystem(savedSubsystem);
        completedTask.setProject(savedProject2);
        completedTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        taskRepository.save(testTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem1 = persistAndFlush(testSubsystem);
        Subsystem savedSubsystem2 = persistAndFlush(otherSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem1);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem1);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem2);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setCompleted(false);
        
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        completedTask.setCompleted(true);
        
        taskRepository.save(testTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // Task due yesterday (overdue)
        testTask.setEndDate(LocalDate.now().minusDays(1));
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        // Task due tomorrow (not overdue)
        highPriorityTask.setEndDate(LocalDate.now().plusDays(1));
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        highPriorityTask.setPriority(Task.Priority.HIGH);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        completedTask.setPriority(Task.Priority.LOW);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember = persistAndFlush(testMember);
        TeamMember savedOtherMember = persistAndFlush(otherMember);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Assign member to tasks using helper method
        testTask.assignMember(savedMember);
        highPriorityTask.assignMember(savedMember);
        highPriorityTask.assignMember(savedOtherMember); // Assigned to both members
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setCompleted(false);
        
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        highPriorityTask.setCompleted(false);
        
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        completedTask.setCompleted(true);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
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
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(farTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
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
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
        // Execute
        List<Task> overdueTasks = taskRepository.findOverdueTasksByProject(savedProject);
        
        // Verify - Should only find incomplete overdue tasks
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks.get(0).getTitle()).isEqualTo("Build Chassis Frame");
        assertThat(overdueTasks.get(0).getEndDate()).isBefore(LocalDate.now());
        assertThat(overdueTasks.get(0).isCompleted()).isFalse();
    }
    
    @Test
    void testFindTasksAssignedToMembers() {
        // Setup - Persist dependencies
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember1 = persistAndFlush(testMember);
        TeamMember savedMember2 = persistAndFlush(otherMember);
        
        // Create third member not assigned to any tasks
        TeamMember member3 = new TeamMember();
        member3.setUsername("unassigned");
        member3.setFirstName("Un");
        member3.setLastName("Assigned");
        member3.setEmail("unassigned@example.com");
        TeamMember savedMember3 = persistAndFlush(member3);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        // Assign tasks to members
        testTask.assignMember(savedMember1);
        highPriorityTask.assignMember(savedMember2);
        completedTask.assignMember(savedMember1);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
        // Execute - Find tasks assigned to member1 and member2
        List<Long> memberIds = Arrays.asList(savedMember1.getId(), savedMember2.getId());
        List<Task> assignedTasks = taskRepository.findTasksAssignedToMembers(memberIds);
        
        // Verify - Should find all tasks assigned to either member
        assertThat(assignedTasks).hasSize(3);
        assertThat(assignedTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Program Autonomous", "Design Intake Mechanism");
        
        // Execute - Find tasks assigned to only member3 (should be empty)
        List<Long> member3Ids = Arrays.asList(savedMember3.getId());
        List<Task> unassignedTasks = taskRepository.findTasksAssignedToMembers(member3Ids);
        
        // Verify
        assertThat(unassignedTasks).isEmpty();
    }
    
    @Test
    void testCountCompletedTasksByProject() {
        // Setup - Create tasks with different completion status
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setCompleted(false);
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        
        highPriorityTask.setCompleted(false);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        completedTask.setCompleted(true);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        // Create another completed task
        Task anotherCompleted = new Task();
        anotherCompleted.setTitle("Another Completed Task");
        anotherCompleted.setCompleted(true);
        anotherCompleted.setProject(savedProject);
        anotherCompleted.setSubsystem(savedSubsystem);
        anotherCompleted.setEstimatedDuration(Duration.ofHours(2));
        anotherCompleted.setPriority(Task.Priority.LOW);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        taskRepository.save(anotherCompleted);
        entityManager.flush();
        
        // Execute
        long completedCount = taskRepository.countCompletedTasksByProject(savedProject);
        
        // Verify
        assertThat(completedCount).isEqualTo(2);
    }
    
    @Test
    void testCountByProject() {
        // Setup - Create projects with different task counts
        Project savedProject1 = persistAndFlush(testProject);
        Project savedProject2 = persistAndFlush(otherProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // 2 tasks for project1
        testTask.setProject(savedProject1);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject1);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // 1 task for project2
        completedTask.setProject(savedProject2);
        completedTask.setSubsystem(savedSubsystem);
        
        taskRepository.save(testTask);
        taskRepository.save(highPriorityTask);
        taskRepository.save(completedTask);
        entityManager.flush();
        
        // Execute
        long project1Count = taskRepository.countByProject(savedProject1);
        long project2Count = taskRepository.countByProject(savedProject2);
        
        // Verify
        assertThat(project1Count).isEqualTo(2);
        assertThat(project2Count).isEqualTo(1);
    }
    
    @Test
    void testFindTasksWithDependencies() {
        // Setup - Create tasks with dependencies
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        // Save tasks first
        Task savedTestTask = taskRepository.save(testTask);
        Task savedHighPriorityTask = taskRepository.save(highPriorityTask);
        Task savedCompletedTask = taskRepository.save(completedTask);
        entityManager.flush();
        
        // Add dependencies: highPriorityTask depends on testTask and completedTask
        savedHighPriorityTask.addPreDependency(savedTestTask);
        savedHighPriorityTask.addPreDependency(savedCompletedTask);
        
        taskRepository.save(savedHighPriorityTask);
        entityManager.flush();
        
        // Execute
        List<Task> tasksWithDependencies = taskRepository.findTasksWithDependencies();
        
        // Verify - Should only find tasks that have dependencies
        assertThat(tasksWithDependencies).hasSize(1);
        assertThat(tasksWithDependencies.get(0).getTitle()).isEqualTo("Program Autonomous");
        assertThat(tasksWithDependencies.get(0).getPreDependencies()).hasSize(2);
    }
    
    @Test
    void testFindBlockingTasks() {
        // Setup - Create tasks with dependencies
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        completedTask.setProject(savedProject);
        completedTask.setSubsystem(savedSubsystem);
        
        // Save tasks first
        Task savedTestTask = taskRepository.save(testTask);
        Task savedHighPriorityTask = taskRepository.save(highPriorityTask);
        Task savedCompletedTask = taskRepository.save(completedTask);
        entityManager.flush();
        
        // Add dependencies: highPriorityTask depends on testTask and completedTask
        savedHighPriorityTask.addPreDependency(savedTestTask);
        savedHighPriorityTask.addPreDependency(savedCompletedTask);
        
        taskRepository.save(savedHighPriorityTask);
        taskRepository.save(savedTestTask);
        taskRepository.save(savedCompletedTask);
        entityManager.flush();
        
        // Execute
        List<Task> blockingTasks = taskRepository.findBlockingTasks();
        
        // Verify - Should find tasks that other tasks depend on
        assertThat(blockingTasks).hasSize(2);
        assertThat(blockingTasks).extracting(Task::getTitle)
            .containsExactlyInAnyOrder("Build Chassis Frame", "Design Intake Mechanism");
        assertThat(blockingTasks).allMatch(task -> task.getPostDependencies().size() > 0);
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testTaskDependencyRelationships() {
        // Setup - Create tasks with bidirectional dependencies
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        
        // Save tasks first
        Task savedTestTask = taskRepository.save(testTask);
        Task savedHighPriorityTask = taskRepository.save(highPriorityTask);
        entityManager.flush();
        
        // Execute - Add dependency using helper method
        savedHighPriorityTask.addPreDependency(savedTestTask);
        
        taskRepository.save(savedHighPriorityTask);
        taskRepository.save(savedTestTask);
        entityManager.flush();
        
        // Verify - Bidirectional relationship
        assertThat(savedHighPriorityTask.getPreDependencies()).contains(savedTestTask);
        assertThat(savedTestTask.getPostDependencies()).contains(savedHighPriorityTask);
        
        // Verify - Dependency counts
        assertThat(savedHighPriorityTask.getPreDependencies()).hasSize(1);
        assertThat(savedTestTask.getPostDependencies()).hasSize(1);
    }
    
    @Test
    void testTaskMemberAssignmentRelationships() {
        // Setup - Create task and members
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember1 = persistAndFlush(testMember);
        TeamMember savedMember2 = persistAndFlush(otherMember);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Execute - Assign members using helper method
        savedTask.assignMember(savedMember1);
        savedTask.assignMember(savedMember2);
        
        taskRepository.save(savedTask);
        entityManager.flush();
        
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
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setProgress(0);
        testTask.setCompleted(false);
        
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Execute - Update progress to 100%
        savedTask.setProgress(100);
        
        taskRepository.save(savedTask);
        entityManager.flush();
        
        // Verify - Task should be automatically marked as completed
        assertThat(savedTask.getProgress()).isEqualTo(100);
        assertThat(savedTask.isCompleted()).isTrue();
        
        // Execute - Mark as completed explicitly
        savedTask.setProgress(75);
        savedTask.setCompleted(true);
        
        taskRepository.save(savedTask);
        entityManager.flush();
        
        // Verify - Progress should be set to 100% when marked completed
        assertThat(savedTask.getProgress()).isEqualTo(100);
        assertThat(savedTask.isCompleted()).isTrue();
    }
    
    @Test
    void testTaskDateValidation() {
        // Setup - Create task with valid dates
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(5));
        
        // Execute - Save task with valid dates
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Verify - Dates are saved correctly
        assertThat(savedTask.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(savedTask.getEndDate()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(savedTask.getEndDate()).isAfter(savedTask.getStartDate());
    }
    
    // ========== BUSINESS LOGIC VALIDATION ==========
    
    @Test
    void testTaskDurationCalculation() {
        // Setup - Create task with estimated and actual duration
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setEstimatedDuration(Duration.ofHours(8));
        testTask.setActualDuration(Duration.ofHours(10));
        
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Verify - Duration methods work correctly
        assertThat(savedTask.getEstimatedDuration()).isEqualTo(Duration.ofHours(8));
        assertThat(savedTask.getActualDuration()).isEqualTo(Duration.ofHours(10));
        
        // Test null actual duration
        savedTask.setActualDuration(null);
        taskRepository.save(savedTask);
        entityManager.flush();
        
        assertThat(savedTask.getActualDuration()).isNull();
    }
    
    @Test
    void testTaskPriorityEnum() {
        // Setup - Create tasks with different priorities
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // Test all priority levels
        Task lowTask = new Task();
        lowTask.setTitle("Low Priority Task");
        lowTask.setPriority(Task.Priority.LOW);
        lowTask.setProject(savedProject);
        lowTask.setSubsystem(savedSubsystem);
        lowTask.setEstimatedDuration(Duration.ofHours(2));
        
        Task criticalTask = new Task();
        criticalTask.setTitle("Critical Priority Task");
        criticalTask.setPriority(Task.Priority.CRITICAL);
        criticalTask.setProject(savedProject);
        criticalTask.setSubsystem(savedSubsystem);
        criticalTask.setEstimatedDuration(Duration.ofHours(1));
        
        taskRepository.save(lowTask);
        taskRepository.save(criticalTask);
        entityManager.flush();
        
        // Execute - Find by different priorities
        List<Task> lowPriorityTasks = taskRepository.findByPriority(Task.Priority.LOW);
        List<Task> criticalPriorityTasks = taskRepository.findByPriority(Task.Priority.CRITICAL);
        
        // Verify
        assertThat(lowPriorityTasks).hasSize(1);
        assertThat(lowPriorityTasks.get(0).getPriority()).isEqualTo(Task.Priority.LOW);
        assertThat(lowPriorityTasks.get(0).getPriority().getValue()).isEqualTo(1);
        
        assertThat(criticalPriorityTasks).hasSize(1);
        assertThat(criticalPriorityTasks.get(0).getPriority()).isEqualTo(Task.Priority.CRITICAL);
        assertThat(criticalPriorityTasks.get(0).getPriority().getValue()).isEqualTo(4);
    }
    
    @Test
    void testComplexTaskScenario() {
        // Setup - Create a complex task scenario with multiple relationships
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember1 = persistAndFlush(testMember);
        TeamMember savedMember2 = persistAndFlush(otherMember);
        
        // Create dependency task
        Task prerequisiteTask = new Task();
        prerequisiteTask.setTitle("Prerequisite Task");
        prerequisiteTask.setProject(savedProject);
        prerequisiteTask.setSubsystem(savedSubsystem);
        prerequisiteTask.setEstimatedDuration(Duration.ofHours(4));
        prerequisiteTask.setPriority(Task.Priority.HIGH);
        prerequisiteTask.setCompleted(true);
        prerequisiteTask.setProgress(100);
        
        Task savedPrerequisite = taskRepository.save(prerequisiteTask);
        
        // Create main task with all relationships
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(3));
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setProgress(75);
        
        Task savedMainTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Add relationships
        savedMainTask.addPreDependency(savedPrerequisite);
        savedMainTask.assignMember(savedMember1);
        savedMainTask.assignMember(savedMember2);
        
        taskRepository.save(savedMainTask);
        taskRepository.save(savedPrerequisite);
        entityManager.flush();
        
        // Execute comprehensive queries
        List<Task> projectTasks = taskRepository.findByProject(savedProject);
        List<Task> highPriorityTasks = taskRepository.findByPriority(Task.Priority.HIGH);
        List<Task> member1Tasks = taskRepository.findByAssignedMember(savedMember1);
        List<Task> tasksWithDeps = taskRepository.findTasksWithDependencies();
        List<Task> blockingTasks = taskRepository.findBlockingTasks();
        
        // Verify comprehensive scenario
        assertThat(projectTasks).hasSize(2);
        assertThat(highPriorityTasks).hasSize(2);
        assertThat(member1Tasks).hasSize(1);
        assertThat(tasksWithDeps).hasSize(1);
        assertThat(blockingTasks).hasSize(1);
        
        // Verify complex relationships
        assertThat(savedMainTask.getPreDependencies()).contains(savedPrerequisite);
        assertThat(savedMainTask.getAssignedTo()).containsExactlyInAnyOrder(savedMember1, savedMember2);
        assertThat(savedPrerequisite.getPostDependencies()).contains(savedMainTask);
    }
    
    // ========== CONSTRAINT AND VALIDATION TESTING ==========
    
    @Test
    void testTaskConstraints() {
        // Setup - Create task with all required fields
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setTitle("Valid Task");
        testTask.setEstimatedDuration(Duration.ofHours(1));
        testTask.setPriority(Task.Priority.MEDIUM);
        
        // Execute - Save valid task
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Verify - Task saved successfully
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Valid Task");
        
        // Verify - Required fields are present
        assertThat(savedTask.getProject()).isNotNull();
        assertThat(savedTask.getSubsystem()).isNotNull();
        assertThat(savedTask.getEstimatedDuration()).isNotNull();
        assertThat(savedTask.getPriority()).isNotNull();
    }
    
    @Test
    void testTaskProgressConstraints() {
        // Setup - Create task
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Execute - Test progress boundary values
        savedTask.setProgress(0);
        taskRepository.save(savedTask);
        assertThat(savedTask.getProgress()).isEqualTo(0);
        
        savedTask.setProgress(100);
        taskRepository.save(savedTask);
        assertThat(savedTask.getProgress()).isEqualTo(100);
        assertThat(savedTask.isCompleted()).isTrue(); // Should auto-complete at 100%
        
        // Test that progress is clamped to valid range (handled by entity logic)
        savedTask.setProgress(150); // Should be clamped to 100
        assertThat(savedTask.getProgress()).isEqualTo(100);
        
        savedTask.setProgress(-10); // Should be clamped to 0
        assertThat(savedTask.getProgress()).isEqualTo(0);
    }
    
    @Test
    void testTaskSelfDependencyPrevention() {
        // Setup - Create task
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Execute and Verify - Adding self as dependency should throw exception
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> savedTask.addPreDependency(savedTask),
            "A task cannot depend on itself"
        );
    }
    
    // ========== PERFORMANCE AND BULK OPERATIONS ==========
    
    @Test
    void testBulkTaskOperations() {
        // Setup - Create project and subsystem
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // Create multiple tasks
        List<Task> tasks = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Task task = new Task();
            task.setTitle("Bulk Task " + i);
            task.setProject(savedProject);
            task.setSubsystem(savedSubsystem);
            task.setEstimatedDuration(Duration.ofHours(i));
            task.setPriority(i % 2 == 0 ? Task.Priority.HIGH : Task.Priority.LOW);
            task.setCompleted(i <= 5); // First 5 are completed
            task.setProgress(i <= 5 ? 100 : i * 10);
            tasks.add(task);
        }
        
        // Execute - Save all tasks
        taskRepository.saveAll(tasks);
        entityManager.flush();
        
        // Verify - Bulk operations
        List<Task> allTasks = taskRepository.findByProject(savedProject);
        assertThat(allTasks).hasSize(10);
        
        List<Task> highPriorityTasks = taskRepository.findByPriority(Task.Priority.HIGH);
        assertThat(highPriorityTasks).hasSize(5); // Even numbered tasks
        
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        assertThat(completedTasks).hasSize(5); // First 5 tasks
        
        long totalTasks = taskRepository.countByProject(savedProject);
        assertThat(totalTasks).isEqualTo(10);
        
        long completedCount = taskRepository.countCompletedTasksByProject(savedProject);
        assertThat(completedCount).isEqualTo(5);
    }
    
    // ========== ERROR HANDLING AND EDGE CASES ==========
    
    @Test
    void testTaskRepositoryErrorHandling() {
        // Test null parameter handling in repository methods
        
        // findByProject with null - Spring Data JPA handles this gracefully
        List<Task> nullProjectTasks = taskRepository.findByProject(null);
        assertThat(nullProjectTasks).isEmpty();
        
        // findBySubsystem with null - Spring Data JPA handles this gracefully
        List<Task> nullSubsystemTasks = taskRepository.findBySubsystem(null);
        assertThat(nullSubsystemTasks).isEmpty();
        
        // findByAssignedMember with null - Custom query handles this
        List<Task> nullMemberTasks = taskRepository.findByAssignedMember(null);
        assertThat(nullMemberTasks).isEmpty();
    }
    
    @Test
    void testTaskDeletionCascading() {
        // Setup - Create tasks with relationships
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        Task savedTask1 = taskRepository.save(testTask);
        
        highPriorityTask.setProject(savedProject);
        highPriorityTask.setSubsystem(savedSubsystem);
        Task savedTask2 = taskRepository.save(highPriorityTask);
        
        entityManager.flush();
        
        // Add relationships
        savedTask2.addPreDependency(savedTask1);
        savedTask1.assignMember(savedMember);
        savedTask2.assignMember(savedMember);
        
        taskRepository.save(savedTask1);
        taskRepository.save(savedTask2);
        entityManager.flush();
        
        // Verify relationships exist
        assertThat(savedTask2.getPreDependencies()).contains(savedTask1);
        assertThat(savedTask1.getPostDependencies()).contains(savedTask2);
        
        // Execute - Delete task with dependencies
        taskRepository.delete(savedTask1);
        entityManager.flush();
        
        // Verify - Task is deleted and relationships are cleaned up
        assertThat(taskRepository.findById(savedTask1.getId())).isEmpty();
        
        // Reload task2 and verify dependency was removed
        Task reloadedTask2 = taskRepository.findById(savedTask2.getId()).orElse(null);
        assertThat(reloadedTask2).isNotNull();
        assertThat(reloadedTask2.getPreDependencies()).doesNotContain(savedTask1);
    }
    
    @Test
    void testTaskDateEdgeCases() {
        // Setup - Create tasks with edge case dates
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // Task with same start and end date
        Task sameDayTask = new Task();
        sameDayTask.setTitle("Same Day Task");
        sameDayTask.setProject(savedProject);
        sameDayTask.setSubsystem(savedSubsystem);
        sameDayTask.setStartDate(LocalDate.now());
        sameDayTask.setEndDate(LocalDate.now());
        sameDayTask.setEstimatedDuration(Duration.ofHours(8));
        sameDayTask.setPriority(Task.Priority.MEDIUM);
        
        // Task with null dates
        Task noDateTask = new Task();
        noDateTask.setTitle("No Date Task");
        noDateTask.setProject(savedProject);
        noDateTask.setSubsystem(savedSubsystem);
        noDateTask.setStartDate(null);
        noDateTask.setEndDate(null);
        noDateTask.setEstimatedDuration(Duration.ofHours(4));
        noDateTask.setPriority(Task.Priority.LOW);
        
        taskRepository.save(sameDayTask);
        taskRepository.save(noDateTask);
        entityManager.flush();
        
        // Execute - Query with date conditions
        List<Task> beforeToday = taskRepository.findByEndDateBefore(LocalDate.now());
        List<Task> dueSoonTasks = taskRepository.findTasksDueSoon(
            savedProject.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        
        // Verify - Edge cases handled correctly
        assertThat(beforeToday).isEmpty(); // Same day task should not appear as "before today"
        assertThat(dueSoonTasks).hasSize(1); // Same day task should appear in "due soon"
        assertThat(dueSoonTasks.get(0).getTitle()).isEqualTo("Same Day Task");
        
        // Verify null dates are handled
        assertThat(noDateTask.getStartDate()).isNull();
        assertThat(noDateTask.getEndDate()).isNull();
    }
    
    // ========== INTEGRATION WITH SERVICE LAYER PATTERNS ==========
    
    @Test
    void testRepositoryServiceIntegration() {
        // This test verifies that the repository works correctly with service layer patterns
        // Setup - Create realistic task scenario
        Project savedProject = persistAndFlush(testProject);
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Create task following service layer patterns
        testTask.setProject(savedProject);
        testTask.setSubsystem(savedSubsystem);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(5));
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setProgress(0);
        testTask.setCompleted(false);
        
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        
        // Simulate service layer operations
        
        // 1. Assign member (service layer pattern)
        savedTask.assignMember(savedMember);
        taskRepository.save(savedTask);
        
        // 2. Update progress (service layer pattern)
        savedTask.setProgress(50);
        taskRepository.save(savedTask);
        
        // 3. Mark as completed (service layer pattern)
        savedTask.setCompleted(true);
        taskRepository.save(savedTask);
        
        entityManager.flush();
        
        // Verify - All service layer operations work through repository
        Task finalTask = taskRepository.findById(savedTask.getId()).orElse(null);
        assertThat(finalTask).isNotNull();
        assertThat(finalTask.getAssignedTo()).contains(savedMember);
        assertThat(finalTask.getProgress()).isEqualTo(100); // Auto-set to 100 when completed
        assertThat(finalTask.isCompleted()).isTrue();
        
        // Verify - Repository queries work for service layer
        List<Task> memberTasks = taskRepository.findByAssignedMember(savedMember);
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        List<Task> highPriorityTasks = taskRepository.findByPriority(Task.Priority.HIGH);
        
        assertThat(memberTasks).contains(finalTask);
        assertThat(completedTasks).contains(finalTask);
        assertThat(highPriorityTasks).contains(finalTask);
    }
}