// src/main/java/org/frcpm/models/ManufacturingProcess.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a manufacturing process for FRC robot components.
 * Tracks workflow steps, quality checks, and production progress.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Entity
@Table(name = "manufacturing_processes", indexes = {
    @Index(name = "idx_process_task", columnList = "task_id"),
    @Index(name = "idx_process_status", columnList = "status"),
    @Index(name = "idx_process_type", columnList = "processType"),
    @Index(name = "idx_process_priority", columnList = "priority")
})
public class ManufacturingProcess {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The task this manufacturing process belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @NotNull(message = "Task is required")
    private Task task;
    
    /**
     * The project this manufacturing process belongs to.
     * Derived from the task's project for easier querying.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull(message = "Project is required")
    private Project project;
    
    /**
     * Name of the manufacturing process.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Process name is required")
    @Size(max = 200, message = "Process name must not exceed 200 characters")
    private String name;
    
    /**
     * Detailed description of the manufacturing process.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    /**
     * Type of manufacturing process.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Process type is required")
    private ProcessType processType;
    
    /**
     * Current status of the manufacturing process.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private ProcessStatus status = ProcessStatus.PLANNED;
    
    /**
     * Priority level of this process.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Priority is required")
    private ProcessPriority priority = ProcessPriority.MEDIUM;
    
    /**
     * Estimated duration in hours.
     */
    @DecimalMin(value = "0.1", message = "Estimated hours must be positive")
    private Double estimatedHours;
    
    /**
     * Actual hours spent on this process.
     */
    @DecimalMin(value = "0.0", message = "Actual hours cannot be negative")
    private Double actualHours = 0.0;
    
    /**
     * Planned start date/time.
     */
    private LocalDateTime plannedStartTime;
    
    /**
     * Planned completion date/time.
     */
    private LocalDateTime plannedEndTime;
    
    /**
     * Target completion date/time.
     */
    private LocalDateTime targetCompletionDate;
    
    /**
     * Actual start date/time.
     */
    private LocalDateTime actualStartTime;
    
    /**
     * Actual completion date/time.
     */
    private LocalDateTime actualEndTime;
    
    /**
     * Team member assigned to this process.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private TeamMember assignedTo;
    
    /**
     * Team member who is supervising this process.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private TeamMember supervisor;
    
    /**
     * Required skill level for this process.
     */
    @Enumerated(EnumType.STRING)
    private SkillLevel requiredSkillLevel;
    
    /**
     * Tools and equipment required for this process.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Required tools must not exceed 1000 characters")
    private String requiredTools;
    
    /**
     * Safety considerations and requirements.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Safety notes must not exceed 1000 characters")
    private String safetyNotes;
    
    /**
     * Quality standards and acceptance criteria.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Quality standards must not exceed 1000 characters")
    private String qualityStandards;
    
    /**
     * Work instructions or procedure reference.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Work instructions must not exceed 500 characters")
    private String workInstructions;
    
    /**
     * Progress percentage (0-100).
     */
    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    private Integer progressPercentage = 0;
    
    /**
     * Notes about the manufacturing process.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
    
    /**
     * Notes about process completion.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Completion notes must not exceed 1000 characters")
    private String completionNotes;
    
    /**
     * Requirements for this manufacturing process.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Requirements must not exceed 2000 characters")
    private String requirements;
    
    /**
     * Materials required for this process.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Materials required must not exceed 1000 characters")
    private String materialsRequired;
    
    /**
     * Whether this process has quality issues.
     */
    @Column(nullable = false)
    @NotNull(message = "Quality issues status is required")
    private Boolean hasQualityIssues = false;
    
    /**
     * Details about quality issues.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Quality issues must not exceed 2000 characters")
    private String qualityIssues;
    
    /**
     * Team member who approved this process.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private TeamMember approvedBy;
    
    /**
     * Team member who rejected this process.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private TeamMember rejectedBy;
    
    /**
     * Notes about approval or rejection.
     */
    @Column(length = 1000)
    @Size(max = 1000, message = "Approval notes must not exceed 1000 characters")
    private String approvalNotes;
    
    /**
     * Whether this process requires quality inspection.
     */
    @Column(nullable = false)
    @NotNull(message = "Quality inspection requirement is required")
    private Boolean requiresQualityInspection = false;
    
    /**
     * Whether this process requires approval before proceeding.
     */
    @Column(nullable = false)
    @NotNull(message = "Requires approval status is required")
    private Boolean requiresApproval = false;
    
    /**
     * Whether this process has been approved.
     */
    @Column(nullable = false)
    @NotNull(message = "Approval status is required")  
    private Boolean isApproved = false;
    
    /**
     * When this process was approved.
     */
    @Column
    private LocalDateTime approvedAt;
    
