// src/main/java/org/frcpm/mvvm/viewmodels/SubteamDetailMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.SubteamServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * ViewModel for the SubteamDetail view using MVVMFx.
 */
public class SubteamDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final SubteamService subteamService;
    private final SubteamServiceAsyncImpl subteamServiceAsync;
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    // Observable properties for subteam fields
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty colorCode = new SimpleStringProperty("#007BFF"); // Default blue color
    private final StringProperty specialties = new SimpleStringProperty("");
    private final BooleanProperty isNewSubteam = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Subteam> subteam = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final ObjectProperty<TeamMember> selectedTeamMember = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Observable collections
    private final ObservableList<TeamMember> teamMembers = FXCollections.observableArrayList();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    private Command manageMembersCommand;
    private Command removeTeamMemberCommand;
    
    /**
     * Creates a new SubteamDetailMvvmViewModel.
     * 
     * @param subteamService the subteam service
     * @param teamMemberService the team member service
     */
   
    public SubteamDetailMvvmViewModel(SubteamService subteamService, TeamMemberService teamMemberService) {
        this.subteamService = subteamService;
        this.subteamServiceAsync = (SubteamServiceAsyncImpl) subteamService;
        this.teamMemberService = teamMemberService;
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        
        initializeCommands();
        setupValidation();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save command
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        
        // Cancel command - implemented by the view to close dialog
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            LOGGER.info("Cancel command executed");
        });
        
        // Manage members command - opens a dialog to manage members
        manageMembersCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view to show dialog
                LOGGER.info("Manage members command executed");
            },
            this::canManageMembers
        );
        
        // Remove team member command
        removeTeamMemberCommand = createValidOnlyCommand(
            this::removeTeamMemberAsync,
            this::canRemoveTeamMember
        );
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        name.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        colorCode.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        specialties.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the subteam data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (name.get() == null || name.get().trim().isEmpty()) {
            errors.add("Subteam name is required");
        }
        
        // Color code must be a valid hex color
        if (colorCode.get() == null || !colorCode.get().matches("^#[0-9A-Fa-f]{6}$")) {
            errors.add("Color code must be a valid hex color code (e.g., #007BFF)");
        }
        
        // Update valid property and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Initializes the view model for a new subteam.
     */
    public void initNewSubteam() {
        // Create a new subteam
        Subteam newSubteam = new Subteam();
        this.subteam.set(newSubteam);
        this.isNewSubteam.set(true);
        
        // Reset properties
        name.set("");
        colorCode.set("#007BFF"); // Default blue color
        specialties.set("");
        
        // Clear team members
        teamMembers.clear();
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing subteam.
     * 
     * @param subteam the subteam to edit
     */
    public void initExistingSubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        
        this.subteam.set(subteam);
        this.isNewSubteam.set(false);
        
        // Set properties from subteam
        name.set(subteam.getName());
        colorCode.set(subteam.getColorCode());
        specialties.set(subteam.getSpecialties());
        
        // Load team members
        loadTeamMembersAsync();
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Loads team members for this subteam.
     */
    private void loadTeamMembersAsync() {
        Subteam existingSubteam = subteam.get();
        if (existingSubteam == null || existingSubteam.getId() == null || loading.get()) {
            return;
        }
        
        loading.set(true);
        
        // Use the async service to load team members
        teamMemberServiceAsync.findBySubteamAsync(
            existingSubteam,
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    teamMembers.clear();
                    teamMembers.addAll(result);
                    LOGGER.info("Loaded " + result.size() + " team members for subteam asynchronously");
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading team members for subteam asynchronously", error);
                    setErrorMessage("Failed to load team members: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Saves the subteam.
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }
        
        loading.set(true);
        
        try {
            // Get the current subteam
            Subteam subteamToSave = subteam.get();
            if (subteamToSave == null) {
                subteamToSave = new Subteam();
            }
            
            // Update subteam from properties
            subteamToSave.setName(name.get());
            subteamToSave.setColorCode(colorCode.get());
            subteamToSave.setSpecialties(specialties.get());
            
            // Use createSubteamAsync for new subteams
            if (isNewSubteam.get()) {
                subteamServiceAsync.createSubteamAsync(
                    name.get(),
                    colorCode.get(),
                    specialties.get(),
                    // Success handler
                    savedSubteam -> {
                        Platform.runLater(() -> {
                            subteam.set(savedSubteam);
                            setDirty(false);
                            isNewSubteam.set(false);
                            loading.set(false);
                            LOGGER.info("Subteam created successfully: " + savedSubteam.getName());
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating subteam", error);
                            setErrorMessage("Failed to create subteam: " + error.getMessage());
                            loading.set(false);
                        });
                    }
                );
            } else {
                // For existing subteams, update individually
                final Long subteamId = subteamToSave.getId();
                if (subteamId != null) {
                    // First update color code
                    subteamServiceAsync.updateColorCodeAsync(
                        subteamId,
                        colorCode.get(),
                        // Success handler for color code update
                        updatedSubteam -> {
                            // Then update specialties
                            subteamServiceAsync.updateSpecialtiesAsync(
                                subteamId,
                                specialties.get(),
                                // Success handler for specialties update
                                finalSubteam -> {
                                    Platform.runLater(() -> {
                                        subteam.set(finalSubteam);
                                        setDirty(false);
                                        loading.set(false);
                                        LOGGER.info("Subteam updated successfully: " + finalSubteam.getName());
                                    });
                                },
                                // Error handler for specialties update
                                error -> {
                                    Platform.runLater(() -> {
                                        LOGGER.log(Level.SEVERE, "Error updating subteam specialties", error);
                                        setErrorMessage("Failed to update subteam specialties: " + error.getMessage());
                                        loading.set(false);
                                    });
                                }
                            );
                        },
                        // Error handler for color code update
                        error -> {
                            Platform.runLater(() -> {
                                LOGGER.log(Level.SEVERE, "Error updating subteam color code", error);
                                setErrorMessage("Failed to update subteam color code: " + error.getMessage());
                                loading.set(false);
                            });
                        }
                    );
                } else {
                    loading.set(false);
                    setErrorMessage("Cannot update subteam: ID is null");
                }
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save subteam: " + e.getMessage());
        }
    }
    
    /**
     * Checks if subteam members can be managed.
     * 
     * @return true if members can be managed, false otherwise
     */
    private boolean canManageMembers() {
        // Can only manage members for saved subteams
        return subteam.get() != null && subteam.get().getId() != null && !loading.get();
    }
    
    /**
     * Checks if a team member can be removed from the subteam.
     * 
     * @return true if a team member can be removed, false otherwise
     */
    private boolean canRemoveTeamMember() {
        return selectedTeamMember.get() != null && !loading.get();
    }
    
    /**
     * Removes a team member from this subteam.
     */
    private void removeTeamMemberAsync() {
        TeamMember member = selectedTeamMember.get();
        if (member == null || member.getId() == null) {
            return;
        }
        
        Subteam currentSubteam = subteam.get();
        if (currentSubteam == null || currentSubteam.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        teamMemberServiceAsync.assignToSubteamAsync(
            member.getId(),
            null, // Setting to null removes from subteam
            // Success handler
            updatedMember -> {
                Platform.runLater(() -> {
                    // Remove from team members list
                    teamMembers.remove(member);
                    loading.set(false);
                    LOGGER.info("Removed team member from subteam successfully");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error removing team member from subteam", error);
                    setErrorMessage("Failed to remove team member: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Adds a team member to this subteam.
     * 
     * @param teamMember the team member to add
     * @return true if successful, false otherwise
     */
    public boolean addTeamMember(TeamMember teamMember) {
        if (teamMember == null) {
            LOGGER.warning("Cannot add null team member");
            return false;
        }
        
        if (subteam.get() == null || subteam.get().getId() == null) {
            setErrorMessage("Subteam must be saved before adding members");
            return false;
        }
        
        // Check if the team member is already in the subteam
        for (TeamMember existingMember : teamMembers) {
            if (existingMember.getId().equals(teamMember.getId())) {
                setErrorMessage("Team member is already in this subteam");
                return false;
            }
        }
        
        loading.set(true);
        
        try {
            // Use the async service to assign the team member to the subteam
            teamMemberServiceAsync.assignToSubteamAsync(
                teamMember.getId(),
                subteam.get().getId(),
                // Success handler
                updatedMember -> {
                    Platform.runLater(() -> {
                        // Reload team members to reflect the changes
                        loadTeamMembersAsync();
                        
                        // Mark as dirty
                        setDirty(true);
                        
                        LOGGER.info("Team member added to subteam successfully");
                    });
                },
                // Error handler
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error adding team member to subteam", error);
                        setErrorMessage("Failed to add team member: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
            
            return true;
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error adding team member to subteam", e);
            setErrorMessage("Failed to add team member: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the subteam is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }
    
    /**
     * Gets the valid property.
     * 
     * @return the valid property
     */
    public BooleanProperty validProperty() {
        return valid;
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
     * Gets whether the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the subteam.
     * 
     * @return the subteam
     */
    public Subteam getSubteam() {
        return subteam.get();
    }
    
    /**
     * Gets the subteam property.
     * 
     * @return the subteam property
     */
    public ObjectProperty<Subteam> subteamProperty() {
        return subteam;
    }
    
    /**
     * Gets whether this is a new subteam.
     * 
     * @return true if this is a new subteam, false if editing an existing subteam
     */
    public boolean isNewSubteam() {
        return isNewSubteam.get();
    }
    
    /**
     * Gets the new subteam property.
     * 
     * @return the new subteam property
     */
    public BooleanProperty isNewSubteamProperty() {
        return isNewSubteam;
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
     * Gets the save command.
     * 
     * @return the save command
     */
    public Command getSaveCommand() {
        return saveCommand;
    }
    
/**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    /**
     * Gets the manage members command.
     * 
     * @return the manage members command
     */
    public Command getManageMembersCommand() {
        return manageMembersCommand;
    }
    
    /**
     * Gets the remove team member command.
     * 
     * @return the remove team member command
     */
    public Command getRemoveTeamMemberCommand() {
        return removeTeamMemberCommand;
    }
    
    // Property getters and setters
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String value) {
        name.set(value);
    }
    
    public StringProperty colorCodeProperty() {
        return colorCode;
    }
    
    public String getColorCode() {
        return colorCode.get();
    }
    
    public void setColorCode(String value) {
        colorCode.set(value);
    }
    
    public StringProperty specialtiesProperty() {
        return specialties;
    }
    
    public String getSpecialties() {
        return specialties.get();
    }
    
    public void setSpecialties(String value) {
        specialties.set(value);
    }
    
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    public void setCurrentProject(Project value) {
        currentProject.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        teamMembers.clear();
    }
}