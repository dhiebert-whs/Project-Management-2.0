// src/main/java/org/frcpm/models/Part.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a physical part in the FRC team's inventory system.
 * Tracks part details, quantities, vendors, and usage across projects.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Entity
@Table(name = "parts", indexes = {
    @Index(name = "idx_part_number", columnList = "partNumber"),
    @Index(name = "idx_part_category", columnList = "category"),
    @Index(name = "idx_part_vendor", columnList = "vendor"),
    @Index(name = "idx_part_low_stock", columnList = "quantityOnHand, minimumStock")
})
public class Part {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique part number for identification and reordering.
     * Format examples: "AL-6061-1x1x12", "MOTOR-CIM", "BOLT-M5x20-SS"
     */
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Part number is required")
    @Size(max = 100, message = "Part number must not exceed 100 characters")
    private String partNumber;
    
    /**
     * Descriptive name of the part for easy identification.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Part name is required")
    @Size(max = 200, message = "Part name must not exceed 200 characters")
    private String name;
    
    /**
     * Detailed description including specifications and usage notes.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    /**
     * Category for organizing and filtering parts.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Part category is required")
    private PartCategory category;
    
    /**
     * Current quantity available in inventory.
     */
    @Column(nullable = false)
    @NotNull(message = "Quantity on hand is required")
    @Min(value = 0, message = "Quantity on hand cannot be negative")
    private Integer quantityOnHand;
    
    /**
     * Minimum stock level that triggers reorder alerts.
     */
    @Column(nullable = false)
    @NotNull(message = "Minimum stock level is required")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock;
    
    /**
     * Optimal stock level for normal operations.
     */
    @Min(value = 0, message = "Optimal stock cannot be negative")
    private Integer optimalStock;
    
    /**
     * Unit of measurement (pieces, feet, pounds, etc.).
     */
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;
    
    /**
     * Cost per unit for budgeting and purchasing decisions.
     */
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Unit cost cannot be negative")
    private BigDecimal unitCost;
    
    /**
     * Primary vendor for purchasing this part.
     */
    @Column(length = 200)
    @Size(max = 200, message = "Vendor name must not exceed 200 characters")
    private String vendor;
    
    /**
     * Vendor's part number for easy reordering.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Vendor part number must not exceed 100 characters")
    private String vendorPartNumber;
    
    /**
     * Vendor's website URL for the part.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Vendor URL must not exceed 500 characters")
    private String vendorUrl;
    
    /**
     * Physical location in the workshop or storage area.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Storage location must not exceed 100 characters")
    private String storageLocation;
    
    /**
     * Date when this part was last restocked.
     */
    private LocalDate lastRestockDate;
    
    /**
     * Date when this part was last used in a project.
     */
    private LocalDate lastUsedDate;
    
    /**
     * Whether this part is currently active in the inventory system.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Whether this part is consumable (gets used up) or reusable.
     */
    @Column(nullable = false)
    @NotNull(message = "Consumable status is required")
    private Boolean isConsumable = true;
    
    /**
     * Safety stock buffer for critical parts.
     */
    @Min(value = 0, message = "Safety stock cannot be negative")
    private Integer safetyStock = 0;
    
    /**
     * Lead time in days for reordering this part.
     */
    @Min(value = 0, message = "Lead time cannot be negative")
    private Integer leadTimeDays;
    
    /**
     * Notes about usage, handling, or special considerations.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    /**
     * Timestamp when this part record was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this part record was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * User who created this part record.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * User who last updated this part record.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;
    
    /**
     * Part transactions (inventory movements).
     */
    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartTransaction> transactions = new ArrayList<>();
    
    /**
     * Part requirements from project templates.
     */
    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartRequirement> requirements = new ArrayList<>();
    
    /**
     * Enum defining major part categories for FRC teams.
     */
    public enum PartCategory {
        // Drivetrain Components
        DRIVETRAIN("Drivetrain", "Drive motors, gearboxes, wheels, chassis", "🚗"),
        
        // Structural Materials
        STRUCTURAL("Structural", "Aluminum, steel, brackets, fasteners", "🔩"),
        
        // Electronics
        ELECTRONICS("Electronics", "Motors, sensors, controllers, wiring", "⚡"),
        
        // Pneumatics
        PNEUMATICS("Pneumatics", "Cylinders, valves, fittings, tubing", "💨"),
        
