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