package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ProjectService;
import org.frcpm.viewmodels.NewProjectViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the new project dialog.
 * Follows the AfterburnerFX presenter convention.
 */
public class NewProjectPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(NewProjectPresenter.class.getName());

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker goalEndDatePicker;

    @FXML
    private DatePicker hardDeadlinePicker;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private NewProjectViewModel viewModel;
    private ResourceBundle resources;

    @Inject
    private ProjectService projectService;
    
    @Inject
    private DialogService dialogService;

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing NewProjectPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected service
        viewModel = new NewProjectViewModel(projectService);

        // Bind UI controls to ViewModel properties
        setupBindings();

        // Setup error message handling
        setupErrorHandling();
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (nameField == null || startDatePicker == null || goalEndDatePicker == null || 
            hardDeadlinePicker == null || descriptionArea == null || 
            createButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Bind text fields to ViewModel properties
        ViewModelBinding.bindTextField(nameField, viewModel.projectNameProperty());
        ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
        ViewModelBinding.bindDatePicker(goalEndDatePicker, viewModel.goalEndDateProperty());
        ViewModelBinding.bindDatePicker(hardDeadlinePicker, viewModel.hardDeadlineProperty());
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());

        // Bind button states and actions
        ViewModelBinding.bindCommandButton(createButton, viewModel.getCreateProjectCommand());

        // Set cancelButton action (not using ViewModelBinding as it's a simple action)
        cancelButton.setOnAction(event -> closeDialog());
    }

    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        // Show an alert when error message changes
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error Creating Project", newValue);
                viewModel.errorMessageProperty().set("");
            }
        });
    }

    /**
     * Sets the dialog stage.
     * 
     * @param dialogStage the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        // Set dialogStage first, then try to execute the command
        // to avoid NullPointerException if command tries to access dialogStage
        if (viewModel.getCreatedProject() != null) {
            dialogStage.close();
        }
    }

    /**
     * Gets the created project.
     * 
     * @return the created project, or null if no project was created
     */
    public Project getCreatedProject() {
        return viewModel.getCreatedProject();
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title   the title
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
     * Closes the dialog.
     */
    @FXML
    private void closeDialog() {
        try {
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Handler for the create button.
     */
    @FXML
    private void handleCreate() {
        if (viewModel.validate()) {
            viewModel.getCreateProjectCommand().execute();
            closeDialog();
        }
    }
}