        // Game-Specific
        GAME_SPECIFIC("Game Specific", "Intake, shooter, climber mechanisms", "🎯"),
        
        // Fasteners
        FASTENERS("Fasteners", "Bolts, nuts, screws, rivets", "🔧"),
        
        // Tools & Consumables
        TOOLS("Tools", "Cutting tools, drill bits, consumables", "🔨"),
        
        // Raw Materials
        RAW_MATERIALS("Raw Materials", "Stock aluminum, plastic, wood", "📏"),
        
        // Safety
        SAFETY("Safety", "PPE, first aid, safety equipment", "🦺"),
        
        // Other
        OTHER("Other", "Miscellaneous parts not fitting other categories", "📦");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        PartCategory(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    // Constructors
    public Part() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Part(String partNumber, String name, PartCategory category, Integer quantityOnHand, 
                Integer minimumStock, String unit) {
        this();
        this.partNumber = partNumber;
        this.name = name;
        this.category = category;
        this.quantityOnHand = quantityOnHand;
        this.minimumStock = minimumStock;
        this.unit = unit;
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
     * Checks if this part is currently low on stock.
     */
    public boolean isLowStock() {
        return quantityOnHand <= minimumStock;
    }
    
    /**
     * Checks if this part is critically low (below safety stock).
     */
    public boolean isCriticallyLow() {
        return quantityOnHand <= safetyStock;
    }
    
    /**
     * Calculates the recommended reorder quantity.
     */
    public int getReorderQuantity() {
        if (optimalStock != null && quantityOnHand < optimalStock) {
            return optimalStock - quantityOnHand;
        }
        return Math.max(minimumStock * 2 - quantityOnHand, 0);
    }
    
    /**
     * Gets the total value of current inventory for this part.
     */
    public BigDecimal getInventoryValue() {
        if (unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(BigDecimal.valueOf(quantityOnHand));
    }
    
    /**
     * Checks if this part needs reordering based on stock levels and lead time.
     */
    public boolean needsReordering() {
        // Simple reorder logic - can be enhanced with demand forecasting
        return isLowStock() || (leadTimeDays != null && quantityOnHand <= minimumStock + (leadTimeDays / 7));
    }
    
    /**
     * Updates the quantity on hand (used for transactions).
     */
    public void updateQuantity(int change, String reason) {
        int newQuantity = quantityOnHand + change;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cannot reduce quantity below zero");
        }
        quantityOnHand = newQuantity;
        lastUsedDate = LocalDate.now();
    }
    
    /**
     * Restocks this part to the optimal level.
     */
    public void restock(int quantity) {
        quantityOnHand += quantity;
        lastRestockDate = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public PartCategory getCategory() { return category; }
    public void setCategory(PartCategory category) { this.category = category; }
    
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    
    public Integer getOptimalStock() { return optimalStock; }
    public void setOptimalStock(Integer optimalStock) { this.optimalStock = optimalStock; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public String getVendorPartNumber() { return vendorPartNumber; }
    public void setVendorPartNumber(String vendorPartNumber) { this.vendorPartNumber = vendorPartNumber; }
    
    public String getVendorUrl() { return vendorUrl; }
    public void setVendorUrl(String vendorUrl) { this.vendorUrl = vendorUrl; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public LocalDate getLastRestockDate() { return lastRestockDate; }
    public void setLastRestockDate(LocalDate lastRestockDate) { this.lastRestockDate = lastRestockDate; }
    
    public LocalDate getLastUsedDate() { return lastUsedDate; }
    public void setLastUsedDate(LocalDate lastUsedDate) { this.lastUsedDate = lastUsedDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsConsumable() { return isConsumable; }
    public void setIsConsumable(Boolean isConsumable) { this.isConsumable = isConsumable; }
    
    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }
    
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public List<PartTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<PartTransaction> transactions) { this.transactions = transactions; }
    
    public List<PartRequirement> getRequirements() { return requirements; }
    public void setRequirements(List<PartRequirement> requirements) { this.requirements = requirements; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Part part = (Part) o;
        return Objects.equals(partNumber, part.partNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partNumber);
    }
    
    @Override
    public String toString() {
        return String.format("Part{id=%d, partNumber='%s', name='%s', category=%s, quantity=%d/%d}",
                id, partNumber, name, category, quantityOnHand, minimumStock);
    }
}