package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberRepositoryTest {
    
    private TeamMemberRepository repository;
    private SubteamRepository subteamRepository;
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getTeamMemberRepository();
        subteamRepository = RepositoryFactory.getSubteamRepository();
        
        // Create test subteam
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam = subteamRepository.save(testSubteam);
        
        // Add test data
        createTestTeamMembers();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestTeamMembers();
        subteamRepository.delete(testSubteam);
        DatabaseConfig.shutdown();
    }
    
    private void createTestTeamMembers() {
        TeamMember member1 = new TeamMember("testuser1", "Test", "User1", "test1@example.com");
        member1.setPhone("555-1234");
        member1.setSkills("Java, Python");
        member1.setLeader(true);
        member1.setSubteam(testSubteam);
        
        TeamMember member2 = new TeamMember("testuser2", "Test", "User2", "test2@example.com");
        member2.setPhone("555-5678");
        member2.setSkills("CAD, Design");
        member2.setLeader(false);
        member2.setSubteam(testSubteam);
        
        repository.save(member1);
        repository.save(member2);
    }
    
    private void cleanupTestTeamMembers() {
        List<TeamMember> members = repository.findAll();
        for (TeamMember member : members) {
            if (member.getUsername().startsWith("testuser")) {
                repository.delete(member);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<TeamMember> members = repository.findAll();
        assertNotNull(members);
        assertTrue(members.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a member ID from the DB
        List<TeamMember> members = repository.findAll();
        TeamMember firstMember = members.stream()
            .filter(m -> m.getUsername().startsWith("testuser"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<TeamMember> found = repository.findById(firstMember.getId());
        assertTrue(found.isPresent());
        assertEquals(firstMember.getUsername(), found.get().getUsername());
    }
    
    @Test
    public void testFindByUsername() {
        Optional<TeamMember> member = repository.findByUsername("testuser1");
        assertTrue(member.isPresent());
        assertEquals("testuser1", member.get().getUsername());
        assertEquals("Test", member.get().getFirstName());
        assertEquals("User1", member.get().getLastName());
    }
    
    @Test
    public void testFindBySubteam() {
        List<TeamMember> members = repository.findBySubteam(testSubteam);
        assertFalse(members.isEmpty());
        assertTrue(members.stream().allMatch(m -> m.getSubteam().getId().equals(testSubteam.getId())));
    }
    
    @Test
    public void testFindBySkill() {
        List<TeamMember> javaMembers = repository.findBySkill("Java");
        assertFalse(javaMembers.isEmpty());
        assertTrue(javaMembers.stream().anyMatch(m -> m.getSkills().contains("Java")));
        
        List<TeamMember> cadMembers = repository.findBySkill("CAD");
        assertFalse(cadMembers.isEmpty());
        assertTrue(cadMembers.stream().anyMatch(m -> m.getSkills().contains("CAD")));
    }
    
    @Test
    public void testFindLeaders() {
        List<TeamMember> leaders = repository.findLeaders();
        assertFalse(leaders.isEmpty());
        assertTrue(leaders.stream().allMatch(TeamMember::isLeader));
    }
    
    @Test
    public void testSave() {
        TeamMember newMember = new TeamMember("testsave", "Test", "Save", "testsave@example.com");
        newMember.setPhone("555-SAVE");
        newMember.setSkills("Testing");
        newMember.setLeader(false);
        
        TeamMember saved = repository.save(newMember);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<TeamMember> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testsave", found.get().getUsername());
    }
    
    @Test
    public void testUpdate() {
        // First, create a member
        TeamMember member = new TeamMember("testupdate", "Test", "Update", "testupdate@example.com");
        TeamMember saved = repository.save(member);
        
        // Now update it
        saved.setFirstName("Updated");
        saved.setSkills("Updated Skills");
        TeamMember updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated", updated.getFirstName());
        assertEquals("Updated Skills", updated.getSkills());
        
        // Check in DB
        Optional<TeamMember> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getFirstName());
        assertEquals("Updated Skills", found.get().getSkills());
    }
    
    @Test
    public void testDelete() {
        // First, create a member
        TeamMember member = new TeamMember("testdelete", "Test", "Delete", "testdelete@example.com");
        TeamMember saved = repository.save(member);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<TeamMember> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a member
        TeamMember member = new TeamMember("testdeletebyid", "Test", "DeleteById", "testdeletebyid@example.com");
        TeamMember saved = repository.save(member);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<TeamMember> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new member
        TeamMember member = new TeamMember("testcount", "Test", "Count", "testcount@example.com");
        repository.save(member);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    public void testFindByName() {
        List<TeamMember> members = repository.findByName("User");
        assertFalse(members.isEmpty());
        
        for (TeamMember member : members) {
            assertTrue(member.getFirstName().contains("User") || member.getLastName().contains("User"));
        }
    }
}
