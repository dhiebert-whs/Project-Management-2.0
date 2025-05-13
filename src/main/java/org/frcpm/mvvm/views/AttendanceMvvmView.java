// src/main/java/org/frcpm/mvvm/views/AttendanceMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.AttendanceMvvmViewModel;
import org.frcpm.mvvm.viewmodels.AttendanceMvvmViewModel.AttendanceRecord;

/**
 * View for the attendance using MVVMFx.
 */
public class AttendanceMvvmView implements FxmlView<AttendanceMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceMvvmView.class.getName());
    
    @FXML
    private Label meetingTitleLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label timeLabel;
    
    @FXML
    private TableView<AttendanceRecord> attendanceTable;
    
    @FXML
    private TableColumn<AttendanceRecord, String> nameColumn;
    
    @FXML
    private TableColumn<AttendanceRecord, String> subteamColumn;
    
    @FXML
    private TableColumn<AttendanceRecord, Boolean> presentColumn;
    
    @FXML
    private TableColumn<AttendanceRecord, LocalTime> arrivalColumn;
    
    @FXML
    private TableColumn<AttendanceRecord, LocalTime> departureColumn;
    
    @FXML
    private TextField arrivalTimeField;
    
    @FXML
    private TextField departureTimeField;
    
    @FXML
    private Button setTimeButton;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @FXML
    private Label errorLabel;
    
    @InjectViewModel
    private AttendanceMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing AttendanceMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        }
        
        // Set up error label
        if (errorLabel != null) {
            errorLabel.textProperty().bind(viewModel.errorMessageProperty());
            errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        }
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        if (nameColumn == null || subteamColumn == null || presentColumn == null || 
            arrivalColumn == null || departureColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }
        
        try {
            // Set up name column
            nameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
            
            // Set up subteam column
            subteamColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubteam()));
            
            // Set up present column with checkboxes
            presentColumn.setCellValueFactory(cellData -> cellData.getValue().presentProperty());
            presentColumn.setCellFactory(column -> new TableCell<AttendanceRecord, Boolean>() {
                private final CheckBox checkBox = new CheckBox();
                
                {
                    // Add listener to checkbox
                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            AttendanceRecord record = getTableRow().getItem();
                            record.setPresent(newVal);
                            
                            // Mark the ViewModel as dirty
                            viewModel.setDirty(true);
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
            arrivalColumn.setCellValueFactory(cellData -> cellData.getValue().arrivalTimeProperty());
            arrivalColumn.setCellFactory(column -> new TableCell<AttendanceRecord, LocalTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                
                @Override
                protected void updateItem(LocalTime time, boolean empty) {
                    super.updateItem(time, empty);
                    if (empty || time == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(time));
                    }
                }
            });
            
            // Set up departure time column
            departureColumn.setCellValueFactory(cellData -> cellData.getValue().departureTimeProperty());
            departureColumn.setCellFactory(column -> new TableCell<AttendanceRecord, LocalTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                
                @Override
                protected void updateItem(LocalTime time, boolean empty) {
                    super.updateItem(time, empty);
                    if (empty || time == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(time));
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
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
        
        try {
            // Bind meeting information
            meetingTitleLabel.textProperty().bind(viewModel.meetingTitleProperty());
            dateLabel.textProperty().bind(viewModel.meetingDateProperty());
            timeLabel.textProperty().bind(viewModel.meetingTimeProperty());
            
            // Bind attendance table to view model's list
            attendanceTable.setItems(viewModel.getAttendanceRecords());
            
            // Set up selection listener
            attendanceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedRecord(newVal)
            );
            
            // Bind buttons
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveAttendanceCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            CommandAdapter.bindCommandButton(setTimeButton, viewModel.getSetTimeCommand());
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
            
            // Set up set time button
            setTimeButton.setOnAction(event -> handleSetTime());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
/**
     * Handles setting time for selected record.
     */
    @FXML
    private void handleSetTime() {
        AttendanceRecord selectedRecord = viewModel.getSelectedRecord();
        if (selectedRecord != null) {
            try {
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
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error setting time", e);
                showErrorAlert(
                    resources.getString("error.title"), 
                    resources.getString("error.time.set.failed") + e.getMessage());
            }
        } else {
            showInfoAlert(
                resources.getString("info.title"), 
                resources.getString("info.no.selection.member"));
        }
    }
    
    /**
     * Initializes the view with a meeting.
     * 
     * @param meeting the meeting
     */
    public void initWithMeeting(Meeting meeting) {
        try {
            viewModel.initWithMeeting(meeting);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing with meeting", e);
            showErrorAlert("Initialization Error", "Failed to initialize with meeting: " + e.getMessage());
        }
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
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
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public AttendanceMvvmViewModel getViewModel() {
        return viewModel;
    }
}
