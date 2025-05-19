// src/test/java/org/frcpm/repositories/specific/TeamMemberRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.SubteamRepositoryImpl;
import org.frcpm.repositories.impl.TeamMemberRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TeamMemberRepository implementation.
 */
public class TeamMemberRepositoryTest extends BaseRepositoryTest {
    
    private TeamMemberRepository teamMemberRepository;
    private SubteamRepository subteamRepository;
    
    private Subteam testSubteam;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        teamMemberRepository = new TeamMemberRepositoryImpl();
        subteamRepository = new SubteamRepositoryImpl();
        
        // Create test data
        createTestEntities();
    }
    
    @Override
    protected void setupTestData() {
        // No data setup by default - tests will create their own data
    }
    
    private void createTestEntities() {
        // Create and save a test subteam
        testSubteam = new Subteam();
        testSubteam.setName("Test Subteam");
        beginTransaction();
        em.persist(testSubteam);
        commitTransaction();
    }
    
    @Test
    public void testSaveAndFindById() {
        // Create a test team member
        TeamMember member = new TeamMember("testuser", "Test", "User", "test@example.com");
        member.setPhone("555-1234");
        member.setSkills("Java, JavaFX, Testing");
        
        // Save the team member
        TeamMember savedMember = teamMemberRepository.save(member);
        
        // Verify that ID was generated
        assertNotNull(savedMember.getId());
        
        // Find the team member by ID
        Optional<TeamMember> foundMember = teamMemberRepository.findById(savedMember.getId());
        
        // Verify that the team member was found
        assertTrue(foundMember.isPresent());
        assertEquals(savedMember.getId(), foundMember.get().getId());
        assertEquals("testuser", foundMember.get().getUsername());
        assertEquals("Test", foundMember.get().getFirstName());
        assertEquals("User", foundMember.get().getLastName());
        assertEquals("test@example.com", foundMember.get().getEmail());
        assertEquals("555-1234", foundMember.get().getPhone());
        assertEquals("Java, JavaFX, Testing", foundMember.get().getSkills());
    }
    
    @Test
    public void testFindAll() {
        // Create test team members
        TeamMember member1 = new TeamMember("user1", "First", "User", "first@example.com");
        TeamMember member2 = new TeamMember("user2", "Second", "User", "second@example.com");
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        
        // Find all team members
        List<TeamMember> members = teamMemberRepository.findAll();
        
        // Verify all team members were found
        assertTrue(members.size() >= 2);
    }
    
    @Test
    public void testUpdate() {
        // Create a test team member
        TeamMember member = new TeamMember("original", "Original", "Name", "original@example.com");
        
        // Save the team member
        TeamMember savedMember = teamMemberRepository.save(member);
        
        // Update the team member
        savedMember.setUsername("updated");
        savedMember.setFirstName("Updated");
        savedMember.setLastName("Name");
        savedMember.setEmail("updated@example.com");
        savedMember.setSkills("Updated Skills");
        
        // Save the updated team member
        TeamMember updatedMember = teamMemberRepository.save(savedMember);
        
        // Find the team member by ID
        Optional<TeamMember> foundMember = teamMemberRepository.findById(updatedMember.getId());
        
        // Verify that the team member was updated
        assertTrue(foundMember.isPresent());
        assertEquals("updated", foundMember.get().getUsername());
        assertEquals("Updated", foundMember.get().getFirstName());
        assertEquals("Name", foundMember.get().getLastName());
        assertEquals("updated@example.com", foundMember.get().getEmail());
        assertEquals("Updated Skills", foundMember.get().getSkills());
    }
    
    @Test
    public void testDelete() {
        // Create a test team member
        TeamMember member = new TeamMember("todelete", "To", "Delete", "delete@example.com");
        
        // Save the team member
        TeamMember savedMember = teamMemberRepository.save(member);
        
        // Verify that the team member exists
        Optional<TeamMember> foundBeforeDelete = teamMemberRepository.findById(savedMember.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the team member
        teamMemberRepository.delete(savedMember);
        
        // Verify that the team member was deleted
        Optional<TeamMember> foundAfterDelete = teamMemberRepository.findById(savedMember.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // Create a test team member
        TeamMember member = new TeamMember("todeleteid", "ToDeleteId", "User", "deleteid@example.com");
        
        // Save the team member
        TeamMember savedMember = teamMemberRepository.save(member);
        
        // Verify that the team member exists
        Optional<TeamMember> foundBeforeDelete = teamMemberRepository.findById(savedMember.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the team member by ID
        boolean deleted = teamMemberRepository.deleteById(savedMember.getId());
        
        // Verify that deletion was successful
        assertTrue(deleted);
        
        // Verify that the team member was deleted
        Optional<TeamMember> foundAfterDelete = teamMemberRepository.findById(savedMember.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testCount() {
        // Get initial count
        long initialCount = teamMemberRepository.count();
        
        // Create test team members
        TeamMember member1 = new TeamMember("count1", "Count", "One", "count1@example.com");
        TeamMember member2 = new TeamMember("count2", "Count", "Two", "count2@example.com");
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        
        // Verify updated count
        assertEquals(initialCount + 2, teamMemberRepository.count());
        
        // Delete a team member
        teamMemberRepository.delete(member1);
        
        // Verify updated count after deletion
        assertEquals(initialCount + 1, teamMemberRepository.count());
    }
    
    @Test
    public void testFindByUsername() {
        // Create test team members
        TeamMember member1 = new TeamMember("uniqueusername", "Unique", "User", "unique@example.com");
        TeamMember member2 = new TeamMember("differentusername", "Different", "User", "different@example.com");
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        
        // Find team member by username
        Optional<TeamMember> foundMember = teamMemberRepository.findByUsername("uniqueusername");
        
        // Verify that the correct team member was found
        assertTrue(foundMember.isPresent());
        assertEquals("uniqueusername", foundMember.get().getUsername());
        assertEquals("Unique", foundMember.get().getFirstName());
        
        // Test finding with non-existent username
        Optional<TeamMember> notFound = teamMemberRepository.findByUsername("nonexistentusername");
        
        // Verify that no team member was found
        assertFalse(notFound.isPresent());
    }
    
    @Test
    public void testFindBySubteam() {
        // Create a second subteam
        Subteam anotherSubteam = new Subteam();
        anotherSubteam.setName("Another Subteam");
        beginTransaction();
        em.persist(anotherSubteam);
        commitTransaction();
        
        // Create team members with different subteams
        TeamMember member1 = new TeamMember("subteam1", "Subteam", "One", "subteam1@example.com");
        member1.setSubteam(testSubteam);
        
        TeamMember member2 = new TeamMember("subteam2", "Subteam", "Two", "subteam2@example.com");
        member2.setSubteam(testSubteam);
        
        TeamMember member3 = new TeamMember("subteam3", "Subteam", "Three", "subteam3@example.com");
        member3.setSubteam(anotherSubteam);
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        teamMemberRepository.save(member3);
        
        // Find team members by subteam
        List<TeamMember> subteamMembers = teamMemberRepository.findBySubteam(testSubteam);
        
        // Verify that only team members in the specified subteam were found
        assertEquals(2, subteamMembers.size());
        assertTrue(subteamMembers.stream().allMatch(m -> m.getSubteam().getId().equals(testSubteam.getId())));
        
        // Find team members for the other subteam
        List<TeamMember> otherSubteamMembers = teamMemberRepository.findBySubteam(anotherSubteam);
        
        // Verify that only team members in the other subteam were found
        assertEquals(1, otherSubteamMembers.size());
        assertEquals("subteam3", otherSubteamMembers.get(0).getUsername());
    }
    
    @Test
    public void testFindBySkill() {
        // Create team members with different skills
        TeamMember member1 = new TeamMember("skill1", "Skill", "One", "skill1@example.com");
        member1.setSkills("Java, SQL, UX Design");
        
        TeamMember member2 = new TeamMember("skill2", "Skill", "Two", "skill2@example.com");
        member2.setSkills("Python, SQL, Data Analysis");
        
        TeamMember member3 = new TeamMember("skill3", "Skill", "Three", "skill3@example.com");
        member3.setSkills("C++, Robotics");
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        teamMemberRepository.save(member3);
        
        // Find team members by skill
        List<TeamMember> sqlMembers = teamMemberRepository.findBySkill("SQL");
        
        // Verify that only team members with the specified skill were found
        assertEquals(2, sqlMembers.size());
        assertTrue(sqlMembers.stream().anyMatch(m -> m.getUsername().equals("skill1")));
        assertTrue(sqlMembers.stream().anyMatch(m -> m.getUsername().equals("skill2")));
        
        // Find team members by another skill
        List<TeamMember> roboticsMembers = teamMemberRepository.findBySkill("Robotics");
        
        // Verify that only team members with the specified skill were found
        assertEquals(1, roboticsMembers.size());
        assertEquals("skill3", roboticsMembers.get(0).getUsername());
    }
    
    @Test
    public void testFindLeaders() {
        // Create team members with different leader status
        TeamMember member1 = new TeamMember("leader1", "Leader", "One", "leader1@example.com");
        member1.setLeader(true);
        
        TeamMember member2 = new TeamMember("leader2", "Leader", "Two", "leader2@example.com");
        member2.setLeader(true);
        
        TeamMember member3 = new TeamMember("nonleader", "Non", "Leader", "nonleader@example.com");
        member3.setLeader(false);
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        teamMemberRepository.save(member3);
        
        // Find team members who are leaders
        List<TeamMember> leaders = teamMemberRepository.findLeaders();
        
        // Verify that only team members who are leaders were found
        assertTrue(leaders.size() >= 2);
        assertTrue(leaders.stream().allMatch(TeamMember::isLeader));
        assertTrue(leaders.stream().anyMatch(m -> m.getUsername().equals("leader1")));
        assertTrue(leaders.stream().anyMatch(m -> m.getUsername().equals("leader2")));
        assertFalse(leaders.stream().anyMatch(m -> m.getUsername().equals("nonleader")));
    }
    
    @Test
    public void testFindByName() {
        // Create team members with different names
        TeamMember member1 = new TeamMember("john", "John", "Smith", "john@example.com");
        TeamMember member2 = new TeamMember("jane", "Jane", "Smith", "jane@example.com");
        TeamMember member3 = new TeamMember("bob", "Bob", "Johnson", "bob@example.com");
        
        // Save team members
        teamMemberRepository.save(member1);
        teamMemberRepository.save(member2);
        teamMemberRepository.save(member3);
        
        // Find team members by first name
        List<TeamMember> johnsOrJanes = teamMemberRepository.findByName("J");
        
        // Verify that team members with names containing "J" were found
        assertTrue(johnsOrJanes.size() >= 2);
        assertTrue(johnsOrJanes.stream().anyMatch(m -> m.getUsername().equals("john")));
        assertTrue(johnsOrJanes.stream().anyMatch(m -> m.getUsername().equals("jane")));
        
        // Find team members by last name
        List<TeamMember> smiths = teamMemberRepository.findByName("Smith");
        
        // Verify that team members with the last name "Smith" were found
        assertEquals(2, smiths.size());
        assertTrue(smiths.stream().anyMatch(m -> m.getUsername().equals("john")));
        assertTrue(smiths.stream().anyMatch(m -> m.getUsername().equals("jane")));
    }
    
    @Test
    public void testFullNameGeneration() {
        // Create team members with different name configurations
        TeamMember member1 = new TeamMember("user1", "First", "Last", "user1@example.com");
        TeamMember member2 = new TeamMember("user2", "First", null, "user2@example.com");
        TeamMember member3 = new TeamMember("user3", null, "Last", "user3@example.com");
        TeamMember member4 = new TeamMember("user4", null, null, "user4@example.com");
        
        // Save team members
        member1 = teamMemberRepository.save(member1);
        member2 = teamMemberRepository.save(member2);
        member3 = teamMemberRepository.save(member3);
        member4 = teamMemberRepository.save(member4);
        
        // Verify full name generation
        assertEquals("First Last", member1.getFullName());
        assertEquals("First", member2.getFullName());
        assertEquals("Last", member3.getFullName());
        assertEquals("user4", member4.getFullName());
    }
}