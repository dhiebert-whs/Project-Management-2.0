package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
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
public class SubteamRepositoryTest {
    
    private SubteamRepository repository;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getSubteamRepository();
        
        // Add test data
        createTestSubteams();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestSubteams();
        DatabaseConfig.shutdown();
    }
    
    private void createTestSubteams() {
        Subteam subteam1 = new Subteam("Test Subteam 1", "#FF0000");
        subteam1.setSpecialties("Java, Programming, Controls");
        
        Subteam subteam2 = new Subteam("Test Subteam 2", "#00FF00");
        subteam2.setSpecialties("CAD, Design, Fabrication");
        
        repository.save(subteam1);
        repository.save(subteam2);
    }
    
    private void cleanupTestSubteams() {
        List<Subteam> subteams = repository.findAll();
        for (Subteam subteam : subteams) {
            if (subteam.getName().startsWith("Test Subteam")) {
                repository.delete(subteam);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Subteam> subteams = repository.findAll();
        assertNotNull(subteams);
        assertTrue(subteams.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a subteam ID from the DB
        List<Subteam> subteams = repository.findAll();
        Subteam firstSubteam = subteams.stream()
            .filter(s -> s.getName().startsWith("Test Subteam"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Subteam> found = repository.findById(firstSubteam.getId());
        assertTrue(found.isPresent());
        assertEquals(firstSubteam.getName(), found.get().getName());
    }
    
    @Test
    public void testFindByName() {
        Optional<Subteam> subteam = repository.findByName("Test Subteam 1");
        assertTrue(subteam.isPresent());
        assertEquals("Test Subteam 1", subteam.get().getName());
        assertEquals("#FF0000", subteam.get().getColorCode());
    }
    
    @Test
    public void testFindByColorCode() {
        List<Subteam> subteams = repository.findByColorCode("#FF0000");
        assertFalse(subteams.isEmpty());
        assertEquals("#FF0000", subteams.get(0).getColorCode());
    }
    
    @Test
    public void testFindBySpecialty() {
        List<Subteam> javaSubteams = repository.findBySpecialty("Java");
        assertFalse(javaSubteams.isEmpty());
        assertTrue(javaSubteams.stream().anyMatch(s -> s.getSpecialties().contains("Java")));
        
        List<Subteam> cadSubteams = repository.findBySpecialty("CAD");
        assertFalse(cadSubteams.isEmpty());
        assertTrue(cadSubteams.stream().anyMatch(s -> s.getSpecialties().contains("CAD")));
    }
    
    @Test
    public void testSave() {
        Subteam newSubteam = new Subteam("Test Save Subteam", "#0000FF");
        newSubteam.setSpecialties("Testing");
        
        Subteam saved = repository.save(newSubteam);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Subteam> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Subteam", found.get().getName());
    }
    
    @Test
    public void testUpdate() {
        // First, create a subteam
        Subteam subteam = new Subteam("Test Update Subteam", "#0000FF");
        Subteam saved = repository.save(subteam);
        
        // Now update it
        saved.setName("Updated Subteam Name");
        saved.setColorCode("#00FFFF");
        Subteam updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Subteam Name", updated.getName());
        assertEquals("#00FFFF", updated.getColorCode());
        
        // Check in DB
        Optional<Subteam> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Subteam Name", found.get().getName());
        assertEquals("#00FFFF", found.get().getColorCode());
    }
    
    @Test
    public void testDelete() {
        // First, create a subteam
        Subteam subteam = new Subteam("Test Delete Subteam", "#0000FF");
        Subteam saved = repository.save(subteam);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Subteam> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a subteam
        Subteam subteam = new Subteam("Test DeleteById Subteam", "#0000FF");
        Subteam saved = repository.save(subteam);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Subteam> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new subteam
        Subteam subteam = new Subteam("Test Count Subteam", "#0000FF");
        repository.save(subteam);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}