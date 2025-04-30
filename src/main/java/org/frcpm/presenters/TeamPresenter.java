// src/main/java/org/frcpm/presenters/TeamPresenter.java
package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.TeamViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for team management using AfterburnerFX pattern.
 */
public class TeamPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TeamPresenter.class.getName());

    // FXML UI components - TabPane
    @FXML
    private TabPane tabPane;

    // FXML UI components - Team Members
    @FXML
    private TableView<TeamMember> membersTable;

    @FXML
    private TableColumn<TeamMember, String> memberUsernameColumn;

    @FXML
    private TableColumn<TeamMember, String> memberNameColumn;

    @FXML
    private TableColumn<TeamMember, String> memberEmailColumn;

    @FXML
    private TableColumn<TeamMember, Subteam> memberSubteamColumn;

    @FXML
    private TableColumn<TeamMember, Boolean> memberLeaderColumn;

    @FXML
    private Button addMemberButton;

    @FXML
    private Button editMemberButton;

    @FXML
    private Button deleteMemberButton;

    // FXML UI components - Subteams
    @FXML
    private TableView<Subteam> subteamsTable;

    @FXML
    private TableColumn<Subteam, String> subteamNameColumn;

    @FXML
    private TableColumn<Subteam, String> subteamColorColumn;

    @FXML
    private TableColumn<Subteam, String> subteamSpecialtiesColumn;

    @FXML
    private Button addSubteamButton;

    @FXML
    private Button editSubteamButton;

    @FXML
    private Button deleteSubteamButton;

    // Injected services
    @Inject
    private TeamMemberService teamMemberService;
    
    @Inject
    private SubteamService subteamService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private TeamViewModel viewModel;
    private ResourceBundle resources;
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TeamPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected services
        viewModel = new TeamViewModel(teamMemberService, subteamService);

        // Setup table columns
        setupMembersTable();
        setupSubteamsTable();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
    }

    /**
     * Sets up the team members table columns.
     */
    private void setupMembersTable() {
        // Check for null UI components for testability
        if (membersTable == null || memberUsernameColumn == null || memberNameColumn == null || 
            memberEmailColumn == null || memberSubteamColumn == null || memberLeaderColumn == null) {
            LOGGER.warning("Members table components not initialized - likely in test environment");
            return;
        }

        // Setup columns with appropriate property factory
        memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        // Custom cell factory for name (combines first and last name)
        memberNameColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            String fullName = (member.getFirstName() != null ? member.getFirstName() : "") + " " +
                             (member.getLastName() != null ? member.getLastName() : "");
            return new javafx.beans.property.SimpleStringProperty(fullName.trim());
        });
        
        memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Custom cell factory for subteam
        memberSubteamColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            Subteam subteam = member.getSubteam();
            return new javafx.beans.property.SimpleObjectProperty<>(subteam);
        });
        memberSubteamColumn.setCellFactory(column -> new TableCell<TeamMember, Subteam>() {
            @Override
            protected void updateItem(Subteam subteam, boolean empty) {
                super.updateItem(subteam, empty);
                if (empty || subteam == null) {
                    setText(null);
                } else {
                    setText(subteam.getName());
                }
            }
        });
        
        // Custom cell factory for leader status
        memberLeaderColumn.setCellValueFactory(new PropertyValueFactory<>("leader"));
        memberLeaderColumn.setCellFactory(column -> new TableCell<TeamMember, Boolean>() {
            @Override
            protected void updateItem(Boolean isLeader, boolean empty) {
                super.updateItem(isLeader, empty);
                if (empty || isLeader == null) {
                    setText(null);
                } else {
                    setText(isLeader ? "Yes" : "No");
                }
            }
        });
        
        // Setup row selection listener
        membersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedMember(newVal));
    }
    
    /**
     * Sets up the subteams table columns.
     */
    private void setupSubteamsTable() {
        // Check for null UI components for testability
        if (subteamsTable == null || subteamNameColumn == null || 
            subteamColorColumn == null || subteamSpecialtiesColumn == null) {
            LOGGER.warning("Subteams table components not initialized - likely in test environment");
            return;
        }

        // Setup columns with appropriate property factory
        subteamNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subteamColorColumn.setCellValueFactory(new PropertyValueFactory<>("colorCode"));
        subteamSpecialtiesColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Setup row selection listener
        subteamsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedSubteam(newVal));
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (membersTable == null || subteamsTable == null || 
            addMemberButton == null || editMemberButton == null || 
            deleteMemberButton == null || addSubteamButton == null || 
            editSubteamButton == null || deleteSubteamButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind lists to view model collections
            membersTable.setItems(viewModel.getMembers());
            subteamsTable.setItems(viewModel.getSubteams());
            
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
            ViewModelBinding.bindCommandButton(editMemberButton, viewModel.getEditMemberCommand());
            ViewModelBinding.bindCommandButton(deleteMemberButton, viewModel.getDeleteMemberCommand());
            ViewModelBinding.bindCommandButton(addSubteamButton, viewModel.getAddSubteamCommand());
            ViewModelBinding.bindCommandButton(editSubteamButton, viewModel.getEditSubteamCommand());
            ViewModelBinding.bindCommandButton(deleteSubteamButton, viewModel.getDeleteSubteamCommand());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        // Show an alert when error message changes
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Sets the current project and loads team data.
     * 
     * @param project the current project
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        this.currentProject = project;
        viewModel.setProject(project);
    }
    
    /**
     * Handles adding a new team member.
     * This method is called when the add member button is clicked.
     */
    @FXML
    private void handleAddMember() {
        // Show member dialog
        TeamMember newMember = showMemberDialog(null);
        if (newMember != null) {
            viewModel.saveMember(newMember);
        }
    }
    
    /**
     * Handles editing a team member.
     * This method is called when the edit member button is clicked.
     */
    @FXML
    private void handleEditMember() {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            TeamMember editedMember = showMemberDialog(selectedMember);
            if (editedMember != null) {
                viewModel.saveMember(editedMember);
            }
        } else {
            showInfoAlert("No Selection", "Please select a team member to edit");
        }
    }
    
    /**
     * Handles deleting a team member.
     * This method is called when the delete member button is clicked.
     */
    @FXML
    private void handleDeleteMember() {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showInfoAlert("No Selection", "Please select a team member to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Member", 
                "Are you sure you want to delete the team member '" + 
                selectedMember.getFirstName() + " " + selectedMember.getLastName() + "'?");
        
        if (confirmed) {
            // The viewModel's command handling will take care of the actual deletion
            viewModel.setSelectedMember(selectedMember);
            viewModel.getDeleteMemberCommand().execute();
        }
    }
    
    /**
     * Handles adding a new subteam.
     * This method is called when the add subteam button is clicked.
     */
    @FXML
    private void handleAddSubteam() {
        // Show subteam dialog
        Subteam newSubteam = showSubteamDialog(null);
        if (newSubteam != null) {
            viewModel.saveSubteam(newSubteam);
        }
    }
    
    /**
     * Handles editing a subteam.
     * This method is called when the edit subteam button is clicked.
     */
    @FXML
    private void handleEditSubteam() {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam != null) {
            Subteam editedSubteam = showSubteamDialog(selectedSubteam);
            if (editedSubteam != null) {
                viewModel.saveSubteam(editedSubteam);
            }
        } else {
            showInfoAlert("No Selection", "Please select a subteam to edit");
        }
    }
    
    /**
     * Handles deleting a subteam.
     * This method is called when the delete subteam button is clicked.
     */
    @FXML
    private void handleDeleteSubteam() {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showInfoAlert("No Selection", "Please select a subteam to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Subteam", 
                "Are you sure you want to delete the subteam '" + 
                selectedSubteam.getName() + "'?");
        
        if (confirmed) {
            // The viewModel's command handling will take care of the actual deletion
            viewModel.setSelectedSubteam(selectedSubteam);
            viewModel.getDeleteSubteamCommand().execute();
        }
    }
    
    /**
     * Shows a dialog for adding or editing a team member.
     * 
     * @param member the team member to edit, or null for a new member
     * @return the created or edited team member, or null if cancelled
     */
    private TeamMember showMemberDialog(TeamMember member) {
        // In a real implementation, this would show a dialog
        // For now, we'll just return the member that was passed in
        return member;
    }
    
    /**
     * Shows a dialog for adding or editing a subteam.
     * 
     * @param subteam the subteam to edit, or null for a new subteam
     * @return the created or edited subteam, or null if cancelled
     */
    private Subteam showSubteamDialog(Subteam subteam) {
        // In a real implementation, this would show a dialog
        // For now, we'll just return the subteam that was passed in
        return subteam;
    }
    
    /**
     * Handles closing the view.
     * This method is called when the close button is clicked.
     */
    @FXML
    private void handleClose() {
        try {
            // Cleanup resources
            cleanup();
            
            // Get the stage from any control
            if (tabPane != null && tabPane.getScene() != null && 
                tabPane.getScene().getWindow() != null) {
                
                Stage stage = (Stage) tabPane.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing window", e);
        }
    }
    
    /**
     * Cleans up resources used by this presenter.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        try {
            return dialogService.showConfirmationAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation would show: {0} - {1}", new Object[] { title, message });
            return false;
        }
    }
    
    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public TeamViewModel getViewModel() {
        return viewModel;
    }
}