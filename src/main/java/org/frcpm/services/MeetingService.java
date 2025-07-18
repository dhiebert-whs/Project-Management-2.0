// src/main/java/org/frcpm/services/MeetingService.java
package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.MeetingType;
import org.frcpm.models.MeetingStatus;
import org.frcpm.models.MeetingPriority;
import org.frcpm.models.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Enhanced Meeting service interface for Phase 3A Meeting Management System.
 * 
 * Features added for Phase 3A:
 * - Meeting type and status management
 * - Priority-based scheduling
 * - Virtual and hybrid meeting support
 * - Recurring meeting management
 * - Advanced search and filtering
 * - Meeting conflict detection
 * - Notification and reminder system
 * - Meeting analytics and reporting
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
public interface MeetingService extends Service<Meeting, Long> {
    
    /**
     * Finds meetings for a specific project.
     * 
     * @param project the project to find meetings for
     * @return a list of meetings for the project
     */
    List<Meeting> findByProject(Project project);
    
    /**
     * Finds meetings on a specific date.
     * 
     * @param date the date to search for
     * @return a list of meetings on the given date
     */
    List<Meeting> findByDate(LocalDate date);
    
    /**
     * Finds meetings after a specific date.
     * 
     * @param date the date to compare against
     * @return a list of meetings after the date
     */
    List<Meeting> findByDateAfter(LocalDate date);
    
    /**
     * Finds meetings in a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of meetings within the date range
     */
    List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Creates a new meeting.
     * 
     * @param date the meeting date
     * @param startTime the start time
     * @param endTime the end time
     * @param projectId the ID of the project the meeting is for
     * @param notes any meeting notes (optional)
     * @return the created meeting
     */
    Meeting createMeeting(LocalDate date, LocalTime startTime, LocalTime endTime, 
                          Long projectId, String notes);
    
    /**
     * Updates a meeting's date and time.
     * 
     * @param meetingId the meeting ID
     * @param date the new date (optional)
     * @param startTime the new start time (optional)
     * @param endTime the new end time (optional)
     * @return the updated meeting, or null if not found
     */
    Meeting updateMeetingDateTime(Long meetingId, LocalDate date, 
                                  LocalTime startTime, LocalTime endTime);
    
    /**
     * Updates meeting notes.
     * 
     * @param meetingId the meeting ID
     * @param notes the new notes
     * @return the updated meeting, or null if not found
     */
    Meeting updateNotes(Long meetingId, String notes);
    
    /**
     * Gets upcoming meetings for a project.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @return a list of upcoming meetings within the specified days
     */
    List<Meeting> getUpcomingMeetings(Long projectId, int days);
    
    // =========================================================================
    // PHASE 3A: ENHANCED MEETING MANAGEMENT METHODS
    // =========================================================================
    
    /**
     * Creates a new meeting with enhanced Phase 3A features.
     * 
     * @param title the meeting title
     * @param description the meeting description
     * @param date the meeting date
     * @param startTime the start time
     * @param endTime the end time
     * @param projectId the project ID
     * @param meetingType the meeting type
     * @param priority the meeting priority
     * @param location the physical location (optional)
     * @param virtualMeetingUrl the virtual meeting URL (optional)
     * @param agenda the meeting agenda (optional)
     * @param createdBy the user who created the meeting
     * @return the created meeting
     */
    Meeting createEnhancedMeeting(String title, String description, LocalDate date, 
                                 LocalTime startTime, LocalTime endTime, Long projectId,
                                 MeetingType meetingType, MeetingPriority priority, 
                                 String location, String virtualMeetingUrl, 
                                 String agenda, String createdBy);
    
    /**
     * Updates meeting status.
     * 
     * @param meetingId the meeting ID
     * @param newStatus the new status
     * @return the updated meeting, or null if not found
     */
    Meeting updateMeetingStatus(Long meetingId, MeetingStatus newStatus);
    
    /**
     * Finds meetings by status.
     * 
     * @param status the meeting status
     * @return a list of meetings with the specified status
     */
    List<Meeting> findByStatus(MeetingStatus status);
    
    /**
     * Finds meetings by type.
     * 
     * @param meetingType the meeting type
     * @return a list of meetings with the specified type
     */
    List<Meeting> findByMeetingType(MeetingType meetingType);
    
