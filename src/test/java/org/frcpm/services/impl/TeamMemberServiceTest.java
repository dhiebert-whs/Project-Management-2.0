// src/test/java/org/frcpm/services/impl/TeamMemberServiceTest.java

package org.frcpm.services.impl;

import de.saxsys.mvvmfx.MvvmFX;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.BaseServiceTest;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for TeamMemberService implementation using TestableTeamMemberServiceImpl.
 */
public class TeamMemberServiceTest extends BaseServiceTest {
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private TeamMemberService teamMemberService;
    
    private TeamMember testMember;
    private Subteam testSubteam;
    
    @Override
    protected void setupTestData() {
        // Initialize test objects
        testSubteam = createTestSubteam();
        testMember = createTestMember();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Configure mock repository responses
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.findAll()).thenReturn(List.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure subteam repository
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Create service with injected mocks
        teamMemberService = new TestableTeamMemberServiceImpl(
            teamMemberRepository,
            subteamRepository
        );
        
        // Configure MVVMFx dependency injector for comprehensive testing
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == TeamMemberRepository.class) return teamMemberRepository;
            if (type == SubteamRepository.class) return subteamRepository;
            if (type == TeamMemberServiceImpl.class) return teamMemberService;
            return null;
        });
    }
    
    @AfterEach
    @Override
    public void tearDown() throws Exception {
        // Clear MVVMFx dependency injector
        MvvmFX.setCustomDependencyInjector(null);
        
        // Call parent tearDown
        super.tearDown();
    }
    
    /**
     * Creates a test subteam for use in tests.
     * 
     * @return a test subteam
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setId(1L);
        subteam.setName("Test Subteam");
        subteam.setColorCode("#FF5733");
        return subteam;
    }
    
    /**
     * Creates a test member for use in tests.
     * 
     * @return a test member
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember("testuser", "Test", "User", "test@example.com");
        member.setId(1L);
        member.setPhone("555-1234");
        member.setSkills("Java, Testing");
        member.setLeader(false);
        return member;
    }
    
    @Test
    public void testFindById() {
        // Reset mocks to ensure clean test state
        reset(teamMemberRepository);
        
        // Setup - create a special member for this test
        TeamMember uniqueMember = new TeamMember("unique_user", "Unique", "User", "unique@example.com");
        uniqueMember.setId(1L);
        
        // Configure fresh mock behavior for this test
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(uniqueMember));
        
        // Execute
        TeamMember result = teamMemberService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Team member ID should match");
        assertEquals("unique_user", result.getUsername(), "Username should match exactly");
        
        // Verify repository was called exactly once with the correct ID
        verify(teamMemberRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testFindAll() {
        // Execute
        List<TeamMember> results = teamMemberService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(teamMemberRepository).findAll();
    }
    
    @Test
    public void testSave() {
        // Setup
        TeamMember newMember = new TeamMember("newuser", "New", "User", "new@example.com");
        
        // Execute
        TeamMember result = teamMemberService.save(newMember);
        
        // Verify
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        
        // Verify repository was called
        verify(teamMemberRepository).save(newMember);
    }
    
    @Test
    public void testDelete() {
        // Setup
        doNothing().when(teamMemberRepository).delete(any(TeamMember.class));
        
        // Execute
        teamMemberService.delete(testMember);
        
        // Verify repository was called
        verify(teamMemberRepository).delete(testMember);
    }
    
    @Test
    public void testDeleteById() {
        // Setup
        when(teamMemberRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = teamMemberService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(teamMemberRepository).deleteById(1L);
    }
    
    @Test
    public void testCount() {
        // Setup
        when(teamMemberRepository.count()).thenReturn(5L);
        
        // Execute
        long result = teamMemberService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(teamMemberRepository).count();
    }
    
    @Test
    public void testFindByUsername() {
        // Setup
        when(teamMemberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        
        // Execute
        Optional<TeamMember> result = teamMemberService.findByUsername("testuser");
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        
        // Verify repository was called
        verify(teamMemberRepository).findByUsername("testuser");
    }
    
    @Test
    public void testFindByUsername_NotFound() {
        // Setup
        when(teamMemberRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // Execute
        Optional<TeamMember> result = teamMemberService.findByUsername("nonexistent");
        
        // Verify
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(teamMemberRepository).findByUsername("nonexistent");
    }
    
    @Test
    public void testFindBySubteam() {
        // Setup
        when(teamMemberRepository.findBySubteam(testSubteam)).thenReturn(List.of(testMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findBySubteam(testSubteam);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMember, results.get(0));
        
        // Verify repository was called
        verify(teamMemberRepository).findBySubteam(testSubteam);
    }
    
    @Test
    public void testFindBySkill() {
        // Setup
        when(teamMemberRepository.findBySkill("Java")).thenReturn(List.of(testMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findBySkill("Java");
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMember, results.get(0));
        
        // Verify repository was called
        verify(teamMemberRepository).findBySkill("Java");
    }
    
    @Test
    public void testFindLeaders() {
        // Setup
        TeamMember leaderMember = new TeamMember("leader", "Leader", "User", "leader@example.com");
        leaderMember.setId(2L);
        leaderMember.setLeader(true);
        
        when(teamMemberRepository.findLeaders()).thenReturn(List.of(leaderMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findLeaders();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isLeader());
        
        // Verify repository was called
        verify(teamMemberRepository).findLeaders();
    }
    
    @Test
    public void testCreateTeamMember() {
        // Setup
        when(teamMemberRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.createTeamMember(
            "newuser",
            "New",
            "User",
            "new@example.com",
            "555-5678",
            true
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("555-5678", result.getPhone());
        assertTrue(result.isLeader());
        
        // Verify repository was called
        verify(teamMemberRepository).findByUsername("newuser");
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testCreateTeamMember_UsernameExists() {
        // Setup
        when(teamMemberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.createTeamMember(
                "testuser",
                "Updated",
                "User",
                "updated@example.com",
                "555-9876",
                true
            );
        });
        
        // Verify exception message
        assertEquals("Username already exists", exception.getMessage());
        
        // Verify repository calls
        verify(teamMemberRepository).findByUsername("testuser");
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
    
    @Test
    public void testCreateTeamMember_UsernameExists_TestEnvironment() {
        // Setup
        System.setProperty("test.environment", "true");
        when(teamMemberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        
        try {
            // Execute
            TeamMember result = teamMemberService.createTeamMember(
                "testuser",
                "Updated",
                "User",
                "updated@example.com",
                "555-9876",
                true
            );
            
            // Verify
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("Updated", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals("updated@example.com", result.getEmail());
            assertEquals("555-9876", result.getPhone());
            assertTrue(result.isLeader());
            
            // Verify repository calls
            verify(teamMemberRepository).findByUsername("testuser");
            verify(teamMemberRepository).save(any(TeamMember.class));
        } finally {
            // Clean up
            System.clearProperty("test.environment");
        }
    }
    
    @Test
    public void testAssignToSubteam() {
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, 1L);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository).findById(1L);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testAssignToSubteam_NullSubteam() {
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, null);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository, never()).findById(anyLong());
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testAssignToSubteam_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(999L, 1L);
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(999L);
        verify(subteamRepository, never()).findById(anyLong());
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
    

    @Test
    public void testAssignToSubteam_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, 999L);
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateSkills() {
        // Execute
        TeamMember result = teamMemberService.updateSkills(1L, "Java, Python, Project Management");
        
        // Verify
        assertNotNull(result);
        assertEquals("Java, Python, Project Management", result.getSkills());
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateSkills_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.updateSkills(999L, "New Skills");
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateContactInfo() {
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(1L, "updated@example.com", "555-9876");
        
        // Verify
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("555-9876", result.getPhone());
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateContactInfo_EmailOnly() {
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(1L, "updated@example.com", null);
        
        // Verify
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("555-1234", result.getPhone()); // Original phone number unchanged
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateContactInfo_PhoneOnly() {
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(1L, null, "555-9876");
        
        // Verify
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail()); // Original email unchanged
        assertEquals("555-9876", result.getPhone());
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }
    
    @Test
    public void testUpdateContactInfo_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(999L, "email@example.com", "555-1111");
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(teamMemberRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
}