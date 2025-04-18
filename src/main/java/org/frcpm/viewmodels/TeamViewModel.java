package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.ServiceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for team management in the FRC Project Management System.
 * Standardized implementation following MVVM pattern.
 */
public class TeamViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamViewModel.class.getName());
    
    // Services
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;
    
    // Team member properties
    private final StringProperty memberUsername = new SimpleStringProperty("");
    private final StringProperty memberFirstName = new SimpleStringProperty("");
    private final StringProperty memberLastName = new SimpleStringProperty("");
    private final StringProperty memberEmail = new SimpleStringProperty("");
    private final StringProperty memberPhone = new SimpleStringProperty("");
    private final StringProperty memberSkills = new SimpleStringProperty("");
    private final BooleanProperty memberIsLeader = new SimpleBooleanProperty(false);
    private final ObjectProperty<Subteam> memberSubteam = new SimpleObjectProperty<>();
    private final ObjectProperty<TeamMember> selectedMember = new SimpleObjectProperty<>();
    private final ObservableList<TeamMember> members = FXCollections.observableArrayList();
    private final BooleanProperty isNewMember = new SimpleBooleanProperty(true);
    private final BooleanProperty memberValid = new SimpleBooleanProperty(false);
    
    // Subteam properties
    private final StringProperty subteamName = new SimpleStringProperty("");
    private final StringProperty subteamColorCode = new SimpleStringProperty("#0000FF"); // Default blue
    private final StringProperty subteamSpecialties = new SimpleStringProperty("");
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    private final ObservableList<TeamMember> subteamMembers = FXCollections.observableArrayList();
    private final BooleanProperty isNewSubteam = new SimpleBooleanProperty(true);
    private final BooleanProperty subteamValid = new SimpleBooleanProperty(false);
    
    // Commands for team members
    private final Command createNewMemberCommand;
    private final Command deleteMemberCommand;
    private final Command editMemberCommand;
    private final Command saveMemberCommand;
    
    // Commands for subteams
    private final Command createNewSubteamCommand;
    private final Command deleteSubteamCommand;
    private final Command editSubteamCommand;
    private final Command saveSubteamCommand;
    private final Command loadSubteamMembersCommand;
    
    /**
     * Creates a new TeamViewModel with default services.
     */
    public TeamViewModel() {
        this(ServiceFactory.getTeamMemberService(), ServiceFactory.getSubteamService());
    }
    
    /**
     * Creates a new TeamViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param teamMemberService the team member service
     * @param subteamService the subteam service
     */
    public TeamViewModel(TeamMemberService teamMemberService, SubteamService subteamService) {
        this.teamMemberService = teamMemberService;
        this.subteamService = subteamService;
        
        // Create commands for team members
        createNewMemberCommand = new Command(this::createNewMember);
        deleteMemberCommand = new Command(this::deleteMember, this::canDeleteMember);
        editMemberCommand = new Command(this::editMember, this::canEditMember);
        saveMemberCommand = new Command(this::saveMember, this::canSaveMember);
        
        // Create commands for subteams
        createNewSubteamCommand = new Command(this::createNewSubteam);
        deleteSubteamCommand = new Command(this::deleteSubteam, this::canDeleteSubteam);
        editSubteamCommand = new Command(this::editSubteam, this::canEditSubteam);
        saveSubteamCommand = new Command(this::saveSubteam, this::canSaveSubteam);
        loadSubteamMembersCommand = new Command(this::loadSubteamMembers, this::canLoadSubteamMembers);
        
        // Set up validation listeners for member using the new BaseViewModel pattern
        Runnable memberValidator = createDirtyFlagHandler(this::validateMember);
        memberUsername.addListener((observable, oldValue, newValue) -> memberValidator.run());
        memberFirstName.addListener((observable, oldValue, newValue) -> memberValidator.run());
        memberLastName.addListener((observable, oldValue, newValue) -> memberValidator.run());
        memberEmail.addListener((observable, oldValue, newValue) -> memberValidator.run());
        
        // Track these listeners for cleanup
        trackPropertyListener(memberValidator);
        
        // Set up dirty flag listeners for member
        Runnable dirtyHandler = createDirtyFlagHandler(null);
        memberPhone.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        memberSkills.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        memberIsLeader.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        memberSubteam.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        
        // Track these listeners for cleanup
        trackPropertyListener(dirtyHandler);
        
        // Set up validation listeners for subteam using the new BaseViewModel pattern
        Runnable subteamValidator = createDirtyFlagHandler(this::validateSubteam);
        subteamName.addListener((observable, oldValue, newValue) -> subteamValidator.run());
        subteamColorCode.addListener((observable, oldValue, newValue) -> subteamValidator.run());
        
        // Track these listeners for cleanup
        trackPropertyListener(subteamValidator);
        
        // Set up dirty flag listeners for subteam
        subteamSpecialties.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        
        // Set up selection listeners
        selectedMember.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateMemberForm(newValue);
            } else {
                clearMemberForm();
            }
        });
        
        selectedSubteam.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSubteamForm(newValue);
                loadSubteamMembers();
            } else {
                clearSubteamForm();
                subteamMembers.clear();
            }
        });
        
        // Initial validation
        validateMember();
        validateSubteam();
        
        // Load initial data - this is called automatically in the constructor
        // which can cause issues for tests if not properly mocked
        loadMembers();
        loadSubteams();
    }
    
    /**
     * Validates the member form.
     */
    private void validateMember() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (memberUsername.get() == null || memberUsername.get().trim().isEmpty()) {
            errors.add("Username is required");
        }
        
        if (memberFirstName.get() == null || memberFirstName.get().trim().isEmpty()) {
            errors.add("First name is required");
        }
        
        // Check email format if provided
        if (memberEmail.get() != null && !memberEmail.get().trim().isEmpty() &&
                !memberEmail.get().contains("@")) {
            errors.add("Email must be a valid email address");
        }
        
        // Update valid state and error message
        memberValid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Validates the subteam form.
     */
    private void validateSubteam() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (subteamName.get() == null || subteamName.get().trim().isEmpty()) {
            errors.add("Subteam name is required");
        }
        
        // Check color code format
        if (subteamColorCode.get() == null || !subteamColorCode.get().matches("^#[0-9A-Fa-f]{6}$")) {
            errors.add("Color code must be a valid hex color code (e.g., #FF0000)");
        }
        
        // Update valid state and error message
        subteamValid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Loads the list of team members.
     * This method is public to allow direct calling from tests.
     */
    public void loadMembers() {
        try {
            List<TeamMember> memberList = teamMemberService.findAll();
            members.clear();
            if (memberList != null) {
                members.addAll(memberList);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team members", e);
            setErrorMessage("Failed to load team members: " + e.getMessage());
        }
    }
    
    /**
     * Loads the list of subteams.
     * This method is public to allow direct calling from tests.
     */
    public void loadSubteams() {
        try {
            List<Subteam> subteamList = subteamService.findAll();
            subteams.clear();
            if (subteamList != null) {
                subteams.addAll(subteamList);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteams", e);
            setErrorMessage("Failed to load subteams: " + e.getMessage());
        }
    }
    
    /**
     * Loads the list of members for the selected subteam.
     */
    private void loadSubteamMembers() {
        try {
            Subteam subteam = selectedSubteam.get();
            if (subteam != null) {
                List<TeamMember> memberList = teamMemberService.findBySubteam(subteam);
                subteamMembers.clear();
                if (memberList != null) {
                    subteamMembers.addAll(memberList);
                }
            } else {
                subteamMembers.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteam members", e);
            setErrorMessage("Failed to load subteam members: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the form for creating a new team member.
     */
    public void initNewMember() {
        selectedMember.set(null);
        isNewMember.set(true);
        
        // Set default values
        memberUsername.set("");
        memberFirstName.set("");
        memberLastName.set("");
        memberEmail.set("");
        memberPhone.set("");
        memberSkills.set("");
        memberIsLeader.set(false);
        memberSubteam.set(null);
        
        // Clear dirty flag and validate
        setDirty(false);
        validateMember();
    }
    
    /**
     * Sets up the form for editing an existing team member.
     * 
     * @param member the team member to edit
     */
    public void initExistingMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Team member cannot be null");
        }
        
        selectedMember.set(member);
        isNewMember.set(false);
        
        // Update form from member
        updateMemberForm(member);
        
        // Clear dirty flag and validate
        setDirty(false);
        validateMember();
    }
    
    /**
     * Updates the member form fields from a team member.
     * 
     * @param member the team member to get values from
     */
    private void updateMemberForm(TeamMember member) {
        memberUsername.set(member.getUsername());
        memberFirstName.set(member.getFirstName());
        memberLastName.set(member.getLastName());
        memberEmail.set(member.getEmail());
        memberPhone.set(member.getPhone());
        memberSkills.set(member.getSkills());
        memberIsLeader.set(member.isLeader());
        memberSubteam.set(member.getSubteam());
    }
    
    /**
     * Clears the member form fields.
     */
    private void clearMemberForm() {
        memberUsername.set("");
        memberFirstName.set("");
        memberLastName.set("");
        memberEmail.set("");
        memberPhone.set("");
        memberSkills.set("");
        memberIsLeader.set(false);
        memberSubteam.set(null);
        
        // Clear error message
        clearErrorMessage();
    }
    
    /**
     * Sets up the form for creating a new subteam.
     */
    public void initNewSubteam() {
        selectedSubteam.set(null);
        isNewSubteam.set(true);
        
        // Set default values
        subteamName.set("");
        subteamColorCode.set("#0000FF"); // Default blue
        subteamSpecialties.set("");
        
        // Clear dirty flag and validate
        setDirty(false);
        validateSubteam();
    }
    
    /**
     * Sets up the form for editing an existing subteam.
     * 
     * @param subteam the subteam to edit
     */
    public void initExistingSubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        
        selectedSubteam.set(subteam);
        isNewSubteam.set(false);
        
        // Update form from subteam
        updateSubteamForm(subteam);
        
        // Load subteam members
        loadSubteamMembers();
        
        // Clear dirty flag and validate
        setDirty(false);
        validateSubteam();
    }
    
    /**
     * Updates the subteam form fields from a subteam.
     * 
     * @param subteam the subteam to get values from
     */
    private void updateSubteamForm(Subteam subteam) {
        subteamName.set(subteam.getName());
        subteamColorCode.set(subteam.getColorCode());
        subteamSpecialties.set(subteam.getSpecialties());
    }
    
    /**
     * Clears the subteam form fields.
     */
    private void clearSubteamForm() {
        subteamName.set("");
        subteamColorCode.set("#0000FF"); // Default blue
        subteamSpecialties.set("");
        
        // Clear error message
        clearErrorMessage();
    }
    
    /**
     * Creates a new team member.
     * Called when the create new member command is executed.
     */
    private void createNewMember() {
        initNewMember();
    }
    
    /**
     * Edits a team member.
     * Called when the edit member command is executed.
     */
    private void editMember() {
        TeamMember member = selectedMember.get();
        if (member != null) {
            initExistingMember(member);
        }
    }
    
    /**
     * Deletes the selected team member.
     * Called when the delete member command is executed.
     */
    private void deleteMember() {
        try {
            TeamMember member = selectedMember.get();
            if (member != null) {
                boolean success = teamMemberService.deleteById(member.getId());
                
                if (success) {
                    // Remove from members list
                    members.remove(member);
                    
                    // Clear selection
                    selectedMember.set(null);
                    
                    // If the deleted member was in the selected subteam, refresh the subteam members list
                    if (selectedSubteam.get() != null) {
                        loadSubteamMembers();
                    }
                } else {
                    setErrorMessage("Failed to delete team member: Operation returned false");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting team member", e);
            setErrorMessage("Failed to delete team member: " + e.getMessage());
        }
    }
    
    /**
     * Saves the team member.
     * Called when the save member command is executed.
     */
    private void saveMember() {
        if (!memberValid.get()) {
            return;
        }
        
        try {
            TeamMember member;
            
            if (isNewMember.get()) {
                // Create new team member
                member = teamMemberService.createTeamMember(
                    memberUsername.get(),
                    memberFirstName.get(),
                    memberLastName.get(),
                    memberEmail.get(),
                    memberPhone.get(),
                    memberIsLeader.get()
                );
                
                // Update additional fields
                if (memberSkills.get() != null && !memberSkills.get().isEmpty()) {
                    member = teamMemberService.updateSkills(
                        member.getId(),
                        memberSkills.get()
                    );
                }
                
                // Add to members list
                members.add(member);
            } else {
                // Update existing member
                member = selectedMember.get();
                
                // Update name and leader status
                member.setFirstName(memberFirstName.get());
                member.setLastName(memberLastName.get());
                member.setLeader(memberIsLeader.get());
                member = teamMemberService.save(member);
                
                // Update contact info
                member = teamMemberService.updateContactInfo(
                    member.getId(),
                    memberEmail.get(),
                    memberPhone.get()
                );
                
                // Update skills
                if (memberSkills.get() != null && !memberSkills.get().isEmpty()) {
                    member = teamMemberService.updateSkills(
                        member.getId(),
                        memberSkills.get()
                    );
                }
                
                // Update in members list
                int index = members.indexOf(selectedMember.get());
                if (index >= 0) {
                    members.set(index, member);
                }
            }
            
            // Assign to subteam if one is selected
            if (memberSubteam.get() != null) {
                member = teamMemberService.assignToSubteam(
                    member.getId(),
                    memberSubteam.get().getId()
                );
            }
            
            // Update selected member
            selectedMember.set(member);
            
            // Clear dirty flag
            setDirty(false);
            
            // If member was added to the selected subteam, refresh the subteam members list
            if (selectedSubteam.get() != null && selectedSubteam.get().equals(memberSubteam.get())) {
                loadSubteamMembers();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving team member", e);
            setErrorMessage("Failed to save team member: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new subteam.
     * Called when the create new subteam command is executed.
     */
    private void createNewSubteam() {
        initNewSubteam();
    }
    
    /**
     * Edits a subteam.
     * Called when the edit subteam command is executed.
     */
    private void editSubteam() {
        Subteam subteam = selectedSubteam.get();
        if (subteam != null) {
            initExistingSubteam(subteam);
        }
    }
    
    /**
     * Deletes the selected subteam.
     * Called when the delete subteam command is executed.
     */
    private void deleteSubteam() {
        try {
            Subteam subteam = selectedSubteam.get();
            if (subteam != null) {
                // Check if the subteam has members
                List<TeamMember> subteamMembers = teamMemberService.findBySubteam(subteam);
                if (subteamMembers != null && !subteamMembers.isEmpty()) {
                    setErrorMessage("Cannot delete subteam that has members assigned to it. Reassign members first.");
                    return;
                }
                
                boolean success = subteamService.deleteById(subteam.getId());
                
                if (success) {
                    // Remove from subteams list
                    subteams.remove(subteam);
                    
                    // Clear selection
                    selectedSubteam.set(null);
                } else {
                    setErrorMessage("Failed to delete subteam: Operation returned false");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
            setErrorMessage("Failed to delete subteam: " + e.getMessage());
        }
    }
    
    /**
     * Saves the subteam.
     * Called when the save subteam command is executed.
     */
    private void saveSubteam() {
        if (!subteamValid.get()) {
            return;
        }
        
        try {
            Subteam subteam;
            
            if (isNewSubteam.get()) {
                // Create new subteam
                subteam = subteamService.createSubteam(
                    subteamName.get(),
                    subteamColorCode.get(),
                    subteamSpecialties.get()
                );
                
                // Add to subteams list
                subteams.add(subteam);
            } else {
                // Update existing subteam
                subteam = selectedSubteam.get();
                
                // Update color code and specialties
                subteam = subteamService.updateColorCode(
                    subteam.getId(),
                    subteamColorCode.get()
                );
                
                subteam = subteamService.updateSpecialties(
                    subteam.getId(),
                    subteamSpecialties.get()
                );
                
                // Update in subteams list
                int index = subteams.indexOf(selectedSubteam.get());
                if (index >= 0) {
                    subteams.set(index, subteam);
                }
            }
            
            // Update selected subteam
            selectedSubteam.set(subteam);
            
            // Clear dirty flag
            setDirty(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subteam", e);
            setErrorMessage("Failed to save subteam: " + e.getMessage());
        }
    }
    
    /**
     * Checks if the save member command can be executed.
     * 
     * @return true if the member form is valid, false otherwise
     */
    private boolean canSaveMember() {
        return memberValid.get();
    }
    
    /**
     * Checks if the delete member command can be executed.
     * 
     * @return true if a member is selected, false otherwise
     */
    private boolean canDeleteMember() {
        return selectedMember.get() != null;
    }
    
    /**
     * Checks if the edit member command can be executed.
     * 
     * @return true if a member is selected, false otherwise
     */
    private boolean canEditMember() {
        return selectedMember.get() != null;
    }
    
    /**
     * Checks if the save subteam command can be executed.
     * 
     * @return true if the subteam form is valid, false otherwise
     */
    private boolean canSaveSubteam() {
        return subteamValid.get();
    }
    
    /**
     * Checks if the delete subteam command can be executed.
     * 
     * @return true if a subteam is selected, false otherwise
     */
    private boolean canDeleteSubteam() {
        return selectedSubteam.get() != null;
    }
    
    /**
     * Checks if the edit subteam command can be executed.
     * 
     * @return true if a subteam is selected, false otherwise
     */
    private boolean canEditSubteam() {
        return selectedSubteam.get() != null;
    }
    
    /**
     * Checks if the load subteam members command can be executed.
     * 
     * @return true if a subteam is selected, false otherwise
     */
    private boolean canLoadSubteamMembers() {
        return selectedSubteam.get() != null;
    }
    
    // Property accessors for team members
    
    public StringProperty memberUsernameProperty() {
        return memberUsername;
    }
    
    public StringProperty memberFirstNameProperty() {
        return memberFirstName;
    }
    
    public StringProperty memberLastNameProperty() {
        return memberLastName;
    }
    
    public StringProperty memberEmailProperty() {
        return memberEmail;
    }
    
    public StringProperty memberPhoneProperty() {
        return memberPhone;
    }
    
    public StringProperty memberSkillsProperty() {
        return memberSkills;
    }
    
    public BooleanProperty memberIsLeaderProperty() {
        return memberIsLeader;
    }
    
    public ObjectProperty<Subteam> memberSubteamProperty() {
        return memberSubteam;
    }
    
    public ObjectProperty<TeamMember> selectedMemberProperty() {
        return selectedMember;
    }
    
    public ObservableList<TeamMember> getMembers() {
        return members;
    }
    
    public BooleanProperty isNewMemberProperty() {
        return isNewMember;
    }
    
    public BooleanProperty memberValidProperty() {
        return memberValid;
    }
    
    // Property accessors for subteams
    
    public StringProperty subteamNameProperty() {
        return subteamName;
    }
    
    public StringProperty subteamColorCodeProperty() {
        return subteamColorCode;
    }
    
    public StringProperty subteamSpecialtiesProperty() {
        return subteamSpecialties;
    }
    
    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }
    
    public ObservableList<Subteam> getSubteams() {
        return subteams;
    }
    
    public ObservableList<TeamMember> getSubteamMembers() {
        return subteamMembers;
    }
    
    public BooleanProperty isNewSubteamProperty() {
        return isNewSubteam;
    }
    
    public BooleanProperty subteamValidProperty() {
        return subteamValid;
    }
    
    // Command accessors
    
    public Command getCreateNewMemberCommand() {
        return createNewMemberCommand;
    }
    
    public Command getDeleteMemberCommand() {
        return deleteMemberCommand;
    }
    
    public Command getEditMemberCommand() {
        return editMemberCommand;
    }
    
    public Command getSaveMemberCommand() {
        return saveMemberCommand;
    }
    
    public Command getCreateNewSubteamCommand() {
        return createNewSubteamCommand;
    }
    
    public Command getDeleteSubteamCommand() {
        return deleteSubteamCommand;
    }
    
    public Command getEditSubteamCommand() {
        return editSubteamCommand;
    }
    
    public Command getSaveSubteamCommand() {
        return saveSubteamCommand;
    }
    
    public Command getLoadSubteamMembersCommand() {
        return loadSubteamMembersCommand;
    }
    
    // Getters and setters
    
    public String getMemberUsername() {
        return memberUsername.get();
    }
    
    public void setMemberUsername(String username) {
        memberUsername.set(username);
    }
    
    public String getMemberFirstName() {
        return memberFirstName.get();
    }
    
    public void setMemberFirstName(String firstName) {
        memberFirstName.set(firstName);
    }
    
    public String getMemberLastName() {
        return memberLastName.get();
    }
    
    public void setMemberLastName(String lastName) {
        memberLastName.set(lastName);
    }
    
    public String getMemberEmail() {
        return memberEmail.get();
    }
    
    public void setMemberEmail(String email) {
        memberEmail.set(email);
    }
    
    public String getMemberPhone() {
        return memberPhone.get();
    }
    
    public void setMemberPhone(String phone) {
        memberPhone.set(phone);
    }
    
    public String getMemberSkills() {
        return memberSkills.get();
    }
    
    public void setMemberSkills(String skills) {
        memberSkills.set(skills);
    }
    
    public boolean getMemberIsLeader() {
        return memberIsLeader.get();
    }
    
    public void setMemberIsLeader(boolean isLeader) {
        memberIsLeader.set(isLeader);
    }
    
    public Subteam getMemberSubteam() {
        return memberSubteam.get();
    }
    
    public void setMemberSubteam(Subteam subteam) {
        memberSubteam.set(subteam);
    }
    
    public TeamMember getSelectedMember() {
        return selectedMember.get();
    }
    
    public void setSelectedMember(TeamMember member) {
        selectedMember.set(member);
    }
    
    public boolean isNewMember() {
        return isNewMember.get();
    }
    
    public boolean isMemberValid() {
        return memberValid.get();
    }
    
    public String getSubteamName() {
        return subteamName.get();
    }
    
    public void setSubteamName(String name) {
        subteamName.set(name);
    }
    
    public String getSubteamColorCode() {
        return subteamColorCode.get();
    }
    
    public void setSubteamColorCode(String colorCode) {
        subteamColorCode.set(colorCode);
    }
    
    public String getSubteamSpecialties() {
        return subteamSpecialties.get();
    }
    
    public void setSubteamSpecialties(String specialties) {
        subteamSpecialties.set(specialties);
    }
    
    public Subteam getSelectedSubteam() {
        return selectedSubteam.get();
    }
    
    public void setSelectedSubteam(Subteam subteam) {
        selectedSubteam.set(subteam);
    }
    
    public boolean isNewSubteam() {
        return isNewSubteam.get();
    }
    
    public boolean isSubteamValid() {
        return subteamValid.get();
    }
    
    /**
     * Clears the error message.
     * Public method to be accessed by the controller.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }
    
    /**
     * Override the cleanupResources method from BaseViewModel
     * to clean up any additional resources specific to this ViewModel.
     */
    @Override
    public void cleanupResources() {
        // Call super to cleanup tracked listeners
        super.cleanupResources();
        
        // Clear collections to help garbage collection
        members.clear();
        subteams.clear();
        subteamMembers.clear();
    }
}