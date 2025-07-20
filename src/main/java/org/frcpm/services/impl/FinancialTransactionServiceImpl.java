// src/main/java/org/frcpm/services/impl/FinancialTransactionServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.FinancialTransaction;
import org.frcpm.models.BudgetCategory;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.FinancialTransactionRepository;
import org.frcpm.repositories.spring.BudgetCategoryRepository;
import org.frcpm.services.FinancialTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of FinancialTransactionService.
 * 
 * Provides comprehensive financial transaction management services including
 * automated integration, budget tracking, compliance reporting, approval workflows,
 * and financial analytics for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
@Service
@Transactional
public class FinancialTransactionServiceImpl implements FinancialTransactionService {

    @Autowired
    private FinancialTransactionRepository transactionRepository;
    
    @Autowired
    private BudgetCategoryRepository budgetCategoryRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public FinancialTransaction create(FinancialTransaction transaction) {
        return createTransaction(transaction);
    }

    @Override
    public FinancialTransaction update(Long id, FinancialTransaction transaction) {
        return updateTransaction(id, transaction);
    }

    @Override
    public void delete(Long id) {
        deactivateTransaction(id);
    }

    @Override
    public Optional<FinancialTransaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<FinancialTransaction> findAll() {
        return transactionRepository.findAll().stream()
                .filter(FinancialTransaction::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return transactionRepository.existsById(id);
    }

    @Override
    public long count() {
        return transactionRepository.count();
    }

    // =========================================================================
    // TRANSACTION MANAGEMENT
    // =========================================================================

    @Override
    public FinancialTransaction createTransaction(FinancialTransaction transaction) {
        validateTransaction(transaction);
        
        // Set defaults
        if (transaction.getStatus() == null) {
            transaction.setStatus(FinancialTransaction.TransactionStatus.DRAFT);
        }
        
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        
        // Auto-approval logic
        if (shouldAutoApprove(transaction)) {
            transaction.setIsApproved(true);
            transaction.setApprovedAt(LocalDateTime.now());
            transaction.setStatus(FinancialTransaction.TransactionStatus.APPROVED);
        }
        
        // Update budget category if assigned
        updateBudgetCategoryOnTransaction(transaction);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public FinancialTransaction createTransaction(Integer teamNumber, Integer season, BigDecimal amount,
                                                FinancialTransaction.TransactionType transactionType,
                                                FinancialTransaction.TransactionCategory category,
                                                String description, TeamMember submittedBy) {
        
        FinancialTransaction transaction = new FinancialTransaction(teamNumber, season, amount, 
                                                                   transactionType, category, description);
        transaction.setSubmittedBy(submittedBy);
        transaction.setSubmittedAt(LocalDateTime.now());
        
        return createTransaction(transaction);
    }

    @Override
    public FinancialTransaction updateTransaction(Long transactionId, FinancialTransaction transaction) {
        FinancialTransaction existing = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        // Update fields
        updateTransactionFields(existing, transaction);
        
        // Recalculate budget impact
        updateBudgetCategoryOnTransaction(existing);
        
        return transactionRepository.save(existing);
    }

    @Override
    public void deactivateTransaction(Long transactionId) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setIsActive(false);
        
        // Update budget category
        if (transaction.getBudgetCategory() != null) {
            updateBudgetCategoryTotals(transaction.getBudgetCategory());
        }
        
        transactionRepository.save(transaction);
    }

    @Override
    public List<FinancialTransaction> findActiveTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    // =========================================================================
    // QUERY OPERATIONS
    // =========================================================================

    @Override
    public List<FinancialTransaction> findByTeamAndSeason(Integer teamNumber, Integer season) {
        return transactionRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findByStatus(Integer teamNumber, Integer season, 
                                                  FinancialTransaction.TransactionStatus status) {
        return transactionRepository.findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(teamNumber, season, status);
    }

    @Override
    public List<FinancialTransaction> findByType(Integer teamNumber, FinancialTransaction.TransactionType type) {
        return transactionRepository.findByTeamNumberAndTransactionTypeAndIsActiveTrue(teamNumber, type);
    }

    @Override
    public List<FinancialTransaction> findByCategory(FinancialTransaction.TransactionCategory category, Integer season) {
        return transactionRepository.findByCategoryAndSeasonAndIsActiveTrue(category, season);
    }

    @Override
    public List<FinancialTransaction> findByAmountRange(Integer teamNumber, Integer season, 
                                                       BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findByAmountRange(teamNumber, season, minAmount, maxAmount);
    }

    @Override
    public List<FinancialTransaction> findByDateRange(Integer teamNumber, Integer season, 
                                                     LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateRange(teamNumber, season, startDate, endDate);
    }

    // =========================================================================
    // BUDGET AND CATEGORY OPERATIONS
    // =========================================================================

    @Override
    public List<FinancialTransaction> findByBudgetCategory(BudgetCategory budgetCategory) {
        return transactionRepository.findByBudgetCategoryAndIsActiveTrue(budgetCategory);
    }

    @Override
    public List<FinancialTransaction> findOverBudgetTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findOverBudgetTransactions(teamNumber, season);
    }

    @Override
    public Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateSpendingByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findSpendingByCategory(teamNumber, season);
        
        Map<FinancialTransaction.TransactionCategory, BigDecimal> spending = new HashMap<>();
        
        for (Object[] row : results) {
            FinancialTransaction.TransactionCategory category = (FinancialTransaction.TransactionCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            spending.put(category, amount);
        }
        
        return spending;
    }

    @Override
    public Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateIncomeByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findIncomeByCategory(teamNumber, season);
        
        Map<FinancialTransaction.TransactionCategory, BigDecimal> income = new HashMap<>();
        
        for (Object[] row : results) {
            FinancialTransaction.TransactionCategory category = (FinancialTransaction.TransactionCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            income.put(category, amount);
        }
        
        return income;
    }

    @Override
    public Map<String, Object> analyzeBudgetCategoryUtilization(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findBudgetCategoryUtilization(teamNumber, season);
        
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        for (Object[] row : results) {
            BudgetCategory category = (BudgetCategory) row[0];
            Long transactionCount = (Long) row[1];
            BigDecimal totalSpent = (BigDecimal) row[2];
            
            Map<String, Object> data = new HashMap<>();
            data.put("category", category);
            data.put("transactionCount", transactionCount);
            data.put("totalSpent", totalSpent);
            data.put("utilization", category.getBudgetUtilization());
            
            categoryData.add(data);
        }
        
        analysis.put("categories", categoryData);
        analysis.put("totalCategories", categoryData.size());
        
        return analysis;
    }

    @Override
    public void updateBudgetCategoryTotals(BudgetCategory budgetCategory) {
        List<FinancialTransaction> transactions = findByBudgetCategory(budgetCategory);
        
        BigDecimal totalSpent = transactions.stream()
                .filter(t -> t.isExpense())
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        budgetCategory.setSpentAmount(totalSpent);
        budgetCategory.updateAvailableAmount();
        
        budgetCategoryRepository.save(budgetCategory);
    }

    // =========================================================================
    // APPROVAL WORKFLOW
    // =========================================================================

    @Override
    public List<FinancialTransaction> findPendingApproval(Integer teamNumber, Integer season) {
        return transactionRepository.findPendingApproval(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findRequiringApproval(Integer teamNumber, Integer season, BigDecimal threshold) {
        return transactionRepository.findRequiringApproval(teamNumber, season, threshold);
    }

    @Override
    public FinancialTransaction approveTransaction(Long transactionId, TeamMember approver, String notes) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.approve(approver, notes);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public FinancialTransaction rejectTransaction(Long transactionId, String reason) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.reject(reason);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public FinancialTransaction submitForApproval(Long transactionId) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setStatus(FinancialTransaction.TransactionStatus.SUBMITTED);
        transaction.setSubmittedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }

    @Override
    public List<FinancialTransaction> findByApprover(TeamMember approver, Integer season) {
        return transactionRepository.findByApprovedByAndSeasonAndIsActiveTrue(approver, season);
    }

    @Override
    public List<FinancialTransaction> findBySubmitter(TeamMember submitter, Integer season) {
        return transactionRepository.findBySubmittedByAndSeasonAndIsActiveTrue(submitter, season);
    }

    // =========================================================================
    // RECEIPT AND DOCUMENTATION
    // =========================================================================

    @Override
    public List<FinancialTransaction> findMissingRequiredReceipts(Integer teamNumber, Integer season) {
        return transactionRepository.findMissingRequiredReceipts(teamNumber, season);
    }

    @Override
    public FinancialTransaction addReceipt(Long transactionId, String receiptUrl) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setReceiptUrl(receiptUrl);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public FinancialTransaction addDocumentation(Long transactionId, String documentationUrl) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setDocumentationUrl(documentationUrl);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public FinancialTransaction addAttachment(Long transactionId, String attachmentUrl) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.addAttachment(attachmentUrl);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public boolean validateDocumentationCompleteness(Long transactionId) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        return transaction.isValid() && !transaction.isMissingRequiredReceipt();
    }

    // =========================================================================
    // PAYMENT METHOD AND VENDOR ANALYSIS
    // =========================================================================

    @Override
    public List<FinancialTransaction> findByPaymentMethod(FinancialTransaction.PaymentMethod paymentMethod, 
                                                          Integer teamNumber, Integer season) {
        return transactionRepository.findByPaymentMethodAndTeamNumberAndSeasonAndIsActiveTrue(paymentMethod, teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findByVendor(Integer teamNumber, Integer season, String vendor) {
        return transactionRepository.findByVendor(teamNumber, season, vendor);
    }

    @Override
    public Map<String, BigDecimal> analyzeTopVendorsBySpending(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findTopVendorsBySpending(teamNumber, season);
        
        Map<String, BigDecimal> topVendors = new LinkedHashMap<>();
        
        for (Object[] row : results) {
            String vendor = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            topVendors.put(vendor, amount);
        }
        
        return topVendors;
    }

    @Override
    public Map<FinancialTransaction.PaymentMethod, Object[]> analyzeSpendingByPaymentMethod(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findSpendingByPaymentMethod(teamNumber, season);
        
        Map<FinancialTransaction.PaymentMethod, Object[]> analysis = new HashMap<>();
        
        for (Object[] row : results) {
            FinancialTransaction.PaymentMethod method = (FinancialTransaction.PaymentMethod) row[0];
            analysis.put(method, row);
        }
        
        return analysis;
    }

    @Override
    public Map<String, Object> analyzePreferences(Integer teamNumber, Integer season) {
        Map<String, Object> preferences = new HashMap<>();
        
        // Top vendors
        Map<String, BigDecimal> topVendors = analyzeTopVendorsBySpending(teamNumber, season);
        preferences.put("topVendors", topVendors);
        
        // Payment method distribution
        Map<FinancialTransaction.PaymentMethod, Object[]> paymentMethods = analyzeSpendingByPaymentMethod(teamNumber, season);
        preferences.put("paymentMethodDistribution", paymentMethods);
        
        // Category preferences
        Map<FinancialTransaction.TransactionCategory, BigDecimal> categorySpending = calculateSpendingByCategory(teamNumber, season);
        preferences.put("categorySpending", categorySpending);
        
        return preferences;
    }

    // =========================================================================
    // INTEGRATION AND AUTOMATION
    // =========================================================================

    @Override
    public List<FinancialTransaction> findAutomatedTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findAutomatedTransactions(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findByIntegrationSource(Integer teamNumber, Integer season, String source) {
        return transactionRepository.findByIntegrationSource(teamNumber, season, source);
    }

    @Override
    public List<FinancialTransaction> importTransactionsFromExternalSystem(String source, Integer teamNumber, Integer season) {
        // Implementation would connect to external systems like QuickBooks, etc.
        // For now, return empty list as placeholder
        List<FinancialTransaction> importedTransactions = new ArrayList<>();
        
        // Log import attempt
        System.out.println("Importing transactions from " + source + " for team " + teamNumber + " season " + season);
        
        return importedTransactions;
    }

    @Override
    public void syncWithExternalSystems(Integer teamNumber, Integer season) {
        // Sync with QuickBooks
        importTransactionsFromExternalSystem("QuickBooks", teamNumber, season);
        
        // Sync with bank feeds
        importTransactionsFromExternalSystem("BankFeed", teamNumber, season);
        
        // Update reconciliation status
        List<FinancialTransaction> unreconciled = findUnreconciledTransactions(teamNumber, season);
        System.out.println("Found " + unreconciled.size() + " unreconciled transactions");
    }

    @Override
    public Optional<FinancialTransaction> findByExternalTransactionId(String externalId) {
        return transactionRepository.findByExternalTransactionIdAndIsActiveTrue(externalId);
    }

    @Override
    public List<String> detectAndResolveDuplicateExternalTransactions(Integer teamNumber, Integer season) {
        List<Object[]> duplicates = transactionRepository.findDuplicateExternalTransactions(teamNumber, season);
        
        List<String> resolvedDuplicates = new ArrayList<>();
        
        for (Object[] duplicate : duplicates) {
            String externalId = (String) duplicate[0];
            Long count = (Long) duplicate[1];
            
            if (count > 1) {
                // Keep the first transaction, deactivate others
                List<FinancialTransaction> transactions = transactionRepository.findAll().stream()
                        .filter(t -> externalId.equals(t.getExternalTransactionId()) && 
                                   t.getTeamNumber().equals(teamNumber) && 
                                   t.getSeason().equals(season) && t.getIsActive())
                        .sorted(Comparator.comparing(FinancialTransaction::getCreatedAt))
                        .collect(Collectors.toList());
                
                // Deactivate duplicates (keep first)
                for (int i = 1; i < transactions.size(); i++) {
                    transactions.get(i).setIsActive(false);
                    transactionRepository.save(transactions.get(i));
                }
                
                resolvedDuplicates.add("Resolved " + (count - 1) + " duplicates for external ID: " + externalId);
            }
        }
        
        return resolvedDuplicates;
    }

    // =========================================================================
    // RECONCILIATION
    // =========================================================================

    @Override
    public List<FinancialTransaction> findUnreconciledTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findUnreconciledTransactions(teamNumber, season);
    }

    @Override
    public FinancialTransaction reconcileTransaction(Long transactionId) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.reconcile();
        
        return transactionRepository.save(transaction);
    }

    @Override
    public List<FinancialTransaction> bulkReconcileTransactions(List<Long> transactionIds) {
        List<FinancialTransaction> reconciled = new ArrayList<>();
        
        for (Long id : transactionIds) {
            try {
                FinancialTransaction transaction = reconcileTransaction(id);
                reconciled.add(transaction);
            } catch (Exception e) {
                System.err.println("Failed to reconcile transaction " + id + ": " + e.getMessage());
            }
        }
        
        return reconciled;
    }

    @Override
    public Map<String, Object> generateReconciliationReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<FinancialTransaction> allTransactions = findByTeamAndSeason(teamNumber, season);
        List<FinancialTransaction> unreconciled = findUnreconciledTransactions(teamNumber, season);
        
        long reconciledCount = allTransactions.size() - unreconciled.size();
        double reconciliationRate = allTransactions.isEmpty() ? 0.0 : 
                                  (double) reconciledCount / allTransactions.size() * 100.0;
        
        report.put("totalTransactions", allTransactions.size());
        report.put("reconciledTransactions", reconciledCount);
        report.put("unreconciledTransactions", unreconciled.size());
        report.put("reconciliationRate", reconciliationRate);
        report.put("unreconciledList", unreconciled);
        
        return report;
    }

    @Override
    public boolean validateTransactionIntegrity(Long transactionId) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        return transaction.isValid();
    }

