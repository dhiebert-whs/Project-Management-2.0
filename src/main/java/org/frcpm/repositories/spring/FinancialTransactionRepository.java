// src/main/java/org/frcpm/repositories/spring/FinancialTransactionRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.FinancialTransaction;
import org.frcpm.models.BudgetCategory;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FinancialTransaction entities.
 * 
 * Provides comprehensive data access for financial transaction management
 * including automated integration, budget tracking, compliance reporting,
 * and financial analytics for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active transactions for a specific team and season.
     */
    List<FinancialTransaction> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds transactions by team, season, and status.
     */
    List<FinancialTransaction> findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, FinancialTransaction.TransactionStatus status);

    /**
     * Finds transactions by type and team.
     */
    List<FinancialTransaction> findByTeamNumberAndTransactionTypeAndIsActiveTrue(
        Integer teamNumber, FinancialTransaction.TransactionType transactionType);

    /**
     * Finds transactions by category and season.
     */
    List<FinancialTransaction> findByCategoryAndSeasonAndIsActiveTrue(
        FinancialTransaction.TransactionCategory category, Integer season);

    // =========================================================================
    // AMOUNT AND DATE RANGE QUERIES
    // =========================================================================

    /**
     * Finds transactions within amount range.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.amount BETWEEN :minAmount AND :maxAmount AND t.isActive = true")
    List<FinancialTransaction> findByAmountRange(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("minAmount") BigDecimal minAmount,
                                               @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Finds transactions within date range.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate AND t.isActive = true")
    List<FinancialTransaction> findByDateRange(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Finds recent transactions within specified days.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionDate >= :cutoffDate AND t.isActive = true ORDER BY t.transactionDate DESC")
    List<FinancialTransaction> findRecentTransactions(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Finds transactions above specified amount threshold.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.amount >= :threshold AND t.isActive = true ORDER BY t.amount DESC")
    List<FinancialTransaction> findLargeTransactions(@Param("teamNumber") Integer teamNumber,
                                                   @Param("season") Integer season,
                                                   @Param("threshold") BigDecimal threshold);

    // =========================================================================
    // BUDGET AND CATEGORY ANALYSIS
    // =========================================================================

    /**
     * Finds transactions by budget category.
     */
    List<FinancialTransaction> findByBudgetCategoryAndIsActiveTrue(BudgetCategory budgetCategory);

    /**
     * Finds over-budget transactions.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.budgetedAmount IS NOT NULL AND t.remainingBudget IS NOT NULL " +
           "AND t.remainingBudget < t.amount AND t.isActive = true")
    List<FinancialTransaction> findOverBudgetTransactions(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season);

    /**
     * Calculates total spending by category for a team and season.
     */
    @Query("SELECT t.category, SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionType IN ('EXPENSE', 'REIMBURSEMENT') AND t.isActive = true " +
           "GROUP BY t.category")
    List<Object[]> findSpendingByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total income by category for a team and season.
     */
    @Query("SELECT t.category, SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionType IN ('INCOME', 'DONATION', 'SPONSORSHIP', 'GRANT') AND t.isActive = true " +
           "GROUP BY t.category")
    List<Object[]> findIncomeByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds budget category utilization rates.
     */
    @Query("SELECT bc, COUNT(t), SUM(t.amount) FROM FinancialTransaction t " +
           "RIGHT JOIN t.budgetCategory bc WHERE bc.teamNumber = :teamNumber AND bc.season = :season " +
           "AND t.isActive = true GROUP BY bc")
    List<Object[]> findBudgetCategoryUtilization(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // APPROVAL AND WORKFLOW QUERIES
    // =========================================================================

    /**
     * Finds transactions pending approval.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.requiresApproval = true AND t.isApproved = false AND t.isActive = true")
    List<FinancialTransaction> findPendingApproval(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds transactions requiring approval above threshold.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.amount >= :approvalThreshold AND t.isApproved = false AND t.isActive = true")
    List<FinancialTransaction> findRequiringApproval(@Param("teamNumber") Integer teamNumber,
                                                   @Param("season") Integer season,
                                                   @Param("approvalThreshold") BigDecimal approvalThreshold);

    /**
     * Finds approved transactions by approver.
     */
    List<FinancialTransaction> findByApprovedByAndSeasonAndIsActiveTrue(TeamMember approvedBy, Integer season);

    /**
     * Finds transactions submitted by specific team member.
     */
    List<FinancialTransaction> findBySubmittedByAndSeasonAndIsActiveTrue(TeamMember submittedBy, Integer season);

    /**
     * Finds transactions with missing required receipts.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.hasReceiptRequired = true AND (t.receiptUrl IS NULL OR t.receiptUrl = '') AND t.isActive = true")
    List<FinancialTransaction> findMissingRequiredReceipts(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // PAYMENT METHOD AND VENDOR ANALYSIS
    // =========================================================================

    /**
     * Finds transactions by payment method.
     */
    List<FinancialTransaction> findByPaymentMethodAndTeamNumberAndSeasonAndIsActiveTrue(
        FinancialTransaction.PaymentMethod paymentMethod, Integer teamNumber, Integer season);

    /**
     * Finds transactions by vendor.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.vendor = :vendor AND t.isActive = true ORDER BY t.transactionDate DESC")
    List<FinancialTransaction> findByVendor(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("vendor") String vendor);

    /**
     * Finds top vendors by spending amount.
     */
    @Query("SELECT t.vendor, SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.vendor IS NOT NULL AND t.transactionType = 'EXPENSE' AND t.isActive = true " +
           "GROUP BY t.vendor ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTopVendorsBySpending(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates spending distribution by payment method.
     */
    @Query("SELECT t.paymentMethod, COUNT(t), SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionType = 'EXPENSE' AND t.isActive = true " +
           "GROUP BY t.paymentMethod")
    List<Object[]> findSpendingByPaymentMethod(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // INTEGRATION AND AUTOMATION QUERIES
    // =========================================================================

    /**
     * Finds automated transactions from external systems.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isAutomated = true AND t.isActive = true ORDER BY t.createdAt DESC")
    List<FinancialTransaction> findAutomatedTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds transactions by integration source.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.integrationSource = :source AND t.isActive = true")
    List<FinancialTransaction> findByIntegrationSource(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("source") String source);

    /**
     * Finds unreconciled transactions.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isReconciled = false AND t.status = 'PROCESSED' AND t.isActive = true")
    List<FinancialTransaction> findUnreconciledTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds transactions by external transaction ID.
     */
    Optional<FinancialTransaction> findByExternalTransactionIdAndIsActiveTrue(String externalTransactionId);

    /**
     * Finds duplicate external transactions.
     */
    @Query("SELECT t.externalTransactionId, COUNT(t) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.externalTransactionId IS NOT NULL AND t.isActive = true " +
           "GROUP BY t.externalTransactionId HAVING COUNT(t) > 1")
    List<Object[]> findDuplicateExternalTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // RECURRING TRANSACTION QUERIES
    // =========================================================================

    /**
     * Finds active recurring transactions.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isRecurring = true AND t.nextRecurrenceDate IS NOT NULL " +
           "AND (t.recurrenceEndDate IS NULL OR t.recurrenceEndDate >= CURRENT_DATE) AND t.isActive = true")
    List<FinancialTransaction> findActiveRecurringTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds recurring transactions due for processing.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isRecurring = true AND t.nextRecurrenceDate <= :dueDate AND t.isActive = true")
    List<FinancialTransaction> findRecurringTransactionsDue(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("dueDate") LocalDate dueDate);

    /**
     * Finds child transactions of a parent recurring transaction.
     */
    List<FinancialTransaction> findByParentTransactionAndIsActiveTrue(FinancialTransaction parentTransaction);

    /**
     * Finds transactions by recurrence pattern.
     */
    List<FinancialTransaction> findByRecurrencePatternAndTeamNumberAndSeasonAndIsActiveTrue(
        FinancialTransaction.RecurrencePattern recurrencePattern, Integer teamNumber, Integer season);

    // =========================================================================
    // TAX AND COMPLIANCE QUERIES
    // =========================================================================

    /**
     * Finds tax-deductible transactions.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isTaxDeductible = true AND t.isActive = true ORDER BY t.transactionDate")
    List<FinancialTransaction> findTaxDeductibleTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds reimbursable transactions.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.isReimbursable = true AND t.isActive = true ORDER BY t.transactionDate")
    List<FinancialTransaction> findReimbursableTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total tax amounts for a period.
     */
    @Query("SELECT SUM(t.taxAmount) FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.taxAmount IS NOT NULL AND t.isActive = true")
    Optional<BigDecimal> findTotalTaxAmount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds transactions by tax category.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.taxCategory = :taxCategory AND t.isActive = true")
    List<FinancialTransaction> findByTaxCategory(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("taxCategory") String taxCategory);

    // =========================================================================
    // PROJECT AND TASK ASSOCIATION QUERIES
    // =========================================================================

    /**
     * Finds transactions associated with a project.
     */
    List<FinancialTransaction> findByProjectAndIsActiveTrue(Project project);

    /**
     * Finds transactions without project association.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.project IS NULL AND t.isActive = true")
    List<FinancialTransaction> findUnassignedTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates project spending totals.
     */
    @Query("SELECT t.project, SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.project IS NOT NULL AND t.transactionType = 'EXPENSE' AND t.isActive = true " +
           "GROUP BY t.project")
    List<Object[]> findProjectSpendingTotals(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds top spending projects.
     */
    @Query("SELECT t.project, SUM(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.project IS NOT NULL AND t.transactionType = 'EXPENSE' AND t.isActive = true " +
           "GROUP BY t.project ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTopSpendingProjects(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // FINANCIAL SUMMARY AND ANALYTICS
    // =========================================================================

    /**
     * Calculates total income for team and season.
     */
    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.transactionType IN ('INCOME', 'DONATION', 'SPONSORSHIP', 'GRANT') " +
           "AND t.isActive = true")
    Optional<BigDecimal> findTotalIncome(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total expenses for team and season.
     */
    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.transactionType IN ('EXPENSE', 'REIMBURSEMENT') " +
           "AND t.isActive = true")
    Optional<BigDecimal> findTotalExpenses(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates net cash flow (income - expenses).
     */
    @Query("SELECT " +
           "(SELECT COALESCE(SUM(i.amount), 0) FROM FinancialTransaction i WHERE i.teamNumber = :teamNumber " +
           "AND i.season = :season AND i.transactionType IN ('INCOME', 'DONATION', 'SPONSORSHIP', 'GRANT') AND i.isActive = true) - " +
           "(SELECT COALESCE(SUM(e.amount), 0) FROM FinancialTransaction e WHERE e.teamNumber = :teamNumber " +
           "AND e.season = :season AND e.transactionType IN ('EXPENSE', 'REIMBURSEMENT') AND e.isActive = true)")
    Optional<BigDecimal> findNetCashFlow(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds monthly spending trends.
     */
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) " +
           "FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.transactionType = 'EXPENSE' AND t.isActive = true " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlySpendingTrends(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds average transaction amounts by category.
     */
    @Query("SELECT t.category, AVG(t.amount) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season AND t.isActive = true " +
           "GROUP BY t.category")
    List<Object[]> findAverageAmountsByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // PERFORMANCE AND EFFICIENCY QUERIES
    // =========================================================================

    /**
     * Finds transactions with fastest approval times.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.submittedAt IS NOT NULL AND t.approvedAt IS NOT NULL " +
           "AND t.isActive = true ORDER BY (t.approvedAt - t.submittedAt) ASC")
    List<FinancialTransaction> findFastestApprovals(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds transactions with longest approval times.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.submittedAt IS NOT NULL AND t.approvedAt IS NOT NULL " +
           "AND t.isActive = true ORDER BY (t.approvedAt - t.submittedAt) DESC")
    List<FinancialTransaction> findSlowestApprovals(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most active team members by transaction volume.
     */
    @Query("SELECT t.submittedBy, COUNT(t) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.submittedBy IS NOT NULL AND t.isActive = true " +
           "GROUP BY t.submittedBy ORDER BY COUNT(t) DESC")
    List<Object[]> findMostActiveSubmitters(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates transaction processing efficiency metrics.
     */
    @Query("SELECT COUNT(t), " +
           "SUM(CASE WHEN t.status = 'PROCESSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN t.status = 'REJECTED' THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN t.submittedAt IS NOT NULL AND t.approvedAt IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, t.submittedAt, t.approvedAt) ELSE NULL END) " +
           "FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season AND t.isActive = true")
    List<Object[]> findProcessingEfficiencyMetrics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // AUDIT AND COMPLIANCE QUERIES
    // =========================================================================

    /**
     * Finds transactions modified within date range.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.updatedAt BETWEEN :startDate AND :endDate AND t.isActive = true")
    List<FinancialTransaction> findModifiedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Finds transactions requiring audit attention.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND (t.amount >= :auditThreshold OR t.hasReceiptRequired = true AND t.receiptUrl IS NULL " +
           "OR t.isAutomated = false AND t.documentationUrl IS NULL) AND t.isActive = true")
    List<FinancialTransaction> findRequiringAuditAttention(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("auditThreshold") BigDecimal auditThreshold);

    /**
     * Finds all transactions for comprehensive audit trail.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "ORDER BY t.transactionDate ASC, t.createdAt ASC")
    List<FinancialTransaction> findForAuditTrail(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches transactions by description or vendor name.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND (LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.vendor) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND t.isActive = true")
    List<FinancialTransaction> searchTransactions(@Param("teamNumber") Integer teamNumber,
                                                @Param("season") Integer season,
                                                @Param("searchTerm") String searchTerm);

    /**
     * Finds transactions by invoice or PO number.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND (t.invoiceNumber = :number OR t.purchaseOrderNumber = :number) AND t.isActive = true")
    List<FinancialTransaction> findByInvoiceOrPoNumber(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("number") String number);

    /**
     * Finds transactions by GL account.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND t.glAccount = :glAccount AND t.isActive = true")
    List<FinancialTransaction> findByGlAccount(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("glAccount") String glAccount);

    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================

    /**
     * Counts transactions by status for a team and season.
     */
    @Query("SELECT t.status, COUNT(t) FROM FinancialTransaction t " +
           "WHERE t.teamNumber = :teamNumber AND t.season = :season AND t.isActive = true " +
           "GROUP BY t.status")
    List<Object[]> findTransactionCountsByStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all active transactions for season reporting.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.season = :season AND t.isActive = true " +
           "ORDER BY t.teamNumber ASC, t.transactionDate ASC")
    List<FinancialTransaction> findAllActiveForSeason(@Param("season") Integer season);

    /**
     * Finds transactions requiring data cleanup.
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber AND t.season = :season " +
           "AND (t.vendor IS NULL OR t.category IS NULL OR t.paymentMethod IS NULL) AND t.isActive = true")
    List<FinancialTransaction> findRequiringDataCleanup(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts active transactions for a team and season.
     */
    @Query("SELECT COUNT(t) FROM FinancialTransaction t WHERE t.teamNumber = :teamNumber " +
           "AND t.season = :season AND t.isActive = true")
    Long countActiveTransactions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);
}