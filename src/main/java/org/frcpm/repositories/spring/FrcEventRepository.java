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

// src/main/java/org/frcpm/repositories/spring/FrcMatchRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.FrcMatch;
import org.frcpm.models.FrcEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FrcMatch entities.
 */
@Repository
public interface FrcMatchRepository extends JpaRepository<FrcMatch, Long> {
    
    /**
     * Find all matches for an event ordered by scheduled time.
     */
    List<FrcMatch> findByFrcEventOrderByScheduledTimeAsc(FrcEvent frcEvent);
    
    /**
     * Find matches by competition level for an event.
     */
    List<FrcMatch> findByFrcEventAndCompetitionLevelOrderByMatchNumberAsc(
        FrcEvent frcEvent, FrcMatch.CompetitionLevel competitionLevel);
    
    /**
     * Find upcoming matches for an event.
     */
    @Query("SELECT m FROM FrcMatch m WHERE m.frcEvent = :event AND m.scheduledTime > :now ORDER BY m.scheduledTime ASC")
    List<FrcMatch> findUpcomingMatches(@Param("event") FrcEvent event, 
                                      @Param("now") LocalDateTime now);
    
    /**
     * Find matches involving a specific team (stored in alliance strings).
     */
    @Query("SELECT m FROM FrcMatch m WHERE " +
           "m.redAllianceTeams LIKE CONCAT('%', :teamNumber, '%') OR " +
           "m.blueAllianceTeams LIKE CONCAT('%', :teamNumber, '%')")
    List<FrcMatch> findMatchesForTeam(@Param("teamNumber") Integer teamNumber);
}

// src/main/java/org/frcpm/repositories/spring/FrcTeamRankingRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.FrcTeamRanking;
import org.frcpm.models.FrcEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FrcTeamRanking entities.
 */
@Repository
public interface FrcTeamRankingRepository extends JpaRepository<FrcTeamRanking, Long> {
    
    /**
     * Find all rankings for an event ordered by rank.
     */
    List<FrcTeamRanking> findByFrcEventOrderByRankAsc(FrcEvent frcEvent);
    
    /**
     * Find ranking for a specific team at an event.
     */
    Optional<FrcTeamRanking> findByFrcEventAndTeamNumber(FrcEvent frcEvent, Integer teamNumber);
    
    /**
     * Find top N teams at an event.
     */
    @Query("SELECT r FROM FrcTeamRanking r WHERE r.frcEvent = :event AND r.rank <= :maxRank ORDER BY r.rank ASC")
    List<FrcTeamRanking> findTopRankings(@Param("event") FrcEvent event, 
                                        @Param("maxRank") Integer maxRank);
    
    /**
     * Find rankings for a specific team across multiple events.
     */
    List<FrcTeamRanking> findByTeamNumberOrderByFrcEvent_StartDateDesc(Integer teamNumber);
    
    /**
     * Get average ranking for a team in a season.
     */
    @Query("SELECT AVG(r.rank) FROM FrcTeamRanking r WHERE r.teamNumber = :teamNumber AND r.frcEvent.seasonYear = :seasonYear")
    Double getAverageRankForTeamInSeason(@Param("teamNumber") Integer teamNumber, 
                                        @Param("seasonYear") Integer seasonYear);
}