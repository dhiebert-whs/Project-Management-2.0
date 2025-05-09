// src/main/java/org/frcpm/mvvm/views/TeamMemberDetailMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.TeamMemberDetailMvvmViewModel;

/**
 * View for the team member detail using MVVMFx.
 */
public class TeamMemberDetailMvvmView implements FxmlView<TeamMemberDetailMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberDetailMvvmView.class.getName());
    
    @FXML private TextField usernameTextField;
    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField skillsTextField;
    @FXML private ComboBox<org.frcpm.models.Subteam> subteamComboBox;
    @FXML private CheckBox leaderCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private TeamMemberDetailMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TeamMemberDetailMvvmView");
        this.resources = resources;
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        }
        
        // Set up error label
        if (errorLabel != null) {
            errorLabel.textProperty().bind(viewModel.errorMessageProperty());
            errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        }
        
        // Set up subteam combo box cell factory
        subteamComboBox.setCellFactory(lv -> new ListCell<org.frcpm.models.Subteam>() {
            @Override
            protected void updateItem(org.frcpm.models.Subteam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        // Set up subteam combo box button cell
        subteamComboBox.setButtonCell(new ListCell<org.frcpm.models.Subteam>() {
            @Override
            protected void updateItem(org.frcpm.models.Subteam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        // Set items for subteam combo box
        subteamComboBox.setItems(viewModel.getSubteams());
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (usernameTextField == null || firstNameTextField == null || lastNameTextField == null || 
            emailTextField == null || phoneTextField == null || skillsTextField == null || 
            subteamComboBox == null || leaderCheckBox == null || 
            saveButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text fields
            usernameTextField.textProperty().bindBidirectional(viewModel.usernameProperty());
            firstNameTextField.textProperty().bindBidirectional(viewModel.firstNameProperty());
            lastNameTextField.textProperty().bindBidirectional(viewModel.lastNameProperty());
            emailTextField.textProperty().bindBidirectional(viewModel.emailProperty());
            phoneTextField.textProperty().bindBidirectional(viewModel.phoneProperty());
            skillsTextField.textProperty().bindBidirectional(viewModel.skillsProperty());
            
            // Bind combo box
            subteamComboBox.valueProperty().bindBidirectional(viewModel.selectedSubteamProperty());
            
            // Bind check box
            leaderCheckBox.selectedProperty().bindBidirectional(viewModel.isLeaderProperty());
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Initializes the view with a new team member.
     * 
     * @param teamMember the team member to create
     */
    public void initNewTeamMember(TeamMember teamMember) {
        try {
            viewModel.initNewTeamMember(teamMember);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new team member", e);
            // Show error alert using AlertHelper or another utility
        }
    }
    
    /**
     * Initializes the view with an existing team member.
     * 
     * @param teamMember the team member to edit
     */
    public void initExistingTeamMember(TeamMember teamMember) {
        try {
            viewModel.initExistingTeamMember(teamMember);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing team member", e);
            // Show error alert using AlertHelper or another utility
        }
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public TeamMemberDetailMvvmViewModel getViewModel() {
        return viewModel;
    }
}