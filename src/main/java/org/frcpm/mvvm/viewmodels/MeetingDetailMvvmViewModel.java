// src/main/java/org/frcpm/mvvm/viewmodels/MeetingDetailMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.services.MeetingService;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;

/**
 * ViewModel for the MeetingDetail view using MVVMFx.
 */
public class MeetingDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingDetailMvvmViewModel.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Service dependencies
    private final MeetingService meetingService;
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    
    // Observable properties for meeting fields
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty startTimeString = new SimpleStringProperty("");
    private final StringProperty endTimeString = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final BooleanProperty isNewMeeting = new SimpleBooleanProperty(true);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Model reference
    private final ObjectProperty<Meeting> meeting = new SimpleObjectProperty<>();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    
    /**
     * Creates a new MeetingDetailMvvmViewModel.
     * 
     * @param meetingService the meeting service
     */
    
    public MeetingDetailMvvmViewModel(MeetingService meetingService) {
        this.meetingService = meetingService;
        this.meetingServiceAsync = (MeetingServiceAsyncImpl) meetingService;
        
        initializeCommands();
        setupValidation();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save command
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        
        // Cancel command - will be implemented by the view to close dialog
        cancelCommand = createValidOnlyCommand(
            () -> {
                LOGGER.info("Cancel command executed");
            },
            () -> true // Always enabled
        );
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        date.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        startTimeString.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        endTimeString.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that only affect dirty state
        notes.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the meeting data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (date.get() == null) {
            errors.add("Meeting date cannot be empty");
        }
        
        // Start time validation
        if (startTimeString.get() == null || startTimeString.get().trim().isEmpty()) {
            errors.add("Start time is required");
        } else {
            try {
                LocalTime.parse(startTimeString.get(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Start time format is invalid");
            }
        }
        
        // End time validation
        if (endTimeString.get() == null || endTimeString.get().trim().isEmpty()) {
            errors.add("End time is required");
        } else {
            try {
                LocalTime.parse(endTimeString.get(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("End time format is invalid");
            }
        }
        
        // Time comparison
        try {
            LocalTime startTime = LocalTime.parse(startTimeString.get(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(endTimeString.get(), TIME_FORMATTER);
            
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                errors.add("End time must be after start time");
            }
        } catch (Exception e) {
            // Ignore parsing exceptions here as they are already handled above
        }
        
        // Update validation state
        valid.set(errors.isEmpty());
        
        // Set error message
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Initializes the view model for a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void initNewMeeting(Project project) {
        if (project == null) {
            LOGGER.warning("Initializing meeting with null project");
        }
        
        this.project.set(project);
        this.meeting.set(null);
        isNewMeeting.set(true);
        
        // Default values
        date.set(LocalDate.now());
        startTimeString.set("16:00");
        endTimeString.set("18:00");
        notes.set("");
        
        // Reset state
        setDirty(false);
        clearErrorMessage();
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void initExistingMeeting(Meeting meeting) {
        if (meeting == null) {
            LOGGER.warning("Initializing with null meeting - treating as new meeting");
            initNewMeeting(null);
            return;
        }
        
        this.meeting.set(meeting);
        this.project.set(meeting.getProject());
        isNewMeeting.set(false);
        
        // Set properties from meeting
        date.set(meeting.getDate());
        startTimeString.set(formatTime(meeting.getStartTime()));
        endTimeString.set(formatTime(meeting.getEndTime()));
        notes.set(meeting.getNotes() != null ? meeting.getNotes() : "");
        
        // Reset state
        setDirty(false);
        clearErrorMessage();
        validate();
    }
    
    /**
     * Saves the meeting.
     */
    private void save() {
        if (!valid.get() || !isDirty() || loading.get()) {
            return;
        }
        
        loading.set(true);
        
        try {
            // Parse times
            LocalTime startTime = parseTime(startTimeString.get());
            LocalTime endTime = parseTime(endTimeString.get());
            
            if (startTime == null || endTime == null) {
                setErrorMessage("Invalid time format");
                loading.set(false);
                return;
            }
            
            // Update or create meeting asynchronously
            if (isNewMeeting.get()) {
                // Create new meeting
                Project currentProject = project.get();
                if (currentProject == null || currentProject.getId() == null) {
                    setErrorMessage("Cannot save meeting - project is invalid");
                    loading.set(false);
                    return;
                }
                
                meetingServiceAsync.createMeetingAsync(
                    date.get(),
                    startTime,
                    endTime,
                    currentProject.getId(),
                    notes.get(),
                    // Success handler
                    savedMeeting -> {
                        Platform.runLater(() -> {
                            meeting.set(savedMeeting);
                            isNewMeeting.set(false);
                            setDirty(false);
                            clearErrorMessage();
                            loading.set(false);
                            LOGGER.info("Meeting created successfully: " + savedMeeting.getId());
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating meeting", error);
                            setErrorMessage("Failed to create meeting: " + error.getMessage());
                            loading.set(false);
                        });
                    }
                );
            } else {
                // Update existing meeting
                Meeting existingMeeting = meeting.get();
                if (existingMeeting == null || existingMeeting.getId() == null) {
                    setErrorMessage("Cannot update meeting - meeting is invalid");
                    loading.set(false);
                    return;
                }
                
                // First update date and time
                meetingServiceAsync.updateMeetingDateTimeAsync(
                    existingMeeting.getId(),
                    date.get(),
                    startTime,
                    endTime,
                    // Success handler for date/time update
                    updatedMeeting -> {
                        // Then update notes
                        meetingServiceAsync.updateNotesAsync(
                            existingMeeting.getId(),
                            notes.get(),
                            // Success handler for notes update
                            finalMeeting -> {
                                Platform.runLater(() -> {
                                    meeting.set(finalMeeting);
                                    setDirty(false);
                                    clearErrorMessage();
                                    loading.set(false);
                                    LOGGER.info("Meeting updated successfully: " + finalMeeting.getId());
                                });
                            },
                            // Error handler for notes update
                            error -> {
                                Platform.runLater(() -> {
                                    LOGGER.log(Level.SEVERE, "Error updating meeting notes", error);
                                    setErrorMessage("Failed to update meeting notes: " + error.getMessage());
                                    loading.set(false);
                                });
                            }
                        );
                    },
                    // Error handler for date/time update
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating meeting date/time", error);
                            setErrorMessage("Failed to update meeting date/time: " + error.getMessage());
                            loading.set(false);
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save meeting: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to parse time from string.
     * 
     * @param timeStr the time string (HH:MM)
     * @return the parsed LocalTime or null if invalid
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Helper method to format time as string.
     * 
     * @param time the LocalTime
     * @return the formatted time string (HH:MM)
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return TIME_FORMATTER.format(time);
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the meeting is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }
    
    /**
     * Gets the valid property.
     * 
     * @return the valid property
     */
    public BooleanProperty validProperty() {
        return valid;
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
     * Gets whether the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the meeting.
     * 
     * @return the meeting
     */
    public Meeting getMeeting() {
        return meeting.get();
    }
    
    /**
     * Gets the meeting property.
     * 
     * @return the meeting property
     */
    public ObjectProperty<Meeting> meetingProperty() {
        return meeting;
    }
    
    /**
     * Gets whether this is a new meeting.
     * 
     * @return true if this is a new meeting, false if editing an existing meeting
     */
    public boolean isNewMeeting() {
        return isNewMeeting.get();
    }
    
    /**
     * Gets the new meeting property.
     * 
     * @return the new meeting property
     */
    public BooleanProperty isNewMeetingProperty() {
        return isNewMeeting;
    }
    
    /**
     * Gets the save command.
     * 
     * @return the save command
     */
    public Command getSaveCommand() {
        return saveCommand;
    }
    
    /**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    // Property getters and setters
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project value) {
        project.set(value);
    }
    
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    public LocalDate getDate() {
        return date.get();
    }
    
    public void setDate(LocalDate value) {
        date.set(value);
    }
    
    public StringProperty startTimeStringProperty() {
        return startTimeString;
    }
    
    public String getStartTimeString() {
        return startTimeString.get();
    }
    
    public void setStartTimeString(String value) {
        startTimeString.set(value);
    }
    
    public StringProperty endTimeStringProperty() {
        return endTimeString;
    }
    
    public String getEndTimeString() {
        return endTimeString.get();
    }
    
    public void setEndTimeString(String value) {
        endTimeString.set(value);
    }
    
    public StringProperty notesProperty() {
        return notes;
    }
    
    public String getNotes() {
        return notes.get();
    }
    
    public void setNotes(String value) {
        notes.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
    }
}