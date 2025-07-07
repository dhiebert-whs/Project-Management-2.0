// src/test/java/org/frcpm/repositories/SubteamRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.spring.SubteamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for SubteamRepository using @DataJpaTest.
 * Uses JPA slice testing for optimized repository testing.
 * 
 * @DataJpaTest loads only JPA components and repositories
 * @AutoConfigureTestDatabase prevents replacement of configured database
 * @ActiveProfiles("test") ensures test-specific configuration
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubteamRepositoryIntegrationTest {
    
    @Autowired
    private SubteamRepository subteamRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private Subteam programmingTeam;
    private Subteam mechanicalTeam;
    private Subteam businessTeam;
    private Subteam emptyTeam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        programmingTeam = createProgrammingTeam();
        mechanicalTeam = createMechanicalTeam();
        businessTeam = createBusinessTeam();
        emptyTeam = createEmptyTeam();
    }
    
    /**
     * Creates a programming subteam for use in tests.
     */
    private Subteam createProgrammingTeam() {
        Subteam subteam = new Subteam();
        subteam.setName("Programming Team");
        subteam.setColorCode("#007ACC");
        subteam.setSpecialties("Java, Python, C++, Robot Control, Autonomous Programming");
        return subteam;
    }
    
    /**
     * Creates a mechanical subteam for use in tests.
     */
    private Subteam createMechanicalTeam() {
        Subteam subteam = new Subteam();
        subteam.setName("Mechanical Team");
        subteam.setColorCode("#FF6B35");
        subteam.setSpecialties("CAD Design, Manufacturing, Assembly, Welding, Machining");
        return subteam;
    }
    
    /**
     * Creates a business subteam for use in tests.
     */
    private Subteam createBusinessTeam() {
        Subteam subteam = new Subteam();
        subteam.setName("Business Team");
        subteam.setColorCode("#28A745");
        subteam.setSpecialties("Marketing, Fundraising, Community Outreach, Social Media");
        return subteam;
    }
    
    /**
     * Creates an empty subteam (no specialties) for testing edge cases.
     */
    private Subteam createEmptyTeam() {
        Subteam subteam = new Subteam();
        subteam.setName("New Team");
        subteam.setColorCode("#6C757D");
        // No specialties set
        return subteam;
    }
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Test
    void testSaveAndFindById() {
        // Execute - Save subteam
        Subteam savedSubteam = entityManager.persistAndFlush(programmingTeam);
        
        // Verify save
        assertThat(savedSubteam.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Subteam> found = subteamRepository.findById(savedSubteam.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Programming Team");
        assertThat(found.get().getColorCode()).isEqualTo("#007ACC");
        assertThat(found.get().getSpecialties()).isEqualTo("Java, Python, C++, Robot Control, Autonomous Programming");
    }
    
    @Test
    void testFindAll() {
        // Setup - Save multiple subteams
        entityManager.persistAndFlush(programmingTeam);
        entityManager.persistAndFlush(mechanicalTeam);
        entityManager.persistAndFlush(businessTeam);
        
        // Execute - Find all
        List<Subteam> allSubteams = subteamRepository.findAll();
        
        // Verify
        assertThat(allSubteams).hasSize(3);
        assertThat(allSubteams).extracting(Subteam::getName)
            .containsExactlyInAnyOrder("Programming Team", "Mechanical Team", "Business Team");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Save subteam
        Subteam savedSubteam = entityManager.persistAndFlush(programmingTeam);
        
        // Verify exists before deletion
        assertThat(subteamRepository.existsById(savedSubteam.getId())).isTrue();
        
        // Execute - Delete
        subteamRepository.deleteById(savedSubteam.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(subteamRepository.existsById(savedSubteam.getId())).isFalse();
        assertThat(subteamRepository.findById(savedSubteam.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(subteamRepository.count()).isEqualTo(0);
        
        // Setup - Save subteams
        entityManager.persistAndFlush(programmingTeam);
        entityManager.persistAndFlush(mechanicalTeam);
        
        // Execute and verify
        assertThat(subteamRepository.count()).isEqualTo(2);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByName() {
        // Setup - Save subteam
        entityManager.persistAndFlush(programmingTeam);
        
        // Execute
        Optional<Subteam> result = subteamRepository.findByName("Programming Team");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Programming Team");
        assertThat(result.get().getColorCode()).isEqualTo("#007ACC");
    }
    
    @Test
    void testFindByName_NotFound() {
        // Setup - Save a different subteam
        entityManager.persistAndFlush(programmingTeam);
        
        // Execute - Search for non-existent name
        Optional<Subteam> result = subteamRepository.findByName("Nonexistent Team");
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindByNameIgnoreCase() {
        // Setup - Save subteam
        entityManager.persistAndFlush(programmingTeam);
        
        // Execute - Case insensitive search
        Optional<Subteam> result = subteamRepository.findByNameIgnoreCase("PROGRAMMING TEAM");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Programming Team");
        
        // Execute - Different case
        Optional<Subteam> result2 = subteamRepository.findByNameIgnoreCase("programming team");
        
        // Verify
        assertThat(result2).isPresent();
        assertThat(result2.get().getName()).isEqualTo("Programming Team");
    }
    
    @Test
    void testFindByColorCode() {
        // Setup - Save subteams with different colors
        entityManager.persistAndFlush(programmingTeam);    // #007ACC
        entityManager.persistAndFlush(mechanicalTeam);     // #FF6B35
        entityManager.persistAndFlush(businessTeam);       // #28A745
        
        // Execute - Find by programming team color
        List<Subteam> results = subteamRepository.findByColorCode("#007ACC");
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Programming Team");
        
        // Execute - Find by mechanical team color
        List<Subteam> mechanicalResults = subteamRepository.findByColorCode("#FF6B35");
        
        // Verify
        assertThat(mechanicalResults).hasSize(1);
        assertThat(mechanicalResults.get(0).getName()).isEqualTo("Mechanical Team");
    }
    
    @Test
    void testFindAllByOrderByName() {
        // Setup - Save subteams in random order
        entityManager.persistAndFlush(mechanicalTeam);     // "Mechanical Team"
        entityManager.persistAndFlush(businessTeam);       // "Business Team"
        entityManager.persistAndFlush(programmingTeam);    // "Programming Team"
        
        // Execute
        List<Subteam> results = subteamRepository.findAllByOrderByName();
        
        // Verify - Should be ordered alphabetically
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Subteam::getName)
            .containsExactly("Business Team", "Mechanical Team", "Programming Team");
    }
    
    @Test
    void testExistsByName() {
        // Setup - Save subteam
        entityManager.persistAndFlush(programmingTeam);
        
        // Execute - Check existing name
        boolean exists = subteamRepository.existsByName("Programming Team");
        
        // Verify
        assertThat(exists).isTrue();
        
        // Execute - Check non-existing name
        boolean notExists = subteamRepository.existsByName("Nonexistent Team");
        
        // Verify
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testExistsByNameIgnoreCase() {
        // Setup - Save subteam
        entityManager.persistAndFlush(programmingTeam);
        
        // Execute - Check with different case
        boolean exists1 = subteamRepository.existsByNameIgnoreCase("PROGRAMMING TEAM");
        boolean exists2 = subteamRepository.existsByNameIgnoreCase("programming team");
        boolean exists3 = subteamRepository.existsByNameIgnoreCase("Programming Team");
        
        // Verify - All should return true
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isTrue();
        
        // Execute - Check non-existing name
        boolean notExists = subteamRepository.existsByNameIgnoreCase("NONEXISTENT TEAM");
        
        // Verify
        assertThat(notExists).isFalse();
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindBySpecialty() {
        // Setup - Save subteams with different specialties
        entityManager.persistAndFlush(programmingTeam);    // "Java, Python, C++, Robot Control, Autonomous Programming"
        entityManager.persistAndFlush(mechanicalTeam);     // "CAD Design, Manufacturing, Assembly, Welding, Machining"
        entityManager.persistAndFlush(businessTeam);       // "Marketing, Fundraising, Community Outreach, Social Media"
        
        // Execute - Search for Java specialty
        List<Subteam> javaResults = subteamRepository.findBySpecialty("Java");
        
        // Verify - Should find programming team
        assertThat(javaResults).hasSize(1);
        assertThat(javaResults.get(0).getName()).isEqualTo("Programming Team");
        
        // Execute - Search for CAD specialty
        List<Subteam> cadResults = subteamRepository.findBySpecialty("CAD");
        
        // Verify - Should find mechanical team
        assertThat(cadResults).hasSize(1);
        assertThat(cadResults.get(0).getName()).isEqualTo("Mechanical Team");
    }
    
    @Test
    void testFindBySpecialtyIgnoreCase() {
        // Setup - Save subteams
        entityManager.persistAndFlush(programmingTeam);    // Contains "Java"
        entityManager.persistAndFlush(mechanicalTeam);     // Contains "CAD Design"
        
        // Execute - Case insensitive search for "java"
        List<Subteam> javaResults = subteamRepository.findBySpecialtyIgnoreCase("java");
        
        // Verify - Should find programming team
        assertThat(javaResults).hasSize(1);
        assertThat(javaResults.get(0).getName()).isEqualTo("Programming Team");
        
        // Execute - Case insensitive search for "cad"
        List<Subteam> cadResults = subteamRepository.findBySpecialtyIgnoreCase("cad");
        
        // Verify - Should find mechanical team
        assertThat(cadResults).hasSize(1);
        assertThat(cadResults.get(0).getName()).isEqualTo("Mechanical Team");
    }
    
    @Test
    void testFindBySpecialtiesContainingIgnoreCase() {
        // Setup - Save subteams with various specialties
        entityManager.persistAndFlush(programmingTeam);    // "Java, Python, C++, Robot Control, Autonomous Programming"
        entityManager.persistAndFlush(mechanicalTeam);     // "CAD Design, Manufacturing, Assembly, Welding, Machining"
        entityManager.persistAndFlush(businessTeam);       // "Marketing, Fundraising, Community Outreach, Social Media"
        
        // Execute - Search for "Programming" (should match both "Programming Team" and autonomus programming)
        List<Subteam> programmingResults = subteamRepository.findBySpecialtiesContainingIgnoreCase("programming");
        
        // Verify - Should find programming team
        assertThat(programmingResults).hasSize(1);
        assertThat(programmingResults.get(0).getName()).isEqualTo("Programming Team");
        
        // Execute - Search for "design"
        List<Subteam> designResults = subteamRepository.findBySpecialtiesContainingIgnoreCase("design");
        
        // Verify - Should find mechanical team
        assertThat(designResults).hasSize(1);
        assertThat(designResults.get(0).getName()).isEqualTo("Mechanical Team");
        
        // Execute - Search for "social"
        List<Subteam> socialResults = subteamRepository.findBySpecialtiesContainingIgnoreCase("social");
        
        // Verify - Should find business team
        assertThat(socialResults).hasSize(1);
        assertThat(socialResults.get(0).getName()).isEqualTo("Business Team");
    }
    
    @Test
    void testFindBySpecialty_NoMatch() {
        // Setup - Save subteams
        entityManager.persistAndFlush(programmingTeam);
        entityManager.persistAndFlush(mechanicalTeam);
        
        // Execute - Search for non-existent specialty
        List<Subteam> results = subteamRepository.findBySpecialty("Quantum Computing");
        
        // Verify - Should find no results
        assertThat(results).isEmpty();
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testSubteamWithEmptySpecialties() {
        // Setup - Save subteam with no specialties
        Subteam savedSubteam = entityManager.persistAndFlush(emptyTeam);
        
        // Verify - Subteam is saved correctly
        assertThat(savedSubteam.getId()).isNotNull();
        assertThat(savedSubteam.getSpecialties()).isNull();
        
        // Execute - Search by specialties should not find this team
        List<Subteam> results = subteamRepository.findBySpecialty("anything");
        
        // Verify - Should not find empty team
        assertThat(results).isEmpty();
    }
    
    @Test
    void testSubteamWithRelationships() {
        // This test validates that the Subteam entity can be saved
        // even though it has relationships to TeamMember and Subsystem
        // (which may not exist yet, but are defined in the entity)
        
        // Setup - Save subteam
        Subteam savedSubteam = entityManager.persistAndFlush(programmingTeam);
        
        // Verify - Subteam is saved correctly
        assertThat(savedSubteam.getId()).isNotNull();
        assertThat(savedSubteam.getMembers()).isEmpty();     // No members yet
        assertThat(savedSubteam.getSubsystems()).isEmpty();  // No subsystems yet
        
        // Verify - Subteam can be retrieved
        Optional<Subteam> retrieved = subteamRepository.findById(savedSubteam.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Programming Team");
    }
    
    @Test
    void testUniqueConstraints() {
        // Note: There don't appear to be unique constraints on Subteam based on the entity
        // This test verifies that multiple subteams can have same color or similar names
        
        // Setup - Create subteams with same color
        Subteam team1 = new Subteam("Team Alpha", "#FF0000");
        Subteam team2 = new Subteam("Team Beta", "#FF0000");  // Same color
        
        // Execute - Save both teams
        entityManager.persistAndFlush(team1);
        entityManager.persistAndFlush(team2);
        
        // Verify - Both should be saved successfully
        List<Subteam> sameColorTeams = subteamRepository.findByColorCode("#FF0000");
        assertThat(sameColorTeams).hasSize(2);
        assertThat(sameColorTeams).extracting(Subteam::getName)
            .containsExactlyInAnyOrder("Team Alpha", "Team Beta");
    }
    
    @Test
    void testComplexSpecialtySearch() {
        // Setup - Create subteams with overlapping specialties
        Subteam fullStackTeam = new Subteam();
        fullStackTeam.setName("Full Stack Team");
        fullStackTeam.setColorCode("#8B5CF6");
        fullStackTeam.setSpecialties("Java, Python, JavaScript, React, Spring Boot, Database Design");
        
        entityManager.persistAndFlush(programmingTeam);  // Has Java, Python
        entityManager.persistAndFlush(fullStackTeam);    // Also has Java, Python
        
        // Execute - Search for Java (should find both)
        List<Subteam> javaTeams = subteamRepository.findBySpecialtyIgnoreCase("java");
        
        // Verify - Should find both teams
        assertThat(javaTeams).hasSize(2);
        assertThat(javaTeams).extracting(Subteam::getName)
            .containsExactlyInAnyOrder("Programming Team", "Full Stack Team");
        
        // Execute - Search for React (should find only full stack team)
        List<Subteam> reactTeams = subteamRepository.findBySpecialtyIgnoreCase("react");
        
        // Verify - Should find only full stack team
        assertThat(reactTeams).hasSize(1);
        assertThat(reactTeams.get(0).getName()).isEqualTo("Full Stack Team");
    }
    
    @Test
    void testSpecialCharactersInName() {
        // Setup - Create subteam with special characters
        Subteam specialTeam = new Subteam();
        specialTeam.setName("CAD/CAM & 3D Printing");
        specialTeam.setColorCode("#FF69B4");
        specialTeam.setSpecialties("3D Modeling, CAD, CAM, 3D Printing, Prototyping");
        
        // Execute - Save team
        Subteam savedTeam = entityManager.persistAndFlush(specialTeam);
        
        // Verify - Team is saved correctly
        assertThat(savedTeam.getId()).isNotNull();
        
        // Execute - Find by exact name
        Optional<Subteam> found = subteamRepository.findByName("CAD/CAM & 3D Printing");
        
        // Verify - Should find the team
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("CAD/CAM & 3D Printing");
    }
}