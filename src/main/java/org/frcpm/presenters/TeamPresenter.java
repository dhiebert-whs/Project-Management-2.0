package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
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

    // FXML UI components - Subteams
    @FXML
    private ListView<Subteam> subteamListView;

    @FXML
    private Button addSubteamButton;

    @FXML
    private Button editSubteamButton;

    @FXML
    private Button deleteSubteamButton;

    // FXML UI components - Team Members
    @FXML
    private TableView<TeamMember> teamMembersTable;

    @FXML
    private TableColumn<TeamMember, String> firstNameColumn;

    @FXML
    private TableColumn<TeamMember, String> lastNameColumn;

    @FXML
    private TableColumn<TeamMember, String> emailColumn;

    @FXML
    private TableColumn<TeamMember, String> roleColumn;

    @FXML
    private TableColumn<TeamMember, String> subteamColumn;

    @FXML
    private Button addMemberButton;

    @FXML
    private Button editMemberButton;

    @FXML
    private Button deleteMemberButton;

    // FXML UI components - Other
    @FXML
    private Button importMembersButton;

    @FXML
    private Button exportMembersButton;

    @FXML
    private Button closeButton;

    @FXML
    private ComboBox<TeamViewModel.MemberFilter> filterComboBox;

    @FXML
    private Label projectNameLabel;

    @FXML
    private Label totalMembersLabel;

    @FXML
    private Label subteamMembersLabel;

    @FXML
    private TextField searchTextField;

    // FXML UI components - Member Details
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<TeamMember.Role> roleComboBox;

    @FXML
    private ComboBox<Subteam> memberSubteamComboBox;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button saveMemberButton;

    @FXML
    private Button cancelMemberButton;

    // FXML UI components - Subteam Details
    @FXML
    private TextField subteamNameField;

    @FXML
    private TextField subteamColorField;

    @FXML
    private TextField subteamLeadField;

    @FXML
    private TextArea subteamDescriptionArea;

    @FXML
    private Button saveSubteamButton;

    @FXML
    private Button cancelSubteamButton;

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
        setupTeamMembersTable();
        
        // Setup listview
        setupSubteamListView();
        
        // Setup filter combo box
        setupFilterComboBox();
        
        // Setup role combo box options
        setupRoleComboBox();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
    }

    /**
     * Sets up the team members table columns.
     */
    private void setupTeamMembersTable() {
        // Check for null UI components for testability
        if (teamMembersTable == null || firstNameColumn == null || lastNameColumn == null || 
            emailColumn == null || roleColumn == null || subteamColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        // Setup columns
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Setup role column with enum display
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<TeamMember, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                } else {
                    String displayName = "";
                    try {
                        TeamMember.Role roleEnum = TeamMember.Role.valueOf(role);
                        displayName = roleEnum.getDisplayName();
                    } catch (IllegalArgumentException e) {
                        displayName = role;
                    }
                    setText(displayName);
                }
            }
        });
        
        // Setup subteam column
        subteamColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    member.getSubteam() != null ? member.getSubteam().getName() : "");
        });
        
        // Setup row double-click handler for editing
        teamMembersTable.setRowFactory(tv -> {
            TableRow<TeamMember> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    TeamMember member = row.getItem();
                    loadMemberDetails(member);
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the subteam list view.
     */
    private void setupSubteamListView() {
        if (subteamListView == null) {
            LOGGER.warning("Subteam list view not initialized - likely in test environment");
            return;
        }

        // Setup custom cell factory for subteam list items
        subteamListView.setCellFactory(new Callback<ListView<Subteam>, ListCell<Subteam>>() {
            @Override
            public ListCell<Subteam> call(ListView<Subteam> param) {
                return new ListCell<Subteam>() {
                    @Override
                    protected void updateItem(Subteam subteam, boolean empty) {
                        super.updateItem(subteam, empty);
                        
                        if (empty || subteam == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(subteam.getName() + " (" + 
                                   viewModel.getMemberCountForSubteam(subteam) + ")");
                            
                            // Set a background color based on the subteam's color if available
                            if (subteam.getColorCode() != null && !subteam.getColorCode().isEmpty()) {
                                setStyle("-fx-background-color: " + subteam.getColorCode() + "25;"); // 25 is hex for 15% opacity
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
        
        // Setup selection handler
        subteamListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    viewModel.setSelectedSubteam(newVal);
                    if (newVal != null) {
                        loadSubteamDetails(newVal);
                        viewModel.filterMembersBySubteam(newVal);
                    } else {
                        clearSubteamDetails();
                        viewModel.clearFilter();
                    }
                });
    }
    
    /**
     * Sets up the filter combo box.
     */
    private void setupFilterComboBox() {
        if (filterComboBox == null) {
            LOGGER.warning("Filter combo box not initialized - likely in test environment");
            return;
        }

        // Setup filter options
        filterComboBox.getItems().clear();
        filterComboBox.getItems().addAll(TeamViewModel.MemberFilter.values());
        filterComboBox.setValue(TeamViewModel.MemberFilter.ALL);
        
        // Setup filter change listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
                viewModel.applyFilter();
                updateMemberCounts();
            }
        });
    }
    
    /**
     * Sets up the role combo box.
     */
    private void setupRoleComboBox() {
        if (roleComboBox == null) {
            LOGGER.warning("Role combo box not initialized - likely in test environment");
            return;
        }

        // Setup role options
        roleComboBox.getItems().clear();
        roleComboBox.getItems().addAll(TeamMember.Role.values());
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (teamMembersTable == null || subteamListView == null || 
            addMemberButton == null || editMemberButton == null || 
            deleteMemberButton == null || addSubteamButton == null || 
            editSubteamButton == null || deleteSubteamButton == null || 
            importMembersButton == null || exportMembersButton == null ||
            closeButton == null || saveMemberButton == null || 
            cancelMemberButton == null || saveSubteamButton == null || 
            cancelSubteamButton == null || searchTextField == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind project name
            if (projectNameLabel != null) {
                projectNameLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.createStringBinding(
                        () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                        viewModel.projectProperty()
                    )
                );
            }
            
            // Bind lists to view model collections
            teamMembersTable.setItems(viewModel.getFilteredMembers());
            subteamListView.setItems(viewModel.getSubteams());
            
            // Bind member details fields
            if (firstNameField != null && lastNameField != null && emailField != null && 
                phoneField != null && notesArea != null) {
                
                ViewModelBinding.bindTextField(firstNameField, viewModel.firstNameProperty());
                ViewModelBinding.bindTextField(lastNameField, viewModel.lastNameProperty());
                ViewModelBinding.bindTextField(emailField, viewModel.emailProperty());
                ViewModelBinding.bindTextField(phoneField, viewModel.phoneProperty());
                ViewModelBinding.bindTextArea(notesArea, viewModel.notesProperty());
                
                if (roleComboBox != null) {
                    ViewModelBinding.bindComboBox(roleComboBox, viewModel.roleProperty());
                }
                
                if (memberSubteamComboBox != null) {
                    memberSubteamComboBox.setItems(viewModel.getSubteams());
                    ViewModelBinding.bindComboBox(memberSubteamComboBox, viewModel.memberSubteamProperty());
                }
            }
            
            // Bind subteam details fields
            if (subteamNameField != null && subteamColorField != null && 
                subteamLeadField != null && subteamDescriptionArea != null) {
                
                ViewModelBinding.bindTextField(subteamNameField, viewModel.subteamNameProperty());
                ViewModelBinding.bindTextField(subteamColorField, viewModel.subteamColorProperty());
                ViewModelBinding.bindTextField(subteamLeadField, viewModel.subteamLeadProperty());
                ViewModelBinding.bindTextArea(subteamDescriptionArea, viewModel.subteamDescriptionProperty());
            }
            
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
            ViewModelBinding.bindCommandButton(editMemberButton, viewModel.getEditMemberCommand());
            ViewModelBinding.bindCommandButton(deleteMemberButton, viewModel.getDeleteMemberCommand());
            ViewModelBinding.bindCommandButton(addSubteamButton, viewModel.getAddSubteamCommand());
            ViewModelBinding.bindCommandButton(editSubteamButton, viewModel.getEditSubteamCommand());
            ViewModelBinding.bindCommandButton(deleteSubteamButton, viewModel.getDeleteSubteamCommand());
            ViewModelBinding.bindCommandButton(importMembersButton, viewModel.getImportMembersCommand());
            ViewModelBinding.bindCommandButton(exportMembersButton, viewModel.getExportMembersCommand());
            ViewModelBinding.bindCommandButton(saveMemberButton, viewModel.getSaveMemberCommand());
            ViewModelBinding.bindCommandButton(saveSubteamButton, viewModel.getSaveSubteamCommand());
            
            // Bind selection changes
            teamMembersTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMember(newVal));
            
            // Set search field listener
            searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                viewModel.setSearchText(newVal);
                viewModel.applyFilter();
            });
            
            // Set manual button actions
            cancelMemberButton.setOnAction(event -> clearMemberDetails());
            cancelSubteamButton.setOnAction(event -> clearSubteamDetails());
            closeButton.setOnAction(event -> handleClose());
            
            // Update member count labels
            updateMemberCounts();
            
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
     * Updates the member count labels.
     */
    private void updateMemberCounts() {
        if (totalMembersLabel != null) {
            totalMembersLabel.setText("Total Members: " + viewModel.getTotalMemberCount());
        }
        
        if (subteamMembersLabel != null && viewModel.getSelectedSubteam() != null) {
            subteamMembersLabel.setText(
                    viewModel.getSelectedSubteam().getName() + " Members: " + 
                    viewModel.getMemberCountForSubteam(viewModel.getSelectedSubteam()));
        } else if (subteamMembersLabel != null) {
            subteamMembersLabel.setText("Filtered Members: " + viewModel.getFilteredMemberCount());
        }
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
        refreshData();
    }
    
    /**
     * Refreshes team data.
     */
    private void refreshData() {
        viewModel.loadMembers();
        viewModel.loadSubteams();
        viewModel.applyFilter();
        updateMemberCounts();
    }
    
    /**
     * Loads member details into the editing form.
     * 
     * @param member the team member to edit
     */
    private void loadMemberDetails(TeamMember member) {
        if (member == null) {
            clearMemberDetails();
            return;
        }
        
        viewModel.initExistingMember(member);
    }
    
    /**
     * Clears the member details form.
     */
    private void clearMemberDetails() {
        viewModel.initNewMember();
    }
    
    /**
     * Loads subteam details into the editing form.
     * 
     * @param subteam the subteam to edit
     */
    private void loadSubteamDetails(Subteam subteam) {
        if (subteam == null) {
            clearSubteamDetails();
            return;
        }
        
        viewModel.initExistingSubteam(subteam);
    }
    
    /**
     * Clears the subteam details form.
     */
    private void clearSubteamDetails() {
        viewModel.initNewSubteam();
    }
    
    /**
     * Handles adding a new team member.
     */
    @FXML
    private void handleAddMember() {
        clearMemberDetails();
    }
    
    /**
     * Handles editing a team member.
     */
    @FXML
    private void handleEditMember() {
        TeamMember selectedMember = teamMembersTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            loadMemberDetails(selectedMember);
        } else {
            showInfoAlert("No Selection", "Please select a team member to edit");
        }
    }
    
    /**
     * Handles deleting a team member.
     */
    @FXML
    private void handleDeleteMember() {
        TeamMember selectedMember = teamMembersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showInfoAlert("No Selection", "Please select a team member to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Member", 
                "Are you sure you want to delete the team member '" + 
                selectedMember.getFullName() + "'?");
        
        if (confirmed) {
            boolean success = viewModel.deleteMember(selectedMember);
            if (success) {
                showInfoAlert("Member Deleted", "Team member was successfully deleted");
                refreshData();
            }
        }
    }
    
    /**
     * Handles adding a new subteam.
     */
    @FXML
    private void handleAddSubteam() {
        clearSubteamDetails();
    }
    
    /**
     * Handles editing a subteam.
     */
    @FXML
    private void handleEditSubteam() {
        Subteam selectedSubteam = subteamListView.getSelectionModel().getSelectedItem();
        if (selectedSubteam != null) {
            loadSubteamDetails(selectedSubteam);
        } else {
            showInfoAlert("No Selection", "Please select a subteam to edit");
        }
    }
    
    /**
     * Handles deleting a subteam.
     */
    @FXML
    private void handleDeleteSubteam() {
        Subteam selectedSubteam = subteamListView.getSelectionModel().getSelectedItem();
        if (selectedSubteam == null) {
            showInfoAlert("No Selection", "Please select a subteam to delete");
            return;
        }
        
        int memberCount = viewModel.getMemberCountForSubteam(selectedSubteam);
        if (memberCount > 0) {
            boolean confirmed = showConfirmationAlert(
                    "Delete Subteam with Members", 
                    "The subteam '" + selectedSubteam.getName() + "' has " + memberCount + 
                    " members. Deleting it will remove the subteam assignment from these members. Continue?");
            
            if (!confirmed) {
                return;
            }
        } else {
            boolean confirmed = showConfirmationAlert(
                    "Delete Subteam", 
                    "Are you sure you want to delete the subteam '" + 
                    selectedSubteam.getName() + "'?");
            
            if (!confirmed) {
                return;
            }
        }
        
        boolean success = viewModel.deleteSubteam(selectedSubteam);
        if (success) {
            showInfoAlert("Subteam Deleted", "Subteam was successfully deleted");
            refreshData();
        }
    }
    
    /**
     * Handles saving a team member.
     */
    @FXML
    private void handleSaveMember() {
        if (viewModel.saveMember()) {
            refreshData();
            clearMemberDetails();
        }
    }
    
    /**
     * Handles saving a subteam.
     */
    @FXML
    private void handleSaveSubteam() {
        if (viewModel.saveSubteam()) {
            refreshData();
            clearSubteamDetails();
        }
    }
    
    /**
     * Handles importing team members from CSV.
     */
    @FXML
    private void handleImportMembers() {
        if (viewModel.importMembersFromCSV()) {
            refreshData();
            showInfoAlert("Import Complete", "Team members were successfully imported");
        }
    }
    
    /**
     * Handles exporting team members to CSV.
     */
    @FXML
    private void handleExportMembers() {
        if (viewModel.exportMembersToCSV()) {
            showInfoAlert("Export Complete", "Team members were successfully exported");
        }
    }
    
    /**
     * Handles closing the view.
     */
    private void handleClose() {
        try {
            if (closeButton != null && closeButton.getScene() != null && 
                closeButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing window", e);
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