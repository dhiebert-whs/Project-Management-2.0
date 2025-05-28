// src/main/java/org/frcpm/services/MeetingService.java
package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for Meeting entity.
 * Enhanced with async methods following the established pattern.
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