    /**
     * Whether this process is currently blocked.
     */
    @Column(nullable = false)
    @NotNull(message = "Blocked status is required")
    private Boolean isBlocked = false;
    
    /**
     * Reason why this process is blocked (if applicable).
     */
    @Column(length = 500)
    @Size(max = 500, message = "Blocked reason must not exceed 500 characters")
    private String blockedReason;
    
    /**
     * Whether this process is currently active.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Timestamp when this process was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this process was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * User who created this process.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * User who last updated this process.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;
    
    /**
     * Manufacturing steps within this process.
     */
    @OneToMany(mappedBy = "manufacturingProcess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    private List<ManufacturingStep> steps = new ArrayList<>();
    
    /**
     * Quality checkpoints for this process.
     */
    @OneToMany(mappedBy = "manufacturingProcess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QualityCheckpoint> qualityCheckpoints = new ArrayList<>();
    
    /**
     * Parts required for this manufacturing process.
     */
    @ManyToMany
    @JoinTable(
        name = "process_parts",
        joinColumns = @JoinColumn(name = "process_id"),
        inverseJoinColumns = @JoinColumn(name = "part_id")
    )
    private List<Part> requiredParts = new ArrayList<>();
    
    /**
     * Enum defining types of manufacturing processes for FRC teams.
     */
    public enum ProcessType {
        // Mechanical Processes
        MACHINING("Machining", "CNC, lathe, mill operations", "⚙️"),
        CUTTING("Cutting", "Saw, plasma, laser cutting", "✂️"),
        DRILLING("Drilling", "Hole drilling and tapping", "🔩"),
        WELDING("Welding", "MIG, TIG, stick welding", "🔥"),
        ASSEMBLY("Assembly", "Mechanical assembly and fastening", "🔧"),
        
        // Fabrication
        BENDING("Bending", "Sheet metal and tube bending", "📐"),
        FORMING("Forming", "Metal forming and shaping", "🔨"),
        FINISHING("Finishing", "Sanding, grinding, polishing", "✨"),
        PAINTING("Painting", "Coating and surface treatment", "🎨"),
        
        // Electronics
        WIRING("Wiring", "Electrical connections and harnesses", "⚡"),
        SOLDERING("Soldering", "Electronic component soldering", "🔌"),
        PROGRAMMING("Programming", "Robot code and configuration", "💻"),
        
        // Quality and Testing
        INSPECTION("Inspection", "Quality control and verification", "🔍"),
        TESTING("Testing", "Functional and performance testing", "🧪"),
        CALIBRATION("Calibration", "Sensor and system calibration", "⚖️"),
        
        // Other
        PROTOTYPING("Prototyping", "Rapid prototyping and iteration", "🚀"),
        DOCUMENTATION("Documentation", "Process and design documentation", "📋"),
        OTHER("Other", "Custom or specialized processes", "📦");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        ProcessType(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    /**
     * Enum defining status of manufacturing processes.
     */
    public enum ProcessStatus {
        PLANNED("Planned", "Process is planned but not started", "📅", 0),
        READY("Ready", "Process is ready to start", "✅", 1),
        IN_PROGRESS("In Progress", "Process is currently being worked on", "🔄", 2),
        PAUSED("Paused", "Process is temporarily paused", "⏸️", 3),
        BLOCKED("Blocked", "Process is blocked by dependencies", "🚫", 4),
        REVIEW("Under Review", "Process completed, awaiting review", "👀", 5),
        REWORK("Rework Required", "Process needs to be redone", "🔄", 6),
        FAILED("Failed", "Process has failed and cannot be completed", "❌", 7),
        ON_HOLD("On Hold", "Process is temporarily on hold", "⏸️", 8),
        COMPLETED("Completed", "Process successfully completed", "✅", 9),
        CANCELLED("Cancelled", "Process has been cancelled", "❌", 10);
        
        private final String displayName;
        private final String description;
        private final String icon;
        private final int sortOrder;
        
        ProcessStatus(String displayName, String description, String icon, int sortOrder) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.sortOrder = sortOrder;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getSortOrder() { return sortOrder; }
        
        public boolean isActive() {
            return this == IN_PROGRESS || this == PAUSED || this == REVIEW;
        }
        
        public boolean isCompleted() {
            return this == COMPLETED;
        }
        
        public boolean isBlocked() {
            return this == BLOCKED;
        }
    }
    
    /**
     * Enum defining priority levels for manufacturing processes.
     */
    public enum ProcessPriority {
        CRITICAL("Critical", "Must be completed immediately", "🔴", 1),
        HIGH("High", "High priority, complete soon", "🟠", 2),
        MEDIUM("Medium", "Normal priority", "🟡", 3),
        LOW("Low", "Low priority, complete when possible", "🟢", 4);
        
        private final String displayName;
        private final String description;
        private final String icon;
        private final int sortOrder;
        
        ProcessPriority(String displayName, String description, String icon, int sortOrder) {
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
     * Enum defining skill levels required for processes.
     */
    public enum SkillLevel {
        BEGINNER("Beginner", "Basic skills, suitable for new members", "🌱"),
        INTERMEDIATE("Intermediate", "Some experience required", "🌿"),
        ADVANCED("Advanced", "Significant experience required", "🌳"),
        EXPERT("Expert", "Mentor-level skills required", "🎓");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        SkillLevel(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    // Constructors
    public ManufacturingProcess() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ManufacturingProcess(Task task, String name, ProcessType processType) {
        this();
        this.task = task;
        this.name = name;
        this.processType = processType;
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
     * Starts the manufacturing process.
     */
    public void startProcess(TeamMember assignedMember) {
        if (status != ProcessStatus.READY && status != ProcessStatus.PLANNED) {
            throw new IllegalStateException("Process must be ready or planned to start");
        }
        
        this.status = ProcessStatus.IN_PROGRESS;
        this.actualStartTime = LocalDateTime.now();
        this.assignedTo = assignedMember;
    }
    
    /**
     * Completes the manufacturing process.
     */
    public void completeProcess() {
        if (status != ProcessStatus.IN_PROGRESS && status != ProcessStatus.REVIEW) {
            throw new IllegalStateException("Process must be in progress or under review to complete");
        }
        
        this.status = ProcessStatus.COMPLETED;
        this.actualEndTime = LocalDateTime.now();
        this.progressPercentage = 100;
    }
    
    /**
     * Pauses the manufacturing process.
     */
    public void pauseProcess(String reason) {
        if (status != ProcessStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress processes can be paused");
        }
        
        this.status = ProcessStatus.PAUSED;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Paused: " + reason;
        }
    }
    
    /**
     * Resumes a paused process.
     */
    public void resumeProcess() {
        if (status != ProcessStatus.PAUSED) {
            throw new IllegalStateException("Only paused processes can be resumed");
        }
        
        this.status = ProcessStatus.IN_PROGRESS;
    }
    
    /**
     * Blocks the process with a reason.
     */
    public void blockProcess(String reason) {
        this.status = ProcessStatus.BLOCKED;
        this.isBlocked = true;
        this.blockedReason = reason;
    }
    
    /**
     * Unblocks the process.
     */
    public void unblockProcess() {
        if (status == ProcessStatus.BLOCKED) {
            this.status = ProcessStatus.READY;
        }
        this.isBlocked = false;
        this.blockedReason = null;
    }
    
    /**
     * Calculates the duration of the process if completed.
     */
    public Double getActualDurationHours() {
        if (actualStartTime != null && actualEndTime != null) {
            return (double) java.time.Duration.between(actualStartTime, actualEndTime).toMinutes() / 60.0;
        }
        return null;
    }
    
    /**
     * Checks if the process is overdue.
     */
    public boolean isOverdue() {
        if (plannedEndTime == null || status == ProcessStatus.COMPLETED) {
            return false;
        }
        return LocalDateTime.now().isAfter(plannedEndTime);
    }
    
    /**
     * Gets the number of completed steps.
     */
    public int getCompletedStepsCount() {
        return (int) steps.stream()
            .filter(step -> step.getStatus() == ManufacturingStep.StepStatus.COMPLETED)
            .count();
    }
    
    /**
     * Updates progress based on completed steps.
     */
    public void updateProgressFromSteps() {
        if (steps.isEmpty()) {
            return;
        }
        
        int completedSteps = getCompletedStepsCount();
        this.progressPercentage = (completedSteps * 100) / steps.size();
        
        // Update status based on progress
        if (progressPercentage == 100 && status == ProcessStatus.IN_PROGRESS) {
            this.status = ProcessStatus.REVIEW;
        }
    }
    
    /**
     * Cancels the process with a reason.
     */
    public void cancelProcess(String reason) {
        this.status = ProcessStatus.CANCELLED;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Cancelled: " + reason;
        }
    }
    
    /**
     * Puts the process on hold.
     */
    public void holdProcess(String reason) {
        this.status = ProcessStatus.ON_HOLD;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "On Hold: " + reason;
        }
    }
    
    /**
     * Releases the process from hold.
     */
    public void releaseFromHold() {
        if (status == ProcessStatus.ON_HOLD) {
            this.status = ProcessStatus.READY;
        }
    }
    
    /**
     * Records quality issues for this process.
     */
    public void recordQualityIssues(String issues, String correctiveActions) {
        this.hasQualityIssues = true;
        this.qualityIssues = issues;
        if (correctiveActions != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Corrective Actions: " + correctiveActions;
        }
    }
    
    /**
     * Resolves quality issues.
     */
    public void resolveQualityIssues(String resolutionNotes) {
        this.hasQualityIssues = false;
        this.qualityIssues = null;
        if (resolutionNotes != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Quality Issues Resolved: " + resolutionNotes;
        }
    }
    
    /**
     * Approves the process.
     */
    public void approveProcess(TeamMember approvedBy, String approvalNotes) {
        this.approvedBy = approvedBy;
        this.approvalNotes = approvalNotes;
        if (status == ProcessStatus.REVIEW) {
            this.status = ProcessStatus.COMPLETED;
        }
    }
    
    /**
     * Rejects the process.
     */
    public void rejectProcess(TeamMember rejectedBy, String rejectionReason) {
        this.rejectedBy = rejectedBy;
        this.approvalNotes = rejectionReason;
        this.status = ProcessStatus.REWORK;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Task getTask() { return task; }
    public void setTask(Task task) { 
        this.task = task; 
        // Auto-set project from task
        if (task != null && task.getProject() != null) {
            this.project = task.getProject();
        }
    }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ProcessType getProcessType() { return processType; }
    public void setProcessType(ProcessType processType) { this.processType = processType; }
    
    public ProcessStatus getStatus() { return status; }
    public void setStatus(ProcessStatus status) { this.status = status; }
    
    public ProcessPriority getPriority() { return priority; }
    public void setPriority(ProcessPriority priority) { this.priority = priority; }
    
    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
    
    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
    
    public LocalDateTime getPlannedStartTime() { return plannedStartTime; }
    public void setPlannedStartTime(LocalDateTime plannedStartTime) { this.plannedStartTime = plannedStartTime; }
    
    public LocalDateTime getPlannedEndTime() { return plannedEndTime; }
    public void setPlannedEndTime(LocalDateTime plannedEndTime) { this.plannedEndTime = plannedEndTime; }
    
    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }
    
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
    
    public TeamMember getAssignedTo() { return assignedTo; }
    public void setAssignedTo(TeamMember assignedTo) { this.assignedTo = assignedTo; }
    
    public TeamMember getSupervisor() { return supervisor; }
    public void setSupervisor(TeamMember supervisor) { this.supervisor = supervisor; }
    
    public SkillLevel getRequiredSkillLevel() { return requiredSkillLevel; }
    public void setRequiredSkillLevel(SkillLevel requiredSkillLevel) { this.requiredSkillLevel = requiredSkillLevel; }
    
    public String getRequiredTools() { return requiredTools; }
    public void setRequiredTools(String requiredTools) { this.requiredTools = requiredTools; }
    
    public String getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(String safetyNotes) { this.safetyNotes = safetyNotes; }
    
    public String getQualityStandards() { return qualityStandards; }
    public void setQualityStandards(String qualityStandards) { this.qualityStandards = qualityStandards; }
    
    public String getWorkInstructions() { return workInstructions; }
    public void setWorkInstructions(String workInstructions) { this.workInstructions = workInstructions; }
    
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getRequiresQualityInspection() { return requiresQualityInspection; }
    public void setRequiresQualityInspection(Boolean requiresQualityInspection) { this.requiresQualityInspection = requiresQualityInspection; }
    
    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
    
    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
    
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
    
    public List<ManufacturingStep> getSteps() { return steps; }
    public void setSteps(List<ManufacturingStep> steps) { this.steps = steps; }
    
    public List<QualityCheckpoint> getQualityCheckpoints() { return qualityCheckpoints; }
    public void setQualityCheckpoints(List<QualityCheckpoint> qualityCheckpoints) { this.qualityCheckpoints = qualityCheckpoints; }
    
    public List<Part> getRequiredParts() { return requiredParts; }
    public void setRequiredParts(List<Part> requiredParts) { this.requiredParts = requiredParts; }
    
    public LocalDateTime getTargetCompletionDate() { return targetCompletionDate; }
    public void setTargetCompletionDate(LocalDateTime targetCompletionDate) { this.targetCompletionDate = targetCompletionDate; }
    
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    
    public String getMaterialsRequired() { return materialsRequired; }
    public void setMaterialsRequired(String materialsRequired) { this.materialsRequired = materialsRequired; }
    
    public Boolean getHasQualityIssues() { return hasQualityIssues; }
    public void setHasQualityIssues(Boolean hasQualityIssues) { this.hasQualityIssues = hasQualityIssues; }
    
    public String getQualityIssues() { return qualityIssues; }
    public void setQualityIssues(String qualityIssues) { this.qualityIssues = qualityIssues; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public TeamMember getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(TeamMember rejectedBy) { this.rejectedBy = rejectedBy; }
    
    public String getApprovalNotes() { return approvalNotes; }
    public void setApprovalNotes(String approvalNotes) { this.approvalNotes = approvalNotes; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManufacturingProcess that = (ManufacturingProcess) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ManufacturingProcess{id=%d, name='%s', type=%s, status=%s}",
                id, name, processType, status);
    }
}