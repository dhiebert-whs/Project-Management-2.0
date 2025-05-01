// src/main/java/org/frcpm/controllers/TeamController.java
package org.frcpm.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.TeamViewModel;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for team management functionality.
 * Standardized to use MVVM architecture.
 */
public class TeamController {

    private static final Logger LOGGER = Logger.getLogger(TeamController.class.getName());

    // FXML UI components
    @FXML
    private TabPane tabPane;

    // Members tab controls
    @FXML
    private TableView<TeamMember> membersTable;

    @FXML
    private TableColumn<TeamMember, String> memberUsernameColumn;

    @FXML
    private TableColumn<TeamMember, String> memberNameColumn;

    @FXML
    private TableColumn<TeamMember, String> memberEmailColumn;

    @FXML
    private TableColumn<TeamMember, String> memberSubteamColumn;

    @FXML
    private TableColumn<TeamMember, Boolean> memberLeaderColumn;

    @FXML
    private Button addMemberButton;

    @FXML
    private Button editMemberButton;

    @FXML
    private Button deleteMemberButton;

    // Subteams tab controls
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

    // ViewModel for data and business logic
    private TeamViewModel viewModel = new TeamViewModel();

    // Dialog service for UI interactions
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    // Project reference
    private Project currentProject;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TeamController");

