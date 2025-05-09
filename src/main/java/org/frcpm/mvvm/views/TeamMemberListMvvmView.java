// src/main/java/org/frcpm/mvvm/views/TeamMemberListMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.TeamMemberDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TeamMemberListMvvmViewModel;

/**
 * View for the team member list using MVVMFx.
 */
public class TeamMemberListMvvmView implements FxmlView<TeamMemberListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private TableView<TeamMember> teamMemberTableView;
    
    @FXML
    private TableColumn<TeamMember, String> nameColumn;
    
    @FXML
    private TableColumn<TeamMember, String> usernameColumn;
    
    @FXML
    private TableColumn<TeamMember, String> emailColumn;
    
    @FXML
    private TableColumn<TeamMember, String> subteamColumn;
    
    @FXML
    private Button newButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private TeamMemberListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TeamMemberListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        nameColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> member != null ? member.getFullName() : ""
            );
        });
        
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        subteamColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> member != null && member.getSubteam() != null ? 
                      member.getSubteam().getName() : ""
            );
        });
        
        // Set up team member table view
        teamMemberTableView.setItems(viewModel.getTeamMembers());
        
        // Bind selected team member
        teamMemberTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedTeamMember(newVal);
        });
        
        // Bind project label
        viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                projectLabel.setText(newVal.getName());
            } else {
                projectLabel.setText("");
            }
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(newButton, viewModel.getNewTeamMemberCommand());
        CommandAdapter.bindCommandButton(editButton, viewModel.getEditTeamMemberCommand());
        CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteTeamMemberCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshTeamMembersCommand());
        
        // Override button actions to handle dialogs
        newButton.setOnAction(e -> handleNewTeamMember());
        editButton.setOnAction(e -> handleEditTeamMember());
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
    }
    
    /**
     * Sets the project for the team member list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadTeamMembersCommand().execute();
    }
    
    /**
     * Handle new team member button click.
     */
    private void handleNewTeamMember() {
        try {
            // Create a new team member and show dialog
            TeamMember newMember = new TeamMember();
            
            // Show team member dialog
            openTeamMemberDetailDialog(newMember, true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new team member", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.teamMember.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit team member button click.
     */
    private void handleEditTeamMember() {
        TeamMember selectedMember = viewModel.getSelectedTeamMember();
        if (selectedMember == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.teamMember.select"));
            return;
        }
        
        try {
            // Show team member dialog
            openTeamMemberDetailDialog(selectedMember, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing team member", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.teamMember.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a team member detail dialog.
     * 
     * @param teamMember the team member to edit or create
     * @param isNew true if creating a new team member, false if editing
     */
    private void openTeamMemberDetailDialog(TeamMember teamMember, boolean isNew) {
        try {
            // Load the team member detail view
            ViewTuple<TeamMemberDetailMvvmView, TeamMemberDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TeamMemberDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TeamMemberDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the team member
            if (isNew) {
                viewController.initNewTeamMember(teamMember);
            } else {
                viewController.initExistingTeamMember(teamMember);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("teamMember.new.title") : 
                                        resources.getString("teamMember.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh team members after dialog closes
            viewModel.getRefreshTeamMembersCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening team member dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.teamMember.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete team member button click.
     */
    @FXML
    private void onDeleteTeamMemberAction() {
        if (viewModel.getSelectedTeamMember() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.teamMember"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("teamMember.delete.confirm") + 
            " '" + viewModel.getSelectedTeamMember().getFullName() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteTeamMemberCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshTeamMembersCommand().execute();
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
}