    /**
     * Finds meetings by priority.
     * 
     * @param priority the meeting priority
     * @return a list of meetings with the specified priority
     */
    List<Meeting> findByPriority(MeetingPriority priority);
    
    /**
     * Finds meetings by location.
     * 
     * @param location the meeting location
     * @return a list of meetings at the specified location
     */
    List<Meeting> findByLocation(String location);
    
    /**
     * Finds virtual meetings (those with virtual meeting URLs).
     * 
     * @return a list of virtual meetings
     */
    List<Meeting> findVirtualMeetings();
    
    /**
     * Finds hybrid meetings (those with both location and virtual URL).
     * 
     * @return a list of hybrid meetings
     */
    List<Meeting> findHybridMeetings();
    
    /**
     * Finds meetings requiring preparation.
     * 
     * @return a list of meetings that require preparation
     */
    List<Meeting> findMeetingsRequiringPreparation();
    
    /**
     * Finds recurring meetings.
     * 
     * @return a list of recurring meetings
     */
    List<Meeting> findRecurringMeetings();
    
    /**
     * Finds meetings created by a specific user.
     * 
     * @param createdBy the username of the creator
     * @return a list of meetings created by the user
     */
    List<Meeting> findByCreatedBy(String createdBy);
    
    /**
     * Finds meetings created within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of meetings created within the date range
     */
    List<Meeting> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds meetings happening today.
     * 
     * @return a list of meetings scheduled for today
     */
    List<Meeting> findTodaysMeetings();
    
    /**
     * Finds meetings happening this week.
     * 
     * @return a list of meetings scheduled for this week
     */
    List<Meeting> findThisWeeksMeetings();
    
    /**
     * Finds meetings that are currently in progress.
     * 
     * @return a list of meetings currently in progress
     */
    List<Meeting> findMeetingsInProgress();
    
    /**
     * Finds overbooked meetings (more attendees than max allowed).
     * 
     * @return a list of overbooked meetings
     */
    List<Meeting> findOverbookedMeetings();
    
    /**
     * Detects scheduling conflicts for a given time period.
     * 
     * @param date the date to check
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeMeetingId optional meeting ID to exclude from conflict check
     * @return a list of conflicting meetings
     */
    List<Meeting> detectSchedulingConflicts(LocalDate date, LocalTime startTime, 
                                          LocalTime endTime, Long excludeMeetingId);
    
    /**
     * Finds the next meeting for a project.
     * 
     * @param projectId the project ID
     * @return the next upcoming meeting, or null if none found
     */
    Optional<Meeting> findNextMeetingForProject(Long projectId);
    
    /**
     * Finds meetings that need reminders to be sent.
     * 
     * @return a list of meetings that need reminders
     */
    List<Meeting> findMeetingsNeedingReminders();
    
    /**
     * Creates a recurring meeting series.
     * 
     * @param meeting the base meeting
     * @param recurrencePattern the recurrence pattern (WEEKLY, BIWEEKLY, MONTHLY)
     * @param numberOfOccurrences the number of occurrences to create
     * @return a list of created recurring meetings
     */
    List<Meeting> createRecurringMeetings(Meeting meeting, String recurrencePattern, int numberOfOccurrences);
    
    /**
     * Cancels a meeting and all its future occurrences (if recurring).
     * 
     * @param meetingId the meeting ID
     * @param cancelFutureOccurrences whether to cancel future occurrences
     * @return the number of meetings cancelled
     */
    int cancelMeeting(Long meetingId, boolean cancelFutureOccurrences);
    
    /**
     * Reschedules a meeting to a new date and time.
     * 
     * @param meetingId the meeting ID
     * @param newDate the new date
     * @param newStartTime the new start time
     * @param newEndTime the new end time
     * @return the rescheduled meeting, or null if not found
     */
    Meeting rescheduleMeeting(Long meetingId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime);
    
    /**
     * Gets meeting statistics for a project.
     * 
     * @param projectId the project ID
     * @return a map containing meeting statistics
     */
    Map<String, Object> getMeetingStatistics(Long projectId);
    
