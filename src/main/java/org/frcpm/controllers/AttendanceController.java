package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Meeting;
import org.frcpm.viewmodels.AttendanceViewModel;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for attendance tracking functionality using MVVM pattern.
 */
public class AttendanceController {

    private static final Logger LOGGER = Logger.getLogger(AttendanceController.class.getName());

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
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // ViewModel
    private final AttendanceViewModel viewModel = new AttendanceViewModel();

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing AttendanceController");

        // Set up table columns
        nameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        subteamColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubteam()));

        presentColumn.setCellValueFactory(
                cellData -> cellData.getValue().presentProperty());

        // Create a cell factory for the present column (checkbox)
        presentColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                // Configure checkbox
                checkBox.setOnAction(event -> {
                    AttendanceViewModel.AttendanceRecord record = getTableView().getItems().get(getIndex());
                    record.setPresent(checkBox.isSelected());

                    // Refresh the table to update time fields
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item != null && item);
                    setGraphic(checkBox);
                }
            }
        });

        // Create time column factories
        arrivalColumn.setCellValueFactory(
                cellData -> cellData.getValue().arrivalTimeProperty());

        departureColumn.setCellValueFactory(
                cellData -> cellData.getValue().departureTimeProperty());

        // Format time cells
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Callback<TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime>, TableCell<AttendanceViewModel.AttendanceRecord, LocalTime>> timeCellFactory = 
                column -> new TableCell<>() {
            private final TextField textField = new TextField();
            
            {
                // Configure text field for editing
                textField.setOnAction(event -> commitEdit(parseTime(textField.getText())));
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(parseTime(textField.getText()));
                    }
                });
            }
            
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);

                AttendanceViewModel.AttendanceRecord record = null;
                if (!empty && getTableRow() != null) {
                    record = (AttendanceViewModel.AttendanceRecord) getTableRow().getItem();
                }

                if (empty || time == null || record == null || !record.isPresent()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(time.format(timeFormatter));
                    setGraphic(null);
                }
            }
            
            @Override
            public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                    setText(null);
                    textField.setText(getItem() != null ? getItem().format(timeFormatter) : "");
                    setGraphic(textField);
                    textField.requestFocus();
                }
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                if (getItem() != null) {
                    setText(getItem().format(timeFormatter));
                } else {
                    setText(null);
                }
                setGraphic(null);
            }
            
            @Override
            public void commitEdit(LocalTime newValue) {
                super.commitEdit(newValue);
                
                if (getTableRow() != null && getTableRow().getItem() != null) {
                    AttendanceViewModel.AttendanceRecord record = 
                            (AttendanceViewModel.AttendanceRecord) getTableRow().getItem();
                    
                    if (getTableColumn() == arrivalColumn) {
                        record.setArrivalTime(newValue);
                    } else if (getTableColumn() == departureColumn) {
                        record.setDepartureTime(newValue);
                    }
                }
            }
            
            private LocalTime parseTime(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                
                try {
                    return LocalTime.parse(text, timeFormatter);
                } catch (DateTimeParseException e) {
                    LOGGER.log(Level.WARNING, "Invalid time format: {0}", text);
                    return null;
                }
            }
        };

        arrivalColumn.setCellFactory(timeCellFactory);
        departureColumn.setCellFactory(timeCellFactory);

        // Make time columns editable
        arrivalColumn.setEditable(true);
        departureColumn.setEditable(true);
        attendanceTable.setEditable(true);

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Bind labels to ViewModel properties
        meetingTitleLabel.textProperty().bind(viewModel.meetingTitleProperty());
        dateLabel.textProperty().bind(viewModel.meetingDateProperty());
        timeLabel.textProperty().bind(viewModel.meetingTimeProperty());
        
        // Bind table items
        attendanceTable.setItems(viewModel.getAttendanceRecords());
        
        // Bind selected record
        attendanceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> viewModel.setSelectedRecord(newValue));
            
        // Bind buttons to commands using ViewModelBinding utility
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveAttendanceCommand());
        cancelButton.setOnAction(event -> closeDialog());
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
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
    
    // Getters for testing purposes
    
    /**
     * Gets the meeting title label.
     * 
     * @return the meeting title label
     */
    public Label getMeetingTitleLabel() {
        return meetingTitleLabel;
    }
    
    /**
     * Gets the date label.
     * 
     * @return the date label
     */
    public Label getDateLabel() {
        return dateLabel;
    }
    
    /**
     * Gets the time label.
     * 
     * @return the time label
     */
    public Label getTimeLabel() {
        return timeLabel;
    }
    
    /**
     * Gets the attendance table.
     * 
     * @return the attendance table
     */
    public TableView<AttendanceViewModel.AttendanceRecord> getAttendanceTable() {
        return attendanceTable;
    }
    
    /**
     * Gets the save button.
     * 
     * @return the save button
     */
    public Button getSaveButton() {
        return saveButton;
    }
    
    /**
     * Gets the cancel button.
     * 
     * @return the cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }
}