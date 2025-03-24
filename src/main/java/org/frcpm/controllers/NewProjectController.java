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
}