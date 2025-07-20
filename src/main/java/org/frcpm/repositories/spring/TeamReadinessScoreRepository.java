// src/main/java/org/frcpm/repositories/spring/TeamReadinessScoreRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.TeamReadinessScore;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TeamReadinessScore entities.
 * 
 * Provides comprehensive data access for team readiness assessment including
 * historical tracking, benchmarking, trend analysis, and comparative analytics
 * for FRC team readiness evaluation.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.4 Team Readiness Scoring System
 */
@Repository
public interface TeamReadinessScoreRepository extends JpaRepository<TeamReadinessScore, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active readiness scores for a specific team and season.
     */
    List<TeamReadinessScore> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds the most recent readiness score for a team in a specific season.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isActive = true ORDER BY t.assessmentDate DESC LIMIT 1")
    Optional<TeamReadinessScore> findLatestByTeamAndSeason(@Param("teamNumber") Integer teamNumber, 
                                                          @Param("season") Integer season);

    /**
     * Finds readiness scores by team and readiness type.
     */
    List<TeamReadinessScore> findByTeamNumberAndReadinessTypeAndIsActiveTrue(
        Integer teamNumber, TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds readiness scores by assessment phase.
     */
    List<TeamReadinessScore> findByAssessmentPhaseAndIsActiveTrue(TeamReadinessScore.AssessmentPhase assessmentPhase);

    // =========================================================================
    // READINESS LEVEL ANALYSIS
    // =========================================================================

    /**
     * Finds teams by overall readiness level.
     */
    List<TeamReadinessScore> findByReadinessLevelAndSeasonAndIsActiveTrue(
        TeamReadinessScore.ReadinessLevel readinessLevel, Integer season);

    /**
     * Finds teams requiring immediate action in current season.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND (t.readinessLevel = 'CRITICAL' OR (t.readinessLevel = 'LOW' AND t.daysToReadiness < 14)) " +
           "ORDER BY t.overallReadinessScore ASC")
    List<TeamReadinessScore> findTeamsRequiringImmediateAction(@Param("season") Integer season);

    /**
     * Finds competition-ready teams.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore >= 70.0 AND t.robotReliability >= 75.0 AND t.driverProficiency >= 70.0")
    List<TeamReadinessScore> findCompetitionReadyTeams(@Param("season") Integer season);

    /**
     * Finds teams not competition ready.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND NOT (t.overallReadinessScore >= 70.0 AND t.robotReliability >= 75.0 AND t.driverProficiency >= 70.0)")
    List<TeamReadinessScore> findTeamsNotCompetitionReady(@Param("season") Integer season);

    // =========================================================================
    // SCORE RANGE ANALYSIS
    // =========================================================================

    /**
     * Finds teams with overall readiness score in specified range.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore BETWEEN :minScore AND :maxScore")
    List<TeamReadinessScore> findByScoreRange(@Param("season") Integer season, 
                                             @Param("minScore") Double minScore, 
                                             @Param("maxScore") Double maxScore);

    /**
     * Finds teams with high technical readiness (>= 80).
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.technicalReadiness >= 80.0 ORDER BY t.technicalReadiness DESC")
    List<TeamReadinessScore> findHighTechnicalReadinessTeams(@Param("season") Integer season);

    /**
     * Finds teams with low resource readiness (< 60).
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.resourceReadiness < 60.0 ORDER BY t.resourceReadiness ASC")
    List<TeamReadinessScore> findLowResourceReadinessTeams(@Param("season") Integer season);

    /**
     * Finds teams with excellent team readiness (>= 85).
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.teamReadiness >= 85.0 ORDER BY t.teamReadiness DESC")
    List<TeamReadinessScore> findExcellentTeamReadinessTeams(@Param("season") Integer season);

    // =========================================================================
    // TREND ANALYSIS
    // =========================================================================

    /**
     * Finds readiness scores for trend analysis over time.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.teamNumber = :teamNumber " +
           "AND t.readinessType = :readinessType AND t.isActive = true " +
           "ORDER BY t.assessmentDate ASC")
    List<TeamReadinessScore> findForTrendAnalysis(@Param("teamNumber") Integer teamNumber,
                                                 @Param("readinessType") TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds multi-season readiness history for a team.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.teamNumber = :teamNumber AND t.isActive = true " +
           "ORDER BY t.season DESC, t.assessmentDate DESC")
    List<TeamReadinessScore> findMultiSeasonHistory(@Param("teamNumber") Integer teamNumber);

    /**
     * Finds teams with improving readiness scores.
     */
    @Query("SELECT DISTINCT t1 FROM TeamReadinessScore t1 WHERE t1.season = :season AND t1.isActive = true " +
           "AND EXISTS (SELECT t2 FROM TeamReadinessScore t2 WHERE t2.teamNumber = t1.teamNumber " +
           "AND t2.season = :season AND t2.assessmentDate < t1.assessmentDate " +
           "AND t2.overallReadinessScore < t1.overallReadinessScore AND t2.isActive = true)")
    List<TeamReadinessScore> findImprovingTeams(@Param("season") Integer season);

    /**
     * Finds teams with declining readiness scores.
     */
    @Query("SELECT DISTINCT t1 FROM TeamReadinessScore t1 WHERE t1.season = :season AND t1.isActive = true " +
           "AND EXISTS (SELECT t2 FROM TeamReadinessScore t2 WHERE t2.teamNumber = t1.teamNumber " +
           "AND t2.season = :season AND t2.assessmentDate < t1.assessmentDate " +
           "AND t2.overallReadinessScore > t1.overallReadinessScore AND t2.isActive = true)")
    List<TeamReadinessScore> findDecliningTeams(@Param("season") Integer season);

    // =========================================================================
    // BENCHMARKING AND COMPARISON
    // =========================================================================

    /**
     * Calculates average readiness scores for benchmarking.
     */
    @Query("SELECT AVG(t.overallReadinessScore) FROM TeamReadinessScore t " +
           "WHERE t.season = :season AND t.readinessType = :readinessType AND t.isActive = true")
    Optional<Double> findAverageReadinessScore(@Param("season") Integer season,
                                              @Param("readinessType") TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds teams with above-average readiness scores.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore > (SELECT AVG(t2.overallReadinessScore) FROM TeamReadinessScore t2 " +
           "WHERE t2.season = :season AND t2.isActive = true)")
    List<TeamReadinessScore> findAboveAverageTeams(@Param("season") Integer season);

    /**
     * Finds teams with below-average readiness scores.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore < (SELECT AVG(t2.overallReadinessScore) FROM TeamReadinessScore t2 " +
           "WHERE t2.season = :season AND t2.isActive = true)")
    List<TeamReadinessScore> findBelowAverageTeams(@Param("season") Integer season);

    /**
     * Finds top performing teams by overall readiness.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "ORDER BY t.overallReadinessScore DESC LIMIT :limit")
    List<TeamReadinessScore> findTopPerformingTeams(@Param("season") Integer season, @Param("limit") Integer limit);

    // =========================================================================
    // CATEGORY-SPECIFIC ANALYSIS
    // =========================================================================

    /**
     * Finds teams with strongest technical readiness.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "ORDER BY t.technicalReadiness DESC LIMIT :limit")
    List<TeamReadinessScore> findStrongestTechnicalTeams(@Param("season") Integer season, @Param("limit") Integer limit);

    /**
     * Finds teams with strongest team readiness.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "ORDER BY t.teamReadiness DESC LIMIT :limit")
    List<TeamReadinessScore> findStrongestTeamReadinessTeams(@Param("season") Integer season, @Param("limit") Integer limit);

    /**
     * Finds teams with weakest areas needing support.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND (t.technicalReadiness < 50.0 OR t.teamReadiness < 50.0 OR t.resourceReadiness < 50.0 " +
           "OR t.processReadiness < 50.0 OR t.strategicReadiness < 50.0 OR t.competitionReadiness < 50.0)")
    List<TeamReadinessScore> findTeamsNeedingSupport(@Param("season") Integer season);

    // =========================================================================
    // RISK AND PERFORMANCE ANALYSIS
    // =========================================================================

    /**
     * Finds teams with high risk exposure.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.riskExposure > 60.0 ORDER BY t.riskExposure DESC")
    List<TeamReadinessScore> findHighRiskTeams(@Param("season") Integer season);

    /**
     * Finds teams with low mitigation strength.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.mitigationStrength < 50.0 ORDER BY t.mitigationStrength ASC")
    List<TeamReadinessScore> findLowMitigationTeams(@Param("season") Integer season);

    /**
     * Finds teams with high improvement potential.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.improvementPotential > 20.0 ORDER BY t.improvementPotential DESC")
    List<TeamReadinessScore> findHighImprovementPotentialTeams(@Param("season") Integer season);

    /**
     * Finds teams with predicted high competition performance.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.predictedCompetitionPerformance >= 80.0 ORDER BY t.predictedCompetitionPerformance DESC")
    List<TeamReadinessScore> findPredictedHighPerformers(@Param("season") Integer season);

    // =========================================================================
    // DATE AND TIME ANALYSIS
    // =========================================================================

    /**
     * Finds recent assessments within specified days.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.assessmentDate >= :cutoffDate ORDER BY t.assessmentDate DESC")
    List<TeamReadinessScore> findRecentAssessments(@Param("season") Integer season, @Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Finds assessments that are overdue for update.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.assessmentDate < :cutoffDate")
    List<TeamReadinessScore> findOverdueAssessments(@Param("season") Integer season, @Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Finds teams approaching readiness deadlines.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.daysToReadiness BETWEEN 1 AND :warningDays ORDER BY t.daysToReadiness ASC")
    List<TeamReadinessScore> findTeamsApproachingDeadlines(@Param("season") Integer season, @Param("warningDays") Integer warningDays);

    // =========================================================================
    // STATISTICAL ANALYSIS
    // =========================================================================

    /**
     * Calculates percentile ranking for team readiness scores.
     */
    @Query("SELECT COUNT(t) FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore < :teamScore")
    Long findTeamsBelowScore(@Param("season") Integer season, @Param("teamScore") Double teamScore);

    /**
     * Finds distribution of readiness levels in season.
     */
    @Query("SELECT t.readinessLevel, COUNT(t) FROM TeamReadinessScore t " +
           "WHERE t.season = :season AND t.isActive = true GROUP BY t.readinessLevel")
    List<Object[]> findReadinessLevelDistribution(@Param("season") Integer season);

    /**
     * Finds teams by assessment phase distribution.
     */
    @Query("SELECT t.assessmentPhase, COUNT(t) FROM TeamReadinessScore t " +
           "WHERE t.season = :season AND t.isActive = true GROUP BY t.assessmentPhase")
    List<Object[]> findAssessmentPhaseDistribution(@Param("season") Integer season);

    // =========================================================================
    // ASSESSOR AND APPROVAL TRACKING
    // =========================================================================

    /**
     * Finds assessments by specific assessor.
     */
    List<TeamReadinessScore> findByAssessedByAndSeasonAndIsActiveTrue(TeamMember assessedBy, Integer season);

    /**
     * Finds approved assessments.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.approvedBy IS NOT NULL AND t.approvedAt IS NOT NULL")
    List<TeamReadinessScore> findApprovedAssessments(@Param("season") Integer season);

    /**
     * Finds pending approval assessments.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.approvedBy IS NULL")
    List<TeamReadinessScore> findPendingApprovalAssessments(@Param("season") Integer season);

    // =========================================================================
    // BULK OPERATIONS AND REPORTING
    // =========================================================================

    /**
     * Finds all assessments for comprehensive reporting.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "ORDER BY t.teamNumber ASC, t.assessmentDate DESC")
    List<TeamReadinessScore> findForSeasonReport(@Param("season") Integer season);

    /**
     * Deactivates old assessments for a team.
     */
    @Query("UPDATE TeamReadinessScore t SET t.isActive = false WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.assessmentDate < :keepAfterDate")
    void deactivateOldAssessments(@Param("teamNumber") Integer teamNumber, 
                                 @Param("season") Integer season, 
                                 @Param("keepAfterDate") LocalDate keepAfterDate);

    /**
     * Counts active assessments by team and season.
     */
    @Query("SELECT COUNT(t) FROM TeamReadinessScore t WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.isActive = true")
    Long countActiveAssessments(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // READINESS TYPE SPECIFIC QUERIES
    // =========================================================================

    /**
     * Finds competition readiness assessments for event preparation.
     */
    List<TeamReadinessScore> findByReadinessTypeAndSeasonAndIsActiveTrue(
        TeamReadinessScore.ReadinessType readinessType, Integer season);

    /**
     * Finds build season readiness assessments.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.readinessType = 'BUILD_SEASON' " +
           "AND t.season = :season AND t.isActive = true ORDER BY t.overallReadinessScore DESC")
    List<TeamReadinessScore> findBuildSeasonReadiness(@Param("season") Integer season);

    /**
     * Finds technical readiness assessments.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.readinessType = 'TECHNICAL' " +
           "AND t.season = :season AND t.isActive = true ORDER BY t.technicalReadiness DESC")
    List<TeamReadinessScore> findTechnicalReadiness(@Param("season") Integer season);

    // =========================================================================
    // PERFORMANCE PREDICTION QUERIES
    // =========================================================================

    /**
     * Finds teams with specific maturity levels.
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND ((t.overallReadinessScore >= 90.0 AND t.assessmentPhase = 'PEAK_PERFORMANCE') " +
           "OR (t.overallReadinessScore >= 80.0 AND t.overallReadinessScore < 90.0) " +
           "OR (t.overallReadinessScore >= 65.0 AND t.overallReadinessScore < 80.0) " +
           "OR (t.overallReadinessScore >= 45.0 AND t.overallReadinessScore < 65.0) " +
           "OR t.overallReadinessScore < 45.0)")
    List<TeamReadinessScore> findByMaturityLevels(@Param("season") Integer season);

    /**
     * Finds world-class teams (90+ score and peak performance phase).
     */
    @Query("SELECT t FROM TeamReadinessScore t WHERE t.season = :season AND t.isActive = true " +
           "AND t.overallReadinessScore >= 90.0 AND t.assessmentPhase = 'PEAK_PERFORMANCE'")
    List<TeamReadinessScore> findWorldClassTeams(@Param("season") Integer season);
}