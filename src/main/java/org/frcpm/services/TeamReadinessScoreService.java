// src/main/java/org/frcpm/services/TeamReadinessScoreService.java

package org.frcpm.services;

import org.frcpm.models.TeamReadinessScore;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for TeamReadinessScore operations.
 * 
 * Provides comprehensive team readiness assessment services including
 * scoring calculations, trend analysis, benchmarking, risk assessment,
 * and improvement recommendations for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.4 Team Readiness Scoring System
 */
public interface TeamReadinessScoreService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new readiness score record.
     */
    TeamReadinessScore create(TeamReadinessScore readinessScore);

    /**
     * Updates an existing readiness score record.
     */
    TeamReadinessScore update(Long id, TeamReadinessScore readinessScore);

    /**
     * Deletes a readiness score record (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a readiness score by ID.
     */
    Optional<TeamReadinessScore> findById(Long id);

    /**
     * Finds all active readiness scores.
     */
    List<TeamReadinessScore> findAll();

    /**
     * Checks if readiness score exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of readiness scores.
     */
    long count();

    // =========================================================================
    // READINESS SCORE MANAGEMENT
    // =========================================================================

    /**
     * Creates a new readiness score assessment.
     */
    TeamReadinessScore createReadinessScore(TeamReadinessScore readinessScore);

    /**
     * Creates a readiness score with basic parameters.
     */
    TeamReadinessScore createReadinessScore(Integer teamNumber, Integer season,
                                          TeamReadinessScore.ReadinessType readinessType,
                                          TeamReadinessScore.AssessmentPhase assessmentPhase,
                                          TeamMember assessedBy);

    /**
     * Updates an existing readiness score.
     */
    TeamReadinessScore updateReadinessScore(Long scoreId, TeamReadinessScore readinessScore);

    /**
     * Deactivates a readiness score record.
     */
    void deactivateReadinessScore(Long scoreId);

    /**
     * Finds active readiness scores for a team and season.
     */
    List<TeamReadinessScore> findActiveScores();

    // =========================================================================
    // TEAM AND SEASON QUERIES
    // =========================================================================

    /**
     * Finds all readiness scores for a specific team and season.
     */
    List<TeamReadinessScore> findByTeamAndSeason(Integer teamNumber, Integer season);

    /**
     * Finds the latest readiness score for a team in a season.
     */
    Optional<TeamReadinessScore> findLatestScoreByTeamAndSeason(Integer teamNumber, Integer season);

    /**
     * Finds readiness scores by team and readiness type.
     */
    List<TeamReadinessScore> findByTeamAndReadinessType(Integer teamNumber, 
                                                       TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds readiness scores by assessment phase.
     */
    List<TeamReadinessScore> findByAssessmentPhase(TeamReadinessScore.AssessmentPhase assessmentPhase);

    /**
     * Finds all teams assessed in a season.
     */
    List<TeamReadinessScore> findBySeasonAllTeams(Integer season);

    // =========================================================================
    // READINESS LEVEL AND SCORING
    // =========================================================================

    /**
     * Finds teams by overall readiness level.
     */
    List<TeamReadinessScore> findByReadinessLevel(TeamReadinessScore.ReadinessLevel readinessLevel, Integer season);

    /**
     * Finds teams requiring immediate action.
     */
    List<TeamReadinessScore> findTeamsRequiringImmediateAction(Integer season);

    /**
     * Finds competition-ready teams.
     */
    List<TeamReadinessScore> findCompetitionReadyTeams(Integer season);

    /**
     * Finds teams not ready for competition.
     */
    List<TeamReadinessScore> findTeamsNotCompetitionReady(Integer season);

    /**
     * Finds teams in a specific score range.
     */
    List<TeamReadinessScore> findByScoreRange(Integer season, Double minScore, Double maxScore);

    // =========================================================================
    // CATEGORY-SPECIFIC ANALYSIS
    // =========================================================================

    /**
     * Finds teams with high technical readiness.
     */
    List<TeamReadinessScore> findHighTechnicalReadinessTeams(Integer season);

    /**
     * Finds teams with low resource readiness.
     */
    List<TeamReadinessScore> findLowResourceReadinessTeams(Integer season);

    /**
     * Finds teams with excellent team readiness.
     */
    List<TeamReadinessScore> findExcellentTeamReadinessTeams(Integer season);

    /**
     * Finds teams needing support in any category.
     */
    List<TeamReadinessScore> findTeamsNeedingSupport(Integer season);

    /**
     * Finds strongest teams in specific categories.
     */
    List<TeamReadinessScore> findStrongestTechnicalTeams(Integer season, Integer limit);

    /**
     * Finds strongest teams in team readiness.
     */
    List<TeamReadinessScore> findStrongestTeamReadinessTeams(Integer season, Integer limit);

    // =========================================================================
    // TREND ANALYSIS
    // =========================================================================

    /**
     * Analyzes readiness trends for a team over time.
     */
    List<TeamReadinessScore> analyzeReadinessTrends(Integer teamNumber, 
                                                   TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds multi-season readiness history for a team.
     */
    List<TeamReadinessScore> findMultiSeasonHistory(Integer teamNumber);

    /**
     * Finds teams with improving readiness scores.
     */
    List<TeamReadinessScore> findImprovingTeams(Integer season);

    /**
     * Finds teams with declining readiness scores.
     */
    List<TeamReadinessScore> findDecliningTeams(Integer season);

    /**
     * Calculates readiness trend direction for a team.
     */
    String calculateReadinessTrend(Integer teamNumber, Integer season);

    /**
     * Predicts future readiness based on trends.
     */
    Double predictFutureReadiness(Integer teamNumber, Integer season, Integer daysAhead);

    // =========================================================================
    // BENCHMARKING AND COMPARISON
    // =========================================================================

    /**
     * Calculates average readiness scores for benchmarking.
     */
    Optional<Double> calculateAverageReadinessScore(Integer season, TeamReadinessScore.ReadinessType readinessType);

    /**
     * Finds teams with above-average readiness.
     */
    List<TeamReadinessScore> findAboveAverageTeams(Integer season);

    /**
     * Finds teams with below-average readiness.
     */
    List<TeamReadinessScore> findBelowAverageTeams(Integer season);

    /**
     * Finds top performing teams by overall readiness.
     */
    List<TeamReadinessScore> findTopPerformingTeams(Integer season, Integer limit);

    /**
     * Calculates percentile ranking for a team.
     */
    Double calculatePercentileRanking(Integer teamNumber, Integer season);

    /**
     * Compares team readiness against benchmarks.
     */
    Map<String, Object> compareAgainstBenchmarks(Integer teamNumber, Integer season);

    // =========================================================================
    // RISK ASSESSMENT
    // =========================================================================

    /**
     * Finds teams with high risk exposure.
     */
    List<TeamReadinessScore> findHighRiskTeams(Integer season);

    /**
     * Finds teams with low mitigation strength.
     */
    List<TeamReadinessScore> findLowMitigationTeams(Integer season);

    /**
     * Assesses overall risk for a team.
     */
    Map<String, Object> assessTeamRisk(Integer teamNumber, Integer season);

    /**
     * Calculates risk-adjusted readiness score.
     */
    Double calculateRiskAdjustedScore(Long scoreId);

    /**
     * Identifies critical risk factors for a team.
     */
    List<String> identifyCriticalRiskFactors(Long scoreId);

    // =========================================================================
    // IMPROVEMENT AND RECOMMENDATIONS
    // =========================================================================

    /**
     * Finds teams with high improvement potential.
     */
    List<TeamReadinessScore> findHighImprovementPotentialTeams(Integer season);

    /**
     * Generates improvement recommendations for a team.
     */
    Map<String, String> generateImprovementRecommendations(Long scoreId);

    /**
     * Identifies priority improvement areas.
     */
    Map<String, String> identifyPriorityImprovementAreas(Long scoreId);

    /**
     * Calculates effort required for readiness improvement.
     */
    Map<String, Object> calculateImprovementEffort(Long scoreId, Double targetScore);

    /**
     * Suggests action plan for readiness improvement.
     */
    List<String> suggestActionPlan(Long scoreId);

    // =========================================================================
    // PERFORMANCE PREDICTION
    // =========================================================================

    /**
     * Finds teams with predicted high competition performance.
     */
    List<TeamReadinessScore> findPredictedHighPerformers(Integer season);

    /**
     * Predicts competition performance for a team.
     */
    Double predictCompetitionPerformance(Long scoreId);

    /**
     * Calculates team maturity level.
     */
    String calculateMaturityLevel(Long scoreId);

    /**
     * Finds world-class teams.
     */
    List<TeamReadinessScore> findWorldClassTeams(Integer season);

    /**
     * Estimates time to reach target readiness.
     */
    Integer estimateTimeToTargetReadiness(Long scoreId, Double targetScore);

    // =========================================================================
    // ASSESSMENT MANAGEMENT
    // =========================================================================

    /**
     * Finds recent assessments within specified days.
     */
    List<TeamReadinessScore> findRecentAssessments(Integer season, Integer withinDays);

    /**
     * Finds assessments that are overdue for update.
     */
    List<TeamReadinessScore> findOverdueAssessments(Integer season, Integer overdueAfterDays);

    /**
     * Finds teams approaching readiness deadlines.
     */
    List<TeamReadinessScore> findTeamsApproachingDeadlines(Integer season, Integer warningDays);

    /**
     * Validates assessment completeness.
     */
    boolean validateAssessmentCompleteness(Long scoreId);

    /**
     * Schedules next assessment date.
     */
    LocalDate scheduleNextAssessment(Long scoreId);

    // =========================================================================
    // SCORING CALCULATIONS
    // =========================================================================

    /**
     * Calculates overall readiness score from category scores.
     */
    void calculateOverallReadinessScore(Long scoreId);

    /**
     * Recalculates all category scores.
     */
    void recalculateAllCategoryScores(Long scoreId);

    /**
     * Updates specific category score.
     */
    void updateCategoryScore(Long scoreId, String category);

    /**
     * Validates scoring consistency.
     */
    boolean validateScoringConsistency(Long scoreId);

    /**
     * Normalizes scores to standard scale.
     */
    void normalizeScores(Long scoreId);

    // =========================================================================
    // STATISTICAL ANALYSIS
    // =========================================================================

    /**
     * Calculates readiness level distribution for season.
     */
    Map<TeamReadinessScore.ReadinessLevel, Long> calculateReadinessLevelDistribution(Integer season);

    /**
     * Calculates assessment phase distribution.
     */
    Map<TeamReadinessScore.AssessmentPhase, Long> calculateAssessmentPhaseDistribution(Integer season);

    /**
     * Calculates statistical summary for season.
     */
    Map<String, Object> calculateSeasonStatistics(Integer season);

    /**
     * Finds correlation between categories.
     */
    Map<String, Double> analyzeCategoryCorrelations(Integer season);

    /**
     * Calculates variance in readiness scores.
     */
    Double calculateReadinessVariance(Integer season);

    // =========================================================================
    // APPROVAL AND WORKFLOW
    // =========================================================================

    /**
     * Finds assessments by specific assessor.
     */
    List<TeamReadinessScore> findByAssessor(TeamMember assessor, Integer season);

    /**
     * Finds approved assessments.
     */
    List<TeamReadinessScore> findApprovedAssessments(Integer season);

    /**
     * Finds assessments pending approval.
     */
    List<TeamReadinessScore> findPendingApprovalAssessments(Integer season);

    /**
     * Approves a readiness score assessment.
     */
    TeamReadinessScore approveAssessment(Long scoreId, TeamMember approver);

    /**
     * Submits assessment for approval.
     */
    TeamReadinessScore submitForApproval(Long scoreId);

    // =========================================================================
    // REPORTING AND ANALYTICS
    // =========================================================================

    /**
     * Generates comprehensive season report.
     */
    Map<String, Object> generateSeasonReport(Integer season);

    /**
     * Generates team readiness report.
     */
    Map<String, Object> generateTeamReport(Integer teamNumber, Integer season);

    /**
     * Generates comparative analysis report.
     */
    Map<String, Object> generateComparativeAnalysis(Integer season);

    /**
     * Exports readiness data for external analysis.
     */
    List<Map<String, Object>> exportReadinessData(Integer season);

    /**
     * Generates executive summary.
     */
    String generateExecutiveSummary(Integer season);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Creates multiple readiness scores.
     */
    List<TeamReadinessScore> createBulkReadinessScores(List<TeamReadinessScore> scores);

    /**
     * Updates multiple readiness scores.
     */
    List<TeamReadinessScore> updateBulkReadinessScores(Map<Long, TeamReadinessScore> scoreUpdates);

    /**
     * Deactivates old assessments for maintenance.
     */
    void deactivateOldAssessments(Integer teamNumber, Integer season, LocalDate keepAfterDate);

    /**
     * Archives completed season assessments.
     */
    void archiveSeasonAssessments(Integer season);

    /**
     * Validates bulk assessment data.
     */
    List<String> validateBulkAssessments(List<TeamReadinessScore> scores);

    // =========================================================================
    // INTEGRATION AND SYNC
    // =========================================================================

    /**
     * Syncs readiness scores with external systems.
     */
    void syncWithExternalSystems(Integer season);

    /**
     * Imports readiness data from external source.
     */
    List<TeamReadinessScore> importReadinessData(String dataSource, Integer season);

    /**
     * Exports readiness scores to external format.
     */
    String exportToFormat(Integer season, String format);

    /**
     * Validates data integrity.
     */
    boolean validateDataIntegrity(Integer season);

    /**
     * Reconciles assessment discrepancies.
     */
    List<String> reconcileAssessmentDiscrepancies(Integer season);
}