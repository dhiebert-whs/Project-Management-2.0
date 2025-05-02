package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.utils.TestEnvironmentSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TestEnvironmentSetup.class})
public class SubteamServiceTest {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamServiceTest.class.getName());
    
    private SubteamService service;
    
    @BeforeEach
    public void setUp() {
        // Force development mode for testing
        System.setProperty("app.db.dev", "true");
        
        // Initialize a clean database for each test
        DatabaseConfig.reinitialize(true);
        
        service = ServiceFactory.getSubteamService();
        
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
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            Subteam subteam1 = new Subteam("Test Service Subteam 1", "#FF0000");
            subteam1.setSpecialties("Java, Programming, Controls");
            
            Subteam subteam2 = new Subteam("Test Service Subteam 2", "#00FF00");
            subteam2.setSpecialties("CAD, Design, Fabrication");
            
            em.persist(subteam1);
            em.persist(subteam2);
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating test subteams: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create test subteams: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    private void cleanupTestSubteams() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Use a native query to avoid cache issues
            em.createQuery("DELETE FROM Subteam s WHERE s.name LIKE 'Test Service Subteam%'")
                .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error cleaning up test subteams: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testFindAll() {
        List<Subteam> subteams = service.findAll();
        assertNotNull(subteams);
        assertTrue(subteams.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a subteam ID from the DB
        List<Subteam> subteams = service.findAll();
        Subteam firstSubteam = subteams.stream()
            .filter(s -> s.getName().startsWith("Test Service Subteam"))
            .findFirst().orElseThrow(() -> new AssertionError("No test subteams found"));
        
        // Now test findById
        Subteam found = service.findById(firstSubteam.getId());
        assertNotNull(found);
        assertEquals(firstSubteam.getName(), found.getName());
    }
    
    @Test
    public void testFindByName() {
        Optional<Subteam> subteam = service.findByName("Test Service Subteam 1");
        assertTrue(subteam.isPresent());
        assertEquals("Test Service Subteam 1", subteam.get().getName());
        assertEquals("#FF0000", subteam.get().getColorCode());
    }
    
    @Test
    public void testFindBySpecialty() {
        List<Subteam> javaSubteams = service.findBySpecialty("Java");
        assertFalse(javaSubteams.isEmpty());
        assertTrue(javaSubteams.stream().anyMatch(s -> s.getSpecialties().contains("Java")));
        
        List<Subteam> cadSubteams = service.findBySpecialty("CAD");
        assertFalse(cadSubteams.isEmpty());
        assertTrue(cadSubteams.stream().anyMatch(s -> s.getSpecialties().contains("CAD")));
    }
    
    @Test
    public void testCreateSubteam() {
        Subteam created = service.createSubteam(
            "Test Create Service Subteam", 
            "#0000FF", 
            "Testing, Quality Assurance"
        );
        
        assertNotNull(created.getId());
        assertEquals("Test Create Service Subteam", created.getName());
        assertEquals("#0000FF", created.getColorCode());
        assertEquals("Testing, Quality Assurance", created.getSpecialties());
        
        // Verify it was saved
        Optional<Subteam> found = service.findByName("Test Create Service Subteam");
        assertTrue(found.isPresent());
        assertEquals("Test Create Service Subteam", found.get().getName());
    }
    
    @Test
    public void testUpdateSpecialties() {
        // First, create a subteam
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Subteam created = null;
        
        try {
            tx.begin();
            
            Subteam subteam = new Subteam("Test Update Subteam", "#0000FF");
            subteam.setSpecialties("Original Specialties");
            
            em.persist(subteam);
            tx.commit();
            
            created = subteam;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating subteam for update test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create subteam for update test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Now update it
        Subteam updated = service.updateSpecialties(
            created.getId(),
            "Updated Specialties"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("Updated Specialties", updated.getSpecialties());
        
        // Check in DB
        Subteam found = service.findById(updated.getId());
        assertNotNull(found);
        assertEquals("Updated Specialties", found.getSpecialties());
    }
    
    @Test
    public void testUpdateColorCode() {
        // First, create a subteam
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Subteam created = null;
        
        try {
            tx.begin();
            
            Subteam subteam = new Subteam("Test Color Update Subteam", "#0000FF");
            subteam.setSpecialties("Specialties");
            
            em.persist(subteam);
            tx.commit();
            
            created = subteam;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating subteam for color update test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create subteam for color update test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Now update it
        Subteam updated = service.updateColorCode(
            created.getId(),
            "#00FFFF"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("#00FFFF", updated.getColorCode());
        
        // Check in DB
        Subteam found = service.findById(updated.getId());
        assertNotNull(found);
        assertEquals("#00FFFF", found.getColorCode());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a subteam
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Subteam created = null;
        
        try {
            tx.begin();
            
            Subteam subteam = new Subteam("Test DeleteById Subteam", "#0000FF");
            subteam.setSpecialties("Specialties");
            
            em.persist(subteam);
            tx.commit();
            
            created = subteam;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating subteam for deleteById test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create subteam for deleteById test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        Long id = created.getId();
        
        // Now delete it by ID
        boolean result = service.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Subteam found = service.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testInvalidSubteamCreation() {
        // Test null name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createSubteam(
                null, 
                "#0000FF", 
                "Specialties"
            );
        });
        assertTrue(exception.getMessage().contains("name cannot be empty"));
        
        // Test invalid color code
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createSubteam(
                "Invalid Color Subteam", 
                "not-a-color", 
                "Specialties"
            );
        });
        assertTrue(exception.getMessage().contains("Color code must be a valid hex color code"));
        
        // Test duplicate name
        service.createSubteam("Duplicate Name Subteam", "#0000FF", "Specialties");
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createSubteam(
                "Duplicate Name Subteam", 
                "#00FF00", 
                "Other Specialties"
            );
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }
}