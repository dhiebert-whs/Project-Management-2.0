// src/main/java/org/frcpm/services/MentorshipRelationshipService.java

package org.frcpm.services;

import org.frcpm.models.MentorshipRelationship;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for MentorshipRelationship operations.
 * 
 * Provides comprehensive mentorship relationship management services including
 * relationship analytics, progress monitoring, goal tracking, and mentorship
 * coordination for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.3 Mentorship Tracking System
 */
public interface MentorshipRelationshipService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new mentorship relationship.
     */
    MentorshipRelationship create(MentorshipRelationship relationship);

    /**
     * Updates an existing mentorship relationship.
     */
    MentorshipRelationship update(Long id, MentorshipRelationship relationship);

    /**
     * Deletes a mentorship relationship (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a mentorship relationship by ID.
     */
    Optional<MentorshipRelationship> findById(Long id);

    /**
     * Finds all active mentorship relationships.
     */
    List<MentorshipRelationship> findAll();

    /**
     * Checks if mentorship relationship exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of mentorship relationships.
     */
    long count();

    // =========================================================================
    // RELATIONSHIP MANAGEMENT
    // =========================================================================

    /**
     * Creates a new mentorship relationship with validation.
     */
    MentorshipRelationship createRelationship(MentorshipRelationship relationship);

    /**
     * Creates a relationship with basic parameters.
     */
    MentorshipRelationship createRelationship(Integer teamNumber, Integer season, TeamMember mentor,
                                             TeamMember mentee, MentorshipRelationship.RelationshipType type,
                                             MentorshipRelationship.MentorshipFocus primaryFocus);

    /**
     * Updates an existing relationship with validation.
     */
    MentorshipRelationship updateRelationship(Long relationshipId, MentorshipRelationship relationship);

    /**
     * Archives a mentorship relationship.
     */
    void archiveRelationship(Long relationshipId, String reason);

    /**
     * Finds all active relationships for a team and season.
     */
    List<MentorshipRelationship> findActiveRelationships(Integer teamNumber, Integer season);

    /**
     * Finds relationships by status for a team and season.
     */
    List<MentorshipRelationship> findByStatus(Integer teamNumber, Integer season, 
                                             MentorshipRelationship.MentorshipStatus status);

    /**
     * Finds relationships by type for a team and season.
     */
    List<MentorshipRelationship> findByType(Integer teamNumber, Integer season, 
                                           MentorshipRelationship.RelationshipType type);

    // =========================================================================
    // MENTOR AND MENTEE MANAGEMENT
    // =========================================================================

    /**
     * Finds all relationships for a specific mentor.
     */
    List<MentorshipRelationship> findByMentor(TeamMember mentor, Integer teamNumber, Integer season);

    /**
     * Finds all relationships for a specific mentee.
     */
    List<MentorshipRelationship> findByMentee(TeamMember mentee, Integer teamNumber, Integer season);

    /**
     * Finds active relationships for a mentor.
     */
    List<MentorshipRelationship> findActiveMentorRelationships(TeamMember mentor, Integer teamNumber, Integer season);

    /**
     * Finds active relationships for a mentee.
     */
    List<MentorshipRelationship> findActiveMenteeRelationships(TeamMember mentee, Integer teamNumber, Integer season);

    /**
     * Counts mentoring relationships for a mentor.
     */
    Long countMentorRelationships(TeamMember mentor, Integer teamNumber, Integer season);

    /**
     * Counts mentee relationships for a team member.
     */
    Long countMenteeRelationships(TeamMember mentee, Integer teamNumber, Integer season);

    /**
     * Finds available mentors for new relationships.
     */
    List<TeamMember> findAvailableMentors(Integer teamNumber, Integer season, Integer maxRelationships);

    /**
     * Finds potential mentees needing mentorship.
     */
    List<TeamMember> findPotentialMentees(Integer teamNumber, Integer season);

    // =========================================================================
    // MEETING AND INTERACTION TRACKING
    // =========================================================================

    /**
     * Records a meeting for a mentorship relationship.
     */
    MentorshipRelationship recordMeeting(Long relationshipId, LocalDateTime meetingDate, 
                                        Integer durationMinutes, String notes);

    /**
     * Records a cancelled meeting.
     */
    MentorshipRelationship recordCancelledMeeting(Long relationshipId, LocalDateTime plannedDate, String reason);

    /**
     * Updates meeting frequency for a relationship.
     */
    MentorshipRelationship updateMeetingFrequency(Long relationshipId, Integer meetingsPerWeek);

    /**
     * Calculates attendance rate for a relationship.
     */
    Double calculateAttendanceRate(Long relationshipId);

    /**
     * Finds relationships with low meeting attendance.
     */
    List<MentorshipRelationship> findLowAttendanceRelationships(Integer teamNumber, Integer season, 
                                                               Double minAttendance, Integer minMeetings);

    /**
     * Finds relationships needing meeting scheduling.
     */
    List<MentorshipRelationship> findNeedingMeetingScheduling(Integer teamNumber, Integer season, Integer maxDays);

    /**
     * Calculates average meeting duration for relationships.
     */
    Double calculateAverageMeetingDuration(Integer teamNumber, Integer season);

    // =========================================================================
    // GOAL AND MILESTONE TRACKING
    // =========================================================================

    /**
     * Sets goals for a mentorship relationship.
     */
    MentorshipRelationship setGoals(Long relationshipId, String goals, List<String> milestones);

    /**
     * Records milestone completion.
     */
    MentorshipRelationship recordMilestoneCompletion(Long relationshipId, String milestone, LocalDate completionDate);

    /**
     * Updates goal completion percentage.
     */
    MentorshipRelationship updateGoalCompletion(Long relationshipId, Double completionPercentage);

    /**
     * Finds relationships with upcoming milestones.
     */
    List<MentorshipRelationship> findWithUpcomingMilestones(Integer teamNumber, Integer season, 
                                                           LocalDate startDate, LocalDate endDate);

    /**
     * Finds relationships with overdue milestones.
     */
    List<MentorshipRelationship> findWithOverdueMilestones(Integer teamNumber, Integer season, LocalDate currentDate);

    /**
     * Finds relationships by goal completion range.
     */
    List<MentorshipRelationship> findByGoalCompletionRange(Integer teamNumber, Integer season, 
                                                          Double minPercentage, Double maxPercentage);

    /**
     * Calculates goal completion metrics for team.
     */
    Map<String, Object> calculateGoalCompletionMetrics(Integer teamNumber, Integer season);

    // =========================================================================
    // FEEDBACK AND ASSESSMENT
    // =========================================================================

    /**
     * Records feedback for a relationship.
     */
    MentorshipRelationship recordFeedback(Long relationshipId, TeamMember feedbackProvider, 
                                         String feedback, Integer rating);

    /**
     * Records mentor feedback from mentee.
     */
    MentorshipRelationship recordMenteeFeedback(Long relationshipId, String feedback, Integer mentorRating);

    /**
     * Records mentee feedback from mentor.
     */
    MentorshipRelationship recordMentorFeedback(Long relationshipId, String feedback, Integer menteeRating);

    /**
     * Updates satisfaction scores.
     */
    MentorshipRelationship updateSatisfactionScores(Long relationshipId, Double mentorSatisfaction, 
                                                   Double menteeSatisfaction);

    /**
     * Calculates overall satisfaction score.
     */
    Double calculateOverallSatisfaction(Long relationshipId);

    /**
     * Finds highest satisfaction relationships.
     */
    List<MentorshipRelationship> findHighestSatisfaction(Integer teamNumber, Integer season, 
                                                        Double minScore, Integer minFeedback);

    // =========================================================================
    // SKILLS DEVELOPMENT TRACKING
    // =========================================================================

    /**
     * Adds skill development area to relationship.
     */
    MentorshipRelationship addSkillDevelopment(Long relationshipId, String skill);

    /**
     * Records skill progress.
     */
    MentorshipRelationship recordSkillProgress(Long relationshipId, String skill, Integer progressLevel);

    /**
     * Updates skill development score.
     */
    MentorshipRelationship updateSkillDevelopmentScore(Long relationshipId, Double score);

    /**
     * Finds relationships by skill development area.
     */
    List<MentorshipRelationship> findBySkillDevelopment(Integer teamNumber, Integer season, String skill);

    /**
     * Finds relationships with high skill development.
     */
    List<MentorshipRelationship> findHighSkillDevelopment(Integer teamNumber, Integer season, Double minScore);

    /**
     * Finds most developed skills across relationships.
     */
    Map<String, Long> findMostDevelopedSkills(Integer teamNumber, Integer season);

    /**
     * Calculates skill development metrics for team.
     */
    Map<String, Object> calculateSkillDevelopmentMetrics(Integer teamNumber, Integer season);

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    /**
     * Calculates engagement score for a relationship.
     */
    Double calculateEngagementScore(Long relationshipId);

    /**
     * Updates engagement metrics.
     */
    MentorshipRelationship updateEngagementMetrics(Long relationshipId);

    /**
     * Finds most engaging relationships.
     */
    List<MentorshipRelationship> findMostEngaging(Integer teamNumber, Integer season, Double minScore);

    /**
     * Finds relationships with low engagement.
     */
    List<MentorshipRelationship> findLowEngagement(Integer teamNumber, Integer season, Double maxScore);

    /**
     * Calculates meeting frequency score.
     */
    Double calculateMeetingFrequencyScore(Long relationshipId);

    /**
     * Finds relationships with high meeting frequency.
     */
    List<MentorshipRelationship> findHighMeetingFrequency(Integer teamNumber, Integer season, Double minFrequency);

    /**
     * Calculates average satisfaction score for team.
     */
    Double calculateAverageSatisfactionScore(Integer teamNumber, Integer season);

    // =========================================================================
    // RISK ASSESSMENT AND INTERVENTION
    // =========================================================================

    /**
     * Calculates risk score for a relationship.
     */
    Double calculateRiskScore(Long relationshipId);

    /**
     * Assesses relationship risk factors.
     */
    Map<String, Object> assessRelationshipRisk(Long relationshipId);

    /**
     * Updates risk assessment for a relationship.
     */
    MentorshipRelationship updateRiskAssessment(Long relationshipId);

    /**
     * Finds high-risk relationships requiring intervention.
     */
    List<MentorshipRelationship> findHighRiskRelationships(Integer teamNumber, Integer season, Double minRiskScore);

    /**
     * Finds relationships requiring intervention.
     */
    List<MentorshipRelationship> findRequiringIntervention(Integer teamNumber, Integer season);

    /**
     * Finds relationships with intervention alerts.
     */
    List<MentorshipRelationship> findWithInterventionAlerts(Integer teamNumber, Integer season);

    /**
     * Flags relationship for intervention.
     */
    MentorshipRelationship flagForIntervention(Long relationshipId, String reason);

    /**
     * Creates intervention alert.
     */
    MentorshipRelationship createInterventionAlert(Long relationshipId, String alertMessage);

    /**
     * Finds relationships needing immediate attention.
     */
    List<MentorshipRelationship> findNeedingImmediateAttention(Integer teamNumber, Integer season, 
                                                              Double urgentRiskScore);

    // =========================================================================
    // MENTOR PERFORMANCE AND EFFECTIVENESS
    // =========================================================================

    /**
     * Finds most effective mentors by relationship success.
     */
    List<Object[]> findMostEffectiveMentors(Integer teamNumber, Integer season, Long minRelationships);

    /**
     * Evaluates mentor effectiveness.
     */
    Map<String, Object> evaluateMentorEffectiveness(TeamMember mentor, Integer teamNumber, Integer season);

    /**
     * Finds mentors ready for advancement.
     */
    List<MentorshipRelationship> findMentorsReadyForAdvancement(Integer teamNumber, Integer season);

    /**
     * Finds mentors needing support or training.
     */
    List<MentorshipRelationship> findMentorsNeedingSupport(Integer teamNumber, Integer season, 
                                                          Double minSatisfaction, Double minEngagement, 
                                                          Double maxRisk);

    /**
     * Sets mentor advancement eligibility.
     */
    MentorshipRelationship setMentorAdvancementEligibility(Long relationshipId, boolean eligible);

    /**
     * Calculates mentor success metrics.
     */
    Map<String, Object> calculateMentorSuccessMetrics(TeamMember mentor, Integer teamNumber, Integer season);

    // =========================================================================
    // OUTCOME AND SUCCESS MEASUREMENT
    // =========================================================================

    /**
     * Records relationship outcome.
     */
    MentorshipRelationship recordOutcome(Long relationshipId, String outcome, Double impactScore);

    /**
     * Calculates impact score for a relationship.
     */
    Double calculateImpactScore(Long relationshipId);

    /**
     * Calculates success probability for a relationship.
     */
    Double calculateSuccessProbability(Long relationshipId);

    /**
     * Updates outcome metrics.
     */
    MentorshipRelationship updateOutcomeMetrics(Long relationshipId);

    /**
     * Finds completed relationships with outcomes.
     */
    List<MentorshipRelationship> findCompletedWithOutcomes(Integer teamNumber, Integer season);

    /**
     * Finds high-impact relationships.
     */
    List<MentorshipRelationship> findHighImpactRelationships(Integer teamNumber, Integer season, Double minImpact);

    /**
     * Calculates average success probability for active relationships.
     */
    Double calculateAverageSuccessProbability(Integer teamNumber, Integer season);

    /**
     * Generates success metrics report.
     */
    Map<String, Object> generateSuccessMetricsReport(Integer teamNumber, Integer season);

    // =========================================================================
    // FOCUS AREAS AND SPECIALIZATION
    // =========================================================================

    /**
     * Finds relationships by focus area.
     */
    List<MentorshipRelationship> findByFocusArea(Integer teamNumber, Integer season, String focusArea);

    /**
     * Finds relationships by primary focus.
     */
    List<MentorshipRelationship> findByPrimaryFocus(Integer teamNumber, Integer season, 
                                                   MentorshipRelationship.MentorshipFocus primaryFocus);

    /**
     * Adds focus area to relationship.
     */
    MentorshipRelationship addFocusArea(Long relationshipId, String focusArea);

    /**
     * Removes focus area from relationship.
     */
    MentorshipRelationship removeFocusArea(Long relationshipId, String focusArea);

    /**
     * Finds most common focus areas.
     */
    Map<String, Long> findMostCommonFocusAreas(Integer teamNumber, Integer season);

    /**
     * Updates primary focus for relationship.
     */
    MentorshipRelationship updatePrimaryFocus(Long relationshipId, MentorshipRelationship.MentorshipFocus focus);

    // =========================================================================
    // TIME-BASED OPERATIONS
    // =========================================================================

    /**
     * Finds relationships started within date range.
     */
    List<MentorshipRelationship> findStartedInDateRange(Integer teamNumber, Integer season, 
                                                       LocalDate startDate, LocalDate endDate);

    /**
     * Finds relationships ending soon.
     */
    List<MentorshipRelationship> findEndingSoon(Integer teamNumber, Integer season, 
                                               LocalDate startDate, LocalDate endDate);

    /**
     * Finds overdue relationships.
     */
    List<MentorshipRelationship> findOverdueRelationships(Integer teamNumber, Integer season, LocalDate currentDate);

    /**
     * Finds long-running relationships.
     */
    List<MentorshipRelationship> findLongRunningRelationships(Integer teamNumber, Integer season, Integer minWeeks);

    /**
     * Updates relationship duration.
     */
    MentorshipRelationship updateDuration(Long relationshipId, Integer actualWeeks);

    /**
     * Extends relationship duration.
     */
    MentorshipRelationship extendRelationship(Long relationshipId, LocalDate newEndDate, String reason);

    // =========================================================================
    // RELATIONSHIP STATUS MANAGEMENT
    // =========================================================================

    /**
     * Updates relationship status.
     */
    MentorshipRelationship updateStatus(Long relationshipId, MentorshipRelationship.MentorshipStatus status);

    /**
     * Activates a relationship.
     */
    MentorshipRelationship activateRelationship(Long relationshipId);

    /**
     * Pauses a relationship.
     */
    MentorshipRelationship pauseRelationship(Long relationshipId, String reason);

    /**
     * Completes a relationship.
     */
    MentorshipRelationship completeRelationship(Long relationshipId, String completionNotes);

    /**
     * Terminates a relationship early.
     */
    MentorshipRelationship terminateRelationship(Long relationshipId, String reason);

    /**
     * Finds successful relationships.
     */
    List<MentorshipRelationship> findSuccessfulRelationships(Integer teamNumber, Integer season, 
                                                            Double minSatisfaction);

    /**
     * Finds relationships needing attention.
     */
    List<MentorshipRelationship> findRelationshipsNeedingAttention(Integer teamNumber, Integer season, 
                                                                  Double minEngagement, Double minSatisfaction, 
                                                                  Double maxRisk);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts relationships by status.
     */
    Map<MentorshipRelationship.MentorshipStatus, Long> countByStatus(Integer teamNumber, Integer season);

    /**
     * Counts relationships by type.
     */
    Map<MentorshipRelationship.RelationshipType, Long> countByType(Integer teamNumber, Integer season);

    /**
     * Counts relationships by primary focus.
     */
    Map<MentorshipRelationship.MentorshipFocus, Long> countByPrimaryFocus(Integer teamNumber, Integer season);

    /**
     * Calculates relationship metrics summary.
     */
    Map<String, Object> calculateRelationshipMetrics(Integer teamNumber, Integer season);

    /**
     * Generates comprehensive analytics report.
     */
    Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season);

    /**
     * Calculates team mentorship health score.
     */
    Double calculateTeamMentorshipHealthScore(Integer teamNumber, Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds mentor-mentee pairs across multiple seasons.
     */
    List<MentorshipRelationship> findMentorMenteePairAcrossSeasons(Integer teamNumber, List<Integer> seasons,
                                                                  TeamMember mentor, TeamMember mentee);

    /**
     * Finds relationships across multiple seasons.
     */
    List<MentorshipRelationship> findMultiSeasonRelationships(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds recurring mentorship patterns.
     */
    List<Object[]> findRecurringMentorships(Integer teamNumber);

    /**
     * Analyzes mentorship progression across seasons.
     */
    Map<String, Object> analyzeMentorshipProgression(Integer teamNumber, List<Integer> seasons);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active relationships.
     */
    Long countActiveRelationships(Integer teamNumber, Integer season);

    /**
     * Finds all active relationships for export.
     */
    List<MentorshipRelationship> findAllActiveRelationships(Integer teamNumber, Integer season);

    /**
     * Creates multiple relationships.
     */
    List<MentorshipRelationship> createBulkRelationships(List<MentorshipRelationship> relationships);

    /**
     * Updates multiple relationships.
     */
    List<MentorshipRelationship> updateBulkRelationships(Map<Long, MentorshipRelationship> relationshipUpdates);

    /**
     * Archives multiple relationships.
     */
    void bulkArchiveRelationships(List<Long> relationshipIds, String reason);

    /**
     * Updates risk assessments for all active relationships.
     */
    void updateAllRiskAssessments(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived relationships.
     */
    List<MentorshipRelationship> findArchivedRelationships(Integer teamNumber, Integer season);

    /**
     * Restores archived relationship.
     */
    MentorshipRelationship restoreArchivedRelationship(Long relationshipId);

    /**
     * Permanently deletes relationship.
     */
    void permanentlyDeleteRelationship(Long relationshipId);

    /**
     * Finds relationships archived within date range.
     */
    List<MentorshipRelationship> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                                        LocalDateTime startDate, LocalDateTime endDate);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates relationship data.
     */
    List<String> validateRelationship(MentorshipRelationship relationship);

    /**
     * Validates mentor capacity.
     */
    boolean validateMentorCapacity(TeamMember mentor, Integer teamNumber, Integer season, Integer maxRelationships);

    /**
     * Validates relationship compatibility.
     */
    boolean validateRelationshipCompatibility(TeamMember mentor, TeamMember mentee);

    /**
     * Checks for relationship conflicts.
     */
    List<String> checkRelationshipConflicts(MentorshipRelationship relationship);

    /**
     * Validates user permissions for relationship operation.
     */
    boolean validateUserPermissions(Long relationshipId, Long userId, String operation);
}