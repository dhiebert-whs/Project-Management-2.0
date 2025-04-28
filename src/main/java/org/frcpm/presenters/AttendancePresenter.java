package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.frcpm.models.Meeting;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.AttendanceViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for attendance tracking functionality using AfterburnerFX pattern.
 */
public class AttendancePresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AttendancePresenter.class.getName());

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

    // Injected services
    @Inject
    private AttendanceService attendanceService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private AttendanceViewModel viewModel;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing AttendancePresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected service
        viewModel = new AttendanceViewModel(attendanceService);

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components
        if (nameColumn == null || subteamColumn == null || presentColumn == null || 
            arrivalColumn == null || departureColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }
        
        // Set up name column
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMember().getFullName()));
        
        // Set up subteam column
        subteamColumn.setCellValueFactory(cellData -> {
            String subteamName = cellData.getValue().getMember().getSubteam() != null ? 
                                cellData.getValue().getMember().getSubteam().getName() : "";
            return new javafx.beans.property.SimpleStringProperty(subteamName);
        });
        
        // Set up present column with checkboxes
        presentColumn.setCellValueFactory(new PropertyValueFactory<>("present"));
        presentColumn.setCellFactory(column -> new TableCell<AttendanceViewModel.AttendanceRecord, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                // Add listener to checkbox
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        AttendanceViewModel.AttendanceRecord record = getTableRow().getItem();
                        record.setPresent(newVal);
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });
        
        // Set up arrival time column
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        arrivalColumn.setCellFactory(column -> new TableCell<AttendanceViewModel.AttendanceRecord, LocalTime>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.toString());
                }
            }
        });
        
        // Set up departure time column
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        departureColumn.setCellFactory(column -> new TableCell<AttendanceViewModel.AttendanceRecord, LocalTime>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.toString());
                }
            }
        });
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components
        if (meetingTitleLabel == null || dateLabel == null || timeLabel == null || 
            attendanceTable == null || saveButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Bind meeting information
        meetingTitleLabel.textProperty().bind(
            javafx.beans.binding.Bindings.createStringBinding(
                () -> viewModel.getMeeting() != null ? viewModel.getMeeting().getTitle() : "",
                viewModel.meetingProperty()
            )
        );
        
        dateLabel.textProperty().bind(
            javafx.beans.binding.Bindings.createStringBinding(
                () -> viewModel.getMeeting() != null ? viewModel.getMeeting().getDate().toString() : "",
                viewModel.meetingProperty()
            )
        );
        
        timeLabel.textProperty().bind(
            javafx.beans.binding.Bindings.createStringBinding(
                () -> viewModel.getMeeting() != null ? 
                     viewModel.getMeeting().getStartTime() + " - " + viewModel.getMeeting().getEndTime() : "",
                viewModel.meetingProperty()
            )
        );
        
        // Bind attendance table to view model's list
        attendanceTable.setItems(viewModel.getAttendanceRecords());
        
        // Set up selection listener
        attendanceTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> viewModel.setSelectedRecord(newVal)
        );
        
        // Bind buttons
        saveButton.setOnAction(event -> {
            if (viewModel.saveMeetingAttendance()) {
                closeDialog();
            }
        });
        
        cancelButton.setOnAction(event -> closeDialog());
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
    public void initWithMeeting(Meeting meeting) {
        viewModel.initWithMeeting(meeting);
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
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
    public AttendanceViewModel getViewModel() {
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
     * Compatibility method for legacy code.
     * 
     * @param meeting the meeting to use
     */
    public void setMeeting(Meeting meeting) {
        initWithMeeting(meeting);
    }
}