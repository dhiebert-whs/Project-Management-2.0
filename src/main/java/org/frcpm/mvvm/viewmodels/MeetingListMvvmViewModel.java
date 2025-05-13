// src/main/java/org/frcpm/mvvm/viewmodels/MeetingListMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.services.MeetingService;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;

/**
 * ViewModel for the MeetingList view using MVVMFx.
 */
public class MeetingListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingListMvvmViewModel.class.getName());
    
    /**
     * Enum for filtering meetings.
     */
    public enum MeetingFilter {
        ALL("All Meetings"),
        UPCOMING("Upcoming Meetings"),
        PAST("Past Meetings");
        
        private final String displayName;
        
        MeetingFilter(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Service dependencies
    private final MeetingService meetingService;
    private final MeetingServiceAsyncImpl meetingServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Meeting> allMeetings = FXCollections.observableArrayList();
    private final FilteredList<Meeting> filteredMeetings = new FilteredList<>(allMeetings);
    private final SortedList<Meeting> sortedMeetings = new SortedList<>(filteredMeetings);
    private final ObjectProperty<Meeting> selectedMeeting = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Current filter
    private MeetingFilter currentFilter = MeetingFilter.ALL;
    
    // Commands
    private Command loadMeetingsCommand;
    private Command newMeetingCommand;
    private Command editMeetingCommand;
    private Command deleteMeetingCommand;
    private Command refreshMeetingsCommand;
    private Command viewAttendanceCommand;
    
    /**
     * Creates a new MeetingListMvvmViewModel.
     * 
     * @param meetingService the meeting service
     */
    @Inject
    public MeetingListMvvmViewModel(MeetingService meetingService) {
        this.meetingService = meetingService;
        this.meetingServiceAsync = (MeetingServiceAsyncImpl) meetingService;
        
        // Set up sorting - by default, sort by date and time
        sortedMeetings.setComparator(Comparator.comparing(Meeting::getDate)
                .thenComparing(Meeting::getStartTime));
        
        initializeCommands();
        updateFilterPredicate();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load meetings command
        loadMeetingsCommand = createValidOnlyCommand(
            this::loadMeetingsAsync,
            () -> !loading.get()
        );
        
        // New meeting command
        newMeetingCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New meeting command executed");
            },
            () -> currentProject.get() != null && !loading.get()
        );
        
        // Edit meeting command
        editMeetingCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit meeting command executed for: " + 
                    (selectedMeeting.get() != null ? selectedMeeting.get().getDate() : "null"));
            },
            () -> selectedMeeting.get() != null && !loading.get()
        );
        
        // Delete meeting command
        deleteMeetingCommand = createValidOnlyCommand(
            this::deleteMeetingAsync,
            () -> selectedMeeting.get() != null && !loading.get()
        );
        
        // Refresh meetings command
        refreshMeetingsCommand = createValidOnlyCommand(
            this::loadMeetingsAsync,
            () -> !loading.get()
        );
        
        // View attendance command
        viewAttendanceCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("View attendance command executed for: " + 
                    (selectedMeeting.get() != null ? selectedMeeting.get().getDate() : "null"));
            },
            () -> selectedMeeting.get() != null && !loading.get()
        );
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadMeetingsAsync();
    }
    
    /**
     * Sets the filter and updates the filtered list.
     * 
     * @param filter the filter to apply
     */
    public void setFilter(MeetingFilter filter) {
        if (filter == null) {
            LOGGER.warning("Attempting to set null filter, using ALL instead");
            filter = MeetingFilter.ALL;
        }
        
        this.currentFilter = filter;
        updateFilterPredicate();
    }
    
    /**
     * Updates the filter predicate based on the current filter.
     */
    private void updateFilterPredicate() {
        filteredMeetings.setPredicate(buildFilterPredicate());
    }
    
    /**
     * Builds a predicate based on the current filter.
     * 
     * @return the predicate
     */
    private Predicate<Meeting> buildFilterPredicate() {
        return meeting -> {
            if (meeting == null) {
                return false;
            }
            
            LocalDate today = LocalDate.now();
            
            switch (currentFilter) {
                case ALL:
                    return true;
                case UPCOMING:
                    return meeting.getDate() != null && !meeting.getDate().isBefore(today);
                case PAST:
                    return meeting.getDate() != null && meeting.getDate().isBefore(today);
                default:
                    return true;
            }
        };
    }
    
    /**
     * Loads meetings asynchronously.
     */
    private void loadMeetingsAsync() {
        Project project = currentProject.get();
        if (project == null) {
            LOGGER.warning("Cannot load meetings - no project selected");
            return;
        }
        
        loading.set(true);
        
        meetingServiceAsync.findByProjectAsync(
            project,
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allMeetings.clear();
                    allMeetings.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " meetings asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading meetings asynchronously", error);
                    setErrorMessage("Failed to load meetings: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a meeting asynchronously.
     */
    private void deleteMeetingAsync() {
        Meeting meeting = selectedMeeting.get();
        if (meeting == null || meeting.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        meetingServiceAsync.deleteByIdAsync(
            meeting.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        allMeetings.remove(meeting);
                        selectedMeeting.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted meeting: " + meeting.getDate() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete meeting: " + meeting.getDate() + " asynchronously");
                        setErrorMessage("Failed to delete meeting: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting meeting asynchronously", error);
                    setErrorMessage("Failed to delete meeting: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Gets upcoming meetings for the current project.
     * 
     * @param days the number of days to look ahead
     */
    public void loadUpcomingMeetingsAsync(int days) {
        Project project = currentProject.get();
        if (project == null) {
            LOGGER.warning("Cannot load upcoming meetings - no project selected");
            return;
        }
        
        loading.set(true);
        
        meetingServiceAsync.getUpcomingMeetingsAsync(
            project.getId(),
            days,
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allMeetings.clear();
                    allMeetings.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " upcoming meetings asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading upcoming meetings asynchronously", error);
                    setErrorMessage("Failed to load upcoming meetings: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Gets the meetings list.
     * 
     * @return the meetings list
     */
    public SortedList<Meeting> getMeetings() {
        return sortedMeetings;
    }
    
    /**
     * Gets the selected meeting property.
     * 
     * @return the selected meeting property
     */
    public ObjectProperty<Meeting> selectedMeetingProperty() {
        return selectedMeeting;
    }
    
    /**
     * Gets the selected meeting.
     * 
     * @return the selected meeting
     */
    public Meeting getSelectedMeeting() {
        return selectedMeeting.get();
    }
    
    /**
     * Sets the selected meeting.
     * 
     * @param meeting the selected meeting
     */
    public void setSelectedMeeting(Meeting meeting) {
        selectedMeeting.set(meeting);
    }
    
    /**
     * Gets the current project property.
     * 
     * @return the current project property
     */
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    /**
     * Gets the current project.
     * 
     * @return the current project
     */
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    /**
     * Sets the current project.
     * 
     * @param project the project
     */
    public void setCurrentProject(Project project) {
        currentProject.set(project);
    }
    
    /**
     * Gets the current filter.
     * 
     * @return the current filter
     */
    public MeetingFilter getCurrentFilter() {
        return currentFilter;
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
     * Checks if the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the load meetings command.
     * 
     * @return the load meetings command
     */
    public Command getLoadMeetingsCommand() {
        return loadMeetingsCommand;
    }
    
    /**
     * Gets the new meeting command.
     * 
     * @return the new meeting command
     */
    public Command getNewMeetingCommand() {
        return newMeetingCommand;
    }
    
    /**
     * Gets the edit meeting command.
     * 
     * @return the edit meeting command
     */
    public Command getEditMeetingCommand() {
        return editMeetingCommand;
    }
    
    /**
     * Gets the delete meeting command.
     * 
     * @return the delete meeting command
     */
    public Command getDeleteMeetingCommand() {
        return deleteMeetingCommand;
    }
    
    /**
     * Gets the refresh meetings command.
     * 
     * @return the refresh meetings command
     */
    public Command getRefreshMeetingsCommand() {
        return refreshMeetingsCommand;
    }
    
    /**
     * Gets the view attendance command.
     * 
     * @return the view attendance command
     */
    public Command getViewAttendanceCommand() {
        return viewAttendanceCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        allMeetings.clear();
    }
}