package org.frcpm.presenters;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import org.frcpm.di.DialogFactory;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.TeamMemberViewModel;
import org.frcpm.views.TeamMemberView;

public class TeamMemberPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TeamMemberPresenter.class.getName());

    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField skillsTextField;
    @FXML
    private ComboBox<Subteam> subteamComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button newButton;
    @FXML
    private TableView<TeamMember> teamMemberTableView;
    @FXML
    private TableColumn<TeamMember, String> nameColumn;
    @FXML
    private TableColumn<TeamMember, String> emailColumn;
    @FXML
    private TableColumn<TeamMember, String> usernameColumn;
    @FXML
    private TableColumn<TeamMember, String> subteamColumn;

    @Inject
    private TeamMemberViewModel viewModel;

    @Inject
    private DialogService dialogService;

    ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if we're in a testing environment and create fallbacks
        if (viewModel == null) {
            LOGGER.severe("ViewModel not injected - creating manually as fallback");
            viewModel = new TeamMemberViewModel();
        }

        if (dialogService == null) {
            LOGGER.severe("DialogService not injected - creating manually as fallback");
            dialogService = ServiceProvider.getService(DialogService.class);
        }

        // Null check UI components for test environments
        if (firstNameTextField == null || lastNameTextField == null || usernameTextField == null ||
                emailTextField == null || phoneTextField == null || skillsTextField == null ||
                subteamComboBox == null || saveButton == null || deleteButton == null ||
                newButton == null || teamMemberTableView == null || nameColumn == null ||
                emailColumn == null || usernameColumn == null || subteamColumn == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        setupTableColumns();
        setupBindings();
        setupEventHandlers();
        setupErrorHandling();
    }

    protected void setupTableColumns() {
        // Use a custom cell value factory for name column to show full name
        nameColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            return new ReadOnlyStringWrapper(member.getFullName());
        });

        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Handle subteam column with custom cell factory to handle null subteams
        subteamColumn.setCellValueFactory(cellData -> {
            Subteam subteam = cellData.getValue().getSubteam();
            return new ReadOnlyStringWrapper(subteam != null ? subteam.getName() : "");
        });

        // Add selection listener
        teamMemberTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        viewModel.initExistingTeamMember(newSelection);
                    }
                });
    }

    protected void setupBindings() {
        // Bind text fields
        firstNameTextField.textProperty().bindBidirectional(viewModel.firstNameProperty());
        lastNameTextField.textProperty().bindBidirectional(viewModel.lastNameProperty());
        usernameTextField.textProperty().bindBidirectional(viewModel.usernameProperty());
        emailTextField.textProperty().bindBidirectional(viewModel.emailProperty());
        phoneTextField.textProperty().bindBidirectional(viewModel.phoneProperty());
        skillsTextField.textProperty().bindBidirectional(viewModel.skillsProperty());

        // Bind combo box
        subteamComboBox.setItems(viewModel.getSubteams());
        subteamComboBox.valueProperty().bindBidirectional(viewModel.selectedSubteamProperty());

        // Set up combo box display with a string converter
        subteamComboBox.setConverter(new StringConverter<Subteam>() {
            @Override
            public String toString(Subteam subteam) {
                return subteam != null ? subteam.getName() : "";
            }

            @Override
            public Subteam fromString(String name) {
                return viewModel.getSubteams().stream()
                        .filter(s -> s.getName().equals(name))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Bind table
        teamMemberTableView.setItems(viewModel.getTeamMembers());

        // Bind buttons
        saveButton.setOnAction(event -> viewModel.getSaveCommand().execute());
        deleteButton.setOnAction(event -> {
            if (showConfirmationDialog("Confirm Delete",
                    "Are you sure you want to delete this team member?")) {
                viewModel.getDeleteCommand().execute();
            }
        });
        newButton.setOnAction(event -> viewModel.getNewCommand().execute());

        // Bind button disable properties based on validation
        saveButton.disableProperty().bind(Bindings.not(viewModel.validProperty()));
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !viewModel.getDeleteCommand().canExecute(),
                viewModel.isNewTeamMemberProperty()));
    }

    protected void setupEventHandlers() {
        // Event handlers already set up in setupBindings()
    }

    protected void setupErrorHandling() {
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorDialog(
                    resources != null ? resources.getString("error.title") : "Error", 
                    newVal);
                viewModel.clearErrorMessage();
            }
        });
    }

    public void initProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        viewModel.initProject(project);
    }

    protected boolean showConfirmationDialog(String defaultTitle, String defaultMessage) {
        String title = resources != null ? 
            resources.getString(defaultTitle.toLowerCase().replace(" ", ".")) : defaultTitle;
        return dialogService.showConfirmationAlert(title, defaultMessage);
    }

    protected void showErrorDialog(String defaultTitle, String defaultMessage) {
        String title = resources != null ? 
            resources.getString(defaultTitle.toLowerCase().replace(" ", ".")) : defaultTitle;
        dialogService.showErrorAlert(title, defaultMessage);
    }

    // Public for testing
    public TeamMemberViewModel getViewModel() {
        return viewModel;
    }

    // Public for testing
    public void setViewModel(TeamMemberViewModel viewModel) {
        this.viewModel = viewModel;
    }

    // Public for testing
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }

    /**
     * Handles adding a new team member action.
     * This uses the current form to create a new team member.
     */
    @FXML
    public void handleAddTeamMember() {
        try {
            // Use the existing viewModel to initialize a new team member
            viewModel.initNewTeamMember();
            
            // Show feedback that the team member form is ready for input
            showInfoDialog(
                resources != null ? resources.getString("info.title") : "Information",
                resources != null ? resources.getString("info.new.member") : "Form ready for new team member entry.");
                
        } catch (Exception e) {
            LOGGER.severe("Error preparing form for new team member: " + e.getMessage());
            showErrorDialog(
                resources != null ? resources.getString("error.title") : "Error",
                "Failed to prepare form for new team member: " + e.getMessage());
        }
    }

    /**
     * Handles editing an existing team member.
     * This uses the current form to edit the selected team member.
     */
    @FXML
    public void handleEditTeamMember() {
        TeamMember selectedMember = teamMemberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorDialog(
                resources != null ? resources.getString("info.title") : "No Selection", 
                resources != null ? resources.getString("info.no.member.selected") : "Please select a team member to edit");
            return;
        }
        
        try {
            // Update the form with the selected member (this already happens via the selection listener)
            // No additional action needed here
        } catch (Exception e) {
            LOGGER.severe("Error editing team member: " + e.getMessage());
            showErrorDialog(
                resources != null ? resources.getString("error.title") : "Error",
                "Failed to load team member data: " + e.getMessage());
        }
    }

    /**
    /**
     * Shows an information alert dialog.
     */
    protected void showInfoDialog(String defaultTitle, String defaultMessage) {
        String title = resources != null ? 
            resources.getString(defaultTitle.toLowerCase().replace(" ", ".")) : defaultTitle;
        dialogService.showInfoAlert(title, defaultMessage);
    }

    /**
     * Refreshes the team members list from the service.
     * This is a public method to allow external components to trigger a refresh.
     */
    public void refreshTeamMembers() {
        // Simply trigger the viewModel to reload, if it has a public method for this
        // Otherwise, the existing data binding should handle updates
        setupBindings();
    }

}