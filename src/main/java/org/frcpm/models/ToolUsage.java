package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_usage")
@EntityListeners(AuditingEntityListener.class)
public class ToolUsage {
    
    public enum ToolType {
        HAND_TOOL("Hand Tool"),
        POWER_TOOL("Power Tool"),
        MACHINERY("Machinery"),
        MEASURING_TOOL("Measuring Tool"),
        SAFETY_EQUIPMENT("Safety Equipment"),
        COMPUTER_EQUIPMENT("Computer Equipment");
        
        private final String displayName;
        
        ToolType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        public boolean requiresSupervision() {
            return this == POWER_TOOL || this == MACHINERY;
        }
        
        public boolean requiresTraining() {
            return this == POWER_TOOL || this == MACHINERY;
        }
    }
    
    public enum UsageStatus {
        CHECKED_OUT("Checked Out"),
        IN_USE("In Use"),
        RETURNED("Returned"),
        OVERDUE("Overdue"),
        LOST("Lost"),
        DAMAGED("Damaged");
        
        private final String displayName;
        
        UsageStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        public boolean isActive() {
            return this == CHECKED_OUT || this == IN_USE;
        }
        
        public boolean isComplete() {
            return this == RETURNED || this == LOST || this == DAMAGED;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "session_id")
    private WorkshopSession session;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private TeamMember user;
    
    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private TeamMember supervisor;
    
    @NotBlank
    private String toolName;
    
    @Enumerated(EnumType.STRING)
    private ToolType toolType;
    
    @Enumerated(EnumType.STRING)
    private UsageStatus status;
    
    @NotNull
    private LocalDateTime checkoutTime;
    
    private LocalDateTime returnTime;
    private LocalDateTime expectedReturnTime;
    
    private String condition;
    private String notes;
    private String damageReport;
    
    private boolean trainingVerified = false;
    private boolean safetyCheckCompleted = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public ToolUsage() {}
    
    public ToolUsage(WorkshopSession session, TeamMember user, String toolName, ToolType toolType) {
        this.session = session;
        this.user = user;
        this.toolName = toolName;
        this.toolType = toolType;
        this.checkoutTime = LocalDateTime.now();
        this.status = UsageStatus.CHECKED_OUT;
        this.condition = "Good";
    }
    
    // Business methods
    public boolean isOverdue() {
        return expectedReturnTime != null && 
               LocalDateTime.now().isAfter(expectedReturnTime) && 
               !status.isComplete();
    }
    
    public boolean isActive() {
        return status.isActive();
    }
    
    public boolean isDamaged() {
        return status == UsageStatus.DAMAGED;
    }
    
    public boolean isLost() {
        return status == UsageStatus.LOST;
    }
    
    public long getUsageDurationMinutes() {
        LocalDateTime endTime = returnTime != null ? returnTime : LocalDateTime.now();
        return java.time.Duration.between(checkoutTime, endTime).toMinutes();
    }
    
    public boolean requiresSupervision() {
        return toolType.requiresSupervision();
    }
    
    public boolean isReadyForUse() {
        if (toolType.requiresTraining() && !trainingVerified) {
            return false;
        }
        if (toolType.requiresSupervision() && supervisor == null) {
            return false;
        }
        return safetyCheckCompleted;
    }
    
    public String getStatusClass() {
        return switch (status) {
            case CHECKED_OUT -> "text-primary";
            case IN_USE -> "text-success";
            case RETURNED -> "text-secondary";
            case OVERDUE -> "text-warning";
            case LOST -> "text-danger";
            case DAMAGED -> "text-danger";
        };
    }
    
    public String getToolTypeClass() {
        return switch (toolType) {
            case HAND_TOOL -> "badge-info";
            case POWER_TOOL -> "badge-warning";
            case MACHINERY -> "badge-danger";
            case MEASURING_TOOL -> "badge-secondary";
            case SAFETY_EQUIPMENT -> "badge-success";
            case COMPUTER_EQUIPMENT -> "badge-primary";
        };
    }
    
    public void returnTool() {
        this.returnTime = LocalDateTime.now();
        this.status = UsageStatus.RETURNED;
    }
    
    public void markDamaged(String damageDescription) {
        this.status = UsageStatus.DAMAGED;
        this.damageReport = damageDescription;
        this.returnTime = LocalDateTime.now();
    }
    
    public void markLost() {
        this.status = UsageStatus.LOST;
        this.returnTime = LocalDateTime.now();
    }
    
    public void startUsing() {
        this.status = UsageStatus.IN_USE;
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public WorkshopSession getSession() { return session; }
    public void setSession(WorkshopSession session) { this.session = session; }
    
    public TeamMember getUser() { return user; }
    public void setUser(TeamMember user) { this.user = user; }
    
    public TeamMember getSupervisor() { return supervisor; }
    public void setSupervisor(TeamMember supervisor) { this.supervisor = supervisor; }
    
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    
    public ToolType getToolType() { return toolType; }
    public void setToolType(ToolType toolType) { this.toolType = toolType; }
    
    public UsageStatus getStatus() { return status; }
    public void setStatus(UsageStatus status) { this.status = status; }
    
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
    
    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
    
    public LocalDateTime getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getDamageReport() { return damageReport; }
    public void setDamageReport(String damageReport) { this.damageReport = damageReport; }
    
    public boolean isTrainingVerified() { return trainingVerified; }
    public void setTrainingVerified(boolean trainingVerified) { this.trainingVerified = trainingVerified; }
    
    public boolean isSafetyCheckCompleted() { return safetyCheckCompleted; }
    public void setSafetyCheckCompleted(boolean safetyCheckCompleted) { this.safetyCheckCompleted = safetyCheckCompleted; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}