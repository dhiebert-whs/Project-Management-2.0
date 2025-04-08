package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MeetingViewModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for meeting management using MVVM pattern.
 */
public class MeetingController {

    private static final Logger LOGGER = Logger.getLogger(MeetingController.class.getName());

    // FXML UI components
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

    // ViewModel
    private MeetingViewModel viewModel = new MeetingViewModel();

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MeetingController");

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (datePicker == null || startTimeField == null || endTimeField == null || 
            notesArea == null || saveButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind UI elements to ViewModel properties using ViewModelBinding utility
        ViewModelBinding.bindDatePicker(datePicker, viewModel.dateProperty());
        ViewModelBinding.bindTextField(startTimeField, viewModel.startTimeStringProperty());
        ViewModelBinding.bindTextField(endTimeField, viewModel.endTimeStringProperty());
        ViewModelBinding.bindTextArea(notesArea, viewModel.notesProperty());

        // Bind buttons to commands using ViewModelBinding
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        cancelButton.setOnAction(event -> closeDialog());

        // Add error message listener
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.errorMessageProperty().set("");
            }
        });
    }

    /**
     * Sets up the controller for creating a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void setNewMeeting(Project project) {
        viewModel.initNewMeeting(project);
    }

    /**
     * Sets up the controller for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void setMeeting(Meeting meeting) {
        viewModel.initExistingMeeting(meeting);
    }

    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null) {
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
    public MeetingViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(MeetingViewModel viewModel) {
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

    // Getters for testing purposes
    public DatePicker getDatePicker() {
        return datePicker;
    }
    
    public TextField getStartTimeField() {
        return startTimeField;
    }
    
    public TextField getEndTimeField() {
        return endTimeField;
    }
    
    public TextArea getNotesArea() {
        return notesArea;
    }
    
    public Button getSaveButton() {
        return saveButton;
    }
    
    public Button getCancelButton() {
        return cancelButton;
    }
}