// src/main/java/org/frcpm/controllers/MilestoneController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.MilestoneViewModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for milestone management.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * MilestoneViewModel.
 */
public class MilestoneController {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneController.class.getName());
    
    // FXML UI components
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label projectLabel;
    @FXML private Label errorLabel;
    
    // ViewModel and services
    private MilestoneViewModel viewModel = new MilestoneViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MilestoneController");
        
        // Comprehensive null check for all UI components
        if (nameField == null || datePicker == null || descriptionArea == null || 
            saveButton == null || cancelButton == null || projectLabel == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Set up bindings
        setupBindings();
        
        // Set up validation error listener
        setupErrorListener();
    }
    
    /**
     * Sets up the bindings between the view model and UI controls.
     * Protected for testability.
     */
    protected void setupBindings() {
        if (nameField == null || datePicker == null || descriptionArea == null || 
            saveButton == null || cancelButton == null || projectLabel == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        try {
            // Bind text fields to view model properties
            ViewModelBinding.bindTextField(nameField, viewModel.nameProperty());
            ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());
            ViewModelBinding.bindDatePicker(datePicker, viewModel.dateProperty());
            
            // Bind project label
            projectLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                    viewModel.projectProperty()
                )
            );
            
            // Set close dialog action
            viewModel.setCloseDialogAction(this::closeDialog);
            
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
            ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            
            // Hide error label initially
            if (errorLabel != null) {
                errorLabel.setVisible(false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the error message listener.
     * Protected for testability.
     */
    protected void setupErrorListener() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        try {
            // Set up validation error listener
            viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    // If we have an error label, use it
                    if (errorLabel != null) {
                        errorLabel.setText(newValue);
                        errorLabel.setVisible(true);
                    } else {
                        // Fall back to dialog
                        showErrorAlert("Validation Error", newValue);
                    }
                } else if (errorLabel != null) {
                    // Hide error label when no error
                    errorLabel.setVisible(false);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up error listener", e);
            showErrorAlert("Setup Error", "Failed to initialize error handling: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the controller for creating a new milestone.
     * 
     * @param project the project for the milestone
     */
    public void initNewMilestone(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot initialize new milestone with null project");
            return;
        }
        
        try {
            // Initialize the view model for a new milestone
            viewModel.initNewMilestone(project);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new milestone", e);
            showErrorAlert("Initialization Error", "Failed to initialize new milestone: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the controller for editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void initExistingMilestone(Milestone milestone) {
        if (milestone == null) {
            LOGGER.warning("Cannot initialize with null milestone");
            return;
        }
        
        try {
            // Initialize the view model with the existing milestone
            viewModel.initExistingMilestone(milestone);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing milestone", e);
            showErrorAlert("Initialization Error", "Failed to initialize milestone: " + e.getMessage());
        }
    }
    
    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
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
     * Sets the view model.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(MilestoneViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
        setupErrorListener();
    }
    
    /**
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
    
    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}