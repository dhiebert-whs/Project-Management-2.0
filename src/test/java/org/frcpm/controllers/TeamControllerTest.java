package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.TeamViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeamController following standardized MVVM pattern.
 */
@ExtendWith(ApplicationExtension.class)
public class TeamControllerTest extends BaseJavaFXTest {

    @Spy
    private TeamController controller;

    @Mock
    private TeamViewModel mockViewModel;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private Command mockCreateNewMemberCommand;
    
    @Mock
    private Command mockEditMemberCommand;
    
    @Mock
    private Command mockDeleteMemberCommand;
    
    @Mock
    private Command mockCreateNewSubteamCommand;
    
    @Mock
    private Command mockEditSubteamCommand;
    
    @Mock
    private Command mockDeleteSubteamCommand;
    
    @Mock
    private Command mockSaveMemberCommand;
    
    @Mock
    private Command mockSaveSubteamCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock ViewModel commands
        when(mockViewModel.getCreateNewMemberCommand()).thenReturn(mockCreateNewMemberCommand);
        when(mockViewModel.getEditMemberCommand()).thenReturn(mockEditMemberCommand);
        when(mockViewModel.getDeleteMemberCommand()).thenReturn(mockDeleteMemberCommand);
        when(mockViewModel.getCreateNewSubteamCommand()).thenReturn(mockCreateNewSubteamCommand);
        when(mockViewModel.getEditSubteamCommand()).thenReturn(mockEditSubteamCommand);
        when(mockViewModel.getDeleteSubteamCommand()).thenReturn(mockDeleteSubteamCommand);
        when(mockViewModel.getSaveMemberCommand()).thenReturn(mockSaveMemberCommand);
        when(mockViewModel.getSaveSubteamCommand()).thenReturn(mockSaveSubteamCommand);
        
        // Set up mock ViewModel properties
        when(mockViewModel.memberUsernameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberFirstNameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberLastNameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberEmailProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberPhoneProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberSkillsProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberIsLeaderProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty());
        when(mockViewModel.memberSubteamProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.subteamNameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.subteamColorCodeProperty()).thenReturn(new SimpleStringProperty("#FF0000"));
        when(mockViewModel.subteamSpecialtiesProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.memberValidProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(true));
        when(mockViewModel.subteamValidProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(true));
        
        // Set up mock ViewModel collections
        when(mockViewModel.getMembers()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getSubteams()).thenReturn(FXCollections.observableArrayList());
        
