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

