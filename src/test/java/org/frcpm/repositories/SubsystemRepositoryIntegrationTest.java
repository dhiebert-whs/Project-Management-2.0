// src/test/java/org/frcpm/repositories/SubsystemRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.spring.SubsystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for SubsystemRepository using Spring Boot @SpringBootTest.
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
class SubsystemRepositoryIntegrationTest {
    
    @Autowired
    private SubsystemRepository subsystemRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private Subsystem testSubsystem;
    private Subsystem drivetrainSubsystem;
    private Subsystem intakeSubsystem;
    private Subteam testSubteam;
    private Subteam mechanicalSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testSubteam = createTestSubteam();
        mechanicalSubteam = createMechanicalSubteam();
        testSubsystem = createTestSubsystem();
        drivetrainSubsystem = createDrivetrainSubsystem();
        intakeSubsystem = createIntakeSubsystem();
    }
    
    /**
     * Creates a test subteam for use in tests.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setName("Programming Team");
        subteam.setColorCode("#007ACC");
        subteam.setSpecialties("Java, Python, Controls");
        return subteam;
    }
    
    /**
     * Creates another subteam for multi-subteam tests.
     */
    private Subteam createMechanicalSubteam() {
        Subteam subteam = new Subteam();
        subteam.setName("Mechanical Team");
        subteam.setColorCode("#FF6B35");
        subteam.setSpecialties("CAD, Machining, Assembly");
        return subteam;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Test Subsystem");
        subsystem.setDescription("A test subsystem for unit testing");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        return subsystem;
    }
    
    /**
     * Creates a drivetrain subsystem for complex tests.
     */
    private Subsystem createDrivetrainSubsystem() {
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Drivetrain");
        subsystem.setDescription("Main robot locomotion system with swerve drive");
        subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        return subsystem;
    }
    
    /**
     * Creates an intake subsystem for status tests.
     */
    private Subsystem createIntakeSubsystem() {
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Intake");
        subsystem.setDescription("Game piece intake mechanism");
        subsystem.setStatus(Subsystem.Status.COMPLETED);
        return subsystem;
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
        // Setup - Persist responsible subteam first
        Subteam savedSubteam = persistAndFlush(testSubteam);
        testSubsystem.setResponsibleSubteam(savedSubteam);
        
        // Execute - Save subsystem
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify save
        assertThat(savedSubsystem.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Subsystem> found = subsystemRepository.findById(savedSubsystem.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Subsystem");
        assertThat(found.get().getDescription()).isEqualTo("A test subsystem for unit testing");
        assertThat(found.get().getStatus()).isEqualTo(Subsystem.Status.NOT_STARTED);
        assertThat(found.get().getResponsibleSubteam().getId()).isEqualTo(savedSubteam.getId());
    }
    
    @Test
    void testFindAll() {
        // Setup - Persist multiple subsystems
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute - Find all
        List<Subsystem> allSubsystems = subsystemRepository.findAll();
        
        // Verify
        assertThat(allSubsystems).hasSize(3);
        assertThat(allSubsystems).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Test Subsystem", "Drivetrain", "Intake");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Persist subsystem
        Subsystem savedSubsystem = persistAndFlush(testSubsystem);
        
        // Verify exists before deletion
        assertThat(subsystemRepository.existsById(savedSubsystem.getId())).isTrue();
        
        // Execute - Delete
        subsystemRepository.deleteById(savedSubsystem.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(subsystemRepository.existsById(savedSubsystem.getId())).isFalse();
        assertThat(subsystemRepository.findById(savedSubsystem.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(subsystemRepository.count()).isEqualTo(0);
        
        // Setup - Persist subsystems
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute and verify
        assertThat(subsystemRepository.count()).isEqualTo(2);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByName() {
        // Setup - Persist subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute
        Optional<Subsystem> result = subsystemRepository.findByName("Drivetrain");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Drivetrain");
        assertThat(result.get().getDescription()).isEqualTo("Main robot locomotion system with swerve drive");
    }
    
    @Test
    void testFindByName_NotFound() {
        // Setup - Persist a different subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute - Search for non-existent name
        Optional<Subsystem> result = subsystemRepository.findByName("NonExistent");
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindByNameIgnoreCase() {
        // Setup - Persist subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute - Case insensitive search
        Optional<Subsystem> result = subsystemRepository.findByNameIgnoreCase("DRIVETRAIN");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Drivetrain");
        
        // Execute - Mixed case search
        Optional<Subsystem> mixedResult = subsystemRepository.findByNameIgnoreCase("DrIvEtRaIn");
        
        // Verify
        assertThat(mixedResult).isPresent();
        assertThat(mixedResult.get().getName()).isEqualTo("Drivetrain");
    }
    
    @Test
    void testFindByStatus() {
        // Setup - Persist subsystems with different statuses
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        drivetrainSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        intakeSubsystem.setStatus(Subsystem.Status.COMPLETED);
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute - Find by different statuses
        List<Subsystem> notStartedSystems = subsystemRepository.findByStatus(Subsystem.Status.NOT_STARTED);
        List<Subsystem> inProgressSystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        List<Subsystem> completedSystems = subsystemRepository.findByStatus(Subsystem.Status.COMPLETED);
        
        // Verify
        assertThat(notStartedSystems).hasSize(1);
        assertThat(notStartedSystems.get(0).getName()).isEqualTo("Test Subsystem");
        
        assertThat(inProgressSystems).hasSize(1);
        assertThat(inProgressSystems.get(0).getName()).isEqualTo("Drivetrain");
        
        assertThat(completedSystems).hasSize(1);
        assertThat(completedSystems.get(0).getName()).isEqualTo("Intake");
    }
    
    @Test
    void testFindByResponsibleSubteam() {
        // Setup - Persist subteam and subsystems
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        testSubsystem.setResponsibleSubteam(savedSubteam);
        drivetrainSubsystem.setResponsibleSubteam(savedSubteam);
        intakeSubsystem.setResponsibleSubteam(null); // No responsible subteam
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findByResponsibleSubteam(savedSubteam);
        
        // Verify
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Test Subsystem", "Drivetrain");
        assertThat(results).allMatch(s -> s.getResponsibleSubteam().getId().equals(savedSubteam.getId()));
    }
    
    @Test
    void testFindByResponsibleSubteamIsNull() {
        // Setup - Create subsystems with and without responsible subteams
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        testSubsystem.setResponsibleSubteam(savedSubteam);      // Has subteam
        drivetrainSubsystem.setResponsibleSubteam(null);        // No subteam
        intakeSubsystem.setResponsibleSubteam(null);            // No subteam
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findByResponsibleSubteamIsNull();
        
        // Verify - Should only find subsystems without responsible subteam
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Drivetrain", "Intake");
        assertThat(results).allMatch(s -> s.getResponsibleSubteam() == null);
    }
    
    @Test
    void testFindByResponsibleSubteamIsNotNull() {
        // Setup - Create subsystems with and without responsible subteams
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        testSubsystem.setResponsibleSubteam(savedSubteam);      // Has subteam
        drivetrainSubsystem.setResponsibleSubteam(savedSubteam); // Has subteam
        intakeSubsystem.setResponsibleSubteam(null);            // No subteam
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findByResponsibleSubteamIsNotNull();
        
        // Verify - Should only find subsystems with responsible subteam
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Test Subsystem", "Drivetrain");
        assertThat(results).allMatch(s -> s.getResponsibleSubteam() != null);
    }
    
    @Test
    void testFindByDescriptionContainingIgnoreCase() {
        // Setup - Persist subsystems with different descriptions
        subsystemRepository.save(drivetrainSubsystem); // "Main robot locomotion system with swerve drive"
        subsystemRepository.save(intakeSubsystem);     // "Game piece intake mechanism"
        subsystemRepository.save(testSubsystem);       // "A test subsystem for unit testing"
        entityManager.flush();
        
        // Execute - Case insensitive search for "robot"
        List<Subsystem> robotResults = subsystemRepository.findByDescriptionContainingIgnoreCase("ROBOT");
        
        // Verify - Should find drivetrain
        assertThat(robotResults).hasSize(1);
        assertThat(robotResults.get(0).getName()).isEqualTo("Drivetrain");
        
        // Execute - Search for "test"
        List<Subsystem> testResults = subsystemRepository.findByDescriptionContainingIgnoreCase("test");
        
        // Verify - Should find test subsystem
        assertThat(testResults).hasSize(1);
        assertThat(testResults.get(0).getName()).isEqualTo("Test Subsystem");
        
        // Execute - Search for "mechanism"
        List<Subsystem> mechanismResults = subsystemRepository.findByDescriptionContainingIgnoreCase("mechanism");
        
        // Verify - Should find intake
        assertThat(mechanismResults).hasSize(1);
        assertThat(mechanismResults.get(0).getName()).isEqualTo("Intake");
    }
    
    @Test
    void testFindAllByOrderByName() {
        // Setup - Persist subsystems in non-alphabetical order
        subsystemRepository.save(drivetrainSubsystem); // "Drivetrain"
        subsystemRepository.save(intakeSubsystem);     // "Intake"  
        subsystemRepository.save(testSubsystem);       // "Test Subsystem"
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findAllByOrderByName();
        
        // Verify - Should be ordered alphabetically by name
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactly("Drivetrain", "Intake", "Test Subsystem");
    }
    
    @Test
    void testFindByStatusOrderByName() {
        // Setup - Create multiple subsystems with same status
        Subsystem anotherInProgress = new Subsystem();
        anotherInProgress.setName("Arm");
        anotherInProgress.setDescription("Robot arm subsystem");
        anotherInProgress.setStatus(Subsystem.Status.IN_PROGRESS);
        
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        drivetrainSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        
        subsystemRepository.save(anotherInProgress);   // "Arm"
        subsystemRepository.save(testSubsystem);       // "Test Subsystem"
        subsystemRepository.save(drivetrainSubsystem); // "Drivetrain"
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findByStatusOrderByName(Subsystem.Status.IN_PROGRESS);
        
        // Verify - Should be ordered alphabetically by name
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactly("Arm", "Drivetrain", "Test Subsystem");
        assertThat(results).allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS);
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testCountByStatus() {
        // Setup - Create subsystems with different statuses
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        drivetrainSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        intakeSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        
        // Create additional subsystems
        Subsystem completedSubsystem1 = new Subsystem();
        completedSubsystem1.setName("Completed System 1");
        completedSubsystem1.setStatus(Subsystem.Status.COMPLETED);
        
        Subsystem completedSubsystem2 = new Subsystem();
        completedSubsystem2.setName("Completed System 2");
        completedSubsystem2.setStatus(Subsystem.Status.COMPLETED);
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        subsystemRepository.save(completedSubsystem1);
        subsystemRepository.save(completedSubsystem2);
        entityManager.flush();
        
        // Execute
        long notStartedCount = subsystemRepository.countByStatus(Subsystem.Status.NOT_STARTED);
        long inProgressCount = subsystemRepository.countByStatus(Subsystem.Status.IN_PROGRESS);
        long completedCount = subsystemRepository.countByStatus(Subsystem.Status.COMPLETED);
        
        // Verify
        assertThat(notStartedCount).isEqualTo(1);
        assertThat(inProgressCount).isEqualTo(2);
        assertThat(completedCount).isEqualTo(2);
    }
    
    @Test
    void testCountByResponsibleSubteam() {
        // Setup - Create subteams and subsystems
        Subteam savedSubteam1 = persistAndFlush(testSubteam);
        Subteam savedSubteam2 = persistAndFlush(mechanicalSubteam);
        
        testSubsystem.setResponsibleSubteam(savedSubteam1);
        drivetrainSubsystem.setResponsibleSubteam(savedSubteam1);
        intakeSubsystem.setResponsibleSubteam(savedSubteam2);
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        entityManager.flush();
        
        // Execute
        long subteam1Count = subsystemRepository.countByResponsibleSubteam(savedSubteam1);
        long subteam2Count = subsystemRepository.countByResponsibleSubteam(savedSubteam2);
        
        // Verify
        assertThat(subteam1Count).isEqualTo(2);
        assertThat(subteam2Count).isEqualTo(1);
    }
    
    @Test
    void testExistsByName() {
        // Setup - Persist subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute - Check existing name
        boolean exists = subsystemRepository.existsByName("Drivetrain");
        
        // Verify
        assertThat(exists).isTrue();
        
        // Execute - Check non-existing name
        boolean notExists = subsystemRepository.existsByName("NonExistent");
        
        // Verify
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testExistsByNameIgnoreCase() {
        // Setup - Persist subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Execute - Case insensitive checks
        boolean existsLower = subsystemRepository.existsByNameIgnoreCase("drivetrain");
        boolean existsUpper = subsystemRepository.existsByNameIgnoreCase("DRIVETRAIN");
        boolean existsMixed = subsystemRepository.existsByNameIgnoreCase("DrIvEtRaIn");
        
        // Verify
        assertThat(existsLower).isTrue();
        assertThat(existsUpper).isTrue();
        assertThat(existsMixed).isTrue();
        
        // Execute - Non-existing name
        boolean notExists = subsystemRepository.existsByNameIgnoreCase("nonexistent");
        
        // Verify
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testFindByStatusAndResponsibleSubteam() {
        // Setup - Create subteam and subsystems
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem.setResponsibleSubteam(savedSubteam);
        
        drivetrainSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        drivetrainSubsystem.setResponsibleSubteam(savedSubteam);
        
        intakeSubsystem.setStatus(Subsystem.Status.COMPLETED);
        intakeSubsystem.setResponsibleSubteam(savedSubteam);
        
        // Create subsystem with same status but different subteam
        Subteam otherSubteam = persistAndFlush(mechanicalSubteam);
        Subsystem otherSubsystem = new Subsystem();
        otherSubsystem.setName("Other System");
        otherSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        otherSubsystem.setResponsibleSubteam(otherSubteam);
        
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        subsystemRepository.save(intakeSubsystem);
        subsystemRepository.save(otherSubsystem);
        entityManager.flush();
        
        // Execute
        List<Subsystem> results = subsystemRepository.findByStatusAndResponsibleSubteam(
            Subsystem.Status.IN_PROGRESS, savedSubteam);
        
        // Verify - Should find only IN_PROGRESS subsystems for the specific subteam
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Drivetrain", "Test Subsystem"); // Ordered by name due to @Query ORDER BY
        assertThat(results).allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS);
        assertThat(results).allMatch(s -> s.getResponsibleSubteam().getId().equals(savedSubteam.getId()));
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testUniqueConstraint_Name() {
        // Setup - Persist first subsystem
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush(); // This should succeed
        
        // Clear the persistence context to ensure fresh entity
        entityManager.clear();
        
        // Execute - Try to save subsystem with same name
        // THIS IS WHERE THE EXCEPTION SHOULD BE CAUGHT
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> {
                Subsystem duplicateName = new Subsystem();
                duplicateName.setName("Drivetrain");  // Same name as drivetrainSubsystem
                duplicateName.setDescription("Different description");
                duplicateName.setStatus(Subsystem.Status.NOT_STARTED);
                
                // The save operation itself may trigger the constraint violation
                subsystemRepository.save(duplicateName);
                entityManager.flush(); // Ensure the save is flushed to database
            }
        );
    }
    
    @Test
    void testSubteamRelationship() {
        // Setup - Create subteam with subsystems
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        // Add subsystems to subteam
        testSubsystem.setResponsibleSubteam(savedSubteam);
        drivetrainSubsystem.setResponsibleSubteam(savedSubteam);
        
        // Execute - Save subsystems
        subsystemRepository.save(testSubsystem);
        subsystemRepository.save(drivetrainSubsystem);
        entityManager.flush();
        
        // Verify - Subsystems are associated with subteam
        assertThat(testSubsystem.getResponsibleSubteam()).isNotNull();
        assertThat(testSubsystem.getResponsibleSubteam().getId()).isEqualTo(savedSubteam.getId());
        assertThat(drivetrainSubsystem.getResponsibleSubteam()).isNotNull();
        assertThat(drivetrainSubsystem.getResponsibleSubteam().getId()).isEqualTo(savedSubteam.getId());
        
        // Verify - Subteam relationship queries work
        List<Subsystem> subteamSubsystems = subsystemRepository.findByResponsibleSubteam(savedSubteam);
        assertThat(subteamSubsystems).hasSize(2);
        assertThat(subteamSubsystems).extracting(Subsystem::getName)
            .containsExactlyInAnyOrder("Test Subsystem", "Drivetrain");
    }
    
    @Test
    void testSubteamAssignmentChange() {
        // Setup - Create subteams and subsystem
        Subteam savedSubteam1 = persistAndFlush(testSubteam);
        Subteam savedSubteam2 = persistAndFlush(mechanicalSubteam);
        
        testSubsystem.setResponsibleSubteam(savedSubteam1);
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify initial assignment
        assertThat(savedSubsystem.getResponsibleSubteam().getId()).isEqualTo(savedSubteam1.getId());
        
        // Execute - Change subteam assignment
        savedSubsystem.setResponsibleSubteam(savedSubteam2);
        Subsystem updatedSubsystem = subsystemRepository.save(savedSubsystem);
        entityManager.flush();
        
        // Verify - Subteam assignment changed
        assertThat(updatedSubsystem.getResponsibleSubteam().getId()).isEqualTo(savedSubteam2.getId());
        
        // Verify - Subsystem counts are correct
        assertThat(subsystemRepository.countByResponsibleSubteam(savedSubteam1)).isEqualTo(0);
        assertThat(subsystemRepository.countByResponsibleSubteam(savedSubteam2)).isEqualTo(1);
    }
    
    @Test
    void testRemoveSubteamAssignment() {
        // Setup - Create subteam and subsystem
        Subteam savedSubteam = persistAndFlush(testSubteam);
        testSubsystem.setResponsibleSubteam(savedSubteam);
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify initial assignment
        assertThat(savedSubsystem.getResponsibleSubteam()).isNotNull();
        
        // Execute - Remove subteam assignment
        savedSubsystem.setResponsibleSubteam(null);
        Subsystem updatedSubsystem = subsystemRepository.save(savedSubsystem);
        entityManager.flush();
        
        // Verify - No subteam assignment
        assertThat(updatedSubsystem.getResponsibleSubteam()).isNull();
        
        // Verify - Subsystem appears in unassigned list
        List<Subsystem> unassignedSubsystems = subsystemRepository.findByResponsibleSubteamIsNull();
        assertThat(unassignedSubsystems).hasSize(1);
        assertThat(unassignedSubsystems.get(0).getName()).isEqualTo("Test Subsystem");
    }
    
    // ========== BUSINESS LOGIC VALIDATION ==========
    
    @Test
    void testSubsystemStatusManagement() {
        // Setup - Save subsystem with initial status
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Execute - Update status
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        Subsystem updatedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify - Status updated
        assertThat(updatedSubsystem.getStatus()).isEqualTo(Subsystem.Status.IN_PROGRESS);
        
        // Verify - Can search by new status
        List<Subsystem> inProgressSubsystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        assertThat(inProgressSubsystems).hasSize(1);
        assertThat(inProgressSubsystems.get(0).getName()).isEqualTo("Test Subsystem");
    }
    
    @Test
    void testSubsystemStatusEnum() {
        // Setup - Create subsystems with all status values
        Subsystem notStarted = new Subsystem();
        notStarted.setName("Not Started System");
        notStarted.setStatus(Subsystem.Status.NOT_STARTED);
        
        Subsystem inProgress = new Subsystem();
        inProgress.setName("In Progress System");
        inProgress.setStatus(Subsystem.Status.IN_PROGRESS);
        
        Subsystem completed = new Subsystem();
        completed.setName("Completed System");
        completed.setStatus(Subsystem.Status.COMPLETED);
        
        Subsystem testing = new Subsystem();
        testing.setName("Testing System");
        testing.setStatus(Subsystem.Status.TESTING);
        
        Subsystem issues = new Subsystem();
        issues.setName("Issues System");
        issues.setStatus(Subsystem.Status.ISSUES);
        
        subsystemRepository.save(notStarted);
        subsystemRepository.save(inProgress);
        subsystemRepository.save(completed);
        subsystemRepository.save(testing);
        subsystemRepository.save(issues);
        entityManager.flush();
        
        // Execute - Find by each status
        List<Subsystem> notStartedSystems = subsystemRepository.findByStatus(Subsystem.Status.NOT_STARTED);
        List<Subsystem> inProgressSystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        List<Subsystem> completedSystems = subsystemRepository.findByStatus(Subsystem.Status.COMPLETED);
        List<Subsystem> testingSystems = subsystemRepository.findByStatus(Subsystem.Status.TESTING);
        List<Subsystem> issuesSystems = subsystemRepository.findByStatus(Subsystem.Status.ISSUES);
        
        // Verify - Each status works correctly
        assertThat(notStartedSystems).hasSize(1);
        assertThat(notStartedSystems.get(0).getStatus()).isEqualTo(Subsystem.Status.NOT_STARTED);
        assertThat(notStartedSystems.get(0).getStatus().getDisplayName()).isEqualTo("Not Started");
        
        assertThat(inProgressSystems).hasSize(1);
        assertThat(inProgressSystems.get(0).getStatus()).isEqualTo(Subsystem.Status.IN_PROGRESS);
        assertThat(inProgressSystems.get(0).getStatus().getDisplayName()).isEqualTo("In Progress");
        
        assertThat(completedSystems).hasSize(1);
        assertThat(completedSystems.get(0).getStatus()).isEqualTo(Subsystem.Status.COMPLETED);
        assertThat(completedSystems.get(0).getStatus().getDisplayName()).isEqualTo("Completed");
        
        assertThat(testingSystems).hasSize(1);
        assertThat(testingSystems.get(0).getStatus()).isEqualTo(Subsystem.Status.TESTING);
        assertThat(testingSystems.get(0).getStatus().getDisplayName()).isEqualTo("Testing");
        
        assertThat(issuesSystems).hasSize(1);
        assertThat(issuesSystems.get(0).getStatus()).isEqualTo(Subsystem.Status.ISSUES);
        assertThat(issuesSystems.get(0).getStatus().getDisplayName()).isEqualTo("Issues");
    }
    
    @Test
    void testComplexSubsystemScenario() {
        // Setup - Create a complex subsystem scenario with multiple relationships
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        // Create subsystem with all properties
        testSubsystem.setName("Complex Subsystem");
        testSubsystem.setDescription("A complex subsystem with full configuration");
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem.setResponsibleSubteam(savedSubteam);
        
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Execute comprehensive queries
        List<Subsystem> allSubsystems = subsystemRepository.findAll();
        List<Subsystem> inProgressSubsystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        List<Subsystem> subteamSubsystems = subsystemRepository.findByResponsibleSubteam(savedSubteam);
        Optional<Subsystem> foundByName = subsystemRepository.findByName("Complex Subsystem");
        List<Subsystem> foundByDescription = subsystemRepository.findByDescriptionContainingIgnoreCase("complex");
        
        // Verify comprehensive scenario
        assertThat(allSubsystems).hasSize(1);
        assertThat(inProgressSubsystems).hasSize(1);
        assertThat(subteamSubsystems).hasSize(1);
        assertThat(foundByName).isPresent();
        assertThat(foundByDescription).hasSize(1);
        
        // Verify all properties
        assertThat(savedSubsystem.getName()).isEqualTo("Complex Subsystem");
        assertThat(savedSubsystem.getDescription()).isEqualTo("A complex subsystem with full configuration");
        assertThat(savedSubsystem.getStatus()).isEqualTo(Subsystem.Status.IN_PROGRESS);
        assertThat(savedSubsystem.getResponsibleSubteam().getId()).isEqualTo(savedSubteam.getId());
    }
    
    // ========== CONSTRAINT AND VALIDATION TESTING ==========
    
    @Test
    void testSubsystemConstraints() {
        // Setup - Create subsystem with all required fields
        testSubsystem.setName("Valid Subsystem");
        testSubsystem.setDescription("Valid description");
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Execute - Save valid subsystem
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify - Subsystem saved successfully
        assertThat(savedSubsystem.getId()).isNotNull();
        assertThat(savedSubsystem.getName()).isEqualTo("Valid Subsystem");
        
        // Verify - Required fields are present
        assertThat(savedSubsystem.getName()).isNotNull();
        assertThat(savedSubsystem.getStatus()).isNotNull();
    }
    
    @Test
    void testSubsystemNameValidation() {
        // Setup - Create subsystem with valid name
        testSubsystem.setName("Valid Name");
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        
        // Execute - Save subsystem with valid name
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Verify - Name saved correctly
        assertThat(savedSubsystem.getName()).isEqualTo("Valid Name");
        
        // Verify - Name-based queries work
        Optional<Subsystem> foundByName = subsystemRepository.findByName("Valid Name");
        assertThat(foundByName).isPresent();
        assertThat(foundByName.get().getId()).isEqualTo(savedSubsystem.getId());
    }
    
    // ========== PERFORMANCE AND BULK OPERATIONS ==========
    
    @Test
    void testBulkSubsystemOperations() {
        // Setup - Create subteam
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        // Create multiple subsystems
        List<Subsystem> subsystems = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Subsystem subsystem = new Subsystem();
            subsystem.setName("Bulk Subsystem " + i);
            subsystem.setDescription("Bulk subsystem description " + i);
            subsystem.setStatus(Subsystem.Status.values()[i % Subsystem.Status.values().length]);
            
            // Assign subteam to every 3rd subsystem
            if (i % 3 == 0) {
                subsystem.setResponsibleSubteam(savedSubteam);
            }
            
            subsystems.add(subsystem);
        }
        
        // Execute - Save all subsystems
        subsystemRepository.saveAll(subsystems);
        entityManager.flush();
        
        // Verify - Bulk operations
        List<Subsystem> allSubsystems = subsystemRepository.findAll();
        assertThat(allSubsystems).hasSize(15);
        
        List<Subsystem> subteamSubsystems = subsystemRepository.findByResponsibleSubteam(savedSubteam);
        assertThat(subteamSubsystems).hasSize(5); // Every 3rd subsystem
        
        List<Subsystem> unassignedSubsystems = subsystemRepository.findByResponsibleSubteamIsNull();
        assertThat(unassignedSubsystems).hasSize(10); // Remaining subsystems
        
        long totalSubsystems = subsystemRepository.count();
        assertThat(totalSubsystems).isEqualTo(15);
        
        long subteamCount = subsystemRepository.countByResponsibleSubteam(savedSubteam);
        assertThat(subteamCount).isEqualTo(5);
    }
    
    @Test
    void testSubsystemQueryPerformance() {
        // Setup - Create larger dataset for performance testing
        Subteam savedSubteam1 = persistAndFlush(testSubteam);
        Subteam savedSubteam2 = persistAndFlush(mechanicalSubteam);
        
        // Create 30 subsystems across different statuses and subteams
        List<Subsystem> subsystems = new java.util.ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Subsystem subsystem = new Subsystem();
            subsystem.setName("Performance Subsystem " + i);
            subsystem.setDescription("Performance testing subsystem " + i);
            subsystem.setStatus(Subsystem.Status.values()[i % Subsystem.Status.values().length]);
            
            // Alternate between subteams and null
            if (i % 3 == 1) {
                subsystem.setResponsibleSubteam(savedSubteam1);
            } else if (i % 3 == 2) {
                subsystem.setResponsibleSubteam(savedSubteam2);
            }
            // Every 3rd subsystem has no responsible subteam
            
            subsystems.add(subsystem);
        }
        
        subsystemRepository.saveAll(subsystems);
        entityManager.flush();
        
        // Execute - Performance-sensitive queries
        long startTime = System.currentTimeMillis();
        
        List<Subsystem> allSubsystems = subsystemRepository.findAll();
        List<Subsystem> orderedSubsystems = subsystemRepository.findAllByOrderByName();
        List<Subsystem> subteam1Subsystems = subsystemRepository.findByResponsibleSubteam(savedSubteam1);
        List<Subsystem> inProgressSubsystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        List<Subsystem> unassignedSubsystems = subsystemRepository.findByResponsibleSubteamIsNull();
        
        long endTime = System.currentTimeMillis();
        
        // Verify - Query results are correct
        assertThat(allSubsystems).hasSize(30);
        assertThat(orderedSubsystems).hasSize(30);
        assertThat(subteam1Subsystems).hasSize(10); // Every 3rd starting with 1
        assertThat(unassignedSubsystems).hasSize(10); // Every 3rd starting with 3
        
        // Verify ordering
        List<String> orderedNames = orderedSubsystems.stream()
            .map(Subsystem::getName)
            .toList();
        List<String> sortedNames = orderedNames.stream()
            .sorted()
            .toList();
        assertThat(orderedNames).isEqualTo(sortedNames);
        
        // Log performance (for development monitoring)
        long queryTime = endTime - startTime;
        System.out.println("Subsystem query execution time: " + queryTime + "ms");
        
        // Verify reasonable performance (should complete quickly)
        assertThat(queryTime).isLessThan(5000); // Should complete within 5 seconds
    }
    
    // ========== ERROR HANDLING AND EDGE CASES ==========
    
    @Test
    void testSubsystemRepositoryErrorHandling() {
        // Test null parameter handling in repository methods
        
        // findByResponsibleSubteam with null - Spring Data JPA handles this gracefully
        List<Subsystem> nullSubteamSubsystems = subsystemRepository.findByResponsibleSubteam(null);
        assertThat(nullSubteamSubsystems).isEmpty();
        
        // findByName with null - Spring Data JPA handles this gracefully
        Optional<Subsystem> nullNameSubsystem = subsystemRepository.findByName(null);
        assertThat(nullNameSubsystem).isEmpty();
        
        // findByStatus with null - Spring Data JPA handles this gracefully
        List<Subsystem> nullStatusSubsystems = subsystemRepository.findByStatus(null);
        assertThat(nullStatusSubsystems).isEmpty();
    }
    
    @Test
    void testSubsystemEdgeCases() {
        // Setup - Create subsystems with edge case values
        
        // Subsystem with minimal data
        Subsystem minimalSubsystem = new Subsystem();
        minimalSubsystem.setName("Minimal");
        minimalSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        // No description, no responsible subteam
        
        // Subsystem with very long description
        Subsystem longDescSubsystem = new Subsystem();
        longDescSubsystem.setName("Long Description System");
        // Create exactly 1000 character description
        String baseDescription = "This is a very long description that tests the system's ability to handle extensive text content in the description field. ";
        String exactDescription = baseDescription.repeat(8) + baseDescription.substring(0, 1000 - (baseDescription.length() * 8));
        longDescSubsystem.setDescription(exactDescription);
        longDescSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        
        subsystemRepository.save(minimalSubsystem);
        subsystemRepository.save(longDescSubsystem);
        entityManager.flush();
        
        // Execute - Query edge cases
        List<Subsystem> allSubsystems = subsystemRepository.findAll();
        List<Subsystem> unassignedSubsystems = subsystemRepository.findByResponsibleSubteamIsNull();
        List<Subsystem> longDescResults = subsystemRepository.findByDescriptionContainingIgnoreCase("very long");
        
        // Verify - Edge cases handled correctly
        assertThat(allSubsystems).hasSize(2);
        assertThat(unassignedSubsystems).hasSize(2); // Both have no responsible subteam
        assertThat(longDescResults).hasSize(1);
        assertThat(longDescResults.get(0).getName()).isEqualTo("Long Description System");
        
        // Verify minimal subsystem
        assertThat(minimalSubsystem.getDescription()).isNull();
        assertThat(minimalSubsystem.getResponsibleSubteam()).isNull();
        
        // Verify long description is preserved
        assertThat(longDescSubsystem.getDescription()).hasSize(1000); // Exactly 1000 characters
    }
    
    // ========== INTEGRATION WITH SERVICE LAYER PATTERNS ==========
    
    @Test
    void testRepositoryServiceIntegration() {
        // This test verifies that the repository works correctly with service layer patterns
        // Setup - Create realistic subsystem scenario
        Subteam savedSubteam = persistAndFlush(testSubteam);
        
        // Create subsystem following service layer patterns
        testSubsystem.setName("Service Integration Test");
        testSubsystem.setDescription("Testing integration with service layer");
        testSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
        testSubsystem.setResponsibleSubteam(savedSubteam);
        
        Subsystem savedSubsystem = subsystemRepository.save(testSubsystem);
        entityManager.flush();
        
        // Simulate service layer operations
        
        // 1. Update status (service layer pattern)
        savedSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        subsystemRepository.save(savedSubsystem);
        
        // 2. Update description (service layer pattern)
        savedSubsystem.setDescription("Updated description through service layer");
        subsystemRepository.save(savedSubsystem);
        
        // 3. Change responsible subteam (service layer pattern)
        Subteam newSubteam = persistAndFlush(mechanicalSubteam);
        savedSubsystem.setResponsibleSubteam(newSubteam);
        subsystemRepository.save(savedSubsystem);
        
        entityManager.flush();
        
        // Verify - All service layer operations work through repository
        Subsystem finalSubsystem = subsystemRepository.findById(savedSubsystem.getId()).orElse(null);
        assertThat(finalSubsystem).isNotNull();
        assertThat(finalSubsystem.getStatus()).isEqualTo(Subsystem.Status.IN_PROGRESS);
        assertThat(finalSubsystem.getDescription()).isEqualTo("Updated description through service layer");
        assertThat(finalSubsystem.getResponsibleSubteam().getId()).isEqualTo(newSubteam.getId());
        
        // Verify - Repository queries work for service layer
        List<Subsystem> statusSubsystems = subsystemRepository.findByStatus(Subsystem.Status.IN_PROGRESS);
        List<Subsystem> subteamSubsystems = subsystemRepository.findByResponsibleSubteam(newSubteam);
        Optional<Subsystem> nameSubsystem = subsystemRepository.findByName("Service Integration Test");
        
        assertThat(statusSubsystems).contains(finalSubsystem);
        assertThat(subteamSubsystems).contains(finalSubsystem);
        assertThat(nameSubsystem).isPresent();
        assertThat(nameSubsystem.get().getId()).isEqualTo(finalSubsystem.getId());
    }
}