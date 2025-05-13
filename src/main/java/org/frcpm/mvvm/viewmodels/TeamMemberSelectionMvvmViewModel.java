// src/main/java/org/frcpm/mvvm/viewmodels/TeamMemberSelectionMvvmViewModel.java
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
import javafx.collections.transformation.FilteredList;

import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * ViewModel for the team member selection dialog using MVVMFx.
 */
public class TeamMemberSelectionMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberSelectionMvvmViewModel.class.getName());
    
    // Service dependencies
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    // Observable properties
    private final ObjectProperty<TeamMember> selectedTeamMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty searchFilter = new SimpleStringProperty("");
    private final BooleanProperty teamMemberSelected = new SimpleBooleanProperty(false);
    
    // Observable collections
    private final ObservableList<TeamMember> allTeamMembers = FXCollections.observableArrayList();
    private final FilteredList<TeamMember> filteredTeamMembers = new FilteredList<>(allTeamMembers);
    
    // Commands
    private Command loadTeamMembersCommand;
    private Command selectTeamMemberCommand;
    private Command cancelCommand;
    
    /**
     * Creates a new TeamMemberSelectionMvvmViewModel.
     * 
     * @param teamMemberService the team member service
     */
    @Inject
    public TeamMemberSelectionMvvmViewModel(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        
        initializeCommands();
        setupFilterPredicate();
        
        // Add search filter listener
        searchFilter.addListener((obs, oldVal, newVal) -> updateFilterPredicate());
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load team members command
        loadTeamMembersCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTeamMembersAsync);
        
        // Select team member command
        selectTeamMemberCommand = createValidOnlyCommand(
            () -> {
                teamMemberSelected.set(true);
                LOGGER.info("Team member selected: " + 
                    (selectedTeamMember.get() != null ? selectedTeamMember.get().getFullName() : "null"));
            },
            () -> selectedTeamMember.get() != null
        );
        
        // Cancel command
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            teamMemberSelected.set(false);
            LOGGER.info("Team member selection canceled");
        });
    }
    
    /**
     * Sets up the filter predicate for filtering team members.
     */
    private void setupFilterPredicate() {
        filteredTeamMembers.setPredicate(buildFilterPredicate());
    }
    
    /**
     * Updates the filter predicate when the search filter changes.
     */
    private void updateFilterPredicate() {
        filteredTeamMembers.setPredicate(buildFilterPredicate());
    }
    
    /**
     * Builds a predicate for filtering team members based on the search filter.
     * 
     * @return the predicate
     */
    private Predicate<TeamMember> buildFilterPredicate() {
        String filter = searchFilter.get();
        if (filter == null || filter.trim().isEmpty()) {
            return member -> true;
        }
        
        String lowerCaseFilter = filter.toLowerCase();
        
        return member -> {
            // Match by name, username, or email
            return (member.getFirstName() != null && member.getFirstName().toLowerCase().contains(lowerCaseFilter)) ||
                   (member.getLastName() != null && member.getLastName().toLowerCase().contains(lowerCaseFilter)) ||
                   (member.getUsername() != null && member.getUsername().toLowerCase().contains(lowerCaseFilter)) ||
                   (member.getEmail() != null && member.getEmail().toLowerCase().contains(lowerCaseFilter));
        };
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
     * Loads team members asynchronously.
     */
    private void loadTeamMembersAsync() {
        loading.set(true);
        
        // Find all team members asynchronously
        teamMemberServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allTeamMembers.clear();
                    allTeamMembers.addAll(result);
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
     * Gets the team members list.
     * 
     * @return the team members list
     */
    public ObservableList<TeamMember> getTeamMembers() {
        return filteredTeamMembers;
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
     * Gets the search filter property.
     * 
     * @return the search filter property
     */
    public StringProperty searchFilterProperty() {
        return searchFilter;
    }
    
    /**
     * Gets the search filter.
     * 
     * @return the search filter
     */
    public String getSearchFilter() {
        return searchFilter.get();
    }
    
    /**
     * Sets the search filter.
     * 
     * @param filter the search filter
     */
    public void setSearchFilter(String filter) {
        searchFilter.set(filter);
    }
    
    /**
     * Gets the team member selected property.
     * 
     * @return the team member selected property
     */
    public BooleanProperty teamMemberSelectedProperty() {
        return teamMemberSelected;
    }
    
    /**
     * Gets whether a team member has been selected.
     * 
     * @return true if a team member has been selected, false otherwise
     */
    public boolean isTeamMemberSelected() {
        return teamMemberSelected.get();
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
     * Gets the select team member command.
     * 
     * @return the select team member command
     */
    public Command getSelectTeamMemberCommand() {
        return selectTeamMemberCommand;
    }
    
    /**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        allTeamMembers.clear();
    }
}