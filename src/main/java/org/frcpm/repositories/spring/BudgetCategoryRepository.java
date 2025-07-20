// src/main/java/org/frcpm/repositories/spring/BudgetCategoryRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BudgetCategory entities.
 * 
 * Provides comprehensive data access for budget category management
 * including allocation tracking, spending analysis, and financial
 * control for FRC team budget management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
@Repository
public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active budget categories for a specific team and season.
     */
    List<BudgetCategory> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds budget category by name for a team and season.
     */
    Optional<BudgetCategory> findByTeamNumberAndSeasonAndCategoryNameAndIsActiveTrue(
        Integer teamNumber, Integer season, String categoryName);

    /**
     * Finds active budget categories requiring approval.
     */
    List<BudgetCategory> findByTeamNumberAndSeasonAndRequiresApprovalTrueAndIsActiveTrue(
        Integer teamNumber, Integer season);

    /**
     * Finds budget categories by approval threshold range.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.approvalThreshold BETWEEN :minThreshold AND :maxThreshold AND bc.isActive = true")
    List<BudgetCategory> findByApprovalThresholdRange(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("minThreshold") BigDecimal minThreshold,
                                                    @Param("maxThreshold") BigDecimal maxThreshold);

    // =========================================================================
    // ALLOCATION AND SPENDING ANALYSIS
    // =========================================================================

    /**
     * Finds budget categories with allocated amounts in specified range.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount BETWEEN :minAmount AND :maxAmount AND bc.isActive = true")
    List<BudgetCategory> findByAllocatedAmountRange(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("minAmount") BigDecimal minAmount,
                                                  @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Finds over-budget categories.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.spentAmount > bc.allocatedAmount AND bc.isActive = true")
    List<BudgetCategory> findOverBudgetCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with high utilization (>= 80% spent).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND (bc.spentAmount / bc.allocatedAmount) >= 0.8 AND bc.isActive = true")
    List<BudgetCategory> findHighUtilizationCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with low utilization (<= 25% spent).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND (bc.spentAmount / bc.allocatedAmount) <= 0.25 AND bc.isActive = true")
    List<BudgetCategory> findLowUtilizationCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with available budget above threshold.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.availableAmount >= :threshold AND bc.isActive = true ORDER BY bc.availableAmount DESC")
    List<BudgetCategory> findCategoriesWithAvailableBudget(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("threshold") BigDecimal threshold);

    /**
     * Finds categories approaching budget limits (>= 90% utilized).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND (bc.spentAmount / bc.allocatedAmount) >= 0.9 " +
           "AND bc.spentAmount <= bc.allocatedAmount AND bc.isActive = true")
    List<BudgetCategory> findCategoriesApproachingLimits(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // FINANCIAL SUMMARY QUERIES
    // =========================================================================

    /**
     * Calculates total allocated budget for team and season.
     */
    @Query("SELECT SUM(bc.allocatedAmount) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season = :season AND bc.isActive = true")
    Optional<BigDecimal> findTotalAllocatedBudget(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total spent amount for team and season.
     */
    @Query("SELECT SUM(bc.spentAmount) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season = :season AND bc.isActive = true")
    Optional<BigDecimal> findTotalSpentAmount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total available budget for team and season.
     */
    @Query("SELECT SUM(bc.availableAmount) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season = :season AND bc.isActive = true")
    Optional<BigDecimal> findTotalAvailableBudget(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total committed amount for team and season.
     */
    @Query("SELECT SUM(bc.committedAmount) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season = :season AND bc.isActive = true")
    Optional<BigDecimal> findTotalCommittedAmount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds budget utilization statistics by category.
     */
    @Query("SELECT bc.categoryName, bc.allocatedAmount, bc.spentAmount, bc.availableAmount, " +
           "(bc.spentAmount / bc.allocatedAmount * 100) as utilizationPercentage " +
           "FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY utilizationPercentage DESC")
    List<Object[]> findBudgetUtilizationStatistics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // RANKING AND COMPARISON QUERIES
    // =========================================================================

    /**
     * Finds largest budget categories by allocation.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY bc.allocatedAmount DESC")
    List<BudgetCategory> findLargestBudgetCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with highest spending.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY bc.spentAmount DESC")
    List<BudgetCategory> findHighestSpendingCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with most available budget.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY bc.availableAmount DESC")
    List<BudgetCategory> findCategoriesWithMostAvailableBudget(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories by utilization percentage (descending).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND bc.isActive = true " +
           "ORDER BY (bc.spentAmount / bc.allocatedAmount) DESC")
    List<BudgetCategory> findCategoriesByUtilizationDesc(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories by utilization percentage (ascending).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND bc.isActive = true " +
           "ORDER BY (bc.spentAmount / bc.allocatedAmount) ASC")
    List<BudgetCategory> findCategoriesByUtilizationAsc(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // THRESHOLD AND APPROVAL QUERIES
    // =========================================================================

    /**
     * Finds categories with approval thresholds set.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.approvalThreshold IS NOT NULL AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithApprovalThresholds(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories without approval thresholds.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.approvalThreshold IS NULL AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithoutApprovalThresholds(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with low approval thresholds (< 10% of allocated amount).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.approvalThreshold IS NOT NULL AND bc.allocatedAmount > 0 " +
           "AND (bc.approvalThreshold / bc.allocatedAmount) < 0.1 AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithLowApprovalThresholds(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories with high approval thresholds (>= 50% of allocated amount).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.approvalThreshold IS NOT NULL AND bc.allocatedAmount > 0 " +
           "AND (bc.approvalThreshold / bc.allocatedAmount) >= 0.5 AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithHighApprovalThresholds(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // SEARCH AND FILTERING QUERIES
    // =========================================================================

    /**
     * Searches budget categories by name (case-insensitive).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND LOWER(bc.categoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND bc.isActive = true")
    List<BudgetCategory> searchByCategoryName(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("searchTerm") String searchTerm);

    /**
     * Searches budget categories by description (case-insensitive).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND LOWER(bc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND bc.isActive = true")
    List<BudgetCategory> searchByDescription(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("searchTerm") String searchTerm);

    /**
     * Finds budget categories with zero allocation.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount = 0 AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithZeroAllocation(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds budget categories with zero spending.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.spentAmount = 0 AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithZeroSpending(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // VALIDATION AND CONSISTENCY QUERIES
    // =========================================================================

    /**
     * Finds categories with inconsistent amounts (spent > allocated).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.spentAmount > bc.allocatedAmount AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithInconsistentAmounts(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories where available amount doesn't match calculation.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.availableAmount != (bc.allocatedAmount - bc.spentAmount - bc.committedAmount) AND bc.isActive = true")
    List<BudgetCategory> findCategoriesWithIncorrectAvailableAmount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds categories requiring data cleanup (missing required fields).
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND (bc.categoryName IS NULL OR bc.categoryName = '' OR bc.allocatedAmount IS NULL) AND bc.isActive = true")
    List<BudgetCategory> findCategoriesRequiringCleanup(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Validates budget category name uniqueness for team and season.
     */
    @Query("SELECT COUNT(bc) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.categoryName = :categoryName AND bc.isActive = true")
    Long countCategoriesWithSameName(@Param("teamNumber") Integer teamNumber,
                                   @Param("season") Integer season,
                                   @Param("categoryName") String categoryName);

    // =========================================================================
    // MULTI-SEASON AND HISTORICAL QUERIES
    // =========================================================================

    /**
     * Finds budget categories across multiple seasons for comparison.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season IN :seasons AND bc.isActive = true ORDER BY bc.season DESC, bc.categoryName ASC")
    List<BudgetCategory> findMultiSeasonCategories(@Param("teamNumber") Integer teamNumber, @Param("seasons") List<Integer> seasons);

    /**
     * Finds categories that exist in previous season for carryover analysis.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :previousSeason " +
           "AND bc.categoryName IN (SELECT bc2.categoryName FROM BudgetCategory bc2 " +
           "WHERE bc2.teamNumber = :teamNumber AND bc2.season = :currentSeason AND bc2.isActive = true) " +
           "AND bc.isActive = true")
    List<BudgetCategory> findCarryoverCategories(@Param("teamNumber") Integer teamNumber,
                                               @Param("currentSeason") Integer currentSeason,
                                               @Param("previousSeason") Integer previousSeason);

    /**
     * Finds new categories that don't exist in previous season.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :currentSeason " +
           "AND bc.categoryName NOT IN (SELECT bc2.categoryName FROM BudgetCategory bc2 " +
           "WHERE bc2.teamNumber = :teamNumber AND bc2.season = :previousSeason AND bc2.isActive = true) " +
           "AND bc.isActive = true")
    List<BudgetCategory> findNewCategories(@Param("teamNumber") Integer teamNumber,
                                         @Param("currentSeason") Integer currentSeason,
                                         @Param("previousSeason") Integer previousSeason);

    // =========================================================================
    // AGGREGATION AND SUMMARY QUERIES
    // =========================================================================

    /**
     * Calculates budget summary statistics for team and season.
     */
    @Query("SELECT COUNT(bc), SUM(bc.allocatedAmount), SUM(bc.spentAmount), SUM(bc.availableAmount), " +
           "AVG(bc.spentAmount / bc.allocatedAmount * 100) " +
           "FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND bc.isActive = true")
    List<Object[]> findBudgetSummaryStatistics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds distribution of budget categories by utilization ranges.
     */
    @Query("SELECT " +
           "SUM(CASE WHEN bc.spentAmount / bc.allocatedAmount < 0.25 THEN 1 ELSE 0 END) as lowUtilization, " +
           "SUM(CASE WHEN bc.spentAmount / bc.allocatedAmount BETWEEN 0.25 AND 0.74 THEN 1 ELSE 0 END) as mediumUtilization, " +
           "SUM(CASE WHEN bc.spentAmount / bc.allocatedAmount BETWEEN 0.75 AND 0.99 THEN 1 ELSE 0 END) as highUtilization, " +
           "SUM(CASE WHEN bc.spentAmount / bc.allocatedAmount >= 1.0 THEN 1 ELSE 0 END) as overBudget " +
           "FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.allocatedAmount > 0 AND bc.isActive = true")
    List<Object[]> findUtilizationDistribution(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average budget amounts across all active categories.
     */
    @Query("SELECT AVG(bc.allocatedAmount), AVG(bc.spentAmount), AVG(bc.availableAmount) " +
           "FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season AND bc.isActive = true")
    List<Object[]> findAverageBudgetAmounts(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // REPORTING AND EXPORT QUERIES
    // =========================================================================

    /**
     * Finds all budget categories for comprehensive reporting.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY bc.categoryName ASC")
    List<BudgetCategory> findForBudgetReport(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds budget categories for executive summary (key metrics only).
     */
    @Query("SELECT bc.categoryName, bc.allocatedAmount, bc.spentAmount, bc.availableAmount " +
           "FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND bc.isActive = true ORDER BY bc.allocatedAmount DESC")
    List<Object[]> findForExecutiveSummary(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds budget categories requiring management attention.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND (bc.spentAmount > bc.allocatedAmount OR " +
           "(bc.allocatedAmount > 0 AND bc.spentAmount / bc.allocatedAmount >= 0.9)) " +
           "AND bc.isActive = true ORDER BY (bc.spentAmount / bc.allocatedAmount) DESC")
    List<BudgetCategory> findRequiringManagementAttention(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================

    /**
     * Counts total active budget categories for team and season.
     */
    @Query("SELECT COUNT(bc) FROM BudgetCategory bc WHERE bc.teamNumber = :teamNumber " +
           "AND bc.season = :season AND bc.isActive = true")
    Long countActiveBudgetCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Updates available amounts for all categories (maintenance operation).
     */
    @Query("UPDATE BudgetCategory bc SET bc.availableAmount = bc.allocatedAmount - bc.spentAmount - bc.committedAmount " +
           "WHERE bc.teamNumber = :teamNumber AND bc.season = :season AND bc.isActive = true")
    void updateAllAvailableAmounts(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all active budget categories for season-wide operations.
     */
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.season = :season AND bc.isActive = true " +
           "ORDER BY bc.teamNumber ASC, bc.categoryName ASC")
    List<BudgetCategory> findAllActiveForSeason(@Param("season") Integer season);
}