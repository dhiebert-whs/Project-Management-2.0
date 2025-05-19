// src/test/java/org/frcpm/repositories/specific/SubsystemRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.SubsystemRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SubsystemRepository implementation.
 */
public class SubsystemRepositoryTest extends BaseRepositoryTest {
    
    private SubsystemRepository subsystemRepository;
    private Project testProject;
    private Subteam testSubteam;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        subsystemRepository = new SubsystemRepositoryImpl();
        
        // Create test data
        createTestEntities();
    }
    
    @Override
    protected void setupTestData() {
        // No data setup by default - tests will create their own data
    }
    
    private void createTestEntities() {
        // Create and save a test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now());
        testProject.setGoalEndDate(LocalDate.now().plusDays(30));
        testProject.setHardDeadline(LocalDate.now().plusDays(28));
        testProject.setDescription("Test project notes");
        
        // Create a test subteam
        testSubteam = new Subteam();
        testSubteam.setName("Test Subteam");
        testSubteam.setColorCode("#FF5733"); // Set the required colorCode
        testSubteam.setSpecialties("Robotics, Programming");
        
        beginTransaction();
        em.persist(testProject);
        em.persist(testSubteam);
        commitTransaction();
    }
    
    @Test
    @DisplayName("Test saving a subsystem and finding it by ID")
    public void testSaveAndFindById() {
        // Create a test subsystem
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Test Subsystem");
        subsystem.setDescription("Test description");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem.setResponsibleSubteam(testSubteam);
        
        // Save the subsystem
        Subsystem savedSubsystem = subsystemRepository.save(subsystem);
        
        // Verify that ID was generated
        assertNotNull(savedSubsystem.getId(), "Subsystem ID should not be null after saving");
        
        // Find the subsystem by ID
        Optional<Subsystem> foundSubsystem = subsystemRepository.findById(savedSubsystem.getId());
        
        // Verify that the subsystem was found
        assertTrue(foundSubsystem.isPresent(), "Subsystem should be found by ID");
        assertEquals("Test Subsystem", foundSubsystem.get().getName(), "Subsystem name should match");
        assertEquals("Test description", foundSubsystem.get().getDescription(), "Subsystem description should match");
        assertEquals(Subsystem.Status.NOT_STARTED, foundSubsystem.get().getStatus(), "Subsystem status should match");
        assertEquals(testSubteam.getId(), foundSubsystem.get().getResponsibleSubteam().getId(), "Responsible subteam should match");
    }
    
    @Test
    @DisplayName("Test finding all subsystems")
    public void testFindAll() {
        // Create test subsystems
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setName("Subsystem 1");
        subsystem1.setDescription("Description 1");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setName("Subsystem 2");
        subsystem2.setDescription("Description 2");
        subsystem2.setStatus(Subsystem.Status.IN_PROGRESS);
        
        // Save subsystems
        subsystemRepository.save(subsystem1);
        subsystemRepository.save(subsystem2);
        
        // Find all subsystems
        List<Subsystem> subsystems = subsystemRepository.findAll();
        
        // Verify all subsystems were found
        assertTrue(subsystems.size() >= 2, "There should be at least 2 subsystems");
        assertTrue(subsystems.stream().anyMatch(s -> s.getId().equals(subsystem1.getId())), "Subsystem 1 should be in the results");
        assertTrue(subsystems.stream().anyMatch(s -> s.getId().equals(subsystem2.getId())), "Subsystem 2 should be in the results");
    }
    
    @Test
    @DisplayName("Test counting subsystems")
    public void testCount() {
        // Get initial count
        long initialCount = subsystemRepository.count();
        
        // Create test subsystems
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setName("Count Subsystem 1");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setName("Count Subsystem 2");
        subsystem2.setStatus(Subsystem.Status.IN_PROGRESS);
        
        // Save subsystems
        subsystemRepository.save(subsystem1);
        subsystemRepository.save(subsystem2);
        
        // Verify updated count
        assertEquals(initialCount + 2, subsystemRepository.count(), "Count should be increased by 2");
        
        // Delete a subsystem
        subsystemRepository.delete(subsystem1);
        
        // Verify updated count after deletion
        assertEquals(initialCount + 1, subsystemRepository.count(), "Count should be decreased by 1 after deletion");
    }
    
    @Test
    @DisplayName("Test updating a subsystem")
    public void testUpdate() {
        // Create a test subsystem
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Original Name");
        subsystem.setDescription("Original description");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Save the subsystem
        Subsystem savedSubsystem = subsystemRepository.save(subsystem);
        
        // Update the subsystem
        savedSubsystem.setName("Updated Name");
        savedSubsystem.setDescription("Updated description");
        savedSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        
        // Save the updated subsystem
        Subsystem updatedSubsystem = subsystemRepository.save(savedSubsystem);
        
        // Find the subsystem by ID
        Optional<Subsystem> foundSubsystem = subsystemRepository.findById(updatedSubsystem.getId());
        
        // Verify that the subsystem was updated
        assertTrue(foundSubsystem.isPresent(), "Subsystem should still exist after update");
        assertEquals("Updated Name", foundSubsystem.get().getName(), "Name should be updated");
        assertEquals("Updated description", foundSubsystem.get().getDescription(), "Description should be updated");
        assertEquals(Subsystem.Status.IN_PROGRESS, foundSubsystem.get().getStatus(), "Status should be updated");
    }
    
    @Test
    @DisplayName("Test deleting a subsystem")
    public void testDelete() {
        // Create a test subsystem
        Subsystem subsystem = new Subsystem();
        subsystem.setName("To Delete");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Save the subsystem
        Subsystem savedSubsystem = subsystemRepository.save(subsystem);
        
        // Verify that the subsystem exists
        Optional<Subsystem> foundBeforeDelete = subsystemRepository.findById(savedSubsystem.getId());
        assertTrue(foundBeforeDelete.isPresent(), "Subsystem should exist before deletion");
        
        // Delete the subsystem
        subsystemRepository.delete(savedSubsystem);
        
        // Verify that the subsystem was deleted
        Optional<Subsystem> foundAfterDelete = subsystemRepository.findById(savedSubsystem.getId());
        assertFalse(foundAfterDelete.isPresent(), "Subsystem should not exist after deletion");
    }
    
    @Test
    @DisplayName("Test deleting a subsystem by ID")
    public void testDeleteById() {
        // Create a test subsystem
        Subsystem subsystem = new Subsystem();
        subsystem.setName("To Delete By ID");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Save the subsystem
        Subsystem savedSubsystem = subsystemRepository.save(subsystem);
        
        // Verify that the subsystem exists
        Optional<Subsystem> foundBeforeDelete = subsystemRepository.findById(savedSubsystem.getId());
        assertTrue(foundBeforeDelete.isPresent(), "Subsystem should exist before deletion");
        
        // Delete the subsystem by ID
        boolean deleted = subsystemRepository.deleteById(savedSubsystem.getId());
        
        // Verify that deletion was successful
        assertTrue(deleted, "Deletion by ID should return true");
        
        // Verify that the subsystem was deleted
        Optional<Subsystem> foundAfterDelete = subsystemRepository.findById(savedSubsystem.getId());
        assertFalse(foundAfterDelete.isPresent(), "Subsystem should not exist after deletion by ID");
    }
    
    @Test
    @DisplayName("Test finding subsystems by status")
    public void testFindByStatus() {
        // Create subsystems with different statuses
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setName("Status Subsystem 1");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setName("Status Subsystem 2");
        subsystem2.setStatus(Subsystem.Status.IN_PROGRESS);
        
        Subsystem subsystem3 = new Subsystem();
        subsystem3.setName("Status Subsystem 3");
        subsystem3.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Save subsystems
        subsystemRepository.save(subsystem1);
        subsystemRepository.save(subsystem2);
        subsystemRepository.save(subsystem3);
        
        // Find subsystems by status
        List<Subsystem> notStartedSubsystems = subsystemRepository.findByStatus(Subsystem.Status.NOT_STARTED);
        
        // Verify that only subsystems with the correct status were found
        assertFalse(notStartedSubsystems.isEmpty(), "Should find subsystems with NOT_STARTED status");
        assertTrue(notStartedSubsystems.size() >= 2, "Should find at least 2 subsystems with NOT_STARTED status");
        assertTrue(notStartedSubsystems.stream().allMatch(s -> s.getStatus() == Subsystem.Status.NOT_STARTED), 
                "All found subsystems should have NOT_STARTED status");
        
        // Find subsystems by another status
        List<Subsystem> inProgressSubsystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        
        // Verify that only subsystems with the correct status were found
        assertFalse(inProgressSubsystems.isEmpty(), "Should find subsystems with IN_PROGRESS status");
        assertTrue(inProgressSubsystems.stream().allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS), 
                "All found subsystems should have IN_PROGRESS status");
    }
    
    @Test
    @DisplayName("Test finding subsystems by responsible subteam")
    public void testFindByResponsibleSubteam() {
        // Create a second subteam
        Subteam anotherSubteam = new Subteam();
        anotherSubteam.setName("Another Subteam");
        anotherSubteam.setColorCode("#33FF57");
        
        beginTransaction();
        em.persist(anotherSubteam);
        commitTransaction();
        
        // Create subsystems with different responsible subteams
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setName("Subteam Subsystem 1");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem1.setResponsibleSubteam(testSubteam);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setName("Subteam Subsystem 2");
        subsystem2.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem2.setResponsibleSubteam(testSubteam);
        
        Subsystem subsystem3 = new Subsystem();
        subsystem3.setName("Subteam Subsystem 3");
        subsystem3.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem3.setResponsibleSubteam(anotherSubteam);
        
        // Save subsystems
        subsystemRepository.save(subsystem1);
        subsystemRepository.save(subsystem2);
        subsystemRepository.save(subsystem3);
        
        // Find subsystems by responsible subteam
        List<Subsystem> testSubteamSubsystems = subsystemRepository.findByResponsibleSubteam(testSubteam);
        
        // Verify that only subsystems with the specified subteam were found
        assertTrue(testSubteamSubsystems.size() >= 2, "Should find at least 2 subsystems for test subteam");
        assertTrue(testSubteamSubsystems.stream().allMatch(
                s -> s.getResponsibleSubteam() != null && s.getResponsibleSubteam().getId().equals(testSubteam.getId())), 
                "All found subsystems should have the test subteam as responsible");
        
        // Find subsystems by another subteam
        List<Subsystem> anotherSubteamSubsystems = subsystemRepository.findByResponsibleSubteam(anotherSubteam);
        
        // Verify that only subsystems with the specified subteam were found
        assertFalse(anotherSubteamSubsystems.isEmpty(), "Should find subsystems for another subteam");
        assertTrue(anotherSubteamSubsystems.stream().allMatch(
                s -> s.getResponsibleSubteam() != null && s.getResponsibleSubteam().getId().equals(anotherSubteam.getId())), 
                "All found subsystems should have another subteam as responsible");
    }
    
    @Test
    @DisplayName("Test finding subsystems by name")
    public void testFindByName() {
        // Create test subsystems with unique names
        Subsystem subsystem1 = new Subsystem();
        subsystem1.setName("Unique Subsystem Name");
        subsystem1.setStatus(Subsystem.Status.NOT_STARTED);
        
        Subsystem subsystem2 = new Subsystem();
        subsystem2.setName("Different Subsystem Name");
        subsystem2.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Save subsystems
        subsystemRepository.save(subsystem1);
        subsystemRepository.save(subsystem2);
        
        // Find subsystem by exact name
        Optional<Subsystem> foundSubsystem = subsystemRepository.findByName("Unique Subsystem Name");
        
        // Verify that the correct subsystem was found
        assertTrue(foundSubsystem.isPresent(), "Subsystem should be found by exact name");
        assertEquals("Unique Subsystem Name", foundSubsystem.get().getName(), "Found subsystem should have the correct name");
        
        // Try finding with non-existent name
        Optional<Subsystem> notFound = subsystemRepository.findByName("Non-existent Name");
        
        // Verify that no subsystem was found
        assertFalse(notFound.isPresent(), "Should not find subsystem with non-existent name");
    }
}