// src/main/java/org/frcpm/mvvm/views/TeamMemberListMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
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
 * FIXED: Uses deferred binding pattern to avoid ViewModel null access during initialize().
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
    private boolean bindingComplete = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TeamMemberListMvvmView");
        this.resources = resources;
        
        // DO NOT ACCESS viewModel HERE - it's still null!
        // Instead, set up basic UI without ViewModel binding
        setupBasicUI();
        
        // Schedule binding for later when ViewModel is injected
        Platform.runLater(this::bindControlsWhenReady);
    }
    
    /**
     * Sets up basic UI components that don't require ViewModel.
     */
    private void setupBasicUI() {
        // Initialize table columns
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> member != null ? member.getFullName() : ""
                );
            });
        }
        
        if (usernameColumn != null) {
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        }
        
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        }
        
        if (subteamColumn != null) {
            subteamColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> member != null && member.getSubteam() != null ? 
                          member.getSubteam().getName() : ""
                );
            });
        }
        
        // Hide error and loading indicators initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }
    
    /**
     * Binds controls to ViewModel when it's ready.
     * Uses deferred binding pattern to handle MVVMFx injection timing.
     */
    private void bindControlsWhenReady() {
        if (viewModel != null && !bindingComplete) {
            LOGGER.info("ViewModel is ready, binding controls");
            bindControls();
            bindingComplete = true;
        } else if (viewModel == null) {
            // ViewModel still not ready, try again later
            Platform.runLater(this::bindControlsWhenReady);
        }
    }
    
    /**
     * Binds all controls to the ViewModel.
     * This is called after ViewModel injection is complete.
     */
    private void bindControls() {
        try {
            // Bind team member table view
            if (viewModel.getTeamMembers() != null && teamMemberTableView != null) {
                teamMemberTableView.setItems(viewModel.getTeamMembers());
            }
            
            // Bind selected team member
            if (teamMemberTableView != null) {
                teamMemberTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (viewModel != null) {
                        viewModel.setSelectedTeamMember(newVal);
                    }
                });
            }
            
            // Bind project label
            if (projectLabel != null && viewModel.currentProjectProperty() != null) {
                viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        projectLabel.setText(newVal.getName());
                    } else {
                        projectLabel.setText("");
                    }
                });
            }
            
            // Bind command buttons using CommandAdapter with null safety
            if (newButton != null && viewModel.getNewTeamMemberCommand() != null) {
                CommandAdapter.bindCommandButton(newButton, viewModel.getNewTeamMemberCommand());
            }
            if (editButton != null && viewModel.getEditTeamMemberCommand() != null) {
                CommandAdapter.bindCommandButton(editButton, viewModel.getEditTeamMemberCommand());
            }
            if (deleteButton != null && viewModel.getDeleteTeamMemberCommand() != null) {
                CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteTeamMemberCommand());
            }
            if (refreshButton != null && viewModel.getRefreshTeamMembersCommand() != null) {
                CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshTeamMembersCommand());
            }
            
            // Override button actions to handle dialogs
            if (newButton != null) {
                newButton.setOnAction(e -> handleNewTeamMember());
            }
            if (editButton != null) {
                editButton.setOnAction(e -> handleEditTeamMember());
            }
            
            // Bind error message with null safety
            if (errorLabel != null && viewModel.errorMessageProperty() != null) {
                errorLabel.textProperty().bind(viewModel.errorMessageProperty());
                errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
            }
            
            // Bind loading indicator with null safety
            if (loadingIndicator != null && viewModel.loadingProperty() != null) {
                loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            }
            
            LOGGER.info("Control binding completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Error binding controls: " + e.getMessage());
            e.printStackTrace();
            // Graceful degradation - UI still works even if binding fails
        }
    }
    
    /**
     * Sets the project for the team member list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        if (viewModel != null) {
            viewModel.setCurrentProject(project);
            if (viewModel.getLoadTeamMembersCommand() != null) {
                viewModel.getLoadTeamMembersCommand().execute();
            }
        }
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
            String errorTitle = resources != null ? resources.getString("error.title") : "Error";
            String errorMessage = (resources != null ? resources.getString("error.teamMember.create.failed") : 
                                  "Failed to create team member") + ": " + e.getMessage();
            showErrorAlert(errorTitle, errorMessage);
        }
    }
    
    /**
     * Handle edit team member button click.
     */
    private void handleEditTeamMember() {
        TeamMember selectedMember = viewModel != null ? viewModel.getSelectedTeamMember() : null;
        if (selectedMember == null) {
            String errorTitle = resources != null ? resources.getString("error.title") : "Error";
            String errorMessage = resources != null ? resources.getString("error.teamMember.select") : 
                                  "Please select a team member to edit";
            showErrorAlert(errorTitle, errorMessage);
            return;
        }
        
        try {
            // Show team member dialog
            openTeamMemberDetailDialog(selectedMember, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing team member", e);
            String errorTitle = resources != null ? resources.getString("error.title") : "Error";
            String errorMessage = (resources != null ? resources.getString("error.teamMember.edit.failed") : 
                                  "Failed to edit team member") + ": " + e.getMessage();
            showErrorAlert(errorTitle, errorMessage);
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
            String title = isNew ? 
                (resources != null ? resources.getString("teamMember.new.title") : "New Team Member") :
                (resources != null ? resources.getString("teamMember.edit.title") : "Edit Team Member");
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (mainPane != null && mainPane.getScene() != null) {
                dialogStage.initOwner(mainPane.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh team members after dialog closes
            if (viewModel != null && viewModel.getRefreshTeamMembersCommand() != null) {
                viewModel.getRefreshTeamMembersCommand().execute();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening team member dialog", e);
            String errorTitle = resources != null ? resources.getString("error.title") : "Error";
            String errorMessage = (resources != null ? resources.getString("error.teamMember.dialog.failed") : 
                                  "Failed to open team member dialog") + ": " + e.getMessage();
            showErrorAlert(errorTitle, errorMessage);
        }
    }
    
    /**
     * Handle delete team member button click.
     */
    @FXML
    private void onDeleteTeamMemberAction() {
        if (viewModel == null || viewModel.getSelectedTeamMember() == null) {
            // Show alert about no selection
            String errorTitle = resources != null ? resources.getString("error.title") : "Error";
            String errorMessage = resources != null ? resources.getString("info.no.selection.teamMember") : 
                                  "No team member selected";
            showErrorAlert(errorTitle, errorMessage);
            return;
        }
        
        // Confirm deletion
        String confirmTitle = resources != null ? resources.getString("confirm.title") : "Confirm";
        String confirmMessage = (resources != null ? resources.getString("teamMember.delete.confirm") : 
                               "Delete team member") + " '" + viewModel.getSelectedTeamMember().getFullName() + "'?";
        
        if (showConfirmationAlert(confirmTitle, confirmMessage)) {
            // Execute delete command
            if (viewModel.getDeleteTeamMemberCommand() != null) {
                viewModel.getDeleteTeamMemberCommand().execute();
            }
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        if (viewModel != null && viewModel.getRefreshTeamMembersCommand() != null) {
            viewModel.getRefreshTeamMembersCommand().execute();
        }
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