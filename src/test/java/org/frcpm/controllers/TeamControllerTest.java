package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamControllerTest {

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private SubteamService subteamService;

    @InjectMocks
    private TeamController teamController;

    @Mock
    private TabPane tabPane;

    @Mock
    private TableView<TeamMember> membersTable;

    @Mock
    private TableColumn<TeamMember, String> memberUsernameColumn;

    @Mock
    private TableColumn<TeamMember, String> memberNameColumn;

    @Mock
    private TableColumn<TeamMember, String> memberEmailColumn;

    @Mock
    private TableColumn<TeamMember, String> memberSubteamColumn;

    @Mock
    private TableColumn<TeamMember, Boolean> memberLeaderColumn;

    @Mock
    private Button addMemberButton;

    @Mock
    private Button editMemberButton;

    @Mock
    private Button deleteMemberButton;

    @Mock
    private TableView<Subteam> subteamsTable;

    @Mock
    private TableColumn<Subteam, String> subteamNameColumn;

    @Mock
    private TableColumn<Subteam, String> subteamColorColumn;

    @Mock
    private TableColumn<Subteam, String> subteamSpecialtiesColumn;

    @Mock
    private Button addSubteamButton;

    @Mock
    private Button editSubteamButton;

    @Mock
    private Button deleteSubteamButton;

    @Mock
    private TextField usernameField;

    @Mock
    private TextField firstNameField;

    @Mock
    private TextField lastNameField;

    @Mock
    private TextField emailField;

    @Mock
    private TextField phoneField;

    @Mock
    private TextArea skillsArea;

    @Mock
    private ComboBox<Subteam> subteamComboBox;

    @Mock
    private CheckBox leaderCheckBox;

    @Mock
    private TextField subteamNameField;

    @Mock
    private ActionEvent mockEvent;

    private List<TeamMember> testMembers;
    private List<Subteam> testSubteams;

    @BeforeEach
    public void setUp() {
        // Create test members
        TeamMember member1 = new TeamMember("testuser1", "Test", "User1", "test1@example.com");
        member1.setId(1L);
        member1.setLeader(true);

        TeamMember member2 = new TeamMember("testuser2", "Test", "User2", "test2@example.com");
        member2.setId(2L);

        testMembers = Arrays.asList(member1, member2);

        // Create test subteams
        Subteam subteam1 = new Subteam("Programming", "#0000FF");
        subteam1.setId(1L);

        Subteam subteam2 = new Subteam("Mechanical", "#FF0000");
        subteam2.setId(2L);

        testSubteams = Arrays.asList(subteam1, subteam2);

        // Initialize controller by setting the mock fields
        when(teamController.getTabPane()).thenReturn(tabPane);
        when(teamController.getMembersTable()).thenReturn(membersTable);
        when(teamController.getMemberUsernameColumn()).thenReturn(memberUsernameColumn);
        when(teamController.getMemberNameColumn()).thenReturn(memberNameColumn);
        when(teamController.getMemberEmailColumn()).thenReturn(memberEmailColumn);
        when(teamController.getMemberSubteamColumn()).thenReturn(memberSubteamColumn);
        when(teamController.getMemberLeaderColumn()).thenReturn(memberLeaderColumn);
        when(teamController.getAddMemberButton()).thenReturn(addMemberButton);
        when(teamController.getEditMemberButton()).thenReturn(editMemberButton);
        when(teamController.getDeleteMemberButton()).thenReturn(deleteMemberButton);
        when(teamController.getSubteamsTable()).thenReturn(subteamsTable);
        when(teamController.getSubteamNameColumn()).thenReturn(subteamNameColumn);
        when(teamController.getSubteamColorColumn()).thenReturn(subteamColorColumn);
        when(teamController.getSubteamSpecialtiesColumn()).thenReturn(subteamSpecialtiesColumn);
        when(teamController.getAddSubteamButton()).thenReturn(addSubteamButton);
        when(teamController.getEditSubteamButton()).thenReturn(editSubteamButton);
        when(teamController.getDeleteSubteamButton()).thenReturn(deleteSubteamButton);
        when(teamController.getUsernameField()).thenReturn(usernameField);
        when(teamController.getFirstNameField()).thenReturn(firstNameField);
        when(teamController.getLastNameField()).thenReturn(lastNameField);
        when(teamController.getEmailField()).thenReturn(emailField);
        when(teamController.getPhoneField()).thenReturn(phoneField);
        when(teamController.getSkillsArea()).thenReturn(skillsArea);
        when(teamController.getSubteamComboBox()).thenReturn(subteamComboBox);
        when(teamController.getLeaderCheckBox()).thenReturn(leaderCheckBox);
        when(teamController.getSubteamNameField()).thenReturn(subteamNameField);

        // Mock service behavior
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(subteamService.findAll()).thenReturn(testSubteams);
        when(teamMemberService.findById(1L)).thenReturn(member1);
        when(teamMemberService.findById(2L)).thenReturn(member2);
        when(subteamService.findById(1L)).thenReturn(subteam1);
        when(subteamService.findById(2L)).thenReturn(subteam2);

        // Mock table behavior
        when(membersTable.getItems()).thenReturn(FXCollections.observableArrayList());
        when(subteamsTable.getItems()).thenReturn(FXCollections.observableArrayList());
    }

    @Test
    public void testInitialize() {

        try {
            teamController.testInitialize();

            // Verify that the table columns are set up
            verify(memberUsernameColumn).setCellValueFactory(any());
            verify(memberNameColumn).setCellValueFactory(any());
            verify(memberEmailColumn).setCellValueFactory(any());
            verify(memberSubteamColumn).setCellValueFactory(any());
            verify(memberLeaderColumn).setCellValueFactory(any());

            verify(subteamNameColumn).setCellValueFactory(any());
            verify(subteamColorColumn).setCellValueFactory(any());
            verify(subteamSpecialtiesColumn).setCellValueFactory(any());

            // Verify that data is loaded
            verify(teamMemberService).findAll();
            verify(subteamService).findAll();

        } catch (Exception e) {
            fail("Exception during initialize: " + e.getMessage());
        }
    }

    @Test
    public void testLoadTeamData() {
        // Call the method to test
        try {
            teamController.testLoadTeamData();

            // Verify that data is loaded from the services
            verify(teamMemberService).findAll();
            verify(subteamService).findAll();

        } catch (Exception e) {
            fail("Exception during loadTeamData: " + e.getMessage());
        }
    }

    @Test
    public void testHandleAddMember() {
        // This is a complex method involving dialog creation
        // We'll just test that it doesn't throw exceptions
        assertDoesNotThrow(() -> teamController.testHandleAddMember(mockEvent));
    }

    @Test
    public void testHandleEditMember() {
        // Mock the table selection
        when(membersTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(membersTable.getSelectionModel().getSelectedItem()).thenReturn(testMembers.get(0));

        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleEditMember(mockEvent));
    }

    @Test
    public void testHandleEditMemberWithNoSelection() {
        // Mock an empty selection
        when(membersTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(membersTable.getSelectionModel().getSelectedItem()).thenReturn(null);

        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleEditMember(mockEvent));
    }

    @Test
    public void testHandleDeleteMember() {
        // Mock the table selection
        when(membersTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(membersTable.getSelectionModel().getSelectedItem()).thenReturn(testMembers.get(0));

        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleDeleteMember(mockEvent));
    }

    @Test
    public void testHandleAddSubteam() {
        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleAddSubteam(mockEvent));
    }

    @Test
    public void testHandleEditSubteam() {
        // Mock the table selection
        when(subteamsTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(subteamsTable.getSelectionModel().getSelectedItem()).thenReturn(testSubteams.get(0));

        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleEditSubteam(mockEvent));
    }

    @Test
    public void testHandleDeleteSubteam() {
        // Mock the table selection
        when(subteamsTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(subteamsTable.getSelectionModel().getSelectedItem()).thenReturn(testSubteams.get(0));

        // Test the method
        assertDoesNotThrow(() -> teamController.testHandleDeleteSubteam(mockEvent));
    }
}