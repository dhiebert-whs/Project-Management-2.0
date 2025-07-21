// src/main/java/org/frcpm/services/impl/MentorshipRelationshipServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.MentorshipRelationship;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.MentorshipRelationshipRepository;
import org.frcpm.services.MentorshipRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MentorshipRelationshipService.
 * 
 * Provides comprehensive mentorship relationship management services including
 * relationship analytics, progress monitoring, goal tracking, and mentorship
 * coordination with sophisticated risk assessment and outcome measurement.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.3 Mentorship Tracking System
 */
@Service
@Transactional
public class MentorshipRelationshipServiceImpl implements MentorshipRelationshipService {

    @Autowired
    private MentorshipRelationshipRepository relationshipRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public MentorshipRelationship create(MentorshipRelationship relationship) {
        return createRelationship(relationship);
    }

    @Override
    public MentorshipRelationship update(Long id, MentorshipRelationship relationship) {
        return updateRelationship(id, relationship);
    }

    @Override
    public void delete(Long id) {
        archiveRelationship(id, "Deleted by user");
    }

    @Override
    public Optional<MentorshipRelationship> findById(Long id) {
        return relationshipRepository.findById(id);
    }

    @Override
    public List<MentorshipRelationship> findAll() {
        return relationshipRepository.findAll().stream()
                .filter(MentorshipRelationship::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return relationshipRepository.existsById(id);
    }

    @Override
    public long count() {
        return relationshipRepository.count();
    }

    // =========================================================================
    // RELATIONSHIP MANAGEMENT
    // =========================================================================

    @Override
    public MentorshipRelationship createRelationship(MentorshipRelationship relationship) {
        validateRelationship(relationship);
        
        // Set defaults
        if (relationship.getStatus() == null) {
            relationship.setStatus(MentorshipRelationship.MentorshipStatus.PENDING);
        }
        if (relationship.getStartDate() == null) {
            relationship.setStartDate(LocalDate.now());
        }
        if (relationship.getExpectedDurationWeeks() == null) {
            relationship.setExpectedDurationWeeks(12); // Default 12-week mentorship
        }
        if (relationship.getExpectedEndDate() == null) {
            relationship.setExpectedEndDate(relationship.getStartDate().plusWeeks(relationship.getExpectedDurationWeeks()));
        }
        
        // Initialize metrics
        relationship.setCreatedAt(LocalDateTime.now());
        relationship.setUpdatedAt(LocalDateTime.now());
        relationship.setIsActive(true);
        relationship.setEngagementScore(0.0);
        relationship.setGoalCompletionPercentage(0.0);
        relationship.setSkillDevelopmentScore(0.0);
        relationship.setOverallSatisfactionScore(0.0);
        relationship.setRiskScore(0.0);
        relationship.setAttendanceRate(0.0);
        
        MentorshipRelationship saved = relationshipRepository.save(relationship);
        updateEngagementMetrics(saved.getId());
        return saved;
    }

    @Override
    public MentorshipRelationship createRelationship(Integer teamNumber, Integer season, TeamMember mentor,
                                                    TeamMember mentee, MentorshipRelationship.RelationshipType type,
                                                    MentorshipRelationship.MentorshipFocus primaryFocus) {
        MentorshipRelationship relationship = new MentorshipRelationship();
        relationship.setTeamNumber(teamNumber);
        relationship.setSeason(season);
        relationship.setMentor(mentor);
        relationship.setMentee(mentee);
        relationship.setRelationshipType(type);
        relationship.setPrimaryFocus(primaryFocus);
        
        return createRelationship(relationship);
    }

    @Override
    public MentorshipRelationship updateRelationship(Long relationshipId, MentorshipRelationship relationship) {
        MentorshipRelationship existing = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Update basic fields
        if (relationship.getRelationshipDescription() != null) {
            existing.setRelationshipDescription(relationship.getRelationshipDescription());
        }
        if (relationship.getMentorshipGoals() != null) {
            existing.setMentorshipGoals(relationship.getMentorshipGoals());
        }
        if (relationship.getPrimaryFocus() != null) {
            existing.setPrimaryFocus(relationship.getPrimaryFocus());
        }
        if (relationship.getExpectedEndDate() != null) {
            existing.setExpectedEndDate(relationship.getExpectedEndDate());
        }
        if (relationship.getExpectedDurationWeeks() != null) {
            existing.setExpectedDurationWeeks(relationship.getExpectedDurationWeeks());
        }
        
        // Update collections
        if (relationship.getFocusAreas() != null) {
            existing.setFocusAreas(new ArrayList<>(relationship.getFocusAreas()));
        }
        if (relationship.getSkillsDevelopment() != null) {
            existing.setSkillsDevelopment(new ArrayList<>(relationship.getSkillsDevelopment()));
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setLastModifiedBy(relationship.getLastModifiedBy());
        
        MentorshipRelationship updated = relationshipRepository.save(existing);
        updateEngagementMetrics(updated.getId());
        updateRiskAssessment(updated.getId());
        
        return updated;
    }

    @Override
    public void archiveRelationship(Long relationshipId, String reason) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setIsArchived(true);
        relationship.setArchiveReason(reason);
        relationship.setArchivedAt(LocalDateTime.now());
        relationship.setUpdatedAt(LocalDateTime.now());
        
        relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findActiveRelationships(Integer teamNumber, Integer season) {
        return relationshipRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findByStatus(Integer teamNumber, Integer season, 
                                                    MentorshipRelationship.MentorshipStatus status) {
        return relationshipRepository.findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(teamNumber, season, status);
    }

    @Override
    public List<MentorshipRelationship> findByType(Integer teamNumber, Integer season, 
                                                  MentorshipRelationship.RelationshipType type) {
        return relationshipRepository.findByTeamNumberAndSeasonAndRelationshipTypeAndIsActiveTrue(teamNumber, season, type);
    }

    // =========================================================================
    // MENTOR AND MENTEE MANAGEMENT
    // =========================================================================

    @Override
    public List<MentorshipRelationship> findByMentor(TeamMember mentor, Integer teamNumber, Integer season) {
        return relationshipRepository.findByMentorAndTeamNumberAndSeasonAndIsActiveTrue(mentor, teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findByMentee(TeamMember mentee, Integer teamNumber, Integer season) {
        return relationshipRepository.findByMenteeAndTeamNumberAndSeasonAndIsActiveTrue(mentee, teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findActiveMentorRelationships(TeamMember mentor, Integer teamNumber, Integer season) {
        return relationshipRepository.findActiveMentorRelationships(mentor, teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findActiveMenteeRelationships(TeamMember mentee, Integer teamNumber, Integer season) {
        return relationshipRepository.findActiveMenteeRelationships(mentee, teamNumber, season);
    }

    @Override
    public Long countMentorRelationships(TeamMember mentor, Integer teamNumber, Integer season) {
        return relationshipRepository.countMentorRelationships(mentor, teamNumber, season);
    }

    @Override
    public Long countMenteeRelationships(TeamMember mentee, Integer teamNumber, Integer season) {
        return relationshipRepository.countMenteeRelationships(mentee, teamNumber, season);
    }

    @Override
    public List<TeamMember> findAvailableMentors(Integer teamNumber, Integer season, Integer maxRelationships) {
        // Find mentors with fewer than maxRelationships active relationships
        List<MentorshipRelationship> activeRelationships = relationshipRepository
                .findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
        
        Map<TeamMember, Long> mentorCounts = activeRelationships.stream()
                .filter(r -> r.getStatus() == MentorshipRelationship.MentorshipStatus.ACTIVE ||
                           r.getStatus() == MentorshipRelationship.MentorshipStatus.PROGRESSING)
                .collect(Collectors.groupingBy(MentorshipRelationship::getMentor, Collectors.counting()));
        
        return mentorCounts.entrySet().stream()
                .filter(entry -> entry.getValue() < maxRelationships)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamMember> findPotentialMentees(Integer teamNumber, Integer season) {
        // Find team members without active mentorship relationships
        List<MentorshipRelationship> activeRelationships = relationshipRepository
                .findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
        
        Set<TeamMember> currentMentees = activeRelationships.stream()
                .filter(r -> r.getStatus() == MentorshipRelationship.MentorshipStatus.ACTIVE ||
                           r.getStatus() == MentorshipRelationship.MentorshipStatus.PROGRESSING)
                .map(MentorshipRelationship::getMentee)
                .collect(Collectors.toSet());
        
        // This would need integration with TeamMemberRepository to get all team members
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    // =========================================================================
    // MEETING AND INTERACTION TRACKING
    // =========================================================================

    @Override
    public MentorshipRelationship recordMeeting(Long relationshipId, LocalDateTime meetingDate, 
                                               Integer durationMinutes, String notes) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Update meeting counts
        relationship.setTotalMeetings(relationship.getTotalMeetings() + 1);
        relationship.setCompletedMeetings(relationship.getCompletedMeetings() + 1);
        relationship.setLastMeetingDate(meetingDate);
        
        // Update duration tracking
        if (durationMinutes != null) {
            int totalDuration = (relationship.getAverageMeetingDurationMinutes() * (relationship.getCompletedMeetings() - 1)) + durationMinutes;
            relationship.setAverageMeetingDurationMinutes(totalDuration / relationship.getCompletedMeetings());
        }
        
        // Calculate days since last meeting
        relationship.setDaysSinceLastMeeting(0);
        
        // Update attendance rate
        Double attendanceRate = calculateAttendanceRate(relationshipId);
        relationship.setAttendanceRate(attendanceRate);
        
        // Update engagement metrics
        relationship.setUpdatedAt(LocalDateTime.now());
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public MentorshipRelationship recordCancelledMeeting(Long relationshipId, LocalDateTime plannedDate, String reason) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setTotalMeetings(relationship.getTotalMeetings() + 1);
        relationship.setCancelledMeetings(relationship.getCancelledMeetings() + 1);
        
        // Update attendance rate
        Double attendanceRate = calculateAttendanceRate(relationshipId);
        relationship.setAttendanceRate(attendanceRate);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateRiskAssessment(updated.getId()); // Cancellations may increase risk
        
        return updated;
    }

    @Override
    public MentorshipRelationship updateMeetingFrequency(Long relationshipId, Integer meetingsPerWeek) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setPlannedMeetingsPerWeek(meetingsPerWeek);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public Double calculateAttendanceRate(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        if (relationship.getTotalMeetings() == 0) {
            return 0.0;
        }
        
        return (double) relationship.getCompletedMeetings() / relationship.getTotalMeetings() * 100.0;
    }

    @Override
    public List<MentorshipRelationship> findLowAttendanceRelationships(Integer teamNumber, Integer season, 
                                                                      Double minAttendance, Integer minMeetings) {
        return relationshipRepository.findLowAttendanceRelationships(teamNumber, season, minAttendance, minMeetings);
    }

    @Override
    public List<MentorshipRelationship> findNeedingMeetingScheduling(Integer teamNumber, Integer season, Integer maxDays) {
        return relationshipRepository.findNeedingMeetingScheduling(teamNumber, season, maxDays);
    }

    @Override
    public Double calculateAverageMeetingDuration(Integer teamNumber, Integer season) {
        return relationshipRepository.findAverageMeetingDuration(teamNumber, season).orElse(0.0);
    }

    // =========================================================================
    // GOAL AND MILESTONE TRACKING
    // =========================================================================

    @Override
    public MentorshipRelationship setGoals(Long relationshipId, String goals, List<String> milestones) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setMentorshipGoals(goals);
        
        // Set milestones (would need additional model for complex milestone tracking)
        if (milestones != null && !milestones.isEmpty()) {
            relationship.setTotalGoals(milestones.size());
            relationship.setCompletedGoals(0);
            relationship.setGoalCompletionPercentage(0.0);
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship recordMilestoneCompletion(Long relationshipId, String milestone, LocalDate completionDate) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Update goal completion
        if (relationship.getTotalGoals() > 0) {
            int completed = relationship.getCompletedGoals() + 1;
            relationship.setCompletedGoals(completed);
            
            Double completionPercentage = (double) completed / relationship.getTotalGoals() * 100.0;
            relationship.setGoalCompletionPercentage(completionPercentage);
            
            // Update next milestone date if needed
            if (completed < relationship.getTotalGoals()) {
                // Calculate next milestone date based on completion rate
                LocalDate nextMilestone = completionDate.plusWeeks(2); // Default 2-week intervals
                relationship.setNextMilestoneDate(nextMilestone);
            }
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public MentorshipRelationship updateGoalCompletion(Long relationshipId, Double completionPercentage) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setGoalCompletionPercentage(completionPercentage);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public List<MentorshipRelationship> findWithUpcomingMilestones(Integer teamNumber, Integer season, 
                                                                  LocalDate startDate, LocalDate endDate) {
        return relationshipRepository.findWithUpcomingMilestones(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<MentorshipRelationship> findWithOverdueMilestones(Integer teamNumber, Integer season, LocalDate currentDate) {
        return relationshipRepository.findWithOverdueMilestones(teamNumber, season, currentDate);
    }

    @Override
    public List<MentorshipRelationship> findByGoalCompletionRange(Integer teamNumber, Integer season, 
                                                                 Double minPercentage, Double maxPercentage) {
        return relationshipRepository.findByGoalCompletionRange(teamNumber, season, minPercentage, maxPercentage);
    }

    @Override
    public Map<String, Object> calculateGoalCompletionMetrics(Integer teamNumber, Integer season) {
        List<MentorshipRelationship> relationships = findActiveRelationships(teamNumber, season);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRelationships", relationships.size());
        
        if (!relationships.isEmpty()) {
            double avgCompletion = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getGoalCompletionPercentage)
                    .average().orElse(0.0);
            metrics.put("averageGoalCompletion", avgCompletion);
            
            long onTrack = relationships.stream()
                    .filter(r -> r.getGoalCompletionPercentage() >= 70.0)
                    .count();
            metrics.put("relationshipsOnTrack", onTrack);
            
            long needingSupport = relationships.stream()
                    .filter(r -> r.getGoalCompletionPercentage() < 30.0)
                    .count();
            metrics.put("relationshipsNeedingSupport", needingSupport);
        }
        
        return metrics;
    }

    // =========================================================================
    // FEEDBACK AND ASSESSMENT
    // =========================================================================

    @Override
    public MentorshipRelationship recordFeedback(Long relationshipId, TeamMember feedbackProvider, 
                                                String feedback, Integer rating) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Update feedback counts
        relationship.setFeedbackCount(relationship.getFeedbackCount() + 1);
        
        // Update ratings (simplified - would need more complex feedback tracking in real implementation)
        if (rating != null) {
            if (feedbackProvider.equals(relationship.getMentee())) {
                relationship.setMentorRating(rating);
            } else if (feedbackProvider.equals(relationship.getMentor())) {
                relationship.setMenteeRating(rating);
            }
        }
        
        // Update overall satisfaction
        Double satisfaction = calculateOverallSatisfaction(relationshipId);
        relationship.setOverallSatisfactionScore(satisfaction);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public MentorshipRelationship recordMenteeFeedback(Long relationshipId, String feedback, Integer mentorRating) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setMentorRating(mentorRating);
        relationship.setFeedbackCount(relationship.getFeedbackCount() + 1);
        
        // Update satisfaction scores
        Double satisfaction = calculateOverallSatisfaction(relationshipId);
        relationship.setOverallSatisfactionScore(satisfaction);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship recordMentorFeedback(Long relationshipId, String feedback, Integer menteeRating) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setMenteeRating(menteeRating);
        relationship.setFeedbackCount(relationship.getFeedbackCount() + 1);
        
        // Update satisfaction scores
        Double satisfaction = calculateOverallSatisfaction(relationshipId);
        relationship.setOverallSatisfactionScore(satisfaction);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship updateSatisfactionScores(Long relationshipId, Double mentorSatisfaction, 
                                                          Double menteeSatisfaction) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setMentorSatisfactionScore(mentorSatisfaction);
        relationship.setMenteeSatisfactionScore(menteeSatisfaction);
        
        // Calculate overall satisfaction
        Double overall = (mentorSatisfaction + menteeSatisfaction) / 2.0;
        relationship.setOverallSatisfactionScore(overall);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public Double calculateOverallSatisfaction(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        double totalScore = 0.0;
        int components = 0;
        
        if (relationship.getMentorSatisfactionScore() != null) {
            totalScore += relationship.getMentorSatisfactionScore();
            components++;
        }
        if (relationship.getMenteeSatisfactionScore() != null) {
            totalScore += relationship.getMenteeSatisfactionScore();
            components++;
        }
        
        // Include ratings if available
        if (relationship.getMentorRating() != null) {
            totalScore += relationship.getMentorRating() * 20.0; // Convert 5-point to 100-point scale
            components++;
        }
        if (relationship.getMenteeRating() != null) {
            totalScore += relationship.getMenteeRating() * 20.0;
            components++;
        }
        
        return components > 0 ? totalScore / components : 0.0;
    }

    @Override
    public List<MentorshipRelationship> findHighestSatisfaction(Integer teamNumber, Integer season, 
                                                               Double minScore, Integer minFeedback) {
        return relationshipRepository.findHighestSatisfaction(teamNumber, season, minScore, minFeedback);
    }

    // =========================================================================
    // SKILLS DEVELOPMENT TRACKING
    // =========================================================================

    @Override
    public MentorshipRelationship addSkillDevelopment(Long relationshipId, String skill) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        if (relationship.getSkillsDevelopment() == null) {
            relationship.setSkillsDevelopment(new ArrayList<>());
        }
        
        if (!relationship.getSkillsDevelopment().contains(skill)) {
            relationship.getSkillsDevelopment().add(skill);
            relationship.setUpdatedAt(LocalDateTime.now());
            return relationshipRepository.save(relationship);
        }
        
        return relationship;
    }

    @Override
    public MentorshipRelationship recordSkillProgress(Long relationshipId, String skill, Integer progressLevel) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Update skill development score based on progress
        if (relationship.getSkillsDevelopment() != null && relationship.getSkillsDevelopment().contains(skill)) {
            // Calculate skill development score (simplified implementation)
            double currentScore = relationship.getSkillDevelopmentScore();
            double skillWeight = 100.0 / relationship.getSkillsDevelopment().size();
            double skillScore = progressLevel * 20.0; // Convert 5-point to 100-point scale
            
            // Update overall score
            relationship.setSkillDevelopmentScore(Math.min(100.0, currentScore + (skillScore * skillWeight / 100.0)));
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship updateSkillDevelopmentScore(Long relationshipId, Double score) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setSkillDevelopmentScore(score);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        updateEngagementMetrics(updated.getId());
        
        return updated;
    }

    @Override
    public List<MentorshipRelationship> findBySkillDevelopment(Integer teamNumber, Integer season, String skill) {
        return relationshipRepository.findBySkillDevelopment(teamNumber, season, skill);
    }

    @Override
    public List<MentorshipRelationship> findHighSkillDevelopment(Integer teamNumber, Integer season, Double minScore) {
        return relationshipRepository.findHighSkillDevelopment(teamNumber, season, minScore);
    }

    @Override
    public Map<String, Long> findMostDevelopedSkills(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.findMostDevelopedSkills(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Object> calculateSkillDevelopmentMetrics(Integer teamNumber, Integer season) {
        List<MentorshipRelationship> relationships = findActiveRelationships(teamNumber, season);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRelationships", relationships.size());
        
        if (!relationships.isEmpty()) {
            double avgSkillDevelopment = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getSkillDevelopmentScore)
                    .average().orElse(0.0);
            metrics.put("averageSkillDevelopmentScore", avgSkillDevelopment);
            
            long highPerformers = relationships.stream()
                    .filter(r -> r.getSkillDevelopmentScore() >= 80.0)
                    .count();
            metrics.put("highSkillDevelopmentCount", highPerformers);
            
            // Most common skills
            Map<String, Long> skills = findMostDevelopedSkills(teamNumber, season);
            metrics.put("topSkillAreas", skills);
        }
        
        return metrics;
    }

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    @Override
    public Double calculateEngagementScore(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        double engagementScore = 0.0;
        int factors = 0;
        
        // Meeting attendance factor (30%)
        if (relationship.getAttendanceRate() != null) {
            engagementScore += relationship.getAttendanceRate() * 0.3;
            factors++;
        }
        
        // Goal completion factor (25%)
        if (relationship.getGoalCompletionPercentage() != null) {
            engagementScore += relationship.getGoalCompletionPercentage() * 0.25;
            factors++;
        }
        
        // Satisfaction factor (25%)
        if (relationship.getOverallSatisfactionScore() != null) {
            engagementScore += relationship.getOverallSatisfactionScore() * 0.25;
            factors++;
        }
        
        // Skill development factor (20%)
        if (relationship.getSkillDevelopmentScore() != null) {
            engagementScore += relationship.getSkillDevelopmentScore() * 0.2;
            factors++;
        }
        
        return factors > 0 ? engagementScore : 0.0;
    }

    @Override
    public MentorshipRelationship updateEngagementMetrics(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Calculate engagement score
        Double engagementScore = calculateEngagementScore(relationshipId);
        relationship.setEngagementScore(engagementScore);
        
        // Calculate meeting frequency score
        Double frequencyScore = calculateMeetingFrequencyScore(relationshipId);
        relationship.setMeetingFrequencyScore(frequencyScore);
        
        // Update days since last meeting
        if (relationship.getLastMeetingDate() != null) {
            long daysSince = ChronoUnit.DAYS.between(relationship.getLastMeetingDate().toLocalDate(), LocalDate.now());
            relationship.setDaysSinceLastMeeting((int) daysSince);
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findMostEngaging(Integer teamNumber, Integer season, Double minScore) {
        return relationshipRepository.findMostEngaging(teamNumber, season, minScore);
    }

    @Override
    public List<MentorshipRelationship> findLowEngagement(Integer teamNumber, Integer season, Double maxScore) {
        return relationshipRepository.findLowEngagement(teamNumber, season, maxScore);
    }

    @Override
    public Double calculateMeetingFrequencyScore(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        if (relationship.getStartDate() == null) {
            return 0.0;
        }
        
        // Calculate weeks since start
        long weeksSinceStart = ChronoUnit.WEEKS.between(relationship.getStartDate(), LocalDate.now());
        if (weeksSinceStart == 0) weeksSinceStart = 1;
        
        // Expected meetings based on planned frequency
        int expectedMeetings = (int) (weeksSinceStart * relationship.getPlannedMeetingsPerWeek());
        if (expectedMeetings == 0) return 0.0;
        
        // Calculate frequency score
        double frequencyRatio = (double) relationship.getCompletedMeetings() / expectedMeetings;
        return Math.min(100.0, frequencyRatio * 100.0);
    }

    @Override
    public List<MentorshipRelationship> findHighMeetingFrequency(Integer teamNumber, Integer season, Double minFrequency) {
        return relationshipRepository.findHighMeetingFrequency(teamNumber, season, minFrequency);
    }

    @Override
    public Double calculateAverageSatisfactionScore(Integer teamNumber, Integer season) {
        return relationshipRepository.findAverageSatisfactionScore(teamNumber, season).orElse(0.0);
    }

    // =========================================================================
    // RISK ASSESSMENT AND INTERVENTION
    // =========================================================================

    @Override
    public Double calculateRiskScore(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        double riskScore = 0.0;
        
        // Low attendance increases risk
        if (relationship.getAttendanceRate() != null && relationship.getAttendanceRate() < 70.0) {
            riskScore += (70.0 - relationship.getAttendanceRate()) * 0.5;
        }
        
        // Low satisfaction increases risk
        if (relationship.getOverallSatisfactionScore() != null && relationship.getOverallSatisfactionScore() < 60.0) {
            riskScore += (60.0 - relationship.getOverallSatisfactionScore()) * 0.4;
        }
        
        // Poor goal progress increases risk
        if (relationship.getGoalCompletionPercentage() != null && relationship.getGoalCompletionPercentage() < 50.0) {
            riskScore += (50.0 - relationship.getGoalCompletionPercentage()) * 0.3;
        }
        
        // Too many days since last meeting
        if (relationship.getDaysSinceLastMeeting() != null && relationship.getDaysSinceLastMeeting() > 14) {
            riskScore += Math.min(30.0, relationship.getDaysSinceLastMeeting() - 14) * 2.0;
        }
        
        // High cancellation rate
        if (relationship.getTotalMeetings() > 0) {
            double cancellationRate = (double) relationship.getCancelledMeetings() / relationship.getTotalMeetings();
            if (cancellationRate > 0.2) {
                riskScore += (cancellationRate - 0.2) * 100.0;
            }
        }
        
        return Math.min(100.0, riskScore);
    }

    @Override
    public Map<String, Object> assessRelationshipRisk(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        Map<String, Object> assessment = new HashMap<>();
        assessment.put("relationshipId", relationshipId);
        assessment.put("riskScore", calculateRiskScore(relationshipId));
        
        List<String> riskFactors = new ArrayList<>();
        
        if (relationship.getAttendanceRate() != null && relationship.getAttendanceRate() < 70.0) {
            riskFactors.add("Low meeting attendance: " + String.format("%.1f%%", relationship.getAttendanceRate()));
        }
        
        if (relationship.getOverallSatisfactionScore() != null && relationship.getOverallSatisfactionScore() < 60.0) {
            riskFactors.add("Low satisfaction score: " + String.format("%.1f", relationship.getOverallSatisfactionScore()));
        }
        
        if (relationship.getDaysSinceLastMeeting() != null && relationship.getDaysSinceLastMeeting() > 14) {
            riskFactors.add("No recent meetings: " + relationship.getDaysSinceLastMeeting() + " days");
        }
        
        assessment.put("riskFactors", riskFactors);
        assessment.put("requiresIntervention", !riskFactors.isEmpty());
        
        return assessment;
    }

    @Override
    public MentorshipRelationship updateRiskAssessment(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        Double riskScore = calculateRiskScore(relationshipId);
        relationship.setRiskScore(riskScore);
        
        // Set intervention flags based on risk level
        if (riskScore >= 70.0) {
            relationship.setRequiresIntervention(true);
            relationship.setHasInterventionAlert(true);
        } else if (riskScore >= 50.0) {
            relationship.setRequiresIntervention(true);
            relationship.setHasInterventionAlert(false);
        } else {
            relationship.setRequiresIntervention(false);
            relationship.setHasInterventionAlert(false);
        }
        
        relationship.setLastRiskAssessment(LocalDateTime.now());
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findHighRiskRelationships(Integer teamNumber, Integer season, Double minRiskScore) {
        return relationshipRepository.findHighRiskRelationships(teamNumber, season, minRiskScore);
    }

    @Override
    public List<MentorshipRelationship> findRequiringIntervention(Integer teamNumber, Integer season) {
        return relationshipRepository.findRequiringIntervention(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findWithInterventionAlerts(Integer teamNumber, Integer season) {
        return relationshipRepository.findWithInterventionAlerts(teamNumber, season);
    }

    @Override
    public MentorshipRelationship flagForIntervention(Long relationshipId, String reason) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setRequiresIntervention(true);
        relationship.setInterventionReason(reason);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship createInterventionAlert(Long relationshipId, String alertMessage) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setHasInterventionAlert(true);
        relationship.setInterventionAlertMessage(alertMessage);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findNeedingImmediateAttention(Integer teamNumber, Integer season, 
                                                                     Double urgentRiskScore) {
        return relationshipRepository.findNeedingImmediateAttention(teamNumber, season, urgentRiskScore);
    }

    // =========================================================================
    // MENTOR PERFORMANCE AND EFFECTIVENESS
    // =========================================================================

    @Override
    public List<Object[]> findMostEffectiveMentors(Integer teamNumber, Integer season, Long minRelationships) {
        return relationshipRepository.findMostEffectiveMentors(teamNumber, season, minRelationships);
    }

    @Override
    public Map<String, Object> evaluateMentorEffectiveness(TeamMember mentor, Integer teamNumber, Integer season) {
        List<MentorshipRelationship> relationships = findByMentor(mentor, teamNumber, season);
        
        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("mentorId", mentor.getId());
        evaluation.put("totalRelationships", relationships.size());
        
        if (!relationships.isEmpty()) {
            double avgSatisfaction = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getOverallSatisfactionScore)
                    .average().orElse(0.0);
            evaluation.put("averageSatisfaction", avgSatisfaction);
            
            double avgGoalCompletion = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getGoalCompletionPercentage)
                    .average().orElse(0.0);
            evaluation.put("averageGoalCompletion", avgGoalCompletion);
            
            long successfulRelationships = relationships.stream()
                    .filter(r -> r.getStatus() == MentorshipRelationship.MentorshipStatus.COMPLETED &&
                               r.getOverallSatisfactionScore() >= 80.0)
                    .count();
            evaluation.put("successfulRelationships", successfulRelationships);
            
            double successRate = (double) successfulRelationships / relationships.size() * 100.0;
            evaluation.put("successRate", successRate);
        }
        
        return evaluation;
    }

    @Override
    public List<MentorshipRelationship> findMentorsReadyForAdvancement(Integer teamNumber, Integer season) {
        return relationshipRepository.findMentorsReadyForAdvancement(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findMentorsNeedingSupport(Integer teamNumber, Integer season, 
                                                                 Double minSatisfaction, Double minEngagement, 
                                                                 Double maxRisk) {
        return relationshipRepository.findMentorsNeedingSupport(teamNumber, season, minSatisfaction, minEngagement, maxRisk);
    }

    @Override
    public MentorshipRelationship setMentorAdvancementEligibility(Long relationshipId, boolean eligible) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setMentorAdvancementEligible(eligible);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public Map<String, Object> calculateMentorSuccessMetrics(TeamMember mentor, Integer teamNumber, Integer season) {
        Map<String, Object> evaluation = evaluateMentorEffectiveness(mentor, teamNumber, season);
        
        // Add additional success metrics
        List<MentorshipRelationship> relationships = findByMentor(mentor, teamNumber, season);
        
        if (!relationships.isEmpty()) {
            double avgEngagement = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getEngagementScore)
                    .average().orElse(0.0);
            evaluation.put("averageEngagement", avgEngagement);
            
            double avgSkillDevelopment = relationships.stream()
                    .mapToDouble(MentorshipRelationship::getSkillDevelopmentScore)
                    .average().orElse(0.0);
            evaluation.put("averageSkillDevelopment", avgSkillDevelopment);
            
            long highRiskRelationships = relationships.stream()
                    .filter(r -> r.getRiskScore() >= 70.0)
                    .count();
            evaluation.put("highRiskRelationships", highRiskRelationships);
        }
        
        return evaluation;
    }

    // =========================================================================
    // OUTCOME AND SUCCESS MEASUREMENT  
    // =========================================================================

    @Override
    public MentorshipRelationship recordOutcome(Long relationshipId, String outcome, Double impactScore) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setOutcome(outcome);
        relationship.setImpactScore(impactScore);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public Double calculateImpactScore(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        double impactScore = 0.0;
        int factors = 0;
        
        // Goal completion impact (40%)
        if (relationship.getGoalCompletionPercentage() != null) {
            impactScore += relationship.getGoalCompletionPercentage() * 0.4;
            factors++;
        }
        
        // Skill development impact (30%)
        if (relationship.getSkillDevelopmentScore() != null) {
            impactScore += relationship.getSkillDevelopmentScore() * 0.3;
            factors++;
        }
        
        // Satisfaction impact (20%)
        if (relationship.getOverallSatisfactionScore() != null) {
            impactScore += relationship.getOverallSatisfactionScore() * 0.2;
            factors++;
        }
        
        // Engagement impact (10%)
        if (relationship.getEngagementScore() != null) {
            impactScore += relationship.getEngagementScore() * 0.1;
            factors++;
        }
        
        return factors > 0 ? impactScore : 0.0;
    }

    @Override
    public Double calculateSuccessProbability(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        // Calculate based on current metrics
        double probability = 50.0; // Base probability
        
        // Adjust based on engagement
        if (relationship.getEngagementScore() != null) {
            probability += (relationship.getEngagementScore() - 50.0) * 0.5;
        }
        
        // Adjust based on risk
        if (relationship.getRiskScore() != null) {
            probability -= relationship.getRiskScore() * 0.3;
        }
        
        // Adjust based on goal progress
        if (relationship.getGoalCompletionPercentage() != null) {
            probability += (relationship.getGoalCompletionPercentage() - 50.0) * 0.4;
        }
        
        return Math.max(0.0, Math.min(100.0, probability));
    }

    @Override
    public MentorshipRelationship updateOutcomeMetrics(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        Double impactScore = calculateImpactScore(relationshipId);
        relationship.setImpactScore(impactScore);
        
        Double successProbability = calculateSuccessProbability(relationshipId);
        relationship.setSuccessProbability(successProbability);
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findCompletedWithOutcomes(Integer teamNumber, Integer season) {
        return relationshipRepository.findCompletedWithOutcomes(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findHighImpactRelationships(Integer teamNumber, Integer season, Double minImpact) {
        return relationshipRepository.findHighImpactRelationships(teamNumber, season, minImpact);
    }

    @Override
    public Double calculateAverageSuccessProbability(Integer teamNumber, Integer season) {
        return relationshipRepository.findAverageSuccessProbability(teamNumber, season).orElse(0.0);
    }

    @Override
    public Map<String, Object> generateSuccessMetricsReport(Integer teamNumber, Integer season) {
        List<MentorshipRelationship> relationships = findActiveRelationships(teamNumber, season);
        
        Map<String, Object> report = new HashMap<>();
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("totalRelationships", relationships.size());
        
        if (!relationships.isEmpty()) {
            double avgSuccessProbability = calculateAverageSuccessProbability(teamNumber, season);
            report.put("averageSuccessProbability", avgSuccessProbability);
            
            long highProbability = relationships.stream()
                    .filter(r -> r.getSuccessProbability() != null && r.getSuccessProbability() >= 80.0)
                    .count();
            report.put("highSuccessProbabilityCount", highProbability);
            
            long lowProbability = relationships.stream()
                    .filter(r -> r.getSuccessProbability() != null && r.getSuccessProbability() < 40.0)
                    .count();
            report.put("lowSuccessProbabilityCount", lowProbability);
            
            // Impact metrics
            double avgImpact = relationships.stream()
                    .filter(r -> r.getImpactScore() != null)
                    .mapToDouble(MentorshipRelationship::getImpactScore)
                    .average().orElse(0.0);
            report.put("averageImpactScore", avgImpact);
        }
        
        return report;
    }

    // =========================================================================
    // FOCUS AREAS AND SPECIALIZATION
    // =========================================================================

    @Override
    public List<MentorshipRelationship> findByFocusArea(Integer teamNumber, Integer season, String focusArea) {
        return relationshipRepository.findByFocusArea(teamNumber, season, focusArea);
    }

    @Override
    public List<MentorshipRelationship> findByPrimaryFocus(Integer teamNumber, Integer season, 
                                                          MentorshipRelationship.MentorshipFocus primaryFocus) {
        return relationshipRepository.findByTeamNumberAndSeasonAndPrimaryFocusAndIsActiveTrue(teamNumber, season, primaryFocus);
    }

    @Override
    public MentorshipRelationship addFocusArea(Long relationshipId, String focusArea) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        if (relationship.getFocusAreas() == null) {
            relationship.setFocusAreas(new ArrayList<>());
        }
        
        if (!relationship.getFocusAreas().contains(focusArea)) {
            relationship.getFocusAreas().add(focusArea);
            relationship.setUpdatedAt(LocalDateTime.now());
            return relationshipRepository.save(relationship);
        }
        
        return relationship;
    }

    @Override
    public MentorshipRelationship removeFocusArea(Long relationshipId, String focusArea) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        if (relationship.getFocusAreas() != null && relationship.getFocusAreas().remove(focusArea)) {
            relationship.setUpdatedAt(LocalDateTime.now());
            return relationshipRepository.save(relationship);
        }
        
        return relationship;
    }

    @Override
    public Map<String, Long> findMostCommonFocusAreas(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.findMostCommonFocusAreas(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public MentorshipRelationship updatePrimaryFocus(Long relationshipId, MentorshipRelationship.MentorshipFocus focus) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setPrimaryFocus(focus);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    // =========================================================================
    // TIME-BASED OPERATIONS
    // =========================================================================

    @Override
    public List<MentorshipRelationship> findStartedInDateRange(Integer teamNumber, Integer season, 
                                                              LocalDate startDate, LocalDate endDate) {
        return relationshipRepository.findStartedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<MentorshipRelationship> findEndingSoon(Integer teamNumber, Integer season, 
                                                      LocalDate startDate, LocalDate endDate) {
        return relationshipRepository.findEndingSoon(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<MentorshipRelationship> findOverdueRelationships(Integer teamNumber, Integer season, LocalDate currentDate) {
        return relationshipRepository.findOverdueRelationships(teamNumber, season, currentDate);
    }

    @Override
    public List<MentorshipRelationship> findLongRunningRelationships(Integer teamNumber, Integer season, Integer minWeeks) {
        return relationshipRepository.findLongRunningRelationships(teamNumber, season, minWeeks);
    }

    @Override
    public MentorshipRelationship updateDuration(Long relationshipId, Integer actualWeeks) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setActualDurationWeeks(actualWeeks);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship extendRelationship(Long relationshipId, LocalDate newEndDate, String reason) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setExpectedEndDate(newEndDate);
        
        // Recalculate expected duration
        if (relationship.getStartDate() != null) {
            long weeks = ChronoUnit.WEEKS.between(relationship.getStartDate(), newEndDate);
            relationship.setExpectedDurationWeeks((int) weeks);
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    // =========================================================================
    // RELATIONSHIP STATUS MANAGEMENT
    // =========================================================================

    @Override
    public MentorshipRelationship updateStatus(Long relationshipId, MentorshipRelationship.MentorshipStatus status) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        MentorshipRelationship.MentorshipStatus oldStatus = relationship.getStatus();
        relationship.setStatus(status);
        
        // Handle status-specific updates
        if (status == MentorshipRelationship.MentorshipStatus.COMPLETED && relationship.getEndDate() == null) {
            relationship.setEndDate(LocalDate.now());
            
            // Calculate actual duration
            if (relationship.getStartDate() != null) {
                long weeks = ChronoUnit.WEEKS.between(relationship.getStartDate(), relationship.getEndDate());
                relationship.setActualDurationWeeks((int) weeks);
            }
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        
        MentorshipRelationship updated = relationshipRepository.save(relationship);
        
        // Update metrics when status changes
        if (!status.equals(oldStatus)) {
            updateOutcomeMetrics(updated.getId());
        }
        
        return updated;
    }

    @Override
    public MentorshipRelationship activateRelationship(Long relationshipId) {
        return updateStatus(relationshipId, MentorshipRelationship.MentorshipStatus.ACTIVE);
    }

    @Override
    public MentorshipRelationship pauseRelationship(Long relationshipId, String reason) {
        MentorshipRelationship relationship = updateStatus(relationshipId, MentorshipRelationship.MentorshipStatus.ON_HOLD);
        
        // Could add pause reason tracking in future enhancement
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public MentorshipRelationship completeRelationship(Long relationshipId, String completionNotes) {
        MentorshipRelationship relationship = updateStatus(relationshipId, MentorshipRelationship.MentorshipStatus.COMPLETED);
        
        // Set completion notes and calculate final metrics
        relationship.setOutcome(completionNotes);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        MentorshipRelationship completed = relationshipRepository.save(relationship);
        updateOutcomeMetrics(completed.getId());
        
        return completed;
    }

    @Override
    public MentorshipRelationship terminateRelationship(Long relationshipId, String reason) {
        MentorshipRelationship relationship = updateStatus(relationshipId, MentorshipRelationship.MentorshipStatus.TERMINATED);
        
        relationship.setEndDate(LocalDate.now());
        relationship.setOutcome("Terminated: " + reason);
        
        // Calculate actual duration
        if (relationship.getStartDate() != null) {
            long weeks = ChronoUnit.WEEKS.between(relationship.getStartDate(), relationship.getEndDate());
            relationship.setActualDurationWeeks((int) weeks);
        }
        
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<MentorshipRelationship> findSuccessfulRelationships(Integer teamNumber, Integer season, 
                                                                   Double minSatisfaction) {
        return relationshipRepository.findSuccessfulRelationships(teamNumber, season, minSatisfaction);
    }

    @Override
    public List<MentorshipRelationship> findRelationshipsNeedingAttention(Integer teamNumber, Integer season, 
                                                                         Double minEngagement, Double minSatisfaction, 
                                                                         Double maxRisk) {
        return relationshipRepository.findRelationshipsNeedingAttention(teamNumber, season, minEngagement, minSatisfaction, maxRisk);
    }

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    @Override
    public Map<MentorshipRelationship.MentorshipStatus, Long> countByStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.countByStatus(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (MentorshipRelationship.MentorshipStatus) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<MentorshipRelationship.RelationshipType, Long> countByType(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.countByRelationshipType(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (MentorshipRelationship.RelationshipType) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<MentorshipRelationship.MentorshipFocus, Long> countByPrimaryFocus(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.countByPrimaryFocus(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (MentorshipRelationship.MentorshipFocus) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Object> calculateRelationshipMetrics(Integer teamNumber, Integer season) {
        List<Object[]> results = relationshipRepository.findRelationshipMetrics(teamNumber, season);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("teamNumber", teamNumber);
        metrics.put("season", season);
        
        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            metrics.put("averageGoalCompletion", result[0]);
            metrics.put("averageAttendanceRate", result[1]);
            metrics.put("averageEngagementScore", result[2]);
            metrics.put("averageSatisfactionScore", result[3]);
            metrics.put("averageSkillDevelopmentScore", result[4]);
            metrics.put("averageRiskScore", result[5]);
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("generatedAt", LocalDateTime.now());
        
        // Basic counts
        Long totalRelationships = countActiveRelationships(teamNumber, season);
        report.put("totalRelationships", totalRelationships);
        
        // Status distribution
        Map<MentorshipRelationship.MentorshipStatus, Long> statusCounts = countByStatus(teamNumber, season);
        report.put("statusDistribution", statusCounts);
        
        // Type distribution
        Map<MentorshipRelationship.RelationshipType, Long> typeCounts = countByType(teamNumber, season);
        report.put("typeDistribution", typeCounts);
        
        // Focus distribution
        Map<MentorshipRelationship.MentorshipFocus, Long> focusCounts = countByPrimaryFocus(teamNumber, season);
        report.put("focusDistribution", focusCounts);
        
        // Metrics summary
        Map<String, Object> metrics = calculateRelationshipMetrics(teamNumber, season);
        report.put("averageMetrics", metrics);
        
        // Risk assessment
        List<MentorshipRelationship> highRisk = findHighRiskRelationships(teamNumber, season, 70.0);
        report.put("highRiskRelationships", highRisk.size());
        
        List<MentorshipRelationship> needingIntervention = findRequiringIntervention(teamNumber, season);
        report.put("relationshipsNeedingIntervention", needingIntervention.size());
        
        // Success metrics
        Map<String, Object> successMetrics = generateSuccessMetricsReport(teamNumber, season);
        report.put("successMetrics", successMetrics);
        
        return report;
    }

    @Override
    public Double calculateTeamMentorshipHealthScore(Integer teamNumber, Integer season) {
        List<MentorshipRelationship> relationships = findActiveRelationships(teamNumber, season);
        
        if (relationships.isEmpty()) {
            return 0.0;
        }
        
        double healthScore = 0.0;
        int factors = 0;
        
        // Average engagement (25%)
        double avgEngagement = relationships.stream()
                .mapToDouble(MentorshipRelationship::getEngagementScore)
                .average().orElse(0.0);
        healthScore += avgEngagement * 0.25;
        factors++;
        
        // Average satisfaction (25%)
        double avgSatisfaction = relationships.stream()
                .mapToDouble(MentorshipRelationship::getOverallSatisfactionScore)
                .average().orElse(0.0);
        healthScore += avgSatisfaction * 0.25;
        factors++;
        
        // Risk factor (20% - inverted)
        double avgRisk = relationships.stream()
                .mapToDouble(MentorshipRelationship::getRiskScore)
                .average().orElse(0.0);
        healthScore += (100.0 - avgRisk) * 0.2;
        factors++;
        
        // Goal completion (20%)
        double avgGoalCompletion = relationships.stream()
                .mapToDouble(MentorshipRelationship::getGoalCompletionPercentage)
                .average().orElse(0.0);
        healthScore += avgGoalCompletion * 0.2;
        factors++;
        
        // Success probability (10%)
        double avgSuccessProbability = relationships.stream()
                .filter(r -> r.getSuccessProbability() != null)
                .mapToDouble(MentorshipRelationship::getSuccessProbability)
                .average().orElse(0.0);
        healthScore += avgSuccessProbability * 0.1;
        factors++;
        
        return factors > 0 ? healthScore : 0.0;
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<MentorshipRelationship> findMentorMenteePairAcrossSeasons(Integer teamNumber, List<Integer> seasons,
                                                                         TeamMember mentor, TeamMember mentee) {
        return relationshipRepository.findMentorMenteePairAcrossSeasons(teamNumber, seasons, mentor, mentee);
    }

    @Override
    public List<MentorshipRelationship> findMultiSeasonRelationships(Integer teamNumber, List<Integer> seasons) {
        return relationshipRepository.findMultiSeasonRelationships(teamNumber, seasons);
    }

    @Override
    public List<Object[]> findRecurringMentorships(Integer teamNumber) {
        return relationshipRepository.findRecurringMentorships(teamNumber);
    }

    @Override
    public Map<String, Object> analyzeMentorshipProgression(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("teamNumber", teamNumber);
        analysis.put("seasons", seasons);
        
        // Find relationships across seasons
        List<MentorshipRelationship> multiSeasonRelationships = findMultiSeasonRelationships(teamNumber, seasons);
        analysis.put("totalMultiSeasonRelationships", multiSeasonRelationships.size());
        
        // Analyze progression by season
        Map<Integer, List<MentorshipRelationship>> relationshipsBySeason = multiSeasonRelationships.stream()
                .collect(Collectors.groupingBy(MentorshipRelationship::getSeason));
        
        Map<Integer, Map<String, Object>> seasonMetrics = new HashMap<>();
        for (Integer season : seasons) {
            List<MentorshipRelationship> seasonRelationships = relationshipsBySeason.get(season);
            if (seasonRelationships != null) {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("relationshipCount", seasonRelationships.size());
                
                double avgSatisfaction = seasonRelationships.stream()
                        .mapToDouble(MentorshipRelationship::getOverallSatisfactionScore)
                        .average().orElse(0.0);
                metrics.put("averageSatisfaction", avgSatisfaction);
                
                seasonMetrics.put(season, metrics);
            }
        }
        analysis.put("seasonMetrics", seasonMetrics);
        
        // Find recurring mentor-mentee pairs
        List<Object[]> recurringPairs = findRecurringMentorships(teamNumber);
        analysis.put("recurringMentorships", recurringPairs.size());
        
        return analysis;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActiveRelationships(Integer teamNumber, Integer season) {
        return relationshipRepository.countActiveRelationships(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> findAllActiveRelationships(Integer teamNumber, Integer season) {
        return relationshipRepository.findAllActiveRelationships(teamNumber, season);
    }

    @Override
    public List<MentorshipRelationship> createBulkRelationships(List<MentorshipRelationship> relationships) {
        List<MentorshipRelationship> created = new ArrayList<>();
        
        for (MentorshipRelationship relationship : relationships) {
            try {
                MentorshipRelationship createdRelationship = createRelationship(relationship);
                created.add(createdRelationship);
            } catch (Exception e) {
                // Log error but continue with other relationships
                System.err.println("Failed to create relationship: " + e.getMessage());
            }
        }
        
        return created;
    }

    @Override
    public List<MentorshipRelationship> updateBulkRelationships(Map<Long, MentorshipRelationship> relationshipUpdates) {
        List<MentorshipRelationship> updated = new ArrayList<>();
        
        for (Map.Entry<Long, MentorshipRelationship> entry : relationshipUpdates.entrySet()) {
            try {
                MentorshipRelationship updatedRelationship = updateRelationship(entry.getKey(), entry.getValue());
                updated.add(updatedRelationship);
            } catch (Exception e) {
                // Log error but continue with other relationships
                System.err.println("Failed to update relationship " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        return updated;
    }

    @Override
    public void bulkArchiveRelationships(List<Long> relationshipIds, String reason) {
        for (Long id : relationshipIds) {
            try {
                archiveRelationship(id, reason);
            } catch (Exception e) {
                // Log error but continue with other relationships
                System.err.println("Failed to archive relationship " + id + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateAllRiskAssessments(Integer teamNumber, Integer season) {
        List<MentorshipRelationship> activeRelationships = findActiveRelationships(teamNumber, season);
        
        for (MentorshipRelationship relationship : activeRelationships) {
            if (relationship.getStatus() == MentorshipRelationship.MentorshipStatus.ACTIVE ||
                relationship.getStatus() == MentorshipRelationship.MentorshipStatus.PROGRESSING) {
                updateRiskAssessment(relationship.getId());
            }
        }
        
        // Update assessment dates in bulk
        relationshipRepository.updateRiskAssessmentDates(teamNumber, season, LocalDateTime.now());
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<MentorshipRelationship> findArchivedRelationships(Integer teamNumber, Integer season) {
        return relationshipRepository.findArchivedRelationships(teamNumber, season);
    }

    @Override
    public MentorshipRelationship restoreArchivedRelationship(Long relationshipId) {
        MentorshipRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found: " + relationshipId));
        
        relationship.setIsArchived(false);
        relationship.setArchiveReason(null);
        relationship.setArchivedAt(null);
        relationship.setUpdatedAt(LocalDateTime.now());
        
        return relationshipRepository.save(relationship);
    }

    @Override
    public void permanentlyDeleteRelationship(Long relationshipId) {
        relationshipRepository.deleteById(relationshipId);
    }

    @Override
    public List<MentorshipRelationship> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                                               LocalDateTime startDate, LocalDateTime endDate) {
        return relationshipRepository.findArchivedInDateRange(teamNumber, season, startDate, endDate);
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validateRelationship(MentorshipRelationship relationship) {
        List<String> errors = new ArrayList<>();
        
        if (relationship.getTeamNumber() == null) {
            errors.add("Team number is required");
        }
        if (relationship.getSeason() == null) {
            errors.add("Season is required");
        }
        if (relationship.getMentor() == null) {
            errors.add("Mentor is required");
        }
        if (relationship.getMentee() == null) {
            errors.add("Mentee is required");
        }
        if (relationship.getRelationshipType() == null) {
            errors.add("Relationship type is required");
        }
        
        // Check for self-mentoring
        if (relationship.getMentor() != null && relationship.getMentee() != null &&
            relationship.getMentor().getId().equals(relationship.getMentee().getId())) {
            errors.add("A team member cannot mentor themselves");
        }
        
        // Check for duplicate relationships
        if (relationship.getMentor() != null && relationship.getMentee() != null) {
            List<MentorshipRelationship> existing = findByMentor(relationship.getMentor(), 
                    relationship.getTeamNumber(), relationship.getSeason());
            
            boolean hasDuplicate = existing.stream()
                    .anyMatch(r -> r.getMentee().getId().equals(relationship.getMentee().getId()) &&
                                 r.getStatus() != MentorshipRelationship.MentorshipStatus.COMPLETED &&
                                 r.getStatus() != MentorshipRelationship.MentorshipStatus.TERMINATED &&
                                 !r.getId().equals(relationship.getId()));
            
            if (hasDuplicate) {
                errors.add("An active relationship already exists between this mentor and mentee");
            }
        }
        
        return errors;
    }

    @Override
    public boolean validateMentorCapacity(TeamMember mentor, Integer teamNumber, Integer season, Integer maxRelationships) {
        Long currentCount = countMentorRelationships(mentor, teamNumber, season);
        return currentCount < maxRelationships;
    }

    @Override
    public boolean validateRelationshipCompatibility(TeamMember mentor, TeamMember mentee) {
        // Basic compatibility checks (can be enhanced with more sophisticated logic)
        
        // Check if they're on the same team (would need access to team data)
        // For now, assume compatible
        return true;
    }

    @Override
    public List<String> checkRelationshipConflicts(MentorshipRelationship relationship) {
        List<String> conflicts = new ArrayList<>();
        
        // Check for schedule conflicts (would need calendar integration)
        // Check for skill mismatch (would need skill profiles)
        // Check for personality conflicts (would need compatibility data)
        
        // For now, return empty list
        return conflicts;
    }

    @Override
    public boolean validateUserPermissions(Long relationshipId, Long userId, String operation) {
        // Implement permission checking based on user roles and relationship involvement
        // For now, assume all operations are permitted
        return true;
    }
}