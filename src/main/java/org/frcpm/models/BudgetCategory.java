// src/main/java/org/frcpm/models/BudgetCategory.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Budget Category model for FRC team financial management.
 * 
 * Defines budget categories with allocated amounts, spending limits,
 * and approval requirements for comprehensive financial control.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.5 Automated Financial Tracking Integration
 */
@Entity
@Table(name = "budget_categories")
public class BudgetCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false, length = 100)
    private String categoryName;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal committedAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableAmount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal approvalThreshold;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean requiresApproval = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "budgetCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FinancialTransaction> transactions = new ArrayList<>();
    
    // Constructors
    public BudgetCategory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BudgetCategory(Integer teamNumber, Integer season, String categoryName, BigDecimal allocatedAmount) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.categoryName = categoryName;
        this.allocatedAmount = allocatedAmount;
        this.availableAmount = allocatedAmount;
    }
    
    // Business Methods
    public void updateAvailableAmount() {
        this.availableAmount = allocatedAmount.subtract(spentAmount).subtract(committedAmount);
    }
    
    public boolean isOverBudget() {
        return spentAmount.compareTo(allocatedAmount) > 0;
    }
    
    public double getBudgetUtilization() {
        if (allocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(allocatedAmount, 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100))
                         .doubleValue();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateAvailableAmount();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    
    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }
    
    public BigDecimal getCommittedAmount() { return committedAmount; }
    public void setCommittedAmount(BigDecimal committedAmount) { this.committedAmount = committedAmount; }
    
    public BigDecimal getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(BigDecimal availableAmount) { this.availableAmount = availableAmount; }
    
    public BigDecimal getApprovalThreshold() { return approvalThreshold; }
    public void setApprovalThreshold(BigDecimal approvalThreshold) { this.approvalThreshold = approvalThreshold; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<FinancialTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<FinancialTransaction> transactions) { this.transactions = transactions; }
}