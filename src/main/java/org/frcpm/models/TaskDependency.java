// src/main/java/org/frcpm/models/TaskDependency.java
package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a dependency relationship between two tasks.
 * 
 * This model supports complex project scheduling for FRC build seasons,
 * including critical path analysis and dependency cycle detection.
 * 
 * Key Features:
 * - Multiple dependency types (Finish-to-Start, Start-to-Start, etc.)
 * - Lag time support for scheduling flexibility
 * - Bidirectional relationship maintenance
 * - Critical path analysis support
 * - Cycle detection capabilities
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-Phase2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
@Entity
@Table(name = "task_dependencies", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"dependent_task_id", "prerequisite_task_id"}),
       indexes = {
           @Index(name = "idx_dependent_task", columnList = "dependent_task_id"),
           @Index(name = "idx_prerequisite_task", columnList = "prerequisite_task_id"),
           @Index(name = "idx_dependency_type", columnList = "dependency_type"),
           @Index(name = "idx_critical_path", columnList = "is_critical_path")
       })
@EntityListeners(AuditingEntityListener.class)
public class TaskDependency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The task that depends on another task (successor).
     * This task cannot proceed according to the dependency rules until prerequisite is satisfied.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_task_id", nullable = false)
    @NotNull(message = "Dependent task is required")
    private Task dependentTask;
    
    /**
     * The task that must be completed first (predecessor).
     * This task's completion state affects the dependent task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_task_id", nullable = false)
    @NotNull(message = "Prerequisite task is required")
    private Task prerequisiteTask;
    
    /**
     * The type of dependency relationship.
     * Determines how the prerequisite affects the dependent task.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    @NotNull(message = "Dependency type is required")
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;
    
    /**
     * Lag time in hours between prerequisite completion and dependent task availability.
     * Positive values create delays, negative values create lead time.
     * Default is 0 (no lag).
     */
    @Column(name = "lag_hours")
    private Integer lagHours = 0;
    
    /**
     * Whether this dependency is part of the critical path.
     * Calculated during critical path analysis.
     */
    @Column(name = "is_critical_path")
    private boolean criticalPath = false;
    
    /**
     * Whether this dependency is currently active.
     * Inactive dependencies are ignored in scheduling calculations.
     */
    @Column(name = "is_active")
    private boolean active = true;
    
    /**
     * Optional notes about this dependency relationship.
     * Useful for explaining why the dependency exists.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    /**
     * The project both tasks belong to (for validation).
     * Cached for performance in cross-project dependency checks.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public TaskDependency() {
        // Default constructor required by JPA
    }
    
    public TaskDependency(Task dependentTask, Task prerequisiteTask, DependencyType dependencyType) {
        this.dependentTask = dependentTask;
        this.prerequisiteTask = prerequisiteTask;
        this.dependencyType = dependencyType;
        
        // Set project from tasks (they should be in the same project)
        if (dependentTask != null && prerequisiteTask != null) {
            if (!Objects.equals(dependentTask.getProject().getId(), prerequisiteTask.getProject().getId())) {
                throw new IllegalArgumentException("Tasks must be in the same project for dependency");
            }
            this.project = dependentTask.getProject();
        }
    }
    
    public TaskDependency(Task dependentTask, Task prerequisiteTask) {
        this(dependentTask, prerequisiteTask, DependencyType.FINISH_TO_START);
    }
    
    // Business Logic Methods
    
    /**
     * Check if this dependency is satisfied based on current task states.
     * 
     * @return true if the dependency constraint is satisfied
     */
    public boolean isSatisfied() {
        if (!active || prerequisiteTask == null || dependentTask == null) {
            return true;
        }
        
        switch (dependencyType) {
            case FINISH_TO_START:
            case BLOCKING:
                return prerequisiteTask.isCompleted();
                
            case START_TO_START:
                return prerequisiteTask.getProgress() > 0 || prerequisiteTask.isCompleted();
                
            case FINISH_TO_FINISH:
                // For FF, we check if prerequisite is completed when dependent needs to finish
                // Since we can't predict the future, we assume it's satisfied if prerequisite is complete
                return prerequisiteTask.isCompleted();
                
            case START_TO_FINISH:
                // SF is complex - prerequisite must start before dependent can finish
                return prerequisiteTask.getProgress() > 0 || prerequisiteTask.isCompleted();
                
            case SOFT:
                // Soft dependencies are always "satisfied" - they're just recommendations
                return true;
                
            default:
                return true;
        }
    }
    
    /**
     * Check if this dependency is blocking the dependent task from starting.
     * 
     * @return true if the dependent task is blocked by this dependency
     */
    public boolean isBlocking() {
        if (!active || dependencyType == DependencyType.SOFT) {
            return false;
        }
        
        return !isSatisfied();
    }
    
    /**
     * Get the earliest start date for the dependent task based on this dependency.
     * 
     * @return LocalDateTime when dependent task can start, or null if no constraint
     */
    public LocalDateTime getEarliestDependentStart() {
        if (!active || prerequisiteTask == null) {
            return null;
        }
        
        LocalDateTime baseTime = null;
        
        switch (dependencyType) {
            case FINISH_TO_START:
            case BLOCKING:
                // Dependent can start when prerequisite finishes
                baseTime = prerequisiteTask.getEndDate() != null ? 
                    prerequisiteTask.getEndDate().atStartOfDay() : null;
                break;
                
            case START_TO_START:
                // Dependent can start when prerequisite starts
                baseTime = prerequisiteTask.getStartDate() != null ? 
                    prerequisiteTask.getStartDate().atStartOfDay() : null;
                break;
                
            case FINISH_TO_FINISH:
            case START_TO_FINISH:
                // These are more complex and depend on dependent task duration
                // For now, return null (no start constraint)
                return null;
                
            case SOFT:
                // Soft dependencies don't constrain start times
                return null;
        }
        
        // Apply lag time if specified
        if (baseTime != null && lagHours != null && lagHours != 0) {
            baseTime = baseTime.plusHours(lagHours);
        }
        
        return baseTime;
    }
    
    /**
     * Check if this dependency could create a circular reference.
     * 
     * @return true if adding this dependency would create a cycle
     */
    public boolean wouldCreateCycle() {
        if (dependentTask == null || prerequisiteTask == null) {
            return false;
        }
        
        // A cycle exists if the prerequisite task depends (directly or indirectly) on the dependent task
        return hasTransitiveDependency(prerequisiteTask, dependentTask);
    }
    
    /**
     * Check if task A has a transitive dependency on task B.
     * Used for cycle detection.
     * 
     * @param taskA The starting task
     * @param taskB The target task to check for dependency
     * @return true if A depends on B through any chain of dependencies
     */
    private boolean hasTransitiveDependency(Task taskA, Task taskB) {
        if (taskA == null || taskB == null || taskA.equals(taskB)) {
            return taskA != null && taskA.equals(taskB);
        }
        
        // Check direct dependencies first
        for (Task directDependency : taskA.getPreDependencies()) {
            if (directDependency.equals(taskB)) {
                return true;
            }
            
            // Recursively check transitive dependencies
            if (hasTransitiveDependency(directDependency, taskB)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the weight of this dependency for critical path analysis.
     * 
     * @return Weight value (higher = more critical)
     */
    public double getCriticalPathWeight() {
        if (!active || !dependencyType.isCriticalPathRelevant()) {
            return 0.0;
        }
        
        switch (dependencyType) {
            case BLOCKING:
                return 10.0; // Highest weight
            case FINISH_TO_START:
                return 5.0;
            case START_TO_START:
            case FINISH_TO_FINISH:
                return 3.0;
            case START_TO_FINISH:
                return 2.0;
            default:
                return 1.0;
        }
    }
    
    /**
     * Get a human-readable description of this dependency.
     * 
     * @return Description string for UI display
     */
    public String getDescription() {
        if (dependentTask == null || prerequisiteTask == null) {
            return "Invalid dependency";
        }
        
        String lagDescription = "";
        if (lagHours != null && lagHours != 0) {
            lagDescription = lagHours > 0 ? 
                " (+" + lagHours + "h lag)" : 
                " (" + Math.abs(lagHours) + "h lead)";
        }
        
        return String.format("%s → %s (%s)%s", 
            prerequisiteTask.getTitle(),
            dependentTask.getTitle(),
            dependencyType.getShortCode(),
            lagDescription);
    }
    
    // JPA Lifecycle Methods
    
    @PrePersist
    @PreUpdate
    protected void validateDependency() {
        // Prevent self-dependency
        if (dependentTask != null && prerequisiteTask != null && 
            dependentTask.getId() != null && prerequisiteTask.getId() != null &&
            dependentTask.getId().equals(prerequisiteTask.getId())) {
            throw new IllegalStateException("A task cannot depend on itself");
        }
        
        // Validate same project
        if (dependentTask != null && prerequisiteTask != null &&
            dependentTask.getProject() != null && prerequisiteTask.getProject() != null &&
            !Objects.equals(dependentTask.getProject().getId(), prerequisiteTask.getProject().getId())) {
            throw new IllegalStateException("Tasks must be in the same project for dependency");
        }
        
        // Set project reference
        if (project == null && dependentTask != null) {
            project = dependentTask.getProject();
        }
    }
    
    // Standard getters and setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getDependentTask() {
        return dependentTask;
    }

    public void setDependentTask(Task dependentTask) {
        this.dependentTask = dependentTask;
        
        // Update project reference
        if (dependentTask != null && prerequisiteTask != null && 
            project == null) {
            project = dependentTask.getProject();
        }
    }

    public Task getPrerequisiteTask() {
        return prerequisiteTask;
    }

    public void setPrerequisiteTask(Task prerequisiteTask) {
        this.prerequisiteTask = prerequisiteTask;
        
        // Update project reference
        if (dependentTask != null && prerequisiteTask != null && 
            project == null) {
            project = prerequisiteTask.getProject();
        }
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Integer getLagHours() {
        return lagHours;
    }

    public void setLagHours(Integer lagHours) {
        this.lagHours = lagHours;
    }

    public boolean isCriticalPath() {
        return criticalPath;
    }

    public void setCriticalPath(boolean criticalPath) {
        this.criticalPath = criticalPath;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TaskDependency that = (TaskDependency) o;
        
        return Objects.equals(dependentTask, that.dependentTask) &&
               Objects.equals(prerequisiteTask, that.prerequisiteTask) &&
               dependencyType == that.dependencyType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dependentTask, prerequisiteTask, dependencyType);
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}