        // Inject fields into controller using reflection
        try {
            // Inject ViewModel
            Field viewModelField = TeamController.class.getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            viewModelField.set(controller, mockViewModel);
            
            // Inject DialogService
            Field dialogServiceField = TeamController.class.getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(controller, mockDialogService);
            
            // Inject necessary JavaFX components
            injectRequiredJavaFXComponents();
            
        } catch (Exception e) {
            fail("Failed to set up controller: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to inject JavaFX components into the controller.
     */
    private void injectRequiredJavaFXComponents() throws Exception {
        // Tab controls
        Field tabPaneField = TeamController.class.getDeclaredField("tabPane");
        tabPaneField.setAccessible(true);
        tabPaneField.set(controller, new TabPane());
        
        // Members tab controls
        Field membersTableField = TeamController.class.getDeclaredField("membersTable");
        membersTableField.setAccessible(true);
        membersTableField.set(controller, new TableView<TeamMember>());
        
        Field memberUsernameColumnField = TeamController.class.getDeclaredField("memberUsernameColumn");
        memberUsernameColumnField.setAccessible(true);
        memberUsernameColumnField.set(controller, new TableColumn<TeamMember, String>());
        
        Field memberNameColumnField = TeamController.class.getDeclaredField("memberNameColumn");
        memberNameColumnField.setAccessible(true);
        memberNameColumnField.set(controller, new TableColumn<TeamMember, String>());
        
        Field memberEmailColumnField = TeamController.class.getDeclaredField("memberEmailColumn");
        memberEmailColumnField.setAccessible(true);
        memberEmailColumnField.set(controller, new TableColumn<TeamMember, String>());
        
        Field memberSubteamColumnField = TeamController.class.getDeclaredField("memberSubteamColumn");
        memberSubteamColumnField.setAccessible(true);
        memberSubteamColumnField.set(controller, new TableColumn<TeamMember, String>());
        
        Field memberLeaderColumnField = TeamController.class.getDeclaredField("memberLeaderColumn");
        memberLeaderColumnField.setAccessible(true);
        memberLeaderColumnField.set(controller, new TableColumn<TeamMember, Boolean>());
        
        // Members tab buttons
        Field addMemberButtonField = TeamController.class.getDeclaredField("addMemberButton");
        addMemberButtonField.setAccessible(true);
        addMemberButtonField.set(controller, new Button());
        
        Field editMemberButtonField = TeamController.class.getDeclaredField("editMemberButton");
        editMemberButtonField.setAccessible(true);
        editMemberButtonField.set(controller, new Button());
        
        Field deleteMemberButtonField = TeamController.class.getDeclaredField("deleteMemberButton");
        deleteMemberButtonField.setAccessible(true);
        deleteMemberButtonField.set(controller, new Button());
        
        // Subteams tab controls
        Field subteamsTableField = TeamController.class.getDeclaredField("subteamsTable");
        subteamsTableField.setAccessible(true);
        subteamsTableField.set(controller, new TableView<Subteam>());
        
        Field subteamNameColumnField = TeamController.class.getDeclaredField("subteamNameColumn");
        subteamNameColumnField.setAccessible(true);
        subteamNameColumnField.set(controller, new TableColumn<Subteam, String>());
        
        Field subteamColorColumnField = TeamController.class.getDeclaredField("subteamColorColumn");
        subteamColorColumnField.setAccessible(true);
        subteamColorColumnField.set(controller, new TableColumn<Subteam, String>());
        
        Field subteamSpecialtiesColumnField = TeamController.class.getDeclaredField("subteamSpecialtiesColumn");
        subteamSpecialtiesColumnField.setAccessible(true);
        subteamSpecialtiesColumnField.set(controller, new TableColumn<Subteam, String>());
        
        // Subteams tab buttons
        Field addSubteamButtonField = TeamController.class.getDeclaredField("addSubteamButton");
        addSubteamButtonField.setAccessible(true);
        addSubteamButtonField.set(controller, new Button());
        
        Field editSubteamButtonField = TeamController.class.getDeclaredField("editSubteamButton");
        editSubteamButtonField.setAccessible(true);
        editSubteamButtonField.set(controller, new Button());
        
        Field deleteSubteamButtonField = TeamController.class.getDeclaredField("deleteSubteamButton");
        deleteSubteamButtonField.setAccessible(true);
        deleteSubteamButtonField.set(controller, new Button());
    }
    
    @Test
    public void testInitialize() {
        // Act
        controller.testInitialize();
        
        // Assert
        verify(mockViewModel).getCreateNewMemberCommand();
        verify(mockViewModel).getEditMemberCommand();
        verify(mockViewModel).getDeleteMemberCommand();
        verify(mockViewModel).getCreateNewSubteamCommand();
        verify(mockViewModel).getEditSubteamCommand();
        verify(mockViewModel).getDeleteSubteamCommand();
        verify(mockViewModel).getMembers();
        verify(mockViewModel).getSubteams();
        verify(mockViewModel).errorMessageProperty();
    }

    @Test
    public void testHandleAddMember() {
        // Arrange
        Dialog<TeamMember> mockDialog = mock(Dialog.class);
        doReturn(mockDialog).when(controller).createMemberDialog();

        // Act
        controller.handleAddMember();

        // Assert
        verify(mockViewModel).initNewMember();
        verify(controller).createMemberDialog();
        verify(controller).showAndWaitDialog(mockDialog);
    }

    @Test
    public void testHandleEditMember_WithSelection() {
        // Arrange
        TeamMember mockMember = mock(TeamMember.class);
        doReturn(mockMember).when(controller).getSelectedTeamMember();
        Dialog<TeamMember> mockDialog = mock(Dialog.class);
        doReturn(mockDialog).when(controller).createMemberDialog();

        // Act
        controller.handleEditMember();

        // Assert
        verify(mockViewModel).initExistingMember(mockMember);
        verify(controller).createMemberDialog();
        verify(controller).showAndWaitDialog(mockDialog);
    }

    @Test
    public void testHandleEditMember_NoSelection() {
        // Arrange
        doReturn(null).when(controller).getSelectedTeamMember();

        // Act
        controller.handleEditMember();

        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(controller, never()).createMemberDialog();
    }

    @Test
    public void testHandleDeleteMember_WithSelection_Confirmed() {
        // Arrange
        TeamMember mockMember = mock(TeamMember.class);
        doReturn(mockMember).when(controller).getSelectedTeamMember();
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

        // Act
        controller.handleDeleteMember();

        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Team Member"), anyString());
        verify(mockViewModel).setSelectedMember(mockMember);
        verify(mockDeleteMemberCommand).execute();
        verify(mockDialogService).showInfoAlert(eq("Member Deleted"), anyString());
    }

    @Test
    public void testHandleDeleteMember_WithSelection_Cancelled() {
        // Arrange
        TeamMember mockMember = mock(TeamMember.class);
        doReturn(mockMember).when(controller).getSelectedTeamMember();
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);

        // Act
        controller.handleDeleteMember();

        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Team Member"), anyString());
        verify(mockDeleteMemberCommand, never()).execute();
        verify(mockDialogService, never()).showInfoAlert(eq("Member Deleted"), anyString());
    }

    @Test
    public void testHandleDeleteMember_NoSelection() {
        // Arrange
        doReturn(null).when(controller).getSelectedTeamMember();

        // Act
        controller.handleDeleteMember();

        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(mockViewModel, never()).setSelectedMember(any());
        verify(mockDeleteMemberCommand, never()).execute();
    }

    @Test
    public void testHandleAddSubteam() {
        // Arrange
        Dialog<Subteam> mockDialog = mock(Dialog.class);
        doReturn(mockDialog).when(controller).createSubteamDialog();

        // Act
        controller.handleAddSubteam();

        // Assert
        verify(mockViewModel).initNewSubteam();
        verify(controller).createSubteamDialog();
        verify(controller).showAndWaitDialog(mockDialog);
    }

    @Test
    public void testHandleEditSubteam_WithSelection() {
        // Arrange
        Subteam mockSubteam = mock(Subteam.class);
        doReturn(mockSubteam).when(controller).getSelectedSubteam();
        Dialog<Subteam> mockDialog = mock(Dialog.class);
        doReturn(mockDialog).when(controller).createSubteamDialog();

        // Act
        controller.handleEditSubteam();

        // Assert
        verify(mockViewModel).initExistingSubteam(mockSubteam);
        verify(controller).createSubteamDialog();
        verify(controller).showAndWaitDialog(mockDialog);
    }

    @Test
    public void testHandleEditSubteam_NoSelection() {
        // Arrange
        doReturn(null).when(controller).getSelectedSubteam();

        // Act
        controller.handleEditSubteam();

        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(controller, never()).createSubteamDialog();
    }

    @Test
    public void testHandleDeleteSubteam_WithSelection_Confirmed() {
        // Arrange
        Subteam mockSubteam = mock(Subteam.class);
        doReturn(mockSubteam).when(controller).getSelectedSubteam();
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

        // Act
        controller.handleDeleteSubteam();

        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Subteam"), anyString());
        verify(mockViewModel).setSelectedSubteam(mockSubteam);
        verify(mockDeleteSubteamCommand).execute();
        verify(mockDialogService).showInfoAlert(eq("Subteam Deleted"), anyString());
    }

    @Test
    public void testHandleDeleteSubteam_WithSelection_Cancelled() {
        // Arrange
        Subteam mockSubteam = mock(Subteam.class);
        doReturn(mockSubteam).when(controller).getSelectedSubteam();
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);

        // Act
        controller.handleDeleteSubteam();

        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Subteam"), anyString());
        verify(mockDeleteSubteamCommand, never()).execute();
        verify(mockDialogService, never()).showInfoAlert(eq("Subteam Deleted"), anyString());
    }

