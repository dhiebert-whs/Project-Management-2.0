// Path: src/main/java/org/frcpm/viewmodels/MeetingViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * ViewModel for Meeting management
 */
public class MeetingViewModel extends BaseViewModel {

    private static final Logger LOGGER = Logger.getLogger(MeetingViewModel.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Services
    private final MeetingService meetingService;

    // Model reference
    private Meeting meeting;

    // Properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty startTime = new SimpleStringProperty("");
    private final StringProperty endTime = new SimpleStringProperty("");
    private final StringProperty location = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final BooleanProperty isNewMeeting = new SimpleBooleanProperty(true);

    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;

    /**
     * Creates a new MeetingViewModel with default service
     */
    public MeetingViewModel() {
        this(ServiceFactory.getMeetingService());
    }

    /**
     * Creates a new MeetingViewModel with the specified service
     * 
     * @param meetingService the meeting service
     */
    public MeetingViewModel(MeetingService meetingService) {
        this.meetingService = meetingService;

        // Create commands
        // IMPORTANT FIX: Use the standardized pattern from BaseViewModel
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        cancelCommand = new Command(this::cancel);

        // Set up property listeners
        setupPropertyListeners();

        // Initial validation
        validate();
    }

    /**
     * Sets up property change listeners
     */
    private void setupPropertyListeners() {
        // Create a dirty flag handler that also validates
        Runnable handler = createDirtyFlagHandler(this::validate);

        // Add listeners to all properties that should trigger validation
        date.addListener((obs, oldVal, newVal) -> handler.run());
        trackPropertyListener(handler);

        startTime.addListener((obs, oldVal, newVal) -> handler.run());
        trackPropertyListener(handler);

        endTime.addListener((obs, oldVal, newVal) -> handler.run());
        trackPropertyListener(handler);

        location.addListener((obs, oldVal, newVal) -> handler.run());
        trackPropertyListener(handler);

        // These properties only affect dirty state but not validation
        Runnable dirtyOnlyHandler = createDirtyFlagHandler(null);

        notes.addListener((obs, oldVal, newVal) -> dirtyOnlyHandler.run());
        trackPropertyListener(dirtyOnlyHandler);
    }

    /**
     * Validates the meeting data
     */
    public void validate() {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (date.get() == null) {
            errors.add("Meeting date is required");
        }

        if (startTime.get() == null || startTime.get().trim().isEmpty()) {
            errors.add("Start time is required");
        } else {
            try {
                parseTime(startTime.get());
            } catch (DateTimeParseException e) {
                errors.add("Invalid start time format (use HH:MM)");
            }
        }

        if (endTime.get() == null || endTime.get().trim().isEmpty()) {
            errors.add("End time is required");
        } else {
            try {
                parseTime(endTime.get());
            } catch (DateTimeParseException e) {
                errors.add("Invalid end time format (use HH:MM)");
            }
        }

        // Check that end time is after start time
        if (errors.isEmpty()) {
            LocalTime start = parseTime(startTime.get());
            LocalTime end = parseTime(endTime.get());

            if (start.equals(end)) {
                errors.add("End time must be after start time");
            } else if (end.isBefore(start)) {
                errors.add("End time cannot be before start time");
            }
        }

        // Update validation state
        valid.set(errors.isEmpty());

        // Update error message
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }

    /**
     * Checks if the meeting is valid
     * 
     * @return true if the meeting is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Initializes the ViewModel for a new meeting
     * 
     * @param project the project for the meeting
     */
    public void initNewMeeting(Project project) {
        this.project.set(project);
        this.meeting = new Meeting();
        isNewMeeting.set(true);

        // Default values
        date.set(LocalDate.now());
        startTime.set("09:00");
        endTime.set("10:00");
        location.set("");
        notes.set("");

        // Reset state
        setDirty(false);
        clearErrorMessage();
        validate();
    }

    /**
     * Initializes the ViewModel for an existing meeting
     * 
     * @param meeting the meeting to edit
     */
    public void initExistingMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new IllegalArgumentException("Meeting cannot be null");
        }

        this.meeting = meeting;
        this.project.set(meeting.getProject());
        isNewMeeting.set(false);

        // Set properties from meeting
        date.set(meeting.getDate());
        startTime.set(formatTime(meeting.getStartTime()));
        endTime.set(formatTime(meeting.getEndTime()));
        location.set(meeting.getLocation() != null ? meeting.getLocation() : "");
        notes.set(meeting.getNotes() != null ? meeting.getNotes() : "");

        // Reset state
        setDirty(false);
        clearErrorMessage();
        validate();
    }

    /**
     * Saves the meeting
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }

        try {
            // Update meeting from properties
            updateMeetingFromProperties();

            // Save meeting
            meeting = meetingService.save(meeting);

            // Reset state
            setDirty(false);
            clearErrorMessage();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            setErrorMessage("Failed to save meeting: " + e.getMessage());
        }
    }

    /**
     * Cancels the current operation
     */
    private void cancel() {
        // Handled by controller
    }

    /**
     * Updates the meeting from the properties
     */
    private void updateMeetingFromProperties() {
        meeting.setProject(project.get());
        meeting.setDate(date.get());
        meeting.setStartTime(parseTime(startTime.get()));
        meeting.setEndTime(parseTime(endTime.get()));
        meeting.setLocation(location.get());
        meeting.setNotes(notes.get());
    }

    /**
     * Helper method to parse time from string
     * 
     * @param timeStr the time string (HH:MM)
     * @return the parsed LocalTime
     */
    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * Helper method to format time as string
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

    // Property getters

    public ObjectProperty<Project> projectProperty() {
        return project;
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public StringProperty startTimeProperty() {
        return startTime;
    }

    public StringProperty endTimeProperty() {
        return endTime;
    }

    public StringProperty locationProperty() {
        return location;
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public BooleanProperty validProperty() {
        return valid;
    }

    public BooleanProperty isNewMeetingProperty() {
        return isNewMeeting;
    }

    // Command getters

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    // Getters and setters

    public Project getProject() {
        return project.get();
    }

    public LocalDate getDate() {
        return date.get();
    }

    public String getStartTime() {
        return startTime.get();
    }

    public String getEndTime() {
        return endTime.get();
    }

    public String getLocation() {
        return location.get();
    }

    public String getNotes() {
        return notes.get();
    }

    public boolean isNewMeeting() {
        return isNewMeeting.get();
    }

    public void setStartTime(String value) {
        startTime.set(value);
    }

    public void setEndTime(String value) {
        endTime.set(value);
    }

    public void setLocation(String value) {
        location.set(value);
    }

    public void setNotes(String value) {
        notes.set(value);
    }

    /**
     * Cleans up resources when the ViewModel is no longer needed
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
    }
}