// src/main/java/org/frcpm/web/dto/TaskDependencyDto.java
// Phase 2E-D: Task Dependency Data Transfer Object

package org.frcpm.web.dto;

import org.frcpm.models.DependencyType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for task dependency operations.
 * 
 * ✅ PHASE 2E-D: New DTO for advanced task dependency management
 * 
 * This DTO carries task dependency information for REST API operations,
 * including creation, updates, and display purposes. Includes validation
 * annotations and proper JSON serialization for clean API responses.
 * 
 * @author FRC Project Management Team - Phase 2E-D
 * @version 2.0.0-2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
public class TaskDependencyDto {
    
    private Long id;
    
    @NotNull(message = "Predecessor task ID is required")
    private Long predecessorTaskId;
    
    private String predecessorTaskTitle;
    
    @NotNull(message = "Successor task ID is required")
    private Long successorTaskId;
    
    private String successorTaskTitle;
    
    @NotNull(message = "Dependency type is required")
    private DependencyType type;
    
    private Integer lagDays;
    
    private Boolean active = true;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private Boolean onCriticalPath = false;
    
    // Additional display fields
    private String predecessorProjectName;
    private String successorProjectName;
    private String typeDisplayName;
    private String statusText;
    private String validationMessage;
    
    // Default constructor
    public TaskDependencyDto() {}
    
    // Constructor for creation
    public TaskDependencyDto(Long predecessorTaskId, Long successorTaskId, DependencyType type) {
        this.predecessorTaskId = predecessorTaskId;
        this.successorTaskId = successorTaskId;
        this.type = type;
    }
    
    // Constructor with lag
    public TaskDependencyDto(Long predecessorTaskId, Long successorTaskId, DependencyType type, Integer lagDays) {
        this(predecessorTaskId, successorTaskId, type);
        this.lagDays = lagDays;
    }
    
    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPredecessorTaskId() { return predecessorTaskId; }
    public void setPredecessorTaskId(Long predecessorTaskId) { this.predecessorTaskId = predecessorTaskId; }
    
    public String getPredecessorTaskTitle() { return predecessorTaskTitle; }
    public void setPredecessorTaskTitle(String predecessorTaskTitle) { this.predecessorTaskTitle = predecessorTaskTitle; }
    
    public Long getSuccessorTaskId() { return successorTaskId; }
    public void setSuccessorTaskId(Long successorTaskId) { this.successorTaskId = successorTaskId; }
    
    public String getSuccessorTaskTitle() { return successorTaskTitle; }
    public void setSuccessorTaskTitle(String successorTaskTitle) { this.successorTaskTitle = successorTaskTitle; }
    
    public DependencyType getType() { return type; }
    public void setType(DependencyType type) { this.type = type; }
    
    public Integer getLagDays() { return lagDays; }
    public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getOnCriticalPath() { return onCriticalPath; }
    public void setOnCriticalPath(Boolean onCriticalPath) { this.onCriticalPath = onCriticalPath; }
    
    // Display fields
    public String getPredecessorProjectName() { return predecessorProjectName; }
    public void setPredecessorProjectName(String predecessorProjectName) { this.predecessorProjectName = predecessorProjectName; }
    
    public String getSuccessorProjectName() { return successorProjectName; }
    public void setSuccessorProjectName(String successorProjectName) { this.successorProjectName = successorProjectName; }
    
    public String getTypeDisplayName() { return typeDisplayName != null ? typeDisplayName : (type != null ? type.getDisplayName() : null); }
    public void setTypeDisplayName(String typeDisplayName) { this.typeDisplayName = typeDisplayName; }
    
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    
    public String getValidationMessage() { return validationMessage; }
    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }
    
    // =========================================================================
    // CONVENIENCE METHODS
    // =========================================================================
    
    /**
     * Check if this dependency is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active != null && active;
    }
    
    /**
     * Check if this dependency is on the critical path.
     * 
     * @return true if on critical path, false otherwise
     */
    public boolean isOnCriticalPath() {
        return onCriticalPath != null && onCriticalPath;
    }
    
    /**
     * Get lag time description for display.
     * 
     * @return human-readable lag time description
     */
    public String getLagDescription() {
        if (lagDays == null || lagDays == 0) {
            return "No lag";
        } else if (lagDays > 0) {
            return lagDays + " day" + (lagDays == 1 ? "" : "s") + " delay";
        } else {
            return Math.abs(lagDays) + " day" + (Math.abs(lagDays) == 1 ? "" : "s") + " lead time";
        }
    }
    
    /**
     * Get dependency relationship description.
     * 
     * @return human-readable dependency description
     */
    public String getDependencyDescription() {
        StringBuilder sb = new StringBuilder();
        
        if (predecessorTaskTitle != null && successorTaskTitle != null) {
            sb.append("'").append(predecessorTaskTitle).append("' → '").append(successorTaskTitle).append("'");
        } else {
            sb.append("Task ").append(predecessorTaskId).append(" → Task ").append(successorTaskId);
        }
        
        if (type != null) {
            sb.append(" (").append(type.getDisplayName()).append(")");
        }
        
        if (lagDays != null && lagDays != 0) {
            sb.append(" with ").append(getLagDescription());
        }
        
        return sb.toString();
    }
    
    /**
     * Validate the dependency data.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (predecessorTaskId == null || successorTaskId == null) {
            return false;
        }
        
        if (predecessorTaskId.equals(successorTaskId)) {
            return false; // Self-dependency
        }
        
        if (type == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get CSS class for dependency type styling.
     * 
     * @return CSS class name
     */
    public String getTypeClass() {
        if (type == null) {
            return "text-secondary";
        }
        
        switch (type) {
            case FINISH_TO_START:
                return "text-primary";
            case START_TO_START:
                return "text-success";
            case FINISH_TO_FINISH:
                return "text-warning";
            case START_TO_FINISH:
                return "text-danger";
            case BLOCKING:
                return "text-danger";
            case SOFT:
                return "text-muted";
            default:
                return "text-secondary";
        }
    }
    
    /**
     * Get icon class for dependency type.
     * 
     * @return Font Awesome icon class
     */
    public String getTypeIcon() {
        if (type == null) {
            return "fas fa-link";
        }
        
        switch (type) {
            case FINISH_TO_START:
                return "fas fa-arrow-right";
            case START_TO_START:
                return "fas fa-play";
            case FINISH_TO_FINISH:
                return "fas fa-stop";
            case START_TO_FINISH:
                return "fas fa-exchange-alt";
            case BLOCKING:
                return "fas fa-ban";
            case SOFT:
                return "fas fa-link";
            default:
                return "fas fa-link";
        }
    }
    
    @Override
    public String toString() {
        return String.format("TaskDependencyDto{id=%d, predecessorTask=%d, successorTask=%d, type=%s, lagDays=%d, active=%s, onCriticalPath=%s}",
                            id, predecessorTaskId, successorTaskId, type, lagDays, active, onCriticalPath);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TaskDependencyDto that = (TaskDependencyDto) o;
        
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (predecessorTaskId != null ? !predecessorTaskId.equals(that.predecessorTaskId) : that.predecessorTaskId != null) return false;
        if (successorTaskId != null ? !successorTaskId.equals(that.successorTaskId) : that.successorTaskId != null) return false;
        return type == that.type;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (predecessorTaskId != null ? predecessorTaskId.hashCode() : 0);
        result = 31 * result + (successorTaskId != null ? successorTaskId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}