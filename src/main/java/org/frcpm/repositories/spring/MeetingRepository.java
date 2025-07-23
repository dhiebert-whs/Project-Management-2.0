// src/main/java/org/frcpm/repositories/spring/MeetingRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.MeetingType;
import org.frcpm.models.MeetingStatus;
import org.frcpm.models.MeetingPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Spring Data JPA repository for Meeting entity - Phase 3A.
 * 
 * Provides comprehensive meeting management queries supporting:
 * - Meeting type and status filtering
 * - Priority-based queries
 * - Virtual and hybrid meeting support
 * - Recurring meeting management
 * - Advanced search capabilities
 * - Conflict detection
 * - Statistics and analytics
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
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
     * Finds meetings for a specific project by project ID.
     * 
     * @param projectId the project ID
     * @return a list of meetings for the project
     */
    @Query("SELECT m FROM Meeting m WHERE m.project.id = :projectId")
    List<Meeting> findByProjectId(@Param("projectId") Long projectId);
    
    /**
     * Finds meetings on or after a specific date, ordered by date and start time.
     * 
     * @param date the date to compare against
     * @return a list of meetings on or after the date
     */
    List<Meeting> findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(LocalDate date);
    
    /**
     * Finds meetings for a project within a date range.
     * 
     * @param project the project
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of meetings for the project within the date range
     */
    List<Meeting> findByProjectAndDateBetween(Project project, LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds upcoming meetings for a project (today and future).
     * 
     * @param project the project
     * @param currentDate the current date
     * @return a list of upcoming meetings for the project
     */
    @Query("SELECT m FROM Meeting m WHERE m.project = :project AND m.date >= :currentDate ORDER BY m.date ASC, m.startTime ASC")
    List<Meeting> findUpcomingMeetingsByProject(@Param("project") Project project, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Finds meetings that overlap with a given time period on a specific date.
     * 
     * @param date the date
     * @param startTime the start time of the period
     * @param endTime the end time of the period
     * @return a list of meetings that overlap with the time period
     */
    @Query("SELECT m FROM Meeting m WHERE m.date = :date AND " +
           "((m.startTime <= :startTime AND m.endTime > :startTime) OR " +
           "(m.startTime < :endTime AND m.endTime >= :endTime) OR " +
           "(m.startTime >= :startTime AND m.endTime <= :endTime))")
    List<Meeting> findOverlappingMeetings(@Param("date") LocalDate date, 
                                         @Param("startTime") LocalTime startTime, 
                                         @Param("endTime") LocalTime endTime);
    
    /**
     * Finds the next upcoming meeting for a project.
     * 
     * @param project the project
     * @param currentDate the current date
     * @return an Optional containing the next meeting, or empty if no upcoming meetings exist
     */
    @Query("SELECT m FROM Meeting m WHERE m.project = :project AND m.date >= :currentDate ORDER BY m.date ASC, m.startTime ASC")
    Optional<Meeting> findNextMeetingByProject(@Param("project") Project project, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Counts meetings for a specific project by ID.
     * 
     * @param projectId the project ID
     * @return the count of meetings for the project
     */
    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
    
    // =========================================================================
    // PHASE 3A: ENHANCED MEETING MANAGEMENT QUERIES
    // =========================================================================
    
    /**
     * Finds meetings by status.
     */
    List<Meeting> findByStatus(MeetingStatus status);
    
    /**
     * Finds meetings by meeting type.
     */
    List<Meeting> findByMeetingType(MeetingType meetingType);
    
    /**
     * Finds meetings by priority.
     */
    List<Meeting> findByPriority(MeetingPriority priority);
    
    /**
     * Finds meetings by location.
     */
    List<Meeting> findByLocationContainingIgnoreCase(String location);
    
    /**
     * Finds meetings created by a specific user.
     */
    List<Meeting> findByCreatedBy(String createdBy);
    
    /**
     * Finds meetings created within a date range.
     */
    List<Meeting> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds virtual meetings (those with virtual meeting URLs).
     */
    @Query("SELECT m FROM Meeting m WHERE m.virtualMeetingUrl IS NOT NULL AND m.virtualMeetingUrl != ''")
    List<Meeting> findVirtualMeetings();
    
    /**
     * Finds hybrid meetings (those with both location and virtual URL).
     */
    @Query("SELECT m FROM Meeting m WHERE m.location IS NOT NULL AND m.location != '' AND m.virtualMeetingUrl IS NOT NULL AND m.virtualMeetingUrl != ''")
    List<Meeting> findHybridMeetings();
    
    /**
     * Finds meetings requiring preparation.
     */
    List<Meeting> findByRequiresPreparationTrue();
    
    /**
     * Finds recurring meetings.
     */
    List<Meeting> findByIsRecurringTrue();
    
    /**
     * Finds meetings happening today.
     */
    @Query("SELECT m FROM Meeting m WHERE m.date = :today ORDER BY m.startTime ASC")
    List<Meeting> findTodaysMeetings(@Param("today") LocalDate today);
    
    /**
     * Finds meetings happening this week.
     */
    @Query("SELECT m FROM Meeting m WHERE m.date >= :startOfWeek AND m.date <= :endOfWeek ORDER BY m.date ASC, m.startTime ASC")
    List<Meeting> findThisWeeksMeetings(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
    
    /**
     * Finds meetings that are currently in progress.
     */
    @Query("SELECT m FROM Meeting m WHERE m.date = :today AND m.startTime <= :currentTime AND m.endTime > :currentTime")
    List<Meeting> findMeetingsInProgress(@Param("today") LocalDate today, @Param("currentTime") LocalTime currentTime);
    
    /**
     * Finds overbooked meetings (more attendees than max allowed).
     */
    // @Query("SELECT m FROM Meeting m WHERE m.maxAttendees IS NOT NULL AND SIZE(m.attendances) > m.maxAttendees")
    // List<Meeting> findOverbookedMeetings();
    
    /**
     * Finds meetings that need reminders to be sent.
     */
    // @Query("SELECT m FROM Meeting m WHERE m.reminderEnabled = true AND m.date >= :today AND " +
    //        "FUNCTION('TIMESTAMPDIFF', 'MINUTE', :now, FUNCTION('TIMESTAMP', m.date, m.startTime)) <= m.reminderMinutesBefore")
    // List<Meeting> findMeetingsNeedingReminders(@Param("today") LocalDate today, @Param("now") LocalDateTime now);
    
    /**
     * Finds the next meeting for a project.
     */
    @Query("SELECT m FROM Meeting m WHERE m.project.id = :projectId AND " +
           "(m.date > :today OR (m.date = :today AND m.startTime > :currentTime)) " +
           "ORDER BY m.date ASC, m.startTime ASC")
    Optional<Meeting> findNextMeetingForProject(@Param("projectId") Long projectId, 
                                               @Param("today") LocalDate today, 
                                               @Param("currentTime") LocalTime currentTime);
    
    /**
     * Detects scheduling conflicts for a given time period, excluding a specific meeting.
     */
    @Query("SELECT m FROM Meeting m WHERE m.date = :date AND m.id != :excludeMeetingId AND " +
           "((m.startTime <= :startTime AND m.endTime > :startTime) OR " +
           "(m.startTime < :endTime AND m.endTime >= :endTime) OR " +
           "(m.startTime >= :startTime AND m.endTime <= :endTime))")
    List<Meeting> findSchedulingConflicts(@Param("date") LocalDate date, 
                                         @Param("startTime") LocalTime startTime, 
                                         @Param("endTime") LocalTime endTime,
                                         @Param("excludeMeetingId") Long excludeMeetingId);
    
    // Note: searchMeetings query removed - LIKE validation issues in H2
    
    /**
     * Finds meetings by project and status.
     */
    List<Meeting> findByProjectAndStatus(Project project, MeetingStatus status);
    
    /**
     * Finds meetings by project and meeting type.
     */
    List<Meeting> findByProjectAndMeetingType(Project project, MeetingType meetingType);
    
    /**
     * Finds meetings by project and priority.
     */
    List<Meeting> findByProjectAndPriority(Project project, MeetingPriority priority);
    
    /**
     * Finds meetings by project within a date range and status.
     */
    List<Meeting> findByProjectAndDateBetweenAndStatus(Project project, LocalDate startDate, LocalDate endDate, MeetingStatus status);
    
    /**
     * Counts meetings by status.
     */
    long countByStatus(MeetingStatus status);
    
    /**
     * Counts meetings by meeting type.
     */
    long countByMeetingType(MeetingType meetingType);
    
    /**
     * Counts meetings by priority.
     */
    long countByPriority(MeetingPriority priority);
    
    /**
     * Counts meetings by project and status.
     */
    long countByProjectAndStatus(Project project, MeetingStatus status);
    
    /**
     * Gets meeting statistics for a project.
     */
    // @Query("SELECT COUNT(m) as totalMeetings, " +
    //        "SUM(CASE WHEN m.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedMeetings, " +
    //        "SUM(CASE WHEN m.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledMeetings, " +
    //        "AVG(FUNCTION('TIMESTAMPDIFF', 'MINUTE', m.startTime, m.endTime)) as avgDurationMinutes " +
    //        "FROM Meeting m WHERE m.project.id = :projectId")
    // Object[] getMeetingStatisticsByProject(@Param("projectId") Long projectId);
    
    /**
     * Gets meeting statistics for a date range.
     */
    // @Query("SELECT COUNT(m) as totalMeetings, " +
    //        "SUM(CASE WHEN m.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedMeetings, " +
    //        "SUM(CASE WHEN m.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledMeetings, " +
    //        "AVG(FUNCTION('TIMESTAMPDIFF', 'MINUTE', m.startTime, m.endTime)) as avgDurationMinutes " +
    //        "FROM Meeting m WHERE m.date BETWEEN :startDate AND :endDate")
    // Object[] getMeetingStatisticsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Finds meetings with specific recurrence pattern.
     */
    List<Meeting> findByRecurrencePattern(String recurrencePattern);
    
    /**
     * Finds meetings ordered by priority (highest first) and date.
     */
    @Query("SELECT m FROM Meeting m ORDER BY " +
           "CASE m.priority " +
           "WHEN 'EMERGENCY' THEN 1 " +
           "WHEN 'CRITICAL' THEN 2 " +
           "WHEN 'HIGH' THEN 3 " +
           "WHEN 'MEDIUM' THEN 4 " +
           "WHEN 'LOW' THEN 5 " +
           "END ASC, m.date ASC, m.startTime ASC")
    List<Meeting> findAllOrderedByPriorityAndDate();
    
    // Note: findByAgendaContaining query removed - LIKE validation issues in H2
    
    /**
     * Finds meetings with action items.
     */
    @Query("SELECT m FROM Meeting m WHERE m.actionItems IS NOT NULL AND m.actionItems != ''")
    List<Meeting> findMeetingsWithActionItems();
}