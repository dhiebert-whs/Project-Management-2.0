// src/main/java/org/frcpm/mvvm/viewmodels/TeamMemberDetailMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * ViewModel for the TeamMemberDetail view using MVVMFx.
 */
public class TeamMemberDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    private final SubteamService subteamService;
    
    // Observable properties for team member fields
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty skills = new SimpleStringProperty("");
    private final BooleanProperty isLeader = new SimpleBooleanProperty(false);
    private final BooleanProperty isNewTeamMember = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<TeamMember> teamMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Observable collections
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    
    /**
     * Creates a new TeamMemberDetailMvvmViewModel.
     * 
     * @param teamMemberService the team member service
     * @param subteamService the subteam service
     */
    @Inject
    public TeamMemberDetailMvvmViewModel(TeamMemberService teamMemberService, SubteamService subteamService) {
        this.teamMemberService = teamMemberService;
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        this.subteamService = subteamService;
        
        initializeCommands();
        setupValidation();
        loadSubteams();
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
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        username.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        email.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        firstName.addListener((obs, oldVal, newVal) -> setDirty(true));
        lastName.addListener((obs, oldVal, newVal) -> setDirty(true));
        phone.addListener((obs, oldVal, newVal) -> setDirty(true));
        skills.addListener((obs, oldVal, newVal) -> setDirty(true));
        isLeader.addListener((obs, oldVal, newVal) -> setDirty(true));
        selectedSubteam.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Loads subteams for the dropdown.
     */
    private void loadSubteams() {
        loading.set(true);
        
        // In a real implementation, this would use an async method
        try {
            subteams.clear();
            subteams.addAll(subteamService.findAll());
            loading.set(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteams", e);
            setErrorMessage("Failed to load subteams: " + e.getMessage());
            loading.set(false);
        }
    }
    
    /**
     * Validates the team member data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        StringBuilder errorMessages = new StringBuilder();
        
        // Check required fields
        if (username.get() == null || username.get().trim().isEmpty()) {
            errorMessages.append("Username is required\n");
        }
        
        // Validate email if provided
        if (email.get() != null && !email.get().trim().isEmpty() && !isValidEmail(email.get())) {
            errorMessages.append("Invalid email format\n");
        }
        
        // Update valid property and error message
        boolean isValid = errorMessages.length() == 0;
        valid.set(isValid);
        if (!isValid) {
            setErrorMessage(errorMessages.toString().trim());
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Simple email validation.
     * 
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Initializes the view model for a new team member.
     * 
     * @param member the team member to initialize
     */
    public void initNewTeamMember(TeamMember member) {
        this.teamMember.set(member);
        this.isNewTeamMember.set(true);
        
        // Set default values
        firstName.set("");
        lastName.set("");
        username.set("");
        email.set("");
        phone.set("");
        skills.set("");
        isLeader.set(false);
        selectedSubteam.set(null);
        
        // Clear dirty flag
        setDirty(false);
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing team member.
     * 
     * @param member the team member to edit
     */
    public void initExistingTeamMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Team member cannot be null");
        }
        
        this.teamMember.set(member);
        this.isNewTeamMember.set(false);
        
        // Set field values from team member
        firstName.set(member.getFirstName());
        lastName.set(member.getLastName());
        username.set(member.getUsername());
        email.set(member.getEmail());
        phone.set(member.getPhone());
        skills.set(member.getSkills());
        isLeader.set(member.isLeader());
        selectedSubteam.set(member.getSubteam());
        
        // Clear dirty flag
        setDirty(false);
        validate();
    }
    
    /**
     * Saves the team member.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }
        
        loading.set(true);
        
        try {
            // Update team member properties
            TeamMember memberToSave = teamMember.get();
            if (memberToSave == null) {
                memberToSave = new TeamMember();
            }
            
            memberToSave.setFirstName(firstName.get());
            memberToSave.setLastName(lastName.get());
            memberToSave.setUsername(username.get());
            memberToSave.setEmail(email.get());
            memberToSave.setPhone(phone.get());
            memberToSave.setSkills(skills.get());
            memberToSave.setLeader(isLeader.get());
            memberToSave.setSubteam(selectedSubteam.get());
            
            // Save the team member asynchronously
            teamMemberServiceAsync.saveAsync(
                memberToSave,
                // Success handler
                savedMember -> {
                    Platform.runLater(() -> {
                        teamMember.set(savedMember);
                        setDirty(false);
                        loading.set(false);
                        LOGGER.info("Team member saved successfully: " + savedMember.getFullName());
                    });
                },
                // Error handler
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving team member", error);
                        setErrorMessage("Failed to save team member: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save team member: " + e.getMessage());
        }
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the team member is valid, false otherwise
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
     * Gets the team member.
     * 
     * @return the team member
     */
    public TeamMember getTeamMember() {
        return teamMember.get();
    }
    
    /**
     * Gets the team member property.
     * 
     * @return the team member property
     */
    public ObjectProperty<TeamMember> teamMemberProperty() {
        return teamMember;
    }
    
    /**
     * Gets whether this is a new team member.
     * 
     * @return true if this is a new team member, false if editing an existing team member
     */
    public boolean isNewTeamMember() {
        return isNewTeamMember.get();
    }
    
    /**
     * Gets the new team member property.
     * 
     * @return the new team member property
     */
    public BooleanProperty isNewTeamMemberProperty() {
        return isNewTeamMember;
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
     * Gets the subteams list.
     * 
     * @return the subteams list
     */
    public ObservableList<Subteam> getSubteams() {
        return subteams;
    }
    
    /**
     * Gets the selected subteam property.
     * 
     * @return the selected subteam property
     */
    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }
    
    /**
     * Gets the selected subteam.
     * 
     * @return the selected subteam
     */
    public Subteam getSelectedSubteam() {
        return selectedSubteam.get();
    }
    
    /**
     * Sets the selected subteam.
     * 
     * @param subteam the selected subteam
     */
    public void setSelectedSubteam(Subteam subteam) {
        selectedSubteam.set(subteam);
    }
    
    // Property getters and setters
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    
    public String getFirstName() {
        return firstName.get();
    }
    
    public void setFirstName(String value) {
        firstName.set(value);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    
    public String getLastName() {
        return lastName.get();
    }
    
    public void setLastName(String value) {
        lastName.set(value);
    }
    
    public StringProperty usernameProperty() {
        return username;
    }
    
    public String getUsername() {
        return username.get();
    }
    
    public void setUsername(String value) {
        username.set(value);
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public void setEmail(String value) {
        email.set(value);
    }
    
    public StringProperty phoneProperty() {
        return phone;
    }
    
    public String getPhone() {
        return phone.get();
    }
    
    public void setPhone(String value) {
        phone.set(value);
    }
    
    public StringProperty skillsProperty() {
        return skills;
    }
    
    public String getSkills() {
        return skills.get();
    }
    
    public void setSkills(String value) {
        skills.set(value);
    }
    
    public BooleanProperty isLeaderProperty() {
        return isLeader;
    }
    
    public boolean isLeader() {
        return isLeader.get();
    }
    
    public void setIsLeader(boolean value) {
        isLeader.set(value);
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
        subteams.clear();
    }
}