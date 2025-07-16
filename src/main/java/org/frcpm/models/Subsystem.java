package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subsystems")
@EntityListeners(AuditingEntityListener.class)
public class Subsystem {
    
    public enum SubsystemStatus {
        DESIGN("Design"),
        IN_PROGRESS("In Progress"),
        TESTING("Testing"),
        COMPLETE("Complete"),
        ON_HOLD("On Hold");
        
        private final String displayName;
        
        SubsystemStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum SubsystemPriority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        SubsystemPriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;
    
    @Enumerated(EnumType.STRING)
    private SubsystemStatus status;
    
    @Enumerated(EnumType.STRING)
    private SubsystemPriority priority;
    
    private String description;
    private String requirements;
    
    @ManyToOne
    @JoinColumn(name = "responsible_member_id")
    private TeamMember responsibleMember;
    
    @OneToMany(mappedBy = "subsystem", cascade = CascadeType.ALL)
    private List<Task> tasks;
    
    private Double budgetAllocated;
    private Double budgetUsed;
    
    private String notes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Subsystem() {}
    
    public Subsystem(String name, Robot robot) {
        this.name = name;
        this.robot = robot;
        this.status = SubsystemStatus.DESIGN;
        this.priority = SubsystemPriority.MEDIUM;
    }
    
    // Business methods
    public boolean isComplete() {
        return status == SubsystemStatus.COMPLETE;
    }
    
    public boolean isInProgress() {
        return status == SubsystemStatus.IN_PROGRESS || status == SubsystemStatus.TESTING;
    }
    
    public double getCompletionPercentage() {
        return switch (status) {
            case DESIGN -> 10.0;
            case IN_PROGRESS -> 50.0;
            case TESTING -> 80.0;
            case COMPLETE -> 100.0;
            case ON_HOLD -> 25.0;
        };
    }
    
    public double getBudgetUtilizationPercentage() {
        if (budgetAllocated == null || budgetAllocated == 0) return 0.0;
        if (budgetUsed == null) return 0.0;
        return (budgetUsed / budgetAllocated) * 100.0;
    }
    
    public double getRemainingBudget() {
        if (budgetAllocated == null) return 0.0;
        if (budgetUsed == null) return budgetAllocated;
        return budgetAllocated - budgetUsed;
    }
    
    public boolean isOverBudget() {
        return budgetAllocated != null && budgetUsed != null && budgetUsed > budgetAllocated;
    }
    
    public boolean isHighPriority() {
        return priority == SubsystemPriority.HIGH || priority == SubsystemPriority.CRITICAL;
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Robot getRobot() { return robot; }
    public void setRobot(Robot robot) { this.robot = robot; }
    
    public SubsystemStatus getStatus() { return status; }
    public void setStatus(SubsystemStatus status) { this.status = status; }
    
    public SubsystemPriority getPriority() { return priority; }
    public void setPriority(SubsystemPriority priority) { this.priority = priority; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    
    public TeamMember getResponsibleMember() { return responsibleMember; }
    public void setResponsibleMember(TeamMember responsibleMember) { this.responsibleMember = responsibleMember; }
    
    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
    
    public Double getBudgetAllocated() { return budgetAllocated; }
    public void setBudgetAllocated(Double budgetAllocated) { this.budgetAllocated = budgetAllocated; }
    
    public Double getBudgetUsed() { return budgetUsed; }
    public void setBudgetUsed(Double budgetUsed) { this.budgetUsed = budgetUsed; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}