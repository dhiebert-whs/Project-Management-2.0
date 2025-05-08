// src/main/java/org/frcpm/viewmodels/AttendanceAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.services.impl.AttendanceServiceAsyncImpl;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous ViewModel for attendance management.
 */
public class AttendanceAsyncViewModel extends AttendanceViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceAsyncViewModel.class.getName());
    
    // Async services
    private final AttendanceServiceAsyncImpl attendanceServiceAsync;
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Async commands
    private Command asyncLoadMeetingsCommand;
    private Command asyncLoadTeamMembersCommand;
    private Command asyncLoadAttendanceCommand;
    private Command asyncSaveAttendanceCommand;
    private Command asyncGenerateReportCommand;
    
    /**
     * Creates a new AttendanceAsyncViewModel with the specified async services.
     * 
     * @param attendanceServiceAsync the async attendance service
     * @param meetingServiceAsync the async meeting service
     * @param teamMemberServiceAsync the async team member service
     */
    public AttendanceAsyncViewModel(AttendanceServiceAsyncImpl attendanceServiceAsync,
                                 MeetingServiceAsyncImpl meetingServiceAsync,
                                 TeamMemberServiceAsyncImpl teamMemberServiceAsync) {
        super(null, null, null); // Initialize parent with null services
        
        this.attendanceServiceAsync = attendanceServiceAsync;
        this.meetingServiceAsync = meetingServiceAsync;
        this.teamMemberServiceAsync = teamMemberServiceAsync;
        
        // Initialize async commands
        initAsyncCommands();
    }
    
    /**
     * Initializes async commands.
     */
    private void initAsyncCommands() {
        asyncLoadMeetingsCommand = new Command(
            this::loadMeetingsAsync,
            () -> !loading.get()
        );
        
        asyncLoadTeamMembersCommand = new Command(
            this::loadTeamMembersAsync,
            () -> !loading.get()
        );
        
        asyncLoadAttendanceCommand = new Command(
            this::loadAttendanceAsync,
            () -> getMeeting() != null && !loading.get()
        );
        
        asyncSaveAttendanceCommand = new Command(
            this::saveAttendanceAsync,
            () -> getMeeting() != null && !loading.get() && isDirty()
        );
        
        asyncGenerateReportCommand = new Command(
            this::generateReportAsync,
            () -> !loading.get()
        );
    }
    
    /**
     * Asynchronously loads meetings.
     */
    public void loadMeetingsAsync() {
        try {
            loading.set(true);
            
            // Get a date range (e.g., today and the next 30 days)
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(30);
            
            // Since we're using a hypothetical method that may vary in implementation,
            // we'll use a flexible approach here
            meetingServiceAsync.findAllAsync(
                meetings -> {
                    Platform.runLater(() -> {
                        ObservableList<Meeting> meetingsList = FXCollections.observableArrayList(meetings);
                        setMeetings(meetingsList);
                        loading.set(false);
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading meetings", error);
                        setErrorMessage("Failed to load meetings: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading meetings", e);
            setErrorMessage("Failed to load meetings: " + e.getMessage());
        }
    }
    
    /**
     * Asynchronously loads team members.
     */
    public void loadTeamMembersAsync() {
        try {
            loading.set(true);
            
            teamMemberServiceAsync.findAllAsync(
                teamMembers -> {
                    Platform.runLater(() -> {
                        ObservableList<TeamMember> teamMembersList = FXCollections.observableArrayList(teamMembers);
                        setTeamMembers(teamMembersList);
                        loading.set(false);
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading team members", error);
                        setErrorMessage("Failed to load team members: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading team members", e);
            setErrorMessage("Failed to load team members: " + e.getMessage());
        }
    }
    

    /**
     * Asynchronously loads attendance records for the selected meeting.
     */
    public void loadAttendanceAsync() {
        try {
            Meeting selectedMeeting = getMeeting();
            if (selectedMeeting == null) {
                setErrorMessage("No meeting selected");
                return;
            }
            
            loading.set(true);
            
            attendanceServiceAsync.findByMeetingAsync(selectedMeeting,
                attendanceList -> {
                    Platform.runLater(() -> {
                        // First, load all team members to ensure we have complete records
                        teamMemberServiceAsync.findAllAsync(
                            teamMembers -> {
                                ObservableList<AttendanceRecord> records = FXCollections.observableArrayList();
                                
                                // Create attendance records for each team member
                                for (TeamMember member : teamMembers) {
                                    // Find matching attendance record
                                    Attendance attendance = attendanceList.stream()
                                        .filter(a -> a.getMember() != null && a.getMember().getId().equals(member.getId()))
                                        .findFirst()
                                        .orElse(null);
                                    
                                    AttendanceRecord record = new AttendanceRecord(member, attendance);
                                    records.add(record);
                                }
                                
                                // Update UI with records
                                Platform.runLater(() -> {
                                    // Use reflection to access the protected method in the parent class
                                    try {
                                        java.lang.reflect.Method method = AttendanceViewModel.class.getDeclaredMethod(
                                            "getAttendanceRecords");
                                        method.setAccessible(true);
                                        ObservableList<AttendanceRecord> attendanceRecords = 
                                            (ObservableList<AttendanceRecord>) method.invoke(this);
                                        attendanceRecords.clear();
                                        attendanceRecords.addAll(records);
                                    } catch (Exception e) {
                                        LOGGER.log(Level.SEVERE, "Error updating attendance records", e);
                                    }
                                    
                                    loading.set(false);
                                });
                            },
                            error -> {
                                Platform.runLater(() -> {
                                    LOGGER.log(Level.SEVERE, "Error loading team members for attendance", error);
                                    setErrorMessage("Failed to load team members: " + error.getMessage());
                                    loading.set(false);
                                });
                            }
                        );
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading attendance", error);
                        setErrorMessage("Failed to load attendance: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading attendance", e);
            setErrorMessage("Failed to load attendance: " + e.getMessage());
        }
    }
    
    /**
     * Asynchronously saves attendance for the current meeting.
     */
    public void saveAttendanceAsync() {
        try {
            Meeting selectedMeeting = getMeeting();
            if (selectedMeeting == null) {
                setErrorMessage("No meeting selected");
                return;
            }
            
            loading.set(true);
            
            // Get present member IDs
            List<Long> presentMemberIds = getPresentMemberIds();
            
            // Use the async service to record attendance
            attendanceServiceAsync.recordAttendanceForMeetingAsync(
                selectedMeeting.getId(),
                presentMemberIds,
                count -> {
                    Platform.runLater(() -> {
                        LOGGER.info("Updated " + count + " attendance records");
                        setDirty(false);
                        clearErrorMessage();
                        loading.set(false);
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving attendance", error);
                        setErrorMessage("Failed to save attendance: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error saving attendance", e);
            setErrorMessage("Failed to save attendance: " + e.getMessage());
        }
    }
    
    /**
     * Called to save attendance records.
     * Override parent method to use async implementation.
     */
    public boolean saveMeetingAttendance() {
        saveAttendanceAsync();
        return true; // Indicates that the save operation was initiated
    }
    
    /**
     * Asynchronously generates an attendance report.
     */
    public void generateReportAsync() {
        // Implementation would depend on the specific report generation needs
        // This is a placeholder
        try {
            loading.set(true);
            
            // Get report parameters - this would depend on the application's needs
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            
            // Example report generation
            attendanceServiceAsync.getAttendanceReportAsync(startDate, endDate,
                reportData -> {
                    Platform.runLater(() -> {
                        LOGGER.info("Generated report with " + reportData.size() + " records");
                        // Process report data
                        loading.set(false);
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error generating report", error);
                        setErrorMessage("Failed to generate report: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            setErrorMessage("Failed to generate report: " + e.getMessage());
        }
    }
    
    /**
     * Updates a record's arrival and departure times asynchronously.
     * 
     * @param record the record to update
     * @param arrivalTime the new arrival time
     * @param departureTime the new departure time
     */
    public void updateRecordTimesAsync(AttendanceRecord record, LocalTime arrivalTime, LocalTime departureTime) {
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
                    updatedAttendance -> {
                        Platform.runLater(() -> {
                            record.setAttendance(updatedAttendance);
                            clearErrorMessage();
                            loading.set(false);
                        });
                    },
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating attendance times", error);
                            setErrorMessage("Failed to update attendance times: " + error.getMessage());
                            loading.set(false);
                        });
                    }
                );
            }
        }
    }
    
    /**
     * Validates that departure time is after arrival time.
     * 
     * @param arrivalTime the arrival time
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
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the ViewModel is currently loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the async load meetings command.
     * 
     * @return the async load meetings command
     */
    public Command getAsyncLoadMeetingsCommand() {
        return asyncLoadMeetingsCommand;
    }
    
    /**
     * Gets the async load team members command.
     * 
     * @return the async load team members command
     */
    public Command getAsyncLoadTeamMembersCommand() {
        return asyncLoadTeamMembersCommand;
    }
    
    /**
     * Gets the async load attendance command.
     * 
     * @return the async load attendance command
     */
    public Command getAsyncLoadAttendanceCommand() {
        return asyncLoadAttendanceCommand;
    }
    
    /**
     * Gets the async save attendance command.
     * 
     * @return the async save attendance command
     */
    public Command getAsyncSaveAttendanceCommand() {
        return asyncSaveAttendanceCommand;
    }
    
    /**
     * Gets the async generate report command.
     * 
     * @return the async generate report command
     */
    public Command getAsyncGenerateReportCommand() {
        return asyncGenerateReportCommand;
    }
    
    // Helper methods
    
    /**
     * Sets the team members in the viewmodel.
     * 
     * @param teamMembers the team members to set
     */
    private void setTeamMembers(ObservableList<TeamMember> teamMembers) {
        // Implementation depends on how the parent class manages team members
        // This would typically use reflection or another mechanism to set the field
        try {
            java.lang.reflect.Field field = AttendanceViewModel.class.getDeclaredField("teamMembers");
            field.setAccessible(true);
            field.set(this, teamMembers);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting team members", e);
        }
    }
    
    /**
     * Sets the meetings in the viewmodel.
     * 
     * @param meetings the meetings to set
     */
    private void setMeetings(ObservableList<Meeting> meetings) {
        // Implementation depends on how the parent class manages meetings
        // This would typically use reflection or another mechanism to set the field
        try {
            java.lang.reflect.Field field = AttendanceViewModel.class.getDeclaredField("meetings");
            field.setAccessible(true);
            field.set(this, meetings);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting meetings", e);
        }
    }
    
    // Override parent methods to use async implementations

    @Override
    public void loadAttendanceData() {
        loadAttendanceAsync();
    }
    
    @Override
    public void updateRecordTimes(AttendanceRecord record, LocalTime arrivalTime, LocalTime departureTime) {
        updateRecordTimesAsync(record, arrivalTime, departureTime);
    }
    
    @Override
    public void initWithMeeting(Meeting meeting) {
        super.initWithMeeting(meeting);
        // After setting the meeting, load attendance asynchronously
        if (meeting != null) {
            loadAttendanceAsync();
        }
    }
}