// src/main/java/org/frcpm/models/QualityCheckpoint.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a quality checkpoint within a manufacturing process.
 * Tracks quality control and inspection requirements and results.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Entity
@Table(name = "quality_checkpoints", indexes = {
    @Index(name = "idx_checkpoint_process", columnList = "manufacturing_process_id"),
    @Index(name = "idx_checkpoint_status", columnList = "status"),
    @Index(name = "idx_checkpoint_type", columnList = "checkpointType")
})
public class QualityCheckpoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The manufacturing process this checkpoint belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_process_id", nullable = false)
    @NotNull(message = "Manufacturing process is required")
    private ManufacturingProcess manufacturingProcess;
    
    /**
     * Name of this quality checkpoint.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Checkpoint name is required")
    @Size(max = 200, message = "Checkpoint name must not exceed 200 characters")
    private String name;
    
    /**
     * Type of quality checkpoint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Checkpoint type is required")
    private CheckpointType checkpointType;
    
    /**
     * Detailed description of what needs to be checked.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    /**
     * Acceptance criteria for this checkpoint.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Acceptance criteria must not exceed 1000 characters")
    private String acceptanceCriteria;
    
    /**
     * Current status of this checkpoint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private CheckpointStatus status = CheckpointStatus.PENDING;
    
    /**
     * Result of the quality check.
     */
    @Enumerated(EnumType.STRING)
    private CheckpointResult result;
    
    /**
     * Priority level of this checkpoint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Priority is required")
    private CheckpointPriority priority = CheckpointPriority.MEDIUM;
    
    /**
     * Team member who performed the inspection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private TeamMember inspector;
    
    /**
     * When the inspection was performed.
     */
    private LocalDateTime inspectedAt;
    
    /**
     * Measurements or observations from the inspection.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Inspection notes must not exceed 2000 characters")
    private String inspectionNotes;
    
    /**
     * Issues found during inspection.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Issues found must not exceed 1000 characters")
    private String issuesFound;
    
    /**
     * Corrective actions taken.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Corrective actions must not exceed 1000 characters")
    private String correctiveActions;
    
    /**
     * Tools or equipment required for this inspection.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Required tools must not exceed 500 characters")
    private String requiredTools;
    
    /**
     * Tolerance specifications (if applicable).
     */
    @Column(length = 500)
    @Size(max = 500, message = "Tolerance specs must not exceed 500 characters")
    private String toleranceSpecs;
    
    /**
     * Whether this checkpoint is mandatory.
     */
    @Column(nullable = false)
    @NotNull(message = "Mandatory status is required")
    private Boolean isMandatory = true;
    
    /**
     * Whether this checkpoint blocks process completion.
     */
    @Column(nullable = false)
    @NotNull(message = "Blocking status is required")
    private Boolean isBlocking = true;
    
    /**
     * Whether this checkpoint is currently active.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Timestamp when this checkpoint was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this checkpoint was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * User who created this checkpoint.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * User who last updated this checkpoint.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;
    
    /**
     * Enum defining types of quality checkpoints.
     */
    public enum CheckpointType {
        DIMENSIONAL("Dimensional", "Size and dimension verification", "📏"),
        VISUAL("Visual", "Visual inspection for defects", "👁️"),
        FUNCTIONAL("Functional", "Functional testing and operation", "⚙️"),
        MATERIAL("Material", "Material quality and properties", "🔬"),
        ASSEMBLY("Assembly", "Assembly and fit verification", "🔧"),
        SAFETY("Safety", "Safety compliance check", "🦺"),
        FINISH("Finish", "Surface finish and appearance", "✨"),
        CALIBRATION("Calibration", "Calibration and accuracy check", "⚖️"),
        DOCUMENTATION("Documentation", "Documentation completeness", "📋"),
        PERFORMANCE("Performance", "Performance and specification test", "🚀"),
        OTHER("Other", "Other quality checks", "📦");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        CheckpointType(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    /**
     * Enum defining status of quality checkpoints.
     */
    public enum CheckpointStatus {
        PENDING("Pending", "Checkpoint not yet performed", "⏳"),
        IN_PROGRESS("In Progress", "Checkpoint currently being performed", "🔄"),
        COMPLETED("Completed", "Checkpoint has been completed", "✅"),
        SKIPPED("Skipped", "Checkpoint was skipped", "⏭️"),
        DEFERRED("Deferred", "Checkpoint deferred to later", "📅");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        CheckpointStatus(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        
        public boolean isCompleted() {
            return this == COMPLETED;
        }
    }
    
    /**
     * Enum defining results of quality checkpoints.
     */
    public enum CheckpointResult {
        PASS("Pass", "Checkpoint passed all criteria", "✅"),
        FAIL("Fail", "Checkpoint failed criteria", "❌"),
        CONDITIONAL_PASS("Conditional Pass", "Pass with minor issues noted", "⚠️"),
        NEEDS_REWORK("Needs Rework", "Requires rework before acceptance", "🔄"),
        INCONCLUSIVE("Inconclusive", "Results inconclusive, needs re-inspection", "❓");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        CheckpointResult(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        
        public boolean isPassing() {
            return this == PASS || this == CONDITIONAL_PASS;
        }
        
        public boolean requiresAction() {
            return this == FAIL || this == NEEDS_REWORK || this == INCONCLUSIVE;
        }
    }
    
    /**
     * Enum defining priority levels for quality checkpoints.
     */
    public enum CheckpointPriority {
        CRITICAL("Critical", "Critical quality checkpoint", "🔴"),
        HIGH("High", "High priority checkpoint", "🟠"),
        MEDIUM("Medium", "Medium priority checkpoint", "🟡"),
        LOW("Low", "Low priority checkpoint", "🟢");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        CheckpointPriority(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    // Constructors
    public QualityCheckpoint() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public QualityCheckpoint(ManufacturingProcess manufacturingProcess, String name, CheckpointType checkpointType) {
        this();
        this.manufacturingProcess = manufacturingProcess;
        this.name = name;
        this.checkpointType = checkpointType;
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
     * Starts the inspection for this checkpoint.
     */
    public void startInspection(TeamMember inspector) {
        if (status != CheckpointStatus.PENDING) {
            throw new IllegalStateException("Only pending checkpoints can be started");
        }
        
        this.status = CheckpointStatus.IN_PROGRESS;
        this.inspector = inspector;
    }
    
    /**
     * Completes the inspection with a result.
     */
    public void completeInspection(CheckpointResult result, String notes) {
        if (status != CheckpointStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress checkpoints can be completed");
        }
        
        this.status = CheckpointStatus.COMPLETED;
        this.result = result;
        this.inspectedAt = LocalDateTime.now();
        this.inspectionNotes = notes;
    }
    
    /**
     * Records issues found during inspection.
     */
    public void recordIssues(String issues, String correctiveActions) {
        this.issuesFound = issues;
        this.correctiveActions = correctiveActions;
        
        if (result == null) {
            this.result = CheckpointResult.NEEDS_REWORK;
        }
    }
    
    /**
     * Skips this checkpoint with a reason.
     */
    public void skipCheckpoint(String reason) {
        this.status = CheckpointStatus.SKIPPED;
        
        if (reason != null) {
            this.inspectionNotes = (this.inspectionNotes != null ? this.inspectionNotes + "\n" : "") + 
                                   "Skipped: " + reason;
        }
    }
    
    /**
     * Defers this checkpoint to later.
     */
    public void deferCheckpoint(String reason) {
        this.status = CheckpointStatus.DEFERRED;
        
        if (reason != null) {
            this.inspectionNotes = (this.inspectionNotes != null ? this.inspectionNotes + "\n" : "") + 
                                   "Deferred: " + reason;
        }
    }
    
    /**
     * Checks if this checkpoint blocks process completion.
     */
    public boolean blocksCompletion() {
        if (!isBlocking) {
            return false;
        }
        
        // Blocks if mandatory and not completed with passing result
        if (isMandatory && status != CheckpointStatus.COMPLETED) {
            return true;
        }
        
        if (status == CheckpointStatus.COMPLETED && result != null && result.requiresAction()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if this checkpoint is ready for inspection.
     */
    public boolean isReadyForInspection() {
        return status == CheckpointStatus.PENDING && isActive;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ManufacturingProcess getManufacturingProcess() { return manufacturingProcess; }
    public void setManufacturingProcess(ManufacturingProcess manufacturingProcess) { this.manufacturingProcess = manufacturingProcess; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public CheckpointType getCheckpointType() { return checkpointType; }
    public void setCheckpointType(CheckpointType checkpointType) { this.checkpointType = checkpointType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    
    public CheckpointStatus getStatus() { return status; }
    public void setStatus(CheckpointStatus status) { this.status = status; }
    
    public CheckpointResult getResult() { return result; }
    public void setResult(CheckpointResult result) { this.result = result; }
    
    public CheckpointPriority getPriority() { return priority; }
    public void setPriority(CheckpointPriority priority) { this.priority = priority; }
    
    public TeamMember getInspector() { return inspector; }
    public void setInspector(TeamMember inspector) { this.inspector = inspector; }
    
    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
    
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    
    public String getIssuesFound() { return issuesFound; }
    public void setIssuesFound(String issuesFound) { this.issuesFound = issuesFound; }
    
    public String getCorrectiveActions() { return correctiveActions; }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions = correctiveActions; }
    
    public String getRequiredTools() { return requiredTools; }
    public void setRequiredTools(String requiredTools) { this.requiredTools = requiredTools; }
    
    public String getToleranceSpecs() { return toleranceSpecs; }
    public void setToleranceSpecs(String toleranceSpecs) { this.toleranceSpecs = toleranceSpecs; }
    
    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; }
    
    public Boolean getIsBlocking() { return isBlocking; }
    public void setIsBlocking(Boolean isBlocking) { this.isBlocking = isBlocking; }
    
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
        QualityCheckpoint that = (QualityCheckpoint) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("QualityCheckpoint{id=%d, name='%s', type=%s, status=%s, result=%s}",
                id, name, checkpointType, status, result);
    }
}