package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a task in the FRC Project Management System.
 * This corresponds to the Task model in the Django application.
 */
@Entity
@Table(name = "tasks")
public class Task {
    
    public enum Priority {
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        CRITICAL(4, "Critical");
        
        private final int value;
        private final String displayName;
        
        Priority(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static Priority fromValue(int value) {
            for (Priority priority : values()) {
                if (priority.getValue() == value) {
                    return priority;
                }
            }
            return MEDIUM; // Default
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "estimated_duration_seconds", nullable = false)
    private long estimatedDurationSeconds;
    
    @Column(name = "actual_duration_seconds")
    private Long actualDurationSeconds;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Min(0)
    @Max(100)
    @Column(name = "progress", nullable = false)
    private int progress = 0;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "completed", nullable = false)
    private boolean completed = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsystem_id", nullable = false)
    private Subsystem subsystem;
    
    // Task dependencies are now managed through the TaskDependency entity
    // instead of direct Many-to-Many relationships
    
    @ManyToMany
    @JoinTable(
        name = "task_components",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private Set<Component> requiredComponents = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "task_assignments",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "team_member_id")
    )
    private Set<TeamMember> assignedTo = new HashSet<>();
    
    // Constructors
    
    public Task() {
        // Default constructor required by JPA
    }
    
    public Task(String title, Project project, Subsystem subsystem) {
        this.title = title;
        this.project = project;
        this.subsystem = subsystem;
        this.estimatedDurationSeconds = Duration.ofHours(1).getSeconds(); // Default 1 hour
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getEstimatedDuration() {
        return Duration.ofSeconds(estimatedDurationSeconds);
    }

    public void setEstimatedDuration(Duration duration) {
        this.estimatedDurationSeconds = duration.getSeconds();
    }

    public Duration getActualDuration() {
        return actualDurationSeconds != null ? Duration.ofSeconds(actualDurationSeconds) : null;
    }

    public void setActualDuration(Duration duration) {
        this.actualDurationSeconds = duration != null ? duration.getSeconds() : null;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.min(100, Math.max(0, progress));
        // If progress is 100%, mark task as completed
        if (this.progress == 100) {
            this.completed = true;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        // If marking as completed, set progress to 100%
        if (completed) {
            this.progress = 100;
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(Subsystem subsystem) {
        this.subsystem = subsystem;
    }

    // Task dependencies are now managed through TaskDependencyService
    // instead of direct entity relationships

    public Set<Component> getRequiredComponents() {
        return requiredComponents;
    }

    public void setRequiredComponents(Set<Component> requiredComponents) {
        this.requiredComponents = requiredComponents;
    }

    public Set<TeamMember> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Set<TeamMember> assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    // Helper methods
        
    public void addRequiredComponent(Component component) {
        requiredComponents.add(component);
        component.getRequiredForTasks().add(this);
    }
    
    public void removeRequiredComponent(Component component) {
        requiredComponents.remove(component);
        component.getRequiredForTasks().remove(this);
    }
    

    
    @Override
    public String toString() {
        return title;
    }

    // Task dependency management is now handled through the TaskDependencyService
    // and TaskDependency entity instead of direct task-to-task relationships

    /**
     * Assigns a team member to this task.
     * 
     * @param member the team member to assign
     */
    public void assignMember(TeamMember member) {
        if (member == null) return;
        
        // Add to this task's assigned members
        if (!this.assignedTo.contains(member)) {
            this.assignedTo.add(member);
        }
        
        // Add this task to member's assigned tasks
        if (!member.getAssignedTasks().contains(this)) {
            member.getAssignedTasks().add(this);
        }
    }

    /**
     * Unassigns a team member from this task.
     * 
     * @param member the team member to unassign
     */
    public void unassignMember(TeamMember member) {
        if (member == null) return;
        
        // Remove from this task's assigned members
        this.assignedTo.remove(member);
        
        // Remove this task from member's assigned tasks
        member.getAssignedTasks().remove(this);
    }
}
