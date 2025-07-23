// src/main/java/org/frcpm/repositories/spring/QualityCheckpointRepository.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.repositories.spring;

import org.frcpm.models.ManufacturingProcess;
import org.frcpm.models.QualityCheckpoint;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for QualityCheckpoint entities.
 * Provides quality control and inspection tracking functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Repository
public interface QualityCheckpointRepository extends JpaRepository<QualityCheckpoint, Long> {
    
    // Process-based Queries
    
    /**
     * Finds all checkpoints for a manufacturing process.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds all checkpoints for a manufacturing process including inactive ones.
     */
    List<QualityCheckpoint> findByManufacturingProcessOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Counts total checkpoints for a manufacturing process.
     */
    long countByManufacturingProcessAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    // Status-based Queries
    
    /**
     * Finds checkpoints by status.
     */
    List<QualityCheckpoint> findByStatusAndIsActiveTrueOrderByPriorityAscNameAsc(
            QualityCheckpoint.CheckpointStatus status);
    
    /**
     * Finds checkpoints by multiple statuses.
     */
    List<QualityCheckpoint> findByStatusInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<QualityCheckpoint.CheckpointStatus> statuses);
    
    /**
     * Finds checkpoints for a process by status.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndStatusAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointStatus status);
    
    /**
     * Finds checkpoints for a process by multiple statuses.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndStatusInAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess, List<QualityCheckpoint.CheckpointStatus> statuses);
    
    // Checkpoint Type Queries
    
    /**
     * Finds checkpoints by type.
     */
    List<QualityCheckpoint> findByCheckpointTypeAndIsActiveTrueOrderByPriorityAscNameAsc(
            QualityCheckpoint.CheckpointType checkpointType);
    
    /**
     * Finds checkpoints by multiple types.
     */
    List<QualityCheckpoint> findByCheckpointTypeInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<QualityCheckpoint.CheckpointType> checkpointTypes);
    
    /**
     * Finds checkpoints for a process by type.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndCheckpointTypeAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointType checkpointType);
    
    // Priority Queries
    
    /**
     * Finds checkpoints by priority level.
     */
    List<QualityCheckpoint> findByPriorityAndIsActiveTrueOrderByNameAsc(
            QualityCheckpoint.CheckpointPriority priority);
    
    /**
     * Finds high-priority checkpoints.
     */
    List<QualityCheckpoint> findByPriorityInAndIsActiveTrueOrderByPriorityAscNameAsc(
            List<QualityCheckpoint.CheckpointPriority> priorities);
    
    /**
     * Finds critical checkpoints for a process.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndPriorityAndIsActiveTrueOrderByNameAsc(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointPriority priority);
    
    // Result-based Queries
    
    /**
     * Finds checkpoints by result.
     */
    List<QualityCheckpoint> findByResultAndIsActiveTrueOrderByInspectedAtDesc(
            QualityCheckpoint.CheckpointResult result);
    
    /**
     * Finds checkpoints with failing results.
     */
    List<QualityCheckpoint> findByResultInAndIsActiveTrueOrderByInspectedAtDesc(
            List<QualityCheckpoint.CheckpointResult> failingResults);
    
    /**
     * Finds checkpoints with passing results.
     */
    List<QualityCheckpoint> findByResultInAndIsActiveTrueOrderByInspectedAtAsc(
            List<QualityCheckpoint.CheckpointResult> passingResults);
    
    /**
     * Finds checkpoints requiring action (failed, needs rework, inconclusive).
     */
    @Query("SELECT qc FROM QualityCheckpoint qc WHERE qc.isActive = true " +
           "AND qc.result IN ('FAIL', 'NEEDS_REWORK', 'INCONCLUSIVE') " +
           "ORDER BY qc.priority ASC, qc.inspectedAt ASC")
    List<QualityCheckpoint> findCheckpointsRequiringAction();
    
    // Inspector and Team Member Queries
    
    /**
     * Finds checkpoints inspected by a team member.
     */
    List<QualityCheckpoint> findByInspectorAndIsActiveTrueOrderByInspectedAtDesc(TeamMember inspector);
    
    /**
     * Finds checkpoints assigned to an inspector with specific status.
     */
    List<QualityCheckpoint> findByInspectorAndStatusAndIsActiveTrueOrderByPriorityAscNameAsc(
            TeamMember inspector, QualityCheckpoint.CheckpointStatus status);
    
    /**
     * Finds pending checkpoints for an inspector.
     */
    List<QualityCheckpoint> findByInspectorAndStatusInAndIsActiveTrueOrderByPriorityAscNameAsc(
            TeamMember inspector, List<QualityCheckpoint.CheckpointStatus> pendingStatuses);
    
    // Time-based Queries
    
    /**
     * Finds checkpoints inspected within a date range.
     */
    List<QualityCheckpoint> findByInspectedAtBetweenAndIsActiveTrueOrderByInspectedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds checkpoints created within a date range.
     */
    List<QualityCheckpoint> findByCreatedAtBetweenAndIsActiveTrueOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds recent checkpoints requiring attention.
     */
    List<QualityCheckpoint> findByStatusInAndCreatedAtAfterAndIsActiveTrueOrderByPriorityAscCreatedAtAsc(
            List<QualityCheckpoint.CheckpointStatus> attentionStatuses, LocalDateTime since);
    
    // Mandatory and Blocking Queries
    
    /**
     * Finds mandatory checkpoints.
     */
    List<QualityCheckpoint> findByIsMandatoryTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    
    /**
     * Finds blocking checkpoints.
     */
    List<QualityCheckpoint> findByIsBlockingTrueAndIsActiveTrueOrderByPriorityAscNameAsc();
    
    /**
     * Finds mandatory checkpoints for a process.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndIsMandatoryTrueAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds blocking checkpoints for a process.
     */
    List<QualityCheckpoint> findByManufacturingProcessAndIsBlockingTrueAndIsActiveTrueOrderByPriorityAscNameAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds checkpoints blocking process completion.
     */
    @Query("SELECT qc FROM QualityCheckpoint qc WHERE qc.manufacturingProcess = :process " +
           "AND qc.isBlocking = true AND qc.isActive = true " +
           "AND (qc.isMandatory = true AND qc.status != 'COMPLETED' " +
           "     OR (qc.status = 'COMPLETED' AND qc.result IN ('FAIL', 'NEEDS_REWORK', 'INCONCLUSIVE'))) " +
           "ORDER BY qc.priority ASC, qc.name ASC")
    List<QualityCheckpoint> findCheckpointsBlockingCompletion(@Param("process") ManufacturingProcess process);
    
    // Search and Filter Queries
    
    /**
     * Finds checkpoints by name containing search term.
     */
    List<QualityCheckpoint> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds checkpoints by description containing search term.
     */
    List<QualityCheckpoint> findByDescriptionContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds checkpoints by acceptance criteria containing search term.
     */
    List<QualityCheckpoint> findByAcceptanceCriteriaContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds checkpoints by inspection notes containing search term.
     */
    List<QualityCheckpoint> findByInspectionNotesContainingIgnoreCaseAndIsActiveTrueOrderByInspectedAtDesc(String searchTerm);
    
    /**
     * Finds checkpoints by issues found containing search term.
     */
    List<QualityCheckpoint> findByIssuesFoundContainingIgnoreCaseAndIsActiveTrueOrderByInspectedAtDesc(String searchTerm);
    
    /**
     * Finds checkpoints by corrective actions containing search term.
     */
    List<QualityCheckpoint> findByCorrectiveActionsContainingIgnoreCaseAndIsActiveTrueOrderByInspectedAtDesc(String searchTerm);
    
    /**
     * Finds checkpoints by required tools containing search term.
     */
    List<QualityCheckpoint> findByRequiredToolsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    /**
     * Finds checkpoints by tolerance specs containing search term.
     */
    List<QualityCheckpoint> findByToleranceSpecsContainingIgnoreCaseAndIsActiveTrueOrderByPriorityAscNameAsc(String searchTerm);
    
    // Statistical Queries
    
    /**
     * Counts checkpoints by status for a manufacturing process.
     */
    long countByManufacturingProcessAndStatusAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointStatus status);
    
    /**
     * Counts checkpoints by priority for a manufacturing process.
     */
    long countByManufacturingProcessAndPriorityAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointPriority priority);
    
    /**
     * Counts checkpoints by type for a manufacturing process.
     */
    long countByManufacturingProcessAndCheckpointTypeAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointType checkpointType);
    
    /**
     * Counts checkpoints by result for a manufacturing process.
     */
    long countByManufacturingProcessAndResultAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, QualityCheckpoint.CheckpointResult result);
    
    /**
     * Counts mandatory checkpoints for a manufacturing process.
     */
    long countByManufacturingProcessAndIsMandatoryTrueAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    /**
     * Counts blocking checkpoints for a manufacturing process.
     */
    long countByManufacturingProcessAndIsBlockingTrueAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    // Workflow State Queries
    
    
    /**
     * Finds checkpoints in progress.
     */
    List<QualityCheckpoint> findByStatusAndIsActiveTrueOrderByCreatedAtAsc(
            QualityCheckpoint.CheckpointStatus inProgressStatus);
    
    /**
     * Finds deferred checkpoints.
     */
    List<QualityCheckpoint> findByStatusAndIsActiveTrueOrderByUpdatedAtDesc(
            QualityCheckpoint.CheckpointStatus deferredStatus);
    
    // Note: Skipped checkpoints query removed - duplicate method signature
    
    // Complex Analytics Queries
    
    /**
     * Finds quality metrics for a manufacturing process.
     */
    @Query("SELECT " +
           "COUNT(qc) as totalCheckpoints, " +
           "SUM(CASE WHEN qc.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedCheckpoints, " +
           "SUM(CASE WHEN qc.result = 'PASS' THEN 1 ELSE 0 END) as passedCheckpoints, " +
           "SUM(CASE WHEN qc.result = 'FAIL' THEN 1 ELSE 0 END) as failedCheckpoints, " +
           "SUM(CASE WHEN qc.result = 'NEEDS_REWORK' THEN 1 ELSE 0 END) as checkpointsNeedingRework, " +
           "SUM(CASE WHEN qc.isMandatory = true THEN 1 ELSE 0 END) as mandatoryCheckpoints, " +
           "SUM(CASE WHEN qc.isBlocking = true THEN 1 ELSE 0 END) as blockingCheckpoints " +
           "FROM QualityCheckpoint qc " +
           "WHERE qc.manufacturingProcess = :process AND qc.isActive = true")
    Object[] findProcessQualityMetrics(@Param("process") ManufacturingProcess process);
    
    /**
     * Finds quality metrics by checkpoint type for a process.
     */
    @Query("SELECT qc.checkpointType, COUNT(qc), " +
           "SUM(CASE WHEN qc.result = 'PASS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN qc.result = 'FAIL' THEN 1 ELSE 0 END) " +
           "FROM QualityCheckpoint qc " +
           "WHERE qc.manufacturingProcess = :process AND qc.isActive = true " +
           "GROUP BY qc.checkpointType " +
           "ORDER BY COUNT(qc) DESC")
    List<Object[]> findQualityMetricsByType(@Param("process") ManufacturingProcess process);
    
    /**
     * Finds inspector performance metrics.
     */
    @Query("SELECT qc.inspector, COUNT(qc), " +
           "SUM(CASE WHEN qc.result = 'PASS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN qc.result = 'FAIL' THEN 1 ELSE 0 END), " +
           "AVG(qc.inspectedAt - qc.createdAt) " +
           "FROM QualityCheckpoint qc " +
           "WHERE qc.inspector IS NOT NULL AND qc.isActive = true " +
           "AND qc.inspectedAt IS NOT NULL " +
           "GROUP BY qc.inspector " +
           "ORDER BY COUNT(qc) DESC")
    List<Object[]> findInspectorPerformanceMetrics();
    
    /**
     * Finds quality trends over time.
     */
    @Query("SELECT DATE(qc.inspectedAt), " +
           "COUNT(qc), " +
           "SUM(CASE WHEN qc.result = 'PASS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN qc.result = 'FAIL' THEN 1 ELSE 0 END) " +
           "FROM QualityCheckpoint qc " +
           "WHERE qc.inspectedAt BETWEEN :startDate AND :endDate " +
           "AND qc.isActive = true " +
           "GROUP BY DATE(qc.inspectedAt) " +
           "ORDER BY DATE(qc.inspectedAt) ASC")
    List<Object[]> findQualityTrends(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    // Active Status Management
    
    /**
     * Finds all inactive checkpoints.
     */
    List<QualityCheckpoint> findByIsActiveFalseOrderByNameAsc();
    
    /**
     * Checks if any checkpoints exist for a manufacturing process.
     */
    boolean existsByManufacturingProcessAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds checkpoints with issues recorded.
     */
    List<QualityCheckpoint> findByIssuesFoundIsNotNullAndIsActiveTrueOrderByInspectedAtDesc();
    
    /**
     * Finds checkpoints with corrective actions.
     */
    List<QualityCheckpoint> findByCorrectiveActionsIsNotNullAndIsActiveTrueOrderByInspectedAtDesc();
}