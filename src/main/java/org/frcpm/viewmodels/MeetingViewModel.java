package org.frcpm.viewmodels;

import javafx.beans.property.*;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

        // Create commands using BaseViewModel utility methods
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        cancelCommand = new Command(() -> {
            // No action needed for cancel
        });

        // Set up default values
        date.set(LocalDate.now());
        startTimeString.set("16:00");
        endTimeString.set("18:00");

        // Set up validation listeners with property tracking
        setupPropertyListeners();
    }

    /**
     * Sets up property listeners for validation and dirty state tracking.
     */
    private void setupPropertyListeners() {
        // Create standard validation handler
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        
        // Add listeners and track them
        date.addListener((observable, oldValue, newValue) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        startTimeString.addListener((observable, oldValue, newValue) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        endTimeString.addListener((observable, oldValue, newValue) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        // Notes only affects dirty state, not validation
        notes.addListener((observable, oldValue, newValue) -> setDirty(true));
        trackPropertyListener(() -> setDirty(true));
    }

    /**
     * Sets up the ViewModel for creating a new meeting.
     * 
     * @param project the project for the meeting
     */
    public void initNewMeeting(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot initialize meeting with null project");
            return;
        }
        
        this.project.set(project);
        meeting.set(null);
        isNewMeeting.set(true);

        // Set default values
        date.set(LocalDate.now());
        startTimeString.set("16:00");
        endTimeString.set("18:00");
        notes.set("");

        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
        
        // Validate after initialization to ensure valid state
        validate();
    }

    /**
     * Sets up the ViewModel for editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void initExistingMeeting(Meeting meeting) {
        if (meeting == null) {
            LOGGER.warning("Cannot initialize with null meeting");
            return;
        }
        
        this.meeting.set(meeting);
        project.set(meeting.getProject());
        isNewMeeting.set(false);

        // Set field values from meeting
        date.set(meeting.getDate());
        startTimeString.set(formatTime(meeting.getStartTime()));
        endTimeString.set(formatTime(meeting.getEndTime()));
        notes.set(meeting.getNotes());

        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
        
        // Validate after initialization to ensure valid state
        validate();
    }

    /**
     * Validates the meeting data.
     * Sets the valid property and error message.
     * Made public for testing.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (date.get() == null) {
            errors.add("Meeting date cannot be empty");
        }

        if (startTimeString.get() == null || startTimeString.get().trim().isEmpty()) {
            errors.add("Start time cannot be empty");
        } else {
            try {
                LocalTime.parse(startTimeString.get(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Start time format should be HH:MM");
            }
        }

        if (endTimeString.get() == null || endTimeString.get().trim().isEmpty()) {
            errors.add("End time cannot be empty");
        } else {
            try {
                LocalTime.parse(endTimeString.get(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("End time format should be HH:MM");
            }
        }

        // If times are valid, check if end time is after start time
        try {
            LocalTime startTime = parseTime(startTimeString.get());
            LocalTime endTime = parseTime(endTimeString.get());

            if (startTime != null && endTime != null &&
                    (endTime.isBefore(startTime) || endTime.equals(startTime))) {
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
            LocalTime startTime = parseTime(startTimeString.get());
            LocalTime endTime = parseTime(endTimeString.get());
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

            // Clear dirty flag and error message
            setDirty(false);
            clearErrorMessage();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            setErrorMessage("Failed to save meeting: " + e.getMessage());
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
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Add any additional cleanup if needed
    }

    // Property getters
    public boolean isValid() {
        return valid.get();
    }

    public BooleanProperty validProperty() {
        return valid;
    }

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public Meeting getMeeting() {
        return meeting.get();
    }

    public ObjectProperty<Meeting> meetingProperty() {
        return meeting;
    }

    public boolean isNewMeeting() {
        return isNewMeeting.get();
    }

    public BooleanProperty isNewMeetingProperty() {
        return isNewMeeting;
    }

    // Date property accessors
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate value) {
        date.set(value);
    }

    // Start time string property accessors
    public StringProperty startTimeStringProperty() {
        return startTimeString;
    }

    public String getStartTimeString() {
        return startTimeString.get();
    }

    public void setStartTimeString(String value) {
        startTimeString.set(value);
    }

    // End time string property accessors
    public StringProperty endTimeStringProperty() {
        return endTimeString;
    }

    public String getEndTimeString() {
        return endTimeString.get();
    }

    public void setEndTimeString(String value) {
        endTimeString.set(value);
    }

    // Notes property accessors
    public StringProperty notesProperty() {
        return notes;
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String value) {
        notes.set(value);
    }

    // Project property accessors
    public ObjectProperty<Project> projectProperty() {
        return project;
    }

    public Project getProject() {
        return project.get();
    }

    public void setProject(Project value) {
        project.set(value);
    }

    public void setMeeting(Meeting value) {
        meeting.set(value);
    }

    public void setIsNewMeeting(boolean value) {
        isNewMeeting.set(value);
    }

    public void setValid(boolean value) {
        valid.set(value);
    }
}