package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the new project dialog.
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
    private Project createdProject;
    private final ProjectService projectService = ServiceFactory.getProjectService();
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Set default dates
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        goalEndDatePicker.setValue(today.plusWeeks(6));
        hardDeadlinePicker.setValue(today.plusWeeks(8));
        
        // Add validation listeners
        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> validateInput());
        goalEndDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> validateInput());
        hardDeadlinePicker.valueProperty().addListener((observable, oldValue, newValue) -> validateInput());
        
        // Initialize validation state
        validateInput();
        
        // Set up button actions
        createButton.setOnAction(event -> handleCreate());
        cancelButton.setOnAction(event -> dialogStage.close());
    }
    
    /**
     * Sets the dialog stage.
     * 
     * @param dialogStage the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Gets the created project.
     * 
     * @return the created project, or null if no project was created
     */
    public Project getCreatedProject() {
        return createdProject;
    }
    
    /**
     * Handles creating a new project.
     */
    private void handleCreate() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Get values from form
            String name = nameField.getText();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate goalEndDate = goalEndDatePicker.getValue();
            LocalDate hardDeadline = hardDeadlinePicker.getValue();
            String description = descriptionArea.getText();
            
            // Create the project
            Project project = projectService.createProject(name, startDate, goalEndDate, hardDeadline);
            
            // Set description if provided
            if (description != null && !description.isEmpty()) {
                project = projectService.updateProject(
                    project.getId(), name, startDate, goalEndDate, hardDeadline, description
                );
            }
            
            // Store the created project
            createdProject = project;
            
            // Close the dialog
            dialogStage.close();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating project", e);
            showErrorAlert("Error Creating Project", "Failed to create the project: " + e.getMessage());
        }
    }
    
    /**
     * Validates the input fields.
     * 
     * @return true if the input is valid, false otherwise
     */
    private boolean validateInput() {
        // Check if required fields are filled
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            createButton.setDisable(true);
            return false;
        }
        
        if (startDatePicker.getValue() == null || 
            goalEndDatePicker.getValue() == null || 
            hardDeadlinePicker.getValue() == null) {
            createButton.setDisable(true);
            return false;
        }
        
        // Check date relationships
        LocalDate startDate = startDatePicker.getValue();
        LocalDate goalEndDate = goalEndDatePicker.getValue();
        LocalDate hardDeadline = hardDeadlinePicker.getValue();
        
        if (goalEndDate.isBefore(startDate) || hardDeadline.isBefore(startDate)) {
            createButton.setDisable(true);
            return false;
        }
        
        // All validations passed
        createButton.setDisable(false);
        return true;
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

    // Add these methods at the end of the NewProjectController class

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
     * Gets the project service.
     * 
     * @return the project service
     */
    public ProjectService getProjectService() {
        return projectService;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Public method to access handleCreate for testing.
     */
    public void testHandleCreate() {
        handleCreate();
    }

    /**
     * Public method to access validateInput for testing.
     * 
     * @return true if the input is valid, false otherwise
     */
    public boolean testValidateInput() {
        return validateInput();
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
     * Helper method to create an error alert for testing mocks.
     * 
     * @return the alert
     */
    protected Alert createErrorAlert() {
        return new Alert(Alert.AlertType.ERROR);
    }
}