// src/main/java/org/frcpm/viewmodels/TeamMemberAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for team member management with asynchronous operations.
 */
public class TeamMemberAsyncViewModel extends TeamMemberViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberAsyncViewModel.class.getName());
    
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncSaveCommand;
    private Command asyncDeleteCommand;
    private Command asyncLoadTeamMembersCommand;
    
    /**
     * Creates a new TeamMemberAsyncViewModel with default services.
     * 
     * @param teamMemberService the team member service
     * @param subteamService the subteam service
     */
    public TeamMemberAsyncViewModel(TeamMemberService teamMemberService, SubteamService subteamService) {
        super(teamMemberService, subteamService);
        
        // Get the async service implementation from AsyncServiceFactory
        // Note: TeamMemberServiceAsyncImpl will need to be added to AsyncServiceFactory
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        
        // Initialize async commands
        asyncSaveCommand = new Command(
            this::saveAsync,
            () -> validProperty().get() && dirtyProperty().get() // Use the public property accessor instead
        );

        asyncDeleteCommand = new Command(
            this::deleteAsync,
            () -> getTeamMember() != null && getTeamMember().getId() != null // Define condition inline
        );

        asyncLoadTeamMembersCommand = new Command(this::loadTeamMembersAsync);
    }
    
    /**
     * Loads team members asynchronously.
     */
    public void loadTeamMembersAsync() {
        loading.set(true);
        
        teamMemberServiceAsync.findAllAsync(
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    ObservableList<TeamMember> teamMembers = getTeamMembers();
                    teamMembers.clear();
                    teamMembers.addAll(result);
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " team members asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading team members asynchronously", error);
                    loading.set(false);
                    // Show error notification
                    setErrorMessage("Failed to load team members: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Loads leaders asynchronously.
     */
    public void loadLeadersAsync() {
        loading.set(true);
        
        teamMemberServiceAsync.findLeadersAsync(
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    // Process leaders as needed
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " team leaders asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading team leaders asynchronously", error);
                    loading.set(false);
                    // Show error notification
                    setErrorMessage("Failed to load team leaders: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Saves the team member asynchronously.
     */
    public void saveAsync() {
        if (!isValid()) {
            return;
        }
        
        loading.set(true);
        
        try {
            TeamMember teamMember = getTeamMember();
            
            if (isNewTeamMember()) {
                // Create new team member
                teamMemberServiceAsync.createTeamMemberAsync(
                    teamMember.getUsername(),
                    teamMember.getFirstName(),
                    teamMember.getLastName(),
                    teamMember.getEmail(),
                    teamMember.getPhone(),
                    teamMember.isLeader(),
                    // Success handler
                    createdMember -> {
                        Platform.runLater(() -> {
                            // If a subteam is selected, assign the member to it
                            if (selectedSubteamProperty().get() != null) {
                                assignToSubteamAsync(createdMember, selectedSubteamProperty().get().getId());
                            } else {
                                finalizeTeamMemberSave(createdMember);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating team member asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create team member: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing team member
                // First update basic info
                teamMemberServiceAsync.saveAsync(
                    teamMember,
                    // Success handler
                    updatedMember -> {
                        Platform.runLater(() -> {
                            // Check if subteam assignment changed
                            Subteam currentSubteam = updatedMember.getSubteam();
                            Subteam selectedSubteam = selectedSubteamProperty().get();
                            
                            if ((currentSubteam == null && selectedSubteam != null) ||
                                (currentSubteam != null && selectedSubteam == null) ||
                                (currentSubteam != null && selectedSubteam != null && 
                                 !currentSubteam.getId().equals(selectedSubteam.getId()))) {
                                
                                // Subteam assignment changed, update it
                                Long subteamId = selectedSubteam != null ? selectedSubteam.getId() : null;
                                assignToSubteamAsync(updatedMember, subteamId);
                            } else {
                                // No subteam change needed, finalize
                                finalizeTeamMemberSave(updatedMember);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating team member asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to update team member: " + error.getMessage());
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in saveAsync method", e);
            setErrorMessage("Failed to save team member: " + e.getMessage());
        }
    }
    
    /**
     * Assigns a team member to a subteam asynchronously.
     * 
     * @param member the team member
     * @param subteamId the subteam ID
     */
    private void assignToSubteamAsync(TeamMember member, Long subteamId) {
        teamMemberServiceAsync.assignToSubteamAsync(
            member.getId(),
            subteamId,
            // Success handler
            updatedMember -> {
                Platform.runLater(() -> {
                    finalizeTeamMemberSave(updatedMember);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error assigning team member to subteam asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to assign team member to subteam: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Finalizes the team member save operation.
     * 
     * @param member the saved team member
     */
    private void finalizeTeamMemberSave(TeamMember member) {
        // Update team members list
        ObservableList<TeamMember> teamMembers = getTeamMembers();
        
        if (isNewTeamMember()) {
            // Add to members list
            teamMembers.add(member);
            
            // Create a new team member for next entry
            initNewTeamMember();
        } else {
            // Update in members list
            int index = -1;
            for (int i = 0; i < teamMembers.size(); i++) {
                if (teamMembers.get(i).getId().equals(member.getId())) {
                    index = i;
                    break;
                }
            }
            
            if (index >= 0) {
                teamMembers.set(index, member);
            }
        }
        
        // Clear dirty flag
        setDirty(false);
        loading.set(false);
        
        LOGGER.info("Saved team member asynchronously: " + member.getFullName());
    }
    
    /**
     * Deletes the current team member asynchronously.
     */
    public void deleteAsync() {
        TeamMember member = getTeamMember();
        if (member == null || member.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        teamMemberServiceAsync.deleteByIdAsync(
            member.getId(),
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        // Remove from team members list
                        getTeamMembers().remove(member);
                        
                        // Initialize a new team member
                        initNewTeamMember();
                        
                        LOGGER.info("Deleted team member: " + member.getFullName() + " asynchronously");
                    } else {
                        setErrorMessage("Failed to delete team member: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting team member asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to delete team member: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Checks if the delete command can be executed.
     * 
     * @return true if a team member with an ID is selected, false otherwise
     */
    private boolean canDelete() {
        TeamMember member = getTeamMember();
        return member != null && member.getId() != null;
    }
    
    /**
     * Updates a team member's skills asynchronously.
     * 
     * @param memberId the team member ID
     * @param skills the new skills
     */
    public void updateSkillsAsync(Long memberId, String skills) {
        if (memberId == null) {
            setErrorMessage("Member ID cannot be null");
            return;
        }
        
        loading.set(true);
        
        teamMemberServiceAsync.updateSkillsAsync(
            memberId,
            skills,
            // Success handler
            updatedMember -> {
                Platform.runLater(() -> {
                    // Update member in the list
                    updateMemberInList(updatedMember);
                    
                    loading.set(false);
                    LOGGER.info("Updated skills for team member: " + updatedMember.getFullName() + " asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error updating team member skills asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to update skills: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Updates a team member's contact information asynchronously.
     * 
     * @param memberId the team member ID
     * @param email the new email
     * @param phone the new phone
     */
    public void updateContactInfoAsync(Long memberId, String email, String phone) {
        if (memberId == null) {
            setErrorMessage("Member ID cannot be null");
            return;
        }
        
        loading.set(true);
        
        teamMemberServiceAsync.updateContactInfoAsync(
            memberId,
            email,
            phone,
            // Success handler
            updatedMember -> {
                Platform.runLater(() -> {
                    // Update member in the list
                    updateMemberInList(updatedMember);
                    
                    loading.set(false);
                    LOGGER.info("Updated contact info for team member: " + updatedMember.getFullName() + " asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error updating team member contact info asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to update contact info: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Utility method to update a team member in the list.
     * 
     * @param updatedMember the updated team member
     */
    private void updateMemberInList(TeamMember updatedMember) {
        ObservableList<TeamMember> teamMembers = getTeamMembers();
        
        int index = -1;
        for (int i = 0; i < teamMembers.size(); i++) {
            if (teamMembers.get(i).getId().equals(updatedMember.getId())) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            teamMembers.set(index, updatedMember);
        }
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
     * Gets whether the view model is currently loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the async save command.
     * 
     * @return the async save command
     */
    public Command getAsyncSaveCommand() {
        return asyncSaveCommand;
    }
    
    /**
     * Gets the async delete command.
     * 
     * @return the async delete command
     */
    public Command getAsyncDeleteCommand() {
        return asyncDeleteCommand;
    }
    
    /**
     * Gets the async load team members command.
     * 
     * @return the async load team members command
     */
    public Command getAsyncLoadTeamMembersCommand() {
        return asyncLoadTeamMembersCommand;
    }
    
    @Override
    public void clearErrorMessage() {
        Platform.runLater(() -> {
            super.clearErrorMessage();
        });
    }
    
    @Override
    public void cleanupResources() {
        super.cleanupResources();
    }
}