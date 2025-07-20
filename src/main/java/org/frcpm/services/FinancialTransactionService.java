// src/main/java/org/frcpm/services/FinancialTransactionService.java

package org.frcpm.services;

import org.frcpm.models.FinancialTransaction;
import org.frcpm.models.BudgetCategory;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for FinancialTransaction operations.
 * 
 * Provides comprehensive financial transaction management services including
 * automated integration, budget tracking, compliance reporting, approval workflows,
 * and financial analytics for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
public interface FinancialTransactionService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new financial transaction.
     */
    FinancialTransaction create(FinancialTransaction transaction);

    /**
     * Updates an existing financial transaction.
     */
    FinancialTransaction update(Long id, FinancialTransaction transaction);

    /**
     * Deletes a financial transaction (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a financial transaction by ID.
     */
    Optional<FinancialTransaction> findById(Long id);

    /**
     * Finds all active financial transactions.
     */
    List<FinancialTransaction> findAll();

    /**
     * Checks if financial transaction exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of financial transactions.
     */
    long count();

    // =========================================================================
    // TRANSACTION MANAGEMENT
    // =========================================================================

    /**
     * Creates a new financial transaction with validation.
     */
    FinancialTransaction createTransaction(FinancialTransaction transaction);

    /**
     * Creates a transaction with basic parameters.
     */
    FinancialTransaction createTransaction(Integer teamNumber, Integer season, BigDecimal amount,
                                         FinancialTransaction.TransactionType transactionType,
                                         FinancialTransaction.TransactionCategory category,
                                         String description, TeamMember submittedBy);

    /**
     * Updates an existing transaction with validation.
     */
    FinancialTransaction updateTransaction(Long transactionId, FinancialTransaction transaction);

    /**
     * Deactivates a financial transaction.
     */
    void deactivateTransaction(Long transactionId);

    /**
     * Finds all active transactions for a team and season.
     */
    List<FinancialTransaction> findActiveTransactions(Integer teamNumber, Integer season);

    // =========================================================================
    // QUERY OPERATIONS
    // =========================================================================

    /**
     * Finds transactions by team and season.
     */
    List<FinancialTransaction> findByTeamAndSeason(Integer teamNumber, Integer season);

    /**
     * Finds transactions by status.
     */
    List<FinancialTransaction> findByStatus(Integer teamNumber, Integer season, 
                                          FinancialTransaction.TransactionStatus status);

    /**
     * Finds transactions by type.
     */
    List<FinancialTransaction> findByType(Integer teamNumber, FinancialTransaction.TransactionType type);

    /**
     * Finds transactions by category.
     */
    List<FinancialTransaction> findByCategory(FinancialTransaction.TransactionCategory category, Integer season);

    /**
     * Finds transactions within amount range.
     */
    List<FinancialTransaction> findByAmountRange(Integer teamNumber, Integer season, 
                                               BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Finds transactions within date range.
     */
    List<FinancialTransaction> findByDateRange(Integer teamNumber, Integer season, 
                                             LocalDate startDate, LocalDate endDate);

    // =========================================================================
    // BUDGET AND CATEGORY OPERATIONS
    // =========================================================================

    /**
     * Finds transactions by budget category.
     */
    List<FinancialTransaction> findByBudgetCategory(BudgetCategory budgetCategory);

    /**
     * Finds over-budget transactions.
     */
    List<FinancialTransaction> findOverBudgetTransactions(Integer teamNumber, Integer season);

    /**
     * Calculates spending by category.
     */
    Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateSpendingByCategory(Integer teamNumber, Integer season);

    /**
     * Calculates income by category.
     */
    Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateIncomeByCategory(Integer teamNumber, Integer season);

    /**
     * Analyzes budget category utilization.
     */
    Map<String, Object> analyzeBudgetCategoryUtilization(Integer teamNumber, Integer season);

    /**
     * Updates budget category spending totals.
     */
    void updateBudgetCategoryTotals(BudgetCategory budgetCategory);

    // =========================================================================
    // APPROVAL WORKFLOW
    // =========================================================================

    /**
     * Finds transactions pending approval.
     */
    List<FinancialTransaction> findPendingApproval(Integer teamNumber, Integer season);

    /**
     * Finds transactions requiring approval above threshold.
     */
    List<FinancialTransaction> findRequiringApproval(Integer teamNumber, Integer season, BigDecimal threshold);

    /**
     * Approves a financial transaction.
     */
    FinancialTransaction approveTransaction(Long transactionId, TeamMember approver, String notes);

    /**
     * Rejects a financial transaction.
     */
    FinancialTransaction rejectTransaction(Long transactionId, String reason);

    /**
     * Submits transaction for approval.
     */
    FinancialTransaction submitForApproval(Long transactionId);

    /**
     * Finds transactions by approver.
     */
    List<FinancialTransaction> findByApprover(TeamMember approver, Integer season);

    /**
     * Finds transactions by submitter.
     */
    List<FinancialTransaction> findBySubmitter(TeamMember submitter, Integer season);

    // =========================================================================
    // RECEIPT AND DOCUMENTATION
    // =========================================================================

    /**
     * Finds transactions with missing required receipts.
     */
    List<FinancialTransaction> findMissingRequiredReceipts(Integer teamNumber, Integer season);

    /**
     * Adds receipt to transaction.
     */
    FinancialTransaction addReceipt(Long transactionId, String receiptUrl);

    /**
     * Adds documentation to transaction.
     */
    FinancialTransaction addDocumentation(Long transactionId, String documentationUrl);

    /**
     * Adds attachment to transaction.
     */
    FinancialTransaction addAttachment(Long transactionId, String attachmentUrl);

    /**
     * Validates transaction documentation completeness.
     */
    boolean validateDocumentationCompleteness(Long transactionId);

    // =========================================================================
    // PAYMENT METHOD AND VENDOR ANALYSIS
    // =========================================================================

    /**
     * Finds transactions by payment method.
     */
    List<FinancialTransaction> findByPaymentMethod(FinancialTransaction.PaymentMethod paymentMethod, 
                                                  Integer teamNumber, Integer season);

    /**
     * Finds transactions by vendor.
     */
    List<FinancialTransaction> findByVendor(Integer teamNumber, Integer season, String vendor);

    /**
     * Analyzes top vendors by spending.
     */
    Map<String, BigDecimal> analyzeTopVendorsBySpending(Integer teamNumber, Integer season);

    /**
     * Analyzes spending distribution by payment method.
     */
    Map<FinancialTransaction.PaymentMethod, Object[]> analyzeSpendingByPaymentMethod(Integer teamNumber, Integer season);

    /**
     * Identifies preferred vendors and payment methods.
     */
    Map<String, Object> analyzePreferences(Integer teamNumber, Integer season);

    // =========================================================================
    // INTEGRATION AND AUTOMATION
    // =========================================================================

    /**
     * Finds automated transactions from external systems.
     */
    List<FinancialTransaction> findAutomatedTransactions(Integer teamNumber, Integer season);

    /**
     * Finds transactions by integration source.
     */
    List<FinancialTransaction> findByIntegrationSource(Integer teamNumber, Integer season, String source);

    /**
     * Imports transactions from external system.
     */
    List<FinancialTransaction> importTransactionsFromExternalSystem(String source, Integer teamNumber, Integer season);

    /**
     * Syncs transactions with external systems.
     */
    void syncWithExternalSystems(Integer teamNumber, Integer season);

    /**
     * Finds transactions by external ID.
     */
    Optional<FinancialTransaction> findByExternalTransactionId(String externalId);

    /**
     * Detects and resolves duplicate external transactions.
     */
    List<String> detectAndResolveDuplicateExternalTransactions(Integer teamNumber, Integer season);

    // =========================================================================
    // RECONCILIATION
    // =========================================================================

    /**
     * Finds unreconciled transactions.
     */
    List<FinancialTransaction> findUnreconciledTransactions(Integer teamNumber, Integer season);

    /**
     * Reconciles a transaction.
     */
    FinancialTransaction reconcileTransaction(Long transactionId);

    /**
     * Bulk reconciles transactions.
     */
    List<FinancialTransaction> bulkReconcileTransactions(List<Long> transactionIds);

    /**
     * Generates reconciliation report.
     */
    Map<String, Object> generateReconciliationReport(Integer teamNumber, Integer season);

    /**
     * Validates transaction data integrity.
     */
    boolean validateTransactionIntegrity(Long transactionId);

    // =========================================================================
    // RECURRING TRANSACTIONS
    // =========================================================================

    /**
     * Finds active recurring transactions.
     */
    List<FinancialTransaction> findActiveRecurringTransactions(Integer teamNumber, Integer season);

    /**
     * Finds recurring transactions due for processing.
     */
    List<FinancialTransaction> findRecurringTransactionsDue(Integer teamNumber, Integer season, LocalDate dueDate);

    /**
     * Processes due recurring transactions.
     */
    List<FinancialTransaction> processDueRecurringTransactions(Integer teamNumber, Integer season);

    /**
     * Creates next recurrence of a transaction.
     */
    FinancialTransaction createNextRecurrence(Long parentTransactionId);

    /**
     * Updates recurring transaction schedule.
     */
    FinancialTransaction updateRecurringSchedule(Long transactionId, LocalDate nextDate, LocalDate endDate);

    /**
     * Finds child transactions of a parent.
     */
    List<FinancialTransaction> findChildTransactions(FinancialTransaction parentTransaction);

    // =========================================================================
    // TAX AND COMPLIANCE
    // =========================================================================

    /**
     * Finds tax-deductible transactions.
     */
    List<FinancialTransaction> findTaxDeductibleTransactions(Integer teamNumber, Integer season);

    /**
     * Finds reimbursable transactions.
     */
    List<FinancialTransaction> findReimbursableTransactions(Integer teamNumber, Integer season);

    /**
     * Calculates total tax amounts.
     */
    BigDecimal calculateTotalTaxAmount(Integer teamNumber, Integer season);

    /**
     * Finds transactions by tax category.
     */
    List<FinancialTransaction> findByTaxCategory(Integer teamNumber, Integer season, String taxCategory);

    /**
     * Generates tax deduction report.
     */
    Map<String, Object> generateTaxDeductionReport(Integer teamNumber, Integer season);

    /**
     * Generates compliance report.
     */
    Map<String, Object> generateComplianceReport(Integer teamNumber, Integer season);

    // =========================================================================
    // PROJECT AND TASK ASSOCIATION
    // =========================================================================

    /**
     * Finds transactions associated with a project.
     */
    List<FinancialTransaction> findByProject(Project project);

    /**
     * Finds unassigned transactions.
     */
    List<FinancialTransaction> findUnassignedTransactions(Integer teamNumber, Integer season);

    /**
     * Assigns transaction to project.
     */
    FinancialTransaction assignToProject(Long transactionId, Project project);

    /**
     * Calculates project spending totals.
     */
    Map<Project, BigDecimal> calculateProjectSpendingTotals(Integer teamNumber, Integer season);

    /**
     * Analyzes top spending projects.
     */
    Map<Project, BigDecimal> analyzeTopSpendingProjects(Integer teamNumber, Integer season);

    /**
     * Generates project financial report.
     */
    Map<String, Object> generateProjectFinancialReport(Project project);

    // =========================================================================
    // FINANCIAL ANALYTICS
    // =========================================================================

    /**
     * Calculates total income for team and season.
     */
    BigDecimal calculateTotalIncome(Integer teamNumber, Integer season);

    /**
     * Calculates total expenses for team and season.
     */
    BigDecimal calculateTotalExpenses(Integer teamNumber, Integer season);

    /**
     * Calculates net cash flow.
     */
    BigDecimal calculateNetCashFlow(Integer teamNumber, Integer season);

    /**
     * Analyzes monthly spending trends.
     */
    Map<String, BigDecimal> analyzeMonthlySpendingTrends(Integer teamNumber, Integer season);

    /**
     * Calculates average transaction amounts by category.
     */
    Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateAverageAmountsByCategory(Integer teamNumber, Integer season);

    /**
     * Generates financial summary.
     */
    Map<String, Object> generateFinancialSummary(Integer teamNumber, Integer season);

    /**
     * Predicts future spending based on trends.
     */
    Map<String, Object> predictFutureSpending(Integer teamNumber, Integer season, Integer monthsAhead);

    // =========================================================================
    // PERFORMANCE ANALYSIS
    // =========================================================================

    /**
     * Analyzes transaction approval performance.
     */
    Map<String, Object> analyzeApprovalPerformance(Integer teamNumber, Integer season);

    /**
     * Finds most active team members by transaction volume.
     */
    Map<TeamMember, Long> findMostActiveSubmitters(Integer teamNumber, Integer season);

    /**
     * Calculates transaction processing efficiency metrics.
     */
    Map<String, Object> calculateProcessingEfficiencyMetrics(Integer teamNumber, Integer season);

    /**
     * Analyzes spending efficiency by category.
     */
    Map<String, Object> analyzeSpendingEfficiency(Integer teamNumber, Integer season);

    /**
     * Identifies cost optimization opportunities.
     */
    List<String> identifyCostOptimizationOpportunities(Integer teamNumber, Integer season);

    // =========================================================================
    // AUDIT AND COMPLIANCE
    // =========================================================================

    /**
     * Finds transactions requiring audit attention.
     */
    List<FinancialTransaction> findRequiringAuditAttention(Integer teamNumber, Integer season, BigDecimal threshold);

    /**
     * Generates comprehensive audit trail.
     */
    List<FinancialTransaction> generateAuditTrail(Integer teamNumber, Integer season);

    /**
     * Finds transactions modified within date range.
     */
    List<FinancialTransaction> findModifiedInDateRange(Integer teamNumber, Integer season, 
                                                     LocalDate startDate, LocalDate endDate);

    /**
     * Validates financial data integrity.
     */
    Map<String, Object> validateFinancialDataIntegrity(Integer teamNumber, Integer season);

    /**
     * Generates audit report.
     */
    Map<String, Object> generateAuditReport(Integer teamNumber, Integer season);

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches transactions by description or vendor.
     */
    List<FinancialTransaction> searchTransactions(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Finds transactions by invoice or PO number.
     */
    List<FinancialTransaction> findByInvoiceOrPoNumber(Integer teamNumber, Integer season, String number);

    /**
     * Finds transactions by GL account.
     */
    List<FinancialTransaction> findByGlAccount(Integer teamNumber, Integer season, String glAccount);

    /**
     * Finds recent transactions.
     */
    List<FinancialTransaction> findRecentTransactions(Integer teamNumber, Integer season, Integer withinDays);

    /**
     * Finds large transactions above threshold.
     */
    List<FinancialTransaction> findLargeTransactions(Integer teamNumber, Integer season, BigDecimal threshold);

    // =========================================================================
    // REPORTING
    // =========================================================================

    /**
     * Generates comprehensive financial report.
     */
    Map<String, Object> generateFinancialReport(Integer teamNumber, Integer season);

    /**
     * Generates executive financial summary.
     */
    String generateExecutiveFinancialSummary(Integer teamNumber, Integer season);

    /**
     * Exports transaction data for external analysis.
     */
    List<Map<String, Object>> exportTransactionData(Integer teamNumber, Integer season);

    /**
     * Generates budget vs actual report.
     */
    Map<String, Object> generateBudgetVsActualReport(Integer teamNumber, Integer season);

    /**
     * Generates vendor analysis report.
     */
    Map<String, Object> generateVendorAnalysisReport(Integer teamNumber, Integer season);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Creates multiple transactions.
     */
    List<FinancialTransaction> createBulkTransactions(List<FinancialTransaction> transactions);

    /**
     * Updates multiple transactions.
     */
    List<FinancialTransaction> updateBulkTransactions(Map<Long, FinancialTransaction> transactionUpdates);

    /**
     * Validates bulk transaction data.
     */
    List<String> validateBulkTransactions(List<FinancialTransaction> transactions);

    /**
     * Processes bulk approval.
     */
    List<FinancialTransaction> bulkApproveTransactions(List<Long> transactionIds, TeamMember approver);

    /**
     * Archives old transactions.
     */
    void archiveOldTransactions(Integer teamNumber, Integer season, LocalDate cutoffDate);

    // =========================================================================
    // INTEGRATION HELPERS
    // =========================================================================

    /**
     * Converts transaction to external format.
     */
    Map<String, Object> convertToExternalFormat(FinancialTransaction transaction, String format);

    /**
     * Imports transaction from external format.
     */
    FinancialTransaction importFromExternalFormat(Map<String, Object> data, String format);

    /**
     * Syncs transaction status with external system.
     */
    FinancialTransaction syncTransactionStatus(Long transactionId, String externalSystem);

    /**
     * Generates integration report.
     */
    Map<String, Object> generateIntegrationReport(Integer teamNumber, Integer season);

    /**
     * Validates external integration data.
     */
    boolean validateExternalIntegrationData(Integer teamNumber, Integer season);
}