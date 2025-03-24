package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MilestoneRepositoryTest {
    
    private MilestoneRepository repository;
    private ProjectRepository projectRepository;
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getMilestoneRepository();
        projectRepository = RepositoryFactory.getProjectRepository();
        
        // Create test project
        testProject = new Project(
            "Test Milestone Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        testProject = projectRepository.save(testProject);
        
        // Add test data
        createTestMilestones();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestMilestones();
        projectRepository.delete(testProject);
        DatabaseConfig.shutdown();
    }
    
    private void createTestMilestones() {
        Milestone milestone1 = new Milestone("Test Milestone 1", LocalDate.now().plusWeeks(2), testProject);
        milestone1.setDescription("First milestone description");
        
        Milestone milestone2 = new Milestone("Test Milestone 2", LocalDate.now().plusWeeks(4), testProject);
        milestone2.setDescription("Second milestone description");
        
        Milestone milestone3 = new Milestone("Test Milestone 3", LocalDate.now().plusWeeks(6), testProject);
        milestone3.setDescription("Third milestone description");
        
        repository.save(milestone1);
        repository.save(milestone2);
        repository.save(milestone3);
    }
    
    private void cleanupTestMilestones() {
        List<Milestone> milestones = repository.findByProject(testProject);
        for (Milestone milestone : milestones) {
            repository.delete(milestone);
        }
    }
    
    @Test
    public void testFindAll() {
        List<Milestone> milestones = repository.findAll();
        assertNotNull(milestones);
        assertTrue(milestones.size() >= 3);
    }
    
    @Test
    public void testFindById() {
        // First, get a milestone ID from the DB
        List<Milestone> milestones = repository.findByProject(testProject);
        Milestone firstMilestone = milestones.stream()
            .filter(m -> m.getName().equals("Test Milestone 1"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Milestone> found = repository.findById(firstMilestone.getId());
        assertTrue(found.isPresent());
        assertEquals(firstMilestone.getName(), found.get().getName());
    }
    
    @Test
    public void testFindByProject() {
        List<Milestone> milestones = repository.findByProject(testProject);
        assertFalse(milestones.isEmpty());
        assertTrue(milestones.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
        assertEquals(3, milestones.size());
    }
    
    @Test
    public void testFindByDateBefore() {
        LocalDate cutoffDate = LocalDate.now().plusWeeks(5);
        List<Milestone> milestones = repository.findByDateBefore(cutoffDate);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(milestone.getDate().isBefore(cutoffDate));
        }
        
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 1")));
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 2")));
    }
    
    @Test
    public void testFindByDateAfter() {
        LocalDate cutoffDate = LocalDate.now().plusWeeks(3);
        List<Milestone> milestones = repository.findByDateAfter(cutoffDate);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(milestone.getDate().isAfter(cutoffDate));
        }
        
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 2")));
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 3")));
    }
    
    @Test
    public void testFindByDateBetween() {
        LocalDate startDate = LocalDate.now().plusWeeks(1);
        LocalDate endDate = LocalDate.now().plusWeeks(5);
        
        List<Milestone> milestones = repository.findByDateBetween(startDate, endDate);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(!milestone.getDate().isBefore(startDate) && !milestone.getDate().isAfter(endDate));
        }
        
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 1")));
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 2")));
    }
    
    @Test
    public void testFindByName() {
        List<Milestone> milestones = repository.findByName("Milestone 1");
        assertFalse(milestones.isEmpty());
        assertTrue(milestones.stream().anyMatch(m -> m.getName().equals("Test Milestone 1")));
    }
    
    @Test
    public void testSave() {
        Milestone newMilestone = new Milestone("Test Save Milestone", LocalDate.now().plusWeeks(7), testProject);
        newMilestone.setDescription("Save milestone description");
        
        Milestone saved = repository.save(newMilestone);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Milestone> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Milestone", found.get().getName());
    }
    
    @Test
    public void testUpdate() {
        // First, create a milestone
        Milestone milestone = new Milestone("Test Update Milestone", LocalDate.now().plusWeeks(5), testProject);
        Milestone saved = repository.save(milestone);
        
        // Now update it
        saved.setName("Updated Milestone Name");
        saved.setDescription("Updated description");
        Milestone updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Milestone Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        
        // Check in DB
        Optional<Milestone> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Milestone Name", found.get().getName());
        assertEquals("Updated description", found.get().getDescription());
    }
    
    @Test
    public void testDelete() {
        // First, create a milestone
        Milestone milestone = new Milestone("Test Delete Milestone", LocalDate.now().plusWeeks(5), testProject);
        Milestone saved = repository.save(milestone);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Milestone> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a milestone
        Milestone milestone = new Milestone("Test DeleteById Milestone", LocalDate.now().plusWeeks(5), testProject);
        Milestone saved = repository.save(milestone);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Milestone> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new milestone
        Milestone milestone = new Milestone("Test Count Milestone", LocalDate.now().plusWeeks(5), testProject);
        repository.save(milestone);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}