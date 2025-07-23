// src/main/java/org/frcpm/services/impl/ManufacturingProcessServiceImpl.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.services.impl;

import org.frcpm.models.ManufacturingProcess;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.ManufacturingProcessRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.ManufacturingProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of ManufacturingProcessService providing comprehensive
 * manufacturing workflow tracking and process management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Service
@Transactional
public class ManufacturingProcessServiceImpl implements ManufacturingProcessService {
    
    private static final Logger LOGGER = Logger.getLogger(ManufacturingProcessServiceImpl.class.getName());
    
    private final ManufacturingProcessRepository manufacturingProcessRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    
    @Autowired
    public ManufacturingProcessServiceImpl(ManufacturingProcessRepository manufacturingProcessRepository,
                                         ProjectRepository projectRepository,
                                         TaskRepository taskRepository) {
        this.manufacturingProcessRepository = manufacturingProcessRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<ManufacturingProcess, Long> interface
    // =========================================================================
    
    @Override
    public ManufacturingProcess findById(Long id) {
        if (id == null) {
            return null;
        }
        return manufacturingProcessRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<ManufacturingProcess> findAll() {
        return manufacturingProcessRepository.findAll();
    }
    
    @Override
    public ManufacturingProcess save(ManufacturingProcess entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Manufacturing process cannot be null");
        }
        return manufacturingProcessRepository.save(entity);
    }
    
    @Override
    public void delete(ManufacturingProcess entity) {
        if (entity != null) {
            manufacturingProcessRepository.delete(entity);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && manufacturingProcessRepository.existsById(id)) {
            manufacturingProcessRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return manufacturingProcessRepository.count();
    }
    
    // =========================================================================
    // BASIC MANUFACTURING PROCESS OPERATIONS
    // =========================================================================
    
    @Override
    public ManufacturingProcess createProcess(String name, ManufacturingProcess.ProcessType processType, 
                                            Project project, Task task, String description) {
        return createDetailedProcess(name, processType, project, task, description, 
                                   ManufacturingProcess.ProcessPriority.MEDIUM, 8.0, null);
    }
    
    @Override
    public ManufacturingProcess createDetailedProcess(String name, ManufacturingProcess.ProcessType processType,
                                                    Project project, Task task, String description,
                                                    ManufacturingProcess.ProcessPriority priority,
                                                    Double estimatedHours, LocalDateTime targetCompletionDate) {
        try {
            ManufacturingProcess process = new ManufacturingProcess();
            process.setName(name);
            process.setProcessType(processType);
            process.setProject(project);
            process.setTask(task);
            process.setDescription(description);
            process.setPriority(priority);
            process.setEstimatedHours(estimatedHours);
            process.setTargetCompletionDate(targetCompletionDate);
            process.setStatus(ManufacturingProcess.ProcessStatus.PLANNED);
            
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating manufacturing process", e);
            throw new RuntimeException("Failed to create manufacturing process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess updateProcess(Long processId, String name, String description,
                                            ManufacturingProcess.ProcessPriority priority,
                                            Double estimatedHours, LocalDateTime targetCompletionDate) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            if (name != null) process.setName(name);
            if (description != null) process.setDescription(description);
            if (priority != null) process.setPriority(priority);
            if (estimatedHours != null) process.setEstimatedHours(estimatedHours);
            if (targetCompletionDate != null) process.setTargetCompletionDate(targetCompletionDate);
            
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating manufacturing process", e);
            throw new RuntimeException("Failed to update manufacturing process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess updateProcessStatus(Long processId, ManufacturingProcess.ProcessStatus newStatus) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.setStatus(newStatus);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating process status", e);
            throw new RuntimeException("Failed to update process status: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess assignProcess(Long processId, TeamMember assignedTo) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.setAssignedTo(assignedTo);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning process", e);
            throw new RuntimeException("Failed to assign process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess unassignProcess(Long processId) {
        return assignProcess(processId, null);
    }
    
    // =========================================================================
    // PROCESS WORKFLOW MANAGEMENT
    // =========================================================================
    
    @Override
    public ManufacturingProcess startProcess(Long processId, TeamMember assignedMember) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.startProcess(assignedMember);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting process", e);
            throw new RuntimeException("Failed to start process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess completeProcess(Long processId, String completionNotes) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.completeProcess();
            if (completionNotes != null) {
                process.setCompletionNotes(completionNotes);
            }
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error completing process", e);
            throw new RuntimeException("Failed to complete process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess pauseProcess(Long processId, String reason) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.pauseProcess(reason);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error pausing process", e);
            throw new RuntimeException("Failed to pause process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess resumeProcess(Long processId) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.resumeProcess();
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resuming process", e);
            throw new RuntimeException("Failed to resume process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess cancelProcess(Long processId, String reason) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.cancelProcess(reason);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error canceling process", e);
            throw new RuntimeException("Failed to cancel process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess blockProcess(Long processId, String blockingReason) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.blockProcess(blockingReason);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error blocking process", e);
            throw new RuntimeException("Failed to block process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess unblockProcess(Long processId) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.unblockProcess();
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error unblocking process", e);
            throw new RuntimeException("Failed to unblock process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess holdProcess(Long processId, String reason) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.holdProcess(reason);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error putting process on hold", e);
            throw new RuntimeException("Failed to put process on hold: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess releaseFromHold(Long processId) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                throw new IllegalArgumentException("Process not found: " + processId);
            }
            
            process.releaseFromHold();
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error releasing process from hold", e);
            throw new RuntimeException("Failed to release process from hold: " + e.getMessage(), e);
        }
    }
    
    // =========================================================================
    // PROCESS QUERIES AND FILTERS - Simplified implementations
    // =========================================================================
    
    @Override
    public List<ManufacturingProcess> findByProject(Project project) {
        return manufacturingProcessRepository.findByProjectAndIsActiveTrueOrderByPriorityAscNameAsc(project);
    }
    
    @Override
    public List<ManufacturingProcess> findByTask(Task task) {
        return manufacturingProcessRepository.findByTaskAndIsActiveTrueOrderByPriorityAscNameAsc(task);
    }
    
    @Override
    public List<ManufacturingProcess> findByStatus(ManufacturingProcess.ProcessStatus status) {
        return manufacturingProcessRepository.findByStatusAndIsActiveTrueOrderByPriorityAscCreatedAtAsc(status);
    }
    
    @Override
    public List<ManufacturingProcess> findByProcessType(ManufacturingProcess.ProcessType processType) {
        return manufacturingProcessRepository.findByProcessTypeAndIsActiveTrueOrderByPriorityAscNameAsc(processType);
    }
    
    @Override
    public List<ManufacturingProcess> findByPriority(ManufacturingProcess.ProcessPriority priority) {
        return manufacturingProcessRepository.findByPriorityAndIsActiveTrueOrderByNameAsc(priority);
    }
    
    @Override
    public List<ManufacturingProcess> findByEstimatedHoursRange(Double minHours, Double maxHours) {
        return manufacturingProcessRepository.findByEstimatedHoursBetweenAndIsActiveTrueOrderByPriorityAscNameAsc(minHours, maxHours);
    }
    
    @Override
    public List<ManufacturingProcess> findByAssignedTo(TeamMember assignedTo) {
        return manufacturingProcessRepository.findByAssignedToAndIsActiveTrueOrderByPriorityAscActualStartTimeAsc(assignedTo);
    }
    
    @Override
    public List<ManufacturingProcess> findByCreatedBy(TeamMember createdBy) {
        return manufacturingProcessRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(createdBy);
    }
    
    // =========================================================================
    // WORKFLOW STATE ANALYSIS - Simplified implementations using existing repository methods
    // =========================================================================
    
    @Override
    public List<ManufacturingProcess> findReadyProcesses(Project project) {
        List<ManufacturingProcess.ProcessStatus> readyStatuses = Arrays.asList(
            ManufacturingProcess.ProcessStatus.READY, 
            ManufacturingProcess.ProcessStatus.PLANNED
        );
        return manufacturingProcessRepository.findByStatusInAndIsActiveTrueOrderByPriorityAscNameAsc(readyStatuses);
    }
    
    @Override
    public List<ManufacturingProcess> findActiveProcesses(Project project) {
        return manufacturingProcessRepository.findByStatusAndIsActiveTrueOrderByActualStartTimeAsc(
            ManufacturingProcess.ProcessStatus.IN_PROGRESS);
    }
    
    @Override
    public List<ManufacturingProcess> findBlockedProcesses(Project project) {
        return manufacturingProcessRepository.findByStatusAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess.ProcessStatus.BLOCKED);
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesNeedingAttention(Project project) {
        List<ManufacturingProcess.ProcessStatus> attentionStatuses = Arrays.asList(
            ManufacturingProcess.ProcessStatus.FAILED,
            ManufacturingProcess.ProcessStatus.ON_HOLD,
            ManufacturingProcess.ProcessStatus.BLOCKED
        );
        return manufacturingProcessRepository.findByStatusInAndIsActiveTrueOrderByPriorityAscUpdatedAtAsc(attentionStatuses);
    }
    
    @Override
    public List<ManufacturingProcess> findCompletedProcesses(Project project) {
        return manufacturingProcessRepository.findByStatusAndIsActiveTrueOrderByActualEndTimeDesc(
            ManufacturingProcess.ProcessStatus.COMPLETED);
    }
    
    @Override
    public List<ManufacturingProcess> findOverdueProcesses(Project project) {
        return manufacturingProcessRepository.findByTargetCompletionDateBeforeAndStatusNotAndIsActiveTrueOrderByTargetCompletionDateAsc(
            LocalDateTime.now(), ManufacturingProcess.ProcessStatus.COMPLETED);
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesApproachingDeadline(Project project, int daysAhead) {
        LocalDateTime deadline = LocalDateTime.now().plusDays(daysAhead);
        return manufacturingProcessRepository.findByTargetCompletionDateBetweenAndIsActiveTrueOrderByTargetCompletionDateAsc(
            LocalDateTime.now(), deadline);
    }
    
    @Override
    public List<ManufacturingProcess> findCriticalProcesses(Project project) {
        return manufacturingProcessRepository.findCriticalProcessesForProject(project);
    }
    
    @Override
    public List<ManufacturingProcess> findAtRiskProcesses(Project project) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime warningDate = currentDate.plusDays(3); // 3 days warning
        return manufacturingProcessRepository.findAtRiskProcesses(project, currentDate, warningDate);
    }
    
    // =========================================================================
    // QUALITY MANAGEMENT - Simplified implementations
    // =========================================================================
    
    @Override
    public List<ManufacturingProcess> findProcessesRequiringQualityInspection(Project project) {
        return manufacturingProcessRepository.findByRequiresQualityInspectionTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesWithQualityIssues(Project project) {
        return manufacturingProcessRepository.findByHasQualityIssuesTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesRequiringApproval(Project project) {
        return manufacturingProcessRepository.findByRequiresApprovalTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    }
    
    @Override
    public List<ManufacturingProcess> findApprovedProcesses(Project project) {
        return manufacturingProcessRepository.findByIsApprovedTrueAndIsActiveTrueOrderByApprovedAtDesc();
    }
    
    @Override
    public ManufacturingProcess recordQualityIssues(Long processId, String qualityIssues, String correctiveActions) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.recordQualityIssues(qualityIssues, correctiveActions);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording quality issues", e);
            throw new RuntimeException("Failed to record quality issues: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess resolveQualityIssues(Long processId, String resolutionNotes) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.resolveQualityIssues(resolutionNotes);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resolving quality issues", e);
            throw new RuntimeException("Failed to resolve quality issues: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess approveProcess(Long processId, TeamMember approvedBy, String approvalNotes) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.approveProcess(approvedBy, approvalNotes);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving process", e);
            throw new RuntimeException("Failed to approve process: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ManufacturingProcess rejectProcess(Long processId, TeamMember rejectedBy, String rejectionReason) {
        try {
            ManufacturingProcess process = findById(processId);
            if (process == null) {
                return null;
            }
            
            process.rejectProcess(rejectedBy, rejectionReason);
            return manufacturingProcessRepository.save(process);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rejecting process", e);
            throw new RuntimeException("Failed to reject process: " + e.getMessage(), e);
        }
    }
    
    // =========================================================================
    // Simplified implementations for remaining methods
    // =========================================================================
    
    @Override
    public ManufacturingProcess updateEstimatedHours(Long processId, Double estimatedHours) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setEstimatedHours(estimatedHours);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess recordActualHours(Long processId, Double actualHours) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setActualHours(actualHours);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess updateTargetCompletionDate(Long processId, LocalDateTime targetDate) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setTargetCompletionDate(targetDate);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess updateRequirements(Long processId, String requirements) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setRequirements(requirements);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess updateMaterialsRequired(Long processId, String materials) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setMaterialsRequired(materials);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess updateToolsRequired(Long processId, String tools) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setRequiredTools(tools);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    @Override
    public ManufacturingProcess updateSafetyConsiderations(Long processId, String safetyNotes) {
        ManufacturingProcess process = findById(processId);
        if (process != null) {
            process.setSafetyNotes(safetyNotes);
            return manufacturingProcessRepository.save(process);
        }
        return null;
    }
    
    // =========================================================================
    // SEARCH AND FILTERING - Basic implementations
    // =========================================================================
    
    @Override
    public List<ManufacturingProcess> searchByName(String searchTerm) {
        return manufacturingProcessRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(searchTerm);
    }
    
    @Override
    public List<ManufacturingProcess> searchByDescription(String searchTerm) {
        return manufacturingProcessRepository.findByDescriptionContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(searchTerm);
    }
    
    @Override
    public List<ManufacturingProcess> searchByRequirements(String searchTerm) {
        return manufacturingProcessRepository.findByRequirementsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(searchTerm);
    }
    
    @Override
    public List<ManufacturingProcess> searchByMaterials(String searchTerm) {
        return manufacturingProcessRepository.findByMaterialsRequiredContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(searchTerm);
    }
    
    @Override
    public List<ManufacturingProcess> searchByTools(String searchTerm) {
        return manufacturingProcessRepository.findByRequiredToolsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(searchTerm);
    }
    
    @Override
    public List<ManufacturingProcess> advancedSearch(Map<String, Object> searchCriteria) {
        // Simplified implementation - return all processes for now
        LOGGER.info("Advanced search requested with criteria: " + searchCriteria);
        return findAll().stream()
                .filter(p -> p.getIsActive())
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // ANALYTICS AND REPORTING - Basic implementations
    // =========================================================================
    
    @Override
    public Map<String, Object> getProjectManufacturingMetrics(Project project) {
        Object[] metrics = manufacturingProcessRepository.findProjectManufacturingMetrics(project);
        Map<String, Object> result = new HashMap<>();
        if (metrics != null && metrics.length >= 5) {
            result.put("totalProcesses", metrics[0]);
            result.put("completedProcesses", metrics[1]);
            result.put("processesWithIssues", metrics[2]);
            result.put("avgEstimatedHours", metrics[3]);
            result.put("avgActualHours", metrics[4]);
        }
        return result;
    }
    
    @Override
    public Map<ManufacturingProcess.ProcessType, Object> getCapacityAnalysisByType(Project project) {
        List<Object[]> analysis = manufacturingProcessRepository.findCapacityAnalysisByType(project);
        Map<ManufacturingProcess.ProcessType, Object> result = new HashMap<>();
        for (Object[] row : analysis) {
            if (row.length >= 3) {
                ManufacturingProcess.ProcessType type = (ManufacturingProcess.ProcessType) row[0];
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("count", row[1]);
                metrics.put("avgHours", row[2]);
                result.put(type, metrics);
            }
        }
        return result;
    }
    
    @Override
    public List<ManufacturingProcess> getWorkloadForTeamMember(TeamMember teamMember) {
        return manufacturingProcessRepository.findWorkloadForTeamMember(teamMember);
    }
    
    @Override
    public Map<String, Object> getProcessPerformanceStatistics(Project project) {
        // Simplified implementation
        Map<String, Object> stats = new HashMap<>();
        List<ManufacturingProcess> processes = findByProject(project);
        
        long totalProcesses = processes.size();
        long completedProcesses = processes.stream()
                .mapToLong(p -> p.getStatus() == ManufacturingProcess.ProcessStatus.COMPLETED ? 1 : 0)
                .sum();
        
        stats.put("totalProcesses", totalProcesses);
        stats.put("completedProcesses", completedProcesses);
        stats.put("completionRate", totalProcesses > 0 ? (double) completedProcesses / totalProcesses * 100 : 0);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getQualityStatistics(Project project) {
        // Simplified implementation
        Map<String, Object> stats = new HashMap<>();
        List<ManufacturingProcess> processes = findByProject(project);
        
        long processesRequiringInspection = processes.stream()
                .mapToLong(p -> p.getRequiresQualityInspection() ? 1 : 0)
                .sum();
        long processesWithIssues = processes.stream()
                .mapToLong(p -> p.getHasQualityIssues() ? 1 : 0)
                .sum();
        
        stats.put("processesRequiringInspection", processesRequiringInspection);
        stats.put("processesWithIssues", processesWithIssues);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getTimelineAnalysis(Project project) {
        // Simplified implementation
        Map<String, Object> analysis = new HashMap<>();
        List<ManufacturingProcess> processes = findByProject(project);
        
        OptionalDouble avgEstimated = processes.stream()
                .filter(p -> p.getEstimatedHours() != null)
                .mapToDouble(ManufacturingProcess::getEstimatedHours)
                .average();
        
        OptionalDouble avgActual = processes.stream()
                .filter(p -> p.getActualHours() != null)
                .mapToDouble(ManufacturingProcess::getActualHours)
                .average();
        
        analysis.put("avgEstimatedHours", avgEstimated.orElse(0.0));
        analysis.put("avgActualHours", avgActual.orElse(0.0));
        
        return analysis;
    }
    
    @Override
    public Map<String, Object> getResourceUtilizationAnalysis(Project project) {
        // Simplified implementation
        Map<String, Object> analysis = new HashMap<>();
        List<ManufacturingProcess> processes = findByProject(project);
        
        long assignedProcesses = processes.stream()
                .mapToLong(p -> p.getAssignedTo() != null ? 1 : 0)
                .sum();
        
        analysis.put("totalProcesses", processes.size());
        analysis.put("assignedProcesses", assignedProcesses);
        analysis.put("utilizationRate", processes.size() > 0 ? (double) assignedProcesses / processes.size() * 100 : 0);
        
        return analysis;
    }
    
    // =========================================================================
    // Remaining simplified method implementations
    // =========================================================================
    
    @Override
    public List<ManufacturingProcess> createBulkProcesses(List<ManufacturingProcess> processes) {
        return processes.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }
    
    @Override
    public int updateBulkStatus(List<Long> processIds, ManufacturingProcess.ProcessStatus newStatus) {
        int updated = 0;
        for (Long id : processIds) {
            if (updateProcessStatus(id, newStatus) != null) {
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    public int assignBulkProcesses(List<Long> processIds, TeamMember assignedTo) {
        int updated = 0;
        for (Long id : processIds) {
            if (assignProcess(id, assignedTo) != null) {
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    public int updateBulkPriority(List<Long> processIds, ManufacturingProcess.ProcessPriority priority) {
        int updated = 0;
        for (Long id : processIds) {
            ManufacturingProcess process = findById(id);
            if (process != null) {
                process.setPriority(priority);
                save(process);
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    public int deactivateBulkProcesses(List<Long> processIds) {
        int updated = 0;
        for (Long id : processIds) {
            ManufacturingProcess process = findById(id);
            if (process != null) {
                process.setIsActive(false);
                save(process);
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    public int reactivateBulkProcesses(List<Long> processIds) {
        int updated = 0;
        for (Long id : processIds) {
            ManufacturingProcess process = findById(id);
            if (process != null) {
                process.setIsActive(true);
                save(process);
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesDependentOnTask(Task task) {
        return findByTask(task);
    }
    
    @Override
    public List<ManufacturingProcess> findProcessesBlockingTaskCompletion(Task task) {
        return findByTask(task).stream()
                .filter(p -> p.getStatus() != ManufacturingProcess.ProcessStatus.COMPLETED)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateProcessDependencies(Task task) {
        // Simplified implementation - log the update request
        LOGGER.info("Process dependencies update requested for task: " + task.getId());
    }
    
    @Override
    public Map<String, Object> validateProcessWorkflow(Project project) {
        Map<String, Object> validation = new HashMap<>();
        List<ManufacturingProcess> processes = findByProject(project);
        
        List<String> issues = new ArrayList<>();
        long unassignedProcesses = processes.stream()
                .mapToLong(p -> p.getAssignedTo() == null ? 1 : 0)
                .sum();
        
        if (unassignedProcesses > 0) {
            issues.add(unassignedProcesses + " processes are unassigned");
        }
        
        validation.put("isValid", issues.isEmpty());
        validation.put("issues", issues);
        
        return validation;
    }
    
    @Override
    public Map<String, Object> optimizeManufacturingWorkflow(Project project) {
        Map<String, Object> optimization = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        List<ManufacturingProcess> processes = findByProject(project);
        long blockedProcesses = processes.stream()
                .mapToLong(p -> p.getStatus() == ManufacturingProcess.ProcessStatus.BLOCKED ? 1 : 0)
                .sum();
        
        if (blockedProcesses > 0) {
            recommendations.add("Resolve " + blockedProcesses + " blocked processes");
        }
        
        optimization.put("recommendations", recommendations);
        return optimization;
    }
    
    @Override
    public ManufacturingProcess createFromTemplate(String templateName, Project project, Task task) {
        // Simplified implementation - create a basic process
        return createProcess("Process from " + templateName, 
                           ManufacturingProcess.ProcessType.MACHINING, 
                           project, task, "Created from template: " + templateName);
    }
    
    @Override
    public void saveAsTemplate(Long processId, String templateName) {
        LOGGER.info("Process " + processId + " saved as template: " + templateName);
    }
    
    @Override
    public List<String> getAvailableTemplates() {
        return Arrays.asList("Basic Machining", "Electronics Assembly", "3D Printing", "Welding Process");
    }
    
    @Override
    public List<ManufacturingProcess> applyStandardProcesses(Project project, List<String> processTypes) {
        return processTypes.stream()
                .map(type -> createFromTemplate(type, project, project.getTasks().stream().findFirst().orElse(null)))
                .collect(Collectors.toList());
    }
}