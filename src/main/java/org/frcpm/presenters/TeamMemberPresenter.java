package org.frcpm.presenters;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.TeamMemberViewModel;

public class TeamMemberPresenter implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberPresenter.class.getName());
    
    @FXML private TextField nameTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField roleTextField;
    @FXML private TextField skillsTextField;
    @FXML private ComboBox<Subteam> subteamComboBox;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button newButton;
    @FXML private TableView<TeamMember> teamMemberTableView;
    @FXML private TableColumn<TeamMember, String> nameColumn;
    @FXML private TableColumn<TeamMember, String> emailColumn;
    @FXML private TableColumn<TeamMember, String> roleColumn;
    @FXML private TableColumn<TeamMember, String> subteamColumn;
    
    @Inject
    private TeamMemberViewModel viewModel;
    
    @Inject
    private DialogService dialogService;
    
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
        if (nameTextField == null || emailTextField == null || phoneTextField == null ||
            roleTextField == null || skillsTextField == null || subteamComboBox == null ||
            saveButton == null || deleteButton == null || newButton == null ||
            teamMemberTableView == null || nameColumn == null || emailColumn == null ||
            roleColumn == null || subteamColumn == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        setupTableColumns();
        setupBindings();
        setupEventHandlers();
        setupErrorHandling();
    }
    
    protected void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
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
        nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
        emailTextField.textProperty().bindBidirectional(viewModel.emailProperty());
        phoneTextField.textProperty().bindBidirectional(viewModel.phoneNumberProperty());
        roleTextField.textProperty().bindBidirectional(viewModel.roleProperty());
        skillsTextField.textProperty().bindBidirectional(viewModel.skillsProperty());
        
        // Bind combo box
        subteamComboBox.setItems(viewModel.getSubteams());
        subteamComboBox.valueProperty().bindBidirectional(viewModel.selectedSubteamProperty());
        
        // Set up combo box display
        subteamComboBox.setConverter(ViewModelBinding.createStringConverter(
            subteam -> subteam != null ? subteam.getName() : "",
            name -> viewModel.getSubteams().stream()
                  .filter(s -> s.getName().equals(name))
                  .findFirst()
                  .orElse(null)
        ));
        
        // Bind table
        teamMemberTableView.setItems(viewModel.getTeamMembers());
        
        // Bind buttons
        ViewModelBinding.bindCommandToButton(saveButton, viewModel.getSaveCommand());
        ViewModelBinding.bindCommandToButton(deleteButton, viewModel.getDeleteCommand());
        ViewModelBinding.bindCommandToButton(newButton, viewModel.getNewCommand());
        
        // Bind button disable properties based on validation
        saveButton.disableProperty().bind(Bindings.not(viewModel.validProperty()));
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> !viewModel.getDeleteCommand().canExecute(),
            viewModel.isNewTeamMemberProperty()
        ));
    }
    
    protected void setupEventHandlers() {
        saveButton.setOnAction(event -> viewModel.getSaveCommand().execute());
        deleteButton.setOnAction(event -> {
            if (showConfirmationDialog("Confirm Delete", 
                    "Are you sure you want to delete this team member?")) {
                viewModel.getDeleteCommand().execute();
            }
        });
        newButton.setOnAction(event -> viewModel.getNewCommand().execute());
    }
    
    protected void setupErrorHandling() {
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorDialog("Error", newVal);
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
    
    protected boolean showConfirmationDialog(String title, String message) {
        return dialogService.showConfirmationDialog(title, message);
    }
    
    protected void showErrorDialog(String title, String message) {
        dialogService.showErrorDialog(title, message);
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
}