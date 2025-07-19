// src/main/java/org/frcpm/repositories/spring/PartRequirementRepository.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.repositories.spring;

import org.frcpm.models.Part;
import org.frcpm.models.PartRequirement;
import org.frcpm.models.ProjectTemplate;
import org.frcpm.models.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for PartRequirement entities.
 * Provides part requirement lookup and template management functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Repository
public interface PartRequirementRepository extends JpaRepository<PartRequirement, Long> {
    
    // Template-based Queries
    
    /**
     * Finds all requirements for a project template.
     */
    List<PartRequirement> findByProjectTemplateAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            ProjectTemplate projectTemplate);
    
    /**
     * Finds all requirements for a task template.
     */
    List<PartRequirement> findByTaskTemplateAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            TaskTemplate taskTemplate);
    
    /**
     * Finds requirements for both project and task templates.
     */
    List<PartRequirement> findByProjectTemplateOrTaskTemplateAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            ProjectTemplate projectTemplate, TaskTemplate taskTemplate);
    
    // Part-based Queries
    
    /**
     * Finds all requirements for a specific part.
     */
    List<PartRequirement> findByPartAndIsActiveTrueOrderByPriorityAsc(Part part);
    
    /**
     * Finds requirements by part category.
     */
    List<PartRequirement> findByPartCategoryAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            Part.PartCategory category);
    
    // Priority and Criticality
    
    /**
     * Finds critical requirements only.
     */
    List<PartRequirement> findByIsCriticalTrueAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds requirements by priority level.
     */
    List<PartRequirement> findByPriorityAndIsActiveTrueOrderByPartNameAsc(
            PartRequirement.RequirementPriority priority);
    
    /**
     * Finds high-priority requirements (Critical and High).
     */
    List<PartRequirement> findByPriorityInAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            List<PartRequirement.RequirementPriority> priorities);
    
    /**
     * Finds optional requirements.
     */
    List<PartRequirement> findByIsOptionalTrueAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds required (non-optional) requirements.
     */
    List<PartRequirement> findByIsOptionalFalseAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    // Build Phase Queries
    
    /**
     * Finds requirements for a specific build phase.
     */
    List<PartRequirement> findByBuildPhaseAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            PartRequirement.BuildPhase buildPhase);
    
    /**
     * Finds requirements for multiple build phases.
     */
    List<PartRequirement> findByBuildPhaseInAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            List<PartRequirement.BuildPhase> buildPhases);
    
    // Inventory Availability
    
    /**
     * Finds requirements where current inventory is insufficient.
     * Note: This is a simplified approach - complex logic handled in service layer.
     */
    List<PartRequirement> findByIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    // Vendor and Cost
    
    /**
     * Finds requirements with preferred vendor specified.
     */
    List<PartRequirement> findByPreferredVendorIsNotNullAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds requirements for specific vendor.
     */
    List<PartRequirement> findByPreferredVendorContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            String vendor);
    
    /**
     * Finds requirements with estimated cost specified.
     */
    List<PartRequirement> findByEstimatedCostPerUnitIsNotNullAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    // Reusability and Safety
    
    /**
     * Finds reusable part requirements.
     */
    List<PartRequirement> findByIsReusableTrueAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds consumable part requirements.
     */
    List<PartRequirement> findByIsReusableFalseAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds requirements with safety considerations.
     */
    List<PartRequirement> findBySafetyNotesIsNotNullAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    // Lead Time
    
    /**
     * Finds requirements with long lead times.
     */
    List<PartRequirement> findByLeadTimeDaysGreaterThanAndIsActiveTrueOrderByLeadTimeDaysDescPriorityAsc(
            int days);
    
    /**
     * Finds requirements with lead time specified.
     */
    List<PartRequirement> findByLeadTimeDaysIsNotNullAndIsActiveTrueOrderByLeadTimeDaysAscPriorityAsc();
    
    // Search and Filter
    
    /**
     * Finds requirements by searching specifications.
     */
    List<PartRequirement> findBySpecificationsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            String searchTerm);
    
    /**
     * Finds requirements by searching usage notes.
     */
    List<PartRequirement> findByUsageNotesContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            String searchTerm);
    
    /**
     * Finds requirements by searching alternatives.
     */
    List<PartRequirement> findByAlternativesContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            String searchTerm);
    
    // Statistical Queries
    
    /**
     * Counts requirements for a project template.
     */
    long countByProjectTemplateAndIsActiveTrue(ProjectTemplate projectTemplate);
    
    /**
     * Counts requirements for a task template.
     */
    long countByTaskTemplateAndIsActiveTrue(TaskTemplate taskTemplate);
    
    /**
     * Counts critical requirements for a project template.
     */
    long countByProjectTemplateAndIsCriticalTrueAndIsActiveTrue(ProjectTemplate projectTemplate);
    
    /**
     * Counts requirements by priority.
     */
    long countByPriorityAndIsActiveTrue(PartRequirement.RequirementPriority priority);
    
    /**
     * Counts requirements for a build phase.
     */
    long countByBuildPhaseAndIsActiveTrue(PartRequirement.BuildPhase buildPhase);
    
    /**
     * Counts requirements for a part.
     */
    long countByPartAndIsActiveTrue(Part part);
    
    // Template Relationships
    
    /**
     * Finds requirements for project template by subsystem type.
     */
    List<PartRequirement> findByProjectTemplateSubsystemTypeAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            org.frcpm.models.SubsystemType subsystemType);
    
    /**
     * Checks if a specific part is required by a project template.
     */
    boolean existsByProjectTemplateAndPartAndIsActiveTrue(ProjectTemplate projectTemplate, Part part);
    
    /**
     * Checks if a specific part is required by a task template.
     */
    boolean existsByTaskTemplateAndPartAndIsActiveTrue(TaskTemplate taskTemplate, Part part);
    
    // Active Status Management
    
    /**
     * Finds all active requirements.
     */
    List<PartRequirement> findByIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds all inactive requirements.
     */
    List<PartRequirement> findByIsActiveFalseOrderByPriorityAscPartNameAsc();
    
    // Quantity-based Queries
    
    /**
     * Finds requirements with high quantities.
     */
    List<PartRequirement> findByQuantityRequiredGreaterThanAndIsActiveTrueOrderByQuantityRequiredDescPriorityAsc(
            int quantity);
    
    /**
     * Finds requirements with flexible quantities (min/max range).
     */
    List<PartRequirement> findByMinimumQuantityIsNotNullAndMaximumQuantityIsNotNullAndIsActiveTrueOrderByPriorityAscPartNameAsc();
    
    /**
     * Finds requirements within quantity range.
     */
    List<PartRequirement> findByQuantityRequiredBetweenAndIsActiveTrueOrderByPriorityAscPartNameAsc(
            int minQuantity, int maxQuantity);
}