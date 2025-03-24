package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Milestone;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for managing Meeting entities.
 */
public interface MeetingService extends Service<Meeting, Long> {

    /**
     * Finds all meetings for a specific project.
     *
     * @param project The project to find meetings for
     * @return List of meetings for the project
     */
    List<Meeting> findByProject(Project project);

    /**
     * Finds all meetings on a specific date.
     *
     * @param date The date to find meetings for
     * @return List of meetings on the date
     */
    List<Meeting> findByDate(LocalDate date);

    /**
     * Finds all meetings between two dates.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of meetings between the dates
     */
    List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Creates a new one-time meeting.
     *
     * @param project The project to create the meeting for
     * @param title The title of the meeting
     * @param description The description of the meeting
     * @param date The date of the meeting
     * @param startTime The start time of the meeting
     * @param endTime The end time of the meeting
     * @param location The location of the meeting
     * @return The created meeting
     */
    Meeting createOneTimeMeeting(Project project, String title, String description, 
                                 LocalDate date, LocalTime startTime, LocalTime endTime, 
                                 String location);

    /**
     * Creates recurring meetings based on a pattern.
     *
     * @param project The project to create the meetings for
     * @param title The title of the meetings
     * @param description The description of the meetings
     * @param startDate The start date for the recurring pattern
     * @param endDate The end date for the recurring pattern
     * @param dayOfWeek The day of the week for the meetings
     * @param startTime The start time of the meetings
     * @param endTime The end time of the meetings
     * @param location The location of the meetings
     * @return List of created meetings
     */
    List<Meeting> createRecurringMeetings(Project project, String title, String description,
                                          LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek,
                                          LocalTime startTime, LocalTime endTime, String location);

    /**
     * Associates a meeting with a milestone.
     *
     * @param meeting The meeting to associate
     * @param milestone The milestone to associate with
     * @return The updated meeting
     */
    Meeting associateWithMilestone(Meeting meeting, Milestone milestone);

    /**
     * Removes a milestone association from a meeting.
     *
     * @param meeting The meeting to update
     * @param milestone The milestone to remove
     * @return The updated meeting
     */
    Meeting removeMilestoneAssociation(Meeting meeting, Milestone milestone);

    /**
     * Updates the meeting notes.
     *
     * @param meeting The meeting to update
     * @param notes The notes to set
     * @return The updated meeting
     */
    Meeting updateNotes(Meeting meeting, String notes);

    /**
     * Cancels a meeting.
     *
     * @param meeting The meeting to cancel
     * @return The canceled meeting
     */
    Meeting cancelMeeting(Meeting meeting);

    /**
     * Gets all upcoming meetings for a project.
     *
     * @param project The project to get meetings for
     * @return List of upcoming meetings
     */
    List<Meeting> getUpcomingMeetings(Project project);

    /**
     * Gets the next scheduled meeting for a project.
     *
     * @param project The project to get the meeting for
     * @return The next scheduled meeting, or null if none exists
     */
    Meeting getNextMeeting(Project project);

    /**
     * Finds all meetings that conflict with a proposed meeting time.
     *
     * @param date The proposed date
     * @param startTime The proposed start time
     * @param endTime The proposed end time
     * @return List of conflicting meetings
     */
    List<Meeting> findConflictingMeetings(LocalDate date, LocalTime startTime, LocalTime endTime);
}
