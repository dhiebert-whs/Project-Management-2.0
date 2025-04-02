package org.frcpm.viewmodels;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TeamViewModel.
 */
public class TeamViewModelTest {

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private SubteamService subteamService;

    private TeamViewModel viewModel;
    private TeamMember testMember;
    private Subteam testSubteam;
    private List<TeamMember> testMembers;
    private List<Subteam> testSubteams;
    private List<TeamMember> testSubteamMembers;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);
        testSubteam.setSpecialties("Programming, Electronics");

        testMember = new TeamMember("testuser", "John", "Doe", "john.doe@example.com");
        testMember.setId(1L);
        testMember.setPhone("555-1234");
        testMember.setSkills("Java, C++");
        testMember.setLeader(true);
        testMember.setSubteam(testSubteam);

        testMembers = new ArrayList<>();
        testMembers.add(testMember);

        testSubteams = new ArrayList<>();
        testSubteams.add(testSubteam);

        testSubteamMembers = new ArrayList<>();
        testSubteamMembers.add(testMember);

        // Create ViewModel
        viewModel = new TeamViewModel(teamMemberService, subteamService);
    }

    @Test
    public void testInitialState() {
        // Verify initial state
        assertEquals("", viewModel.getMemberUsername());
        assertEquals("", viewModel.getMemberFirstName());
        assertEquals("", viewModel.getMemberLastName());
        assertEquals("", viewModel.getMemberEmail());
        assertEquals("", viewModel.getMemberPhone());
        assertEquals("", viewModel.getMemberSkills());
        assertFalse(viewModel.getMemberIsLeader());
        assertNull(viewModel.getMemberSubteam());

        assertEquals("", viewModel.getSubteamName());
        assertEquals("#0000FF", viewModel.getSubteamColorCode());
        assertEquals("", viewModel.getSubteamSpecialties());

        assertTrue(viewModel.getMembers().isEmpty());
        assertTrue(viewModel.getSubteams().isEmpty());
        assertTrue(viewModel.getSubteamMembers().isEmpty());

        assertFalse(viewModel.isMemberValid());
        assertFalse(viewModel.isSubteamValid());
    }

    @Test
    public void testLoadMembers() {
        // Configure mock
        when(teamMemberService.findAll()).thenReturn(testMembers);

        // Load members
        viewModel.getLoadMembersCommand().execute();

        // Verify members are loaded
        assertEquals(1, viewModel.getMembers().size());
        assertEquals("testuser", viewModel.getMembers().get(0).getUsername());
    }

    @Test
    public void testLoadSubteams() {
        // Configure mock
        when(subteamService.findAll()).thenReturn(testSubteams);

        // Load subteams
        viewModel.getLoadSubteamsCommand().execute();

        // Verify subteams are loaded
        assertEquals(1, viewModel.getSubteams().size());
        assertEquals("Test Subteam", viewModel.getSubteams().get(0).getName());
    }

    @Test
    public void testInitNewMember() {
        // Initialize new member
        viewModel.initNewMember();

        // Verify state
        assertTrue(viewModel.isNewMember());
        assertEquals("", viewModel.getMemberUsername());
        assertEquals("", viewModel.getMemberFirstName());
        assertEquals("", viewModel.getMemberLastName());
        assertEquals("", viewModel.getMemberEmail());
        assertEquals("", viewModel.getMemberPhone());
        assertEquals("", viewModel.getMemberSkills());
        assertFalse(viewModel.getMemberIsLeader());
        assertNull(viewModel.getMemberSubteam());
        assertFalse(viewModel.isMemberValid());
    }

    @Test
    public void testInitExistingMember() {
        // Initialize existing member
        viewModel.initExistingMember(testMember);

        // Verify state
        assertFalse(viewModel.isNewMember());
        assertEquals("testuser", viewModel.getMemberUsername());
        assertEquals("John", viewModel.getMemberFirstName());
        assertEquals("Doe", viewModel.getMemberLastName());
        assertEquals("john.doe@example.com", viewModel.getMemberEmail());
        assertEquals("555-1234", viewModel.getMemberPhone());
        assertEquals("Java, C++", viewModel.getMemberSkills());
        assertTrue(viewModel.getMemberIsLeader());
        assertEquals(testSubteam, viewModel.getMemberSubteam());
    }

    @Test
    public void testInitNewSubteam() {
        // Initialize new subteam
        viewModel.initNewSubteam();

        // Verify state
        assertTrue(viewModel.isNewSubteam());
        assertEquals("", viewModel.getSubteamName());
        assertEquals("#0000FF", viewModel.getSubteamColorCode());
        assertEquals("", viewModel.getSubteamSpecialties());
        assertFalse(viewModel.isSubteamValid());
    }

    @Test
    public void testInitExistingSubteam() {
        // Configure mock
        when(teamMemberService.findBySubteam(any(Subteam.class))).thenReturn(testSubteamMembers);

        // Initialize existing subteam
        viewModel.initExistingSubteam(testSubteam);

        // Verify state
        assertFalse(viewModel.isNewSubteam());
        assertEquals("Test Subteam", viewModel.getSubteamName());
        assertEquals("#FF0000", viewModel.getSubteamColorCode());
        assertEquals("Programming, Electronics", viewModel.getSubteamSpecialties());

        // Verify subteam members loaded
        assertEquals(1, viewModel.getSubteamMembers().size());
        assertEquals("testuser", viewModel.getSubteamMembers().get(0).getUsername());
    }

    @Test
    public void testMemberValidation_Valid() {
        // Set valid values
        viewModel.setMemberUsername("testuser");
        viewModel.setMemberFirstName("John");
        viewModel.setMemberLastName("Doe");
        viewModel.setMemberEmail("john.doe@example.com");

        // Check validation
        assertTrue(viewModel.isMemberValid());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testMemberValidation_MissingUsername() {
        // Set values with missing username
        viewModel.setMemberUsername("");
        viewModel.setMemberFirstName("John");
        viewModel.setMemberLastName("Doe");
        viewModel.setMemberEmail("john.doe@example.com");

        // Check validation
        assertFalse(viewModel.isMemberValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Username is required"));
    }

    @Test
    public void testMemberValidation_MissingFirstName() {
        // Set values with missing first name
        viewModel.setMemberUsername("testuser");
        viewModel.setMemberFirstName("");
        viewModel.setMemberLastName("Doe");
        viewModel.setMemberEmail("john.doe@example.com");

        // Check validation
        assertFalse(viewModel.isMemberValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("First name is required"));
    }

    @Test
    public void testMemberValidation_InvalidEmail() {
        // Set values with invalid email
        viewModel.setMemberUsername("testuser");
        viewModel.setMemberFirstName("John");
        viewModel.setMemberLastName("Doe");
        viewModel.setMemberEmail("invalid-email");

        // Check validation
        assertFalse(viewModel.isMemberValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Email must be a valid email address"));
    }

    @Test
    public void testSubteamValidation_Valid() {
        // Set valid values
        viewModel.setSubteamName("Test Subteam");
        viewModel.setSubteamColorCode("#FF0000");

        // Check validation
        assertTrue(viewModel.isSubteamValid());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testSubteamValidation_MissingName() {
        // Set values with missing name
        viewModel.setSubteamName("");
        viewModel.setSubteamColorCode("#FF0000");

        // Check validation
        assertFalse(viewModel.isSubteamValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Subteam name is required"));
    }

    @Test
    public void testSubteamValidation_InvalidColorCode() {
        // Set values with invalid color code
        viewModel.setSubteamName("Test Subteam");
        viewModel.setSubteamColorCode("invalid");

        // Check validation
        assertFalse(viewModel.isSubteamValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Color code must be a valid hex color code"));
    }

    @Test
    public void testSaveMemberCommand_NewMember() {
        // Configure mocks
        when(teamMemberService.createTeamMember(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyBoolean()))
                .thenReturn(testMember);
        when(teamMemberService.updateSkills(anyLong(), anyString())).thenReturn(testMember);
        when(teamMemberService.assignToSubteam(anyLong(), anyLong())).thenReturn(testMember);

        // Set valid values for new member
        viewModel.initNewMember();
        viewModel.setMemberUsername("testuser");
        viewModel.setMemberFirstName("John");
        viewModel.setMemberLastName("Doe");
        viewModel.setMemberEmail("john.doe@example.com");
        viewModel.setMemberPhone("555-1234");
        viewModel.setMemberSkills("Java, C++");
        viewModel.setMemberIsLeader(true);
        viewModel.setMemberSubteam(testSubteam);

        // Save member
        viewModel.getSaveMemberCommand().execute();

        // Verify service calls
        verify(teamMemberService).createTeamMember(
                eq("testuser"), eq("John"), eq("Doe"), eq("john.doe@example.com"), eq("555-1234"), eq(true));
        verify(teamMemberService).updateSkills(anyLong(), eq("Java, C++"));
        verify(teamMemberService).assignToSubteam(anyLong(), eq(1L));
    }

    @Test
    public void testSaveMemberCommand_ExistingMember() {
        // Configure mocks
        when(teamMemberService.save(any(TeamMember.class))).thenReturn(testMember);
        when(teamMemberService.updateContactInfo(anyLong(), anyString(), anyString())).thenReturn(testMember);
        when(teamMemberService.updateSkills(anyLong(), anyString())).thenReturn(testMember);
        when(teamMemberService.assignToSubteam(anyLong(), anyLong())).thenReturn(testMember);

        // Set up existing member
        viewModel.initExistingMember(testMember);

        // Modify member
        viewModel.setMemberFirstName("Jane");
        viewModel.setMemberEmail("jane.doe@example.com");

        // Save member
        viewModel.getSaveMemberCommand().execute();

        // Verify service calls
        verify(teamMemberService).save(any(TeamMember.class));
        verify(teamMemberService).updateContactInfo(eq(1L), eq("jane.doe@example.com"), eq("555-1234"));
        verify(teamMemberService).updateSkills(eq(1L), eq("Java, C++"));
        verify(teamMemberService).assignToSubteam(eq(1L), eq(1L));
    }

    @Test
    public void testDeleteMemberCommand() {
        // Configure mocks
        doNothing().when(teamMemberService).deleteById(anyLong());

        // Load members
        when(teamMemberService.findAll()).thenReturn(testMembers);
        viewModel.getLoadMembersCommand().execute();

        // Set up existing member
        viewModel.initExistingMember(testMember);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteMemberCommand().canExecute());

        // Delete member
        viewModel.getDeleteMemberCommand().execute();

        // Verify service calls
        verify(teamMemberService).deleteById(eq(1L));

        // After deletion, the member list should be empty
        assertEquals(0, viewModel.getMembers().size());

        // And the delete command should no longer be executable
        assertFalse(viewModel.getDeleteMemberCommand().canExecute());
    }

    @Test
    public void testSaveSubteamCommand_NewSubteam() {
        // Configure mocks
        when(subteamService.createSubteam(anyString(), anyString(), anyString())).thenReturn(testSubteam);

        // Set valid values for new subteam
        viewModel.initNewSubteam();
        viewModel.setSubteamName("Test Subteam");
        viewModel.setSubteamColorCode("#FF0000");
        viewModel.setSubteamSpecialties("Programming, Electronics");

        // Save subteam
        viewModel.getSaveSubteamCommand().execute();

        // Verify service calls
        verify(subteamService).createSubteam(eq("Test Subteam"), eq("#FF0000"), eq("Programming, Electronics"));
    }

    @Test
    public void testSaveSubteamCommand_ExistingSubteam() {
        // Configure mocks
        when(subteamService.updateColorCode(anyLong(), anyString())).thenReturn(testSubteam);
        when(subteamService.updateSpecialties(anyLong(), anyString())).thenReturn(testSubteam);
        when(teamMemberService.findBySubteam(any(Subteam.class))).thenReturn(testSubteamMembers);

        // Set up existing subteam
        viewModel.initExistingSubteam(testSubteam);

        // Modify subteam
        viewModel.setSubteamColorCode("#00FF00");
        viewModel.setSubteamSpecialties("Mechanical, Electronics");

        // Save subteam
        viewModel.getSaveSubteamCommand().execute();

        // Verify service calls
        verify(subteamService).updateColorCode(eq(1L), eq("#00FF00"));
        verify(subteamService).updateSpecialties(eq(1L), eq("Mechanical, Electronics"));
    }

    @Test
    public void testDeleteSubteamCommand_WithNoMembers() {
        // Configure mocks
        doNothing().when(subteamService).deleteById(anyLong());
        when(teamMemberService.findBySubteam(any(Subteam.class))).thenReturn(new ArrayList<>());

        // Load subteams
        when(subteamService.findAll()).thenReturn(testSubteams);
        viewModel.getLoadSubteamsCommand().execute();

        // Set up existing subteam
        viewModel.initExistingSubteam(testSubteam);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteSubteamCommand().canExecute());

        // Delete subteam
        viewModel.getDeleteSubteamCommand().execute();

        // Verify service calls
        verify(subteamService).deleteById(eq(1L));

        // After deletion, the subteam list should be empty
        assertEquals(0, viewModel.getSubteams().size());

        // And the delete command should no longer be executable
        assertFalse(viewModel.getDeleteSubteamCommand().canExecute());
    }

    @Test
    public void testDeleteSubteamCommand_WithMembers() {
        // Configure mocks
        when(teamMemberService.findBySubteam(any(Subteam.class))).thenReturn(testSubteamMembers);

        // Load subteams
        when(subteamService.findAll()).thenReturn(testSubteams);
        viewModel.getLoadSubteamsCommand().execute();

        // Set up existing subteam
        viewModel.initExistingSubteam(testSubteam);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteSubteamCommand().canExecute());

        // Delete subteam
        viewModel.getDeleteSubteamCommand().execute();

        // Verify service calls - deleteById should NOT be called
        verify(subteamService, never()).deleteById(anyLong());

        // The error message should be set
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Cannot delete subteam that has members"));
    }

    @Test
    public void testLoadSubteamMembersCommand() {
        // Configure mocks
        when(teamMemberService.findBySubteam(any(Subteam.class))).thenReturn(testSubteamMembers);

        // Initially the load subteam members command should not be executable
        assertFalse(viewModel.getLoadSubteamMembersCommand().canExecute());

        // Set up existing subteam
        viewModel.setSelectedSubteam(testSubteam);

        // Now the load subteam members command should be executable
        assertTrue(viewModel.getLoadSubteamMembersCommand().canExecute());

        // Clear subteam members
        viewModel.getSubteamMembers().clear();
        assertEquals(0, viewModel.getSubteamMembers().size());

        // Load subteam members
        viewModel.getLoadSubteamMembersCommand().execute();

        // Verify subteam members are loaded
        assertEquals(1, viewModel.getSubteamMembers().size());
        assertEquals("testuser", viewModel.getSubteamMembers().get(0).getUsername());

        // Verify service calls
        verify(teamMemberService, times(2)).findBySubteam(eq(testSubteam));
    }
}