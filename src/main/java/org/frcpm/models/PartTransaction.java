// src/main/java/org/frcpm/models/PartTransaction.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a transaction that changes part inventory levels.
 * Tracks all movements (in/out) of parts for audit and inventory management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Entity
@Table(name = "part_transactions", indexes = {
    @Index(name = "idx_transaction_part", columnList = "part_id"),
    @Index(name = "idx_transaction_date", columnList = "transactionDate"),
    @Index(name = "idx_transaction_type", columnList = "transactionType"),
    @Index(name = "idx_transaction_project", columnList = "project_id")
})
public class PartTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The part involved in this transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    @NotNull(message = "Part is required for transaction")
    private Part part;
    
    /**
     * Type of transaction (purchase, usage, adjustment, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    /**
     * Quantity change (positive for incoming, negative for outgoing).
     */
    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    
    /**
     * Unit cost for this transaction (may differ from part's standard cost).
     */
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Unit cost cannot be negative")
    private BigDecimal unitCost;
    
    /**
     * Total cost/value of this transaction.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    /**
     * Reason or description for this transaction.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    /**
     * Reference number (PO number, invoice number, etc.).
     */
    @Column(length = 100)
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;
    
    /**
     * Vendor involved in this transaction (for purchases).
     */
    @Column(length = 200)
    @Size(max = 200, message = "Vendor must not exceed 200 characters")
    private String vendor;
    
    /**
     * Project associated with this transaction (for usage tracking).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    
    /**
     * Task associated with this transaction (for detailed usage tracking).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    /**
     * Team member who performed this transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id")
    private TeamMember performedBy;
    
    /**
     * When this transaction occurred.
     */
    @Column(nullable = false)
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;
    
    /**
     * Quantity on hand after this transaction.
     */
    @Column(nullable = false)
    @NotNull(message = "Balance after transaction is required")
    @Min(value = 0, message = "Balance cannot be negative")
    private Integer balanceAfter;
    
    /**
     * Notes about this transaction.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    /**
     * Whether this transaction has been approved (for high-value items).
     */
    @Column(nullable = false)
    private Boolean isApproved = true;
    
    /**
     * Who approved this transaction.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Approved by must not exceed 100 characters")
    private String approvedBy;
    
    /**
     * When this transaction was approved.
     */
    private LocalDateTime approvedAt;
    
    /**
     * Timestamp when this transaction record was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * User who created this transaction record.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * Enum defining types of part transactions.
     */
    public enum TransactionType {
        // Incoming Transactions
        PURCHASE("Purchase", "Parts purchased from vendor", "📦", 1),
        DONATION("Donation", "Parts donated to the team", "🎁", 1),
        RETURN("Return", "Parts returned from project/task", "↩️", 1),
        FOUND("Found", "Parts discovered during inventory", "🔍", 1),
        
        // Outgoing Transactions  
        USAGE("Usage", "Parts used in project/task", "🔧", -1),
        DAMAGED("Damaged", "Parts damaged and removed", "💥", -1),
        LOST("Lost", "Parts lost or misplaced", "❓", -1),
        DISPOSED("Disposed", "Parts disposed of properly", "🗑️", -1),
        
        // Adjustments
        ADJUSTMENT_POSITIVE("Positive Adjustment", "Inventory count correction (up)", "⬆️", 1),
        ADJUSTMENT_NEGATIVE("Negative Adjustment", "Inventory count correction (down)", "⬇️", -1),
        INITIAL_STOCK("Initial Stock", "Starting inventory entry", "🏁", 1),
        
        // Transfers
        TRANSFER_IN("Transfer In", "Parts transferred from another location", "📨", 1),
        TRANSFER_OUT("Transfer Out", "Parts transferred to another location", "📤", -1);
        
        private final String displayName;
        private final String description;
        private final String icon;
        private final int quantityMultiplier; // 1 for positive, -1 for negative
        
        TransactionType(String displayName, String description, String icon, int quantityMultiplier) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.quantityMultiplier = quantityMultiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getQuantityMultiplier() { return quantityMultiplier; }
        
        public boolean isIncoming() { return quantityMultiplier > 0; }
        public boolean isOutgoing() { return quantityMultiplier < 0; }
    }
    
    // Constructors
    public PartTransaction() {
        this.transactionDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public PartTransaction(Part part, TransactionType transactionType, Integer quantity, String reason) {
        this();
        this.part = part;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.reason = reason;
    }
    
    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        
        // Calculate total cost if unit cost is provided
        if (unitCost != null && quantity != null) {
            totalCost = unitCost.multiply(BigDecimal.valueOf(Math.abs(quantity)));
        }
    }
    
    // Business Logic Methods
    
    /**
     * Gets the effective quantity change for inventory calculations.
     */
    public int getEffectiveQuantityChange() {
        return quantity * transactionType.getQuantityMultiplier();
    }
    
    /**
     * Checks if this transaction requires approval.
     */
    public boolean requiresApproval() {
        // High-value transactions or large quantities might require approval
        if (totalCost != null && totalCost.compareTo(new BigDecimal("500.00")) > 0) {
            return true;
        }
        if (Math.abs(quantity) > 100) {
            return true;
        }
        return false;
    }
    
    /**
     * Approves this transaction.
     */
    public void approve(String approvedBy) {
        this.isApproved = true;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }
    
    /**
     * Gets a formatted description of this transaction.
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(transactionType.getIcon()).append(" ");
        sb.append(transactionType.getDisplayName()).append(": ");
        sb.append(Math.abs(quantity)).append(" ").append(part.getUnit());
        
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(" - ").append(reason);
        }
        
        if (project != null) {
            sb.append(" (Project: ").append(project.getName()).append(")");
        }
        
        return sb.toString();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    
    public TeamMember getPerformedBy() { return performedBy; }
    public void setPerformedBy(TeamMember performedBy) { this.performedBy = performedBy; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartTransaction that = (PartTransaction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("PartTransaction{id=%d, part=%s, type=%s, quantity=%d, date=%s}",
                id, part != null ? part.getPartNumber() : "null", transactionType, quantity, transactionDate);
    }
}