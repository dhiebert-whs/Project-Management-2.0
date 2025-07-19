// src/main/java/org/frcpm/services/ManufacturingProcessService.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.services;

import org.frcpm.models.ManufacturingProcess;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for ManufacturingProcess management.
 * Provides comprehensive manufacturing workflow tracking and process management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
public interface ManufacturingProcessService extends Service<ManufacturingProcess, Long> {
    
    // =========================================================================
    // BASIC MANUFACTURING PROCESS OPERATIONS
    // =========================================================================
    
    /**
     * Creates a new manufacturing process.
     */
    ManufacturingProcess createProcess(String name, ManufacturingProcess.ProcessType processType, 
                                     Project project, Task task, String description);
    
    /**
     * Creates a manufacturing process with full details.
     */
    ManufacturingProcess createDetailedProcess(String name, ManufacturingProcess.ProcessType processType,
                                             Project project, Task task, String description,
                                             ManufacturingProcess.ProcessPriority priority,
                                             Double estimatedHours, LocalDateTime targetCompletionDate);
    
    /**
     * Updates manufacturing process details.
     */
    ManufacturingProcess updateProcess(Long processId, String name, String description,
                                     ManufacturingProcess.ProcessPriority priority,
                                     Double estimatedHours, LocalDateTime targetCompletionDate);
    
    /**
     * Updates process status.
     */
    ManufacturingProcess updateProcessStatus(Long processId, ManufacturingProcess.ProcessStatus newStatus);
    
    /**
     * Assigns a team member to a process.
     */
    ManufacturingProcess assignProcess(Long processId, TeamMember assignedTo);
    
    /**
     * Removes team member assignment from a process.
     */
    ManufacturingProcess unassignProcess(Long processId);
    
    // =========================================================================
    // PROCESS WORKFLOW MANAGEMENT
    // =========================================================================
    
    /**
     * Starts a manufacturing process.
     */
    ManufacturingProcess startProcess(Long processId, TeamMember assignedMember);
    
    /**
     * Completes a manufacturing process.
     */
    ManufacturingProcess completeProcess(Long processId, String completionNotes);
    
    /**
     * Pauses a manufacturing process.
     */
    ManufacturingProcess pauseProcess(Long processId, String reason);
    
    /**
     * Resumes a paused manufacturing process.
     */
    ManufacturingProcess resumeProcess(Long processId);
    
    /**
     * Cancels a manufacturing process.
     */
    ManufacturingProcess cancelProcess(Long processId, String reason);
    
    /**
     * Marks a process as blocked.
     */
    ManufacturingProcess blockProcess(Long processId, String blockingReason);
    
    /**
     * Unblocks a process.
     */
    ManufacturingProcess unblockProcess(Long processId);
    
    /**
     * Puts a process on hold.
     */
    ManufacturingProcess holdProcess(Long processId, String reason);
    
    /**
     * Releases a process from hold.
     */
    ManufacturingProcess releaseFromHold(Long processId);
    
    // =========================================================================
    // PROCESS QUERIES AND FILTERS
    // =========================================================================
    
    /**
     * Finds all processes for a project.
     */
    List<ManufacturingProcess> findByProject(Project project);
    
    /**
     * Finds all processes for a task.
     */
    List<ManufacturingProcess> findByTask(Task task);
    
