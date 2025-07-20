// src/main/java/org/frcpm/services/impl/TeamReadinessScoreServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.TeamReadinessScore;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.TeamReadinessScoreRepository;
import org.frcpm.services.TeamReadinessScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TeamReadinessScoreService.
 * 
 * Provides comprehensive team readiness assessment services including
 * scoring calculations, trend analysis, benchmarking, risk assessment,
 * and improvement recommendations for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.4 Team Readiness Scoring System
 */
@Service
@Transactional
public class TeamReadinessScoreServiceImpl implements TeamReadinessScoreService {

    @Autowired
    private TeamReadinessScoreRepository readinessRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public TeamReadinessScore create(TeamReadinessScore readinessScore) {
        return createReadinessScore(readinessScore);
    }

    @Override
    public TeamReadinessScore update(Long id, TeamReadinessScore readinessScore) {
        return updateReadinessScore(id, readinessScore);
    }

    @Override
    public void delete(Long id) {
        deactivateReadinessScore(id);
    }

    @Override
    public Optional<TeamReadinessScore> findById(Long id) {
        return readinessRepository.findById(id);
    }

    @Override
    public List<TeamReadinessScore> findAll() {
        return findActiveScores();
    }

    @Override
    public boolean existsById(Long id) {
        return readinessRepository.existsById(id);
    }

    @Override
    public long count() {
        return readinessRepository.count();
    }

    // =========================================================================
    // READINESS SCORE MANAGEMENT
    // =========================================================================

    @Override
    public TeamReadinessScore createReadinessScore(TeamReadinessScore readinessScore) {
        validateReadinessScore(readinessScore);
        
        // Recalculate all scores
        readinessScore.recalculateAllScores();
        
        // Set metadata
        if (readinessScore.getCreatedAt() == null) {
            readinessScore.setCreatedAt(LocalDateTime.now());
        }
        readinessScore.setUpdatedAt(LocalDateTime.now());
        
        return readinessRepository.save(readinessScore);
    }

    @Override
    public TeamReadinessScore createReadinessScore(Integer teamNumber, Integer season,
                                                  TeamReadinessScore.ReadinessType readinessType,
                                                  TeamReadinessScore.AssessmentPhase assessmentPhase,
                                                  TeamMember assessedBy) {
        
        TeamReadinessScore readinessScore = new TeamReadinessScore(teamNumber, season, readinessType, assessmentPhase);
        readinessScore.setAssessedBy(assessedBy);
        
        return createReadinessScore(readinessScore);
    }