    @Test
    public void testHandleDeleteSubteam_NoSelection() {
        // Arrange
        doReturn(null).when(controller).getSelectedSubteam();

        // Act
        controller.handleDeleteSubteam();

        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(mockViewModel, never()).setSelectedSubteam(any());
        verify(mockDeleteSubteamCommand, never()).execute();
    }

    @Test
    public void testCreateMemberDialog() {
        // Act
        Dialog<TeamMember> result = controller.createMemberDialog();

        // Assert
        assertNotNull(result);
        verify(mockViewModel).memberUsernameProperty();
        verify(mockViewModel).memberFirstNameProperty();
        verify(mockViewModel).memberLastNameProperty();
        verify(mockViewModel).memberEmailProperty();
        verify(mockViewModel).memberPhoneProperty();
        verify(mockViewModel).memberSkillsProperty();
        verify(mockViewModel).memberIsLeaderProperty();
        verify(mockViewModel).memberSubteamProperty();
        verify(mockViewModel).isNewMember();
    }

    @Test
    public void testCreateSubteamDialog() {
        // Arrange
        when(mockViewModel.getSubteamColorCode()).thenReturn("#FF0000");

        // Act
        Dialog<Subteam> result = controller.createSubteamDialog();

        // Assert
        assertNotNull(result);
        verify(mockViewModel).subteamNameProperty();
        verify(mockViewModel).subteamSpecialtiesProperty();
        verify(mockViewModel).getSubteamColorCode();
        verify(mockViewModel).isNewSubteam();
    }

    @Test
    public void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }

    @Test
    public void testShowInfoAlert() {
        // Act
        controller.showInfoAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showInfoAlert("Test Title", "Test Message");
    }

    @Test
    public void testShowConfirmationAlert() {
        // Arrange
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = controller.showConfirmationAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showConfirmationAlert("Test Title", "Test Message");
        assertTrue(result);
    }

    @Test
    public void testGetViewModel() {
        // Act
        TeamViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }
}