    /**
     * Gets meeting statistics for a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a map containing meeting statistics
     */
    Map<String, Object> getMeetingStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * Gets attendance statistics for meetings.
     * 
     * @param meetingIds the meeting IDs
     * @return a map containing attendance statistics
     */
    Map<String, Object> getAttendanceStatistics(List<Long> meetingIds);
    
    /**
     * Search meetings by title or description.
     * 
     * @param searchTerm the search term
     * @return a list of meetings matching the search term
     */
    List<Meeting> searchMeetings(String searchTerm);
    
    /**
     * Advanced meeting search with multiple criteria.
     * 
     * @param criteria the search criteria
     * @return a list of meetings matching the criteria
     */
    List<Meeting> searchMeetings(Map<String, Object> criteria);
    
    /**
     * Updates meeting reminder settings.
     * 
     * @param meetingId the meeting ID
     * @param reminderEnabled whether reminders are enabled
     * @param reminderMinutesBefore minutes before meeting to send reminder
     * @return the updated meeting, or null if not found
     */
    Meeting updateReminderSettings(Long meetingId, boolean reminderEnabled, Integer reminderMinutesBefore);
    
    /**
     * Adds action items to a meeting.
     * 
     * @param meetingId the meeting ID
     * @param actionItems the action items to add
     * @return the updated meeting, or null if not found
     */
    Meeting addActionItems(Long meetingId, String actionItems);
    
    /**
     * Updates meeting agenda.
     * 
     * @param meetingId the meeting ID
     * @param agenda the new agenda
     * @return the updated meeting, or null if not found
     */
    Meeting updateAgenda(Long meetingId, String agenda);
    
    /**
     * Sets maximum attendees for a meeting.
     * 
     * @param meetingId the meeting ID
     * @param maxAttendees the maximum number of attendees
     * @return the updated meeting, or null if not found
     */
    Meeting setMaxAttendees(Long meetingId, Integer maxAttendees);
    
    /**
     * Gets the meeting capacity status.
     * 
     * @param meetingId the meeting ID
     * @return a map containing capacity information
     */
    Map<String, Object> getMeetingCapacityStatus(Long meetingId);
    
    // =========================================================================
    // ASYNC METHODS - Following the established pattern from SubteamService
    // =========================================================================
    
    /**
     * Finds all meetings asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    default CompletableFuture<List<Meeting>> findAllAsync(
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        try {
            List<Meeting> result = findAll();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Saves a meeting asynchronously.
     * 
     * @param entity the meeting to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved meeting
     */
    default CompletableFuture<Meeting> saveAsync(
            Meeting entity,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        try {
            Meeting result = save(entity);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a meeting by ID asynchronously.
     * 
     * @param id the ID of the meeting to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    default CompletableFuture<Boolean> deleteByIdAsync(
            Long id,
            Consumer<Boolean> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            Boolean result = deleteById(id);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds meetings by project asynchronously.
     * 
     * @param project the project
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    default CompletableFuture<List<Meeting>> findByProjectAsync(
            Project project,
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        try {
            List<Meeting> result = findByProject(project);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Gets upcoming meetings asynchronously.
     * 
     * @param projectId the project ID
     * @param days the number of days ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of upcoming meetings
     */
    default CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(
            Long projectId, 
            int days,
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        try {
            List<Meeting> result = getUpcomingMeetings(projectId, days);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Creates a meeting asynchronously.
     * 
     * @param date the meeting date
     * @param startTime the start time
     * @param endTime the end time
     * @param projectId the project ID
     * @param notes the meeting notes
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created meeting
     */
    default CompletableFuture<Meeting> createMeetingAsync(
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime, 
            Long projectId, 
            String notes,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        try {
            Meeting result = createMeeting(date, startTime, endTime, projectId, notes);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Updates meeting date and time asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param date the new date
     * @param startTime the new start time
     * @param endTime the new end time
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    default CompletableFuture<Meeting> updateMeetingDateTimeAsync(
            Long meetingId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        try {
            Meeting result = updateMeetingDateTime(meetingId, date, startTime, endTime);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Updates meeting notes asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param notes the new notes
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    default CompletableFuture<Meeting> updateNotesAsync(
            Long meetingId, 
            String notes,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        try {
            Meeting result = updateNotes(meetingId, notes);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
}