// src/main/java/org/frcpm/services/impl/SkillDevelopmentRecordServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.SkillDevelopmentRecord;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.SkillDevelopmentRecordRepository;
import org.frcpm.services.SkillDevelopmentRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SkillDevelopmentRecordService.
 * 
 * Provides comprehensive skills development tracking services including
 * competency assessments, learning paths, certification management, and
 * team-wide skills analytics with sophisticated business logic and
 * advanced analytics capabilities for effective FRC team development.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.5 Skills Development Tracking
 */
@Service
@Transactional
public class SkillDevelopmentRecordServiceImpl implements SkillDevelopmentRecordService {

    @Autowired
    private SkillDevelopmentRecordRepository recordRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public SkillDevelopmentRecord create(SkillDevelopmentRecord record) {
        return createSkillRecord(record);
    }

    @Override
    public SkillDevelopmentRecord update(Long id, SkillDevelopmentRecord record) {
        return updateSkillRecord(id, record);
    }

    @Override
    public void delete(Long id) {
        archiveSkillRecord(id, "Deleted by user");
    }

    @Override
    public Optional<SkillDevelopmentRecord> findById(Long id) {
        return recordRepository.findById(id);
    }

    @Override
    public List<SkillDevelopmentRecord> findAll() {
        return recordRepository.findAll().stream()
                .filter(SkillDevelopmentRecord::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return recordRepository.existsById(id);
    }

    @Override
    public long count() {
        return recordRepository.count();
    }

    // =========================================================================
    // SKILL RECORD MANAGEMENT
    // =========================================================================

    @Override
    public SkillDevelopmentRecord createSkillRecord(SkillDevelopmentRecord record) {
        validateSkillRecord(record);
        
        // Set defaults
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        if (record.getLastUpdateDate() == null) {
            record.setLastUpdateDate(LocalDateTime.now());
        }
        if (record.getIsActive() == null) {
            record.setIsActive(true);
        }
        if (record.getCurrentLevel() == null) {
            record.setCurrentLevel(SkillDevelopmentRecord.CompetencyLevel.NOVICE);
        }
        if (record.getLearningStatus() == null) {
            record.setLearningStatus(SkillDevelopmentRecord.LearningStatus.NOT_STARTED);
        }
        if (record.getCertificationStatus() == null) {
            record.setCertificationStatus(SkillDevelopmentRecord.CertificationStatus.NOT_CERTIFIED);
        }
        
        // Calculate initial metrics
        record = calculateInitialMetrics(record);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord createSkillRecord(Integer teamNumber, Integer season, TeamMember teamMember,
                                                   String skillName, SkillDevelopmentRecord.SkillCategory category,
                                                   SkillDevelopmentRecord.SkillType skillType, String description) {
        SkillDevelopmentRecord record = new SkillDevelopmentRecord();
        record.setTeamNumber(teamNumber);
        record.setSeason(season);
        record.setTeamMember(teamMember);
        record.setSkillName(skillName);
        record.setCategory(category);
        record.setSkillType(skillType);
        record.setSkillDescription(description);
        
        return createSkillRecord(record);
    }

    @Override
    public SkillDevelopmentRecord createSkillRecordWithTarget(Integer teamNumber, Integer season, TeamMember teamMember,
                                                             String skillName, SkillDevelopmentRecord.SkillCategory category,
                                                             SkillDevelopmentRecord.SkillType skillType, String description,
                                                             SkillDevelopmentRecord.CompetencyLevel targetLevel) {
        SkillDevelopmentRecord record = createSkillRecord(teamNumber, season, teamMember, skillName, category, skillType, description);
        record.setTargetLevel(targetLevel);
        record = calculateProgressToTarget(record.getId());
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateSkillRecord(Long recordId, SkillDevelopmentRecord record) {
        SkillDevelopmentRecord existing = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Update fields
        updateRecordFields(existing, record);
        existing.setLastUpdateDate(LocalDateTime.now());
        
        // Recalculate metrics
        existing = recalculateMetrics(existing);
        
        return recordRepository.save(existing);
    }

    @Override
    public void archiveSkillRecord(Long recordId, String reason) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setIsActive(false);
        record.setArchiveReason(reason);
        record.setArchivedAt(LocalDateTime.now());
        record.setLastUpdateDate(LocalDateTime.now());
        
        recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findActiveRecords(Integer teamNumber, Integer season) {
        return recordRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findBySkillCategory(Integer teamNumber, Integer season, 
                                                           SkillDevelopmentRecord.SkillCategory category) {
        return recordRepository.findByTeamNumberAndSeasonAndCategoryAndIsActiveTrue(teamNumber, season, category);
    }

    @Override
    public List<SkillDevelopmentRecord> findBySkillType(Integer teamNumber, Integer season, 
                                                       SkillDevelopmentRecord.SkillType skillType) {
        return recordRepository.findByTeamNumberAndSeasonAndSkillTypeAndIsActiveTrue(teamNumber, season, skillType);
    }

    // =========================================================================
    // COMPETENCY ASSESSMENT OPERATIONS
    // =========================================================================

    @Override
    public SkillDevelopmentRecord updateCompetencyLevel(Long recordId, SkillDevelopmentRecord.CompetencyLevel newLevel) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Validate progression
        if (!validateCompetencyProgression(recordId, newLevel)) {
            throw new RuntimeException("Invalid competency level progression");
        }
        
        // Store previous level
        record.setPreviousLevel(record.getCurrentLevel());
        record.setCurrentLevel(newLevel);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Calculate new competency score
        record = calculateCompetencyScore(record.getId());
        
        // Update progress towards target
        if (record.getTargetLevel() != null) {
            record = calculateProgressToTarget(record.getId());
        }
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordAssessment(Long recordId, Double competencyScore, 
                                                  SkillDevelopmentRecord.CompetencyLevel newLevel, String assessorNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Update assessment data
        record.setCompetencyScore(competencyScore);
        record.setAssessmentCount(record.getAssessmentCount() + 1);
        record.setLastAssessmentDate(LocalDate.now());
        record.setAssessorNotes(assessorNotes);
        
        // Update level if provided
        if (newLevel != null && !newLevel.equals(record.getCurrentLevel())) {
            record = updateCompetencyLevel(recordId, newLevel);
        }
        
        // Schedule next assessment
        record.setNextAssessmentDate(calculateNextAssessmentDate(record));
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateCompetencyScore(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Calculate score based on current level and various factors
        Double baseScore = record.getCurrentLevel().getBaseScore();
        
        // Apply adjustments based on practice, applications, consistency
        Double practiceAdjustment = calculatePracticeAdjustment(record);
        Double applicationAdjustment = calculateApplicationAdjustment(record);
        Double consistencyAdjustment = calculateConsistencyAdjustment(record);
        
        Double finalScore = baseScore + practiceAdjustment + applicationAdjustment + consistencyAdjustment;
        finalScore = Math.max(0.0, Math.min(100.0, finalScore)); // Clamp to 0-100
        
        record.setCompetencyScore(finalScore);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public SkillDevelopmentRecord setTargetLevel(Long recordId, SkillDevelopmentRecord.CompetencyLevel targetLevel) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setTargetLevel(targetLevel);
        record = calculateProgressToTarget(recordId);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateProgressToTarget(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        if (record.getTargetLevel() == null) {
            record.setProgressPercentage(0.0);
            return record;
        }
        
        // Calculate progress based on competency levels
        int currentLevelIndex = record.getCurrentLevel().ordinal();
        int targetLevelIndex = record.getTargetLevel().ordinal();
        
        if (currentLevelIndex >= targetLevelIndex) {
            record.setProgressPercentage(100.0);
        } else {
            double progress = ((double) currentLevelIndex / targetLevelIndex) * 100.0;
            record.setProgressPercentage(progress);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return record;
    }

    @Override
    public SkillDevelopmentRecord scheduleNextAssessment(Long recordId, LocalDate nextAssessmentDate) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setNextAssessmentDate(nextAssessmentDate);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findByCompetencyScoreRange(Integer teamNumber, Integer season, 
                                                                  Double minScore, Double maxScore) {
        return recordRepository.findByCompetencyScoreRange(teamNumber, season, minScore, maxScore);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighCompetencyRecords(Integer teamNumber, Integer season, Double minScore) {
        return recordRepository.findHighCompetencyRecords(teamNumber, season, minScore);
    }

    @Override
    public List<SkillDevelopmentRecord> findRecordsNeedingImprovement(Integer teamNumber, Integer season, Double maxScore) {
        return recordRepository.findRecordsNeedingImprovement(teamNumber, season, maxScore);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentAssessments(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentAssessments(teamNumber, season, sinceDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithUpcomingAssessments(Integer teamNumber, Integer season, Integer days) {
        LocalDate beforeDate = LocalDate.now().plusDays(days);
        return recordRepository.findWithUpcomingAssessments(teamNumber, season, beforeDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithOverdueAssessments(Integer teamNumber, Integer season) {
        return recordRepository.findWithOverdueAssessments(teamNumber, season);
    }

    // =========================================================================
    // LEARNING PATH MANAGEMENT
    // =========================================================================

    @Override
    public SkillDevelopmentRecord startLearningPath(Long recordId, Integer estimatedHours) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setLearningStatus(SkillDevelopmentRecord.LearningStatus.ACTIVE);
        record.setLearningStartDate(LocalDate.now());
        record.setEstimatedLearningHours(estimatedHours);
        record.setActualLearningHours(0);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateLearningStatus(Long recordId, SkillDevelopmentRecord.LearningStatus status) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setLearningStatus(status);
        
        if (status == SkillDevelopmentRecord.LearningStatus.COMPLETED) {
            record.setLearningCompletionDate(LocalDate.now());
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        record = calculateLearningEfficiency(recordId);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordLearningTime(Long recordId, Integer hours) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setActualLearningHours(record.getActualLearningHours() + hours);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update learning status if not already active
        if (record.getLearningStatus() == SkillDevelopmentRecord.LearningStatus.NOT_STARTED) {
            record.setLearningStatus(SkillDevelopmentRecord.LearningStatus.IN_PROGRESS);
            if (record.getLearningStartDate() == null) {
                record.setLearningStartDate(LocalDate.now());
            }
        }
        
        record = calculateLearningEfficiency(recordId);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord completeLearningPath(Long recordId) {
        return updateLearningStatus(recordId, SkillDevelopmentRecord.LearningStatus.COMPLETED);
    }

    @Override
    public SkillDevelopmentRecord pauseLearningPath(Long recordId, String reason) {
        SkillDevelopmentRecord record = updateLearningStatus(recordId, SkillDevelopmentRecord.LearningStatus.PAUSED);
        record.setPauseReason(reason);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord resumeLearningPath(Long recordId) {
        return updateLearningStatus(recordId, SkillDevelopmentRecord.LearningStatus.IN_PROGRESS);
    }

    @Override
    public SkillDevelopmentRecord calculateLearningEfficiency(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        if (record.getEstimatedLearningHours() == null || record.getEstimatedLearningHours() == 0) {
            record.setLearningEfficiency(0.0);
            return record;
        }
        
        // Calculate efficiency as estimated/actual ratio
        double efficiency = (double) record.getEstimatedLearningHours() / record.getActualLearningHours();
        efficiency = Math.min(2.0, efficiency); // Cap at 200% efficiency
        
        record.setLearningEfficiency(efficiency);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public List<SkillDevelopmentRecord> findActiveLearningPaths(Integer teamNumber, Integer season) {
        return recordRepository.findActiveLearningPaths(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findCompletedLearningPaths(Integer teamNumber, Integer season) {
        return recordRepository.findCompletedLearningPaths(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findPausedLearningPaths(Integer teamNumber, Integer season) {
        return recordRepository.findPausedLearningPaths(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findByLearningCompletionDateRange(Integer teamNumber, Integer season,
                                                                         LocalDate startDate, LocalDate endDate) {
        return recordRepository.findByLearningCompletionDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighEfficiencyLearning(Integer teamNumber, Integer season, Double minEfficiency) {
        return recordRepository.findHighEfficiencyLearning(teamNumber, season, minEfficiency);
    }

    @Override
    public List<SkillDevelopmentRecord> findLowEfficiencyLearning(Integer teamNumber, Integer season, Double maxEfficiency) {
        return recordRepository.findLowEfficiencyLearning(teamNumber, season, maxEfficiency);
    }

    // =========================================================================
    // MILESTONE AND PROGRESS TRACKING
    // =========================================================================

    @Override
    public SkillDevelopmentRecord recordMilestone(Long recordId, String milestoneDescription) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setMilestoneCount(record.getMilestoneCount() + 1);
        record.setLastMilestoneDate(LocalDate.now());
        record.setLastMilestoneDescription(milestoneDescription);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update progress if milestone targets are set
        if (record.getMilestoneTarget() != null && record.getMilestoneTarget() > 0) {
            double progress = ((double) record.getMilestoneCount() / record.getMilestoneTarget()) * 100.0;
            record.setProgressPercentage(Math.min(100.0, progress));
        }
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateProgress(Long recordId, Double progressPercentage) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setProgressPercentage(Math.max(0.0, Math.min(100.0, progressPercentage)));
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord setMilestoneTarget(Long recordId, Integer targetMilestones) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setMilestoneTarget(targetMilestones);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Recalculate progress
        record = calculateMilestoneCompletionRate(recordId);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateMilestoneCompletionRate(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        if (record.getMilestoneTarget() == null || record.getMilestoneTarget() == 0) {
            record.setMilestoneCompletionRate(0.0);
            return record;
        }
        
        double completionRate = ((double) record.getMilestoneCount() / record.getMilestoneTarget()) * 100.0;
        record.setMilestoneCompletionRate(Math.min(100.0, completionRate));
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public List<SkillDevelopmentRecord> findByProgressRange(Integer teamNumber, Integer season, 
                                                           Double minProgress, Double maxProgress) {
        return recordRepository.findByProgressRange(teamNumber, season, minProgress, maxProgress);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithCompletedMilestones(Integer teamNumber, Integer season) {
        return recordRepository.findWithCompletedMilestones(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentMilestones(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentMilestones(teamNumber, season, sinceDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findMeetingTargetLevel(Integer teamNumber, Integer season) {
        return recordRepository.findMeetingTargetLevel(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findBelowTargetLevel(Integer teamNumber, Integer season) {
        return recordRepository.findBelowTargetLevel(teamNumber, season);
    }

    // =========================================================================
    // PRACTICE AND APPLICATION TRACKING
    // =========================================================================

    @Override
    public SkillDevelopmentRecord recordPracticeSession(Long recordId, Integer hours, String practiceType, 
                                                       String practiceNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPracticeHours(record.getPracticeHours() + hours);
        record.setPracticeSessionCount(record.getPracticeSessionCount() + 1);
        record.setLastPracticeDate(LocalDate.now());
        record.setLastPracticeType(practiceType);
        record.setLastPracticeNotes(practiceNotes);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update days since activity
        record.setDaysSinceActivity(0);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordSkillApplication(Long recordId, String applicationContext, 
                                                        Boolean successful, String applicationNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setApplicationCount(record.getApplicationCount() + 1);
        if (successful) {
            record.setSuccessfulApplicationCount(record.getSuccessfulApplicationCount() + 1);
        }
        record.setLastApplicationDate(LocalDate.now());
        record.setLastApplicationContext(applicationContext);
        record.setLastApplicationNotes(applicationNotes);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Recalculate success rate
        record = calculateApplicationSuccessRate(recordId);
        
        // Update days since activity
        record.setDaysSinceActivity(0);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updatePracticeEffectiveness(Long recordId, Double effectiveness) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPracticeEffectiveness(effectiveness);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateApplicationSuccessRate(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        if (record.getApplicationCount() == 0) {
            record.setApplicationSuccessRate(0.0);
            return record;
        }
        
        double successRate = ((double) record.getSuccessfulApplicationCount() / record.getApplicationCount()) * 100.0;
        record.setApplicationSuccessRate(successRate);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public List<SkillDevelopmentRecord> findHighPracticeRecords(Integer teamNumber, Integer season, Integer minHours) {
        return recordRepository.findHighPracticeRecords(teamNumber, season, minHours);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentPractice(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentPractice(teamNumber, season, sinceDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighPracticeEffectiveness(Integer teamNumber, Integer season, 
                                                                     Double minEffectiveness) {
        return recordRepository.findHighPracticeEffectiveness(teamNumber, season, minEffectiveness);
    }

    @Override
    public List<SkillDevelopmentRecord> findLowPracticeEffectiveness(Integer teamNumber, Integer season, 
                                                                    Double maxEffectiveness) {
        return recordRepository.findLowPracticeEffectiveness(teamNumber, season, maxEffectiveness);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighApplicationSuccess(Integer teamNumber, Integer season, Double minRate) {
        return recordRepository.findHighApplicationSuccess(teamNumber, season, minRate);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentApplications(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentApplications(teamNumber, season, sinceDate);
    }

    // =========================================================================
    // ASSESSMENT AND EVALUATION SYSTEMS
    // =========================================================================

    @Override
    public SkillDevelopmentRecord recordMultiSourceAssessment(Long recordId, Map<String, Double> assessmentScores, 
                                                             String primaryAssessor) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Calculate composite score
        double compositeScore = assessmentScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        record.setMultiSourceAssessmentScore(compositeScore);
        record.setPrimaryAssessor(primaryAssessor);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update overall competency score
        record = calculateCompositeAssessmentScore(recordId);
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordPeerAssessment(Long recordId, Long assessorId, Double score, String feedback) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPeerAssessmentCount(record.getPeerAssessmentCount() + 1);
        
        // Calculate running average
        if (record.getPeerAssessmentScore() == null || record.getPeerAssessmentScore() == 0.0) {
            record.setPeerAssessmentScore(score);
        } else {
            double newAverage = ((record.getPeerAssessmentScore() * (record.getPeerAssessmentCount() - 1)) + score) 
                               / record.getPeerAssessmentCount();
            record.setPeerAssessmentScore(newAverage);
        }
        
        record.setLastPeerFeedback(feedback);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordSelfAssessment(Long recordId, Double score, String selfReflection) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setSelfAssessmentScore(score);
        record.setSelfReflectionNotes(selfReflection);
        record.setLastSelfAssessmentDate(LocalDate.now());
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordMentorAssessment(Long recordId, Long mentorId, Double score, 
                                                        String mentorFeedback, String developmentSuggestions) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setMentorAssessmentScore(score);
        record.setMentorFeedback(mentorFeedback);
        record.setDevelopmentSuggestions(developmentSuggestions);
        record.setLastMentorAssessmentDate(LocalDate.now());
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateCompositeAssessmentScore(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<Double> scores = new ArrayList<>();
        
        if (record.getCompetencyScore() != null && record.getCompetencyScore() > 0) {
            scores.add(record.getCompetencyScore());
        }
        if (record.getPeerAssessmentScore() != null && record.getPeerAssessmentScore() > 0) {
            scores.add(record.getPeerAssessmentScore());
        }
        if (record.getSelfAssessmentScore() != null && record.getSelfAssessmentScore() > 0) {
            scores.add(record.getSelfAssessmentScore());
        }
        if (record.getMentorAssessmentScore() != null && record.getMentorAssessmentScore() > 0) {
            scores.add(record.getMentorAssessmentScore());
        }
        
        if (scores.isEmpty()) {
            record.setCompositeAssessmentScore(0.0);
        } else {
            double compositeScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            record.setCompositeAssessmentScore(compositeScore);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return record;
    }

    @Override
    public SkillDevelopmentRecord updateFeedback(Long recordId, String feedback, String developmentNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setFeedbackNotes(feedback);
        record.setDevelopmentNotes(developmentNotes);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    // =========================================================================
    // CERTIFICATION AND RECOGNITION TRACKING
    // =========================================================================

    @Override
    public SkillDevelopmentRecord recordCertification(Long recordId, String certificationName, String certificationBody, 
                                                     LocalDate certificationDate, LocalDate expiryDate) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCertificationStatus(SkillDevelopmentRecord.CertificationStatus.CERTIFIED);
        record.setCertificationName(certificationName);
        record.setCertificationBody(certificationBody);
        record.setCertificationDate(certificationDate);
        record.setCertificationExpiryDate(expiryDate);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordRecognition(Long recordId, String recognitionType, String recognitionDetails, 
                                                   LocalDate recognitionDate) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setRecognitionCount(record.getRecognitionCount() + 1);
        record.setLastRecognitionType(recognitionType);
        record.setLastRecognitionDetails(recognitionDetails);
        record.setLastRecognitionDate(recognitionDate);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateCertificationStatus(Long recordId, 
                                                           SkillDevelopmentRecord.CertificationStatus status) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCertificationStatus(status);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord renewCertification(Long recordId, LocalDate newExpiryDate) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCertificationExpiryDate(newExpiryDate);
        record.setCertificationStatus(SkillDevelopmentRecord.CertificationStatus.CERTIFIED);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithActiveCertifications(Integer teamNumber, Integer season) {
        return recordRepository.findWithActiveCertifications(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithExpiringCertifications(Integer teamNumber, Integer season, Integer days) {
        LocalDate beforeDate = LocalDate.now().plusDays(days);
        return recordRepository.findWithExpiringCertifications(teamNumber, season, beforeDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecognitions(Integer teamNumber, Integer season) {
        return recordRepository.findWithRecognitions(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findEligibleForCertification(Integer teamNumber, Integer season, 
                                                                    Double minScore, Integer minHours) {
        return recordRepository.findEligibleForCertification(teamNumber, season, minScore, minHours);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentCertifications(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentCertifications(teamNumber, season, sinceDate);
    }

    // =========================================================================
    // PREREQUISITES AND DEPENDENCIES MANAGEMENT
    // =========================================================================

    @Override
    public SkillDevelopmentRecord addPrerequisite(Long recordId, String prerequisiteSkill, 
                                                 SkillDevelopmentRecord.CompetencyLevel requiredLevel) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> prerequisites = record.getPrerequisiteSkills();
        if (prerequisites == null) {
            prerequisites = new ArrayList<>();
        }
        
        String prerequisiteEntry = prerequisiteSkill + ":" + requiredLevel.name();
        if (!prerequisites.contains(prerequisiteEntry)) {
            prerequisites.add(prerequisiteEntry);
            record.setPrerequisiteSkills(prerequisites);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord removePrerequisite(Long recordId, String prerequisiteSkill) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> prerequisites = record.getPrerequisiteSkills();
        if (prerequisites != null) {
            prerequisites.removeIf(prereq -> prereq.startsWith(prerequisiteSkill + ":"));
            record.setPrerequisiteSkills(prerequisites);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public boolean checkPrerequisitesMet(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> prerequisites = record.getPrerequisiteSkills();
        if (prerequisites == null || prerequisites.isEmpty()) {
            return true;
        }
        
        // For this implementation, we assume prerequisites are met
        // In a full implementation, this would check other skill records
        return true;
    }

    @Override
    public SkillDevelopmentRecord addSkillDependency(Long recordId, String dependentSkill, String dependencyType) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> dependencies = record.getSkillDependencies();
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        
        String dependencyEntry = dependentSkill + ":" + dependencyType;
        if (!dependencies.contains(dependencyEntry)) {
            dependencies.add(dependencyEntry);
            record.setSkillDependencies(dependencies);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord removeSkillDependency(Long recordId, String dependentSkill) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> dependencies = record.getSkillDependencies();
        if (dependencies != null) {
            dependencies.removeIf(dep -> dep.startsWith(dependentSkill + ":"));
            record.setSkillDependencies(dependencies);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public List<String> findPrerequisiteGaps(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> gaps = new ArrayList<>();
        List<String> prerequisites = record.getPrerequisiteSkills();
        
        if (prerequisites != null) {
            for (String prereq : prerequisites) {
                // For this implementation, we return all prerequisites as gaps
                // In a full implementation, this would check actual skill levels
                gaps.add(prereq);
            }
        }
        
        return gaps;
    }

    // =========================================================================
    // RISK ASSESSMENT AND INTERVENTION SYSTEMS
    // =========================================================================

    @Override
    public SkillDevelopmentRecord calculateRiskScore(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        double riskScore = 0.0;
        
        // Factor 1: Days since activity
        if (record.getDaysSinceActivity() > 30) {
            riskScore += 30.0;
        } else if (record.getDaysSinceActivity() > 14) {
            riskScore += 15.0;
        }
        
        // Factor 2: Low competency score
        if (record.getCompetencyScore() < 40.0) {
            riskScore += 25.0;
        } else if (record.getCompetencyScore() < 60.0) {
            riskScore += 10.0;
        }
        
        // Factor 3: Low practice effectiveness
        if (record.getPracticeEffectiveness() < 0.5) {
            riskScore += 20.0;
        } else if (record.getPracticeEffectiveness() < 0.7) {
            riskScore += 10.0;
        }
        
        // Factor 4: Low engagement
        if (record.getEngagementScore() < 40.0) {
            riskScore += 15.0;
        }
        
        // Factor 5: Inconsistent performance
        if (record.getConsistencyScore() < 60.0) {
            riskScore += 10.0;
        }
        
        record.setRiskScore(Math.min(100.0, riskScore));
        record.setInterventionRequired(riskScore >= 60.0);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public SkillDevelopmentRecord updateInterventionStatus(Long recordId, boolean interventionRequired, String reason) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setInterventionRequired(interventionRequired);
        record.setInterventionReason(reason);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordInterventionAction(Long recordId, String interventionType, 
                                                          String interventionDescription, LocalDate interventionDate) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setInterventionCount(record.getInterventionCount() + 1);
        record.setLastInterventionType(interventionType);
        record.setLastInterventionDescription(interventionDescription);
        record.setLastInterventionDate(interventionDate);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateConsistencyScore(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Calculate consistency based on assessment variance and improvement trends
        double consistencyScore = 75.0; // Base consistency score
        
        // Adjust based on improvement rate variance
        if (record.getImprovementRate() != null) {
            if (record.getImprovementRate() > 0) {
                consistencyScore += 15.0; // Positive improvement
            } else if (record.getImprovementRate() < -0.1) {
                consistencyScore -= 20.0; // Declining performance
            }
        }
        
        // Adjust based on application success rate
        if (record.getApplicationSuccessRate() != null) {
            if (record.getApplicationSuccessRate() > 80.0) {
                consistencyScore += 10.0;
            } else if (record.getApplicationSuccessRate() < 50.0) {
                consistencyScore -= 15.0;
            }
        }
        
        record.setConsistencyScore(Math.max(0.0, Math.min(100.0, consistencyScore)));
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public SkillDevelopmentRecord updateEngagementScore(Long recordId, Double engagementScore) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setEngagementScore(engagementScore);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighRiskRecords(Integer teamNumber, Integer season, Double minRisk) {
        return recordRepository.findHighRiskRecords(teamNumber, season, minRisk);
    }

    @Override
    public List<SkillDevelopmentRecord> findRequiringAttention(Integer teamNumber, Integer season, 
                                                              Double highRisk, Integer maxInactiveDays) {
        return recordRepository.findRequiringAttention(teamNumber, season, highRisk, maxInactiveDays);
    }

    @Override
    public List<SkillDevelopmentRecord> findInconsistentPerformance(Integer teamNumber, Integer season, 
                                                                   Double maxConsistency) {
        return recordRepository.findInconsistentPerformance(teamNumber, season, maxConsistency);
    }

    @Override
    public List<SkillDevelopmentRecord> findLowEngagement(Integer teamNumber, Integer season, Double maxEngagement) {
        return recordRepository.findLowEngagement(teamNumber, season, maxEngagement);
    }

    @Override
    public List<SkillDevelopmentRecord> findStagnantRecords(Integer teamNumber, Integer season, Integer maxDays) {
        return recordRepository.findStagnantRecords(teamNumber, season, maxDays);
    }

    // =========================================================================
    // PERFORMANCE IMPACT MEASUREMENT
    // =========================================================================

    @Override
    public SkillDevelopmentRecord updatePerformanceImpactScore(Long recordId, Double impactScore) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPerformanceImpactScore(impactScore);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordPerformanceCorrelation(Long recordId, String performanceMetric, 
                                                              Double correlationValue) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Store performance correlation data
        List<String> correlations = record.getPerformanceCorrelations();
        if (correlations == null) {
            correlations = new ArrayList<>();
        }
        
        String correlationEntry = performanceMetric + ":" + correlationValue;
        correlations.add(correlationEntry);
        record.setPerformanceCorrelations(correlations);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateCompetitionUtilization(Long recordId, Double utilizationPercentage) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCompetitionUtilization(utilizationPercentage);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighPerformanceImpact(Integer teamNumber, Integer season, Double minImpact) {
        return recordRepository.findHighPerformanceImpact(teamNumber, season, minImpact);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighCompetitionUtilization(Integer teamNumber, Integer season, 
                                                                       Double minUtilization) {
        return recordRepository.findHighCompetitionUtilization(teamNumber, season, minUtilization);
    }

    // =========================================================================
    // MENTORSHIP AND INSTRUCTION INTEGRATION
    // =========================================================================

    @Override
    public SkillDevelopmentRecord recordMentorshipSession(Long recordId, Long mentorId, Integer durationMinutes, 
                                                         String sessionType, String sessionNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setMentorshipSessionCount(record.getMentorshipSessionCount() + 1);
        record.setMentorshipHours(record.getMentorshipHours() + (durationMinutes / 60.0));
        record.setLastMentorshipDate(LocalDate.now());
        record.setLastMentorshipType(sessionType);
        record.setLastMentorshipNotes(sessionNotes);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord recordInstructionSession(Long recordId, Long instructorId, String instructionType, 
                                                          Integer durationMinutes, String instructionNotes) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setInstructionSessionCount(record.getInstructionSessionCount() + 1);
        record.setInstructionHours(record.getInstructionHours() + (durationMinutes / 60.0));
        record.setLastInstructionDate(LocalDate.now());
        record.setLastInstructionType(instructionType);
        record.setLastInstructionNotes(instructionNotes);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateInstructionEffectiveness(Long recordId, Double effectiveness) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setInstructionEffectiveness(effectiveness);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updateMentorshipImpactScore(Long recordId, Double impactScore) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setMentorshipImpactScore(impactScore);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighInstructionEffectiveness(Integer teamNumber, Integer season, 
                                                                         Double minEffectiveness) {
        return recordRepository.findHighInstructionEffectiveness(teamNumber, season, minEffectiveness);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighMentorshipImpact(Integer teamNumber, Integer season, Double minImpact) {
        return recordRepository.findHighMentorshipImpact(teamNumber, season, minImpact);
    }

    @Override
    public List<SkillDevelopmentRecord> findWithRecentMentorship(Integer teamNumber, Integer season, Integer days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return recordRepository.findWithRecentMentorship(teamNumber, season, sinceDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findNeedingMentorshipSupport(Integer teamNumber, Integer season, 
                                                                    Double maxImpact, Double maxEffectiveness) {
        return recordRepository.findNeedingMentorshipSupport(teamNumber, season, maxImpact, maxEffectiveness);
    }

    // =========================================================================
    // COMPETITION READINESS EVALUATION
    // =========================================================================

    @Override
    public SkillDevelopmentRecord updateCompetitionReadiness(Long recordId, boolean competitionReady) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCompetitionReadiness(competitionReady);
        record.setLastUpdateDate(LocalDateTime.now());
        
        if (competitionReady) {
            record = calculateCompetitionReadinessScore(recordId);
        }
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord calculateCompetitionReadinessScore(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        double readinessScore = 0.0;
        
        // Factor 1: Competency score (40% weight)
        readinessScore += (record.getCompetencyScore() * 0.4);
        
        // Factor 2: Application success rate (30% weight)
        if (record.getApplicationSuccessRate() != null) {
            readinessScore += (record.getApplicationSuccessRate() * 0.3);
        }
        
        // Factor 3: Consistency score (20% weight)
        if (record.getConsistencyScore() != null) {
            readinessScore += (record.getConsistencyScore() * 0.2);
        }
        
        // Factor 4: Recent practice (10% weight)
        if (record.getDaysSinceActivity() <= 7) {
            readinessScore += 10.0;
        } else if (record.getDaysSinceActivity() <= 14) {
            readinessScore += 5.0;
        }
        
        record.setCompetitionReadinessScore(Math.min(100.0, readinessScore));
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public SkillDevelopmentRecord markCompetitionCritical(Long recordId, boolean critical) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setCompetitionCritical(critical);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public List<SkillDevelopmentRecord> findCompetitionReadySkills(Integer teamNumber, Integer season) {
        return recordRepository.findCompetitionReadySkills(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findCompetitionCriticalSkills(Integer teamNumber, Integer season) {
        return recordRepository.findCompetitionCriticalSkills(teamNumber, season);
    }

    // =========================================================================
    // DOCUMENTATION AND EVIDENCE PORTFOLIO
    // =========================================================================

    @Override
    public SkillDevelopmentRecord addDocumentationEvidence(Long recordId, String evidenceType, String evidenceDescription, 
                                                          String evidenceUrl) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> evidence = record.getDocumentationEvidence();
        if (evidence == null) {
            evidence = new ArrayList<>();
        }
        
        String evidenceEntry = evidenceType + ":" + evidenceDescription + ":" + evidenceUrl;
        evidence.add(evidenceEntry);
        record.setDocumentationEvidence(evidence);
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update portfolio completeness
        record = updatePortfolioCompleteness(recordId, calculatePortfolioCompleteness(record));
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord removeDocumentationEvidence(Long recordId, String evidenceType) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> evidence = record.getDocumentationEvidence();
        if (evidence != null) {
            evidence.removeIf(ev -> ev.startsWith(evidenceType + ":"));
            record.setDocumentationEvidence(evidence);
        }
        
        record.setLastUpdateDate(LocalDateTime.now());
        
        // Update portfolio completeness
        record = updatePortfolioCompleteness(recordId, calculatePortfolioCompleteness(record));
        
        return recordRepository.save(record);
    }

    @Override
    public SkillDevelopmentRecord updatePortfolioCompleteness(Long recordId, Double completenessPercentage) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPortfolioCompleteness(completenessPercentage);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return record;
    }

    @Override
    public SkillDevelopmentRecord validateEvidencePortfolio(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setPortfolioValidated(true);
        record.setPortfolioValidationDate(LocalDate.now());
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================

    @Override
    public Map<SkillDevelopmentRecord.SkillCategory, Long> countByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.countByCategory(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.SkillCategory) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Map<SkillDevelopmentRecord.SkillType, Long> countBySkillType(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.countBySkillType(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.SkillType) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Map<SkillDevelopmentRecord.CompetencyLevel, Long> countByCompetencyLevel(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.countByCompetencyLevel(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.CompetencyLevel) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Map<SkillDevelopmentRecord.LearningStatus, Long> countByLearningStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.countByLearningStatus(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.LearningStatus) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Map<SkillDevelopmentRecord.SkillCategory, Double> averageCompetencyByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.averageCompetencyByCategory(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.SkillCategory) r[0],
            r -> (Double) r[1]
        ));
    }

    @Override
    public Map<SkillDevelopmentRecord.SkillType, Long> totalPracticeHoursBySkillType(Integer teamNumber, Integer season) {
        List<Object[]> results = recordRepository.totalPracticeHoursBySkillType(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (SkillDevelopmentRecord.SkillType) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public List<SkillDevelopmentRecord> findMostImprovedSkills(Integer teamNumber, Integer season) {
        return recordRepository.findMostImprovedSkills(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findHighRetentionSkills(Integer teamNumber, Integer season, Double minRetention) {
        return recordRepository.findHighRetentionSkills(teamNumber, season, minRetention);
    }

    @Override
    public Map<String, Object> generateSkillsAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Basic statistics
        report.put("totalSkillRecords", countActiveRecords(teamNumber, season));
        report.put("categoryDistribution", countByCategory(teamNumber, season));
        report.put("skillTypeDistribution", countBySkillType(teamNumber, season));
        report.put("competencyLevelDistribution", countByCompetencyLevel(teamNumber, season));
        report.put("learningStatusDistribution", countByLearningStatus(teamNumber, season));
        
        // Performance metrics
        report.put("averageCompetencyByCategory", averageCompetencyByCategory(teamNumber, season));
        report.put("totalPracticeHoursBySkillType", totalPracticeHoursBySkillType(teamNumber, season));
        
        // Quality indicators
        report.put("highRiskSkills", findHighRiskRecords(teamNumber, season, 60.0));
        report.put("mostImprovedSkills", findMostImprovedSkills(teamNumber, season));
        report.put("competitionReadySkills", findCompetitionReadySkills(teamNumber, season));
        
        // Certification status
        report.put("activeCertifications", findWithActiveCertifications(teamNumber, season));
        report.put("expiringCertifications", findWithExpiringCertifications(teamNumber, season, 30));
        
        report.put("generatedAt", LocalDateTime.now());
        return report;
    }

    @Override
    public Map<String, Object> generateSkillsDashboardData(Integer teamNumber, Integer season) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Key metrics
        dashboard.put("totalSkills", countActiveRecords(teamNumber, season));
        dashboard.put("competitionReadySkills", findCompetitionReadySkills(teamNumber, season).size());
        dashboard.put("skillsNeedingAttention", findRequiringAttention(teamNumber, season, 60.0, 14).size());
        dashboard.put("recentCertifications", findWithRecentCertifications(teamNumber, season, 30).size());
        
        // Progress indicators
        dashboard.put("averageCompetencyScore", calculateAverageCompetencyScore(teamNumber, season));
        dashboard.put("skillsAboveTarget", findMeetingTargetLevel(teamNumber, season).size());
        dashboard.put("skillsBelowTarget", findBelowTargetLevel(teamNumber, season).size());
        
        // Recent activity
        dashboard.put("recentPractice", findWithRecentPractice(teamNumber, season, 7));
        dashboard.put("recentAssessments", findWithRecentAssessments(teamNumber, season, 7));
        dashboard.put("upcomingAssessments", findWithUpcomingAssessments(teamNumber, season, 7));
        
        dashboard.put("lastUpdated", LocalDateTime.now());
        return dashboard;
    }

    @Override
    public Map<String, Object> generateIndividualSkillsReport(Integer teamNumber, Integer season, TeamMember teamMember) {
        Map<String, Object> report = new HashMap<>();
        
        List<SkillDevelopmentRecord> memberSkills = findTeamMemberTopSkills(teamNumber, season, teamMember);
        
        report.put("teamMember", teamMember);
        report.put("totalSkills", memberSkills.size());
        report.put("topSkills", memberSkills);
        report.put("skillsNeedingImprovement", findTeamMemberSkillsNeedingImprovement(teamNumber, season, teamMember));
        report.put("certifications", findTeamMemberCertifications(teamNumber, season, teamMember));
        report.put("overallSkillScore", calculateTeamMemberOverallSkillScore(teamNumber, season, teamMember));
        report.put("developmentPlan", generateTeamMemberDevelopmentPlan(teamNumber, season, teamMember));
        
        report.put("generatedAt", LocalDateTime.now());
        return report;
    }

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<SkillDevelopmentRecord> searchBySkillName(Integer teamNumber, Integer season, String searchTerm) {
        return recordRepository.searchBySkillName(teamNumber, season, searchTerm);
    }

    @Override
    public List<SkillDevelopmentRecord> searchByDescription(Integer teamNumber, Integer season, String searchTerm) {
        return recordRepository.searchByDescription(teamNumber, season, searchTerm);
    }

    @Override
    public List<SkillDevelopmentRecord> fullTextSearch(Integer teamNumber, Integer season, String searchTerm) {
        return recordRepository.fullTextSearch(teamNumber, season, searchTerm);
    }

    @Override
    public List<SkillDevelopmentRecord> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria) {
        // This would implement complex search logic based on multiple criteria
        // For now, return all active records
        return findActiveRecords(teamNumber, season);
    }

    // =========================================================================
    // TEAM MEMBER SPECIFIC OPERATIONS
    // =========================================================================

    @Override
    public List<SkillDevelopmentRecord> findTeamMemberTopSkills(Integer teamNumber, Integer season, TeamMember teamMember) {
        return recordRepository.findTeamMemberTopSkills(teamNumber, season, teamMember);
    }

    @Override
    public List<SkillDevelopmentRecord> findTeamMemberSkillsNeedingImprovement(Integer teamNumber, Integer season, 
                                                                              TeamMember teamMember) {
        return recordRepository.findTeamMemberSkillsNeedingImprovement(teamNumber, season, teamMember);
    }

    @Override
    public List<SkillDevelopmentRecord> findTeamMemberCertifications(Integer teamNumber, Integer season, 
                                                                    TeamMember teamMember) {
        return recordRepository.findTeamMemberCertifications(teamNumber, season, teamMember);
    }

    @Override
    public Double calculateTeamMemberOverallSkillScore(Integer teamNumber, Integer season, TeamMember teamMember) {
        List<SkillDevelopmentRecord> memberSkills = findTeamMemberTopSkills(teamNumber, season, teamMember);
        
        if (memberSkills.isEmpty()) {
            return 0.0;
        }
        
        return memberSkills.stream()
                .mapToDouble(SkillDevelopmentRecord::getCompetencyScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public Map<String, Object> generateTeamMemberDevelopmentPlan(Integer teamNumber, Integer season, TeamMember teamMember) {
        Map<String, Object> plan = new HashMap<>();
        
        List<SkillDevelopmentRecord> skillsNeedingImprovement = findTeamMemberSkillsNeedingImprovement(teamNumber, season, teamMember);
        List<SkillDevelopmentRecord> topSkills = findTeamMemberTopSkills(teamNumber, season, teamMember);
        
        plan.put("prioritySkills", skillsNeedingImprovement.stream().limit(5).collect(Collectors.toList()));
        plan.put("strengthSkills", topSkills.stream().limit(5).collect(Collectors.toList()));
        plan.put("recommendedLearningHours", calculateRecommendedLearningHours(skillsNeedingImprovement));
        plan.put("suggestedMentorship", skillsNeedingImprovement.size() > 3);
        
        return plan;
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<SkillDevelopmentRecord> findMultiSeasonRecords(Integer teamNumber, List<Integer> seasons) {
        return recordRepository.findMultiSeasonRecords(teamNumber, seasons);
    }

    @Override
    public List<SkillDevelopmentRecord> findSkillEvolution(Integer teamNumber, TeamMember teamMember, String skillName) {
        return recordRepository.findSkillEvolution(teamNumber, teamMember, skillName);
    }

    @Override
    public Map<String, Object> compareSkillsAcrossSeasons(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> comparison = new HashMap<>();
        
        for (Integer season : seasons) {
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("totalSkills", countActiveRecords(teamNumber, season));
            seasonData.put("averageCompetency", calculateAverageCompetencyScore(teamNumber, season));
            seasonData.put("certifications", findWithActiveCertifications(teamNumber, season).size());
            comparison.put("season" + season, seasonData);
        }
        
        return comparison;
    }

    @Override
    public Map<String, Object> analyzeTeamSkillsEvolution(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> evolution = new HashMap<>();
        
        evolution.put("seasonComparison", compareSkillsAcrossSeasons(teamNumber, seasons));
        evolution.put("skillTrends", calculateSkillTrends(teamNumber, seasons));
        evolution.put("improvementAreas", identifyImprovementAreas(teamNumber, seasons));
        
        return evolution;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActiveRecords(Integer teamNumber, Integer season) {
        return recordRepository.countByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> findAllActiveRecords(Integer teamNumber, Integer season) {
        return recordRepository.findAllActiveRecords(teamNumber, season);
    }

    @Override
    public List<SkillDevelopmentRecord> createBulkRecords(List<SkillDevelopmentRecord> records) {
        List<SkillDevelopmentRecord> createdRecords = new ArrayList<>();
        
        for (SkillDevelopmentRecord record : records) {
            try {
                createdRecords.add(createSkillRecord(record));
            } catch (Exception e) {
                // Log error and continue with next record
                System.err.println("Failed to create skill record: " + e.getMessage());
            }
        }
        
        return createdRecords;
    }

    @Override
    public List<SkillDevelopmentRecord> updateBulkRecords(Map<Long, SkillDevelopmentRecord> recordUpdates) {
        List<SkillDevelopmentRecord> updatedRecords = new ArrayList<>();
        
        for (Map.Entry<Long, SkillDevelopmentRecord> entry : recordUpdates.entrySet()) {
            try {
                updatedRecords.add(updateSkillRecord(entry.getKey(), entry.getValue()));
            } catch (Exception e) {
                // Log error and continue with next record
                System.err.println("Failed to update skill record " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        return updatedRecords;
    }

    @Override
    public void bulkArchiveRecords(List<Long> recordIds, String reason) {
        for (Long recordId : recordIds) {
            try {
                archiveSkillRecord(recordId, reason);
            } catch (Exception e) {
                // Log error and continue with next record
                System.err.println("Failed to archive skill record " + recordId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateAllRiskScores(Integer teamNumber, Integer season) {
        List<SkillDevelopmentRecord> records = findActiveRecords(teamNumber, season);
        
        for (SkillDevelopmentRecord record : records) {
            try {
                calculateRiskScore(record.getId());
            } catch (Exception e) {
                // Log error and continue
                System.err.println("Failed to update risk score for record " + record.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateAllCompetencyScores(Integer teamNumber, Integer season) {
        List<SkillDevelopmentRecord> records = findActiveRecords(teamNumber, season);
        
        for (SkillDevelopmentRecord record : records) {
            try {
                calculateCompetencyScore(record.getId());
            } catch (Exception e) {
                // Log error and continue
                System.err.println("Failed to update competency score for record " + record.getId() + ": " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<SkillDevelopmentRecord> findArchivedRecords(Integer teamNumber, Integer season) {
        return recordRepository.findByTeamNumberAndSeasonAndIsActiveFalse(teamNumber, season);
    }

    @Override
    public SkillDevelopmentRecord restoreArchivedRecord(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        record.setIsActive(true);
        record.setArchiveReason(null);
        record.setArchivedAt(null);
        record.setLastUpdateDate(LocalDateTime.now());
        
        return recordRepository.save(record);
    }

    @Override
    public void permanentlyDeleteRecord(Long recordId) {
        recordRepository.deleteById(recordId);
    }

    @Override
    public List<SkillDevelopmentRecord> findCreatedInDateRange(Integer teamNumber, Integer season, 
                                                              LocalDateTime startDate, LocalDateTime endDate) {
        return recordRepository.findCreatedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<SkillDevelopmentRecord> findUpdatedInDateRange(Integer teamNumber, Integer season, 
                                                              LocalDateTime startDate, LocalDateTime endDate) {
        return recordRepository.findUpdatedInDateRange(teamNumber, season, startDate, endDate);
    }

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    @Override
    public Map<String, Object> exportToExternalFormat(Long recordId, String format) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("skillName", record.getSkillName());
        exportData.put("category", record.getCategory());
        exportData.put("competencyLevel", record.getCurrentLevel());
        exportData.put("competencyScore", record.getCompetencyScore());
        exportData.put("practiceHours", record.getPracticeHours());
        exportData.put("certificationStatus", record.getCertificationStatus());
        exportData.put("exportFormat", format);
        exportData.put("exportDate", LocalDateTime.now());
        
        return exportData;
    }

    @Override
    public SkillDevelopmentRecord importFromExternalSource(Map<String, Object> recordData, String sourceType) {
        SkillDevelopmentRecord record = new SkillDevelopmentRecord();
        
        // Map external data to internal structure
        record.setSkillName((String) recordData.get("skillName"));
        record.setSkillDescription((String) recordData.get("description"));
        record.setCompetencyScore(((Number) recordData.getOrDefault("competencyScore", 0.0)).doubleValue());
        record.setPracticeHours(((Number) recordData.getOrDefault("practiceHours", 0)).intValue());
        
        return createSkillRecord(record);
    }

    @Override
    public SkillDevelopmentRecord syncWithExternalLMS(Long recordId, String lmsType) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Placeholder for LMS integration
        record.setLastUpdateDate(LocalDateTime.now());
        record.setSyncStatus("SYNCED_" + lmsType);
        
        return recordRepository.save(record);
    }

    @Override
    public Map<String, Object> generateSkillsPortfolioReport(Integer teamNumber, Integer season, TeamMember teamMember) {
        Map<String, Object> portfolio = new HashMap<>();
        
        List<SkillDevelopmentRecord> memberSkills = findTeamMemberTopSkills(teamNumber, season, teamMember);
        
        portfolio.put("teamMember", teamMember);
        portfolio.put("skills", memberSkills);
        portfolio.put("certifications", findTeamMemberCertifications(teamNumber, season, teamMember));
        portfolio.put("competencyAnalysis", generateCompetencyAnalysis(memberSkills));
        portfolio.put("learningPath", generateLearningPathSummary(memberSkills));
        portfolio.put("achievements", generateAchievementsSummary(memberSkills));
        
        return portfolio;
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validateSkillRecord(SkillDevelopmentRecord record) {
        List<String> errors = new ArrayList<>();
        
        if (record.getTeamNumber() == null) {
            errors.add("Team number is required");
        }
        if (record.getSeason() == null) {
            errors.add("Season is required");
        }
        if (record.getTeamMember() == null) {
            errors.add("Team member is required");
        }
        if (record.getSkillName() == null || record.getSkillName().trim().isEmpty()) {
            errors.add("Skill name is required");
        }
        if (record.getCategory() == null) {
            errors.add("Skill category is required");
        }
        if (record.getSkillType() == null) {
            errors.add("Skill type is required");
        }
        
        return errors;
    }

    @Override
    public boolean validateCompetencyProgression(Long recordId, SkillDevelopmentRecord.CompetencyLevel newLevel) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Allow progression to any level for now
        // In a full implementation, this would check for prerequisites and logical progression
        return true;
    }

    @Override
    public boolean validateUserPermissions(Long recordId, Long userId, String operation) {
        // Placeholder for permission validation
        // In a full implementation, this would check user roles and permissions
        return true;
    }

    @Override
    public Map<String, Object> checkSkillRecordQuality(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        Map<String, Object> quality = new HashMap<>();
        
        int qualityScore = 0;
        List<String> issues = new ArrayList<>();
        
        // Check completeness
        if (record.getSkillDescription() != null && !record.getSkillDescription().trim().isEmpty()) {
            qualityScore += 20;
        } else {
            issues.add("Missing skill description");
        }
        
        if (record.getTargetLevel() != null) {
            qualityScore += 15;
        } else {
            issues.add("No target level set");
        }
        
        if (record.getPracticeHours() > 0) {
            qualityScore += 25;
        } else {
            issues.add("No practice hours recorded");
        }
        
        if (record.getAssessmentCount() > 0) {
            qualityScore += 25;
        } else {
            issues.add("No assessments recorded");
        }
        
        if (record.getDocumentationEvidence() != null && !record.getDocumentationEvidence().isEmpty()) {
            qualityScore += 15;
        } else {
            issues.add("No documentation evidence");
        }
        
        quality.put("qualityScore", qualityScore);
        quality.put("issues", issues);
        quality.put("qualityLevel", qualityScore >= 80 ? "HIGH" : qualityScore >= 60 ? "MEDIUM" : "LOW");
        
        return quality;
    }

    @Override
    public List<String> suggestSkillDevelopmentImprovements(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        List<String> suggestions = new ArrayList<>();
        
        if (record.getPracticeHours() < 10) {
            suggestions.add("Increase practice hours to improve competency");
        }
        
        if (record.getApplicationCount() == 0) {
            suggestions.add("Apply skill in real scenarios to validate competency");
        }
        
        if (record.getAssessmentCount() == 0) {
            suggestions.add("Schedule formal assessment to measure progress");
        }
        
        if (record.getCurrentLevel().ordinal() < record.getTargetLevel().ordinal()) {
            suggestions.add("Focus on bridging gap to target competency level");
        }
        
        if (record.getDaysSinceActivity() > 14) {
            suggestions.add("Resume active skill development - skill may be deteriorating");
        }
        
        return suggestions;
    }

    @Override
    public boolean validateLearningPathCompletion(Long recordId) {
        SkillDevelopmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Skill development record not found: " + recordId));
        
        // Check completion criteria
        boolean hasMinimumHours = record.getActualLearningHours() >= 5;
        boolean hasAssessment = record.getAssessmentCount() > 0;
        boolean hasApplication = record.getApplicationCount() > 0;
        boolean meetsCompetency = record.getCompetencyScore() >= 70.0;
        
        return hasMinimumHours && hasAssessment && hasApplication && meetsCompetency;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private SkillDevelopmentRecord calculateInitialMetrics(SkillDevelopmentRecord record) {
        // Set initial values for calculated fields
        record.setDaysSinceActivity(0);
        record.setRiskScore(0.0);
        record.setEngagementScore(50.0); // Default engagement
        record.setConsistencyScore(75.0); // Default consistency
        
        return record;
    }

    private SkillDevelopmentRecord recalculateMetrics(SkillDevelopmentRecord record) {
        // Recalculate all derived metrics
        calculateDaysSinceActivity(record);
        calculateRiskScore(record.getId());
        calculateConsistencyScore(record.getId());
        
        return record;
    }

    private void updateRecordFields(SkillDevelopmentRecord existing, SkillDevelopmentRecord update) {
        if (update.getSkillName() != null) {
            existing.setSkillName(update.getSkillName());
        }
        if (update.getSkillDescription() != null) {
            existing.setSkillDescription(update.getSkillDescription());
        }
        if (update.getCategory() != null) {
            existing.setCategory(update.getCategory());
        }
        if (update.getSkillType() != null) {
            existing.setSkillType(update.getSkillType());
        }
        if (update.getTargetLevel() != null) {
            existing.setTargetLevel(update.getTargetLevel());
        }
    }

    private LocalDate calculateNextAssessmentDate(SkillDevelopmentRecord record) {
        // Calculate next assessment based on current level and progress
        int daysToNext = 30; // Default 30 days
        
        if (record.getCurrentLevel() == SkillDevelopmentRecord.CompetencyLevel.NOVICE) {
            daysToNext = 14; // More frequent for beginners
        } else if (record.getCurrentLevel() == SkillDevelopmentRecord.CompetencyLevel.EXPERT) {
            daysToNext = 90; // Less frequent for experts
        }
        
        return LocalDate.now().plusDays(daysToNext);
    }

    private Double calculatePracticeAdjustment(SkillDevelopmentRecord record) {
        if (record.getPracticeHours() == 0) {
            return -10.0;
        } else if (record.getPracticeHours() > 50) {
            return 10.0;
        } else if (record.getPracticeHours() > 20) {
            return 5.0;
        }
        return 0.0;
    }

    private Double calculateApplicationAdjustment(SkillDevelopmentRecord record) {
        if (record.getApplicationSuccessRate() == null) {
            return 0.0;
        }
        
        if (record.getApplicationSuccessRate() > 90.0) {
            return 10.0;
        } else if (record.getApplicationSuccessRate() > 70.0) {
            return 5.0;
        } else if (record.getApplicationSuccessRate() < 50.0) {
            return -10.0;
        }
        return 0.0;
    }

    private Double calculateConsistencyAdjustment(SkillDevelopmentRecord record) {
        if (record.getConsistencyScore() == null) {
            return 0.0;
        }
        
        if (record.getConsistencyScore() > 85.0) {
            return 5.0;
        } else if (record.getConsistencyScore() < 60.0) {
            return -5.0;
        }
        return 0.0;
    }

    private void calculateDaysSinceActivity(SkillDevelopmentRecord record) {
        LocalDate lastActivity = null;
        
        if (record.getLastPracticeDate() != null) {
            lastActivity = record.getLastPracticeDate();
        }
        if (record.getLastApplicationDate() != null && 
            (lastActivity == null || record.getLastApplicationDate().isAfter(lastActivity))) {
            lastActivity = record.getLastApplicationDate();
        }
        if (record.getLastAssessmentDate() != null && 
            (lastActivity == null || record.getLastAssessmentDate().isAfter(lastActivity))) {
            lastActivity = record.getLastAssessmentDate();
        }
        
        if (lastActivity != null) {
            record.setDaysSinceActivity((int) ChronoUnit.DAYS.between(lastActivity, LocalDate.now()));
        } else {
            record.setDaysSinceActivity(999); // No activity recorded
        }
    }

    private Double calculatePortfolioCompleteness(SkillDevelopmentRecord record) {
        int completenessPoints = 0;
        int totalPoints = 100;
        
        // Basic information (20 points)
        if (record.getSkillDescription() != null && !record.getSkillDescription().trim().isEmpty()) {
            completenessPoints += 20;
        }
        
        // Assessment data (20 points)
        if (record.getAssessmentCount() > 0) {
            completenessPoints += 20;
        }
        
        // Practice data (20 points)
        if (record.getPracticeHours() > 0) {
            completenessPoints += 20;
        }
        
        // Application data (20 points)
        if (record.getApplicationCount() > 0) {
            completenessPoints += 20;
        }
        
        // Documentation evidence (20 points)
        if (record.getDocumentationEvidence() != null && !record.getDocumentationEvidence().isEmpty()) {
            completenessPoints += 20;
        }
        
        return ((double) completenessPoints / totalPoints) * 100.0;
    }

    private Double calculateAverageCompetencyScore(Integer teamNumber, Integer season) {
        List<SkillDevelopmentRecord> records = findActiveRecords(teamNumber, season);
        
        if (records.isEmpty()) {
            return 0.0;
        }
        
        return records.stream()
                .mapToDouble(SkillDevelopmentRecord::getCompetencyScore)
                .average()
                .orElse(0.0);
    }

    private Integer calculateRecommendedLearningHours(List<SkillDevelopmentRecord> skills) {
        return skills.size() * 10; // 10 hours per skill needing improvement
    }

    private Map<String, Object> calculateSkillTrends(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> trends = new HashMap<>();
        
        for (Integer season : seasons) {
            trends.put("season" + season, calculateAverageCompetencyScore(teamNumber, season));
        }
        
        return trends;
    }

    private List<String> identifyImprovementAreas(Integer teamNumber, List<Integer> seasons) {
        List<String> areas = new ArrayList<>();
        
        // Analyze trends and identify areas needing improvement
        areas.add("Technical Skills"); // Placeholder
        areas.add("Communication"); // Placeholder
        
        return areas;
    }

    private Map<String, Object> generateCompetencyAnalysis(List<SkillDevelopmentRecord> skills) {
        Map<String, Object> analysis = new HashMap<>();
        
        double averageScore = skills.stream()
                .mapToDouble(SkillDevelopmentRecord::getCompetencyScore)
                .average()
                .orElse(0.0);
        
        analysis.put("averageCompetency", averageScore);
        analysis.put("skillCount", skills.size());
        analysis.put("topSkills", skills.stream().limit(3).collect(Collectors.toList()));
        
        return analysis;
    }

    private Map<String, Object> generateLearningPathSummary(List<SkillDevelopmentRecord> skills) {
        Map<String, Object> summary = new HashMap<>();
        
        long activePathsCount = skills.stream()
                .filter(s -> s.getLearningStatus() == SkillDevelopmentRecord.LearningStatus.ACTIVE || 
                           s.getLearningStatus() == SkillDevelopmentRecord.LearningStatus.IN_PROGRESS)
                .count();
        
        summary.put("activePaths", activePathsCount);
        summary.put("totalLearningHours", skills.stream().mapToInt(SkillDevelopmentRecord::getActualLearningHours).sum());
        
        return summary;
    }

    private Map<String, Object> generateAchievementsSummary(List<SkillDevelopmentRecord> skills) {
        Map<String, Object> achievements = new HashMap<>();
        
        long certifiedSkills = skills.stream()
                .filter(s -> s.getCertificationStatus() == SkillDevelopmentRecord.CertificationStatus.CERTIFIED)
                .count();
        
        achievements.put("certifiedSkills", certifiedSkills);
        achievements.put("recognitions", skills.stream().mapToInt(SkillDevelopmentRecord::getRecognitionCount).sum());
        
        return achievements;
    }
}