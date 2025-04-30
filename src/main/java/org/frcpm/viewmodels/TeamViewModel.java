// src/main/java/org/frcpm/viewmodels/TeamViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ViewModel for team management in the FRC Project Management System.
 * Provides functionality for managing team members and subteams.
 */
public class TeamViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamViewModel.class.getName());
    
    /**
     * Enum for filtering team members.
     */
    public enum MemberFilter {
        ALL("All Members"),
        LEADERS("Leaders Only"),
        UNASSIGNED("Unassigned");
        
        private final String displayName;
        
        MemberFilter(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Services
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;
    private final ProjectService projectService;
    
    // Project
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    
    // Member properties
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final ObjectProperty<TeamMember.Role> role = new SimpleObjectProperty<>();
    private final StringProperty skills = new SimpleStringProperty("");
    private final BooleanProperty isLeader = new SimpleBooleanProperty(false);
    private final ObjectProperty<Subteam> memberSubteam = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty("");
    
    // Subteam properties
    private final StringProperty subteamName = new SimpleStringProperty("");
    private final StringProperty subteamColor = new SimpleStringProperty("#2196F3"); // Default blue
    private final StringProperty subteamLead = new SimpleStringProperty("");
    private final StringProperty subteamDescription = new SimpleStringProperty("");
    
    // Collections
    private final ObservableList<TeamMember> members = FXCollections.observableArrayList();
    private final FilteredList<TeamMember> filteredMembers = new FilteredList<>(members);
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    
    // Selection
    private final ObjectProperty<TeamMember> selectedMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    
    // Filtering
    private MemberFilter currentFilter = MemberFilter.ALL;
    private String searchText = "";
    
    // State
    private final BooleanProperty isNewMember = new SimpleBooleanProperty(true);
    private final BooleanProperty isNewSubteam = new SimpleBooleanProperty(true);
    
    // Commands
    private final Command addMemberCommand;
    private final Command editMemberCommand;
    private final Command deleteMemberCommand;
    private final Command saveMemberCommand;
    private final Command addSubteamCommand;
    private final Command editSubteamCommand;
    private final Command deleteSubteamCommand;
    private final Command saveSubteamCommand;
    private final Command importMembersCommand;
    private final Command exportMembersCommand;
    
    /**
     * Creates a new TeamViewModel with default services.
     */
    public TeamViewModel() {
        this(
            ServiceFactory.getTeamMemberService(),
            ServiceFactory.getSubteamService(),
            ServiceFactory.getProjectService()
        );
    }
    
    /**
     * Creates a new TeamViewModel with the specified TeamMemberService and SubteamService.
     * This constructor is used by the TeamPresenter.
     * 
     * @param teamMemberService the team member service
     * @param subteamService the subteam service
     */
    public TeamViewModel(TeamMemberService teamMemberService, SubteamService subteamService) {
        this(teamMemberService, subteamService, ServiceFactory.getProjectService());
    }
    
    /**
     * Creates a new TeamViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param teamMemberService the team member service
     * @param subteamService the subteam service
     * @param projectService the project service
     */
    public TeamViewModel(TeamMemberService teamMemberService, SubteamService subteamService, ProjectService projectService) {
        this.teamMemberService = teamMemberService;
        this.subteamService = subteamService;
        this.projectService = projectService;
        
        // Initialize commands
        addMemberCommand = new Command(this::initNewMember);
        editMemberCommand = new Command(this::editMember, this::canEditMember);
        deleteMemberCommand = new Command(this::deleteMember, this::canDeleteMember);
        saveMemberCommand = createValidOnlyCommand(this::saveMember, this::isValidMember);
        
        addSubteamCommand = new Command(this::initNewSubteam);
        editSubteamCommand = new Command(this::editSubteam, this::canEditSubteam);
        deleteSubteamCommand = new Command(this::deleteSubteam, this::canDeleteSubteam);
        saveSubteamCommand = createValidOnlyCommand(this::saveSubteam, this::isValidSubteam);
        
        importMembersCommand = new Command(this::importMembersFromCSV);
        exportMembersCommand = new Command(this::exportMembersToCSV);
        
        // Setup property change listeners
        setupPropertyListeners();
        
        // Setup filter predicate
        updateFilterPredicate();
        
        // Set up project property listener
        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMembers();
                loadSubteams();
            } else {
                members.clear();
                subteams.clear();
            }
        });
    }
    
    /**
     * Sets up property change listeners for dirty flag and validation.
     */
    private void setupPropertyListeners() {
        // Member property listeners
        Runnable memberValidateHandler = createDirtyFlagHandler(this::validateMember);
        firstName.addListener((obs, oldVal, newVal) -> memberValidateHandler.run());
        lastName.addListener((obs, oldVal, newVal) -> memberValidateHandler.run());
        email.addListener((obs, oldVal, newVal) -> memberValidateHandler.run());
        trackPropertyListener(memberValidateHandler);
        
        Runnable memberDirtyHandler = createDirtyFlagHandler(null);
        phone.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        role.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        skills.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        isLeader.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        memberSubteam.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        notes.addListener((obs, oldVal, newVal) -> memberDirtyHandler.run());
        trackPropertyListener(memberDirtyHandler);
        
        // Subteam property listeners
        Runnable subteamValidateHandler = createDirtyFlagHandler(this::validateSubteam);
        subteamName.addListener((obs, oldVal, newVal) -> subteamValidateHandler.run());
        trackPropertyListener(subteamValidateHandler);
        
        Runnable subteamDirtyHandler = createDirtyFlagHandler(null);
        subteamColor.addListener((obs, oldVal, newVal) -> subteamDirtyHandler.run());
        subteamLead.addListener((obs, oldVal, newVal) -> subteamDirtyHandler.run());
        subteamDescription.addListener((obs, oldVal, newVal) -> subteamDirtyHandler.run());
        trackPropertyListener(subteamDirtyHandler);
        
        // Selection listeners
        selectedMember.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateMemberForm(newVal);
            }
        });
        
        selectedSubteam.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSubteamForm(newVal);
            }
        });
    }
    
    /**
     * Validates the current member form data.
     */
    private void validateMember() {
        boolean isValid = (firstName.get() != null && !firstName.get().trim().isEmpty()) ||
                         (lastName.get() != null && !lastName.get().trim().isEmpty());
        
        // Check email format if provided
        if (isValid && email.get() != null && !email.get().trim().isEmpty() && !isValidEmail(email.get())) {
            isValid = false;
            setErrorMessage("Invalid email format");
        } else if (!isValid) {
            setErrorMessage("First name or last name is required");
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Validates the current subteam form data.
     */
    private void validateSubteam() {
        boolean isValid = subteamName.get() != null && !subteamName.get().trim().isEmpty();
        
        if (!isValid) {
            setErrorMessage("Subteam name is required");
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Checks if the provided email is valid.
     * 
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Loads team members for the current project.
     */
    public void loadMembers() {
        try {
            List<TeamMember> memberList;
            
            if (project.get() != null) {
                // In a real implementation, this would filter by project
                // For now, we'll just load all members
                memberList = teamMemberService.findAll();
            } else {
                memberList = teamMemberService.findAll();
            }
            
            members.clear();
            if (memberList != null) {
                members.addAll(memberList);
            }
            
            applyFilter();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team members", e);
            setErrorMessage("Failed to load team members: " + e.getMessage());
        }
    }
    
    /**
     * Loads subteams for the current project.
     */
    public void loadSubteams() {
        try {
            List<Subteam> subteamList;
            
            if (project.get() != null) {
                // In a real implementation, this would filter by project
                // For now, we'll just load all subteams
                subteamList = subteamService.findAll();
            } else {
                subteamList = subteamService.findAll();
            }
            
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
     * Sets the filter and updates the filtered list.
     * 
     * @param filter the filter to apply
     */
    public void setFilter(MemberFilter filter) {
        this.currentFilter = filter;
        updateFilterPredicate();
    }
    
    /**
     * Sets the search text and updates the filtered list.
     * 
     * @param searchText the search text
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText != null ? searchText.toLowerCase() : "";
        updateFilterPredicate();
    }
    
    /**
     * Updates the filter predicate based on the current filter and search text.
     */
    private void updateFilterPredicate() {
        filteredMembers.setPredicate(member -> {
            // Check filter first
            boolean matchesFilter = switch (currentFilter) {
                case ALL -> true;
                case LEADERS -> member.isLeader();
                case UNASSIGNED -> member.getSubteam() == null;
            };
            
            // Then check search text
            boolean matchesSearch = true;
            if (matchesFilter && searchText != null && !searchText.isEmpty()) {
                String lowerSearchText = searchText.toLowerCase();
                matchesSearch = (member.getFirstName() != null && member.getFirstName().toLowerCase().contains(lowerSearchText)) ||
                               (member.getLastName() != null && member.getLastName().toLowerCase().contains(lowerSearchText)) ||
                               (member.getEmail() != null && member.getEmail().toLowerCase().contains(lowerSearchText));
            }
            
            return matchesFilter && matchesSearch;
        });
    }
    
    /**
     * Applies the current filter to the team members list.
     * This method is called explicitly from the presenter.
     */
    public void applyFilter() {
        updateFilterPredicate();
    }
    
    /**
     * Filters team members by the specified subteam.
     * 
     * @param subteam the subteam to filter by
     */
    public void filterMembersBySubteam(Subteam subteam) {
        if (subteam == null) {
            filteredMembers.setPredicate(null);
            return;
        }
        
        filteredMembers.setPredicate(member -> 
            member.getSubteam() != null && member.getSubteam().getId().equals(subteam.getId())
        );
    }
    
    /**
     * Clears the current filter.
     */
    public void clearFilter() {
        filteredMembers.setPredicate(null);
    }
    
    /**
     * Sets up the form for creating a new team member.
     */
    public void initNewMember() {
        isNewMember.set(true);
        
        // Clear form fields
        firstName.set("");
        lastName.set("");
        email.set("");
        phone.set("");
        role.set(null);
        skills.set("");
        isLeader.set(false);
        memberSubteam.set(null);
        notes.set("");
        
        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
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
        
        isNewMember.set(false);
        
        updateMemberForm(member);
        
        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
    }
    
    /**
     * Updates the member form fields from a team member.
     * 
     * @param member the team member to get values from
     */
    private void updateMemberForm(TeamMember member) {
        firstName.set(member.getFirstName());
        lastName.set(member.getLastName());
        email.set(member.getEmail());
        phone.set(member.getPhone());
        // Convert string role to Role enum if possible
        try {
            if (member.getRole() != null && !member.getRole().isEmpty()) {
                role.set(TeamMember.Role.valueOf(member.getRole()));
            } else {
                role.set(null);
            }
        } catch (IllegalArgumentException e) {
            role.set(null);
        }
        skills.set(member.getSkills());
        isLeader.set(member.isLeader());
        memberSubteam.set(member.getSubteam());
        notes.set("");  // Notes is not directly mapped in the TeamMember entity
    }
    
    /**
     * Sets up the form for creating a new subteam.
     */
    public void initNewSubteam() {
        isNewSubteam.set(true);
        
        // Clear form fields
        subteamName.set("");
        subteamColor.set("#2196F3"); // Default blue
        subteamLead.set("");
        subteamDescription.set("");
        
        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
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
        
        isNewSubteam.set(false);
        
        updateSubteamForm(subteam);
        
        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
    }
    
    /**
     * Updates the subteam form fields from a subteam.
     * 
     * @param subteam the subteam to get values from
     */
    private void updateSubteamForm(Subteam subteam) {
        subteamName.set(subteam.getName());
        subteamColor.set(subteam.getColor());
        subteamLead.set("");  // Leader is not directly mapped in the Subteam entity
        subteamDescription.set(subteam.getDescription());
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
                    
                    clearErrorMessage();
                } else {
                    setErrorMessage("Failed to delete team member: Operation unsuccessful");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting team member", e);
            setErrorMessage("Failed to delete team member: " + e.getMessage());
        }
    }
    
    /**
     * Deletes the specified team member.
     * 
     * @param member the team member to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMember(TeamMember member) {
        if (member == null) {
            return false;
        }
        
        try {
            boolean success = teamMemberService.deleteById(member.getId());
            
            if (success) {
                // Remove from members list
                members.remove(member);
                
                // Clear selection if this was the selected member
                if (selectedMember.get() != null && selectedMember.get().getId().equals(member.getId())) {
                    selectedMember.set(null);
                }
                
                clearErrorMessage();
                return true;
            } else {
                setErrorMessage("Failed to delete team member: Operation unsuccessful");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting team member", e);
            setErrorMessage("Failed to delete team member: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves the current team member.
     * Called when the save member command is executed.
     * 
     * @return true if the member was saved successfully, false otherwise
     */
    public boolean saveMember() {
        try {
            TeamMember member;
            
            if (isNewMember.get()) {
                // Create new team member
                member = new TeamMember();
            } else {
                // Update existing team member
                member = selectedMember.get();
                if (member == null) {
                    setErrorMessage("No team member selected for update");
                    return false;
                }
            }
            
            // Update fields
            member.setFirstName(firstName.get());
            member.setLastName(lastName.get());
            member.setEmail(email.get());
            member.setPhone(phone.get());
            if (role.get() != null) {
                member.setRole(role.get().toString());
            }
            member.setSkills(skills.get());
            member.setLeader(isLeader.get());
            member.setSubteam(memberSubteam.get());
            
            // Save to database
            member = teamMemberService.save(member);
            
            if (isNewMember.get()) {
                // Add to members list
                members.add(member);
            } else {
                // Update in members list
                int index = findMemberIndex(selectedMember.get());
                if (index >= 0) {
                    members.set(index, member);
                }
            }
            
            // Update selected member
            selectedMember.set(member);
            
            // Clear dirty flag
            setDirty(false);
            clearErrorMessage();
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving team member", e);
            setErrorMessage("Failed to save team member: " + e.getMessage());
            return false;
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
                boolean success = subteamService.deleteById(subteam.getId());
                
                if (success) {
                    // Remove from subteams list
                    subteams.remove(subteam);
                    
                    // Clear selection
                    selectedSubteam.set(null);
                    
                    clearErrorMessage();
                } else {
                    setErrorMessage("Failed to delete subteam: Operation unsuccessful");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
            setErrorMessage("Failed to delete subteam: " + e.getMessage());
        }
    }
    
    /**
     * Deletes the specified subteam.
     * 
     * @param subteam the subteam to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteSubteam(Subteam subteam) {
        if (subteam == null) {
            return false;
        }
        
        try {
            boolean success = subteamService.deleteById(subteam.getId());
            
            if (success) {
                // Remove from subteams list
                subteams.remove(subteam);
                
                // Clear selection if this was the selected subteam
                if (selectedSubteam.get() != null && selectedSubteam.get().getId().equals(subteam.getId())) {
                    selectedSubteam.set(null);
                }
                
                clearErrorMessage();
                return true;
            } else {
                setErrorMessage("Failed to delete subteam: Operation unsuccessful");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
            setErrorMessage("Failed to delete subteam: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves the current subteam.
     * Called when the save subteam command is executed.
     * 
     * @return true if the subteam was saved successfully, false otherwise
     */
    public boolean saveSubteam() {
        try {
            Subteam subteam;
            
            if (isNewSubteam.get()) {
                // Create new subteam
                subteam = new Subteam();
            } else {
                // Update existing subteam
                subteam = selectedSubteam.get();
                if (subteam == null) {
                    setErrorMessage("No subteam selected for update");
                    return false;
                }
            }
            
            // Update fields
            subteam.setName(subteamName.get());
            subteam.setColor(subteamColor.get());
            subteam.setDescription(subteamDescription.get());
            
            // Save to database
            subteam = subteamService.save(subteam);
            
            if (isNewSubteam.get()) {
                // Add to subteams list
                subteams.add(subteam);
            } else {
                // Update in subteams list
                int index = findSubteamIndex(selectedSubteam.get());
                if (index >= 0) {
                    subteams.set(index, subteam);
                }
            }
            
            // Update selected subteam
            selectedSubteam.set(subteam);
            
            // Clear dirty flag
            setDirty(false);
            clearErrorMessage();
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subteam", e);
            setErrorMessage("Failed to save subteam: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Imports team members from a CSV file.
     * For the MVP, this is just a placeholder.
     * 
     * @return true if import was successful, false otherwise
     */
    public boolean importMembersFromCSV() {
        LOGGER.info("Import members from CSV action triggered");
        // This would be implemented in a future version
        return false;
    }
    
    /**
     * Exports team members to a CSV file.
     * For the MVP, this is just a placeholder.
     * 
     * @return true if export was successful, false otherwise
     */
    public boolean exportMembersToCSV() {
        LOGGER.info("Export members to CSV action triggered");
        // This would be implemented in a future version
        return false;
    }
    
    /**
     * Gets the member count for a specific subteam.
     * 
     * @param subteam the subteam
     * @return the number of members in the subteam
     */
    public int getMemberCountForSubteam(Subteam subteam) {
        if (subteam == null) {
            return 0;
        }
        
        return (int) members.stream()
            .filter(m -> m.getSubteam() != null && m.getSubteam().getId().equals(subteam.getId()))
            .count();
    }
    
    /**
     * Gets the total member count.
     * 
     * @return the total number of members
     */
    public int getTotalMemberCount() {
        return members.size();
    }
    
    /**
     * Gets the filtered member count.
     * 
     * @return the number of filtered members
     */
    public int getFilteredMemberCount() {
        return filteredMembers.size();
    }
    
    /**
     * Finds the index of a member in the members list.
     * 
     * @param member the member to find
     * @return the index, or -1 if not found
     */
    private int findMemberIndex(TeamMember member) {
        if (member == null || member.getId() == null) {
            return -1;
        }
        
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(member.getId())) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Finds the index of a subteam in the subteams list.
     * 
     * @param subteam the subteam to find
     * @return the index, or -1 if not found
     */
    private int findSubteamIndex(Subteam subteam) {
        if (subteam == null || subteam.getId() == null) {
            return -1;
        }
        
        for (int i = 0; i < subteams.size(); i++) {
            if (subteams.get(i).getId().equals(subteam.getId())) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Checks if the member is valid for saving.
     * 
     * @return true if the member is valid, false otherwise
     */
    private boolean isValidMember() {
        return (firstName.get() != null && !firstName.get().trim().isEmpty()) ||
               (lastName.get() != null && !lastName.get().trim().isEmpty());
    }
    
    /**
     * Checks if the subteam is valid for saving.
     * 
     * @return true if the subteam is valid, false otherwise
     */
    private boolean isValidSubteam() {
        return subteamName.get() != null && !subteamName.get().trim().isEmpty();
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
     * Checks if the delete member command can be executed.
     * 
     * @return true if a member is selected, false otherwise
     */
    private boolean canDeleteMember() {
        return selectedMember.get()