// src/main/java/org/frcpm/models/ResourceRequirement.java
// Phase 4A: Resource Requirements for Project Templates

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Entity representing resource requirements for project templates.
 * 
 * Resource requirements define the materials, tools, and equipment
 * needed to complete a specific robot subsystem project.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
@Entity
@Table(name = "resource_requirements")
public class ResourceRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_template_id", nullable = false)
    private ProjectTemplate projectTemplate;
    
    @NotBlank(message = "Resource name is required")
    @Size(max = 100, message = "Resource name must not exceed 100 characters")
    @Column(name = "resource_name", length = 100, nullable = false)
    private String resourceName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;
    
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private int quantity = 1;
    
    @Size(max = 20)
    @Column(name = "unit", length = 20)
    private String unit; // "pieces", "feet", "pounds", "hours", etc.
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ResourcePriority priority = ResourcePriority.REQUIRED;
    
    @Column(name = "is_reusable", nullable = false)
    private boolean isReusable = false;
    
    @Column(name = "is_consumable", nullable = false)
    private boolean isConsumable = true;
    
    @Size(max = 50)
    @Column(name = "vendor", length = 50)
    private String vendor;
    
    @Size(max = 50)
    @Column(name = "part_number", length = 50)
    private String partNumber;
    
    @Size(max = 200)
    @Column(name = "vendor_url", length = 200)
    private String vendorUrl;
    
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;
    
    @Size(max = 200)
    @Column(name = "alternatives", length = 200)
    private String alternatives; // Alternative parts or suppliers
    
    @Size(max = 200)
    @Column(name = "notes", length = 200)
    private String notes;
    
    // Inventory integration
    @Column(name = "inventory_item_id")
    private Long inventoryItemId; // Link to parts inventory system
    
    @Column(name = "check_inventory", nullable = false)
    private boolean checkInventory = true; // Check team inventory before ordering
    
    // Safety and compliance
    @Column(name = "requires_safety_training", nullable = false)
    private boolean requiresSafetyTraining = false;
    
    @Size(max = 100)
    @Column(name = "safety_requirements", length = 100)
    private String safetyRequirements;
    
    // Constructors
    
    public ResourceRequirement() {
        // Default constructor required by JPA
    }
    
    public ResourceRequirement(String resourceName, ResourceType resourceType, int quantity) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProjectTemplate getProjectTemplate() {
        return projectTemplate;
    }
    
    public void setProjectTemplate(ProjectTemplate projectTemplate) {
        this.projectTemplate = projectTemplate;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
    
    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    public ResourcePriority getPriority() {
        return priority;
    }
    
    public void setPriority(ResourcePriority priority) {
        this.priority = priority;
    }
    
    public boolean isReusable() {
        return isReusable;
    }
    
    public void setReusable(boolean reusable) {
        isReusable = reusable;
    }
    
    public boolean isConsumable() {
        return isConsumable;
    }
    
    public void setConsumable(boolean consumable) {
        isConsumable = consumable;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public String getPartNumber() {
        return partNumber;
    }
    
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
    
    public String getVendorUrl() {
        return vendorUrl;
    }
    
    public void setVendorUrl(String vendorUrl) {
        this.vendorUrl = vendorUrl;
    }
    
    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }
    
    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }
    
    public String getAlternatives() {
        return alternatives;
    }
    
    public void setAlternatives(String alternatives) {
        this.alternatives = alternatives;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Long getInventoryItemId() {
        return inventoryItemId;
    }
    
    public void setInventoryItemId(Long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }
    
    public boolean isCheckInventory() {
        return checkInventory;
    }
    
    public void setCheckInventory(boolean checkInventory) {
        this.checkInventory = checkInventory;
    }
    
    public boolean isRequiresSafetyTraining() {
        return requiresSafetyTraining;
    }
    
    public void setRequiresSafetyTraining(boolean requiresSafetyTraining) {
        this.requiresSafetyTraining = requiresSafetyTraining;
    }
    
    public String getSafetyRequirements() {
        return safetyRequirements;
    }
    
    public void setSafetyRequirements(String safetyRequirements) {
        this.safetyRequirements = safetyRequirements;
    }
    
    // Helper methods
    
    /**
     * Get formatted quantity with unit.
     */
    public String getQuantityWithUnit() {
        if (unit != null && !unit.trim().isEmpty()) {
            return quantity + " " + unit;
        }
        return String.valueOf(quantity);
    }
    
    /**
     * Get formatted cost display.
     */
    public String getCostDisplay() {
        if (estimatedCost != null) {
            return "$" + estimatedCost.toString();
        }
        return "TBD";
    }
    
    /**
     * Get total cost (quantity * unit cost).
     */
    public BigDecimal getTotalCost() {
        if (estimatedCost != null) {
            return estimatedCost.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Check if this resource has a long lead time.
     */
    public boolean hasLongLeadTime() {
        return leadTimeDays != null && leadTimeDays > 7;
    }
    
    /**
     * Check if this resource is available from inventory.
     */
    public boolean isAvailableFromInventory() {
        return inventoryItemId != null && checkInventory;
    }
    
    @Override
    public String toString() {
        return resourceName + " (" + getQuantityWithUnit() + ")";
    }
    
    /**
     * Types of resources needed for robot construction.
     */
    public enum ResourceType {
        // Raw Materials
        ALUMINUM("Aluminum", "Aluminum stock and extrusions", "🔩"),
        STEEL("Steel", "Steel stock and components", "⚙️"),
        PLASTIC("Plastic", "Plastic sheets, rods, and custom parts", "🔧"),
        WOOD("Wood", "Wooden components and hardware", "🪵"),
        
        // Mechanical Components
        BEARINGS("Bearings", "Ball bearings and bushings", "⚪"),
        GEARS("Gears", "Gears, sprockets, and pulleys", "⚙️"),
        CHAINS_BELTS("Chains & Belts", "Drive chains, timing belts, etc.", "🔗"),
        FASTENERS("Fasteners", "Bolts, screws, nuts, and washers", "🔩"),
        PNEUMATIC_PARTS("Pneumatic Parts", "Cylinders, valves, and fittings", "💨"),
        
        // Electrical Components
        MOTORS("Motors", "Drive motors and actuators", "⚡"),
        SENSORS("Sensors", "Encoders, gyros, and limit switches", "📡"),
        ELECTRONICS("Electronics", "Controllers, relays, and circuits", "💻"),
        WIRING("Wiring", "Wire, connectors, and terminals", "🔌"),
        
        // Tools and Equipment
        TOOLS("Tools", "Hand tools and equipment", "🔨"),
        CONSUMABLES("Consumables", "Welding wire, cutting discs, etc.", "🧰"),
        SAFETY_EQUIPMENT("Safety Equipment", "Safety glasses, gloves, etc.", "🦺"),
        
        // Services
        MACHINING_SERVICES("Machining Services", "External machining and fabrication", "🏭"),
        CUSTOM_PARTS("Custom Parts", "3D printed or custom manufactured parts", "🖨️");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        ResourceType(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getDisplayWithIcon() {
            return icon + " " + displayName;
        }
    }
    
    /**
     * Priority levels for resource requirements.
     */
    public enum ResourcePriority {
        CRITICAL("Critical", "Must have - project cannot proceed without this", "🔴"),
        REQUIRED("Required", "Necessary for standard implementation", "🟡"),
        RECOMMENDED("Recommended", "Improves quality or performance", "🟢"),
        OPTIONAL("Optional", "Nice to have if budget allows", "🔵"),
        ALTERNATIVE("Alternative", "Alternative option if primary unavailable", "⚪");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        ResourcePriority(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getDisplayWithIcon() {
            return icon + " " + displayName;
        }
    }
}