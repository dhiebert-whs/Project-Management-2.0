package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.TeamViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeamController using the MVVM pattern.
 */
public class TeamControllerTest {

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private SubteamService subteamService;

    private TeamController controller;
    private TeamViewModel viewModel;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create sample data
        List<TeamMember> members = createSampleMembers();
        List<Subteam> subteams = createSampleSubteams();
        
        // Configure mock services
        when(teamMemberService.findAll()).thenReturn(members);
        when(subteamService.findAll()).thenReturn(subteams);
        
        // Create TeamViewModel with mock services
        viewModel = new TeamViewModel(teamMemberService, subteamService);
        
        // Create controller instance
        controller = new TeamController();
        
        // Inject mocked ViewModel
        setField(controller, "viewModel", viewModel);
    }

    @Test
    public void testInitializeViewModel() {
        // Verify the ViewModel was initialized
        assertNotNull(viewModel);
        
        // Verify initial lists were loaded
        verify(teamMemberService).findAll();
        verify(subteamService).findAll();
        
        // Verify the lists are populated
        assertEquals(2, viewModel.getMembers().size());
        assertEquals(2, viewModel.getSubteams().size());
    }

    @Test
    public void testInitNewMember() {
        // Call the method
        viewModel.initNewMember();
        
        // Verify state
        assertTrue(viewModel.isNewMember());
        assertEquals("", viewModel.getMemberUsername());
        assertEquals("", viewModel.getMemberFirstName());
        assertEquals("", viewModel.getMemberLastName());
        assertFalse(viewModel.getMemberIsLeader());
        assertNull(viewModel.getMemberSubteam());
        assertFalse(viewModel.isDirty());
    }

    @Test
    public void testInitExistingMember() {
        // Get a sample member
        TeamMember member = viewModel.getMembers().get(0);
        
        // Call the method
        viewModel.initExistingMember(member);
        
        // Verify state
        assertFalse(viewModel.isNewMember());
        assertEquals(member.getUsername(), viewModel.getMemberUsername());
        assertEquals(member.getFirstName(), viewModel.getMemberFirstName());
        assertEquals(member.getLastName(), viewModel.getMemberLastName());
        assertEquals(member.getEmail(), viewModel.getMemberEmail());
        assertEquals(member.isLeader(), viewModel.getMemberIsLeader());
        assertEquals(member.getSubteam(), viewModel.getMemberSubteam());
        assertFalse(viewModel.isDirty());
    }

    @Test
    public void testSaveMember_Create() {
        // Setup test data
        viewModel.initNewMember();
        viewModel.setMemberUsername("newuser");
        viewModel.setMemberFirstName("New");
        viewModel.setMemberLastName("User");
        viewModel.setMemberEmail("new@example.com");
        viewModel.setMemberIsLeader(true);
        
        // Mock service response
        TeamMember newMember = new TeamMember("newuser", "New", "User", "new@example.com");
        newMember.setId(3L);
        newMember.setLeader(true);
        
        when(teamMemberService.createTeamMember(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(newMember);
        when(teamMemberService.updateContactInfo(anyLong(), anyString(), anyString()))
            .thenReturn(newMember);
        when(teamMemberService.updateSkills(anyLong(), anyString()))
            .thenReturn(newMember);
        
        // Execute command
        viewModel.getSaveMemberCommand().execute();
        
        // Verify service calls
        verify(teamMemberService).createTeamMember(
            eq("newuser"), eq("New"), eq("User"), eq("new@example.com"), anyString(), eq(true));
        
        // Verify the member was added to the list
        boolean found = false;
        for (TeamMember member : viewModel.getMembers()) {
            if (member.getUsername().equals("newuser")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "New member should be added to the list");
    }

    @Test
    public void testSaveMember_Update() {
        // Get a sample member
        TeamMember member = viewModel.getMembers().get(0);
        member.setId(1L);
        
        // Setup for edit
        viewModel.initExistingMember(member);
        
        // Change some properties
        viewModel.setMemberFirstName("Updated");
        viewModel.setMemberEmail("updated@example.com");
        
        // Mock service response
        TeamMember updatedMember = new TeamMember(member.getUsername(), "Updated", member.getLastName(), "updated@example.com");
        updatedMember.setId(member.getId());
        updatedMember.setLeader(member.isLeader());
        updatedMember.setSubteam(member.getSubteam());
        
        when(teamMemberService.save(any())).thenReturn(updatedMember);
        when(teamMemberService.updateContactInfo(anyLong(), anyString(), anyString()))
            .thenReturn(updatedMember);
        
        // Execute command
        viewModel.getSaveMemberCommand().execute();
        
        // Verify service calls
        verify(teamMemberService).save(any());
        verify(teamMemberService).updateContactInfo(eq(1L), eq("updated@example.com"), anyString());
        
        // Verify the member was updated in the list
        boolean found = false;
        for (TeamMember m : viewModel.getMembers()) {
            if (m.getId() != null && m.getId().equals(1L) && 
                m.getFirstName().equals("Updated") && 
                m.getEmail().equals("updated@example.com")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Member should be updated in the list");
    }

    @Test
    public void testDeleteMember() {
        // Get a sample member
        TeamMember member = viewModel.getMembers().get(0);
        member.setId(1L);
        
        // Select the member
        viewModel.setSelectedMember(member);
        
        // Mock service response
        when(teamMemberService.deleteById(anyLong())).thenReturn(true);
        
        // Initial list size
        int initialSize = viewModel.getMembers().size();
        
        // Execute command
        viewModel.getDeleteMemberCommand().execute();
        
        // Verify service call
        verify(teamMemberService).deleteById(eq(1L));
        
        // Verify the member was removed from the list
        assertEquals(initialSize - 1, viewModel.getMembers().size());
        boolean found = false;
        for (TeamMember m : viewModel.getMembers()) {
            if (m.getId() != null && m.getId().equals(1L)) {
                found = true;
                break;
            }
        }
        assertFalse(found, "Member should be removed from the list");
    }

    @Test
    public void testInitNewSubteam() {
        // Call the method
        viewModel.initNewSubteam();
        
        // Verify state
        assertTrue(viewModel.isNewSubteam());
        assertEquals("", viewModel.getSubteamName());
        assertEquals("#0000FF", viewModel.getSubteamColorCode());
        assertEquals("", viewModel.getSubteamSpecialties());
        assertFalse(viewModel.isDirty());
    }

    @Test
    public void testInitExistingSubteam() {
        // Get a sample subteam
        Subteam subteam = viewModel.getSubteams().get(0);
        
        // Call the method
        viewModel.initExistingSubteam(subteam);
        
        // Verify state
        assertFalse(viewModel.isNewSubteam());
        assertEquals(subteam.getName(), viewModel.getSubteamName());
        assertEquals(subteam.getColorCode(), viewModel.getSubteamColorCode());
        assertEquals(subteam.getSpecialties(), viewModel.getSubteamSpecialties());
        assertFalse(viewModel.isDirty());
        
        // Verify members were loaded
        verify(teamMemberService).findBySubteam(eq(subteam));
    }

    @Test
    public void testSaveSubteam_Create() {
        // Setup test data
        viewModel.initNewSubteam();
        viewModel.setSubteamName("New Team");
        viewModel.setSubteamColorCode("#FF0000");
        viewModel.setSubteamSpecialties("Specialized in testing");
        
        // Mock service response
        Subteam newSubteam = new Subteam("New Team", "#FF0000");
        newSubteam.setId(3L);
        newSubteam.setSpecialties("Specialized in testing");
        
        when(subteamService.createSubteam(anyString(), anyString(), anyString()))
            .thenReturn(newSubteam);
        
        // Initial list size
        int initialSize = viewModel.getSubteams().size();
        
        // Execute command
        viewModel.getSaveSubteamCommand().execute();
        
        // Verify service calls
        verify(subteamService).createSubteam(
            eq("New Team"), eq("#FF0000"), eq("Specialized in testing"));
        
        // Verify the subteam was added to the list
        assertEquals(initialSize + 1, viewModel.getSubteams().size());
        boolean found = false;
        for (Subteam s : viewModel.getSubteams()) {
            if (s.getName().equals("New Team") && s.getColorCode().equals("#FF0000")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "New subteam should be added to the list");
    }

    @Test
    public void testSaveSubteam_Update() {
        // Get a sample subteam
        Subteam subteam = viewModel.getSubteams().get(0);
        subteam.setId(1L);
        
        // Setup for edit
        viewModel.initExistingSubteam(subteam);
        
        // Change some properties
        viewModel.setSubteamColorCode("#00FF00");
        viewModel.setSubteamSpecialties("Updated specialties");
        
        // Mock service response
        Subteam updatedSubteam = new Subteam(subteam.getName(), "#00FF00");
        updatedSubteam.setId(subteam.getId());
        updatedSubteam.setSpecialties("Updated specialties");
        
        when(subteamService.updateColorCode(anyLong(), anyString()))
            .thenReturn(updatedSubteam);
        when(subteamService.updateSpecialties(anyLong(), anyString()))
            .thenReturn(updatedSubteam);
        
        // Execute command
        viewModel.getSaveSubteamCommand().execute();
        
        // Verify service calls
        verify(subteamService).updateColorCode(eq(1L), eq("#00FF00"));
        verify(subteamService).updateSpecialties(eq(1L), eq("Updated specialties"));
        
        // Verify the subteam was updated in the list
        boolean found = false;
        for (Subteam s : viewModel.getSubteams()) {
            if (s.getId() != null && s.getId().equals(1L) && 
                s.getColorCode().equals("#00FF00") && 
                s.getSpecialties().equals("Updated specialties")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Subteam should be updated in the list");
    }

    @Test
    public void testDeleteSubteam() {
        // Get a sample subteam
        Subteam subteam = viewModel.getSubteams().get(0);
        subteam.setId(1L);
        
        // Select the subteam
        viewModel.setSelectedSubteam(subteam);
        
        // Mock service response
        when(teamMemberService.findBySubteam(any())).thenReturn(new ArrayList<>());
        when(subteamService.deleteById(anyLong())).thenReturn(true);
        
        // Initial list size
        int initialSize = viewModel.getSubteams().size();
        
        // Execute command
        viewModel.getDeleteSubteamCommand().execute();
        
        // Verify service calls
        verify(teamMemberService).findBySubteam(eq(subteam));
        verify(subteamService).deleteById(eq(1L));
        
        // Verify the subteam was removed from the list
        assertEquals(initialSize - 1, viewModel.getSubteams().size());
        boolean found = false;
        for (Subteam s : viewModel.getSubteams()) {
            if (s.getId() != null && s.getId().equals(1L)) {
                found = true;
                break;
            }
        }
        assertFalse(found, "Subteam should be removed from the list");
    }

    @Test
    public void testDeleteSubteam_WithMembers() {
        // Get a sample subteam
        Subteam subteam = viewModel.getSubteams().get(0);
        subteam.setId(1L);
        
        // Select the subteam
        viewModel.setSelectedSubteam(subteam);
        
        // Mock service response - subteam has members
        List<TeamMember> members = new ArrayList<>();
        members.add(new TeamMember("user1", "John", "Doe", "john@example.com"));
        when(teamMemberService.findBySubteam(any())).thenReturn(members);
        
        // Initial list size
        int initialSize = viewModel.getSubteams().size();
        
        // Execute command
        viewModel.getDeleteSubteamCommand().execute();
        
        // Verify service call
        verify(teamMemberService).findBySubteam(eq(subteam));
        
        // Verify no delete was performed
        verify(subteamService, never()).deleteById(anyLong());
        
        // Verify error message was set
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Cannot delete subteam"));
        
        // Verify the list size didn't change
        assertEquals(initialSize, viewModel.getSubteams().size());
    }

    @Test
    public void testMemberValidation() {
        // Setup test data with invalid values
        viewModel.initNewMember();
        
        // Empty username - should be invalid
        viewModel.setMemberUsername("");
        assertFalse(viewModel.isMemberValid());
        
        // Set username, but empty firstName - should be invalid
        viewModel.setMemberUsername("user1");
        viewModel.setMemberFirstName("");
        assertFalse(viewModel.isMemberValid());
        
        // Set firstName - should be valid
        viewModel.setMemberFirstName("John");
        assertTrue(viewModel.isMemberValid());
        
        // Set invalid email - should be invalid
        viewModel.setMemberEmail("invalid-email");
        assertFalse(viewModel.isMemberValid());
        
        // Set valid email - should be valid again
        viewModel.setMemberEmail("john@example.com");
        assertTrue(viewModel.isMemberValid());
    }

    @Test
    public void testSubteamValidation() {
        // Setup test data with invalid values
        viewModel.initNewSubteam();
        
        // Empty name - should be invalid
        viewModel.setSubteamName("");
        assertFalse(viewModel.isSubteamValid());
        
        // Set name - should be valid
        viewModel.setSubteamName("Test Team");
        assertTrue(viewModel.isSubteamValid());
        
        // Set invalid color code - should be invalid
        viewModel.setSubteamColorCode("invalid-color");
        assertFalse(viewModel.isSubteamValid());
        
        // Set valid color code - should be valid again
        viewModel.setSubteamColorCode("#FF0000");
        assertTrue(viewModel.isSubteamValid());
    }

    // Helper methods

    private List<TeamMember> createSampleMembers() {
        List<TeamMember> members = new ArrayList<>();
        
        TeamMember member1 = new TeamMember("jdoe", "John", "Doe", "john@example.com");
        member1.setId(1L);
        member1.setLeader(true);
        
        TeamMember member2 = new TeamMember("jsmith", "Jane", "Smith", "jane@example.com");
        member2.setId(2L);
        member2.setLeader(false);
        
        members.add(member1);
        members.add(member2);
        
        return members;
    }

    private List<Subteam> createSampleSubteams() {
        List<Subteam> subteams = new ArrayList<>();
        
        Subteam subteam1 = new Subteam("Programming", "#0000FF");
        subteam1.setId(1L);
        subteam1.setSpecialties("Java, Python, C++");
        
        Subteam subteam2 = new Subteam("Mechanical", "#FF0000");
        subteam2.setId(2L);
        subteam2.setSpecialties("CAD, Machining, Welding");
        
        subteams.add(subteam1);
        subteams.add(subteam2);
        
        return subteams;
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}