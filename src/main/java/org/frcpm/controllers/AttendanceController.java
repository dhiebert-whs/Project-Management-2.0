package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Meeting;
import org.frcpm.viewmodels.AttendanceViewModel;

import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for attendance tracking functionality using MVVM pattern.
 */
public class AttendanceController {

    private static final Logger LOGGER = Logger.getLogger(AttendanceController.class.getName());

    // FXML UI components
    @FXML
    private Label meetingTitleLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private TableView<AttendanceViewModel.AttendanceRecord> attendanceTable;

    @FXML
    private TableColumn<AttendanceViewModel.AttendanceRecord, String> nameColumn;

    @FXML
    private TableColumn<AttendanceViewModel.AttendanceRecord, String> subteamColumn;

    @FXML
    private TableColumn<AttendanceViewModel.AttendanceRecord, Boolean> presentColumn;

    @FXML
    private TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime> arrivalColumn;

    @FXML
    private TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime> departureColumn;

    @FXML
    private TextField arrivalTimeField;
    
    @FXML
    private TextField departureTimeField;
    
    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // ViewModel
    private AttendanceViewModel viewModel = new AttendanceViewModel();

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing AttendanceController");

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Implementation remains the same (MVVM compliant)
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Implementation remains the same (MVVM compliant)
    }
    
    /**
     * Handles setting time for selected record.
     */
    @FXML
    public void handleSetTime() {
        AttendanceViewModel.AttendanceRecord selectedRecord = viewModel.getSelectedRecord();
        if (selectedRecord != null) {
            // Check for null UI components for testability
            LocalTime arrivalTime = null;
            LocalTime departureTime = null;
            
            if (arrivalTimeField != null) {
                arrivalTime = viewModel.parseTime(arrivalTimeField.getText());
            }
            
            if (departureTimeField != null) {
                departureTime = viewModel.parseTime(departureTimeField.getText());
            }
            
            viewModel.updateRecordTimes(selectedRecord, arrivalTime, departureTime);
            
            // Check for null UI component for testability
            if (attendanceTable != null) {
                attendanceTable.refresh();
            }
        } else {
            showInfoAlert("No Selection", "Please select a team member first.");
        }
    }

    /**
     * Sets the meeting for attendance tracking.
     * 
     * @param meeting the meeting
     */
    public void setMeeting(Meeting meeting) {
        viewModel.initWithMeeting(meeting);
    }

    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Creates an alert dialog.
     * Protected for testability.
     * 
     * @param alertType the type of alert
     * @return the created alert
     */
    protected Alert createAlert(Alert.AlertType alertType) {
        return new Alert(alertType);
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public AttendanceViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(AttendanceViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Gets the meeting from the ViewModel.
     * 
     * @return the meeting
     */
    public Meeting getMeeting() {
        return viewModel.getMeeting();
    }

    // Getters for testing purposes remain the same
    public Label getMeetingTitleLabel() {
        return meetingTitleLabel;
    }

    public Label getDateLabel() {
        return dateLabel;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public TableView<AttendanceViewModel.AttendanceRecord> getAttendanceTable() {
        return attendanceTable;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}