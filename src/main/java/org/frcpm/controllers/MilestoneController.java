package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MilestoneViewModel;

import java.util.logging.Logger;

/**
 * Controller for milestone management.
 * Fully implements MVVM pattern with proper bindings.
 */
public class MilestoneController {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneController.class.getName());
    
    @FXML
    private TextField nameField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    // ViewModel
    private MilestoneViewModel viewModel;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MilestoneController");
        
        // Create the view model
        viewModel = new MilestoneViewModel();
        
        // Set up bindings
        setupBindings();
        
        // Set up validation error listener
        viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Validation Error", newValue);
            }
        });
    }
    
    /**
     * Sets up the bindings between the view and the view model.
     */
    private void setupBindings() {
        // Bind text fields to view model properties
        ViewModelBinding.bindTextField(nameField, viewModel.nameProperty());
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());
        ViewModelBinding.bindDatePicker(datePicker, viewModel.dateProperty());
        
        // Set close dialog action
        viewModel.setCloseDialogAction(this::closeDialog);
        
        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
    }
    
    /**
     * Sets up the controller for creating a new milestone.
     * 
     * @param project the project for the milestone
     */
    public void setNewMilestone(Project project) {
        // Initialize the view model for a new milestone
        viewModel.initNewMilestone(project);
        
        // Set up save success listener to close dialog when save succeeds
        setupSaveSuccessListener();
    }
    
    /**
     * Sets up the controller for editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void setMilestone(Milestone milestone) {
        // Initialize the view model with the existing milestone
        viewModel.initExistingMilestone(milestone);
        
        // Set up save success listener to close dialog when save succeeds
        setupSaveSuccessListener();
    }
    
    /**
     * Sets up a listener to close the dialog when save is successful.
     */
    private void setupSaveSuccessListener() {
        // Listen for successful save (dirty flag cleared and no error message)
        viewModel.dirtyProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && viewModel.getErrorMessage() == null) {
                // Save was successful, close the dialog
                closeDialog();
            }
        });
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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
     * Gets the name field.
     * 
     * @return the name field
     */
    public TextField getNameField() {
        return nameField;
    }

    /**
     * Gets the date picker.
     * 
     * @return the date picker
     */
    public DatePicker getDatePicker() {
        return datePicker;
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
     * Gets the save button.
     * 
     * @return the save button
     */
    public Button getSaveButton() {
        return saveButton;
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
     * Gets the milestone.
     * 
     * @return the milestone from the view model
     */
    public Milestone getMilestone() {
        return viewModel.getMilestone();
    }

    /**
     * Gets the project.
     * 
     * @return the project from the view model
     */
    public Project getProject() {
        return viewModel.getProject();
    }

    /**
     * Gets the isNewMilestone flag.
     * 
     * @return true if this is a new milestone, false otherwise
     */
    public boolean isNewMilestone() {
        return viewModel.isNewMilestone();
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public MilestoneViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}