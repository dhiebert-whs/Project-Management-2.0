// src/main/java/org/frcpm/repositories/spring/ManufacturingProcessRepository.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.repositories.spring;

import org.frcpm.models.ManufacturingProcess;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ManufacturingProcess entities.
 * Provides manufacturing workflow tracking and process management functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Repository
public interface ManufacturingProcessRepository extends JpaRepository<ManufacturingProcess, Long> {
    
    // Project-based Queries
    
    /**
     * Finds all processes for a specific project.
     */
    List<ManufacturingProcess> findByProjectAndIsActiveTrueOrderByPriorityAscNameAsc(Project project);
    
    /**
     * Finds all processes for a project including inactive ones.
     */
    List<ManufacturingProcess> findByProjectOrderByPriorityAscNameAsc(Project project);
    
    /**
     * Counts total processes for a project.
     */
    long countByProjectAndIsActiveTrue(Project project);
    
    // Task-based Queries
    
    /**
     * Finds all processes for a specific task.
     */
    List<ManufacturingProcess> findByTaskAndIsActiveTrueOrderByPriorityAscNameAsc(Task task);
    
    /**
     * Finds process for a task and process type.
     */
    Optional<ManufacturingProcess> findByTaskAndProcessTypeAndIsActiveTrue(
            Task task, ManufacturingProcess.ProcessType processType);
    
    // Status-based Queries
    
    /**
     * Finds processes by status.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByPriorityAscCreatedAtAsc(
            ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds processes by multiple statuses.
     */
    List<ManufacturingProcess> findByStatusInAndIsActiveTrueOrderByPriorityAscCreatedAtAsc(
            List<ManufacturingProcess.ProcessStatus> statuses);
    
