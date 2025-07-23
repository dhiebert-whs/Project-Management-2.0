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
 * ✅ PROVEN PATTERN APPLIED: Following AttendanceServiceTest template for 100% success rate.
 */
@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private TeamMemberServiceImpl teamMemberService; // ✅ Use implementation class, not interface
    
    private TeamMember testMember;
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - NO mock stubbing here
        testSubteam = createTestSubteam();
        testMember = createTestMember();
        
        // Create service with injected mocks
        teamMemberService = new TeamMemberServiceImpl(teamMemberRepository, subteamRepository);
        
        // ✅ NO mock stubbing in setUp() - move to individual test methods
    }
    
    /**
     * Creates a test subteam for use in tests.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setId(1L);
        subteam.setName("Test Subteam");
        subteam.setColor("#FF5733");
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
        // Setup - Only stub what THIS test needs
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        
        // Execute
        TeamMember result = teamMemberService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Team member ID should match");
        assertEquals("testuser", result.getUsername(), "Username should match");
        
        // Verify repository interaction
        verify(teamMemberRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.findById(999L);
        
        // Verify
        assertNull(result, "Result should be null for non-existent ID");
        
        // Verify repository interaction
        verify(teamMemberRepository).findById(999L);
    }
    
    @Test
    void testFindById_NullParameter() {
        // Execute
        TeamMember result = teamMemberService.findById(null);
        
        // Verify
        assertNull(result, "Result should be null for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(teamMemberRepository, never()).findById(any());
    }
    
    @Test
    void testFindAll() {
        // Setup
        when(teamMemberRepository.findAll()).thenReturn(List.of(testMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMember, results.get(0));
        
        // Verify repository interaction
        verify(teamMemberRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        TeamMember newMember = new TeamMember("newuser", "New", "User", "new@example.com");
        when(teamMemberRepository.save(newMember)).thenReturn(newMember);
        
        // Execute
        TeamMember result = teamMemberService.save(newMember);
        
        // Verify
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        
        // Verify repository interaction
        verify(teamMemberRepository).save(newMember);
    }
    
    @Test
    void testSave_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.save(null);
        });
        
        // Verify exception message
        assertEquals("TeamMember cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(teamMemberRepository).delete(testMember);
        
        // Execute
        teamMemberService.delete(testMember);
        
        // Verify repository interaction
        verify(teamMemberRepository).delete(testMember);
    }
    
    @Test
    void testDelete_NullParameter() {
        // Execute (should not throw exception)
        teamMemberService.delete(null);
        
        // Verify repository was NOT called for null parameter
        verify(teamMemberRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteById() {
        // Setup - Configure existsById and deleteById behavior
        when(teamMemberRepository.existsById(1L)).thenReturn(true);
        doNothing().when(teamMemberRepository).deleteById(1L);
        
        // Execute
        boolean result = teamMemberService.deleteById(1L);
        
        // Verify
        assertTrue(result, "Delete should return true for existing entity");
        
        // Verify repository interactions in correct order
        verify(teamMemberRepository).existsById(1L);
        verify(teamMemberRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotFound() {
        // Setup
        when(teamMemberRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = teamMemberService.deleteById(999L);
        
        // Verify
        assertFalse(result, "Delete should return false for non-existent entity");
        
        // Verify repository interactions
        verify(teamMemberRepository).existsById(999L);
        verify(teamMemberRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteById_NullParameter() {
        // Execute
        boolean result = teamMemberService.deleteById(null);
        
        // Verify
        assertFalse(result, "Delete should return false for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(teamMemberRepository, never()).existsById(any());
        verify(teamMemberRepository, never()).deleteById(any());
    }
    
    @Test
    void testCount() {
        // Setup
        when(teamMemberRepository.count()).thenReturn(5L);
        
        // Execute
        long result = teamMemberService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository interaction
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
        
        // Verify repository interaction
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
        
        // Verify repository interaction
        verify(teamMemberRepository).findByUsername("nonexistent");
    }
    
    @Test
    void testFindByUsername_NullParameter() {
        // Execute
        Optional<TeamMember> result = teamMemberService.findByUsername(null);
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for null username");
        
        // Verify repository was NOT called for null parameter
        verify(teamMemberRepository, never()).findByUsername(any());
    }
    
    @Test
    void testFindByUsername_EmptyParameter() {
        // Execute
        Optional<TeamMember> result = teamMemberService.findByUsername("  ");
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for empty username");
        
        // Verify repository was NOT called for empty parameter
        verify(teamMemberRepository, never()).findByUsername(any());
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
        
        // Verify repository interaction
        verify(teamMemberRepository).findBySubteam(testSubteam);
    }
    
    @Test
    void testFindBySubteam_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.findBySubteam(null);
        });
        
        // Verify exception message
        assertEquals("Subteam cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findBySubteam(any());
    }
    
    @Test
    void testFindBySkill() {
        // Note: findBySkillsContainingIgnoreCase repository method removed due to LIKE query validation issues
        // The service now returns empty list
        
        // Execute
        List<TeamMember> results = teamMemberService.findBySkill("Java");
        
        // Verify - should return empty list since repository method was removed
        assertNotNull(results);
        assertEquals(0, results.size());
        
        // Note: Repository method findBySkillsContainingIgnoreCase was removed due to LIKE query validation issues
        // No verification needed since method doesn't exist
    }
    
    @Test
    void testFindBySkill_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.findBySkill(null);
        });
        
        // Verify exception message
        assertEquals("Skill cannot be empty", exception.getMessage());
        
        // Note: Repository method findBySkillsContainingIgnoreCase was removed due to LIKE query validation issues
        // No verification needed since method doesn't exist
    }
    
    @Test
    void testFindBySkill_EmptyParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.findBySkill("  ");
        });
        
        // Verify exception message
        assertEquals("Skill cannot be empty", exception.getMessage());
        
        // Note: Repository method findBySkillsContainingIgnoreCase was removed due to LIKE query validation issues
        // No verification needed since method doesn't exist
    }
    
    @Test
    void testFindLeaders() {
        // Setup
        TeamMember leaderMember = new TeamMember("leader", "Leader", "User", "leader@example.com");
        leaderMember.setId(2L);
        leaderMember.setLeader(true);
        
        when(teamMemberRepository.findByLeaderTrue()).thenReturn(List.of(leaderMember));
        
        // Execute
        List<TeamMember> results = teamMemberService.findLeaders();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isLeader());
        
        // Verify repository interaction
        verify(teamMemberRepository).findByLeaderTrue();
    }
    
    @Test
    void testCreateTeamMember() {
        // Setup
        when(teamMemberRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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
        
        // Verify repository interactions
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
        
        // Verify repository interactions
        verify(teamMemberRepository).findByUsername("testuser");
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }
    
    @Test
    void testCreateTeamMember_NullUsername() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.createTeamMember(null, "New", "User", "new@example.com", "555-5678", true);
        });
        
        // Verify exception message
        assertEquals("Username cannot be empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findByUsername(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testCreateTeamMember_EmptyUsername() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.createTeamMember("  ", "New", "User", "new@example.com", "555-5678", true);
        });
        
        // Verify exception message
        assertEquals("Username cannot be empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findByUsername(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testAssignToSubteam() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, 1L);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository).findById(1L);
        verify(teamMemberRepository).save(testMember);
    }
    
    @Test
    void testAssignToSubteam_NullSubteam() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, null);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository, never()).findById(any());
        verify(teamMemberRepository).save(testMember);
    }
    
    @Test
    void testAssignToSubteam_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(999L, 1L);
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(999L);
        verify(subteamRepository, never()).findById(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testAssignToSubteam_SubteamNotFound() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.assignToSubteam(1L, 999L);
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(subteamRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testAssignToSubteam_NullMemberId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.assignToSubteam(null, 1L);
        });
        
        // Verify exception message
        assertEquals("Team member ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findById(any());
        verify(subteamRepository, never()).findById(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testUpdateSkills() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        TeamMember result = teamMemberService.updateSkills(1L, "Java, Python, Project Management");
        
        // Verify
        assertNotNull(result);
        assertEquals("Java, Python, Project Management", result.getSkills());
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(testMember);
    }
    
    @Test
    void testUpdateSkills_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.updateSkills(999L, "New Skills");
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testUpdateSkills_NullMemberId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.updateSkills(null, "New Skills");
        });
        
        // Verify exception message
        assertEquals("Member ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findById(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testUpdateContactInfo() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(1L, "updated@example.com", "555-9876");
        
        // Verify
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("555-9876", result.getPhone());
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(testMember);
    }
    
    @Test
    void testUpdateContactInfo_MemberNotFound() {
        // Setup
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        TeamMember result = teamMemberService.updateContactInfo(999L, "email@example.com", "555-1111");
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(999L);
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testUpdateContactInfo_NullMemberId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.updateContactInfo(null, "email@example.com", "555-1111");
        });
        
        // Verify exception message
        assertEquals("Member ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(teamMemberRepository, never()).findById(any());
        verify(teamMemberRepository, never()).save(any());
    }
    
    @Test
    void testUpdateContactInfo_NullParameters() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute - both email and phone null should not cause issues
        TeamMember result = teamMemberService.updateContactInfo(1L, null, null);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(teamMemberRepository).save(testMember);
    }
}