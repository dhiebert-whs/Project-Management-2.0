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

        public AttendanceRecord(TeamMember teamMember, Attendance attendance) {
            this.teamMember = teamMember;
            this.attendance = attendance;
            
            if (attendance != null) {
                present.set(attendance.isPresent());
                arrivalTime.set(attendance.getArrivalTime());
                departureTime.set(attendance.getDepartureTime());
            }
            
            // Add listener to set default times when present is checked
            present.addListener((observable, oldValue, newValue) -> {
                if (newValue && arrivalTime.get() == null && teamMember != null) {
                    // Use meeting times as defaults when available
                    Meeting meeting = attendance != null ? attendance.getMeeting() : null;
                    if (meeting != null) {
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

        public String getName() {
            return teamMember != null ? teamMember.getFullName() : "";
        }

        public String getSubteam() {
            return teamMember != null && teamMember.getSubteam() != null 
                ? teamMember.getSubteam().getName() : "";
        }
    }
    
    /**
     * Creates a new AttendanceViewModel with default services.
     */
    public AttendanceViewModel() {
        this(
            ServiceFactory.getAttendanceService(),
            ServiceFactory.getTeamMemberService(),
            ServiceFactory.getMeetingService()
        );
    }
    
    /**
     * Creates a new AttendanceViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param attendanceService the attendance service
     * @param teamMemberService the team member service
     * @param meetingService the meeting service
     */
    public AttendanceViewModel(
            AttendanceService attendanceService,
            TeamMemberService teamMemberService,
            MeetingService meetingService) {
        this.attendanceService = attendanceService;
        this.teamMemberService = teamMemberService;
        this.meetingService = meetingService;
        
        // Create commands
        saveAttendanceCommand = new Command(this::saveAttendance, this::canSaveAttendance);
        cancelCommand = new Command(() -> {});
        loadAttendanceCommand = new Command(this::loadAttendanceData, this::canLoadAttendanceData);
        
        // Set up listeners
        meeting.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateMeetingInfo(newValue);
                loadAttendanceData();
            } else {
                clearMeetingInfo();
                attendanceRecords.clear();
            }
        });
        
        selectedRecord.addListener((observable, oldValue, newValue) -> {
            setDirty(true);
        });
    }
    
    /**
     * Updates the meeting information displayed in the UI.
     * 
     * @param meeting the meeting
     */
    private void updateMeetingInfo(Meeting meeting) {
        meetingTitle.set("Meeting Attendance");
        meetingDate.set(meeting.getDate().toString());
        meetingTime.set(meeting.getStartTime() + " - " + meeting.getEndTime());
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
    private void loadAttendanceData() {
        Meeting currentMeeting = meeting.get();
        if (currentMeeting == null) {
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
                // Find existing attendance record for this member
                Optional<Attendance> attendance = attendances.stream()
                    .filter(a -> a.getMember().getId().equals(member.getId()))
                    .findFirst();
                
                // Create a record for this member
                AttendanceRecord record = new AttendanceRecord(
                    member, 
                    attendance.orElse(null)
                );
                
                attendanceRecords.add(record);
            }
            
            // Clear dirty flag
            setDirty(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance data", e);
            setErrorMessage("Failed to load attendance data: " + e.getMessage());
        }
    }
    
    /**
     * Saves attendance data for all team members.
     */
    private void saveAttendance() {
        Meeting currentMeeting = meeting.get();
        if (currentMeeting == null) {
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
                            true
                        );
                        record.setAttendance(attendance);
                    }
                    
                    // Update times
                    attendance = attendanceService.updateAttendance(
                        attendance.getId(),
                        true,
                        record.getArrivalTime(),
                        record.getDepartureTime()
                    );
                    record.setAttendance(attendance);
                    
                } else if (attendance != null) {
                    // Update existing record to mark as absent
                    attendance = attendanceService.updateAttendance(
                        attendance.getId(),
                        false,
                        null,
                        null
                    );
                    record.setAttendance(attendance);
                }
            }
            
            // Clear dirty flag
            setDirty(false);
            
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
     * @return true if a meeting is selected and there are attendance records, false otherwise
     */
    private boolean canSaveAttendance() {
        return meeting.get() != null && !attendanceRecords.isEmpty();
    }
    
    /**
     * Initializes the ViewModel with a meeting.
     * 
     * @param meeting the meeting for attendance tracking
     */
    public void initWithMeeting(Meeting meeting) {
        this.meeting.set(meeting);
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