    /**
     * Finds active processes (in progress).
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByActualStartTimeAsc(
            ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds completed processes.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByActualEndTimeDesc(
            ManufacturingProcess.ProcessStatus status);
    
    // Process Type Queries
    
    /**
     * Finds processes by process type.
     */
    List<ManufacturingProcess> findByProcessTypeAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess.ProcessType processType);
    
    /**
     * Finds processes by multiple process types.
     */
    List<ManufacturingProcess> findByProcessTypeInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<ManufacturingProcess.ProcessType> processTypes);
    
    /**
     * Counts processes by type for a project.
     */
    long countByProjectAndProcessTypeAndIsActiveTrue(Project project, ManufacturingProcess.ProcessType processType);
    
    // Priority and Complexity Queries
    
    /**
     * Finds processes by priority level.
     */
    List<ManufacturingProcess> findByPriorityAndIsActiveTrueOrderByNameAsc(
            ManufacturingProcess.ProcessPriority priority);
    
    /**
     * Finds high-priority processes.
     */
    List<ManufacturingProcess> findByPriorityInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<ManufacturingProcess.ProcessPriority> priorities);
    
    /**
     * Finds processes by estimated hours range.
     */
    List<ManufacturingProcess> findByEstimatedHoursBetweenAndIsActiveTrueOrderByPriorityAscNameAsc(
            Double minHours, Double maxHours);
    
    // Team Member Assignment Queries
    
    /**
     * Finds processes assigned to a team member.
     */
    List<ManufacturingProcess> findByAssignedToAndIsActiveTrueOrderByPriorityAscActualStartTimeAsc(TeamMember assignedTo);
    
    /**
     * Finds processes assigned to a team member with specific status.
     */
    List<ManufacturingProcess> findByAssignedToAndStatusAndIsActiveTrueOrderByPriorityAscActualStartTimeAsc(
            TeamMember assignedTo, ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds processes created by a team member.
     */
    List<ManufacturingProcess> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(TeamMember createdBy);
    
    // Time-based Queries
    
    /**
     * Finds processes started within a date range.
     */
    List<ManufacturingProcess> findByActualStartTimeBetweenAndIsActiveTrueOrderByActualStartTimeAsc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds processes completed within a date range.
     */
    List<ManufacturingProcess> findByActualEndTimeBetweenAndIsActiveTrueOrderByActualEndTimeDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds processes with target completion dates approaching.
     */
    List<ManufacturingProcess> findByTargetCompletionDateBetweenAndIsActiveTrueOrderByTargetCompletionDateAsc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds overdue processes.
     */
    List<ManufacturingProcess> findByTargetCompletionDateBeforeAndStatusNotAndIsActiveTrueOrderByTargetCompletionDateAsc(
            LocalDateTime currentDate, ManufacturingProcess.ProcessStatus excludeStatus);
    
    // Quality and Approval Queries
    
    /**
     * Finds processes requiring quality inspection.
     */
    List<ManufacturingProcess> findByRequiresQualityInspectionTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    
    /**
     * Finds processes requiring approval.
     */
    List<ManufacturingProcess> findByRequiresApprovalTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    
    /**
     * Finds processes with quality issues.
     */
    List<ManufacturingProcess> findByHasQualityIssuesTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    
    /**
     * Finds approved processes.
     */
    List<ManufacturingProcess> findByApprovedByNotNullAndIsActiveTrueOrderByUpdatedAtDesc();
    
    // Search and Filter Queries
    
    /**
     * Finds processes by name containing search term.
     */
    List<ManufacturingProcess> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds processes by description containing search term.
     */
    List<ManufacturingProcess> findByDescriptionContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds processes by requirements containing search term.
     */
    List<ManufacturingProcess> findByRequirementsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds processes by materials containing search term.
     */
    List<ManufacturingProcess> findByMaterialsRequiredContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds processes by tools containing search term.
     */
    List<ManufacturingProcess> findByRequiredToolsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    // Statistics and Analytics Queries
    
    /**
     * Counts processes by status for a project.
     */
    long countByProjectAndStatusAndIsActiveTrue(Project project, ManufacturingProcess.ProcessStatus status);
    
    /**
     * Counts processes by priority for a project.
     */
    long countByProjectAndPriorityAndIsActiveTrue(Project project, ManufacturingProcess.ProcessPriority priority);
    
    /**
     * Counts processes requiring quality inspection for a project.
     */
    long countByProjectAndRequiresQualityInspectionTrueAndIsActiveTrue(Project project);
    
    /**
     * Counts processes with quality issues for a project.
     */
    long countByProjectAndHasQualityIssuesTrueAndIsActiveTrue(Project project);
    
    // Workflow State Queries
    
    /**
     * Finds processes that can be started (status READY or PLANNED).
     */
    List<ManufacturingProcess> findByStatusInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<ManufacturingProcess.ProcessStatus> readyStatuses);
    
    /**
     * Finds blocked processes.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess.ProcessStatus blockedStatus);
    
    /**
     * Finds processes needing attention (failed, on hold, blocked).
     */
    List<ManufacturingProcess> findByStatusInAndIsActiveTrueOrderByPriorityAscUpdatedAtAsc(
            List<ManufacturingProcess.ProcessStatus> attentionStatuses);
    
    // Complex Queries with Multiple Conditions
    
    /**
     * Finds critical processes for a project (high priority and active status).
     */
    @Query("SELECT mp FROM ManufacturingProcess mp WHERE mp.project = :project " +
           "AND mp.priority IN ('CRITICAL', 'HIGH') " +
           "AND mp.status IN ('IN_PROGRESS', 'READY', 'PLANNED') " +
           "AND mp.isActive = true " +
           "ORDER BY mp.priority ASC, mp.targetCompletionDate ASC")
    List<ManufacturingProcess> findCriticalProcessesForProject(@Param("project") Project project);
    
    /**
     * Finds processes at risk (overdue or approaching deadline with issues).
     */
    @Query("SELECT mp FROM ManufacturingProcess mp WHERE mp.project = :project " +
           "AND mp.isActive = true " +
           "AND (mp.targetCompletionDate < :currentDate " +
           "     OR (mp.targetCompletionDate < :warningDate AND mp.hasQualityIssues = true)) " +
           "ORDER BY mp.targetCompletionDate ASC, mp.priority ASC")
    List<ManufacturingProcess> findAtRiskProcesses(@Param("project") Project project, 
                                                  @Param("currentDate") LocalDateTime currentDate,
                                                  @Param("warningDate") LocalDateTime warningDate);
    
    /**
     * Finds manufacturing workload for a team member.
     */
    @Query("SELECT mp FROM ManufacturingProcess mp WHERE mp.assignedTo = :teamMember " +
           "AND mp.status IN ('IN_PROGRESS', 'READY', 'PLANNED') " +
           "AND mp.isActive = true " +
           "ORDER BY mp.priority ASC, mp.targetCompletionDate ASC")
    List<ManufacturingProcess> findWorkloadForTeamMember(@Param("teamMember") TeamMember teamMember);
    
    /**
     * Finds manufacturing capacity analysis for a project.
     */
    @Query("SELECT mp.processType, COUNT(mp), AVG(mp.estimatedHours) FROM ManufacturingProcess mp " +
           "WHERE mp.project = :project AND mp.isActive = true " +
           "GROUP BY mp.processType " +
           "ORDER BY COUNT(mp) DESC")
    List<Object[]> findCapacityAnalysisByType(@Param("project") Project project);
    
    // Advanced Analytics
    
    /**
     * Finds manufacturing process metrics for a project.
     */
    @Query("SELECT " +
           "COUNT(mp) as totalProcesses, " +
           "SUM(CASE WHEN mp.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedProcesses, " +
           "SUM(CASE WHEN mp.hasQualityIssues = true THEN 1 ELSE 0 END) as processesWithIssues, " +
           "AVG(mp.estimatedHours) as avgEstimatedHours, " +
           "AVG(mp.actualHours) as avgActualHours " +
           "FROM ManufacturingProcess mp " +
           "WHERE mp.project = :project AND mp.isActive = true")
    Object[] findProjectManufacturingMetrics(@Param("project") Project project);
    
    // Active Status Management
    
    /**
     * Finds all inactive processes.
     */
    List<ManufacturingProcess> findByIsActiveFalseOrderByNameAsc();
    
    /**
     * Checks if any processes exist for a task.
     */
    boolean existsByTaskAndIsActiveTrue(Task task);
    
    /**
     * Checks if any processes exist for a project.
     */
    boolean existsByProjectAndIsActiveTrue(Project project);
    
    // Additional methods for ManufacturingProcessServiceImpl
    
    /**
     * Finds processes by status ordered by priority and started time.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByPriorityAscStartedAtAsc(ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds processes by assigned team member ordered by priority and started time.
     */
    List<ManufacturingProcess> findByAssignedToAndIsActiveTrueOrderByPriorityAscStartedAtAsc(TeamMember assignedTo);
    
    /**
     * Finds processes by status ordered by started time.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByStartedAtAsc(ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds processes by status ordered by completed time descending.
     */
    List<ManufacturingProcess> findByStatusAndIsActiveTrueOrderByCompletedAtDesc(ManufacturingProcess.ProcessStatus status);
    
    /**
     * Finds approved processes ordered by approval time descending.
     */
    List<ManufacturingProcess> findByIsApprovedTrueAndIsActiveTrueOrderByApprovedAtDesc();
    
    /**
     * Finds processes by tools required containing search term.
     */
    List<ManufacturingProcess> findByToolsRequiredContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
}