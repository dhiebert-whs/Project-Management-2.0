// src/test/java/org/frcpm/repositories/MilestoneRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MilestoneRepository;
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
 * Integration test for MilestoneRepository using Spring Boot @SpringBootTest.
 * Uses full Spring context instead of @DataJpaTest to avoid context loading issues.
 * 
 * Tests milestone management with date-based queries and project relationships.
 * 
 * @SpringBootTest loads the complete application context
 * @Transactional ensures each test runs in a transaction that's rolled back
 * @AutoConfigureMockMvc configures MockMvc (though not used in repository tests)
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MilestoneRepositoryIntegrationTest {
    
    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private Milestone testMilestone;
    private Milestone futureMilestone;
    private Milestone pastMilestone;
    private Milestone urgentMilestone;
    private Project testProject;
    private Project otherProject;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testProject = createTestProject();
        otherProject = createOtherProject();
        testMilestone = createTestMilestone();
        futureMilestone = createFutureMilestone();
        pastMilestone = createPastMilestone();
        urgentMilestone = createUrgentMilestone();
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("Build Season Project");
        project.setStartDate(LocalDate.now().minusWeeks(2));
        project.setGoalEndDate(LocalDate.now().plusWeeks(6));
        project.setHardDeadline(LocalDate.now().plusWeeks(8));
        return project;
    }
    
    /**
     * Creates another project for multi-project tests.
     */
    private Project createOtherProject() {
        Project project = new Project();
        project.setName("Competition Project");
        project.setStartDate(LocalDate.now());
        project.setGoalEndDate(LocalDate.now().plusWeeks(4));
        project.setHardDeadline(LocalDate.now().plusWeeks(6));
        return project;
    }
    
    /**
     * Creates a test milestone for use in tests.
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone();
        milestone.setName("Design Review");
        milestone.setDescription("Complete design review with mentors and team leads");
        milestone.setDate(LocalDate.now().plusDays(7)); // One week from now
        return milestone;
    }
    
    /**
     * Creates a future milestone for date range tests.
     */
    private Milestone createFutureMilestone() {
        Milestone milestone = new Milestone();
        milestone.setName("Competition Ready");
        milestone.setDescription("Robot fully competition ready with all systems tested");
        milestone.setDate(LocalDate.now().plusWeeks(6)); // Six weeks from now
        return milestone;
    }
    
    /**
     * Creates a past milestone for historical tests.
     */
    private Milestone createPastMilestone() {
        Milestone milestone = new Milestone();
        milestone.setName("Kickoff Complete");
        milestone.setDescription("FRC Kickoff event completed and strategy session held");
        milestone.setDate(LocalDate.now().minusWeeks(2)); // Two weeks ago
        return milestone;
    }
    
    /**
     * Creates an urgent milestone for upcoming tests.
     */
    private Milestone createUrgentMilestone() {
        Milestone milestone = new Milestone();
        milestone.setName("Prototype Demo");
        milestone.setDescription("Demonstrate working prototype to stakeholders");
        milestone.setDate(LocalDate.now().plusDays(2)); // Two days from now
        return milestone;
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
        // Setup - Persist project first
        Project savedProject = persistAndFlush(testProject);
        testMilestone.setProject(savedProject);
        
        // Execute - Save milestone
        Milestone savedMilestone = milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Verify save
        assertThat(savedMilestone.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Milestone> found = milestoneRepository.findById(savedMilestone.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Design Review");
        assertThat(found.get().getDescription()).isEqualTo("Complete design review with mentors and team leads");
        assertThat(found.get().getDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(found.get().getProject().getId()).isEqualTo(savedProject.getId());
    }
    
    @Test
    void testFindAll() {
        // Setup - Persist project and milestones
        Project savedProject = persistAndFlush(testProject);
        
        testMilestone.setProject(savedProject);
        futureMilestone.setProject(savedProject);
        
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        entityManager.flush();
        
        // Execute - Find all
        List<Milestone> allMilestones = milestoneRepository.findAll();
        
        // Verify
        assertThat(allMilestones).hasSize(2);
        assertThat(allMilestones).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Design Review", "Competition Ready");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Persist project and milestone
        Project savedProject = persistAndFlush(testProject);
        testMilestone.setProject(savedProject);
        Milestone savedMilestone = milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Verify exists before deletion
        assertThat(milestoneRepository.existsById(savedMilestone.getId())).isTrue();
        
        // Execute - Delete
        milestoneRepository.deleteById(savedMilestone.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(milestoneRepository.existsById(savedMilestone.getId())).isFalse();
        assertThat(milestoneRepository.findById(savedMilestone.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(milestoneRepository.count()).isEqualTo(0);
        
        // Setup - Persist project and milestone
        Project savedProject = persistAndFlush(testProject);
        testMilestone.setProject(savedProject);
        milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute and verify
        assertThat(milestoneRepository.count()).isEqualTo(1);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByProject() {
        // Setup - Create projects with different milestones
        Project savedProject1 = persistAndFlush(testProject);
        Project savedProject2 = persistAndFlush(otherProject);
        
        testMilestone.setProject(savedProject1);
        futureMilestone.setProject(savedProject1);
        pastMilestone.setProject(savedProject2);
        
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        milestoneRepository.save(pastMilestone);
        entityManager.flush();
        
        // Execute
        List<Milestone> project1Milestones = milestoneRepository.findByProject(savedProject1);
        List<Milestone> project2Milestones = milestoneRepository.findByProject(savedProject2);
        
        // Verify
        assertThat(project1Milestones).hasSize(2);
        assertThat(project1Milestones).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Design Review", "Competition Ready");
        
        assertThat(project2Milestones).hasSize(1);
        assertThat(project2Milestones.get(0).getName()).isEqualTo("Kickoff Complete");
    }
    
    @Test
    void testFindByDateBefore() {
        // Setup - Create milestones with different dates
        Project savedProject = persistAndFlush(testProject);
        
        testMilestone.setDate(LocalDate.now().plusDays(7));  // Future
        pastMilestone.setDate(LocalDate.now().minusDays(7)); // Past
        urgentMilestone.setDate(LocalDate.now().plusDays(2)); // Near future
        
        testMilestone.setProject(savedProject);
        pastMilestone.setProject(savedProject);
        urgentMilestone.setProject(savedProject);
        
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(urgentMilestone);
        entityManager.flush();
        
        // Execute - Find milestones before today (should find past milestone)
        List<Milestone> beforeToday = milestoneRepository.findByDateBefore(LocalDate.now());
        
        // Execute - Find milestones before next week (should find past and urgent)
        List<Milestone> beforeNextWeek = milestoneRepository.findByDateBefore(LocalDate.now().plusDays(5));
        
        // Verify
        assertThat(beforeToday).hasSize(1);
        assertThat(beforeToday.get(0).getName()).isEqualTo("Kickoff Complete");
        assertThat(beforeToday.get(0).getDate()).isBefore(LocalDate.now());
        
        assertThat(beforeNextWeek).hasSize(2);
        assertThat(beforeNextWeek).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Kickoff Complete", "Prototype Demo");
    }
    
    @Test
    void testFindByDateAfter() {
        // Setup - Create milestones with different dates
        Project savedProject = persistAndFlush(testProject);
        
        testMilestone.setDate(LocalDate.now().plusDays(7));  // Future
        pastMilestone.setDate(LocalDate.now().minusDays(7)); // Past
        urgentMilestone.setDate(LocalDate.now().plusDays(2)); // Near future
        
        testMilestone.setProject(savedProject);
        pastMilestone.setProject(savedProject);
        urgentMilestone.setProject(savedProject);
        
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(urgentMilestone);
        entityManager.flush();
        
        // Execute - Find milestones after today (should find future milestones)
        List<Milestone> afterToday = milestoneRepository.findByDateAfter(LocalDate.now());
        
        // Execute - Find milestones after next week (should find none of our test milestones)
        List<Milestone> afterNextWeek = milestoneRepository.findByDateAfter(LocalDate.now().plusDays(10));
        
        // Verify
        assertThat(afterToday).hasSize(2);
        assertThat(afterToday).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Design Review", "Prototype Demo");
        assertThat(afterToday).allMatch(milestone -> milestone.getDate().isAfter(LocalDate.now()));
        
        assertThat(afterNextWeek).hasSize(0);
    }
    
    @Test
    void testFindByDateBetween() {
        // Setup - Create milestones with different dates
        Project savedProject = persistAndFlush(testProject);
        
        pastMilestone.setDate(LocalDate.now().minusDays(10));    // Outside range (past)
        urgentMilestone.setDate(LocalDate.now().plusDays(3));   // Inside range
        testMilestone.setDate(LocalDate.now().plusDays(7));     // Inside range
        futureMilestone.setDate(LocalDate.now().plusDays(20));  // Outside range (future)
        
        pastMilestone.setProject(savedProject);
        urgentMilestone.setProject(savedProject);
        testMilestone.setProject(savedProject);
        futureMilestone.setProject(savedProject);
        
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(urgentMilestone);
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        entityManager.flush();
        
        // Execute - Find milestones in the next two weeks
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(14);
        List<Milestone> milestonesInRange = milestoneRepository.findByDateBetween(startDate, endDate);
        
        // Verify - Should find milestones within the range
        assertThat(milestonesInRange).hasSize(2);
        assertThat(milestonesInRange).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Prototype Demo", "Design Review");
        assertThat(milestonesInRange).allMatch(milestone -> 
            !milestone.getDate().isBefore(startDate) && !milestone.getDate().isAfter(endDate));
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        // Setup - Create milestones with different names
        Project savedProject = persistAndFlush(testProject);
        
        testMilestone.setProject(savedProject);      // "Design Review"
        futureMilestone.setProject(savedProject);    // "Competition Ready"
        pastMilestone.setProject(savedProject);      // "Kickoff Complete"
        urgentMilestone.setProject(savedProject);    // "Prototype Demo"
        
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(urgentMilestone);
        entityManager.flush();
        
        // Execute - Case insensitive search for "design"
        List<Milestone> designMilestones = milestoneRepository.findByNameContainingIgnoreCase("DESIGN");
        
        // Execute - Search for "complete"
        List<Milestone> completeMilestones = milestoneRepository.findByNameContainingIgnoreCase("complete");
        
        // Execute - Search for "demo"
        List<Milestone> demoMilestones = milestoneRepository.findByNameContainingIgnoreCase("demo");
        
        // Verify
        assertThat(designMilestones).hasSize(1);
        assertThat(designMilestones.get(0).getName()).isEqualTo("Design Review");
        
        assertThat(completeMilestones).hasSize(1);
        assertThat(completeMilestones.get(0).getName()).isEqualTo("Kickoff Complete");
        
        assertThat(demoMilestones).hasSize(1);
        assertThat(demoMilestones.get(0).getName()).isEqualTo("Prototype Demo");
    }
    
    @Test
    void testFindByNameContainingIgnoreCase_NoMatch() {
        // Setup - Persist milestones
        Project savedProject = persistAndFlush(testProject);
        testMilestone.setProject(savedProject);
        milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute - Search for non-existent text
        List<Milestone> results = milestoneRepository.findByNameContainingIgnoreCase("nonexistent");
        
        // Verify
        assertThat(results).isEmpty();
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindUpcomingMilestones() {
        // Setup - Create milestones with different dates
        Project savedProject = persistAndFlush(testProject);
        
        pastMilestone.setDate(LocalDate.now().minusDays(5));     // Past (should not appear)
        urgentMilestone.setDate(LocalDate.now().plusDays(3));   // Within range
        testMilestone.setDate(LocalDate.now().plusDays(7));     // Within range
        futureMilestone.setDate(LocalDate.now().plusDays(20));  // Outside range
        
        pastMilestone.setProject(savedProject);
        urgentMilestone.setProject(savedProject);
        testMilestone.setProject(savedProject);
        futureMilestone.setProject(savedProject);
        
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(urgentMilestone);
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        entityManager.flush();
        
        // Execute - Find upcoming milestones within 10 days
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(10);
        List<Milestone> upcomingMilestones = milestoneRepository.findUpcomingMilestones(
            savedProject, today, endDate);
        
        // Verify - Should find milestones within the next 10 days, ordered by date
        assertThat(upcomingMilestones).hasSize(2);
        assertThat(upcomingMilestones).extracting(Milestone::getName)
            .containsExactly("Prototype Demo", "Design Review"); // Should be ordered by date
        assertThat(upcomingMilestones).allMatch(milestone -> 
            !milestone.getDate().isBefore(today) && !milestone.getDate().isAfter(endDate));
        
        // Verify ordering
        assertThat(upcomingMilestones.get(0).getDate()).isBefore(upcomingMilestones.get(1).getDate());
    }
    
    @Test
    void testFindUpcomingMilestones_DifferentProjects() {
        // Setup - Create milestones for different projects
        Project savedProject1 = persistAndFlush(testProject);
        Project savedProject2 = persistAndFlush(otherProject);
        
        urgentMilestone.setDate(LocalDate.now().plusDays(3));
        urgentMilestone.setProject(savedProject1);
        
        testMilestone.setDate(LocalDate.now().plusDays(5));
        testMilestone.setProject(savedProject2); // Different project
        
        milestoneRepository.save(urgentMilestone);
        milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute - Find upcoming milestones for project1 only
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(10);
        List<Milestone> project1Milestones = milestoneRepository.findUpcomingMilestones(
            savedProject1, today, endDate);
        
        // Verify - Should only find milestones for project1
        assertThat(project1Milestones).hasSize(1);
        assertThat(project1Milestones.get(0).getName()).isEqualTo("Prototype Demo");
        assertThat(project1Milestones.get(0).getProject().getId()).isEqualTo(savedProject1.getId());
    }
    
    @Test
    void testFindOverdueMilestones() {
        // Setup - Create milestones with different dates
        Project savedProject = persistAndFlush(testProject);
        
        // Past milestones (overdue)
        pastMilestone.setDate(LocalDate.now().minusDays(10));
        pastMilestone.setProject(savedProject);
        
        Milestone recentPast = new Milestone();
        recentPast.setName("Recent Overdue");
        recentPast.setDescription("Recently overdue milestone");
        recentPast.setDate(LocalDate.now().minusDays(2));
        recentPast.setProject(savedProject);
        
        // Future milestone (not overdue)
        urgentMilestone.setDate(LocalDate.now().plusDays(3));
        urgentMilestone.setProject(savedProject);
        
        milestoneRepository.save(pastMilestone);
        milestoneRepository.save(recentPast);
        milestoneRepository.save(urgentMilestone);
        entityManager.flush();
        
        // Execute - Find overdue milestones
        LocalDate today = LocalDate.now();
        List<Milestone> overdueMilestones = milestoneRepository.findOverdueMilestones(savedProject, today);
        
        // Verify - Should find past milestones, ordered by date
        assertThat(overdueMilestones).hasSize(2);
        assertThat(overdueMilestones).extracting(Milestone::getName)
            .containsExactly("Kickoff Complete", "Recent Overdue"); // Should be ordered by date
        assertThat(overdueMilestones).allMatch(milestone -> milestone.getDate().isBefore(today));
        
        // Verify ordering (earliest first)
        assertThat(overdueMilestones.get(0).getDate()).isBefore(overdueMilestones.get(1).getDate());
    }
    
    @Test
    void testFindOverdueMilestones_NoOverdue() {
        // Setup - Create only future milestones
        Project savedProject = persistAndFlush(testProject);
        
        urgentMilestone.setDate(LocalDate.now().plusDays(1));
        urgentMilestone.setProject(savedProject);
        
        testMilestone.setDate(LocalDate.now().plusDays(7));
        testMilestone.setProject(savedProject);
        
        milestoneRepository.save(urgentMilestone);
        milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute - Find overdue milestones
        LocalDate today = LocalDate.now();
        List<Milestone> overdueMilestones = milestoneRepository.findOverdueMilestones(savedProject, today);
        
        // Verify - Should find no overdue milestones
        assertThat(overdueMilestones).isEmpty();
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testMilestoneProjectRelationship() {
        // Setup - Create project with multiple milestones
        Project savedProject = persistAndFlush(testProject);
        
        testMilestone.setProject(savedProject);
        futureMilestone.setProject(savedProject);
        urgentMilestone.setProject(savedProject);
        
        // Execute - Save milestones
        milestoneRepository.save(testMilestone);
        milestoneRepository.save(futureMilestone);
        milestoneRepository.save(urgentMilestone);
        entityManager.flush();
        
        // Verify - All milestones are associated with the project
        List<Milestone> projectMilestones = milestoneRepository.findByProject(savedProject);
        assertThat(projectMilestones).hasSize(3);
        assertThat(projectMilestones).allMatch(milestone -> 
            milestone.getProject().getId().equals(savedProject.getId()));
        
        // Verify - Individual milestone-project relationships
        assertThat(testMilestone.getProject()).isNotNull();
        assertThat(testMilestone.getProject().getId()).isEqualTo(savedProject.getId());
        assertThat(futureMilestone.getProject()).isNotNull();
        assertThat(futureMilestone.getProject().getId()).isEqualTo(savedProject.getId());
    }
    
    @Test
    void testMilestoneProjectChange() {
        // Setup - Create projects and milestone
        Project savedProject1 = persistAndFlush(testProject);
        Project savedProject2 = persistAndFlush(otherProject);
        
        testMilestone.setProject(savedProject1);
        Milestone savedMilestone = milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Verify initial assignment
        assertThat(savedMilestone.getProject().getId()).isEqualTo(savedProject1.getId());
        assertThat(milestoneRepository.findByProject(savedProject1)).hasSize(1);
        assertThat(milestoneRepository.findByProject(savedProject2)).hasSize(0);
        
        // Execute - Change project assignment
        savedMilestone.setProject(savedProject2);
        milestoneRepository.save(savedMilestone);
        entityManager.flush();
        
        // Verify - Project assignment changed
        assertThat(savedMilestone.getProject().getId()).isEqualTo(savedProject2.getId());
        assertThat(milestoneRepository.findByProject(savedProject1)).hasSize(0);
        assertThat(milestoneRepository.findByProject(savedProject2)).hasSize(1);
    }
    
    // ========== BUSINESS LOGIC VALIDATION ==========
    
    @Test
    void testMilestoneHelperMethods() {
        // Setup - Create milestone with specific date
        Project savedProject = persistAndFlush(testProject);
        
        // Past milestone
        pastMilestone.setDate(LocalDate.now().minusDays(5));
        pastMilestone.setProject(savedProject);
        Milestone savedPastMilestone = milestoneRepository.save(pastMilestone);
        
        // Future milestone
        testMilestone.setDate(LocalDate.now().plusDays(10));
        testMilestone.setProject(savedProject);
        Milestone savedFutureMilestone = milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute and verify - isPassed() method
        assertThat(savedPastMilestone.isPassed()).isTrue();
        assertThat(savedFutureMilestone.isPassed()).isFalse();
        
        // Execute and verify - getDaysUntil() method
        assertThat(savedPastMilestone.getDaysUntil()).isEqualTo(-5); // 5 days ago
        assertThat(savedFutureMilestone.getDaysUntil()).isEqualTo(10); // 10 days from now
    }
    
    @Test
    void testMilestoneToStringMethod() {
        // Setup - Create milestone
        Project savedProject = persistAndFlush(testProject);
        testMilestone.setProject(savedProject);
        Milestone savedMilestone = milestoneRepository.save(testMilestone);
        entityManager.flush();
        
        // Execute and verify - toString() method
        assertThat(savedMilestone.toString()).isEqualTo("Design Review");
    }
    
    @Test
    void testMilestoneDescriptionHandling() {
        // Setup - Create milestone with null description
        Project savedProject = persistAndFlush(testProject);
        
        Milestone milestoneWithoutDesc = new Milestone();
        milestoneWithoutDesc.setName("Simple Milestone");
        milestoneWithoutDesc.setDate(LocalDate.now().plusDays(5));
        milestoneWithoutDesc.setProject(savedProject);
        milestoneWithoutDesc.setDescription(null); // Explicitly null
        
        // Execute - Save milestone with null description
        Milestone savedMilestone = milestoneRepository.save(milestoneWithoutDesc);
        entityManager.flush();
        
        // Verify - Null description is handled correctly
        assertThat(savedMilestone.getDescription()).isNull();
        assertThat(savedMilestone.getName()).isEqualTo("Simple Milestone");
        
        // Execute - Update with description
        savedMilestone.setDescription("Added description later");
        milestoneRepository.save(savedMilestone);
        entityManager.flush();
        
        // Verify - Description update works
        assertThat(savedMilestone.getDescription()).isEqualTo("Added description later");
    }
    
    @Test
    void testMilestoneDateEdgeCases() {
        // Setup - Create milestones on boundary dates
        Project savedProject = persistAndFlush(testProject);
        
        // Milestone due today
        Milestone todayMilestone = new Milestone();
        todayMilestone.setName("Today Milestone");
        todayMilestone.setDate(LocalDate.now()); // Today
        todayMilestone.setProject(savedProject);
        
        // Milestone due exactly one year from now
        Milestone yearMilestone = new Milestone();
        yearMilestone.setName("One Year Milestone");
        yearMilestone.setDate(LocalDate.now().plusYears(1));
        yearMilestone.setProject(savedProject);
        
        milestoneRepository.save(todayMilestone);
        milestoneRepository.save(yearMilestone);
        entityManager.flush();
        
        // Execute - Test edge case queries
        List<Milestone> beforeTomorrow = milestoneRepository.findByDateBefore(LocalDate.now().plusDays(1));
        List<Milestone> afterYesterday = milestoneRepository.findByDateAfter(LocalDate.now().minusDays(1));
        
        // Verify - Today milestone appears in correct queries
        assertThat(beforeTomorrow).hasSize(1);
        assertThat(beforeTomorrow.get(0).getName()).isEqualTo("Today Milestone");
        
        assertThat(afterYesterday).hasSize(2);
        assertThat(afterYesterday).extracting(Milestone::getName)
            .containsExactlyInAnyOrder("Today Milestone", "One Year Milestone");
        
        // Verify - Helper methods work for edge cases
        assertThat(todayMilestone.getDaysUntil()).isEqualTo(0);
        assertThat(todayMilestone.isPassed()).isFalse(); // Today is not considered "passed"
        assertThat(yearMilestone.getDaysUntil()).isEqualTo(365); // Assuming no leap year
    }
    
    @Test
    void testComplexMilestoneScenario() {
        // Setup - Create a complete project milestone scenario
        Project savedProject = persistAndFlush(testProject);
        
        // Create milestones representing a typical FRC build season
        Milestone kickoff = new Milestone("Season Kickoff", LocalDate.now().minusWeeks(8), savedProject);
        kickoff.setDescription("FRC season begins with game reveal");
        
        Milestone designFreeze = new Milestone("Design Freeze", LocalDate.now().minusWeeks(4), savedProject);
        designFreeze.setDescription("All major design decisions finalized");
        
        Milestone prototypeDemo = new Milestone("Prototype Demo", LocalDate.now().minusDays(3), savedProject);
        prototypeDemo.setDescription("Working prototype demonstration to sponsors");
        
        Milestone stopBuild = new Milestone("Stop Build Day", LocalDate.now().plusWeeks(2), savedProject);
        stopBuild.setDescription("Final day for robot modifications");
        
        Milestone competition = new Milestone("First Competition", LocalDate.now().plusWeeks(4), savedProject);
        competition.setDescription("First regional competition event");
        
        // Save all milestones
        milestoneRepository.save(kickoff);
        milestoneRepository.save(designFreeze);
        milestoneRepository.save(prototypeDemo);
        milestoneRepository.save(stopBuild);
        milestoneRepository.save(competition);
        entityManager.flush();
        
        // Execute comprehensive queries
        List<Milestone> allProjectMilestones = milestoneRepository.findByProject(savedProject);
        List<Milestone> pastMilestones = milestoneRepository.findOverdueMilestones(savedProject, LocalDate.now());
        List<Milestone> upcomingMilestones = milestoneRepository.findUpcomingMilestones(
            savedProject, LocalDate.now(), LocalDate.now().plusWeeks(6));
        List<Milestone> buildSeasonMilestones = milestoneRepository.findByNameContainingIgnoreCase("build");
        
        // Verify comprehensive scenario
        assertThat(allProjectMilestones).hasSize(5);
        assertThat(pastMilestones).hasSize(3); // kickoff, designFreeze, prototypeDemo
        assertThat(upcomingMilestones).hasSize(2); // stopBuild, competition
        assertThat(buildSeasonMilestones).hasSize(1); // stopBuild ("Stop Build Day")
        
        // Verify timeline logic
        assertThat(pastMilestones).allMatch(Milestone::isPassed);
        assertThat(upcomingMilestones).noneMatch(Milestone::isPassed);
        
        // Verify ordering in queries with ORDER BY clauses
        assertThat(pastMilestones.get(0).getDate()).isBefore(pastMilestones.get(1).getDate());
        assertThat(upcomingMilestones.get(0).getDate()).isBefore(upcomingMilestones.get(1).getDate());
        
        // Verify business logic methods
        assertThat(kickoff.getDaysUntil()).isLessThan(0); // In the past
        assertThat(competition.getDaysUntil()).isGreaterThan(0); // In the future
    }
}