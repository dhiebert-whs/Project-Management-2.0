// src/main/java/org/frcpm/mvvm/views/TeamMemberSelectionMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.TeamMemberSelectionMvvmViewModel;

/**
 * View for the team member selection dialog using MVVMFx.
 */
public class TeamMemberSelectionMvvmView implements FxmlView<TeamMemberSelectionMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberSelectionMvvmView.class.getName());
    
    @FXML private TextField searchTextField;
    @FXML private TableView<TeamMember> teamMembersTable;
    @FXML private TableColumn<TeamMember, String> nameColumn;
    @FXML private TableColumn<TeamMember, String> usernameColumn;
    @FXML private TableColumn<TeamMember, String> emailColumn;
    @FXML private Button selectButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private TeamMemberSelectionMvvmViewModel viewModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TeamMemberSelectionMvvmView");
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        
        // Set up error label
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Set up row double-click handler
        teamMembersTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<TeamMember> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && viewModel.getSelectTeamMemberCommand().isExecutable()) {
                    viewModel.getSelectTeamMemberCommand().execute();
                    closeDialog();
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Set up the name column
        nameColumn.setCellValueFactory(cellData -> {
            TeamMember member = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> member.getFullName());
        });
        
        // Set up the username column
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        // Set up the email column
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Bind search field
        searchTextField.textProperty().bindBidirectional(viewModel.searchFilterProperty());
        
        // Bind team members table
        teamMembersTable.setItems(viewModel.getTeamMembers());
        
        // Bind selected team member
        teamMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedTeamMember(newVal));
        
        // Bind buttons to commands
        CommandAdapter.bindCommandButton(selectButton, viewModel.getSelectTeamMemberCommand());
        CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
        
        // Set up button actions
        selectButton.setOnAction(event -> {
            viewModel.getSelectTeamMemberCommand().execute();
            closeDialog();
        });
        
        cancelButton.setOnAction(event -> {
            viewModel.getCancelCommand().execute();
            closeDialog();
        });
    }
    
    /**
     * Initializes the view with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        viewModel.initWithProject(project);
    }
    
    /**
     * Checks if a team member was selected.
     * 
     * @return true if a team member was selected, false otherwise
     */
    public boolean wasTeamMemberSelected() {
        return viewModel.isTeamMemberSelected();
    }
    
    /**
     * Gets the selected team member.
     * 
     * @return the selected team member
     */
    public TeamMember getSelectedTeamMember() {
        return viewModel.getSelectedTeamMember();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        if (cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }
}