package org.frcpm.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
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
    private final TeamViewModel viewModel = new TeamViewModel();
    
    // Dialog service for UI interactions
    private final DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TeamController");

        // Initialize tables
        setupMembersTable();
        setupSubteamsTable();
        
        // Set up bindings
        setupBindings();
        
        // Set up row selection handlers
        setupSelectionHandlers();
        
        // Set up row double-click handlers
        setupRowFactories();

        // Set initial selection
        if (!viewModel.getMembers().isEmpty()) {
            membersTable.getSelectionModel().select(0);
        }
        
        if (!viewModel.getSubteams().isEmpty()) {
            subteamsTable.getSelectionModel().select(0);
        }
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Bind tables to the ViewModel's ObservableLists
        membersTable.setItems(viewModel.getMembers());
        subteamsTable.setItems(viewModel.getSubteams());
        
        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getCreateNewMemberCommand());
        ViewModelBinding.bindCommandButton(editMemberButton, viewModel.getEditMemberCommand());
        ViewModelBinding.bindCommandButton(deleteMemberButton, viewModel.getDeleteMemberCommand());

        ViewModelBinding.bindCommandButton(addSubteamButton, viewModel.getCreateNewSubteamCommand());
        ViewModelBinding.bindCommandButton(editSubteamButton, viewModel.getEditSubteamCommand());
        ViewModelBinding.bindCommandButton(deleteSubteamButton, viewModel.getDeleteSubteamCommand());

        // Bind error messages
        viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Sets up selection handlers for tables.
     */
    private void setupSelectionHandlers() {
        // Set up row selection handlers
        membersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewModel.setSelectedMember(newValue));

        subteamsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewModel.setSelectedSubteam(newValue));
    }
    
    /**
     * Sets up row factories for double-click handling.
     */
    private void setupRowFactories() {
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
    }

    /**
     * Sets up the members table columns and cell factories.
     */
    private void setupMembersTable() {
        memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        memberNameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        memberSubteamColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSubteam() != null ? cellData.getValue().getSubteam().getName() : ""));
        memberLeaderColumn.setCellValueFactory(new PropertyValueFactory<>("leader"));

        // Create a cell factory for the leader column (checkbox)
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
    }

    /**
     * Sets up the subteams table columns and cell factories.
     */
    private void setupSubteamsTable() {
        subteamNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subteamColorColumn.setCellValueFactory(new PropertyValueFactory<>("colorCode"));
        subteamSpecialtiesColumn.setCellValueFactory(new PropertyValueFactory<>("specialties"));

        // Create a cell factory for the color column (colored rectangle)
        subteamColorColumn.setCellFactory(column -> new TableCell<Subteam, String>() {
            @Override
            protected void updateItem(String colorCode, boolean empty) {
                super.updateItem(colorCode, empty);
                if (empty || colorCode == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(colorCode);
                    setStyle("-fx-background-color: " + colorCode + "; -fx-text-fill: white;");
                }
            }
        });
    }

    /**
     * Handles adding a new team member.
     */
    @FXML
    public void handleAddMember() {
        // Prepare the ViewModel for a new member
        viewModel.initNewMember();
        
        // Create and show the dialog
        Dialog<TeamMember> dialog = createMemberDialog();
        showAndWaitDialog(dialog);
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

        // Prepare the ViewModel for editing the selected member
        viewModel.initExistingMember(selectedMember);
        
        // Create and show the dialog
        Dialog<TeamMember> dialog = createMemberDialog();
        showAndWaitDialog(dialog);
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
            "Are you sure you want to delete " + selectedMember.getFullName() + "?"
        );
        
        if (confirmed) {
            try {
                // Execute the delete command
                viewModel.setSelectedMember(selectedMember);
                viewModel.getDeleteMemberCommand().execute();
                
                showInfoAlert("Member Deleted", "Team member deleted successfully");
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
        // Prepare the ViewModel for a new subteam
        viewModel.initNewSubteam();
        
        // Create and show the dialog
        Dialog<Subteam> dialog = createSubteamDialog();
        showAndWaitDialog(dialog);
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

        // Prepare the ViewModel for editing the selected subteam
        viewModel.initExistingSubteam(selectedSubteam);
        
        // Create and show the dialog
        Dialog<Subteam> dialog = createSubteamDialog();
        showAndWaitDialog(dialog);
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
            "Are you sure you want to delete " + selectedSubteam.getName() + "?"
        );
        
        if (confirmed) {
            try {
                // Execute the delete command
                viewModel.setSelectedSubteam(selectedSubteam);
                viewModel.getDeleteSubteamCommand().execute();
                
                showInfoAlert("Subteam Deleted", "Subteam deleted successfully");
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
     * @return the dialog
     */
    protected Dialog<TeamMember> createMemberDialog() {
        // Create the dialog
        Dialog<TeamMember> dialog = new Dialog<>();
        dialog.setTitle(viewModel.isNewMember() ? "Add Team Member" : "Edit Team Member");
        dialog.setHeaderText(viewModel.isNewMember() ? "Create a new team member" : "Edit team member details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        TextArea skillsArea = new TextArea();
        skillsArea.setPromptText("Skills");
        skillsArea.setPrefRowCount(3);

        ComboBox<Subteam> subteamComboBox = new ComboBox<>();
        subteamComboBox.setItems(viewModel.getSubteams());
        subteamComboBox.setPromptText("Select Subteam");

        CheckBox leaderCheckBox = new CheckBox("Is Team Leader");

        // Add fields to the grid
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("Subteam:"), 0, 5);
        grid.add(subteamComboBox, 1, 5);
        grid.add(leaderCheckBox, 1, 6);
        grid.add(new Label("Skills:"), 0, 7);
        grid.add(skillsArea, 1, 7);

        // Bind UI elements to ViewModel properties
        ViewModelBinding.bindTextField(usernameField, viewModel.memberUsernameProperty());
        ViewModelBinding.bindTextField(firstNameField, viewModel.memberFirstNameProperty());
        ViewModelBinding.bindTextField(lastNameField, viewModel.memberLastNameProperty());
        ViewModelBinding.bindTextField(emailField, viewModel.memberEmailProperty());
        ViewModelBinding.bindTextField(phoneField, viewModel.memberPhoneProperty());
        ViewModelBinding.bindTextArea(skillsArea, viewModel.memberSkillsProperty());
        ViewModelBinding.bindComboBox(subteamComboBox, viewModel.memberSubteamProperty());
        leaderCheckBox.selectedProperty().bindBidirectional(viewModel.memberIsLeaderProperty());

        // Disable username field if editing existing member
        usernameField.setDisable(!viewModel.isNewMember());

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field
        Platform.runLater(() -> usernameField.requestFocus());

        // Set the dialog result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (viewModel.memberValidProperty().get()) {
                    viewModel.getSaveMemberCommand().execute();
                    return viewModel.getSelectedMember();
                } else {
                    // Show error if validation fails
                    showErrorAlert("Validation Error", viewModel.getErrorMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Creates a dialog for adding or editing a subteam.
     * Protected for testability.
     * 
     * @return the dialog
     */
    protected Dialog<Subteam> createSubteamDialog() {
        // Create the dialog
        Dialog<Subteam> dialog = new Dialog<>();
        dialog.setTitle(viewModel.isNewSubteam() ? "Add Subteam" : "Edit Subteam");
        dialog.setHeaderText(viewModel.isNewSubteam() ? "Create a new subteam" : "Edit subteam details");

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

        TextArea specialtiesArea = new TextArea();
        specialtiesArea.setPromptText("Specialties");
        specialtiesArea.setPrefRowCount(3);

        // Add fields to the grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(new Label("Specialties:"), 0, 2);
        grid.add(specialtiesArea, 1, 2);

        // Bind UI elements to ViewModel properties
        ViewModelBinding.bindTextField(nameField, viewModel.subteamNameProperty());
        ViewModelBinding.bindTextArea(specialtiesArea, viewModel.subteamSpecialtiesProperty());
        
        // Custom binding for color picker
        String colorCode = viewModel.getSubteamColorCode();
        if (colorCode != null && !colorCode.isEmpty()) {
            try {
                Color color = Color.web(colorCode);
                colorPicker.setValue(color);
            } catch (Exception e) {
                colorPicker.setValue(Color.BLUE);
            }
        } else {
            colorPicker.setValue(Color.BLUE);
        }
        
        // Update color code when color picker changes
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            String webColor = String.format("#%02X%02X%02X",
                    (int) (newValue.getRed() * 255),
                    (int) (newValue.getGreen() * 255),
                    (int) (newValue.getBlue() * 255));
            viewModel.setSubteamColorCode(webColor);
        });

        // Disable name field if editing existing subteam
        nameField.setDisable(!viewModel.isNewSubteam());

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field
        Platform.runLater(() -> nameField.requestFocus());

        // Set the dialog result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (viewModel.subteamValidProperty().get()) {
                    viewModel.getSaveSubteamCommand().execute();
                    return viewModel.getSelectedSubteam();
                } else {
                    // Show error if validation fails
                    showErrorAlert("Validation Error", viewModel.getErrorMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Shows a dialog and waits for it to be closed.
     * Protected for testability.
     * 
     * @param <T> the type of the dialog result
     * @param dialog the dialog to show
     * @return an Optional containing the dialog result
     */
    protected <T> Optional<T> showAndWaitDialog(Dialog<T> dialog) {
        return dialog.showAndWait();
    }

    /**
     * Handles closing the window.
     */
    @FXML
    public void handleClose() {
        // Get the stage from any UI element
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        dialogService.showErrorAlert(title, message);
    }

    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        dialogService.showInfoAlert(title, message);
    }
    
    /**
     * Shows a confirmation alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    protected boolean showConfirmationAlert(String title, String message) {
        return dialogService.showConfirmationAlert(title, message);
    }

    /**
     * Gets the ViewModel.
     * For testing purposes.
     * 
     * @return the ViewModel
     */
    TeamViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the currently selected team member.
     * This method exists to facilitate testing.
     * 
     * @return the selected team member
     */
    protected TeamMember getSelectedTeamMember() {
        return membersTable.getSelectionModel().getSelectedItem();
    }

    /**
     * Gets the currently selected subteam.
     * This method exists to facilitate testing.
     * 
     * @return the selected subteam
     */
    protected Subteam getSelectedSubteam() {
        return subteamsTable.getSelectionModel().getSelectedItem();
    }

    /**
     * Initialize method for testing purposes.
     * This method allows tests to call initialize explicitly.
     */
    public void testInitialize() {
        initialize();
    }
}