    // =========================================================================
    // RECURRING TRANSACTIONS
    // =========================================================================

    @Override
    public List<FinancialTransaction> findActiveRecurringTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findActiveRecurringTransactions(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findRecurringTransactionsDue(Integer teamNumber, Integer season, LocalDate dueDate) {
        return transactionRepository.findRecurringTransactionsDue(teamNumber, season, dueDate);
    }

    @Override
    public List<FinancialTransaction> processDueRecurringTransactions(Integer teamNumber, Integer season) {
        List<FinancialTransaction> dueTransactions = findRecurringTransactionsDue(teamNumber, season, LocalDate.now());
        List<FinancialTransaction> processedTransactions = new ArrayList<>();
        
        for (FinancialTransaction dueTransaction : dueTransactions) {
            try {
                FinancialTransaction nextRecurrence = createNextRecurrence(dueTransaction.getId());
                if (nextRecurrence != null) {
                    processedTransactions.add(nextRecurrence);
                }
            } catch (Exception e) {
                System.err.println("Failed to process recurring transaction " + dueTransaction.getId() + ": " + e.getMessage());
            }
        }
        
        return processedTransactions;
    }

    @Override
    public FinancialTransaction createNextRecurrence(Long parentTransactionId) {
        FinancialTransaction parent = transactionRepository.findById(parentTransactionId)
                .orElseThrow(() -> new RuntimeException("Parent transaction not found: " + parentTransactionId));
        
        if (!parent.getIsRecurring() || parent.getNextRecurrenceDate() == null) {
            return null;
        }
        
        // Check if end date has passed
        if (parent.getRecurrenceEndDate() != null && 
            LocalDate.now().isAfter(parent.getRecurrenceEndDate())) {
            return null;
        }
        
        FinancialTransaction nextRecurrence = parent.createNextRecurrence();
        
        if (nextRecurrence != null) {
            // Save the new recurring transaction
            FinancialTransaction saved = createTransaction(nextRecurrence);
            
            // Update parent's next recurrence date
            parent.setNextRecurrenceDate(nextRecurrence.getNextRecurrenceDate());
            transactionRepository.save(parent);
            
            return saved;
        }
        
        return null;
    }

    @Override
    public FinancialTransaction updateRecurringSchedule(Long transactionId, LocalDate nextDate, LocalDate endDate) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setNextRecurrenceDate(nextDate);
        transaction.setRecurrenceEndDate(endDate);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public List<FinancialTransaction> findChildTransactions(FinancialTransaction parentTransaction) {
        return transactionRepository.findByParentTransactionAndIsActiveTrue(parentTransaction);
    }

    // =========================================================================
    // TAX AND COMPLIANCE
    // =========================================================================

    @Override
    public List<FinancialTransaction> findTaxDeductibleTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findTaxDeductibleTransactions(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findReimbursableTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findReimbursableTransactions(teamNumber, season);
    }

    @Override
    public BigDecimal calculateTotalTaxAmount(Integer teamNumber, Integer season) {
        return transactionRepository.findTotalTaxAmount(teamNumber, season).orElse(BigDecimal.ZERO);
    }

    @Override
    public List<FinancialTransaction> findByTaxCategory(Integer teamNumber, Integer season, String taxCategory) {
        return transactionRepository.findByTaxCategory(teamNumber, season, taxCategory);
    }

    @Override
    public Map<String, Object> generateTaxDeductionReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<FinancialTransaction> taxDeductible = findTaxDeductibleTransactions(teamNumber, season);
        BigDecimal totalTaxDeductible = taxDeductible.stream()
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("taxDeductibleTransactions", taxDeductible);
        report.put("totalTaxDeductibleAmount", totalTaxDeductible);
        report.put("transactionCount", taxDeductible.size());
        
        // Group by category
        Map<FinancialTransaction.TransactionCategory, BigDecimal> byCategory = taxDeductible.stream()
                .collect(Collectors.groupingBy(
                    FinancialTransaction::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, FinancialTransaction::getAmount, BigDecimal::add)
                ));
        
