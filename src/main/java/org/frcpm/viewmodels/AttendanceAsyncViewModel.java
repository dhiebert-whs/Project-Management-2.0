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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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
            () -> getSelectedMeeting() != null && !loading.get()
        );
        
        asyncSaveAttendanceCommand = new Command(
            this::saveAttendanceAsync,
            () -> getSelectedMeeting() != null && getSelectedTeamMember() != null && !loading.get()
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
            
            meetingServiceAsync.findByDateBetweenAsync(today, endDate,
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
                result -> {
                    Platform.runLater(() -> {
                        ObservableList<TeamMember> teamMembersList = FXCollections.observableArrayList(result);
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
            Meeting selectedMeeting = getSelectedMeeting();
            if (selectedMeeting == null) {
                setErrorMessage("No meeting selected");
                return;
            }
            
            loading.set(true);
            
            attendanceServiceAsync.findByMeetingAsync(selectedMeeting,
                attendanceList -> {
                    Platform.runLater(() -> {
                        ObservableList<Attendance> attendanceRecords = FXCollections.observableArrayList(attendanceList);
                        setAttendanceRecords(attendanceRecords);
                        loading.set(false);
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
     * Asynchronously saves attendance for the selected meeting and team member.
     */
    public void saveAttendanceAsync() {
        try {
            Meeting selectedMeeting = getSelectedMeeting();
            TeamMember selectedTeamMember = getSelectedTeamMember();
            
            if (selectedMeeting == null || selectedTeamMember == null) {
                setErrorMessage("Meeting and team member must be selected");
                return;
            }
            
            loading.set(true);
            
            // Get attendance status and notes
            boolean isPresent = isAttendancePresent();
            String notes = getAttendanceNotes();
            
            attendanceServiceAsync.recordAttendanceAsync(
                selectedMeeting.getId(),
                selectedTeamMember.getId(),
                isPresent,
                notes,
                savedAttendance -> {
                    Platform.runLater(() -> {
                        // Reload attendance after saving
                        loadAttendanceAsync();
                        setDirty(false);
                        clearErrorMessage();
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
     * Asynchronously generates an attendance report.
     */
    public void generateReportAsync() {
        try {
            LocalDate startDate = getReportStartDate();
            LocalDate endDate = getReportEndDate();
            
            if (startDate == null || endDate == null) {
                setErrorMessage("Start and end dates must be set");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                setErrorMessage("Start date cannot be after end date");
                return;
            }
            
            loading.set(true);
            
            attendanceServiceAsync.getAttendanceReportAsync(startDate, endDate,
                reportData -> {
                    Platform.runLater(() -> {
                        ObservableList<Attendance> reportRecords = FXCollections.observableArrayList(reportData);
                        setReportData(reportRecords);
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
    
    // Override parent methods to use async implementations
    
    @Override
    public void loadMeetings() {
        loadMeetingsAsync();
    }
    
    @Override
    public void loadTeamMembers() {
        loadTeamMembersAsync();
    }
    
    @Override
    public void loadAttendance() {
        loadAttendanceAsync();
    }
    
    @Override
    protected void saveAttendance() {
        saveAttendanceAsync();
    }
    
    @Override
    public void generateReport() {
        generateReportAsync();
    }
}