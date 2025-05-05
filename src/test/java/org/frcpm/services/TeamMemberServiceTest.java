package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.utils.TestDatabaseCleaner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberServiceTest {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberServiceTest.class.getName());
    
    private TeamMemberService teamMemberService;
    private SubteamService subteamService;
    private TeamMemberRepository teamMemberRepository;
    private SubteamRepository subteamRepository;
    
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        // Initialize database in development mode for clean state
        DatabaseConfig.initialize(true);
        
        // Get services and repositories
        teamMemberService = ServiceFactory.getTeamMemberService();
        subteamService = ServiceFactory.getSubteamService();
        teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
        subteamRepository = RepositoryFactory.getSubteamRepository();
        
        // Clean the database first
        TestDatabaseCleaner.clearTestDatabase();
        
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
        // Clean up test data - using direct entity manager for more control
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // Delete team members first
            em.createQuery("DELETE FROM TeamMember m WHERE m.username LIKE 'servicetest%'")
              .executeUpdate();
            
            // Delete test subteam
            em.createQuery("DELETE FROM Subteam t WHERE t.id = :id")
              .setParameter("id", testSubteam.getId())
              .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error during test cleanup: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Shutdown database
        DatabaseConfig.shutdown();
    }
    
    private void createTestTeamMembers() {
        teamMemberService.createTeamMember(
            "servicetestuser1",
            "Service",
            "Test1",
            "servicetest1@example.com",
            "555-1234",
            true
        );
        
        teamMemberService.createTeamMember(
            "servicetestuser2",
            "Service",
            "Test2",
            "servicetest2@example.com",
            "555-5678",
            false
        );
    }
    
    @Test
    public void testFindAll() {
        List<TeamMember> members = teamMemberService.findAll();
        assertNotNull(members);
        assertTrue(members.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a member ID from the DB
        List<TeamMember> members = teamMemberService.findAll();
        TeamMember firstMember = members.stream()
            .filter(m -> m.getUsername().startsWith("servicetest"))
            .findFirst().orElseThrow();
        
        // Now test findById
        TeamMember found = teamMemberService.findById(firstMember.getId());
        assertNotNull(found);
        assertEquals(firstMember.getUsername(), found.getUsername());
    }
    
    @Test
    public void testFindByUsername() {
        Optional<TeamMember> member = teamMemberService.findByUsername("servicetestuser1");
        assertTrue(member.isPresent());
        assertEquals("servicetestuser1", member.get().getUsername());
        assertEquals("Service", member.get().getFirstName());
        assertEquals("Test1", member.get().getLastName());
    }
    
    @Test
    public void testFindLeaders() {
        List<TeamMember> leaders = teamMemberService.findLeaders();
        assertFalse(leaders.isEmpty());
        assertTrue(leaders.stream().allMatch(TeamMember::isLeader));
        assertTrue(leaders.stream().anyMatch(m -> m.getUsername().equals("servicetestuser1")));
    }
    
    @Test
    public void testCreateTeamMember() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestcreate" + System.currentTimeMillis();
        
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Create", 
            "servicecreate@example.com", 
            "555-CREATE", 
            false
        );
        
        assertNotNull(created.getId());
        assertEquals(uniqueUsername, created.getUsername());
        assertEquals("Service", created.getFirstName());
        assertEquals("Create", created.getLastName());
        assertEquals("servicecreate@example.com", created.getEmail());
        assertEquals("555-CREATE", created.getPhone());
        assertFalse(created.isLeader());
        
        // Verify it was saved using repository directly
        Optional<TeamMember> found = teamMemberRepository.findByUsername(uniqueUsername);
        assertTrue(found.isPresent());
        assertEquals(uniqueUsername, found.get().getUsername());
    }
    
    @Test
    public void testAssignToSubteam() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestassign" + System.currentTimeMillis();
        
        // Create a member
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Assign", 
            "serviceassign@example.com", 
            "555-ASSIGN", 
            false
        );
        
        // Assign to subteam
        TeamMember updated = teamMemberService.assignToSubteam(created.getId(), testSubteam.getId());
        
        // Verify the assignment
        assertNotNull(updated);
        assertNotNull(updated.getSubteam());
        assertEquals(testSubteam.getId(), updated.getSubteam().getId());
        
        // Check in DB using repository directly
        Optional<TeamMember> found = teamMemberRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getSubteam());
        assertEquals(testSubteam.getId(), found.get().getSubteam().getId());
        
        // Verify the member is in the subteam's member list using a new transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Get fresh subteam with initialized collections
            Subteam freshSubteam = em.find(Subteam.class, testSubteam.getId());
            
            // Check if member is in the subteam's members list
            boolean memberFound = false;
            for (TeamMember member : freshSubteam.getMembers()) {
                if (member.getId().equals(updated.getId())) {
                    memberFound = true;
                    break;
                }
            }
            
            tx.commit();
            
            assertTrue(memberFound, "Member should be in the subteam's members list");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testUpdateSkills() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestskills" + System.currentTimeMillis();
        
        // Create a member
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Skills", 
            "serviceskills@example.com", 
            "555-SKILLS", 
            false
        );
        
        // Update skills
        TeamMember updated = teamMemberService.updateSkills(
            created.getId(), 
            "Java, Python, Testing"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("Java, Python, Testing", updated.getSkills());
        
        // Check in DB using repository directly
        Optional<TeamMember> found = teamMemberRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Java, Python, Testing", found.get().getSkills());
    }
    
    @Test
    public void testUpdateContactInfo() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestcontact" + System.currentTimeMillis();
        
        // Create a member
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Contact", 
            "servicecontact@example.com", 
            "555-CONTACT", 
            false
        );
        
        // Update contact info
        TeamMember updated = teamMemberService.updateContactInfo(
            created.getId(), 
            "newcontact@example.com", 
            "555-NEW"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("newcontact@example.com", updated.getEmail());
        assertEquals("555-NEW", updated.getPhone());
        
        // Check in DB using repository directly
        Optional<TeamMember> found = teamMemberRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("newcontact@example.com", found.get().getEmail());
        assertEquals("555-NEW", found.get().getPhone());
    }
    
    @Test
    public void testDeleteById() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestdelete" + System.currentTimeMillis();
        
        // Create a member
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Delete", 
            "servicedelete@example.com", 
            "555-DELETE", 
            false
        );
        Long id = created.getId();
        
        // Delete the member
        boolean result = teamMemberService.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion using repository directly
        Optional<TeamMember> found = teamMemberRepository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testRemoveFromSubteam() {
        // Use a timestamp to ensure unique username
        String uniqueUsername = "servicetestremove" + System.currentTimeMillis();
        
        // Create a member and assign to subteam
        TeamMember created = teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Remove", 
            "serviceremove@example.com", 
            "555-REMOVE", 
            false
        );
        TeamMember assigned = teamMemberService.assignToSubteam(created.getId(), testSubteam.getId());
        
        assertNotNull(assigned.getSubteam());
        
        // Remove from subteam by assigning null
        TeamMember removed = teamMemberService.assignToSubteam(assigned.getId(), null);
        
        // Verify removal
        assertNotNull(removed);
        assertNull(removed.getSubteam());
        
        // Check in DB using repository directly
        Optional<TeamMember> found = teamMemberRepository.findById(removed.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getSubteam());
        
        // Verify the member is no longer in the subteam's member list using a new transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Get fresh subteam with initialized collections
            Subteam freshSubteam = em.find(Subteam.class, testSubteam.getId());
            
            // Check if member is in the subteam's members list
            boolean memberFound = false;
            for (TeamMember member : freshSubteam.getMembers()) {
                if (member.getId().equals(removed.getId())) {
                    memberFound = true;
                    break;
                }
            }
            
            tx.commit();
            
            assertFalse(memberFound, "Member should not be in the subteam's members list");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testInvalidTeamMemberCreation() {
        // Test null username
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.createTeamMember(
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
        String uniqueUsername = "servicetestduplicate" + System.currentTimeMillis(); 
        
        // Create first member
        teamMemberService.createTeamMember(
            uniqueUsername, 
            "Service", 
            "Duplicate", 
            "serviceduplicate@example.com", 
            "555-DUPLICATE", 
            false
        );
        
        // Try to create duplicate
        exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.createTeamMember(
                uniqueUsername, // Same username
                "Service", 
                "Duplicate2", 
                "serviceduplicate2@example.com", 
                "555-DUPLICATE2", 
                false
            );
        });
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    public void testFindBySubteam() {
        // Use a timestamp to ensure unique usernames
        String uniqueUsername1 = "servicetestsubteam1" + System.currentTimeMillis();
        String uniqueUsername2 = "servicetestsubteam2" + System.currentTimeMillis();
        
        // Create two members and assign one to the test subteam
        TeamMember member1 = teamMemberService.createTeamMember(
            uniqueUsername1, 
            "Service", 
            "Subteam1", 
            "servicesubteam1@example.com", 
            "555-SUBTEAM1", 
            false
        );
        teamMemberService.assignToSubteam(member1.getId(), testSubteam.getId());
        
        TeamMember member2 = teamMemberService.createTeamMember(
            uniqueUsername2, 
            "Service", 
            "Subteam2", 
            "servicesubteam2@example.com", 
            "555-SUBTEAM2", 
            false
        );
        // Intentionally not assigning member2 to a subteam
        
        // Find by subteam
        List<TeamMember> found = teamMemberService.findBySubteam(testSubteam);
        assertFalse(found.isEmpty());
        
        // Verify member1 is in the results and member2 is not
        boolean foundMember1 = false;
        boolean foundMember2 = false;
        
        for (TeamMember member : found) {
            if (member.getUsername().equals(uniqueUsername1)) {
                foundMember1 = true;
            }
            if (member.getUsername().equals(uniqueUsername2)) {
                foundMember2 = true;
            }
        }
        
        assertTrue(foundMember1, "Member assigned to subteam should be found");
        assertFalse(foundMember2, "Member not assigned to subteam should not be found");
    }
}