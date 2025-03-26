package org.frcpm.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for team management functionality.
 */
public class TeamController {
    
    private static final Logger LOGGER = Logger.getLogger(TeamController.class.getName());
    
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
    
    // Member details controls
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextArea skillsArea;
    
    @FXML
    private ComboBox<Subteam> subteamComboBox;
    
    @FXML
    private CheckBox leaderCheckBox;
    
    // Subteam details controls
    @FXML
    private TextField subteamNameField;
    
    @FXML
    private ColorPicker colorPicker;
    
    @FXML
    private TextArea specialtiesArea;
    
    private final TeamMemberService teamMemberService = ServiceFactory.getTeamMemberService();
    private final SubteamService subteamService = ServiceFactory.getSubteamService();
    
    private ObservableList<TeamMember> membersList = FXCollections.observableArrayList();
    private ObservableList<Subteam> subteamsList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TeamController");
        
        // Initialize Members Table
        memberUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        memberNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        memberSubteamColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSubteam() != null ? 
                cellData.getValue().getSubteam().getName() : ""));
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
        
        // Initialize Subteams Table
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
        
        // Set up button actions
        addMemberButton.setOnAction(this::handleAddMember);
        editMemberButton.setOnAction(this::handleEditMember);
        deleteMemberButton.setOnAction(this::handleDeleteMember);
        
        addSubteamButton.setOnAction(this::handleAddSubteam);
        editSubteamButton.setOnAction(this::handleEditSubteam);
        deleteSubteamButton.setOnAction(this::handleDeleteSubteam);
        
        // Set up row double-click handlers
        membersTable.setRowFactory(tv -> {
            TableRow<TeamMember> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditMember(new ActionEvent());
                }
            });
            return row;
        });
        
        subteamsTable.setRowFactory(tv -> {
            TableRow<Subteam> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditSubteam(new ActionEvent());
                }
            });
            return row;
        });
        
        // Set table items
        membersTable.setItems(membersList);
        subteamsTable.setItems(subteamsList);
        
        // Load data
        loadTeamData();
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
    
    /**
     * Loads team data from the database.
     */
    private void loadTeamData() {
        // Load subteams
        List<Subteam> subteams = subteamService.findAll();
        subteamsList.setAll(subteams);
        
        // Load team members
        List<TeamMember> members = teamMemberService.findAll();
        membersList.setAll(members);
    }

    /**
     * Public method to access loadTeamData for testing.
     */
    public void testLoadTeamData() {
        loadTeamData();
    }
    
    /**
     * Handles adding a new team member.
     * 
     * @param event the action event
     */
    private void handleAddMember(ActionEvent event) {
        Dialog<TeamMember> dialog = createMemberDialog(null);
        
        Optional<TeamMember> result = dialog.showAndWait();
        result.ifPresent(member -> {
            // Reload data to show the new member
            loadTeamData();
        });
    }

    /**
     * Public method to access handleAddMember for testing.
     * 
     * @param event the action event
     */
    public void testHandleAddMember(ActionEvent event) {
        handleAddMember(event);
    }
    
    /**
     * Handles editing a team member.
     * 
     * @param event the action event
     */
    private void handleEditMember(ActionEvent event) {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to edit");
            return;
        }
        
        Dialog<TeamMember> dialog = createMemberDialog(selectedMember);
        
        Optional<TeamMember> result = dialog.showAndWait();
        result.ifPresent(member -> {
            // Reload data to show the updated member
            loadTeamData();
        });
    }

    /**
     * Public method to access handleEditMember for testing.
     * 
     * @param event the action event
     */
    public void testHandleEditMember(ActionEvent event) {
        handleEditMember(event);
    }
    
    /**
     * Handles deleting a team member.
     * 
     * @param event the action event
     */
    private void handleDeleteMember(ActionEvent event) {
        TeamMember selectedMember = membersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to delete");
            return;
        }
        
        // Ask for confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Team Member");
        confirmDialog.setContentText("Are you sure you want to delete " + 
                                    selectedMember.getFullName() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the member
                teamMemberService.deleteById(selectedMember.getId());
                
                // Reload data
                loadTeamData();
                
                showInfoAlert("Member Deleted", "Team member deleted successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting team member", e);
                showErrorAlert("Error", "Failed to delete team member: " + e.getMessage());
            }
        }
    }

    /**
     * Public method to access handleDeleteMember for testing.
     * 
     * @param event the action event
     */
    public void testHandleDeleteMember(ActionEvent event) {
        handleDeleteMember(event);
    }
    
    /**
     * Handles adding a new subteam.
     * 
     * @param event the action event
     */
    private void handleAddSubteam(ActionEvent event) {
        Dialog<Subteam> dialog = createSubteamDialog(null);
        
        Optional<Subteam> result = dialog.showAndWait();
        result.ifPresent(subteam -> {
            // Reload data to show the new subteam
            loadTeamData();
        });
    }

    /**
     * Public method to access handleAddSubteam for testing.
     * 
     * @param event the action event
     */
    public void testHandleAddSubteam(ActionEvent event) {
        handleAddSubteam(event);
    }
    
    /**
     * Handles editing a subteam.
     * 
     * @param event the action event
     */
    private void handleEditSubteam(ActionEvent event) {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to edit");
            return;
        }
        
        Dialog<Subteam> dialog = createSubteamDialog(selectedSubteam);
        
        Optional<Subteam> result = dialog.showAndWait();
        result.ifPresent(subteam -> {
            // Reload data to show the updated subteam
            loadTeamData();
        });
    }

    /**
     * Public method to access handleEditSubteam for testing.
     * 
     * @param event the action event
     */
    public void testHandleEditSubteam(ActionEvent event) {
        handleEditSubteam(event);
    }
    
    /**
     * Handles deleting a subteam.
     * 
     * @param event the action event
     */
    private void handleDeleteSubteam(ActionEvent event) {
        Subteam selectedSubteam = subteamsTable.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showErrorAlert("No Selection", "Please select a subteam to delete");
            return;
        }
        
        // Check if subteam has members
        List<TeamMember> members = teamMemberService.findBySubteam(selectedSubteam);
        if (!members.isEmpty()) {
            showErrorAlert("Cannot Delete", "This subteam has members assigned to it. " +
                          "Please reassign or delete these members first.");
            return;
        }
        
        // Ask for confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Subteam");
        confirmDialog.setContentText("Are you sure you want to delete " + 
                                    selectedSubteam.getName() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the subteam
                subteamService.deleteById(selectedSubteam.getId());
                
                // Reload data
                loadTeamData();
                
                showInfoAlert("Subteam Deleted", "Subteam deleted successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting subteam", e);
                showErrorAlert("Error", "Failed to delete subteam: " + e.getMessage());
            }
        }
    }

    /**
     * Public method to access handleDeleteSubteam for testing.
     * 
     * @param event the action event
     */
    public void testHandleDeleteSubteam(ActionEvent event) {
        handleDeleteSubteam(event);
    }
    
    /**
     * Creates a dialog for adding or editing a team member.
     * 
     * @param member the team member to edit, or null for a new member
     * @return the dialog
     */
    private Dialog<TeamMember> createMemberDialog(TeamMember member) {
        boolean isNewMember = member == null;
        
        // Create the dialog
        Dialog<TeamMember> dialog = new Dialog<>();
        dialog.setTitle(isNewMember ? "Add Team Member" : "Edit Team Member");
        dialog.setHeaderText(isNewMember ? "Create a new team member" : "Edit team member details");
        
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
        subteamComboBox.setItems(FXCollections.observableArrayList(subteamService.findAll()));
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
        
        // Set initial values if editing existing member
        if (!isNewMember) {
            usernameField.setText(member.getUsername());
            usernameField.setDisable(true); // Username cannot be changed
            firstNameField.setText(member.getFirstName());
            lastNameField.setText(member.getLastName());
            emailField.setText(member.getEmail());
            phoneField.setText(member.getPhone());
            skillsArea.setText(member.getSkills());
            leaderCheckBox.setSelected(member.isLeader());
            
            if (member.getSubteam() != null) {
                // Find the matching subteam in the list
                for (Subteam subteam : subteamComboBox.getItems()) {
                    if (subteam.getId().equals(member.getSubteam().getId())) {
                        subteamComboBox.setValue(subteam);
                        break;
                    }
                }
            }
        }
        
        // Set the dialog content
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Convert the result to a team member when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String username = usernameField.getText();
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String email = emailField.getText();
                    String phone = phoneField.getText();
                    boolean isLeader = leaderCheckBox.isSelected();
                    String skills = skillsArea.getText();
                    Subteam subteam = subteamComboBox.getValue();
                    
                    // Validate required fields
                    if (username == null || username.trim().isEmpty()) {
                        throw new IllegalArgumentException("Username is required");
                    }
                    
                    if (firstName == null || firstName.trim().isEmpty()) {
                        throw new IllegalArgumentException("First name is required");
                    }
                    
                    TeamMember result;
                    if (isNewMember) {
                        // Create new team member
                        result = teamMemberService.createTeamMember(
                            username, firstName, lastName, email, phone, isLeader);
                    } else {
                        // Update existing team member
                        result = member;
                        result.setFirstName(firstName);
                        result.setLastName(lastName);
                        result.setLeader(isLeader);
                        result = teamMemberService.updateContactInfo(
                            result.getId(), email, phone);
                    }
                    
                    // Update skills
                    result = teamMemberService.updateSkills(result.getId(), skills);
                    
                    // Assign to subteam if selected
                    if (subteam != null) {
                        result = teamMemberService.assignToSubteam(
                            result.getId(), subteam.getId());
                    }
                    
                    return result;
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to save team member: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }

    /**
     * Public method to access createMemberDialog for testing.
     * 
     * @param member the team member to edit, or null for a new member
     * @return the dialog
     */
    public Dialog<TeamMember> testCreateMemberDialog(TeamMember member) {
        return createMemberDialog(member);
    }
    
    /**
     * Creates a dialog for adding or editing a subteam.
     * 
     * @param subteam the subteam to edit, or null for a new subteam
     * @return the dialog
     */
    private Dialog<Subteam> createSubteamDialog(Subteam subteam) {
        boolean isNewSubteam = subteam == null;
        
        // Create the dialog
        Dialog<Subteam> dialog = new Dialog<>();
        dialog.setTitle(isNewSubteam ? "Add Subteam" : "Edit Subteam");
        dialog.setHeaderText(isNewSubteam ? "Create a new subteam" : "Edit subteam details");
        
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
        
        // Set initial values if editing existing subteam
        if (!isNewSubteam) {
            nameField.setText(subteam.getName());
            specialtiesArea.setText(subteam.getSpecialties());
            
            // Set color picker value
            try {
                javafx.scene.paint.Color color = javafx.scene.paint.Color.web(subteam.getColorCode());
                colorPicker.setValue(color);
            } catch (Exception e) {
                // Use default color if parsing fails
                colorPicker.setValue(javafx.scene.paint.Color.BLUE);
            }
        } else {
            // Default color for new subteam
            colorPicker.setValue(javafx.scene.paint.Color.BLUE);
        }
        
        // Set the dialog content
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field
        Platform.runLater(() -> nameField.requestFocus());
        
        // Convert the result to a subteam when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    javafx.scene.paint.Color color = colorPicker.getValue();
                    String colorCode = String.format("#%02X%02X%02X",
                        (int)(color.getRed() * 255),
                        (int)(color.getGreen() * 255),
                        (int)(color.getBlue() * 255));
                    String specialties = specialtiesArea.getText();
                    
                    // Validate required fields
                    if (name == null || name.trim().isEmpty()) {
                        throw new IllegalArgumentException("Name is required");
                    }
                    
                    Subteam result;
                    if (isNewSubteam) {
                        // Create new subteam
                        result = subteamService.createSubteam(name, colorCode, specialties);
                    } else {
                        // Update existing subteam
                        result = subteamService.updateColorCode(subteam.getId(), colorCode);
                        result = subteamService.updateSpecialties(result.getId(), specialties);
                    }
                    
                    return result;
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to save subteam: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }

    /**
     * Public method to access createSubteamDialog for testing.
     * 
     * @param subteam the subteam to edit, or null for a new subteam
     * @return the dialog
     */
    public Dialog<Subteam> testCreateSubteamDialog(Subteam subteam) {
        return createSubteamDialog(subteam);
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Public method to access showErrorAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowErrorAlert(String title, String message) {
        showErrorAlert(title, message);
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Public method to access showInfoAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowInfoAlert(String title, String message) {
        showInfoAlert(title, message);
    }
    
    /**
     * Gets the tab pane.
     * 
     * @return the tab pane
     */
    public TabPane getTabPane() {
        return tabPane;
    }
    
    /**
     * Gets the members table.
     * 
     * @return the members table
     */
    public TableView<TeamMember> getMembersTable() {
        return membersTable;
    }
    
    /**
     * Gets the member username column.
     * 
     * @return the member username column
     */
    public TableColumn<TeamMember, String> getMemberUsernameColumn() {
        return memberUsernameColumn;
    }
    
    /**
     * Gets the member name column.
     * 
     * @return the member name column
     */
    public TableColumn<TeamMember, String> getMemberNameColumn() {
        return memberNameColumn;
    }
    
    /**
     * Gets the member email column.
     * 
     * @return the member email column
     */
    public TableColumn<TeamMember, String> getMemberEmailColumn() {
        return memberEmailColumn;
    }
    
    /**
     * Gets the member subteam column.
     * 
     * @return the member subteam column
     */
    public TableColumn<TeamMember, String> getMemberSubteamColumn() {
        return memberSubteamColumn;
    }
    
    /**
     * Gets the member leader column.
     * 
     * @return the member leader column
     */
    public TableColumn<TeamMember, Boolean> getMemberLeaderColumn() {
        return memberLeaderColumn;
    }
    
    /**
     * Gets the add member button.
     * 
     * @return the add member button
     */
    public Button getAddMemberButton() {
        return addMemberButton;
    }
    
    /**
     * Gets the edit member button.
     * 
     * @return the edit member button
     */
    public Button getEditMemberButton() {
        return editMemberButton;
    }
    
    /**
     * Gets the delete member button.
     * 
     * @return the delete member button
     */
    public Button getDeleteMemberButton() {
        return deleteMemberButton;
    }
    
    /**
     * Gets the subteams table.
     * 
     * @return the subteams table
     */
    public TableView<Subteam> getSubteamsTable() {
        return subteamsTable;
    }
    
    /**
     * Gets the subteam name column.
     * 
     * @return the subteam name column
     */
    public TableColumn<Subteam, String> getSubteamNameColumn() {
        return subteamNameColumn;
    }
    
    /**
     * Gets the subteam color column.
     * 
     * @return the subteam color column
     */
    public TableColumn<Subteam, String> getSubteamColorColumn() {
        return subteamColorColumn;
    }
    
    /**
     * Gets the subteam specialties column.
     * 
     * @return the subteam specialties column
     */
    public TableColumn<Subteam, String> getSubteamSpecialtiesColumn() {
        return subteamSpecialtiesColumn;
    }
    
    /**
     * Gets the add subteam button.
     * 
     * @return the add subteam button
     */
    public Button getAddSubteamButton() {
        return addSubteamButton;
    }
    
    /**
     * Gets the edit subteam button.
     * 
     * @return the edit subteam button
     */
    public Button getEditSubteamButton() {
        return editSubteamButton;
    }
    
    /**
     * Gets the delete subteam button.
     * 
     * @return the delete subteam button
     */
    public Button getDeleteSubteamButton() {
        return deleteSubteamButton;
    }
    
    /**
     * Gets the username field.
     * 
     * @return the username field
     */
    public TextField getUsernameField() {
        return usernameField;
    }
    
    /**
     * Gets the first name field.
     * 
     * @return the first name field
     */
    public TextField getFirstNameField() {
        return firstNameField;
    }
    
    /**
     * Gets the last name field.
     * 
     * @return the last name field
     */
    public TextField getLastNameField() {
        return lastNameField;
    }
    
    /**
     * Gets the email field.
     * 
     * @return the email field
     */
    public TextField getEmailField() {
        return emailField;
    }
    
    /**
     * Gets the phone field.
     * 
     * @return the phone field
     */
    public TextField getPhoneField() {
        return phoneField;
    }
    
    /**
     * Gets the skills area.
     * 
     * @return the skills area
     */
    public TextArea getSkillsArea() {
        return skillsArea;
    }
    
    /**
     * Gets the subteam combo box.
     * 
     * @return the subteam combo box
     */
    public ComboBox<Subteam> getSubteamComboBox() {
        return subteamComboBox;
    }
    
    /**
     * Gets the leader check box.
     * 
     * @return the leader check box
     */
    public CheckBox getLeaderCheckBox() {
        return leaderCheckBox;
    }
    
    /**
     * Gets the subteam name field.
     * 
     * @return the subteam name field
     */
    public TextField getSubteamNameField() {
        return subteamNameField;
    }
    
    /**
     * Gets the color picker.
     * 
     * @return the color picker
     */
    public ColorPicker getColorPicker() {
        return colorPicker;
    }
    
    /**
     * Gets the specialties area.
     * 
     * @return the specialties area
     */
    public TextArea getSpecialtiesArea() {
        return specialtiesArea;
    }
    
    /**
     * Gets the team member service.
     * 
     * @return the team member service
     */
    public TeamMemberService getTeamMemberService() {
        return teamMemberService;
    }
    
    /**
     * Gets the subteam service.
     * 
     * @return the subteam service
     */
    public SubteamService getSubteamService() {
        return subteamService;
    }
    
    /**
     * Gets the members list.
     * 
     * @return the members list
     */
    public ObservableList<TeamMember> getMembersList() {
        return membersList;
    }
    
    /**
     * Gets the subteams list.
     * 
     * @return the subteams list
     */
    public ObservableList<Subteam> getSubteamsList() {
        return subteamsList;
    }
}