        report.put("byCategory", byCategory);
        
        return report;
    }

    @Override
    public Map<String, Object> generateComplianceReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<FinancialTransaction> allTransactions = findByTeamAndSeason(teamNumber, season);
        List<FinancialTransaction> missingReceipts = findMissingRequiredReceipts(teamNumber, season);
        List<FinancialTransaction> pendingApproval = findPendingApproval(teamNumber, season);
        
        report.put("totalTransactions", allTransactions.size());
        report.put("missingReceipts", missingReceipts.size());
        report.put("pendingApproval", pendingApproval.size());
        
        // Compliance score
        double complianceScore = 100.0;
        if (!allTransactions.isEmpty()) {
            complianceScore -= (double) missingReceipts.size() / allTransactions.size() * 30.0; // 30% penalty for missing receipts
            complianceScore -= (double) pendingApproval.size() / allTransactions.size() * 20.0; // 20% penalty for pending approvals
        }
        
        report.put("complianceScore", Math.max(0.0, complianceScore));
        report.put("complianceIssues", generateComplianceIssues(missingReceipts, pendingApproval));
        
        return report;
    }

    // =========================================================================
    // PROJECT AND TASK ASSOCIATION
    // =========================================================================

    @Override
    public List<FinancialTransaction> findByProject(Project project) {
        return transactionRepository.findByProjectAndIsActiveTrue(project);
    }

    @Override
    public List<FinancialTransaction> findUnassignedTransactions(Integer teamNumber, Integer season) {
        return transactionRepository.findUnassignedTransactions(teamNumber, season);
    }

    @Override
    public FinancialTransaction assignToProject(Long transactionId, Project project) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        transaction.setProject(project);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public Map<Project, BigDecimal> calculateProjectSpendingTotals(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findProjectSpendingTotals(teamNumber, season);
        
        Map<Project, BigDecimal> projectSpending = new HashMap<>();
        
        for (Object[] row : results) {
            Project project = (Project) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            projectSpending.put(project, amount);
        }
        
        return projectSpending;
    }

    @Override
    public Map<Project, BigDecimal> analyzeTopSpendingProjects(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findTopSpendingProjects(teamNumber, season);
        
        Map<Project, BigDecimal> topProjects = new LinkedHashMap<>();
        
        for (Object[] row : results) {
            Project project = (Project) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            topProjects.put(project, amount);
        }
        
        return topProjects;
    }

    @Override
    public Map<String, Object> generateProjectFinancialReport(Project project) {
        Map<String, Object> report = new HashMap<>();
        
        List<FinancialTransaction> projectTransactions = findByProject(project);
        
        BigDecimal totalIncome = projectTransactions.stream()
                .filter(FinancialTransaction::isIncome)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = projectTransactions.stream()
                .filter(FinancialTransaction::isExpense)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("project", project);
        report.put("transactions", projectTransactions);
        report.put("totalIncome", totalIncome);
        report.put("totalExpenses", totalExpenses);
        report.put("netAmount", totalIncome.subtract(totalExpenses));
        report.put("transactionCount", projectTransactions.size());
        
        return report;
    }

    // =========================================================================
    // FINANCIAL ANALYTICS
    // =========================================================================

    @Override
    public BigDecimal calculateTotalIncome(Integer teamNumber, Integer season) {
        return transactionRepository.findTotalIncome(teamNumber, season).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateTotalExpenses(Integer teamNumber, Integer season) {
        return transactionRepository.findTotalExpenses(teamNumber, season).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateNetCashFlow(Integer teamNumber, Integer season) {
        return transactionRepository.findNetCashFlow(teamNumber, season).orElse(BigDecimal.ZERO);
    }

    @Override
    public Map<String, BigDecimal> analyzeMonthlySpendingTrends(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findMonthlySpendingTrends(teamNumber, season);
        
        Map<String, BigDecimal> trends = new LinkedHashMap<>();
        
        for (Object[] row : results) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            
            String monthKey = year + "-" + String.format("%02d", month);
            trends.put(monthKey, amount);
        }
        
        return trends;
    }

    @Override
    public Map<FinancialTransaction.TransactionCategory, BigDecimal> calculateAverageAmountsByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findAverageAmountsByCategory(teamNumber, season);
        
        Map<FinancialTransaction.TransactionCategory, BigDecimal> averages = new HashMap<>();
        
        for (Object[] row : results) {
            FinancialTransaction.TransactionCategory category = (FinancialTransaction.TransactionCategory) row[0];
            BigDecimal average = (BigDecimal) row[1];
            averages.put(category, average);
        }
        
        return averages;
    }

    @Override
    public Map<String, Object> generateFinancialSummary(Integer teamNumber, Integer season) {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal totalIncome = calculateTotalIncome(teamNumber, season);
        BigDecimal totalExpenses = calculateTotalExpenses(teamNumber, season);
        BigDecimal netCashFlow = calculateNetCashFlow(teamNumber, season);
        
        List<FinancialTransaction> allTransactions = findByTeamAndSeason(teamNumber, season);
        
        summary.put("teamNumber", teamNumber);
        summary.put("season", season);
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netCashFlow", netCashFlow);
        summary.put("transactionCount", allTransactions.size());
        
        // Category breakdowns
        summary.put("spendingByCategory", calculateSpendingByCategory(teamNumber, season));
        summary.put("incomeByCategory", calculateIncomeByCategory(teamNumber, season));
        
        // Monthly trends
        summary.put("monthlyTrends", analyzeMonthlySpendingTrends(teamNumber, season));
        
        return summary;
    }

    @Override
    public Map<String, Object> predictFutureSpending(Integer teamNumber, Integer season, Integer monthsAhead) {
        Map<String, Object> prediction = new HashMap<>();
        
        Map<String, BigDecimal> monthlyTrends = analyzeMonthlySpendingTrends(teamNumber, season);
        
        if (monthlyTrends.size() < 3) {
            prediction.put("error", "Insufficient data for prediction (need at least 3 months)");
            return prediction;
        }
        
        // Simple linear regression for prediction
        List<BigDecimal> amounts = new ArrayList<>(monthlyTrends.values());
        BigDecimal averageSpending = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        // Calculate trend (simplified)
        BigDecimal trend = BigDecimal.ZERO;
        if (amounts.size() >= 2) {
            BigDecimal recent = amounts.get(amounts.size() - 1);
            BigDecimal previous = amounts.get(amounts.size() - 2);
            trend = recent.subtract(previous);
        }
        
        BigDecimal predictedMonthlySpending = averageSpending.add(trend);
        BigDecimal predictedTotalSpending = predictedMonthlySpending.multiply(BigDecimal.valueOf(monthsAhead));
        
        prediction.put("averageMonthlySpending", averageSpending);
        prediction.put("trend", trend);
        prediction.put("predictedMonthlySpending", predictedMonthlySpending);
        prediction.put("predictedTotalSpending", predictedTotalSpending);
        prediction.put("monthsAhead", monthsAhead);
        prediction.put("confidence", calculatePredictionConfidence(amounts));
        
        return prediction;
    }

    // =========================================================================
    // PERFORMANCE ANALYSIS
    // =========================================================================

    @Override
    public Map<String, Object> analyzeApprovalPerformance(Integer teamNumber, Integer season) {
        Map<String, Object> analysis = new HashMap<>();
        
        List<Object[]> metrics = transactionRepository.findProcessingEfficiencyMetrics(teamNumber, season);
        
        if (!metrics.isEmpty()) {
            Object[] row = metrics.get(0);
            Long totalTransactions = (Long) row[0];
            Long processedTransactions = (Long) row[1];
            Long rejectedTransactions = (Long) row[2];
            Double averageApprovalTimeHours = (Double) row[3];
            
            analysis.put("totalTransactions", totalTransactions);
            analysis.put("processedTransactions", processedTransactions);
            analysis.put("rejectedTransactions", rejectedTransactions);
            analysis.put("averageApprovalTimeHours", averageApprovalTimeHours);
            
            if (totalTransactions > 0) {
                analysis.put("approvalRate", (double) processedTransactions / totalTransactions * 100.0);
                analysis.put("rejectionRate", (double) rejectedTransactions / totalTransactions * 100.0);
            }
        }
        
        return analysis;
    }

    @Override
    public Map<TeamMember, Long> findMostActiveSubmitters(Integer teamNumber, Integer season) {
        List<Object[]> results = transactionRepository.findMostActiveSubmitters(teamNumber, season);
        
        Map<TeamMember, Long> activeSubmitters = new LinkedHashMap<>();
        
        for (Object[] row : results) {
            TeamMember submitter = (TeamMember) row[0];
            Long count = (Long) row[1];
            activeSubmitters.put(submitter, count);
        }
        
        return activeSubmitters;
    }

    @Override
    public Map<String, Object> calculateProcessingEfficiencyMetrics(Integer teamNumber, Integer season) {
        return analyzeApprovalPerformance(teamNumber, season);
    }

    @Override
    public Map<String, Object> analyzeSpendingEfficiency(Integer teamNumber, Integer season) {
        Map<String, Object> efficiency = new HashMap<>();
        
        Map<FinancialTransaction.TransactionCategory, BigDecimal> spending = calculateSpendingByCategory(teamNumber, season);
        Map<FinancialTransaction.TransactionCategory, BigDecimal> averages = calculateAverageAmountsByCategory(teamNumber, season);
        
        efficiency.put("categorySpending", spending);
        efficiency.put("categoryAverages", averages);
        
        // Calculate efficiency scores
        Map<FinancialTransaction.TransactionCategory, Double> efficiencyScores = new HashMap<>();
        for (FinancialTransaction.TransactionCategory category : spending.keySet()) {
            BigDecimal totalSpent = spending.get(category);
            BigDecimal avgAmount = averages.get(category);
            
            if (avgAmount != null && avgAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Lower average transaction amount = higher efficiency (bulk purchasing)
                double score = 100.0 / (avgAmount.doubleValue() + 1.0);
                efficiencyScores.put(category, score);
            }
        }
        
        efficiency.put("efficiencyScores", efficiencyScores);
        
        return efficiency;
    }

    @Override
    public List<String> identifyCostOptimizationOpportunities(Integer teamNumber, Integer season) {
        List<String> opportunities = new ArrayList<>();
        
        // Analyze vendor consolidation opportunities
        Map<String, BigDecimal> topVendors = analyzeTopVendorsBySpending(teamNumber, season);
        if (topVendors.size() > 10) {
            opportunities.add("Consider vendor consolidation - currently using " + topVendors.size() + " vendors");
        }
        
        // Analyze payment method efficiency
        Map<FinancialTransaction.PaymentMethod, Object[]> paymentMethods = analyzeSpendingByPaymentMethod(teamNumber, season);
        long cashTransactions = paymentMethods.entrySet().stream()
                .filter(entry -> entry.getKey() == FinancialTransaction.PaymentMethod.CASH)
                .mapToLong(entry -> (Long) entry.getValue()[1])
                .sum();
        
        if (cashTransactions > 10) {
            opportunities.add("Consider reducing cash transactions for better tracking and rewards");
        }
        
        // Analyze recurring transaction automation
        List<FinancialTransaction> allTransactions = findByTeamAndSeason(teamNumber, season);
        long recurringCount = allTransactions.stream()
                .filter(FinancialTransaction::getIsRecurring)
                .count();
        
        if (recurringCount < 5) {
            opportunities.add("Consider setting up recurring transactions for regular expenses");
        }
        
        return opportunities;
    }

    // =========================================================================
    // AUDIT AND COMPLIANCE
    // =========================================================================

    @Override
    public List<FinancialTransaction> findRequiringAuditAttention(Integer teamNumber, Integer season, BigDecimal threshold) {
        return transactionRepository.findRequiringAuditAttention(teamNumber, season, threshold);
    }

    @Override
    public List<FinancialTransaction> generateAuditTrail(Integer teamNumber, Integer season) {
        return transactionRepository.findForAuditTrail(teamNumber, season);
    }

    @Override
    public List<FinancialTransaction> findModifiedInDateRange(Integer teamNumber, Integer season, 
                                                             LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findModifiedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public Map<String, Object> validateFinancialDataIntegrity(Integer teamNumber, Integer season) {
        Map<String, Object> validation = new HashMap<>();
        
        List<FinancialTransaction> allTransactions = findByTeamAndSeason(teamNumber, season);
        List<String> issues = new ArrayList<>();
        
        for (FinancialTransaction transaction : allTransactions) {
            if (!validateTransactionIntegrity(transaction.getId())) {
                issues.add("Transaction " + transaction.getId() + " has integrity issues");
            }
        }
        
        validation.put("totalTransactions", allTransactions.size());
        validation.put("integrityIssues", issues.size());
        validation.put("integrityScore", allTransactions.isEmpty() ? 100.0 : 
                      (double) (allTransactions.size() - issues.size()) / allTransactions.size() * 100.0);
        validation.put("issues", issues);
        
        return validation;
    }

    @Override
    public Map<String, Object> generateAuditReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Basic statistics
        List<FinancialTransaction> auditTrail = generateAuditTrail(teamNumber, season);
        report.put("totalTransactions", auditTrail.size());
        
        // Compliance analysis
        Map<String, Object> compliance = generateComplianceReport(teamNumber, season);
        report.put("compliance", compliance);
        
        // Data integrity
        Map<String, Object> integrity = validateFinancialDataIntegrity(teamNumber, season);
        report.put("dataIntegrity", integrity);
        
        // Reconciliation status
        Map<String, Object> reconciliation = generateReconciliationReport(teamNumber, season);
        report.put("reconciliation", reconciliation);
        
        // Audit recommendations
        List<String> recommendations = generateAuditRecommendations(teamNumber, season);
        report.put("recommendations", recommendations);
        
        return report;
    }

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<FinancialTransaction> searchTransactions(Integer teamNumber, Integer season, String searchTerm) {
        return transactionRepository.searchTransactions(teamNumber, season, searchTerm);
    }

    @Override
    public List<FinancialTransaction> findByInvoiceOrPoNumber(Integer teamNumber, Integer season, String number) {
        return transactionRepository.findByInvoiceOrPoNumber(teamNumber, season, number);
    }

    @Override
    public List<FinancialTransaction> findByGlAccount(Integer teamNumber, Integer season, String glAccount) {
        return transactionRepository.findByGlAccount(teamNumber, season, glAccount);
    }

    @Override
    public List<FinancialTransaction> findRecentTransactions(Integer teamNumber, Integer season, Integer withinDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(withinDays);
        return transactionRepository.findRecentTransactions(teamNumber, season, cutoffDate);
    }

    @Override
    public List<FinancialTransaction> findLargeTransactions(Integer teamNumber, Integer season, BigDecimal threshold) {
        return transactionRepository.findLargeTransactions(teamNumber, season, threshold);
    }

    // =========================================================================
    // REPORTING
    // =========================================================================

    @Override
    public Map<String, Object> generateFinancialReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Executive summary
        Map<String, Object> summary = generateFinancialSummary(teamNumber, season);
        report.put("summary", summary);
        
        // Detailed analysis
        report.put("categoryAnalysis", calculateSpendingByCategory(teamNumber, season));
        report.put("vendorAnalysis", analyzeTopVendorsBySpending(teamNumber, season));
        report.put("projectAnalysis", calculateProjectSpendingTotals(teamNumber, season));
        report.put("monthlyTrends", analyzeMonthlySpendingTrends(teamNumber, season));
        
        // Performance metrics
        report.put("approvalPerformance", analyzeApprovalPerformance(teamNumber, season));
        report.put("reconciliationStatus", generateReconciliationReport(teamNumber, season));
        
        // Compliance and audit
        report.put("compliance", generateComplianceReport(teamNumber, season));
        
        return report;
    }

    @Override
    public String generateExecutiveFinancialSummary(Integer teamNumber, Integer season) {
        Map<String, Object> summary = generateFinancialSummary(teamNumber, season);
        
        StringBuilder report = new StringBuilder();
        report.append("Financial Summary - Team ").append(teamNumber).append(" Season ").append(season).append("\n\n");
        
        report.append("Total Income: $").append(summary.get("totalIncome")).append("\n");
        report.append("Total Expenses: $").append(summary.get("totalExpenses")).append("\n");
        report.append("Net Cash Flow: $").append(summary.get("netCashFlow")).append("\n");
        report.append("Total Transactions: ").append(summary.get("transactionCount")).append("\n\n");
        
        // Top spending categories
        Map<FinancialTransaction.TransactionCategory, BigDecimal> categorySpending = 
                (Map<FinancialTransaction.TransactionCategory, BigDecimal>) summary.get("spendingByCategory");
        
        report.append("Top Spending Categories:\n");
        categorySpending.entrySet().stream()
                .sorted(Map.Entry.<FinancialTransaction.TransactionCategory, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> report.append("- ").append(entry.getKey().getDisplayName())
                                       .append(": $").append(entry.getValue()).append("\n"));
        
        return report.toString();
    }

    @Override
    public List<Map<String, Object>> exportTransactionData(Integer teamNumber, Integer season) {
        List<FinancialTransaction> transactions = findByTeamAndSeason(teamNumber, season);
        
        return transactions.stream().map(transaction -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", transaction.getId());
            data.put("date", transaction.getTransactionDate());
            data.put("amount", transaction.getAmount());
            data.put("type", transaction.getTransactionType().getDisplayName());
            data.put("category", transaction.getCategory().getDisplayName());
            data.put("description", transaction.getDescription());
            data.put("vendor", transaction.getVendor());
            data.put("paymentMethod", transaction.getPaymentMethod().getDisplayName());
            data.put("status", transaction.getStatus().getDisplayName());
            data.put("approved", transaction.getIsApproved());
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> generateBudgetVsActualReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<BudgetCategory> budgetCategories = budgetCategoryRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
        List<Map<String, Object>> categoryComparisons = new ArrayList<>();
        
        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (BudgetCategory category : budgetCategories) {
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("category", category.getCategoryName());
            comparison.put("budgeted", category.getAllocatedAmount());
            comparison.put("spent", category.getSpentAmount());
            comparison.put("variance", category.getAllocatedAmount().subtract(category.getSpentAmount()));
            comparison.put("utilization", category.getBudgetUtilization());
            
            categoryComparisons.add(comparison);
            
            totalBudgeted = totalBudgeted.add(category.getAllocatedAmount());
            totalSpent = totalSpent.add(category.getSpentAmount());
        }
        
        report.put("categoryComparisons", categoryComparisons);
        report.put("totalBudgeted", totalBudgeted);
        report.put("totalSpent", totalSpent);
        report.put("totalVariance", totalBudgeted.subtract(totalSpent));
        report.put("overallUtilization", totalBudgeted.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 
                  totalSpent.divide(totalBudgeted, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
        
        return report;
    }

    @Override
    public Map<String, Object> generateVendorAnalysisReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        Map<String, BigDecimal> topVendors = analyzeTopVendorsBySpending(teamNumber, season);
        report.put("topVendors", topVendors);
        report.put("vendorCount", topVendors.size());
        
        BigDecimal totalVendorSpending = topVendors.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalVendorSpending", totalVendorSpending);
        
        // Vendor concentration analysis
        if (!topVendors.isEmpty()) {
            BigDecimal top3Spending = topVendors.values().stream()
                    .limit(3)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double concentration = totalVendorSpending.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    top3Spending.divide(totalVendorSpending, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
            
            report.put("top3Concentration", concentration);
        }
        
        return report;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public List<FinancialTransaction> createBulkTransactions(List<FinancialTransaction> transactions) {
        List<String> validationErrors = validateBulkTransactions(transactions);
        
        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Validation errors: " + String.join(", ", validationErrors));
        }
        
        return transactions.stream()
                .map(this::createTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialTransaction> updateBulkTransactions(Map<Long, FinancialTransaction> transactionUpdates) {
        List<FinancialTransaction> updated = new ArrayList<>();
        
        for (Map.Entry<Long, FinancialTransaction> entry : transactionUpdates.entrySet()) {
            try {
                FinancialTransaction updatedTransaction = updateTransaction(entry.getKey(), entry.getValue());
                updated.add(updatedTransaction);
            } catch (Exception e) {
                System.err.println("Failed to update transaction " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        return updated;
    }

    @Override
    public List<String> validateBulkTransactions(List<FinancialTransaction> transactions) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < transactions.size(); i++) {
            FinancialTransaction transaction = transactions.get(i);
            
            try {
                validateTransaction(transaction);
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        return errors;
    }

    @Override
    public List<FinancialTransaction> bulkApproveTransactions(List<Long> transactionIds, TeamMember approver) {
        List<FinancialTransaction> approved = new ArrayList<>();
        
        for (Long id : transactionIds) {
            try {
                FinancialTransaction transaction = approveTransaction(id, approver, "Bulk approval");
                approved.add(transaction);
            } catch (Exception e) {
                System.err.println("Failed to approve transaction " + id + ": " + e.getMessage());
            }
        }
        
        return approved;
    }

    @Override
    public void archiveOldTransactions(Integer teamNumber, Integer season, LocalDate cutoffDate) {
        List<FinancialTransaction> oldTransactions = findByDateRange(teamNumber, season, 
                                                                    LocalDate.of(2000, 1, 1), cutoffDate);
        
        for (FinancialTransaction transaction : oldTransactions) {
            // Archive by adding note and deactivating
            if (transaction.getNotes() == null) {
                transaction.setNotes("ARCHIVED: " + LocalDate.now());
            } else {
                transaction.setNotes("ARCHIVED: " + LocalDate.now() + " | " + transaction.getNotes());
            }
            transaction.setIsActive(false);
            transactionRepository.save(transaction);
        }
    }

    // =========================================================================
    // INTEGRATION HELPERS
    // =========================================================================

    @Override
    public Map<String, Object> convertToExternalFormat(FinancialTransaction transaction, String format) {
        Map<String, Object> external = new HashMap<>();
        
        if ("QuickBooks".equalsIgnoreCase(format)) {
            external.put("Type", transaction.getTransactionType().getDisplayName());
            external.put("Date", transaction.getTransactionDate());
            external.put("Amount", transaction.getAmount());
            external.put("Description", transaction.getDescription());
            external.put("Vendor", transaction.getVendor());
            external.put("Category", transaction.getCategory().getDisplayName());
        } else if ("CSV".equalsIgnoreCase(format)) {
            external.put("Transaction_ID", transaction.getId());
            external.put("Date", transaction.getTransactionDate());
            external.put("Amount", transaction.getAmount());
            external.put("Type", transaction.getTransactionType());
            external.put("Category", transaction.getCategory());
            external.put("Description", transaction.getDescription());
            external.put("Status", transaction.getStatus());
        }
        
        return external;
    }

    @Override
    public FinancialTransaction importFromExternalFormat(Map<String, Object> data, String format) {
        FinancialTransaction transaction = new FinancialTransaction();
        
        if ("QuickBooks".equalsIgnoreCase(format)) {
            // Map QuickBooks fields to transaction
            transaction.setTransactionDate((LocalDate) data.get("Date"));
            transaction.setAmount((BigDecimal) data.get("Amount"));
            transaction.setDescription((String) data.get("Description"));
            transaction.setVendor((String) data.get("Vendor"));
            transaction.setIntegrationSource("QuickBooks");
            transaction.setIsAutomated(true);
        }
        
        return transaction;
    }

    @Override
    public FinancialTransaction syncTransactionStatus(Long transactionId, String externalSystem) {
        FinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        // Sync status with external system
        // Implementation would connect to external API
        transaction.setIntegrationSource(externalSystem);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }

    @Override
    public Map<String, Object> generateIntegrationReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<FinancialTransaction> automated = findAutomatedTransactions(teamNumber, season);
        List<FinancialTransaction> manual = findByTeamAndSeason(teamNumber, season).stream()
                .filter(t -> !t.getIsAutomated())
                .collect(Collectors.toList());
        
        report.put("automatedTransactions", automated.size());
        report.put("manualTransactions", manual.size());
        report.put("automationRate", automated.size() + manual.size() == 0 ? 0.0 :
                  (double) automated.size() / (automated.size() + manual.size()) * 100.0);
        
        // Integration sources
        Map<String, Long> sources = automated.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getIntegrationSource() != null ? t.getIntegrationSource() : "Unknown",
                    Collectors.counting()
                ));
        
        report.put("integrationSources", sources);
        
        return report;
    }

    @Override
    public boolean validateExternalIntegrationData(Integer teamNumber, Integer season) {
        List<FinancialTransaction> automated = findAutomatedTransactions(teamNumber, season);
        
        for (FinancialTransaction transaction : automated) {
            if (transaction.getExternalTransactionId() == null || 
                transaction.getIntegrationSource() == null ||
                !transaction.isValid()) {
                return false;
            }
        }
        
        return true;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private void validateTransaction(FinancialTransaction transaction) {
        if (transaction.getTeamNumber() == null) {
            throw new RuntimeException("Team number is required");
        }
        
        if (transaction.getSeason() == null) {
            throw new RuntimeException("Season is required");
        }
        
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        if (transaction.getTransactionType() == null) {
            throw new RuntimeException("Transaction type is required");
        }
        
        if (transaction.getCategory() == null) {
            throw new RuntimeException("Transaction category is required");
        }
        
        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
    }

    private boolean shouldAutoApprove(FinancialTransaction transaction) {
        // Auto-approve small amounts or specific categories
        BigDecimal autoApprovalThreshold = BigDecimal.valueOf(100.0);
        
        return transaction.getAmount().compareTo(autoApprovalThreshold) <= 0 ||
               transaction.getIsAutomated() ||
               !transaction.getRequiresApproval();
    }

    private void updateBudgetCategoryOnTransaction(FinancialTransaction transaction) {
        if (transaction.getBudgetCategory() != null && transaction.isExpense()) {
            updateBudgetCategoryTotals(transaction.getBudgetCategory());
        }
    }

    private void updateTransactionFields(FinancialTransaction existing, FinancialTransaction updated) {
        if (updated.getAmount() != null) {
            existing.setAmount(updated.getAmount());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getVendor() != null) {
            existing.setVendor(updated.getVendor());
        }
        if (updated.getTransactionDate() != null) {
            existing.setTransactionDate(updated.getTransactionDate());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getReceiptUrl() != null) {
            existing.setReceiptUrl(updated.getReceiptUrl());
        }
        if (updated.getNotes() != null) {
            existing.setNotes(updated.getNotes());
        }
    }

    private List<String> generateComplianceIssues(List<FinancialTransaction> missingReceipts,
                                                 List<FinancialTransaction> pendingApproval) {
        List<String> issues = new ArrayList<>();
        
        if (!missingReceipts.isEmpty()) {
            issues.add(missingReceipts.size() + " transactions missing required receipts");
        }
        
        if (!pendingApproval.isEmpty()) {
            issues.add(pendingApproval.size() + " transactions pending approval");
        }
        
        return issues;
    }

    private List<String> generateAuditRecommendations(Integer teamNumber, Integer season) {
        List<String> recommendations = new ArrayList<>();
        
        List<FinancialTransaction> missingReceipts = findMissingRequiredReceipts(teamNumber, season);
        if (!missingReceipts.isEmpty()) {
            recommendations.add("Collect missing receipts for " + missingReceipts.size() + " transactions");
        }
        
        List<FinancialTransaction> unreconciled = findUnreconciledTransactions(teamNumber, season);
        if (!unreconciled.isEmpty()) {
            recommendations.add("Reconcile " + unreconciled.size() + " outstanding transactions");
        }
        
        List<FinancialTransaction> pendingApproval = findPendingApproval(teamNumber, season);
        if (!pendingApproval.isEmpty()) {
            recommendations.add("Process " + pendingApproval.size() + " transactions pending approval");
        }
        
        return recommendations;
    }

    private double calculatePredictionConfidence(List<BigDecimal> amounts) {
        if (amounts.size() < 3) return 0.0;
        
        // Simple confidence based on data consistency
        double variance = calculateVariance(amounts);
        double mean = amounts.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        
        double coefficientOfVariation = mean == 0 ? 0 : Math.sqrt(variance) / mean;
        
        // Lower coefficient of variation = higher confidence
        return Math.max(0.0, Math.min(100.0, 100.0 - (coefficientOfVariation * 100.0)));
    }

    private double calculateVariance(List<BigDecimal> amounts) {
        double mean = amounts.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        
        return amounts.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .map(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
    }
}