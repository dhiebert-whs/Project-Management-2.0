// src/main/java/org/frcpm/viewmodels/MeetingAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous ViewModel for meeting management.
 */
public class MeetingAsyncViewModel extends MeetingViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingAsyncViewModel.class.getName());
    
    // Services
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    
    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Async commands
    private Command asyncSaveCommand;
    
    /**
     * Creates a new MeetingAsyncViewModel with the specified service.
     * 
     * @param meetingServiceAsync the async meeting service
     */
    public MeetingAsyncViewModel(MeetingServiceAsyncImpl meetingServiceAsync) {
        super(null); // Initialize parent with null service
        
        this.meetingServiceAsync = meetingServiceAsync;
        
        // Initialize async commands
        initAsyncCommands();
    }
    
    /**
     * Initializes async commands.
     */
    private void initAsyncCommands() {
        asyncSaveCommand = new Command(
            this::saveAsync, 
            () -> {
                try {
                    // Access the valid property via reflection
                    Field validField = MeetingViewModel.class.getDeclaredField("valid");
                    validField.setAccessible(true);
                    BooleanProperty validProperty = (BooleanProperty) validField.get(this);
                    
                    return validProperty.get() && this.isDirty() && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error accessing valid field", e);
                    return false;
                }
            }
        );
    }
    
    /**
     * Asynchronously saves the meeting.
     */
    public void saveAsync() {
        try {
            // Get required fields and values using reflection
            Field validField = MeetingViewModel.class.getDeclaredField("valid");
            validField.setAccessible(true);
            BooleanProperty validProperty = (BooleanProperty) validField.get(this);
            
            if (!validProperty.get() || !isDirty() || loading.get()) {
                return;
            }
            
            // Get the meeting reference
            Field meetingField = MeetingViewModel.class.getDeclaredField("meeting");
            meetingField.setAccessible(true);
            Meeting meeting = (Meeting) meetingField.get(this);
            
            // Get the project reference
            Field projectField = MeetingViewModel.class.getDeclaredField("project");
            projectField.setAccessible(true);
            Project project = (Project) projectField.get(this);
            
            if (project == null) {
                setErrorMessage("Project cannot be null");
                return;
            }
            
            // Get date and time fields
            Field dateField = MeetingViewModel.class.getDeclaredField("date");
            dateField.setAccessible(true);
            LocalDate date = (LocalDate) dateField.get(this);
            
            Field startTimeStringField = MeetingViewModel.class.getDeclaredField("startTimeString");
            startTimeStringField.setAccessible(true);
            String startTimeString = (String) startTimeStringField.get(this);
            
            Field endTimeStringField = MeetingViewModel.class.getDeclaredField("endTimeString");
            endTimeStringField.setAccessible(true);
            String endTimeString = (String) endTimeStringField.get(this);
            
            Field notesField = MeetingViewModel.class.getDeclaredField("notes");
            notesField.setAccessible(true);
            String notes = (String) notesField.get(this);
            
            // Get isNewMeeting flag
            Field isNewMeetingField = MeetingViewModel.class.getDeclaredField("isNewMeeting");
            isNewMeetingField.setAccessible(true);
            BooleanProperty isNewMeetingProperty = (BooleanProperty) isNewMeetingField.get(this);
            
            // Parse times
            LocalTime startTime = parseTime(startTimeString);
            LocalTime endTime = parseTime(endTimeString);
            
            if (startTime == null || endTime == null) {
                setErrorMessage("Invalid time format");
                return;
            }
            
            loading.set(true);
            
            CompletableFuture<Meeting> future;
            if (isNewMeetingProperty.get()) {
                // Create new meeting
                future = meetingServiceAsync.createMeetingAsync(
                    date,
                    startTime,
                    endTime,
                    project.getId(),
                    notes,
                    this::handleSaveSuccess,
                    this::handleError
                );
            } else {
                if (meeting == null || meeting.getId() == null) {
                    setErrorMessage("Cannot update meeting - meeting is invalid");
                    loading.set(false);
                    return;
                }
                
                // Update existing meeting
                future = meetingServiceAsync.updateMeetingDateTimeAsync(
                    meeting.getId(),
                    date,
                    startTime,
                    endTime,
                    updatedMeeting -> {
                        // Then update notes
                        meetingServiceAsync.updateNotesAsync(
                            meeting.getId(),
                            notes,
                            this::handleSaveSuccess,
                            this::handleError
                        );
                    },
                    this::handleError
                );
            }
            
            // Handle completion
            future.whenComplete((result, error) -> {
                if (error != null) {
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Error saving meeting: " + error.getMessage());
                    });
                }
            });
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            setErrorMessage("Failed to save meeting: " + e.getMessage());
        }
    }
    
    /**
     * Handles successful save operation.
     * 
     * @param savedMeeting the saved meeting
     */
    private void handleSaveSuccess(Meeting savedMeeting) {
        Platform.runLater(() -> {
            try {
                // Update the meeting reference with the saved one
                Field meetingField = MeetingViewModel.class.getDeclaredField("meeting");
                meetingField.setAccessible(true);
                meetingField.set(this, savedMeeting);
                
                // Not dirty after save
                setDirty(false);
                
                // Clear error message
                clearErrorMessage();
                
                // Update the isNewMeeting property if this was a new meeting
                if (savedMeeting.getId() != null) {
                    Field isNewMeetingField = MeetingViewModel.class.getDeclaredField("isNewMeeting");
                    isNewMeetingField.setAccessible(true);
                    BooleanProperty isNewMeetingProperty = (BooleanProperty) isNewMeetingField.get(this);
                    isNewMeetingProperty.set(false);
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating ViewModel after save", e);
                setErrorMessage("Error updating ViewModel after save: " + e.getMessage());
            } finally {
                loading.set(false);
            }
        });
    }
    
    /**
     * Handles errors from async operations.
     * 
     * @param error the error that occurred
     */
    private void handleError(Throwable error) {
        Platform.runLater(() -> {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in async operation", error);
            setErrorMessage("Error: " + error.getMessage());
        });
    }
    
    /**
         * Helper method to parse time from string.
         * 
         * @param timeStr the time string (HH:MM)
         * @return the parsed LocalTime or null if invalid
         */
        public static LocalTime parseTime(String timeStr) {
            try {
                // Use reflection to access the static method in MeetingViewModel
                return (LocalTime) MeetingViewModel.class.getMethod("parseTime", String.class).invoke(null, timeStr);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing time", e);
                return null;
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
     * Gets the async save command.
     * 
     * @return the async save command
     */
    public Command getAsyncSaveCommand() {
        return asyncSaveCommand;
    }
    
    // Override save method to use async implementation
    @Override
    protected void save() {
        saveAsync();
    }
}