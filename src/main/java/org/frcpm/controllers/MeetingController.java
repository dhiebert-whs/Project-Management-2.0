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
    private final MeetingViewModel viewModel = new MeetingViewModel();

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
        // Bind UI elements to ViewModel properties using ViewModelBinding utility
        ViewModelBinding.bindDatePicker(datePicker, viewModel.dateProperty());
        ViewModelBinding.bindTextField(startTimeField, viewModel.startTimeStringProperty());
        ViewModelBinding.bindTextField(endTimeField, viewModel.endTimeStringProperty());
        ViewModelBinding.bindTextArea(notesArea, viewModel.notesProperty());

        // Bind buttons to commands using onAction
        saveButton.setOnAction(event -> {
            if (viewModel.isValid()) {
                viewModel.getSaveCommand().execute();
                closeDialog();
            } else {
                showErrorAlert("Invalid Input", viewModel.getErrorMessage());
            }
        });

        cancelButton.setOnAction(event -> closeDialog());

        // Bind button disable property to command canExecute
        saveButton.disableProperty().bind(viewModel.validProperty().not());
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
     */
    private void closeDialog() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            // Just log the error for testing purposes
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
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
     * Gets the meeting from the ViewModel.
     * 
     * @return the meeting
     */
    public Meeting getMeeting() {
        return viewModel.getMeeting();
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}