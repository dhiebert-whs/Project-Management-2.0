// src/test/java/org/frcpm/repositories/specific/SubteamRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.SubteamRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SubteamRepository implementation.
 */
public class SubteamRepositoryTest extends BaseRepositoryTest {
    
    private SubteamRepository subteamRepository;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        subteamRepository = new SubteamRepositoryImpl();
    }
    
    @Override
    protected void setupTestData() {
        // No data setup by default - tests will create their own data
    }
    
    @Test
    @DisplayName("Test saving a subteam and finding it by ID")
    public void testSaveAndFindById() {
        // Create a test subteam
        Subteam subteam = new Subteam();
        subteam.setName("Test Subteam");
        subteam.setColorCode("#FF5733");
        subteam.setSpecialties("Robotics, Programming");
        
        // Save the subteam
        Subteam savedSubteam = subteamRepository.save(subteam);
        
        // Verify that ID was generated
        assertNotNull(savedSubteam.getId(), "Subteam ID should not be null after saving");
        
        // Find the subteam by ID
        Optional<Subteam> foundSubteam = subteamRepository.findById(savedSubteam.getId());
        
        // Verify that the subteam was found
        assertTrue(foundSubteam.isPresent(), "Subteam should be found by ID");
        assertEquals("Test Subteam", foundSubteam.get().getName(), "Subteam name should match");
        assertEquals("#FF5733", foundSubteam.get().getColorCode(), "Subteam color code should match");
        assertEquals("Robotics, Programming", foundSubteam.get().getSpecialties(), "Subteam specialties should match");
    }
    
    @Test
    @DisplayName("Test finding all subteams")
    public void testFindAll() {
        // Create test subteams
        Subteam subteam1 = new Subteam();
        subteam1.setName("Subteam 1");
        subteam1.setColorCode("#FF5733");
        
        Subteam subteam2 = new Subteam();
        subteam2.setName("Subteam 2");
        subteam2.setColorCode("#33FF57");
        
        // Save subteams
        subteamRepository.save(subteam1);
        subteamRepository.save(subteam2);
        
        // Find all subteams
        List<Subteam> subteams = subteamRepository.findAll();
        
        // Verify all subteams were found
        assertTrue(subteams.size() >= 2, "There should be at least 2 subteams");
        assertTrue(subteams.stream().anyMatch(s -> s.getId().equals(subteam1.getId())), "Subteam 1 should be in the results");
        assertTrue(subteams.stream().anyMatch(s -> s.getId().equals(subteam2.getId())), "Subteam 2 should be in the results");
    }
    
    @Test
    @DisplayName("Test counting subteams")
    public void testCount() {
        // Get initial count
        long initialCount = subteamRepository.count();
        
        // Create test subteams
        Subteam subteam1 = new Subteam();
        subteam1.setName("Count Subteam 1");
        subteam1.setColorCode("#FF5733");
        
        Subteam subteam2 = new Subteam();
        subteam2.setName("Count Subteam 2");
        subteam2.setColorCode("#33FF57");
        
        // Save subteams
        subteamRepository.save(subteam1);
        subteamRepository.save(subteam2);
        
        // Verify updated count
        assertEquals(initialCount + 2, subteamRepository.count(), "Count should be increased by 2");
        
        // Delete a subteam
        subteamRepository.delete(subteam1);
        
        // Verify updated count after deletion
        assertEquals(initialCount + 1, subteamRepository.count(), "Count should be decreased by 1 after deletion");
    }
    
    @Test
    @DisplayName("Test updating a subteam")
    public void testUpdate() {
        // Create a test subteam
        Subteam subteam = new Subteam();
        subteam.setName("Original Name");
        subteam.setColorCode("#FF5733");
        subteam.setSpecialties("Original specialties");
        
        // Save the subteam
        Subteam savedSubteam = subteamRepository.save(subteam);
        
        // Update the subteam
        savedSubteam.setName("Updated Name");
        savedSubteam.setColorCode("#33FF57");
        savedSubteam.setSpecialties("Updated specialties");
        
        // Save the updated subteam
        Subteam updatedSubteam = subteamRepository.save(savedSubteam);
        
        // Find the subteam by ID
        Optional<Subteam> foundSubteam = subteamRepository.findById(updatedSubteam.getId());
        
        // Verify that the subteam was updated
        assertTrue(foundSubteam.isPresent(), "Subteam should still exist after update");
        assertEquals("Updated Name", foundSubteam.get().getName(), "Name should be updated");
        assertEquals("#33FF57", foundSubteam.get().getColorCode(), "Color code should be updated");
        assertEquals("Updated specialties", foundSubteam.get().getSpecialties(), "Specialties should be updated");
    }
    
    @Test
    @DisplayName("Test deleting a subteam")
    public void testDelete() {
        // Create a test subteam
        Subteam subteam = new Subteam();
        subteam.setName("To Delete");
        subteam.setColorCode("#FF5733");
        
        // Save the subteam
        Subteam savedSubteam = subteamRepository.save(subteam);
        
        // Verify that the subteam exists
        Optional<Subteam> foundBeforeDelete = subteamRepository.findById(savedSubteam.getId());
        assertTrue(foundBeforeDelete.isPresent(), "Subteam should exist before deletion");
        
        // Delete the subteam
        subteamRepository.delete(savedSubteam);
        
        // Verify that the subteam was deleted
        Optional<Subteam> foundAfterDelete = subteamRepository.findById(savedSubteam.getId());
        assertFalse(foundAfterDelete.isPresent(), "Subteam should not exist after deletion");
    }
    
    @Test
    @DisplayName("Test deleting a subteam by ID")
    public void testDeleteById() {
        // Create a test subteam
        Subteam subteam = new Subteam();
        subteam.setName("To Delete By ID");
        subteam.setColorCode("#FF5733");
        
        // Save the subteam
        Subteam savedSubteam = subteamRepository.save(subteam);
        
        // Verify that the subteam exists
        Optional<Subteam> foundBeforeDelete = subteamRepository.findById(savedSubteam.getId());
        assertTrue(foundBeforeDelete.isPresent(), "Subteam should exist before deletion");
        
        // Delete the subteam by ID
        boolean deleted = subteamRepository.deleteById(savedSubteam.getId());
        
        // Verify that deletion was successful
        assertTrue(deleted, "Deletion by ID should return true");
        
        // Verify that the subteam was deleted
        Optional<Subteam> foundAfterDelete = subteamRepository.findById(savedSubteam.getId());
        assertFalse(foundAfterDelete.isPresent(), "Subteam should not exist after deletion by ID");
    }
    
    @Test
    @DisplayName("Test finding subteams by name")
    public void testFindByName() {
        // Create test subteams with unique names
        Subteam subteam1 = new Subteam();
        subteam1.setName("Unique Subteam Name");
        subteam1.setColorCode("#FF5733");
        
        Subteam subteam2 = new Subteam();
        subteam2.setName("Different Subteam Name");
        subteam2.setColorCode("#33FF57");
        
        // Save subteams
        subteamRepository.save(subteam1);
        subteamRepository.save(subteam2);
        
        // Find subteam by exact name
        Optional<Subteam> foundSubteam = subteamRepository.findByName("Unique Subteam Name");
        
        // Verify that the correct subteam was found
        assertTrue(foundSubteam.isPresent(), "Subteam should be found by exact name");
        assertEquals("Unique Subteam Name", foundSubteam.get().getName(), "Found subteam should have the correct name");
        
        // Try finding with non-existent name
        Optional<Subteam> notFound = subteamRepository.findByName("Non-existent Name");
        
        // Verify that no subteam was found
        assertFalse(notFound.isPresent(), "Should not find subteam with non-existent name");
    }
    
    @Test
    @DisplayName("Test finding subteams by color code")
    public void testFindByColorCode() {
        // Create test subteams with different color codes
        Subteam subteam1 = new Subteam();
        subteam1.setName("Red Subteam");
        subteam1.setColorCode("#FF0000");
        
        Subteam subteam2 = new Subteam();
        subteam2.setName("Blue Subteam");
        subteam2.setColorCode("#0000FF");
        
        Subteam subteam3 = new Subteam();
        subteam3.setName("Red Subteam 2");
        subteam3.setColorCode("#FF0000");
        
        // Save subteams
        subteamRepository.save(subteam1);
        subteamRepository.save(subteam2);
        subteamRepository.save(subteam3);
        
        // Find subteams by color code
        List<Subteam> redSubteams = subteamRepository.findByColorCode("#FF0000");
        
        // Verify that only subteams with the specified color code were found
        assertEquals(2, redSubteams.size(), "Should find 2 subteams with red color code");
        assertTrue(redSubteams.stream().allMatch(s -> s.getColorCode().equals("#FF0000")), 
                "All found subteams should have red color code");
        
        // Find subteams by another color code
        List<Subteam> blueSubteams = subteamRepository.findByColorCode("#0000FF");
        
        // Verify that only subteams with the specified color code were found
        assertEquals(1, blueSubteams.size(), "Should find 1 subteam with blue color code");
        assertEquals("Blue Subteam", blueSubteams.get(0).getName(), "Should find the blue subteam");
    }
    
    @Test
    @DisplayName("Test finding subteams by specialty")
    public void testFindBySpecialty() {
        // Create test subteams with different specialties
        Subteam subteam1 = new Subteam();
        subteam1.setName("Programming Subteam");
        subteam1.setColorCode("#FF5733");
        subteam1.setSpecialties("Java, Python, Robotics Programming");
        
        Subteam subteam2 = new Subteam();
        subteam2.setName("Mechanical Subteam");
        subteam2.setColorCode("#33FF57");
        subteam2.setSpecialties("CAD, Mechanical Design, 3D Printing");
        
        Subteam subteam3 = new Subteam();
        subteam3.setName("Design Subteam");
        subteam3.setColorCode("#5733FF");
        subteam3.setSpecialties("UI Design, Graphic Design, 3D Printing");
        
        // Save subteams
        subteamRepository.save(subteam1);
        subteamRepository.save(subteam2);
        subteamRepository.save(subteam3);
        
        // Find subteams by specialty
        List<Subteam> programmingSubteams = subteamRepository.findBySpecialty("Programming");
        
        // Verify that only subteams with the specified specialty were found
        assertEquals(1, programmingSubteams.size(), "Should find 1 subteam with Programming specialty");
        assertEquals("Programming Subteam", programmingSubteams.get(0).getName(), "Should find the Programming subteam");
        
        // Find subteams by another specialty
        List<Subteam> printingSubteams = subteamRepository.findBySpecialty("3D Printing");
        
        // Verify that only subteams with the specified specialty were found
        assertEquals(2, printingSubteams.size(), "Should find 2 subteams with 3D Printing specialty");
        assertTrue(printingSubteams.stream().anyMatch(s -> s.getName().equals("Mechanical Subteam")), 
                "Should find the Mechanical subteam");
        assertTrue(printingSubteams.stream().anyMatch(s -> s.getName().equals("Design Subteam")), 
                "Should find the Design subteam");
    }
}