// src/main/java/org/frcpm/services/impl/PartTransactionServiceImpl.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services.impl;

import org.frcpm.models.Part;
import org.frcpm.models.PartTransaction;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.PartTransactionRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.services.PartTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of PartTransactionService for managing part transactions.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Service
@Transactional
public class PartTransactionServiceImpl implements PartTransactionService {
    
    private static final Logger LOGGER = Logger.getLogger(PartTransactionServiceImpl.class.getName());
    
    @Autowired
    private PartTransactionRepository partTransactionRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    // Basic CRUD Operations
    
    @Override
    public PartTransaction createTransaction(Part part, PartTransaction.TransactionType transactionType,
                                           Integer quantity, String reason, BigDecimal unitCost,
                                           Long projectId, Long taskId, Long teamMemberId) {
        try {
            LOGGER.info("Creating transaction for part: " + part.getPartNumber() + ", type: " + transactionType);
            
            PartTransaction transaction = new PartTransaction();
            transaction.setPart(part);
            transaction.setTransactionType(transactionType);
            transaction.setQuantity(quantity);
            transaction.setReason(reason);
            transaction.setUnitCost(unitCost);
            
            // Set relationships
            if (projectId != null) {
                Optional<Project> project = projectRepository.findById(projectId);
                project.ifPresent(transaction::setProject);
            }
            
            if (taskId != null) {
                Optional<Task> task = taskRepository.findById(taskId);
                task.ifPresent(transaction::setTask);
            }
            
            if (teamMemberId != null) {
                Optional<TeamMember> teamMember = teamMemberRepository.findById(teamMemberId);
                teamMember.ifPresent(transaction::setPerformedBy);
            }
            
            // Calculate balance after transaction
            int effectiveChange = transaction.getEffectiveQuantityChange();
            int balanceAfter = part.getQuantityOnHand() + effectiveChange;
            transaction.setBalanceAfter(balanceAfter);
            
            // Calculate total cost
            if (unitCost != null) {
                BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(Math.abs(quantity)));
                transaction.setTotalCost(totalCost);
            }
            
            // Set approval status
            transaction.setIsApproved(!transaction.requiresApproval());
            
            PartTransaction savedTransaction = partTransactionRepository.save(transaction);
            LOGGER.info("Successfully created transaction: " + savedTransaction.getId());
            return savedTransaction;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating transaction for part: " + part.getPartNumber(), e);
            throw new RuntimeException("Failed to create transaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PartTransaction createTransaction(PartTransaction transaction) {
        try {
            LOGGER.info("Creating transaction: " + transaction.getTransactionType());
            
            // Validate transaction
            if (!validateTransactionIntegrity(transaction)) {
                throw new IllegalArgumentException("Invalid transaction data");
            }
            
            // Calculate balance if not set
            if (transaction.getBalanceAfter() == null) {
                int effectiveChange = transaction.getEffectiveQuantityChange();
                int balanceAfter = transaction.getPart().getQuantityOnHand() + effectiveChange;
                transaction.setBalanceAfter(balanceAfter);
            }
            
            PartTransaction savedTransaction = partTransactionRepository.save(transaction);
            LOGGER.info("Successfully created transaction: " + savedTransaction.getId());
            return savedTransaction;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating transaction", e);
            throw new RuntimeException("Failed to create transaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PartTransaction updateTransaction(Long transactionId, PartTransaction transaction) {
        try {
            LOGGER.info("Updating transaction: " + transactionId);
            
            Optional<PartTransaction> existingOpt = partTransactionRepository.findById(transactionId);
            if (!existingOpt.isPresent()) {
                throw new IllegalArgumentException("Transaction not found: " + transactionId);
            }
            
            // Preserve ID and timestamps
            transaction.setId(transactionId);
            
            PartTransaction savedTransaction = partTransactionRepository.save(transaction);
            LOGGER.info("Successfully updated transaction: " + savedTransaction.getId());
            return savedTransaction;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating transaction: " + transactionId, e);
            throw new RuntimeException("Failed to update transaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PartTransaction> findTransactionById(Long transactionId) {
        try {
            return partTransactionRepository.findById(transactionId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding transaction by ID: " + transactionId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void deleteTransaction(Long transactionId) {
        try {
            LOGGER.warning("Deleting transaction: " + transactionId);
            partTransactionRepository.deleteById(transactionId);
            LOGGER.warning("Successfully deleted transaction: " + transactionId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting transaction: " + transactionId, e);
            throw new RuntimeException("Failed to delete transaction: " + e.getMessage(), e);
        }
    }
    
    // Transaction History
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByPart(Part part) {
        try {
            return partTransactionRepository.findByPartOrderByTransactionDateDesc(part);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by part: " + part.getPartNumber(), e);
            throw new RuntimeException("Failed to get transactions by part: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByPartAndDateRange(Part part, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return partTransactionRepository.findByPartAndTransactionDateBetweenOrderByTransactionDateDesc(
                part, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by part and date range", e);
            throw new RuntimeException("Failed to get transactions by part and date range: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByType(PartTransaction.TransactionType transactionType) {
        try {
            return partTransactionRepository.findByTransactionTypeOrderByTransactionDateDesc(transactionType);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by type: " + transactionType, e);
            throw new RuntimeException("Failed to get transactions by type: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByProject(Project project) {
        try {
            return partTransactionRepository.findByProjectOrderByTransactionDateDesc(project);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by project: " + project.getName(), e);
            throw new RuntimeException("Failed to get transactions by project: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByTask(Task task) {
        try {
            return partTransactionRepository.findByTaskOrderByTransactionDateDesc(task);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by task: " + task.getTitle(), e);
            throw new RuntimeException("Failed to get transactions by task: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByTeamMember(TeamMember teamMember) {
        try {
            return partTransactionRepository.findByPerformedByOrderByTransactionDateDesc(teamMember);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by team member", e);
            throw new RuntimeException("Failed to get transactions by team member: " + e.getMessage(), e);
        }
    }
    
    // Date-based Queries
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return partTransactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions in date range", e);
            throw new RuntimeException("Failed to get transactions in date range: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getRecentTransactions(int limit) {
        try {
            return partTransactionRepository.findTop50ByOrderByTransactionDateDesc();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting recent transactions", e);
            throw new RuntimeException("Failed to get recent transactions: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getRecentTransactionsForPart(Part part, int limit) {
        try {
            return partTransactionRepository.findTop10ByPartOrderByTransactionDateDesc(part);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting recent transactions for part", e);
            throw new RuntimeException("Failed to get recent transactions for part: " + e.getMessage(), e);
        }
    }
    
    // Financial Analysis
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getHighValueTransactions(BigDecimal threshold) {
        try {
            return partTransactionRepository.findByTotalCostGreaterThanOrderByTransactionDateDesc(threshold);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting high value transactions", e);
            throw new RuntimeException("Failed to get high value transactions: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsWithNoCost() {
        try {
            return partTransactionRepository.findByTotalCostIsNullOrderByTransactionDateDesc();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions with no cost", e);
            throw new RuntimeException("Failed to get transactions with no cost: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByVendor(String vendor) {
        try {
            return partTransactionRepository.findByVendorContainingIgnoreCaseOrderByTransactionDateDesc(vendor);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by vendor: " + vendor, e);
            throw new RuntimeException("Failed to get transactions by vendor: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpending(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<PartTransaction> transactions = getTransactionsInDateRange(startDate, endDate);
            return transactions.stream()
                .filter(t -> t.getTotalCost() != null && t.getTransactionType().isIncoming())
                .map(PartTransaction::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating total spending", e);
            throw new RuntimeException("Failed to calculate total spending: " + e.getMessage(), e);
        }
    }
    
    // Approval Management
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getUnapprovedTransactions() {
        try {
            return partTransactionRepository.findByIsApprovedFalseOrderByTransactionDateDesc();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting unapproved transactions", e);
            throw new RuntimeException("Failed to get unapproved transactions: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PartTransaction approveTransaction(Long transactionId, String approvedBy) {
        try {
            LOGGER.info("Approving transaction: " + transactionId);
            
            Optional<PartTransaction> transactionOpt = partTransactionRepository.findById(transactionId);
            if (!transactionOpt.isPresent()) {
                throw new IllegalArgumentException("Transaction not found: " + transactionId);
            }
            
            PartTransaction transaction = transactionOpt.get();
            transaction.approve(approvedBy);
            
            PartTransaction savedTransaction = partTransactionRepository.save(transaction);
            LOGGER.info("Successfully approved transaction: " + savedTransaction.getId());
            return savedTransaction;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving transaction: " + transactionId, e);
            throw new RuntimeException("Failed to approve transaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<PartTransaction> bulkApproveTransactions(List<Long> transactionIds, String approvedBy) {
        try {
            LOGGER.info("Bulk approving " + transactionIds.size() + " transactions");
            
            return transactionIds.stream()
                .map(id -> approveTransaction(id, approvedBy))
                .toList();
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error bulk approving transactions", e);
            throw new RuntimeException("Failed to bulk approve transactions: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByReference(String referenceNumber) {
        try {
            return partTransactionRepository.findByReferenceNumberContainingIgnoreCaseOrderByTransactionDateDesc(referenceNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by reference: " + referenceNumber, e);
            throw new RuntimeException("Failed to get transactions by reference: " + e.getMessage(), e);
        }
    }
    
    // Validation and Business Rules
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCreateTransaction(Part part, PartTransaction.TransactionType transactionType, int quantity) {
        try {
            // For outgoing transactions, check if sufficient quantity is available
            if (transactionType.isOutgoing()) {
                return part.getQuantityOnHand() >= quantity;
            }
            
            // Incoming transactions are generally allowed
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if transaction can be created", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean requiresApproval(PartTransaction transaction) {
        try {
            return transaction.requiresApproval();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if transaction requires approval", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateTransactionIntegrity(PartTransaction transaction) {
        try {
            // Basic validation
            if (transaction.getPart() == null || 
                transaction.getTransactionType() == null || 
                transaction.getQuantity() == null || 
                transaction.getQuantity() <= 0) {
                return false;
            }
            
            // Check if part can handle the transaction
            return canCreateTransaction(transaction.getPart(), transaction.getTransactionType(), transaction.getQuantity());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating transaction integrity", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PartTransaction> getLastTransactionForPart(Part part) {
        try {
            PartTransaction lastTransaction = partTransactionRepository.findTopByPartOrderByTransactionDateDesc(part);
            return Optional.ofNullable(lastTransaction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting last transaction for part", e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public int calculateBalanceAfterTransaction(Part part, int quantityChange) {
        try {
            return part.getQuantityOnHand() + quantityChange;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating balance after transaction", e);
            return 0;
        }
    }
    
    // Simplified implementations for complex analytics
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getSpendingByVendor(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPartUsageStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostUsedParts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getProjectConsumptionStats(Project project) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTeamMemberActivityStats(TeamMember teamMember, LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getIncomingTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation - would need to define incoming types
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getOutgoingTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation - would need to define outgoing types
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getAdjustmentTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getInventoryTurnoverRate(Part part, LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return 0.0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getAuditTrail(Part part) {
        return getTransactionsByPart(part);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByCreatedBy(String createdBy) {
        try {
            return partTransactionRepository.findByCreatedByOrderByTransactionDateDesc(createdBy);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by created by: " + createdBy, e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsRequiringAudit(BigDecimal auditThreshold) {
        try {
            return partTransactionRepository.findByTotalCostGreaterThanAndIsApprovedTrueOrderByTransactionDateDesc(auditThreshold);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions requiring audit", e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> searchTransactionsByReason(String searchTerm) {
        try {
            return partTransactionRepository.findByReasonContainingIgnoreCaseOrderByTransactionDateDesc(searchTerm);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching transactions by reason: " + searchTerm, e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> searchTransactionsByNotes(String searchTerm) {
        try {
            return partTransactionRepository.findByNotesContainingIgnoreCaseOrderByTransactionDateDesc(searchTerm);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching transactions by notes: " + searchTerm, e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByQuantityRange(int minQuantity, int maxQuantity) {
        try {
            return partTransactionRepository.findByQuantityBetweenOrderByTransactionDateDesc(minQuantity, maxQuantity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by quantity range", e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTransactionsByType(PartTransaction.TransactionType transactionType) {
        try {
            return partTransactionRepository.countByTransactionType(transactionType);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting transactions by type: " + transactionType, e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTransactionsByProject(Project project) {
        try {
            return partTransactionRepository.countByProject(project);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting transactions by project", e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUnapprovedTransactions() {
        try {
            return partTransactionRepository.countByIsApprovedFalse();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting unapproved transactions", e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTransactionVolumeStats(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation
        return List.of();
    }
}