    @Override
    public TeamReadinessScore updateReadinessScore(Long scoreId, TeamReadinessScore readinessScore) {
        TeamReadinessScore existing = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));

        // Update fields
        updateReadinessScoreFields(existing, readinessScore);
        
        // Recalculate scores
        existing.recalculateAllScores();
        
        return readinessRepository.save(existing);
    }

    @Override
    public void deactivateReadinessScore(Long scoreId) {
        TeamReadinessScore readinessScore = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        readinessScore.setIsActive(false);
        readinessRepository.save(readinessScore);
    }

    @Override
    public List<TeamReadinessScore> findActiveScores() {
        return readinessRepository.findAll().stream()
                .filter(TeamReadinessScore::getIsActive)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // TEAM AND SEASON QUERIES
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findByTeamAndSeason(Integer teamNumber, Integer season) {
        return readinessRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public Optional<TeamReadinessScore> findLatestScoreByTeamAndSeason(Integer teamNumber, Integer season) {
        return readinessRepository.findLatestByTeamAndSeason(teamNumber, season);
    }

    @Override
    public List<TeamReadinessScore> findByTeamAndReadinessType(Integer teamNumber, 
                                                              TeamReadinessScore.ReadinessType readinessType) {
        return readinessRepository.findByTeamNumberAndReadinessTypeAndIsActiveTrue(teamNumber, readinessType);
    }

    @Override
    public List<TeamReadinessScore> findByAssessmentPhase(TeamReadinessScore.AssessmentPhase assessmentPhase) {
        return readinessRepository.findByAssessmentPhaseAndIsActiveTrue(assessmentPhase);
    }

    @Override
    public List<TeamReadinessScore> findBySeasonAllTeams(Integer season) {
        return readinessRepository.findForSeasonReport(season);
    }

    // =========================================================================
    // READINESS LEVEL AND SCORING
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findByReadinessLevel(TeamReadinessScore.ReadinessLevel readinessLevel, Integer season) {
        return readinessRepository.findByReadinessLevelAndSeasonAndIsActiveTrue(readinessLevel, season);
    }

    @Override
    public List<TeamReadinessScore> findTeamsRequiringImmediateAction(Integer season) {
        return readinessRepository.findTeamsRequiringImmediateAction(season);
    }

    @Override
    public List<TeamReadinessScore> findCompetitionReadyTeams(Integer season) {
        return readinessRepository.findCompetitionReadyTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findTeamsNotCompetitionReady(Integer season) {
        return readinessRepository.findTeamsNotCompetitionReady(season);
    }

    @Override
    public List<TeamReadinessScore> findByScoreRange(Integer season, Double minScore, Double maxScore) {
        return readinessRepository.findByScoreRange(season, minScore, maxScore);
    }

    // =========================================================================
    // CATEGORY-SPECIFIC ANALYSIS
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findHighTechnicalReadinessTeams(Integer season) {
        return readinessRepository.findHighTechnicalReadinessTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findLowResourceReadinessTeams(Integer season) {
        return readinessRepository.findLowResourceReadinessTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findExcellentTeamReadinessTeams(Integer season) {
        return readinessRepository.findExcellentTeamReadinessTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findTeamsNeedingSupport(Integer season) {
        return readinessRepository.findTeamsNeedingSupport(season);
    }

    @Override
    public List<TeamReadinessScore> findStrongestTechnicalTeams(Integer season, Integer limit) {
        return readinessRepository.findStrongestTechnicalTeams(season, limit);
    }

    @Override
    public List<TeamReadinessScore> findStrongestTeamReadinessTeams(Integer season, Integer limit) {
        return readinessRepository.findStrongestTeamReadinessTeams(season, limit);
    }

    // =========================================================================
    // TREND ANALYSIS
    // =========================================================================

    @Override
    public List<TeamReadinessScore> analyzeReadinessTrends(Integer teamNumber, 
                                                          TeamReadinessScore.ReadinessType readinessType) {
        return readinessRepository.findForTrendAnalysis(teamNumber, readinessType);
    }

    @Override
    public List<TeamReadinessScore> findMultiSeasonHistory(Integer teamNumber) {
        return readinessRepository.findMultiSeasonHistory(teamNumber);
    }

    @Override
    public List<TeamReadinessScore> findImprovingTeams(Integer season) {
        return readinessRepository.findImprovingTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findDecliningTeams(Integer season) {
        return readinessRepository.findDecliningTeams(season);
    }

    @Override
    public String calculateReadinessTrend(Integer teamNumber, Integer season) {
        List<TeamReadinessScore> scores = findByTeamAndSeason(teamNumber, season);
        
        if (scores.size() < 2) {
            return "INSUFFICIENT_DATA";
        }
        
        // Sort by assessment date
        scores.sort(Comparator.comparing(TeamReadinessScore::getAssessmentDate));
        
        TeamReadinessScore first = scores.get(0);
        TeamReadinessScore last = scores.get(scores.size() - 1);
        
        double improvement = last.getOverallReadinessScore() - first.getOverallReadinessScore();
        
        if (improvement > 10.0) return "STRONG_IMPROVEMENT";
        if (improvement > 5.0) return "IMPROVING";
        if (improvement > -5.0) return "STABLE";
        if (improvement > -10.0) return "DECLINING";
        return "SIGNIFICANT_DECLINE";
    }

    @Override
    public Double predictFutureReadiness(Integer teamNumber, Integer season, Integer daysAhead) {
        List<TeamReadinessScore> scores = findByTeamAndSeason(teamNumber, season);
        
        if (scores.size() < 2) {
            return null; // Insufficient data for prediction
        }
        
        // Simple linear trend prediction
        scores.sort(Comparator.comparing(TeamReadinessScore::getAssessmentDate));
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = scores.size();
        
        for (int i = 0; i < n; i++) {
            double x = i; // Time index
            double y = scores.get(i).getOverallReadinessScore();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        // Linear regression: y = mx + b
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Predict future value
        double futureX = n + (daysAhead / 7.0); // Convert days to weeks
        double prediction = slope * futureX + intercept;
        
        // Bound prediction to realistic range
        return Math.max(0.0, Math.min(100.0, prediction));
    }

    // =========================================================================
    // BENCHMARKING AND COMPARISON
    // =========================================================================

    @Override
    public Optional<Double> calculateAverageReadinessScore(Integer season, TeamReadinessScore.ReadinessType readinessType) {
        return readinessRepository.findAverageReadinessScore(season, readinessType);
    }

    @Override
    public List<TeamReadinessScore> findAboveAverageTeams(Integer season) {
        return readinessRepository.findAboveAverageTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findBelowAverageTeams(Integer season) {
        return readinessRepository.findBelowAverageTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findTopPerformingTeams(Integer season, Integer limit) {
        return readinessRepository.findTopPerformingTeams(season, limit);
    }

    @Override
    public Double calculatePercentileRanking(Integer teamNumber, Integer season) {
        Optional<TeamReadinessScore> teamScore = findLatestScoreByTeamAndSeason(teamNumber, season);
        
        if (teamScore.isEmpty()) {
            return null;
        }
        
        Double score = teamScore.get().getOverallReadinessScore();
        Long teamsBelow = readinessRepository.findTeamsBelowScore(season, score);
        Long totalTeams = readinessRepository.countActiveAssessments(teamNumber, season);
        
        if (totalTeams == 0) {
            return null;
        }
        
        return (teamsBelow.doubleValue() / totalTeams.doubleValue()) * 100.0;
    }

    @Override
    public Map<String, Object> compareAgainstBenchmarks(Integer teamNumber, Integer season) {
        Map<String, Object> comparison = new HashMap<>();
        
        Optional<TeamReadinessScore> teamScore = findLatestScoreByTeamAndSeason(teamNumber, season);
        if (teamScore.isEmpty()) {
            comparison.put("error", "No readiness score found for team");
            return comparison;
        }
        
        TeamReadinessScore score = teamScore.get();
        
        // Compare against season averages
        Optional<Double> avgScore = calculateAverageReadinessScore(season, score.getReadinessType());
        if (avgScore.isPresent()) {
            comparison.put("seasonAverage", avgScore.get());
            comparison.put("aboveAverage", score.getOverallReadinessScore() > avgScore.get());
            comparison.put("percentageDifference", 
                ((score.getOverallReadinessScore() - avgScore.get()) / avgScore.get()) * 100.0);
        }
        
        // Percentile ranking
        Double percentile = calculatePercentileRanking(teamNumber, season);
        if (percentile != null) {
            comparison.put("percentileRanking", percentile);
        }
        
        // Category comparisons
        comparison.put("categoryComparisons", analyzeCategoryPerformance(score, season));
        
        // Maturity level
        comparison.put("maturityLevel", score.getMaturityLevel());
        
        return comparison;
    }

    // =========================================================================
    // RISK ASSESSMENT
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findHighRiskTeams(Integer season) {
        return readinessRepository.findHighRiskTeams(season);
    }

    @Override
    public List<TeamReadinessScore> findLowMitigationTeams(Integer season) {
        return readinessRepository.findLowMitigationTeams(season);
    }

    @Override
    public Map<String, Object> assessTeamRisk(Integer teamNumber, Integer season) {
        Map<String, Object> riskAssessment = new HashMap<>();
        
        Optional<TeamReadinessScore> scoreOpt = findLatestScoreByTeamAndSeason(teamNumber, season);
        if (scoreOpt.isEmpty()) {
            riskAssessment.put("error", "No readiness score found");
            return riskAssessment;
        }
        
        TeamReadinessScore score = scoreOpt.get();
        
        // Overall risk level
        String riskLevel = determineRiskLevel(score);
        riskAssessment.put("riskLevel", riskLevel);
        riskAssessment.put("riskExposure", score.getRiskExposure());
        riskAssessment.put("mitigationStrength", score.getMitigationStrength());
        
        // Critical risk factors
        List<String> criticalFactors = identifyCriticalRiskFactors(score.getId());
        riskAssessment.put("criticalFactors", criticalFactors);
        
        // Risk-adjusted score
        riskAssessment.put("riskAdjustedScore", score.getRiskAdjustedScore());
        
        // Recommendations
        riskAssessment.put("riskMitigationRecommendations", generateRiskMitigationRecommendations(score));
        
        return riskAssessment;
    }

    @Override
    public Double calculateRiskAdjustedScore(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        return score.getRiskAdjustedScore();
    }

    @Override
    public List<String> identifyCriticalRiskFactors(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        List<String> criticalFactors = new ArrayList<>();
        
        // Check various risk factors
        if (score.getRiskExposure() > 70.0) {
            criticalFactors.add("High overall risk exposure");
        }
        
        if (score.getMitigationStrength() < 40.0) {
            criticalFactors.add("Weak risk mitigation capabilities");
        }
        
        if (score.getContingencyPreparedness() < 50.0) {
            criticalFactors.add("Inadequate contingency planning");
        }
        
        if (score.getTechnicalReadiness() < 50.0) {
            criticalFactors.add("Technical capability deficiencies");
        }
        
        if (score.getResourceReadiness() < 50.0) {
            criticalFactors.add("Resource availability concerns");
        }
        
        if (score.getTeamReadiness() < 50.0) {
            criticalFactors.add("Team coordination and leadership issues");
        }
        
        if (score.getDaysToReadiness() > 0 && score.getDaysToReadiness() < 14) {
            criticalFactors.add("Approaching readiness deadline");
        }
        
        return criticalFactors;
    }

    // =========================================================================
    // IMPROVEMENT AND RECOMMENDATIONS
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findHighImprovementPotentialTeams(Integer season) {
        return readinessRepository.findHighImprovementPotentialTeams(season);
    }

    @Override
    public Map<String, String> generateImprovementRecommendations(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        Map<String, String> recommendations = new HashMap<>();
        
        // Technical improvements
        if (score.getTechnicalReadiness() < 70.0) {
            recommendations.put("Technical", generateTechnicalRecommendations(score));
        }
        
        // Team improvements
        if (score.getTeamReadiness() < 70.0) {
            recommendations.put("Team", generateTeamRecommendations(score));
        }
        
        // Resource improvements
        if (score.getResourceReadiness() < 70.0) {
            recommendations.put("Resource", generateResourceRecommendations(score));
        }
        
        // Process improvements
        if (score.getProcessReadiness() < 70.0) {
            recommendations.put("Process", generateProcessRecommendations(score));
        }
        
        // Strategic improvements
        if (score.getStrategicReadiness() < 70.0) {
            recommendations.put("Strategic", generateStrategicRecommendations(score));
        }
        
        // Competition improvements
        if (score.getCompetitionReadiness() < 70.0) {
            recommendations.put("Competition", generateCompetitionRecommendations(score));
        }
        
        return recommendations;
    }

    @Override
    public Map<String, String> identifyPriorityImprovementAreas(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        return score.getPriorityImprovementAreas();
    }

    @Override
    public Map<String, Object> calculateImprovementEffort(Long scoreId, Double targetScore) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        Map<String, Object> effort = new HashMap<>();
        
        double currentScore = score.getOverallReadinessScore();
        double gap = targetScore - currentScore;
        
        effort.put("currentScore", currentScore);
        effort.put("targetScore", targetScore);
        effort.put("scoreGap", gap);
        
        // Estimate effort based on gap size
        String effortLevel;
        int estimatedDays;
        
        if (gap <= 5.0) {
            effortLevel = "MINIMAL";
            estimatedDays = 7;
        } else if (gap <= 15.0) {
            effortLevel = "MODERATE";
            estimatedDays = 21;
        } else if (gap <= 30.0) {
            effortLevel = "SIGNIFICANT";
            estimatedDays = 42;
        } else {
            effortLevel = "EXTENSIVE";
            estimatedDays = 84;
        }
        
        effort.put("effortLevel", effortLevel);
        effort.put("estimatedDays", estimatedDays);
        effort.put("improvementPotential", score.getImprovementPotential());
        
        return effort;
    }

    @Override
    public List<String> suggestActionPlan(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        List<String> actionPlan = new ArrayList<>();
        
        // Prioritize based on weakest areas
        String weakestArea = score.getWeakestArea();
        
        switch (weakestArea) {
            case "Technical":
                actionPlan.add("Focus on robot design and manufacturing improvements");
                actionPlan.add("Enhance software development and testing processes");
                actionPlan.add("Invest in technical training and skill development");
                break;
            case "Team":
                actionPlan.add("Strengthen leadership development programs");
                actionPlan.add("Improve team communication and collaboration");
                actionPlan.add("Enhance mentorship and skill sharing");
                break;
            case "Resource":
                actionPlan.add("Secure additional funding and sponsorships");
                actionPlan.add("Improve facility and equipment access");
                actionPlan.add("Optimize resource allocation and planning");
                break;
            case "Process":
                actionPlan.add("Implement better project management practices");
                actionPlan.add("Enhance quality control and documentation");
                actionPlan.add("Strengthen safety compliance procedures");
                break;
            case "Strategic":
                actionPlan.add("Develop comprehensive game strategy");
                actionPlan.add("Conduct competitive analysis and benchmarking");
                actionPlan.add("Improve long-term planning and sustainability");
                break;
            case "Competition":
                actionPlan.add("Focus on robot reliability and testing");
                actionPlan.add("Enhance driver training and proficiency");
                actionPlan.add("Improve pit crew efficiency and coordination");
                break;
        }
        
        // Add time-sensitive actions if approaching deadline
        if (score.getDaysToReadiness() > 0 && score.getDaysToReadiness() < 30) {
            actionPlan.add(0, "URGENT: Address critical readiness gaps immediately");
            actionPlan.add(1, "Focus on highest-impact improvements first");
        }
        
        return actionPlan;
    }

    // =========================================================================
    // PERFORMANCE PREDICTION
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findPredictedHighPerformers(Integer season) {
        return readinessRepository.findPredictedHighPerformers(season);
    }

    @Override
    public Double predictCompetitionPerformance(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        return score.getPredictedCompetitionPerformance();
    }

    @Override
    public String calculateMaturityLevel(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        return score.getMaturityLevel();
    }

    @Override
    public List<TeamReadinessScore> findWorldClassTeams(Integer season) {
        return readinessRepository.findWorldClassTeams(season);
    }

    @Override
    public Integer estimateTimeToTargetReadiness(Long scoreId, Double targetScore) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        double currentScore = score.getOverallReadinessScore();
        double gap = targetScore - currentScore;
        
        if (gap <= 0) {
            return 0; // Already at or above target
        }
        
        // Estimate based on improvement potential and current maturity
        double improvementRate = score.getImprovementPotential() / 100.0; // Daily improvement rate
        
        if (improvementRate <= 0) {
            return -1; // Cannot reach target with current trajectory
        }
        
        return (int) Math.ceil(gap / improvementRate);
    }

    // =========================================================================
    // ASSESSMENT MANAGEMENT
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findRecentAssessments(Integer season, Integer withinDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(withinDays);
        return readinessRepository.findRecentAssessments(season, cutoffDate);
    }

    @Override
    public List<TeamReadinessScore> findOverdueAssessments(Integer season, Integer overdueAfterDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(overdueAfterDays);
        return readinessRepository.findOverdueAssessments(season, cutoffDate);
    }

    @Override
    public List<TeamReadinessScore> findTeamsApproachingDeadlines(Integer season, Integer warningDays) {
        return readinessRepository.findTeamsApproachingDeadlines(season, warningDays);
    }

    @Override
    public boolean validateAssessmentCompleteness(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        // Check that all required fields are populated
        return score.getTeamNumber() != null &&
               score.getSeason() != null &&
               score.getReadinessType() != null &&
               score.getAssessmentPhase() != null &&
               score.getOverallReadinessScore() != null &&
               score.getOverallReadinessScore() >= 0.0 &&
               score.getOverallReadinessScore() <= 100.0;
    }

    @Override
    public LocalDate scheduleNextAssessment(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        // Schedule next assessment based on readiness type and current level
        LocalDate nextDate = LocalDate.now();
        
        switch (score.getReadinessType()) {
            case COMPETITION:
                nextDate = nextDate.plusWeeks(1); // Weekly during competition season
                break;
            case BUILD_SEASON:
                nextDate = nextDate.plusWeeks(2); // Bi-weekly during build season
                break;
            case OVERALL:
                nextDate = nextDate.plusMonths(1); // Monthly for overall assessments
                break;
            default:
                nextDate = nextDate.plusWeeks(3); // Default 3 weeks
        }
        
        // Adjust based on readiness level - more frequent for lower readiness
        if (score.getReadinessLevel() == TeamReadinessScore.ReadinessLevel.CRITICAL) {
            nextDate = LocalDate.now().plusDays(3); // Very frequent for critical
        } else if (score.getReadinessLevel() == TeamReadinessScore.ReadinessLevel.LOW) {
            nextDate = LocalDate.now().plusWeeks(1); // Weekly for low readiness
        }
        
        return nextDate;
    }

    // =========================================================================
    // SCORING CALCULATIONS
    // =========================================================================

    @Override
    public void calculateOverallReadinessScore(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        score.calculateOverallReadinessScore();
        readinessRepository.save(score);
    }

    @Override
    public void recalculateAllCategoryScores(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        score.recalculateAllScores();
        readinessRepository.save(score);
    }

    @Override
    public void updateCategoryScore(Long scoreId, String category) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        score.calculateCategoryScore(category);
        readinessRepository.save(score);
    }

    @Override
    public boolean validateScoringConsistency(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        // Validate that all scores are within expected ranges
        return validateScoreRange(score.getOverallReadinessScore()) &&
               validateScoreRange(score.getTechnicalReadiness()) &&
               validateScoreRange(score.getTeamReadiness()) &&
               validateScoreRange(score.getResourceReadiness()) &&
               validateScoreRange(score.getProcessReadiness()) &&
               validateScoreRange(score.getStrategicReadiness()) &&
               validateScoreRange(score.getCompetitionReadiness());
    }

    @Override
    public void normalizeScores(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        // Ensure all scores are within 0-100 range
        score.setOverallReadinessScore(normalizeScore(score.getOverallReadinessScore()));
        score.setTechnicalReadiness(normalizeScore(score.getTechnicalReadiness()));
        score.setTeamReadiness(normalizeScore(score.getTeamReadiness()));
        score.setResourceReadiness(normalizeScore(score.getResourceReadiness()));
        score.setProcessReadiness(normalizeScore(score.getProcessReadiness()));
        score.setStrategicReadiness(normalizeScore(score.getStrategicReadiness()));
        score.setCompetitionReadiness(normalizeScore(score.getCompetitionReadiness()));
        
        readinessRepository.save(score);
    }

    // =========================================================================
    // STATISTICAL ANALYSIS
    // =========================================================================

    @Override
    public Map<TeamReadinessScore.ReadinessLevel, Long> calculateReadinessLevelDistribution(Integer season) {
        List<Object[]> distribution = readinessRepository.findReadinessLevelDistribution(season);
        
        Map<TeamReadinessScore.ReadinessLevel, Long> result = new HashMap<>();
        
        // Initialize all levels with 0
        for (TeamReadinessScore.ReadinessLevel level : TeamReadinessScore.ReadinessLevel.values()) {
            result.put(level, 0L);
        }
        
        // Populate with actual counts
        for (Object[] row : distribution) {
            TeamReadinessScore.ReadinessLevel level = (TeamReadinessScore.ReadinessLevel) row[0];
            Long count = (Long) row[1];
            result.put(level, count);
        }
        
        return result;
    }

    @Override
    public Map<TeamReadinessScore.AssessmentPhase, Long> calculateAssessmentPhaseDistribution(Integer season) {
        List<Object[]> distribution = readinessRepository.findAssessmentPhaseDistribution(season);
        
        Map<TeamReadinessScore.AssessmentPhase, Long> result = new HashMap<>();
        
        // Initialize all phases with 0
        for (TeamReadinessScore.AssessmentPhase phase : TeamReadinessScore.AssessmentPhase.values()) {
            result.put(phase, 0L);
        }
        
        // Populate with actual counts
        for (Object[] row : distribution) {
            TeamReadinessScore.AssessmentPhase phase = (TeamReadinessScore.AssessmentPhase) row[0];
            Long count = (Long) row[1];
            result.put(phase, count);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> calculateSeasonStatistics(Integer season) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<TeamReadinessScore> allScores = findBySeasonAllTeams(season);
        
        if (allScores.isEmpty()) {
            statistics.put("error", "No data available for season " + season);
            return statistics;
        }
        
        // Basic statistics
        OptionalDouble avgScore = allScores.stream()
                .mapToDouble(TeamReadinessScore::getOverallReadinessScore)
                .average();
        
        OptionalDouble maxScore = allScores.stream()
                .mapToDouble(TeamReadinessScore::getOverallReadinessScore)
                .max();
        
        OptionalDouble minScore = allScores.stream()
                .mapToDouble(TeamReadinessScore::getOverallReadinessScore)
                .min();
        
        statistics.put("totalTeams", allScores.size());
        statistics.put("averageScore", avgScore.orElse(0.0));
        statistics.put("maxScore", maxScore.orElse(0.0));
        statistics.put("minScore", minScore.orElse(0.0));
        
        // Distribution analysis
        statistics.put("readinessLevelDistribution", calculateReadinessLevelDistribution(season));
        statistics.put("assessmentPhaseDistribution", calculateAssessmentPhaseDistribution(season));
        
        // Performance categories
        statistics.put("competitionReadyCount", findCompetitionReadyTeams(season).size());
        statistics.put("immediateActionRequiredCount", findTeamsRequiringImmediateAction(season).size());
        statistics.put("highRiskCount", findHighRiskTeams(season).size());
        
        return statistics;
    }

    @Override
    public Map<String, Double> analyzeCategoryCorrelations(Integer season) {
        List<TeamReadinessScore> scores = findBySeasonAllTeams(season);
        
        Map<String, Double> correlations = new HashMap<>();
        
        if (scores.size() < 2) {
            return correlations; // Insufficient data for correlation analysis
        }
        
        // Calculate simple correlations between categories
        correlations.put("technical_team", calculateCorrelation(scores, 
            TeamReadinessScore::getTechnicalReadiness, TeamReadinessScore::getTeamReadiness));
        
        correlations.put("technical_resource", calculateCorrelation(scores,
            TeamReadinessScore::getTechnicalReadiness, TeamReadinessScore::getResourceReadiness));
        
        correlations.put("team_competition", calculateCorrelation(scores,
            TeamReadinessScore::getTeamReadiness, TeamReadinessScore::getCompetitionReadiness));
        
        correlations.put("process_strategic", calculateCorrelation(scores,
            TeamReadinessScore::getProcessReadiness, TeamReadinessScore::getStrategicReadiness));
        
        return correlations;
    }

    @Override
    public Double calculateReadinessVariance(Integer season) {
        List<TeamReadinessScore> scores = findBySeasonAllTeams(season);
        
        OptionalDouble avg = scores.stream()
                .mapToDouble(TeamReadinessScore::getOverallReadinessScore)
                .average();
        
        if (avg.isEmpty()) {
            return null;
        }
        
        double average = avg.getAsDouble();
        
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score.getOverallReadinessScore() - average, 2))
                .average()
                .orElse(0.0);
        
        return variance;
    }

    // =========================================================================
    // APPROVAL AND WORKFLOW
    // =========================================================================

    @Override
    public List<TeamReadinessScore> findByAssessor(TeamMember assessor, Integer season) {
        return readinessRepository.findByAssessedByAndSeasonAndIsActiveTrue(assessor, season);
    }

    @Override
    public List<TeamReadinessScore> findApprovedAssessments(Integer season) {
        return readinessRepository.findApprovedAssessments(season);
    }

    @Override
    public List<TeamReadinessScore> findPendingApprovalAssessments(Integer season) {
        return readinessRepository.findPendingApprovalAssessments(season);
    }

    @Override
    public TeamReadinessScore approveAssessment(Long scoreId, TeamMember approver) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        score.setApprovedBy(approver);
        score.setApprovedAt(LocalDateTime.now());
        
        return readinessRepository.save(score);
    }

    @Override
    public TeamReadinessScore submitForApproval(Long scoreId) {
        TeamReadinessScore score = readinessRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Readiness score not found: " + scoreId));
        
        // Validate completeness before submission
        if (!validateAssessmentCompleteness(scoreId)) {
            throw new RuntimeException("Assessment is incomplete and cannot be submitted for approval");
        }
        
        // Recalculate scores before submission
        score.recalculateAllScores();
        
        return readinessRepository.save(score);
    }

    // =========================================================================
    // REPORTING AND ANALYTICS
    // =========================================================================

    @Override
    public Map<String, Object> generateSeasonReport(Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Basic statistics
        report.putAll(calculateSeasonStatistics(season));
        
        // Team lists
        report.put("topPerformers", findTopPerformingTeams(season, 10));
        report.put("teamsNeedingAction", findTeamsRequiringImmediateAction(season));
        report.put("highRiskTeams", findHighRiskTeams(season));
        report.put("improvingTeams", findImprovingTeams(season));
        
        // Analysis
        report.put("categoryCorrelations", analyzeCategoryCorrelations(season));
        report.put("variance", calculateReadinessVariance(season));
        
        return report;
    }

    @Override
    public Map<String, Object> generateTeamReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        Optional<TeamReadinessScore> latestScore = findLatestScoreByTeamAndSeason(teamNumber, season);
        
        if (latestScore.isEmpty()) {
            report.put("error", "No readiness data found for team " + teamNumber + " in season " + season);
            return report;
        }
        
        TeamReadinessScore score = latestScore.get();
        
        // Basic information
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("latestAssessment", score);
        
        // Analysis
        report.put("readinessSummary", score.generateReadinessSummary());
        report.put("maturityLevel", score.getMaturityLevel());
        report.put("benchmarkComparison", compareAgainstBenchmarks(teamNumber, season));
        report.put("riskAssessment", assessTeamRisk(teamNumber, season));
        report.put("improvementRecommendations", generateImprovementRecommendations(score.getId()));
        report.put("actionPlan", suggestActionPlan(score.getId()));
        
        // Historical data
        List<TeamReadinessScore> history = findByTeamAndSeason(teamNumber, season);
        report.put("assessmentHistory", history);
        report.put("trendAnalysis", calculateReadinessTrend(teamNumber, season));
        
        return report;
    }

    @Override
    public Map<String, Object> generateComparativeAnalysis(Integer season) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Season overview
        analysis.put("seasonStatistics", calculateSeasonStatistics(season));
        
        // Category analysis
        analysis.put("strongestTechnical", findStrongestTechnicalTeams(season, 5));
        analysis.put("strongestTeam", findStrongestTeamReadinessTeams(season, 5));
        analysis.put("highestOverall", findTopPerformingTeams(season, 5));
        
        // Improvement opportunities
        analysis.put("highPotential", findHighImprovementPotentialTeams(season));
        analysis.put("needingSupport", findTeamsNeedingSupport(season));
        
        return analysis;
    }

    @Override
    public List<Map<String, Object>> exportReadinessData(Integer season) {
        List<TeamReadinessScore> scores = findBySeasonAllTeams(season);
        
        return scores.stream().map(score -> {
            Map<String, Object> data = new HashMap<>();
            data.put("teamNumber", score.getTeamNumber());
            data.put("teamName", score.getTeamName());
            data.put("assessmentDate", score.getAssessmentDate());
            data.put("readinessType", score.getReadinessType());
            data.put("assessmentPhase", score.getAssessmentPhase());
            data.put("overallScore", score.getOverallReadinessScore());
            data.put("readinessLevel", score.getReadinessLevel());
            data.put("technicalReadiness", score.getTechnicalReadiness());
            data.put("teamReadiness", score.getTeamReadiness());
            data.put("resourceReadiness", score.getResourceReadiness());
            data.put("processReadiness", score.getProcessReadiness());
            data.put("strategicReadiness", score.getStrategicReadiness());
            data.put("competitionReadiness", score.getCompetitionReadiness());
            data.put("riskExposure", score.getRiskExposure());
            data.put("improvementPotential", score.getImprovementPotential());
            data.put("predictedPerformance", score.getPredictedCompetitionPerformance());
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public String generateExecutiveSummary(Integer season) {
        Map<String, Object> stats = calculateSeasonStatistics(season);
        
        StringBuilder summary = new StringBuilder();
        summary.append("Season ").append(season).append(" Readiness Assessment Summary\n\n");
        
        summary.append("Teams Assessed: ").append(stats.get("totalTeams")).append("\n");
        summary.append("Average Readiness Score: ").append(String.format("%.1f", stats.get("averageScore"))).append("/100\n");
        summary.append("Competition Ready Teams: ").append(stats.get("competitionReadyCount")).append("\n");
        summary.append("Teams Requiring Immediate Action: ").append(stats.get("immediateActionRequiredCount")).append("\n");
        summary.append("High Risk Teams: ").append(stats.get("highRiskCount")).append("\n\n");
        
        // Top performers
        List<TeamReadinessScore> topTeams = findTopPerformingTeams(season, 3);
        summary.append("Top Performing Teams:\n");
        for (TeamReadinessScore team : topTeams) {
            summary.append("- Team ").append(team.getTeamNumber())
                   .append(": ").append(String.format("%.1f", team.getOverallReadinessScore()))
                   .append(" (").append(team.getReadinessLevel().getDisplayName()).append(")\n");
        }
        
        return summary.toString();
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public List<TeamReadinessScore> createBulkReadinessScores(List<TeamReadinessScore> scores) {
        List<String> validationErrors = validateBulkAssessments(scores);
        
        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Validation errors: " + String.join(", ", validationErrors));
        }
        
        return scores.stream()
                .map(this::createReadinessScore)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamReadinessScore> updateBulkReadinessScores(Map<Long, TeamReadinessScore> scoreUpdates) {
        List<TeamReadinessScore> updated = new ArrayList<>();
        
        for (Map.Entry<Long, TeamReadinessScore> entry : scoreUpdates.entrySet()) {
            TeamReadinessScore updatedScore = updateReadinessScore(entry.getKey(), entry.getValue());
            updated.add(updatedScore);
        }
        
        return updated;
    }

    @Override
    public void deactivateOldAssessments(Integer teamNumber, Integer season, LocalDate keepAfterDate) {
        readinessRepository.deactivateOldAssessments(teamNumber, season, keepAfterDate);
    }

    @Override
    public void archiveSeasonAssessments(Integer season) {
        // Implementation would typically move assessments to archive tables
        // For now, just mark them as archived in notes
        List<TeamReadinessScore> seasonScores = findBySeasonAllTeams(season);
        
        for (TeamReadinessScore score : seasonScores) {
            if (score.getReadinessRecommendations() == null) {
                score.setReadinessRecommendations("ARCHIVED: " + LocalDate.now());
            } else {
                score.setReadinessRecommendations("ARCHIVED: " + LocalDate.now() + " | " + score.getReadinessRecommendations());
            }
            readinessRepository.save(score);
        }
    }

    @Override
    public List<String> validateBulkAssessments(List<TeamReadinessScore> scores) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < scores.size(); i++) {
            TeamReadinessScore score = scores.get(i);
            
            if (score.getTeamNumber() == null) {
                errors.add("Row " + (i + 1) + ": Team number is required");
            }
            
            if (score.getSeason() == null) {
                errors.add("Row " + (i + 1) + ": Season is required");
            }
            
            if (score.getReadinessType() == null) {
                errors.add("Row " + (i + 1) + ": Readiness type is required");
            }
            
            if (score.getAssessmentPhase() == null) {
                errors.add("Row " + (i + 1) + ": Assessment phase is required");
            }
            
            // Validate score ranges
            if (!validateScoreRange(score.getOverallReadinessScore())) {
                errors.add("Row " + (i + 1) + ": Overall readiness score must be between 0 and 100");
            }
        }
        
        return errors;
    }

    // =========================================================================
    // INTEGRATION AND SYNC
    // =========================================================================

    @Override
    public void syncWithExternalSystems(Integer season) {
        // Implementation would sync with external assessment systems
        // For now, just validate and update existing assessments
        List<TeamReadinessScore> seasonScores = findBySeasonAllTeams(season);
        
        for (TeamReadinessScore score : seasonScores) {
            // Recalculate scores to ensure consistency
            score.recalculateAllScores();
            readinessRepository.save(score);
        }
    }

    @Override
    public List<TeamReadinessScore> importReadinessData(String dataSource, Integer season) {
        // Implementation would import from external data sources
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    @Override
    public String exportToFormat(Integer season, String format) {
        List<Map<String, Object>> data = exportReadinessData(season);
        
        if ("CSV".equalsIgnoreCase(format)) {
            return convertToCSV(data);
        } else if ("JSON".equalsIgnoreCase(format)) {
            return convertToJSON(data);
        }
        
        return "Unsupported format: " + format;
    }

    @Override
    public boolean validateDataIntegrity(Integer season) {
        List<TeamReadinessScore> scores = findBySeasonAllTeams(season);
        
        for (TeamReadinessScore score : scores) {
            if (!validateScoringConsistency(score.getId())) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<String> reconcileAssessmentDiscrepancies(Integer season) {
        List<String> discrepancies = new ArrayList<>();
        List<TeamReadinessScore> scores = findBySeasonAllTeams(season);
        
        for (TeamReadinessScore score : scores) {
            // Check for scoring inconsistencies
            if (!validateScoringConsistency(score.getId())) {
                discrepancies.add("Team " + score.getTeamNumber() + ": Scoring inconsistency detected");
            }
            
            // Check for missing required data
            if (!validateAssessmentCompleteness(score.getId())) {
                discrepancies.add("Team " + score.getTeamNumber() + ": Incomplete assessment data");
            }
        }
        
        return discrepancies;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private void validateReadinessScore(TeamReadinessScore readinessScore) {
        if (readinessScore.getTeamNumber() == null) {
            throw new RuntimeException("Team number is required");
        }
        
        if (readinessScore.getSeason() == null) {
            throw new RuntimeException("Season is required");
        }
        
        if (readinessScore.getReadinessType() == null) {
            throw new RuntimeException("Readiness type is required");
        }
        
        if (readinessScore.getAssessmentPhase() == null) {
            throw new RuntimeException("Assessment phase is required");
        }
    }

    private void updateReadinessScoreFields(TeamReadinessScore existing, TeamReadinessScore updated) {
        // Update category scores
        if (updated.getTechnicalReadiness() != null) {
            existing.setTechnicalReadiness(updated.getTechnicalReadiness());
        }
        if (updated.getTeamReadiness() != null) {
            existing.setTeamReadiness(updated.getTeamReadiness());
        }
        if (updated.getResourceReadiness() != null) {
            existing.setResourceReadiness(updated.getResourceReadiness());
        }
        if (updated.getProcessReadiness() != null) {
            existing.setProcessReadiness(updated.getProcessReadiness());
        }
        if (updated.getStrategicReadiness() != null) {
            existing.setStrategicReadiness(updated.getStrategicReadiness());
        }
        if (updated.getCompetitionReadiness() != null) {
            existing.setCompetitionReadiness(updated.getCompetitionReadiness());
        }
        
        // Update detailed scores
        if (updated.getRobotDesignScore() != null) {
            existing.setRobotDesignScore(updated.getRobotDesignScore());
        }
        if (updated.getSoftwareReadiness() != null) {
            existing.setSoftwareReadiness(updated.getSoftwareReadiness());
        }
        if (updated.getManufacturingCapability() != null) {
            existing.setManufacturingCapability(updated.getManufacturingCapability());
        }
        
        // Update assessment metadata
        if (updated.getStrengthsAssessment() != null) {
            existing.setStrengthsAssessment(updated.getStrengthsAssessment());
        }
        if (updated.getWeaknessesAssessment() != null) {
            existing.setWeaknessesAssessment(updated.getWeaknessesAssessment());
        }
        if (updated.getReadinessRecommendations() != null) {
            existing.setReadinessRecommendations(updated.getReadinessRecommendations());
        }
    }

    private String determineRiskLevel(TeamReadinessScore score) {
        double riskScore = score.getRiskExposure();
        double mitigationScore = score.getMitigationStrength();
        
        // Calculate overall risk considering both exposure and mitigation
        double overallRisk = riskScore - (mitigationScore * 0.5);
        
        if (overallRisk >= 80.0) return "CRITICAL";
        if (overallRisk >= 65.0) return "HIGH";
        if (overallRisk >= 45.0) return "MEDIUM";
        if (overallRisk >= 25.0) return "LOW";
        return "MINIMAL";
    }

    private List<String> generateRiskMitigationRecommendations(TeamReadinessScore score) {
        List<String> recommendations = new ArrayList<>();
        
        if (score.getRiskExposure() > 70.0) {
            recommendations.add("Implement comprehensive risk management framework");
            recommendations.add("Develop detailed contingency plans for high-risk scenarios");
        }
        
        if (score.getMitigationStrength() < 50.0) {
            recommendations.add("Strengthen risk mitigation capabilities through training");
            recommendations.add("Establish clear escalation procedures for risk events");
        }
        
        if (score.getContingencyPreparedness() < 60.0) {
            recommendations.add("Develop backup plans for critical project components");
            recommendations.add("Create resource buffers for unexpected challenges");
        }
        
        return recommendations;
    }

    private String generateTechnicalRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getRobotDesignScore() < 70.0) {
            recommendations.append("Improve robot design methodology and CAD skills. ");
        }
        if (score.getSoftwareReadiness() < 70.0) {
            recommendations.append("Enhance programming skills and code quality practices. ");
        }
        if (score.getManufacturingCapability() < 70.0) {
            recommendations.append("Strengthen manufacturing processes and equipment training. ");
        }
        if (score.getTestingMaturity() < 70.0) {
            recommendations.append("Implement comprehensive testing protocols and validation. ");
        }
        
        return recommendations.toString();
    }

    private String generateTeamRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getLeadershipStrength() < 70.0) {
            recommendations.append("Develop leadership skills and team coordination. ");
        }
        if (score.getTeamCohesion() < 70.0) {
            recommendations.append("Improve team communication and collaboration practices. ");
        }
        if (score.getMentorshipQuality() < 70.0) {
            recommendations.append("Enhance mentorship programs and knowledge transfer. ");
        }
        
        return recommendations.toString();
    }

    private String generateResourceRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getBudgetPreparedness() < 70.0) {
            recommendations.append("Secure additional funding and improve budget planning. ");
        }
        if (score.getFacilityAdequacy() < 70.0) {
            recommendations.append("Improve workspace and facility access. ");
        }
        if (score.getToolsAndEquipment() < 70.0) {
            recommendations.append("Acquire necessary tools and equipment for development. ");
        }
        
        return recommendations.toString();
    }

    private String generateProcessRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getProjectManagement() < 70.0) {
            recommendations.append("Implement better project management practices and tools. ");
        }
        if (score.getQualityControl() < 70.0) {
            recommendations.append("Establish quality control processes and standards. ");
        }
        if (score.getSafetyCompliance() < 70.0) {
            recommendations.append("Improve safety training and compliance procedures. ");
        }
        
        return recommendations.toString();
    }

    private String generateStrategicRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getGameStrategyDepth() < 70.0) {
            recommendations.append("Develop comprehensive game strategy and analysis. ");
        }
        if (score.getCompetitiveAnalysis() < 70.0) {
            recommendations.append("Conduct thorough competitive analysis and benchmarking. ");
        }
        if (score.getSustainabilityPlanning() < 70.0) {
            recommendations.append("Create long-term sustainability and succession plans. ");
        }
        
        return recommendations.toString();
    }

    private String generateCompetitionRecommendations(TeamReadinessScore score) {
        StringBuilder recommendations = new StringBuilder();
        
        if (score.getRobotReliability() < 70.0) {
            recommendations.append("Focus on robot reliability testing and validation. ");
        }
        if (score.getDriverProficiency() < 70.0) {
            recommendations.append("Enhance driver training and practice sessions. ");
        }
        if (score.getPitCrewEfficiency() < 70.0) {
            recommendations.append("Improve pit crew coordination and efficiency. ");
        }
        
        return recommendations.toString();
    }

    private Map<String, Object> analyzeCategoryPerformance(TeamReadinessScore score, Integer season) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Compare each category against season averages
        List<TeamReadinessScore> allScores = findBySeasonAllTeams(season);
        
        if (!allScores.isEmpty()) {
            OptionalDouble avgTechnical = allScores.stream().mapToDouble(TeamReadinessScore::getTechnicalReadiness).average();
            OptionalDouble avgTeam = allScores.stream().mapToDouble(TeamReadinessScore::getTeamReadiness).average();
            OptionalDouble avgResource = allScores.stream().mapToDouble(TeamReadinessScore::getResourceReadiness).average();
            OptionalDouble avgProcess = allScores.stream().mapToDouble(TeamReadinessScore::getProcessReadiness).average();
            OptionalDouble avgStrategic = allScores.stream().mapToDouble(TeamReadinessScore::getStrategicReadiness).average();
            OptionalDouble avgCompetition = allScores.stream().mapToDouble(TeamReadinessScore::getCompetitionReadiness).average();
            
            analysis.put("technicalVsAverage", score.getTechnicalReadiness() - avgTechnical.orElse(0.0));
            analysis.put("teamVsAverage", score.getTeamReadiness() - avgTeam.orElse(0.0));
            analysis.put("resourceVsAverage", score.getResourceReadiness() - avgResource.orElse(0.0));
            analysis.put("processVsAverage", score.getProcessReadiness() - avgProcess.orElse(0.0));
            analysis.put("strategicVsAverage", score.getStrategicReadiness() - avgStrategic.orElse(0.0));
            analysis.put("competitionVsAverage", score.getCompetitionReadiness() - avgCompetition.orElse(0.0));
        }
        
        return analysis;
    }

    private boolean validateScoreRange(Double score) {
        return score != null && score >= 0.0 && score <= 100.0;
    }

    private Double normalizeScore(Double score) {
        if (score == null) return 0.0;
        return Math.max(0.0, Math.min(100.0, score));
    }

    private double calculateCorrelation(List<TeamReadinessScore> scores,
                                      java.util.function.Function<TeamReadinessScore, Double> xExtractor,
                                      java.util.function.Function<TeamReadinessScore, Double> yExtractor) {
        
        if (scores.size() < 2) return 0.0;
        
        double sumX = scores.stream().mapToDouble(xExtractor::apply).sum();
        double sumY = scores.stream().mapToDouble(yExtractor::apply).sum();
        double sumXY = scores.stream().mapToDouble(score -> xExtractor.apply(score) * yExtractor.apply(score)).sum();
        double sumX2 = scores.stream().mapToDouble(score -> Math.pow(xExtractor.apply(score), 2)).sum();
        double sumY2 = scores.stream().mapToDouble(score -> Math.pow(yExtractor.apply(score), 2)).sum();
        
        int n = scores.size();
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    private String convertToCSV(List<Map<String, Object>> data) {
        if (data.isEmpty()) return "No data available";
        
        StringBuilder csv = new StringBuilder();
        
        // Header
        Set<String> keys = data.get(0).keySet();
        csv.append(String.join(",", keys)).append("\n");
        
        // Data rows
        for (Map<String, Object> row : data) {
            List<String> values = keys.stream()
                    .map(key -> String.valueOf(row.get(key)))
                    .collect(Collectors.toList());
            csv.append(String.join(",", values)).append("\n");
        }
        
        return csv.toString();
    }

    private String convertToJSON(List<Map<String, Object>> data) {
        // Simple JSON conversion - in production, use proper JSON library
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{");
            
            Map<String, Object> row = data.get(i);
            int keyIndex = 0;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (keyIndex > 0) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
                keyIndex++;
            }
            
            json.append("}");
        }
        
        json.append("]");
        return json.toString();
    }
}