        // Check UI components for testability
        if (membersTable == null || subteamsTable == null || tabPane == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Initialize tables
            setupMembersTable();
            setupSubteamsTable();
    
            // Set up bindings
            setupBindings();
    
            // Set up row selection handlers
            setupSelectionHandlers();
    
            // Set up row double-click handlers
            setupRowFactories();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing TeamController", e);
            showErrorAlert("Initialization Error", "Failed to initialize controller: " + e.getMessage());
        }
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        try {
            // Bind tables to the ViewModel's ObservableLists
            membersTable.setItems(viewModel.getMembers());
            subteamsTable.setItems(viewModel.getSubteams());
    
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
            ViewModelBinding.bindCommandButton(editMemberButton, viewModel.getEditMemberCommand());
            ViewModelBinding.bindCommandButton(deleteMemberButton, viewModel.getDeleteMemberCommand());
    
            ViewModelBinding.bindCommandButton(addSubteamButton, viewModel.getAddSubteamCommand());
            ViewModelBinding.bindCommandButton(editSubteamButton, viewModel.getEditSubteamCommand());
            ViewModelBinding.bindCommandButton(deleteSubteamButton, viewModel.getDeleteSubteamCommand());
    
            // Bind error messages
            viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    showErrorAlert("Error", newValue);
                    viewModel.clearErrorMessage();
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to set up bindings: " + e.getMessage());
        }
    }

    /**
     * Sets up selection handlers for tables.
     */
    private void setupSelectionHandlers() {
        try {
            // Set up row selection handlers
            membersTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> viewModel.setSelectedMember(newValue));
    
            subteamsTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> viewModel.setSelectedSubteam(newValue));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up selection handlers", e);
        }
    }

    /**
     * Sets up row factories for double-click handling.
     */
    private void setupRowFactories() {
        try {
            // Set up row double-click handlers
            membersTable.setRowFactory(tv -> {
                TableRow<TeamMember> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleEditMember();
                    }
                });
                return row;
            });
    
            subteamsTable.setRowFactory(tv -> {
                TableRow<Subteam> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleEditSubteam();
                    }
                });
                return row;
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up row factories", e);
        }
    }

    /**
     * Sets up the members table columns and cell factories.
     */
    private void setupMembersTable() {
        try {
            memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            
            // Custom cell factory for full name
            memberNameColumn.setCellValueFactory(
                    cellData -> {
                        TeamMember member = cellData.getValue();
                        String firstName = member.getFirstName() != null ? member.getFirstName() : "";
                        String lastName = member.getLastName() != null ? member.getLastName() : "";
                        return new javafx.beans.property.SimpleStringProperty(
                                (firstName + " " + lastName).trim());
                    });
                    
            memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            
            // Custom cell factory for subteam
            memberSubteamColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                Subteam subteam = member.getSubteam();
                return new javafx.beans.property.SimpleStringProperty(
                        subteam != null ? subteam.getName() : "");
            });
            
            // Create a cell factory for the leader column (checkbox)
            memberLeaderColumn.setCellValueFactory(new PropertyValueFactory<>("leader"));
            memberLeaderColumn.setCellFactory(column -> new TableCell<TeamMember, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        CheckBox checkBox = new CheckBox();
                        checkBox.setSelected(item);
                        checkBox.setDisable(true); // Read-only in the table
                        setGraphic(checkBox);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up members table", e);
            showErrorAlert("Setup Error", "Failed to set up members table: " + e.getMessage());
        }
    }

    /**
     * Sets up the subteams table columns and cell factories.
     */
    private void setupSubteamsTable() {
        try {
            subteamNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            subteamColorColumn.setCellValueFactory(new PropertyValueFactory<>("colorCode"));
            subteamSpecialtiesColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    
            // Create a cell factory for the color column (colored rectangle)
            subteamColorColumn.setCellFactory(column -> new TableCell<Subteam, String>() {
                @Override
                protected void updateItem(String colorCode, boolean empty) {
                    super.updateItem(colorCode, empty);
                    if (empty || colorCode == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(colorCode);
                        setStyle("-fx-background-color: " + colorCode + "25; -fx-text-fill: black;");
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up subteams table", e);
            showErrorAlert("Setup Error", "Failed to set up subteams table: " + e.getMessage());
        }
    }

    /**
     * Sets the current project and loads related data.
     * 
     * @param project the project to set
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        this.currentProject = project;
        viewModel.setProject(project);
        
        // Select first item in each table if available
        Platform.runLater(() -> {
            if (!viewModel.getMembers().isEmpty()) {
                membersTable.getSelectionModel().select(0);
            }
    
            if (!viewModel.getSubteams().isEmpty()) {
                subteamsTable.getSelectionModel().select(0);
            }
        });
    }

    /**
     * Handles adding a new team member.
     */
    @FXML
    public void handleAddMember() {
        // Create and show the dialog
        Dialog<TeamMember> dialog = createMemberDialog(null);
        Optional<TeamMember> result = showAndWaitDialog(dialog);
        
        if (result.isPresent()) {
            // Refresh the data
            viewModel.loadMembers();
        }
    }

    /**
     * Handles editing a team member.
     */
    @FXML
    public void handleEditMember() {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to edit");
            return;
        }

        // Create and show the dialog
        Dialog<TeamMember> dialog = createMemberDialog(selectedMember);
        Optional<TeamMember> result = showAndWaitDialog(dialog);
        
        if (result.isPresent()) {
            // Refresh the data
            viewModel.loadMembers();
        }
    }

    /**
     * Handles deleting a team member.
     */
    @FXML
    public void handleDeleteMember() {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to delete");
            return;
        }

        // Ask for confirmation
        boolean confirmed = showConfirmationAlert(
                "Delete Team Member",
                "Are you sure you want to delete " + 
                (selectedMember.getFirstName() != null ? selectedMember.getFirstName() : "") + " " +
                (selectedMember.getLastName() != null ? selectedMember.getLastName() : "") + "?");

        if (confirmed) {
            try {
                // Delete the member
                boolean success = viewModel.deleteMember(selectedMember);
                
                if (success) {
                    showInfoAlert("Member Deleted", "Team member deleted successfully");
                    // No need to refresh, view model has already updated the list
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting team member", e);
                showErrorAlert("Error", "Failed to delete team member: " + e.getMessage());
            }
        }
    }

    /**
     * Handles adding a new subteam.
     */
    @FXML
    public void handleAddSubteam() {
        // Create and show the dialog
        Dialog<Subteam> dialog = createSubteamDialog(null);
        Optional<Subteam> result = showAndWaitDialog(dialog);
        
        if (result.isPresent()) {
            // Refresh the data
            viewModel.loadSubteams();
        }
    }

    /**
     * Handles editing a subteam.
     */
    @FXML
    public void handleEditSubteam() {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to edit");
            return;
        }

        // Create and show the dialog
        Dialog<Subteam> dialog = createSubteamDialog(selectedSubteam);
        Optional<Subteam> result = showAndWaitDialog(dialog);
        
        if (result.isPresent()) {
            // Refresh the data
            viewModel.loadSubteams();
        }
    }

    /**
     * Handles deleting a subteam.
     */
    @FXML
    public void handleDeleteSubteam() {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to delete");
            return;
        }

        // Ask for confirmation
        boolean confirmed = showConfirmationAlert(
                "Delete Subteam",
                "Are you sure you want to delete " + selectedSubteam.getName() + "?");

        if (confirmed) {
            try {
                // Delete the subteam
                boolean success = viewModel.deleteSubteam(selectedSubteam);
                
                if (success) {
                    showInfoAlert("Subteam Deleted", "Subteam deleted successfully");
                    // No need to refresh, view model has already updated the list
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
                showErrorAlert("Error", "Failed to delete subteam: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a dialog for adding or editing a team member.
     * Protected for testability.
     * 
     * @param member the member to edit, or null for a new member
     * @return the dialog
     */
    protected Dialog<TeamMember> createMemberDialog(TeamMember member) {
        // Create the dialog
        Dialog<TeamMember> dialog = new Dialog<>();
        dialog.setTitle(member == null ? "Add Team Member" : "Edit Team Member");
        dialog.setHeaderText(member == null ? "Create a new team member" : "Edit team member details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        ComboBox<TeamMember.Role> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(TeamMember.Role.values());
        roleComboBox.setPromptText("Select Role");

        ComboBox<Subteam> subteamComboBox = new ComboBox<>();
        subteamComboBox.setItems(viewModel.getSubteams());
        subteamComboBox.setPromptText("Select Subteam");

        CheckBox leaderCheckBox = new CheckBox("Is Team Leader");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setPrefRowCount(3);

        // Add fields to the grid
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);
        grid.add(new Label("Subteam:"), 0, 5);
        grid.add(subteamComboBox, 1, 5);
        grid.add(leaderCheckBox, 1, 6);
        grid.add(new Label("Notes:"), 0, 7);
        grid.add(notesArea, 1, 7);

        // Initialize form with member data if editing
        if (member != null) {
            firstNameField.setText(member.getFirstName());
            lastNameField.setText(member.getLastName());
            emailField.setText(member.getEmail());
            phoneField.setText(member.getPhone());
            
            if (member.getRole() != null && !member.getRole().isEmpty()) {
                try {
                    roleComboBox.setValue(TeamMember.Role.valueOf(member.getRole()));
                } catch (IllegalArgumentException e) {
                    // Invalid role value, leave unselected
                }
            }
            
            subteamComboBox.setValue(member.getSubteam());
            leaderCheckBox.setSelected(member.isLeader());
            notesArea.setText(""); // Notes not directly stored in TeamMember entity
        }

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the first name field
        Platform.runLater(() -> firstNameField.requestFocus());

        // Set the dialog result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate fields
                if (firstNameField.getText().trim().isEmpty() && lastNameField.getText().trim().isEmpty()) {
                    showErrorAlert("Validation Error", "First name or last name is required");
                    return null;
                }
                
                // Create/update member
                TeamMember result = member != null ? member : new TeamMember();
                result.setFirstName(firstNameField.getText().trim());
                result.setLastName(lastNameField.getText().trim());
                result.setEmail(emailField.getText().trim());
                result.setPhone(phoneField.getText().trim());
                
                if (roleComboBox.getValue() != null) {
                    result.setRole(roleComboBox.getValue().toString());
                }
                
                result.setLeader(leaderCheckBox.isSelected());
                result.setSubteam(subteamComboBox.getValue());
                
                // Save the member
                TeamMember savedMember = viewModel.saveMember(result);
                return savedMember;
            }
            return null;
        });

        return dialog;
    }

    /**
     * Creates a dialog for adding or editing a subteam.
     * Protected for testability.
     * 
     * @param subteam the subteam to edit, or null for a new subteam
     * @return the dialog
     */
    protected Dialog<Subteam> createSubteamDialog(Subteam subteam) {
        // Create the dialog
        Dialog<Subteam> dialog = new Dialog<>();
        dialog.setTitle(subteam == null ? "Add Subteam" : "Edit Subteam");
        dialog.setHeaderText(subteam == null ? "Create a new subteam" : "Edit subteam details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Subteam Name");

        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker();
        colorPicker.setPromptText("Select Color");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        // Add fields to the grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);

        // Initialize form with subteam data if editing
        if (subteam != null) {
            nameField.setText(subteam.getName());
            
            if (subteam.getColorCode() != null && !subteam.getColorCode().isEmpty()) {
                try {
                    colorPicker.setValue(Color.web(subteam.getColorCode()));
                } catch (Exception e) {
                    colorPicker.setValue(Color.web("#2196F3")); // Default blue
                }
            } else {
                colorPicker.setValue(Color.web("#2196F3")); // Default blue
            }
            
            descriptionArea.setText(subteam.getDescription());
        } else {
            colorPicker.setValue(Color.web("#2196F3")); // Default blue
        }

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field
        Platform.runLater(() -> nameField.requestFocus());

        // Set the dialog result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate fields
                if (nameField.getText().trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Subteam name is required");
                    return null;
                }
                
                // Create/update subteam
                Subteam result = subteam != null ? subteam : new Subteam();
                result.setName(nameField.getText().trim());
                
                // Convert color to hex string
                Color color = colorPicker.getValue();
                String colorCode = String.format("#%02X%02X%02X",
                        (int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255));
                result.setColorCode(colorCode);
                
                result.setDescription(descriptionArea.getText().trim());
                
                // Save the subteam
                Subteam savedSubteam = viewModel.saveSubteam(result);
                return savedSubteam;
            }
            return null;
        });

        return dialog;
    }

    /**
     * Shows a dialog and waits for it to be closed.
     * Protected for testability.
     * 
     * @param <T>    the type of the dialog result
     * @param dialog the dialog to show
     * @return an Optional containing the dialog result
     */
    protected <T> Optional<T> showAndWaitDialog(Dialog<T> dialog) {
        try {
            return dialog.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
            return Optional.empty();
        }
    }

    /**
     * Handles closing the window.
     */
    @FXML
    public void handleClose() {
        try {
            // Clean up resources
            cleanup();
            
            // Get the stage from any UI element
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
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            if (dialogService != null) {
                dialogService.showErrorAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            if (dialogService != null) {
                dialogService.showInfoAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Info alert would show: {0} - {1}", new Object[] { title, message });
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Info alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows a confirmation alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    protected boolean showConfirmationAlert(String title, String message) {
        try {
            if (dialogService != null) {
                return dialogService.showConfirmationAlert(title, message);
            } else {
                // Fallback for tests
                LOGGER.log(Level.INFO, "Confirmation alert would show: {0} - {1}", new Object[] { title, message });
                return false;
            }
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation alert would show: {0} - {1}", new Object[] { title, message });
            return false;
        }
    }
    
    /**
     * Cleans up resources when the controller is no longer needed.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
    }

    /**
     * Gets the ViewModel.
     * For testing purposes.
     * 
     * @return the ViewModel
     */
    public TeamViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the currently selected team member.
     * This method exists to facilitate testing.
     * 
     * @return the selected team member
     */
    protected TeamMember getSelectedTeamMember() {
        if (membersTable != null && membersTable.getSelectionModel() != null) {
            return membersTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Gets the currently selected subteam.
     * This method exists to facilitate testing.
     * 
     * @return the selected subteam
     */
    protected Subteam getSelectedSubteam() {
        if (subteamsTable != null && subteamsTable.getSelectionModel() != null) {
            return subteamsTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Initialize method for testing purposes.
     * This method allows tests to call initialize explicitly.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Sets the ViewModel.
     * For testing purposes.
     * 
     * @param viewModel the ViewModel to set
     */
    public void setViewModel(TeamViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Sets the dialog service.
     * For testing purposes.
     * 
     * @param dialogService the dialog service to set
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
}