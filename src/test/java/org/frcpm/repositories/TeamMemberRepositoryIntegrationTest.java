// src/test/java/org/frcpm/repositories/TeamMemberRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TeamMemberRepository using Spring Boot @DataJpaTest.
 * Tests all repository methods including auto-implemented Spring Data JPA methods
 * and custom @Query methods.
 * 
 * ✅ PROVEN PATTERN APPLIED: Following AttendanceRepositoryIntegrationTest template for 100% success rate.
 * ✅ DEPENDENCY CONFLICT RESOLVED: Uses findByLeaderTrue() instead of findByIsLeaderTrue()
 */
@DataJpaTest
@ActiveProfiles("test")
class TeamMemberRepositoryIntegrationTest {
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private TeamMember testMember;
    private TeamMember leaderMember;
    private Subteam testSubteam;
    private Subteam otherSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testSubteam = createTestSubteam();
        otherSubteam = createOtherSubteam();
        testMember = createTestMember();
        leaderMember = createLeaderMember();
    }
    
    /**
     * Creates a test subteam for use in tests.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setName("Programming Team");
        subteam.setColorCode("#007ACC");
        return subteam;
    }
    
    /**
     * Creates another subteam for multi-subteam tests.
     */
    private Subteam createOtherSubteam() {
        Subteam subteam = new Subteam();
        subteam.setName("Mechanical Team");
        subteam.setColorCode("#FF6B35");
        return subteam;
    }
    
    /**
     * Creates a test team member for use in tests.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember();
        member.setUsername("testuser");
        member.setFirstName("Test");
        member.setLastName("User");
        member.setEmail("test@example.com");
        member.setPhone("555-1234");
        member.setSkills("Java, Spring Boot, Testing");
        member.setLeader(false);
        return member;
    }
    
    /**
     * Creates a leader team member for use in tests.
     */
    private TeamMember createLeaderMember() {
        TeamMember member = new TeamMember();
        member.setUsername("leader");
        member.setFirstName("Team");
        member.setLastName("Leader");
        member.setEmail("leader@example.com");
        member.setPhone("555-5678");
        member.setSkills("Leadership, Project Management, Java");
        member.setLeader(true);
        return member;
    }
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Test
    void testSaveAndFindById() {
        // Setup - Persist subteam first
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        testMember.setSubteam(savedSubteam);
        
        // Execute - Save team member
        TeamMember savedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Verify save
        assertThat(savedMember.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<TeamMember> found = teamMemberRepository.findById(savedMember.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getLastName()).isEqualTo("User");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getPhone()).isEqualTo("555-1234");
        assertThat(found.get().getSkills()).isEqualTo("Java, Spring Boot, Testing");
        assertThat(found.get().isLeader()).isFalse();
        assertThat(found.get().getSubteam().getId()).isEqualTo(savedSubteam.getId());
    }
    
    @Test
    void testFindAll() {
        // Setup - Persist members
        TeamMember savedMember1 = entityManager.persistAndFlush(testMember);
        TeamMember savedMember2 = entityManager.persistAndFlush(leaderMember);
        
        // Execute - Find all
        List<TeamMember> allMembers = teamMemberRepository.findAll();
        
        // Verify
        assertThat(allMembers).hasSize(2);
        assertThat(allMembers).extracting(TeamMember::getUsername)
            .containsExactlyInAnyOrder("testuser", "leader");
    }
    
    @Test
    void testDeleteById() {
        // Setup - Persist team member
        TeamMember savedMember = entityManager.persistAndFlush(testMember);
        
        // Verify exists before deletion
        assertThat(teamMemberRepository.existsById(savedMember.getId())).isTrue();
        
        // Execute - Delete
        teamMemberRepository.deleteById(savedMember.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(teamMemberRepository.existsById(savedMember.getId())).isFalse();
        assertThat(teamMemberRepository.findById(savedMember.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(teamMemberRepository.count()).isEqualTo(0);
        
        // Setup - Persist members
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        entityManager.flush();
        
        // Execute and verify
        assertThat(teamMemberRepository.count()).isEqualTo(2);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByUsername() {
        // Setup - Persist team member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute
        Optional<TeamMember> result = teamMemberRepository.findByUsername("testuser");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getFirstName()).isEqualTo("Test");
        assertThat(result.get().getLastName()).isEqualTo("User");
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Setup - Persist a different member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Search for non-existent username
        Optional<TeamMember> result = teamMemberRepository.findByUsername("nonexistent");
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindBySubteam() {
        // Setup - Persist subteam and members
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        testMember.setSubteam(savedSubteam);
        leaderMember.setSubteam(savedSubteam);
        
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findBySubteam(savedSubteam);
        
        // Verify
        assertThat(results).hasSize(2);
        assertThat(results).extracting(TeamMember::getUsername)
            .containsExactlyInAnyOrder("testuser", "leader");
        assertThat(results).allMatch(member -> member.getSubteam().getId().equals(savedSubteam.getId()));
    }
    
    @Test
    void testFindBySubteamId() {
        // Setup - Persist subteam and members
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        testMember.setSubteam(savedSubteam);
        
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findBySubteamId(savedSubteam.getId());
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("testuser");
        assertThat(results.get(0).getSubteam().getId()).isEqualTo(savedSubteam.getId());
    }
    
    @Test
    void testFindByLeaderTrue() {
        // Setup - Persist both leader and non-leader
        teamMemberRepository.save(testMember);     // leader = false
        teamMemberRepository.save(leaderMember);   // leader = true
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findByLeaderTrue();
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("leader");
        assertThat(results.get(0).isLeader()).isTrue();
    }
    
    @Test
    void testFindByLeaderFalse() {
        // Setup - Persist both leader and non-leader
        teamMemberRepository.save(testMember);     // leader = false
        teamMemberRepository.save(leaderMember);   // leader = true
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findByLeaderFalse();
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("testuser");
        assertThat(results.get(0).isLeader()).isFalse();
    }
    
    @Test
    void testFindByFirstNameContainingIgnoreCase() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);     // firstName = "Test"
        teamMemberRepository.save(leaderMember);   // firstName = "Team"
        entityManager.flush();
        
        // Execute - Case insensitive search
        List<TeamMember> results = teamMemberRepository.findByFirstNameContainingIgnoreCase("tea");
        
        // Verify - Should find both "Test" and "Team"
        assertThat(results).hasSize(2);
        assertThat(results).extracting(TeamMember::getFirstName)
            .containsExactlyInAnyOrder("Test", "Team");
    }
    
    @Test
    void testFindByLastNameContainingIgnoreCase() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);     // lastName = "User"
        teamMemberRepository.save(leaderMember);   // lastName = "Leader"
        entityManager.flush();
        
        // Execute - Case insensitive search
        List<TeamMember> results = teamMemberRepository.findByLastNameContainingIgnoreCase("LEAD");
        
        // Verify - Should find "Leader"
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Leader");
    }
    
    @Test
    void testFindByEmail() {
        // Setup - Persist member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute
        Optional<TeamMember> result = teamMemberRepository.findByEmail("test@example.com");
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    void testFindByEmail_NotFound() {
        // Setup - Persist a different member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Search for non-existent email
        Optional<TeamMember> result = teamMemberRepository.findByEmail("nonexistent@example.com");
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindBySubteamIsNull() {
        // Setup - Create members with and without subteams
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        testMember.setSubteam(null);           // No subteam
        leaderMember.setSubteam(savedSubteam); // Has subteam
        
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findBySubteamIsNull();
        
        // Verify - Should only find member without subteam
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("testuser");
        assertThat(results.get(0).getSubteam()).isNull();
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindBySkillsContainingIgnoreCase() {
        // Setup - Persist members with different skills
        teamMemberRepository.save(testMember);     // skills = "Java, Spring Boot, Testing"
        teamMemberRepository.save(leaderMember);   // skills = "Leadership, Project Management, Java"
        entityManager.flush();
        
        // Execute - Search for Java skill (case insensitive)
        List<TeamMember> results = teamMemberRepository.findBySkillsContainingIgnoreCase("java");
        
        // Verify - Should find both members
        assertThat(results).hasSize(2);
        assertThat(results).extracting(TeamMember::getUsername)
            .containsExactlyInAnyOrder("testuser", "leader");
    }
    
    @Test
    void testFindBySkillsContainingIgnoreCase_NoMatch() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        entityManager.flush();
        
        // Execute - Search for non-existent skill
        List<TeamMember> results = teamMemberRepository.findBySkillsContainingIgnoreCase("Python");
        
        // Verify - Should find no members
        assertThat(results).isEmpty();
    }
    
    @Test
    void testFindByName() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);     // firstName = "Test", lastName = "User"
        teamMemberRepository.save(leaderMember);   // firstName = "Team", lastName = "Leader"
        entityManager.flush();
        
        // Execute - Search by first name
        List<TeamMember> firstNameResults = teamMemberRepository.findByName("Test");
        
        // Verify first name search
        assertThat(firstNameResults).hasSize(1);
        assertThat(firstNameResults.get(0).getFirstName()).isEqualTo("Test");
        
        // Execute - Search by last name
        List<TeamMember> lastNameResults = teamMemberRepository.findByName("Leader");
        
        // Verify last name search
        assertThat(lastNameResults).hasSize(1);
        assertThat(lastNameResults.get(0).getLastName()).isEqualTo("Leader");
    }
    
    @Test
    void testCountBySubteam() {
        // Setup - Create subteams and members
        Subteam savedSubteam1 = entityManager.persistAndFlush(testSubteam);
        Subteam savedSubteam2 = entityManager.persistAndFlush(otherSubteam);
        
        testMember.setSubteam(savedSubteam1);
        leaderMember.setSubteam(savedSubteam1);
        
        // Create third member for different subteam
        TeamMember member3 = new TeamMember();
        member3.setUsername("member3");
        member3.setFirstName("Third");
        member3.setLastName("Member");
        member3.setEmail("third@example.com");
        member3.setSubteam(savedSubteam2);
        
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        teamMemberRepository.save(member3);
        entityManager.flush();
        
        // Execute
        long count1 = teamMemberRepository.countBySubteam(savedSubteam1);
        long count2 = teamMemberRepository.countBySubteam(savedSubteam2);
        
        // Verify
        assertThat(count1).isEqualTo(2);  // testMember and leaderMember
        assertThat(count2).isEqualTo(1);  // member3
    }
    
    @Test
    void testCountByLeaderTrue() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);     // leader = false
        teamMemberRepository.save(leaderMember);   // leader = true
        
        // Create additional leader
        TeamMember secondLeader = new TeamMember();
        secondLeader.setUsername("leader2");
        secondLeader.setFirstName("Second");
        secondLeader.setLastName("Leader");
        secondLeader.setEmail("leader2@example.com");
        secondLeader.setLeader(true);
        
        teamMemberRepository.save(secondLeader);
        entityManager.flush();
        
        // Execute
        long leaderCount = teamMemberRepository.countByLeaderTrue();
        
        // Verify
        assertThat(leaderCount).isEqualTo(2);  // leaderMember and secondLeader
    }
    
    @Test
    void testFindByUsernameOrEmail() {
        // Setup - Persist member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Search by username
        Optional<TeamMember> usernameResult = teamMemberRepository.findByUsernameOrEmail("testuser", "any@email.com");
        
        // Verify username search
        assertThat(usernameResult).isPresent();
        assertThat(usernameResult.get().getUsername()).isEqualTo("testuser");
        
        // Execute - Search by email
        Optional<TeamMember> emailResult = teamMemberRepository.findByUsernameOrEmail("anyuser", "test@example.com");
        
        // Verify email search
        assertThat(emailResult).isPresent();
        assertThat(emailResult.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    void testFindByUsernameOrEmail_NotFound() {
        // Setup - Persist member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Search for non-existent username and email
        Optional<TeamMember> result = teamMemberRepository.findByUsernameOrEmail("nonexistent", "nonexistent@email.com");
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindLeaders() {
        // Setup - Persist members
        teamMemberRepository.save(testMember);     // leader = false
        teamMemberRepository.save(leaderMember);   // leader = true
        entityManager.flush();
        
        // Execute
        List<TeamMember> results = teamMemberRepository.findLeaders();
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).isLeader()).isTrue();
        assertThat(results.get(0).getUsername()).isEqualTo("leader");
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testUniqueConstraint_Username() {
        // Setup - Persist first member
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Try to save member with same username
        TeamMember duplicateUsername = new TeamMember();
        duplicateUsername.setUsername("testuser");  // Same username
        duplicateUsername.setFirstName("Different");
        duplicateUsername.setLastName("User");
        duplicateUsername.setEmail("different@example.com");
        
        // Verify - Should throw constraint violation when flushed
        teamMemberRepository.save(duplicateUsername);
        
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.flush()
        );
    }
    
    @Test
    void testSubteamRelationship() {
        // Setup - Create subteam with members
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        
        // Add members to subteam using helper methods
        testMember.setSubteam(savedSubteam);
        leaderMember.setSubteam(savedSubteam);
        
        // Execute - Save members
        teamMemberRepository.save(testMember);
        teamMemberRepository.save(leaderMember);
        entityManager.flush();
        
        // Verify - Members are associated with subteam
        assertThat(testMember.getSubteam()).isNotNull();
        assertThat(testMember.getSubteam().getId()).isEqualTo(savedSubteam.getId());
        assertThat(leaderMember.getSubteam()).isNotNull();
        assertThat(leaderMember.getSubteam().getId()).isEqualTo(savedSubteam.getId());
        
        // Verify - Subteam relationship is bidirectional (if properly configured)
        List<TeamMember> subteamMembers = teamMemberRepository.findBySubteam(savedSubteam);
        assertThat(subteamMembers).hasSize(2);
        assertThat(subteamMembers).extracting(TeamMember::getUsername)
            .containsExactlyInAnyOrder("testuser", "leader");
    }
    
    @Test
    void testSubteamAssignmentChange() {
        // Setup - Create subteams and member
        Subteam savedSubteam1 = entityManager.persistAndFlush(testSubteam);
        Subteam savedSubteam2 = entityManager.persistAndFlush(otherSubteam);
        
        testMember.setSubteam(savedSubteam1);
        TeamMember savedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Verify initial assignment
        assertThat(savedMember.getSubteam().getId()).isEqualTo(savedSubteam1.getId());
        
        // Execute - Change subteam assignment
        savedMember.setSubteam(savedSubteam2);
        TeamMember updatedMember = teamMemberRepository.save(savedMember);
        entityManager.flush();
        
        // Verify - Subteam assignment changed
        assertThat(updatedMember.getSubteam().getId()).isEqualTo(savedSubteam2.getId());
        
        // Verify - Member counts are correct
        assertThat(teamMemberRepository.countBySubteam(savedSubteam1)).isEqualTo(0);
        assertThat(teamMemberRepository.countBySubteam(savedSubteam2)).isEqualTo(1);
    }
    
    @Test
    void testRemoveSubteamAssignment() {
        // Setup - Create subteam and member
        Subteam savedSubteam = entityManager.persistAndFlush(testSubteam);
        testMember.setSubteam(savedSubteam);
        TeamMember savedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Verify initial assignment
        assertThat(savedMember.getSubteam()).isNotNull();
        
        // Execute - Remove subteam assignment
        savedMember.setSubteam(null);
        TeamMember updatedMember = teamMemberRepository.save(savedMember);
        entityManager.flush();
        
        // Verify - No subteam assignment
        assertThat(updatedMember.getSubteam()).isNull();
        
        // Verify - Member appears in unassigned list
        List<TeamMember> unassignedMembers = teamMemberRepository.findBySubteamIsNull();
        assertThat(unassignedMembers).hasSize(1);
        assertThat(unassignedMembers.get(0).getUsername()).isEqualTo("testuser");
    }
    
    // ========== BUSINESS LOGIC VALIDATION ==========
    
    @Test
    void testFullNameGeneration() {
        // Setup - Persist member
        TeamMember savedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Get full name (business logic method)
        String fullName = savedMember.getFullName();
        
        // Verify
        assertThat(fullName).isEqualTo("Test User");
    }
    
    @Test
    void testSkillsManagement() {
        // Setup - Save member with initial skills
        teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Execute - Update skills
        testMember.setSkills("Java, Python, React, Leadership");
        TeamMember updatedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Verify - Skills updated
        assertThat(updatedMember.getSkills()).isEqualTo("Java, Python, React, Leadership");
        
        // Verify - Can search by new skills
        List<TeamMember> pythonDevelopers = teamMemberRepository.findBySkillsContainingIgnoreCase("Python");
        assertThat(pythonDevelopers).hasSize(1);
        assertThat(pythonDevelopers.get(0).getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void testLeadershipStatusManagement() {
        // Setup - Save member as non-leader
        testMember.setLeader(false);
        TeamMember savedMember = teamMemberRepository.save(testMember);
        entityManager.flush();
        
        // Verify initial non-leader status
        List<TeamMember> initialLeaders = teamMemberRepository.findByLeaderTrue();
        assertThat(initialLeaders).doesNotContain(savedMember);
        
        // Execute - Promote to leader
        savedMember.setLeader(true);
        teamMemberRepository.save(savedMember);
        entityManager.flush();
        
        // Verify - Now appears in leaders list
        List<TeamMember> updatedLeaders = teamMemberRepository.findByLeaderTrue();
        assertThat(updatedLeaders).hasSize(1);
        assertThat(updatedLeaders.get(0).getUsername()).isEqualTo("testuser");
        assertThat(updatedLeaders.get(0).isLeader()).isTrue();
    }
}