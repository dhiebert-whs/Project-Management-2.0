package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.viewmodels.MeetingViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for meeting management using AfterburnerFX pattern.
 */
public class MeetingPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MeetingPresenter.class.getName());

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

    // Injected services and ViewModel
    @Inject
    private MeetingService meetingService;
    
    @Inject
    private DialogService dialogService;
    
    @Inject
    private MeetingViewModel viewModel;

    // Resources
    private ResourceBundle resources;

    /**
     * Initializes the presenter.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MeetingPresenter with resource bundle");
        
        this.resources = resources;
        
        // Verify injection - create fallback if needed
        if (viewModel == null) {
            LOGGER.severe("MeetingViewModel not injected - creating manually as fallback");
            viewModel = new MeetingViewModel(meetingService);
        }

        // Set up bindings
        setupBindings();
        
        // Set up error handling
        setupErrorHandling();
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

        try {
            // Bind UI elements to ViewModel properties using ViewModelBinding utility
            ViewModelBinding.bindDatePicker(datePicker, viewModel.dateProperty());
            ViewModelBinding.bindTextField(startTimeField, viewModel.startTimeStringProperty());
            ViewModelBinding.bindTextField(endTimeField, viewModel.endTimeStringProperty());
            ViewModelBinding.bindTextArea(notesArea, viewModel.notesProperty());
    
            // Bind buttons to commands using ViewModelBinding
            ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
            cancelButton.setOnAction(event -> closeDialog());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the error message handling.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        try {
            // Add error message listener
            viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    showErrorAlert("Validation Error", newVal);
                    viewModel.errorMessageProperty().set("");
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up error handler", e);
            showErrorAlert("Setup Error", "Failed to initialize error handling: " + e.getMessage());
        }
    }

    /**
     * Sets up the presenter for creating a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void initNewMeeting(Project project) {
        try {
            viewModel.initNewMeeting(project);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new meeting", e);
            showErrorAlert("Initialization Error", "Failed to initialize new meeting: " + e.getMessage());
        }
    }

    /**
     * Sets up the presenter for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void initExistingMeeting(Meeting meeting) {
        try {
            viewModel.initExistingMeeting(meeting);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing meeting", e);
            showErrorAlert("Initialization Error", "Failed to initialize meeting: " + e.getMessage());
        }
    }

    /**
     * Closes the dialog.
     */
    @FXML
    private void closeDialog() {
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
     * 
     * @param title   the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
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
     * Sets the ViewModel (for testing purposes).
     * 
     * @param viewModel the ViewModel to set
     */
    public void setViewModel(MeetingViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
        setupErrorHandling();
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
     * Compatibility method for legacy code
     * 
     * @param meeting the meeting to edit
     */
    public void setMeeting(Meeting meeting) {
        initExistingMeeting(meeting);
    }
}