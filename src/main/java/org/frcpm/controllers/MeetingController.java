package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for meeting management.
 */
public class MeetingController {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingController.class.getName());
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private TextField startTimeField;
    
    @FXML
    private TextField endTimeField;
    
    @FXML
    private TextArea notesArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private final MeetingService meetingService = ServiceFactory.getMeetingService();
    
    private Meeting meeting;
    private Project project;
    private boolean isNewMeeting;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MeetingController");
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Set default times
        startTimeField.setText("16:00");
        endTimeField.setText("18:00");
        
        // Set up button actions
        saveButton.setOnAction(this::handleSave);
        cancelButton.setOnAction(this::handleCancel);
    }
    
    /**
     * Sets up the controller for creating a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void setNewMeeting(Project project) {
        this.project = project;
        this.meeting = null;
        this.isNewMeeting = true;
        
        // Clear fields
        datePicker.setValue(LocalDate.now());
        startTimeField.setText("16:00");
        endTimeField.setText("18:00");
        notesArea.setText("");
    }
    
    /**
     * Sets up the controller for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
        this.project = meeting.getProject();
        this.isNewMeeting = false;
        
        // Set field values
        datePicker.setValue(meeting.getDate());
        startTimeField.setText(meeting.getStartTime().toString());
        endTimeField.setText(meeting.getEndTime().toString());
        notesArea.setText(meeting.getNotes());
    }
    
    /**
     * Handles saving the meeting.
     * 
     * @param event the action event
     */
    private void handleSave(ActionEvent event) {
        try {
            // Get field values
            LocalDate date = datePicker.getValue();
            String startTimeStr = startTimeField.getText();
            String endTimeStr = endTimeField.getText();
            String notes = notesArea.getText();
            
            // Validate required fields
            if (date == null) {
                showErrorAlert("Invalid Input", "Meeting date cannot be empty");
                return;
            }
            
            if (startTimeStr == null || startTimeStr.trim().isEmpty()) {
                showErrorAlert("Invalid Input", "Start time cannot be empty");
                return;
            }
            
            if (endTimeStr == null || endTimeStr.trim().isEmpty()) {
                showErrorAlert("Invalid Input", "End time cannot be empty");
                return;
            }
            
            // Parse time fields
            LocalTime startTime;
            LocalTime endTime;
            
            try {
                startTime = LocalTime.parse(startTimeStr);
            } catch (DateTimeParseException e) {
                showErrorAlert("Invalid Time", "Start time format should be HH:MM");
                return;
            }
            
            try {
                endTime = LocalTime.parse(endTimeStr);
            } catch (DateTimeParseException e) {
                showErrorAlert("Invalid Time", "End time format should be HH:MM");
                return;
            }
            
            // Validate end time is after start time
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showErrorAlert("Invalid Time", "End time must be after start time");
                return;
            }
            
            if (isNewMeeting) {
                // Create new meeting
                meeting = meetingService.createMeeting(date, startTime, endTime, project.getId(), notes);
            } else {
                // Update existing meeting
                meeting = meetingService.updateMeetingDateTime(meeting.getId(), date, startTime, endTime);
                meeting = meetingService.updateNotes(meeting.getId(), notes);
            }
            
            // Close the dialog
            closeDialog();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            showErrorAlert("Error", "Failed to save meeting: " + e.getMessage());
        }
    }
    
    /**
     * Handles canceling meeting editing.
     * 
     * @param event the action event
     */
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Gets the meeting that was created or edited.
     * 
     * @return the meeting
     */
    public Meeting getMeeting() {
        return meeting;
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
