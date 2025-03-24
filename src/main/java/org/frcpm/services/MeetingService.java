package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for Meeting entity.
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
}