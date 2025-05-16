// src/main/java/org/frcpm/mvvm/viewmodels/AttendanceMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.impl.AttendanceServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;

/**
 * ViewModel for the Attendance view using MVVMFx.
 */
public class AttendanceMvvmViewModel extends BaseMvvmViewModel {

    private static final Logger LOGGER = Logger.getLogger(AttendanceMvvmViewModel.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Service dependencies
    private final AttendanceService attendanceService;
    private final AttendanceServiceAsyncImpl attendanceServiceAsync;
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    private final MeetingService meetingService;
    private final MeetingServiceAsyncImpl meetingServiceAsync;

    // Meeting properties
    private final ObjectProperty<Meeting> meeting = new SimpleObjectProperty<>();
    private final StringProperty meetingTitle = new SimpleStringProperty("");
    private final StringProperty meetingDate = new SimpleStringProperty("");
    private final StringProperty meetingTime = new SimpleStringProperty("");

    // Attendance records
    private final ObservableList<AttendanceRecord> attendanceRecords = FXCollections.observableArrayList();
    private final ObjectProperty<AttendanceRecord> selectedRecord = new SimpleObjectProperty<>();

    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    // Commands
    private Command saveAttendanceCommand;
    private Command cancelCommand;
    private Command loadAttendanceCommand;
    private Command setTimeCommand;

    /**
     * Represents an attendance record in the UI.
     */
    public static class AttendanceRecord {
        private final TeamMember teamMember;
        private Attendance attendance;
        private final BooleanProperty present = new SimpleBooleanProperty(false);
        private final ObjectProperty<LocalTime> arrivalTime = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalTime> departureTime = new SimpleObjectProperty<>();
        private Meeting meeting;

        public AttendanceRecord(TeamMember teamMember, Attendance attendance) {
            this.teamMember = teamMember;
            this.attendance = attendance;

            // Extract meeting from attendance if available
            if (attendance != null) {
                this.meeting = attendance.getMeeting();
                present.set(attendance.isPresent());
                arrivalTime.set(attendance.getArrivalTime());
                departureTime.set(attendance.getDepartureTime());
            }

            // Add listener to set default times when present is checked
            present.addListener((observable, oldValue, newValue) -> {
                // If changing to present
                if (Boolean.TRUE.equals(newValue)) {
                    if (meeting != null) {
                        // Always set default times when present
                        arrivalTime.set(meeting.getStartTime());
                        departureTime.set(meeting.getEndTime());
                    }
                }
            });
        }

        // Getters and setters
        public TeamMember getTeamMember() {
            return teamMember;
        }

        public Attendance getAttendance() {
            return attendance;
        }

        public void setAttendance(Attendance attendance) {
            this.attendance = attendance;
            if (attendance != null && attendance.getMeeting() != null) {
                this.meeting = attendance.getMeeting();
            }
        }

        public boolean isPresent() {
            return present.get();
        }

        public BooleanProperty presentProperty() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present.set(present);
        }

        public LocalTime getArrivalTime() {
            return arrivalTime.get();
        }

        public ObjectProperty<LocalTime> arrivalTimeProperty() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalTime arrivalTime) {
            this.arrivalTime.set(arrivalTime);
        }

        public LocalTime getDepartureTime() {
            return departureTime.get();
        }

        public ObjectProperty<LocalTime> departureTimeProperty() {
            return departureTime;
        }

        public void setDepartureTime(LocalTime departureTime) {
            this.departureTime.set(departureTime);
        }

        public Meeting getMeeting() {
            return meeting;
        }

        public void setMeeting(Meeting meeting) {
            this.meeting = meeting;
        }

        public String getName() {
            return teamMember != null ? teamMember.getFullName() : "";
        }

