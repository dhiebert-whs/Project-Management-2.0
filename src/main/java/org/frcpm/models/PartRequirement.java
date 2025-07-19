// src/main/java/org/frcpm/models/PartRequirement.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a part requirement for project templates and tasks.
 * Links project templates to specific parts with quantities and specifications.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Entity
@Table(name = "part_requirements", indexes = {
    @Index(name = "idx_requirement_template", columnList = "project_template_id"),
    @Index(name = "idx_requirement_part", columnList = "part_id"),
    @Index(name = "idx_requirement_priority", columnList = "priority"),
    @Index(name = "idx_requirement_critical", columnList = "isCritical")
})
public class PartRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The project template that requires this part.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_template_id")
    private ProjectTemplate projectTemplate;
    
    /**
     * The task template that requires this part (if more specific than project level).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_template_id")
    private TaskTemplate taskTemplate;
    
    /**
     * The part that is required.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    @NotNull(message = "Part is required")
    private Part part;
    
    /**
     * Quantity of this part required.
     */
    @Column(nullable = false)
    @NotNull(message = "Quantity required is required")
    @Min(value = 1, message = "Quantity required must be at least 1")
    private Integer quantityRequired;
    
    /**
     * Optional minimum quantity (for ranges like "2-4 wheels").
     */
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minimumQuantity;
    
    /**
     * Optional maximum quantity for flexible requirements.
     */
    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maximumQuantity;
    
    /**
     * Priority of this part requirement.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Priority is required")
    private RequirementPriority priority;
    
    /**
     * Whether this part is critical for the project's success.
     */
    @Column(nullable = false)
    @NotNull(message = "Critical status is required")
    private Boolean isCritical = false;
    
    /**
     * Whether this part is optional or required.
     */
    @Column(nullable = false)
    @NotNull(message = "Optional status is required")
    private Boolean isOptional = false;
    
    /**
     * Specific specifications or requirements for this part.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Specifications must not exceed 1000 characters")
    private String specifications;
    
    /**
     * Alternatives or substitutes for this part.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Alternatives must not exceed 500 characters")
    private String alternatives;
    
    /**
     * Usage notes specific to this requirement.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Usage notes must not exceed 1000 characters")
    private String usageNotes;
    
    /**
     * Build phase when this part is typically needed.
     */
    @Enumerated(EnumType.STRING)
    private BuildPhase buildPhase;
    
    /**
     * Estimated cost per unit for this requirement (may differ from standard part cost).
     */
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Estimated cost cannot be negative")
    private BigDecimal estimatedCostPerUnit;
    
    /**
     * Lead time in days for acquiring this part.
     */
    @Min(value = 0, message = "Lead time cannot be negative")
    private Integer leadTimeDays;
    
    /**
     * Whether this part is typically reusable across projects.
     */
    @Column(nullable = false)
    private Boolean isReusable = true;
    
    /**
     * Vendor preference for this specific requirement.
     */
    @Column(length = 200)
    @Size(max = 200, message = "Preferred vendor must not exceed 200 characters")
    private String preferredVendor;
    
    /**
     * Safety considerations for handling this part.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Safety notes must not exceed 500 characters")
    private String safetyNotes;
    
    /**
     * Whether this requirement is currently active.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Timestamp when this requirement was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this requirement was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * User who created this requirement.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * User who last updated this requirement.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;
    
    /**
     * Enum defining priority levels for part requirements.
     */
    public enum RequirementPriority {
        CRITICAL("Critical", "Must have for project success", "🔴", 1),
        HIGH("High", "Important for optimal performance", "🟠", 2),
        MEDIUM("Medium", "Beneficial but not essential", "🟡", 3),
        LOW("Low", "Nice to have", "🟢", 4),
        OPTIONAL("Optional", "Enhancement only", "🔵", 5);
        
        private final String displayName;
        private final String description;
        private final String icon;
        private final int sortOrder;
        
        RequirementPriority(String displayName, String description, String icon, int sortOrder) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.sortOrder = sortOrder;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getSortOrder() { return sortOrder; }
    }
    
    /**
     * Enum defining build phases for FRC robot development.
     */
    public enum BuildPhase {
        DESIGN("Design", "Initial design and prototyping phase", "📐", 1),
        FABRICATION("Fabrication", "Manufacturing and assembly phase", "⚙️", 2),
        TESTING("Testing", "Testing and iteration phase", "🧪", 3),
        INTEGRATION("Integration", "System integration phase", "🔗", 4),
        COMPETITION("Competition", "Competition and maintenance phase", "🏆", 5),
        ANY("Any Phase", "Can be used in any phase", "🔄", 0);
        
        private final String displayName;
        private final String description;
        private final String icon;
        private final int sortOrder;
        
        BuildPhase(String displayName, String description, String icon, int sortOrder) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.sortOrder = sortOrder;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getSortOrder() { return sortOrder; }
    }
    
    // Constructors
    public PartRequirement() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PartRequirement(Part part, Integer quantityRequired, RequirementPriority priority) {
        this();
        this.part = part;
        this.quantityRequired = quantityRequired;
        this.priority = priority;
    }
    
    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    
    /**
     * Gets the total estimated cost for this requirement.
     */
    public BigDecimal getTotalEstimatedCost() {
        BigDecimal costPerUnit = estimatedCostPerUnit != null ? estimatedCostPerUnit : 
                                 (part.getUnitCost() != null ? part.getUnitCost() : BigDecimal.ZERO);
        return costPerUnit.multiply(BigDecimal.valueOf(quantityRequired));
    }
    
    /**
     * Checks if the current inventory is sufficient for this requirement.
     */
    public boolean isInventorySufficient() {
        return part.getQuantityOnHand() >= quantityRequired;
    }
    
    /**
     * Calculates how many additional units are needed.
     */
    public int getAdditionalUnitsNeeded() {
        return Math.max(0, quantityRequired - part.getQuantityOnHand());
    }
    
    /**
     * Checks if this requirement is flexible (has min/max range).
     */
    public boolean isFlexibleQuantity() {
        return minimumQuantity != null && maximumQuantity != null && 
               !minimumQuantity.equals(maximumQuantity);
    }
    
    /**
     * Gets the effective minimum quantity (considering flexibility).
     */
    public int getEffectiveMinimumQuantity() {
        return minimumQuantity != null ? minimumQuantity : quantityRequired;
    }
    
    /**
     * Gets the effective maximum quantity (considering flexibility).
     */
    public int getEffectiveMaximumQuantity() {
        return maximumQuantity != null ? maximumQuantity : quantityRequired;
    }
    
    /**
     * Checks if this requirement can be fulfilled with available inventory.
     */
    public boolean canBeFulfilled() {
        if (isOptional) {
            return true; // Optional requirements can always be "fulfilled"
        }
        return part.getQuantityOnHand() >= getEffectiveMinimumQuantity();
    }
    
    /**
     * Gets a formatted description of this requirement.
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        
        // Priority indicator
        sb.append(priority.getIcon()).append(" ");
        
        // Quantity
        if (isFlexibleQuantity()) {
            sb.append(minimumQuantity).append("-").append(maximumQuantity);
        } else {
            sb.append(quantityRequired);
        }
        sb.append(" ").append(part.getUnit()).append(" ");
        
        // Part name
        sb.append(part.getName());
        
        // Critical/Optional indicators
        if (isCritical) {
            sb.append(" (CRITICAL)");
        } else if (isOptional) {
            sb.append(" (Optional)");
        }
        
        // Build phase
        if (buildPhase != null && buildPhase != BuildPhase.ANY) {
            sb.append(" [").append(buildPhase.getDisplayName()).append("]");
        }
        
        return sb.toString();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProjectTemplate getProjectTemplate() { return projectTemplate; }
    public void setProjectTemplate(ProjectTemplate projectTemplate) { this.projectTemplate = projectTemplate; }
    
    public TaskTemplate getTaskTemplate() { return taskTemplate; }
    public void setTaskTemplate(TaskTemplate taskTemplate) { this.taskTemplate = taskTemplate; }
    
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    
    public Integer getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(Integer quantityRequired) { this.quantityRequired = quantityRequired; }
    
    public Integer getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(Integer minimumQuantity) { this.minimumQuantity = minimumQuantity; }
    
    public Integer getMaximumQuantity() { return maximumQuantity; }
    public void setMaximumQuantity(Integer maximumQuantity) { this.maximumQuantity = maximumQuantity; }
    
    public RequirementPriority getPriority() { return priority; }
    public void setPriority(RequirementPriority priority) { this.priority = priority; }
    
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    
    public Boolean getIsOptional() { return isOptional; }
    public void setIsOptional(Boolean isOptional) { this.isOptional = isOptional; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public String getAlternatives() { return alternatives; }
    public void setAlternatives(String alternatives) { this.alternatives = alternatives; }
    
    public String getUsageNotes() { return usageNotes; }
    public void setUsageNotes(String usageNotes) { this.usageNotes = usageNotes; }
    
    public BuildPhase getBuildPhase() { return buildPhase; }
    public void setBuildPhase(BuildPhase buildPhase) { this.buildPhase = buildPhase; }
    
    public BigDecimal getEstimatedCostPerUnit() { return estimatedCostPerUnit; }
    public void setEstimatedCostPerUnit(BigDecimal estimatedCostPerUnit) { this.estimatedCostPerUnit = estimatedCostPerUnit; }
    
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    
    public Boolean getIsReusable() { return isReusable; }
    public void setIsReusable(Boolean isReusable) { this.isReusable = isReusable; }
    
    public String getPreferredVendor() { return preferredVendor; }
    public void setPreferredVendor(String preferredVendor) { this.preferredVendor = preferredVendor; }
    
    public String getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(String safetyNotes) { this.safetyNotes = safetyNotes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartRequirement that = (PartRequirement) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("PartRequirement{id=%d, part=%s, quantity=%d, priority=%s}",
                id, part != null ? part.getPartNumber() : "null", quantityRequired, priority);
    }
}