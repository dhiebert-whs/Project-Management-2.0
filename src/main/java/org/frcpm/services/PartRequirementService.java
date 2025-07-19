// src/main/java/org/frcpm/services/PartRequirementService.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services;

import org.frcpm.models.Part;
import org.frcpm.models.PartRequirement;
import org.frcpm.models.ProjectTemplate;
import org.frcpm.models.TaskTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing part requirements.
 * Provides business logic for requirement CRUD operations and template management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
public interface PartRequirementService {
    
    // Basic CRUD Operations
    
    /**
     * Creates a new part requirement.
     */
    PartRequirement createRequirement(PartRequirement requirement);
    
    /**
     * Updates an existing requirement.
     */
    PartRequirement updateRequirement(Long requirementId, PartRequirement requirement);
    
    /**
     * Finds a requirement by ID.
     */
    Optional<PartRequirement> findRequirementById(Long requirementId);
    
    /**
     * Deletes a requirement.
     */
    void deleteRequirement(Long requirementId);
    
    // Template-based Operations
    
    /**
     * Gets all requirements for a project template.
     */
    List<PartRequirement> getRequirementsByProjectTemplate(ProjectTemplate projectTemplate);
    
    /**
     * Gets all requirements for a task template.
     */
    List<PartRequirement> getRequirementsByTaskTemplate(TaskTemplate taskTemplate);
    
    /**
     * Gets critical requirements for a project template.
     */
    List<PartRequirement> getCriticalRequirements(ProjectTemplate projectTemplate);
    
    /**
     * Gets optional requirements for a project template.
     */
    List<PartRequirement> getOptionalRequirements(ProjectTemplate projectTemplate);
    
    // Inventory Analysis
    
    /**
     * Checks if all requirements can be fulfilled with current inventory.
     */
    boolean canFulfillAllRequirements(ProjectTemplate projectTemplate);
    
    /**
     * Gets requirements that cannot be fulfilled with current inventory.
     */
    List<PartRequirement> getUnfulfillableRequirements(ProjectTemplate projectTemplate);
    
    /**
     * Gets parts needed to fulfill all requirements.
     */
    List<Object[]> getPartsNeededForTemplate(ProjectTemplate projectTemplate);
    
    /**
     * Calculates total estimated cost for template requirements.
     */
    java.math.BigDecimal calculateTotalCost(ProjectTemplate projectTemplate);
    
    // Build Phase Management
    
    /**
     * Gets requirements for a specific build phase.
     */
    List<PartRequirement> getRequirementsByBuildPhase(ProjectTemplate projectTemplate, 
                                                     PartRequirement.BuildPhase buildPhase);
    
    /**
     * Gets requirements needed immediately (current and next phases).
     */
    List<PartRequirement> getImmediateRequirements(ProjectTemplate projectTemplate, 
                                                  PartRequirement.BuildPhase currentPhase);
    
    // Priority Management
    
    /**
     * Gets requirements by priority level.
     */
    List<PartRequirement> getRequirementsByPriority(ProjectTemplate projectTemplate, 
                                                   PartRequirement.RequirementPriority priority);
    
    /**
     * Gets high-priority requirements (Critical and High).
     */
    List<PartRequirement> getHighPriorityRequirements(ProjectTemplate projectTemplate);
    
    // Validation and Business Rules
    
    /**
     * Validates that requirement quantities are realistic.
     */
    boolean validateRequirement(PartRequirement requirement);
    
    /**
     * Checks for conflicting requirements in template.
     */
    List<String> findConflictingRequirements(ProjectTemplate projectTemplate);
}