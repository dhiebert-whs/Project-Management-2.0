package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.NewProjectViewModel;

import java.util.logging.Logger;

/**
 * Controller for the new project dialog.
 * Follows the MVVM pattern by delegating business logic to the
 * NewProjectViewModel.
 */
public class NewProjectController {

    private static final Logger LOGGER = Logger.getLogger(NewProjectController.class.getName());

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
    private final NewProjectViewModel viewModel = new NewProjectViewModel();

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing NewProjectController with MVVM pattern");

        // Bind UI controls to ViewModel properties
        setupBindings();

        // Setup error message handling
        setupErrorHandling();
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Bind text fields to ViewModel properties
        ViewModelBinding.bindTextField(nameField, viewModel.projectNameProperty());
        ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
        ViewModelBinding.bindDatePicker(goalEndDatePicker, viewModel.goalEndDateProperty());
        ViewModelBinding.bindDatePicker(hardDeadlinePicker, viewModel.hardDeadlineProperty());
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());

        // Bind button states and actions
        ViewModelBinding.bindCommandButton(createButton, viewModel.getCreateProjectCommand());

        // Set cancelButton action (not using ViewModelBinding as it's a simple action)
        cancelButton.setOnAction(event -> dialogStage.close());
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

        // Close the dialog when the project is created
        viewModel.getCreateProjectCommand().execute();
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
        Alert alert = createErrorAlert();
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to create an error alert for testing mocks.
     * 
     * @return the alert
     */
    protected Alert createErrorAlert() {
        return new Alert(Alert.AlertType.ERROR);
    }

    /**
     * Public method to access showErrorAlert for testing.
     * 
     * @param title   the title
     * @param message the message
     */
    public void testShowErrorAlert(String title, String message) {
        showErrorAlert(title, message);
    }

    /**
     * Public method to access setupErrorHandling for testing.
     */
    public void testSetupErrorHandling() {
        setupErrorHandling();
    }

    // Getter methods for UI components (for testing)

    /**
     * Gets the name field.
     * 
     * @return the name field
     */
    public TextField getNameField() {
        return nameField;
    }

    /**
     * Gets the start date picker.
     * 
     * @return the start date picker
     */
    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    /**
     * Gets the goal end date picker.
     * 
     * @return the goal end date picker
     */
    public DatePicker getGoalEndDatePicker() {
        return goalEndDatePicker;
    }

    /**
     * Gets the hard deadline picker.
     * 
     * @return the hard deadline picker
     */
    public DatePicker getHardDeadlinePicker() {
        return hardDeadlinePicker;
    }

    /**
     * Gets the description area.
     * 
     * @return the description area
     */
    public TextArea getDescriptionArea() {
        return descriptionArea;
    }

    /**
     * Gets the create button.
     * 
     * @return the create button
     */
    public Button getCreateButton() {
        return createButton;
    }

    /**
     * Gets the cancel button.
     * 
     * @return the cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Gets the dialog stage.
     * 
     * @return the dialog stage
     */
    public Stage getDialogStage() {
        return dialogStage;
    }

    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public NewProjectViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}