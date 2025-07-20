// src/main/java/org/frcpm/services/SkillDevelopmentRecordService.java

package org.frcpm.services;

import org.frcpm.models.SkillDevelopmentRecord;
import org.frcpm.models.TeamMember;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for SkillDevelopmentRecord operations.
 * 
 * Provides comprehensive skills development tracking services including
 * competency assessments, learning paths, certification management, and
 * team-wide skills analytics for effective FRC team development.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.5 Skills Development Tracking
 */
public interface SkillDevelopmentRecordService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new skill development record.
     */
    SkillDevelopmentRecord create(SkillDevelopmentRecord record);

    /**
     * Updates an existing skill development record.
     */
    SkillDevelopmentRecord update(Long id, SkillDevelopmentRecord record);

    /**
     * Deletes a skill development record (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a skill development record by ID.
     */
    Optional<SkillDevelopmentRecord> findById(Long id);

    /**
     * Finds all active skill development records.
     */
    List<SkillDevelopmentRecord> findAll();

    /**
     * Checks if skill development record exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of skill development records.
     */
    long count();

    // =========================================================================
    // SKILL RECORD MANAGEMENT
    // =========================================================================

    /**
     * Creates a new skill development record with validation.
     */
    SkillDevelopmentRecord createSkillRecord(SkillDevelopmentRecord record);

    /**
     * Creates a skill record with basic parameters.
     */
    SkillDevelopmentRecord createSkillRecord(Integer teamNumber, Integer season, TeamMember teamMember,
                                           String skillName, SkillDevelopmentRecord.SkillCategory category,
                                           SkillDevelopmentRecord.SkillType skillType, String description);

    /**
     * Creates a skill record with target level.
     */
    SkillDevelopmentRecord createSkillRecordWithTarget(Integer teamNumber, Integer season, TeamMember teamMember,
                                                     String skillName, SkillDevelopmentRecord.SkillCategory category,
                                                     SkillDevelopmentRecord.SkillType skillType, String description,
                                                     SkillDevelopmentRecord.CompetencyLevel targetLevel);

    /**
     * Updates an existing skill record with validation.
     */
    SkillDevelopmentRecord updateSkillRecord(Long recordId, SkillDevelopmentRecord record);

    /**
     * Archives a skill development record.
     */
    void archiveSkillRecord(Long recordId, String reason);

    /**
     * Finds all active records for a team and season.
     */
    List<SkillDevelopmentRecord> findActiveRecords(Integer teamNumber, Integer season);

    /**
     * Finds records by skill category for a team and season.
     */
    List<SkillDevelopmentRecord> findBySkillCategory(Integer teamNumber, Integer season, 
                                                   SkillDevelopmentRecord.SkillCategory category);

    /**
     * Finds records by skill type for a team and season.
     */
    List<SkillDevelopmentRecord> findBySkillType(Integer teamNumber, Integer season, 
                                               SkillDevelopmentRecord.SkillType skillType);

    // =========================================================================
    // COMPETENCY ASSESSMENT OPERATIONS
    // =========================================================================

    /**
     * Updates competency level for a skill record.
     */
    SkillDevelopmentRecord updateCompetencyLevel(Long recordId, SkillDevelopmentRecord.CompetencyLevel newLevel);

    /**
     * Records competency assessment with detailed scoring.
     */
    SkillDevelopmentRecord recordAssessment(Long recordId, Double competencyScore, 
                                          SkillDevelopmentRecord.CompetencyLevel newLevel, String assessorNotes);

    /**
     * Calculates competency score based on assessment data.
     */
    SkillDevelopmentRecord calculateCompetencyScore(Long recordId);

    /**
     * Sets target competency level for skill.
     */
    SkillDevelopmentRecord setTargetLevel(Long recordId, SkillDevelopmentRecord.CompetencyLevel targetLevel);

    /**
     * Calculates progress towards target level.
     */
    SkillDevelopmentRecord calculateProgressToTarget(Long recordId);

    /**
     * Schedules next assessment for skill record.
     */
    SkillDevelopmentRecord scheduleNextAssessment(Long recordId, LocalDate nextAssessmentDate);

    /**
     * Finds records by competency score range.
     */
    List<SkillDevelopmentRecord> findByCompetencyScoreRange(Integer teamNumber, Integer season, 
                                                           Double minScore, Double maxScore);

    /**
     * Finds high competency skill records.
     */
    List<SkillDevelopmentRecord> findHighCompetencyRecords(Integer teamNumber, Integer season, Double minScore);

    /**
     * Finds records needing competency improvement.
     */
    List<SkillDevelopmentRecord> findRecordsNeedingImprovement(Integer teamNumber, Integer season, Double maxScore);

    /**
     * Finds records with recent assessments.
     */
    List<SkillDevelopmentRecord> findWithRecentAssessments(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records with upcoming assessments.
     */
    List<SkillDevelopmentRecord> findWithUpcomingAssessments(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records with overdue assessments.
     */
    List<SkillDevelopmentRecord> findWithOverdueAssessments(Integer teamNumber, Integer season);

    // =========================================================================
    // LEARNING PATH MANAGEMENT
    // =========================================================================

    /**
     * Starts learning path for skill record.
     */
    SkillDevelopmentRecord startLearningPath(Long recordId, Integer estimatedHours);

    /**
     * Updates learning status for skill record.
     */
    SkillDevelopmentRecord updateLearningStatus(Long recordId, SkillDevelopmentRecord.LearningStatus status);

    /**
     * Records learning time for skill record.
     */
    SkillDevelopmentRecord recordLearningTime(Long recordId, Integer hours);

    /**
     * Completes learning path for skill record.
     */
    SkillDevelopmentRecord completeLearningPath(Long recordId);

    /**
     * Pauses learning path for skill record.
     */
    SkillDevelopmentRecord pauseLearningPath(Long recordId, String reason);

    /**
     * Resumes learning path for skill record.
     */
    SkillDevelopmentRecord resumeLearningPath(Long recordId);

    /**
     * Calculates learning efficiency for skill record.
     */
    SkillDevelopmentRecord calculateLearningEfficiency(Long recordId);

    /**
     * Finds active learning paths for a team and season.
     */
    List<SkillDevelopmentRecord> findActiveLearningPaths(Integer teamNumber, Integer season);

    /**
     * Finds completed learning paths for a team and season.
     */
    List<SkillDevelopmentRecord> findCompletedLearningPaths(Integer teamNumber, Integer season);

    /**
     * Finds paused learning paths for a team and season.
     */
    List<SkillDevelopmentRecord> findPausedLearningPaths(Integer teamNumber, Integer season);

    /**
     * Finds learning paths by completion date range.
     */
    List<SkillDevelopmentRecord> findByLearningCompletionDateRange(Integer teamNumber, Integer season,
                                                                  LocalDate startDate, LocalDate endDate);

    /**
     * Finds high efficiency learning paths.
     */
    List<SkillDevelopmentRecord> findHighEfficiencyLearning(Integer teamNumber, Integer season, Double minEfficiency);

    /**
     * Finds low efficiency learning paths requiring intervention.
     */
    List<SkillDevelopmentRecord> findLowEfficiencyLearning(Integer teamNumber, Integer season, Double maxEfficiency);

    // =========================================================================
    // MILESTONE AND PROGRESS TRACKING
    // =========================================================================

    /**
     * Records milestone completion for skill record.
     */
    SkillDevelopmentRecord recordMilestone(Long recordId, String milestoneDescription);

    /**
     * Updates progress percentage for skill record.
     */
    SkillDevelopmentRecord updateProgress(Long recordId, Double progressPercentage);

    /**
     * Sets milestone target for skill record.
     */
    SkillDevelopmentRecord setMilestoneTarget(Long recordId, Integer targetMilestones);

    /**
     * Calculates milestone completion rate.
     */
    SkillDevelopmentRecord calculateMilestoneCompletionRate(Long recordId);

    /**
     * Finds records by progress range.
     */
    List<SkillDevelopmentRecord> findByProgressRange(Integer teamNumber, Integer season, 
                                                   Double minProgress, Double maxProgress);

    /**
     * Finds records with completed milestones.
     */
    List<SkillDevelopmentRecord> findWithCompletedMilestones(Integer teamNumber, Integer season);

    /**
     * Finds records with recent milestones.
     */
    List<SkillDevelopmentRecord> findWithRecentMilestones(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records meeting target levels.
     */
    List<SkillDevelopmentRecord> findMeetingTargetLevel(Integer teamNumber, Integer season);

    /**
     * Finds records below target levels requiring attention.
     */
    List<SkillDevelopmentRecord> findBelowTargetLevel(Integer teamNumber, Integer season);

    // =========================================================================
    // PRACTICE AND APPLICATION TRACKING
    // =========================================================================

    /**
     * Records practice session for skill record.
     */
    SkillDevelopmentRecord recordPracticeSession(Long recordId, Integer hours, String practiceType, 
                                                String practiceNotes);

    /**
     * Records skill application in real scenarios.
     */
    SkillDevelopmentRecord recordSkillApplication(Long recordId, String applicationContext, 
                                                Boolean successful, String applicationNotes);

    /**
     * Updates practice effectiveness score.
     */
    SkillDevelopmentRecord updatePracticeEffectiveness(Long recordId, Double effectiveness);

    /**
     * Calculates application success rate.
     */
    SkillDevelopmentRecord calculateApplicationSuccessRate(Long recordId);

    /**
     * Finds records with high practice hours.
     */
    List<SkillDevelopmentRecord> findHighPracticeRecords(Integer teamNumber, Integer season, Integer minHours);

    /**
     * Finds records with recent practice sessions.
     */
    List<SkillDevelopmentRecord> findWithRecentPractice(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records with high practice effectiveness.
     */
    List<SkillDevelopmentRecord> findHighPracticeEffectiveness(Integer teamNumber, Integer season, 
                                                              Double minEffectiveness);

    /**
     * Finds records with low practice effectiveness requiring intervention.
     */
    List<SkillDevelopmentRecord> findLowPracticeEffectiveness(Integer teamNumber, Integer season, 
                                                             Double maxEffectiveness);

    /**
     * Finds records with high application success rates.
     */
    List<SkillDevelopmentRecord> findHighApplicationSuccess(Integer teamNumber, Integer season, Double minRate);

    /**
     * Finds records with recent skill applications.
     */
    List<SkillDevelopmentRecord> findWithRecentApplications(Integer teamNumber, Integer season, Integer days);

    // =========================================================================
    // ASSESSMENT AND EVALUATION SYSTEMS
    // =========================================================================

    /**
     * Records multi-source assessment for skill record.
     */
    SkillDevelopmentRecord recordMultiSourceAssessment(Long recordId, Map<String, Double> assessmentScores, 
                                                      String primaryAssessor);

    /**
     * Records peer assessment for skill record.
     */
    SkillDevelopmentRecord recordPeerAssessment(Long recordId, Long assessorId, Double score, String feedback);

    /**
     * Records self-assessment for skill record.
     */
    SkillDevelopmentRecord recordSelfAssessment(Long recordId, Double score, String selfReflection);

    /**
     * Records mentor assessment for skill record.
     */
    SkillDevelopmentRecord recordMentorAssessment(Long recordId, Long mentorId, Double score, 
                                                String mentorFeedback, String developmentSuggestions);

    /**
     * Calculates composite assessment score from multiple sources.
     */
    SkillDevelopmentRecord calculateCompositeAssessmentScore(Long recordId);

    /**
     * Updates feedback and development notes.
     */
    SkillDevelopmentRecord updateFeedback(Long recordId, String feedback, String developmentNotes);

    // =========================================================================
    // CERTIFICATION AND RECOGNITION TRACKING
    // =========================================================================

    /**
     * Records certification achievement for skill record.
     */
    SkillDevelopmentRecord recordCertification(Long recordId, String certificationName, String certificationBody, 
                                             LocalDate certificationDate, LocalDate expiryDate);

    /**
     * Records recognition achievement for skill record.
     */
    SkillDevelopmentRecord recordRecognition(Long recordId, String recognitionType, String recognitionDetails, 
                                           LocalDate recognitionDate);

    /**
     * Updates certification status for skill record.
     */
    SkillDevelopmentRecord updateCertificationStatus(Long recordId, 
                                                   SkillDevelopmentRecord.CertificationStatus status);

    /**
     * Renews certification for skill record.
     */
    SkillDevelopmentRecord renewCertification(Long recordId, LocalDate newExpiryDate);

    /**
     * Finds records with active certifications.
     */
    List<SkillDevelopmentRecord> findWithActiveCertifications(Integer teamNumber, Integer season);

    /**
     * Finds records with expiring certifications.
     */
    List<SkillDevelopmentRecord> findWithExpiringCertifications(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records with recognitions.
     */
    List<SkillDevelopmentRecord> findWithRecognitions(Integer teamNumber, Integer season);

    /**
     * Finds records eligible for certification.
     */
    List<SkillDevelopmentRecord> findEligibleForCertification(Integer teamNumber, Integer season, 
                                                            Double minScore, Integer minHours);

    /**
     * Finds records with recent certifications.
     */
    List<SkillDevelopmentRecord> findWithRecentCertifications(Integer teamNumber, Integer season, Integer days);

    // =========================================================================
    // PREREQUISITES AND DEPENDENCIES MANAGEMENT
    // =========================================================================

    /**
     * Adds prerequisite skill for skill record.
     */
    SkillDevelopmentRecord addPrerequisite(Long recordId, String prerequisiteSkill, 
                                         SkillDevelopmentRecord.CompetencyLevel requiredLevel);

    /**
     * Removes prerequisite skill from skill record.
     */
    SkillDevelopmentRecord removePrerequisite(Long recordId, String prerequisiteSkill);

    /**
     * Checks if prerequisites are met for skill record.
     */
    boolean checkPrerequisitesMet(Long recordId);

    /**
     * Adds skill dependency for skill record.
     */
    SkillDevelopmentRecord addSkillDependency(Long recordId, String dependentSkill, String dependencyType);

    /**
     * Removes skill dependency from skill record.
     */
    SkillDevelopmentRecord removeSkillDependency(Long recordId, String dependentSkill);

    /**
     * Finds prerequisite gaps for skill record.
     */
    List<String> findPrerequisiteGaps(Long recordId);

    // =========================================================================
    // RISK ASSESSMENT AND INTERVENTION SYSTEMS
    // =========================================================================

    /**
     * Calculates risk score for skill record.
     */
    SkillDevelopmentRecord calculateRiskScore(Long recordId);

    /**
     * Updates intervention status for skill record.
     */
    SkillDevelopmentRecord updateInterventionStatus(Long recordId, boolean interventionRequired, String reason);

    /**
     * Records intervention action for skill record.
     */
    SkillDevelopmentRecord recordInterventionAction(Long recordId, String interventionType, 
                                                  String interventionDescription, LocalDate interventionDate);

    /**
     * Calculates consistency score for skill record.
     */
    SkillDevelopmentRecord calculateConsistencyScore(Long recordId);

    /**
     * Updates engagement score for skill record.
     */
    SkillDevelopmentRecord updateEngagementScore(Long recordId, Double engagementScore);

    /**
     * Finds high risk records requiring intervention.
     */
    List<SkillDevelopmentRecord> findHighRiskRecords(Integer teamNumber, Integer season, Double minRisk);

    /**
     * Finds records requiring immediate attention.
     */
    List<SkillDevelopmentRecord> findRequiringAttention(Integer teamNumber, Integer season, 
                                                       Double highRisk, Integer maxInactiveDays);

    /**
     * Finds records with inconsistent performance.
     */
    List<SkillDevelopmentRecord> findInconsistentPerformance(Integer teamNumber, Integer season, 
                                                            Double maxConsistency);

    /**
     * Finds records with low engagement.
     */
    List<SkillDevelopmentRecord> findLowEngagement(Integer teamNumber, Integer season, Double maxEngagement);

    /**
     * Finds stagnant records with no recent progress.
     */
    List<SkillDevelopmentRecord> findStagnantRecords(Integer teamNumber, Integer season, Integer maxDays);

    // =========================================================================
    // PERFORMANCE IMPACT MEASUREMENT
    // =========================================================================

    /**
     * Updates performance impact score for skill record.
     */
    SkillDevelopmentRecord updatePerformanceImpactScore(Long recordId, Double impactScore);

    /**
     * Records performance correlation for skill record.
     */
    SkillDevelopmentRecord recordPerformanceCorrelation(Long recordId, String performanceMetric, 
                                                       Double correlationValue);

    /**
     * Updates competition utilization for skill record.
     */
    SkillDevelopmentRecord updateCompetitionUtilization(Long recordId, Double utilizationPercentage);

    /**
     * Finds skills with high performance impact.
     */
    List<SkillDevelopmentRecord> findHighPerformanceImpact(Integer teamNumber, Integer season, Double minImpact);

    /**
     * Finds skills with high competition utilization.
     */
    List<SkillDevelopmentRecord> findHighCompetitionUtilization(Integer teamNumber, Integer season, 
                                                               Double minUtilization);

    // =========================================================================
    // MENTORSHIP AND INSTRUCTION INTEGRATION
    // =========================================================================

    /**
     * Records mentorship session for skill record.
     */
    SkillDevelopmentRecord recordMentorshipSession(Long recordId, Long mentorId, Integer durationMinutes, 
                                                 String sessionType, String sessionNotes);

    /**
     * Records instruction session for skill record.
     */
    SkillDevelopmentRecord recordInstructionSession(Long recordId, Long instructorId, String instructionType, 
                                                   Integer durationMinutes, String instructionNotes);

    /**
     * Updates instruction effectiveness for skill record.
     */
    SkillDevelopmentRecord updateInstructionEffectiveness(Long recordId, Double effectiveness);

    /**
     * Updates mentorship impact score for skill record.
     */
    SkillDevelopmentRecord updateMentorshipImpactScore(Long recordId, Double impactScore);

    /**
     * Finds records with high instruction effectiveness.
     */
    List<SkillDevelopmentRecord> findHighInstructionEffectiveness(Integer teamNumber, Integer season, 
                                                                 Double minEffectiveness);

    /**
     * Finds records with high mentorship impact.
     */
    List<SkillDevelopmentRecord> findHighMentorshipImpact(Integer teamNumber, Integer season, Double minImpact);

    /**
     * Finds records with recent mentorship sessions.
     */
    List<SkillDevelopmentRecord> findWithRecentMentorship(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds records needing additional mentorship support.
     */
    List<SkillDevelopmentRecord> findNeedingMentorshipSupport(Integer teamNumber, Integer season, 
                                                             Double maxImpact, Double maxEffectiveness);

    // =========================================================================
    // COMPETITION READINESS EVALUATION
    // =========================================================================

    /**
     * Updates competition readiness status for skill record.
     */
    SkillDevelopmentRecord updateCompetitionReadiness(Long recordId, boolean competitionReady);

    /**
     * Calculates competition readiness score for skill record.
     */
    SkillDevelopmentRecord calculateCompetitionReadinessScore(Long recordId);

    /**
     * Marks skill as competition critical.
     */
    SkillDevelopmentRecord markCompetitionCritical(Long recordId, boolean critical);

    /**
     * Finds competition-ready skills for a team and season.
     */
    List<SkillDevelopmentRecord> findCompetitionReadySkills(Integer teamNumber, Integer season);

    /**
     * Finds skills critical for competition performance.
     */
    List<SkillDevelopmentRecord> findCompetitionCriticalSkills(Integer teamNumber, Integer season);

    // =========================================================================
    // DOCUMENTATION AND EVIDENCE PORTFOLIO
    // =========================================================================

    /**
     * Adds documentation evidence for skill record.
     */
    SkillDevelopmentRecord addDocumentationEvidence(Long recordId, String evidenceType, String evidenceDescription, 
                                                   String evidenceUrl);

    /**
     * Removes documentation evidence from skill record.
     */
    SkillDevelopmentRecord removeDocumentationEvidence(Long recordId, String evidenceType);

    /**
     * Updates portfolio completeness for skill record.
     */
    SkillDevelopmentRecord updatePortfolioCompleteness(Long recordId, Double completenessPercentage);

    /**
     * Validates evidence portfolio for skill record.
     */
    SkillDevelopmentRecord validateEvidencePortfolio(Long recordId);

    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================

    /**
     * Counts skill records by category for a team and season.
     */
    Map<SkillDevelopmentRecord.SkillCategory, Long> countByCategory(Integer teamNumber, Integer season);

    /**
     * Counts skill records by skill type for a team and season.
     */
    Map<SkillDevelopmentRecord.SkillType, Long> countBySkillType(Integer teamNumber, Integer season);

    /**
     * Counts skill records by competency level for a team and season.
     */
    Map<SkillDevelopmentRecord.CompetencyLevel, Long> countByCompetencyLevel(Integer teamNumber, Integer season);

    /**
     * Counts skill records by learning status for a team and season.
     */
    Map<SkillDevelopmentRecord.LearningStatus, Long> countByLearningStatus(Integer teamNumber, Integer season);

    /**
     * Calculates average competency score by category.
     */
    Map<SkillDevelopmentRecord.SkillCategory, Double> averageCompetencyByCategory(Integer teamNumber, Integer season);

    /**
     * Calculates total practice hours by skill type.
     */
    Map<SkillDevelopmentRecord.SkillType, Long> totalPracticeHoursBySkillType(Integer teamNumber, Integer season);

    /**
     * Finds most improved skills based on competency score increase.
     */
    List<SkillDevelopmentRecord> findMostImprovedSkills(Integer teamNumber, Integer season);

    /**
     * Finds skills with high retention rates.
     */
    List<SkillDevelopmentRecord> findHighRetentionSkills(Integer teamNumber, Integer season, Double minRetention);

    /**
     * Generates comprehensive skills analytics report.
     */
    Map<String, Object> generateSkillsAnalyticsReport(Integer teamNumber, Integer season);

    /**
     * Generates team skills dashboard data.
     */
    Map<String, Object> generateSkillsDashboardData(Integer teamNumber, Integer season);

    /**
     * Generates individual skills development report.
     */
    Map<String, Object> generateIndividualSkillsReport(Integer teamNumber, Integer season, TeamMember teamMember);

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches skill records by skill name.
     */
    List<SkillDevelopmentRecord> searchBySkillName(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Searches skill records by description.
     */
    List<SkillDevelopmentRecord> searchByDescription(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Full text search across skill records.
     */
    List<SkillDevelopmentRecord> fullTextSearch(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Advanced search with multiple criteria.
     */
    List<SkillDevelopmentRecord> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria);

    // =========================================================================
    // TEAM MEMBER SPECIFIC OPERATIONS
    // =========================================================================

    /**
     * Finds team member's top skills by competency score.
     */
    List<SkillDevelopmentRecord> findTeamMemberTopSkills(Integer teamNumber, Integer season, TeamMember teamMember);

    /**
     * Finds team member's skills needing improvement.
     */
    List<SkillDevelopmentRecord> findTeamMemberSkillsNeedingImprovement(Integer teamNumber, Integer season, 
                                                                       TeamMember teamMember);

    /**
     * Finds team member's completed certifications.
     */
    List<SkillDevelopmentRecord> findTeamMemberCertifications(Integer teamNumber, Integer season, 
                                                             TeamMember teamMember);

    /**
     * Calculates team member's overall skill score.
     */
    Double calculateTeamMemberOverallSkillScore(Integer teamNumber, Integer season, TeamMember teamMember);

    /**
     * Generates team member skill development plan.
     */
    Map<String, Object> generateTeamMemberDevelopmentPlan(Integer teamNumber, Integer season, TeamMember teamMember);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds skill records across multiple seasons for comparison.
     */
    List<SkillDevelopmentRecord> findMultiSeasonRecords(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds skill evolution for specific team member across seasons.
     */
    List<SkillDevelopmentRecord> findSkillEvolution(Integer teamNumber, TeamMember teamMember, String skillName);

    /**
     * Compares skills development across seasons.
     */
    Map<String, Object> compareSkillsAcrossSeasons(Integer teamNumber, List<Integer> seasons);

    /**
     * Analyzes team skills evolution across seasons.
     */
    Map<String, Object> analyzeTeamSkillsEvolution(Integer teamNumber, List<Integer> seasons);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active skill records.
     */
    Long countActiveRecords(Integer teamNumber, Integer season);

    /**
     * Finds all active skill records for export.
     */
    List<SkillDevelopmentRecord> findAllActiveRecords(Integer teamNumber, Integer season);

    /**
     * Creates multiple skill records.
     */
    List<SkillDevelopmentRecord> createBulkRecords(List<SkillDevelopmentRecord> records);

    /**
     * Updates multiple skill records.
     */
    List<SkillDevelopmentRecord> updateBulkRecords(Map<Long, SkillDevelopmentRecord> recordUpdates);

    /**
     * Archives multiple skill records.
     */
    void bulkArchiveRecords(List<Long> recordIds, String reason);

    /**
     * Updates all risk scores for team and season.
     */
    void updateAllRiskScores(Integer teamNumber, Integer season);

    /**
     * Updates all competency scores for team and season.
     */
    void updateAllCompetencyScores(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived skill records.
     */
    List<SkillDevelopmentRecord> findArchivedRecords(Integer teamNumber, Integer season);

    /**
     * Restores archived skill record.
     */
    SkillDevelopmentRecord restoreArchivedRecord(Long recordId);

    /**
     * Permanently deletes skill record.
     */
    void permanentlyDeleteRecord(Long recordId);

    /**
     * Finds records created within date range.
     */
    List<SkillDevelopmentRecord> findCreatedInDateRange(Integer teamNumber, Integer season, 
                                                       LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds records updated within date range.
     */
    List<SkillDevelopmentRecord> findUpdatedInDateRange(Integer teamNumber, Integer season, 
                                                       LocalDateTime startDate, LocalDateTime endDate);

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    /**
     * Exports skill record to external format.
     */
    Map<String, Object> exportToExternalFormat(Long recordId, String format);

    /**
     * Imports skill record from external source.
     */
    SkillDevelopmentRecord importFromExternalSource(Map<String, Object> recordData, String sourceType);

    /**
     * Syncs with external learning management systems.
     */
    SkillDevelopmentRecord syncWithExternalLMS(Long recordId, String lmsType);

    /**
     * Generates skills portfolio report.
     */
    Map<String, Object> generateSkillsPortfolioReport(Integer teamNumber, Integer season, TeamMember teamMember);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates skill development record data.
     */
    List<String> validateSkillRecord(SkillDevelopmentRecord record);

    /**
     * Validates competency level progression.
     */
    boolean validateCompetencyProgression(Long recordId, SkillDevelopmentRecord.CompetencyLevel newLevel);

    /**
     * Validates user permissions for skill record operation.
     */
    boolean validateUserPermissions(Long recordId, Long userId, String operation);

    /**
     * Checks skill record data quality.
     */
    Map<String, Object> checkSkillRecordQuality(Long recordId);

    /**
     * Suggests improvements for skill development.
     */
    List<String> suggestSkillDevelopmentImprovements(Long recordId);

    /**
     * Validates learning path completion requirements.
     */
    boolean validateLearningPathCompletion(Long recordId);
}