// src/main/java/org/frcpm/repositories/spring/MeetingRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Meeting entity.
 * Extends the existing custom MeetingRepository interface and adds Spring Data JPA methods.
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>, org.frcpm.repositories.specific.MeetingRepository {
    
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
}