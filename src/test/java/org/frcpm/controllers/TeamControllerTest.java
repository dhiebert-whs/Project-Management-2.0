package org.frcpm.controllers;

import javafx.scene.control.*;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.TeamViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TeamController that avoid JavaFX toolkit initialization.
 * Uses a testable controller subclass to avoid UI component access.
 */
public class TeamControllerTest {

    @Spy
    private TestableTeamController controller;

    @Mock
    private TeamViewModel mockViewModel;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private TeamMember mockTeamMember;
    
    @Mock
    private Subteam mockSubteam;
    
    @Mock
    private Command mockCreateNewMemberCommand;
    
    @Mock
    private Command mockEditMemberCommand;
    
    @Mock
    private Command mockDeleteMemberCommand;
    
    @Mock
    private Command mockSaveMemberCommand;
    
    @Mock
    private Command mockCreateNewSubteamCommand;
    
    @Mock
    private Command mockEditSubteamCommand;
    
    @Mock
    private Command mockDeleteSubteamCommand;
    
    @Mock
    private Command mockSaveSubteamCommand;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up command mocks
        when(mockViewModel.getCreateNewMemberCommand()).thenReturn(mockCreateNewMemberCommand);
        when(mockViewModel.getEditMemberCommand()).thenReturn(mockEditMemberCommand);
        when(mockViewModel.getDeleteMemberCommand()).thenReturn(mockDeleteMemberCommand);
        when(mockViewModel.getSaveMemberCommand()).thenReturn(mockSaveMemberCommand);
        when(mockViewModel.getCreateNewSubteamCommand()).thenReturn(mockCreateNewSubteamCommand);
        when(mockViewModel.getEditSubteamCommand()).thenReturn(mockEditSubteamCommand);
        when(mockViewModel.getDeleteSubteamCommand()).thenReturn(mockDeleteSubteamCommand);
        when(mockViewModel.getSaveSubteamCommand()).thenReturn(mockSaveSubteamCommand);
        
        // Mock dialog creation and showing
        doReturn(mock(Dialog.class)).when(controller).createMemberDialog();
        doReturn(mock(Dialog.class)).when(controller).createSubteamDialog();
        doReturn(Optional.empty()).when(controller).showAndWaitDialog(any());
        
        // Set member and subteam properties
        when(mockTeamMember.getFullName()).thenReturn("John Doe");
        when(mockSubteam.getName()).thenReturn("Test Subteam");
        
        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
        
        // Set the mock selected member and subteam
        controller.setMockSelectedMember(mockTeamMember);
        controller.setMockSelectedSubteam(mockSubteam);
    }
    
    @Test
    public void testHandleAddMember() {
        // Act
        controller.handleAddMember();
        
        // Assert
        verify(mockViewModel).initNewMember();
        verify(controller).createMemberDialog();
        verify(controller).showAndWaitDialog(any());
    }
    
    @Test
    public void testHandleEditMember() {
        // Act
        controller.handleEditMember();
        
        // Assert
        verify(mockViewModel).initExistingMember(mockTeamMember);
        verify(controller).createMemberDialog();
        verify(controller).showAndWaitDialog(any());
    }
    
    @Test
    public void testHandleEditMember_NoSelection() {
        // Arrange
        controller.setMockSelectedMember(null);
        
        // Act
        controller.handleEditMember();
        
        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(controller, never()).createMemberDialog();
    }
    
    @Test
    public void testHandleDeleteMember_Confirmed() {
        // Arrange - Mock confirmation dialog to return true
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);
        
        // Act
        controller.handleDeleteMember();
        
        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Team Member"), anyString());
        verify(mockViewModel).setSelectedMember(mockTeamMember);
        verify(mockDeleteMemberCommand).execute();
        verify(mockDialogService).showInfoAlert(eq("Member Deleted"), anyString());
    }
    
    @Test
    public void testHandleDeleteMember_Cancelled() {
        // Arrange - Mock confirmation dialog to return false
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);
        
        // Act
        controller.handleDeleteMember();
        
        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Team Member"), anyString());
        verify(mockViewModel, never()).setSelectedMember(any());
        verify(mockDeleteMemberCommand, never()).execute();
        verify(mockDialogService, never()).showInfoAlert(anyString(), anyString());
    }
    
    @Test
    public void testHandleDeleteMember_NoSelection() {
        // Arrange
        controller.setMockSelectedMember(null);
        
        // Act
        controller.handleDeleteMember();
        
        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(mockDeleteMemberCommand, never()).execute();
    }
    
    @Test
    public void testHandleAddSubteam() {
        // Act
        controller.handleAddSubteam();
        
        // Assert
        verify(mockViewModel).initNewSubteam();
        verify(controller).createSubteamDialog();
        verify(controller).showAndWaitDialog(any());
    }
    
    @Test
    public void testHandleEditSubteam() {
        // Act
        controller.handleEditSubteam();
        
        // Assert
        verify(mockViewModel).initExistingSubteam(mockSubteam);
        verify(controller).createSubteamDialog();
        verify(controller).showAndWaitDialog(any());
    }
    
    @Test
    public void testHandleEditSubteam_NoSelection() {
        // Arrange
        controller.setMockSelectedSubteam(null);
        
        // Act
        controller.handleEditSubteam();
        
        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(controller, never()).createSubteamDialog();
    }
    
    @Test
    public void testHandleDeleteSubteam_Confirmed() {
        // Arrange - Mock confirmation dialog to return true
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
    public void testHandleDeleteSubteam_Cancelled() {
        // Arrange - Mock confirmation dialog to return false
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);
        
        // Act
        controller.handleDeleteSubteam();
        
        // Assert
        verify(mockDialogService).showConfirmationAlert(eq("Delete Subteam"), anyString());
        verify(mockViewModel, never()).setSelectedSubteam(any());
        verify(mockDeleteSubteamCommand, never()).execute();
        verify(mockDialogService, never()).showInfoAlert(anyString(), anyString());
    }
    
    @Test
    public void testHandleDeleteSubteam_NoSelection() {
        // Arrange
        controller.setMockSelectedSubteam(null);
        
        // Act
        controller.handleDeleteSubteam();
        
        // Assert
        verify(mockDialogService).showErrorAlert(eq("No Selection"), anyString());
        verify(mockDeleteSubteamCommand, never()).execute();
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
        // Arrange
        when(controller.getViewModel()).thenReturn(mockViewModel);
        
        // Assert
        assertEquals(mockViewModel, controller.getViewModel());
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        TeamViewModel newMockViewModel = mock(TeamViewModel.class);
        
        // Act
        controller.setViewModel(newMockViewModel);
        
        // Assert
        verify(controller).setViewModel(newMockViewModel);
    }
    
    @Test
    public void testHandleClose() {
        // Just test that no exceptions are thrown
        assertDoesNotThrow(() -> controller.handleClose());
    }
    
    @Test
    public void testTestInitialize() {
        // This test just ensures that the method doesn't throw exceptions
        assertDoesNotThrow(() -> controller.testInitialize());
    }
}