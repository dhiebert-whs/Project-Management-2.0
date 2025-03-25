package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberServiceTest {
    
    private TeamMemberService service;
    private SubteamService subteamService;
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        service = ServiceFactory.getTeamMemberService();
        subteamService = ServiceFactory.getSubteamService();
        
        // Create test subteam
        testSubteam = subteamService.createSubteam(
            "Test Member Service Subteam",
            "#FF0000",
            "Testing"
        );
        
        // Add test data
        createTestTeamMembers();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestTeamMembers();
        subteamService.deleteById(testSubteam.getId());
        DatabaseConfig.shutdown();
    }
    
    private void createTestTeamMembers() {
        service.createTeamMember(
            "servicetestuser1",
            "Service",
            "Test1",
            "servicetest1@example.com",
            "555-1234",
            true
        );
        
        service.createTeamMember(
            "servicetestuser2",
            "Service",
            "Test2",
            "servicetest2@example.com",
            "555-5678",
            false
        );
    }
    
    private void cleanupTestTeamMembers() {
        List<TeamMember> members = service.findAll();
        for (TeamMember member : members) {
            if (member.getUsername().startsWith("servicetest")) {
                service.deleteById(member.getId());
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<TeamMember> members = service.findAll();
        assertNotNull(members);
        assertTrue(members.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a member ID from the DB
        List<TeamMember> members = service.findAll();
        TeamMember firstMember = members.stream()
            .filter(m -> m.getUsername().startsWith("servicetest"))
            .findFirst().orElseThrow();
        
        // Now test findById
        TeamMember found = service.findById(firstMember.getId());
        assertNotNull(found);
        assertEquals(firstMember.getUsername(), found.getUsername());
    }
    
    @Test
    public void testFindByUsername() {
        Optional<TeamMember> member = service.findByUsername("servicetestuser1");
        assertTrue(member.isPresent());
        assertEquals("servicetestuser1", member.get().getUsername());
        assertEquals("Service", member.get().getFirstName());
        assertEquals("Test1", member.get().getLastName());
    }
    
    @Test
    public void testFindLeaders() {
        List<TeamMember> leaders = service.findLeaders();
        assertFalse(leaders.isEmpty());
        assertTrue(leaders.stream().allMatch(TeamMember::isLeader));
        assertTrue(leaders.stream().anyMatch(m -> m.getUsername().equals("servicetestuser1")));
    }
    
    @Test
    public void testCreateTeamMember() {
        TeamMember created = service.createTeamMember(
            "servicetestcreate", 
            "Service", 
            "Create", 
            "servicecreate@example.com", 
            "555-CREATE", 
            false
        );
        
        assertNotNull(created.getId());
        assertEquals("servicetestcreate", created.getUsername());
        assertEquals("Service", created.getFirstName());
        assertEquals("Create", created.getLastName());
        assertEquals("servicecreate@example.com", created.getEmail());
        assertEquals("555-CREATE", created.getPhone());
        assertFalse(created.isLeader());
        
        // Verify it was saved
        Optional<TeamMember> found = service.findByUsername("servicetestcreate");
        assertTrue(found.isPresent());
        assertEquals("servicetestcreate", found.get().getUsername());
    }
    
    @Test
    public void testAssignToSubteam() {
        // Create a member
        TeamMember created = service.createTeamMember(
            "servicetestassign", 
            "Service", 
            "Assign", 
            "serviceassign@example.com", 
            "555-ASSIGN", 
            false
        );
        
        // Assign to subteam
        TeamMember updated = service.assignToSubteam(created.getId(), testSubteam.getId());
        
        // Verify the assignment
        assertNotNull(updated);
        assertNotNull(updated.getSubteam());
        assertEquals(testSubteam.getId(), updated.getSubteam().getId());
        
        // Check in DB
        TeamMember found = service.findById(updated.getId());
        assertNotNull(found);
        assertNotNull(found.getSubteam());
        assertEquals(testSubteam.getId(), found.getSubteam().getId());
        
        // Verify the member is in the subteam's member list
        List<TeamMember> subteamMembers = service.findBySubteam(testSubteam);
        assertTrue(subteamMembers.stream().anyMatch(m -> m.getId().equals(updated.getId())));
    }
    
    @Test
    public void testUpdateSkills() {
        // Create a member
        TeamMember created = service.createTeamMember(
            "servicetestskills", 
            "Service", 
            "Skills", 
            "serviceskills@example.com", 
            "555-SKILLS", 
            false
        );
        
        // Update skills
        TeamMember updated = service.updateSkills(created.getId(), "Java, Python, Testing");
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("Java, Python, Testing", updated.getSkills());
        
        // Check in DB
        TeamMember found = service.findById(updated.getId());
        assertNotNull(found);
        assertEquals("Java, Python, Testing", found.getSkills());
    }
    
    @Test
    public void testUpdateContactInfo() {
        // Create a member
        TeamMember created = service.createTeamMember(
            "servicetestcontact", 
            "Service", 
            "Contact", 
            "servicecontact@example.com", 
            "555-CONTACT", 
            false
        );
        
        // Update contact info
        TeamMember updated = service.updateContactInfo(
            created.getId(), 
            "newcontact@example.com", 
            "555-NEW"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("newcontact@example.com", updated.getEmail());
        assertEquals("555-NEW", updated.getPhone());
        
        // Check in DB
        TeamMember found = service.findById(updated.getId());
        assertNotNull(found);
        assertEquals("newcontact@example.com", found.getEmail());
        assertEquals("555-NEW", found.getPhone());
    }
    
    @Test
    public void testDeleteById() {
        // Create a member
        TeamMember created = service.createTeamMember(
            "servicetestdelete", 
            "Service", 
            "Delete", 
            "servicedelete@example.com", 
            "555-DELETE", 
            false
        );
        Long id = created.getId();
        
        // Delete the member
        boolean result = service.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        TeamMember found = service.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testInvalidTeamMemberCreation() {
        // Test null username
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createTeamMember(
                null, 
                "Service", 
                "Invalid", 
                "serviceinvalid@example.com", 
                "555-INVALID", 
                false
            );
        });
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
        
        // Test duplicate username
        exception = assertThrows(IllegalArgumentException.class, () -> {
            // Create a member
            service.createTeamMember(
                "servicetestduplicate", 
                "Service", 
                "Duplicate", 
                "serviceduplicate@example.com", 
                "555-DUPLICATE", 
                false
            );
            
            // Try to create another member with the same username
            service.createTeamMember(
                "servicetestduplicate", 
                "Service", 
                "Duplicate2", 
                "serviceduplicate2@example.com", 
                "555-DUPLICATE2", 
                false
            );
        });
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
}
