// src/main/java/org/frcpm/models/MilestoneTemplate.java
// Phase 4A: Milestone Templates for Project Templates

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entity representing milestone templates within project templates.
 * 
 * Milestone templates define key checkpoints and deliverables
 * for robot subsystem development projects.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
@Entity
@Table(name = "milestone_templates")
public class MilestoneTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_template_id", nullable = false)
    private ProjectTemplate projectTemplate;
    
    @NotBlank(message = "Milestone name is required")
    @Size(max = 100, message = "Milestone name must not exceed 100 characters")
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Min(0)
    @Max(20)
    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;
    
    @Min(0)
    @Column(name = "week_offset", nullable = false)
    private int weekOffset = 0; // Weeks from project start
    
    @Enumerated(EnumType.STRING)
    @Column(name = "milestone_type", nullable = false)
    private MilestoneType milestoneType = MilestoneType.DELIVERABLE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "build_phase", nullable = false)
    private TaskTemplate.BuildPhase buildPhase = TaskTemplate.BuildPhase.DESIGN;
    
    @Size(max = 500)
    @Column(name = "success_criteria", length = 500)
    private String successCriteria;
    
    @Size(max = 200)
    @Column(name = "deliverables", length = 200)
    private String deliverables;
    
    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval = false;
    
    @Size(max = 50)
    @Column(name = "approver_role", length = 50)
    private String approverRole; // "MENTOR", "LEAD", "SAFETY_CAPTAIN"
    
    @Column(name = "is_critical_path", nullable = false)
    private boolean isCriticalPath = false;
    
    @Column(name = "is_gate", nullable = false)
    private boolean isGate = false; // Blocks subsequent work if not completed
    
    @Size(max = 200)
    @Column(name = "notes", length = 200)
    private String notes;
    
    // Safety and compliance
    @Column(name = "safety_review_required", nullable = false)
    private boolean safetyReviewRequired = false;
    
    @Size(max = 200)
    @Column(name = "safety_checklist", length = 200)
    private String safetyChecklist;
    
    // Build season context
    @Column(name = "competition_critical", nullable = false)
    private boolean competitionCritical = false;
    
    @Size(max = 100)
    @Column(name = "risk_if_delayed", length = 100)
    private String riskIfDelayed;
    
    // Constructors
    
    public MilestoneTemplate() {
        // Default constructor required by JPA
    }
    
    public MilestoneTemplate(String name, ProjectTemplate projectTemplate, MilestoneType milestoneType) {
        this.name = name;
        this.projectTemplate = projectTemplate;
        this.milestoneType = milestoneType;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
    
    public int getWeekOffset() {
        return weekOffset;
    }
    
    public void setWeekOffset(int weekOffset) {
        this.weekOffset = weekOffset;
    }
    
    public MilestoneType getMilestoneType() {
        return milestoneType;
    }
    
    public void setMilestoneType(MilestoneType milestoneType) {
        this.milestoneType = milestoneType;
    }
    
    public TaskTemplate.BuildPhase getBuildPhase() {
        return buildPhase;
    }
    
    public void setBuildPhase(TaskTemplate.BuildPhase buildPhase) {
        this.buildPhase = buildPhase;
    }
    
    public String getSuccessCriteria() {
        return successCriteria;
    }
    
    public void setSuccessCriteria(String successCriteria) {
        this.successCriteria = successCriteria;
    }
    
    public String getDeliverables() {
        return deliverables;
    }
    
    public void setDeliverables(String deliverables) {
        this.deliverables = deliverables;
    }
    
    public boolean isRequiresApproval() {
        return requiresApproval;
    }
    
    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
    
    public String getApproverRole() {
        return approverRole;
    }
    
    public void setApproverRole(String approverRole) {
        this.approverRole = approverRole;
    }
    
    public boolean isCriticalPath() {
        return isCriticalPath;
    }
    
    public void setCriticalPath(boolean criticalPath) {
        isCriticalPath = criticalPath;
    }
    
    public boolean isGate() {
        return isGate;
    }
    
    public void setGate(boolean gate) {
        isGate = gate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isSafetyReviewRequired() {
        return safetyReviewRequired;
    }
    
    public void setSafetyReviewRequired(boolean safetyReviewRequired) {
        this.safetyReviewRequired = safetyReviewRequired;
    }
    
    public String getSafetyChecklist() {
        return safetyChecklist;
    }
    
    public void setSafetyChecklist(String safetyChecklist) {
        this.safetyChecklist = safetyChecklist;
    }
    
    public boolean isCompetitionCritical() {
        return competitionCritical;
    }
    
    public void setCompetitionCritical(boolean competitionCritical) {
        this.competitionCritical = competitionCritical;
    }
    
    public String getRiskIfDelayed() {
        return riskIfDelayed;
    }
    
    public void setRiskIfDelayed(String riskIfDelayed) {
        this.riskIfDelayed = riskIfDelayed;
    }
    
    // Helper methods
    
    /**
     * Get milestone display name with type icon.
     */
    public String getDisplayName() {
        return milestoneType.getIcon() + " " + name;
    }
    
    /**
     * Check if this milestone blocks other work.
     */
    public boolean isBlocking() {
        return isGate || isCriticalPath;
    }
    
    /**
     * Get urgency level based on various factors.
     */
    public String getUrgencyLevel() {
        if (competitionCritical && isCriticalPath) {
            return "CRITICAL";
        } else if (competitionCritical || isCriticalPath) {
            return "HIGH";
        } else if (isGate) {
            return "MEDIUM";
        }
        return "NORMAL";
    }
    
    @Override
    public String toString() {
        return name + " (" + milestoneType.getDisplayName() + ")";
    }
    
    /**
     * Types of milestones in robot development.
     */
    public enum MilestoneType {
        DELIVERABLE("Deliverable", "Concrete deliverable or output", "📦"),
        REVIEW("Review", "Design or progress review", "👥"),
        APPROVAL("Approval", "Formal approval checkpoint", "✅"),
        TEST("Test", "Testing or validation milestone", "🧪"),
        INTEGRATION("Integration", "System integration point", "🔗"),
        SAFETY("Safety", "Safety review or certification", "🦺"),
        COMPETITION("Competition", "Competition preparation milestone", "🏆");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        MilestoneType(String displayName, String description, String icon) {
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