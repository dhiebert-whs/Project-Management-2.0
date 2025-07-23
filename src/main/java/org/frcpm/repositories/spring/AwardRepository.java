// src/main/java/org/frcpm/repositories/spring/AwardRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Award entities.
 * 
 * Provides database access methods for award tracking and analytics,
 * including team performance, seasonal analysis, and achievement reporting.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-Awards
 * @since Phase 4A.5 Regional Award Tracking System
 */
@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {
    
    // =========================================================================
    // BASIC QUERIES
    // =========================================================================
    
    /**
     * Finds all active awards.
     */
    List<Award> findByIsActiveTrueOrderByEventDateDesc();
    
    /**
     * Finds awards by team number.
     */
    List<Award> findByTeamNumberAndIsActiveTrueOrderByEventDateDesc(Integer teamNumber);
    
    /**
     * Finds awards by season.
     */
    List<Award> findBySeasonAndIsActiveTrueOrderByEventDateDesc(Integer season);
    
    /**
     * Finds awards by team number and season.
     */
    List<Award> findByTeamNumberAndSeasonAndIsActiveTrueOrderByEventDateDesc(Integer teamNumber, Integer season);
    
    /**
     * Finds awards by award type.
     */
    List<Award> findByAwardTypeAndIsActiveTrueOrderByEventDateDesc(Award.AwardType awardType);
    
    /**
     * Finds awards by award level.
     */
    List<Award> findByAwardLevelAndIsActiveTrueOrderByEventDateDesc(Award.AwardLevel awardLevel);
    
    /**
     * Finds awards by competition type.
     */
    List<Award> findByCompetitionTypeAndIsActiveTrueOrderByEventDateDesc(Award.CompetitionType competitionType);
    
    /**
     * Finds awards by event code.
     */
    List<Award> findByEventCodeAndIsActiveTrueOrderByEventDateDesc(String eventCode);
    
    // =========================================================================
    // TEAM PERFORMANCE QUERIES
    // =========================================================================
    
    /**
     * Finds championship-qualifying awards for a team.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.isActive = true " +
           "AND a.isChampionshipQualifying = true " +
           "ORDER BY a.eventDate DESC")
    List<Award> findChampionshipQualifyingAwards(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Finds awards with district points for a team in a season.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.season = :season " +
           "AND a.isActive = true " +
           "AND a.isDistrictPointsEarning = true " +
           "AND a.districtPoints > 0 " +
           "ORDER BY a.districtPoints DESC")
    List<Award> findDistrictPointsAwards(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);
    
    /**
     * Calculates total district points for a team in a season.
     */
    @Query("SELECT SUM(a.districtPoints) FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.season = :season " +
           "AND a.isActive = true " +
           "AND a.isDistrictPointsEarning = true")
    Integer calculateTotalDistrictPoints(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);
    
    /**
     * Finds highest prestige awards for a team.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.isActive = true " +
           "AND a.awardType IN ('CHAIRMANS', 'ENGINEERING_INSPIRATION', 'WINNER', 'DISTRICT_CHAMPIONSHIP', 'REGIONAL_CHAMPIONSHIP') " +
           "ORDER BY a.eventDate DESC")
    List<Award> findHighPrestigeAwards(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Finds most recent awards for a team.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.isActive = true " +
           "ORDER BY a.eventDate DESC")
    List<Award> findRecentAwards(@Param("teamNumber") Integer teamNumber);
    
    // =========================================================================
    // SEASONAL ANALYSIS QUERIES
    // =========================================================================
    
    /**
     * Finds teams with the most awards in a season.
     */
    @Query("SELECT a.teamNumber, a.teamName, COUNT(a) as awardCount FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "GROUP BY a.teamNumber, a.teamName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findTeamsWithMostAwards(@Param("season") Integer season);
    
    /**
     * Finds teams with the most district points in a season.
     */
    @Query("SELECT a.teamNumber, a.teamName, SUM(a.districtPoints) as totalPoints FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "AND a.isDistrictPointsEarning = true " +
           "GROUP BY a.teamNumber, a.teamName " +
           "ORDER BY SUM(a.districtPoints) DESC")
    List<Object[]> findTopDistrictPointsTeams(@Param("season") Integer season);
    
    /**
     * Finds Chairman's Award winners by season.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "AND a.awardType = 'CHAIRMANS' " +
           "ORDER BY a.awardLevel DESC, a.eventDate DESC")
    List<Award> findChairmansAwardWinners(@Param("season") Integer season);
    
    /**
     * Finds championship winners by season and level.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "AND a.awardType IN ('WINNER', 'DISTRICT_CHAMPIONSHIP', 'REGIONAL_CHAMPIONSHIP', 'WORLD_CHAMPIONSHIP') " +
           "ORDER BY a.awardLevel DESC, a.eventDate DESC")
    List<Award> findChampionshipWinners(@Param("season") Integer season);
    
    // =========================================================================
    // EVENT AND COMPETITION ANALYSIS
    // =========================================================================
    
    /**
     * Finds all awards from a specific event.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.eventCode = :eventCode " +
           "AND a.isActive = true " +
           "ORDER BY a.awardType, a.placement")
    List<Award> findAwardsByEvent(@Param("eventCode") String eventCode);
    
    /**
     * Finds events with the most competitive awards.
     */
    @Query("SELECT a.eventCode, a.eventName, COUNT(a) as awardCount FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "GROUP BY a.eventCode, a.eventName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findMostCompetitiveEvents(@Param("season") Integer season);
    
    /**
     * Finds regional vs district award distribution.
     */
    @Query("SELECT a.competitionType, COUNT(a) as awardCount FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "GROUP BY a.competitionType " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findAwardDistributionByCompetitionType(@Param("season") Integer season);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    // Note: searchAwards query removed - LIKE CONCAT validation issues in H2
    
    /**
     * Finds awards within a date range.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.isActive = true " +
           "AND a.eventDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.eventDate DESC")
    List<Award> findAwardsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Finds unverified awards that need attention.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.isActive = true " +
           "AND a.isVerified = false " +
           "AND (a.awardType IN ('CHAIRMANS', 'WINNER', 'DISTRICT_CHAMPIONSHIP', 'REGIONAL_CHAMPIONSHIP') " +
           "     OR a.districtPoints > 50) " +
           "ORDER BY a.eventDate DESC")
    List<Award> findUnverifiedImportantAwards();
    
    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================
    
    /**
     * Gets award type distribution across all seasons.
     */
    @Query("SELECT a.awardType, COUNT(a) FROM Award a " +
           "WHERE a.isActive = true " +
           "GROUP BY a.awardType " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getAwardTypeDistribution();
    
    /**
     * Gets award level distribution for a season.
     */
    @Query("SELECT a.awardLevel, COUNT(a) FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "GROUP BY a.awardLevel " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getAwardLevelDistribution(@Param("season") Integer season);
    
    /**
     * Gets monthly award activity for a season.
     */
    @Query("SELECT EXTRACT(MONTH FROM a.eventDate), COUNT(a) FROM Award a " +
           "WHERE a.season = :season " +
           "AND a.isActive = true " +
           "GROUP BY EXTRACT(MONTH FROM a.eventDate) " +
           "ORDER BY EXTRACT(MONTH FROM a.eventDate)")
    List<Object[]> getMonthlyAwardActivity(@Param("season") Integer season);
    
    /**
     * Finds teams with consistent performance across multiple seasons.
     */
    @Query("SELECT a.teamNumber, a.teamName, COUNT(DISTINCT a.season) as seasonCount, COUNT(a) as totalAwards FROM Award a " +
           "WHERE a.isActive = true " +
           "AND a.awardType IN ('WINNER', 'FINALIST', 'CHAIRMANS', 'ENGINEERING_INSPIRATION') " +
           "GROUP BY a.teamNumber, a.teamName " +
           "HAVING COUNT(DISTINCT a.season) >= :minSeasons " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findConsistentPerformers(@Param("minSeasons") Integer minSeasons);
    
    // =========================================================================
    // TEAM HISTORY AND TRENDS
    // =========================================================================
    
    /**
     * Gets team's award history summary.
     */
    @Query("SELECT a.season, COUNT(a) as awardCount, SUM(a.districtPoints) as totalPoints FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.isActive = true " +
           "GROUP BY a.season " +
           "ORDER BY a.season DESC")
    List<Object[]> getTeamAwardHistory(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Finds team's best performing seasons.
     */
    @Query("SELECT a.season, COUNT(a) as awardCount, " +
           "       SUM(CASE WHEN a.awardType IN ('CHAIRMANS', 'WINNER', 'ENGINEERING_INSPIRATION') THEN 1 ELSE 0 END) as majorAwards " +
           "FROM Award a " +
           "WHERE a.teamNumber = :teamNumber " +
           "AND a.isActive = true " +
           "GROUP BY a.season " +
           "ORDER BY COUNT(a) DESC, majorAwards DESC")
    List<Object[]> getTeamBestSeasons(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Finds rookie teams that won awards in their first season.
     */
    // Note: findRookieAwards query removed - LIKE validation issues in H2
    
    // =========================================================================
    // VALIDATION AND MAINTENANCE
    // =========================================================================
    
    /**
     * Finds duplicate awards that may need consolidation.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.isActive = true " +
           "AND EXISTS (SELECT a2 FROM Award a2 " +
           "           WHERE a2.isActive = true " +
           "           AND a2.id != a.id " +
           "           AND a2.teamNumber = a.teamNumber " +
           "           AND a2.eventCode = a.eventCode " +
           "           AND a2.awardType = a.awardType) " +
           "ORDER BY a.teamNumber, a.eventCode, a.awardType")
    List<Award> findPotentialDuplicates();
    
    /**
     * Counts awards by verification status.
     */
    @Query("SELECT a.isVerified, COUNT(a) FROM Award a " +
           "WHERE a.isActive = true " +
           "GROUP BY a.isVerified")
    List<Object[]> getVerificationStatistics();
    
    /**
     * Finds awards missing critical information.
     */
    @Query("SELECT a FROM Award a " +
           "WHERE a.isActive = true " +
           "AND (a.eventDate IS NULL " +
           "     OR a.eventLocation IS NULL OR a.eventLocation = '' " +
           "     OR a.description IS NULL OR a.description = '') " +
           "ORDER BY a.eventDate DESC")
    List<Award> findIncompleteAwards();
}