        public String getSubteam() {
            return teamMember != null && teamMember.getSubteam() != null
                    ? teamMember.getSubteam().getName()
                    : "";
        }
    }

    /**
     * Creates a new AttendanceMvvmViewModel.
     * 
     * @param attendanceService the attendance service
     * @param teamMemberService the team member service
     * @param meetingService    the meeting service
     */

    public AttendanceMvvmViewModel(
            AttendanceService attendanceService,
            TeamMemberService teamMemberService,
            MeetingService meetingService) {

        this.attendanceService = attendanceService;
        this.attendanceServiceAsync = (AttendanceServiceAsyncImpl) attendanceService;
        this.teamMemberService = teamMemberService;
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        this.meetingService = meetingService;
        this.meetingServiceAsync = (MeetingServiceAsyncImpl) meetingService;

        initializeCommands();
        setupPropertyListeners();
        validate();
    }

    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save attendance command
        saveAttendanceCommand = createValidAndDirtyCommand(
                this::saveAttendanceAsync,
                this::isValid);

        // Cancel command
        cancelCommand = createValidOnlyCommand(
                () -> {
                    LOGGER.info("Cancel command executed");
                },
                () -> true // Always enabled
        );

        // Load attendance command
        loadAttendanceCommand = createValidOnlyCommand(
                this::loadAttendanceData,
                this::canLoadAttendanceData);

        // Set time command
        setTimeCommand = createValidOnlyCommand(
                this::setSelectedMemberTime,
                this::canSetTime);
    }

    /**
     * Sets up property listeners for this ViewModel.
     */
    private void setupPropertyListeners() {
        // Meeting property listener
        Runnable meetingListener = createDirtyFlagHandler(() -> {
            Meeting newValue = meeting.get();
            if (newValue != null) {
                updateMeetingInfo(newValue);
                loadAttendanceData();
            } else {
                clearMeetingInfo();
                attendanceRecords.clear();
            }
            validate();
        });

        meeting.addListener((observable, oldValue, newValue) -> meetingListener.run());
        trackPropertyListener(meetingListener);

        // Selected record listener
        selectedRecord.addListener((observable, oldValue, newValue) -> {
            validate();
        });
    }

    /**
     * Validates the ViewModel's state.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();

        if (meeting.get() == null) {
            errors.add("No meeting selected");
        }

        valid.set(errors.isEmpty());

        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }

    /**
     * Updates the meeting information displayed in the UI.
     * 
     * @param meeting the meeting
     */
    private void updateMeetingInfo(Meeting meeting) {
        if (meeting == null) {
            return;
        }

        meetingTitle.set("Meeting Attendance");
        meetingDate.set(meeting.getDate() != null ? meeting.getDate().toString() : "");

        LocalTime startTime = meeting.getStartTime();
        LocalTime endTime = meeting.getEndTime();
        String timeString = "";

        if (startTime != null && endTime != null) {
            timeString = formatTime(startTime) + " - " + formatTime(endTime);
        } else if (startTime != null) {
            timeString = formatTime(startTime);
        } else if (endTime != null) {
            timeString = formatTime(endTime);
        }

        meetingTime.set(timeString);
    }

    /**
     * Clears the meeting information displayed in the UI.
     */
    private void clearMeetingInfo() {
        meetingTitle.set("");
        meetingDate.set("");
        meetingTime.set("");
    }

    /**
     * Loads attendance data for the current meeting.
     */
    public void loadAttendanceData() {
        Meeting currentMeeting = meeting.get();
        if (currentMeeting == null) {
            LOGGER.warning("Cannot load attendance data: no meeting selected");
            return;
        }

        loading.set(true);

        attendanceServiceAsync.findByMeetingAsync(currentMeeting,
                // Success callback for attendance records
                attendances -> {
                    // Load all team members
                    teamMemberServiceAsync.findAllAsync(
                            // Success callback for team members
                            teamMembers -> {
                                Platform.runLater(() -> {
                                    try {
                                        // Create attendance records for each team member
                                        attendanceRecords.clear();

                                        for (TeamMember member : teamMembers) {
                                            if (member == null) {
                                                continue;
                                            }

                                            // Find existing attendance record for this member
                                            Optional<Attendance> attendance = attendances.stream()
                                                    .filter(a -> a.getMember() != null &&
                                                            a.getMember().getId().equals(member.getId()))
                                                    .findFirst();

                                            // Create a record for this member
                                            AttendanceRecord record = new AttendanceRecord(
                                                    member,
                                                    attendance.orElse(null));

                                            // Set the meeting for the record
                                            record.setMeeting(currentMeeting);

                                            attendanceRecords.add(record);
                                        }

                                        // Clear dirty flag and error messages
                                        setDirty(false);
                                        clearErrorMessage();
                                    } finally {
                                        loading.set(false);
                                    }
                                });
                            },
                            // Error callback for team members
                            error -> {
                                Platform.runLater(() -> {
                                    LOGGER.log(Level.SEVERE, "Error loading team members", error);
                                    setErrorMessage("Failed to load team members: " + error.getMessage());
                                    loading.set(false);
                                });
                            });
                },
                // Error callback for attendance records
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading attendance data", error);
                        setErrorMessage("Failed to load attendance data: " + error.getMessage());
                        loading.set(false);
                    });
                });
    }

    /**
     * Asynchronously saves attendance data for all team members.
     */
    private void saveAttendanceAsync() {
        Meeting currentMeeting = meeting.get();
        if (currentMeeting == null) {
            LOGGER.warning("Cannot save attendance: no meeting selected");
            setErrorMessage("No meeting selected");
            return;
        }

        if (!isDirty()) {
            LOGGER.fine("No changes to save");
            return;
        }

        loading.set(true);

        // Get present member IDs
        List<Long> presentMemberIds = getPresentMemberIds();

        // Use the async service to record attendance
        attendanceServiceAsync.recordAttendanceForMeetingAsync(
                currentMeeting.getId(),
                presentMemberIds,
                // Success callback
                count -> {
                    Platform.runLater(() -> {
                        LOGGER.info("Updated " + count + " attendance records");
                        setDirty(false);
                        clearErrorMessage();

                        // Reload attendance to get updated records
                        loadAttendanceData();
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving attendance", error);
                        setErrorMessage("Failed to save attendance: " + error.getMessage());
                        loading.set(false);
                    });
                });
    }

    /**
     * Checks if attendance data can be loaded.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canLoadAttendanceData() {
        return meeting.get() != null && !loading.get();
    }

    /**
     * Checks if attendance data can be saved.
     * 
     * @return true if a meeting is selected and there are attendance records, false
     *         otherwise
     */
    private boolean isValid() {
        return meeting.get() != null && !attendanceRecords.isEmpty() && !loading.get();
    }

    /**
     * Initializes the ViewModel with a meeting.
     * 
     * @param meeting the meeting for attendance tracking
     */
    public void initWithMeeting(Meeting meeting) {
        if (meeting == null) {
            LOGGER.warning("Cannot initialize with null meeting");
            return;
        }

        this.meeting.set(meeting);
        clearErrorMessage();
    }

    /**
     * Checks if time can be set for the selected member.
     * 
     * @return true if a member is selected, false otherwise
     */
    private boolean canSetTime() {
        return getSelectedRecord() != null && !loading.get();
    }

    /**
     * Sets the arrival and departure time for the selected member.
     */
    private void setSelectedMemberTime() {
        // This will be called by the view with appropriate times
        // The actual implementation is in updateRecordTimes
    }

    /**
     * Updates a record's arrival and departure times.
     * 
     * @param record        the record to update
     * @param arrivalTime   the new arrival time
     * @param departureTime the new departure time
     */
    public void updateRecordTimes(AttendanceRecord record, LocalTime arrivalTime, LocalTime departureTime) {
        if (record == null) {
            LOGGER.warning("Cannot update times for null record");
            return;
        }

        if (validateTimes(arrivalTime, departureTime)) {
            // Update the record in memory
            record.setArrivalTime(arrivalTime);
            record.setDepartureTime(departureTime);
            record.setPresent(true); // If times are set, record is present

            // Mark viewmodel as dirty
            setDirty(true);

            // If there's an existing attendance record and we want immediate persistence
            Attendance attendance = record.getAttendance();
            if (attendance != null) {
                loading.set(true);

                attendanceServiceAsync.updateAttendanceAsync(
                        attendance.getId(),
                        true, // Set to present since times are being updated
                        arrivalTime,
                        departureTime,
                        // Success callback
                        updatedAttendance -> {
                            Platform.runLater(() -> {
                                record.setAttendance(updatedAttendance);
                                clearErrorMessage();
                                loading.set(false);
                            });
                        },
                        // Error callback
                        error -> {
                            Platform.runLater(() -> {
                                LOGGER.log(Level.SEVERE, "Error updating attendance times", error);
                                setErrorMessage("Failed to update attendance times: " + error.getMessage());
                                loading.set(false);
                            });
                        });
            }
        }
    }

    /**
     * Parses a time string into a LocalTime object.
     * 
     * @param text the time string to parse
     * @return the parsed time, or null if invalid
     */
    public LocalTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(text, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOGGER.log(Level.WARNING, "Invalid time format: {0}", text);
            setErrorMessage("Invalid time format. Please use HH:MM format (e.g., 14:30)");
            return null;
        }
    }

    /**
     * Formats a LocalTime object to a string.
     * 
     * @param time the time to format
     * @return the formatted time string
     */
    public String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * Validates that departure time is after arrival time.
     * 
     * @param arrivalTime   the arrival time
     * @param departureTime the departure time
     * @return true if valid, false otherwise
     */
    private boolean validateTimes(LocalTime arrivalTime, LocalTime departureTime) {
        if (arrivalTime != null && departureTime != null) {
            if (departureTime.isBefore(arrivalTime)) {
                setErrorMessage("Departure time cannot be before arrival time");
                return false;
            }
        }

        return true;
    }

    /**
     * Gets a list of member IDs that are present at the meeting.
     * 
     * @return a list of member IDs
     */
    public List<Long> getPresentMemberIds() {
        List<Long> presentMemberIds = new ArrayList<>();

        for (AttendanceRecord record : attendanceRecords) {
            if (record.isPresent() && record.getTeamMember() != null) {
                presentMemberIds.add(record.getTeamMember().getId());
            }
        }

        return presentMemberIds;
    }

    // Property getters

    public BooleanProperty validProperty() {
        return valid;
    }

    /*
     * public boolean isValid() {
     * return valid.get();
     * }
     */

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public ObjectProperty<Meeting> meetingProperty() {
        return meeting;
    }

    public Meeting getMeeting() {
        return meeting.get();
    }

    public StringProperty meetingTitleProperty() {
        return meetingTitle;
    }

    public String getMeetingTitle() {
        return meetingTitle.get();
    }

    public StringProperty meetingDateProperty() {
        return meetingDate;
    }

    public String getMeetingDate() {
        return meetingDate.get();
    }

    public StringProperty meetingTimeProperty() {
        return meetingTime;
    }

    public String getMeetingTime() {
        return meetingTime.get();
    }

    public ObservableList<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    public ObjectProperty<AttendanceRecord> selectedRecordProperty() {
        return selectedRecord;
    }

    public AttendanceRecord getSelectedRecord() {
        return selectedRecord.get();
    }

    public void setSelectedRecord(AttendanceRecord record) {
        selectedRecord.set(record);
    }

    // Command getters

    public Command getSaveAttendanceCommand() {
        return saveAttendanceCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public Command getLoadAttendanceCommand() {
        return loadAttendanceCommand;
    }

    public Command getSetTimeCommand() {
        return setTimeCommand;
    }

    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        attendanceRecords.clear();
    }
}