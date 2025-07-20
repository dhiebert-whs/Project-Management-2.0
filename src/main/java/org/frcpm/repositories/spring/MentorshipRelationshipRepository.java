// src/main/java/org/frcpm/repositories/spring/MentorshipRelationshipRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.MentorshipRelationship;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MentorshipRelationship entities.
 * 
 * Provides comprehensive data access for mentorship relationship management
 * including relationship analytics, progress monitoring, and mentorship
 * tracking for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.3 Mentorship Tracking System
 */
@Repository
public interface MentorshipRelationshipRepository extends JpaRepository<MentorshipRelationship, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active relationships for a specific team and season.
     */
    List<MentorshipRelationship> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds relationships by status for a team and season.
     */
    List<MentorshipRelationship> findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, MentorshipRelationship.MentorshipStatus status);

    /**
     * Finds relationships by type for a team and season.
     */
    List<MentorshipRelationship> findByTeamNumberAndSeasonAndRelationshipTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, MentorshipRelationship.RelationshipType relationshipType);

    /**
     * Finds relationships by primary focus for a team and season.
     */
    List<MentorshipRelationship> findByTeamNumberAndSeasonAndPrimaryFocusAndIsActiveTrue(
        Integer teamNumber, Integer season, MentorshipRelationship.MentorshipFocus primaryFocus);

    /**
     * Finds relationships by mentor.
     */
    List<MentorshipRelationship> findByMentorAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember mentor, Integer teamNumber, Integer season);

    /**
     * Finds relationships by mentee.
     */
    List<MentorshipRelationship> findByMenteeAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember mentee, Integer teamNumber, Integer season);

    // =========================================================================
    // RELATIONSHIP ANALYTICS
    // =========================================================================

    /**
     * Finds active relationships for specific mentor.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.mentor = :mentor " +
           "AND mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findActiveMentorRelationships(@Param("mentor") TeamMember mentor,
                                                              @Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season);

    /**
     * Finds active relationships for specific mentee.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.mentee = :mentee " +
           "AND mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findActiveMenteeRelationships(@Param("mentee") TeamMember mentee,
                                                              @Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season);

    /**
     * Counts total mentoring relationships for a mentor.
     */
    @Query("SELECT COUNT(mr) FROM MentorshipRelationship mr WHERE mr.mentor = :mentor " +
           "AND mr.teamNumber = :teamNumber AND mr.season = :season AND mr.isActive = true")
    Long countMentorRelationships(@Param("mentor") TeamMember mentor,
                                 @Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season);

    /**
     * Counts total mentee relationships for a team member.
     */
    @Query("SELECT COUNT(mr) FROM MentorshipRelationship mr WHERE mr.mentee = :mentee " +
           "AND mr.teamNumber = :teamNumber AND mr.season = :season AND mr.isActive = true")
    Long countMenteeRelationships(@Param("mentee") TeamMember mentee,
                                 @Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season);

    /**
     * Finds successful mentorship relationships (completed with high satisfaction).
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.status = 'COMPLETED' " +
           "AND mr.overallSatisfactionScore >= :minSatisfaction AND mr.isActive = true")
    List<MentorshipRelationship> findSuccessfulRelationships(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season,
                                                            @Param("minSatisfaction") Double minSatisfaction);

    /**
     * Finds relationships requiring attention (low engagement or satisfaction).
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.status IN ('ACTIVE', 'PROGRESSING') " +
           "AND (mr.engagementScore < :minEngagement OR mr.overallSatisfactionScore < :minSatisfaction " +
           "OR mr.riskScore >= :maxRisk) AND mr.isActive = true")
    List<MentorshipRelationship> findRelationshipsNeedingAttention(@Param("teamNumber") Integer teamNumber,
                                                                  @Param("season") Integer season,
                                                                  @Param("minEngagement") Double minEngagement,
                                                                  @Param("minSatisfaction") Double minSatisfaction,
                                                                  @Param("maxRisk") Double maxRisk);

    // =========================================================================
    // PROGRESS AND GOAL TRACKING
    // =========================================================================

    /**
     * Finds relationships with upcoming milestones.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.nextMilestoneDate BETWEEN :startDate AND :endDate " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findWithUpcomingMilestones(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * Finds relationships with overdue milestones.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.nextMilestoneDate < :currentDate " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findWithOverdueMilestones(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("currentDate") LocalDate currentDate);

    /**
     * Finds relationships by goal completion percentage range.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.goalCompletionPercentage BETWEEN :minPercentage AND :maxPercentage " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findByGoalCompletionRange(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minPercentage") Double minPercentage,
                                                          @Param("maxPercentage") Double maxPercentage);

    /**
     * Finds relationships with high skill development progress.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.skillDevelopmentScore >= :minScore " +
           "AND mr.isActive = true ORDER BY mr.skillDevelopmentScore DESC")
    List<MentorshipRelationship> findHighSkillDevelopment(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("minScore") Double minScore);

    // =========================================================================
    // MEETING AND INTERACTION TRACKING
    // =========================================================================

    /**
     * Finds relationships with low meeting attendance.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.attendanceRate < :minAttendance " +
           "AND mr.totalMeetings >= :minMeetings AND mr.status IN ('ACTIVE', 'PROGRESSING') " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findLowAttendanceRelationships(@Param("teamNumber") Integer teamNumber,
                                                               @Param("season") Integer season,
                                                               @Param("minAttendance") Double minAttendance,
                                                               @Param("minMeetings") Integer minMeetings);

    /**
     * Finds relationships with high meeting frequency.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.meetingFrequencyScore >= :minFrequency " +
           "AND mr.isActive = true ORDER BY mr.meetingFrequencyScore DESC")
    List<MentorshipRelationship> findHighMeetingFrequency(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("minFrequency") Double minFrequency);

    /**
     * Finds relationships requiring meeting scheduling.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.daysSinceLastMeeting > :maxDays " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findNeedingMeetingScheduling(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("maxDays") Integer maxDays);

    /**
     * Calculates average meeting duration for relationships.
     */
    @Query("SELECT AVG(mr.averageMeetingDurationMinutes) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.completedMeetings > 0 AND mr.isActive = true")
    Optional<Double> findAverageMeetingDuration(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season);

    // =========================================================================
    // ENGAGEMENT AND SATISFACTION ANALYTICS
    // =========================================================================

    /**
     * Finds most engaging mentorship relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.engagementScore >= :minScore " +
           "AND mr.isActive = true ORDER BY mr.engagementScore DESC")
    List<MentorshipRelationship> findMostEngaging(@Param("teamNumber") Integer teamNumber,
                                                 @Param("season") Integer season,
                                                 @Param("minScore") Double minScore);

    /**
     * Finds highest satisfaction relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.overallSatisfactionScore >= :minScore " +
           "AND mr.feedbackCount >= :minFeedback AND mr.isActive = true " +
           "ORDER BY mr.overallSatisfactionScore DESC")
    List<MentorshipRelationship> findHighestSatisfaction(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("minScore") Double minScore,
                                                        @Param("minFeedback") Integer minFeedback);

    /**
     * Finds relationships with low engagement scores.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.engagementScore < :maxScore " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true " +
           "ORDER BY mr.engagementScore ASC")
    List<MentorshipRelationship> findLowEngagement(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("maxScore") Double maxScore);

    /**
     * Calculates average satisfaction score for team.
     */
    @Query("SELECT AVG(mr.overallSatisfactionScore) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.feedbackCount > 0 AND mr.isActive = true")
    Optional<Double> findAverageSatisfactionScore(@Param("teamNumber") Integer teamNumber,
                                                 @Param("season") Integer season);

    // =========================================================================
    // MENTOR PERFORMANCE AND EFFECTIVENESS
    // =========================================================================

    /**
     * Finds most effective mentors by relationship success.
     */
    @Query("SELECT mr.mentor, COUNT(mr), AVG(mr.overallSatisfactionScore), AVG(mr.goalCompletionPercentage) " +
           "FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.isActive = true GROUP BY mr.mentor " +
           "HAVING COUNT(mr) >= :minRelationships " +
           "ORDER BY AVG(mr.overallSatisfactionScore) DESC, AVG(mr.goalCompletionPercentage) DESC")
    List<Object[]> findMostEffectiveMentors(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("minRelationships") Long minRelationships);

    /**
     * Finds mentors ready for advancement.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.mentorAdvancementEligible = true " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findMentorsReadyForAdvancement(@Param("teamNumber") Integer teamNumber,
                                                               @Param("season") Integer season);

    /**
     * Finds mentors needing support or training.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND (mr.overallSatisfactionScore < :minSatisfaction " +
           "OR mr.engagementScore < :minEngagement OR mr.riskScore >= :maxRisk) " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findMentorsNeedingSupport(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minSatisfaction") Double minSatisfaction,
                                                          @Param("minEngagement") Double minEngagement,
                                                          @Param("maxRisk") Double maxRisk);

    // =========================================================================
    // SKILL DEVELOPMENT TRACKING
    // =========================================================================

    /**
     * Finds relationships by focus area.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND :focusArea MEMBER OF mr.focusAreas " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findByFocusArea(@Param("teamNumber") Integer teamNumber,
                                                @Param("season") Integer season,
                                                @Param("focusArea") String focusArea);

    /**
     * Finds relationships by skill development area.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND :skill MEMBER OF mr.skillsDevelopment " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findBySkillDevelopment(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("skill") String skill);

    /**
     * Finds most common focus areas.
     */
    @Query("SELECT focusArea, COUNT(focusArea) FROM MentorshipRelationship mr " +
           "JOIN mr.focusAreas focusArea WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isActive = true " +
           "GROUP BY focusArea ORDER BY COUNT(focusArea) DESC")
    List<Object[]> findMostCommonFocusAreas(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season);

    /**
     * Finds most developed skills.
     */
    @Query("SELECT skill, COUNT(skill) FROM MentorshipRelationship mr " +
           "JOIN mr.skillsDevelopment skill WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isActive = true " +
           "GROUP BY skill ORDER BY COUNT(skill) DESC")
    List<Object[]> findMostDevelopedSkills(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season);

    // =========================================================================
    // RISK ASSESSMENT AND INTERVENTION
    // =========================================================================

    /**
     * Finds high-risk relationships requiring intervention.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.riskScore >= :minRiskScore " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true " +
           "ORDER BY mr.riskScore DESC")
    List<MentorshipRelationship> findHighRiskRelationships(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minRiskScore") Double minRiskScore);

    /**
     * Finds relationships requiring intervention.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.requiresIntervention = true " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findRequiringIntervention(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season);

    /**
     * Finds relationships with intervention alerts.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.hasInterventionAlert = true " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findWithInterventionAlerts(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season);

    /**
     * Finds relationships needing immediate attention.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND (mr.requiresIntervention = true " +
           "OR mr.hasInterventionAlert = true OR mr.riskScore >= :urgentRiskScore) " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true " +
           "ORDER BY mr.riskScore DESC")
    List<MentorshipRelationship> findNeedingImmediateAttention(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("urgentRiskScore") Double urgentRiskScore);

    // =========================================================================
    // OUTCOME AND SUCCESS MEASUREMENT
    // =========================================================================

    /**
     * Finds completed relationships with outcomes.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.status = 'COMPLETED' " +
           "AND mr.impactScore IS NOT NULL AND mr.isActive = true " +
           "ORDER BY mr.impactScore DESC")
    List<MentorshipRelationship> findCompletedWithOutcomes(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season);

    /**
     * Finds high-impact mentorship relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.impactScore >= :minImpact " +
           "AND mr.isActive = true ORDER BY mr.impactScore DESC")
    List<MentorshipRelationship> findHighImpactRelationships(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season,
                                                            @Param("minImpact") Double minImpact);

    /**
     * Calculates success probability for active relationships.
     */
    @Query("SELECT AVG(mr.successProbability) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    Optional<Double> findAverageSuccessProbability(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season);

    // =========================================================================
    // TIME-BASED QUERIES
    // =========================================================================

    /**
     * Finds relationships started within date range.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.startDate BETWEEN :startDate AND :endDate " +
           "AND mr.isActive = true")
    List<MentorshipRelationship> findStartedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Finds relationships ending soon.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.expectedEndDate BETWEEN :startDate AND :endDate " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findEndingSoon(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Finds overdue relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.expectedEndDate < :currentDate " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    List<MentorshipRelationship> findOverdueRelationships(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("currentDate") LocalDate currentDate);

    /**
     * Finds long-running relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.actualDurationWeeks >= :minWeeks " +
           "AND mr.isActive = true ORDER BY mr.actualDurationWeeks DESC")
    List<MentorshipRelationship> findLongRunningRelationships(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season,
                                                             @Param("minWeeks") Integer minWeeks);

    // =========================================================================
    // STATISTICAL ANALYSIS
    // =========================================================================

    /**
     * Counts relationships by status.
     */
    @Query("SELECT mr.status, COUNT(mr) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.isActive = true GROUP BY mr.status")
    List<Object[]> countByStatus(@Param("teamNumber") Integer teamNumber,
                                @Param("season") Integer season);

    /**
     * Counts relationships by type.
     */
    @Query("SELECT mr.relationshipType, COUNT(mr) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.isActive = true GROUP BY mr.relationshipType")
    List<Object[]> countByRelationshipType(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season);

    /**
     * Counts relationships by primary focus.
     */
    @Query("SELECT mr.primaryFocus, COUNT(mr) FROM MentorshipRelationship mr " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.isActive = true GROUP BY mr.primaryFocus")
    List<Object[]> countByPrimaryFocus(@Param("teamNumber") Integer teamNumber,
                                      @Param("season") Integer season);

    /**
     * Calculates relationship metrics summary.
     */
    @Query("SELECT AVG(mr.goalCompletionPercentage), AVG(mr.attendanceRate), " +
           "AVG(mr.engagementScore), AVG(mr.overallSatisfactionScore), " +
           "AVG(mr.skillDevelopmentScore), AVG(mr.riskScore) " +
           "FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isActive = true")
    List<Object[]> findRelationshipMetrics(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds mentor-mentee pairs across multiple seasons.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season IN :seasons AND mr.mentor = :mentor AND mr.mentee = :mentee " +
           "AND mr.isActive = true ORDER BY mr.season DESC")
    List<MentorshipRelationship> findMentorMenteePairAcrossSeasons(@Param("teamNumber") Integer teamNumber,
                                                                  @Param("seasons") List<Integer> seasons,
                                                                  @Param("mentor") TeamMember mentor,
                                                                  @Param("mentee") TeamMember mentee);

    /**
     * Finds relationships across multiple seasons.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season IN :seasons AND mr.isActive = true " +
           "ORDER BY mr.season DESC, mr.startDate DESC")
    List<MentorshipRelationship> findMultiSeasonRelationships(@Param("teamNumber") Integer teamNumber,
                                                             @Param("seasons") List<Integer> seasons);

    /**
     * Finds recurring mentorship patterns.
     */
    @Query("SELECT mr.mentor, mr.mentee, COUNT(DISTINCT mr.season) as seasonCount " +
           "FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.isActive = true GROUP BY mr.mentor, mr.mentee " +
           "HAVING COUNT(DISTINCT mr.season) > 1 ORDER BY seasonCount DESC")
    List<Object[]> findRecurringMentorships(@Param("teamNumber") Integer teamNumber);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active relationships for team and season.
     */
    @Query("SELECT COUNT(mr) FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isActive = true")
    Long countActiveRelationships(@Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season);

    /**
     * Finds all active relationships for export.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isActive = true " +
           "ORDER BY mr.status ASC, mr.startDate ASC")
    List<MentorshipRelationship> findAllActiveRelationships(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season);

    /**
     * Updates all risk scores for recalculation.
     */
    @Query("UPDATE MentorshipRelationship mr SET mr.lastRiskAssessment = :assessmentDate " +
           "WHERE mr.teamNumber = :teamNumber AND mr.season = :season " +
           "AND mr.status IN ('ACTIVE', 'PROGRESSING') AND mr.isActive = true")
    void updateRiskAssessmentDates(@Param("teamNumber") Integer teamNumber,
                                  @Param("season") Integer season,
                                  @Param("assessmentDate") LocalDateTime assessmentDate);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived relationships.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isArchived = true " +
           "ORDER BY mr.archivedAt DESC")
    List<MentorshipRelationship> findArchivedRelationships(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season);

    /**
     * Finds relationships archived within date range.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isArchived = true " +
           "AND mr.archivedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY mr.archivedAt DESC")
    List<MentorshipRelationship> findArchivedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Finds relationships by archive reason.
     */
    @Query("SELECT mr FROM MentorshipRelationship mr WHERE mr.teamNumber = :teamNumber " +
           "AND mr.season = :season AND mr.isArchived = true " +
           "AND mr.archiveReason LIKE CONCAT('%', :reason, '%')")
    List<MentorshipRelationship> findByArchiveReason(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("reason") String reason);
}