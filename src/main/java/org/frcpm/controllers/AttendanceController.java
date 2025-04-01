package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TeamMemberService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for attendance tracking functionality.
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
    private TableView<TeamMemberAttendanceRecord> attendanceTable;

    @FXML
    private TableColumn<TeamMemberAttendanceRecord, String> nameColumn;

    @FXML
    private TableColumn<TeamMemberAttendanceRecord, String> subteamColumn;

    @FXML
    private TableColumn<TeamMemberAttendanceRecord, Boolean> presentColumn;

    @FXML
    private TableColumn<TeamMemberAttendanceRecord, LocalTime> arrivalColumn;

    @FXML
    private TableColumn<TeamMemberAttendanceRecord, LocalTime> departureColumn;

    @FXML
    private TextField arrivalTimeField;

    @FXML
    private TextField departureTimeField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private final MeetingService meetingService = ServiceFactory.getMeetingService();
    private final TeamMemberService teamMemberService = ServiceFactory.getTeamMemberService();
    private final AttendanceService attendanceService = ServiceFactory.getAttendanceService();

    private Meeting meeting;
    private ObservableList<TeamMemberAttendanceRecord> attendanceRecords = FXCollections.observableArrayList();

    /**
     * Helper class to represent attendance data in the UI.
     */
    public static class TeamMemberAttendanceRecord {
        private final TeamMember teamMember;
        private final Attendance attendance;
        private boolean present;
        private LocalTime arrivalTime;
        private LocalTime departureTime;

        public TeamMemberAttendanceRecord(TeamMember teamMember, Attendance attendance) {
            this.teamMember = teamMember;
            this.attendance = attendance;
            this.present = attendance != null && attendance.isPresent();
            this.arrivalTime = attendance != null ? attendance.getArrivalTime() : null;
            this.departureTime = attendance != null ? attendance.getDepartureTime() : null;
        }

        public TeamMember getTeamMember() {
            return teamMember;
        }

        public Attendance getAttendance() {
            return attendance;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }

        public LocalTime getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public LocalTime getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(LocalTime departureTime) {
            this.departureTime = departureTime;
        }

        public String getName() {
            return teamMember.getFullName();
        }

        public String getSubteam() {
            return teamMember.getSubteam() != null ? teamMember.getSubteam().getName() : "";
        }
    }

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
                cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isPresent()));

        // Create a cell factory for the present column (checkbox)
        presentColumn.setCellFactory(column -> new TableCell<TeamMemberAttendanceRecord, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                // Configure checkbox
                checkBox.setOnAction(event -> {
                    TeamMemberAttendanceRecord record = getTableView().getItems().get(getIndex());
                    record.setPresent(checkBox.isSelected());

                    // If checked, set default times
                    if (checkBox.isSelected() && record.getArrivalTime() == null) {
                        record.setArrivalTime(meeting.getStartTime());
                        record.setDepartureTime(meeting.getEndTime());
                    }

                    // Refresh the table to show/hide time fields
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

        // Create time column cell factories
        arrivalColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getArrivalTime()));

        departureColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDepartureTime()));

        // Format time cells
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Callback<TableColumn<TeamMemberAttendanceRecord, LocalTime>, TableCell<TeamMemberAttendanceRecord, LocalTime>> timeCellFactory = column -> new TableCell<TeamMemberAttendanceRecord, LocalTime>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);

                TeamMemberAttendanceRecord record = null;
                if (!empty && getTableRow() != null) {
                    record = (TeamMemberAttendanceRecord) getTableRow().getItem();
                }

                if (empty || time == null || record == null || !record.isPresent()) {
                    setText(null);
                } else {
                    setText(time.format(timeFormatter));
                }
            }
        };

        arrivalColumn.setCellFactory(timeCellFactory);
        departureColumn.setCellFactory(timeCellFactory);

        // Make time columns editable
        arrivalColumn.setEditable(true);
        departureColumn.setEditable(true);

        // Set up button handlers
        saveButton.setOnAction(this::handleSave);
        cancelButton.setOnAction(this::handleCancel);

        // Set table items
        attendanceTable.setItems(attendanceRecords);
    }

    /**
     * Sets the meeting for attendance tracking.
     * 
     * @param meeting the meeting
     */
    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
        loadAttendanceData();
    }

    /**
     * Loads attendance data for the meeting.
     */
    private void loadAttendanceData() {
        if (meeting == null) {
            return;
        }

        // Update meeting information
        meetingTitleLabel.setText("Meeting Attendance");
        dateLabel.setText(meeting.getDate().toString());
        timeLabel.setText(meeting.getStartTime() + " - " + meeting.getEndTime());

        // Load team members and attendance records
        List<TeamMember> teamMembers = teamMemberService.findAll();
        List<Attendance> attendances = attendanceService.findByMeeting(meeting);

        // Clear previous records
        attendanceRecords.clear();

        // Create attendance records for each team member
        for (TeamMember member : teamMembers) {
            // Find existing attendance record for this member
            Attendance attendance = null;
            for (Attendance a : attendances) {
                if (a.getMember().getId().equals(member.getId())) {
                    attendance = a;
                    break;
                }
            }

            // Create a record for this member
            TeamMemberAttendanceRecord record = new TeamMemberAttendanceRecord(member, attendance);
            attendanceRecords.add(record);
        }
    }

    /**
     * Handles saving attendance data.
     * 
     * @param event the action event
     */
    private void handleSave(ActionEvent event) {
        if (meeting == null) {
            return;
        }

        try {
            // Create a list of IDs for present members
            List<Long> presentMemberIds = new ArrayList<>();

            // Process each attendance record
            for (TeamMemberAttendanceRecord record : attendanceRecords) {
                Long memberId = record.getTeamMember().getId();
                boolean present = record.isPresent();

                if (present) {
                    presentMemberIds.add(memberId);

                    // Create or update the attendance record
                    Attendance attendance = record.getAttendance();
                    if (attendance == null) {
                        // Create new attendance
                        attendance = attendanceService.createAttendance(
                                meeting.getId(), memberId, true);
                    }

                    // Update times
                    attendanceService.updateAttendance(
                            attendance.getId(), true,
                            record.getArrivalTime(),
                            record.getDepartureTime());
                } else if (record.getAttendance() != null) {
                    // Update existing record to mark as absent
                    attendanceService.updateAttendance(
                            record.getAttendance().getId(), false, null, null);
                }
            }

            // Show success message
            showInfoAlert("Attendance Saved", "Attendance data has been saved successfully.");

            // Close the dialog
            closeDialog();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving attendance data", e);
            showErrorAlert("Error", "Failed to save attendance data: " + e.getMessage());
        }
    }

    /**
     * Handles canceling attendance tracking.
     * 
     * @param event the action event
     */
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        // Get the current stage
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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

    // getters for testing
    /**
     * Gets the meeting title label.
     * 
     * @return the meeting title label
     */
    public Label getMeetingTitleLabel() {
        return meetingTitleLabel;
    }

    /**
     * Gets the current meeting.
     * 
     * @return the meeting
     */
    public Meeting getMeeting() {
        return meeting;
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
    public TableView<TeamMemberAttendanceRecord> getAttendanceTable() {
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

    /**
     * Gets the attendance records.
     * 
     * @return the attendance records
     */
    public ObservableList<TeamMemberAttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    /**
     * Gets the name column.
     * 
     * @return the name column
     */
    public TableColumn<TeamMemberAttendanceRecord, String> getNameColumn() {
        return nameColumn;
    }

    /**
     * Gets the subteam column.
     * 
     * @return the subteam column
     */
    public TableColumn<TeamMemberAttendanceRecord, String> getSubteamColumn() {
        return subteamColumn;
    }

    /**
     * Gets the present column.
     * 
     * @return the present column
     */
    public TableColumn<TeamMemberAttendanceRecord, Boolean> getPresentColumn() {
        return presentColumn;
    }

    /**
     * Gets the arrival column.
     * 
     * @return the arrival column
     */
    public TableColumn<TeamMemberAttendanceRecord, LocalTime> getArrivalColumn() {
        return arrivalColumn;
    }

    /**
     * Gets the departure column.
     * 
     * @return the departure column
     */
    public TableColumn<TeamMemberAttendanceRecord, LocalTime> getDepartureColumn() {
        return departureColumn;
    }

    /**
     * Gets the arrival time field.
     * 
     * @return the arrival time field
     */
    public TextField getArrivalTimeField() {
        return arrivalTimeField;
    }

    /**
     * Gets the departure time field.
     * 
     * @return the departure time field
     */
    public TextField getDepartureTimeField() {
        return departureTimeField;
    }

    /**
     * Public method to access handleSave for testing.
     * 
     * @param event the action event
     */
    public void testHandleSave(ActionEvent event) {
        handleSave(event);
    }

    /**
     * Public method to access handleCancel for testing.
     * 
     * @param event the action event
     */
    public void testHandleCancel(ActionEvent event) {
        handleCancel(event);
    }

}