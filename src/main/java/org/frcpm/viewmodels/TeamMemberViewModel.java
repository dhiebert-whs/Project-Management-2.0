package org.frcpm.viewmodels;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;

public class TeamMemberViewModel extends BaseViewModel {

    // Services
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;

    // Model reference
    private TeamMember teamMember;
    private Project currentProject;

    // Observable properties
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty skills = new SimpleStringProperty("");
    private final BooleanProperty isLeader = new SimpleBooleanProperty(false);
    private final BooleanProperty isNewTeamMember = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    // Collections
    private final ObservableList<TeamMember> teamMembers = FXCollections.observableArrayList();
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();

    // Commands
    private final Command saveCommand;
    private final Command deleteCommand;
    private final Command newCommand;

    // Default constructor using ServiceProvider
    public TeamMemberViewModel() {
        this(
                ServiceProvider.getService(TeamMemberService.class),
                ServiceProvider.getService(SubteamService.class));
    }

    // Constructor with explicit service injection for testing
    public TeamMemberViewModel(TeamMemberService teamMemberService,
            SubteamService subteamService) {
        this.teamMemberService = teamMemberService;
        this.subteamService = subteamService;

        // Initialize commands using enhanced BaseViewModel methods
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        deleteCommand = createValidOnlyCommand(this::delete, this::canDelete);
        newCommand = new Command(this::initNewTeamMember);

        // Set up property listeners
        setupPropertyListeners();

        // Initial validation
        validate();
    }

