// src/main/java/org/frcpm/models/ManufacturingStep.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an individual step within a manufacturing process.
 * Tracks detailed progress and execution of manufacturing workflow steps.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Entity
@Table(name = "manufacturing_steps", indexes = {
    @Index(name = "idx_step_process", columnList = "manufacturing_process_id"),
    @Index(name = "idx_step_sequence", columnList = "sequenceNumber"),
    @Index(name = "idx_step_status", columnList = "status")
})
public class ManufacturingStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The manufacturing process this step belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_process_id", nullable = false)
    @NotNull(message = "Manufacturing process is required")
    private ManufacturingProcess manufacturingProcess;
    
    /**
     * Sequential order of this step within the process.
     */
    @Column(nullable = false)
    @NotNull(message = "Sequence number is required")
    @Min(value = 1, message = "Sequence number must be positive")
    private Integer sequenceNumber;
    
    /**
     * Name of this manufacturing step.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Step name is required")
    @Size(max = 200, message = "Step name must not exceed 200 characters")
    private String name;
    
    /**
     * Detailed instructions for this step.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Instructions must not exceed 2000 characters")
    private String instructions;
    
    /**
     * Current status of this step.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private StepStatus status = StepStatus.PENDING;
    
    /**
     * Estimated time to complete this step (in minutes).
     */
    @Min(value = 1, message = "Estimated minutes must be positive")
    private Integer estimatedMinutes;
    
    /**
     * Actual time spent on this step (in minutes).
     */
    @Min(value = 0, message = "Actual minutes cannot be negative")
    private Integer actualMinutes = 0;
    
    /**
     * When this step was started.
     */
    private LocalDateTime startedAt;
    
    /**
     * When this step was completed.
     */
    private LocalDateTime completedAt;
    
    /**
     * Team member who performed this step.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private TeamMember performedBy;
    
    /**
     * Notes about the execution of this step.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    /**
     * Whether this step requires verification before proceeding.
     */
    @Column(nullable = false)
    @NotNull(message = "Verification requirement is required")
    private Boolean requiresVerification = false;
    
    /**
     * Whether this step has been verified.
     */
    @Column(nullable = false)
    @NotNull(message = "Verification status is required")
    private Boolean isVerified = false;
    
    /**
     * Who verified this step.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private TeamMember verifiedBy;
    
    /**
     * When this step was verified.
     */
    private LocalDateTime verifiedAt;
    
    /**
     * Whether this step is currently active.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Timestamp when this step was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this step was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Enum defining status of manufacturing steps.
     */
    public enum StepStatus {
        PENDING("Pending", "Step is planned but not started", "⏳"),
        IN_PROGRESS("In Progress", "Step is currently being performed", "🔄"),
        COMPLETED("Completed", "Step has been completed", "✅"),
        SKIPPED("Skipped", "Step was skipped", "⏭️"),
        FAILED("Failed", "Step failed and needs rework", "❌"),
        BLOCKED("Blocked", "Step is blocked by dependencies", "🚫");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        StepStatus(String displayName, String description, String icon) {
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
        
        public boolean isActive() {
            return this == IN_PROGRESS;
        }
    }
    
    // Constructors
    public ManufacturingStep() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ManufacturingStep(ManufacturingProcess manufacturingProcess, Integer sequenceNumber, String name) {
        this();
        this.manufacturingProcess = manufacturingProcess;
        this.sequenceNumber = sequenceNumber;
        this.name = name;
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
     * Starts the execution of this step.
     */
    public void startStep(TeamMember performer) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Only pending steps can be started");
        }
        
        this.status = StepStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.performedBy = performer;
    }
    
    /**
     * Completes this step.
     */
    public void completeStep() {
        if (status != StepStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress steps can be completed");
        }
        
        this.status = StepStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        
        // Calculate actual time if started
        if (startedAt != null) {
            long minutes = java.time.Duration.between(startedAt, completedAt).toMinutes();
            this.actualMinutes = (int) minutes;
        }
    }
    
    /**
     * Verifies this step.
     */
    public void verifyStep(TeamMember verifier) {
        if (status != StepStatus.COMPLETED) {
            throw new IllegalStateException("Only completed steps can be verified");
        }
        
        this.isVerified = true;
        this.verifiedBy = verifier;
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * Skips this step with a reason.
     */
    public void skipStep(String reason) {
        this.status = StepStatus.SKIPPED;
        this.completedAt = LocalDateTime.now();
        
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Skipped: " + reason;
        }
    }
    
    /**
     * Marks this step as failed.
     */
    public void failStep(String reason) {
        this.status = StepStatus.FAILED;
        
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Failed: " + reason;
        }
    }
    
    /**
     * Checks if this step can be started.
     */
    public boolean canStart() {
        return status == StepStatus.PENDING && isActive;
    }
    
    /**
     * Checks if this step needs verification.
     */
    public boolean needsVerification() {
        return requiresVerification && status == StepStatus.COMPLETED && !isVerified;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ManufacturingProcess getManufacturingProcess() { return manufacturingProcess; }
    public void setManufacturingProcess(ManufacturingProcess manufacturingProcess) { this.manufacturingProcess = manufacturingProcess; }
    
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    
    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public TeamMember getPerformedBy() { return performedBy; }
    public void setPerformedBy(TeamMember performedBy) { this.performedBy = performedBy; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getRequiresVerification() { return requiresVerification; }
    public void setRequiresVerification(Boolean requiresVerification) { this.requiresVerification = requiresVerification; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public TeamMember getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(TeamMember verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManufacturingStep that = (ManufacturingStep) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ManufacturingStep{id=%d, name='%s', sequence=%d, status=%s}",
                id, name, sequenceNumber, status);
    }
}