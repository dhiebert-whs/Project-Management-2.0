// src/main/java/org/frcpm/services/impl/MultiYearPerformanceServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.MultiYearPerformance;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.MultiYearPerformanceRepository;
import org.frcpm.services.MultiYearPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MultiYearPerformanceService.
 * 
 * Provides comprehensive multi-year performance analysis, trend tracking,
 * competitive benchmarking, and improvement recommendations for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.3 Multi-Year Performance Tracking
 */
@Service
@Transactional
public class MultiYearPerformanceServiceImpl implements MultiYearPerformanceService {
    
    @Autowired
    private MultiYearPerformanceRepository performanceRepository;
    
    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================
    
    @Override
    public MultiYearPerformance create(MultiYearPerformance performance) {
        return createPerformanceRecord(performance);
    }
    
    @Override
    public MultiYearPerformance update(Long id, MultiYearPerformance performance) {
        return updatePerformanceRecord(id, performance);
    }
    
    @Override
    public void delete(Long id) {
        deactivatePerformanceRecord(id);
    }
    
    @Override
    public Optional<MultiYearPerformance> findById(Long id) {
        return performanceRepository.findById(id);
    }
    
    @Override
    public List<MultiYearPerformance> findAll() {
        return findActivePerformanceRecords();
    }
    
    @Override
    public boolean existsById(Long id) {
        return performanceRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return performanceRepository.count();
    }
    
    // =========================================================================
    // PERFORMANCE RECORD MANAGEMENT
    // =========================================================================
    
    @Override
    public MultiYearPerformance createPerformanceRecord(MultiYearPerformance performance) {
        validatePerformanceRecord(performance);
        calculateDerivedMetrics(performance);
        return performanceRepository.save(performance);
    }
    
    @Override
    public MultiYearPerformance createPerformanceRecord(Integer teamNumber, Integer season,
                                                       MultiYearPerformance.PerformanceCategory category,
                                                       MultiYearPerformance.MetricType metricType,
                                                       Double metricValue, TeamMember createdBy) {
        
        MultiYearPerformance performance = new MultiYearPerformance(teamNumber, season, category, metricType);
        performance.setMetricValue(metricValue);
        performance.setCreatedBy(createdBy);
        
        return createPerformanceRecord(performance);
    }
    
    @Override
    public MultiYearPerformance updatePerformanceRecord(Long performanceId, MultiYearPerformance performance) {
        MultiYearPerformance existing = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance record not found: " + performanceId));
        
        // Update key fields
        existing.setMetricValue(performance.getMetricValue());
        existing.setPercentileRank(performance.getPercentileRank());
        existing.setRank(performance.getRank());
        existing.setTotalTeams(performance.getTotalTeams());
        existing.setPerformanceSummary(performance.getPerformanceSummary());
        existing.setStrengthsIdentified(performance.getStrengthsIdentified());
        existing.setImprovementAreas(performance.getImprovementAreas());
        existing.setRecommendedActions(performance.getRecommendedActions());
        
        calculateDerivedMetrics(existing);
        return performanceRepository.save(existing);
    }
    
    @Override
    public MultiYearPerformance verifyPerformanceRecord(Long performanceId, TeamMember verifiedBy) {
        MultiYearPerformance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance record not found: " + performanceId));
        
        performance.setVerifiedBy(verifiedBy);
        performance.setVerifiedAt(LocalDateTime.now());
        performance.setLastVerificationDate(LocalDate.now());
        
        return performanceRepository.save(performance);
    }
    
    @Override
    public MultiYearPerformance deactivatePerformanceRecord(Long performanceId) {
        MultiYearPerformance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance record not found: " + performanceId));
        
        performance.setIsActive(false);
        return performanceRepository.save(performance);
    }
    
    // =========================================================================
    // PERFORMANCE DISCOVERY AND RETRIEVAL
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> findActivePerformanceRecords() {
        return performanceRepository.findByIsActiveTrueOrderBySeasonDescTeamNumberAsc();
    }
    
    @Override
    public List<MultiYearPerformance> findByTeamNumber(Integer teamNumber) {
        return performanceRepository.findByTeamNumberAndIsActiveTrueOrderBySeasonDesc(teamNumber);
    }
    
    @Override
    public List<MultiYearPerformance> findBySeason(Integer season) {
        return performanceRepository.findBySeasonAndIsActiveTrueOrderByTeamNumberAsc(season);
    }
    
    @Override
    public List<MultiYearPerformance> findByCategory(MultiYearPerformance.PerformanceCategory category) {
        return performanceRepository.findByCategoryAndIsActiveTrueOrderBySeasonDescTeamNumberAsc(category);
    }
    
    @Override
    public List<MultiYearPerformance> findByMetricType(MultiYearPerformance.MetricType metricType) {
        return performanceRepository.findByMetricTypeAndIsActiveTrueOrderBySeasonDescTeamNumberAsc(metricType);
    }
    
    @Override
    public Optional<MultiYearPerformance> getPerformanceRecord(Integer teamNumber, Integer season,
                                                              MultiYearPerformance.PerformanceCategory category,
                                                              MultiYearPerformance.MetricType metricType) {
        return performanceRepository.findByTeamNumberAndSeasonAndCategoryAndMetricTypeAndIsActiveTrue(
                teamNumber, season, category, metricType);
    }
    
    // =========================================================================
    // TEAM PERFORMANCE ANALYSIS
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> getTeamPerformanceHistory(Integer teamNumber) {
        return performanceRepository.getTeamPerformanceHistory(teamNumber);
    }
    
    @Override
    public List<MultiYearPerformance> getTeamCategoryHistory(Integer teamNumber, 
                                                            MultiYearPerformance.PerformanceCategory category) {
        return performanceRepository.getTeamCategoryHistory(teamNumber, category);
    }
    
