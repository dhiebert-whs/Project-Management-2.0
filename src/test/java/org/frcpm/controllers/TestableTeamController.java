package org.frcpm.controllers;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;

import javafx.scene.control.Dialog;

/**
 * A testable subclass of TeamController that overrides methods that directly access
 * JavaFX UI components to enable testing without JavaFX initialization.
 */
public class TestableTeamController extends TeamController {
    
    private TeamMember mockSelectedMember;
    private Subteam mockSelectedSubteam;
    
    /**
     * Overrides methods that access membersTable
     */
    @Override
    public void handleEditMember() {
        TeamMember selectedMember = getSelectedTeamMember();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to edit");
            return;
        }
        
        // Prepare the ViewModel for editing the selected member
        getViewModel().initExistingMember(selectedMember);
        
        // Create and show the dialog
        Dialog<TeamMember> dialog = createMemberDialog();
        showAndWaitDialog(dialog);
    }
    
    @Override
    public void handleDeleteMember() {
        TeamMember selectedMember = getSelectedTeamMember();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to delete");
            return;
        }
        
        // Ask for confirmation
        boolean confirmed = showConfirmationAlert(
                "Delete Team Member",
                "Are you sure you want to delete " + selectedMember.getFullName() + "?");
        
        if (confirmed) {
            try {
                // Execute the delete command
                getViewModel().setSelectedMember(selectedMember);
                getViewModel().getDeleteMemberCommand().execute();
                
                showInfoAlert("Member Deleted", "Team member deleted successfully");
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to delete team member: " + e.getMessage());
            }
        }
    }
    
    /**
     * Overrides methods that access subteamsTable
     */
    @Override
    public void handleEditSubteam() {
        Subteam selectedSubteam = getSelectedSubteam();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to edit");
            return;
        }
        
        // Prepare the ViewModel for editing the selected subteam
        getViewModel().initExistingSubteam(selectedSubteam);
        
        // Create and show the dialog
        Dialog<Subteam> dialog = createSubteamDialog();
        showAndWaitDialog(dialog);
    }
    
    @Override
    public void handleDeleteSubteam() {
        Subteam selectedSubteam = getSelectedSubteam();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to delete");
            return;
        }
        
        // Ask for confirmation
        boolean confirmed = showConfirmationAlert(
                "Delete Subteam",
                "Are you sure you want to delete " + selectedSubteam.getName() + "?");
        
        if (confirmed) {
            try {
                // Execute the delete command
                getViewModel().setSelectedSubteam(selectedSubteam);
                getViewModel().getDeleteSubteamCommand().execute();
                
                showInfoAlert("Subteam Deleted", "Subteam deleted successfully");
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to delete subteam: " + e.getMessage());
            }
        }
    }
    
    /**
     * Override the tabPane reference in handleClose
     */
    @Override
    public void handleClose() {
        // Do nothing in tests
    }
    
    /**
     * Override initialize to avoid UI access
     */
    @Override
    public void testInitialize() {
        // Do nothing in tests
    }
    
    /**
     * Override the protected method to return our mock member
     */
    @Override
    protected TeamMember getSelectedTeamMember() {
        return mockSelectedMember;
    }
    
    /**
     * Override the protected method to return our mock subteam
     */
    @Override
    protected Subteam getSelectedSubteam() {
        return mockSelectedSubteam;
    }
    
    /**
     * Set the mock selected team member for testing
     */
    public void setMockSelectedMember(TeamMember member) {
        this.mockSelectedMember = member;
    }
    
    /**
     * Set the mock selected subteam for testing
     */
    public void setMockSelectedSubteam(Subteam subteam) {
        this.mockSelectedSubteam = subteam;
    }
}