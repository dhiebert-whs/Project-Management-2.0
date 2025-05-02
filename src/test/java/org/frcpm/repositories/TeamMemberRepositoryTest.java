package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
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
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TestEnvironmentSetup.class})
public class TeamMemberRepositoryTest {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberRepositoryTest.class.getName());
    
    private TeamMemberRepository repository;
    private SubteamRepository subteamRepository;
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        // Force development mode for testing
        System.setProperty("app.db.dev", "true");
        
        // Initialize a clean database for each test
        DatabaseConfig.reinitialize(true);
        
        repository = RepositoryFactory.getTeamMemberRepository();
        subteamRepository = RepositoryFactory.getSubteamRepository();
        
        // Create test subteam in a transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            testSubteam = new Subteam("Test Subteam", "#FF0000");
            
            em.persist(testSubteam);
            tx.commit();
            
            // Create test team members in their own transaction
            createTestTeamMembers();
            
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error setting up test data: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to set up test data: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    @AfterEach
    public void tearDown() {
        try {
            // Clean up test data in reverse order
            cleanupTestTeamMembers();
            
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Subteam subteam = em.find(Subteam.class, testSubteam.getId());
                if (subteam != null) {
                    em.remove(subteam);
                }
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                LOGGER.warning("Error cleaning up test subteam: " + e.getMessage());
            } finally {
                em.close();
            }
        } finally {
            DatabaseConfig.shutdown();
        }
    }
    
    private void createTestTeamMembers() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Get a fresh reference to the subteam
            Subteam subteam = em.find(Subteam.class, testSubteam.getId());
            
            // Use unique usernames with UUID to avoid conflicts
            TeamMember member1 = new TeamMember("testuser1_" + UUID.randomUUID().toString().substring(0, 8), 
                "Test", "User1", "test1@example.com");
            member1.setPhone("555-1234");
            member1.setSkills("Java, Python");
            member1.setLeader(true);
            member1.setSubteam(subteam);
            
            TeamMember member2 = new TeamMember("testuser2_" + UUID.randomUUID().toString().substring(0, 8), 
                "Test", "User2", "test2@example.com");
            member2.setPhone("555-5678");
            member2.setSkills("CAD, Design");
            member2.setLeader(false);
            member2.setSubteam(subteam);
            
            em.persist(member1);
            em.persist(member2);
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating test team members: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create test team members: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    private void cleanupTestTeamMembers() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Use a native query to avoid cache issues - delete anything with testuser in the name
            em.createQuery("DELETE FROM TeamMember tm WHERE tm.username LIKE 'testuser%'")
                .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error cleaning up test team members: " + e.getMessage());
        } finally {
            em.close();
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
            .findFirst().orElseThrow(() -> new AssertionError("No test users found"));
        
        // Now test findById
        Optional<TeamMember> found = repository.findById(firstMember.getId());
        assertTrue(found.isPresent());
        assertEquals(firstMember.getUsername(), found.get().getUsername());
    }
    
    @Test
    public void testFindByUsername() {
        // First, get a member from the DB to get a valid username
        List<TeamMember> members = repository.findAll();
        TeamMember member = members.stream()
            .filter(m -> m.getUsername().startsWith("testuser1"))
            .findFirst().orElseThrow(() -> new AssertionError("No test users found"));
            
        Optional<TeamMember> found = repository.findByUsername(member.getUsername());
        assertTrue(found.isPresent());
        assertTrue(found.get().getUsername().startsWith("testuser1"));
        assertEquals("Test", found.get().getFirstName());
        assertEquals("User1", found.get().getLastName());
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
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        TeamMember saved = null;
        
        try {
            tx.begin();
            
            // Generate a unique username with UUID
            String uniqueUsername = "testsave_" + UUID.randomUUID().toString().substring(0, 8);
            TeamMember newMember = new TeamMember(uniqueUsername, "Test", "Save", "testsave@example.com");
            newMember.setPhone("555-SAVE");
            newMember.setSkills("Testing");
            newMember.setLeader(false);
            
            em.persist(newMember);
            tx.commit();
            
            saved = newMember;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error saving test team member: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to save test team member: " + e.getMessage());
        } finally {
            em.close();
        }
        
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<TeamMember> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().getUsername().startsWith("testsave_"));
    }
    
    @Test
    public void testUpdate() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        TeamMember saved = null;
        
        try {
            tx.begin();
            
            // Generate a unique username with UUID
            String uniqueUsername = "testupdate_" + UUID.randomUUID().toString().substring(0, 8);
            
            // First, create a member
            TeamMember member = new TeamMember(uniqueUsername, "Test", "Update", "testupdate@example.com");
            
            em.persist(member);
            em.flush();
            
            // Now update it
            member.setFirstName("Updated");
            member.setSkills("Updated Skills");
            
            em.merge(member);
            tx.commit();
            
            saved = member;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error updating test team member: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to update test team member: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Verify the update through the repository
        Optional<TeamMember> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getFirstName());
        assertEquals("Updated Skills", found.get().getSkills());
    }
    
    @Test
    public void testDelete() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        TeamMember saved = null;
        
        try {
            tx.begin();
            
            // Generate a unique username with UUID
            String uniqueUsername = "testdelete_" + UUID.randomUUID().toString().substring(0, 8);
            
            // First, create a member
            TeamMember member = new TeamMember(uniqueUsername, "Test", "Delete", "testdelete@example.com");
            
            em.persist(member);
            tx.commit();
            
            saved = member;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating team member for delete test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create team member for delete test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<TeamMember> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        TeamMember saved = null;
        
        try {
            tx.begin();
            
            // Generate a unique username with UUID
            String uniqueUsername = "testdeletebyid_" + UUID.randomUUID().toString().substring(0, 8);
            
            // First, create a member
            TeamMember member = new TeamMember(uniqueUsername, "Test", "DeleteById", "testdeletebyid@example.com");
            
            em.persist(member);
            tx.commit();
            
            saved = member;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating team member for deleteById test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create team member for deleteById test: " + e.getMessage());
        } finally {
            em.close();
        }
        
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
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        TeamMember saved = null;
        
        try {
            tx.begin();
            
            // Generate a unique username with UUID
            String uniqueUsername = "testcount_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Add a new member
            TeamMember member = new TeamMember(uniqueUsername, "Test", "Count", "testcount@example.com");
            
            em.persist(member);
            tx.commit();
            
            saved = member;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating team member for count test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create team member for count test: " + e.getMessage());
        } finally {
            em.close();
        }
        
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