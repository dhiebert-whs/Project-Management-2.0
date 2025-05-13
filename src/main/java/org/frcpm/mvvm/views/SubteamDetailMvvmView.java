// src/main/java/org/frcpm/mvvm/views/SubteamDetailMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.SubteamDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TeamMemberSelectionMvvmViewModel;
import org.frcpm.mvvm.views.TeamMemberSelectionMvvmView;

/**
 * View for the subteam detail using MVVMFx.
 */
public class SubteamDetailMvvmView implements FxmlView<SubteamDetailMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamDetailMvvmView.class.getName());
    
    @FXML private TextField nameTextField;
    @FXML private TextField colorCodeTextField;
    @FXML private TextArea specialtiesTextArea;
    @FXML private TableView<TeamMember> teamMembersTable;
    @FXML private TableColumn<TeamMember, String> memberNameColumn;
    @FXML private TableColumn<TeamMember, String> memberUsernameColumn;
    @FXML private TableColumn<TeamMember, String> memberEmailColumn;
    @FXML private Button addMemberButton;
    @FXML private Button removeMemberButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private Rectangle colorPreview;
    
    @InjectViewModel
    private SubteamDetailMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubteamDetailMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTableColumns();
        
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
        
        // Set up color preview
        setupColorPreview();
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        if (teamMembersTable == null || memberNameColumn == null || 
            memberUsernameColumn == null || memberEmailColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        try {
            // Set up the name column
            memberNameColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                return Bindings.createStringBinding(() -> member.getFullName());
            });
            
            // Set up the username column
            memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            
            // Set up the email column
            memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (nameTextField == null || colorCodeTextField == null || specialtiesTextArea == null || 
            teamMembersTable == null || saveButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text fields
            nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
            colorCodeTextField.textProperty().bindBidirectional(viewModel.colorCodeProperty());
            specialtiesTextArea.textProperty().bindBidirectional(viewModel.specialtiesProperty());
            
            // Bind table to view model collection
            teamMembersTable.setItems(viewModel.getTeamMembers());
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            CommandAdapter.bindCommandButton(addMemberButton, viewModel.getManageMembersCommand());
            CommandAdapter.bindCommandButton(removeMemberButton, viewModel.getRemoveTeamMemberCommand());
            
            // Setup selection changes
            teamMembersTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTeamMember(newVal));
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
            
            // Set up add member button to show team member selection dialog
            addMemberButton.setOnAction(event -> handleAddTeamMember());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Sets up the color preview.
     */
    private void setupColorPreview() {
        if (colorPreview == null || colorCodeTextField == null) {
            return;
        }
        
        // Update color preview when color code changes
        colorCodeTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal != null && newVal.matches("^#[0-9A-Fa-f]{6}$")) {
                    colorPreview.setFill(Color.web(newVal));
                } else {
                    colorPreview.setFill(Color.WHITE);
                }
            } catch (Exception e) {
                colorPreview.setFill(Color.WHITE);
            }
        });
        
        // Initial color
        try {
            String initialColor = colorCodeTextField.getText();
            if (initialColor != null && initialColor.matches("^#[0-9A-Fa-f]{6}$")) {
                colorPreview.setFill(Color.web(initialColor));
            } else {
                colorPreview.setFill(Color.web("#007BFF"));
            }
        } catch (Exception e) {
            colorPreview.setFill(Color.web("#007BFF"));
        }
    }
    
    /**
     * Initializes the view with a new subteam.
     */
    public void initNewSubteam() {
        try {
            viewModel.initNewSubteam();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new subteam", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize new subteam: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the view with an existing subteam.
     * 
     * @param subteam the subteam to edit
     */
    public void initExistingSubteam(Subteam subteam) {
        try {
            viewModel.initExistingSubteam(subteam);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing subteam", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize subteam: " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a team member to the subteam.
     * Shows a team member selection dialog.
     */
    private void handleAddTeamMember() {
        try {
            // Load the team member selection view
            ViewTuple<TeamMemberSelectionMvvmView, TeamMemberSelectionMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TeamMemberSelectionMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TeamMemberSelectionMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the current project
            viewController.initWithProject(viewModel.getCurrentProject());
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("teamMember.selection.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(saveButton.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Check if a team member was selected
            if (viewController.wasTeamMemberSelected()) {
                TeamMember selectedMember = viewController.getSelectedTeamMember();
                if (selectedMember != null) {
                    // Add the team member to the subteam
                    boolean success = viewModel.addTeamMember(selectedMember);
                    if (!success) {
                        // Show error if unsuccessful
                        showErrorAlert(resources.getString("error.title"), 
                                    viewModel.getErrorMessage());
                        viewModel.clearErrorMessage();
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing team member selection dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                        "Failed to show team member selection dialog: " + e.getMessage());
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
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    public SubteamDetailMvvmViewModel getViewModel() {
        return viewModel;
    }
}