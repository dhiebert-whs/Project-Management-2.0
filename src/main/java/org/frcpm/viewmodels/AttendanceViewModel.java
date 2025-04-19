package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TeamMemberService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for attendance management in the FRC Project Management System.
 * Follows the MVVM pattern to handle attendance tracking functionality.
 */
public class AttendanceViewModel extends BaseViewModel {

    private static final Logger LOGGER = Logger.getLogger(AttendanceViewModel.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Services
    private final AttendanceService attendanceService;
    private final TeamMemberService teamMemberService;
    private final MeetingService meetingService;

    // Meeting properties
    private final ObjectProperty<Meeting> meeting = new SimpleObjectProperty<>();
    private final StringProperty meetingTitle = new SimpleStringProperty("");
    private final StringProperty meetingDate = new SimpleStringProperty("");
    private final StringProperty meetingTime = new SimpleStringProperty("");

    // Attendance records
    private final ObservableList<AttendanceRecord> attendanceRecords = FXCollections.observableArrayList();
    private final ObjectProperty<AttendanceRecord> selectedRecord = new SimpleObjectProperty<>();

    // Validation
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    // Commands
    private final Command saveAttendanceCommand;
    private final Command cancelCommand;
    private final Command loadAttendanceCommand;

    /**
     * Represents an attendance record in the UI.
     */
    public static class AttendanceRecord {
        private final TeamMember teamMember;
        private Attendance attendance;
        private final BooleanProperty present = new SimpleBooleanProperty(false);
        private final ObjectProperty<LocalTime> arrivalTime = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalTime> departureTime = new SimpleObjectProperty<>();
        private Meeting meeting;  // Not final anymore to support direct setting

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
     * Creates a new AttendanceViewModel with default services.
     */
    public AttendanceViewModel() {
        this(
                ServiceFactory.getAttendanceService(),
                ServiceFactory.getTeamMemberService(),
                ServiceFactory.getMeetingService());
    }

    /**
     * Creates a new AttendanceViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param attendanceService the attendance service
     * @param teamMemberService the team member service
     * @param meetingService    the meeting service
     */
    public AttendanceViewModel(
            AttendanceService attendanceService,
            TeamMemberService teamMemberService,
            MeetingService meetingService) {
        
        // Validate services
        if (attendanceService == null || teamMemberService == null || meetingService == null) {
            throw new IllegalArgumentException("Services cannot be null");
        }
        
        this.attendanceService = attendanceService;
        this.teamMemberService = teamMemberService;
        this.meetingService = meetingService;

        // Create commands using BaseViewModel utility methods
        saveAttendanceCommand = createValidOnlyCommand(this::saveAttendance, this::canSaveAttendance);
        cancelCommand = new Command(() -> {
            // Cancel operation
        });
        loadAttendanceCommand = createValidOnlyCommand(this::loadAttendanceData, this::canLoadAttendanceData);

        // Set up listeners
        setupPropertyListeners();
        
        // Initial validation
        validate();
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
        Runnable selectedRecordListener = createDirtyFlagHandler(this::validate);
        selectedRecord.addListener((observable, oldValue, newValue) -> selectedRecordListener.run());
        trackPropertyListener(selectedRecordListener);
    }
    
    /**
     * Validates the ViewModel's state.
     */
    public void validate() {
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
            timeString = startTime + " - " + endTime;
        } else if (startTime != null) {
            timeString = startTime.toString();
        } else if (endTime != null) {
            timeString = endTime.toString();
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

        try {
            // Load team members and attendance records
            List<TeamMember> teamMembers = teamMemberService.findAll();
            List<Attendance> attendances = attendanceService.findByMeeting(currentMeeting);

            // Clear previous records
            attendanceRecords.clear();

            // Create attendance records for each team member
            for (TeamMember member : teamMembers) {
                if (member == null) {
                    continue;
                }
                
                // Find existing attendance record for this member
                Optional<Attendance> attendance = attendances.stream()
                        .filter(a -> a.getMember() != null && a.getMember().getId().equals(member.getId()))
                        .findFirst();

                // Create a record for this member
                AttendanceRecord record = new AttendanceRecord(
                        member,
                        attendance.orElse(null));

                attendanceRecords.add(record);
            }

            // Clear dirty flag and error messages
            setDirty(false);
            clearErrorMessage();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance data", e);
            setErrorMessage("Failed to load attendance data: " + e.getMessage());
        }
    }

    /**
     * Saves attendance data for all team members.
     */
    public void saveAttendance() {
        Meeting currentMeeting = meeting.get();
        if (currentMeeting == null) {
            LOGGER.warning("Cannot save attendance: no meeting selected");
            return;
        }

        try {
            // Process each attendance record
            for (AttendanceRecord record : attendanceRecords) {
                TeamMember member = record.getTeamMember();
                boolean present = record.isPresent();

                if (member == null) {
                    continue;
                }

                Attendance attendance = record.getAttendance();

                if (present) {
                    // Create or update the attendance record
                    if (attendance == null) {
                        // Create new attendance
                        attendance = attendanceService.createAttendance(
                                currentMeeting.getId(),
                                member.getId(),
                                true);
                        record.setAttendance(attendance);
                    }

                    // Update times
                    attendance = attendanceService.updateAttendance(
                            attendance.getId(),
                            true,
                            record.getArrivalTime(),
                            record.getDepartureTime());
                    record.setAttendance(attendance);

                } else if (attendance != null) {
                    // Update existing record to mark as absent
                    attendance = attendanceService.updateAttendance(
                            attendance.getId(),
                            false,
                            null,
                            null);
                    record.setAttendance(attendance);
                }
            }

            // Clear dirty flag and error message
            setDirty(false);
            clearErrorMessage();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving attendance data", e);
            setErrorMessage("Failed to save attendance data: " + e.getMessage());
        }
    }

    /**
     * Checks if attendance data can be loaded.
     * 
     * @return true if a meeting is selected, false otherwise
     */
    private boolean canLoadAttendanceData() {
        return meeting.get() != null;
    }

    /**
     * Checks if attendance data can be saved.
     * 
     * @return true if a meeting is selected and there are attendance records, false
     *         otherwise
     */
    private boolean canSaveAttendance() {
        return meeting.get() != null && !attendanceRecords.isEmpty() && isDirty();
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
            record.setArrivalTime(arrivalTime);
            record.setDepartureTime(departureTime);
            setDirty(true);
            clearErrorMessage();
        }
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
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
    }

    // Property getters
    
    public BooleanProperty validProperty() {
        return valid;
    }
    
    public boolean isValid() {
        return valid.get();
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
}