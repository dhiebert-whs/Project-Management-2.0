// src/main/java/org/frcpm/services/PartTransactionService.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services;

import org.frcpm.models.Part;
import org.frcpm.models.PartTransaction;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing part transactions.
 * Provides business logic for transaction CRUD operations, audit trails,
 * and transaction analysis.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
public interface PartTransactionService {
    
    // Basic CRUD Operations
    
    /**
     * Creates a new part transaction.
     */
    PartTransaction createTransaction(Part part, PartTransaction.TransactionType transactionType,
                                    Integer quantity, String reason, BigDecimal unitCost,
                                    Long projectId, Long taskId, Long teamMemberId);
    
    /**
     * Creates a transaction with full details.
     */
    PartTransaction createTransaction(PartTransaction transaction);
    
    /**
     * Updates an existing transaction.
     */
    PartTransaction updateTransaction(Long transactionId, PartTransaction transaction);
    
    /**
     * Finds a transaction by ID.
     */
    Optional<PartTransaction> findTransactionById(Long transactionId);
    
    /**
     * Deletes a transaction (admin only).
     */
    void deleteTransaction(Long transactionId);
    
    // Transaction History
    
    /**
     * Gets all transactions for a specific part.
     */
    List<PartTransaction> getTransactionsByPart(Part part);
    
    /**
     * Gets transactions for a part within date range.
     */
    List<PartTransaction> getTransactionsByPartAndDateRange(Part part, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets all transactions by type.
     */
    List<PartTransaction> getTransactionsByType(PartTransaction.TransactionType transactionType);
    
    /**
     * Gets transactions by project.
     */
    List<PartTransaction> getTransactionsByProject(Project project);
    
    /**
     * Gets transactions by task.
     */
    List<PartTransaction> getTransactionsByTask(Task task);
    
    /**
     * Gets transactions performed by team member.
     */
    List<PartTransaction> getTransactionsByTeamMember(TeamMember teamMember);
    
    // Date-based Queries
    
    /**
     * Gets transactions within date range.
     */
    List<PartTransaction> getTransactionsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets recent transactions.
     */
    List<PartTransaction> getRecentTransactions(int limit);
    
    /**
     * Gets recent transactions for a part.
     */
    List<PartTransaction> getRecentTransactionsForPart(Part part, int limit);
    
    // Financial Analysis
    
    /**
     * Gets transactions above cost threshold.
     */
    List<PartTransaction> getHighValueTransactions(BigDecimal threshold);
    
    /**
     * Gets transactions with no cost information.
     */
    List<PartTransaction> getTransactionsWithNoCost();
    
    /**
     * Gets transactions by vendor.
     */
    List<PartTransaction> getTransactionsByVendor(String vendor);
    
    /**
     * Calculates total spending in date range.
     */
    BigDecimal getTotalSpending(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calculates spending by vendor.
     */
    List<Object[]> getSpendingByVendor(LocalDateTime startDate, LocalDateTime endDate);
    
    // Approval Management
    
    /**
     * Gets unapproved transactions.
     */
    List<PartTransaction> getUnapprovedTransactions();
    
    /**
     * Approves a transaction.
     */
    PartTransaction approveTransaction(Long transactionId, String approvedBy);
    
    /**
     * Bulk approves transactions.
     */
    List<PartTransaction> bulkApproveTransactions(List<Long> transactionIds, String approvedBy);
    
    /**
     * Gets transactions by reference number.
     */
    List<PartTransaction> getTransactionsByReference(String referenceNumber);
    
    // Usage Analytics
    
    /**
     * Gets part usage statistics for date range.
     */
    List<Object[]> getPartUsageStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets most used parts.
     */
    List<Object[]> getMostUsedParts(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    /**
     * Gets project consumption statistics.
     */
    List<Object[]> getProjectConsumptionStats(Project project);
    
    /**
     * Gets team member activity statistics.
     */
    List<Object[]> getTeamMemberActivityStats(TeamMember teamMember, LocalDateTime startDate, LocalDateTime endDate);
    
    // Inventory Movement Analysis
    
    /**
     * Gets incoming transactions (purchases, donations, returns).
     */
    List<PartTransaction> getIncomingTransactions(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets outgoing transactions (usage, damage, disposal).
     */
    List<PartTransaction> getOutgoingTransactions(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Gets adjustment transactions.
     */
    List<PartTransaction> getAdjustmentTransactions(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calculates inventory turnover rate.
     */
    Double getInventoryTurnoverRate(Part part, LocalDateTime startDate, LocalDateTime endDate);
    
    // Audit and Compliance
    
    /**
     * Gets audit trail for a part.
     */
    List<PartTransaction> getAuditTrail(Part part);
    
    /**
     * Gets transactions created by user.
     */
    List<PartTransaction> getTransactionsByCreatedBy(String createdBy);
    
    /**
     * Gets transactions requiring audit review.
     */
    List<PartTransaction> getTransactionsRequiringAudit(BigDecimal auditThreshold);
    
    /**
     * Validates transaction integrity.
     */
    boolean validateTransactionIntegrity(PartTransaction transaction);
    
    // Search and Filter
    
    /**
     * Searches transactions by reason.
     */
    List<PartTransaction> searchTransactionsByReason(String searchTerm);
    
    /**
     * Searches transactions by notes.
     */
    List<PartTransaction> searchTransactionsByNotes(String searchTerm);
    
    /**
     * Gets transactions by quantity range.
     */
    List<PartTransaction> getTransactionsByQuantityRange(int minQuantity, int maxQuantity);
    
    // Statistical Analysis
    
    /**
     * Counts transactions by type.
     */
    long countTransactionsByType(PartTransaction.TransactionType transactionType);
    
    /**
     * Counts transactions for project.
     */
    long countTransactionsByProject(Project project);
    
    /**
     * Counts unapproved transactions.
     */
    long countUnapprovedTransactions();
    
    /**
     * Gets transaction volume statistics.
     */
    List<Object[]> getTransactionVolumeStats(LocalDateTime startDate, LocalDateTime endDate);
    
    // Transaction Validation
    
    /**
     * Validates that a transaction is allowed.
     */
    boolean canCreateTransaction(Part part, PartTransaction.TransactionType transactionType, int quantity);
    
    /**
     * Checks if transaction requires approval.
     */
    boolean requiresApproval(PartTransaction transaction);
    
    /**
     * Gets last transaction for a part.
     */
    Optional<PartTransaction> getLastTransactionForPart(Part part);
    
    /**
     * Gets balance after transaction.
     */
    int calculateBalanceAfterTransaction(Part part, int quantityChange);
}