    @Override
    public Map<Integer, Map<String, Object>> analyzeTeamBestSeasons(Integer teamNumber) {
        List<Object[]> results = performanceRepository.getTeamBestSeasons(teamNumber);
        Map<Integer, Map<String, Object>> bestSeasons = new HashMap<>();
        
        for (Object[] result : results) {
            Integer season = (Integer) result[0];
            Double avgPercentile = (Double) result[1];
            Long metricCount = (Long) result[2];
            
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("averagePercentile", avgPercentile);
            seasonData.put("metricCount", metricCount);
            seasonData.put("performanceGrade", getGradeFromPercentile(avgPercentile));
            
            bestSeasons.put(season, seasonData);
        }
        
        return bestSeasons;
    }
    
    @Override
    public Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> getTeamPerformanceTrends(Integer teamNumber) {
        List<Object[]> results = performanceRepository.getTeamPerformanceTrends(teamNumber);
        Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> trends = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            Double avgChange = (Double) result[1];
            Double avgTrend = (Double) result[2];
            
            Map<String, Object> trendData = new HashMap<>();
            trendData.put("averageYearOverYearChange", avgChange);
            trendData.put("averageThreeYearTrend", avgTrend);
            trendData.put("trendDirection", determineTrendDirection(avgChange, avgTrend));
            
            trends.put(category, trendData);
        }
        