    // Initialize for project context
    public void initProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        this.currentProject = project;
        loadTeamMembers();
        loadSubteams();
        initNewTeamMember();
    }

    // Initialize for new team member
    public void initNewTeamMember() {
        teamMember = new TeamMember();
        // No direct Project field in TeamMember
        isNewTeamMember.set(true);
        clearProperties();
        selectedSubteam.set(null);
        setDirty(false);
        validate();
    }

    // Initialize for existing team member
    public void initExistingTeamMember(TeamMember teamMember) {
        if (teamMember == null) {
            throw new IllegalArgumentException("TeamMember cannot be null");
        }

        this.teamMember = teamMember;
        isNewTeamMember.set(false);
        updatePropertiesFromEntity();
        if (teamMember.getSubteam() != null) {
            selectedSubteam.set(teamMember.getSubteam());
        } else {
            selectedSubteam.set(null);
        }
        setDirty(false);
        validate();
    }

    // Property listeners
    private void setupPropertyListeners() {
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);

        firstName.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        lastName.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        username.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        email.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        phone.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        skills.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        isLeader.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        selectedSubteam.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());

        // Track listeners for cleanup
        trackPropertyListener(validateAndMarkDirty);
    }

    // Validation
    private void validate() {
        boolean isValid = username.get() != null && !username.get().trim().isEmpty() &&
                (email.get() == null || email.get().trim().isEmpty() || isValidEmail(email.get()));

        if (!isValid) {
            if (username.get() == null || username.get().trim().isEmpty()) {
                setErrorMessage("Username is required");
            } else if (email.get() != null && !email.get().trim().isEmpty() && !isValidEmail(email.get())) {
                setErrorMessage("Email format is invalid");
            }
        } else {
            clearErrorMessage();
        }

        valid.set(isValid);
    }

    // Simple email validation
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Command implementations
    private void save() {
        if (!valid.get()) {
            return;
        }

        try {
            updateEntityFromProperties();
            teamMember = teamMemberService.save(teamMember);

            // If this is a new team member, add it to the list
            if (isNewTeamMember.get()) {
                teamMembers.add(teamMember);
                initNewTeamMember(); // Reset for next entry
            } else {
                // Refresh the list to show updated data
                int index = findTeamMemberIndex(teamMember);
                if (index >= 0) {
                    teamMembers.set(index, teamMember);
                }
            }

            setDirty(false);
        } catch (Exception e) {
            setErrorMessage("Error saving team member: " + e.getMessage());
        }
    }

    private void delete() {
        if (teamMember == null || teamMember.getId() == null) {
            return;
        }

        try {
            teamMemberService.delete(teamMember);
            teamMembers.remove(teamMember);
            initNewTeamMember();
        } catch (Exception e) {
            setErrorMessage("Error deleting team member: " + e.getMessage());
        }
    }

    private boolean canDelete() {
        return teamMember != null && teamMember.getId() != null;
    }

    private boolean isValid() {
        return valid.get();
    }

    // Helper methods
    private void clearProperties() {
        firstName.set("");
        lastName.set("");
        username.set("");
        email.set("");
        phone.set("");
        skills.set("");
        isLeader.set(false);
    }

    private void updatePropertiesFromEntity() {
        firstName.set(teamMember.getFirstName());
        lastName.set(teamMember.getLastName());
        username.set(teamMember.getUsername());
        email.set(teamMember.getEmail());
        phone.set(teamMember.getPhone());
        skills.set(teamMember.getSkills());
        isLeader.set(teamMember.isLeader());
    }

    private void updateEntityFromProperties() {
        teamMember.setFirstName(firstName.get());
        teamMember.setLastName(lastName.get());
        teamMember.setUsername(username.get());
        teamMember.setEmail(email.get());
        teamMember.setPhone(phone.get());
        teamMember.setSkills(skills.get());
        teamMember.setLeader(isLeader.get());
        teamMember.setSubteam(selectedSubteam.get());
        // No direct Project field in TeamMember
    }

    private int findTeamMemberIndex(TeamMember member) {
        for (int i = 0; i < teamMembers.size(); i++) {
            if (teamMembers.get(i).getId().equals(member.getId())) {
                return i;
            }
        }
        return -1;
    }

    private void loadTeamMembers() {
        try {
            // Since we can't directly query by project, load all and keep in memory for now
            // In a real implementation, you might want to improve this with a custom query
            List<TeamMember> allMembers = teamMemberService.findAll();

            // For now, loading all team members
            // In a real implementation, this would filter by project association
            teamMembers.clear();
            teamMembers.addAll(allMembers);
        } catch (Exception e) {
            setErrorMessage("Error loading team members: " + e.getMessage());
        }
    }

    private void loadSubteams() {
        try {
            // Load all subteams
            // In a real implementation, this would filter by project association
            List<Subteam> allSubteams = subteamService.findAll();

            subteams.clear();
            subteams.addAll(allSubteams);
        } catch (Exception e) {
            setErrorMessage("Error loading subteams: " + e.getMessage());
        }
    }

    // Filtering methods
    public List<TeamMember> getTeamMembersBySubteam(Subteam subteam) {
        if (subteam == null) {
            return teamMembers;
        }

        return teamMembers.stream()
                .filter(member -> member.getSubteam() != null &&
                        member.getSubteam().getId().equals(subteam.getId()))
                .collect(Collectors.toList());
    }

    // Property getters
    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public StringProperty skillsProperty() {
        return skills;
    }

    public BooleanProperty isLeaderProperty() {
        return isLeader;
    }

    public BooleanProperty isNewTeamMemberProperty() {
        return isNewTeamMember;
    }

    public BooleanProperty validProperty() {
        return valid;
    }

    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }

    // Computed property for displaying full name
    public String getFullName() {
        return teamMember != null ? teamMember.getFullName() : "";
    }

    // Collection getters
    public ObservableList<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public ObservableList<Subteam> getSubteams() {
        return subteams;
    }

    // Command getters
    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getDeleteCommand() {
        return deleteCommand;
    }

    public Command getNewCommand() {
        return newCommand;
    }

    // Entity getters
    public TeamMember getTeamMember() {
        return teamMember;
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    // State getters
    public boolean isNewTeamMember() {
        return isNewTeamMember.get();
    }

    // Make clearErrorMessage public - required by project pattern
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }

    // Resource cleanup
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        teamMembers.clear();
        subteams.clear();
    }
}