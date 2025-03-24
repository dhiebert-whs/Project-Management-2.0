package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for milestone management.
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
    
    private final MilestoneService milestoneService = ServiceFactory.getMilestoneService();
    
    private Milestone milestone;
    private Project project;
    private boolean isNewMilestone;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MilestoneController");
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Set up button actions
        saveButton.setOnAction(this::handleSave);
        cancelButton.setOnAction(this::handleCancel);
    }
    
    /**
     * Sets up the controller for creating a new milestone.
     * 
     * @param project the project for the milestone
     */
    public void setNewMilestone(Project project) {
        this.project = project;
        this.milestone = null;
        this.isNewMilestone = true;
        
        // Clear fields
        nameField.setText("");
        datePicker.setValue(LocalDate.now());
        descriptionArea.setText("");
    }
    
    /**
     * Sets up the controller for editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
        this.project = milestone.getProject();
        this.isNewMilestone = false;
        
        // Set field values
        nameField.setText(milestone.getName());
        datePicker.setValue(milestone.getDate());
        descriptionArea.setText(milestone.getDescription());
    }
    
    /**
     * Handles saving the milestone.
     * 
     * @param event the action event
     */
    private void handleSave(ActionEvent event) {
        try {
            // Get field values
            String name = nameField.getText();
            LocalDate date = datePicker.getValue();
            String description = descriptionArea.getText();
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                showErrorAlert("Invalid Input", "Milestone name cannot be empty");
                return;
            }
            
            if (date == null) {
                showErrorAlert("Invalid Input", "Milestone date cannot be empty");
                return;
            }
            
            // Validate date is within project timeline
            if (date.isBefore(project.getStartDate()) || date.isAfter(project.getHardDeadline())) {
                showErrorAlert("Invalid Date", "Milestone date must be within the project timeline");
                return;
            }
            
            if (isNewMilestone) {
                // Create new milestone
                milestone = milestoneService.createMilestone(name, date, project.getId(), description);
            } else {
                // Update existing milestone
                milestone = milestoneService.updateMilestoneDate(milestone.getId(), date);
                milestone = milestoneService.updateDescription(milestone.getId(), description);
            }
            
            // Close the dialog
            closeDialog();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving milestone", e);
            showErrorAlert("Error", "Failed to save milestone: " + e.getMessage());
        }
    }
    
    /**
     * Handles canceling milestone editing.
     * 
     * @param event the action event
     */
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Gets the milestone that was created or edited.
     * 
     * @return the milestone
     */
    public Milestone getMilestone() {
        return milestone;
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
}