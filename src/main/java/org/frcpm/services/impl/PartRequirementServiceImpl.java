// src/main/java/org/frcpm/services/impl/PartRequirementServiceImpl.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services.impl;

import org.frcpm.models.PartRequirement;
import org.frcpm.models.ProjectTemplate;
import org.frcpm.models.TaskTemplate;
import org.frcpm.repositories.spring.PartRequirementRepository;
import org.frcpm.services.PartRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of PartRequirementService for managing part requirements.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Service
@Transactional
public class PartRequirementServiceImpl implements PartRequirementService {
    
    private static final Logger LOGGER = Logger.getLogger(PartRequirementServiceImpl.class.getName());
    
    @Autowired
    private PartRequirementRepository partRequirementRepository;
    
    // Basic CRUD Operations
    
    @Override
    public PartRequirement createRequirement(PartRequirement requirement) {
        try {
            LOGGER.info("Creating part requirement for: " + requirement.getPart().getPartNumber());
            
            // Validate requirement
            if (!validateRequirement(requirement)) {
                throw new IllegalArgumentException("Invalid requirement data");
            }
            
            // Set timestamps
            requirement.setCreatedAt(LocalDateTime.now());
            requirement.setUpdatedAt(LocalDateTime.now());
            
            PartRequirement savedRequirement = partRequirementRepository.save(requirement);
            LOGGER.info("Successfully created requirement: " + savedRequirement.getId());
            return savedRequirement;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating part requirement", e);
            throw new RuntimeException("Failed to create requirement: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PartRequirement updateRequirement(Long requirementId, PartRequirement requirement) {
        try {
            LOGGER.info("Updating requirement: " + requirementId);
            
            Optional<PartRequirement> existingOpt = partRequirementRepository.findById(requirementId);
            if (!existingOpt.isPresent()) {
                throw new IllegalArgumentException("Requirement not found: " + requirementId);
            }
            
            PartRequirement existing = existingOpt.get();
            
            // Preserve ID and creation timestamp
            requirement.setId(requirementId);
            requirement.setCreatedAt(existing.getCreatedAt());
            requirement.setUpdatedAt(LocalDateTime.now());
            
            PartRequirement savedRequirement = partRequirementRepository.save(requirement);
            LOGGER.info("Successfully updated requirement: " + savedRequirement.getId());
            return savedRequirement;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating requirement: " + requirementId, e);
            throw new RuntimeException("Failed to update requirement: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PartRequirement> findRequirementById(Long requirementId) {
        try {
            return partRequirementRepository.findById(requirementId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding requirement by ID: " + requirementId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void deleteRequirement(Long requirementId) {
        try {
            LOGGER.info("Deleting requirement: " + requirementId);
            partRequirementRepository.deleteById(requirementId);
            LOGGER.info("Successfully deleted requirement: " + requirementId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting requirement: " + requirementId, e);
            throw new RuntimeException("Failed to delete requirement: " + e.getMessage(), e);
        }
    }
    
    // Template-based Operations
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getRequirementsByProjectTemplate(ProjectTemplate projectTemplate) {
        try {
            return partRequirementRepository.findByProjectTemplateAndIsActiveTrueOrderByPriorityAscPartNameAsc(projectTemplate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting requirements by project template", e);
            throw new RuntimeException("Failed to get requirements by project template: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getRequirementsByTaskTemplate(TaskTemplate taskTemplate) {
        try {
            return partRequirementRepository.findByTaskTemplateAndIsActiveTrueOrderByPriorityAscPartNameAsc(taskTemplate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting requirements by task template", e);
            throw new RuntimeException("Failed to get requirements by task template: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getCriticalRequirements(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement> allRequirements = getRequirementsByProjectTemplate(projectTemplate);
            return allRequirements.stream()
                .filter(PartRequirement::getIsCritical)
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting critical requirements", e);
            throw new RuntimeException("Failed to get critical requirements: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getOptionalRequirements(ProjectTemplate projectTemplate) {
        try {
            return partRequirementRepository.findByIsOptionalTrueAndIsActiveTrueOrderByPriorityAscPartNameAsc()
                .stream()
                .filter(req -> req.getProjectTemplate() != null && req.getProjectTemplate().equals(projectTemplate))
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting optional requirements", e);
            throw new RuntimeException("Failed to get optional requirements: " + e.getMessage(), e);
        }
    }
    
    // Inventory Analysis
    
    @Override
    @Transactional(readOnly = true)
    public boolean canFulfillAllRequirements(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement> requirements = getRequirementsByProjectTemplate(projectTemplate);
            return requirements.stream()
                .filter(req -> !req.getIsOptional()) // Only check required items
                .allMatch(PartRequirement::canBeFulfilled);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if can fulfill all requirements", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getUnfulfillableRequirements(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement> requirements = getRequirementsByProjectTemplate(projectTemplate);
            return requirements.stream()
                .filter(req -> !req.canBeFulfilled())
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting unfulfillable requirements", e);
            throw new RuntimeException("Failed to get unfulfillable requirements: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPartsNeededForTemplate(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement> unfulfillable = getUnfulfillableRequirements(projectTemplate);
            // Return simplified data - part and additional quantity needed
            return unfulfillable.stream()
                .map(req -> new Object[]{req.getPart(), req.getAdditionalUnitsNeeded()})
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts needed for template", e);
            throw new RuntimeException("Failed to get parts needed for template: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCost(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement> requirements = getRequirementsByProjectTemplate(projectTemplate);
            return requirements.stream()
                .map(PartRequirement::getTotalEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating total cost", e);
            throw new RuntimeException("Failed to calculate total cost: " + e.getMessage(), e);
        }
    }
    
    // Build Phase Management
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getRequirementsByBuildPhase(ProjectTemplate projectTemplate, 
                                                           PartRequirement.BuildPhase buildPhase) {
        try {
            List<PartRequirement> allRequirements = getRequirementsByProjectTemplate(projectTemplate);
            return allRequirements.stream()
                .filter(req -> req.getBuildPhase() == buildPhase || req.getBuildPhase() == PartRequirement.BuildPhase.ANY)
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting requirements by build phase: " + buildPhase, e);
            throw new RuntimeException("Failed to get requirements by build phase: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getImmediateRequirements(ProjectTemplate projectTemplate, 
                                                         PartRequirement.BuildPhase currentPhase) {
        try {
            // Get requirements for current phase and next phase
            List<PartRequirement> currentPhaseReqs = getRequirementsByBuildPhase(projectTemplate, currentPhase);
            
            // Get next phase
            PartRequirement.BuildPhase nextPhase = getNextBuildPhase(currentPhase);
            if (nextPhase != null) {
                List<PartRequirement> nextPhaseReqs = getRequirementsByBuildPhase(projectTemplate, nextPhase);
                currentPhaseReqs.addAll(nextPhaseReqs);
            }
            
            return currentPhaseReqs.stream().distinct().toList();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting immediate requirements", e);
            throw new RuntimeException("Failed to get immediate requirements: " + e.getMessage(), e);
        }
    }
    
    private PartRequirement.BuildPhase getNextBuildPhase(PartRequirement.BuildPhase currentPhase) {
        switch (currentPhase) {
            case DESIGN: return PartRequirement.BuildPhase.FABRICATION;
            case FABRICATION: return PartRequirement.BuildPhase.TESTING;
            case TESTING: return PartRequirement.BuildPhase.INTEGRATION;
            case INTEGRATION: return PartRequirement.BuildPhase.COMPETITION;
            case COMPETITION: return null;
            default: return null;
        }
    }
    
    // Priority Management
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getRequirementsByPriority(ProjectTemplate projectTemplate, 
                                                          PartRequirement.RequirementPriority priority) {
        try {
            List<PartRequirement> allRequirements = getRequirementsByProjectTemplate(projectTemplate);
            return allRequirements.stream()
                .filter(req -> req.getPriority() == priority)
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting requirements by priority: " + priority, e);
            throw new RuntimeException("Failed to get requirements by priority: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PartRequirement> getHighPriorityRequirements(ProjectTemplate projectTemplate) {
        try {
            List<PartRequirement.RequirementPriority> highPriorities = Arrays.asList(
                PartRequirement.RequirementPriority.CRITICAL,
                PartRequirement.RequirementPriority.HIGH
            );
            
            List<PartRequirement> allRequirements = getRequirementsByProjectTemplate(projectTemplate);
            return allRequirements.stream()
                .filter(req -> highPriorities.contains(req.getPriority()))
                .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting high priority requirements", e);
            throw new RuntimeException("Failed to get high priority requirements: " + e.getMessage(), e);
        }
    }
    
    // Validation and Business Rules
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateRequirement(PartRequirement requirement) {
        try {
            // Basic validation
            if (requirement.getPart() == null || 
                requirement.getQuantityRequired() == null || 
                requirement.getQuantityRequired() <= 0 ||
                requirement.getPriority() == null) {
                return false;
            }
            
            // Validate quantity ranges
            if (requirement.getMinimumQuantity() != null && requirement.getMaximumQuantity() != null) {
                if (requirement.getMinimumQuantity() > requirement.getMaximumQuantity()) {
                    return false;
                }
                
                // Quantity required should be within the range
                if (requirement.getQuantityRequired() < requirement.getMinimumQuantity() ||
                    requirement.getQuantityRequired() > requirement.getMaximumQuantity()) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating requirement", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> findConflictingRequirements(ProjectTemplate projectTemplate) {
        try {
            // Simple conflict detection - parts required by multiple templates with different specs
            // This would require more complex logic to implement fully
            return List.of();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding conflicting requirements", e);
            return List.of();
        }
    }
}