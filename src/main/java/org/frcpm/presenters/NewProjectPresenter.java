// src/main/java/org/frcpm/presenters/NewProjectPresenter.java

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
 * Presenter for the new project dialog in AfterburnerFX pattern.
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

    // Injected services
    @Inject
    private ProjectService projectService;
    
    @Inject
    private DialogService dialogService;

    // Use @Inject instead of creating it manually
    @Inject
    private NewProjectViewModel viewModel;
    
    private ResourceBundle resources;


    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing NewProjectPresenter with resource bundle");
        
        this.resources = resources;
        

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

        // Set cancelButton action
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
     * Gets the created project.
     * 
     * @return the created project, or null if no project was created
     */
    public Project getCreatedProject() {
        return viewModel.getCreatedProject();
    }

    /**
     * Closes the dialog.
     */
    @FXML
    private void closeDialog() {
        try {
            if (cancelButton != null && cancelButton.getScene() != null && 
                cancelButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
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
        if (viewModel.isInputValid()) {
            viewModel.getCreateProjectCommand().execute();
            closeDialog();
        }
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public NewProjectViewModel getViewModel() {
        return viewModel;
    }
}