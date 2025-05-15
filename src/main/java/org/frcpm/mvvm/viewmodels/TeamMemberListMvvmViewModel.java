// src/main/java/org/frcpm/mvvm/viewmodels/TeamMemberListMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * ViewModel for the TeamMemberList view using MVVMFx.
 */
public class TeamMemberListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberListMvvmViewModel.class.getName());
    
    // Service dependencies
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<TeamMember> teamMembers = FXCollections.observableArrayList();
    private final ObjectProperty<TeamMember> selectedTeamMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command loadTeamMembersCommand;
    private Command newTeamMemberCommand;
    private Command editTeamMemberCommand;
    private Command deleteTeamMemberCommand;
    private Command refreshTeamMembersCommand;
    
    /**
     * Creates a new TeamMemberListMvvmViewModel.
     * 
     * @param teamMemberService the team member service
     */
  
    public TeamMemberListMvvmViewModel(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
        
        // Cast to the async implementation
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        
        initializeCommands();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load team members command
        loadTeamMembersCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTeamMembersAsync);
        
        // New team member command
        newTeamMemberCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New team member command executed");
            },
            () -> true // Always enabled
        );
        
        // Edit team member command
        editTeamMemberCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit team member command executed for: " + 
                    (selectedTeamMember.get() != null ? selectedTeamMember.get().getFullName() : "null"));
            },
            () -> selectedTeamMember.get() != null
        );
        
        // Delete team member command
        deleteTeamMemberCommand = createValidOnlyCommand(
            this::deleteTeamMemberAsync,
            () -> selectedTeamMember.get() != null
        );
        
        // Refresh team members command
        refreshTeamMembersCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTeamMembersAsync);
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadTeamMembersAsync();
    }
    
    /**
     * Gets the team members list.
     * 
     * @return the team members list
     */
    public ObservableList<TeamMember> getTeamMembers() {
        return teamMembers;
    }
    
    /**
     * Gets the selected team member property.
     * 
     * @return the selected team member property
     */
    public ObjectProperty<TeamMember> selectedTeamMemberProperty() {
        return selectedTeamMember;
    }
    
    /**
     * Gets the selected team member.
     * 
     * @return the selected team member
     */
    public TeamMember getSelectedTeamMember() {
        return selectedTeamMember.get();
    }
    
    /**
     * Sets the selected team member.
     * 
     * @param teamMember the selected team member
     */
    public void setSelectedTeamMember(TeamMember teamMember) {
        selectedTeamMember.set(teamMember);
    }
    
    /**
     * Gets the current project property.
     * 
     * @return the current project property
     */
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    /**
     * Gets the current project.
     * 
     * @return the current project
     */
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    /**
     * Sets the current project.
     * 
     * @param project the project
     */
    public void setCurrentProject(Project project) {
        currentProject.set(project);
    }
    
    /**
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Checks if the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the load team members command.
     * 
     * @return the load team members command
     */
    public Command getLoadTeamMembersCommand() {
        return loadTeamMembersCommand;
    }
    
    /**
     * Gets the new team member command.
     * 
     * @return the new team member command
     */
    public Command getNewTeamMemberCommand() {
        return newTeamMemberCommand;
    }
    
    /**
     * Gets the edit team member command.
     * 
     * @return the edit team member command
     */
    public Command getEditTeamMemberCommand() {
        return editTeamMemberCommand;
    }
    
    /**
     * Gets the delete team member command.
     * 
     * @return the delete team member command
     */
    public Command getDeleteTeamMemberCommand() {
        return deleteTeamMemberCommand;
    }
    
    /**
     * Gets the refresh team members command.
     * 
     * @return the refresh team members command
     */
    public Command getRefreshTeamMembersCommand() {
        return refreshTeamMembersCommand;
    }
    
    /**
     * Loads team members asynchronously.
     */
    private void loadTeamMembersAsync() {
        loading.set(true);
        
        // Find all team members asynchronously
        teamMemberServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    teamMembers.clear();
                    teamMembers.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " team members asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading team members asynchronously", error);
                    setErrorMessage("Failed to load team members: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a team member asynchronously.
     */
    private void deleteTeamMemberAsync() {
        TeamMember teamMember = selectedTeamMember.get();
        if (teamMember == null || teamMember.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        // Using the async service method
        teamMemberServiceAsync.deleteByIdAsync(
            teamMember.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        teamMembers.remove(teamMember);
                        selectedTeamMember.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted team member: " + teamMember.getFullName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete team member: " + teamMember.getFullName() + " asynchronously");
                        setErrorMessage("Failed to delete team member: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting team member asynchronously", error);
                    setErrorMessage("Failed to delete team member: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
}