    /**
     * Finds processes by status.
     */
    List<ManufacturingProcess> findByStatus(ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds processes by process type.
     */
    List<ManufacturingProcess> findByProcessType(ManufacturingProcess.ProcessType processType);
    
    /**
     * Finds processes by priority.
     */
    List<ManufacturingProcess> findByPriority(ManufacturingProcess.ProcessPriority priority);
    
    /**
     * Finds processes by estimated hours range.
     */
    List<ManufacturingProcess> findByEstimatedHoursRange(Double minHours, Double maxHours);
    
    /**
     * Finds processes assigned to a team member.
     */
    List<ManufacturingProcess> findByAssignedTo(TeamMember assignedTo);
    
    /**
     * Finds processes created by a team member.
     */
    List<ManufacturingProcess> findByCreatedBy(TeamMember createdBy);
    
    // =========================================================================
    // WORKFLOW STATE ANALYSIS
    // =========================================================================
    
    /**
     * Finds processes ready to start.
     */
    List<ManufacturingProcess> findReadyProcesses(Project project);
    
    /**
     * Finds active processes (in progress).
     */
    List<ManufacturingProcess> findActiveProcesses(Project project);
    
    /**
     * Finds blocked processes.
     */
    List<ManufacturingProcess> findBlockedProcesses(Project project);
    
    /**
     * Finds processes needing attention (failed, on hold, blocked).
     */
    List<ManufacturingProcess> findProcessesNeedingAttention(Project project);
    
    /**
     * Finds completed processes.
     */
    List<ManufacturingProcess> findCompletedProcesses(Project project);
    
    /**
     * Finds overdue processes.
     */
    List<ManufacturingProcess> findOverdueProcesses(Project project);
    
    /**
     * Finds processes approaching deadline.
     */
    List<ManufacturingProcess> findProcessesApproachingDeadline(Project project, int daysAhead);
    
    /**
     * Finds critical processes (high priority and active status).
     */
    List<ManufacturingProcess> findCriticalProcesses(Project project);
    
    /**
     * Finds processes at risk (overdue or approaching deadline with issues).
     */
    List<ManufacturingProcess> findAtRiskProcesses(Project project);
    
    // =========================================================================
    // QUALITY MANAGEMENT
    // =========================================================================
    
    /**
     * Finds processes requiring quality inspection.
     */
    List<ManufacturingProcess> findProcessesRequiringQualityInspection(Project project);
    
    /**
     * Finds processes with quality issues.
     */
    List<ManufacturingProcess> findProcessesWithQualityIssues(Project project);
    
    /**
     * Finds processes requiring approval.
     */
    List<ManufacturingProcess> findProcessesRequiringApproval(Project project);
    
    /**
     * Finds approved processes.
     */
    List<ManufacturingProcess> findApprovedProcesses(Project project);
    
    /**
     * Records quality issues for a process.
     */
    ManufacturingProcess recordQualityIssues(Long processId, String qualityIssues, String correctiveActions);
    
    /**
     * Resolves quality issues for a process.
     */
    ManufacturingProcess resolveQualityIssues(Long processId, String resolutionNotes);
    
    /**
     * Approves a manufacturing process.
     */
    ManufacturingProcess approveProcess(Long processId, TeamMember approvedBy, String approvalNotes);
    
    /**
     * Rejects a manufacturing process approval.
     */
    ManufacturingProcess rejectProcess(Long processId, TeamMember rejectedBy, String rejectionReason);
    
    // =========================================================================
    // TIME AND RESOURCE MANAGEMENT
    // =========================================================================
    
    /**
     * Updates estimated hours for a process.
     */
    ManufacturingProcess updateEstimatedHours(Long processId, Double estimatedHours);
    
    /**
     * Records actual hours spent on a process.
     */
    ManufacturingProcess recordActualHours(Long processId, Double actualHours);
    
    /**
     * Updates target completion date.
     */
    ManufacturingProcess updateTargetCompletionDate(Long processId, LocalDateTime targetDate);
    
    /**
     * Updates process requirements.
     */
    ManufacturingProcess updateRequirements(Long processId, String requirements);
    
    /**
     * Updates materials required.
     */
    ManufacturingProcess updateMaterialsRequired(Long processId, String materials);
    
    /**
     * Updates tools required.
     */
    ManufacturingProcess updateToolsRequired(Long processId, String tools);
    
    /**
     * Updates safety considerations.
     */
    ManufacturingProcess updateSafetyConsiderations(Long processId, String safetyNotes);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches processes by name.
     */
    List<ManufacturingProcess> searchByName(String searchTerm);
    
    /**
     * Searches processes by description.
     */
    List<ManufacturingProcess> searchByDescription(String searchTerm);
    
    /**
     * Searches processes by requirements.
     */
    List<ManufacturingProcess> searchByRequirements(String searchTerm);
    
    /**
     * Searches processes by materials.
     */
    List<ManufacturingProcess> searchByMaterials(String searchTerm);
    
    /**
     * Searches processes by tools.
     */
    List<ManufacturingProcess> searchByTools(String searchTerm);
    
    /**
     * Advanced search with multiple criteria.
     */
    List<ManufacturingProcess> advancedSearch(Map<String, Object> searchCriteria);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets manufacturing metrics for a project.
     */
    Map<String, Object> getProjectManufacturingMetrics(Project project);
    
    /**
     * Gets capacity analysis by process type for a project.
     */
    Map<ManufacturingProcess.ProcessType, Object> getCapacityAnalysisByType(Project project);
    
    /**
     * Gets manufacturing workload for a team member.
     */
    List<ManufacturingProcess> getWorkloadForTeamMember(TeamMember teamMember);
    
    /**
     * Gets process performance statistics.
     */
    Map<String, Object> getProcessPerformanceStatistics(Project project);
    
    /**
     * Gets quality statistics for a project.
     */
    Map<String, Object> getQualityStatistics(Project project);
    
    /**
     * Gets timeline analysis for manufacturing processes.
     */
    Map<String, Object> getTimelineAnalysis(Project project);
    
    /**
     * Gets resource utilization analysis.
     */
    Map<String, Object> getResourceUtilizationAnalysis(Project project);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Creates multiple manufacturing processes from templates.
     */
    List<ManufacturingProcess> createBulkProcesses(List<ManufacturingProcess> processes);
    
    /**
     * Updates status for multiple processes.
     */
    int updateBulkStatus(List<Long> processIds, ManufacturingProcess.ProcessStatus newStatus);
    
    /**
     * Assigns multiple processes to a team member.
     */
    int assignBulkProcesses(List<Long> processIds, TeamMember assignedTo);
    
    /**
     * Updates priority for multiple processes.
     */
    int updateBulkPriority(List<Long> processIds, ManufacturingProcess.ProcessPriority priority);
    
    /**
     * Deactivates multiple processes.
     */
    int deactivateBulkProcesses(List<Long> processIds);
    
    /**
     * Reactivates multiple processes.
     */
    int reactivateBulkProcesses(List<Long> processIds);
    
    // =========================================================================
    // INTEGRATION AND DEPENDENCIES
    // =========================================================================
    
    /**
     * Finds processes dependent on a specific task.
     */
    List<ManufacturingProcess> findProcessesDependentOnTask(Task task);
    
    /**
     * Finds processes blocking task completion.
     */
    List<ManufacturingProcess> findProcessesBlockingTaskCompletion(Task task);
    
    /**
     * Updates process dependencies when tasks change.
     */
    void updateProcessDependencies(Task task);
    
    /**
     * Validates process workflow consistency.
     */
    Map<String, Object> validateProcessWorkflow(Project project);
    
    /**
     * Optimizes manufacturing workflow.
     */
    Map<String, Object> optimizeManufacturingWorkflow(Project project);
    
    // =========================================================================
    // TEMPLATE AND STANDARDIZATION
    // =========================================================================
    
    /**
     * Creates a process from a template.
     */
    ManufacturingProcess createFromTemplate(String templateName, Project project, Task task);
    
    /**
     * Saves a process as a template.
     */
    void saveAsTemplate(Long processId, String templateName);
    
    /**
     * Gets available process templates.
     */
    List<String> getAvailableTemplates();
    
    /**
     * Applies standard process template to a project.
     */
    List<ManufacturingProcess> applyStandardProcesses(Project project, List<String> processTypes);
}