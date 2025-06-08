// src/test/java/org/frcpm/services/impl/TeamMemberServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for TeamMemberService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private TeamMemberServiceImpl teamMemberService;
    
    private TeamMember testMember;
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Initialize test objects
        testSubteam = createTestSubteam();
        testMember = createTestMember();
        
        // Configure mock repository responses
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.findAll()).thenReturn(List.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure subteam repository
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Create service with injected mocks
        teamMemberService = new TeamMemberServiceImpl(
            teamMemberRepository,
            subteamRepository
        );
    }
    
    /**
     * Creates a test subteam for use in tests.
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
    void testFindById() {
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
    void testFindAll() {
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
    void testSave() {
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
    void testDelete() {
        // Setup
        doNothing().when(teamMemberRepository).delete(any(TeamMember.class));
        
        // Execute
        teamMemberService.delete(testMember);
        
        // Verify repository was called
        verify(teamMemberRepository).delete(testMember);
    }
    
    @Test
    void testDeleteById() {
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
    void testCount() {
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
    void testFindByUsername() {
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
    void testFindByUsername_NotFound() {
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
    void testFindBySubteam() {
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
    void testFindBySkill() {
        // Setup
        when(teamMemberRepository.findBySkillsContainingIgnoreCase("Java")).thenReturn(List.of(testMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findBySkill("Java");
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMember, results.get(0));
        
        // Verify repository was called
        verify(teamMemberRepository).findBySkillsContainingIgnoreCase("Java");
    }
    
    @Test
    void testFindLeaders() {
        // Setup
        TeamMember leaderMember = new TeamMember("leader", "Leader", "User", "leader@example.com");
        leaderMember.setId(2L);
        leaderMember.setLeader(true);
        
        when(teamMemberRepository.findByIsLeaderTrue()).thenReturn(List.of(leaderMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findLeaders();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isLeader());
        
        // Verify repository was called
        verify(teamMemberRepository).findByIsLeaderTrue();
    }
    
    @Test
    void testCreateTeamMember() {
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
    void testCreateTeamMember_UsernameExists() {
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
    void testAssignToSubteam() {
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
    void testAssignToSubteam_NullSubteam() {
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
    void testAssignToSubteam_MemberNotFound() {
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
    void testAssignToSubteam_SubteamNotFound() {
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
    void testUpdateSkills() {
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
    void testUpdateSkills_MemberNotFound() {
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
    void testUpdateContactInfo() {
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
    void testUpdateContactInfo_MemberNotFound() {
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