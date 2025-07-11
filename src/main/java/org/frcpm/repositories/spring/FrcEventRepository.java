// src/main/java/org/frcpm/repositories/spring/FrcEventRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.FrcEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FrcEvent entities.
 * 
 * Provides data access methods for FRC events with support for
 * API synchronization, timeline integration, and competition tracking.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
 */
@Repository
public interface FrcEventRepository extends JpaRepository<FrcEvent, Long> {
    
    /**
     * Find event by event code and season year.
     * Used for API synchronization to check for existing events.
     */
    Optional<FrcEvent> findByEventCodeAndSeasonYear(String eventCode, Integer seasonYear);
    
    /**
     * Find all events for a specific season year.
     */
    List<FrcEvent> findBySeasonYearOrderByStartDateAsc(Integer seasonYear);
    
    /**
     * Find events by type and season.
     */
    List<FrcEvent> findByEventTypeAndSeasonYearOrderByStartDateAsc(
        FrcEvent.EventType eventType, Integer seasonYear);
    
    /**
     * Find upcoming events (start date in the future).
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.startDate > :currentDate ORDER BY e.startDate ASC")
    List<FrcEvent> findUpcomingEvents(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find events happening in a date range.
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.startDate <= :endDate AND e.endDate >= :startDate ORDER BY e.startDate ASC")
    List<FrcEvent> findEventsInDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * Find events by location (city, state/province, or country).
     */
    @Query("SELECT e FROM FrcEvent e WHERE " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(e.stateProvince) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(e.country) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<FrcEvent> findByLocationContaining(@Param("location") String location);
    
    /**
     * Find events that need synchronization (not synced recently).
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.lastSynced IS NULL OR e.lastSynced < :cutoffTime")
    List<FrcEvent> findEventsNeedingSync(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find events linked to projects.
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.linkedProject IS NOT NULL")
    List<FrcEvent> findEventsLinkedToProjects();
    
    /**
     * Find events not linked to any project.
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.linkedProject IS NULL AND e.startDate > :currentDate")
    List<FrcEvent> findUnlinkedUpcomingEvents(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find current season events for a specific team.
     * This would require a separate team_events join table in full implementation.
     */
    @Query("SELECT e FROM FrcEvent e WHERE e.seasonYear = :seasonYear AND e.isOfficial = true ORDER BY e.startDate ASC")
    List<FrcEvent> findOfficialEventsBySeasonYear(@Param("seasonYear") Integer seasonYear);
    
    /**
     * Count events by type and season.
     */
    @Query("SELECT COUNT(e) FROM FrcEvent e WHERE e.eventType = :eventType AND e.seasonYear = :seasonYear")
    Long countByEventTypeAndSeasonYear(@Param("eventType") FrcEvent.EventType eventType, 
                                      @Param("seasonYear") Integer seasonYear);
    
    /**
     * Find events with registration currently open.
     */
    @Query("SELECT e FROM FrcEvent e WHERE " +
           "e.registrationOpen <= :now AND e.registrationClose >= :now " +
           "ORDER BY e.registrationClose ASC")
    List<FrcEvent> findEventsWithOpenRegistration(@Param("now") LocalDateTime now);
    
    /**
     * Find events happening this week.
     */
    @Query("SELECT e FROM FrcEvent e WHERE " +
           "e.startDate >= :weekStart AND e.startDate <= :weekEnd " +
           "ORDER BY e.startDate ASC")
    List<FrcEvent> findEventsThisWeek(@Param("weekStart") LocalDate weekStart, 
                                     @Param("weekEnd") LocalDate weekEnd);
}

