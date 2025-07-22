// src/main/java/org/frcpm/repositories/spring/SkillDevelopmentRecordRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.SkillDevelopmentRecord;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SkillDevelopmentRecord entities.
 * 
 * Provides comprehensive data access for skills development tracking including
 * competency assessments, learning paths, certification management, and
 * team-wide skills analytics for effective FRC team development.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.5 Skills Development Tracking
 */
@Repository
public interface SkillDevelopmentRecordRepository extends JpaRepository<SkillDevelopmentRecord, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active skill records for a specific team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds skill records by team member for a team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndTeamMemberAndIsActiveTrue(
        Integer teamNumber, Integer season, TeamMember teamMember);

    /**
     * Finds skill records by skill category for a team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndCategoryAndIsActiveTrue(
        Integer teamNumber, Integer season, SkillDevelopmentRecord.SkillCategory category);

    /**
     * Finds skill records by skill type for a team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndSkillTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, SkillDevelopmentRecord.SkillType skillType);

    /**
     * Finds skill records by competency level for a team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndCurrentLevelAndIsActiveTrue(
        Integer teamNumber, Integer season, SkillDevelopmentRecord.CompetencyLevel currentLevel);

    /**
     * Finds skill records by learning status for a team and season.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndLearningStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, SkillDevelopmentRecord.LearningStatus learningStatus);

    // =========================================================================
    // COMPETENCY ASSESSMENT QUERIES
    // =========================================================================

    /**
     * Finds records by competency score range for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competencyScore BETWEEN :minScore AND :maxScore AND sdr.isActive = true " +
           "ORDER BY sdr.competencyScore DESC")
    List<SkillDevelopmentRecord> findByCompetencyScoreRange(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("minScore") Double minScore,
                                                           @Param("maxScore") Double maxScore);

    /**
     * Finds high competency skill records for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competencyScore >= :minScore AND sdr.isActive = true " +
           "ORDER BY sdr.competencyScore DESC")
    List<SkillDevelopmentRecord> findHighCompetencyRecords(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minScore") Double minScore);

    /**
     * Finds skill records requiring improvement for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competencyScore < :maxScore AND sdr.isActive = true " +
           "ORDER BY sdr.competencyScore ASC")
    List<SkillDevelopmentRecord> findRecordsNeedingImprovement(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("maxScore") Double maxScore);

    /**
     * Finds records with recent assessments.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastAssessmentDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastAssessmentDate DESC")
    List<SkillDevelopmentRecord> findWithRecentAssessments(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("sinceDate") LocalDate sinceDate);

    /**
     * Finds records with upcoming assessments.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.nextAssessmentDate <= :beforeDate AND sdr.isActive = true " +
           "ORDER BY sdr.nextAssessmentDate ASC")
    List<SkillDevelopmentRecord> findWithUpcomingAssessments(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season,
                                                            @Param("beforeDate") LocalDate beforeDate);

    /**
     * Finds records with overdue assessments.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.nextAssessmentDate < CURRENT_DATE AND sdr.isActive = true " +
           "ORDER BY sdr.nextAssessmentDate ASC")
    List<SkillDevelopmentRecord> findWithOverdueAssessments(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season);

    // =========================================================================
    // LEARNING PATH QUERIES
    // =========================================================================

    /**
     * Finds active learning paths for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningStatus IN (org.frcpm.models.SkillDevelopmentRecord.LearningStatus.ACTIVE, " +
           "org.frcpm.models.SkillDevelopmentRecord.LearningStatus.IN_PROGRESS) AND sdr.isActive = true " +
           "ORDER BY sdr.learningStartDate ASC")
    List<SkillDevelopmentRecord> findActiveLearningPaths(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season);

    /**
     * Finds completed learning paths for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningStatus = org.frcpm.models.SkillDevelopmentRecord.LearningStatus.COMPLETED " +
           "AND sdr.isActive = true ORDER BY sdr.learningCompletionDate DESC")
    List<SkillDevelopmentRecord> findCompletedLearningPaths(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season);

    /**
     * Finds paused or on-hold learning paths for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningStatus IN (org.frcpm.models.SkillDevelopmentRecord.LearningStatus.PAUSED, " +
           "org.frcpm.models.SkillDevelopmentRecord.LearningStatus.ON_HOLD) AND sdr.isActive = true " +
           "ORDER BY sdr.lastUpdateDate DESC")
    List<SkillDevelopmentRecord> findPausedLearningPaths(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season);

    /**
     * Finds learning paths by completion date range.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningCompletionDate BETWEEN :startDate AND :endDate AND sdr.isActive = true " +
           "ORDER BY sdr.learningCompletionDate DESC")
    List<SkillDevelopmentRecord> findByLearningCompletionDateRange(@Param("teamNumber") Integer teamNumber,
                                                                  @Param("season") Integer season,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    /**
     * Finds learning paths with high efficiency.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningEfficiency >= :minEfficiency AND sdr.isActive = true " +
           "ORDER BY sdr.learningEfficiency DESC")
    List<SkillDevelopmentRecord> findHighEfficiencyLearning(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("minEfficiency") Double minEfficiency);

    /**
     * Finds learning paths with low efficiency requiring intervention.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.learningEfficiency < :maxEfficiency AND sdr.learningEfficiency > 0 AND sdr.isActive = true " +
           "ORDER BY sdr.learningEfficiency ASC")
    List<SkillDevelopmentRecord> findLowEfficiencyLearning(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("maxEfficiency") Double maxEfficiency);

    // =========================================================================
    // MILESTONE AND PROGRESS QUERIES
    // =========================================================================

    /**
     * Finds records by progress percentage range.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.progressPercentage BETWEEN :minProgress AND :maxProgress AND sdr.isActive = true " +
           "ORDER BY sdr.progressPercentage DESC")
    List<SkillDevelopmentRecord> findByProgressRange(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("minProgress") Double minProgress,
                                                    @Param("maxProgress") Double maxProgress);

    /**
     * Finds records with completed milestones.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.milestoneCount > 0 AND sdr.isActive = true ORDER BY sdr.milestoneCount DESC")
    List<SkillDevelopmentRecord> findWithCompletedMilestones(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season);

    /**
     * Finds records with recent milestone completion.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastAssessmentDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastAssessmentDate DESC")
    List<SkillDevelopmentRecord> findWithRecentMilestones(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("sinceDate") LocalDate sinceDate);

    /**
     * Finds records meeting target levels.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.currentLevel = sdr.targetLevel AND sdr.isActive = true " +
           "ORDER BY sdr.lastUpdateDate DESC")
    List<SkillDevelopmentRecord> findMeetingTargetLevel(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season);

    /**
     * Finds records below target levels requiring attention.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.currentLevel < sdr.targetLevel AND sdr.isActive = true " +
           "ORDER BY sdr.progressPercentage ASC")
    List<SkillDevelopmentRecord> findBelowTargetLevel(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season);

    // =========================================================================
    // PRACTICE AND APPLICATION QUERIES
    // =========================================================================

    /**
     * Finds records with high practice hours.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.practiceHours >= :minHours AND sdr.isActive = true ORDER BY sdr.practiceHours DESC")
    List<SkillDevelopmentRecord> findHighPracticeRecords(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("minHours") Integer minHours);

    /**
     * Finds records with recent practice sessions.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastPracticeDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastPracticeDate DESC")
    List<SkillDevelopmentRecord> findWithRecentPractice(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("sinceDate") LocalDate sinceDate);

    /**
     * Finds records with high practice effectiveness.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.practiceEffectiveness >= :minEffectiveness AND sdr.isActive = true " +
           "ORDER BY sdr.practiceEffectiveness DESC")
    List<SkillDevelopmentRecord> findHighPracticeEffectiveness(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("minEffectiveness") Double minEffectiveness);

    /**
     * Finds records with low practice effectiveness requiring intervention.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.practiceEffectiveness < :maxEffectiveness AND sdr.practiceEffectiveness > 0 " +
           "AND sdr.isActive = true ORDER BY sdr.practiceEffectiveness ASC")
    List<SkillDevelopmentRecord> findLowPracticeEffectiveness(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("maxEffectiveness") Double maxEffectiveness);

    /**
     * Finds records with high application success rates.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.applicationSuccessRate >= :minRate AND sdr.isActive = true " +
           "ORDER BY sdr.applicationSuccessRate DESC")
    List<SkillDevelopmentRecord> findHighApplicationSuccess(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("minRate") Double minRate);

    /**
     * Finds records with recent skill applications.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastApplicationDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastApplicationDate DESC")
    List<SkillDevelopmentRecord> findWithRecentApplications(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("sinceDate") LocalDate sinceDate);

    // =========================================================================
    // CERTIFICATION AND RECOGNITION QUERIES
    // =========================================================================

    /**
     * Finds records with active certifications.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.certificationStatus = org.frcpm.models.SkillDevelopmentRecord.CertificationStatus.CERTIFIED " +
           "AND sdr.isActive = true ORDER BY sdr.certificationDate DESC")
    List<SkillDevelopmentRecord> findWithActiveCertifications(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season);

    /**
     * Finds records with expiring certifications.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.certificationExpiryDate <= :beforeDate " +
           "AND sdr.certificationStatus = org.frcpm.models.SkillDevelopmentRecord.CertificationStatus.CERTIFIED " +
           "AND sdr.isActive = true ORDER BY sdr.certificationExpiryDate ASC")
    List<SkillDevelopmentRecord> findWithExpiringCertifications(@Param("teamNumber") Integer teamNumber,
                                                               @Param("season") Integer season,
                                                               @Param("beforeDate") LocalDate beforeDate);

    /**
     * Finds records with recognition achievements.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.recognitionCount > 0 AND sdr.isActive = true ORDER BY sdr.recognitionCount DESC")
    List<SkillDevelopmentRecord> findWithRecognitions(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season);

    /**
     * Finds records eligible for certification.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competencyScore >= :minScore AND sdr.practiceHours >= :minHours " +
           "AND sdr.certificationStatus = org.frcpm.models.SkillDevelopmentRecord.CertificationStatus.NOT_CERTIFIED " +
           "AND sdr.isActive = true ORDER BY sdr.competencyScore DESC")
    List<SkillDevelopmentRecord> findEligibleForCertification(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("minScore") Double minScore,
                                                             @Param("minHours") Integer minHours);

    /**
     * Finds records with recent certifications.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.certificationDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.certificationDate DESC")
    List<SkillDevelopmentRecord> findWithRecentCertifications(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("sinceDate") LocalDate sinceDate);

    // =========================================================================
    // RISK AND INTERVENTION QUERIES
    // =========================================================================

    /**
     * Finds records with high risk scores requiring intervention.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.riskScore >= :minRisk AND sdr.isActive = true ORDER BY sdr.riskScore DESC")
    List<SkillDevelopmentRecord> findHighRiskRecords(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("minRisk") Double minRisk);

    /**
     * Finds records requiring immediate attention.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND (sdr.interventionRequired = true OR sdr.riskScore >= :highRisk " +
           "OR sdr.daysSinceActivity > :maxInactiveDays) AND sdr.isActive = true " +
           "ORDER BY sdr.riskScore DESC")
    List<SkillDevelopmentRecord> findRequiringAttention(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("highRisk") Double highRisk,
                                                       @Param("maxInactiveDays") Integer maxInactiveDays);

    /**
     * Finds records with consistent performance issues.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.consistencyScore < :maxConsistency AND sdr.isActive = true " +
           "ORDER BY sdr.consistencyScore ASC")
    List<SkillDevelopmentRecord> findInconsistentPerformance(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season,
                                                            @Param("maxConsistency") Double maxConsistency);

    /**
     * Finds records with low engagement scores.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.engagementScore < :maxEngagement AND sdr.isActive = true " +
           "ORDER BY sdr.engagementScore ASC")
    List<SkillDevelopmentRecord> findLowEngagement(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("maxEngagement") Double maxEngagement);

    /**
     * Finds stagnant records with no recent progress.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.daysSinceActivity > :maxDays AND sdr.isActive = true ORDER BY sdr.daysSinceActivity DESC")
    List<SkillDevelopmentRecord> findStagnantRecords(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("maxDays") Integer maxDays);

    // =========================================================================
    // MENTORSHIP AND INSTRUCTION QUERIES
    // =========================================================================

    /**
     * Finds records with high instruction effectiveness.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.instructionEffectiveness >= :minEffectiveness AND sdr.isActive = true " +
           "ORDER BY sdr.instructionEffectiveness DESC")
    List<SkillDevelopmentRecord> findHighInstructionEffectiveness(@Param("teamNumber") Integer teamNumber,
                                                                 @Param("season") Integer season,
                                                                 @Param("minEffectiveness") Double minEffectiveness);

    /**
     * Finds records with high mentorship impact scores.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.mentorshipImpactScore >= :minImpact AND sdr.isActive = true " +
           "ORDER BY sdr.mentorshipImpactScore DESC")
    List<SkillDevelopmentRecord> findHighMentorshipImpact(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("minImpact") Double minImpact);

    /**
     * Finds records with recent mentorship sessions.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastMentorshipDate >= :sinceDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastMentorshipDate DESC")
    List<SkillDevelopmentRecord> findWithRecentMentorship(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("sinceDate") LocalDate sinceDate);

    /**
     * Finds records requiring additional mentorship support.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND (sdr.mentorshipImpactScore < :maxImpact OR sdr.instructionEffectiveness < :maxEffectiveness) " +
           "AND sdr.isActive = true ORDER BY sdr.mentorshipImpactScore ASC")
    List<SkillDevelopmentRecord> findNeedingMentorshipSupport(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("maxImpact") Double maxImpact,
                                                             @Param("maxEffectiveness") Double maxEffectiveness);

    // =========================================================================
    // COMPETITION READINESS QUERIES
    // =========================================================================

    /**
     * Finds competition-ready skills for a team and season.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competitionReadiness = true AND sdr.isActive = true " +
           "ORDER BY sdr.competitionReadinessScore DESC")
    List<SkillDevelopmentRecord> findCompetitionReadySkills(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season);

    /**
     * Finds skills critical for competition performance.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competitionCritical = true AND sdr.isActive = true " +
           "ORDER BY sdr.competitionReadinessScore DESC")
    List<SkillDevelopmentRecord> findCompetitionCriticalSkills(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season);

    /**
     * Finds skills with high performance impact.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.performanceImpactScore >= :minImpact AND sdr.isActive = true " +
           "ORDER BY sdr.performanceImpactScore DESC")
    List<SkillDevelopmentRecord> findHighPerformanceImpact(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minImpact") Double minImpact);

    /**
     * Finds skills with high competition utilization.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.competitionUtilization >= :minUtilization AND sdr.isActive = true " +
           "ORDER BY sdr.competitionUtilization DESC")
    List<SkillDevelopmentRecord> findHighCompetitionUtilization(@Param("teamNumber") Integer teamNumber,
                                                               @Param("season") Integer season,
                                                               @Param("minUtilization") Double minUtilization);

    // =========================================================================
    // ANALYTICS AND AGGREGATION QUERIES
    // =========================================================================

    /**
     * Counts skill records by category for a team and season.
     */
    @Query("SELECT sdr.category, COUNT(sdr) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.category ORDER BY COUNT(sdr) DESC")
    List<Object[]> countByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts skill records by skill type for a team and season.
     */
    @Query("SELECT sdr.skillType, COUNT(sdr) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.skillType ORDER BY COUNT(sdr) DESC")
    List<Object[]> countBySkillType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts skill records by competency level for a team and season.
     */
    @Query("SELECT sdr.currentLevel, COUNT(sdr) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.currentLevel ORDER BY sdr.currentLevel")
    List<Object[]> countByCompetencyLevel(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts skill records by learning status for a team and season.
     */
    @Query("SELECT sdr.learningStatus, COUNT(sdr) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.learningStatus ORDER BY COUNT(sdr) DESC")
    List<Object[]> countByLearningStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average competency score by category.
     */
    @Query("SELECT sdr.category, AVG(sdr.competencyScore) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.category ORDER BY AVG(sdr.competencyScore) DESC")
    List<Object[]> averageCompetencyByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total practice hours by skill type.
     */
    @Query("SELECT sdr.skillType, SUM(sdr.practiceHours) FROM SkillDevelopmentRecord sdr " +
           "WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season AND sdr.isActive = true " +
           "GROUP BY sdr.skillType ORDER BY SUM(sdr.practiceHours) DESC")
    List<Object[]> totalPracticeHoursBySkillType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most improved skills based on competency score increase.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.improvementRate > 0 AND sdr.isActive = true ORDER BY sdr.improvementRate DESC")
    List<SkillDevelopmentRecord> findMostImprovedSkills(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season);

    /**
     * Finds skills with high retention rates.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.retentionScore >= :minRetention AND sdr.isActive = true " +
           "ORDER BY sdr.retentionScore DESC")
    List<SkillDevelopmentRecord> findHighRetentionSkills(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("minRetention") Double minRetention);

    // =========================================================================
    // SEARCH AND FILTERING QUERIES
    // =========================================================================

    /**
     * Searches skill records by skill name.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND LOWER(sdr.skillName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND sdr.isActive = true " +
           "ORDER BY sdr.skillName ASC")
    List<SkillDevelopmentRecord> searchBySkillName(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("searchTerm") String searchTerm);

    /**
     * Searches skill records by description.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND LOWER(sdr.skillDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND sdr.isActive = true " +
           "ORDER BY sdr.skillName ASC")
    List<SkillDevelopmentRecord> searchByDescription(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("searchTerm") String searchTerm);

    /**
     * Full text search across skill records.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND (LOWER(sdr.skillName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(sdr.skillDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND sdr.isActive = true ORDER BY sdr.skillName ASC")
    List<SkillDevelopmentRecord> fullTextSearch(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("searchTerm") String searchTerm);

    // =========================================================================
    // TEAM MEMBER SPECIFIC QUERIES
    // =========================================================================

    /**
     * Finds team member's top skills by competency score.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.teamMember = :teamMember AND sdr.isActive = true " +
           "ORDER BY sdr.competencyScore DESC")
    List<SkillDevelopmentRecord> findTeamMemberTopSkills(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("teamMember") TeamMember teamMember);

    /**
     * Finds team member's skills needing improvement.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.teamMember = :teamMember AND sdr.currentLevel < sdr.targetLevel " +
           "AND sdr.isActive = true ORDER BY sdr.progressPercentage ASC")
    List<SkillDevelopmentRecord> findTeamMemberSkillsNeedingImprovement(@Param("teamNumber") Integer teamNumber,
                                                                       @Param("season") Integer season,
                                                                       @Param("teamMember") TeamMember teamMember);

    /**
     * Finds team member's completed certifications.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.teamMember = :teamMember " +
           "AND sdr.certificationStatus = org.frcpm.models.SkillDevelopmentRecord.CertificationStatus.CERTIFIED " +
           "AND sdr.isActive = true ORDER BY sdr.certificationDate DESC")
    List<SkillDevelopmentRecord> findTeamMemberCertifications(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("teamMember") TeamMember teamMember);

    // =========================================================================
    // CROSS-SEASON ANALYSIS QUERIES
    // =========================================================================

    /**
     * Finds skill records across multiple seasons for comparison.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber " +
           "AND sdr.season IN :seasons AND sdr.isActive = true " +
           "ORDER BY sdr.season DESC, sdr.competencyScore DESC")
    List<SkillDevelopmentRecord> findMultiSeasonRecords(@Param("teamNumber") Integer teamNumber,
                                                       @Param("seasons") List<Integer> seasons);

    /**
     * Finds skill evolution for specific team member across seasons.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber " +
           "AND sdr.teamMember = :teamMember AND sdr.skillName = :skillName " +
           "AND sdr.isActive = true ORDER BY sdr.season ASC")
    List<SkillDevelopmentRecord> findSkillEvolution(@Param("teamNumber") Integer teamNumber,
                                                   @Param("teamMember") TeamMember teamMember,
                                                   @Param("skillName") String skillName);

    // =========================================================================
    // BULK OPERATIONS QUERIES
    // =========================================================================

    /**
     * Counts total active skill records for a team and season.
     */
    Long countByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds all active skill records for bulk export.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.isActive = true ORDER BY sdr.skillName ASC")
    List<SkillDevelopmentRecord> findAllActiveRecords(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season);

    /**
     * Finds skill records created within date range.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.createdAt BETWEEN :startDate AND :endDate AND sdr.isActive = true " +
           "ORDER BY sdr.createdAt DESC")
    List<SkillDevelopmentRecord> findCreatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Finds skill records updated within date range.
     */
    @Query("SELECT sdr FROM SkillDevelopmentRecord sdr WHERE sdr.teamNumber = :teamNumber AND sdr.season = :season " +
           "AND sdr.lastUpdateDate BETWEEN :startDate AND :endDate AND sdr.isActive = true " +
           "ORDER BY sdr.lastUpdateDate DESC")
    List<SkillDevelopmentRecord> findUpdatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Finds archived skill records.
     */
    List<SkillDevelopmentRecord> findByTeamNumberAndSeasonAndIsActiveFalse(Integer teamNumber, Integer season);
}