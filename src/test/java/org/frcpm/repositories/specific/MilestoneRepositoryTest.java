// src/test/java/org/frcpm/repositories/specific/MilestoneRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.MilestoneRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MilestoneRepository implementation.
 */
public class MilestoneRepositoryTest extends BaseRepositoryTest {

    private MilestoneRepository milestoneRepository;
    private Project testProject;
    private Project secondProject;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        milestoneRepository = new MilestoneRepositoryImpl();
    }

    @Override
    protected void setupTestData() {
        // Create test projects
        beginTransaction();
        
        testProject = new Project("Test Project", 
                LocalDate.now().minusDays(10), 
                LocalDate.now().plusDays(80), 
                LocalDate.now().plusDays(90));
        em.persist(testProject);
        
        secondProject = new Project("Second Project", 
                LocalDate.now().minusDays(5), 
                LocalDate.now().plusDays(25), 
                LocalDate.now().plusDays(30));
        em.persist(secondProject);
        
        commitTransaction();
    }

    @Test
    public void testSave() {
        // Create a new milestone
        Milestone milestone = new Milestone("Test Milestone", LocalDate.now().plusDays(30), testProject);
        milestone.setDescription("This is a test milestone");

        // Save the milestone
        Milestone savedMilestone = milestoneRepository.save(milestone);

        // Verify saved milestone
        assertNotNull(savedMilestone);
        assertNotNull(savedMilestone.getId());
        assertEquals("Test Milestone", savedMilestone.getName());
        assertEquals("This is a test milestone", savedMilestone.getDescription());
        assertEquals(testProject.getId(), savedMilestone.getProject().getId());
    }

    @Test
    public void testFindById() {
        // Create and save a milestone
        beginTransaction();
        Milestone milestone = new Milestone("Find By ID Milestone", LocalDate.now().plusDays(15), testProject);
        em.persist(milestone);
        Long milestoneId = milestone.getId();
        commitTransaction();

        // Find by ID
        Optional<Milestone> found = milestoneRepository.findById(milestoneId);

        // Verify the result
        assertTrue(found.isPresent());
        assertEquals("Find By ID Milestone", found.get().getName());
        assertEquals(testProject.getId(), found.get().getProject().getId());
    }

    @Test
    public void testFindAll() {
        // Create multiple milestones
        beginTransaction();
        Milestone milestone1 = new Milestone("Milestone 1", LocalDate.now().plusDays(10), testProject);
        Milestone milestone2 = new Milestone("Milestone 2", LocalDate.now().plusDays(20), testProject);
        Milestone milestone3 = new Milestone("Milestone 3", LocalDate.now().plusDays(5), secondProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        commitTransaction();

        // Find all milestones
        List<Milestone> milestones = milestoneRepository.findAll();

        // Verify results
        assertNotNull(milestones);
        assertEquals(3, milestones.size());
    }

    @Test
    public void testUpdate() {
        // Create and save a milestone
        beginTransaction();
        Milestone milestone = new Milestone("Original Name", LocalDate.now().plusDays(15), testProject);
        em.persist(milestone);
        Long milestoneId = milestone.getId();
        commitTransaction();

        // Update the milestone
        Optional<Milestone> found = milestoneRepository.findById(milestoneId);
        assertTrue(found.isPresent());
        
        Milestone toUpdate = found.get();
        toUpdate.setName("Updated Name");
        toUpdate.setDescription("Updated Description");
        toUpdate.setDate(LocalDate.now().plusDays(25));
        
        milestoneRepository.save(toUpdate);

        // Verify the update
        Optional<Milestone> updated = milestoneRepository.findById(milestoneId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("Updated Description", updated.get().getDescription());
        assertEquals(LocalDate.now().plusDays(25), updated.get().getDate());
    }

    @Test
    public void testDelete() {
        // Create and save a milestone
        beginTransaction();
        Milestone milestone = new Milestone("To Delete", LocalDate.now().plusDays(15), testProject);
        em.persist(milestone);
        Long milestoneId = milestone.getId();
        commitTransaction();

        // Verify it exists
        Optional<Milestone> found = milestoneRepository.findById(milestoneId);
        assertTrue(found.isPresent());

        // Delete the milestone
        milestoneRepository.delete(found.get());

        // Verify it was deleted
        Optional<Milestone> deleted = milestoneRepository.findById(milestoneId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testDeleteById() {
        // Create and save a milestone
        beginTransaction();
        Milestone milestone = new Milestone("To Delete By ID", LocalDate.now().plusDays(15), testProject);
        em.persist(milestone);
        Long milestoneId = milestone.getId();
        commitTransaction();

        // Verify it exists
        Optional<Milestone> found = milestoneRepository.findById(milestoneId);
        assertTrue(found.isPresent());

        // Delete by ID
        boolean result = milestoneRepository.deleteById(milestoneId);
        assertTrue(result);

        // Verify it was deleted
        Optional<Milestone> deleted = milestoneRepository.findById(milestoneId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testCount() {
        // Create multiple milestones
        beginTransaction();
        Milestone milestone1 = new Milestone("Milestone 1", LocalDate.now().plusDays(10), testProject);
        Milestone milestone2 = new Milestone("Milestone 2", LocalDate.now().plusDays(20), testProject);
        em.persist(milestone1);
        em.persist(milestone2);
        commitTransaction();

        // Count milestones
        long count = milestoneRepository.count();

        // Verify count
        assertEquals(2, count);
    }

    @Test
    public void testFindByProject() {
        // Create milestones for different projects
        beginTransaction();
        Milestone milestone1 = new Milestone("Project 1 Milestone 1", LocalDate.now().plusDays(10), testProject);
        Milestone milestone2 = new Milestone("Project 1 Milestone 2", LocalDate.now().plusDays(20), testProject);
        Milestone milestone3 = new Milestone("Project 2 Milestone", LocalDate.now().plusDays(15), secondProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        commitTransaction();

        // Find milestones for testProject
        List<Milestone> projectMilestones = milestoneRepository.findByProject(testProject);

        // Verify results
        assertNotNull(projectMilestones);
        assertEquals(2, projectMilestones.size());
        assertTrue(projectMilestones.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
    }

    @Test
    public void testFindByDateBefore() {
        // Create milestones with different dates
        LocalDate cutoffDate = LocalDate.now().plusDays(15);
        beginTransaction();
        Milestone milestone1 = new Milestone("Early Milestone", LocalDate.now().plusDays(5), testProject);
        Milestone milestone2 = new Milestone("Middle Milestone", LocalDate.now().plusDays(15), testProject);
        Milestone milestone3 = new Milestone("Late Milestone", LocalDate.now().plusDays(25), testProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        commitTransaction();

        // Find milestones before cutoff date
        List<Milestone> earlyMilestones = milestoneRepository.findByDateBefore(cutoffDate);

        // Verify results
        assertNotNull(earlyMilestones);
        assertEquals(1, earlyMilestones.size());
        assertEquals("Early Milestone", earlyMilestones.get(0).getName());
    }

    @Test
    public void testFindByDateAfter() {
        // Create milestones with different dates
        LocalDate cutoffDate = LocalDate.now().plusDays(15);
        beginTransaction();
        Milestone milestone1 = new Milestone("Early Milestone", LocalDate.now().plusDays(5), testProject);
        Milestone milestone2 = new Milestone("Middle Milestone", LocalDate.now().plusDays(15), testProject);
        Milestone milestone3 = new Milestone("Late Milestone", LocalDate.now().plusDays(25), testProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        commitTransaction();

        // Find milestones after cutoff date
        List<Milestone> lateMilestones = milestoneRepository.findByDateAfter(cutoffDate);

        // Verify results
        assertNotNull(lateMilestones);
        assertEquals(1, lateMilestones.size());
        assertEquals("Late Milestone", lateMilestones.get(0).getName());
    }

    @Test
    public void testFindByDateBetween() {
        // Create milestones with different dates
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(20);
        
        beginTransaction();
        Milestone milestone1 = new Milestone("Early Milestone", LocalDate.now().plusDays(5), testProject);
        Milestone milestone2 = new Milestone("Middle Milestone 1", LocalDate.now().plusDays(10), testProject);
        Milestone milestone3 = new Milestone("Middle Milestone 2", LocalDate.now().plusDays(20), testProject);
        Milestone milestone4 = new Milestone("Late Milestone", LocalDate.now().plusDays(25), testProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        em.persist(milestone4);
        commitTransaction();

        // Find milestones between dates
        List<Milestone> middleMilestones = milestoneRepository.findByDateBetween(startDate, endDate);

        // Verify results
        assertNotNull(middleMilestones);
        assertEquals(2, middleMilestones.size());
        assertTrue(middleMilestones.stream().allMatch(
                m -> m.getName().startsWith("Middle Milestone")));
    }

    @Test
    public void testFindByName() {
        // Create milestones with different names
        beginTransaction();
        Milestone milestone1 = new Milestone("Design Review", LocalDate.now().plusDays(10), testProject);
        Milestone milestone2 = new Milestone("Implementation Complete", LocalDate.now().plusDays(20), testProject);
        Milestone milestone3 = new Milestone("Design Verification", LocalDate.now().plusDays(30), secondProject);
        em.persist(milestone1);
        em.persist(milestone2);
        em.persist(milestone3);
        commitTransaction();

        // Find milestones with 'Design' in the name
        List<Milestone> designMilestones = milestoneRepository.findByName("Design");

        // Verify results
        assertNotNull(designMilestones);
        assertEquals(2, designMilestones.size());
        assertTrue(designMilestones.stream().allMatch(
                m -> m.getName().contains("Design")));
    }

    @Test
    public void testMilestoneHelperMethods() {
        // Create a milestone in the past
        LocalDate pastDate = LocalDate.now().minusDays(5);
        beginTransaction();
        Milestone pastMilestone = new Milestone("Past Milestone", pastDate, testProject);
        em.persist(pastMilestone);
        commitTransaction();

        // Test helper methods
        Optional<Milestone> found = milestoneRepository.findById(pastMilestone.getId());
        assertTrue(found.isPresent());
        
        Milestone milestone = found.get();
        assertTrue(milestone.isPassed());
        assertTrue(milestone.getDaysUntil() < 0);
        
        // Create a future milestone and test its helper methods
        LocalDate futureDate = LocalDate.now().plusDays(10);
        beginTransaction();
        Milestone futureMilestone = new Milestone("Future Milestone", futureDate, testProject);
        em.persist(futureMilestone);
        commitTransaction();
        
        found = milestoneRepository.findById(futureMilestone.getId());
        assertTrue(found.isPresent());
        
        milestone = found.get();
        assertFalse(milestone.isPassed());
        assertTrue(milestone.getDaysUntil() > 0);
    }
}