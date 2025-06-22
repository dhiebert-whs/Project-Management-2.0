// src/test/java/org/frcpm/repositories/ProjectRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Project;
import org.frcpm.repositories.spring.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ProjectRepository using Spring Boot @SpringBootTest.
 * Uses full Spring context instead of @DataJpaTest to avoid context loading issues.
 * 
 * @SpringBootTest loads the complete application context
 * @Transactional ensures each test runs in a transaction that's rolled back
 * @AutoConfigureMockMvc configures MockMvc (though not used in repository tests)
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectRepositoryIntegrationTest {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private Project testProject;
    private Project urgentProject;
    private Project futureProject;
    private Project overdueProject;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testProject = createTestProject();
        urgentProject = createUrgentProject();
        futureProject = createFutureProject();
        overdueProject = createOverdueProject();
    }
    
    /**
     * Creates a standard test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("Robot Chassis");
        project.setStartDate(LocalDate.now().minusDays(7));
        project.setGoalEndDate(LocalDate.now().plusWeeks(4));
        project.setHardDeadline(LocalDate.now().plusWeeks(6));
        project.setDescription("Design and build the robot chassis for competition");
        return project;
    }
    
    /**
     * Creates an urgent project for deadline testing.
     */
    private Project createUrgentProject() {
        Project project = new Project();
        project.setName("Control System");
        project.setStartDate(LocalDate.now().minusDays(3));
        project.setGoalEndDate(LocalDate.now().plusDays(3));
        project.setHardDeadline(LocalDate.now().plusDays(5));
        project.setDescription("Implement robot control system");
        return project;
    }
    
    /**
     * Creates a future project for date filtering tests.
     */
    private Project createFutureProject() {
        Project project = new Project();
        project.setName("Drive Train");
        project.setStartDate(LocalDate.now().plusWeeks(2));
        project.setGoalEndDate(LocalDate.now().plusWeeks(8));
        project.setHardDeadline(LocalDate.now().plusWeeks(10));
        project.setDescription("Design and build the drive train");
        return project;
    }
    
    /**
     * Creates an overdue project for overdue testing.
     */
    private Project createOverdueProject() {
        Project project = new Project();
        project.setName("Vision System");
        project.setStartDate(LocalDate.now().minusWeeks(4));
        project.setGoalEndDate(LocalDate.now().minusDays(7));
        project.setHardDeadline(LocalDate.now().plusDays(14));
        project.setDescription("Implement computer vision system");
        return project;
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
        // Execute - Save project
        Project savedProject = projectRepository.save(testProject);
        entityManager.flush();
        
        // Verify save
        assertThat(savedProject.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Project> found = projectRepository.findById(savedProject.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Robot Chassis");
        assertThat(found.get().getStartDate()).isEqualTo(LocalDate.now().minusDays(7));
        assertThat(found.get().getGoalEndDate()).isEqualTo(LocalDate.now().plusWeeks(4));
        assertThat(found.get().getHardDeadline()).isEqualTo(LocalDate.now().plusWeeks(6));
        assertThat(found.get().getDescription()).isEqualTo("Design and build the robot chassis for competition");
    }
    
    @Test
    void testFindAll() {
        // Setup - Save multiple projects
        projectRepository.save(testProject);
        projectRepository.save(urgentProject);
        entityManager.flush();
        
        // Execute - Find all
        List<Project> allProjects = projectRepository.findAll();
        
        // Verify
        assertThat(allProjects).hasSize(2);
        assertThat(allProjects).extracting(Project::getName)
            .containsExactlyInAnyOrder("Robot Chassis", "Control System");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Save project
        Project savedProject = persistAndFlush(testProject);
        
        // Verify exists before deletion
        assertThat(projectRepository.existsById(savedProject.getId())).isTrue();
        
        // Execute - Delete
        projectRepository.deleteById(savedProject.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(projectRepository.existsById(savedProject.getId())).isFalse();
        assertThat(projectRepository.findById(savedProject.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(projectRepository.count()).isEqualTo(0);
        
        // Setup - Save projects
        projectRepository.save(testProject);
        projectRepository.save(urgentProject);
        projectRepository.save(futureProject);
        entityManager.flush();
        
        // Execute and verify
        assertThat(projectRepository.count()).isEqualTo(3);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        // Setup - Save projects with different names
        projectRepository.save(testProject);      // "Robot Chassis"
        projectRepository.save(urgentProject);    // "Control System"
        projectRepository.save(futureProject);    // "Drive Train"
        entityManager.flush();
        
        // Execute - Case insensitive search for "robot"
        List<Project> robotResults = projectRepository.findByNameContainingIgnoreCase("robot");
        
        // Verify - Should find "Robot Chassis"
        assertThat(robotResults).hasSize(1);
        assertThat(robotResults.get(0).getName()).isEqualTo("Robot Chassis");
        
        // Execute - Search for "system"
        List<Project> systemResults = projectRepository.findByNameContainingIgnoreCase("system");
        
        // Verify - Should find "Control System"
        assertThat(systemResults).hasSize(1);
        assertThat(systemResults.get(0).getName()).isEqualTo("Control System");
        
        // Execute - Search for partial match "trai"
        List<Project> trainResults = projectRepository.findByNameContainingIgnoreCase("trai");
        
        // Verify - Should find "Drive Train"
        assertThat(trainResults).hasSize(1);
        assertThat(trainResults.get(0).getName()).isEqualTo("Drive Train");
    }
    
    @Test
    void testFindByHardDeadlineBefore() {
        // Setup - Save projects with different deadlines
        projectRepository.save(testProject);      // deadline: now + 6 weeks
        projectRepository.save(urgentProject);    // deadline: now + 5 days
        projectRepository.save(futureProject);    // deadline: now + 10 weeks
        entityManager.flush();
        
        // Execute - Find projects with deadlines before now + 1 week
        LocalDate cutoffDate = LocalDate.now().plusWeeks(1);
        List<Project> results = projectRepository.findByHardDeadlineBefore(cutoffDate);
        
        // Verify - Should only find urgentProject (deadline in 5 days)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Control System");
        assertThat(results.get(0).getHardDeadline()).isBefore(cutoffDate);
    }
    
    @Test
    void testFindByStartDateAfter() {
        // Setup - Save projects with different start dates
        projectRepository.save(testProject);      // start: now - 7 days (in past)
        projectRepository.save(urgentProject);    // start: now - 3 days (in past)
        projectRepository.save(futureProject);    // start: now + 2 weeks (in future)
        entityManager.flush();
        
        // Execute - Find projects starting after now
        LocalDate cutoffDate = LocalDate.now();
        List<Project> results = projectRepository.findByStartDateAfter(cutoffDate);
        
        // Verify - Should only find futureProject
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Drive Train");
        assertThat(results.get(0).getStartDate()).isAfter(cutoffDate);
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindByName() {
        // Setup - Save projects
        projectRepository.save(testProject);      // "Robot Chassis"
        projectRepository.save(urgentProject);    // "Control System"
        entityManager.flush();
        
        // Execute - Search for partial name match
        List<Project> results = projectRepository.findByName("Robot");
        
        // Verify - Should find projects containing "Robot"
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Robot Chassis");
    }
    
    @Test
    void testFindByDeadlineBefore() {
        // Setup - Save projects with different deadlines
        projectRepository.save(testProject);      // deadline: now + 6 weeks
        projectRepository.save(urgentProject);    // deadline: now + 5 days
        projectRepository.save(futureProject);    // deadline: now + 10 weeks
        entityManager.flush();
        
        // Execute - Find projects with deadlines before now + 2 weeks
        LocalDate cutoffDate = LocalDate.now().plusWeeks(2);
        List<Project> results = projectRepository.findByDeadlineBefore(cutoffDate);
        
        // Verify - Should find urgentProject (deadline in 5 days)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Control System");
    }
    
    @Test
    void testFindActiveProjects() {
        // Setup - Save projects with different date ranges
        projectRepository.save(testProject);      // active: start in past, goal end in future
        projectRepository.save(urgentProject);    // active: start in past, goal end soon
        projectRepository.save(futureProject);    // not active: start in future
        projectRepository.save(overdueProject);   // not active: goal end in past
        entityManager.flush();
        
        // Execute - Find active projects
        List<Project> results = projectRepository.findActiveProjects();
        
        // Verify - Should find testProject and urgentProject
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Project::getName)
            .containsExactlyInAnyOrder("Robot Chassis", "Control System");
        
        // Verify all results are actually active (started but not past goal end date)
        LocalDate today = LocalDate.now();
        assertThat(results).allMatch(p -> 
            !p.getStartDate().isAfter(today) && !p.getGoalEndDate().isBefore(today));
    }
    
    @Test
    void testFindOverdueProjects() {
        // Setup - Save projects with different statuses
        projectRepository.save(testProject);      // not overdue: goal end in future
        projectRepository.save(urgentProject);    // not overdue: goal end in future
        projectRepository.save(futureProject);    // not overdue: hasn't started
        projectRepository.save(overdueProject);   // overdue: goal end in past
        entityManager.flush();
        
        // Execute - Find overdue projects
        List<Project> results = projectRepository.findOverdueProjects();
        
        // Verify - Should only find overdueProject
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Vision System");
        assertThat(results.get(0).getGoalEndDate()).isBefore(LocalDate.now());
    }
    
    @Test
    void testFindProjectsDueSoon() {
        // Setup - Save projects with different goal end dates
        projectRepository.save(testProject);      // due in 4 weeks
        projectRepository.save(urgentProject);    // due in 3 days
        projectRepository.save(futureProject);    // due in 8 weeks
        projectRepository.save(overdueProject);   // overdue (past due)
        entityManager.flush();
        
        // Execute - Find projects due within 1 week
        LocalDate endDate = LocalDate.now().plusWeeks(1);
        List<Project> results = projectRepository.findProjectsDueSoon(endDate);
        
        // Verify - Should only find urgentProject (due in 3 days)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Control System");
        assertThat(results.get(0).getGoalEndDate()).isBetween(LocalDate.now(), endDate);
    }
    
    @Test
    void testCountActiveProjects() {
        // Setup - Save projects with different statuses
        projectRepository.save(testProject);      // active
        projectRepository.save(urgentProject);    // active
        projectRepository.save(futureProject);    // not active (future)
        projectRepository.save(overdueProject);   // not active (overdue)
        entityManager.flush();
        
        // Execute - Count active projects
        long activeCount = projectRepository.countActiveProjects();
        
        // Verify - Should count 2 active projects
        assertThat(activeCount).isEqualTo(2);
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testProjectWithRelationships() {
        // This test validates that the Project entity can be saved
        // even though it has relationships to Task, Milestone, and Meeting
        // (which don't exist yet, but are defined in the entity)
        
        // Setup - Save project
        Project savedProject = persistAndFlush(testProject);
        
        // Verify - Project is saved correctly
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getTasks()).isEmpty();      // No tasks yet
        assertThat(savedProject.getMilestones()).isEmpty(); // No milestones yet
        assertThat(savedProject.getMeetings()).isEmpty();   // No meetings yet
        
        // Verify - Project can be retrieved
        Optional<Project> retrieved = projectRepository.findById(savedProject.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Robot Chassis");
    }
    
    @Test
    void testDateValidation() {
        // Setup - Create project with invalid date sequence
        Project invalidProject = new Project();
        invalidProject.setName("Invalid Project");
        invalidProject.setStartDate(LocalDate.now());
        invalidProject.setGoalEndDate(LocalDate.now().minusDays(1)); // Goal before start
        invalidProject.setHardDeadline(LocalDate.now().plusDays(1));
        
        // Execute - Save should succeed (no database constraints on date logic)
        Project savedProject = projectRepository.save(invalidProject);
        entityManager.flush();
        
        // Verify - Project is saved (business logic validation would be in service layer)
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getGoalEndDate()).isBefore(savedProject.getStartDate());
    }
    
    @Test
    void testProjectNameSearch() {
        // Setup - Save projects with similar names
        Project chassis1 = new Project("Robot Chassis V1", LocalDate.now(), 
            LocalDate.now().plusWeeks(4), LocalDate.now().plusWeeks(6));
        Project chassis2 = new Project("Robot Chassis V2", LocalDate.now(), 
            LocalDate.now().plusWeeks(8), LocalDate.now().plusWeeks(10));
        Project controlSystem = new Project("Control System", LocalDate.now(), 
            LocalDate.now().plusWeeks(3), LocalDate.now().plusWeeks(5));
        
        projectRepository.save(chassis1);
        projectRepository.save(chassis2);
        projectRepository.save(controlSystem);
        entityManager.flush();
        
        // Execute - Search for "chassis"
        List<Project> chassisResults = projectRepository.findByNameContainingIgnoreCase("chassis");
        
        // Verify - Should find both chassis projects
        assertThat(chassisResults).hasSize(2);
        assertThat(chassisResults).extracting(Project::getName)
            .containsExactlyInAnyOrder("Robot Chassis V1", "Robot Chassis V2");
        
        // Execute - Search for "v1"
        List<Project> v1Results = projectRepository.findByNameContainingIgnoreCase("v1");
        
        // Verify - Should find only V1
        assertThat(v1Results).hasSize(1);
        assertThat(v1Results.get(0).getName()).isEqualTo("Robot Chassis V1");
    }
    
    @Test
    void testProjectTimeRangeQueries() {
        // Setup - Create projects spanning different time periods
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        
        Project pastProject = new Project("Past Project", baseDate.minusMonths(2),
            baseDate.minusMonths(1), baseDate.minusDays(15));
        Project currentProject = new Project("Current Project", baseDate.minusDays(15),
            baseDate.plusMonths(1), baseDate.plusMonths(2));
        Project futureProjectLong = new Project("Future Project", baseDate.plusMonths(1),
            baseDate.plusMonths(3), baseDate.plusMonths(4));
        
        projectRepository.save(pastProject);
        projectRepository.save(currentProject);
        projectRepository.save(futureProjectLong);
        entityManager.flush();
        
        // Execute - Find projects starting after base date
        List<Project> futureStarts = projectRepository.findByStartDateAfter(baseDate);
        
        // Verify - Should find futureProjectLong
        assertThat(futureStarts).hasSize(1);
        assertThat(futureStarts.get(0).getName()).isEqualTo("Future Project");
        
        // Execute - Find projects with deadlines before base date + 1 month
        List<Project> earlyDeadlines = projectRepository.findByHardDeadlineBefore(baseDate.plusMonths(1));
        
        // Verify - Should find pastProject (deadline before cutoff)
        assertThat(earlyDeadlines).hasSize(1);
        assertThat(earlyDeadlines.get(0).getName()).isEqualTo("Past Project");
    }
}