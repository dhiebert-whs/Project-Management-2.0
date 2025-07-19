// src/main/java/org/frcpm/repositories/spring/PartTransactionRepository.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.repositories.spring;

import org.frcpm.models.Part;
import org.frcpm.models.PartTransaction;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for PartTransaction entities.
 * Provides transaction history and audit trail functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Repository
public interface PartTransactionRepository extends JpaRepository<PartTransaction, Long> {
    
    // Basic Lookups
    
    /**
     * Finds all transactions for a specific part.
     */
    List<PartTransaction> findByPartOrderByTransactionDateDesc(Part part);
    
    /**
     * Finds all transactions for a specific part within date range.
     */
    List<PartTransaction> findByPartAndTransactionDateBetweenOrderByTransactionDateDesc(
            Part part, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds transactions by type.
     */
    List<PartTransaction> findByTransactionTypeOrderByTransactionDateDesc(
            PartTransaction.TransactionType transactionType);
    
    /**
     * Finds transactions by type within date range.
     */
    List<PartTransaction> findByTransactionTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
            PartTransaction.TransactionType transactionType, LocalDateTime startDate, LocalDateTime endDate);
    
    // Project and Task Tracking
    
    /**
     * Finds all transactions for a specific project.
     */
    List<PartTransaction> findByProjectOrderByTransactionDateDesc(Project project);
    
    /**
     * Finds all transactions for a specific task.
     */
    List<PartTransaction> findByTaskOrderByTransactionDateDesc(Task task);
    
    /**
     * Finds transactions performed by a specific team member.
     */
    List<PartTransaction> findByPerformedByOrderByTransactionDateDesc(TeamMember teamMember);
    
    // Date-based Queries
    
    /**
     * Finds transactions within a date range.
     */
    List<PartTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds transactions after a specific date.
     */
    List<PartTransaction> findByTransactionDateAfterOrderByTransactionDateDesc(LocalDateTime date);
    
    /**
     * Finds recent transactions (last N records).
     */
    List<PartTransaction> findTop50ByOrderByTransactionDateDesc();
    
    /**
     * Finds recent transactions for a part.
     */
    List<PartTransaction> findTop10ByPartOrderByTransactionDateDesc(Part part);
    
    // Financial Queries
    
    /**
     * Finds transactions with total cost above threshold.
     */
    List<PartTransaction> findByTotalCostGreaterThanOrderByTransactionDateDesc(BigDecimal threshold);
    
    /**
     * Finds transactions with no cost information.
     */
    List<PartTransaction> findByTotalCostIsNullOrderByTransactionDateDesc();
    
    /**
     * Finds transactions from specific vendor.
     */
    List<PartTransaction> findByVendorContainingIgnoreCaseOrderByTransactionDateDesc(String vendor);
    
    // Approval and Reference
    
    /**
     * Finds unapproved transactions.
     */
    List<PartTransaction> findByIsApprovedFalseOrderByTransactionDateDesc();
    
    /**
     * Finds transactions by reference number.
     */
    List<PartTransaction> findByReferenceNumberContainingIgnoreCaseOrderByTransactionDateDesc(String referenceNumber);
    
    /**
     * Finds transactions approved by specific person.
     */
    List<PartTransaction> findByApprovedByOrderByTransactionDateDesc(String approvedBy);
    
    // Quantity-based Queries
    
    /**
     * Finds large quantity transactions.
     */
    List<PartTransaction> findByQuantityGreaterThanOrderByTransactionDateDesc(int quantity);
    
    /**
     * Finds transactions affecting specific quantity range.
     */
    List<PartTransaction> findByQuantityBetweenOrderByTransactionDateDesc(int minQuantity, int maxQuantity);
    
    // Advanced Search
    
    /**
     * Finds transactions by reason containing search term.
     */
    List<PartTransaction> findByReasonContainingIgnoreCaseOrderByTransactionDateDesc(String searchTerm);
    
    /**
     * Finds transactions by notes containing search term.
     */
    List<PartTransaction> findByNotesContainingIgnoreCaseOrderByTransactionDateDesc(String searchTerm);
    
    /**
     * Finds transactions by part and type.
     */
    List<PartTransaction> findByPartAndTransactionTypeOrderByTransactionDateDesc(
            Part part, PartTransaction.TransactionType transactionType);
    
    /**
     * Finds transactions by project and type.
     */
    List<PartTransaction> findByProjectAndTransactionTypeOrderByTransactionDateDesc(
            Project project, PartTransaction.TransactionType transactionType);
    
    // Statistical Queries
    
    /**
     * Counts transactions for a part.
     */
    long countByPart(Part part);
    
    /**
     * Counts transactions by type.
     */
    long countByTransactionType(PartTransaction.TransactionType transactionType);
    
    /**
     * Counts transactions for a project.
     */
    long countByProject(Project project);
    
    /**
     * Counts transactions performed by team member.
     */
    long countByPerformedBy(TeamMember teamMember);
    
    /**
     * Counts unapproved transactions.
     */
    long countByIsApprovedFalse();
    
    // Usage Tracking
    
    /**
     * Finds incoming transactions (purchases, donations, returns).
     */
    List<PartTransaction> findByTransactionTypeInOrderByTransactionDateDesc(
            List<PartTransaction.TransactionType> incomingTypes);
    
    /**
     * Finds transactions for parts in specific categories.
     */
    // Simplified without complex joins - get transactions and filter in service layer
    List<PartTransaction> findByPartInOrderByTransactionDateDesc(List<Part> parts);
    
    // Audit and Compliance
    
    /**
     * Finds transactions created by specific user.
     */
    List<PartTransaction> findByCreatedByOrderByTransactionDateDesc(String createdBy);
    
    /**
     * Finds transactions created within date range.
     */
    List<PartTransaction> findByCreatedAtBetweenOrderByTransactionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds high-value transactions requiring audit.
     */
    List<PartTransaction> findByTotalCostGreaterThanAndIsApprovedTrueOrderByTransactionDateDesc(
            BigDecimal auditThreshold);
    
    // Part-specific Transaction History
    
    /**
     * Gets the last transaction for a part.
     */
    PartTransaction findTopByPartOrderByTransactionDateDesc(Part part);
    
    /**
     * Gets the last purchase transaction for a part.
     */
    PartTransaction findTopByPartAndTransactionTypeOrderByTransactionDateDesc(
            Part part, PartTransaction.TransactionType transactionType);
    
    /**
     * Finds all purchase transactions for a part.
     */
    List<PartTransaction> findByPartAndTransactionTypeInOrderByTransactionDateDesc(
            Part part, List<PartTransaction.TransactionType> purchaseTypes);
}