package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubsystemRepositoryTest {
    
    private SubsystemRepository repository;
    private SubteamRepository subteamRepository;
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getSubsystemRepository();
        subteamRepository = RepositoryFactory.getSubteamRepository();
        
        // Create test subteam
        testSubteam = new Subteam("Test Subsystem Team", "#FF0000");
        testSubteam = subteamRepository.save(testSubteam);
        
        // Add test data
        createTestSubsystems();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestSubsystems();
        subteamRepository.delete(testSubteam);
        DatabaseConfig.shutdown();
    }
    
    private void createTestSubsystems() {
        Subsystem subsystem1 = new Subsystem("Test Subsystem 1");
        subsystem1.setDescription("First test subsystem description");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem1.setResponsibleSubteam(testSubteam);
        
        Subsystem subsystem2 = new Subsystem("Test Subsystem 2");
        subsystem2.setDescription("Second test subsystem description");
        subsystem2.setStatus(Subsystem.Status.IN_PROGRESS);
        
        Subsystem subsystem3 = new Subsystem("Test Subsystem 3");
        subsystem3.setDescription("Third test subsystem description");
        subsystem3.setStatus(Subsystem.Status.COMPLETED);
        subsystem3.setResponsibleSubteam(testSubteam);
        
        repository.save(subsystem1);
        repository.save(subsystem2);
        repository.save(subsystem3);
    }
    
    private void cleanupTestSubsystems() {
        List<Subsystem> subsystems = repository.findAll();
        for (Subsystem subsystem : subsystems) {
            if (subsystem.getName().startsWith("Test Subsystem")) {
                repository.delete(subsystem);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Subsystem> subsystems = repository.findAll();
        assertNotNull(subsystems);
        assertTrue(subsystems.size() >= 3);
    }
    
    @Test
    public void testFindById() {
        // First, get a subsystem ID from the DB
        List<Subsystem> subsystems = repository.findAll();
        Subsystem firstSubsystem = subsystems.stream()
            .filter(s -> s.getName().startsWith("Test Subsystem"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Subsystem> found = repository.findById(firstSubsystem.getId());
        assertTrue(found.isPresent());
        assertEquals(firstSubsystem.getName(), found.get().getName());
    }
    
    @Test
    public void testFindByName() {
        Optional<Subsystem> subsystem = repository.findByName("Test Subsystem 1");
        assertTrue(subsystem.isPresent());
        assertEquals("Test Subsystem 1", subsystem.get().getName());
        assertEquals(Subsystem.Status.NOT_STARTED, subsystem.get().getStatus());
    }
    
    @Test
    public void testFindByStatus() {
        List<Subsystem> notStartedSubsystems = repository.findByStatus(Subsystem.Status.NOT_STARTED);
        assertFalse(notStartedSubsystems.isEmpty());
        assertTrue(notStartedSubsystems.stream().allMatch(s -> s.getStatus() == Subsystem.Status.NOT_STARTED));
        assertTrue(notStartedSubsystems.stream().anyMatch(s -> s.getName().equals("Test Subsystem 1")));
        
        List<Subsystem> inProgressSubsystems = repository.findByStatus(Subsystem.Status.IN_PROGRESS);
        assertFalse(inProgressSubsystems.isEmpty());
        assertTrue(inProgressSubsystems.stream().allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS));
        assertTrue(inProgressSubsystems.stream().anyMatch(s -> s.getName().equals("Test Subsystem 2")));
        
        List<Subsystem> completedSubsystems = repository.findByStatus(Subsystem.Status.COMPLETED);
        assertFalse(completedSubsystems.isEmpty());
        assertTrue(completedSubsystems.stream().allMatch(s -> s.getStatus() == Subsystem.Status.COMPLETED));
        assertTrue(completedSubsystems.stream().anyMatch(s -> s.getName().equals("Test Subsystem 3")));
    }
    
    @Test
    public void testFindByResponsibleSubteam() {
        List<Subsystem> subsystems = repository.findByResponsibleSubteam(testSubteam);
        assertFalse(subsystems.isEmpty());
        assertTrue(subsystems.stream().allMatch(s -> 
            s.getResponsibleSubteam() != null && 
            s.getResponsibleSubteam().getId().equals(testSubteam.getId())));
        assertEquals(2, subsystems.size());
        assertTrue(subsystems.stream().anyMatch(s -> s.getName().equals("Test Subsystem 1")));
        assertTrue(subsystems.stream().anyMatch(s -> s.getName().equals("Test Subsystem 3")));
    }
    
    @Test
    public void testSave() {
        Subsystem newSubsystem = new Subsystem("Test Save Subsystem");
        newSubsystem.setDescription("Save subsystem description");
        newSubsystem.setStatus(Subsystem.Status.TESTING);
        
        Subsystem saved = repository.save(newSubsystem);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Subsystem> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Subsystem", found.get().getName());
        assertEquals(Subsystem.Status.TESTING, found.get().getStatus());
    }
    
    @Test
    public void testUpdate() {
        // First, create a subsystem
        Subsystem subsystem = new Subsystem("Test Update Subsystem");
        Subsystem saved = repository.save(subsystem);
        
        // Now update it
        saved.setName("Updated Subsystem Name");
        saved.setDescription("Updated description");
        saved.setStatus(Subsystem.Status.ISSUES);
        Subsystem updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Subsystem Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(Subsystem.Status.ISSUES, updated.getStatus());
        
        // Check in DB
        Optional<Subsystem> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Subsystem Name", found.get().getName());
        assertEquals("Updated description", found.get().getDescription());
        assertEquals(Subsystem.Status.ISSUES, found.get().getStatus());
    }
    
    @Test
    public void testDelete() {
        // First, create a subsystem
        Subsystem subsystem = new Subsystem("Test Delete Subsystem");
        Subsystem saved = repository.save(subsystem);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Subsystem> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a subsystem
        Subsystem subsystem = new Subsystem("Test DeleteById Subsystem");
        Subsystem saved = repository.save(subsystem);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Subsystem> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new subsystem
        Subsystem subsystem = new Subsystem("Test Count Subsystem");
        repository.save(subsystem);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}