        return trends;
    }
    
    @Override
    public double calculateTeamOverallScore(Integer teamNumber, Integer season) {
        List<MultiYearPerformance> teamPerformances = performanceRepository.findByTeamNumberAndIsActiveTrueOrderBySeasonDesc(teamNumber)
                .stream()
                .filter(p -> p.getSeason().equals(season))
                .collect(Collectors.toList());
        
        if (teamPerformances.isEmpty()) return 0.0;
        
        double totalWeightedScore = teamPerformances.stream()
                .mapToDouble(MultiYearPerformance::calculateWeightedPerformanceScore)
                .sum();
        
        return totalWeightedScore / teamPerformances.size();
    }
    
    @Override
    public Map<String, Object> generateTeamPerformanceReport(Integer teamNumber, int seasons) {
        Map<String, Object> report = new HashMap<>();
        
        List<MultiYearPerformance> history = getTeamPerformanceHistory(teamNumber).stream()
                .limit(seasons * 10) // Approximate limit based on expected records per season
                .collect(Collectors.toList());
        
        if (history.isEmpty()) {
            report.put("error", "No performance data found for team " + teamNumber);
            return report;
        }
        
        report.put("teamNumber", teamNumber);
        report.put("teamName", history.get(0).getTeamName());
        report.put("reportGenerationDate", LocalDate.now());
        report.put("seasonsAnalyzed", seasons);
        
        // Calculate summary statistics
        double avgPercentile = history.stream()
                .mapToDouble(MultiYearPerformance::getPercentileRank)
                .average().orElse(0.0);
        
        long improvingTrends = history.stream()
                .mapToLong(p -> p.getIsImproving() ? 1 : 0)
                .sum();
        
        long atRiskCount = history.stream()
                .mapToLong(p -> p.getIsAtRisk() ? 1 : 0)
                .sum();
        
        report.put("averagePercentile", avgPercentile);
        report.put("overallGrade", getGradeFromPercentile(avgPercentile));
        report.put("improvingTrends", improvingTrends);
        report.put("atRiskCount", atRiskCount);
        report.put("totalRecords", history.size());
        
        // Get performance by category
        Map<MultiYearPerformance.PerformanceCategory, Double> categoryAverage = history.stream()
                .collect(Collectors.groupingBy(
                    MultiYearPerformance::getCategory,
                    Collectors.averagingDouble(MultiYearPerformance::getPercentileRank)
                ));
        
        report.put("categoryAverages", categoryAverage);
        
        return report;
    }
    
    // =========================================================================
    // TREND ANALYSIS AND PROJECTIONS
    // =========================================================================
    
    @Override
    public Map<String, Object> analyzeMultiSeasonTrends(Integer startSeason, Integer endSeason) {
        List<Object[]> results = performanceRepository.getMultiSeasonTrends(startSeason, endSeason);
        Map<String, Object> analysis = new HashMap<>();
        
        Map<String, List<Map<String, Object>>> trendsData = new HashMap<>();
        
        for (Object[] result : results) {
            Integer season = (Integer) result[0];
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[1];
            Double avgMetricValue = (Double) result[2];
            Double avgPercentile = (Double) result[3];
            
            String categoryKey = category.getDisplayName();
            
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("season", season);
            dataPoint.put("averageMetricValue", avgMetricValue);
            dataPoint.put("averagePercentile", avgPercentile);
            
            trendsData.computeIfAbsent(categoryKey, k -> new ArrayList<>()).add(dataPoint);
        }
        
        analysis.put("trendsData", trendsData);
        analysis.put("analysisTimeframe", startSeason + "-" + endSeason);
        analysis.put("totalSeasons", endSeason - startSeason + 1);
        
        return analysis;
    }
    
    @Override
    public Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> analyzeImprovementPatterns() {
        List<Object[]> results = performanceRepository.getImprovementPatterns();
        Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> patterns = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            Double improvementRate = (Double) result[1];
            Double avgChange = (Double) result[2];
            Long totalRecords = (Long) result[3];
            
            Map<String, Object> patternData = new HashMap<>();
            patternData.put("improvementRate", improvementRate);
            patternData.put("averageChange", avgChange);
            patternData.put("totalRecords", totalRecords);
            patternData.put("categoryTrend", avgChange > 0 ? "IMPROVING" : "DECLINING");
            
            patterns.put(category, patternData);
        }
        
        return patterns;
    }
    
    @Override
    public List<Map<String, Object>> findConsistentTopPerformers(double percentileThreshold, int minSeasons) {
        List<Object[]> results = performanceRepository.findConsistentTopPerformers(percentileThreshold, minSeasons);
        List<Map<String, Object>> performers = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer teamNumber = (Integer) result[0];
            String teamName = (String) result[1];
            Long seasons = (Long) result[2];
            Double avgPercentile = (Double) result[3];
            
            Map<String, Object> performer = new HashMap<>();
            performer.put("teamNumber", teamNumber);
            performer.put("teamName", teamName);
            performer.put("seasonsQualified", seasons);
            performer.put("averagePercentile", avgPercentile);
            performer.put("consistencyScore", calculateConsistencyScore(avgPercentile, seasons));
            
            performers.add(performer);
        }
        
        return performers;
    }
    
    @Override
    public Map<String, Object> projectTeamPerformance(Integer teamNumber, Integer targetSeason) {
        List<MultiYearPerformance> history = getTeamPerformanceHistory(teamNumber);
        Map<String, Object> projection = new HashMap<>();
        
        if (history.size() < 2) {
            projection.put("error", "Insufficient historical data for projection");
            return projection;
        }
        
        // Simple trend-based projection
        Map<MultiYearPerformance.PerformanceCategory, Double> categoryProjections = new HashMap<>();
        
        for (MultiYearPerformance.PerformanceCategory category : MultiYearPerformance.PerformanceCategory.values()) {
            List<MultiYearPerformance> categoryHistory = history.stream()
                    .filter(p -> p.getCategory() == category)
                    .sorted(Comparator.comparing(MultiYearPerformance::getSeason))
                    .collect(Collectors.toList());
            
            if (categoryHistory.size() >= 2) {
                double trend = calculateTrendProjection(categoryHistory);
                categoryProjections.put(category, trend);
            }
        }
        
        projection.put("teamNumber", teamNumber);
        projection.put("targetSeason", targetSeason);
        projection.put("categoryProjections", categoryProjections);
        projection.put("projectionDate", LocalDate.now());
        projection.put("confidence", calculateProjectionConfidence(history.size()));
        
        return projection;
    }
    
    @Override
    public List<MultiYearPerformance> findPerformanceOutliers(double changeThreshold) {
        return performanceRepository.findPerformanceOutliers(changeThreshold);
    }
    
    // =========================================================================
    // COMPETITIVE ANALYSIS AND BENCHMARKING
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> getSeasonRankings(Integer season, MultiYearPerformance.PerformanceCategory category) {
        return performanceRepository.getSeasonRankings(season, category);
    }
    
    @Override
    public List<MultiYearPerformance> getTopPerformers(Integer season, MultiYearPerformance.MetricType metricType, int limit) {
        return performanceRepository.getTopPerformers(season, metricType).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> analyzeCompetitiveLandscape(Integer season) {
        List<Object[]> results = performanceRepository.getCompetitiveLandscape(season);
        Map<String, Object> landscape = new HashMap<>();
        
        Map<String, Map<String, Object>> categoryAnalysis = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            Double avgAdvantage = (Double) result[1];
            Double minAdvantage = (Double) result[2];
            Double maxAdvantage = (Double) result[3];
            Long teamCount = (Long) result[4];
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("averageAdvantage", avgAdvantage);
            analysis.put("minimumAdvantage", minAdvantage);
            analysis.put("maximumAdvantage", maxAdvantage);
            analysis.put("teamCount", teamCount);
            analysis.put("competitiveSpread", maxAdvantage - minAdvantage);
            
            categoryAnalysis.put(category.getDisplayName(), analysis);
        }
        
        landscape.put("season", season);
        landscape.put("categoryAnalysis", categoryAnalysis);
        landscape.put("analysisDate", LocalDate.now());
        
        return landscape;
    }
    
    @Override
    public List<MultiYearPerformance> getMarketLeaders(Integer season, MultiYearPerformance.PerformanceCategory category,
                                                      double advantageThreshold) {
        return performanceRepository.getMarketLeaders(season, category, advantageThreshold);
    }
    
    @Override
    public Map<MultiYearPerformance.MetricType, Map<String, Object>> calculateBenchmarkStandards(Integer season) {
        List<Object[]> results = performanceRepository.calculateBenchmarkStandards(season);
        Map<MultiYearPerformance.MetricType, Map<String, Object>> standards = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.MetricType metricType = 
                    (MultiYearPerformance.MetricType) result[0];
            Double avgValue = (Double) result[1];
            Double minValue = (Double) result[2];
            Double maxValue = (Double) result[3];
            Double avgPercentile = (Double) result[4];
            
            Map<String, Object> benchmark = new HashMap<>();
            benchmark.put("averageValue", avgValue);
            benchmark.put("minimumValue", minValue);
            benchmark.put("maximumValue", maxValue);
            benchmark.put("averagePercentile", avgPercentile);
            benchmark.put("standardRange", maxValue - minValue);
            
            standards.put(metricType, benchmark);
        }
        
        return standards;
    }
    
    @Override
    public Map<String, Object> analyzePeerGroupPerformance(int minTeamSize, int maxTeamSize) {
        List<Object[]> results = performanceRepository.analyzePeerGroupPerformance(minTeamSize, maxTeamSize);
        Map<String, Object> analysis = new HashMap<>();
        
        List<Map<String, Object>> peerData = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer teamNumber = (Integer) result[0];
            String teamName = (String) result[1];
            Double avgPercentile = (Double) result[2];
            
            Map<String, Object> peer = new HashMap<>();
            peer.put("teamNumber", teamNumber);
            peer.put("teamName", teamName);
            peer.put("averagePercentile", avgPercentile);
            
            peerData.add(peer);
        }
        
        analysis.put("teamSizeRange", minTeamSize + "-" + maxTeamSize);
        analysis.put("peerData", peerData);
        analysis.put("totalPeers", peerData.size());
        
        return analysis;
    }
    
    // =========================================================================
    // IMPROVEMENT AND RISK ANALYSIS
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> getMostImprovedTeams(Integer season) {
        return performanceRepository.getMostImprovedTeams(season);
    }
    
    @Override
    public List<MultiYearPerformance> getAtRiskTeams(Integer season, double declineThreshold) {
        return performanceRepository.getAtRiskTeams(season, declineThreshold);
    }
    
    @Override
    public List<Map<String, Object>> generateImprovementRecommendations(Integer teamNumber) {
        List<MultiYearPerformance> history = getTeamPerformanceHistory(teamNumber);
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Analyze each category for improvement opportunities
        for (MultiYearPerformance.PerformanceCategory category : MultiYearPerformance.PerformanceCategory.values()) {
            List<MultiYearPerformance> categoryHistory = history.stream()
                    .filter(p -> p.getCategory() == category)
                    .collect(Collectors.toList());
            
            if (!categoryHistory.isEmpty()) {
                double avgPercentile = categoryHistory.stream()
                        .mapToDouble(MultiYearPerformance::getPercentileRank)
                        .average().orElse(0.0);
                
                if (avgPercentile < 60.0) { // Below average performance
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("category", category.getDisplayName());
                    recommendation.put("currentPerformance", avgPercentile);
                    recommendation.put("priority", avgPercentile < 30.0 ? "HIGH" : "MEDIUM");
                    recommendation.put("recommendation", generateCategoryRecommendation(category, avgPercentile));
                    
                    recommendations.add(recommendation);
                }
            }
        }
        
        // Sort by priority
        recommendations.sort((r1, r2) -> {
            String priority1 = (String) r1.get("priority");
            String priority2 = (String) r2.get("priority");
            return priority2.compareTo(priority1); // HIGH before MEDIUM
        });
        
        return recommendations;
    }
    
    @Override
    public Map<String, Object> analyzeTeamStrengthsWeaknesses(Integer teamNumber, Integer season) {
        List<MultiYearPerformance> seasonPerformances = performanceRepository.findByTeamNumberAndIsActiveTrueOrderBySeasonDesc(teamNumber)
                .stream()
                .filter(p -> p.getSeason().equals(season))
                .collect(Collectors.toList());
        
        Map<String, Object> analysis = new HashMap<>();
        
        List<Map<String, Object>> strengths = new ArrayList<>();
        List<Map<String, Object>> weaknesses = new ArrayList<>();
        
        for (MultiYearPerformance performance : seasonPerformances) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", performance.getCategory().getDisplayName());
            item.put("percentile", performance.getPercentileRank());
            item.put("grade", performance.getPerformanceGrade());
            
            if (performance.getPercentileRank() >= 75.0) {
                strengths.add(item);
            } else if (performance.getPercentileRank() <= 40.0) {
                weaknesses.add(item);
            }
        }
        
        analysis.put("teamNumber", teamNumber);
        analysis.put("season", season);
        analysis.put("strengths", strengths);
        analysis.put("weaknesses", weaknesses);
        analysis.put("analysisDate", LocalDate.now());
        
        return analysis;
    }
    
    @Override
    public Map<String, Object> identifyPerformanceRiskFactors(Integer teamNumber) {
        List<MultiYearPerformance> history = getTeamPerformanceHistory(teamNumber);
        Map<String, Object> riskAnalysis = new HashMap<>();
        
        List<String> riskFactors = new ArrayList<>();
        String riskLevel = "LOW";
        
        // Check for declining trends
        long decliningCategories = history.stream()
                .filter(p -> p.getYearOverYearChange() < -10.0)
                .count();
        
        if (decliningCategories > 2) {
            riskFactors.add("Multiple categories showing significant decline");
            riskLevel = "HIGH";
        }
        
        // Check for at-risk flags
        long atRiskCount = history.stream()
                .filter(MultiYearPerformance::getIsAtRisk)
                .count();
        
        if (atRiskCount > 0) {
            riskFactors.add("Performance indicators flagged as at-risk");
            if (!riskLevel.equals("HIGH")) {
                riskLevel = "MEDIUM";
            }
        }
        
        // Check for low percentile performance
        double avgPercentile = history.stream()
                .mapToDouble(MultiYearPerformance::getPercentileRank)
                .average().orElse(50.0);
        
        if (avgPercentile < 30.0) {
            riskFactors.add("Consistently low percentile performance");
            riskLevel = "HIGH";
        }
        
        riskAnalysis.put("teamNumber", teamNumber);
        riskAnalysis.put("riskLevel", riskLevel);
        riskAnalysis.put("riskFactors", riskFactors);
        riskAnalysis.put("averagePercentile", avgPercentile);
        riskAnalysis.put("decliningCategories", decliningCategories);
        riskAnalysis.put("atRiskCount", atRiskCount);
        
        return riskAnalysis;
    }
    
    // =========================================================================
    // SEASONAL AND STATISTICAL ANALYSIS
    // =========================================================================
    
    @Override
    public Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> getSeasonAverages(Integer season) {
        List<Object[]> results = performanceRepository.getSeasonAverages(season);
        Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> averages = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            Double avgMetricValue = (Double) result[1];
            Double avgPercentile = (Double) result[2];
            Long count = (Long) result[3];
            
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("averageMetricValue", avgMetricValue);
            seasonData.put("averagePercentile", avgPercentile);
            seasonData.put("teamCount", count);
            
            averages.put(category, seasonData);
        }
        
        return averages;
    }
    
    @Override
    public Map<String, Object> getPerformanceDistribution(Integer season) {
        List<Object[]> results = performanceRepository.getPerformanceDistribution(season);
        Map<String, Object> distribution = new HashMap<>();
        
        Map<String, Map<String, Object>> categoryDistribution = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            MultiYearPerformance.MetricType metricType = 
                    (MultiYearPerformance.MetricType) result[1];
            Long recordCount = (Long) result[2];
            Double avgValue = (Double) result[3];
            Double minValue = (Double) result[4];
            Double maxValue = (Double) result[5];
            
            String key = category.getDisplayName() + " - " + metricType.getDisplayName();
            
            Map<String, Object> metricData = new HashMap<>();
            metricData.put("recordCount", recordCount);
            metricData.put("averageValue", avgValue);
            metricData.put("minimumValue", minValue);
            metricData.put("maximumValue", maxValue);
            metricData.put("range", maxValue - minValue);
            
            categoryDistribution.put(key, metricData);
        }
        
        distribution.put("season", season);
        distribution.put("categoryDistribution", categoryDistribution);
        
        return distribution;
    }
    
    @Override
    public Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> calculateGrowthRates() {
        List<Object[]> results = performanceRepository.calculateGrowthRates();
        Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> growthRates = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.PerformanceCategory category = 
                    (MultiYearPerformance.PerformanceCategory) result[0];
            Double avgGrowthRate = (Double) result[1];
            Long improvingCount = (Long) result[2];
            Long atRiskCount = (Long) result[3];
            Long totalCount = (Long) result[4];
            
            Map<String, Object> growthData = new HashMap<>();
            growthData.put("averageGrowthRate", avgGrowthRate);
            growthData.put("improvingTeams", improvingCount);
            growthData.put("atRiskTeams", atRiskCount);
            growthData.put("totalTeams", totalCount);
            growthData.put("improvementPercentage", (double) improvingCount / totalCount * 100.0);
            
            growthRates.put(category, growthData);
        }
        
        return growthRates;
    }
    
    @Override
    public Map<String, Object> analyzeSeasonalPatterns() {
        List<Object[]> results = performanceRepository.getSeasonalPatterns();
        Map<String, Object> patterns = new HashMap<>();
        
        List<Map<String, Object>> seasonalData = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer season = (Integer) result[0];
            Long totalRecords = (Long) result[1];
            Double avgPercentile = (Double) result[2];
            Long topPerformers = (Long) result[3];
            Double avgChange = (Double) result[4];
            
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("season", season);
            seasonData.put("totalRecords", totalRecords);
            seasonData.put("averagePercentile", avgPercentile);
            seasonData.put("topPerformers", topPerformers);
            seasonData.put("averageChange", avgChange);
            seasonData.put("topPerformerPercentage", (double) topPerformers / totalRecords * 100.0);
            
            seasonalData.add(seasonData);
        }
        
        patterns.put("seasonalData", seasonalData);
        patterns.put("analysisDate", LocalDate.now());
        
        return patterns;
    }
    
    @Override
    public Map<String, Object> getRegionalComparisons(MultiYearPerformance.PerformanceCategory category) {
        List<Object[]> results = performanceRepository.getRegionalComparisons(category);
        Map<String, Object> comparisons = new HashMap<>();
        
        List<Map<String, Object>> comparisonData = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer season = (Integer) result[0];
            Double regionalAvg = (Double) result[1];
            Double nationalAvg = (Double) result[2];
            Long recordCount = (Long) result[3];
            
            Map<String, Object> seasonComparison = new HashMap<>();
            seasonComparison.put("season", season);
            seasonComparison.put("regionalAverage", regionalAvg);
            seasonComparison.put("nationalAverage", nationalAvg);
            seasonComparison.put("recordCount", recordCount);
            seasonComparison.put("regionalVsNational", regionalAvg - nationalAvg);
            
            comparisonData.add(seasonComparison);
        }
        
        comparisons.put("category", category.getDisplayName());
        comparisons.put("comparisonData", comparisonData);
        
        return comparisons;
    }
    
    @Override
    public Map<MultiYearPerformance.MetricType, Map<String, Object>> getExcellenceStandards() {
        List<Object[]> results = performanceRepository.getExcellenceStandards();
        Map<MultiYearPerformance.MetricType, Map<String, Object>> standards = new HashMap<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.MetricType metricType = 
                    (MultiYearPerformance.MetricType) result[0];
            Double excellenceStandard = (Double) result[1];
            Double topTierAvg = (Double) result[2];
            Long topTierCount = (Long) result[3];
            
            Map<String, Object> standardData = new HashMap<>();
            standardData.put("excellenceStandard", excellenceStandard);
            standardData.put("topTierAverage", topTierAvg);
            standardData.put("topTierTeamCount", topTierCount);
            
            standards.put(metricType, standardData);
        }
        
        return standards;
    }
    
    // =========================================================================
    // DATA QUALITY AND VALIDATION
    // =========================================================================
    
    @Override
    public Map<String, Object> validateDataQuality(Long performanceId) {
        MultiYearPerformance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance record not found: " + performanceId));
        
        Map<String, Object> validation = new HashMap<>();
        
        List<String> issues = new ArrayList<>();
        String qualityLevel = "HIGH";
        
        if (performance.getDataCompleteness() < 80.0) {
            issues.add("Data completeness below 80%");
            qualityLevel = "MEDIUM";
        }
        
        if (performance.getDataAccuracy() < 85.0) {
            issues.add("Data accuracy below 85%");
            qualityLevel = "MEDIUM";
        }
        
        if (performance.getVerifiedBy() == null) {
            issues.add("Record not verified");
            if (!qualityLevel.equals("MEDIUM")) {
                qualityLevel = "LOW";
            }
        }
        
        if (performance.needsDataRefresh()) {
            issues.add("Data needs refresh (older than 30 days)");
        }
        
        validation.put("qualityLevel", qualityLevel);
        validation.put("issues", issues);
        validation.put("dataCompleteness", performance.getDataCompleteness());
        validation.put("dataAccuracy", performance.getDataAccuracy());
        validation.put("reliability", performance.getDataReliability());
        
        return validation;
    }
    
    @Override
    public List<MultiYearPerformance> findLowQualityData(double completenessThreshold, double accuracyThreshold) {
        return performanceRepository.findLowQualityData(completenessThreshold, accuracyThreshold);
    }
    
    @Override
    public Map<String, Object> getDataQualityStatistics() {
        List<Object[]> results = performanceRepository.getDataQualityStatistics();
        Map<String, Object> statistics = new HashMap<>();
        
        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            Double avgCompleteness = (Double) result[0];
            Double avgAccuracy = (Double) result[1];
            Long highQualityCount = (Long) result[2];
            Long totalCount = (Long) result[3];
            
            statistics.put("averageCompleteness", avgCompleteness);
            statistics.put("averageAccuracy", avgAccuracy);
            statistics.put("highQualityRecords", highQualityCount);
            statistics.put("totalRecords", totalCount);
            statistics.put("highQualityPercentage", (double) highQualityCount / totalCount * 100.0);
        }
        
        return statistics;
    }
    
    @Override
    public List<MultiYearPerformance> findRecordsNeedingVerification(LocalDate verificationCutoff) {
        return performanceRepository.findRecordsNeedingVerification(verificationCutoff);
    }
    
    @Override
    public MultiYearPerformance updateDataQuality(Long performanceId, double completeness, double accuracy) {
        MultiYearPerformance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance record not found: " + performanceId));
        
        performance.setDataCompleteness(completeness);
        performance.setDataAccuracy(accuracy);
        
        return performanceRepository.save(performance);
    }
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> searchByTeamName(String searchTerm) {
        return performanceRepository.searchByTeamName(searchTerm);
    }
    
    @Override
    public List<MultiYearPerformance> advancedSearch(Map<String, Object> criteria) {
        Integer teamNumber = (Integer) criteria.get("teamNumber");
        Integer season = (Integer) criteria.get("season");
        MultiYearPerformance.PerformanceCategory category = 
                (MultiYearPerformance.PerformanceCategory) criteria.get("category");
        MultiYearPerformance.MetricType metricType = 
                (MultiYearPerformance.MetricType) criteria.get("metricType");
        Double minPercentile = (Double) criteria.get("minPercentile");
        Double maxPercentile = (Double) criteria.get("maxPercentile");
        
        return filterPerformanceRecords(teamNumber, season, category, metricType, minPercentile, maxPercentile);
    }
    
    @Override
    public List<MultiYearPerformance> filterPerformanceRecords(Integer teamNumber, Integer season,
                                                              MultiYearPerformance.PerformanceCategory category,
                                                              MultiYearPerformance.MetricType metricType,
                                                              Double minPercentile, Double maxPercentile) {
        return performanceRepository.advancedSearch(teamNumber, season, category, metricType, minPercentile, maxPercentile);
    }
    
    @Override
    public List<Map<String, Object>> findTeamsByPerformanceCriteria(double percentileThreshold, int seasonStart) {
        List<Object[]> results = performanceRepository.findTeamsByPerformanceCriteria(percentileThreshold, seasonStart);
        List<Map<String, Object>> teams = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer teamNumber = (Integer) result[0];
            String teamName = (String) result[1];
            
            Map<String, Object> team = new HashMap<>();
            team.put("teamNumber", teamNumber);
            team.put("teamName", teamName);
            
            teams.add(team);
        }
        
        return teams;
    }
    
    // =========================================================================
    // BULK OPERATIONS AND DATA MANAGEMENT
    // =========================================================================
    
    @Override
    public List<MultiYearPerformance> createBulkPerformanceRecords(List<MultiYearPerformance> performances) {
        List<MultiYearPerformance> created = new ArrayList<>();
        for (MultiYearPerformance performance : performances) {
            created.add(createPerformanceRecord(performance));
        }
        return created;
    }
    
    @Override
    public List<MultiYearPerformance> updateBulkPerformanceRecords(Map<Long, MultiYearPerformance> performanceUpdates) {
        List<MultiYearPerformance> updated = new ArrayList<>();
        for (Map.Entry<Long, MultiYearPerformance> entry : performanceUpdates.entrySet()) {
            updated.add(updatePerformanceRecord(entry.getKey(), entry.getValue()));
        }
        return updated;
    }
    
    @Override
    public int updatePercentileRankings(Integer season, MultiYearPerformance.PerformanceCategory category) {
        List<MultiYearPerformance> seasonRecords = performanceRepository.getSeasonRankings(season, category);
        
        int totalTeams = seasonRecords.size();
        for (int i = 0; i < seasonRecords.size(); i++) {
            MultiYearPerformance performance = seasonRecords.get(i);
            performance.setRank(i + 1);
            performance.setTotalTeams(totalTeams);
            performance.setPercentileRank(((double) (totalTeams - i) / totalTeams) * 100.0);
            performanceRepository.save(performance);
        }
        
        return seasonRecords.size();
    }
    
    @Override
    public int recalculateTrendData(Integer season) {
        List<MultiYearPerformance> seasonRecords = findBySeason(season);
        int updated = 0;
        
        for (MultiYearPerformance performance : seasonRecords) {
            // Get historical data for trend calculation
            List<MultiYearPerformance> history = getTeamCategoryHistory(
                    performance.getTeamNumber(), performance.getCategory());
            
            if (history.size() > 1) {
                List<Double> historicalValues = history.stream()
                        .sorted(Comparator.comparing(MultiYearPerformance::getSeason))
                        .map(MultiYearPerformance::getMetricValue)
                        .collect(Collectors.toList());
                
                performance.updateTrendAnalysis(historicalValues);
                performanceRepository.save(performance);
                updated++;
            }
        }
        
        return updated;
    }
    
    @Override
    public Map<String, Object> importPerformanceData(String sourceType, Integer season) {
        Map<String, Object> importResults = new HashMap<>();
        importResults.put("sourceType", sourceType);
        importResults.put("season", season);
        importResults.put("message", "Performance data import functionality would be implemented here");
        importResults.put("importDate", LocalDateTime.now());
        return importResults;
    }
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    @Override
    public Map<String, Object> getPerformanceAnalyticsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        List<MultiYearPerformance> allRecords = findActivePerformanceRecords();
        
        dashboard.put("totalRecords", allRecords.size());
        dashboard.put("uniqueTeams", getUniqueTeams().size());
        dashboard.put("uniqueSeasons", getUniqueSeasons().size());
        
        double avgPercentile = allRecords.stream()
                .mapToDouble(MultiYearPerformance::getPercentileRank)
                .average().orElse(0.0);
        dashboard.put("averagePercentile", avgPercentile);
        
        long improvingTeams = allRecords.stream()
                .filter(MultiYearPerformance::getIsImproving)
                .count();
        dashboard.put("improvingTeams", improvingTeams);
        
        long atRiskTeams = allRecords.stream()
                .filter(MultiYearPerformance::getIsAtRisk)
                .count();
        dashboard.put("atRiskTeams", atRiskTeams);
        
        return dashboard;
    }
    
    @Override
    public Map<String, Object> generateExecutivePerformanceSummary(Integer season) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("season", season);
        summary.put("generationDate", LocalDate.now());
        summary.putAll(getSeasonAverages(season));
        summary.putAll(analyzeCompetitiveLandscape(season));
        
        return summary;
    }
    
    @Override
    public Map<String, Object> createTrendAnalysisReport(Integer startSeason, Integer endSeason) {
        Map<String, Object> report = new HashMap<>();
        
        report.putAll(analyzeMultiSeasonTrends(startSeason, endSeason));
        report.putAll(analyzeImprovementPatterns());
        report.put("reportDate", LocalDate.now());
        
        return report;
    }
    
    @Override
    public Map<String, Object> generateCompetitiveIntelligenceReport(Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        report.putAll(analyzeCompetitiveLandscape(season));
        report.put("benchmarkStandards", calculateBenchmarkStandards(season));
        report.put("excellenceStandards", getExcellenceStandards());
        
        return report;
    }
    
    @Override
    public Map<String, Object> createTeamDevelopmentRecommendations(Integer teamNumber) {
        Map<String, Object> recommendations = new HashMap<>();
        
        recommendations.put("teamNumber", teamNumber);
        recommendations.put("improvementRecommendations", generateImprovementRecommendations(teamNumber));
        recommendations.put("riskFactors", identifyPerformanceRiskFactors(teamNumber));
        recommendations.put("performanceTrends", getTeamPerformanceTrends(teamNumber));
        
        return recommendations;
    }
    
    // =========================================================================
    // STATISTICAL ANALYSIS AND METRICS
    // =========================================================================
    
    @Override
    public Map<String, Object> calculateMetricCorrelations() {
        List<Object[]> results = performanceRepository.getMetricCorrelations();
        Map<String, Object> correlations = new HashMap<>();
        
        List<Map<String, Object>> correlationData = new ArrayList<>();
        
        for (Object[] result : results) {
            MultiYearPerformance.MetricType metric1 = (MultiYearPerformance.MetricType) result[0];
            MultiYearPerformance.MetricType metric2 = (MultiYearPerformance.MetricType) result[1];
            Long pairCount = (Long) result[2];
            
            Map<String, Object> correlation = new HashMap<>();
            correlation.put("metric1", metric1.getDisplayName());
            correlation.put("metric2", metric2.getDisplayName());
            correlation.put("pairCount", pairCount);
            
            correlationData.add(correlation);
        }
        
        correlations.put("correlationData", correlationData);
        correlations.put("analysisDate", LocalDate.now());
        
        return correlations;
    }
    
    @Override
    public Map<String, Object> performStatisticalSignificanceTest(Integer teamNumber, 
                                                                 MultiYearPerformance.MetricType metricType, 
                                                                 int seasons) {
        Map<String, Object> test = new HashMap<>();
        test.put("message", "Statistical significance testing would be implemented here");
        test.put("teamNumber", teamNumber);
        test.put("metricType", metricType.getDisplayName());
        test.put("seasons", seasons);
        return test;
    }
    
    @Override
    public Map<String, Object> calculateConfidenceIntervals(Integer teamNumber, MultiYearPerformance.MetricType metricType) {
        Map<String, Object> intervals = new HashMap<>();
        intervals.put("message", "Confidence interval calculation would be implemented here");
        intervals.put("teamNumber", teamNumber);
        intervals.put("metricType", metricType.getDisplayName());
        return intervals;
    }
    
    @Override
    public Map<String, Object> analyzePerformanceVolatility(Integer teamNumber) {
        List<MultiYearPerformance> history = getTeamPerformanceHistory(teamNumber);
        Map<String, Object> volatility = new HashMap<>();
        
        if (history.size() < 3) {
            volatility.put("error", "Insufficient data for volatility analysis");
            return volatility;
        }
        
        // Calculate standard deviation of year-over-year changes
        List<Double> changes = history.stream()
                .map(MultiYearPerformance::getYearOverYearChange)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!changes.isEmpty()) {
            double mean = changes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = changes.stream()
                    .mapToDouble(change -> Math.pow(change - mean, 2))
                    .average().orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            volatility.put("volatilityScore", stdDev);
            volatility.put("meanChange", mean);
            volatility.put("consistencyRating", stdDev < 10.0 ? "HIGH" : stdDev < 20.0 ? "MEDIUM" : "LOW");
        }
        
        volatility.put("teamNumber", teamNumber);
        volatility.put("dataPoints", changes.size());
        
        return volatility;
    }
    
    // =========================================================================
    // SYSTEM ADMINISTRATION
    // =========================================================================
    
    @Override
    public List<Map<String, Object>> getUniqueTeams() {
        List<Object[]> results = performanceRepository.getUniqueTeams();
        List<Map<String, Object>> teams = new ArrayList<>();
        
        for (Object[] result : results) {
            Integer teamNumber = (Integer) result[0];
            String teamName = (String) result[1];
            
            Map<String, Object> team = new HashMap<>();
            team.put("teamNumber", teamNumber);
            team.put("teamName", teamName);
            
            teams.add(team);
        }
        
        return teams;
    }
    
    @Override
    public List<Integer> getUniqueSeasons() {
        return performanceRepository.getUniqueSeasons();
    }
    
    @Override
    public Map<String, Object> getDatabaseStatistics() {
        List<Object[]> results = performanceRepository.getDatabaseStatistics();
        Map<String, Object> statistics = new HashMap<>();
        
        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            Long totalRecords = (Long) result[0];
            Long uniqueTeams = (Long) result[1];
            Long uniqueSeasons = (Long) result[2];
            Double avgDataCompleteness = (Double) result[3];
            
            statistics.put("totalRecords", totalRecords);
            statistics.put("uniqueTeams", uniqueTeams);
            statistics.put("uniqueSeasons", uniqueSeasons);
            statistics.put("averageDataCompleteness", avgDataCompleteness);
            statistics.put("recordsPerTeam", (double) totalRecords / uniqueTeams);
            statistics.put("recordsPerSeason", (double) totalRecords / uniqueSeasons);
        }
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> optimizeDatabasePerformance() {
        Map<String, Object> optimization = new HashMap<>();
        optimization.put("message", "Database optimization would be implemented here");
        optimization.put("optimizationDate", LocalDateTime.now());
        return optimization;
    }
    
    @Override
    public Map<String, Object> archiveOldPerformanceData(Integer cutoffSeason) {
        Map<String, Object> archival = new HashMap<>();
        archival.put("cutoffSeason", cutoffSeason);
        archival.put("message", "Data archival would be implemented here");
        archival.put("archivalDate", LocalDateTime.now());
        return archival;
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    private void validatePerformanceRecord(MultiYearPerformance performance) {
        if (performance.getTeamNumber() == null) {
            throw new IllegalArgumentException("Team number is required");
        }
        if (performance.getSeason() == null) {
            throw new IllegalArgumentException("Season is required");
        }
        if (performance.getCategory() == null) {
            throw new IllegalArgumentException("Performance category is required");
        }
        if (performance.getMetricType() == null) {
            throw new IllegalArgumentException("Metric type is required");
        }
        if (performance.getMetricValue() == null) {
            throw new IllegalArgumentException("Metric value is required");
        }
    }
    
    private void calculateDerivedMetrics(MultiYearPerformance performance) {
        // Calculate weighted score
        performance.setWeightedScore(performance.calculateWeightedPerformanceScore());
        
        // Set default data quality if not specified
        if (performance.getDataCompleteness() == 0.0) {
            performance.setDataCompleteness(85.0); // Default completeness
        }
        if (performance.getDataAccuracy() == 0.0) {
            performance.setDataAccuracy(90.0); // Default accuracy
        }
        
        // Set competitive advantage based on percentile
        if (performance.getPercentileRank() > 0) {
            performance.setCompetitiveAdvantage(performance.getPercentileRank() - 50.0);
        }
    }
    
    private String getGradeFromPercentile(double percentile) {
        if (percentile >= 95) return "A+";
        if (percentile >= 90) return "A";
        if (percentile >= 85) return "A-";
        if (percentile >= 80) return "B+";
        if (percentile >= 75) return "B";
        if (percentile >= 70) return "B-";
        if (percentile >= 65) return "C+";
        if (percentile >= 60) return "C";
        if (percentile >= 55) return "C-";
        if (percentile >= 50) return "D+";
        if (percentile >= 45) return "D";
        return "F";
    }
    
    private String determineTrendDirection(double avgChange, double avgTrend) {
        if (avgChange > 5.0 && avgTrend > 3.0) return "STRONG_UPWARD";
        if (avgChange > 0.0 && avgTrend > 0.0) return "UPWARD";
        if (Math.abs(avgChange) <= 2.0 && Math.abs(avgTrend) <= 2.0) return "STABLE";
        if (avgChange < 0.0 && avgTrend < 0.0) return "DOWNWARD";
        if (avgChange < -5.0 && avgTrend < -3.0) return "STRONG_DOWNWARD";
        return "MIXED";
    }
    
    private double calculateConsistencyScore(double avgPercentile, long seasons) {
        // Higher consistency for teams with both high performance and multiple seasons
        return avgPercentile * (1.0 + Math.log10(seasons));
    }
    
    private double calculateTrendProjection(List<MultiYearPerformance> categoryHistory) {
        if (categoryHistory.size() < 2) return 0.0;
        
        // Simple linear trend calculation
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int n = categoryHistory.size();
        
        for (int i = 0; i < n; i++) {
            double x = i; // Season index
            double y = categoryHistory.get(i).getPercentileRank();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        // Calculate slope (trend)
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }
    
    private double calculateProjectionConfidence(int historySize) {
        // More history = higher confidence, max 95%
        return Math.min(95.0, 50.0 + (historySize * 5.0));
    }
    
    private String generateCategoryRecommendation(MultiYearPerformance.PerformanceCategory category, double percentile) {
        return switch (category) {
            case COMPETITION -> percentile < 30.0 ? 
                "Focus on robot reliability and driver practice" : 
                "Enhance strategic game play and alliance selection";
            case TECHNICAL -> percentile < 30.0 ? 
                "Invest in technical training and design review processes" : 
                "Pursue advanced manufacturing techniques and innovation";
            case TEAM_DEVELOPMENT -> percentile < 30.0 ? 
                "Implement structured mentorship and retention programs" : 
                "Expand leadership development and cross-training";
            case OUTREACH -> percentile < 30.0 ? 
                "Develop community outreach strategy and partnerships" : 
                "Scale impact and measure outreach effectiveness";
            case SUSTAINABILITY -> percentile < 30.0 ? 
                "Build diversified funding and succession planning" : 
                "Enhance long-term strategic planning";
            default -> "Focus on fundamental improvements in this area";
        };
    }
}