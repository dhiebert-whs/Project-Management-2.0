// src/main/java/org/frcpm/models/FinancialTransaction.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Financial Transaction model for FRC teams.
 * 
 * Provides comprehensive financial tracking with automated integration
 * capabilities for budget management, expense tracking, and financial
 * reporting for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false)
    private LocalDate transactionDate;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(nullable = false, length = 200)
    private String description;
    
    @Column(length = 100)
    private String vendor;
    
    @Column(length = 50)
    private String invoiceNumber;
    
    @Column(length = 50)
    private String purchaseOrderNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    // Budget Tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id")
    private BudgetCategory budgetCategory;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal budgetedAmount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal remainingBudget;
    
    @Column(nullable = false)
    private Boolean isApproved = false;
    
    @Column(nullable = false)
    private Boolean isReimbursable = false;
    
    @Column(nullable = false)
    private Boolean isTaxDeductible = false;
    
    // Project and Task Association
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    // Integration and Automation
    @Column(length = 100)
    private String externalTransactionId; // ID from external system
    
    @Column(length = 50)
    private String integrationSource; // Source system (QuickBooks, etc.)
    
    @Column(nullable = false)
    private Boolean isAutomated = false; // Automatically imported
    
    @Column(nullable = false)
    private Boolean isReconciled = false;
    
    @Column
    private LocalDateTime reconciledAt;
    
    // Receipt and Documentation
    @Column(length = 500)
    private String receiptUrl;
    
    @Column(length = 500)
    private String documentationUrl;
    
    @ElementCollection
    @CollectionTable(name = "transaction_attachments", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "attachment_url")
    private List<String> attachments = new ArrayList<>();
    
    // Tax and Compliance
    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal taxRate;
    
    @Column(length = 20)
    private String taxCategory;
    
    @Column(nullable = false)
    private Boolean requiresApproval = false;
    
    @Column(nullable = false)
    private Boolean hasReceiptRequired = false;
    
    // Approval Workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private TeamMember submittedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private TeamMember approvedBy;
    
    @Column
    private LocalDateTime submittedAt;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column(length = 1000)
    private String approvalNotes;
    
    // Recurring Transaction Support
    @Column(nullable = false)
    private Boolean isRecurring = false;
    
    @Enumerated(EnumType.STRING)
    private RecurrencePattern recurrencePattern;
    
    @Column
    private LocalDate nextRecurrenceDate;
    
    @Column
    private LocalDate recurrenceEndDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transaction_id")
    private FinancialTransaction parentTransaction;
    
    // Analytics and Reporting
    @Column(length = 1000)
    private String notes;
    
    @Column(length = 100)
    private String costCenter;
    
    @Column(length = 100)
    private String glAccount; // General Ledger account
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Types of financial transactions
     */
    public enum TransactionType {
        INCOME("Income", "Money received by the team"),
        EXPENSE("Expense", "Money spent by the team"),
        TRANSFER("Transfer", "Money moved between accounts"),
        REFUND("Refund", "Money returned from previous expense"),
        REIMBURSEMENT("Reimbursement", "Money paid back to team member"),
        DONATION("Donation", "Charitable contribution received"),
        SPONSORSHIP("Sponsorship", "Sponsorship payment received"),
        GRANT("Grant", "Grant funding received"),
        ADJUSTMENT("Adjustment", "Accounting adjustment or correction");
        
        private final String displayName;
        private final String description;
        
        TransactionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Categories for financial transactions
     */
    public enum TransactionCategory {
        // Robot and Technical
        ROBOT_PARTS("Robot Parts", "Components and materials for robot"),
        TOOLS_EQUIPMENT("Tools & Equipment", "Tools and manufacturing equipment"),
        SOFTWARE_LICENSES("Software Licenses", "Software and licensing costs"),
        ELECTRONICS("Electronics", "Electronic components and sensors"),
        MANUFACTURING("Manufacturing", "Manufacturing and fabrication costs"),
        
        // Competition and Travel
        REGISTRATION_FEES("Registration Fees", "Competition and event registration"),
        TRAVEL_EXPENSES("Travel Expenses", "Transportation and lodging"),
        COMPETITION_MATERIALS("Competition Materials", "Competition-specific supplies"),
        TEAM_MEALS("Team Meals", "Food and catering expenses"),
        
        // Team Operations
        FACILITY_COSTS("Facility Costs", "Workshop rent and utilities"),
        SAFETY_EQUIPMENT("Safety Equipment", "Safety gear and equipment"),
        TRAINING_EDUCATION("Training & Education", "Training and educational materials"),
        OUTREACH_EVENTS("Outreach Events", "Community outreach expenses"),
        MARKETING_MATERIALS("Marketing Materials", "Promotional and marketing costs"),
        
        // Administrative
        INSURANCE("Insurance", "Liability and equipment insurance"),
        LEGAL_PROFESSIONAL("Legal & Professional", "Legal and professional services"),
        BANKING_FEES("Banking Fees", "Bank and financial service fees"),
        OFFICE_SUPPLIES("Office Supplies", "General office and administrative supplies"),
        
        // Income Categories
        SPONSORSHIP_INCOME("Sponsorship Income", "Corporate sponsorship payments"),
        FUNDRAISING_INCOME("Fundraising Income", "Fundraising event proceeds"),
        GRANT_INCOME("Grant Income", "Grant and award funding"),
        DONATION_INCOME("Donation Income", "Individual and organizational donations"),
        
        // Other
        CONTINGENCY("Contingency", "Emergency and unexpected expenses"),
        MISCELLANEOUS("Miscellaneous", "Other uncategorized expenses");
        
        private final String displayName;
        private final String description;
        
        TransactionCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Transaction processing status
     */
    public enum TransactionStatus {
        DRAFT("Draft", "Transaction being prepared"),
        SUBMITTED("Submitted", "Submitted for approval"),
        APPROVED("Approved", "Approved for processing"),
        PROCESSED("Processed", "Successfully processed"),
        PAID("Paid", "Payment completed"),
        REJECTED("Rejected", "Rejected and not processed"),
        CANCELLED("Cancelled", "Cancelled before processing"),
        PENDING("Pending", "Awaiting additional information"),
        RECONCILED("Reconciled", "Reconciled with bank statements");
        
        private final String displayName;
        private final String description;
        
        TransactionStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Payment methods
     */
    public enum PaymentMethod {
        CASH("Cash", "Cash payment"),
        CHECK("Check", "Check payment"),
        CREDIT_CARD("Credit Card", "Credit card payment"),
        DEBIT_CARD("Debit Card", "Debit card payment"),
        BANK_TRANSFER("Bank Transfer", "Electronic bank transfer"),
        PAYPAL("PayPal", "PayPal payment"),
        VENMO("Venmo", "Venmo payment"),
        WIRE_TRANSFER("Wire Transfer", "Wire transfer"),
        PURCHASE_ORDER("Purchase Order", "Purchase order"),
        REIMBURSEMENT_CHECK("Reimbursement Check", "Reimbursement check"),
        AUTOMATIC_PAYMENT("Automatic Payment", "Automated recurring payment"),
        OTHER("Other", "Other payment method");
        
        private final String displayName;
        private final String description;
        
        PaymentMethod(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Recurring payment patterns
     */
    public enum RecurrencePattern {
        WEEKLY("Weekly", "Every week"),
        MONTHLY("Monthly", "Every month"),
        QUARTERLY("Quarterly", "Every quarter"),
        ANNUALLY("Annually", "Every year"),
        CUSTOM("Custom", "Custom recurrence pattern");
        
        private final String displayName;
        private final String description;
        
        RecurrencePattern(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Constructors
    public FinancialTransaction() {
        this.transactionDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public FinancialTransaction(Integer teamNumber, Integer season, BigDecimal amount,
                              TransactionType transactionType, TransactionCategory category,
                              String description) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.amount = amount;
        this.transactionType = transactionType;
        this.category = category;
        this.description = description;
        this.status = TransactionStatus.DRAFT;
        this.paymentMethod = PaymentMethod.CREDIT_CARD; // Default
    }
    
    // Business Methods
    
    /**
     * Checks if transaction is an expense.
     */
    public boolean isExpense() {
        return transactionType == TransactionType.EXPENSE || 
               transactionType == TransactionType.REIMBURSEMENT;
    }
    
    /**
     * Checks if transaction is income.
     */
    public boolean isIncome() {
        return transactionType == TransactionType.INCOME ||
               transactionType == TransactionType.DONATION ||
               transactionType == TransactionType.SPONSORSHIP ||
               transactionType == TransactionType.GRANT;
    }
    
    /**
     * Gets the net amount (positive for income, negative for expenses).
     */
    public BigDecimal getNetAmount() {
        return isIncome() ? amount : amount.negate();
    }
    
    /**
     * Checks if transaction is over budget.
     */
    public boolean isOverBudget() {
        return budgetedAmount != null && 
               remainingBudget != null && 
               remainingBudget.compareTo(amount) < 0;
    }
    
    /**
     * Calculates budget utilization percentage.
     */
    public double getBudgetUtilization() {
        if (budgetedAmount == null || budgetedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal utilized = budgetedAmount.subtract(remainingBudget != null ? remainingBudget : budgetedAmount);
        return utilized.divide(budgetedAmount, 4, BigDecimal.ROUND_HALF_UP)
                      .multiply(BigDecimal.valueOf(100))
                      .doubleValue();
    }
    
    /**
     * Checks if transaction needs approval.
     */
    public boolean needsApproval() {
        return requiresApproval && !isApproved && status != TransactionStatus.APPROVED;
    }
    
    /**
     * Checks if receipt is missing but required.
     */
    public boolean isMissingRequiredReceipt() {
        return hasReceiptRequired && (receiptUrl == null || receiptUrl.trim().isEmpty());
    }
    
    /**
     * Approves the transaction.
     */
    public void approve(TeamMember approver, String notes) {
        this.isApproved = true;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
        this.status = TransactionStatus.APPROVED;
    }
    
    /**
     * Rejects the transaction.
     */
    public void reject(String reason) {
        this.status = TransactionStatus.REJECTED;
        this.approvalNotes = reason;
    }
    
    /**
     * Marks transaction as reconciled.
     */
    public void reconcile() {
        this.isReconciled = true;
        this.reconciledAt = LocalDateTime.now();
        if (this.status == TransactionStatus.PROCESSED) {
            this.status = TransactionStatus.RECONCILED;
        }
    }
    
    /**
     * Calculates total transaction amount including tax.
     */
    public BigDecimal getTotalAmount() {
        if (taxAmount != null) {
            return amount.add(taxAmount);
        }
        return amount;
    }
    
    /**
     * Checks if transaction is recent (within last 30 days).
     */
    public boolean isRecent() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return transactionDate.isAfter(thirtyDaysAgo);
    }
    
    /**
     * Gets age of transaction in days.
     */
    public int getTransactionAge() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(transactionDate, LocalDate.now());
    }
    
    /**
     * Checks if transaction is eligible for tax deduction.
     */
    public boolean isEligibleForTaxDeduction() {
        return isTaxDeductible && 
               (category == TransactionCategory.DONATION_INCOME ||
                category == TransactionCategory.OUTREACH_EVENTS ||
                category == TransactionCategory.TRAINING_EDUCATION);
    }
    
    /**
     * Creates next recurring transaction.
     */
    public FinancialTransaction createNextRecurrence() {
        if (!isRecurring || nextRecurrenceDate == null) {
            return null;
        }
        
        FinancialTransaction nextTransaction = new FinancialTransaction();
        nextTransaction.setTeamNumber(this.teamNumber);
        nextTransaction.setSeason(this.season);
        nextTransaction.setAmount(this.amount);
        nextTransaction.setTransactionType(this.transactionType);
        nextTransaction.setCategory(this.category);
        nextTransaction.setDescription(this.description + " (Recurring)");
        nextTransaction.setTransactionDate(this.nextRecurrenceDate);
        nextTransaction.setParentTransaction(this);
        nextTransaction.setPaymentMethod(this.paymentMethod);
        nextTransaction.setIsRecurring(true);
        nextTransaction.setRecurrencePattern(this.recurrencePattern);
        
        // Calculate next recurrence date
        LocalDate nextDate = calculateNextRecurrenceDate();
        nextTransaction.setNextRecurrenceDate(nextDate);
        
        return nextTransaction;
    }
    
    /**
     * Calculates next recurrence date based on pattern.
     */
    private LocalDate calculateNextRecurrenceDate() {
        if (recurrencePattern == null || nextRecurrenceDate == null) {
            return null;
        }
        
        return switch (recurrencePattern) {
            case WEEKLY -> nextRecurrenceDate.plusWeeks(1);
            case MONTHLY -> nextRecurrenceDate.plusMonths(1);
            case QUARTERLY -> nextRecurrenceDate.plusMonths(3);
            case ANNUALLY -> nextRecurrenceDate.plusYears(1);
            default -> null;
        };
    }
    
    /**
     * Generates transaction summary.
     */
    public String getTransactionSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(transactionType.getDisplayName()).append(": ");
        summary.append("$").append(amount);
        summary.append(" - ").append(description);
        
        if (vendor != null && !vendor.trim().isEmpty()) {
            summary.append(" (").append(vendor).append(")");
        }
        
        summary.append(" [").append(status.getDisplayName()).append("]");
        
        if (needsApproval()) {
            summary.append(" - NEEDS APPROVAL");
        }
        
        if (isMissingRequiredReceipt()) {
            summary.append(" - MISSING RECEIPT");
        }
        
        return summary.toString();
    }
    
    /**
     * Validates transaction data.
     */
    public boolean isValid() {
        return teamNumber != null &&
               season != null &&
               amount != null &&
               amount.compareTo(BigDecimal.ZERO) > 0 &&
               transactionType != null &&
               category != null &&
               description != null &&
               !description.trim().isEmpty();
    }
    
    /**
     * Adds attachment URL.
     */
    public void addAttachment(String attachmentUrl) {
        if (attachmentUrl != null && !attachmentUrl.trim().isEmpty()) {
            this.attachments.add(attachmentUrl);
        }
    }
    
    /**
     * Removes attachment URL.
     */
    public void removeAttachment(String attachmentUrl) {
        this.attachments.remove(attachmentUrl);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BudgetCategory getBudgetCategory() { return budgetCategory; }
    public void setBudgetCategory(BudgetCategory budgetCategory) { this.budgetCategory = budgetCategory; }
    
    public BigDecimal getBudgetedAmount() { return budgetedAmount; }
    public void setBudgetedAmount(BigDecimal budgetedAmount) { this.budgetedAmount = budgetedAmount; }
    
    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public Boolean getIsReimbursable() { return isReimbursable; }
    public void setIsReimbursable(Boolean isReimbursable) { this.isReimbursable = isReimbursable; }
    
    public Boolean getIsTaxDeductible() { return isTaxDeductible; }
    public void setIsTaxDeductible(Boolean isTaxDeductible) { this.isTaxDeductible = isTaxDeductible; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    
    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
    
    public String getIntegrationSource() { return integrationSource; }
    public void setIntegrationSource(String integrationSource) { this.integrationSource = integrationSource; }
    
    public Boolean getIsAutomated() { return isAutomated; }
    public void setIsAutomated(Boolean isAutomated) { this.isAutomated = isAutomated; }
    
    public Boolean getIsReconciled() { return isReconciled; }
    public void setIsReconciled(Boolean isReconciled) { this.isReconciled = isReconciled; }
    
    public LocalDateTime getReconciledAt() { return reconciledAt; }
    public void setReconciledAt(LocalDateTime reconciledAt) { this.reconciledAt = reconciledAt; }
    
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    
    public String getDocumentationUrl() { return documentationUrl; }
    public void setDocumentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl; }
    
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    
    public String getTaxCategory() { return taxCategory; }
    public void setTaxCategory(String taxCategory) { this.taxCategory = taxCategory; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public Boolean getHasReceiptRequired() { return hasReceiptRequired; }
    public void setHasReceiptRequired(Boolean hasReceiptRequired) { this.hasReceiptRequired = hasReceiptRequired; }
    
    public TeamMember getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(TeamMember submittedBy) { this.submittedBy = submittedBy; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getApprovalNotes() { return approvalNotes; }
    public void setApprovalNotes(String approvalNotes) { this.approvalNotes = approvalNotes; }
    
    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
    
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    
    public LocalDate getNextRecurrenceDate() { return nextRecurrenceDate; }
    public void setNextRecurrenceDate(LocalDate nextRecurrenceDate) { this.nextRecurrenceDate = nextRecurrenceDate; }
    
    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }
    
    public FinancialTransaction getParentTransaction() { return parentTransaction; }
    public void setParentTransaction(FinancialTransaction parentTransaction) { this.parentTransaction = parentTransaction; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
    
    public String getGlAccount() { return glAccount; }
    public void setGlAccount(String glAccount) { this.glAccount = glAccount; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("FinancialTransaction{id=%d, team=%d, amount=$%.2f, type=%s, category=%s, status=%s}", 
                           id, teamNumber, amount, transactionType, category, status);
    }
}