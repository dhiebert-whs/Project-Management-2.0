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

/**
 * ViewModel for team management in the FRC Project Management System.
 * Provides functionality for managing team members and subteams.
 */
public class TeamViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamViewModel.class.getName());
    
    // Services
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;
    private final ProjectService projectService;
    
    // Project
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    
    // Collections
    private final ObservableList<TeamMember> members = FXCollections.observableArrayList();
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    
    // Selected items
    private final ObjectProperty<TeamMember> selectedMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    
    // Commands for members
    private final Command addMemberCommand;
    private final Command editMemberCommand;
    private final Command deleteMemberCommand;
    
    // Commands for subteams
    private final Command addSubteamCommand;
    private final Command editSubteamCommand;
    private final Command deleteSubteamCommand;
    
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
     * Creates a new TeamViewModel with the specified services.
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
        
        // Initialize commands using the BaseViewModel helper methods
        addMemberCommand = new Command(this::handleAddMember);
        editMemberCommand = createValidOnlyCommand(this::handleEditMember, this::canEditMember);
        deleteMemberCommand = createValidOnlyCommand(this::handleDeleteMember, this::canDeleteMember);
        
        addSubteamCommand = new Command(this::handleAddSubteam);
        editSubteamCommand = createValidOnlyCommand(this::handleEditSubteam, this::canEditSubteam);
        deleteSubteamCommand = createValidOnlyCommand(this::handleDeleteSubteam, this::canDeleteSubteam);
        
        // Set up project property listener
        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadData();
            } else {
                members.clear();
                subteams.clear();
            }
        });
    }
    
    /**
     * Loads all data for the current project.
     */
    private void loadData() {
        loadMembers();
        loadSubteams();
    }
    
    /**
     * Loads team members for the current project.
     */
    public void loadMembers() {
        try {
            members.clear();
            
            if (project.get() != null) {
                List<TeamMember> memberList = teamMemberService.findAll();
                if (memberList != null) {
                    members.addAll(memberList);
                }
            }
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
            subteams.clear();
            
            if (project.get() != null) {
                List<Subteam> subteamList = subteamService.findAll();
                if (subteamList != null) {
                    subteams.addAll(subteamList);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteams", e);
            setErrorMessage("Failed to load subteams: " + e.getMessage());
        }
    }
    
    // Handler methods for commands
    
    private void handleAddMember() {
        LOGGER.info("Add member command executed");
        // This will be handled by the presenter to show a dialog
    }
    
    private void handleEditMember() {
        LOGGER.info("Edit member command executed");
        // This will be handled by the presenter to show a dialog
    }
    
    private void handleDeleteMember() {
        if (selectedMember.get() == null) {
            return;
        }
        
        try {
            boolean success = teamMemberService.deleteById(selectedMember.get().getId());
            if (success) {
                members.remove(selectedMember.get());
                selectedMember.set(null);
            } else {
                setErrorMessage("Failed to delete team member");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting team member", e);
            setErrorMessage("Error deleting team member: " + e.getMessage());
        }
    }
    
    private void handleAddSubteam() {
        LOGGER.info("Add subteam command executed");
        // This will be handled by the presenter to show a dialog
    }
    
    private void handleEditSubteam() {
        LOGGER.info("Edit subteam command executed");
        // This will be handled by the presenter to show a dialog
    }
    
    private void handleDeleteSubteam() {
        if (selectedSubteam.get() == null) {
            return;
        }
        
        try {
            boolean success = subteamService.deleteById(selectedSubteam.get().getId());
            if (success) {
                subteams.remove(selectedSubteam.get());
                selectedSubteam.set(null);
            } else {
                setErrorMessage("Failed to delete subteam");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
            setErrorMessage("Error deleting subteam: " + e.getMessage());
        }
    }
    
    // Helper methods for command conditions
    
    private boolean canEditMember() {
        return selectedMember.get() != null;
    }
    
    private boolean canDeleteMember() {
        return selectedMember.get() != null;
    }
    
    private boolean canEditSubteam() {
        return selectedSubteam.get() != null;
    }
    
    private boolean canDeleteSubteam() {
        return selectedSubteam.get() != null;
    }
    
    /**
     * Saves a team member.
     * 
     * @param member the team member to save
     * @return the saved team member
     */
    public TeamMember saveMember(TeamMember member) {
        try {
            member = teamMemberService.save(member);
            
            // Update or add to the list
            int index = findMemberIndex(member);
            if (index >= 0) {
                members.set(index, member);
            } else {
                members.add(member);
            }
            
            return member;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving team member", e);
            setErrorMessage("Error saving team member: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Saves a subteam.
     * 
     * @param subteam the subteam to save
     * @return the saved subteam
     */
    public Subteam saveSubteam(Subteam subteam) {
        try {
            subteam = subteamService.save(subteam);
            
            // Update or add to the list
            int index = findSubteamIndex(subteam);
            if (index >= 0) {
                subteams.set(index, subteam);
            } else {
                subteams.add(subteam);
            }
            
            return subteam;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subteam", e);
            setErrorMessage("Error saving subteam: " + e.getMessage());
            return null;
        }
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
    
    // Property getters for binding
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    public ObjectProperty<TeamMember> selectedMemberProperty() {
        return selectedMember;
    }
    
    public TeamMember getSelectedMember() {
        return selectedMember.get();
    }
    
    public void setSelectedMember(TeamMember member) {
        selectedMember.set(member);
    }
    
    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }
    
    public Subteam getSelectedSubteam() {
        return selectedSubteam.get();
    }
    
    public void setSelectedSubteam(Subteam subteam) {
        selectedSubteam.set(subteam);
    }
    
    // Collection getters
    
    public ObservableList<TeamMember> getMembers() {
        return members;
    }
    
    public ObservableList<Subteam> getSubteams() {
        return subteams;
    }
    
    // Command getters
    
    public Command getAddMemberCommand() {
        return addMemberCommand;
    }
    
    public Command getEditMemberCommand() {
        return editMemberCommand;
    }
    
    public Command getDeleteMemberCommand() {
        return deleteMemberCommand;
    }
    
    public Command getAddSubteamCommand() {
        return addSubteamCommand;
    }
    
    public Command getEditSubteamCommand() {
        return editSubteamCommand;
    }
    
    public Command getDeleteSubteamCommand() {
        return deleteSubteamCommand;
    }
    
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Clean up any additional resources or listeners
    }
}