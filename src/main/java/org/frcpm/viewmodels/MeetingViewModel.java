package org.frcpm.viewmodels;

import javafx.beans.property.*;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Meeting view.
 */
public class MeetingViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingViewModel.class.getName());
    
    // Services
    private final MeetingService meetingService;
    
    // Observable properties
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty startTimeString = new SimpleStringProperty();
    private final StringProperty endTimeString = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<Meeting> meeting = new SimpleObjectProperty<>();
    private final BooleanProperty isNewMeeting = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    
    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;
    
    /**
     * Creates a new MeetingViewModel.
     */
    public MeetingViewModel() {
        this(ServiceFactory.getMeetingService());
    }
    
    /**
     * Creates a new MeetingViewModel with the specified service.
     * This constructor is mainly used for testing.
     * 
     * @param meetingService the meeting service
     */
    public MeetingViewModel(MeetingService meetingService) {
        this.meetingService = meetingService;
        
        // Create commands
        saveCommand = new Command(this::save, this::isValid);
        cancelCommand = new Command(() -> {});
        
        // Set up default values
        date.set(LocalDate.now());
        startTimeString.set("16:00");
        endTimeString.set("18:00");
        
        // Set up validation listeners
        date.addListener((observable, oldValue, newValue) -> validate());
        startTimeString.addListener((observable, oldValue, newValue) -> validate());
        endTimeString.addListener((observable, oldValue, newValue) -> validate());
        notes.addListener((observable, oldValue, newValue) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Sets up the ViewModel for creating a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void initNewMeeting(Project project) {
        this.project.set(project);
        meeting.set(null);
        isNewMeeting.set(true);
        
        // Set default values
        date.set(LocalDate.now());
        startTimeString.set("16:00");
        endTimeString.set("18:00");
        notes.set("");
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Sets up the ViewModel for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void initExistingMeeting(Meeting meeting) {
        this.meeting.set(meeting);
        project.set(meeting.getProject());
        isNewMeeting.set(false);
        
        // Set field values from meeting
        date.set(meeting.getDate());
        startTimeString.set(meeting.getStartTime().toString());
        endTimeString.set(meeting.getEndTime().toString());
        notes.set(meeting.getNotes());
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Validates the meeting data.
     * Sets the valid property and error message.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (date.get() == null) {
            errors.add("Meeting date cannot be empty");
        }
        
        if (startTimeString.get() == null || startTimeString.get().trim().isEmpty()) {
            errors.add("Start time cannot be empty");
        } else {
            try {
                LocalTime.parse(startTimeString.get());
            } catch (DateTimeParseException e) {
                errors.add("Start time format should be HH:MM");
            }
        }
        
        if (endTimeString.get() == null || endTimeString.get().trim().isEmpty()) {
            errors.add("End time cannot be empty");
        } else {
            try {
                LocalTime.parse(endTimeString.get());
            } catch (DateTimeParseException e) {
                errors.add("End time format should be HH:MM");
            }
        }
        
        // If times are valid, check if end time is after start time
        try {
            LocalTime startTime = LocalTime.parse(startTimeString.get());
            LocalTime endTime = LocalTime.parse(endTimeString.get());
            
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                errors.add("End time must be after start time");
            }
        } catch (DateTimeParseException ignored) {
            // Ignore - already handled above
        }
        
        // Update valid property and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Saves the meeting.
     * Called when the save command is executed.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }
        
        try {
            LocalDate meetingDate = date.get();
            LocalTime startTime = LocalTime.parse(startTimeString.get());
            LocalTime endTime = LocalTime.parse(endTimeString.get());
            String meetingNotes = notes.get();
            
            Meeting savedMeeting;
            if (isNewMeeting.get()) {
                // Create new meeting
                savedMeeting = meetingService.createMeeting(
                    meetingDate, startTime, endTime, project.get().getId(), meetingNotes);
            } else {
                // Update existing meeting
                savedMeeting = meetingService.updateMeetingDateTime(
                    meeting.get().getId(), meetingDate, startTime, endTime);
                savedMeeting = meetingService.updateNotes(savedMeeting.getId(), meetingNotes);
            }
            
            // Update meeting property with saved meeting
            meeting.set(savedMeeting);
            
            // Clear dirty flag
            setDirty(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            setErrorMessage("Failed to save meeting: " + e.getMessage());
            valid.set(false);
        }
    }
    
    // Property getters
    
    /**
     * Gets whether the input is valid.
     * 
     * @return true if the input is valid, false otherwise
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
     * Gets the date property.
     * 
     * @return the date property
     */
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    /**
     * Gets the start time string property.
     * 
     * @return the start time string property
     */
    public StringProperty startTimeStringProperty() {
        return startTimeString;
    }
    
    /**
     * Gets the end time string property.
     * 
     * @return the end time string property
     */
    public StringProperty endTimeStringProperty() {
        return endTimeString;
    }
    
    /**
     * Gets the notes property.
     * 
     * @return the notes property
     */
    public StringProperty notesProperty() {
        return notes;
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
     * Gets the new meeting flag.
     * 
     * @return true if this is a new meeting, false otherwise
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
}